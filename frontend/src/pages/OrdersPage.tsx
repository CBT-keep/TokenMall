import { Button, Empty, message, Radio, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { errorMessage } from '../api/client';
import { listOrders } from '../api/orders';
import { ORDER_STATUS_OPTIONS, orderStatusMeta } from '../constants/business';
import type { OrderSummary } from '../types/api';

export default function OrdersPage() {
  const navigate = useNavigate();
  const [status, setStatus] = useState('');
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    listOrders(status || undefined, 1, 50)
      .then((page) => {
        if (active) setOrders(page.records);
      })
      .catch((error) => {
        if (active) message.error(errorMessage(error));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [status]);

  return (
    <div className="page-shell">
      <h1 className="page-title">我的订单</h1>
      <p className="page-subtitle">查看支付状态和订单详情。</p>
      <Radio.Group
        value={status}
        onChange={(event) => setStatus(event.target.value)}
        options={[{ label: '全部', value: '' }, ...ORDER_STATUS_OPTIONS]}
        style={{ marginBottom: 16 }}
      />
      {!loading && orders.length === 0 ? (
        <Empty description="暂无订单" />
      ) : (
        <Table<OrderSummary>
          rowKey="orderNo"
          loading={loading}
          dataSource={orders}
          scroll={{ x: 'max-content' }}
          columns={[
            { title: '订单号', dataIndex: 'orderNo' },
            { title: '类型', dataIndex: 'orderType' },
            { title: '金额', render: (_, order) => `¥${order.payAmount}` },
            {
              title: '状态',
              render: (_, order) => {
                const meta = orderStatusMeta(order.status);
                return <Tag color={meta.color}>{meta.label}</Tag>;
              },
            },
            { title: '创建时间', dataIndex: 'createdAt' },
            {
              title: '操作',
              render: (_, order) => (
                <Button type="link" onClick={() => navigate(`/orders/${order.orderNo}`)}>
                  查看
                </Button>
              ),
            },
          ]}
        />
      )}
    </div>
  );
}
