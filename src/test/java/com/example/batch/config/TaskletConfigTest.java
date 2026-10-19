package com.example.batch.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;

import com.example.batch.service.FileValidationTasklet;

public class TaskletConfigTest {

    private final TaskletConfig config = new TaskletConfig();

    @ParameterizedTest
    @ValueSource(strings = { "file1", "file2" })
    void createsTasklet(String which) {
        DataSize size = DataSize.ofMegabytes(5);
        String path = which + ".csv";

        FileValidationTasklet tasklet = "file1".equals(which)
                ? config.file1ValidationTasklet(path, size)
                : config.file2ValidationTasklet(path, size);

        assertThat(ReflectionTestUtils.getField(tasklet, "filePath")).isEqualTo(path);
        assertThat(ReflectionTestUtils.getField(tasklet, "maxFileSize")).isEqualTo(size);
    }
}