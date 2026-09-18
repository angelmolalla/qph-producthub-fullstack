import {
  TestBed
} from '@angular/core/testing';

import {
  Router
} from '@angular/router';

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
  LoginComponent
} from './login';


describe(
  'LoginComponent',
  () => {

    const authServiceMock = {
      login:
        vi.fn(),
      verifyTwoFactor:
        vi.fn()
    };

    const routerMock = {
      navigate:
        vi.fn()
    };

    let component:
      LoginComponent;


    beforeEach(
      async () => {

        authServiceMock
          .login
          .mockReset();

        authServiceMock
          .verifyTwoFactor
          .mockReset();

        routerMock
          .navigate
          .mockReset();

        await TestBed
          .configureTestingModule({
            imports: [
              LoginComponent
            ],
            providers: [
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
            LoginComponent,
            {
              set: {
                template: ''
              }
            }
          )
          .compileComponents();

        component =
          TestBed
            .createComponent(
              LoginComponent
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
      }
    );


    it(
      'should not submit invalid login form',
      () => {

        component.onSubmit();

        expect(
          authServiceMock.login
        ).not
          .toHaveBeenCalled();

        expect(
          component.loginForm
            .controls
            .username
            .touched
        ).toBe(
          true
        );
      }
    );


    it(
      'should navigate to products after login without 2FA',
      () => {

        authServiceMock
          .login
          .mockReturnValue(
            of({
              token: 'jwt',
              twoFactorRequired: false,
              challengeId: null,
              expiresInSeconds: null
            })
          );

        component
          .loginForm
          .setValue({
            username: 'angelo',
            password: 'Password123'
          });

        component.onSubmit();

        expect(
          authServiceMock.login
        ).toHaveBeenCalledWith({
          username: 'angelo',
          password: 'Password123'
        });

        expect(
          routerMock.navigate
        ).toHaveBeenCalledWith([
          '/products'
        ]);
      }
    );


    it(
      'should move to two factor step when required',
      () => {

        authServiceMock
          .login
          .mockReturnValue(
            of({
              token: null,
              twoFactorRequired: true,
              challengeId:
                'challenge-1',
              expiresInSeconds: 120
            })
          );

        component
          .loginForm
          .setValue({
            username: 'angelo',
            password: 'Password123'
          });

        component.onSubmit();

        expect(
          component.step
        ).toBe(
          'TWO_FACTOR'
        );

        expect(
          component.challengeId
        ).toBe(
          'challenge-1'
        );

        expect(
          component.expiresInSeconds
        ).toBe(
          120
        );

        expect(
          component.loading
        ).toBe(
          false
        );
      }
    );


    it(
      'should use default expiration when backend does not return it',
      () => {

        authServiceMock
          .login
          .mockReturnValue(
            of({
              token: null,
              twoFactorRequired: true,
              challengeId:
                'challenge-1',
              expiresInSeconds: null
            })
          );

        component
          .loginForm
          .setValue({
            username: 'angelo',
            password: 'Password123'
          });

        component.onSubmit();

        expect(
          component.expiresInSeconds
        ).toBe(
          300
        );
      }
    );


    it(
      'should show login error',
      () => {

        authServiceMock
          .login
          .mockReturnValue(
            throwError(
              () => ({
                error: {
                  error:
                    'Credenciales inválidas'
                }
              })
            )
          );

        component
          .loginForm
          .setValue({
            username: 'angelo',
            password: 'bad'
          });

        component.onSubmit();

        expect(
          component.errorMessage
        ).toBe(
          'Credenciales inválidas'
        );

        expect(
          component.loading
        ).toBe(
          false
        );
      }
    );


    it(
      'should not verify invalid 2FA form',
      () => {

        component.challengeId =
          'challenge-1';

        component.verifyTwoFactor();

        expect(
          authServiceMock
            .verifyTwoFactor
        ).not
          .toHaveBeenCalled();

        expect(
          component.twoFactorForm
            .controls
            .code
            .touched
        ).toBe(
          true
        );
      }
    );


    it(
      'should not verify when challenge id is missing',
      () => {

        component
          .twoFactorForm
          .setValue({
            code: '123456'
          });

        component.challengeId =
          null;

        component.verifyTwoFactor();

        expect(
          authServiceMock
            .verifyTwoFactor
        ).not
          .toHaveBeenCalled();
      }
    );


    it(
      'should verify 2FA and navigate when token is returned',
      () => {

        authServiceMock
          .verifyTwoFactor
          .mockReturnValue(
            of({
              token: 'jwt',
              twoFactorRequired: false,
              challengeId: null,
              expiresInSeconds: null
            })
          );

        component.challengeId =
          'challenge-1';

        component.twoFactorType =
          'TOTP_AUTH';

        component
          .twoFactorForm
          .setValue({
            code: '123456'
          });

        component.verifyTwoFactor();

        expect(
          authServiceMock
            .verifyTwoFactor
        ).toHaveBeenCalledWith(
          'challenge-1',
          '123456',
          'TOTP_AUTH'
        );

        expect(
          routerMock.navigate
        ).toHaveBeenCalledWith([
          '/products'
        ]);
      }
    );


    it(
      'should show an error when verify response has no token',
      () => {

        authServiceMock
          .verifyTwoFactor
          .mockReturnValue(
            of({
              token: null,
              twoFactorRequired: false,
              challengeId: null,
              expiresInSeconds: null
            })
          );

        component.challengeId =
          'challenge-1';

        component
          .twoFactorForm
          .setValue({
            code: '123456'
          });

        component.verifyTwoFactor();

        expect(
          component.errorMessage
        ).toBe(
          'No se recibió el token de autenticación'
        );

        expect(
          component.loading
        ).toBe(
          false
        );
      }
    );


    it(
      'should show 2FA verification error',
      () => {

        authServiceMock
          .verifyTwoFactor
          .mockReturnValue(
            throwError(
              () => ({
                error: {
                  message:
                    'Código inválido'
                }
              })
            )
          );

        component.challengeId =
          'challenge-1';

        component
          .twoFactorForm
          .setValue({
            code: '123456'
          });

        component.verifyTwoFactor();

        expect(
          component.errorMessage
        ).toBe(
          'Código inválido'
        );

        expect(
          component.loading
        ).toBe(
          false
        );
      }
    );


    it(
      'should change 2FA type and reset form',
      () => {

        component
          .twoFactorForm
          .setValue({
            code: '123456'
          });

        component.errorMessage =
          'Error';

        component.selectType(
          'TOTP_AUTH'
        );

        expect(
          component.twoFactorType
        ).toBe(
          'TOTP_AUTH'
        );

        expect(
          component.twoFactorForm
            .controls
            .code
            .value
        ).toBe(
          ''
        );

        expect(
          component.errorMessage
        ).toBe(
          ''
        );
      }
    );


    it(
      'should return to login step',
      () => {

        component.step =
          'TWO_FACTOR';

        component.challengeId =
          'challenge-1';

        component.loading =
          true;

        component.errorMessage =
          'Error';

        component
          .twoFactorForm
          .setValue({
            code: '123456'
          });

        component.backToLogin();

        expect(
          component.step
        ).toBe(
          'LOGIN'
        );

        expect(
          component.challengeId
        ).toBeNull();

        expect(
          component.errorMessage
        ).toBe(
          ''
        );

        expect(
          component.loading
        ).toBe(
          false
        );

        expect(
          component.twoFactorForm
            .controls
            .code
            .value
        ).toBe(
          ''
        );
      }
    );

  }
);
