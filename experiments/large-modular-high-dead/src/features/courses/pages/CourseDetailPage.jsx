import { useParams, useNavigate } from 'react-router-dom'
import { courses } from '../data/coursesData'
import { useAuth } from '../../../shared/contexts/AuthContext'
import './CourseDetailPage.css'

function CourseDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  
  const course = courses.find(c => c.id === parseInt(id))

  if (!course) {
    return <div className="not-found">Курс не найден</div>
  }

  const handleEnroll = () => {
    if (!isAuthenticated) {
      alert('Пожалуйста, войдите в систему')
      return
    }
    alert('Вы записаны на курс!')
  }

  return (
    <div className="course-detail-page">
      <button className="back-btn" onClick={() => navigate(-1)}>
        ← Назад
      </button>
      
      <div className="course-detail">
        <img src={course.image} alt={course.title} className="detail-image" />
        <div className="detail-info">
          <h1>{course.title}</h1>
          <p className="detail-instructor">👨‍🏫 Преподаватель: {course.instructor}</p>
          <p className="detail-duration">⏱️ Длительность: {course.duration}</p>
          <p className="detail-level">📊 Уровень: {course.level}</p>
          <p className="detail-enrolled">👥 Студентов: {course.enrolled}</p>
          <p className="detail-description">{course.description}</p>
          <p className="detail-price">{course.price.toLocaleString('ru-RU')} ₽</p>
          <button className="enroll-btn" onClick={handleEnroll}>
            Записаться на курс
          </button>
        </div>
      </div>
    </div>
  )
}

export default CourseDetailPage
