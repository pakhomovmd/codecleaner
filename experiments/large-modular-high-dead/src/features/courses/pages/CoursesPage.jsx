import { useState } from 'react'
import { courses, categories } from '../data/coursesData'
import CourseCard from '../components/CourseCard'
import './CoursesPage.css'

function CoursesPage() {
  const [selectedCategory, setSelectedCategory] = useState('all')

  const filteredCourses = selectedCategory === 'all'
    ? courses
    : courses.filter(c => c.category === selectedCategory)

  return (
    <div className="courses-page">
      <h1>Каталог курсов</h1>
      
      <div className="filters">
        {categories.map(cat => (
          <button
            key={cat.id}
            className={`filter-btn ${selectedCategory === cat.id ? 'active' : ''}`}
            onClick={() => setSelectedCategory(cat.id)}
          >
            {cat.name}
          </button>
        ))}
      </div>

      <div className="courses-grid">
        {filteredCourses.map(course => (
          <CourseCard key={course.id} course={course} />
        ))}
      </div>
    </div>
  )
}

export default CoursesPage
