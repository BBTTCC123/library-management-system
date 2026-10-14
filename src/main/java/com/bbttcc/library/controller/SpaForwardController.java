package com.bbttcc.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端单页应用（Vue Router history 模式）的路由转发。
 *
 * <p>前端打包产物位于 {@code classpath:/static/}。使用 history 模式时，
 * 直接访问或刷新 {@code /books} 这类前端路由会到达后端，这里把它们统一转发到
 * {@code index.html}，由前端路由接管；而 {@code /api/**} 等接口路径不受影响。</p>
 */
@Controller
public class SpaForwardController {

    private static final String FORWARD_INDEX = "forward:/index.html";

    @GetMapping({
            "/login",
            "/dashboard",
            "/books",
            "/categories",
            "/borrows",
            "/borrow-records",
            "/users"
    })
    public String forward() {
        return FORWARD_INDEX;
    }
}
