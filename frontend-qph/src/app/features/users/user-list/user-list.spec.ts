import {
  TestBed
} from '@angular/core/testing';

import {
  Router
} from '@angular/router';

import {
  ConfirmationService,
  MessageService
} from 'primeng/api';

import {
  of,
  throwError
} from 'rxjs';

import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi
} from 'vitest';

import {
  AuthService
} from '../../../core/services/auth';

import {
  UserService
} from '../../../core/services/user';

import {
  User
} from '../../../shared/models/user.model';

import {
  UserListComponent
} from './user-list';


describe(
  'UserListComponent',
  () => {

    const userServiceMock = {
      getAll:
        vi.fn(),
      create:
        vi.fn(),
      update:
        vi.fn(),
      delete:
        vi.fn()
    };

    const authServiceMock = {
      getUsername:
        vi.fn()
    };

    const routerMock = {
      navigate:
        vi.fn()
    };

    const confirmationServiceMock = {
      confirm:
        vi.fn()
    };

    const messageServiceMock = {
      add:
        vi.fn()
    };

    let component:
      UserListComponent;

    const user:
      User = {
        id: 2,
        username: 'maria',
        email:
          'maria@example.com',
        role: 'USER',
        enabled: true,
        twoFactorEnabled: false
      };


    beforeEach(
      async () => {

        userServiceMock
          .getAll
          .mockReset();

        userServiceMock
          .create
          .mockReset();

        userServiceMock
          .update
          .mockReset();

        userServiceMock
          .delete
          .mockReset();

        authServiceMock
          .getUsername
          .mockReset();

        routerMock
          .navigate
          .mockReset();

        confirmationServiceMock
          .confirm
          .mockReset();

        messageServiceMock
          .add
          .mockReset();

        userServiceMock
          .getAll
          .mockReturnValue(
            of([])
          );

        authServiceMock
          .getUsername
          .mockReturnValue(
            'admin'
          );

        await TestBed
          .configureTestingModule({
            imports: [
              UserListComponent
            ],
            providers: [
              {
                provide:
                  UserService,
                useValue:
                  userServiceMock
              },
              {
                provide:
                  AuthService,
                useValue:
                  authServiceMock
              },
              {
                provide:
                  Router,
                useValue:
                  routerMock
              }
            ]
          })
          .overrideComponent(
            UserListComponent,
            {
              set: {
                template: '',
                providers: [
                  {
                    provide:
                      ConfirmationService,
                    useValue:
                      confirmationServiceMock
                  },
                  {
                    provide:
                      MessageService,
                    useValue:
                      messageServiceMock
                  }
                ]
              }
            }
          )
          .compileComponents();

        component =
          TestBed
            .createComponent(
              UserListComponent
            )
            .componentInstance;
      }
    );


    afterEach(
      () => {

        TestBed.resetTestingModule();
      }
    );


    it(
      'should create',
      () => {

        expect(
          component
        ).toBeTruthy();

        expect(
          component.currentUsername
        ).toBe(
          'admin'
        );
      }
    );


    it(
      'should load users',
      () => {

        userServiceMock
          .getAll
          .mockReturnValue(
            of([
              user
            ])
          );

        component.loadUsers();

        expect(
          component.users
        ).toEqual([
          user
        ]);

        expect(
          component.loading
        ).toBe(
          false
        );
      }
    );


    it(
      'should show error when users cannot be loaded',
      () => {

        userServiceMock
          .getAll
          .mockReturnValue(
            throwError(
              () =>
                new Error(
                  'load failed'
                )
            )
          );

        component.loadUsers();

        expect(
          component.loading
        ).toBe(
          false
        );

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'error',
          summary: 'Error',
          detail:
            'No se pudieron cargar los usuarios'
        });
      }
    );


    it(
      'should prepare form for user creation',
      () => {

        component.openCreate();

        expect(
          component.isEdit
        ).toBe(
          false
        );

        expect(
          component.selectedUserId
        ).toBeNull();

        expect(
          component.dialogVisible
        ).toBe(
          true
        );

        expect(
          component.form
            .controls
            .password
            .hasError(
              'required'
            )
        ).toBe(
          true
        );
      }
    );


    it(
      'should prepare form for user editing',
      () => {

        component.openEdit(
          user
        );

        expect(
          component.isEdit
        ).toBe(
          true
        );

        expect(
          component.selectedUserId
        ).toBe(
          2
        );

        expect(
          component.form
            .getRawValue()
        ).toEqual({
          username: 'maria',
          email:
            'maria@example.com',
          password: '',
          role: 'USER',
          enabled: true,
          twoFactorEnabled: false
        });

        expect(
          component.form
            .controls
            .password
            .hasError(
              'required'
            )
        ).toBe(
          false
        );
      }
    );


    it(
      'should not save invalid form',
      () => {

        component.openCreate();

        component.save();

        expect(
          userServiceMock.create
        ).not
          .toHaveBeenCalled();

        expect(
          userServiceMock.update
        ).not
          .toHaveBeenCalled();
      }
    );


    it(
      'should create a user',
      () => {

        userServiceMock
          .create
          .mockReturnValue(
            of(user)
          );

        component.openCreate();

        component.form.setValue({
          username: 'maria',
          email:
            'maria@example.com',
          password:
            'Password123',
          role: 'USER',
          enabled: true,
          twoFactorEnabled: false
        });

        component.save();

        expect(
          userServiceMock.create
        ).toHaveBeenCalledWith({
          username: 'maria',
          email:
            'maria@example.com',
          password:
            'Password123',
          role: 'USER',
          twoFactorEnabled: false
        });

        expect(
          component.dialogVisible
        ).toBe(
          false
        );

        expect(
          component.submitting
        ).toBe(
          false
        );

        expect(
          userServiceMock.getAll
        ).toHaveBeenCalled();

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'success',
          summary: 'Éxito',
          detail:
            'Usuario creado correctamente'
        });
      }
    );


    it(
      'should update a user',
      () => {

        const updated:
          User = {
            ...user,
            role: 'ADMIN'
          };

        userServiceMock
          .update
          .mockReturnValue(
            of(updated)
          );

        component.openEdit(
          user
        );

        component.form.patchValue({
          role: 'ADMIN'
        });

        component.save();

        expect(
          userServiceMock.update
        ).toHaveBeenCalledWith(
          2,
          {
            username: 'maria',
            email:
              'maria@example.com',
            password: '',
            role: 'ADMIN',
            enabled: true,
            twoFactorEnabled: false
          }
        );

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'success',
          summary: 'Éxito',
          detail:
            'Usuario actualizado'
        });
      }
    );


    it(
      'should show error when user save fails',
      () => {

        userServiceMock
          .create
          .mockReturnValue(
            throwError(
              () => ({
                error: {
                  error:
                    'El email ya existe'
                }
              })
            )
          );

        component.openCreate();

        component.form.setValue({
          username: 'maria',
          email:
            'maria@example.com',
          password:
            'Password123',
          role: 'USER',
          enabled: true,
          twoFactorEnabled: false
        });

        component.save();

        expect(
          component.submitting
        ).toBe(
          false
        );

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'error',
          summary: 'Error',
          detail:
            'El email ya existe'
        });
      }
    );


    it(
      'should prevent deleting current user',
      () => {

        component.confirmDelete({
          id: 1,
          username: 'admin',
          email:
            'admin@example.com',
          role: 'ADMIN',
          enabled: true,
          twoFactorEnabled: true
        });

        expect(
          confirmationServiceMock.confirm
        ).not
          .toHaveBeenCalled();

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'warn',
          summary:
            'Operación no permitida',
          detail:
            'No puedes eliminar tu propio usuario'
        });
      }
    );


    it(
      'should delete another user after confirmation',
      () => {

        userServiceMock
          .delete
          .mockReturnValue(
            of(undefined)
          );

        component.confirmDelete(
          user
        );

        const config =
          confirmationServiceMock
            .confirm
            .mock
            .calls[0][0] as any;

        config.accept();

        expect(
          userServiceMock.delete
        ).toHaveBeenCalledWith(
          2
        );

        expect(
          userServiceMock.getAll
        ).toHaveBeenCalled();

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'success',
          summary: 'Éxito',
          detail:
            'Usuario eliminado'
        });
      }
    );


    it(
      'should show error when user deletion fails',
      () => {

        userServiceMock
          .delete
          .mockReturnValue(
            throwError(
              () => ({
                error: {
                  error:
                    'No se puede eliminar'
                }
              })
            )
          );

        component.confirmDelete(
          user
        );

        const config =
          confirmationServiceMock
            .confirm
            .mock
            .calls[0][0] as any;

        config.accept();

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'error',
          summary: 'Error',
          detail:
            'No se puede eliminar'
        });
      }
    );


    it(
      'should return role severity',
      () => {

        expect(
          component.roleSeverity(
            'ADMIN'
          )
        ).toBe(
          'success'
        );

        expect(
          component.roleSeverity(
            'USER'
          )
        ).toBe(
          'info'
        );
      }
    );


    it(
      'should navigate to products',
      () => {

        component.goToProducts();

        expect(
          routerMock.navigate
        ).toHaveBeenCalledWith([
          '/products'
        ]);
      }
    );

  }
);
