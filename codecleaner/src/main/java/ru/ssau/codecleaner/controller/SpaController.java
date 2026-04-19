package ru.ssau.codecleaner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для поддержки Angular роутинга
 * Перенаправляет все неизвестные пути на index.html
 */
@Controller
public class SpaController {
    
    /**
     * Перехватывает все GET запросы, которые:
     * - НЕ начинаются с /api/ (это REST API)
     * - НЕ содержат точку (это статические файлы: .js, .css, .html)
     * 
     * Примеры:
     * /projects → forward:/index.html (Angular обработает)
     * /project/6 → forward:/index.html (Angular обработает)
     * /admin → forward:/index.html (Angular обработает)
     * /api/projects → НЕ перехватывается (идёт в ProjectController)
     * /main.js → НЕ перехватывается (статический файл)
     */
    @GetMapping(value = {"/{path:[^\\.]*}", "/{path:[^\\.]*}/**"})
    public String redirect() {
        return "forward:/index.html";
    }
}
