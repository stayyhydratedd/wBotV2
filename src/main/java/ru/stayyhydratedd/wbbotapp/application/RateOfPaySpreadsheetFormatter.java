package ru.stayyhydratedd.wbbotapp.application;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import ru.stayyhydratedd.wbbotapp.application.services.GoogleServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RateOfPaySpreadsheetFormatter {
    private final SomeAlgorithms someAlgorithms;
    private final Sheets sheetsService;

    public RateOfPaySpreadsheetFormatter(SomeAlgorithms someAlgorithms, GoogleServices googleServices) {
        this.someAlgorithms = someAlgorithms;
        this.sheetsService = googleServices.getSheetsService();
    }

    protected void formatingRateOfPayFile(String rateOfPayFileId, String sheetTitle, boolean exampleSheet) {

        GridProperties gridProperties = new GridProperties();
        gridProperties.setFrozenColumnCount(1).setFrozenRowCount(1);

        String clearSheetTitle = sheetTitle;

        if (exampleSheet)
            gridProperties.setColumnCount(4).setRowCount(15);
        else {
            gridProperties.setColumnCount(2).setRowCount(15);
            clearSheetTitle = sheetTitle.substring(30);
        }

        List<Request> requestsForAll = Arrays.asList(
                new Request().setAddSheet(
                        new AddSheetRequest().setProperties(
                                new SheetProperties()
                                        .setGridProperties(gridProperties)
                                        .setTitle(clearSheetTitle)
                                        .setSheetId(someAlgorithms.generateIdFromName(clearSheetTitle))
                        )
                ),
                new Request().setRepeatCell(
                        new RepeatCellRequest().setCell(
                                new CellData().setUserEnteredFormat(
                                        new CellFormat().setTextFormat(
                                                new TextFormat().setFontSize(11)
                                        )
                                )
                        ).setRange(
                                new GridRange()
                                        .setSheetId(someAlgorithms.generateIdFromName(clearSheetTitle))
                                        .setStartRowIndex(0)
                                        .setStartColumnIndex(0)
                                        .setEndRowIndex(16)
                                        .setEndColumnIndex(1)
                        ).setFields("*")
                ),
                new Request().setRepeatCell(
                        new RepeatCellRequest().setCell(
                                new CellData().setUserEnteredFormat(
                                        new CellFormat().setTextFormat(
                                                new TextFormat().setFontSize(11)
                                        ).setHorizontalAlignment("right")
                                )
                        ).setRange(
                                new GridRange()
                                        .setSheetId(someAlgorithms.generateIdFromName(clearSheetTitle))
                                        .setStartRowIndex(0)
                                        .setStartColumnIndex(1)
                                        .setEndRowIndex(16)
                                        .setEndColumnIndex(2)
                        ).setFields("*")
                )
        );

        List<Request> requestsForExample = Arrays.asList(
                new Request().setDeleteSheet(
                        new DeleteSheetRequest().setSheetId(0)
                ),
                new Request().setMergeCells(    //объединение ячеек в первой строке четыре столбца
                        new MergeCellsRequest().setRange(
                                new GridRange()
                                        .setStartRowIndex(0)
                                        .setStartColumnIndex(2)
                                        .setEndRowIndex(1)
                                        .setEndColumnIndex(4)
                                        .setSheetId(someAlgorithms.generateIdFromName(clearSheetTitle))
                        )
                ),
                new Request().setMergeCells(    //объединение ячеек в нижних строках четыре столбца
                        new MergeCellsRequest().setRange(
                                new GridRange()
                                        .setStartRowIndex(1)
                                        .setStartColumnIndex(2)
                                        .setEndRowIndex(16)
                                        .setEndColumnIndex(4)
                                        .setSheetId(someAlgorithms.generateIdFromName(clearSheetTitle))
                        )
                ),
                new Request().setRepeatCell(
                        new RepeatCellRequest().setCell(
                                new CellData().setUserEnteredFormat(
                                        new CellFormat().setTextFormat(
                                                new TextFormat().setFontSize(11)
                                        ).setVerticalAlignment("top")
                                )
                        ).setRange(
                                new GridRange()
                                        .setSheetId(someAlgorithms.generateIdFromName(clearSheetTitle))
                                        .setStartRowIndex(0)
                                        .setStartColumnIndex(2)
                                        .setEndRowIndex(16)
                                        .setEndColumnIndex(4)
                        ).setFields("*")
                )
        );
        try {
            BatchUpdateSpreadsheetRequest bodyForAll =
                    new BatchUpdateSpreadsheetRequest()
                            .setRequests(requestsForAll);
            sheetsService.spreadsheets()
                    .batchUpdate(rateOfPayFileId, bodyForAll)
                    .execute();
            if (exampleSheet) {
                BatchUpdateSpreadsheetRequest bodyForExample =
                        new BatchUpdateSpreadsheetRequest()
                                .setRequests(requestsForExample);
                sheetsService.spreadsheets()
                        .batchUpdate(rateOfPayFileId, bodyForExample)
                        .execute();

                System.out.printf("%sЛист %s был успешно создан в таблице ставок!\n",
                        TextColour.SUCCESS,
                        TextColour.turnTextIntoColor("example", TextColour.COLORS.SUCCESS));

                formatConfigSheet(
                        clearSheetTitle + "!" +
                                someAlgorithms.getCellsRange(1, 1, 3, 16),
                        rateOfPayFileId, true
                );
            } else {
                System.out.printf("%sЛист ставок '%s' для таблицы '%s' был успешно создан в таблице ставок!\n",
                        TextColour.SUCCESS,
                        TextColour.turnTextIntoColor(clearSheetTitle, TextColour.COLORS.SUCCESS),
                        TextColour.turnTextIntoColor(sheetTitle, TextColour.COLORS.SUCCESS));

                formatConfigSheet(
                        clearSheetTitle + "!" +
                                someAlgorithms.getCellsRange(1, 1, 2, 1),
                        rateOfPayFileId, false
                );
            }
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf(TextColour.ERROR + "Таблица ставок '%s' не была найдена\n", rateOfPayFileId);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void formatConfigSheet(String range, String rateOfPayFileId, boolean exampleSheet)
            throws IOException {

        List<List<Object>> values = getLists(exampleSheet);

        UpdateValuesResponse result;
        try {
            ValueRange body = new ValueRange()
                    .setValues(values);
            result = sheetsService.spreadsheets().values().update(rateOfPayFileId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            if(!exampleSheet)
                System.out.printf("""
                     %sЯчеек (%s) обновлено в листе ставок
                     """, TextColour.INFO,
                     TextColour.turnTextIntoColor(Integer.toString(result.getUpdatedCells()), TextColour.COLORS.INFO));
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf(TextColour.ERROR + "Таблица ставок '%s' не была найдена\n", rateOfPayFileId);
            } else {
                throw e;
            }
        }
    }

    private static List<List<Object>> getLists(boolean exampleSheet) {

        List<List<Object>> values;

        String tabs = "\t".repeat(6);

        String rateOfPayInfo = String.format("""
                Кликни дважды по ячейке,
                чтобы увидеть информацию
                %s Для каждой созданной таблицы, будет автоматически создаваться подобный этому лист ставок.
                %s Он нужен только для того, чтобы удобно было посчитывать зарплату для каждого сотрудника.
                %s К примеру, кто-то из сотрудников имеет повышенную ставку в час, это можно указать в этой таблице:
                %s 1. Для созданной уже таблицы графика работы выберите соответствующий ей лист ставок.
                %s 2. Далее в колонке с именем впишите имя сотрудника, которое используется в таблице графика.
                %s 3. В колонке ставки впишите числом его ставку в час.
                %s При подсчете заработной платы, программа будет в первую очередь пытаться взять значение отсюда.
                %s Если в таблицу ставок ничего не записывать, программа будет брать значение ставки по умолчанию
                %s для всех незаписанных сотрудников.
                """, tabs, tabs, tabs, tabs, tabs, tabs, tabs, tabs, tabs);

        if(exampleSheet){
            values = new ArrayList<>(){{
                add(new ArrayList<>(){{
                    add("Имя");
                    add("Ставка");
                    add("Информация о ставках");
                }});
                add(new ArrayList<>(){{
                    add("Имя1");
                    add("115");
                    add(rateOfPayInfo);
                }});
                add(new ArrayList<>(){{
                    add("Имя2");
                    add("115");
                }});
                add(new ArrayList<>(){{
                    add("Имя3");
                    add("120");
                }});
            }};
        } else{
             values = new ArrayList<>(){{
                add(new ArrayList<>(){{
                    add("Имя");
                    add("Ставка");
                }});
            }};
        }
        return values;
    }
}