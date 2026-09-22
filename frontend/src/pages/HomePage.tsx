import { ArrowRightOutlined, FireOutlined } from '@ant-design/icons';
import {
  Button,
  Card,
  Col,
  Empty,
  message,
  Row,
  Skeleton,
  Space,
  Statistic,
  Tag,
  Typography,
} from 'antd';
import dayjs, { type Dayjs } from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { listProducts } from '../api/catalog';
import { errorMessage } from '../api/client';
import { listSeckillActivities } from '../api/seckill';
import {
  seckillRuntimeStatus,
  seckillStatusMeta,
} from '../constants/business';
import type { ProductSummary, SeckillActivity } from '../types/api';

export default function HomePage() {
  const [packs, setPacks] = useState<ProductSummary[]>([]);
  const [plans, setPlans] = useState<ProductSummary[]>([]);
  const [activities, setActivities] = useState<SeckillActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [now, setNow] = useState<Dayjs>(() => dayjs());

  useEffect(() => {
    const timer = window.setInterval(() => setNow(dayjs()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    Promise.all([
      listProducts('TOKEN_PACK', 1, 8),
      listProducts('TOKEN_PLAN', 1, 8),
      listSeckillActivities(),
    ])
      .then(([packPage, planPage, seckill]) => {
        if (!active) return;
        setPacks(packPage.records);
        setPlans(planPage.records);
        setActivities(seckill);
      })
      .catch((error) => {
        if (active) message.error(errorMessage(error));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const bannerActivity = useMemo(
    () =>
      activities.find((item) => seckillRuntimeStatus(item, now) === 'RUNNING')
      ?? activities.find((item) => seckillRuntimeStatus(item, now) === 'READY')
      ?? null,
    [activities, now],
  );

  const bannerStatus = bannerActivity
    ? seckillRuntimeStatus(bannerActivity, now)
    : undefined;
  const bannerStatusMeta = seckillStatusMeta(bannerStatus);

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
      ) : bannerActivity ? (
        <Card className="seckill-banner">
          <Row align="middle" gutter={[16, 16]}>
            <Col flex="auto">
              <Space direction="vertical" size={4}>
                <Space>
                  <FireOutlined style={{ color: '#fa541c' }} />
                  <Typography.Text strong>限时秒杀</Typography.Text>
                  <Tag color={bannerStatusMeta.color}>{bannerStatusMeta.label}</Tag>
                </Space>
                <Typography.Title level={3} style={{ margin: 0 }}>
                  {bannerActivity.name}
                </Typography.Title>
                <Typography.Text type="secondary">
                  剩余 {Math.max(bannerActivity.seckillStock - bannerActivity.soldCount, 0)} 份，
                  {bannerStatus === 'READY' ? '距开始 ' : '距结束 '}
                  {formatCountdown(
                    dayjs(bannerStatus === 'READY' ? bannerActivity.startTime : bannerActivity.endTime),
                    now,
                  )}
                </Typography.Text>
              </Space>
            </Col>
            <Col>
              <Statistic title="秒杀价" value={bannerActivity.seckillPrice} prefix="¥" />
            </Col>
            <Col>
              <Link to="/seckill">
                <Button
                  type="primary"
                  size="large"
                  icon={<ArrowRightOutlined />}
                >
                  {bannerStatus === 'RUNNING' ? '去抢购' : '查看活动'}
                </Button>
              </Link>
            </Col>
          </Row>
        </Card>
      ) : (
        <Empty description="暂无进行中的秒杀活动" />
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

function formatCountdown(target: Dayjs, now: Dayjs): string {
  const totalSeconds = Math.max(target.diff(now, 'second'), 0);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  return [hours, minutes, seconds]
    .map((value) => String(value).padStart(2, '0'))
    .join(':');
}
