import { Button, Drawer, message, Radio, Space, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import { getAdminOrder, listAdminOrders } from '../../api/admin';
import { errorMessage } from '../../api/client';
import type { OrderDetail, OrderSummary } from '../../types/api';

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [status, setStatus] = useState('');
  const [detail, setDetail] = useState<OrderDetail>();
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    listAdminOrders(status || undefined)
      .then((page) => setOrders(page.records))
      .catch((error) => message.error(errorMessage(error)))
      .finally(() => setLoading(false));
  };

  useEffect(load, [status]);

  return (
    <>
      <h1 className="page-title">订单管理</h1>
      <p className="page-subtitle">查看全部用户订单。</p>
      <Radio.Group value={status} onChange={(event) => setStatus(event.target.value)} style={{ marginBottom: 16 }}>
        <Radio.Button value="">全部</Radio.Button>
        <Radio.Button value="PENDING_PAYMENT">待支付</Radio.Button>
        <Radio.Button value="PAID">已支付</Radio.Button>
        <Radio.Button value="CLOSED">已关闭</Radio.Button>
      </Radio.Group>
      <Table<OrderSummary>
        rowKey="orderNo"
        loading={loading}
        dataSource={orders}
        columns={[
          { title: '订单号', dataIndex: 'orderNo' },
          { title: '类型', dataIndex: 'orderType' },
          { title: '金额', render: (_, item) => `¥${item.payAmount}` },
          { title: '状态', render: (_, item) => <Tag>{item.status}</Tag> },
          { title: '创建时间', dataIndex: 'createdAt' },
          {
            title: '操作',
            render: (_, item) => (
              <Button
                type="link"
                onClick={async () => {
                  try {
                    setDetail(await getAdminOrder(item.orderNo));
                  } catch (error) {
                    message.error(errorMessage(error));
                  }
                }}
              >
                详情
              </Button>
            ),
          },
        ]}
      />
      <Drawer title="订单详情" width={640} open={!!detail} onClose={() => setDetail(undefined)}>
        {detail && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div>订单号：{detail.orderNo}</div>
            <div>状态：{detail.status}</div>
            <div>金额：¥{detail.payAmount}</div>
            <Table
              rowKey="id"
              pagination={false}
              dataSource={detail.items}
              columns={[
                { title: '商品', dataIndex: 'productName' },
                { title: 'SKU', dataIndex: 'skuName' },
                { title: '单价', render: (_, item) => `¥${item.unitPrice}` },
                { title: '数量', dataIndex: 'quantity' },
              ]}
            />
          </Space>
        )}
      </Drawer>
    </>
  );
}
