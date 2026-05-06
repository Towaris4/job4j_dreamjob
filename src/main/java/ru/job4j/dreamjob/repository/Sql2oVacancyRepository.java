package ru.job4j.dreamjob.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.model.Vacancy;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Repository
public class Sql2oVacancyRepository implements VacancyRepository {

    private static final Logger LOG = LoggerFactory.getLogger(Sql2oVacancyRepository.class);
    private final Sql2o sql2o;

    public Sql2oVacancyRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Vacancy save(Vacancy vacancy) {
        try (var connection = sql2o.open()) {
            var sql = """
                      INSERT INTO vacancies(title, description, creation_date, visible, city_id, file_id)
                      VALUES (:title, :description, :creationDate, :visible, :cityId, :fileId)
                      """;
            var query = connection.createQuery(sql, true)
                    .addParameter("title", vacancy.getTitle())
                    .addParameter("description", vacancy.getDescription())
                    .addParameter("creationDate", vacancy.getCreationDate())
                    .addParameter("visible", vacancy.getVisible())
                    .addParameter("cityId", vacancy.getCityId())
                    .addParameter("fileId", vacancy.getFileId());
            int generatedId = query.executeUpdate().getKey(Integer.class);
            vacancy.setId(generatedId);
            return vacancy;
        } catch (Exception e) {
            LOG.error("Ошибка при сохранении вакансии: {}", vacancy.getTitle(), e);
            return null;
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("DELETE FROM vacancies WHERE id = :id");
            query.addParameter("id", id);
            return query.executeUpdate().getResult() > 0;
        } catch (Exception e) {
            // Исправлен синтаксис логирования: добавлен {} для параметра
            LOG.error("Ошибка при удалении вакансии с id: {}", id, e);
            return false;
        }
    }

    @Override
    public boolean update(Vacancy vacancy) {
        try (var connection = sql2o.open()) {
            var sql = """
                    UPDATE vacancies
                    SET title = :title, description = :description, creation_date = :creationDate,
                        visible = :visible, city_id = :cityId, file_id = :fileId
                    WHERE id = :id
                    """;
            var query = connection.createQuery(sql)
                    .addParameter("title", vacancy.getTitle())
                    .addParameter("description", vacancy.getDescription())
                    .addParameter("creationDate", vacancy.getCreationDate())
                    .addParameter("visible", vacancy.getVisible())
                    .addParameter("cityId", vacancy.getCityId())
                    .addParameter("fileId", vacancy.getFileId())
                    .addParameter("id", vacancy.getId());
            var affectedRows = query.executeUpdate().getResult();
            return affectedRows > 0;
        } catch (Exception e) {
            LOG.error("Ошибка при обновлении вакансии с id: {}", vacancy.getId(), e);
            return false;
        }
    }

    @Override
    public Optional<Vacancy> findById(int id) {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("SELECT * FROM vacancies WHERE id = :id");
            query.addParameter("id", id);
            var vacancy = query.setColumnMappings(Vacancy.COLUMN_MAPPING).executeAndFetchFirst(Vacancy.class);
            return Optional.ofNullable(vacancy);
        } catch (Exception e) {
            LOG.error("Ошибка при поиске вакансии по id: {}", id, e);
            return Optional.empty();
        }
    }

    @Override
    public Collection<Vacancy> findAll() {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("SELECT * FROM vacancies");
            return query.setColumnMappings(Vacancy.COLUMN_MAPPING).executeAndFetch(Vacancy.class);
        } catch (Exception e) {
            LOG.error("Ошибка при получении списка всех вакансий", e);
            return Collections.emptyList();
        }
    }
}