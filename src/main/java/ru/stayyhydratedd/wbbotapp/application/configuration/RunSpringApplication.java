package ru.stayyhydratedd.wbbotapp.application.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import ru.stayyhydratedd.wbbotapp.application.*;
import ru.stayyhydratedd.wbbotapp.application.services.Credentials;
import ru.stayyhydratedd.wbbotapp.application.services.GoogleServices;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationYMLProperties.class)
@PropertySource("classpath:application.yml")
public class RunSpringApplication {
    private static final AnnotationConfigApplicationContext annContext =
            new AnnotationConfigApplicationContext(AnnotationConfiguration.class);

    public static void main(String[] args) {

        if(System.getProperty("os.name").startsWith("Windows") && System.getProperty("os.version").equals("10.0")) {
            TextColour.enableWindows10AnsiSupport();
        }

        annContext.start();

        ConfigurableApplicationContext context =
                SpringApplication.run(RunSpringApplication.class, args);


        TextColour.printLogo();

        UserInput userInput = context.getBean(UserInput.class);
        userInput.greet(true);
    }
    @Bean
    public SomeAlgorithms algorithms() {
        return new SomeAlgorithms();
    }
    @Bean
    public Credentials credentials() {
        return new Credentials();
    }
    @Bean
    public GoogleServices googleServices() {
        return new GoogleServices(credentials(), algorithms());
    }
    @Bean
    public RateOfPaySpreadsheetFormatter formatter() {
        return new RateOfPaySpreadsheetFormatter(algorithms(), googleServices());
    }
    @Bean
    public DateCompute date(){
        return new DateCompute();
    }
    @Bean
    public HelpInfo helpInfo(){
        return new HelpInfo();
    }
    @Bean
    public FileCreator fileCreator() {
        return new FileCreator(googleServices());
    }
    @Bean
    public InitialSetup setup() {
        return new InitialSetup(algorithms(), fileCreator(), formatter());
    }
    @Bean
    public ReportFileConstructor fileConstructor() {
        return new ReportFileConstructor(googleServices(), setup(), fileCreator());
    }
    @Bean
    public SheetsFormatter sheetsFormatter() {
        return new SheetsFormatter(googleServices(), date(), algorithms(), setup(), fileCreator(), formatter());
    }
    @Bean
    public SalaryCalculator salaryCalculator() {
        return new SalaryCalculator(setup(), googleServices(), date(),
                algorithms(), fileConstructor(), formatter());
    }
    @Bean
    public UserInput input() {
        return new UserInput(helpInfo(), date(), algorithms(), sheetsFormatter(),
                googleServices(), salaryCalculator(), fileCreator(), setup());
    }
}