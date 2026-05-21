package br.com.desafio.leads;

import br.com.desafio.leads.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class LeadsApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LeadsApiApplication.class, args);
    }
}
