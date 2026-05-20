// Утилиты для работы с API
export const API_BASE_URL = 'https://api.eduplatform.com'

export async function fetchData(endpoint) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`)
  return response.json()
}

export async function postData(endpoint, data) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return response.json()
}

// DEAD CODE START
// Неиспользуемая функция для старого API
export async function oldFetchData(url) {
  return fetch(url).then(r => r.json())
}

// Неиспользуемая функция для обработки ошибок
export function handleApiError(error) {
  console.error('API Error:', error)
  return { error: error.message }
}

// Неиспользуемая функция для PUT запроса
export async function putData(endpoint, data) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  })
  return response.json()
}

// Неиспользуемая функция для DELETE запроса
export async function deleteData(endpoint) {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'DELETE'
  })
  return response.json()
}

// Неиспользуемая функция для загрузки файлов
export async function uploadFile(endpoint, file) {
  const formData = new FormData()
  formData.append('file', file)
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'POST',
    body: formData
  })
  return response.json()
}

// Неиспользуемая функция для работы с токенами
export function setAuthToken(token) {
  localStorage.setItem('authToken', token)
}

export function getAuthToken() {
  return localStorage.getItem('authToken')
}

export function removeAuthToken() {
  localStorage.removeItem('authToken')
}
// DEAD CODE END
