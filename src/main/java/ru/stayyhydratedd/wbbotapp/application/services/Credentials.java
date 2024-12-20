package ru.stayyhydratedd.wbbotapp.application.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class Credentials {

    private static final String TOKENS_DIRECTORY = "tokens";

    public static final File storedCredential = new File(
            System.getProperty("user.dir") + "\\" + TOKENS_DIRECTORY + "\\StoredCredential");

    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    protected Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, ServiceScope scope)
            throws IOException {

        String CREDENTIALS_FILE = "/credentials.json";

        InputStream IN = Credentials.class.getResourceAsStream(CREDENTIALS_FILE);

        if (IN == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(IN));

        List<String> SCOPES = scope == ServiceScope.DRIVE ?
                Arrays.asList(DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_FILE) :
                List.of(SheetsScopes.SPREADSHEETS);


        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("wbotV2");
    }

    protected JsonFactory getJsonFactory() {
        return JSON_FACTORY;
    }
}
