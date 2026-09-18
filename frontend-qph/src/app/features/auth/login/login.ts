import {
  Component,
  inject
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  CommonModule
} from '@angular/common';

import {
  AuthService
} from '../../../core/services/auth';

import {
  TwoFactorType
} from '../../../shared/models/auth.model';

import {
  InputTextModule
} from 'primeng/inputtext';

import {
  PasswordModule
} from 'primeng/password';

import {
  ButtonModule
} from 'primeng/button';

@Component({
  selector: 'app-login',

  standalone: true,

  imports: [
    CommonModule,
    ReactiveFormsModule,
    InputTextModule,
    PasswordModule,
    ButtonModule
  ],

  templateUrl: './login.html',
  styleUrls: ['./login.scss']
})
export class LoginComponent {

  private fb =
    inject(FormBuilder);

  private authService =
    inject(AuthService);

  private router =
    inject(Router);

  loginForm =
    this.fb.nonNullable.group({

      username: [
        '',
        Validators.required
      ],

      password: [
        '',
        Validators.required
      ]

    });

  twoFactorForm =
    this.fb.nonNullable.group({

      code: [
        '',
        [
          Validators.required,
          Validators.pattern(
            /^\d{6}$/
          )
        ]
      ]

    });

  step:
    'LOGIN' | 'TWO_FACTOR' =
    'LOGIN';

  twoFactorType:
    TwoFactorType =
    'EMAIL_AUTH';

  challengeId:
    string | null = null;

  expiresInSeconds = 0;

  loading = false;

  errorMessage = '';

  onSubmit(): void {

    if (
      this.loginForm.invalid
    ) {

      this.loginForm
        .markAllAsTouched();

      return;
    }

    this.loading = true;

    this.errorMessage = '';

    this.authService
      .login(
        this.loginForm
          .getRawValue()
      )
      .subscribe({

        next: response => {

          if (
            !response
              .twoFactorRequired
          ) {

            this.router.navigate([
              '/products'
            ]);

            return;
          }

          this.challengeId =
            response.challengeId;

          this.expiresInSeconds =
            response
              .expiresInSeconds
            ?? 300;

          this.step =
            'TWO_FACTOR';

          this.loading = false;

        },

        error: err => {

          this.errorMessage =
            err.error?.error ??
            err.error?.message ??
            'Usuario o contraseña incorrectos';

          this.loading = false;

        }

      });
  }

  verifyTwoFactor(): void {

    if (
      this.twoFactorForm.invalid ||
      !this.challengeId
    ) {
      this.twoFactorForm
        .markAllAsTouched();
      return;
    }

    this.loading = true;

    this.errorMessage = '';

    const code =
      this.twoFactorForm
        .getRawValue()
        .code;

    this.authService
      .verifyTwoFactor(
        this.challengeId,
        code,
        this.twoFactorType
      )
      .subscribe({

        next: response => {

          if (!response.token) {

            this.errorMessage =
              'No se recibió el token de autenticación';

            this.loading = false;

            return;
          }

          this.router.navigate([
            '/products'
          ]);

        },

        error: err => {

          this.errorMessage =
            err.error?.error ??
            err.error?.message ??
            'Código de verificación incorrecto';

          this.loading = false;

        }

      });
  }

  selectType(
    type: TwoFactorType
  ): void {

    this.twoFactorType =
      type;

    this.twoFactorForm
      .reset();

    this.errorMessage = '';
  }

  backToLogin(): void {

    this.step = 'LOGIN';

    this.challengeId = null;

    this.errorMessage = '';

    this.twoFactorForm
      .reset();

    this.loading = false;
  }
}