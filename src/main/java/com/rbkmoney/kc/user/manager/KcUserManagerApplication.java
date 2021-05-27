package com.rbkmoney.kc.user.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class KcUserManagerApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(KcUserManagerApplication.class, args);
    }

}
