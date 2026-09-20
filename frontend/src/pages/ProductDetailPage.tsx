import { ShoppingCartOutlined, ThunderboltOutlined } from '@ant-design/icons';
import { Button, Card, Descriptions, InputNumber, message, Radio, Skeleton, Space, Typography } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { addCartItem } from '../api/cart';
import { getProduct } from '../api/catalog';
import { errorMessage } from '../api/client';
import { createDirectOrder } from '../api/orders';
import { newRequestId } from '../components/RequestId';
import type { ProductDetail } from '../types/api';

export default function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [skuId, setSkuId] = useState<number>();
  const [quantity, setQuantity] = useState(1);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!id) {
      return;
    }
    getProduct(Number(id))
      .then((data) => {
        setProduct(data);
        setSkuId(data.skus[0]?.id);
      })
      .catch((error) => message.error(errorMessage(error)))
      .finally(() => setLoading(false));
  }, [id]);

  const selectedSku = useMemo(
    () => product?.skus.find((sku) => sku.id === skuId),
    [product, skuId],
  );

  const addToCart = async () => {
    if (!selectedSku) {
      message.warning('请选择 SKU');
      return;
    }
    setSubmitting(true);
    try {
      await addCartItem(selectedSku.id, quantity);
      message.success('已加入购物车');
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  const buyNow = async () => {
    if (!selectedSku) {
      message.warning('请选择 SKU');
      return;
    }
    setSubmitting(true);
    try {
      const order = await createDirectOrder(selectedSku.id, quantity, newRequestId('direct'));
      navigate(`/orders/${order.orderNo}`);
    } catch (error) {
      message.error(errorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="page-shell"><Skeleton active /></div>;
  }
  if (!product) {
    return <div className="page-shell">商品不存在</div>;
  }

  return (
    <div className="page-shell">
      <Button type="link" onClick={() => navigate(-1)} style={{ paddingLeft: 0 }}>
        返回
      </Button>
      <Card>
        <Typography.Title level={2}>{product.name}</Typography.Title>
        <Typography.Paragraph type="secondary">{product.subtitle}</Typography.Paragraph>
        <div className="price">¥{selectedSku?.price ?? product.price}</div>
        <Descriptions column={1} style={{ marginTop: 24 }}>
          <Descriptions.Item label="商品类型">
            {product.productType === 'TOKEN_PACK' ? 'Token 资源包' : 'Token Plan'}
          </Descriptions.Item>
          <Descriptions.Item label="Token 数量">
            {product.productType === 'TOKEN_PACK'
              ? `${product.tokenAmount?.toLocaleString()} Token`
              : `${product.planQuota?.toLocaleString()} Token`}
          </Descriptions.Item>
          {product.productType === 'TOKEN_PLAN' && (
            <Descriptions.Item label="有效期">{product.planDays} 天</Descriptions.Item>
          )}
          <Descriptions.Item label="说明">{product.description}</Descriptions.Item>
        </Descriptions>

        <div className="section">
          <Typography.Title level={5}>选择 SKU</Typography.Title>
          <Radio.Group value={skuId} onChange={(event) => setSkuId(event.target.value)}>
            <Space wrap>
              {product.skus.map((sku) => (
                <Radio.Button key={sku.id} value={sku.id} disabled={sku.status !== 1}>
                  {sku.name}
                </Radio.Button>
              ))}
            </Space>
          </Radio.Group>
        </div>

        <div className="section">
          <Space size="large" wrap>
            <Space>
              <Typography.Text>数量</Typography.Text>
              <InputNumber min={1} value={quantity} onChange={(value) => setQuantity(value ?? 1)} />
            </Space>
            <Button
              icon={<ShoppingCartOutlined />}
              onClick={addToCart}
              loading={submitting}
            >
              加入购物车
            </Button>
            <Button
              type="primary"
              icon={<ThunderboltOutlined />}
              onClick={buyNow}
              loading={submitting}
            >
              立即购买
            </Button>
          </Space>
        </div>
      </Card>
    </div>
  );
}
