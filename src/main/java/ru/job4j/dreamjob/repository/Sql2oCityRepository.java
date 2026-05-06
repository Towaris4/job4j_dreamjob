package ru.job4j.dreamjob.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.sql2o.Sql2o;
import ru.job4j.dreamjob.model.City;

import java.util.ArrayList;
import java.util.Collection;

@Repository
public class Sql2oCityRepository implements CityRepository {

    private static final Logger LOG = LoggerFactory.getLogger(Sql2oCityRepository.class);
    private final Sql2o sql2o;

    public Sql2oCityRepository(Sql2o sql2o) {
        this.sql2o = sql2o;
    }

    @Override
    public Collection<City> findAll() {
        try (var connection = sql2o.open()) {
            var query = connection.createQuery("SELECT * FROM cities");
            return query.executeAndFetch(City.class);
        } catch (Exception e) {
            LOG.error("Не удалось получить список городов", e);
        }
        return new ArrayList<>();
    }
}