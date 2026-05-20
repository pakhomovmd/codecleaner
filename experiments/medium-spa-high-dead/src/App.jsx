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

// Неиспользуемый компонент для загрузки
function LoadingSpinner() {
  return <div className="spinner">Loading...</div>
}

// Неиспользуемый компонент для ошибок
function ErrorBoundary({ children, fallback }) {
  const [hasError, setHasError] = useState(false)
  
  if (hasError) {
    return fallback
  }
  
  return children
}

// Неиспользуемая функция для форматирования URL
function formatUrl(path, params) {
  const query = new URLSearchParams(params).toString()
  return query ? `${path}?${query}` : path
}

// Неиспользуемый хук для медиа-запросов
function useMediaQuery(query) {
  const [matches, setMatches] = useState(false)
  
  useEffect(() => {
    const media = window.matchMedia(query)
    setMatches(media.matches)
    
    const listener = () => setMatches(media.matches)
    media.addEventListener('change', listener)
    return () => media.removeEventListener('change', listener)
  }, [query])
  
  return matches
}

// Неиспользуемый хук для локального хранилища
function useLocalStorage(key, initialValue) {
  const [value, setValue] = useState(() => {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : initialValue
  })
  
  const setStoredValue = (newValue) => {
    setValue(newValue)
    localStorage.setItem(key, JSON.stringify(newValue))
  }
  
  return [value, setStoredValue]
}

// Неиспользуемая функция для дебаунса
function debounce(func, delay) {
  let timeoutId
  return (...args) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => func(...args), delay)
  }
}

// Неиспользуемая функция для троттлинга
function throttle(func, limit) {
  let inThrottle
  return (...args) => {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// Неиспользуемый компонент для модального окна
function Modal({ isOpen, onClose, children }) {
  if (!isOpen) return null
  
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        {children}
      </div>
    </div>
  )
}

// Неиспользуемый компонент для тостов
function Toast({ message, type, onClose }) {
  useEffect(() => {
    const timer = setTimeout(onClose, 3000)
    return () => clearTimeout(timer)
  }, [onClose])
  
  return (
    <div className={`toast toast-${type}`}>
      {message}
    </div>
  )
}
// DEAD CODE END
