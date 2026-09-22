import {
  Button,
  Col,
  DatePicker,
  Drawer,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Table,
  Tag,
} from 'antd';
import dayjs, { type Dayjs } from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import {
  createSeckillActivity,
  deleteSeckillActivity,
  getSeckillRecords,
  listAdminProducts,
  listAdminSeckillActivities,
  listAdminSkus,
  updateSeckillActivity,
} from '../../api/admin';
import { errorMessage } from '../../api/client';
import {
  SECKILL_STATUS_OPTIONS,
  productTypeLabel,
  seckillStatusMeta,
} from '../../constants/business';
import type {
  ProductSummary,
  SeckillActivity,
  SeckillRecord,
  Sku,
} from '../../types/api';

interface SeckillFormValues {
  productId: number;
  skuId: number;
  name: string;
  seckillPrice: number;
  seckillStock: number;
  perUserLimit: number;
  timeRange: [Dayjs, Dayjs];
  status?: string;
}

export default function AdminSeckillPage() {
  const [activities, setActivities] = useState<SeckillActivity[]>([]);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [skus, setSkus] = useState<Sku[]>([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<SeckillActivity>();
  const [records, setRecords] = useState<SeckillRecord[]>([]);
  const [recordsOpen, setRecordsOpen] = useState(false);
  const [recordsLoading, setRecordsLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<number>();
  const [form] = Form.useForm<SeckillFormValues>();
  const selectedProductId = Form.useWatch('productId', form);

  const productMap = useMemo(
    () => new Map(products.map((product) => [product.id, product])),
    [products],
  );
  const skuMap = useMemo(
    () => new Map(skus.map((sku) => [sku.id, sku])),
    [skus],
  );
  const skuOptions = skus
    .filter((sku) => sku.productId === selectedProductId)
    .map((sku) => ({ label: `${sku.name}（¥${sku.price}）`, value: sku.id }));

  const load = async () => {
    setLoading(true);
    try {
      const [activityData, productData, skuData] = await Promise.all([
        listAdminSeckillActivities(),
        listAdminProducts(),
        listAdminSkus(),
      ]);
      setActivities(activityData);
      setProducts(productData);
      setSkus(skuData);
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const openEditor = (item?: SeckillActivity) => {
    setEditing(item);
    form.resetFields();
    if (item) {
      form.setFieldsValue({
        productId: item.productId,
        skuId: item.skuId,
        name: item.name,
        seckillPrice: item.seckillPrice,
        seckillStock: item.seckillStock,
        perUserLimit: item.perUserLimit,
        timeRange: [dayjs(item.startTime), dayjs(item.endTime)],
        status: item.status,
      });
    }
    setOpen(true);
  };

  const openRecords = async (item: SeckillActivity) => {
    setRecords([]);
    setRecordsOpen(true);
    setRecordsLoading(true);
    try {
      setRecords(await getSeckillRecords(item.id));
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setRecordsLoading(false);
    }
  };

  return (
    <>
      <div className="toolbar">
        <div>
          <h1 className="page-title">秒杀活动管理</h1>
          <p className="page-subtitle">配置 Token Plan 秒杀库存和时间。</p>
        </div>
        <Space>
          <Button onClick={() => void load()} loading={loading}>刷新</Button>
          <Button type="primary" onClick={() => openEditor()}>新增活动</Button>
        </Space>
      </div>
      <Table<SeckillActivity>
        rowKey="id"
        loading={loading}
        dataSource={activities}
        scroll={{ x: 'max-content' }}
        columns={[
          { title: 'ID', dataIndex: 'id' },
          { title: '名称', dataIndex: 'name' },
          {
            title: '商品',
            render: (_, item) => productMap.get(item.productId)?.name ?? `#${item.productId}`,
          },
          {
            title: 'SKU',
            render: (_, item) => skuMap.get(item.skuId)?.name ?? `#${item.skuId}`,
          },
          { title: '秒杀价', render: (_, item) => `¥${item.seckillPrice}` },
          { title: '库存', render: (_, item) => `${item.soldCount}/${item.seckillStock}` },
          {
            title: '状态',
            render: (_, item) => {
              const meta = seckillStatusMeta(item.status);
              return <Tag color={meta.color}>{meta.label}</Tag>;
            },
          },
          {
            title: '活动时间',
            render: (_, item) => `${dayjs(item.startTime).format('MM-DD HH:mm')} 至 ${dayjs(item.endTime).format('MM-DD HH:mm')}`,
          },
          {
            title: '操作',
            render: (_, item) => (
              <Space>
                <Button type="link" onClick={() => openEditor(item)}>编辑</Button>
                <Button type="link" onClick={() => void openRecords(item)}>记录</Button>
                <Popconfirm
                  title="确认删除？"
                  onConfirm={async () => {
                    setDeletingId(item.id);
                    try {
                      await deleteSeckillActivity(item.id);
                      message.success('已删除');
                      await load();
                    } catch (error) {
                      message.error(errorMessage(error));
                    } finally {
                      setDeletingId(undefined);
                    }
                  }}
                >
                  <Button type="link" danger loading={deletingId === item.id}>删除</Button>
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />
      <Modal
        title={editing ? '编辑秒杀活动' : '新增秒杀活动'}
        open={open}
        width={760}
        confirmLoading={saving}
        onCancel={() => setOpen(false)}
        onOk={() => form.submit()}
        destroyOnHidden
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={async (values) => {
            setSaving(true);
            try {
              const { timeRange, ...rest } = values;
              const payload = {
                ...rest,
                startTime: timeRange[0].format('YYYY-MM-DDTHH:mm:ss'),
                endTime: timeRange[1].format('YYYY-MM-DDTHH:mm:ss'),
              };
              if (editing) {
                await updateSeckillActivity(editing.id, payload);
              } else {
                await createSeckillActivity(payload);
              }
              message.success('保存成功');
              setOpen(false);
              await load();
            } catch (error) {
              message.error(errorMessage(error));
            } finally {
              setSaving(false);
            }
          }}
        >
          <Form.Item name="name" label="活动名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item name="productId" label="Token Plan" rules={[{ required: true }]}>
                <Select
                  showSearch
                  optionFilterProp="label"
                  placeholder="选择 Token Plan"
                  options={products
                    .filter((product) => product.productType === 'TOKEN_PLAN')
                    .map((product) => ({
                      label: `${product.name}（${productTypeLabel(product.productType)}）`,
                      value: product.id,
                    }))}
                  onChange={() => {
                    form.setFieldsValue({
                      skuId: undefined,
                      seckillPrice: undefined,
                    });
                  }}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="skuId" label="SKU" rules={[{ required: true }]}>
                <Select
                  disabled={!selectedProductId}
                  placeholder={selectedProductId ? '选择 SKU' : '请先选择 Token Plan'}
                  options={skuOptions}
                  onChange={(skuId) => {
                    const sku = skuMap.get(skuId);
                    if (sku) {
                      form.setFieldValue('seckillPrice', sku.price);
                    }
                  }}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item name="seckillPrice" label="秒杀价" rules={[{ required: true }]}>
                <InputNumber min={0} precision={2} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item name="seckillStock" label="活动库存" rules={[{ required: true }]}>
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item name="perUserLimit" label="每人限购" rules={[{ required: true }]}>
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item
            name="timeRange"
            label="活动时间"
            rules={[
              { required: true },
              {
                validator: (_, value: [Dayjs, Dayjs] | undefined) => (
                  !value || value[1].isAfter(value[0])
                    ? Promise.resolve()
                    : Promise.reject(new Error('结束时间必须晚于开始时间'))
                ),
              },
            ]}
          >
            <DatePicker.RangePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={SECKILL_STATUS_OPTIONS}
              allowClear
              placeholder="使用后端默认状态（草稿）"
            />
          </Form.Item>
        </Form>
      </Modal>
      <Drawer
        title="秒杀记录"
        width={720}
        open={recordsOpen}
        onClose={() => setRecordsOpen(false)}
      >
        <Table<SeckillRecord>
          rowKey="id"
          loading={recordsLoading}
          dataSource={records}
          pagination={false}
          scroll={{ x: 'max-content' }}
          columns={[
            { title: '用户 ID', dataIndex: 'userId' },
            { title: '订单号', dataIndex: 'orderNo' },
            { title: '数量', dataIndex: 'quantity' },
            { title: '请求 ID', dataIndex: 'requestId' },
            {
              title: '状态',
              render: (_, item) => (
                <Tag color={item.status === 'SUCCESS' ? 'green' : 'red'}>
                  {item.status}
                </Tag>
              ),
            },
            { title: '时间', dataIndex: 'createdAt' },
            { title: '错误', dataIndex: 'errorMessage' },
          ]}
        />
      </Drawer>
    </>
  );
}
