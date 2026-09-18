import {
  TestBed
} from '@angular/core/testing';

import {
  ActivatedRoute,
  convertToParamMap,
  Router
} from '@angular/router';

import {
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
  ProductService
} from '../../../core/services/product';

import {
  ProductFormComponent
} from './product-form';


describe(
  'ProductFormComponent',
  () => {

    const productServiceMock = {
      getById:
        vi.fn(),
      create:
        vi.fn(),
      update:
        vi.fn()
    };

    const routerMock = {
      navigate:
        vi.fn()
    };

    const messageServiceMock = {
      add:
        vi.fn()
    };

    const routeMock = {
      snapshot: {
        paramMap:
          convertToParamMap({})
      }
    };

    let component:
      ProductFormComponent;


    beforeEach(
      async () => {

        productServiceMock
          .getById
          .mockReset();

        productServiceMock
          .create
          .mockReset();

        productServiceMock
          .update
          .mockReset();

        routerMock
          .navigate
          .mockReset();

        messageServiceMock
          .add
          .mockReset();

        routeMock.snapshot.paramMap =
          convertToParamMap({});

        await TestBed
          .configureTestingModule({
            imports: [
              ProductFormComponent
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
                  Router,
                useValue:
                  routerMock
              },
              {
                provide:
                  ActivatedRoute,
                useValue:
                  routeMock
              }
            ]
          })
          .overrideComponent(
            ProductFormComponent,
            {
              set: {
                template: '',
                providers: [
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
              ProductFormComponent
            )
            .componentInstance;
      }
    );


    afterEach(
      () => {

        vi.useRealTimers();

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
      'should remain in create mode when route has no id',
      () => {

        component.ngOnInit();

        expect(
          component.isEdit
        ).toBe(
          false
        );

        expect(
          productServiceMock.getById
        ).not
          .toHaveBeenCalled();
      }
    );


    it(
      'should load product in edit mode',
      () => {

        routeMock.snapshot.paramMap =
          convertToParamMap({
            id: '5'
          });

        productServiceMock
          .getById
          .mockReturnValue(
            of({
              id: 5,
              nombre: 'Laptop',
              precio: 1000,
              stock: 4
            })
          );

        component.ngOnInit();

        expect(
          component.isEdit
        ).toBe(
          true
        );

        expect(
          component.productId
        ).toBe(
          5
        );

        expect(
          component.form.value
        ).toMatchObject({
          nombre: 'Laptop',
          precio: 1000,
          stock: 4
        });

        expect(
          component.loading
        ).toBe(
          false
        );
      }
    );


    it(
      'should show error when product load fails',
      () => {

        routeMock.snapshot.paramMap =
          convertToParamMap({
            id: '5'
          });

        productServiceMock
          .getById
          .mockReturnValue(
            throwError(
              () =>
                new Error(
                  'load failed'
                )
            )
          );

        component.ngOnInit();

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'error',
          summary: 'Error',
          detail:
            'No se pudo cargar el producto'
        });

        expect(
          component.loading
        ).toBe(
          false
        );
      }
    );


    it(
      'should not submit invalid form',
      () => {

        component.onSubmit();

        expect(
          productServiceMock.create
        ).not
          .toHaveBeenCalled();

        expect(
          productServiceMock.update
        ).not
          .toHaveBeenCalled();

        expect(
          component.form
            .controls
            .nombre
            .touched
        ).toBe(
          true
        );
      }
    );


    it(
      'should create product and navigate after success',
      () => {

        vi.useFakeTimers();

        productServiceMock
          .create
          .mockReturnValue(
            of({
              id: 1,
              nombre: 'Monitor',
              precio: 300,
              stock: 4
            })
          );

        component.form.setValue({
          nombre: 'Monitor',
          precio: 300,
          stock: 4
        });

        component.onSubmit();

        expect(
          productServiceMock.create
        ).toHaveBeenCalledWith({
          nombre: 'Monitor',
          precio: 300,
          stock: 4
        });

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'success',
          summary: 'Éxito',
          detail:
            'Producto creado'
        });

        vi.advanceTimersByTime(
          1500
        );

        expect(
          routerMock.navigate
        ).toHaveBeenCalledWith([
          '/products'
        ]);
      }
    );


    it(
      'should update product in edit mode',
      () => {

        vi.useFakeTimers();

        component.isEdit =
          true;

        component.productId =
          8;

        productServiceMock
          .update
          .mockReturnValue(
            of({
              id: 8,
              nombre: 'Laptop Pro',
              precio: 1500,
              stock: 7
            })
          );

        component.form.setValue({
          nombre: 'Laptop Pro',
          precio: 1500,
          stock: 7
        });

        component.onSubmit();

        expect(
          productServiceMock.update
        ).toHaveBeenCalledWith(
          8,
          {
            nombre:
              'Laptop Pro',
            precio: 1500,
            stock: 7
          }
        );

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'success',
          summary: 'Éxito',
          detail:
            'Producto actualizado'
        });

        vi.advanceTimersByTime(
          1500
        );

        expect(
          routerMock.navigate
        ).toHaveBeenCalledWith([
          '/products'
        ]);
      }
    );


    it(
      'should show backend error when save fails',
      () => {

        productServiceMock
          .create
          .mockReturnValue(
            throwError(
              () => ({
                error: {
                  error:
                    'Nombre duplicado'
                }
              })
            )
          );

        component.form.setValue({
          nombre: 'Monitor',
          precio: 300,
          stock: 4
        });

        component.onSubmit();

        expect(
          messageServiceMock.add
        ).toHaveBeenCalledWith({
          severity: 'error',
          summary: 'Error',
          detail:
            'Nombre duplicado'
        });

        expect(
          component.submitting
        ).toBe(
          false
        );
      }
    );


    it(
      'should navigate back to products',
      () => {

        component.goBack();

        expect(
          routerMock.navigate
        ).toHaveBeenCalledWith([
          '/products'
        ]);
      }
    );

  }
);
