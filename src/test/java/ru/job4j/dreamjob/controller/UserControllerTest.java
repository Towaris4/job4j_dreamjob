package ru.job4j.dreamjob.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    public void whenRequestRegistrationPageThenGetRegisterPage() {
        var model = new ConcurrentModel();
        var session = mock(HttpSession.class);
        var view = userController.getCreationPage(model, session);

        assertThat(view).isEqualTo("users/register");
    }

    @Test
    public void whenRegisterNewUserThenRedirectToVacancies() {
        var user = new User(1, "test@mail.ru", "Name", "pass");
        when(userService.save(user)).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.register(model, user);

        assertThat(view).isEqualTo("redirect:/vacancies");
        verify(userService).save(user);
    }

    @Test
    public void whenRegisterExistingUserThenGetErrorPage() {
        var user = new User(1, "test@mail.ru", "Name", "pass");
        when(userService.save(user)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.register(model, user);
        var message = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(message).isEqualTo("Пользователь с такой почтой уже существует");
    }

    @Test
    public void whenRequestLoginPageThenGetLoginPage() {
        var view = userController.getLoginPage();
        assertThat(view).isEqualTo("users/login");
    }

    @Test
    public void whenLoginValidCredentialsThenRedirectAndSetSession() {
        var user = new User(1, "test@mail.ru", "Name", "pass");
        when(userService.findByEmailAndPassword("test@mail.ru", "pass")).thenReturn(Optional.of(user));

        var request = new MockHttpServletRequest();
        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, request);
        var session = request.getSession(false);

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(session).isNotNull();
        assertThat(session.getAttribute("user")).isEqualTo(user);
    }

    @Test
    public void whenLoginInvalidCredentialsThenReturnLoginPageWithError() {
        var user = new User(1, "wrong@mail.ru", "Name", "wrong");
        when(userService.findByEmailAndPassword("wrong@mail.ru", "wrong")).thenReturn(Optional.empty());

        var request = new MockHttpServletRequest();
        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, request);
        var error = model.getAttribute("error");

        assertThat(view).isEqualTo("users/login");
        assertThat(error).isEqualTo("Почта или пароль введены неверно");
    }

    @Test
    public void whenLogoutThenInvalidateSessionAndRedirectToLogin() {
        var session = mock(HttpSession.class);
        var view = userController.logout(session);

        assertThat(view).isEqualTo("redirect:/users/login");
        verify(session).invalidate();
    }
}