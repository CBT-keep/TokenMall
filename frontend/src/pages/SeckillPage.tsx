import { FireOutlined } from '@ant-design/icons';
import { Button, Card, Empty, message, Progress, Space, Tag, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { errorMessage } from '../api/client';
import { getSeckillResult, listSeckillActivities, purchaseSeckill } from '../api/seckill';
import { newRequestId } from '../components/RequestId';
import type { SeckillActivity } from '../types/api';

export default function SeckillPage() {
  const navigate = useNavigate();
  const [activities, setActivities] = useState<SeckillActivity[]>([]);
  const [loading, setLoading] = useState(true);
  const [buyingId, setBuyingId] = useState<number>();

  useEffect(() => {
    listSeckillActivities()
      .then(setActivities)
      .catch((error) => message.error(errorMessage(error)))
      .finally(() => setLoading(false));
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
      {loading ? null : activities.length === 0 ? (
        <Empty description="暂无秒杀活动" />
      ) : (
        <div className="card-grid">
          {activities.map((activity) => {
            const remaining = Math.max(activity.seckillStock - activity.soldCount, 0);
            const percent = Math.round((activity.soldCount / activity.seckillStock) * 100);
            return (
              <Card key={activity.id}>
                <Space direction="vertical" size={12} style={{ width: '100%' }}>
                  <Space>
                    <FireOutlined style={{ color: '#fa541c' }} />
                    <Typography.Text strong>{activity.name}</Typography.Text>
                    <Tag color={activity.status === 'RUNNING' ? 'volcano' : 'default'}>
                      {activity.status}
                    </Tag>
                  </Space>
                  <div className="price">¥{activity.seckillPrice}</div>
                  <Typography.Text>剩余 {remaining} 份</Typography.Text>
                  <Progress percent={percent} showInfo={false} status="active" />
                  <Typography.Text type="secondary">
                    {activity.startTime} 至 {activity.endTime}
                  </Typography.Text>
                  <Button
                    type="primary"
                    block
                    disabled={activity.status !== 'RUNNING' || remaining <= 0}
                    loading={buyingId === activity.id}
                    onClick={() => buy(activity)}
                  >
                    立即抢购
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
