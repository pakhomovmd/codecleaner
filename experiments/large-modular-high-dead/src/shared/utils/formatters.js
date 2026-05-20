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

// Неиспользуемая функция для форматирования процентов
export function formatPercent(value) {
  return `${(value * 100).toFixed(2)}%`
}

// Неиспользуемая функция для форматирования размера файла
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// Неиспользуемая функция для форматирования телефона
export function formatPhone(phone) {
  return phone.replace(/(\d{1})(\d{3})(\d{3})(\d{2})(\d{2})/, '+$1 ($2) $3-$4-$5')
}

// Неиспользуемая функция для капитализации
export function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase()
}

// Неиспользуемая функция для форматирования длительности
export function formatDuration(seconds) {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}
// DEAD CODE END
