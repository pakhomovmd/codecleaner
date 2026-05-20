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

// DEAD CODE START
// Неиспользуемый компонент для галереи изображений
function ImageGallery({ images }) {
  const [currentIndex, setCurrentIndex] = useState(0)
  
  return (
    <div className="image-gallery">
      <img src={images[currentIndex]} alt="Product" />
      <div className="thumbnails">
        {images.map((img, idx) => (
          <img 
            key={idx}
            src={img}
            onClick={() => setCurrentIndex(idx)}
            className={idx === currentIndex ? 'active' : ''}
          />
        ))}
      </div>
    </div>
  )
}

// Неиспользуемый компонент для отзывов
function ProductReviews({ reviews }) {
  return (
    <div className="product-reviews">
      <h3>Отзывы</h3>
      {reviews.map(review => (
        <div key={review.id} className="review">
          <div className="review-header">
            <span>{review.author}</span>
            <span>{'⭐'.repeat(review.rating)}</span>
          </div>
          <p>{review.text}</p>
        </div>
      ))}
    </div>
  )
}

// Неиспользуемый компонент для характеристик
function ProductSpecs({ specs }) {
  return (
    <div className="product-specs">
      <h3>Характеристики</h3>
      <table>
        <tbody>
          {Object.entries(specs).map(([key, value]) => (
            <tr key={key}>
              <td>{key}</td>
              <td>{value}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

// Неиспользуемый компонент для выбора количества
function QuantitySelector({ value, onChange, max }) {
  return (
    <div className="quantity-selector">
      <button onClick={() => onChange(Math.max(1, value - 1))}>-</button>
      <input 
        type="number" 
        value={value}
        onChange={(e) => onChange(Math.min(max, parseInt(e.target.value) || 1))}
      />
      <button onClick={() => onChange(Math.min(max, value + 1))}>+</button>
    </div>
  )
}

// Неиспользуемый компонент для выбора варианта
function VariantSelector({ variants, selected, onSelect }) {
  return (
    <div className="variant-selector">
      {variants.map(variant => (
        <button
          key={variant.id}
          className={selected === variant.id ? 'active' : ''}
          onClick={() => onSelect(variant.id)}
        >
          {variant.name}
        </button>
      ))}
    </div>
  )
}

// Неиспользуемый компонент для похожих товаров
function SimilarProducts({ products }) {
  return (
    <div className="similar-products">
      <h3>Похожие товары</h3>
      <div className="similar-grid">
        {products.map(p => (
          <div key={p.id} className="similar-item">
            <img src={p.image} alt={p.name} />
            <span>{p.name}</span>
            <span>{p.price} ₽</span>
          </div>
        ))}
      </div>
    </div>
  )
}

// Неиспользуемая функция для расчета рейтинга
function calculateAverageRating(reviews) {
  if (reviews.length === 0) return 0
  const sum = reviews.reduce((acc, r) => acc + r.rating, 0)
  return (sum / reviews.length).toFixed(1)
}

// Неиспользуемая функция для проверки избранного
function isInWishlist(productId, wishlist) {
  return wishlist.includes(productId)
}
// DEAD CODE END
