import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { InventorySummary, Order, OrderPayload, OrderStatus, Product, ProductPayload } from '../core/models';
import { OrderService } from '../services/order.service';
import { ProductService } from '../services/product.service';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css'
})
export class DashboardPageComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly productService = inject(ProductService);
  private readonly orderService = inject(OrderService);

  readonly productForm = this.formBuilder.nonNullable.group({
    name: ['', Validators.required],
    brand: ['', Validators.required],
    category: ['', Validators.required],
    description: [''],
    price: [0, [Validators.required, Validators.min(0.01)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    active: [true, Validators.required]
  });

  readonly orderForm = this.formBuilder.group({
    notes: [''],
    status: ['PENDING' as OrderStatus, Validators.required],
    items: this.formBuilder.array([this.createItemGroup()])
  });

  readonly statusOptions: OrderStatus[] = ['PENDING', 'PAID', 'DELIVERED', 'CANCELLED'];

  products: Product[] = [];
  orders: Order[] = [];
  summary: InventorySummary | null = null;

  loading = true;
  savingProduct = false;
  savingOrder = false;
  activePanel: 'products' | 'orders' = 'products';
  feedbackMessage = '';
  errorMessage = '';
  editingProductId: number | null = null;
  editingOrderId: number | null = null;

  get currentUser() {
    return this.authService.user;
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin;
  }

  get orderItems(): FormArray {
    return this.orderForm.controls.items;
  }

  get totalSalesAmount(): number {
    return this.orders.reduce((total, order) => total + order.total, 0);
  }

  get pendingOrders(): number {
    return this.orders.filter((order) => order.status === 'PENDING').length;
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      products: this.productService.findAll(),
      summary: this.productService.getSummary(),
      orders: this.orderService.findAll()
    }).subscribe({
      next: ({ products, summary, orders }) => {
        this.products = products;
        this.summary = summary;
        this.orders = orders;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.loading = false;
        this.errorMessage = extractErrorMessage(error);
      }
    });
  }

  submitProduct(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    this.savingProduct = true;
    this.feedbackMessage = '';
    this.errorMessage = '';
    const payload = this.productForm.getRawValue() as ProductPayload;

    const request$ =
      this.editingProductId === null
        ? this.productService.create(payload)
        : this.productService.update(this.editingProductId, payload);

    request$.subscribe({
      next: () => {
        this.savingProduct = false;
        this.feedbackMessage = this.editingProductId === null ? 'Producto creado' : 'Producto actualizado';
        this.cancelProductEdit();
        this.loadData();
      },
      error: (error: HttpErrorResponse) => {
        this.savingProduct = false;
        this.errorMessage = extractErrorMessage(error);
      }
    });
  }

  editProduct(product: Product): void {
    this.activePanel = 'products';
    this.editingProductId = product.id;
    this.productForm.reset({
      name: product.name,
      brand: product.brand,
      category: product.category,
      description: product.description ?? '',
      price: product.price,
      stock: product.stock,
      active: product.active
    });
  }

  cancelProductEdit(): void {
    this.editingProductId = null;
    this.productForm.reset({
      name: '',
      brand: '',
      category: '',
      description: '',
      price: 0,
      stock: 0,
      active: true
    });
  }

  removeProduct(id: number): void {
    this.errorMessage = '';
    this.feedbackMessage = '';

    this.productService.remove(id).subscribe({
      next: () => {
        this.feedbackMessage = 'Producto eliminado';
        this.loadData();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = extractErrorMessage(error);
      }
    });
  }

  submitOrder(): void {
    if (this.orderForm.invalid) {
      this.orderForm.markAllAsTouched();
      return;
    }

    this.savingOrder = true;
    this.feedbackMessage = '';
    this.errorMessage = '';
    const payload = this.buildOrderPayload();

    const request$ =
      this.editingOrderId === null
        ? this.orderService.create(payload)
        : this.orderService.update(this.editingOrderId, payload);

    request$.subscribe({
      next: () => {
        this.savingOrder = false;
        this.feedbackMessage = this.editingOrderId === null ? 'Pedido creado' : 'Pedido actualizado';
        this.cancelOrderEdit();
        this.loadData();
      },
      error: (error: HttpErrorResponse) => {
        this.savingOrder = false;
        this.errorMessage = extractErrorMessage(error);
      }
    });
  }

  editOrder(order: Order): void {
    this.activePanel = 'orders';
    this.editingOrderId = order.id;
    this.orderItems.clear();

    for (const item of order.items) {
      this.orderItems.push(
        this.formBuilder.group({
          productName: [item.productName, Validators.required],
          quantity: [item.quantity, [Validators.required, Validators.min(1)]],
          unitPrice: [item.unitPrice, [Validators.required, Validators.min(0.01)]]
        })
      );
    }

    this.orderForm.patchValue({
      notes: order.notes ?? '',
      status: order.status
    });
  }

  cancelOrderEdit(): void {
    this.editingOrderId = null;
    this.orderItems.clear();
    this.orderItems.push(this.createItemGroup());
    this.orderForm.reset({
      notes: '',
      status: 'PENDING'
    });
  }

  removeOrder(id: number): void {
    this.errorMessage = '';
    this.feedbackMessage = '';

    this.orderService.remove(id).subscribe({
      next: () => {
        this.feedbackMessage = 'Pedido eliminado';
        this.loadData();
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = extractErrorMessage(error);
      }
    });
  }

  addOrderItem(): void {
    this.orderItems.push(this.createItemGroup());
  }

  removeOrderItem(index: number): void {
    if (this.orderItems.length === 1) {
      return;
    }
    this.orderItems.removeAt(index);
  }

  fillPriceFromProduct(index: number): void {
    const group = this.orderItems.at(index);
    const productName = group.get('productName')?.value as string | null;
    const product = this.products.find((item) => item.name === productName);
    if (!product) {
      return;
    }
    group.patchValue({
      unitPrice: product.price
    });
  }

  changePanel(panel: 'products' | 'orders'): void {
    this.activePanel = panel;
    this.feedbackMessage = '';
    this.errorMessage = '';
  }

  logout(): void {
    this.authService.logout();
  }

  private buildOrderPayload(): OrderPayload {
    const value = this.orderForm.getRawValue();
    return {
      notes: value.notes?.trim() ? value.notes.trim() : null,
      status: (value.status ?? 'PENDING') as OrderStatus,
      items: (value.items ?? []).map((item) => ({
        productName: String(item?.productName ?? '').trim(),
        quantity: Number(item?.quantity ?? 0),
        unitPrice: Number(item?.unitPrice ?? 0)
      }))
    };
  }

  private createItemGroup() {
    return this.formBuilder.group({
      productName: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      unitPrice: [0, [Validators.required, Validators.min(0.01)]]
    });
  }
}

function extractErrorMessage(error: HttpErrorResponse): string {
  if (typeof error.error?.message === 'string') {
    return error.error.message;
  }

  return 'No fue posible completar la operacion';
}
