import { useAuth } from '../../../shared/contexts/AuthContext'
import './ProfilePage.css'

function ProfilePage() {
  const { user, isAuthenticated } = useAuth()

  if (!isAuthenticated) {
    return (
      <div className="profile-page">
        <h1>Профиль</h1>
        <p>Пожалуйста, войдите в систему</p>
      </div>
    )
  }

  return (
    <div className="profile-page">
      <h1>Мой профиль</h1>
      <div className="profile-card">
        <div className="profile-avatar">👤</div>
        <div className="profile-info">
          <h2>{user?.name || 'Пользователь'}</h2>
          <p>{user?.email || 'user@example.com'}</p>
        </div>
      </div>

      <div className="profile-section">
        <h3>Мои курсы</h3>
        <p>У вас пока нет записей на курсы</p>
      </div>
    </div>
  )
}

export default ProfilePage
