package ru.job4j.dreamjob.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.configuration.DatasourceConfiguration;
import ru.job4j.dreamjob.model.User;

import java.util.Properties;

import static java.util.Optional.empty;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Sql2oUserRepositoryTest {

    private static Sql2oUserRepository sql2oUserRepository;
    private static Sql2o sql2o;

    @BeforeAll
    public static void initRepositories() throws Exception {
        var properties = new Properties();
        try (var inputStream = Sql2oUserRepositoryTest.class.getClassLoader()
                .getResourceAsStream("connection.properties")) {
            properties.load(inputStream);
        }
        var url = properties.getProperty("datasource.url");
        var username = properties.getProperty("datasource.username");
        var password = properties.getProperty("datasource.password");

        var configuration = new DatasourceConfiguration();
        var datasource = configuration.connectionPool(url, username, password);
        sql2o = configuration.databaseClient(datasource);

        sql2oUserRepository = new Sql2oUserRepository(sql2o);
    }

    @AfterEach
    public void clearUsers() {
        try (var connection = sql2o.open()) {
            connection.createQuery("TRUNCATE TABLE users RESTART IDENTITY").executeUpdate();
        }
    }

    @Test
    public void whenSaveThenGetSame() {
        var userToSave = new User(0, "test@mail.ru", "Иван Иванов", "password123");
        var savedUser = sql2oUserRepository.save(userToSave);

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getId()).isGreaterThan(0);
        assertThat(savedUser.get().getEmail()).isEqualTo("test@mail.ru");
        assertThat(savedUser.get().getName()).isEqualTo("Иван Иванов");

        var found = sql2oUserRepository.findByEmailAndPassword("test@mail.ru", "password123");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(savedUser.get().getId());
    }

    @Test
    public void whenSaveSeveralThenGetAllByEmailAndPassword() {
        var user1 = sql2oUserRepository.save(
                new User(0, "user1@mail.ru", "Пользователь 1", "pass1")
        );
        var user2 = sql2oUserRepository.save(
                new User(0, "user2@mail.ru", "Пользователь 2", "pass2")
        );
        var user3 = sql2oUserRepository.save(
                new User(0, "user3@mail.ru", "Пользователь 3", "pass3")
        );

        var found1 = sql2oUserRepository.findByEmailAndPassword("user1@mail.ru", "pass1");
        var found2 = sql2oUserRepository.findByEmailAndPassword("user2@mail.ru", "pass2");
        var found3 = sql2oUserRepository.findByEmailAndPassword("user3@mail.ru", "pass3");

        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(found3).isPresent();
        assertThat(found1.get().getId()).isEqualTo(user1.get().getId());
        assertThat(found2.get().getId()).isEqualTo(user2.get().getId());
        assertThat(found3.get().getId()).isEqualTo(user3.get().getId());
    }

    @Test
    public void whenDontSaveThenNothingFound() {
        var found = sql2oUserRepository.findByEmailAndPassword("nonexistent@mail.ru", "any");
        assertThat(found).isEqualTo(empty());
    }

    @Test
    public void whenSaveUserWithSameEmailThenReturnEmpty() {
        var user1 = new User(0, "duplicate@mail.ru", "Первый", "pass1");
        var user2 = new User(0, "duplicate@mail.ru", "Второй", "pass2");

        var savedUser1 = sql2oUserRepository.save(user1);
        var savedUser2 = sql2oUserRepository.save(user2);

        assertThat(savedUser1).isPresent();
        assertThat(savedUser1.get().getPassword()).isEqualTo("pass1");
        assertThat(savedUser2).isEmpty(); // Второй не должен сохраниться

        var found = sql2oUserRepository.findByEmailAndPassword("duplicate@mail.ru", "pass1");
        assertThat(found).isPresent();
        assertThat(found.get().getPassword()).isEqualTo("pass1");

        var foundWrong = sql2oUserRepository.findByEmailAndPassword("duplicate@mail.ru", "pass2");
        assertThat(foundWrong).isEmpty();
    }

    @Test
    public void whenFindByEmailWithWrongPasswordThenEmpty() {
        var user = new User(0, "test@mail.ru", "Иван", "correctPass");
        sql2oUserRepository.save(user);

        var found = sql2oUserRepository.findByEmailAndPassword("test@mail.ru", "wrongPass");
        assertThat(found).isEmpty();
    }

    @Test
    public void whenFindByEmailWithWrongEmailThenEmpty() {
        var user = new User(0, "real@mail.ru", "Иван", "password123");
        sql2oUserRepository.save(user);

        var found = sql2oUserRepository.findByEmailAndPassword("fake@mail.ru", "password123");
        assertThat(found).isEmpty();
    }
}