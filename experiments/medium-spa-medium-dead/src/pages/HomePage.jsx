import { Link } from 'react-router-dom'
import { products } from '../data/products'
import ProductCard from '../components/ProductCard'
import './HomePage.css'

function HomePage() {
  const featuredProducts = products.slice(0, 4)

  return (
    <div className="home-page">
      <section className="hero">
        <div className="hero-content">
          <h1>Добро пожаловать в TechShop</h1>
          <p>Лучшая электроника по выгодным ценам</p>
          <Link to="/products" className="hero-btn">Смотреть товары</Link>
        </div>
      </section>

      <section className="featured-section">
        <h2>Популярные товары</h2>
        <div className="products-grid">
          {featuredProducts.map(product => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      </section>
    </div>
  )
}

export default HomePage

// DEAD CODE START
// Неиспользуемый компонент для старого баннера
function OldBanner() {
  return (
    <div className="old-banner">
      <h2>Special Offer!</h2>
    </div>
  )
}

// Неиспользуемый компонент для слайдера
function ImageSlider({ images }) {
  const [currentIndex, setCurrentIndex] = useState(0)
  
  const nextSlide = () => {
    setCurrentIndex((prev) => (prev + 1) % images.length)
  }
  
  const prevSlide = () => {
    setCurrentIndex((prev) => (prev - 1 + images.length) % images.length)
  }
  
  return (
    <div className="slider">
      <button onClick={prevSlide}>←</button>
      <img src={images[currentIndex]} alt="Slide" />
      <button onClick={nextSlide}>→</button>
    </div>
  )
}

// Неиспользуемый компонент для отзывов
function TestimonialSection() {
  const testimonials = [
    { id: 1, name: 'Иван', text: 'Отличный магазин!' },
    { id: 2, name: 'Мария', text: 'Быстрая доставка' }
  ]
  
  return (
    <div className="testimonials">
      {testimonials.map(t => (
        <div key={t.id} className="testimonial">
          <p>{t.text}</p>
          <span>- {t.name}</span>
        </div>
      ))}
    </div>
  )
}

// Неиспользуемый компонент для категорий
function CategoryGrid() {
  const categories = ['Смартфоны', 'Ноутбуки', 'Планшеты']
  
  return (
    <div className="category-grid">
      {categories.map(cat => (
        <div key={cat} className="category-card">
          <h3>{cat}</h3>
        </div>
      ))}
    </div>
  )
}
// DEAD CODE END
