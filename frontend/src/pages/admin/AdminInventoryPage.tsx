import { Button, Form, Input, InputNumber, message, Modal, Space, Table } from 'antd';
import { useEffect, useState } from 'react';
import { adjustInventory, createSku, listAdminProducts, listAdminSkus, updateSku } from '../../api/admin';
import { errorMessage } from '../../api/client';
import type { ProductSummary, Sku } from '../../types/api';

export default function AdminInventoryPage() {
  const [skus, setSkus] = useState<Sku[]>([]);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [skuOpen, setSkuOpen] = useState(false);
  const [inventoryOpen, setInventoryOpen] = useState(false);
  const [editing, setEditing] = useState<Sku>();
  const [skuForm] = Form.useForm();
  const [inventoryForm] = Form.useForm();

  const load = () => {
    Promise.all([listAdminSkus(), listAdminProducts()])
      .then(([skuData, productData]) => {
        setSkus(skuData);
        setProducts(productData);
      })
      .catch((error) => message.error(errorMessage(error)));
  };

  useEffect(load, []);

  const openSkuEditor = (item?: Sku) => {
    setEditing(item);
    skuForm.setFieldsValue(item ?? { status: 1 });
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
        <Button type="primary" onClick={() => openSkuEditor()}>新增 SKU</Button>
      </div>
      <Table<Sku>
        rowKey="id"
        dataSource={skus}
        columns={[
          { title: 'ID', dataIndex: 'id' },
          { title: 'SKU 编码', dataIndex: 'skuCode' },
          { title: '名称', dataIndex: 'name' },
          { title: '价格', render: (_, item) => `¥${item.price}` },
          { title: '总库存', dataIndex: 'totalStock' },
          { title: '可用库存', dataIndex: 'availableStock' },
          { title: '锁定库存', dataIndex: 'lockedStock' },
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
        onCancel={() => setSkuOpen(false)}
        onOk={() => skuForm.submit()}
        destroyOnClose
      >
        <Form
          form={skuForm}
          layout="vertical"
          onFinish={async (values) => {
            try {
              if (editing) {
                await updateSku(editing.id, values);
              } else {
                await createSku(values);
              }
              message.success('保存成功');
              setSkuOpen(false);
              load();
            } catch (error) {
              message.error(errorMessage(error));
            }
          }}
        >
          <Form.Item name="productId" label="商品 ID" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="skuCode" label="SKU 编码" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="name" label="SKU 名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="price" label="价格" rules={[{ required: true }]}><InputNumber min={0} precision={2} /></Form.Item>
          <Space size="large" style={{ display: 'flex' }}>
            <Form.Item name="tokenAmount" label="Token 数量"><InputNumber min={0} /></Form.Item>
            <Form.Item name="planDays" label="Plan 天数"><InputNumber min={1} /></Form.Item>
            <Form.Item name="planQuota" label="Plan 配额"><InputNumber min={0} /></Form.Item>
          </Space>
          <Form.Item name="status" label="状态"><InputNumber min={0} max={1} /></Form.Item>
        </Form>
      </Modal>
      <Modal
        title={`调整库存：${editing?.name ?? ''}`}
        open={inventoryOpen}
        onCancel={() => setInventoryOpen(false)}
        onOk={() => inventoryForm.submit()}
        destroyOnClose
      >
        <Form
          form={inventoryForm}
          layout="vertical"
          onFinish={async (values) => {
            try {
              if (!editing) return;
              await adjustInventory(editing.id, values);
              message.success('库存已更新');
              setInventoryOpen(false);
              load();
            } catch (error) {
              message.error(errorMessage(error));
            }
          }}
        >
          <Form.Item name="totalStock" label="总库存"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="availableStock" label="可用库存"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          <Form.Item name="lockedStock" label="锁定库存"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
        </Form>
      </Modal>
    </>
  );
}
