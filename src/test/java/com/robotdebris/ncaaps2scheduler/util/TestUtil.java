package com.robotdebris.ncaaps2scheduler.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.mock.web.MockMultipartFile;

public class TestUtil {

    public static MockMultipartFile createMockMultipartFile(String fileName) throws IOException, URISyntaxException {
        Path path = Paths.get(TestUtil.class.getClassLoader().getResource(fileName).toURI());
        byte[] content = Files.readAllBytes(path);
        return new MockMultipartFile("file", fileName, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content);
    }
}