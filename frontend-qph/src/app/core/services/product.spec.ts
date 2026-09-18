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
  ProductService
} from './product';

import {
  Product
} from '../../shared/models/product.model';


describe(
  'ProductService',
  () => {

    let service:
      ProductService;

    let httpTesting:
      HttpTestingController;

    const apiUrl =
      'http://localhost:8080/api/products';


    beforeEach(
      () => {

        TestBed.configureTestingModule({
          providers: [
            ProductService,
            provideHttpClient(),
            provideHttpClientTesting()
          ]
        });

        service =
          TestBed.inject(
            ProductService
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
      'should get all products',
      () => {

        const products:
          Product[] = [
            {
              id: 1,
              nombre: 'Laptop',
              precio: 1200,
              stock: 5
            },
            {
              id: 2,
              nombre: 'Mouse',
              precio: 25,
              stock: 10
            }
          ];

        service
          .getAll()
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                products
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
          products
        );
      }
    );


    it(
      'should get a product by id',
      () => {

        const product:
          Product = {
            id: 1,
            nombre: 'Laptop',
            precio: 1200,
            stock: 5
          };

        service
          .getById(1)
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                product
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
          product
        );
      }
    );


    it(
      'should create a product',
      () => {

        const requestBody:
          Product = {
            nombre: 'Monitor',
            precio: 350,
            stock: 8
          };

        const created:
          Product = {
            id: 3,
            ...requestBody
          };

        service
          .create(
            requestBody
          )
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                created
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
          requestBody
        );

        request.flush(
          created
        );
      }
    );


    it(
      'should update a product',
      () => {

        const product:
          Product = {
            id: 1,
            nombre: 'Laptop Pro',
            precio: 1500,
            stock: 7
          };

        service
          .update(
            1,
            product
          )
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                product
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
          product
        );

        request.flush(
          product
        );
      }
    );


    it(
      'should delete a product',
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


    it(
      'should propagate an HTTP error',
      () => {

        service
          .getById(99)
          .subscribe({

            next: () => {

              throw new Error(
                'La petición debía fallar'
              );
            },

            error: error => {

              expect(
                error.status
              ).toBe(
                404
              );

              expect(
                error.error.error
              ).toBe(
                'Producto no encontrado'
              );
            }

          });

        const request =
          httpTesting.expectOne(
            `${apiUrl}/99`
          );

        request.flush(
          {
            error:
              'Producto no encontrado'
          },
          {
            status: 404,
            statusText:
              'Not Found'
          }
        );
      }
    );

  }
);
