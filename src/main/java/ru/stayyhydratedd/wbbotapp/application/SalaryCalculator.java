package ru.stayyhydratedd.wbbotapp.application;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.springframework.beans.factory.annotation.Value;
import ru.stayyhydratedd.wbbotapp.application.services.GoogleServices;

import java.io.IOException;
import java.util.*;

public class SalaryCalculator {

    @Value("${google.rate-of-pay:100}")
    private double rateOfPay;

    private final DateCompute date;
    private final Drive driveService;
    private final Sheets sheetsService;
    private final SomeAlgorithms someAlgorithms;
    private final ReportFileConstructor reportFileConstructor;
    private final InitialSetup setup;
    private final RateOfPaySpreadsheetFormatter formatter;

    public SalaryCalculator(InitialSetup setup, GoogleServices googleServices, DateCompute date,
                            SomeAlgorithms someAlgorithms, ReportFileConstructor reportFileConstructor,
                            RateOfPaySpreadsheetFormatter formatter){
        this.date = date;
        driveService = googleServices.getDriveService();
        sheetsService = googleServices.getSheetsService();
        this.someAlgorithms = someAlgorithms;
        this.reportFileConstructor = reportFileConstructor;
        this.setup = setup;
        this.formatter = formatter;
    }
    //второй метод подсчета, который принимает MonthCondition,
    //этот метод используется в случае для всех таблиц
    protected void calculateSalaryForMonthCondition(List<String> spreadsheetIds,
                                                    MonthCondition monthCondition) throws IOException {

        List<String> spreadsheetsIdsWithSpecificSheet = new ArrayList<>();
        List<Sheet> sheetsForMonthCondition = new ArrayList<>();
        Spreadsheet currentSpreadsheet;

        System.out.println("============================================================");

        String monthConditionSheetTitle = date.getMonthAndYear(monthCondition);

        for (String spreadsheetId : spreadsheetIds) {
            try {
                currentSpreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            boolean sheetIsFound = false;
            List<Sheet> sheets = currentSpreadsheet.getSheets();

            for (Sheet sheet : sheets) {
                if (sheet.getProperties().getTitle().equals(monthConditionSheetTitle)) {
                    sheetIsFound = true;
                    sheetsForMonthCondition.add(sheet);
                    spreadsheetsIdsWithSpecificSheet.add(spreadsheetId);
                }
            }
            if(!sheetIsFound) {
                String currentSpreadsheetTitle = currentSpreadsheet.getProperties().getTitle();
                System.out.printf("%sВ таблице '%s' не удалось найти лист за '%s'\n", TextColour.WARN,
                        TextColour.turnTextIntoColor(currentSpreadsheetTitle, TextColour.COLORS.WARN),
                        TextColour.turnTextIntoColor(monthConditionSheetTitle, TextColour.COLORS.WARN));
            }
        }
        if(spreadsheetsIdsWithSpecificSheet.isEmpty()){
            System.out.printf("%sНе удалось найти листы в таблицах за '%s'\n", TextColour.ERROR,
                    TextColour.turnTextIntoColor(date.getMonthAndYear(monthCondition), TextColour.COLORS.ERROR));
        } else {

            List<String> rateOfPaySheets = sheetsService.spreadsheets()
                    .get(setup.getRateOfPayFileId())
                    .execute()
                    .getSheets()
                    .stream()
                    .map(e -> e.getProperties().getTitle())
                    .toList();

            System.out.printf("""
                    %sЛисты ставок (%s) успешно применены!
                    """, TextColour.SUCCESS,
                    TextColour.turnTextIntoColor(Integer.toString(sheetsForMonthCondition.size()), TextColour.COLORS.SUCCESS));

//            ValueRange response;
            int sheetNum = 0;
            for (String spreadsheetId : spreadsheetsIdsWithSpecificSheet) {
                List<Sheet> specificSheet = new ArrayList<>();
                try {
                    currentSpreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                specificSheet.add(sheetsForMonthCondition.get(sheetNum++));

                String forMonth;
                if(monthCondition == MonthCondition.THIS_MONTH){
                    forMonth = "за текущий месяц";
                }else {
                    forMonth = "за предыдущий месяц";
                }
                System.out.printf("""
                                ############################################################
                                %sВыполняется подсчет зарплаты для таблицы '%s' %s
                                """, TextColour.IN_PROCESS,
                        TextColour.turnTextIntoColor(currentSpreadsheet.getProperties().getTitle(), TextColour.COLORS.IN_PROCESS),
                        forMonth);

                String spreadsheetName = currentSpreadsheet.getProperties().getTitle();
                if(!rateOfPaySheets.contains(spreadsheetName.substring(30))){
                    System.out.printf("""
                    %sНе удалось обнаружить лист ставок, он будет создан заново
                    """, TextColour.WARN);
                    formatter.formatingRateOfPayFile(setup.getRateOfPayFileId(), spreadsheetName, false);
//                    sheetsFormatter.createNewFormattedSheetInConfigSpreadsheet(spreadsheetName);
                }

                Map<String, String> configData = checkRateOfPaySheet(currentSpreadsheet);

                calculateSalary(spreadsheetId, specificSheet, configData);

                if(sheetNum == spreadsheetsIdsWithSpecificSheet.size())
                    System.out.println("############################################################");
            }
        }
    }
    protected void calculateSalaryForSpecificMonth(List<String> spreadsheetIds, List<Sheet> sheet)
            throws IOException {
//        ValueRange response;
        Spreadsheet currentSpreadsheet;

        for (String id : spreadsheetIds) {
            try {
                currentSpreadsheet = sheetsService.spreadsheets().get(id).execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.printf("""
                                ############################################################
                                %sВыполняется подсчет зарплаты для таблицы '%s'
                                """, TextColour.IN_PROCESS,
                    TextColour.turnTextIntoColor(currentSpreadsheet.getProperties().getTitle(), TextColour.COLORS.IN_PROCESS));

            String spreadsheetName = driveService.files().get(id).execute().getName();
            String configSheetName = spreadsheetName.substring(30);

            if(!sheetsService.spreadsheets()
                    .get(setup.getRateOfPayFileId())
                    .execute()
                    .getSheets()
                    .stream()
                    .map(e -> e.getProperties().getTitle())
                    .toList()
                    .contains(configSheetName)){
                System.out.printf("""
                %sНе удалось обнаружить лист ставок, он будет создан заново
                """, TextColour.WARN);
                formatter.formatingRateOfPayFile(setup.getRateOfPayFileId(), spreadsheetName, false);
//                sheetsFormatter.createNewFormattedSheetInConfigSpreadsheet(spreadsheetName);
            } else
                System.out.printf("""
                %sЛист ставок (%s) успешно применен!
                """, TextColour.SUCCESS, TextColour.turnTextIntoColor(Integer.toString(sheet.size()), TextColour.COLORS.SUCCESS));

            Map<String, String> configData = checkRateOfPaySheet(currentSpreadsheet);

            calculateSalary(id, sheet, configData);

            if(id.equals(spreadsheetIds.getLast()))
                System.out.println("############################################################");
        }
    }
    //    если конфигурационный файл/файлы не выбраны, принимается пустой Map<String, String>
    //    в List<Sheet> всегда один элемент, так что можно использовать getFirst()
    protected void calculateSalary(String spreadsheetId,
                                   List<Sheet> sheetForSpecificMonth,
                                   Map<String, String> configData) throws IOException {

        Spreadsheet currentSpreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();

        String range = sheetForSpecificMonth.getFirst().getProperties().getTitle() + "!"
                + someAlgorithms.getCellsRange(1, 4,
                sheetForSpecificMonth.getFirst().getProperties().getGridProperties().getColumnCount() - 1,
                sheetForSpecificMonth.getFirst().getProperties().getGridProperties().getRowCount());

        LinkedHashMap<String, Map<String, Double>> employeeData = new LinkedHashMap<>();

        List<List<Object>> hoursResults = new ArrayList<>();

        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();

        if (values == null) {
            System.out.printf("""
                    %sЛист '%s' в таблице '%s' пуст, заполните его перед тем как считать зарплату
                    """, TextColour.WARN,
                    TextColour.turnTextIntoColor(sheetForSpecificMonth.getFirst().getProperties().getTitle(), TextColour.COLORS.WARN),
                    TextColour.turnTextIntoColor(currentSpreadsheet.getProperties().getTitle(), TextColour.COLORS.WARN));
        } else {
            int rowNum = 4;
            int colNum = 2;
            for (List<Object> row : values) {
                Map<String, Double> data = new HashMap<>();
                String name;
                try {
                    name = (String) row.getFirst();
                } catch (NoSuchElementException e) {
                    hoursResults.add(new ArrayList<>() {{
                        add(null);
                    }});
                    continue;
                }

                double currentRateOfPay = rateOfPay;

                if (!configData.isEmpty()) {
                    if (configData.containsKey(name))
                        try {
                            currentRateOfPay = Integer.parseInt(configData.get(name));
                        } catch (NumberFormatException e) {
                            System.out.printf("%sНеверное значение ставки для имени = '%s' в листе ставки '%s'\n",
                                    TextColour.WARN, TextColour.turnTextIntoColor(name, TextColour.COLORS.WARN),
                                    TextColour.turnTextIntoColor(currentSpreadsheet.getProperties().getTitle().substring(30), TextColour.COLORS.WARN));
                        }
                }
                int minutesFirstHalf = 0;
                int minutesSecondHalf = 0;

                for (int i = 1; i < row.size(); i++) {
                    if (!row.get(i).toString().isEmpty()) {
                        int hoursInCell = someAlgorithms.calculateWorkingTimeInCell((String) row.get(i),
                                someAlgorithms.getCell(colNum, rowNum));
                        if (i <= 15)
                            minutesFirstHalf += hoursInCell;
                        else
                            minutesSecondHalf += hoursInCell;
                    }
                    colNum++;
                }
                double hoursFirstHalf = ((double) (minutesFirstHalf - (minutesFirstHalf % 60)) / 60) + ((double) (minutesFirstHalf % 60) / 100);
                double hoursSecondHalf = ((double) (minutesSecondHalf - (minutesSecondHalf % 60)) / 60) + ((double) (minutesSecondHalf % 60) / 100);
                double salFirstHalf = ((hoursFirstHalf - (hoursFirstHalf % 1)) * currentRateOfPay) + ((hoursFirstHalf % 1) * 100) * currentRateOfPay / 60;
                double salSecondHalf = ((hoursSecondHalf - (hoursSecondHalf % 1)) * currentRateOfPay) + ((hoursSecondHalf % 1) * 100) * currentRateOfPay / 60;
                double salResult = salFirstHalf + salSecondHalf;
                int minutesResult = minutesFirstHalf + minutesSecondHalf;
                double hoursResult = ((minutesResult - ((double) (minutesResult % 60))) / 60 + ((double) (minutesResult % 60) / 100));

                hoursResults.add(new ArrayList<>() {{
                    add(Double.toString(hoursResult).replace('.', ','));
                }});

                data.put("hoursFirstHalf", hoursFirstHalf);
                data.put("hoursSecondHalf", hoursSecondHalf);
                data.put("rateOfPay", currentRateOfPay);
                data.put("salFirstHalf", salFirstHalf);
                data.put("salSecondHalf", salSecondHalf);
                data.put("salResult", salResult);

                employeeData.put(name, data);

                colNum = 2;
                rowNum++;
            }

            UpdateValuesResponse result;

            try {
                String rangeHoursResults = sheetForSpecificMonth.getFirst().getProperties().getTitle() + "!" +
                        someAlgorithms.getCellsRange(
                                sheetForSpecificMonth.getFirst().getProperties().getGridProperties().getColumnCount(),
                                4,
                                sheetForSpecificMonth.getFirst().getProperties().getGridProperties().getColumnCount(),
                                sheetForSpecificMonth.getFirst().getProperties().getGridProperties().getRowCount());
                ValueRange body = new ValueRange()
                        .setValues(hoursResults);
                result = sheetsService.spreadsheets().values().update(spreadsheetId, rangeHoursResults, body)
                        .setValueInputOption("USER_ENTERED")
                        .execute();
                System.out.printf("%sЯчеек (%s) обновлено в таблице\n", TextColour.INFO,
                        TextColour.turnTextIntoColor(Integer.toString(result.getUpdatedCells()), TextColour.COLORS.INFO));
            } catch (GoogleJsonResponseException e) {
                GoogleJsonError error = e.getDetails();
                if (error.getCode() == 404) {
                    System.out.printf(TextColour.ERROR + "Таблица с id '%s' не была найдена\n", spreadsheetId);
                } else {
                    throw e;
                }
            }
            Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();

            reportFileConstructor.compileRecordFile(employeeData,
                    sheetForSpecificMonth.getFirst().getProperties().getTitle(),
                    spreadsheet.getProperties().getTitle());
        }
    }

    private Map<String, String> checkRateOfPaySheet(Spreadsheet currentSpreadsheet) throws IOException {

        ValueRange response = sheetsService.spreadsheets()
                .values()
                .get(setup.getRateOfPayFileId(),
                        currentSpreadsheet.getProperties().getTitle().substring(30) + "!A2:B15")
                .execute();

        List<List<Object>> values = response.getValues();
        Map<String, String> configData = new HashMap<>();

        if (values == null || values.isEmpty()) {
            System.out.printf("%sЛист ставок '%s' пуст " +
                            "(значение ставки для всех будет использовано по умолчанию)\n", TextColour.INFO,
                    TextColour.turnTextIntoColor(currentSpreadsheet.getProperties().getTitle().substring(30), TextColour.COLORS.INFO));
        } else {
            for (List<Object> row : values) {
                try {
                    configData.put((String) row.get(0), (String) row.get(1));
                } catch (IndexOutOfBoundsException e) {
                    System.out.printf("%sНе найдено значение ставки для имени = '%s' в листе ставки '%s'\n",
                            TextColour.WARN, TextColour.turnTextIntoColor((String) row.getFirst(), TextColour.COLORS.WARN),
                            TextColour.turnTextIntoColor(currentSpreadsheet.getProperties().getTitle().substring(30), TextColour.COLORS.WARN));
                }
            }
        }
        return configData;
    }
}