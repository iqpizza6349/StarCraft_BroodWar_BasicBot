package com.tistory.workshop6349.basicbot;

import java.util.ArrayList;
import java.util.Arrays;

public class Learner {

    public static int chooseStrategy() {

    }

    public static ArrayList<String> filterResults(ArrayList<String> myData) {
        if (!myData.isEmpty() && !myData.contains("unfiltered")) {
            myData.remove(myData.size()-1);
            ArrayList<String> filteredData = new ArrayList<>();
            String newLine = "";
            for (String myLine : myData) {
                ArrayList<String> s = splitLine(myLine);
                if (s.size() == 5 && s.get(1).equals())
            }
        }
    }






    public static ArrayList<String> splitLine(String myLine) {
        String[] ss = myLine.split(",");
        return new ArrayList<>(Arrays.asList(ss));
    }

}
