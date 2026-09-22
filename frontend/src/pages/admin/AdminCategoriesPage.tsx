import {
  Button,
  Form,
  Input,
  InputNumber,
  message,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
} from 'antd';
import { useEffect, useState } from 'react';
import {
  createCategory,
  deleteCategory,
  listAdminCategories,
  updateCategory,
} from '../../api/admin';
import { errorMessage } from '../../api/client';
import { ENABLED_STATUS_OPTIONS } from '../../constants/business';
import type { Category } from '../../types/api';

interface CategoryFormValues {
  name: string;
  code: string;
  sortOrder?: number;
  status?: number;
}

export default function AdminCategoriesPage() {
  const [items, setItems] = useState<Category[]>([]);
  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState<Category>();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<number>();
  const [form] = Form.useForm<CategoryFormValues>();

  const load = async () => {
    setLoading(true);
    try {
      setItems(await listAdminCategories());
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const openEditor = (item?: Category) => {
    setEditing(item);
    form.resetFields();
    if (item) {
      form.setFieldsValue({
        name: item.name,
        code: item.code,
        sortOrder: item.sortOrder,
        status: item.status,
      });
    }
    setOpen(true);
  };

  return (
    <>
      <div className="toolbar">
        <div>
          <h1 className="page-title">分类管理</h1>
          <p className="page-subtitle">维护 Token 资源包和 Token Plan 分类。</p>
        </div>
        <Space>
          <Button onClick={() => void load()} loading={loading}>刷新</Button>
          <Button type="primary" onClick={() => openEditor()}>新增分类</Button>
        </Space>
      </div>
      <Table<Category>
        rowKey="id"
        loading={loading}
        dataSource={items}
        scroll={{ x: 'max-content' }}
        columns={[
          { title: 'ID', dataIndex: 'id' },
          { title: '名称', dataIndex: 'name' },
          { title: '编码', dataIndex: 'code' },
          { title: '排序', dataIndex: 'sortOrder' },
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
                <Button type="link" onClick={() => openEditor(item)}>编辑</Button>
                <Popconfirm
                  title="确认删除？"
                  onConfirm={async () => {
                    setDeletingId(item.id);
                    try {
                      await deleteCategory(item.id);
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
        title={editing ? '编辑分类' : '新增分类'}
        open={open}
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
              if (editing) {
                await updateCategory(editing.id, values);
              } else {
                await createCategory(values);
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
          <Form.Item name="name" label="名称" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="code" label="编码" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="sortOrder" label="排序"><InputNumber min={0} /></Form.Item>
          <Form.Item name="status" label="状态">
            <Select options={ENABLED_STATUS_OPTIONS} allowClear placeholder="使用后端默认状态" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
