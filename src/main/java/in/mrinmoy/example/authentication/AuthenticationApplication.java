package in.mrinmoy.example.authentication;

import in.mrinmoy.example.authentication.service.HealthCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
@Slf4j
public class AuthenticationApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AuthenticationApplication.class, args);
        try {
            HealthCheckService healthCheckService = context.getBean(HealthCheckService.class);
            healthCheckService.tokenCleanup();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public Docket allApi() {
//        return new Docket(DocumentationType.SWAGGER_2).select()
//                .apis(RequestHandlerSelectors.basePackage("in.mrinmoy.example.authentication.controller")).build();
//    }
}
