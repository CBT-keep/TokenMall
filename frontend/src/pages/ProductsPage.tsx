import { Card, Empty, Radio, Skeleton, Space, Typography } from 'antd';
import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { listProducts } from '../api/catalog';
import type { ProductSummary } from '../types/api';

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const type = searchParams.get('type') ?? '';
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    listProducts(type || undefined, 1, 50)
      .then((page) => setProducts(page.records))
      .finally(() => setLoading(false));
  }, [type]);

  return (
    <div className="page-shell">
      <h1 className="page-title">商品列表</h1>
      <p className="page-subtitle">选择 Token 资源包或 Token Plan。</p>
      <div className="toolbar">
        <Radio.Group
          value={type}
          onChange={(event) => {
            const value = event.target.value;
            setSearchParams(value ? { type: value } : {});
          }}
        >
          <Radio.Button value="">全部</Radio.Button>
          <Radio.Button value="TOKEN_PACK">资源包</Radio.Button>
          <Radio.Button value="TOKEN_PLAN">Token Plan</Radio.Button>
        </Radio.Group>
      </div>
      {loading ? (
        <Skeleton active />
      ) : products.length === 0 ? (
        <Empty description="暂无商品" />
      ) : (
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
                <Link to={`/products/${product.id}`}>查看详情</Link>
              </Space>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
