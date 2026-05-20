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
// DEAD CODE END
