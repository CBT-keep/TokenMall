export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  traceId?: string;
}

export interface PageResult<T> {
  records: T[];
  page: number;
  size: number;
  total: number;
}

export interface UserView {
  id: number;
  username: string;
  nickname: string;
  role: 'USER' | 'ADMIN' | string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserView;
}

export interface Category {
  id: number;
  name: string;
  code: string;
  sortOrder: number;
  status: number;
}

export interface ProductSummary {
  id: number;
  categoryId: number;
  productType: 'TOKEN_PACK' | 'TOKEN_PLAN' | string;
  name: string;
  subtitle?: string;
  coverUrl?: string;
  price: number;
  originalPrice?: number;
  tokenAmount?: number;
  planDays?: number;
  planQuota?: number;
  status: number;
  sortOrder: number;
}

export interface Sku {
  id: number;
  productId: number;
  skuCode: string;
  name: string;
  price: number;
  tokenAmount?: number;
  planDays?: number;
  planQuota?: number;
  status: number;
  totalStock?: number;
  availableStock?: number;
  lockedStock?: number;
}

export interface ProductDetail extends ProductSummary {
  description?: string;
  purchaseLimit?: number;
  skus: Sku[];
}

export interface CartItem {
  id: number;
  skuId: number;
  productId: number;
  productName: string;
  skuName: string;
  productType: string;
  price: number;
  quantity: number;
  selected: number;
  availableStock: number;
  tokenAmount?: number;
  planDays?: number;
  planQuota?: number;
}

export interface OrderItem {
  id: number;
  productId: number;
  skuId: number;
  productType: string;
  productName: string;
  skuName: string;
  unitPrice: number;
  quantity: number;
  tokenAmount?: number;
  planDays?: number;
  planQuota?: number;
}

export interface OrderSummary {
  id: number;
  orderNo: string;
  orderType: string;
  activityId?: number;
  payAmount: number;
  status: string;
  expireTime: string;
  createdAt: string;
}

export interface OrderDetail extends OrderSummary {
  totalAmount: number;
  payTime?: string;
  items: OrderItem[];
  statusLogs: Array<{
    fromStatus?: string;
    toStatus: string;
    operatorType: string;
    remark?: string;
    createdAt: string;
  }>;
}

export interface Payment {
  paymentNo: string;
  orderNo: string;
  amount: number;
  status: string;
  paidAt?: string;
}

export interface TokenAccount {
  packBalance: number;
  planBalance: number;
  totalPurchased: number;
  totalConsumed: number;
}

export interface TokenPlan {
  id: number;
  orderId: number;
  productId: number;
  skuId: number;
  startTime: string;
  endTime: string;
  totalQuota: number;
  usedQuota: number;
  remainingQuota: number;
  status: string;
}

export interface TokenTransaction {
  id: number;
  transactionNo: string;
  orderId?: number;
  planId?: number;
  balanceType: string;
  transactionType: string;
  amount: number;
  balanceAfter: number;
  description?: string;
  createdAt: string;
}

export interface SeckillActivity {
  id: number;
  productId: number;
  skuId: number;
  name: string;
  seckillPrice: number;
  seckillStock: number;
  soldCount: number;
  perUserLimit: number;
  startTime: string;
  endTime: string;
  status: string;
}

export interface SeckillResult {
  success: boolean;
  requestId: string;
  orderNo?: string;
  status: string;
  message?: string;
}

export interface DashboardData {
  productCount: number;
  pendingOrderCount: number;
  paidOrderCount: number;
  runningSeckillCount: number;
}
