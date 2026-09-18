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
  afterEach,
  beforeEach,
  describe,
  expect,
  it
} from 'vitest';

import {
  UserService
} from './user';

import {
  CreateUserRequest,
  UpdateUserRequest,
  User
} from '../../shared/models/user.model';


describe(
  'UserService',
  () => {

    let service:
      UserService;

    let httpTesting:
      HttpTestingController;

    const apiUrl =
      'http://localhost:8080/api/users';

    const user:
      User = {
        id: 1,
        username: 'angelo',
        email:
          'angelo@example.com',
        role: 'USER',
        enabled: true,
        twoFactorEnabled: false
      };


    beforeEach(
      () => {

        TestBed.configureTestingModule({
          providers: [
            UserService,
            provideHttpClient(),
            provideHttpClientTesting()
          ]
        });

        service =
          TestBed.inject(
            UserService
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
      'should be created',
      () => {

        expect(
          service
        ).toBeTruthy();
      }
    );


    it(
      'should get all users',
      () => {

        service
          .getAll()
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                [user]
              );
            }
          );

        const request =
          httpTesting.expectOne(
            apiUrl
          );

        expect(
          request.request.method
        ).toBe(
          'GET'
        );

        request.flush(
          [user]
        );
      }
    );


    it(
      'should get a user by id',
      () => {

        service
          .getById(1)
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                user
              );
            }
          );

        const request =
          httpTesting.expectOne(
            `${apiUrl}/1`
          );

        expect(
          request.request.method
        ).toBe(
          'GET'
        );

        request.flush(
          user
        );
      }
    );


    it(
      'should create a user',
      () => {

        const body:
          CreateUserRequest = {
            username: 'angelo',
            email:
              'angelo@example.com',
            password:
              'Password123',
            role: 'USER',
            twoFactorEnabled: false
          };

        service
          .create(body)
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                user
              );
            }
          );

        const request =
          httpTesting.expectOne(
            apiUrl
          );

        expect(
          request.request.method
        ).toBe(
          'POST'
        );

        expect(
          request.request.body
        ).toEqual(
          body
        );

        request.flush(
          user
        );
      }
    );


    it(
      'should update a user',
      () => {

        const body:
          UpdateUserRequest = {
            username: 'angelo',
            email:
              'angelo@example.com',
            password: '',
            role: 'ADMIN',
            enabled: true,
            twoFactorEnabled: true
          };

        const updated:
          User = {
            ...user,
            role: 'ADMIN',
            twoFactorEnabled: true
          };

        service
          .update(
            1,
            body
          )
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                updated
              );
            }
          );

        const request =
          httpTesting.expectOne(
            `${apiUrl}/1`
          );

        expect(
          request.request.method
        ).toBe(
          'PUT'
        );

        expect(
          request.request.body
        ).toEqual(
          body
        );

        request.flush(
          updated
        );
      }
    );


    it(
      'should delete a user',
      () => {

        service
          .delete(1)
          .subscribe(
            response => {

              expect(
                response
              ).toBeNull();
            }
          );

        const request =
          httpTesting.expectOne(
            `${apiUrl}/1`
          );

        expect(
          request.request.method
        ).toBe(
          'DELETE'
        );

        request.flush(
          null
        );
      }
    );

  }
);
