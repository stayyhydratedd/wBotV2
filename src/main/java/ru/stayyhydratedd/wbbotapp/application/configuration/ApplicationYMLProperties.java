package ru.stayyhydratedd.wbbotapp.application.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google")
@Data
public class ApplicationYMLProperties {
    private int rateOfPay;

    private String mainDirectoryId;
    private String rateOfPayFileId;
    private String salaryDirectoryId;
    private String schedulesDirectoryId;

    private int delay;
}
