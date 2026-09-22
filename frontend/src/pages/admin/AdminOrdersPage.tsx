import { Button, Drawer, message, Radio, Space, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import { getAdminOrder, listAdminOrders } from '../../api/admin';
import { errorMessage } from '../../api/client';
import { ORDER_STATUS_OPTIONS, orderStatusMeta } from '../../constants/business';
import type { OrderDetail, OrderSummary } from '../../types/api';

export default function AdminOrdersPage() {
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [status, setStatus] = useState('');
  const [detail, setDetail] = useState<OrderDetail>();
  const [loading, setLoading] = useState(true);
  const [detailLoading, setDetailLoading] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    listAdminOrders(status || undefined)
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

  const openDetail = async (orderNo: string) => {
    setDetailLoading(true);
    try {
      setDetail(await getAdminOrder(orderNo));
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setDetailLoading(false);
    }
  };

  return (
    <>
      <h1 className="page-title">订单管理</h1>
      <p className="page-subtitle">查看全部用户订单。</p>
      <Radio.Group
        value={status}
        onChange={(event) => setStatus(event.target.value)}
        options={[{ label: '全部', value: '' }, ...ORDER_STATUS_OPTIONS]}
        style={{ marginBottom: 16 }}
      />
      <Table<OrderSummary>
        rowKey="orderNo"
        loading={loading}
        dataSource={orders}
        scroll={{ x: 'max-content' }}
        columns={[
          { title: '订单号', dataIndex: 'orderNo' },
          { title: '类型', dataIndex: 'orderType' },
          { title: '金额', render: (_, item) => `¥${item.payAmount}` },
          {
            title: '状态',
            render: (_, item) => {
              const meta = orderStatusMeta(item.status);
              return <Tag color={meta.color}>{meta.label}</Tag>;
            },
          },
          { title: '创建时间', dataIndex: 'createdAt' },
          {
            title: '操作',
            render: (_, item) => (
              <Button
                type="link"
                loading={detailLoading}
                onClick={() => void openDetail(item.orderNo)}
              >
                详情
              </Button>
            ),
          },
        ]}
      />
      <Drawer
        title="订单详情"
        width={640}
        open={!!detail}
        onClose={() => setDetail(undefined)}
      >
        {detail && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <div>订单号：{detail.orderNo}</div>
            <div>状态：{orderStatusMeta(detail.status).label}</div>
            <div>金额：¥{detail.payAmount}</div>
            <Table
              rowKey="id"
              pagination={false}
              dataSource={detail.items}
              scroll={{ x: 'max-content' }}
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
