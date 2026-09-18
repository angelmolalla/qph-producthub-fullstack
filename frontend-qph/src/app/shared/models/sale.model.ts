export type SaleStatus =
  | 'COMPLETED'
  | 'CANCELLED';

export interface CreateSaleRequest {
  productId: number;
  quantity: number;
}

export interface SaleResponse {
  id: string;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  total: number;
  username: string;
  status: SaleStatus;
  createdAt: string;
  remainingStock: number | null;
}