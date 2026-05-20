import { createContext, useContext, useState } from 'react'

const AuthContext = createContext()

export function useAuth() {
  return useContext(AuthContext)
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  const login = (userData) => {
    setUser(userData)
    setIsAuthenticated(true)
  }

  const logout = () => {
    setUser(null)
    setIsAuthenticated(false)
  }

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

// DEAD CODE START
// Неиспользуемая функция для старой аутентификации
function oldAuthCheck(token) {
  return token && token.length > 0
}

// Неиспользуемая функция для проверки прав
function checkPermissions(user, permission) {
  return user?.permissions?.includes(permission)
}

// Неиспользуемая функция для обновления токена
function refreshToken(oldToken) {
  return `${oldToken}_refreshed`
}

// Неиспользуемый хук для сессии
function useSession() {
  const [session, setSession] = useState(null)
  
  const createSession = (data) => {
    setSession({ ...data, timestamp: Date.now() })
  }
  
  return { session, createSession }
}

// Неиспользуемая функция для валидации пользователя
function validateUser(userData) {
  return userData && userData.email && userData.password
}
// DEAD CODE END
