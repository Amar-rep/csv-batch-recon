package com.example.batch.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class BatchTaskExecutorConfigTest {

	@Test
	void executorIsConfigured() {
		ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) new BatchTaskExecutorConfig().batchTaskExecutor();
		try {
			assertThat(executor.getCorePoolSize()).isEqualTo(8);
			assertThat(executor.getMaxPoolSize()).isEqualTo(8);
			assertThat(executor.getThreadPoolExecutor().getQueue().remainingCapacity()).isEqualTo(1000);
		} finally {
			executor.shutdown();
		}
	}
}