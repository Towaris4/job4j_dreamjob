package ru.job4j.dreamjob.controller;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.job4j.dreamjob.model.User;
import org.springframework.ui.Model;
import ru.job4j.dreamjob.service.UserService;

@ThreadSafe
@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/register")
    public String getCreationPage(Model model) {
        model.addAttribute("user", new User());
        return "users/register";
    }

    @PostMapping("/users/register")
    public String register(Model model, @ModelAttribute User user) {
        var savedUser = userService.save(user);
        if (savedUser.isEmpty()) {
            model.addAttribute("message", "Пользователь с такой почтой уже существует");
            return "errors/404";
        }
        return "redirect:/vacancies";
    }

    @GetMapping("/users/login")
    public String getLoginPage() {
        return "users/login";
    }

    @PostMapping("/users/login")
    public String loginUser(@ModelAttribute User user, Model model) {
        var userOptional = userService.findByEmailAndPassword(user.getEmail(), user.getPassword());
        if (userOptional.isEmpty()) {
            model.addAttribute("error", "Почта или пароль введены неверно");
            return "users/login";
        }
        return "redirect:/vacancies";
    }
}
