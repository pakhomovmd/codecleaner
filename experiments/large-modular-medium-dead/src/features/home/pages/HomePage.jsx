import { Link } from 'react-router-dom'
import './HomePage.css'

function HomePage() {
  return (
    <div className="home-page">
      <section className="hero">
        <div className="hero-content">
          <h1>Добро пожаловать в EduPlatform</h1>
          <p>Обучайтесь у лучших преподавателей онлайн</p>
          <Link to="/courses" className="hero-btn">Смотреть курсы</Link>
        </div>
      </section>

      <section className="features">
        <h2>Почему выбирают нас</h2>
        <div className="features-grid">
          <div className="feature-card">
            <h3>🎓 Качественное обучение</h3>
            <p>Курсы от экспертов индустрии</p>
          </div>
          <div className="feature-card">
            <h3>⏰ Гибкий график</h3>
            <p>Учитесь в удобное время</p>
          </div>
          <div className="feature-card">
            <h3>📜 Сертификаты</h3>
            <p>Получите признанный сертификат</p>
          </div>
        </div>
      </section>
    </div>
  )
}

export default HomePage

// DEAD CODE START
// Неиспользуемый компонент для старого баннера
function OldBanner() {
  return <div className="old-banner">Banner</div>
}

// Неиспользуемый компонент для отзывов
function TestimonialsSection() {
  const testimonials = [
    { id: 1, name: 'Иван', text: 'Отличная платформа!' },
    { id: 2, name: 'Мария', text: 'Много полезных курсов' }
  ]
  
  return (
    <div className="testimonials">
      {testimonials.map(t => (
        <div key={t.id} className="testimonial">
          <p>{t.text}</p>
          <span>- {t.name}</span>
        </div>
      ))}
    </div>
  )
}

// Неиспользуемый компонент для статистики
function StatsSection() {
  return (
    <div className="stats">
      <div className="stat-item">
        <h3>1000+</h3>
        <p>Студентов</p>
      </div>
      <div className="stat-item">
        <h3>50+</h3>
        <p>Курсов</p>
      </div>
    </div>
  )
}
// DEAD CODE END
