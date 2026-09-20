import { Button, DatePicker, Drawer, Form, Input, InputNumber, message, Modal, Popconfirm, Space, Table, Tag } from 'antd';
import dayjs, { type Dayjs } from 'dayjs';
import { useEffect, useState } from 'react';
import {
  createSeckillActivity,
  deleteSeckillActivity,
  getSeckillRecords,
  listAdminSeckillActivities,
  updateSeckillActivity,
} from '../../api/admin';
import { errorMessage } from '../../api/client';
import type { SeckillActivity } from '../../types/api';

interface SeckillFormValues {
  productId: number;
  skuId: number;
  name: string;
  seckillPrice: number;
  seckillStock: number;
  perUserLimit: number;
  timeRange: [Dayjs, Dayjs];
  status: string;
}

export default function AdminSeckillPage() {
  const [activities, setActivities] = useState<SeckillActivity[]>([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<SeckillActivity>();
  const [records, setRecords] = useState<unknown[]>([]);
  const [recordsOpen, setRecordsOpen] = useState(false);
  const [form] = Form.useForm<SeckillFormValues>();

  const load = () => {
    listAdminSeckillActivities().then(setActivities).catch((error) => message.error(errorMessage(error)));
  };

  useEffect(load, []);

  const openEditor = (item?: SeckillActivity) => {
    setEditing(item);
    form.setFieldsValue(
      item
        ? {
            ...item,
            timeRange: [dayjs(item.startTime), dayjs(item.endTime)],
          }
        : {
            status: 'RUNNING',
            seckillStock: 100,
            perUserLimit: 1,
          },
    );
    setOpen(true);
  };

  return (
    <>
      <div className="toolbar">
        <div>
          <h1 className="page-title">秒杀活动管理</h1>
          <p className="page-subtitle">配置 Token Plan 秒杀库存和时间。</p>
        </div>
        <Button type="primary" onClick={() => openEditor()}>新增活动</Button>
      </div>
      <Table<SeckillActivity>
        rowKey="id"
        dataSource={activities}
        columns={[
          { title: 'ID', dataIndex: 'id' },
          { title: '名称', dataIndex: 'name' },
          { title: 'SKU', dataIndex: 'skuId' },
          { title: '秒杀价', render: (_, item) => `¥${item.seckillPrice}` },
          { title: '库存', render: (_, item) => `${item.soldCount}/${item.seckillStock}` },
          { title: '状态', render: (_, item) => <Tag>{item.status}</Tag> },
          {
            title: '操作',
            render: (_, item) => (
              <Space>
                <Button type="link" onClick={() => openEditor(item)}>编辑</Button>
                <Button
                  type="link"
                  onClick={async () => {
                    setRecords(await getSeckillRecords(item.id));
                    setRecordsOpen(true);
                  }}
                >
                  记录
                </Button>
                <Popconfirm
                  title="确认删除？"
                  onConfirm={async () => {
                    await deleteSeckillActivity(item.id);
                    message.success('已删除');
                    load();
                  }}
                >
                  <Button type="link" danger>删除</Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />
      <Modal
        title={editing ? '编辑秒杀活动' : '新增秒杀活动'}
        open={open}
        onCancel={() => setOpen(false)}
        onOk={() => form.submit()}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={async (values) => {
            const payload = {
              ...values,
              startTime: values.timeRange[0].format('YYYY-MM-DDTHH:mm:ss'),
              endTime: values.timeRange[1].format('YYYY-MM-DDTHH:mm:ss'),
            };
            delete (payload as Partial<SeckillFormValues>).timeRange;
            try {
              if (editing) {
                await updateSeckillActivity(editing.id, payload);
              } else {
                await createSeckillActivity(payload);
              }
              message.success('保存成功');
              setOpen(false);
              load();
            } catch (error) {
              message.error(errorMessage(error));
            }
          }}
        >
          <Form.Item name="name" label="活动名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item name="productId" label="商品 ID" rules={[{ required: true }]}><InputNumber /></Form.Item>
            <Form.Item name="skuId" label="SKU ID" rules={[{ required: true }]}><InputNumber /></Form.Item>
          </Space>
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item name="seckillPrice" label="秒杀价" rules={[{ required: true }]}><InputNumber min={0} precision={2} /></Form.Item>
            <Form.Item name="seckillStock" label="活动库存" rules={[{ required: true }]}><InputNumber min={1} /></Form.Item>
            <Form.Item name="perUserLimit" label="限购" rules={[{ required: true }]}><InputNumber min={1} /></Form.Item>
          </Space>
          <Form.Item name="timeRange" label="活动时间" rules={[{ required: true }]}>
            <DatePicker.RangePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="status" label="状态"><Input /></Form.Item>
        </Form>
      </Modal>
      <Drawer title="秒杀记录" width={640} open={recordsOpen} onClose={() => setRecordsOpen(false)}>
        <pre>{JSON.stringify(records, null, 2)}</pre>
      </Drawer>
    </>
  );
}
