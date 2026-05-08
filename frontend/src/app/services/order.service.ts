import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiConfigService } from '../core/api-config.service';
import { Order, OrderPayload } from '../core/models';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly apiConfig = inject(ApiConfigService);

  findAll(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiConfig.salesApiUrl}/orders`);
  }

  create(payload: OrderPayload): Observable<Order> {
    return this.http.post<Order>(`${this.apiConfig.salesApiUrl}/orders`, payload);
  }

  update(id: number, payload: OrderPayload): Observable<Order> {
    return this.http.put<Order>(`${this.apiConfig.salesApiUrl}/orders/${id}`, payload);
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiConfig.salesApiUrl}/orders/${id}`);
  }
}
