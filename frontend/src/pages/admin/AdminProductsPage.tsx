import {
  Button,
  Col,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Spin,
  Table,
  Tag,
} from 'antd';
import { useEffect, useState } from 'react';
import {
  createProduct,
  deleteProduct,
  listAdminCategories,
  listAdminProducts,
  updateProduct,
} from '../../api/admin';
import { getProduct } from '../../api/catalog';
import { errorMessage } from '../../api/client';
import {
  ENABLED_STATUS_OPTIONS,
  PRODUCT_TYPE_OPTIONS,
  productTypeLabel,
} from '../../constants/business';
import type { Category, ProductDetail, ProductSummary } from '../../types/api';

interface ProductFormValues {
  categoryId: number;
  productType: string;
  name: string;
  subtitle?: string;
  description?: string;
  coverUrl?: string;
  price: number;
  originalPrice?: number;
  tokenAmount?: number;
  planDays?: number;
  planQuota?: number;
  purchaseLimit?: number;
  status?: number;
  sortOrder?: number;
}

export default function AdminProductsPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<ProductSummary>();
  const [loading, setLoading] = useState(true);
  const [editorLoading, setEditorLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<number>();
  const [form] = Form.useForm<ProductFormValues>();
  const productType = Form.useWatch('productType', form);

  const load = async () => {
    setLoading(true);
    try {
      const [productData, categoryData] = await Promise.all([
        listAdminProducts(),
        listAdminCategories(),
      ]);
      setProducts(productData);
      setCategories(categoryData);
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const openEditor = async (item?: ProductSummary) => {
    setEditing(item);
    form.resetFields();
    setOpen(true);
    setEditorLoading(!!item);
    if (!item) return;

    try {
      const detail = await getProduct(item.id);
      form.setFieldsValue(toProductFormValues(detail));
    } catch (error) {
      message.error(errorMessage(error));
      setOpen(false);
    } finally {
      setEditorLoading(false);
    }
  };

  return (
    <>
      <div className="toolbar">
        <div>
          <h1 className="page-title">商品管理</h1>
          <p className="page-subtitle">维护资源包和 Token Plan。</p>
        </div>
        <Space>
          <Button onClick={() => void load()} loading={loading}>刷新</Button>
          <Button type="primary" onClick={() => void openEditor()}>新增商品</Button>
        </Space>
      </div>
      <Table<ProductSummary>
        rowKey="id"
        loading={loading}
        dataSource={products}
        scroll={{ x: 'max-content' }}
        columns={[
          { title: 'ID', dataIndex: 'id' },
          { title: '名称', dataIndex: 'name' },
          { title: '类型', render: (_, item) => <Tag>{productTypeLabel(item.productType)}</Tag> },
          { title: '价格', render: (_, item) => `¥${item.price}` },
          {
            title: 'Token',
            render: (_, item) => item.tokenAmount ?? item.planQuota,
          },
          {
            title: '状态',
            render: (_, item) => (
              <Tag color={item.status === 1 ? 'green' : 'default'}>
                {item.status === 1 ? '启用' : '停用'}
              </Tag>
            ),
          },
          {
            title: '操作',
            render: (_, item) => (
              <Space>
                <Button type="link" onClick={() => void openEditor(item)}>编辑</Button>
                <Popconfirm
                  title="确认删除？"
                  onConfirm={async () => {
                    setDeletingId(item.id);
                    try {
                      await deleteProduct(item.id);
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
        title={editing ? '编辑商品' : '新增商品'}
        open={open}
        width={760}
        confirmLoading={saving}
        okButtonProps={{ disabled: editorLoading }}
        onCancel={() => setOpen(false)}
        onOk={() => form.submit()}
        destroyOnHidden
      >
        <Spin spinning={editorLoading}>
          <Form
            form={form}
            layout="vertical"
            disabled={editorLoading}
            onFinish={async (values) => {
              setSaving(true);
              try {
                if (editing) {
                  await updateProduct(editing.id, values);
                } else {
                  await createProduct(values);
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
            <Row gutter={16}>
              <Col xs={24} md={12}>
                <Form.Item name="categoryId" label="分类" rules={[{ required: true }]}>
                  <Select
                    options={categories.map((item) => ({ label: item.name, value: item.id }))}
                    placeholder="选择分类"
                  />
                </Form.Item>
              </Col>
              <Col xs={24} md={12}>
                <Form.Item name="productType" label="商品类型" rules={[{ required: true }]}>
                  <Select
                    options={PRODUCT_TYPE_OPTIONS}
                    placeholder="选择商品类型"
                    onChange={(value) => {
                      if (value === 'TOKEN_PACK') {
                        form.setFields([
                          { name: 'planDays', value: undefined },
                          { name: 'planQuota', value: undefined },
                        ]);
                      } else {
                        form.setFields([{ name: 'tokenAmount', value: undefined }]);
                      }
                    }}
                  />
                </Form.Item>
              </Col>
            </Row>
            <Form.Item name="name" label="商品名称" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item name="subtitle" label="副标题"><Input /></Form.Item>
            <Form.Item name="description" label="说明"><Input.TextArea rows={3} /></Form.Item>
            <Form.Item name="coverUrl" label="封面地址"><Input /></Form.Item>
            <Row gutter={16}>
              <Col xs={24} md={12}>
                <Form.Item name="price" label="价格" rules={[{ required: true }]}>
                  <InputNumber min={0} precision={2} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col xs={24} md={12}>
                <Form.Item name="originalPrice" label="原价">
                  <InputNumber min={0} precision={2} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              {!productType ? null : productType === 'TOKEN_PACK' ? (
                <Col xs={24}>
                  <Form.Item name="tokenAmount" label="Token 数量">
                    <InputNumber min={0} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
              ) : (
                <>
                  <Col xs={24} md={12}>
                    <Form.Item name="planDays" label="Plan 天数">
                      <InputNumber min={1} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={12}>
                    <Form.Item name="planQuota" label="Plan 配额">
                      <InputNumber min={0} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                </>
              )}
            </Row>
            <Row gutter={16}>
              <Col xs={24} md={8}>
                <Form.Item name="purchaseLimit" label="限购数量">
                  <InputNumber min={0} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col xs={24} md={8}>
                <Form.Item name="sortOrder" label="排序">
                  <InputNumber min={0} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col xs={24} md={8}>
                <Form.Item name="status" label="状态">
                  <Select options={ENABLED_STATUS_OPTIONS} allowClear placeholder="使用后端默认状态" />
                </Form.Item>
              </Col>
            </Row>
          </Form>
        </Spin>
      </Modal>
    </>
  );
}

function toProductFormValues(product: ProductDetail): ProductFormValues {
  return {
    categoryId: product.categoryId,
    productType: product.productType,
    name: product.name,
    subtitle: product.subtitle,
    description: product.description,
    coverUrl: product.coverUrl,
    price: product.price,
    originalPrice: product.originalPrice,
    tokenAmount: product.tokenAmount,
    planDays: product.planDays,
    planQuota: product.planQuota,
    purchaseLimit: product.purchaseLimit,
    status: product.status,
    sortOrder: product.sortOrder,
  };
}
