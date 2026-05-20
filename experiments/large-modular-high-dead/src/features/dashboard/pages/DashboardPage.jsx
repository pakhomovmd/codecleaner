import { useAuth } from '../../../shared/contexts/AuthContext'
import './DashboardPage.css'

function DashboardPage() {
  const { isAuthenticated } = useAuth()

  if (!isAuthenticated) {
    return (
      <div className="dashboard-page">
        <h1>Панель управления</h1>
        <p>Пожалуйста, войдите в систему</p>
      </div>
    )
  }

  return (
    <div className="dashboard-page">
      <h1>Панель управления</h1>
      
      <div className="dashboard-grid">
        <div className="dashboard-card">
          <h3>📚 Мои курсы</h3>
          <p className="dashboard-number">0</p>
        </div>
        <div className="dashboard-card">
          <h3>✅ Завершено</h3>
          <p className="dashboard-number">0</p>
        </div>
        <div className="dashboard-card">
          <h3>⏳ В процессе</h3>
          <p className="dashboard-number">0</p>
        </div>
        <div className="dashboard-card">
          <h3>🎯 Прогресс</h3>
          <p className="dashboard-number">0%</p>
        </div>
      </div>

      <div className="dashboard-section">
        <h2>Недавняя активность</h2>
        <p>Нет активности</p>
      </div>
    </div>
  )
}

export default DashboardPage
