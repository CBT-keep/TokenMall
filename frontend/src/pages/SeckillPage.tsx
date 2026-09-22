import { FireOutlined } from '@ant-design/icons';
import { Button, Card, Empty, message, Progress, Skeleton, Space, Tag, Typography } from 'antd';
import dayjs, { type Dayjs } from 'dayjs';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { errorMessage } from '../api/client';
import { getSeckillResult, listSeckillActivities, purchaseSeckill } from '../api/seckill';
import { newRequestId } from '../components/RequestId';
import { seckillRuntimeStatus, seckillStatusMeta } from '../constants/business';
import type { SeckillActivity } from '../types/api';

export default function SeckillPage() {
  const navigate = useNavigate();
  const [activities, setActivities] = useState<SeckillActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [buyingId, setBuyingId] = useState<number>();
  const [now, setNow] = useState<Dayjs>(() => dayjs());

  useEffect(() => {
    const timer = window.setInterval(() => setNow(dayjs()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    listSeckillActivities()
      .then((data) => {
        if (active) setActivities(data);
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

  const buy = async (activity: SeckillActivity) => {
    setBuyingId(activity.id);
    const requestId = newRequestId(`seckill-${activity.id}`);
    try {
      const result = await purchaseSeckill(activity.id, requestId, 1);
      if (result.orderNo) {
        message.success(result.message ?? '抢购成功');
        navigate(`/orders/${result.orderNo}`);
        return;
      }
      for (let index = 0; index < 10; index += 1) {
        await new Promise((resolve) => window.setTimeout(resolve, 800));
        const current = await getSeckillResult(requestId);
        if (current.orderNo) {
          message.success('抢购成功');
          navigate(`/orders/${current.orderNo}`);
          return;
        }
        if (current.status === 'FAILED') {
          throw new Error(current.message ?? '抢购失败');
        }
      }
      message.info('请求处理中，请稍后到订单列表查看');
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setBuyingId(undefined);
    }
  };

  return (
    <div className="page-shell">
      <h1 className="page-title">Token Plan 秒杀</h1>
      <p className="page-subtitle">当前版本直接访问 MySQL，故意保留并发问题供后续学习。</p>
      {loading ? (
        <Skeleton active />
      ) : activities.length === 0 ? (
        <Empty description="暂无秒杀活动" />
      ) : (
        <div className="card-grid">
          {activities.map((activity) => {
            const runtimeStatus = seckillRuntimeStatus(activity, now);
            const statusMeta = seckillStatusMeta(runtimeStatus);
            const remaining = Math.max(activity.seckillStock - activity.soldCount, 0);
            const percent = activity.seckillStock > 0
              ? Math.min(Math.round((activity.soldCount / activity.seckillStock) * 100), 100)
              : 0;
            const isRunning = runtimeStatus === 'RUNNING';
            const soldOut = remaining <= 0;
            return (
              <Card key={activity.id}>
                <Space direction="vertical" size={12} style={{ width: '100%' }}>
                  <Space>
                    <FireOutlined style={{ color: '#fa541c' }} />
                    <Typography.Text strong>{activity.name}</Typography.Text>
                    <Tag color={statusMeta.color}>{statusMeta.label}</Tag>
                  </Space>
                  <div className="price">¥{activity.seckillPrice}</div>
                  <Typography.Text>剩余 {remaining} 份</Typography.Text>
                  <Progress
                    percent={percent}
                    showInfo={false}
                    status={isRunning ? 'active' : 'normal'}
                  />
                  <Typography.Text type="secondary">
                    {dayjs(activity.startTime).format('YYYY-MM-DD HH:mm')}
                    {' 至 '}
                    {dayjs(activity.endTime).format('YYYY-MM-DD HH:mm')}
                  </Typography.Text>
                  <Typography.Text type="secondary">
                    {countdownLabel(runtimeStatus, activity, now)}
                  </Typography.Text>
                  <Button
                    type="primary"
                    block
                    disabled={!isRunning || soldOut || buyingId !== undefined}
                    loading={buyingId === activity.id}
                    onClick={() => buy(activity)}
                  >
                    {soldOut ? '已售罄' : isRunning ? '立即抢购' : statusMeta.label}
                  </Button>
                </Space>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}

function countdownLabel(status: string, activity: SeckillActivity, now: Dayjs): string {
  if (status === 'READY' && now.isBefore(dayjs(activity.startTime))) {
    return `距开始 ${formatCountdown(dayjs(activity.startTime), now)}`;
  }
  if (status === 'RUNNING' && now.isBefore(dayjs(activity.endTime))) {
    return `距结束 ${formatCountdown(dayjs(activity.endTime), now)}`;
  }
  return status === 'ENDED' ? '活动已结束' : '';
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
