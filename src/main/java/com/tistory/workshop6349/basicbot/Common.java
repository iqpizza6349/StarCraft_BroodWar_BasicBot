package com.tistory.workshop6349.basicbot;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class Common {

    public static void appendTextToFile(String logFile, String msg) {
        try {
            PrintWriter logWriter = new PrintWriter(new FileWriter(logFile, true));
            logWriter.println(msg);
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void overWriteToFile(String logFile, String msg) {
        try {
            PrintWriter logWriter = new PrintWriter(logFile);
            logWriter.println(msg);
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void makeDirectory(String fullPath) {
        if (fullPath == null) {
            return;
        }

        File folder = new File(fullPath);

        if (!folder.exists()) {
            try {
                folder.mkdir();
                System.out.println("디렉토리 생성");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.err.println("이미 디렉토리가 있습니다");
        }
    }

    public static String readFile(String fileName) {
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                result.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    public static void readResults() {
        String enemyName = BasicBotModule.BroodWar.enemy().getName();
        enemyName = enemyName.replace(" ", "_");

        String enemyResultsFile = Config.readDirectory + enemyName + ".txt";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(enemyResultsFile));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeResults() {
        String enemyName = BasicBotModule.BroodWar.enemy().getName();
        enemyName = enemyName.replace(" ", "_");

        String enemyResultsFile = Config.readDirectory + enemyName + ".txt";

        String s = "";

//        int wins = 1;
//        int losses = 0;
//        s = wins + " " + losses;

        overWriteToFile(enemyResultsFile, s);
    }

    public static String getDateTimeOfNow() {
        return LocalDate.now().getYear() + "-"
                + LocalDate.now().getMonth() + "-"
                + LocalDate.now().getDayOfMonth() + "-"
                + LocalTime.now().getHour() + "-"
                + LocalTime.now().getMinute() + "-"
                + LocalTime.now().getSecond();
    }

    public static void pause(int ms) {
        BasicBotModule.BroodWar.pauseGame();
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BasicBotModule.BroodWar.resumeGame();
    }

}
