import { Card, Col, Row, Statistic } from 'antd';
import { useEffect, useState } from 'react';
import { getDashboard } from '../../api/admin';
import type { DashboardData } from '../../types/api';

export default function AdminDashboardPage() {
  const [data, setData] = useState<DashboardData>();

  useEffect(() => {
    getDashboard().then(setData);
  }, []);

  return (
    <>
      <h1 className="page-title">管理控制台</h1>
      <p className="page-subtitle">基础业务数据概览。</p>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}><Card><Statistic title="商品数量" value={data?.productCount ?? 0} /></Card></Col>
        <Col xs={24} sm={12} lg={6}><Card><Statistic title="待支付订单" value={data?.pendingOrderCount ?? 0} /></Card></Col>
        <Col xs={24} sm={12} lg={6}><Card><Statistic title="已支付订单" value={data?.paidOrderCount ?? 0} /></Card></Col>
        <Col xs={24} sm={12} lg={6}><Card><Statistic title="运行中秒杀" value={data?.runningSeckillCount ?? 0} /></Card></Col>
      </Row>
    </>
  );
}
