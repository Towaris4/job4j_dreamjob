package ru.job4j.dreamjob.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;
import ru.job4j.dreamjob.model.Candidate;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Repository
public class Sql2oCandidateRepository implements CandidateRepository {

    private static final Logger LOG = LoggerFactory.getLogger(Sql2oCandidateRepository.class);
    private final Sql2o sql2o;

    public Sql2oCandidateRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Candidate save(Candidate candidate) {
        try (var connection = sql2o.open()) {
            var sql = """
                      INSERT INTO candidates(name, description, creation_date, city_id, file_id)
                      VALUES (:name, :description, :creationDate, :cityId, :fileId)
                      """;
            var query = connection.createQuery(sql, true)
                    .addParameter("name", candidate.getName())
                    .addParameter("description", candidate.getDescription())
                    .addParameter("creationDate", candidate.getCreationDate())
                    .addParameter("cityId", candidate.getCityId())
                    .addParameter("fileId", candidate.getFileId());
            int generatedId = query.executeUpdate().getKey(Integer.class);
            candidate.setId(generatedId);
            return candidate;
        } catch (Exception e) {
            LOG.error("Ошибка при сохранении кандидата: name={}", candidate.getName(), e);
            return null;
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("DELETE FROM candidates WHERE id = :id");
            query.addParameter("id", id);
            return query.executeUpdate().getResult() > 0;
        } catch (Exception e) {
            LOG.error("Ошибка при удалении кандидата по id={}", id, e);
            return false;
        }
    }

    @Override
    public boolean update(Candidate candidate) {
        try (var connection = sql2o.open()) {
            var sql = """
                    UPDATE candidates
                    SET name = :name, description = :description, creation_date = :creationDate,
                        city_id = :cityId, file_id = :fileId
                    WHERE id = :id
                    """;
            var query = connection.createQuery(sql)
                    .addParameter("name", candidate.getName())
                    .addParameter("description", candidate.getDescription())
                    .addParameter("creationDate", candidate.getCreationDate())
                    .addParameter("cityId", candidate.getCityId())
                    .addParameter("fileId", candidate.getFileId())
                    .addParameter("id", candidate.getId());
            var affectedRows = query.executeUpdate().getResult();
            return affectedRows > 0;
        } catch (Exception e) {
            LOG.error("Ошибка при обновлении кандидата id={}", candidate.getId(), e);
            return false;
        }
    }

    @Override
    public Optional<Candidate> findById(int id) {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("SELECT * FROM candidates WHERE id = :id");
            query.addParameter("id", id);
            var candidate = query.setColumnMappings(Candidate.COLUMN_MAPPING).executeAndFetchFirst(Candidate.class);
            return Optional.ofNullable(candidate);
        } catch (Exception e) {
            LOG.error("Ошибка при поиске кандидата по id={}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Candidate> findAll() {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("SELECT * FROM candidates");
            return query.setColumnMappings(Candidate.COLUMN_MAPPING).executeAndFetch(Candidate.class);
        } catch (Exception e) {
            LOG.error("Ошибка при получении списка всех кандидатов", e);
        }
        return new ArrayList<>();
    }
}