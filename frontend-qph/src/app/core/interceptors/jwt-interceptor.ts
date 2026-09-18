import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';

import { inject } from '@angular/core';

import {
  catchError,
  throwError
} from 'rxjs';

import { AuthService }
  from '../services/auth';

export const jwtInterceptor:
  HttpInterceptorFn =
  (req, next) => {

    const authService =
      inject(AuthService);

    const token =
      authService.getToken();

    const isAuthRequest =
      req.url.includes(
        '/api/auth/'
      );
    if (token && !isAuthRequest) {

      req = req.clone({
        setHeaders: {
          Authorization:
            `Bearer ${token}`
        }
      });
    }

    return next(req).pipe(

      catchError(
        (
          error:
            HttpErrorResponse
        ) => {
          if (
            error.status === 401 &&
            !isAuthRequest
          ) {

            authService.logout();
          }

          return throwError(
            () => error
          );
        }
      )
    );
  };