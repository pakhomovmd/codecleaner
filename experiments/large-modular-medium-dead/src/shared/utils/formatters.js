// Утилиты для форматирования
export function formatDate(date) {
  return new Date(date).toLocaleDateString('ru-RU')
}

export function formatPrice(price) {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB'
  }).format(price)
}

// DEAD CODE START
// Неиспользуемая функция для форматирования времени
export function formatTime(date) {
  return new Date(date).toLocaleTimeString('ru-RU')
}

// Неиспользуемая функция для форматирования числа
export function formatNumber(num) {
  return new Intl.NumberFormat('ru-RU').format(num)
}

// Неиспользуемая функция для сокращения текста
export function truncateText(text, maxLength) {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}
// DEAD CODE END
