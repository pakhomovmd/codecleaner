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
// DEAD CODE END
