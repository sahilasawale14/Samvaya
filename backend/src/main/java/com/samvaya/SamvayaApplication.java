package com.samvaya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SamvayaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SamvayaApplication.class, args);
        System.out.println("==================================================================");
        System.out.println("  SAMVAYA SOCIETY MANAGEMENT SYSTEM BACKEND STARTED SUCCESSFULLY  ");
        System.out.println("  REST API Base URL: http://localhost:8080/api                    ");
        System.out.println("  Database: MySQL Local (samvaya)                                 ");
        System.out.println("==================================================================");
    }
}
