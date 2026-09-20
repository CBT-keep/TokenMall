import { Button, Form, Input, InputNumber, message, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import {
  createProduct,
  deleteProduct,
  listAdminCategories,
  listAdminProducts,
  updateProduct,
} from '../../api/admin';
import { errorMessage } from '../../api/client';
import type { Category, ProductSummary } from '../../types/api';

export default function AdminProductsPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<ProductSummary>();
  const [form] = Form.useForm();

  const load = () => {
    Promise.all([listAdminProducts(), listAdminCategories()])
      .then(([productData, categoryData]) => {
        setProducts(productData);
        setCategories(categoryData);
      })
      .catch((error) => message.error(errorMessage(error)));
  };

  useEffect(load, []);

  const openEditor = (item?: ProductSummary) => {
    setEditing(item);
    form.setFieldsValue(
      item ?? {
        productType: 'TOKEN_PACK',
        status: 1,
        sortOrder: 0,
        purchaseLimit: 0,
      },
    );
    setOpen(true);
  };

  return (
    <>
      <div className="toolbar">
        <div>
          <h1 className="page-title">商品管理</h1>
          <p className="page-subtitle">维护资源包和 Token Plan。</p>
        </div>
        <Button type="primary" onClick={() => openEditor()}>新增商品</Button>
      </div>
      <Table<ProductSummary>
        rowKey="id"
        dataSource={products}
        columns={[
          { title: 'ID', dataIndex: 'id' },
          { title: '名称', dataIndex: 'name' },
          { title: '类型', render: (_, item) => <Tag>{item.productType}</Tag> },
          { title: '价格', render: (_, item) => `¥${item.price}` },
          {
            title: 'Token',
            render: (_, item) => item.tokenAmount ?? item.planQuota,
          },
          { title: '状态', dataIndex: 'status' },
          {
            title: '操作',
            render: (_, item) => (
              <Space>
                <Button type="link" onClick={() => openEditor(item)}>编辑</Button>
                <Popconfirm
                  title="确认删除？"
                  onConfirm={async () => {
                    await deleteProduct(item.id);
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
        title={editing ? '编辑商品' : '新增商品'}
        open={open}
        width={720}
        onCancel={() => setOpen(false)}
        onOk={() => form.submit()}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={async (values) => {
            try {
              if (editing) {
                await updateProduct(editing.id, values);
              } else {
                await createProduct(values);
              }
              message.success('保存成功');
              setOpen(false);
              load();
            } catch (error) {
              message.error(errorMessage(error));
            }
          }}
        >
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item name="categoryId" label="分类" rules={[{ required: true }]} style={{ minWidth: 220 }}>
              <Select options={categories.map((item) => ({ label: item.name, value: item.id }))} />
            </Form.Item>
            <Form.Item name="productType" label="商品类型" rules={[{ required: true }]} style={{ minWidth: 220 }}>
              <Select
                options={[
                  { label: 'Token 资源包', value: 'TOKEN_PACK' },
                  { label: 'Token Plan', value: 'TOKEN_PLAN' },
                ]}
              />
            </Form.Item>
          </Space>
          <Form.Item name="name" label="商品名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="subtitle" label="副标题"><Input /></Form.Item>
          <Form.Item name="description" label="说明"><Input.TextArea rows={3} /></Form.Item>
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item name="price" label="价格" rules={[{ required: true }]}><InputNumber min={0} precision={2} /></Form.Item>
            <Form.Item name="originalPrice" label="原价"><InputNumber min={0} precision={2} /></Form.Item>
          </Space>
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item name="tokenAmount" label="Token 数量"><InputNumber min={0} /></Form.Item>
            <Form.Item name="planDays" label="Plan 天数"><InputNumber min={1} /></Form.Item>
            <Form.Item name="planQuota" label="Plan 配额"><InputNumber min={0} /></Form.Item>
          </Space>
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item name="purchaseLimit" label="限购数量"><InputNumber min={0} /></Form.Item>
            <Form.Item name="sortOrder" label="排序"><InputNumber min={0} /></Form.Item>
            <Form.Item name="status" label="状态"><InputNumber min={0} max={1} /></Form.Item>
          </Space>
        </Form>
      </Modal>
    </>
  );
}
