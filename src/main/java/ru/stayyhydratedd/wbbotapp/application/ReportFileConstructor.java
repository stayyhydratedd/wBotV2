package ru.stayyhydratedd.wbbotapp.application;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import ru.stayyhydratedd.wbbotapp.application.services.GoogleServices;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ReportFileConstructor {
    /*
MMMM YY     SPREADSHEET_NAME
Имя         |Часы(c 1 по 15)   |Часы(с 16 по 30)   |Ставка     |Сумма(с 1 по 15)   |Сумма(с 16 по 30)  |Итоговая сумма |
------------|------------------|-------------------|-----------|-------------------|-------------------|---------------|
Name1		|90				   |85				   |120		   |8800			   |8400			   |17200		   |
------------|------------------|-------------------|-----------|-------------------|-------------------|---------------|
Name2		|81				   |90				   |120		   |6900			   |7300			   |15000		   |
------------|------------------|-------------------|-----------|-------------------|-------------------|---------------|
Name3		|52				   |50				   |100		   |5000			   |4800			   |9800		   |
------------|------------------|-------------------|-----------|-------------------|-------------------|---------------|
*/
    private static final StringBuilder recordFile = new StringBuilder();
    private static final String USERNAME = System.getProperty("user.name");
    protected static final String USERS_PATH = "C:\\Users\\" + USERNAME;

    private final InitialSetup setup;
    private final FileCreator fileCreator;
    private final Drive driveService;
//    private final List<List<String>> PARENT_FOLDERS_IDS = new ArrayList<>();

    public ReportFileConstructor(GoogleServices googleServices, InitialSetup setup,
                                 FileCreator fileCreator){
        this.setup = setup;
        this.fileCreator = fileCreator;
        this.driveService = googleServices.getDriveService();
    }

    private void setFirstRowRecordFile(int daysInMonth){
        recordFile
                .append(String.format("%-12s|%-18s|Часы(с 16 по %d%-3s|%-11s|%-18s|Сумма(с 16 по %d%-2s|%-15s|\n",
                        "Имя", "Часы(с 1 по 15)", daysInMonth, ")", "Ставка", "Сумма(с 1 по 15)", daysInMonth, ")",
                        "Итоговая сумма"))
                .append("------------+------------------+------------------+-----------+------------------+------------------+---------------|\n");
    }
    private void appendEmployeeInRecordFile(LinkedHashMap<String, Map<String, Double>> employeeData){

        LinkedHashSet<String> setNames = new LinkedHashSet<>(employeeData.keySet());
        for(String name: setNames){
            Map<String, Double> data = employeeData.get(name);
            recordFile.append(String.format("%-12s|%-18.2f|%-18.2f|%-11.1f|%-18.1f|%-18.1f|%-15.1f|\n",
                    name, data.get("hoursFirstHalf"), data.get("hoursSecondHalf"), data.get("rateOfPay"),
                    data.get("salFirstHalf"), data.get("salSecondHalf"), data.get("salResult")));
            recordFile.append("------------+------------------+------------------+-----------+------------------+------------------+---------------|\n");
        }
    }
    //    for specific
    //    argument date example: сентябрь 24; август 25
    protected void compileRecordFile(
            LinkedHashMap<String, Map<String, Double>> employeeData, String date, String spreadsheetName){
        recordFile.append(String.format("%14s%21s\n", date.toUpperCase(), spreadsheetName.substring(26).toUpperCase()));
        setFirstRowRecordFile(DateCompute.getLengthOfMonth(date));
        appendEmployeeInRecordFile(employeeData);
        try {
            uploadRecordFile(spreadsheetName, date);
            recordFile.setLength(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void uploadRecordFile(String spreadsheetName, String date) throws IOException {
        String name = spreadsheetName.substring(30);
        String folderId = getFolderIdInFolderRecords(name);
        createTxtInUsers(date);
        uploadToFolder(folderId, date);
        deleteTxtInUsers(date);
    }
    protected void uploadToFolder(String realFolderId, String date) throws IOException {
        List<String> parentFolder = Collections.singletonList(realFolderId);

        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(date).setMimeType("text/plain").setParents(parentFolder);

        java.io.File filePath = new java.io.File(USERS_PATH + "\\" + date + ".txt");
        FileContent mediaContent = new FileContent("text/plain", filePath);
        com.google.api.services.drive.model.File folder = driveService.files().get(realFolderId).execute();

        try {
            FileList result = driveService.files().list()
                    .setQ("name='" + date + "' and mimeType='text/plain' and '" + parentFolder.getFirst() + "' in parents and trashed = false")
                    .setPageSize(30)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<com.google.api.services.drive.model.File> files = result.getFiles();
            if (!files.isEmpty()) {
                driveService.files().delete(files.getFirst().getId()).execute();
                driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
                System.out.printf("""
                        %sФайл '%s' в папке '%s' был перезаписан
                        """, TextColour.SUCCESS,
                        TextColour.turnTextIntoColor(date, TextColour.COLORS.SUCCESS),
                        TextColour.turnTextIntoColor(folder.getName(), TextColour.COLORS.SUCCESS));
            } else {
                driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
                System.out.printf("""
                        %sФайл '%s' в папке '%s' был успешно создан
                        """, TextColour.SUCCESS,
                        TextColour.turnTextIntoColor(date, TextColour.COLORS.SUCCESS),
                        TextColour.turnTextIntoColor(folder.getName(), TextColour.COLORS.SUCCESS));
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println(TextColour.ERROR + "Не удалось загрузить файл: " + e.getDetails());
            throw e;
        }
    }
    protected String getFolderIdInFolderRecords(String spreadsheetName) throws IOException {
        String folderName = "Зарплаты сотрудников ул. " + spreadsheetName;

        FileList result = fileCreator.getDirectoryFileList(setup.getSalaryDirectoryId());

        List<com.google.api.services.drive.model.File> files = result.getFiles();
        for(com.google.api.services.drive.model.File file: files){
            if(file.getName().equals(folderName)){
                return file.getId();
            }
        }
        return null;
    }
    private void createTxtInUsers(String date) throws IOException {
        BufferedWriter output = null;
        try {
            File file = new File(USERS_PATH + "\\" + date + ".txt");
            output = new BufferedWriter(new FileWriter(file));
            output.write(recordFile.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
    private void deleteTxtInUsers(String date){
        File file = new File(USERS_PATH + "\\" + date + ".txt");
        file.delete();
    }
}
