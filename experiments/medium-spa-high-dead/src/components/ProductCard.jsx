import { Link } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import './ProductCard.css'

function ProductCard({ product }) {
  const { addToCart } = useCart()

  const handleAddToCart = (e) => {
    e.preventDefault()
    addToCart(product)
  }

  return (
    <Link to={`/product/${product.id}`} className="product-card">
      <img src={product.image} alt={product.name} className="product-image" />
      <div className="product-info">
        <h3 className="product-name">{product.name}</h3>
        <p className="product-price">{product.price.toLocaleString('ru-RU')} ₽</p>
        {!product.inStock && <span className="out-of-stock">Нет в наличии</span>}
        {product.inStock && (
          <button 
            className="add-to-cart-btn"
            onClick={handleAddToCart}
          >
            В корзину
          </button>
        )}
      </div>
    </Link>
  )
}

export default ProductCard

// DEAD CODE START
// Неиспользуемый компонент для карточки товара в списке
function ProductListItem({ product, onSelect }) {
  return (
    <div className="product-list-item" onClick={() => onSelect(product.id)}>
      <img src={product.image} alt={product.name} />
      <span>{product.name}</span>
      <span>{product.price} ₽</span>
    </div>
  )
}

// Неиспользуемый компонент для рейтинга
function ProductRating({ rating, reviews }) {
  return (
    <div className="product-rating">
      <span>{'⭐'.repeat(rating)}</span>
      <span>({reviews} отзывов)</span>
    </div>
  )
}

// Неиспользуемый компонент для бейджа
function ProductBadge({ text, type }) {
  return <span className={`product-badge badge-${type}`}>{text}</span>
}

// Неиспользуемая функция для форматирования цены
function formatPrice(price, currency = 'RUB') {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: currency
  }).format(price)
}

// Неиспользуемая функция для расчета скидки
function calculateDiscount(originalPrice, discountPercent) {
  return originalPrice * (1 - discountPercent / 100)
}

// Неиспользуемый компонент для сравнения товаров
function ProductCompare({ products }) {
  return (
    <div className="product-compare">
      {products.map(p => (
        <div key={p.id} className="compare-item">
          <h4>{p.name}</h4>
          <p>{p.price} ₽</p>
        </div>
      ))}
    </div>
  )
}
// DEAD CODE END
