package ru.stayyhydratedd.wbbotapp.application;

import java.util.*;

public class SomeAlgorithms {

    //abdArr.size() == 26
    private final String[] abcArr = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    protected String getCellsRange(int colNumFrom, int rowNumFrom,
                                   int colNumTill, int rowNumTill) {

        if (colNumFrom > colNumTill || rowNumFrom > rowNumTill) {
            throw new RuntimeException("First column or row number argument is bigger " +
                    "than the second");
        }

        StringBuilder sbResultRange = new StringBuilder();

        appendCells(sbResultRange, colNumFrom, rowNumFrom, true);
        appendCells(sbResultRange, colNumTill, rowNumTill, false);

        return sbResultRange.toString();
    }

    protected String getCell(int colNum, int rowNum){
        StringBuilder sbResult = new StringBuilder();
        appendCells(sbResult, colNum, rowNum, false);
        return sbResult.toString();
    }
    private void appendCells(StringBuilder sbResult, int colNum, int rowNum, boolean isFrom){

        int countSymbolA;

        if(colNum <= 26)
            sbResult.append(abcArr[colNum - 1]).append(rowNum);
        else{
            if(colNum % 26 == 0){
                countSymbolA = colNum / 26 - 1;
                sbResult.append(abcArr[0].repeat(countSymbolA)).append(abcArr[25])
                        .append(rowNum);
            } else {
                countSymbolA = colNum / 26;
                sbResult.append(abcArr[0].repeat(countSymbolA))
                        .append(abcArr[colNum % 26 - 1]).append(rowNum);
            }
        }
        if(isFrom)
            sbResult.append(":");
    }
    //    argument name example: Попова 12
    public int generateIdFromName(String name){
        List<char[]> listChars = Arrays.stream(name.split(" ")).map(String::toCharArray).toList();
        StringBuilder sb = new StringBuilder();
        for(int i = listChars.size() - 1; i >= 0; i--){
            for(int j = listChars.get(i).length - 1; j >= 0; j--){
                if(Integer.toString(listChars.get(i)[j]).length() >= 3){
                    sb.append(Integer.toString(listChars.get(i)[j]).substring(1));
                } else{
                    sb.append(Integer.toString(listChars.get(i)[j]).charAt(1));
                }
            }
        }
        if(sb.toString().length() > 9)
            return Integer.parseInt(sb.substring(0, 9));
        else {
            return Integer.parseInt(sb.toString());
        }
    }

    public void printSequenceFromString(String message) {
        int outputDelay = 15;
        for (char c : message.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(outputDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected int calculateWorkingTimeInCell(String time, String cell){
        if(!time.matches("\\d{2}:\\d{2}[-–—]\\d{2}:\\d{2}((;\\d{2}:\\d{2}[-–—]\\d{2}:\\d{2})+)?")){
            System.out.printf("%sОшибка в ячейке '%s' (запись времени не соответствует паттерну, " +
                            "время из ячейки учтено не будет)\n",
                    TextColour.ERROR, TextColour.turnTextIntoColor(cell, TextColour.COLORS.ERROR));
            return 0;
        } else{
            int minResult = 0;
            //08:00-15:30
            List<String> timeRanges = Arrays.stream(time.split(";")).toList();
            for(String timeRange: timeRanges){
                //{480,930}, first - from min value, second - till min value
                List<Integer> totalMinValues = new ArrayList<>();
                //{08:00,15:30}, first - from, second - till
                List<String> hoursAndMinValues = new ArrayList<>(Arrays.asList(timeRange.split("[-–—]")));
                for(String value: hoursAndMinValues){
                    //{08,00}, first - hours, second - minutes
                    List<Integer> hoursAndMin = new ArrayList<>(
                            Arrays.stream(value.split(":")).map(Integer::parseInt).toList());
                    if(hoursAndMin.getFirst() > 23){
                        System.out.printf("%sОшибка в ячейке '%s' (значение часов >23, " +
                                        "время из ячейки учтено не будет)\n",
                                TextColour.ERROR, TextColour.turnTextIntoColor(cell, TextColour.COLORS.ERROR));
                        return 0;
                    } else if(hoursAndMin.getLast() > 59){
                        System.out.printf("%sОшибка в ячейке '%s' (значение минут >59, " +
                                        "время из ячейки учтено не будет)\n",
                                TextColour.ERROR, TextColour.turnTextIntoColor(cell, TextColour.COLORS.ERROR));
                        return 0;
                    } else{
                        int firstMinValue = hoursAndMin.getFirst() * 60;
                        int lastMinValue = hoursAndMin.getLast();
                        int totalMinValue = firstMinValue + lastMinValue;
                        totalMinValues.add(totalMinValue);
                    }
                }
                int difInMinutes;
                if(totalMinValues.getLast() > totalMinValues.getFirst()){
                    difInMinutes = totalMinValues.getLast() - totalMinValues.getFirst();
                } else {
                    difInMinutes = 24 * 60 + totalMinValues.getLast() - totalMinValues.getFirst();
                    if(difInMinutes > 15 * 60)
                        System.out.printf("%sВремя работы в ячейке '%s' превышает 15 часов, проверьте ее на корректность\n",
                                TextColour.WARN, TextColour.turnTextIntoColor(cell, TextColour.COLORS.WARN));
                }
                minResult += difInMinutes;
            }
            return minResult;
        }
    }
}