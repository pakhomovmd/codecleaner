import { Link } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import './Header.css'

function Header() {
  const { getTotalItems } = useCart()

  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="logo">
          🛒 TechShop
        </Link>
        <nav className="nav">
          <Link to="/" className="nav-link">Главная</Link>
          <Link to="/products" className="nav-link">Товары</Link>
          <Link to="/cart" className="nav-link cart-link">
            Корзина
            {getTotalItems() > 0 && (
              <span className="cart-badge">{getTotalItems()}</span>
            )}
          </Link>
        </nav>
      </div>
    </header>
  )
}

export default Header

// DEAD CODE START
// Неиспользуемый компонент для старого меню
function OldNavigation() {
  return (
    <div className="old-nav">
      <a href="/home">Home</a>
      <a href="/shop">Shop</a>
    </div>
  )
}
// DEAD CODE END
