package com.tistory.workshop6349.basicbot.config;

import bwapi.Player;
import com.tistory.workshop6349.basicbot.BasicBotModule;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class Common {

    public static Player Self() {
        return BasicBotModule.BroodWar.self();
    }

    public static Player Enemy() {
        return BasicBotModule.BroodWar.enemy();
    }

    public static int Time() {
        return BasicBotModule.BroodWar.getFrameCount();
    }

    /// 로그 유틸
    public static void appendTextToFile(final String logFile, final String msg) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(logFile, true));
            bos.write(msg.getBytes());
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /// 로그 유틸
    public static void overwriteToFile(final String logFile, final String msg) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(logFile));
            bos.write(msg.getBytes());
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /// 파일 유틸 - 텍스트 파일을 읽어들인다
    public static String readFile(final String filename) {
        BufferedInputStream bis;
        StringBuilder sb = new StringBuilder();
        try {
            bis = new BufferedInputStream(new FileInputStream(filename));

            while (bis.available() > 0) {
                sb.append((char) bis.read());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
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
