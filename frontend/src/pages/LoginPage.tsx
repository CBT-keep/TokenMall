import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, message, Tabs, Typography } from 'antd';
import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { errorMessage } from '../api/client';
import { useAuth } from '../auth/AuthContext';

interface LoginForm {
  username: string;
  password: string;
}

interface RegisterForm extends LoginForm {
  nickname: string;
}

export default function LoginPage() {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const [tab, setTab] = useState('login');

  const finishLogin = async (values: LoginForm) => {
    setLoading(true);
    try {
      const user = await login(values.username, values.password);
      message.success('登录成功');
      const from = (location.state as { from?: string } | null)?.from;
      navigate(from ?? (user.role === 'ADMIN' ? '/admin' : '/'), { replace: true });
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  const finishRegister = async (values: RegisterForm) => {
    setLoading(true);
    try {
      await register(values.username, values.password, values.nickname);
      message.success('注册成功，请登录');
      setTab('login');
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <Typography.Title level={2} style={{ marginTop: 0 }}>
          TokenMall 学习商城
        </Typography.Title>
        <Typography.Paragraph type="secondary">
          Redis、RabbitMQ 和 MySQL 学习项目
        </Typography.Paragraph>
        <Tabs
          activeKey={tab}
          onChange={setTab}
          items={[
            {
              key: 'login',
              label: '登录',
              children: (
                <Form<LoginForm> layout="vertical" onFinish={finishLogin}>
                  <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
                    <Input prefix={<UserOutlined />} autoComplete="username" />
                  </Form.Item>
                  <Form.Item name="password" label="密码" rules={[{ required: true }]}>
                    <Input.Password prefix={<LockOutlined />} autoComplete="current-password" />
                  </Form.Item>
                  <Button type="primary" htmlType="submit" block loading={loading}>
                    登录
                  </Button>
                </Form>
              ),
            },
            {
              key: 'register',
              label: '注册',
              children: (
                <Form<RegisterForm> layout="vertical" onFinish={finishRegister}>
                  <Form.Item name="username" label="用户名" rules={[{ required: true, min: 3 }]}>
                    <Input />
                  </Form.Item>
                  <Form.Item name="nickname" label="昵称" rules={[{ required: true }]}>
                    <Input />
                  </Form.Item>
                  <Form.Item name="password" label="密码" rules={[{ required: true, min: 6 }]}>
                    <Input.Password />
                  </Form.Item>
                  <Button type="primary" htmlType="submit" block loading={loading}>
                    注册
                  </Button>
                </Form>
              ),
            },
          ]}
        />
        <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
          管理员种子账号：admin / admin123
        </Typography.Paragraph>
      </Card>
    </div>
  );
}
