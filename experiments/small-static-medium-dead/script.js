// ========== ИСПОЛЬЗУЕМЫЙ КОД ==========

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    initMobileMenu();
    initOrderButton();
    initContactForm();
    initSmoothScroll();
});

// Мобильное меню
function initMobileMenu() {
    const menuToggle = document.getElementById('menuToggle');
    const navMenu = document.querySelector('.nav-menu');
    
    if (menuToggle) {
        menuToggle.addEventListener('click', function() {
            navMenu.style.display = navMenu.style.display === 'flex' ? 'none' : 'flex';
        });
    }
}

// Кнопка заказа
function initOrderButton() {
    const orderBtn = document.getElementById('orderBtn');
    
    if (orderBtn) {
        orderBtn.addEventListener('click', function() {
            alert('Спасибо за интерес! Функция заказа скоро будет доступна.');
        });
    }
}

// Форма контактов
function initContactForm() {
    const contactForm = document.getElementById('contactForm');
    
    if (contactForm) {
        contactForm.addEventListener('submit', function(e) {
            e.preventDefault();
            alert('Спасибо за ваше сообщение! Мы свяжемся с вами в ближайшее время.');
            contactForm.reset();
        });
    }
}

// Плавная прокрутка
function initSmoothScroll() {
    const links = document.querySelectorAll('.nav-link');
    
    links.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            
            if (targetSection) {
                targetSection.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });
}

// ========== МЁРТВЫЙ КОД (30-40%) ==========

// Неиспользуемая функция для старой версии скидок
function oldCalculateDiscount(price) {
    return price * 0.9;
}

// Неиспользуемая функция для валидации
function validatePhoneNumber(phone) {
    const regex = /^\+7\d{10}$/;
    return regex.test(phone);
}

// Неиспользуемая функция для работы с корзиной
function addToCart(itemId, quantity) {
    const cart = JSON.parse(localStorage.getItem('cart')) || [];
    cart.push({ id: itemId, qty: quantity });
    localStorage.setItem('cart', JSON.stringify(cart));
}

// Неиспользуемая функция для получения корзины
function getCartItems() {
    return JSON.parse(localStorage.getItem('cart')) || [];
}

// Неиспользуемая функция для очистки корзины
function clearCart() {
    localStorage.removeItem('cart');
}

// Неиспользуемая функция для старой аналитики
function trackPageView(pageName) {
    console.log('Page viewed:', pageName);
}

// Неиспользуемая функция для форматирования даты
function formatDate(date) {
    return new Date(date).toLocaleDateString('ru-RU');
}

// Неиспользуемые переменные
const DEPRECATED_API_URL = 'https://old-api.example.com';
const OLD_MAX_ITEMS = 50;
const UNUSED_TIMEOUT = 5000;
let oldCartCounter = 0;
