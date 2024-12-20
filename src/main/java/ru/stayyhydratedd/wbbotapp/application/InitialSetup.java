package ru.stayyhydratedd.wbbotapp.application;


import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Value;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class InitialSetup {
    private final SomeAlgorithms someAlgorithms;
    private final FileCreator fileCreator;
    private final RateOfPaySpreadsheetFormatter formatter;

    public static final java.io.File YML = new java.io.File(
            System.getProperty("user.dir") + "\\application.yml");

    public InitialSetup(SomeAlgorithms someAlgorithms, FileCreator fileCreator, RateOfPaySpreadsheetFormatter formatter) {
        this.someAlgorithms = someAlgorithms;
        this.fileCreator = fileCreator;
        this.formatter = formatter;
    }

    @Value("${google.main-directory-id}")
    private String mainDirectoryId;
    @Value("${google.schedules-directory-id}")
    private String schedulesDirectoryId;
    @Value("${google.salary-directory-id}")
    private String salaryDirectoryId;
    @Value("${google.rate-of-pay-file-id}")
    private String rateOfPayFileId;

    @PostConstruct
    protected void checkProperties() {
        if(mainDirectoryId.isEmpty())
            initializeProperties(true);

    }
    protected boolean initializeProperties(boolean firstLaunch) {

        if(firstLaunch){
            System.out.println("============================================================");
            someAlgorithms.printSequenceFromString
                    ("При первом запуске приложения, необходимо произвести некоторую настройку\n");
        }
        someAlgorithms.printSequenceFromString("Пожалуйста вставьте ID вашей рабочей папки Google Drive: ");

        mainDirectoryId = new Scanner(System.in).nextLine();

        try{
            FileList result = fileCreator.getDriveFileList();
            List<File> files = result.getFiles();

            boolean workingDirectoryIdIsCorrect = false;

            for(File file : files){
                if(file.getId().equals(mainDirectoryId)){
                    workingDirectoryIdIsCorrect = true;
                    break;
                }
            }
            if(workingDirectoryIdIsCorrect) {
                LinkedHashMap<String, String> ownerDirectoryMap = fileCreator.getDirectoryDataMap(
                        fileCreator.getDirectoryFileList(mainDirectoryId));

                String schedulesDirectoryTitle = "Графики работы сотрудников ПВЗ";
                String salaryDirectoryTitle = "Зарплаты сотрудников";
                String rateOfPayTitle = "Ставка";

                if(ownerDirectoryMap.isEmpty()){
                    fileCreator.createFile(schedulesDirectoryTitle, mainDirectoryId,
                            "вашей рабочей папке", MimeType.FOLDER);
                    fileCreator.createFile(salaryDirectoryTitle, mainDirectoryId,
                            "вашей рабочей папке", MimeType.FOLDER);

                    ownerDirectoryMap = fileCreator.getDirectoryDataMap(
                            fileCreator.getDirectoryFileList(mainDirectoryId));

                    fileCreator.createFile(rateOfPayTitle, ownerDirectoryMap.get(salaryDirectoryTitle),
                            salaryDirectoryTitle, MimeType.SPREADSHEET);
                }
                schedulesDirectoryId = ownerDirectoryMap.get(schedulesDirectoryTitle);
                salaryDirectoryId = ownerDirectoryMap.get(salaryDirectoryTitle);

                LinkedHashMap<String, String> salaryDirectoryMap = fileCreator.getDirectoryDataMap(
                        fileCreator.getDirectoryFileList(ownerDirectoryMap.get(salaryDirectoryTitle)));

                rateOfPayFileId = salaryDirectoryMap.get(rateOfPayTitle);

                if(firstLaunch)
                    formatter.formatingRateOfPayFile(rateOfPayFileId, "Лист ставок example", true);

                updateYml();
            }else{
                System.out.print(TextColour.ERROR);
                someAlgorithms.printSequenceFromString("""
                        Не удалось найти рабочую папку с введенным ID.
                        Проверьте правильность скопированного ID и повторите попытку ввода
                        """);
                return initializeProperties(false);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private void updateYml() {
        setYml();

        try (PrintWriter printWriter = new PrintWriter(YML)) {

            Map<String, Map<String, String>> dataMap = new HashMap<>();
            dataMap.put("google", new HashMap<>(){{
                put("main-directory-id", mainDirectoryId);
                put("schedules-directory-id", schedulesDirectoryId);
                put("salary-directory-id", salaryDirectoryId);
                put("rate-of-pay-file-id", rateOfPayFileId);
            }});

            DumperOptions options = new DumperOptions();
            options.setIndent(2);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);

            Yaml yaml = new Yaml(options);
            yaml.dump(dataMap, printWriter);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void setYml(){
        if(!YML.exists()){
            try {
                Files.createFile(YML.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    protected String getSchedulesDirectoryId() {
        return schedulesDirectoryId;
    }
    protected String getSalaryDirectoryId() {
        return salaryDirectoryId;
    }
    protected String getRateOfPayFileId() {
        return rateOfPayFileId;
    }
}

