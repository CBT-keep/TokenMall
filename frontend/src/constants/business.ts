import dayjs, { type Dayjs } from 'dayjs';

export const PRODUCT_TYPE_OPTIONS = [
  { label: 'Token 资源包', value: 'TOKEN_PACK' },
  { label: 'Token Plan', value: 'TOKEN_PLAN' },
];

export const ENABLED_STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

export const ORDER_STATUS_OPTIONS = [
  { label: '待支付', value: 'PENDING_PAYMENT' },
  { label: '已支付', value: 'PAID' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已关闭', value: 'CLOSED' },
];

export const SECKILL_STATUS_OPTIONS = [
  { label: '草稿', value: 'DRAFT' },
  { label: '待开始', value: 'READY' },
  { label: '进行中', value: 'RUNNING' },
  { label: '已结束', value: 'ENDED' },
  { label: '已取消', value: 'CANCELLED' },
];

const PRODUCT_TYPE_LABELS: Record<string, string> = Object.fromEntries(
  PRODUCT_TYPE_OPTIONS.map((item) => [item.value, item.label]),
);

const ORDER_STATUS_META: Record<string, { label: string; color: string }> = {
  PENDING_PAYMENT: { label: '待支付', color: 'orange' },
  PAID: { label: '已支付', color: 'green' },
  COMPLETED: { label: '已完成', color: 'blue' },
  CANCELLED: { label: '已取消', color: 'default' },
  CLOSED: { label: '已关闭', color: 'red' },
};

const SECKILL_STATUS_META: Record<string, { label: string; color: string }> = {
  DRAFT: { label: '草稿', color: 'default' },
  READY: { label: '待开始', color: 'blue' },
  RUNNING: { label: '进行中', color: 'volcano' },
  ENDED: { label: '已结束', color: 'default' },
  CANCELLED: { label: '已取消', color: 'red' },
};

export function productTypeLabel(value?: string): string {
  return value ? PRODUCT_TYPE_LABELS[value] ?? value : '-';
}

export function orderStatusMeta(value?: string): { label: string; color: string } {
  return value
    ? ORDER_STATUS_META[value] ?? { label: value, color: 'default' }
    : { label: '-', color: 'default' };
}

export function seckillStatusMeta(value?: string): { label: string; color: string } {
  return value
    ? SECKILL_STATUS_META[value] ?? { label: value, color: 'default' }
    : { label: '-', color: 'default' };
}

export function seckillRuntimeStatus(
  activity: { status: string; startTime: string; endTime: string },
  now: Dayjs = dayjs(),
): string {
  if (activity.status !== 'RUNNING') {
    return activity.status;
  }
  if (now.isBefore(dayjs(activity.startTime))) {
    return 'READY';
  }
  if (now.isAfter(dayjs(activity.endTime))) {
    return 'ENDED';
  }
  return 'RUNNING';
}
