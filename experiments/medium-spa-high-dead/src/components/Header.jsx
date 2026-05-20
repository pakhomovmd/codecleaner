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

// Неиспользуемый компонент для поиска
function SearchBar() {
  const [query, setQuery] = useState('')
  
  const handleSearch = (e) => {
    e.preventDefault()
    console.log('Searching for:', query)
  }
  
  return (
    <form onSubmit={handleSearch}>
      <input 
        type="text" 
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Поиск..."
      />
      <button type="submit">Найти</button>
    </form>
  )
}

// Неиспользуемый компонент для меню пользователя
function UserMenu({ user }) {
  return (
    <div className="user-menu">
      <span>{user?.name}</span>
      <button>Выйти</button>
    </div>
  )
}

// Неиспользуемая функция для форматирования
function formatCartCount(count) {
  return count > 99 ? '99+' : count.toString()
}
// DEAD CODE END
