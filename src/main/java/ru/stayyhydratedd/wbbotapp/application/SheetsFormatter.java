package ru.stayyhydratedd.wbbotapp.application;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import ru.stayyhydratedd.wbbotapp.application.services.GoogleServices;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class SheetsFormatter {
    private LinkedHashMap<String, String> spreadsheetsData;

    private final Sheets sheetsService;
    private final DateCompute date;
    private final SomeAlgorithms someAlgorithms;
    private final FileCreator fileCreator;
    private final InitialSetup setup;
    private final RateOfPaySpreadsheetFormatter formatter;

    public SheetsFormatter(GoogleServices googleServices, DateCompute date, SomeAlgorithms someAlgorithms,
                           InitialSetup setup, FileCreator fileCreator, RateOfPaySpreadsheetFormatter formatter) {
        this.date = date;
        this.setup = setup;
        this.sheetsService = googleServices.getSheetsService();
        this.someAlgorithms = someAlgorithms;
        this.fileCreator = fileCreator;
        this.formatter = formatter;
    }
    //    форматирование только что созданных листов
    protected void createNewFormattedSheetInSpreadsheet
    (List<String> spreadsheetIds, MonthCondition monthCondition) throws IOException {
        Spreadsheet spreadsheet;

        for(String spreadsheetId: spreadsheetIds){
            spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
            List<Request> requests = Arrays.asList(
                    new Request().setAddSheet(  //смена id листа, имени и количества строк с колоннами
                            new AddSheetRequest().setProperties(
                                    new SheetProperties().setGridProperties(
                                                    new GridProperties()
                                                            .setColumnCount(date.getLengthOfMonth(monthCondition) + 2)
                                                            .setRowCount(15)
                                                            .setFrozenColumnCount(1)    //заморозка одной колонны слева
                                                            .setFrozenRowCount(3)   //заморозка трех строк сверху
                                            ).setSheetId(date.getMonthAndYearForId(monthCondition))
                                            .setTitle(date.getMonthAndYear(monthCondition))
                            )
                    ),
                    new Request().setRepeatCell(    //12 шрифт для первой строки
                            new RepeatCellRequest().setCell(
                                    new CellData().setUserEnteredFormat(
                                            new CellFormat().setTextFormat(
                                                    new TextFormat().setFontSize(12)
                                            ).setHorizontalAlignment("center")
                                    )
                            ).setRange(
                                    new GridRange()
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                                            .setStartRowIndex(0)
                                            .setEndRowIndex(1)
                                            .setStartColumnIndex(0)
                                            .setEndColumnIndex(date.getLengthOfMonth(monthCondition) + 3)
                            ).setFields("*")
                    ),
                    new Request().setRepeatCell(    //11 шрифт для второй и третей строк
                            new RepeatCellRequest().setCell(
                                    new CellData().setUserEnteredFormat(
                                            new CellFormat().setTextFormat(
                                                    new TextFormat().setFontSize(11)
                                            ).setHorizontalAlignment("center")
                                    )
                            ).setRange(
                                    new GridRange()
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                                            .setStartRowIndex(1)
                                            .setEndRowIndex(3)
                                            .setStartColumnIndex(0)
                                            .setEndColumnIndex(date.getLengthOfMonth(monthCondition) + 3)
                            ).setFields("*")
                    ),
                    new Request().setRepeatCell(    //выравнивание по горизонтали на центр для всех ячеек
                            new RepeatCellRequest().setCell(
                                    new CellData().setUserEnteredFormat(
                                            new CellFormat().setHorizontalAlignment("center")
                                    )
                            ).setRange(
                                    new GridRange()
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                                            .setStartRowIndex(3)
                                            .setEndRowIndex(15)
                                            .setStartColumnIndex(0)
                                            .setEndColumnIndex(date.getLengthOfMonth(monthCondition) + 2)
                            ).setFields("*")
                    ),
                    new Request().setRepeatCell(    //красим последние ячейки во второй и третей строке в рандомный цвет
                            new RepeatCellRequest().setCell(
                                    new CellData().setUserEnteredFormat(
                                            new CellFormat().setBackgroundColor(
                                                            new Color()
                                                                    .setGreen(new Random().nextFloat(0.8f, 0.99f))
                                                                    .setBlue(new Random().nextFloat(0.8f, 0.99f))
                                                                    .setRed(new Random().nextFloat(0.8f, 0.99f))
                                                    ).setTextFormat(
                                                            new TextFormat()
                                                                    .setFontSize(11)
                                                    ).setHorizontalAlignment("center")
                                                    .setVerticalAlignment("bottom")
                                    )
                            ).setRange(
                                    new GridRange()
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                                            .setStartRowIndex(1)
                                            .setEndRowIndex(3)
                                            .setStartColumnIndex(date.getLengthOfMonth(monthCondition) + 1)
                                            .setEndColumnIndex(date.getLengthOfMonth(monthCondition) + 2)
                            ).setFields("*")
                    ),
                    new Request().setRepeatCell(    //красим первые ячейки во второй и третей строке в рандомный цвет
                            new RepeatCellRequest().setCell(
                                    new CellData().setUserEnteredFormat(
                                            new CellFormat().setBackgroundColor(
                                                            new Color()
                                                                    .setGreen(new Random().nextFloat(0.8f, 0.99f))
                                                                    .setBlue(new Random().nextFloat(0.8f, 0.99f))
                                                                    .setRed(new Random().nextFloat(0.8f, 0.99f))
                                                    ).setTextFormat(
                                                            new TextFormat()
                                                                    .setFontSize(11)
                                                    ).setHorizontalAlignment("center")
                                                    .setVerticalAlignment("bottom")
                                    )
                            ).setRange(
                                    new GridRange()
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                                            .setStartRowIndex(1)
                                            .setEndRowIndex(3)
                                            .setStartColumnIndex(0)
                                            .setEndColumnIndex(1)
                            ).setFields("*")
                    ),
                    new Request().setMergeCells(    //объединение ячеек с 1 по 16 в первой строке
                            new MergeCellsRequest().setRange(
                                    new GridRange()
                                            .setStartRowIndex(0)
                                            .setEndRowIndex(1)
                                            .setStartColumnIndex(1)
                                            .setEndColumnIndex(16)
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                            )
                    ),
                    new Request().setMergeCells(    //объединение ячеек с 16 по последнюю во второй строке
                            new MergeCellsRequest().setRange(
                                    new GridRange()
                                            .setStartRowIndex(0)
                                            .setEndRowIndex(1)
                                            .setStartColumnIndex(16)
                                            .setEndColumnIndex(date.getLengthOfMonth(monthCondition) + 2)
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                            )
                    ),
                    new Request().setMergeCells(    //объединение последних ячеек во второй и третей строках
                            new MergeCellsRequest().setRange(
                                    new GridRange()
                                            .setStartRowIndex(1)
                                            .setEndRowIndex(3)
                                            .setStartColumnIndex(date.getLengthOfMonth(monthCondition) + 1)
                                            .setEndColumnIndex(date.getLengthOfMonth(monthCondition) + 2)
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                            )
                    ),
                    new Request().setMergeCells(    //объединение первых ячеек во второй и третей строках
                            new MergeCellsRequest().setRange(
                                    new GridRange()
                                            .setStartRowIndex(1)
                                            .setEndRowIndex(3)
                                            .setStartColumnIndex(0)
                                            .setEndColumnIndex(1)
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                            )
                    ),
                    new Request().setUpdateDimensionProperties(     //88 пикселей для всех колонн
                            new UpdateDimensionPropertiesRequest().setRange(
                                    new DimensionRange()
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                                            .setDimension("columns")
                                            .setStartIndex(0)
                                            .setEndIndex(date.getLengthOfMonth(monthCondition) + 3)
                            ).setProperties(
                                    new DimensionProperties()
                                            .setPixelSize(88)
                            ).setFields("*")
                    ),
                    new Request().setUpdateDimensionProperties(     //25 пикселей для всех строк
                            new UpdateDimensionPropertiesRequest().setRange(
                                    new DimensionRange()
                                            .setSheetId(date.getMonthAndYearForId(monthCondition))
                                            .setDimension("rows")
                                            .setStartIndex(0)
                                            .setEndIndex(16)
                            ).setProperties(
                                    new DimensionProperties()
                                            .setPixelSize(25)
                            ).setFields("*")
                    )
            );
            try {
                BatchUpdateSpreadsheetRequest body =
                        new BatchUpdateSpreadsheetRequest()
                                .setRequests(requests);
                sheetsService.spreadsheets()
                        .batchUpdate(spreadsheetId, body)
                        .execute();
                System.out.printf("%sНовый лист для таблицы '%s' был успешно создан!\n", TextColour.SUCCESS,
                        TextColour.turnTextIntoColor(spreadsheet.getProperties().getTitle(), TextColour.COLORS.SUCCESS));
                try {
                    formatSheet(spreadsheetId, monthCondition);
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            } catch (GoogleJsonResponseException e) {
                GoogleJsonError error = e.getDetails();
                if (error.getCode() == 404) {
                    //вывод, если таблица с указанным id не найдена (скорее всего никогда не выполнится)
                    System.out.printf("Таблица '%s' не найдена\n", spreadsheet.getProperties().getTitle());
                } else {
                    //вывод, если лист с id месяца уже существует в таблице
                    System.out.printf("Лист за '%s' уже существует в таблице '%s'\n",
                            date.getMonthAndYear(monthCondition), spreadsheet.getProperties().getTitle());
                }
            }
            if(!spreadsheetId.equals(spreadsheetIds.getLast())){
                System.out.println("************************************************************");
            }
        }
    }

    private void formatSheet(String spreadsheetId, MonthCondition monthCondition)
            throws IOException, GeneralSecurityException {

        String sheetTitle = date.getMonthAndYear(monthCondition) + "!";

        List<Object> datesList = new ArrayList<>();

        for(int i = 1; i <= date.getLengthOfMonth(monthCondition); i++)
            datesList.add(Integer.toString(i));

        List<Object> weekdaysList = Arrays.asList(date.getWeekdaysList(monthCondition).toArray());

        List<List<Object>> firstValues = new ArrayList<>(){{
            add(datesList);
            add(weekdaysList);
        }};
        String firstRange = sheetTitle + someAlgorithms.getCellsRange(2,2,
                date.getLengthOfMonth(monthCondition) + 2, 3);

        List<List<Object>> secondValues = new ArrayList<>(){{
            add(new ArrayList<>(){{
                add("Имя");
            }});
        }};
        String secondRange = sheetTitle + someAlgorithms.getCellsRange(1,2,
                1, 3);

        List<List<Object>> thirdValues = new ArrayList<>(){{
            add(new ArrayList<>(){{
                add("Кол-во\nчасов");
            }});
        }};
        String thirdRange = sheetTitle + someAlgorithms.getCellsRange(
                date.getLengthOfMonth(monthCondition) + 2,2,
                date.getLengthOfMonth(monthCondition) + 2, 3);

        List<List<Object>> fourthValues = new ArrayList<>(){{
            add(new ArrayList<>(){{
                add(date.getMonthAndYear(monthCondition));
            }});
        }};
        String fourthRange = sheetTitle + someAlgorithms.getCellsRange(2,1,
                16, 1);

        String fifthRange = sheetTitle + someAlgorithms.getCellsRange(17, 1,
                date.getLengthOfMonth(monthCondition) + 1, 1);

        List<ValueRange> data = new ArrayList<>(){{
            add(new ValueRange()
                    .setValues(firstValues)
                    .setRange(firstRange));
            add(new ValueRange()
                    .setValues(secondValues)
                    .setRange(secondRange));
            add(new ValueRange()
                    .setValues(thirdValues)
                    .setRange(thirdRange));
            add(new ValueRange()
                    .setValues(fourthValues)
                    .setRange(fourthRange));
            add(new ValueRange()
                    .setValues(fourthValues)
                    .setRange(fifthRange));
        }};

        BatchUpdateValuesResponse result;
        try {
            BatchUpdateValuesRequest body = new BatchUpdateValuesRequest()
                    .setValueInputOption("USER_ENTERED")
                    .setData(data);
            result = sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, body).execute();
            System.out.printf("%sЯчеек (%s) обновлено в текущем листе\n",
                    TextColour.INFO,
                    TextColour.turnTextIntoColor(Integer.toString(result.getTotalUpdatedCells()), TextColour.COLORS.INFO));

        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf(TextColour.ERROR + "Таблица с id '%s' не была найдена\n", spreadsheetId);
            } else {
                throw e;
            }
        }
    }

    protected boolean createNewSpreadsheet(String spreadsheetName)
            throws GeneralSecurityException, IOException{

        updateSpreadsheetData();

        if(spreadsheetsData.containsKey(spreadsheetName)){
            System.out.printf("%sТаблица с таким названием уже существует! Введите другое название\n",
                    TextColour.WARN);
            return false;
        } else if(spreadsheetName.length() <= 32){
            System.out.printf("%sНазвание таблицы слишком короткое, укажите другое название\n",
                    TextColour.WARN);
            return false;
        } else {
            System.out.printf("""
                                ############################################################
                                %sВыполняется создание таблицы '%s'
                                """, TextColour.IN_PROCESS,
                    TextColour.turnTextIntoColor(spreadsheetName, TextColour.COLORS.IN_PROCESS));
            String parentsDirectoryTitle = "Графики работы сотрудников";
            fileCreator.createFile(spreadsheetName, setup.getSchedulesDirectoryId(),
                    parentsDirectoryTitle, MimeType.SPREADSHEET);

            updateSpreadsheetData();
        }
        deleteFirstSheetFromNewSpreadsheet(spreadsheetName);
        createNewFolderInSalaryDirectory(spreadsheetName);
        formatter.formatingRateOfPayFile(setup.getRateOfPayFileId(), spreadsheetName, false);

        System.out.println("############################################################");

        return true;
    }
    protected void createNewFolderInSalaryDirectory(String spreadsheetName)
            throws IOException {
        String folderName = "Зарплаты сотрудников ул. " + spreadsheetName.substring(30);

        FileList result = fileCreator.getDirectoryFileList(setup.getSalaryDirectoryId());

        List<String> files = result.getFiles().stream().map(File::getName).toList();

        if(!files.contains(folderName)){

            fileCreator.createFile(folderName, setup.getSalaryDirectoryId(),
                    "Зарплаты сотрудников", MimeType.FOLDER);

        } else
            System.out.printf("""
                    %s%s '%s' не была создана, так как уже существует
                    """, TextColour.INFO, fileCreator.getFileType(MimeType.FOLDER),
                    TextColour.turnTextIntoColor(folderName, TextColour.COLORS.INFO));
    }
    protected void showListOfSpreadsheets() throws GeneralSecurityException, IOException {

        List<File> files = fileCreator.getDirectoryFileList(setup.getSchedulesDirectoryId()).getFiles();

        if (!files.isEmpty()) {
            System.out.println("============================================================");
            if(files.size() != 1) {
                System.out.println("Выберите таблицу из списка:\n");
                int fileNum = 1;
                for (File file : files) {
                    System.out.printf("%d. %s\n", fileNum++, file.getName());
                }
                System.out.println("============================================================");
            } else{
                System.out.println("[INFO] Таблица '" + files.getFirst().getName() + "' была автоматически выбрана");
            }
        }
    }

    protected void deleteFirstSheetFromNewSpreadsheet(String spreadsheetName) throws IOException {


        createNewFormattedSheetInSpreadsheet(
                new ArrayList<>(){{
                    add(spreadsheetsData.get(spreadsheetName));
                }}, MonthCondition.THIS_MONTH
        );

        List<Request> requests = List.of(
                new Request().setDeleteSheet(
                        new DeleteSheetRequest().setSheetId(0)
                )
        );
        try {
            BatchUpdateSpreadsheetRequest body =
                    new BatchUpdateSpreadsheetRequest()
                            .setRequests(requests);
            sheetsService.spreadsheets()
                    .batchUpdate(spreadsheetsData.get(spreadsheetName), body)
                    .execute();
        } catch (GoogleJsonResponseException e) {
            System.out.printf("%sНе удалось удалить нулевой лист\n", TextColour.WARN);
        }
    }
    protected void updateSpreadsheetData() throws IOException {
        spreadsheetsData = fileCreator.getDirectoryDataMap(
                fileCreator.getDirectoryFileList(setup.getSchedulesDirectoryId())
        );
    }
}