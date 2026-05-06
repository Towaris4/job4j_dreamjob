package ru.job4j.dreamjob.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.model.User;

import java.util.Optional;

@Repository
public class Sql2oUserRepository implements UserRepository {

    private static final Logger LOG = LoggerFactory.getLogger(Sql2oUserRepository.class);
    private final Sql2o sql2o;

    public Sql2oUserRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Optional<User> save(User user) {
        try (var connection = sql2o.open()) {
            var sql = """
                INSERT INTO users (email, password)
                VALUES (:email, :password)
                """;
            var query = connection.createQuery(sql, true)
                    .addParameter("email", user.getEmail())
                    .addParameter("password", user.getPassword());

            int generatedId = query.executeUpdate().getKey(Integer.class);
            user.setId(generatedId);
            return Optional.of(user);
        } catch (Exception e) {
            LOG.error("Ошибка при сохранении пользователя с email: {}", user.getEmail(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmailAndPassword(String email, String password) {
        try (var connection = sql2o.open()) {
            var sql = """
                SELECT id, email, password
                FROM users
                WHERE email = :email AND password = :password
                """;
            var query = connection.createQuery(sql)
                    .addParameter("email", email)
                    .addParameter("password", password);

            User user = query.executeAndFetchFirst(User.class);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            LOG.error("Ошибка при поиске пользователя по email: {}", email, e);
            return Optional.empty();
        }
    }
}