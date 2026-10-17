package gest_cabinet_back;

import gest_cabinet_back.model.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication
public class GestCabinetBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestCabinetBackApplication.class, args);

		System.out.println("Application démarrée");

	}

}
