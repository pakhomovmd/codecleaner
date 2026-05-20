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

// ========== МЁРТВЫЙ КОД (60-70%) ==========

// Неиспользуемая функция для старой версии скидок
function oldCalculateDiscount(price) {
    return price * 0.9;
}

// Неиспользуемая функция для новых скидок
function calculateNewDiscount(price, percentage) {
    return price * (1 - percentage / 100);
}

// Неиспользуемая функция для валидации телефона
function validatePhoneNumber(phone) {
    const regex = /^\+7\d{10}$/;
    return regex.test(phone);
}

// Неиспользуемая функция для валидации email
function validateEmail(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

// Неиспользуемая функция для работы с корзиной
function addToCart(itemId, quantity) {
    const cart = JSON.parse(localStorage.getItem('cart')) || [];
    cart.push({ id: itemId, qty: quantity, timestamp: Date.now() });
    localStorage.setItem('cart', JSON.stringify(cart));
}

// Неиспользуемая функция для получения корзины
function getCartItems() {
    return JSON.parse(localStorage.getItem('cart')) || [];
}

// Неиспользуемая функция для удаления из корзины
function removeFromCart(itemId) {
    let cart = getCartItems();
    cart = cart.filter(item => item.id !== itemId);
    localStorage.setItem('cart', JSON.stringify(cart));
}

// Неиспользуемая функция для очистки корзины
function clearCart() {
    localStorage.removeItem('cart');
}

// Неиспользуемая функция для подсчета суммы корзины
function calculateCartTotal() {
    const cart = getCartItems();
    return cart.reduce((total, item) => total + (item.price * item.qty), 0);
}

// Неиспользуемая функция для старой аналитики
function trackPageView(pageName) {
    console.log('Page viewed:', pageName);
    sendAnalytics('pageview', { page: pageName });
}

// Неиспользуемая функция для отправки аналитики
function sendAnalytics(eventType, data) {
    console.log('Analytics:', eventType, data);
}

// Неиспользуемая функция для форматирования даты
function formatDate(date) {
    return new Date(date).toLocaleDateString('ru-RU');
}

// Неиспользуемая функция для форматирования времени
function formatTime(date) {
    return new Date(date).toLocaleTimeString('ru-RU');
}

// Неиспользуемая функция для работы с cookies
function setCookie(name, value, days) {
    const expires = new Date();
    expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
    document.cookie = name + '=' + value + ';expires=' + expires.toUTCString() + ';path=/';
}

// Неиспользуемая функция для получения cookie
function getCookie(name) {
    const nameEQ = name + '=';
    const ca = document.cookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

// Неиспользуемая функция для удаления cookie
function deleteCookie(name) {
    setCookie(name, '', -1);
}

// Неиспользуемая функция для модального окна
function showModal(title, content) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h2>${title}</h2>
                <span class="modal-close">&times;</span>
            </div>
            <div class="modal-body">${content}</div>
        </div>
    `;
    document.body.appendChild(modal);
}

// Неиспользуемая функция для закрытия модального окна
function closeModal() {
    const modal = document.querySelector('.modal-overlay');
    if (modal) {
        modal.remove();
    }
}

// Неиспользуемая функция для уведомлений
function showNotification(message, type) {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    document.body.appendChild(notification);
    setTimeout(() => notification.remove(), 3000);
}

// Неиспользуемая функция для дебаунса
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Неиспользуемая функция для троттлинга
function throttle(func, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// Неиспользуемая функция для генерации случайного ID
function generateId() {
    return Math.random().toString(36).substr(2, 9);
}

// Неиспользуемая функция для сортировки массива
function sortArray(arr, key, order = 'asc') {
    return arr.sort((a, b) => {
        if (order === 'asc') {
            return a[key] > b[key] ? 1 : -1;
        } else {
            return a[key] < b[key] ? 1 : -1;
        }
    });
}

// Неиспользуемая функция для фильтрации массива
function filterArray(arr, predicate) {
    return arr.filter(predicate);
}

// Неиспользуемая функция для группировки массива
function groupBy(arr, key) {
    return arr.reduce((result, item) => {
        (result[item[key]] = result[item[key]] || []).push(item);
        return result;
    }, {});
}

// Неиспользуемые переменные
const DEPRECATED_API_URL = 'https://old-api.example.com';
const NEW_API_URL = 'https://api.example.com';
const OLD_MAX_ITEMS = 50;
const NEW_MAX_ITEMS = 100;
const UNUSED_TIMEOUT = 5000;
const RETRY_ATTEMPTS = 3;
const CACHE_DURATION = 3600000;

let oldCartCounter = 0;
let sessionId = null;
let userPreferences = {};
let cachedData = {};

// Неиспользуемый класс для работы с API
class OldApiClient {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    async get(endpoint) {
        const response = await fetch(this.baseUrl + endpoint);
        return response.json();
    }
    
    async post(endpoint, data) {
        const response = await fetch(this.baseUrl + endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        return response.json();
    }
}

// Неиспользуемый класс для управления состоянием
class StateManager {
    constructor() {
        this.state = {};
        this.listeners = [];
    }
    
    setState(key, value) {
        this.state[key] = value;
        this.notify();
    }
    
    getState(key) {
        return this.state[key];
    }
    
    subscribe(listener) {
        this.listeners.push(listener);
    }
    
    notify() {
        this.listeners.forEach(listener => listener(this.state));
    }
}
