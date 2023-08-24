package guru.sfg.msscserviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class MsscServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsscServiceRegistryApplication.class, args);
	}

}
