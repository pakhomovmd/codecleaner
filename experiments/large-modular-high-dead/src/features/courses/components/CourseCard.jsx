import { Link } from 'react-router-dom'
import './CourseCard.css'

function CourseCard({ course }) {
  return (
    <Link to={`/course/${course.id}`} className="course-card">
      <img src={course.image} alt={course.title} className="course-image" />
      <div className="course-info">
        <h3 className="course-title">{course.title}</h3>
        <p className="course-instructor">👨‍🏫 {course.instructor}</p>
        <p className="course-duration">⏱️ {course.duration}</p>
        <div className="course-footer">
          <span className="course-price">{course.price.toLocaleString('ru-RU')} ₽</span>
          <span className="course-rating">⭐ {course.rating}</span>
        </div>
      </div>
    </Link>
  )
}

export default CourseCard

// DEAD CODE START
// Неиспользуемый компонент для старой карточки
function OldCourseCard() {
  return <div>Old Card</div>
}

// Неиспользуемый компонент для списка курсов
function CourseListItem({ course }) {
  return (
    <div className="course-list-item">
      <span>{course.title}</span>
      <span>{course.price} ₽</span>
    </div>
  )
}

// Неиспользуемый компонент для рейтинга
function CourseRating({ rating }) {
  return <div>{'⭐'.repeat(rating)}</div>
}

// Неиспользуемая функция для форматирования цены
function formatPrice(price) {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB'
  }).format(price)
}
// DEAD CODE END
