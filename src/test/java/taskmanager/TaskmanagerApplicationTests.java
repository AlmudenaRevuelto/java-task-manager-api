package taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import taskmanager.TaskmanagerApplication;

@SpringBootTest(classes = TaskmanagerApplication.class)
@ActiveProfiles("test")
class TaskmanagerApplicationTests {

	@Test
	void contextLoads() {
	}

}
