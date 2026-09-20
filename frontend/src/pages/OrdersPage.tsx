import { Button, Empty, message, Radio, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { errorMessage } from '../api/client';
import { listOrders } from '../api/orders';
import type { OrderSummary } from '../types/api';

const statusColor: Record<string, string> = {
  PENDING_PAYMENT: 'orange',
  PAID: 'green',
  COMPLETED: 'blue',
  CANCELLED: 'default',
  CLOSED: 'red',
};

export default function OrdersPage() {
  const navigate = useNavigate();
  const [status, setStatus] = useState('');
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    listOrders(status || undefined, 1, 50)
      .then((page) => setOrders(page.records))
      .catch((error) => message.error(errorMessage(error)))
      .finally(() => setLoading(false));
  }, [status]);

  return (
    <div className="page-shell">
      <h1 className="page-title">我的订单</h1>
      <p className="page-subtitle">查看支付状态和订单详情。</p>
      <Radio.Group value={status} onChange={(event) => setStatus(event.target.value)} style={{ marginBottom: 16 }}>
        <Radio.Button value="">全部</Radio.Button>
        <Radio.Button value="PENDING_PAYMENT">待支付</Radio.Button>
        <Radio.Button value="PAID">已支付</Radio.Button>
        <Radio.Button value="CLOSED">已关闭</Radio.Button>
      </Radio.Group>
      {!loading && orders.length === 0 ? (
        <Empty description="暂无订单" />
      ) : (
        <Table<OrderSummary>
          rowKey="orderNo"
          loading={loading}
          dataSource={orders}
          columns={[
            { title: '订单号', dataIndex: 'orderNo' },
            { title: '类型', dataIndex: 'orderType' },
            { title: '金额', render: (_, order) => `¥${order.payAmount}` },
            {
              title: '状态',
              render: (_, order) => <Tag color={statusColor[order.status]}>{order.status}</Tag>,
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
