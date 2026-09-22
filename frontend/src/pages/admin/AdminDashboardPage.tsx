import { ReloadOutlined } from '@ant-design/icons';
import { Button, Card, Col, message, Row, Statistic } from 'antd';
import { useCallback, useEffect, useState } from 'react';
import { getDashboard } from '../../api/admin';
import { errorMessage } from '../../api/client';
import type { DashboardData } from '../../types/api';

export default function AdminDashboardPage() {
  const [data, setData] = useState<DashboardData>();
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setData(await getDashboard());
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <>
      <div className="toolbar">
        <div>
          <h1 className="page-title">管理控制台</h1>
          <p className="page-subtitle">基础业务数据概览。</p>
        </div>
        <Button icon={<ReloadOutlined />} onClick={() => void load()} loading={loading}>
          刷新
        </Button>
      </div>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}><Statistic title="商品数量" value={data?.productCount ?? '-'} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}><Statistic title="待支付订单" value={data?.pendingOrderCount ?? '-'} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}><Statistic title="已支付订单" value={data?.paidOrderCount ?? '-'} /></Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card loading={loading}><Statistic title="运行中秒杀" value={data?.runningSeckillCount ?? '-'} /></Card>
        </Col>
      </Row>
    </>
  );
}
