package ru.stayyhydratedd.wbbotapp.application.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.stayyhydratedd.wbbotapp.application.SomeAlgorithms;
import ru.stayyhydratedd.wbbotapp.application.services.Credentials;
import ru.stayyhydratedd.wbbotapp.application.services.GoogleServices;

                    /*НУЖЕН ТОЛЬКО ДЛЯ ТОГО, ЧТОБЫ ОТКРЫЛСЯ БРАУЗЕР*/
@Configuration
public class AnnotationConfiguration {
    @Bean
    public Credentials credentials() {
        return new Credentials();
    }
    @Bean
    public SomeAlgorithms algorithms() {
        return new SomeAlgorithms();
    }
    @Bean
    public GoogleServices googleServices() {
        return new GoogleServices(credentials(), algorithms());
    }
}
