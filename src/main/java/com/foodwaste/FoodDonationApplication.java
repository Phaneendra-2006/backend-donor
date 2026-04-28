package com.foodwaste;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FoodDonationApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FoodDonationApplication.class, args);
        System.out.println("🍱 Food Donation System Started Successfully!");
        System.out.println("📄 Swagger UI: http://localhost:8080/swagger-ui.html");
    }
}
	