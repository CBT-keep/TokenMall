import {
  AppstoreOutlined,
  DatabaseOutlined,
  GiftOutlined,
  HomeOutlined,
  LogoutOutlined,
  ShoppingOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { Button, Layout, Menu, Space, Typography } from 'antd';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const { Header, Sider, Content } = Layout;

export default function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const items = [
    { key: '/admin', icon: <HomeOutlined />, label: <Link to="/admin">控制台</Link> },
    { key: '/admin/categories', icon: <AppstoreOutlined />, label: <Link to="/admin/categories">分类</Link> },
    { key: '/admin/products', icon: <GiftOutlined />, label: <Link to="/admin/products">商品</Link> },
    { key: '/admin/inventory', icon: <DatabaseOutlined />, label: <Link to="/admin/inventory">SKU 与库存</Link> },
    { key: '/admin/orders', icon: <ShoppingOutlined />, label: <Link to="/admin/orders">订单</Link> },
    { key: '/admin/seckill', icon: <ThunderboltOutlined />, label: <Link to="/admin/seckill">秒杀</Link> },
  ];

  return (
    <Layout className="admin-layout">
      <Sider breakpoint="lg" collapsedWidth="0">
        <div className="admin-brand">TokenMall Admin</div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={items}
        />
      </Sider>
      <Layout>
        <Header className="admin-header">
          <Typography.Text strong>后台管理</Typography.Text>
          <Space>
            <Link to="/">
              <Button>返回商城</Button>
            </Link>
            <Typography.Text>{user?.nickname}</Typography.Text>
            <Button
              icon={<LogoutOutlined />}
              onClick={() => {
                logout();
                navigate('/login');
              }}
            >
              退出
            </Button>
          </Space>
        </Header>
        <Content className="admin-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
