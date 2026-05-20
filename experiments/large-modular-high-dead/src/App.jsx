import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './shared/contexts/AuthContext'
import Layout from './shared/components/Layout/Layout'
import HomePage from './features/home/pages/HomePage'
import CoursesPage from './features/courses/pages/CoursesPage'
import CourseDetailPage from './features/courses/pages/CourseDetailPage'
import ProfilePage from './features/profile/pages/ProfilePage'
import DashboardPage from './features/dashboard/pages/DashboardPage'
import './App.css'

function App() {
  return (
    <AuthProvider>
      <Router>
        <Layout>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/courses" element={<CoursesPage />} />
            <Route path="/course/:id" element={<CourseDetailPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
          </Routes>
        </Layout>
      </Router>
    </AuthProvider>
  )
}

export default App

// DEAD CODE START
// Неиспользуемая функция для старого роутинга
function getOldRoutes() {
  return ['/home', '/about', '/contact']
}

// Неиспользуемый компонент для старой страницы
function OldAboutPage() {
  return <div>About Us</div>
}

// Неиспользуемая функция для валидации роутов
function validateRoute(path) {
  const validRoutes = ['/', '/courses', '/profile', '/dashboard']
  return validRoutes.includes(path)
}

// Неиспользуемый хук для навигации
function useOldNavigation() {
  const navigate = (path) => {
    console.log('Navigating to:', path)
  }
  return { navigate }
}

// Неиспользуемая функция для логирования
function logPageView(page) {
  console.log('Page viewed:', page)
}

// Неиспользуемый компонент для загрузки
function LoadingSpinner() {
  return <div className="spinner">Loading...</div>
}

// Неиспользуемый компонент для ошибок
function ErrorBoundary({ children }) {
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
  return matches
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
      <div className="modal-content">
        {children}
      </div>
    </div>
  )
}

// Неиспользуемый компонент для тостов
function Toast({ message, type }) {
  return <div className={`toast toast-${type}`}>{message}</div>
}
// DEAD CODE END
