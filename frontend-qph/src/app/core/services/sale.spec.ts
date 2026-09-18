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
  SaleService
} from './sale';

import {
  CreateSaleRequest,
  SaleResponse
} from '../../shared/models/sale.model';


describe(
  'SaleService',
  () => {

    let service:
      SaleService;

    let httpTesting:
      HttpTestingController;

    const apiUrl =
      'http://localhost:8080/api/sales';

    const sale:
      SaleResponse = {
        id: 'sale-1',
        productId: 1,
        productName: 'Laptop',
        quantity: 2,
        unitPrice: 1000,
        total: 2000,
        username: 'angelo',
        status: 'COMPLETED',
        createdAt:
          '2026-09-18T10:00:00Z',
        remainingStock: 8
      };


    beforeEach(
      () => {

        TestBed.configureTestingModule({
          providers: [
            SaleService,
            provideHttpClient(),
            provideHttpClientTesting()
          ]
        });

        service =
          TestBed.inject(
            SaleService
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
      'should create a sale',
      () => {

        const body:
          CreateSaleRequest = {
            productId: 1,
            quantity: 2
          };

        service
          .create(body)
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                sale
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
          sale
        );
      }
    );


    it(
      'should get current user sales',
      () => {

        service
          .getMySales()
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                [sale]
              );
            }
          );

        const request =
          httpTesting.expectOne(
            `${apiUrl}/me`
          );

        expect(
          request.request.method
        ).toBe(
          'GET'
        );

        request.flush(
          [sale]
        );
      }
    );


    it(
      'should get all sales',
      () => {

        service
          .getAll()
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                [sale]
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
          [sale]
        );
      }
    );


    it(
      'should get sale by id',
      () => {

        service
          .getById('sale-1')
          .subscribe(
            response => {

              expect(
                response
              ).toEqual(
                sale
              );
            }
          );

        const request =
          httpTesting.expectOne(
            `${apiUrl}/sale-1`
          );

        expect(
          request.request.method
        ).toBe(
          'GET'
        );

        request.flush(
          sale
        );
      }
    );

  }
);
