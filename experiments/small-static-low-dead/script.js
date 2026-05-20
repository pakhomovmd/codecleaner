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

// ========== МЁРТВЫЙ КОД (10-15%) ==========

// Неиспользуемая функция для старой версии
function oldCalculateDiscount(price) {
    return price * 0.9;
}

// Неиспользуемая переменная
const DEPRECATED_API_URL = 'https://old-api.example.com';
