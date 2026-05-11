package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class IndexControllerTest {

    private IndexController indexController;

    @BeforeEach
    public void init() {
        indexController = new IndexController();
    }

    @Test
    public void whenGetIndexThenReturnIndexPage() {
        var view = indexController.getIndex();
        assertThat(view).isEqualTo("index");
    }
}