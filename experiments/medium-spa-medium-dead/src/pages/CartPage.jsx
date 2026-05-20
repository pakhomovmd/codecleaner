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
