import { Suspense, lazy } from 'react';
import { Skeleton } from 'antd';
import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { RequireAuth } from './auth/RequireAuth';
import AdminLayout from './layouts/AdminLayout';
import MainLayout from './layouts/MainLayout';

const LoginPage = lazy(() => import('./pages/LoginPage'));
const HomePage = lazy(() => import('./pages/HomePage'));
const ProductsPage = lazy(() => import('./pages/ProductsPage'));
const ProductDetailPage = lazy(() => import('./pages/ProductDetailPage'));
const CartPage = lazy(() => import('./pages/CartPage'));
const OrdersPage = lazy(() => import('./pages/OrdersPage'));
const OrderDetailPage = lazy(() => import('./pages/OrderDetailPage'));
const TokenAccountPage = lazy(() => import('./pages/TokenAccountPage'));
const SeckillPage = lazy(() => import('./pages/SeckillPage'));
const AdminDashboardPage = lazy(() => import('./pages/admin/AdminDashboardPage'));
const AdminCategoriesPage = lazy(() => import('./pages/admin/AdminCategoriesPage'));
const AdminProductsPage = lazy(() => import('./pages/admin/AdminProductsPage'));
const AdminInventoryPage = lazy(() => import('./pages/admin/AdminInventoryPage'));
const AdminOrdersPage = lazy(() => import('./pages/admin/AdminOrdersPage'));
const AdminSeckillPage = lazy(() => import('./pages/admin/AdminSeckillPage'));

function RouteFallback() {
  return <div className="page-shell"><Skeleton active paragraph={{ rows: 8 }} /></div>;
}

function RouteSuspense() {
  return (
    <Suspense fallback={<RouteFallback />}>
      <Outlet />
    </Suspense>
  );
}

export default function App() {
  return (
    <Suspense fallback={<RouteFallback />}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          element={
            <RequireAuth>
              <MainLayout />
            </RequireAuth>
          }
        >
          <Route element={<RouteSuspense />}>
            <Route index element={<HomePage />} />
            <Route path="/products" element={<ProductsPage />} />
            <Route path="/products/:id" element={<ProductDetailPage />} />
            <Route path="/cart" element={<CartPage />} />
            <Route path="/orders" element={<OrdersPage />} />
            <Route path="/orders/:orderNo" element={<OrderDetailPage />} />
            <Route path="/token" element={<TokenAccountPage />} />
            <Route path="/seckill" element={<SeckillPage />} />
          </Route>
        </Route>
        <Route
          path="/admin"
          element={
            <RequireAuth admin>
              <AdminLayout />
            </RequireAuth>
          }
        >
          <Route element={<RouteSuspense />}>
            <Route index element={<AdminDashboardPage />} />
            <Route path="categories" element={<AdminCategoriesPage />} />
            <Route path="products" element={<AdminProductsPage />} />
            <Route path="inventory" element={<AdminInventoryPage />} />
            <Route path="orders" element={<AdminOrdersPage />} />
            <Route path="seckill" element={<AdminSeckillPage />} />
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}
