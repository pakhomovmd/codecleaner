import { createContext, useContext, useState } from 'react'

const CartContext = createContext()

export function useCart() {
  return useContext(CartContext)
}

export function CartProvider({ children }) {
  const [cartItems, setCartItems] = useState([])

  const addToCart = (product) => {
    setCartItems(prev => {
      const existing = prev.find(item => item.id === product.id)
      if (existing) {
        return prev.map(item =>
          item.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        )
      }
      return [...prev, { ...product, quantity: 1 }]
    })
  }

  const removeFromCart = (productId) => {
    setCartItems(prev => prev.filter(item => item.id !== productId))
  }

  const updateQuantity = (productId, quantity) => {
    if (quantity <= 0) {
      removeFromCart(productId)
      return
    }
    setCartItems(prev =>
      prev.map(item =>
        item.id === productId ? { ...item, quantity } : item
      )
    )
  }

  const clearCart = () => {
    setCartItems([])
  }

  const getTotalPrice = () => {
    return cartItems.reduce((total, item) => total + item.price * item.quantity, 0)
  }

  const getTotalItems = () => {
    return cartItems.reduce((total, item) => total + item.quantity, 0)
  }

  return (
    <CartContext.Provider value={{
      cartItems,
      addToCart,
      removeFromCart,
      updateQuantity,
      clearCart,
      getTotalPrice,
      getTotalItems
    }}>
      {children}
    </CartContext.Provider>
  )
}

// DEAD CODE START
// Неиспользуемая функция для старой версии корзины
function oldCalculateDiscount(total) {
  if (total > 10000) return total * 0.1
  return 0
}

// Неиспользуемая функция для расчета скидки по промокоду
function calculatePromoDiscount(total, promoCode) {
  const promoCodes = {
    'SAVE10': 0.1,
    'SAVE20': 0.2,
    'SAVE30': 0.3
  }
  return total * (promoCodes[promoCode] || 0)
}

// Неиспользуемая функция для расчета доставки
function calculateShipping(total, city) {
  if (total > 5000) return 0
  if (city === 'Moscow') return 300
  return 500
}

// Неиспользуемая функция для валидации корзины
function validateCart(items) {
  return items.every(item => item.quantity > 0 && item.price > 0)
}

// Неиспользуемая функция для сохранения в localStorage
function saveCartToStorage(items) {
  localStorage.setItem('savedCart', JSON.stringify(items))
}

// Неиспользуемая функция для загрузки из localStorage
function loadCartFromStorage() {
  const saved = localStorage.getItem('savedCart')
  return saved ? JSON.parse(saved) : []
}

// Неиспользуемый хук для истории корзины
function useCartHistory() {
  const [history, setHistory] = useState([])
  
  const addToHistory = (action) => {
    setHistory(prev => [...prev, { action, timestamp: Date.now() }])
  }
  
  return { history, addToHistory }
}
// DEAD CODE END
