package ru.stayyhydratedd.wbbotapp.application;

public class HelpInfo {
    private final String helpInfo = String.format(
                """
                ????????????????????????????????????????????????????????????
                %sСправочная информация:
                
                Вы можете ввести команду '%s' в любое время, чтобы вызвать актуальную справку
                Вы также можете ввести команду '%s', чтобы вернуться к предыдущему выбору
                
                """, TextColour.INFO, TextColour.turnTextIntoColor("/help", TextColour.COLORS.INFO),
            TextColour.turnTextIntoColor("/back", TextColour.COLORS.INFO));

    private final String afterHelpInfo =
            """
            
            *После вывода этой информации, вы так же должны выбрать последнее действие цифрой для продолжения работы
            ????????????????????????????????????????????????????????????""";

    protected void printHelpInfoForGreet(){
        System.out.println(helpInfo +
                """
                '1': Создание новой таблицы с указанным именем (с текущим месяцем по умолчанию)
                '2': Переход к выбору конкретной таблицы для взаимодействия с ней
                '3': Переход к взаимодействию со всеми таблицами одновременно
                """ + afterHelpInfo);
    }

    protected void printHelpInfoForInputSelectSpreadsheet(){
        System.out.println(helpInfo +
                """
                Выберите цифрой таблицу из списка для взаимодействия с ней
                """ + afterHelpInfo);
    }

    protected void printHelpInfoForChooseActionForSpreadsheet(boolean forAll){
        String info;
        if(forAll)
            info = """
                   '1': Создание новых отформатированных листов (нового месяца) для всех таблиц
                   '2': Переход к выбору текущего или предыдущего месяца для высчитывания зарплаты у сотрудников
                   """;
        else
            info = """
                   '1': Создание нового отформатированного листа (нового месяца) для выбранной таблицы
                   '2': Переход к выбору конкретного месяца для подсчитывания зарплаты у сотрудников
                   """;
        System.out.println(helpInfo + info + afterHelpInfo);
    }

    protected void printHelpInfoForInputForChooseMonthConditionForNewSpreadsheet(boolean forAll){
        String info;
        if(forAll)
            info = """
                   '1': Создание новых отформатированных листов за текущий месяц
                   '2': Создание новых отформатированных листов за следующий месяц
                   """;
        else
            info = """
                   '1': Создание нового отформатированного листа за текущий месяц
                   '2': Создание нового отформатированного листа за следующий месяц
                   """;
        System.out.println(helpInfo + info + afterHelpInfo);
    }

    protected void printHelpInfoForInputForChooseSheet(){
        System.out.println(helpInfo +
                """
                Выберите лист (месяц) для подсчета зарплаты у сотрудников
                p.s. После выполнения этой команды, обновится лист во выбранной ранее таблице за выбранный месяц,
                у каждого сотрудника напротив его имени в конце таблицы обновится значение количества
                отработанных часов и сгенерируется файл с названием выбранного месяца с подробной информацией в папке
                'Зарплаты сотрудников'
                """ + afterHelpInfo);
    }

    protected void printHelpInfoForInputForChooseSheetInSpreadsheets(){
        System.out.println(helpInfo +
                """
                '1': Посчитать зарплату во всех таблицах за текущий месяц
                '2': Посчитать зарплату во всех таблицах за прошлый месяц
                p.s. После выполнения этой команды, обновятся листы во всех таблицах за выбранный месяц,
                у каждого сотрудника напротив его имени в конце таблицы обновится значение количества
                отработанных часов и сгенерируются файлы с названием выбранного месяца с подробной информацией в папке
                'Зарплаты сотрудников'
                """ + afterHelpInfo);
    }
}
