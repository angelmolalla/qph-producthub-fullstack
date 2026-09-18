import {
  TestBed
} from '@angular/core/testing';

import {
  Router
} from '@angular/router';

import {
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
  authGuard
} from './auth-guard';


describe(
  'authGuard',
  () => {

    const authServiceMock = {
      isAuthenticated:
        vi.fn()
    };

    const loginTree = {
      redirect:
        '/login'
    };

    const routerMock = {
      createUrlTree:
        vi.fn()
    };


    beforeEach(
      () => {

        authServiceMock
          .isAuthenticated
          .mockReset();

        routerMock
          .createUrlTree
          .mockReset();

        routerMock
          .createUrlTree
          .mockReturnValue(
            loginTree
          );

        TestBed.configureTestingModule({
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
        });
      }
    );


    it(
      'should allow authenticated users',
      () => {

        authServiceMock
          .isAuthenticated
          .mockReturnValue(
            true
          );

        const result =
          executeGuard();

        expect(
          result
        ).toBe(
          true
        );

        expect(
          routerMock.createUrlTree
        ).not
          .toHaveBeenCalled();
      }
    );


    it(
      'should redirect unauthenticated users to login',
      () => {

        authServiceMock
          .isAuthenticated
          .mockReturnValue(
            false
          );

        const result =
          executeGuard();

        expect(
          result
        ).toBe(
          loginTree
        );

        expect(
          routerMock.createUrlTree
        ).toHaveBeenCalledWith([
          '/login'
        ]);
      }
    );


    function executeGuard() {

      return TestBed
        .runInInjectionContext(
          () =>
            authGuard(
              {} as never,
              {} as never
            )
        );
    }

  }
);
