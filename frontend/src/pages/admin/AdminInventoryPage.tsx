import {
  Button,
  Col,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Row,
  Select,
  Space,
  Table,
  Tag,
} from 'antd';
import { useEffect, useMemo, useState } from 'react';
import {
  adjustInventory,
  createSku,
  listAdminProducts,
  listAdminSkus,
  updateSku,
} from '../../api/admin';
import { errorMessage } from '../../api/client';
import { ENABLED_STATUS_OPTIONS, productTypeLabel } from '../../constants/business';
import type { ProductSummary, Sku } from '../../types/api';

interface SkuFormValues {
  productId: number;
  skuCode: string;
  name: string;
  price: number;
  tokenAmount?: number;
  planDays?: number;
  planQuota?: number;
  status?: number;
}

interface InventoryFormValues {
  totalStock: number;
  availableStock: number;
  lockedStock: number;
}

export default function AdminInventoryPage() {
  const [skus, setSkus] = useState<Sku[]>([]);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [skuOpen, setSkuOpen] = useState(false);
  const [inventoryOpen, setInventoryOpen] = useState(false);
  const [editing, setEditing] = useState<Sku>();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [skuForm] = Form.useForm<SkuFormValues>();
  const [inventoryForm] = Form.useForm<InventoryFormValues>();
  const selectedProductId = Form.useWatch('productId', skuForm);

  const productMap = useMemo(
    () => new Map(products.map((product) => [product.id, product])),
    [products],
  );
  const selectedProduct = selectedProductId ? productMap.get(selectedProductId) : undefined;

  const load = async () => {
    setLoading(true);
    try {
      const [skuData, productData] = await Promise.all([
        listAdminSkus(),
        listAdminProducts(),
      ]);
      setSkus(skuData);
      setProducts(productData);
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const openSkuEditor = (item?: Sku) => {
    setEditing(item);
    skuForm.resetFields();
    if (item) {
      skuForm.setFieldsValue({
        productId: item.productId,
        skuCode: item.skuCode,
        name: item.name,
        price: item.price,
        tokenAmount: item.tokenAmount,
        planDays: item.planDays,
        planQuota: item.planQuota,
        status: item.status,
      });
    }
    setSkuOpen(true);
  };

  const openInventory = (item: Sku) => {
    setEditing(item);
    inventoryForm.setFieldsValue({
      totalStock: item.totalStock ?? 0,
      availableStock: item.availableStock ?? 0,
      lockedStock: item.lockedStock ?? 0,
    });
    setInventoryOpen(true);
  };

  return (
    <>
      <div className="toolbar">
        <div>
          <h1 className="page-title">SKU 与库存</h1>
          <p className="page-subtitle">基础版本直接修改 MySQL 库存，便于后续复现并发问题。</p>
        </div>
        <Space>
          <Button onClick={() => void load()} loading={loading}>刷新</Button>
          <Button type="primary" onClick={() => openSkuEditor()}>新增 SKU</Button>
        </Space>
      </div>
      <Table<Sku>
        rowKey="id"
        loading={loading}
        dataSource={skus}
        scroll={{ x: 'max-content' }}
        columns={[
          { title: 'ID', dataIndex: 'id' },
          { title: '商品', render: (_, item) => productMap.get(item.productId)?.name ?? `#${item.productId}` },
          { title: 'SKU 编码', dataIndex: 'skuCode' },
          { title: '名称', dataIndex: 'name' },
          { title: '价格', render: (_, item) => `¥${item.price}` },
          { title: '总库存', dataIndex: 'totalStock' },
          { title: '可用库存', dataIndex: 'availableStock' },
          { title: '锁定库存', dataIndex: 'lockedStock' },
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
                <Button type="link" onClick={() => openSkuEditor(item)}>编辑</Button>
                <Button type="link" onClick={() => openInventory(item)}>调整库存</Button>
              </Space>
            ),
          },
        ]}
      />
      <Modal
        title={editing ? '编辑 SKU' : '新增 SKU'}
        open={skuOpen}
        width={720}
        confirmLoading={saving}
        onCancel={() => setSkuOpen(false)}
        onOk={() => skuForm.submit()}
        destroyOnHidden
      >
        <Form
          form={skuForm}
          layout="vertical"
          onFinish={async (values) => {
            setSaving(true);
            try {
              if (editing) {
                await updateSku(editing.id, values);
              } else {
                await createSku(values);
              }
              message.success('保存成功');
              setSkuOpen(false);
              await load();
            } catch (error) {
              message.error(errorMessage(error));
            } finally {
              setSaving(false);
            }
          }}
        >
          <Form.Item name="productId" label="所属商品" rules={[{ required: true }]}>
            <Select
              showSearch
              optionFilterProp="label"
              placeholder="选择商品"
              options={products.map((product) => ({
                label: `${product.name}（${productTypeLabel(product.productType)}）`,
                value: product.id,
              }))}
              onChange={() => {
                skuForm.setFields([
                  { name: 'tokenAmount', value: undefined },
                  { name: 'planDays', value: undefined },
                  { name: 'planQuota', value: undefined },
                ]);
              }}
            />
          </Form.Item>
          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item name="skuCode" label="SKU 编码" rules={[{ required: true }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="name" label="SKU 名称" rules={[{ required: true }]}>
                <Input />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item name="price" label="价格" rules={[{ required: true }]}>
                <InputNumber min={0} precision={2} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            {selectedProduct?.productType === 'TOKEN_PACK' && (
              <Col xs={24} md={8}>
                <Form.Item name="tokenAmount" label="Token 数量">
                  <InputNumber min={0} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            )}
            {selectedProduct?.productType === 'TOKEN_PLAN' && (
              <>
                <Col xs={24} md={8}>
                  <Form.Item name="planDays" label="Plan 天数">
                    <InputNumber min={1} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
                <Col xs={24} md={8}>
                  <Form.Item name="planQuota" label="Plan 配额">
                    <InputNumber min={0} style={{ width: '100%' }} />
                  </Form.Item>
                </Col>
              </>
            )}
            <Col xs={24} md={8}>
              <Form.Item name="status" label="状态">
                <Select options={ENABLED_STATUS_OPTIONS} allowClear placeholder="使用后端默认状态" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
      <Modal
        title={`调整库存：${editing?.name ?? ''}`}
        open={inventoryOpen}
        confirmLoading={saving}
        onCancel={() => setInventoryOpen(false)}
        onOk={() => inventoryForm.submit()}
        destroyOnHidden
      >
        <Form
          form={inventoryForm}
          layout="vertical"
          onFinish={async (values) => {
            if (!editing) return;
            setSaving(true);
            try {
              await adjustInventory(editing.id, values);
              message.success('库存已更新');
              setInventoryOpen(false);
              await load();
            } catch (error) {
              message.error(errorMessage(error));
            } finally {
              setSaving(false);
            }
          }}
        >
          <Form.Item name="totalStock" label="总库存" rules={[{ required: true }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="availableStock" label="可用库存" rules={[{ required: true }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="lockedStock" label="锁定库存" rules={[{ required: true }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
