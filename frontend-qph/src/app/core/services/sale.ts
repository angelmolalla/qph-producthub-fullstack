import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  CreateSaleRequest,
  SaleResponse
} from '../../shared/models/sale.model';

@Injectable({
  providedIn: 'root'
})
export class SaleService {

  private readonly apiUrl =
    'http://localhost:8080/api/sales';

  constructor(
    private http: HttpClient
  ) {}

  create(
    request: CreateSaleRequest
  ): Observable<SaleResponse> {

    return this.http.post<SaleResponse>(
      this.apiUrl,
      request
    );
  }

  getMySales(): Observable<SaleResponse[]> {

    return this.http.get<SaleResponse[]>(
      `${this.apiUrl}/me`
    );
  }

  getAll(): Observable<SaleResponse[]> {

    return this.http.get<SaleResponse[]>(
      this.apiUrl
    );
  }

  getById(
    id: string
  ): Observable<SaleResponse> {

    return this.http.get<SaleResponse>(
      `${this.apiUrl}/${id}`
    );
  }
}