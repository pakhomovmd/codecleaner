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
// DEAD CODE END
