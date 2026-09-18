import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  UserService
} from '../../../core/services/user';

import {
  AuthService
} from '../../../core/services/auth';

import {
  User,
  UserRole
} from '../../../shared/models/user.model';

import {
  TableModule
} from 'primeng/table';

import {
  ButtonModule
} from 'primeng/button';

import {
  DialogModule
} from 'primeng/dialog';

import {
  InputTextModule
} from 'primeng/inputtext';

import {
  PasswordModule
} from 'primeng/password';

import {
  TagModule
} from 'primeng/tag';

import {
  ToastModule
} from 'primeng/toast';

import {
  ConfirmDialogModule
} from 'primeng/confirmdialog';

import {
  TooltipModule
} from 'primeng/tooltip';

import {
  ConfirmationService,
  MessageService
} from 'primeng/api';

@Component({
  selector:
    'app-user-list',

  standalone: true,

  imports: [
    CommonModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    PasswordModule,
    TagModule,
    ToastModule,
    ConfirmDialogModule,
    TooltipModule
  ],

  providers: [
    ConfirmationService,
    MessageService
  ],

  templateUrl:
    './user-list.html',

  styleUrls: [
    './user-list.scss'
  ]
})
export class UserListComponent
  implements OnInit {

  private userService =
    inject(UserService);

  private authService =
    inject(AuthService);

  private fb =
    inject(FormBuilder);

  private router =
    inject(Router);

  private confirmationService =
    inject(
      ConfirmationService
    );

  private messageService =
    inject(MessageService);

  users: User[] = [];

  loading = false;

  submitting = false;

  dialogVisible = false;

  isEdit = false;

  selectedUserId:
    number | null = null;

  currentUsername =
    this.authService
      .getUsername();

  roles: UserRole[] = [
    'ADMIN',
    'USER'
  ];

  form =
    this.fb.nonNullable.group({

      username: [
        '',
        [
          Validators.required,
          Validators.minLength(3)
        ]
      ],

      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],

      password: [
        ''
      ],

      role: [
        'USER' as UserRole,
        Validators.required
      ],

      enabled: [
        true
      ],

      twoFactorEnabled: [
        false
      ]

    });

  ngOnInit(): void {

    this.loadUsers();
  }

  loadUsers(): void {

    this.loading = true;

    this.userService
      .getAll()
      .subscribe({

        next: users => {

          this.users =
            users;

          this.loading =
            false;
        },

        error: () => {

          this.messageService
            .add({
              severity: 'error',
              summary: 'Error',
              detail:
                'No se pudieron cargar los usuarios'
            });

          this.loading =
            false;
        }

      });
  }

  openCreate(): void {

    this.isEdit = false;

    this.selectedUserId =
      null;

    const passwordControl =
      this.form.controls
        .password;

    passwordControl
      .setValidators([
        Validators.required,
        Validators.minLength(8)
      ]);

    passwordControl
      .updateValueAndValidity();

    this.form.reset({

      username: '',

      email: '',

      password: '',

      role: 'USER',

      enabled: true,

      twoFactorEnabled: false

    });

    this.dialogVisible =
      true;
  }

  openEdit(
    user: User
  ): void {

    this.isEdit = true;

    this.selectedUserId =
      user.id;

    const passwordControl =
      this.form.controls
        .password;

    passwordControl
      .clearValidators();

    passwordControl
      .setValidators(
        Validators.minLength(8)
      );

    passwordControl
      .updateValueAndValidity();

    this.form.reset({

      username:
        user.username,

      email:
        user.email ?? '',

      password: '',

      role:
        user.role,

      enabled:
        user.enabled,

      twoFactorEnabled:
        user.twoFactorEnabled

    });

    this.dialogVisible =
      true;
  }

  save(): void {

    if (
      this.form.invalid
    ) {

      this.form
        .markAllAsTouched();

      return;
    }

    this.submitting =
      true;

    const value =
      this.form
        .getRawValue();

    const request$ =
      this.isEdit &&
        this.selectedUserId

        ? this.userService
          .update(
            this.selectedUserId,
            {
              username:
                value.username,

              email:
                value.email,

              password:
                value.password,

              role:
                value.role,

              enabled:
                value.enabled,

              twoFactorEnabled:
                value.twoFactorEnabled
            }
          )

        : this.userService
          .create({
            username:
              value.username,

            email:
              value.email,

            password:
              value.password,

            role:
              value.role,

            twoFactorEnabled:
              value.twoFactorEnabled
          });

    request$
      .subscribe({

        next: () => {

          this.messageService
            .add({

              severity:
                'success',

              summary:
                'Éxito',

              detail:
                this.isEdit
                  ? 'Usuario actualizado'
                  : 'Usuario creado correctamente'

            });

          this.dialogVisible =
            false;

          this.submitting =
            false;

          this.loadUsers();

        },

        error: err => {

          this.messageService
            .add({

              severity:
                'error',

              summary:
                'Error',

              detail:
                err.error?.error ??
                err.error?.message ??
                'No se pudo guardar el usuario'

            });

          this.submitting =
            false;

        }

      });
  }

  confirmDelete(
    user: User
  ): void {

    if (
      user.username ===
      this.currentUsername
    ) {

      this.messageService
        .add({

          severity: 'warn',

          summary:
            'Operación no permitida',

          detail:
            'No puedes eliminar tu propio usuario'

        });

      return;
    }

    this.confirmationService
      .confirm({

        header:
          'Eliminar usuario',

        message:
          `¿Eliminar al usuario "${user.username}"?`,

        icon:
          'pi pi-exclamation-triangle',

        acceptLabel:
          'Sí, eliminar',

        rejectLabel:
          'Cancelar',

        acceptButtonStyleClass:
          'p-button-danger',

        accept: () =>
          this.deleteUser(
            user.id
          )

      });
  }

  private deleteUser(
    id: number
  ): void {

    this.userService
      .delete(id)
      .subscribe({

        next: () => {

          this.messageService
            .add({

              severity:
                'success',

              summary:
                'Éxito',

              detail:
                'Usuario eliminado'

            });

          this.loadUsers();
        },

        error: err => {

          this.messageService
            .add({

              severity:
                'error',

              summary:
                'Error',

              detail:
                err.error?.error ??
                'No se pudo eliminar el usuario'

            });

        }

      });
  }

  roleSeverity(
    role: UserRole
  ):
    'success' |
    'info' {

    return role === 'ADMIN'
      ? 'success'
      : 'info';
  }

  goToProducts(): void {

    this.router.navigate([
      '/products'
    ]);
  }
}