package ru.job4j.dreamjob.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import ru.job4j.dreamjob.model.File;

import java.sql.SQLException;
import java.util.Optional;

@Repository
@Primary
public class Sql2oFileRepository implements FileRepository {

    private static final Logger LOG = LoggerFactory.getLogger(Sql2oFileRepository.class);
    private final Sql2o sql2o;

    public Sql2oFileRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public File save(File file) {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("INSERT INTO files (name, path) VALUES (:name, :path)", true)
                    .addParameter("name", file.getName())
                    .addParameter("path", file.getPath());
            int generatedId = query.executeUpdate().getKey(Integer.class);
            file.setId(generatedId);
            return file;
        } catch (Sql2oException e) {
            if (e.getCause() instanceof SQLException) {
                SQLException sqlEx = (SQLException) e.getCause();
                if ("23505".equals(sqlEx.getSQLState())) {
                    LOG.error("Такой файл уже существует: name={}, path={}", file.getName(), file.getPath(), e);
                }
            }
            return null;
        }
    }

    @Override
    public Optional<File> findById(int id) {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("SELECT * FROM files WHERE id = :id");
            var file = query.addParameter("id", id).executeAndFetchFirst(File.class);
            return Optional.ofNullable(file);
        } catch (Exception e) {
            LOG.error("Не удалось найти файл по id={}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public boolean deleteById(int id) {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("DELETE FROM files WHERE id = :id");
            return query.addParameter("id", id).executeUpdate().getResult() > 0;
        } catch (Exception e) {
            LOG.error("Не удалось удалить файл по id={}", id, e);
        }
        return false;
    }
}