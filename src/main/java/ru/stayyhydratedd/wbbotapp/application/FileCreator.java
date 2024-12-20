package ru.stayyhydratedd.wbbotapp.application;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import ru.stayyhydratedd.wbbotapp.application.services.GoogleServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FileCreator {
    private final Drive driveService;

    public FileCreator(GoogleServices googleServices) {
        driveService = googleServices.getDriveService();
    }

    protected void createFile(String fileName, String parentsFolderId,
                              String parentsFolderName, MimeType mimeType) throws IOException {
        List<String> parentsFolderIdList = new ArrayList<>(){{
            add(parentsFolderId);
        }};

        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setMimeType("application/vnd.google-apps." + mimeType.toString().toLowerCase())
                .setParents(parentsFolderIdList);
        try {
            driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
            String fileType = getFileType(mimeType);
            System.out.printf("""
                    %s%s '%s' успешно создана в '%s'!
                    """, TextColour.SUCCESS, fileType,
                    TextColour.turnTextIntoColor(fileName, TextColour.COLORS.SUCCESS),
                    TextColour.turnTextIntoColor(parentsFolderName, TextColour.COLORS.SUCCESS));
        } catch (GoogleJsonResponseException e) {
            System.err.printf(TextColour.ERROR + "Не удалось создать %s: " + e.getDetails(), getFileType(mimeType));
            throw e;
        }
    }
    protected String getFileType(MimeType mimeType) {
        if(mimeType == MimeType.FOLDER)
            return "Папка";
        else if (mimeType == MimeType.SPREADSHEET)
            return "Таблица";
        else
            return null;
    }
    //используется, чтобы вернуть список файлов в папке с указанным id
    protected FileList getDirectoryFileList(String folderId) throws IOException {
        return driveService.files().list()
                .setQ("'" + folderId + "' in parents and trashed = false")
                .setFields("nextPageToken, files(id, name)")
                .execute();
    }
    //перегруженный метод, используется, чтобы вернуть список доступных файлов в google drive
    protected FileList getDriveFileList() throws IOException {
        return driveService.files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
    }

    protected LinkedHashMap<String, String> getDirectoryDataMap(FileList fileList){
        LinkedHashMap<String, String> spreadsheetData = new LinkedHashMap<>();
        List<File> files = fileList.getFiles();
        for(File file: files){
            spreadsheetData.put(file.getName(), file.getId());
        }
        return spreadsheetData;
    }
}
