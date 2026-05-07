package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpSession;
import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.job4j.dreamjob.model.User;
import org.springframework.ui.Model;

@Controller
@ThreadSafe
public class IndexController {

    @GetMapping({"/", "/index"})
    public String getIndex() {
        return "index";
    }
}
