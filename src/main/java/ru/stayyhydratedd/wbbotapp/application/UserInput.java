package ru.stayyhydratedd.wbbotapp.application;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import org.springframework.beans.factory.annotation.Autowired;
import ru.stayyhydratedd.wbbotapp.application.services.Credentials;
import ru.stayyhydratedd.wbbotapp.application.services.GoogleServices;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserInput {
    private final HelpInfo helpInfo;
    private final DateCompute dateCompute;
    private final SomeAlgorithms someAlgorithms;
    private final SheetsFormatter sheetsFormatter;
    private final Sheets sheetsService;
    private final SalaryCalculator salaryCalculator;
    private final FileCreator fileCreator;
    private final InitialSetup setup;

    @Autowired
    public UserInput(HelpInfo helpInfo, DateCompute dateCompute, SomeAlgorithms someAlgorithms,
                     SheetsFormatter sheetsFormatter, GoogleServices googleServices,
                     SalaryCalculator salaryCalculator, FileCreator fileCreator,
                     InitialSetup setup) {
        this.helpInfo = helpInfo;
        this.dateCompute = dateCompute;
        this.someAlgorithms = someAlgorithms;
        this.sheetsFormatter = sheetsFormatter;
        this.salaryCalculator = salaryCalculator;
        this.sheetsService = googleServices.getSheetsService();
        this.fileCreator = fileCreator;
        this.setup = setup;
    }

    public boolean greet(boolean firstLaunch){
        String welcome;
        if(firstLaunch){
            System.out.println("============================================================");
            welcome = String.format("""
                    Для начала работы выбери действие цифрой и нажми %s
                    Вы всегда можете вызвать краткую справку командой '%s'
                    """, TextColour.turnTextIntoColor("Enter", TextColour.COLORS.SUCCESS),
                    TextColour.turnTextIntoColor("help", TextColour.COLORS.INFO));
        }
        else
            welcome = "============================================================";
        System.out.printf("""
                %s
                1. Создать новую таблицу с графиком работы ПВЗ в директории
                2. Выбрать действие для существующей таблицы
                3. Выбрать действие для всех существующих таблиц
                ============================================================
                """, welcome);
        try {
            inputForGreet();
        } catch (GeneralSecurityException | IOException e) {
            if(Credentials.storedCredential.delete()) {
                someAlgorithms.printSequenceFromString("Ваш токен доступа истек, сейчас приложение перезапустится" +
                        " и вам нужно будет авторизоваться заново\n");
                throw new RuntimeException("Token has been expired or revoked.");
            }
        }
        return true;
    }
    private String parseInput(){
        String inputString = new Scanner(System.in).next();
        if(!inputString.matches("((back|/back)|(help|/help)|(changedir|/changedir))")) {
            try {
                Integer.parseInt(inputString);
            } catch (NumberFormatException e) {
                System.out.printf("""
                                %sУказано неверное значение
                                %sВведите команду '%s' для вызова справки
                                """,
                        TextColour.ERROR, TextColour.INFO,
                        TextColour.turnTextIntoColor("help", TextColour.COLORS.INFO));
                return parseInput();
            }
        } else {
            inputString = inputString.replaceFirst("/", "");
        }
        return inputString;
    }
    private boolean inputForGreet() throws GeneralSecurityException, IOException {
        String input = parseInput();
        return switch (input) {
            case "1" -> {
                setNewSpreadsheetName(true);
                yield true;
            }
            case "2" -> {
                selectSpreadsheet(false);
                yield true;
            }
            case "3" -> {
                selectSpreadsheet(true);
                yield true;
            }
            case "help" -> {
                helpInfo.printHelpInfoForGreet();
                yield inputForGreet();
            }
            case "changedir" -> {
                setup.initializeProperties(false);
                System.out.printf("%sРабочая папка успешно изменена\n", TextColour.SUCCESS);
                yield greet(false);
            }
            default -> {
                System.out.printf("%sЭто действие недоступно\n", TextColour.WARN);
                yield inputForGreet();
            }
        };
    }
    private boolean chooseActionForSpreadsheet(boolean forAll, List<String> spreadsheetIds){
        String countOfSpreadsheet = "";
        String forThisSpreadsheet;
        String createNewList;
        if(forAll){
            forThisSpreadsheet = "для всех таблиц";
            createNewList = "новые листы во всех таблицах";
            countOfSpreadsheet = "(" + spreadsheetIds.size() + ")";
        }else{
            forThisSpreadsheet = "для этой таблицы";
            createNewList = "новый лист в текущей таблице";
        }
        System.out.printf("""
                ============================================================
                Выберите действие %s %s
                
                1. Создать %s
                2. Посчитать зарплату у сотрудников
                ============================================================
                """, forThisSpreadsheet, countOfSpreadsheet, createNewList);
        return inputForChooseActionForSpreadsheet(spreadsheetIds, forAll);
    }
    private boolean inputForChooseActionForSpreadsheet(List<String> spreadsheetIds, boolean forAll){
        String inputString = parseInput();
        return switch (inputString) {
            case "1" -> {
                chooseMonthConditionForNewSpreadsheet(spreadsheetIds, forAll);
                yield true;
            }
            case "2" -> {
                if (forAll)
                    chooseSheetInSpreadsheets(spreadsheetIds);
                else
                    chooseSheetInSingleSpreadsheet(spreadsheetIds.getFirst());
                yield true;
            }
            case "help" -> {
                helpInfo.printHelpInfoForChooseActionForSpreadsheet(forAll);
                yield inputForChooseActionForSpreadsheet(spreadsheetIds, forAll);
            }
            case "back" -> {
                if(!forAll){
                    if(spreadsheetIds.size() == 1)
                        yield greet(false);
                    else
                        try {
                            yield selectSpreadsheet(false);
                        } catch (GeneralSecurityException | IOException e) {
                            throw new RuntimeException(e);
                        }
                } else {
                    yield greet(false);
                }
            }
            default -> {
                System.out.printf("%sЭто действие недоступно\n", TextColour.WARN);
                yield inputForChooseActionForSpreadsheet(spreadsheetIds, forAll);
            }
        };
    }
    private void chooseSheetInSpreadsheets(List<String> spreadsheetIds){
        System.out.printf("""
                ============================================================
                Выберите за какой месяц посчитать зарплату:
                
                1. Текущий (%s)
                2. Предыдущий (%s)
                ============================================================
                """,
                dateCompute.getMonthAndYear(MonthCondition.THIS_MONTH),
                dateCompute.getMonthAndYear(MonthCondition.PREV_MONTH));
        try {
            inputForChooseSheetInSpreadsheets(spreadsheetIds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean inputForChooseSheetInSpreadsheets(List<String> spreadsheetIds) throws IOException {
        String input = parseInput();
        return switch (input) {
            case "1" -> {
                salaryCalculator.calculateSalaryForMonthCondition(spreadsheetIds, MonthCondition.THIS_MONTH);
                yield greet(false);
            }
            case "2" -> {
                salaryCalculator.calculateSalaryForMonthCondition(spreadsheetIds, MonthCondition.PREV_MONTH);
                yield greet(false);
            }
            case "help" -> {
                helpInfo.printHelpInfoForInputForChooseSheetInSpreadsheets();
                yield inputForChooseSheetInSpreadsheets(spreadsheetIds);
            }
            case "back" -> chooseActionForSpreadsheet(true, spreadsheetIds);
            default -> {
                System.out.printf("%sЭто действие недоступно\n", TextColour.WARN);
                yield inputForChooseSheetInSpreadsheets(spreadsheetIds);
            }
        };
    }
    private void chooseMonthConditionForNewSpreadsheet(List<String> spreadsheetIds, boolean forAll){
        String forList;
        if(forAll)
            forList = "новые листы";
        else
            forList = "новый лист";
        System.out.printf("""
                ============================================================
                Выберите за какой месяц создать %s:
                
                1. Текущий (%s)
                2. Следующий (%s)
                ============================================================
                """, forList, dateCompute.getMonthAndYear(MonthCondition.THIS_MONTH),
                dateCompute.getMonthAndYear(MonthCondition.NEXT_MONTH));
        try {
            inputForChooseMonthConditionForNewSpreadsheet(spreadsheetIds, forAll);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private boolean inputForChooseMonthConditionForNewSpreadsheet(List<String> spreadsheetIds, boolean forAll)
            throws IOException {
        String input = parseInput();
        return switch (input) {
            case "1" -> {
                System.out.println("============================================================");
                sheetsFormatter.createNewFormattedSheetInSpreadsheet(spreadsheetIds, MonthCondition.THIS_MONTH);
                yield greet(false);
            }
            case "2" -> {
                System.out.println("============================================================");
                sheetsFormatter.createNewFormattedSheetInSpreadsheet(spreadsheetIds, MonthCondition.NEXT_MONTH);
                yield greet(false);
            }
            case "help" -> {
                helpInfo.printHelpInfoForInputForChooseMonthConditionForNewSpreadsheet(forAll);
                yield inputForChooseMonthConditionForNewSpreadsheet(spreadsheetIds, forAll);
            }
            case "back" -> chooseActionForSpreadsheet(forAll, spreadsheetIds);
            default -> {
                System.out.printf("%sЭто действие недоступно\n", TextColour.WARN);
                yield inputForChooseMonthConditionForNewSpreadsheet(spreadsheetIds, forAll);
            }
        };
    }
    private void chooseSheetInSingleSpreadsheet(String spreadsheetId) {
        System.out.println("""
                ============================================================
                Выберите лист для текущей таблицы из списка:
                """);

        Spreadsheet spreadsheet;
        try {
            spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<Sheet> sheets = spreadsheet.getSheets();
        int sheetCount = 1;
        for(Sheet sheet: sheets)
            System.out.println(sheetCount++ + ". "  + sheet.getProperties().getTitle());
        System.out.println("============================================================");
        inputForChooseSheet(spreadsheetId, sheets);
    }
    private boolean inputForChooseSheet(String spreadsheetId, List<Sheet> sheets){
        String input = parseInput();
        if(input.equals("back"))
            return chooseActionForSpreadsheet(false, new ArrayList<>(){{add(spreadsheetId);}});
        else if (input.equals("help")) {
            helpInfo.printHelpInfoForInputForChooseSheet();
            return inputForChooseSheet(spreadsheetId, sheets);
        } else if (input.equals("changedir")) {
            System.out.printf("%sЭто действие недоступно\n", TextColour.WARN);
            return inputForChooseSheet(spreadsheetId, sheets);
        } else if (Integer.parseInt(input) > 0 && Integer.parseInt(input) <= sheets.size()) {
            try {
                salaryCalculator.calculateSalaryForSpecificMonth(
                        new ArrayList<>(){{
                            add(spreadsheetId);
                        }}, new ArrayList<>(){{add(sheets.get(Integer.parseInt(input) - 1));}});
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return greet(false);
        } else{
            System.out.printf("%sНе могу выбрать лист под этим номером\n", TextColour.WARN);
            return inputForChooseSheet(spreadsheetId, sheets);
        }
    }
    protected boolean setNewSpreadsheetName(boolean firstLaunch){
        StringBuilder stName = new StringBuilder();
        String schedule = "График работы сотрудников ул. ";
        if(firstLaunch)
            System.out.printf("""
                ============================================================
                Введите название и номер улицы ПВЗ
                %s""", schedule);
        else
            System.out.print(schedule);

        stName.append(schedule).append(new Scanner(System.in).nextLine());
        System.out.println("============================================================");
        try {
            if(!sheetsFormatter.createNewSpreadsheet(stName.toString()))
                return setNewSpreadsheetName(false);
            else return greet(false);
        }catch (GeneralSecurityException | IOException e){
            throw new RuntimeException("Ваш токен доступа был отозван, свяжитесь с разработчиком");
        }
    }

    protected boolean selectSpreadsheet(boolean forAll)
            throws GeneralSecurityException, IOException {

        List<String> spreadsheetIds = fileCreator.getDirectoryDataMap(
                fileCreator.getDirectoryFileList(
                        setup.getSchedulesDirectoryId()))
                .values()
                .stream()
                .toList();

        if(!forAll) {
            try {
                sheetsFormatter.showListOfSpreadsheets();
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
            if (spreadsheetIds.isEmpty()) {
                System.out.printf("""
                        %sТаблиц не найдено :|
                        Попробуйте создать новую таблицу, указав название и номер улицы
                        """, TextColour.INFO);
                setNewSpreadsheetName(false);
                return selectSpreadsheet(false);
            } else if (spreadsheetIds.size() == 1) {
                chooseActionForSpreadsheet(false, spreadsheetIds);
            } else {
                inputSelectSpreadsheet(spreadsheetIds);
            }
        } else{
            if(spreadsheetIds.isEmpty()){
                System.out.printf("""
                        %sТаблиц не найдено :|
                        Попробуйте создать новую таблицу, указав название и номер улицы
                        """, TextColour.INFO);
                setNewSpreadsheetName(false);
            } else
                chooseActionForSpreadsheet(true, spreadsheetIds);
        }
        return true;
    }

    private boolean inputSelectSpreadsheet(List<String> spreadsheetIds){
        String selectedSpreadsheet = parseInput();
        if(selectedSpreadsheet.matches("help")){
            helpInfo.printHelpInfoForInputSelectSpreadsheet();
            return inputSelectSpreadsheet(spreadsheetIds);
        } else if(selectedSpreadsheet.matches("back")){
            return greet(false);
        } else {
            try {
                Integer.parseInt(selectedSpreadsheet);
            } catch (NumberFormatException e) {
                System.out.printf("%sЭто действие недоступно\n", TextColour.WARN);
                return inputSelectSpreadsheet(spreadsheetIds);
            }
            int selectedSpreadsheetAsInt = Integer.parseInt(selectedSpreadsheet);

            if (selectedSpreadsheetAsInt > spreadsheetIds.size() || selectedSpreadsheetAsInt <= 0) {
                System.out.printf("%sТаблицы с таким номером не существует\n", TextColour.WARN);
                return inputSelectSpreadsheet(spreadsheetIds);
            } else {
                String id = spreadsheetIds.get(selectedSpreadsheetAsInt - 1);
                chooseActionForSpreadsheet(false, new ArrayList<>() {{
                    add(id);
                }});
            }
            return false;
        }
    }
}
