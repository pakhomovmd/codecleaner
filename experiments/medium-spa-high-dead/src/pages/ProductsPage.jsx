import { useState } from 'react'
import { products, categories } from '../data/products'
import ProductCard from '../components/ProductCard'
import './ProductsPage.css'

function ProductsPage() {
  const [selectedCategory, setSelectedCategory] = useState('all')

  const filteredProducts = selectedCategory === 'all'
    ? products
    : products.filter(p => p.category === selectedCategory)

  return (
    <div className="products-page">
      <h1>Каталог товаров</h1>
      
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

      <div className="products-grid">
        {filteredProducts.map(product => (
          <ProductCard key={product.id} product={product} />
        ))}
      </div>
    </div>
  )
}

export default ProductsPage

// DEAD CODE START
// Неиспользуемый компонент для сортировки
function SortDropdown({ onSort }) {
  return (
    <select onChange={(e) => onSort(e.target.value)}>
      <option value="default">По умолчанию</option>
      <option value="price-asc">Цена: по возрастанию</option>
      <option value="price-desc">Цена: по убыванию</option>
      <option value="name">По названию</option>
    </select>
  )
}

// Неиспользуемый компонент для фильтра по цене
function PriceFilter({ min, max, onChange }) {
  return (
    <div className="price-filter">
      <input 
        type="range" 
        min={min} 
        max={max} 
        onChange={(e) => onChange(e.target.value)}
      />
      <span>{min} - {max} ₽</span>
    </div>
  )
}

// Неиспользуемый компонент для поиска
function SearchInput({ onSearch }) {
  const [query, setQuery] = useState('')
  
  const handleSubmit = (e) => {
    e.preventDefault()
    onSearch(query)
  }
  
  return (
    <form onSubmit={handleSubmit}>
      <input 
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder="Поиск товаров..."
      />
      <button type="submit">Найти</button>
    </form>
  )
}

// Неиспользуемый компонент для пагинации
function Pagination({ currentPage, totalPages, onPageChange }) {
  return (
    <div className="pagination">
      <button 
        disabled={currentPage === 1}
        onClick={() => onPageChange(currentPage - 1)}
      >
        Назад
      </button>
      <span>Страница {currentPage} из {totalPages}</span>
      <button 
        disabled={currentPage === totalPages}
        onClick={() => onPageChange(currentPage + 1)}
      >
        Вперед
      </button>
    </div>
  )
}

// Неиспользуемая функция для фильтрации по цене
function filterByPriceRange(products, minPrice, maxPrice) {
  return products.filter(p => p.price >= minPrice && p.price <= maxPrice)
}

// Неиспользуемая функция для сортировки
function sortProducts(products, sortBy) {
  const sorted = [...products]
  switch(sortBy) {
    case 'price-asc':
      return sorted.sort((a, b) => a.price - b.price)
    case 'price-desc':
      return sorted.sort((a, b) => b.price - a.price)
    case 'name':
      return sorted.sort((a, b) => a.name.localeCompare(b.name))
    default:
      return sorted
  }
}

// Неиспользуемый компонент для отображения в виде списка
function ProductListView({ products }) {
  return (
    <div className="product-list-view">
      {products.map(p => (
        <div key={p.id} className="list-item">
          <img src={p.image} alt={p.name} />
          <div>
            <h3>{p.name}</h3>
            <p>{p.description}</p>
            <span>{p.price} ₽</span>
          </div>
        </div>
      ))}
    </div>
  )
}
// DEAD CODE END
