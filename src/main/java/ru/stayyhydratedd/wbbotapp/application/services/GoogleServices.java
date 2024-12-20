package ru.stayyhydratedd.wbbotapp.application.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import ru.stayyhydratedd.wbbotapp.application.SomeAlgorithms;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleServices {
    private Drive DRIVE_SERVICE;
    private Sheets SHEETS_SERVICE;
    private final Credentials credentials;
    private final SomeAlgorithms someAlgorithms;

    public GoogleServices(Credentials credentials, SomeAlgorithms someAlgorithms) {
        this.credentials = credentials;
        this.someAlgorithms = someAlgorithms;
    }

    public void initServices() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        Credential driveCredential = credentials.getCredentials(HTTP_TRANSPORT, ServiceScope.DRIVE);
        Credential sheetsCredential = credentials.getCredentials(HTTP_TRANSPORT, ServiceScope.SHEETS);

        String sheetsApplicationName = "Google Sheets App";
        SHEETS_SERVICE = new Sheets.Builder(
                HTTP_TRANSPORT, credentials.getJsonFactory(), sheetsCredential)
                .setApplicationName(sheetsApplicationName)
                .build();

        String driveApplicationName = "Google Drive App";
        DRIVE_SERVICE = new Drive.Builder(
                HTTP_TRANSPORT, credentials.getJsonFactory(), driveCredential)
                .setApplicationName(driveApplicationName)
                .build();
    }

    @PostConstruct
    protected void checkStoredCredentialInTokens() throws IOException, GeneralSecurityException {
        if (!Credentials.storedCredential.exists()){
            String browserWillOpenNow = """
                    Сейчас откроется браузер, где необходимо будет авторизоваться \
                    и разрешить доступ для этого приложения
                    Открываю браузер...
                    """;
            someAlgorithms.printSequenceFromString(browserWillOpenNow);

        }

        initServices();

    }
    public Drive getDriveService() {
        return DRIVE_SERVICE;
    }
    public Sheets getSheetsService() {
        return SHEETS_SERVICE;
    }
}
