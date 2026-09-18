import {
  TestBed
} from '@angular/core/testing';

import {
  provideHttpClient
} from '@angular/common/http';

import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import {
  Router
} from '@angular/router';

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
} from './auth';

import {
  AuthRequest,
  AuthResponse
} from '../../shared/models/auth.model';


describe(
  'AuthService',
  () => {

    let service:
      AuthService;

    let httpTesting:
      HttpTestingController;

    const routerMock = {
      navigate:
        vi.fn()
    };

    const apiUrl =
      'http://localhost:8080/api/auth';


    beforeEach(
      () => {

        localStorage.clear();

        routerMock
          .navigate
          .mockReset();

        TestBed.configureTestingModule({
          providers: [
            AuthService,
            provideHttpClient(),
            provideHttpClientTesting(),
            {
              provide:
                Router,
              useValue:
                routerMock
            }
          ]
        });

        service =
          TestBed.inject(
            AuthService
          );

        httpTesting =
          TestBed.inject(
            HttpTestingController
          );
      }
    );


    afterEach(
      () => {

        httpTesting.verify();

        localStorage.clear();

        TestBed.resetTestingModule();
      }
    );


    it(
      'should be created',
      () => {

        expect(
          service
        ).toBeTruthy();
      }
    );


    it(
      'should login and save token',
      () => {

        const credentials:
          AuthRequest = {
            username: 'angelo',
            password: 'Password123'
          };

        const response:
          AuthResponse = {
            token: 'jwt-token',
            twoFactorRequired: false,
            challengeId: null,
            expiresInSeconds: null
          };

        service
          .login(
            credentials
          )
          .subscribe(
            result => {

              expect(
                result
              ).toEqual(
                response
              );
            }
          );

        const request =
          httpTesting.expectOne(
            `${apiUrl}/login`
          );

        expect(
          request.request.method
        ).toBe(
          'POST'
        );

        expect(
          request.request.body
        ).toEqual(
          credentials
        );

        request.flush(
          response
        );

        expect(
          service.getToken()
        ).toBe(
          'jwt-token'
        );
      }
    );


    it(
      'should not save token when login response has no token',
      () => {

        const response:
          AuthResponse = {
            token: null,
            twoFactorRequired: true,
            challengeId:
              'challenge-1',
            expiresInSeconds: 300
          };

        service
          .login({
            username: 'angelo',
            password: 'Password123'
          })
          .subscribe();

        const request =
          httpTesting.expectOne(
            `${apiUrl}/login`
          );

        request.flush(
          response
        );

        expect(
          service.getToken()
        ).toBeNull();
      }
    );


    it(
      'should verify two factor and save token',
      () => {

        const response:
          AuthResponse = {
            token: 'jwt-2fa',
            twoFactorRequired: false,
            challengeId: null,
            expiresInSeconds: null
          };

        service
          .verifyTwoFactor(
            'challenge-1',
            '123456',
            'EMAIL_AUTH'
          )
          .subscribe();

        const request =
          httpTesting.expectOne(
            `${apiUrl}/2fa/verify`
          );

        expect(
          request.request.method
        ).toBe(
          'POST'
        );

        expect(
          request.request.body
        ).toEqual({
          challengeId:
            'challenge-1'
        });

        expect(
          request.request.headers.get(
            'X-2FA-CODE'
          )
        ).toBe(
          '123456'
        );

        expect(
          request.request.headers.get(
            'X-2FA-TYPE'
          )
        ).toBe(
          'EMAIL_AUTH'
        );

        request.flush(
          response
        );

        expect(
          service.getToken()
        ).toBe(
          'jwt-2fa'
        );
      }
    );


    it(
      'should logout, remove token and navigate to login',
      () => {

        localStorage.setItem(
          'jwt_token',
          'jwt-token'
        );

        service.logout();

        expect(
          service.getToken()
        ).toBeNull();

        expect(
          routerMock.navigate
        ).toHaveBeenCalledWith([
          '/login'
        ]);
      }
    );


    it(
      'should return false when there is no token',
      () => {

        expect(
          service.isAuthenticated()
        ).toBe(
          false
        );
      }
    );


    it(
      'should return true for a valid non-expired token',
      () => {

        const token =
          createToken({
            sub: 'angelo',
            roles: [
              'ROLE_USER'
            ],
            exp:
              Math.floor(
                Date.now() / 1000
              ) + 3600
          });

        localStorage.setItem(
          'jwt_token',
          token
        );

        expect(
          service.isAuthenticated()
        ).toBe(
          true
        );
      }
    );


    it(
      'should return false for an expired token',
      () => {

        const token =
          createToken({
            sub: 'angelo',
            roles: [
              'ROLE_USER'
            ],
            exp:
              Math.floor(
                Date.now() / 1000
              ) - 60
          });

        localStorage.setItem(
          'jwt_token',
          token
        );

        expect(
          service.isAuthenticated()
        ).toBe(
          false
        );
      }
    );


    it(
      'should return false for a malformed token',
      () => {

        localStorage.setItem(
          'jwt_token',
          'malformed-token'
        );

        expect(
          service.isAuthenticated()
        ).toBe(
          false
        );
      }
    );


    it(
      'should read username and roles from token',
      () => {

        const token =
          createToken({
            sub: 'angelo',
            roles: [
              'ROLE_ADMIN',
              'ROLE_USER'
            ],
            exp:
              Math.floor(
                Date.now() / 1000
              ) + 3600
          });

        localStorage.setItem(
          'jwt_token',
          token
        );

        expect(
          service.getUsername()
        ).toBe(
          'angelo'
        );

        expect(
          service.getRoles()
        ).toEqual([
          'ROLE_ADMIN',
          'ROLE_USER'
        ]);

        expect(
          service.hasRole('ADMIN')
        ).toBe(
          true
        );

        expect(
          service.hasRole('OTHER')
        ).toBe(
          false
        );
      }
    );


    it(
      'should return safe defaults when token is missing',
      () => {

        expect(
          service.getUsername()
        ).toBeNull();

        expect(
          service.getRoles()
        ).toEqual([]);

        expect(
          service.hasRole('ADMIN')
        ).toBe(
          false
        );
      }
    );


    it(
      'should return safe defaults when token payload is invalid',
      () => {

        localStorage.setItem(
          'jwt_token',
          'a.invalid-json.c'
        );

        expect(
          service.getUsername()
        ).toBeNull();

        expect(
          service.getRoles()
        ).toEqual([]);
      }
    );

  }
);


function createToken(
  payload: object
): string {

  const json =
    JSON.stringify(
      payload
    );

  const base64 =
    btoa(json)
      .replace(
        /\+/g,
        '-'
      )
      .replace(
        /\//g,
        '_'
      )
      .replace(
        /=+$/,
        ''
      );

  return `header.${base64}.signature`;
}
