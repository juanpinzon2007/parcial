import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiConfigService } from '../core/api-config.service';
import { InventorySummary, Product, ProductPayload } from '../core/models';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly apiConfig = inject(ApiConfigService);

  findAll(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiConfig.inventoryApiUrl}/products`);
  }

  getSummary(): Observable<InventorySummary> {
    return this.http.get<InventorySummary>(`${this.apiConfig.inventoryApiUrl}/products/summary`);
  }

  create(payload: ProductPayload): Observable<Product> {
    return this.http.post<Product>(`${this.apiConfig.inventoryApiUrl}/products`, payload);
  }

  update(id: number, payload: ProductPayload): Observable<Product> {
    return this.http.put<Product>(`${this.apiConfig.inventoryApiUrl}/products/${id}`, payload);
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiConfig.inventoryApiUrl}/products/${id}`);
  }
}
