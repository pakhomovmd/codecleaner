import { Link } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'
import './Header.css'

function Header() {
  const { isAuthenticated, user, logout } = useAuth()

  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="logo">
          📚 EduPlatform
        </Link>
        <nav className="nav">
          <Link to="/" className="nav-link">Главная</Link>
          <Link to="/courses" className="nav-link">Курсы</Link>
          {isAuthenticated && (
            <>
              <Link to="/dashboard" className="nav-link">Панель</Link>
              <Link to="/profile" className="nav-link">Профиль</Link>
              <button onClick={logout} className="logout-btn">Выйти</button>
            </>
          )}
        </nav>
      </div>
    </header>
  )
}

export default Header

// DEAD CODE START
// Неиспользуемый компонент для старого меню
function OldMenu() {
  return <div className="old-menu">Menu</div>
}
// DEAD CODE END
