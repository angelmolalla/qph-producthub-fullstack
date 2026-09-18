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
  adminGuard
} from './admin-guard';


describe(
  'adminGuard',
  () => {

    const authServiceMock = {
      isAuthenticated:
        vi.fn(),
      hasRole:
        vi.fn()
    };

    const loginTree = {
      redirect:
        '/login'
    };

    const productsTree = {
      redirect:
        '/products'
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

        authServiceMock
          .hasRole
          .mockReset();

        routerMock
          .createUrlTree
          .mockReset();

        routerMock
          .createUrlTree
          .mockImplementation(
            commands =>
              commands[0] ===
              '/login'
                ? loginTree
                : productsTree
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
          authServiceMock.hasRole
        ).not
          .toHaveBeenCalled();

        expect(
          routerMock.createUrlTree
        ).toHaveBeenCalledWith([
          '/login'
        ]);
      }
    );


    it(
      'should allow authenticated admin users',
      () => {

        authServiceMock
          .isAuthenticated
          .mockReturnValue(
            true
          );

        authServiceMock
          .hasRole
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
          authServiceMock.hasRole
        ).toHaveBeenCalledWith(
          'ADMIN'
        );
      }
    );


    it(
      'should redirect authenticated non-admin users to products',
      () => {

        authServiceMock
          .isAuthenticated
          .mockReturnValue(
            true
          );

        authServiceMock
          .hasRole
          .mockReturnValue(
            false
          );

        const result =
          executeGuard();

        expect(
          result
        ).toBe(
          productsTree
        );

        expect(
          routerMock.createUrlTree
        ).toHaveBeenCalledWith([
          '/products'
        ]);
      }
    );


    function executeGuard() {

      return TestBed
        .runInInjectionContext(
          () =>
            adminGuard(
              {} as never,
              {} as never
            )
        );
    }

  }
);
