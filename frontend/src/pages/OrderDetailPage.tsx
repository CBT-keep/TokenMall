import { Button, Card, Descriptions, message, Popconfirm, Space, Steps, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { errorMessage } from '../api/client';
import { cancelOrder, getOrder } from '../api/orders';
import { mockPayment } from '../api/payments';
import { newRequestId } from '../components/RequestId';
import type { OrderDetail } from '../types/api';

export default function OrderDetailPage() {
  const { orderNo } = useParams();
  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);

  const load = () => {
    if (!orderNo) return;
    setLoading(true);
    getOrder(orderNo)
      .then(setOrder)
      .catch((error) => message.error(errorMessage(error)))
      .finally(() => setLoading(false));
  };

  useEffect(load, [orderNo]);

  const pay = async () => {
    if (!orderNo) return;
    setPaying(true);
    try {
      await mockPayment(orderNo, newRequestId('pay'));
      message.success('支付成功，Token 已发放');
      load();
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setPaying(false);
    }
  };

  if (!order) {
    return <div className="page-shell">{loading ? '加载中...' : '订单不存在'}</div>;
  }

  const currentStep = order.status === 'PENDING_PAYMENT' ? 1 : order.status === 'PAID' ? 2 : 0;

  return (
    <div className="page-shell">
      <h1 className="page-title">订单详情</h1>
      <p className="page-subtitle">{order.orderNo}</p>
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Steps
            current={currentStep}
            items={[
              { title: '创建订单' },
              { title: '待支付' },
              { title: '已支付' },
              { title: '完成' },
            ]}
          />
          <Descriptions column={2}>
            <Descriptions.Item label="订单类型">{order.orderType}</Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={order.status === 'PAID' ? 'green' : 'orange'}>{order.status}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="订单金额">¥{order.totalAmount}</Descriptions.Item>
            <Descriptions.Item label="实付金额">¥{order.payAmount}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{order.createdAt}</Descriptions.Item>
            <Descriptions.Item label="过期时间">{order.expireTime}</Descriptions.Item>
          </Descriptions>

          <Table
            rowKey="id"
            pagination={false}
            dataSource={order.items}
            columns={[
              { title: '商品', dataIndex: 'productName' },
              { title: 'SKU', dataIndex: 'skuName' },
              { title: '单价', render: (_, item) => `¥${item.unitPrice}` },
              { title: '数量', dataIndex: 'quantity' },
              { title: '小计', render: (_, item) => `¥${(item.unitPrice * item.quantity).toFixed(2)}` },
            ]}
          />

          {order.status === 'PENDING_PAYMENT' && (
            <Space>
              <Button type="primary" loading={paying} onClick={pay}>
                模拟支付成功
              </Button>
              <Popconfirm
                title="确认取消订单？"
                onConfirm={async () => {
                  await cancelOrder(order.orderNo);
                  message.success('订单已取消');
                  load();
                }}
              >
                <Button danger>取消订单</Button>
              </Popconfirm>
            </Space>
          )}
        </Space>
      </Card>
    </div>
  );
}
