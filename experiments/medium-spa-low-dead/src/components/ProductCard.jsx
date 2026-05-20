import { Link } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import './ProductCard.css'

function ProductCard({ product }) {
  const { addToCart } = useCart()

  const handleAddToCart = (e) => {
    e.preventDefault()
    addToCart(product)
  }

  return (
    <Link to={`/product/${product.id}`} className="product-card">
      <img src={product.image} alt={product.name} className="product-image" />
      <div className="product-info">
        <h3 className="product-name">{product.name}</h3>
        <p className="product-price">{product.price.toLocaleString('ru-RU')} ₽</p>
        {!product.inStock && <span className="out-of-stock">Нет в наличии</span>}
        {product.inStock && (
          <button 
            className="add-to-cart-btn"
            onClick={handleAddToCart}
          >
            В корзину
          </button>
        )}
      </div>
    </Link>
  )
}

export default ProductCard
