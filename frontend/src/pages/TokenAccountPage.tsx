import { Button, Card, Form, Input, InputNumber, message, Statistic, Table, Tag } from 'antd';
import { useEffect, useState } from 'react';
import { errorMessage } from '../api/client';
import { consumeTokens, getTokenAccount, listTokenPlans, listTokenTransactions } from '../api/token';
import { newRequestId } from '../components/RequestId';
import type { TokenAccount, TokenPlan, TokenTransaction } from '../types/api';

export default function TokenAccountPage() {
  const [account, setAccount] = useState<TokenAccount | null>(null);
  const [plans, setPlans] = useState<TokenPlan[]>([]);
  const [transactions, setTransactions] = useState<TokenTransaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<{ amount: number; description?: string }>();

  const load = async () => {
    setLoading(true);
    try {
      const [accountData, planData, transactionData] = await Promise.all([
        getTokenAccount(),
        listTokenPlans(),
        listTokenTransactions(),
      ]);
      setAccount(accountData);
      setPlans(planData);
      setTransactions(transactionData);
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  return (
    <div className="page-shell">
      <h1 className="page-title">Token 账户</h1>
      <p className="page-subtitle">查看资源包余额、Plan 配额和流水。</p>
      <div className="metric-grid">
        <Card><Statistic title="资源包余额" value={account?.packBalance ?? '-'} loading={loading} /></Card>
        <Card><Statistic title="Plan 余额" value={account?.planBalance ?? '-'} loading={loading} /></Card>
        <Card><Statistic title="累计购买" value={account?.totalPurchased ?? '-'} loading={loading} /></Card>
        <Card><Statistic title="累计消费" value={account?.totalConsumed ?? '-'} loading={loading} /></Card>
      </div>

      <Card title="我的 Token Plan" className="section">
        <Table<TokenPlan>
          rowKey="id"
          loading={loading}
          pagination={false}
          dataSource={plans}
          scroll={{ x: 'max-content' }}
          columns={[
            { title: '总配额', dataIndex: 'totalQuota' },
            { title: '已使用', dataIndex: 'usedQuota' },
            { title: '剩余', dataIndex: 'remainingQuota' },
            { title: '开始时间', dataIndex: 'startTime' },
            { title: '到期时间', dataIndex: 'endTime' },
            { title: '状态', render: (_, plan) => <Tag>{plan.status}</Tag> },
          ]}
        />
      </Card>

      <Card title="模拟 Token 消费" className="section">
        <Form
          form={form}
          layout="inline"
          onFinish={async (values) => {
            setSubmitting(true);
            try {
              await consumeTokens(values.amount, newRequestId('consume'), values.description);
              message.success('消费成功');
              form.resetFields();
              await load();
            } catch (error) {
              message.error(errorMessage(error));
            } finally {
              setSubmitting(false);
            }
          }}
        >
          <Form.Item name="amount" rules={[{ required: true }]}>
            <InputNumber min={1} placeholder="消费数量" disabled={submitting} />
          </Form.Item>
          <Form.Item name="description">
            <Input placeholder="说明，可选" disabled={submitting} />
          </Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting}>消费</Button>
        </Form>
      </Card>

      <Card title="Token 流水" className="section">
        <Table<TokenTransaction>
          rowKey="id"
          loading={loading}
          pagination={false}
          dataSource={transactions}
          scroll={{ x: 'max-content' }}
          columns={[
            { title: '时间', dataIndex: 'createdAt' },
            { title: '类型', dataIndex: 'transactionType' },
            { title: '账户', dataIndex: 'balanceType' },
            {
              title: '变动',
              render: (_, item) => (
                <span style={{ color: item.amount >= 0 ? '#389e0d' : '#cf1322' }}>
                  {item.amount >= 0 ? '+' : ''}{item.amount}
                </span>
              ),
            },
            { title: '变动后', dataIndex: 'balanceAfter' },
            { title: '说明', dataIndex: 'description' },
          ]}
        />
      </Card>
    </div>
  );
}
