// Утилиты для работы с localStorage
export function setItem(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
}

export function getItem(key) {
  const item = localStorage.getItem(key)
  return item ? JSON.parse(item) : null
}

export function removeItem(key) {
  localStorage.removeItem(key)
}

// DEAD CODE START
// Неиспользуемая функция для очистки всего хранилища
export function clearAll() {
  localStorage.clear()
}

// Неиспользуемая функция для получения всех ключей
export function getAllKeys() {
  return Object.keys(localStorage)
}

// Неиспользуемая функция для проверки существования ключа
export function hasKey(key) {
  return localStorage.getItem(key) !== null
}

// Неиспользуемая функция для работы с sessionStorage
export function setSessionItem(key, value) {
  sessionStorage.setItem(key, JSON.stringify(value))
}

export function getSessionItem(key) {
  const item = sessionStorage.getItem(key)
  return item ? JSON.parse(item) : null
}

// Неиспользуемая функция для получения размера хранилища
export function getStorageSize() {
  let total = 0
  for (let key in localStorage) {
    if (localStorage.hasOwnProperty(key)) {
      total += localStorage[key].length + key.length
    }
  }
  return total
}
// DEAD CODE END
