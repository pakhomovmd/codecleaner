export const products = [
  {
    id: 1,
    name: 'iPhone 15 Pro',
    category: 'smartphones',
    price: 89990,
    image: 'https://via.placeholder.com/300x300/667eea/ffffff?text=iPhone+15+Pro',
    description: 'Новейший флагманский смартфон от Apple с процессором A17 Pro',
    inStock: true
  },
  {
    id: 2,
    name: 'Samsung Galaxy S24',
    category: 'smartphones',
    price: 74990,
    image: 'https://via.placeholder.com/300x300/764ba2/ffffff?text=Galaxy+S24',
    description: 'Мощный Android-смартфон с отличной камерой',
    inStock: true
  },
  {
    id: 3,
    name: 'MacBook Pro 14"',
    category: 'laptops',
    price: 149990,
    image: 'https://via.placeholder.com/300x300/f093fb/ffffff?text=MacBook+Pro',
    description: 'Профессиональный ноутбук с чипом M3 Pro',
    inStock: true
  },
  {
    id: 4,
    name: 'Dell XPS 15',
    category: 'laptops',
    price: 119990,
    image: 'https://via.placeholder.com/300x300/4facfe/ffffff?text=Dell+XPS+15',
    description: 'Мощный ноутбук для работы и творчества',
    inStock: true
  },
  {
    id: 5,
    name: 'iPad Air',
    category: 'tablets',
    price: 54990,
    image: 'https://via.placeholder.com/300x300/00f2fe/ffffff?text=iPad+Air',
    description: 'Универсальный планшет для работы и развлечений',
    inStock: true
  },
  {
    id: 6,
    name: 'AirPods Pro',
    category: 'accessories',
    price: 21990,
    image: 'https://via.placeholder.com/300x300/43e97b/ffffff?text=AirPods+Pro',
    description: 'Беспроводные наушники с активным шумоподавлением',
    inStock: true
  },
  {
    id: 7,
    name: 'Apple Watch Series 9',
    category: 'accessories',
    price: 34990,
    image: 'https://via.placeholder.com/300x300/38f9d7/ffffff?text=Watch+S9',
    description: 'Умные часы с расширенными функциями здоровья',
    inStock: true
  },
  {
    id: 8,
    name: 'Sony WH-1000XM5',
    category: 'accessories',
    price: 29990,
    image: 'https://via.placeholder.com/300x300/fa709a/ffffff?text=Sony+WH',
    description: 'Премиальные наушники с лучшим шумоподавлением',
    inStock: false
  }
]

export const categories = [
  { id: 'all', name: 'Все товары' },
  { id: 'smartphones', name: 'Смартфоны' },
  { id: 'laptops', name: 'Ноутбуки' },
  { id: 'tablets', name: 'Планшеты' },
  { id: 'accessories', name: 'Аксессуары' }
]

// DEAD CODE START
// Неиспользуемые данные для старой версии
export const oldCategories = [
  { id: 1, title: 'Phones' },
  { id: 2, title: 'Computers' }
]

export const deprecatedProducts = [
  { id: 999, name: 'Old Product', price: 0 }
]

// Неиспользуемые данные для брендов
export const brands = [
  { id: 1, name: 'Apple', logo: '/logos/apple.png' },
  { id: 2, name: 'Samsung', logo: '/logos/samsung.png' },
  { id: 3, name: 'Dell', logo: '/logos/dell.png' },
  { id: 4, name: 'Sony', logo: '/logos/sony.png' }
]

// Неиспользуемые данные для отзывов
export const reviews = [
  { id: 1, productId: 1, rating: 5, text: 'Отличный товар!' },
  { id: 2, productId: 1, rating: 4, text: 'Хорошо, но дорого' }
]

// Неиспользуемые данные для промокодов
export const promoCodes = [
  { code: 'SAVE10', discount: 10, minPurchase: 5000 },
  { code: 'SAVE20', discount: 20, minPurchase: 10000 }
]

// Неиспользуемая функция для фильтрации
export function filterByPrice(products, minPrice, maxPrice) {
  return products.filter(p => p.price >= minPrice && p.price <= maxPrice)
}

// Неиспользуемая функция для сортировки
export function sortProducts(products, sortBy) {
  const sorted = [...products]
  if (sortBy === 'price-asc') {
    return sorted.sort((a, b) => a.price - b.price)
  }
  if (sortBy === 'price-desc') {
    return sorted.sort((a, b) => b.price - a.price)
  }
  return sorted
}

// Неиспользуемая функция для поиска
export function searchProducts(products, query) {
  return products.filter(p => 
    p.name.toLowerCase().includes(query.toLowerCase())
  )
}
// DEAD CODE END
