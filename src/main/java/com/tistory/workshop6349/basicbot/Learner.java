package com.tistory.workshop6349.basicbot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Learner {

    public static int chooseStrategy() throws IOException {
        EnemyManager.getInstance().name = writeName();
        EnemyManager.getInstance().history = filterResults(ReadWrite.read_learning_data(EnemyManager.getInstance().name));
        countResults();
        EnemyManager.getInstance().score = writeScore();
        return analyze_results_random();
    }

    public static ArrayList<String> filterResults(ArrayList<String> myData) {
        if (!myData.isEmpty() && !myData.contains("unfiltered")) {
            myData.remove(myData.size()-1);
            ArrayList<String> filteredData = new ArrayList<>();
            String newLine;
            for (String myLine : myData) {
                ArrayList<String> s = splitLine(myLine);
                if (s.size() == 5 && s.get(1).equals(EnemyManager.getInstance().name)) {
                    newLine = s.get(0) + "," + s.get(2) + "," + s.get(3) + "," + s.get(4);
                    filteredData.add(newLine);
                }
            }
            if (!filteredData.isEmpty()) {
                BasicBotAI.BroodWar.printf("filtered results by " + EnemyManager.getInstance().name);
            }
            else {
                BasicBotAI.BroodWar.printf("no results by " + EnemyManager.getInstance().name);
            }
            return filteredData;
        }
        return myData;
    }

    public static void countResults() {
        for (String myLine : EnemyManager.getInstance().history) {
            ArrayList<String> mySplit = splitLine(myLine);
            if (mySplit.size() == 4) {
                int e = 2 * Integer.parseInt(mySplit.get(2)) - Integer.parseInt(mySplit.get(3)) - 1;
                if (e >= 0 && e < (EnemyManager.getInstance().result.length) / 4) {
                    EnemyManager.getInstance().result[e]++;
                }
            }
        }
    }

    public static int analyze_results_random() {
        if (StateManager.getInstance().test_strategy != 0) {
            return StateManager.getInstance().test_strategy;
        }
        if (StateManager.getInstance().useHardcodedStrategies
                && check_using_hardcoded() != 0) {
            return check_using_hardcoded();
        }

        final int nStats = 4;
        double []wp = new double[nStats];
        double wpSum = 0.0;
        int []cp = new int[nStats];

        for (int i = 0; i < nStats; i++) {
            wp[i] = (Math.pow(1 + EnemyManager.getInstance().result[2*i], 2))
                    / (Math.pow(2 + EnemyManager.getInstance().result[2*i] + EnemyManager.getInstance().result[2*i+1], 2));
            wpSum += wp[i];
        }
        
        if (StateManager.getInstance().avoidWeakStrategies) {
            int max = 0;
            for (int i = 0; i < nStats; i++) {
                if (wp[i] > wp[max]) {
                    max = i;
                }
            }
            boolean standsOut = true;
            for (int i = 0; i < nStats; i++) {
                if (i != max
                        && wp[max] < 2 * wp[i]) {
                    standsOut = false;
                    break;
                }
            }
            if (standsOut) {
                System.out.println("Stand out Strategy " + max+1 + ", squared win percentage " + wp[max]);
                return max + 1;
            }
        }

        for (int i = 0; i < nStats; i++) {
            cp[i] = (int)((100.0 * wp[i]) / wpSum + 0.5);
            if (cp[i] == 0) {
                cp[i] = 1;
            }
            if (i > 0) {
                cp[i] += cp[i-1];
            }
        }

        Random random = new Random();
        int n = random.nextInt(32767) % 100;
        for (int i = 0; i < nStats; i++) {
            if (n < cp[i]) {
                return i + 1;
            }
        }
        return 1;
    }

    public static int check_using_hardcoded() {
        if (EnemyManager.getInstance().name.equalsIgnoreCase("Stardust")) {
            return 4;
        }
        if (EnemyManager.getInstance().name.equalsIgnoreCase("krasi0")) {
            return 3;
        }
        return 0;
    }

    public static String writeName() {
        int n = BasicBotAI.BroodWar.enemies().size();
        return (n == 1) ? BasicBotAI.BroodWar.enemy().getName() : "enemies";
    }

    public static String writeScore() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < EnemyManager.getInstance().result.length; i++) {
            s.append(EnemyManager.getInstance().result[i]);
            s.append((i % 2 == 0) ? "-" : " ");
        }
        return s.toString();
    }

    public static void addResultToHistory(boolean isWin) {
        String newLine = writeDate() + "," +
                EnemyManager.getInstance().race.toString().charAt(0) + "," +
                "0" + StateManager.getInstance().orig_strategy + "," +
                ((isWin) ? "1" : "0");
        EnemyManager.getInstance().history.add(newLine);
    }

    public static String writeDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        Date currentTime = new Date();
        return format.format(currentTime);
    }



    public static ArrayList<String> splitLine(String myLine) {
        String[] ss = myLine.split(",");
        return new ArrayList<>(Arrays.asList(ss));
    }

}
