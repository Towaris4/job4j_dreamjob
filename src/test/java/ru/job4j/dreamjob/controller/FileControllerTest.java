package ru.job4j.dreamjob.controller;

import ru.job4j.dreamjob.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.job4j.dreamjob.dto.FileDto;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpStatus;


public class FileControllerTest {
    private FileService fileService;
    private FileController fileController;

    @BeforeEach
    public void initServices() {
        fileService = mock(FileService.class);
        fileController = new FileController(fileService);
    }

    @Test
    public void whenFileExistsThenReturnOkWithContent() {
        byte[] expectedContent = {1, 2, 3};
        var fileDto = new FileDto("test.txt", expectedContent);
        when(fileService.getFileById(1)).thenReturn(java.util.Optional.of(fileDto));

        var response = fileController.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedContent);
    }

    @Test
    public void whenFileNotFoundThenReturnNotFound() {
        when(fileService.getFileById(99)).thenReturn(java.util.Optional.empty());

        var response = fileController.getById(99);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
