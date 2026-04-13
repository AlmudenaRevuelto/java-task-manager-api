package taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import taskmanager.TaskmanagerApplication;
import taskmanager.config.TestSecurityConfig;

@SpringBootTest(classes = TaskmanagerApplication.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TaskmanagerApplicationTests {

	@Test
	void contextLoads() {
	}

}
