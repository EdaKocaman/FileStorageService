package com.example.FileStorageSystem;

import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.example.FileStorageService.controller.FileUploadController;
import com.example.FileStorageService.exception.StorageFileNotFoundException;
import com.example.FileStorageService.Interface.FileUploadServiceInterface;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(FileUploadController.class)
@AutoConfigureMockMvc
public class FileUploadTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FileUploadServiceInterface fileService;

    @Test
    public void shouldListAllFiles() throws Exception {
        given(this.fileService.loadAll())
                .willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));

        this.mvc.perform(get("/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value("first.txt"))
                .andExpect(jsonPath("$[1]").value("second.txt"));
    }
    /*@Test
    public void shouldListAllFiles() throws Exception {
        given(this.fileService.loadAll())
                .willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));

        this.mvc.perform(get("/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]").value("first.txt"))
                .andExpect(jsonPath("$[1]").value("second.txt"));
    }*/

    @Test
    public void shouldSaveUploadedFile() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "Spring Framework".getBytes());

        this.mvc.perform(multipart("/upload")
                        .file(multipartFile)
                        .param("directoryId", "1"))
                .andExpect(status().isOk());

        then(this.fileService).should().store(multipartFile, 1L, "param1", "param2");
    }

    @Test
    public void should404WhenMissingFile() throws Exception {
        given(this.fileService.loadAsResource("test.txt"))
                .willThrow(new StorageFileNotFoundException("File not found"));

        this.mvc.perform(get("/files/test.txt"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("File not found")));
    }


    /*@Test
    public void shouldSaveUploadedFile() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        this.mvc.perform(multipart("/").file(multipartFile).param("directoryId", "1"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/"));

        then(this.fileService).should().store(multipartFile, 1L);
    }*/

    /*@Test
    public void should404WhenMissingFile() throws Exception {
        given(this.fileService.loadAsResource("test.txt"))
                .willThrow(new StorageFileNotFoundException("File not found"));

        this.mvc.perform(get("/files/test.txt"))
                .andExpect(status().isNotFound());
    }*/
}