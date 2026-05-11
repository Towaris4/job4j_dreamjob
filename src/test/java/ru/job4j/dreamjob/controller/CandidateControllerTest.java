package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.util.List;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class CandidateControllerTest {
    private CandidateService candidateService;
    private CityService cityService;
    private CandidateController candidateController;
    private MultipartFile testFile;


    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[] {1, 2, 3});
    }

    @Test
    public void whenRequestCandidateListPageThenGetPageWithCandidates() {
        var c1 = new Candidate(1, "Ivan", "Java Dev", now(), 1, 1);
        var c2 = new Candidate(2, "Petr", "Spring Boot", now(), 1, 2);
        var expectedCandidates = List.of(c1, c2);
        when(candidateService.findAll()).thenReturn(expectedCandidates);

        var model = new ConcurrentModel();
        var view = candidateController.getAll(model);
        var actualCandidates = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualCandidates).isEqualTo(expectedCandidates);
    }

    @Test
    public void whenRequestCandidateCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = candidateController.getCreationPage(model);
        var actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate(1, "Ivan", "Java Dev", now(), 1, 1);
        var candidateCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.save(candidateCaptor.capture(), fileDtoCaptor.capture())).thenReturn(candidate);

        var model = new ConcurrentModel();
        var view = candidateController.create(candidate, testFile, model);
        var actualCandidate = candidateCaptor.getValue();
        var actualFileDto = fileDtoCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison()
                .isEqualTo(new FileDto(testFile.getOriginalFilename(), testFile.getBytes()));
    }

    @Test
    public void whenCreateCandidateThrowsExceptionThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to save candidate");
        when(candidateService.save(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.create(new Candidate(), testFile, model);
        var actualMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestCandidateByIdThenGetPageWithCandidateAndCities() {
        var candidate = new Candidate(1, "Ivan", "Java Dev", now(), 1, 1);
        var expectedCities = List.of(new City(1, "Москва"), new City(2, "СПб"));
        when(candidateService.findById(1)).thenReturn(java.util.Optional.of(candidate));
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, 1);

        assertThat(view).isEqualTo("candidates/one");
        assertThat(model.getAttribute("candidate")).isEqualTo(candidate);
        assertThat(model.getAttribute("cities")).isEqualTo(expectedCities);
    }

    @Test
    public void whenRequestCandidateByIdNotFoundThenGetErrorPage() {
        when(candidateService.findById(99)).thenReturn(java.util.Optional.empty());

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, 99);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo("Кандидат с указанным идентификатором не найден");
    }

    @Test
    public void whenPostCandidateUpdateWithFileThenRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate(1, "Ivan", "Java Dev", now(), 1, 1);
        var candidateCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateCaptor.capture(), fileDtoCaptor.capture())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(candidateCaptor.getValue()).isEqualTo(candidate);
        assertThat(fileDtoCaptor.getValue()).usingRecursiveComparison()
                .isEqualTo(new FileDto(testFile.getOriginalFilename(), testFile.getBytes()));
    }

    @Test
    public void whenPostCandidateUpdateNotFoundThenGetErrorPage() {
        var candidate = new Candidate(99, "test", "desc", now(), 1, 1);
        when(candidateService.update(any(), any())).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo("Резюме с указанным идентификатором не найдено");
    }

    @Test
    public void whenPostCandidateUpdateThrowsExceptionThenGetErrorPage() {
        var expectedException = new RuntimeException("DB connection lost");
        when(candidateService.update(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.update(new Candidate(), testFile, model);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestCandidateDeleteThenRedirectToCandidatesPage() {
        when(candidateService.deleteById(1)).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, 1);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    public void whenRequestCandidateDeleteNotFoundThenGetErrorPage() {
        when(candidateService.deleteById(99)).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, 99);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo("Кандидат с указанным идентификатором не найден");
    }
}
