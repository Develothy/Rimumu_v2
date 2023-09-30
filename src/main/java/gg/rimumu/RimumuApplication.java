package gg.rimumu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RimumuApplication {

	public static void main(String[] args) {
		SpringApplication.run(RimumuApplication.class, args);
	}

}
