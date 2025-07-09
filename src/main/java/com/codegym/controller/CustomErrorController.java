package com.codegym.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            // Nếu lỗi là 404 (Not Found)
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/404";
            }
            // Nếu lỗi là 500 (Internal Server Error)
            else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                // Bạn có thể thêm các thông tin lỗi vào model để hiển thị nếu muốn
                model.addAttribute("message", request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
                return "error/error";
            }
        }

        // Trả về trang lỗi chung cho các trường hợp khác
        return "error/error";
    }
}