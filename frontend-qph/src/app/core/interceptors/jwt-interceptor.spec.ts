import {
  HttpClient,
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';

import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import {
  TestBed
} from '@angular/core/testing';

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
} from '../services/auth';

import {
  jwtInterceptor
} from './jwt-interceptor';


describe(
  'jwtInterceptor',
  () => {

    let http:
      HttpClient;

    let httpTesting:
      HttpTestingController;

    const authServiceMock = {
      getToken:
        vi.fn(),
      logout:
        vi.fn()
    };


    beforeEach(
      () => {

        authServiceMock
          .getToken
          .mockReset();

        authServiceMock
          .logout
          .mockReset();

        TestBed.configureTestingModule({
          providers: [
            {
              provide:
                AuthService,
              useValue:
                authServiceMock
            },
            provideHttpClient(
              withInterceptors([
                jwtInterceptor
              ])
            ),
            provideHttpClientTesting()
          ]
        });

        http =
          TestBed.inject(
            HttpClient
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

        TestBed.resetTestingModule();
      }
    );


    it(
      'should add bearer token to protected requests',
      () => {

        authServiceMock
          .getToken
          .mockReturnValue(
            'jwt-token'
          );

        http
          .get(
            '/api/products'
          )
          .subscribe();

        const request =
          httpTesting.expectOne(
            '/api/products'
          );

        expect(
          request.request.headers.get(
            'Authorization'
          )
        ).toBe(
          'Bearer jwt-token'
        );

        request.flush({});
      }
    );


    it(
      'should not add authorization header when token is missing',
      () => {

        authServiceMock
          .getToken
          .mockReturnValue(
            null
          );

        http
          .get(
            '/api/products'
          )
          .subscribe();

        const request =
          httpTesting.expectOne(
            '/api/products'
          );

        expect(
          request.request.headers.has(
            'Authorization'
          )
        ).toBe(
          false
        );

        request.flush({});
      }
    );


    it(
      'should not add bearer token to auth requests',
      () => {

        authServiceMock
          .getToken
          .mockReturnValue(
            'jwt-token'
          );

        http
          .post(
            '/api/auth/login',
            {}
          )
          .subscribe();

        const request =
          httpTesting.expectOne(
            '/api/auth/login'
          );

        expect(
          request.request.headers.has(
            'Authorization'
          )
        ).toBe(
          false
        );

        request.flush({});
      }
    );


    it(
      'should logout on 401 from protected endpoint',
      () => {

        authServiceMock
          .getToken
          .mockReturnValue(
            'jwt-token'
          );

        http
          .get(
            '/api/products'
          )
          .subscribe({
            error: error => {

              expect(
                error.status
              ).toBe(
                401
              );
            }
          });

        const request =
          httpTesting.expectOne(
            '/api/products'
          );

        request.flush(
          {
            error:
              'Unauthorized'
          },
          {
            status: 401,
            statusText:
              'Unauthorized'
          }
        );

        expect(
          authServiceMock.logout
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );


    it(
      'should not logout on 401 from auth endpoint',
      () => {

        authServiceMock
          .getToken
          .mockReturnValue(
            null
          );

        http
          .post(
            '/api/auth/login',
            {}
          )
          .subscribe({
            error: error => {

              expect(
                error.status
              ).toBe(
                401
              );
            }
          });

        const request =
          httpTesting.expectOne(
            '/api/auth/login'
          );

        request.flush(
          {
            error:
              'Bad credentials'
          },
          {
            status: 401,
            statusText:
              'Unauthorized'
          }
        );

        expect(
          authServiceMock.logout
        ).not
          .toHaveBeenCalled();
      }
    );

  }
);
