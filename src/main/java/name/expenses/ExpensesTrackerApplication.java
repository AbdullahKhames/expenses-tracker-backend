package name.expenses;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ExpensesTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpensesTrackerApplication.class, args);
    }
}
