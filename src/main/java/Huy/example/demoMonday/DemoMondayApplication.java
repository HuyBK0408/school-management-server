package huy.example.demoMonday;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "huy.example.demoMonday")
@EnableJpaRepositories(basePackages = "huy.example.demoMonday.repo")
@EntityScan(basePackages = "huy.example.demoMonday.entity")
public class DemoMondayApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoMondayApplication.class, args);
	}

}
