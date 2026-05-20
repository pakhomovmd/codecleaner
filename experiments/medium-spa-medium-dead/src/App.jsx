import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Header from './components/Header'
import Footer from './components/Footer'
import HomePage from './pages/HomePage'
import ProductsPage from './pages/ProductsPage'
import ProductDetailPage from './pages/ProductDetailPage'
import CartPage from './pages/CartPage'
import { CartProvider } from './context/CartContext'
import './App.css'

function App() {
  return (
    <CartProvider>
      <Router>
        <div className="app">
          <Header />
          <main className="main-content">
            <Routes>
              <Route path="/" element={<HomePage />} />
              <Route path="/products" element={<ProductsPage />} />
              <Route path="/product/:id" element={<ProductDetailPage />} />
              <Route path="/cart" element={<CartPage />} />
            </Routes>
          </main>
          <Footer />
        </div>
      </Router>
    </CartProvider>
  )
}

export default App

// DEAD CODE START
// Неиспользуемая функция для старой версии роутинга
function oldRouteConfig() {
  return {
    home: '/',
    shop: '/shop',
    about: '/about'
  }
}

// Неиспользуемый компонент для старой страницы
function OldAboutPage() {
  return (
    <div className="old-about">
      <h1>About Us</h1>
      <p>Old about page content</p>
    </div>
  )
}

// Неиспользуемый компонент для уведомлений
function NotificationBanner({ message, type }) {
  return (
    <div className={`notification-banner ${type}`}>
      {message}
    </div>
  )
}

// Неиспользуемая функция для валидации
function validateRoute(path) {
  const validRoutes = ['/', '/products', '/cart', '/about']
  return validRoutes.includes(path)
}

// Неиспользуемый хук
function useOldAuth() {
  const [user, setUser] = useState(null)
  const login = (credentials) => {
    setUser(credentials)
  }
  return { user, login }
}

// Неиспользуемая функция для логирования
function logNavigation(path) {
  console.log('Navigated to:', path)
}
// DEAD CODE END
