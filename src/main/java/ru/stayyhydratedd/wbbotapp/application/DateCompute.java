package ru.stayyhydratedd.wbbotapp.application;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class DateCompute {
    protected final List<Integer> currentDate = Arrays.stream(new SimpleDateFormat("yyyy:M")
            .format(new Date())
            .split(":")).map(Integer::parseInt).toList();

    protected int getLengthOfMonth(MonthCondition monthCondition){
        if(monthCondition == MonthCondition.NEXT_MONTH){
            if(currentDate.getLast() == 12)
                return YearMonth.of(currentDate.getFirst() + 1, 1).lengthOfMonth();
            else
                return YearMonth.of(currentDate.getFirst(), currentDate.getLast() + 1).lengthOfMonth();
        } else if(monthCondition == MonthCondition.PREV_MONTH) {
            if (currentDate.getLast() == 1)
                return YearMonth.of(currentDate.getFirst() - 1, 12).lengthOfMonth();
            else
                return YearMonth.of(currentDate.getFirst(), currentDate.getLast() - 1).lengthOfMonth();
        } else
            return YearMonth.of(currentDate.getFirst(), currentDate.getLast()).lengthOfMonth();
    }
    //    argument date example: октябрь 26; июнь 25
    protected static int getLengthOfMonth(String date){
        List<String> dateArr = Arrays.stream(date.split(" ")).map(String::toLowerCase).toList();
        Map<String, Integer> months = new HashMap<>(){{
            put("январь", 1); put("февраль", 2); put("март", 3); put("апрель", 4); put("май", 5); put("июнь", 6);
            put("июль", 7); put("август", 8); put("сентябрь", 9); put("октябрь", 10); put("ноябрь", 11); put("декабрь", 12);
        }};
        return YearMonth.of(Integer.parseInt(dateArr.get(1)) + 2000, months.get(dateArr.get(0))).lengthOfMonth();
    }

    protected List<String> getWeekdaysList(MonthCondition monthCondition){
        List<String> weekdaysArr = new ArrayList<>();

        StringBuilder yearMonth = new StringBuilder();
        if(monthCondition == MonthCondition.NEXT_MONTH){
            if(currentDate.getLast() == 12)
                yearMonth.append(currentDate.getFirst() + 1)
                        .append("-")
                        .append(1)
                        .append("-");
            else
                yearMonth.append(currentDate.getFirst())
                        .append("-")
                        .append(currentDate.getLast() + 1)
                        .append("-");
        } else if(monthCondition == MonthCondition.PREV_MONTH){
            if(currentDate.getLast() == 1)
                yearMonth.append(currentDate.getFirst() - 1)
                        .append("-")
                        .append(12)
                        .append("-");
            else
                yearMonth.append(currentDate.getFirst())
                        .append("-")
                        .append(currentDate.getLast() - 1)
                        .append("-");
        } else{
            yearMonth.append(currentDate.getFirst())
                    .append("-")
                    .append(currentDate.getLast())
                    .append("-");
        }
        StringBuilder yearMonthDay = new StringBuilder();

        DateTimeFormatter dtfInput = DateTimeFormatter.ofPattern("u-M-d"); //pattern example: 2024-9-18
        for(int day = 1; day <= getLengthOfMonth(monthCondition); day++){
            yearMonthDay.append(yearMonth).append(day);
            weekdaysArr.add(LocalDate.parse(yearMonthDay, dtfInput)
                    .getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("ru")));
            yearMonthDay.setLength(0);
        }
        return weekdaysArr;
    }
    protected String getMonthAndYear(MonthCondition monthCondition){
        List<Integer> currentYearAndMonthList = new ArrayList<>(){{
            add(Integer.parseInt(currentDate.getFirst().toString().substring(2)));
            add(currentDate.getLast());
        }};
        StringBuilder sheetName = new StringBuilder();
        if(monthCondition == MonthCondition.NEXT_MONTH){
            if(currentYearAndMonthList.getLast() == 12)
                sheetName.append(Month.of(1)
                                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru")))
                        .append(" ").append(currentYearAndMonthList.getFirst() + 1);
            else
                sheetName.append(Month.of(currentYearAndMonthList.getLast() + 1)
                                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru")))
                        .append(" ").append(currentYearAndMonthList.getFirst());
        } else if(monthCondition == MonthCondition.PREV_MONTH){
            if(currentYearAndMonthList.getLast() == 1)
                sheetName.append(Month.of(12)
                                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru")))
                        .append(" ").append(currentYearAndMonthList.getFirst() - 1);
            else
                sheetName.append(Month.of(currentYearAndMonthList.getLast() - 1)
                                .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru")))
                        .append(" ").append(currentYearAndMonthList.getFirst());
        } else
            sheetName.append(Month.of(currentYearAndMonthList.getLast())
                            .getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("ru")))
                    .append(" ").append(currentYearAndMonthList.getFirst());

        return sheetName.toString();
    }
    protected int getMonthAndYearForId(MonthCondition monthCondition){
        String date;
        List<String> currentDateList = Arrays.stream(new SimpleDateFormat("yy:MM")
                .format(new Date())
                .split(":")).toList();
        if(monthCondition == MonthCondition.NEXT_MONTH){
            if(currentDate.getLast() == 12)
                date = Integer.parseInt(currentDateList.getFirst()) + 1 + "01";
            else{
                date = currentDateList.getFirst() + currentDateList.getLast();
                return Integer.parseInt(date) + 1;
            }
        } else if (monthCondition == MonthCondition.PREV_MONTH) {
            if(currentDate.getLast() == 1)
                date = Integer.parseInt(currentDateList.getFirst()) - 1 + "12";
            else{
                date = currentDateList.getFirst() + currentDateList.getLast();
                return Integer.parseInt(date) - 1;
            }
        } else
            date = currentDateList.getFirst() + currentDateList.getLast();
        return Integer.parseInt(date);
    }
}
