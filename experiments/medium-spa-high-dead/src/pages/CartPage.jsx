import { useCart } from '../context/CartContext'
import { Link } from 'react-router-dom'
import './CartPage.css'

function CartPage() {
  const { cartItems, removeFromCart, updateQuantity, getTotalPrice, clearCart } = useCart()

  if (cartItems.length === 0) {
    return (
      <div className="empty-cart">
        <h2>Корзина пуста</h2>
        <Link to="/products" className="continue-shopping">
          Перейти к покупкам
        </Link>
      </div>
    )
  }

  return (
    <div className="cart-page">
      <h1>Корзина</h1>
      
      <div className="cart-items">
        {cartItems.map(item => (
          <div key={item.id} className="cart-item">
            <img src={item.image} alt={item.name} className="cart-item-image" />
            <div className="cart-item-info">
              <h3>{item.name}</h3>
              <p className="cart-item-price">{item.price.toLocaleString('ru-RU')} ₽</p>
            </div>
            <div className="cart-item-controls">
              <button onClick={() => updateQuantity(item.id, item.quantity - 1)}>-</button>
              <span>{item.quantity}</span>
              <button onClick={() => updateQuantity(item.id, item.quantity + 1)}>+</button>
            </div>
            <div className="cart-item-total">
              {(item.price * item.quantity).toLocaleString('ru-RU')} ₽
            </div>
            <button 
              className="remove-btn"
              onClick={() => removeFromCart(item.id)}
            >
              ✕
            </button>
          </div>
        ))}
      </div>

      <div className="cart-summary">
        <h2>Итого: {getTotalPrice().toLocaleString('ru-RU')} ₽</h2>
        <button className="checkout-btn">Оформить заказ</button>
        <button className="clear-cart-btn" onClick={clearCart}>Очистить корзину</button>
      </div>
    </div>
  )
}

export default CartPage

// DEAD CODE START
// Неиспользуемый компонент для промокода
function PromoCodeInput({ onApply }) {
  const [code, setCode] = useState('')
  
  const handleSubmit = (e) => {
    e.preventDefault()
    onApply(code)
  }
  
  return (
    <form onSubmit={handleSubmit} className="promo-form">
      <input 
        type="text"
        value={code}
        onChange={(e) => setCode(e.target.value)}
        placeholder="Введите промокод"
      />
      <button type="submit">Применить</button>
    </form>
  )
}

// Неиспользуемый компонент для выбора доставки
function DeliveryOptions({ selected, onSelect }) {
  const options = [
    { id: 'pickup', name: 'Самовывоз', price: 0 },
    { id: 'courier', name: 'Курьер', price: 300 },
    { id: 'express', name: 'Экспресс', price: 500 }
  ]
  
  return (
    <div className="delivery-options">
      {options.map(opt => (
        <label key={opt.id}>
          <input 
            type="radio"
            checked={selected === opt.id}
            onChange={() => onSelect(opt.id)}
          />
          {opt.name} - {opt.price} ₽
        </label>
      ))}
    </div>
  )
}

// Неиспользуемый компонент для способа оплаты
function PaymentMethods({ selected, onSelect }) {
  const methods = ['card', 'cash', 'online']
  
  return (
    <div className="payment-methods">
      {methods.map(method => (
        <button 
          key={method}
          className={selected === method ? 'active' : ''}
          onClick={() => onSelect(method)}
        >
          {method}
        </button>
      ))}
    </div>
  )
}

// Неиспользуемый компонент для сохраненных адресов
function SavedAddresses({ addresses, onSelect }) {
  return (
    <div className="saved-addresses">
      {addresses.map(addr => (
        <div key={addr.id} onClick={() => onSelect(addr)}>
          <p>{addr.street}</p>
          <p>{addr.city}</p>
        </div>
      ))}
    </div>
  )
}

// Неиспользуемая функция для валидации корзины
function validateCartItems(items) {
  return items.every(item => item.quantity > 0 && item.inStock)
}

// Неиспользуемая функция для расчета веса
function calculateTotalWeight(items) {
  return items.reduce((total, item) => total + (item.weight || 0) * item.quantity, 0)
}

// Неиспользуемая функция для проверки наличия
function checkAvailability(items) {
  return items.map(item => ({
    ...item,
    available: item.inStock && item.quantity <= item.stockCount
  }))
}

// Неиспользуемый компонент для рекомендаций
function RecommendedProducts({ products }) {
  return (
    <div className="recommended">
      <h3>Вам может понравиться</h3>
      <div className="recommended-grid">
        {products.map(p => (
          <div key={p.id} className="recommended-item">
            <img src={p.image} alt={p.name} />
            <span>{p.name}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
// DEAD CODE END
