import { ArrowRightOutlined, FireOutlined } from '@ant-design/icons';
import { Button, Card, Col, Empty, Row, Skeleton, Space, Statistic, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { listProducts } from '../api/catalog';
import { listSeckillActivities } from '../api/seckill';
import type { ProductSummary, SeckillActivity } from '../types/api';

export default function HomePage() {
  const [packs, setPacks] = useState<ProductSummary[]>([]);
  const [plans, setPlans] = useState<ProductSummary[]>([]);
  const [activities, setActivities] = useState<SeckillActivity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      listProducts('TOKEN_PACK', 1, 8),
      listProducts('TOKEN_PLAN', 1, 8),
      listSeckillActivities(),
    ])
      .then(([packPage, planPage, seckill]) => {
        setPacks(packPage.records);
        setPlans(planPage.records);
        setActivities(seckill);
      })
      .finally(() => setLoading(false));
  }, []);

  const activeActivity = activities.find((item) => item.status === 'RUNNING') ?? activities[0];

  return (
    <div className="page-shell">
      <div className="toolbar">
        <div>
          <h1 className="page-title">Token 资源与 Plan</h1>
          <p className="page-subtitle">购买资源包，或抢购限量 Token Plan。</p>
        </div>
      </div>

      {loading ? (
        <Skeleton active />
      ) : activeActivity ? (
        <Card className="seckill-banner">
          <Row align="middle" gutter={[16, 16]}>
            <Col flex="auto">
              <Space direction="vertical" size={4}>
                <Space>
                  <FireOutlined style={{ color: '#fa541c' }} />
                  <Typography.Text strong>秒杀进行中</Typography.Text>
                  <Tag color="volcano">限时限量</Tag>
                </Space>
                <Typography.Title level={3} style={{ margin: 0 }}>
                  {activeActivity.name}
                </Typography.Title>
                <Typography.Text type="secondary">
                  剩余 {Math.max(activeActivity.seckillStock - activeActivity.soldCount, 0)} 份
                </Typography.Text>
              </Space>
            </Col>
            <Col>
              <Statistic title="秒杀价" value={activeActivity.seckillPrice} prefix="¥" />
            </Col>
            <Col>
              <Link to="/seckill">
                <Button type="primary" size="large" icon={<ArrowRightOutlined />}>
                  去抢购
                </Button>
              </Link>
            </Col>
          </Row>
        </Card>
      ) : (
        <Empty description="暂无秒杀活动" />
      )}

      <section className="section">
        <div className="toolbar">
          <Typography.Title level={4} style={{ margin: 0 }}>
            Token 资源包
          </Typography.Title>
          <Link to="/products?type=TOKEN_PACK">查看全部</Link>
        </div>
        <ProductCards products={packs} />
      </section>

      <section className="section">
        <div className="toolbar">
          <Typography.Title level={4} style={{ margin: 0 }}>
            Token Plan
          </Typography.Title>
          <Link to="/products?type=TOKEN_PLAN">查看全部</Link>
        </div>
        <ProductCards products={plans} />
      </section>
    </div>
  );
}

function ProductCards({ products }: { products: ProductSummary[] }) {
  if (!products.length) {
    return <Empty description="暂无商品" />;
  }
  return (
    <div className="card-grid">
      {products.map((product) => (
        <Card key={product.id} hoverable>
          <Space direction="vertical" size={10} style={{ width: '100%' }}>
            <Typography.Text strong>{product.name}</Typography.Text>
            <Typography.Text type="secondary">{product.subtitle}</Typography.Text>
            <div className="price">¥{product.price}</div>
            <Typography.Text>
              {product.productType === 'TOKEN_PACK'
                ? `${product.tokenAmount?.toLocaleString()} Token`
                : `${product.planDays} 天 / ${product.planQuota?.toLocaleString()} Token`}
            </Typography.Text>
            <Link to={`/products/${product.id}`}>
              <Button block>查看详情</Button>
            </Link>
          </Space>
        </Card>
      ))}
    </div>
  );
}
