import { DeleteOutlined } from '@ant-design/icons';
import { Button, Empty, InputNumber, message, Space, Table, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { deleteCartItem, listCart, updateCartItem } from '../api/cart';
import { errorMessage } from '../api/client';
import { createOrderFromCart } from '../api/orders';
import { newRequestId } from '../components/RequestId';
import type { CartItem } from '../types/api';

export default function CartPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState<CartItem[]>([]);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const load = () => {
    setLoading(true);
    listCart()
      .then((data) => {
        setItems(data);
        setSelectedIds((current) => current.filter((id) => data.some((item) => item.id === id)));
      })
      .catch((error) => message.error(errorMessage(error)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const selectedTotal = useMemo(
    () =>
      items
        .filter((item) => selectedIds.includes(item.id))
        .reduce((sum, item) => sum + item.price * item.quantity, 0),
    [items, selectedIds],
  );

  const submit = async () => {
    if (!selectedIds.length) {
      message.warning('请选择要结算的商品');
      return;
    }
    setSubmitting(true);
    try {
      const order = await createOrderFromCart(selectedIds, newRequestId('cart'));
      navigate(`/orders/${order.orderNo}`);
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="page-shell">
      <h1 className="page-title">购物车</h1>
      <p className="page-subtitle">选择商品后创建订单。</p>
      {!loading && items.length === 0 ? (
        <Empty description="购物车为空" />
      ) : (
        <>
          <Table<CartItem>
            rowKey="id"
            loading={loading}
            dataSource={items}
            rowSelection={{
              selectedRowKeys: selectedIds,
              onChange: (keys) => setSelectedIds(keys.map(Number)),
            }}
            columns={[
              { title: '商品', dataIndex: 'productName' },
              { title: 'SKU', dataIndex: 'skuName' },
              { title: '单价', render: (_, item) => `¥${item.price}` },
              {
                title: '数量',
                render: (_, item) => (
                  <InputNumber
                    min={1}
                    max={item.availableStock}
                    value={item.quantity}
                    onChange={async (value) => {
                      if (!value) return;
                      await updateCartItem(item.id, value, item.selected === 1);
                      load();
                    }}
                  />
                ),
              },
              {
                title: '小计',
                render: (_, item) => `¥${(item.price * item.quantity).toFixed(2)}`,
              },
              {
                title: '操作',
                render: (_, item) => (
                  <Button
                    type="text"
                    danger
                    icon={<DeleteOutlined />}
                    onClick={async () => {
                      await deleteCartItem(item.id);
                      load();
                    }}
                  />
                ),
              },
            ]}
          />
          <div className="toolbar" style={{ marginTop: 20 }}>
            <Typography.Text>
              已选金额：<span className="price">¥{selectedTotal.toFixed(2)}</span>
            </Typography.Text>
            <Space>
              <Button onClick={load}>刷新</Button>
              <Button type="primary" loading={submitting} onClick={submit}>
                去结算
              </Button>
            </Space>
          </div>
        </>
      )}
    </div>
  );
}
