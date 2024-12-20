package ru.stayyhydratedd.wbbotapp.application;

import com.diogonunes.jcolor.AnsiFormat;
import com.diogonunes.jcolor.Attribute;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;

import com.sun.jna.Function;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinDef.DWORDByReference;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public class TextColour {
    private static final int ERROR_COLOR = 9;
    private static final int WARN_COLOR = 221;
    private static final int INFO_COLOR = 75;
    private static final int SUCCESS_COLOR = 41;
    private static final int IN_PROCESS_COLOR = 213;

    public static String ERROR = "[" + colorize("ERROR", TEXT_COLOR(ERROR_COLOR)) + "] ";
    public static String WARN = "[" + colorize("WARN", TEXT_COLOR(WARN_COLOR)) + "] ";
    public static String INFO = "[" + colorize("INFO", TEXT_COLOR(INFO_COLOR)) + "] ";
    public static String SUCCESS = "[" + colorize("SUCCESS", TEXT_COLOR(SUCCESS_COLOR)) + "] ";
    public static String IN_PROCESS = "[" + colorize("IN_PROCESS", TEXT_COLOR(IN_PROCESS_COLOR)) + "] ";

    public enum COLORS {
        ERROR, WARN, INFO, SUCCESS, IN_PROCESS
    }

    public static String turnTextIntoColor(String text, COLORS color){
        if(color == COLORS.ERROR){
            return colorize(text, TEXT_COLOR(ERROR_COLOR));
        } else if (color == COLORS.WARN) {
            return colorize(text, TEXT_COLOR(WARN_COLOR));
        } else if (color == COLORS.INFO) {
            return colorize(text, TEXT_COLOR(INFO_COLOR));
        } else if (color == COLORS.SUCCESS) {
            return colorize(text, TEXT_COLOR(SUCCESS_COLOR));
        } else {
            return colorize(text, TEXT_COLOR(IN_PROCESS_COLOR));
        }
    }

    public static void printColor() {
        // Use Case 1: use Ansi.colorize() to format inline
        System.out.println(colorize("This text will be yellow on magenta", YELLOW_TEXT(), MAGENTA_BACK()));
        System.out.println("\n");

// Use Case 2: compose Attributes to create your desired format
        Attribute[] myFormat = new Attribute[]{RED_TEXT(), YELLOW_BACK(), BOLD()};
        System.out.println(colorize("This text will be red on yellow", myFormat));
        System.out.println("\n");

// Use Case 3: AnsiFormat is syntactic sugar for an array of Attributes
        AnsiFormat fWarning = new AnsiFormat(GREEN_TEXT(), BLUE_BACK(), BOLD());
        System.out.println(colorize("AnsiFormat is just a pretty way to declare formats", fWarning));
        System.out.println(fWarning.format("...and use those formats without calling colorize() directly"));
        System.out.println("\n");

// Use Case 4: you can define your formats and use them throughout your code
        AnsiFormat fInfo = new AnsiFormat(CYAN_TEXT());
        AnsiFormat fError = new AnsiFormat(YELLOW_TEXT(), RED_BACK());
        System.out.println(fInfo.format("This info message will be cyan"));
        System.out.println("This normal message will not be formatted");
        System.out.println(fError.format("This error message will be yellow on red"));
        System.out.println("\n");

// Use Case 5: we support bright colors
        AnsiFormat fNormal = new AnsiFormat(MAGENTA_BACK(), YELLOW_TEXT());
        AnsiFormat fBright = new AnsiFormat(BRIGHT_MAGENTA_BACK(), BRIGHT_YELLOW_TEXT());
        System.out.println(fNormal.format("You can use normal colors ") + fBright.format(" and bright colors too"));

// Use Case 6: we support 8-bit colors
        System.out.println("Any 8-bit color (0-255), as long as your terminal supports it:");
        for (int i = 0; i <= 255; i++) {
            Attribute txtColor = TEXT_COLOR(i);
            System.out.print(colorize(String.format("%4d", i), txtColor));
        }
        System.out.println("\n");

// Credits
        System.out.print("This example used JColor 5.0.0   ");
        System.out.print(colorize("\tMADE ", BOLD(), BRIGHT_YELLOW_TEXT(), GREEN_BACK()));
        System.out.println(colorize("IN PORTUGAL\t", BOLD(), BRIGHT_YELLOW_TEXT(), RED_BACK()));
        System.out.println("I hope you find it useful ;)");
    }

    public static void enableWindows10AnsiSupport() {
        Function GetStdHandleFunc = Function.getFunction("kernel32", "GetStdHandle");
        DWORD STD_OUTPUT_HANDLE = new DWORD(-11);
        HANDLE hOut = (HANDLE) GetStdHandleFunc.invoke(HANDLE.class, new Object[]{STD_OUTPUT_HANDLE});

        DWORDByReference p_dwMode = new DWORDByReference(new DWORD(0));
        Function GetConsoleModeFunc = Function.getFunction("kernel32", "GetConsoleMode");
        GetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, p_dwMode});

        int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
        DWORD dwMode = p_dwMode.getValue();
        dwMode.setValue(dwMode.intValue() | ENABLE_VIRTUAL_TERMINAL_PROCESSING);
        Function SetConsoleModeFunc = Function.getFunction("kernel32", "SetConsoleMode");
        SetConsoleModeFunc.invoke(BOOL.class, new Object[]{hOut, dwMode});
    }

    public static void printLogo(){
        int brightColor = 41;
        int shadyColor = 35;

        Attribute bright = TEXT_COLOR(brightColor);
        Attribute shady = TEXT_COLOR(shadyColor);

        String logo = """
                /**
                * .........:.......█████..........:....█████...
                * ....:...:.......░░███.....:.........░░███....
                * .█████.███.█████.░███████...██████..███████..
                * ░░███.░███░░███..░███░░███.███░░███░░░███░...
                * .░███.░███.░███..░███.░███░███.░███..░███....
                * .░░███████████.:.░███.░███░███.░███..░███.███
                * ..░░████░████....████████.░░██████...░░█████.
                * ...░░░░.░░░░..:.░░░░░░░░...░░░░░░.....░░░░░..
                * ...:................................:........
                * .............:........:████████:.............
                * ..:..................:███░░░░███:......:.....
                * ..........█████.█████░░░:..:░███:.........:..
                * .........░░███.░░███...:███████:.............
                * ......:...░███..░███..:███░░░░:....:.........
                * ..........░░███.███..:███:....:█:............
                * ...........░░█████..:░██████████:....:.......
                * .....:......░░░░░...:░░░░░░░░░░:..........:..
                * ..........:...........made.by.stayyhydratedd.
                */
                """;

        logo.lines().map(line -> {
            StringBuilder sb = new StringBuilder();
            for(char c : line.toCharArray()){
                String s = Character.toString(c);
                if(s.matches("░"))
                    sb.append(colorize(s, shady));
                else
                    sb.append(colorize(s, bright));
            }
            return sb.toString();
        }).forEach(line -> {
            System.out.println(line);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void main(String[] args) {
        printLogo();
    }
}
