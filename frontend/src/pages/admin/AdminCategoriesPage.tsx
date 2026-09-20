import { Button, Form, Input, InputNumber, message, Modal, Popconfirm, Space, Table } from 'antd';
import { useEffect, useState } from 'react';
import {
  createCategory,
  deleteCategory,
  listAdminCategories,
  updateCategory,
} from '../../api/admin';
import { errorMessage } from '../../api/client';
import type { Category } from '../../types/api';

export default function AdminCategoriesPage() {
  const [items, setItems] = useState<Category[]>([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Category>();
  const [form] = Form.useForm();

  const load = () => {
    listAdminCategories().then(setItems).catch((error) => message.error(errorMessage(error)));
  };

  useEffect(load, []);

  const openEditor = (item?: Category) => {
    setEditing(item);
    form.setFieldsValue(item ?? { status: 1, sortOrder: 0 });
    setOpen(true);
  };

  return (
    <>
      <div className="toolbar">
        <div>
          <h1 className="page-title">分类管理</h1>
          <p className="page-subtitle">维护 Token 资源包和 Token Plan 分类。</p>
        </div>
        <Button type="primary" onClick={() => openEditor()}>新增分类</Button>
      </div>
      <Table<Category>
        rowKey="id"
        dataSource={items}
        columns={[
          { title: 'ID', dataIndex: 'id' },
          { title: '名称', dataIndex: 'name' },
          { title: '编码', dataIndex: 'code' },
          { title: '排序', dataIndex: 'sortOrder' },
          { title: '状态', dataIndex: 'status' },
          {
            title: '操作',
            render: (_, item) => (
              <Space>
                <Button type="link" onClick={() => openEditor(item)}>编辑</Button>
                <Popconfirm
                  title="确认删除？"
                  onConfirm={async () => {
                    await deleteCategory(item.id);
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
        title={editing ? '编辑分类' : '新增分类'}
        open={open}
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
                await updateCategory(editing.id, values);
              } else {
                await createCategory(values);
              }
              message.success('保存成功');
              setOpen(false);
              load();
            } catch (error) {
              message.error(errorMessage(error));
            }
          }}
        >
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="code" label="编码" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="sortOrder" label="排序"><InputNumber min={0} /></Form.Item>
          <Form.Item name="status" label="状态"><InputNumber min={0} max={1} /></Form.Item>
        </Form>
      </Modal>
    </>
  );
}
