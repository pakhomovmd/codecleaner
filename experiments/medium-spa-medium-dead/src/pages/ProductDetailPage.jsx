import { useParams, useNavigate } from 'react-router-dom'
import { products } from '../data/products'
import { useCart } from '../context/CartContext'
import './ProductDetailPage.css'

function ProductDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { addToCart } = useCart()
  
  const product = products.find(p => p.id === parseInt(id))

  if (!product) {
    return <div className="not-found">Товар не найден</div>
  }

  const handleAddToCart = () => {
    addToCart(product)
    navigate('/cart')
  }

  return (
    <div className="product-detail-page">
      <button className="back-btn" onClick={() => navigate(-1)}>
        ← Назад
      </button>
      
      <div className="product-detail">
        <img src={product.image} alt={product.name} className="detail-image" />
        <div className="detail-info">
          <h1>{product.name}</h1>
          <p className="detail-price">{product.price.toLocaleString('ru-RU')} ₽</p>
          <p className="detail-description">{product.description}</p>
          
          {product.inStock ? (
            <button className="add-to-cart-btn-large" onClick={handleAddToCart}>
              Добавить в корзину
            </button>
          ) : (
            <button className="out-of-stock-btn" disabled>
              Нет в наличии
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

export default ProductDetailPage
