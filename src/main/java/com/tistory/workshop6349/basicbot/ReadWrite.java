package com.tistory.workshop6349.basicbot;

import java.io.*;
import java.util.ArrayList;

public class ReadWrite {

    public static ArrayList<String> read_learning_data(String name) throws IOException {
        String myPath = "C:\\Users\\DGSW\\Downloads\\Starcraft_Bot_Files\\StarCraft\\bwapi-data\\data\\BasicBot_" + name + ".txt";
        String myPath_orig = "C:\\Users\\DGSW\\Downloads\\Starcraft_Bot_Files\\StarCraft\\bwapi-data\\data\\BasicBotLog.txt";
        ArrayList<String> myData;

        myData = readFile(myPath);
        if (myData.isEmpty()) {
            myData = readFile(myPath_orig);
            if (!myData.isEmpty()) {
                myData.add("unfiltered");
            }
        }
        return myData;
    }

    public static ArrayList<String> readFile(String fileName) throws IOException {
        ArrayList<String> myData = new ArrayList<>();
        FileReader fileReader = new FileReader(fileName);
        BufferedReader reader = new BufferedReader(fileReader);

        String str = null;
        while ((str = reader.readLine()) != null) {
            myData.add(str);
        }
        reader.close();
        fileReader.close();

        return myData;
    }

    public static String writeLogfile(ArrayList<String> list, String name) throws IOException {
        String myPath = "C:\\Users\\DGSW\\Downloads\\Starcraft_Bot_Files\\StarCraft\\bwapi-data\\data\\BasicBot_" + name + ".txt";
        File file = new File(myPath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        if (file.isFile() && file.canWrite()) {
            for (String line : list) {
                writer.write(line);
                writer.newLine();
            }
            writer.close();
            return myPath;
        }
        else {
            return "unable to write log file";
        }
    }

    public static void writeFrame(int n) throws IOException {
        File file = new File("BasicBotFrame.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        if (!file.exists()) {
            return;
        }
        writer.write(n);
        writer.newLine();
        writer.close();
    }

    public static void writeFrame(String n) throws IOException {
        File file = new File("BasicBotFrame.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        if (!file.exists()) {
            return;
        }
        writer.write(n);
        writer.newLine();
        writer.close();
    }

}
