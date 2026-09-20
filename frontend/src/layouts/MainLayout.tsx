import {
  AppstoreOutlined,
  GiftOutlined,
  LogoutOutlined,
  ShoppingCartOutlined,
  ThunderboltOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Avatar, Button, Dropdown, Layout, Menu, Space, Typography } from 'antd';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const { Header, Content } = Layout;

export default function MainLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const selectedKey = location.pathname.startsWith('/products')
    ? '/products'
    : location.pathname.startsWith('/orders')
      ? '/orders'
      : location.pathname.startsWith('/token')
        ? '/token'
        : location.pathname.startsWith('/seckill')
          ? '/seckill'
          : '/';

  return (
    <Layout>
      <Header className="site-header">
        <Link to="/" className="brand">
          <ThunderboltOutlined /> TokenMall
        </Link>
        <Menu
          mode="horizontal"
          selectedKeys={[selectedKey]}
          items={[
            { key: '/', icon: <AppstoreOutlined />, label: <Link to="/">首页</Link> },
            { key: '/products', icon: <GiftOutlined />, label: <Link to="/products">商品</Link> },
            { key: '/seckill', icon: <ThunderboltOutlined />, label: <Link to="/seckill">秒杀</Link> },
            { key: '/orders', label: <Link to="/orders">订单</Link> },
            { key: '/token', label: <Link to="/token">Token 账户</Link> },
          ]}
          style={{ flex: 1, minWidth: 0 }}
        />
        <Space>
          <Link to="/cart">
            <Button icon={<ShoppingCartOutlined />}>购物车</Button>
          </Link>
          <Dropdown
            menu={{
              items: [
                ...(user?.role === 'ADMIN'
                  ? [{ key: 'admin', icon: <UserOutlined />, label: '管理后台', onClick: () => navigate('/admin') }]
                  : []),
                {
                  key: 'logout',
                  icon: <LogoutOutlined />,
                  label: '退出登录',
                  onClick: () => {
                    logout();
                    navigate('/login');
                  },
                },
              ],
            }}
          >
            <Space className="user-chip">
              <Avatar size="small">{user?.nickname?.slice(0, 1) ?? 'U'}</Avatar>
              <Typography.Text>{user?.nickname ?? user?.username}</Typography.Text>
            </Space>
          </Dropdown>
        </Space>
      </Header>
      <Content>
        <Outlet />
      </Content>
    </Layout>
  );
}
