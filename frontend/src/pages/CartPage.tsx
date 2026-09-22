import { DeleteOutlined } from '@ant-design/icons';
import { Button, Empty, InputNumber, message, Space, Table, Typography } from 'antd';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
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
  const [quantityDrafts, setQuantityDrafts] = useState<Record<number, number | null>>({});
  const [updatingIds, setUpdatingIds] = useState<number[]>([]);
  const [deletingIds, setDeletingIds] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const quantityRequests = useRef(new Map<number, Promise<CartItem>>());

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await listCart();
      setItems(data);
      setQuantityDrafts({});
      setSelectedIds((current) => current.filter((id) => data.some((item) => item.id === id)));
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const selectedTotal = useMemo(
    () =>
      items
        .filter((item) => selectedIds.includes(item.id))
        .reduce((sum, item) => sum + item.price * item.quantity, 0),
    [items, selectedIds],
  );

  const persistQuantity = (item: CartItem, quantity: number): Promise<CartItem> => {
    const currentRequest = quantityRequests.current.get(item.id);
    if (currentRequest) {
      return currentRequest;
    }

    const request = updateCartItem(item.id, quantity, item.selected === 1)
      .then((updated) => {
        setItems((current) => current.map((currentItem) => (
          currentItem.id === item.id ? updated : currentItem
        )));
        return updated;
      })
      .finally(() => {
        quantityRequests.current.delete(item.id);
      });
    quantityRequests.current.set(item.id, request);
    return request;
  };

  const commitQuantity = async (item: CartItem): Promise<boolean> => {
    const quantity = quantityDrafts[item.id];
    if (quantity == null || quantity === item.quantity) {
      setQuantityDrafts((current) => ({ ...current, [item.id]: null }));
      return true;
    }

    setUpdatingIds((current) => (
      current.includes(item.id) ? current : [...current, item.id]
    ));
    try {
      await persistQuantity(item, quantity);
      setQuantityDrafts((current) => ({ ...current, [item.id]: null }));
      return true;
    } catch (error) {
      message.error(errorMessage(error));
      setQuantityDrafts((current) => ({ ...current, [item.id]: null }));
      return false;
    } finally {
      setUpdatingIds((current) => current.filter((id) => id !== item.id));
    }
  };

  const removeItem = async (item: CartItem) => {
    setDeletingIds((current) => [...current, item.id]);
    try {
      await deleteCartItem(item.id);
      setItems((current) => current.filter((currentItem) => currentItem.id !== item.id));
      setSelectedIds((current) => current.filter((id) => id !== item.id));
      setQuantityDrafts((current) => {
        const next = { ...current };
        delete next[item.id];
        return next;
      });
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setDeletingIds((current) => current.filter((id) => id !== item.id));
    }
  };

  const submit = async () => {
    if (!selectedIds.length) {
      message.warning('请选择要结算的商品');
      return;
    }
    setSubmitting(true);
    try {
      const pendingItems = items.filter((item) => (
        selectedIds.includes(item.id)
        && quantityDrafts[item.id] != null
        && quantityDrafts[item.id] !== item.quantity
      ));
      const commits = await Promise.all(pendingItems.map((item) => commitQuantity(item)));
      if (commits.some((committed) => !committed)) {
        return;
      }
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
            pagination={false}
            scroll={{ x: 'max-content' }}
            rowSelection={{
              selectedRowKeys: selectedIds,
              onChange: (keys) => setSelectedIds(keys.map(Number)),
              getCheckboxProps: (item) => ({ disabled: deletingIds.includes(item.id) }),
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
                    max={Math.max(item.availableStock, 1)}
                    value={quantityDrafts[item.id] ?? item.quantity}
                    disabled={updatingIds.includes(item.id)}
                    onChange={(value) => {
                      setQuantityDrafts((current) => ({ ...current, [item.id]: value }));
                    }}
                    onBlur={() => void commitQuantity(item)}
                    onPressEnter={() => void commitQuantity(item)}
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
                    loading={deletingIds.includes(item.id)}
                    onClick={() => void removeItem(item)}
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
              <Button onClick={() => void load()} loading={loading}>刷新</Button>
              <Button
                type="primary"
                loading={submitting}
                disabled={!selectedIds.length || loading}
                onClick={submit}
              >
                去结算
              </Button>
            </Space>
          </div>
        </>
      )}
    </div>
  );
}
