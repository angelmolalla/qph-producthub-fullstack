import {
  Component,
  inject,
  OnInit
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  Router
} from '@angular/router';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  ProductService
} from '../../../core/services/product';

import {
  AuthService
} from '../../../core/services/auth';

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
  TableModule
} from 'primeng/table';

import {
  ButtonModule
} from 'primeng/button';

import {
  CardModule
} from 'primeng/card';

import {
  TagModule
} from 'primeng/tag';

import {
  ConfirmDialogModule
} from 'primeng/confirmdialog';

import {
  ToastModule
} from 'primeng/toast';

import {
  ToolbarModule
} from 'primeng/toolbar';

import {
  DialogModule
} from 'primeng/dialog';

import {
  InputNumberModule
} from 'primeng/inputnumber';

import {
  TooltipModule
} from 'primeng/tooltip';

import {
  ConfirmationService,
  MessageService
} from 'primeng/api';

@Component({
  selector: 'app-product-list',

  standalone: true,

  imports: [
    CommonModule,
    ReactiveFormsModule,

    TableModule,
    ButtonModule,
    CardModule,
    TagModule,
    ConfirmDialogModule,
    ToastModule,
    ToolbarModule,
    DialogModule,
    InputNumberModule,
    TooltipModule
  ],

  providers: [
    ConfirmationService,
    MessageService
  ],

  templateUrl:
    './product-list.html',

  styleUrls: [
    './product-list.scss'
  ]
})
export class ProductListComponent
  implements OnInit {

  private productService =
    inject(ProductService);

  private authService =
    inject(AuthService);

  private saleService =
    inject(SaleService);

  private router =
    inject(Router);

  private confirmationService =
    inject(ConfirmationService);

  private messageService =
    inject(MessageService);

  private fb = inject(FormBuilder);

  products: Product[] = [];

  loading = false;

  saleDialogVisible = false;

  saleLoading = false;

  selectedProduct:
    Product | null = null;

  lastSale:
    SaleResponse | null = null;


  saleForm =
    this.fb.nonNullable.group({

      quantity: [
        1,
        [
          Validators.required,
          Validators.min(1)
        ]
      ]

    });

  ngOnInit(): void {

    this.loadProducts();
  }

  get isAdmin(): boolean {

    return this.authService
      .hasRole('ADMIN');
  }

  loadProducts(): void {

    this.loading = true;

    this.productService
      .getAll()
      .subscribe({

        next: data => {

          this.products = data;

          this.loading = false;
        },

        error: err => {

          this.loading = false;

          this.messageService.add({

            severity: 'error',

            summary: 'Error',

            detail:
              err.error?.error ??
              'No se pudieron cargar los productos'
          });
        }

      });
  }


  goToCreate(): void {

    this.router.navigate([
      '/products/new'
    ]);
  }


  goToEdit(
    id?: number
  ): void {

    if (id == null) {
      return;
    }

    this.router.navigate([
      '/products/edit',
      id
    ]);
  }


  goToUsers(): void {

    this.router.navigate([
      '/users'
    ]);
  }


  confirmDelete(
    product: Product
  ): void {

    if (product.id == null) {
      return;
    }

    this.confirmationService.confirm({

      message:
        `¿Estás seguro de eliminar "${product.nombre}"?`,

      header:
        'Confirmar eliminación',

      icon:
        'pi pi-exclamation-triangle',

      acceptLabel:
        'Sí, eliminar',

      rejectLabel:
        'Cancelar',

      acceptButtonStyleClass:
        'p-button-danger',

      accept: () =>
        this.deleteProduct(
          product.id!
        )
    });
  }


  private deleteProduct(
    id: number
  ): void {

    this.productService
      .delete(id)
      .subscribe({

        next: () => {

          this.messageService.add({

            severity: 'success',

            summary: 'Éxito',

            detail:
              'Producto eliminado correctamente'
          });

          this.loadProducts();
        },

        error: err => {

          this.messageService.add({

            severity: 'error',

            summary: 'Error',

            detail:
              err.error?.error ??
              'No se pudo eliminar el producto'
          });
        }

      });
  }


  getStockSeverity(
    stock: number
  ):
    'success'
    | 'warn'
    | 'danger' {

    if (stock > 10) {
      return 'success';
    }

    if (stock > 0) {
      return 'warn';
    }

    return 'danger';
  }

  openSale(
    product: Product
  ): void {

    if (
      product.id == null ||
      product.stock <= 0
    ) {

      return;
    }


    this.selectedProduct =
      product;


    const quantityControl =
      this.saleForm
        .controls
        .quantity;


    quantityControl
      .setValidators([
        Validators.required,
        Validators.min(1),
        Validators.max(
          product.stock
        )
      ]);


    quantityControl
      .setValue(1);


    quantityControl
      .updateValueAndValidity();


    this.lastSale = null;

    this.saleDialogVisible =
      true;
  }


  closeSale(): void {

    if (this.saleLoading) {
      return;
    }

    this.saleDialogVisible =
      false;

    this.selectedProduct =
      null;

    this.saleForm.reset({
      quantity: 1
    });
  }


  get saleTotal(): number {

    if (!this.selectedProduct) {
      return 0;
    }

    const quantity =
      this.saleForm
        .controls
        .quantity
        .value;

    return (
      this.selectedProduct.precio
      * quantity
    );
  }


  submitSale(): void {

    if (
      this.saleForm.invalid ||
      this.selectedProduct?.id == null
    ) {

      this.saleForm
        .markAllAsTouched();

      return;
    }


    this.saleLoading =
      true;


    const quantity =
      this.saleForm
        .getRawValue()
        .quantity;


    this.saleService
      .create({

        productId:
          this.selectedProduct.id,

        quantity

      })
      .subscribe({

        next: sale => {

          this.lastSale =
            sale;


          this.messageService.add({

            severity: 'success',

            summary:
              'Venta realizada',

            detail:
              `${sale.quantity} unidad(es) de ` +
              `${sale.productName}. ` +
              `Total: $${Number(sale.total).toFixed(2)}`
          });


          this.saleLoading =
            false;

          this.saleDialogVisible =
            false;

          this.selectedProduct =
            null;

          this.loadProducts();
        },


        error: err => {

          this.saleLoading =
            false;


          this.messageService.add({

            severity: 'error',

            summary:
              'No se pudo realizar la venta',

            detail:
              err.error?.error ??
              err.error?.message ??
              'Error procesando la venta'
          });
          if (
            err.status === 409
          ) {

            this.saleDialogVisible =
              false;

            this.selectedProduct =
              null;

            this.loadProducts();
          }

        }

      });
  }

  logout(): void {

    this.authService.logout();
  }
}