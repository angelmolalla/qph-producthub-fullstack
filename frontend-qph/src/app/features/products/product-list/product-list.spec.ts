import {
  TestBed
} from '@angular/core/testing';

import {
  Router
} from '@angular/router';

import {
  ConfirmationService,
  MessageService
} from 'primeng/api';

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
  ProductService
} from '../../../core/services/product';

import {
  SaleService
} from '../../../core/services/sale';

import {
  Product
} from '../../../shared/models/product.model';

import {
  SaleResponse
} from '../../../shared/models/sale.model';

import {
  ProductListComponent
} from './product-list';


describe(
  'ProductListComponent',
  () => {

    const productServiceMock = {
      getAll:
        vi.fn(),
      delete:
        vi.fn()
    };

    const authServiceMock = {
      hasRole:
        vi.fn(),
      logout:
        vi.fn()
    };

    const saleServiceMock = {
      create:
        vi.fn()
    };

    const routerMock = {
      navigate:
        vi.fn()
    };

    const confirmationServiceMock = {
      confirm:
        vi.fn()
    };

    const messageServiceMock = {
      add:
        vi.fn()
    };

    let component:
      ProductListComponent;

    const product:
      Product = {
        id: 1,
        nombre: 'Laptop',
        precio: 1000,
        stock: 5
      };

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
        remainingStock: 3
      };


    beforeEach(
      async () => {

        productServiceMock
          .getAll
          .mockReset();

        productServiceMock
          .delete
          .mockReset();

        authServiceMock
          .hasRole
          .mockReset();

        authServiceMock
          .logout
          .mockReset();

        saleServiceMock
          .create
          .mockReset();

        routerMock
          .navigate
          .mockReset();

        confirmationServiceMock
          .confirm
          .mockReset();

        messageServiceMock
          .add
          .mockReset();

        productServiceMock
          .getAll
          .mockReturnValue(
            of([])
          );

        authServiceMock
          .hasRole
          .mockReturnValue(
            false
          );

        await TestBed
          .configureTestingModule({
            imports: [
              ProductListComponent
            ],
            providers: [
              {
                provide:
                  ProductService,
                useValue:
                  productServiceMock
              },
              {
                provide:
                  AuthService,
                useValue:
                  authServiceMock
              },
              {
                provide:
                  SaleService,
                useValue:
                  saleServiceMock
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
            ProductListComponent,
            {
              set: {
                template: '',
                providers: [
                  {
                    provide:
                      ConfirmationService,
                    useValue:
                      confirmationServiceMock
                  },
                  {
                    provide:
                      MessageService,
                    useValue:
                      messageServiceMock
                  }
                ]
              }
            }
          )
          .compileComponents();

        component =
          TestBed
            .createComponent(
              ProductListComponent
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
      'should load products successfully',
      () => {

        productServiceMock
          .getAll
          .mockReturnValue(
            of([
              product
            ])
          );

        component.loadProducts();

        expect(
          component.products
        ).toEqual([
          product
        ]);

        expect(
          component.loading
        ).toBe(
          false
        );
      }
    );


    it(
      'should show error when loading products fails',
      () => {

        productServiceMock
          .getAll
          .mockReturnValue(
            throwError(
              () => ({
                error: {
                  error:
                    'Backend error'
                }
              })
            )
          );

        component.loadProducts();

        expect(
          component.loading
        ).toBe(
          false
        );

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'error',
          summary: 'Error',
          detail:
            'Backend error'
        });
      }
    );


    it(
      'should expose admin role state',
      () => {

        authServiceMock
          .hasRole
          .mockReturnValue(
            true
          );

        expect(
          component.isAdmin
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
      'should navigate to product creation',
      () => {

        component.goToCreate();

        expect(
          routerMock.navigate
        ).toHaveBeenCalledWith([
          '/products/new'
        ]);
      }
    );


    it(
      'should navigate to product edit when id exists',
      () => {

        component.goToEdit(5);

        expect(
          routerMock.navigate
        ).toHaveBeenCalledWith([
          '/products/edit',
          5
        ]);
      }
    );


    it(
      'should ignore edit when id is missing',
      () => {

        component.goToEdit();

        expect(
          routerMock.navigate
        ).not
          .toHaveBeenCalled();
      }
    );


    it(
      'should navigate to users',
      () => {

        component.goToUsers();

        expect(
          routerMock.navigate
        ).toHaveBeenCalledWith([
          '/users'
        ]);
      }
    );


    it(
      'should ignore delete confirmation when product has no id',
      () => {

        component.confirmDelete({
          nombre: 'Nuevo',
          precio: 10,
          stock: 1
        });

        expect(
          confirmationServiceMock.confirm
        ).not
          .toHaveBeenCalled();
      }
    );


    it(
      'should delete product after confirmation',
      () => {

        productServiceMock
          .delete
          .mockReturnValue(
            of(undefined)
          );

        component.confirmDelete(
          product
        );

        const config =
          confirmationServiceMock
            .confirm
            .mock
            .calls[0][0] as any;

        config.accept();

        expect(
          productServiceMock.delete
        ).toHaveBeenCalledWith(
          1
        );

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'success',
          summary: 'Éxito',
          detail:
            'Producto eliminado correctamente'
        });

        expect(
          productServiceMock.getAll
        ).toHaveBeenCalled();
      }
    );


    it(
      'should show error when product deletion fails',
      () => {

        productServiceMock
          .delete
          .mockReturnValue(
            throwError(
              () => ({
                error: {
                  error:
                    'No se puede eliminar'
                }
              })
            )
          );

        component.confirmDelete(
          product
        );

        const config =
          confirmationServiceMock
            .confirm
            .mock
            .calls[0][0] as any;

        config.accept();

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'error',
          summary: 'Error',
          detail:
            'No se puede eliminar'
        });
      }
    );


    it(
      'should calculate stock severity',
      () => {

        expect(
          component.getStockSeverity(
            11
          )
        ).toBe(
          'success'
        );

        expect(
          component.getStockSeverity(
            1
          )
        ).toBe(
          'warn'
        );

        expect(
          component.getStockSeverity(
            0
          )
        ).toBe(
          'danger'
        );
      }
    );


    it(
      'should ignore sale for product without id',
      () => {

        component.openSale({
          nombre: 'Nuevo',
          precio: 20,
          stock: 3
        });

        expect(
          component.saleDialogVisible
        ).toBe(
          false
        );
      }
    );


    it(
      'should ignore sale when stock is zero',
      () => {

        component.openSale({
          ...product,
          stock: 0
        });

        expect(
          component.saleDialogVisible
        ).toBe(
          false
        );
      }
    );


    it(
      'should open sale dialog and limit quantity by stock',
      () => {

        component.openSale(
          product
        );

        expect(
          component.selectedProduct
        ).toEqual(
          product
        );

        expect(
          component.saleDialogVisible
        ).toBe(
          true
        );

        expect(
          component.saleForm
            .controls
            .quantity
            .value
        ).toBe(
          1
        );

        component.saleForm
          .controls
          .quantity
          .setValue(6);

        expect(
          component.saleForm.invalid
        ).toBe(
          true
        );
      }
    );


    it(
      'should not close sale while sale is loading',
      () => {

        component.openSale(
          product
        );

        component.saleLoading =
          true;

        component.closeSale();

        expect(
          component.saleDialogVisible
        ).toBe(
          true
        );

        expect(
          component.selectedProduct
        ).toEqual(
          product
        );
      }
    );


    it(
      'should close and reset sale dialog',
      () => {

        component.openSale(
          product
        );

        component.saleForm
          .controls
          .quantity
          .setValue(3);

        component.closeSale();

        expect(
          component.saleDialogVisible
        ).toBe(
          false
        );

        expect(
          component.selectedProduct
        ).toBeNull();

        expect(
          component.saleForm
            .controls
            .quantity
            .value
        ).toBe(
          1
        );
      }
    );


    it(
      'should calculate sale total',
      () => {

        expect(
          component.saleTotal
        ).toBe(
          0
        );

        component.openSale(
          product
        );

        component.saleForm
          .controls
          .quantity
          .setValue(3);

        expect(
          component.saleTotal
        ).toBe(
          3000
        );
      }
    );


    it(
      'should not submit invalid sale',
      () => {

        component.submitSale();

        expect(
          saleServiceMock.create
        ).not
          .toHaveBeenCalled();
      }
    );


    it(
      'should submit sale successfully',
      () => {

        saleServiceMock
          .create
          .mockReturnValue(
            of(sale)
          );

        component.openSale(
          product
        );

        component.saleForm
          .controls
          .quantity
          .setValue(2);

        component.submitSale();

        expect(
          saleServiceMock.create
        ).toHaveBeenCalledWith({
          productId: 1,
          quantity: 2
        });

        expect(
          component.lastSale
        ).toEqual(
          sale
        );

        expect(
          component.saleLoading
        ).toBe(
          false
        );

        expect(
          component.saleDialogVisible
        ).toBe(
          false
        );

        expect(
          component.selectedProduct
        ).toBeNull();

        expect(
          productServiceMock.getAll
        ).toHaveBeenCalled();

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'success',
          summary:
            'Venta realizada',
          detail:
            '2 unidad(es) de Laptop. Total: $2000.00'
        });
      }
    );


    it(
      'should reload products and close dialog on stock conflict',
      () => {

        saleServiceMock
          .create
          .mockReturnValue(
            throwError(
              () => ({
                status: 409,
                error: {
                  error:
                    'Stock insuficiente'
                }
              })
            )
          );

        component.openSale(
          product
        );

        component.submitSale();

        expect(
          component.saleLoading
        ).toBe(
          false
        );

        expect(
          component.saleDialogVisible
        ).toBe(
          false
        );

        expect(
          component.selectedProduct
        ).toBeNull();

        expect(
          productServiceMock.getAll
        ).toHaveBeenCalled();

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'error',
          summary:
            'No se pudo realizar la venta',
          detail:
            'Stock insuficiente'
        });
      }
    );


    it(
      'should keep dialog open on non-conflict sale error',
      () => {

        saleServiceMock
          .create
          .mockReturnValue(
            throwError(
              () => ({
                status: 500,
                error: {
                  message:
                    'Servidor no disponible'
                }
              })
            )
          );

        component.openSale(
          product
        );

        component.submitSale();

        expect(
          component.saleLoading
        ).toBe(
          false
        );

        expect(
          component.saleDialogVisible
        ).toBe(
          true
        );

        expect(
          component.selectedProduct
        ).toEqual(
          product
        );

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'error',
          summary:
            'No se pudo realizar la venta',
          detail:
            'Servidor no disponible'
        });
      }
    );


    it(
      'should logout',
      () => {

        component.logout();

        expect(
          authServiceMock.logout
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );

  }
);
