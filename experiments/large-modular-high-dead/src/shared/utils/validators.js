// Утилиты для валидации
export function validateEmail(email) {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return regex.test(email)
}

export function validatePassword(password) {
  return password.length >= 8
}

// DEAD CODE START
// Неиспользуемая функция для валидации телефона
export function validatePhone(phone) {
  const regex = /^\+7\d{10}$/
  return regex.test(phone)
}

// Неиспользуемая функция для валидации URL
export function validateUrl(url) {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

// Неиспользуемая функция для валидации имени
export function validateName(name) {
  return name.length >= 2 && name.length <= 50
}

// Неиспользуемая функция для валидации возраста
export function validateAge(age) {
  return age >= 18 && age <= 100
}

// Неиспользуемая функция для валидации кредитной карты
export function validateCreditCard(cardNumber) {
  const regex = /^\d{16}$/
  return regex.test(cardNumber.replace(/\s/g, ''))
}

// Неиспользуемая функция для валидации даты
export function validateDate(dateString) {
  const date = new Date(dateString)
  return date instanceof Date && !isNaN(date)
}
// DEAD CODE END
