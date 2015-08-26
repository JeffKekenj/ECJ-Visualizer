/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visuallizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Scanner;

/**
 * Reads out2.stat file and saves the information in a better format Also
 * provides some functions to retrieve data in certain formats to be used in the
 * graphcreator
 *
 * @author Eric
 * @author Jeff
 */
public class OutputReader {

    public ArrayList<Integer> gen;      // Generation values
    public ArrayList<Double> median;    // Median values
    public ArrayList<Double> bestRun;   // Best Run Values
    public ArrayList<Double> bestGen;   // Best Gen Values
    public int maxGens;                 // Number of generations in the out2 file
    public boolean reading;             // Is the output reader currently reading

    /**
     * Empty constructor
     */
    public OutputReader() {
        maxGens = 0;
    }

    /**
     * Set the reading boolean variable
     *
     * @param b boolean
     */
    public void setReading(boolean b) {
        reading = b;
    }

    /**
     * Returns the first line of the file at outFileLoc location
     *
     * @param outFileLoc File path of the out2.stat location
     * @return String of the first line
     */
    public String firstLine(String outFileLoc) {

        // Attempt to open a file stream and read the first line
        try {
            String fileName = outFileLoc + "\\out2.stat";
            FileInputStream fstream = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String fl = br.readLine();
            // If the file is empty, continually try until you get something
            if (fl == null) {
                return firstLine(outFileLoc);
            }

            // Close the streams and return the first line
            br.close();
            fstream.close();
            return fl;

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Error
        return "";
    }

    /**
     * Returns true when the file has been written to. Continually checks the
     * file to see if the first line has changed
     *
     * @param outFileLoc File location path
     * @return Boolean - true when the file has changed, false if it doesnt
     */
    public boolean outDataReady(String outFileLoc) {

        // Attempt to open a file stream and read the first line
        try {
            String line1 = firstLine(outFileLoc);
            // Continually compare the old first line to the first line of the
            // out2.stat file
            while (true) {

                String line2 = firstLine(outFileLoc);
                // Read the first line until it changes 
                if (line2.equals(line1)) {
                    //System.out.println("Same out file still");
                    Thread.sleep(50);
                } else {
                    //System.out.println("New File!");
                    return true;
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Error
        return false;

    }

    /**
     * Method to read out2.stat file constantly, but since out2.stat only
     * updates once it is finished running
     *
     * @param outFileLoc
     * @return
     */
    public double readOut2(String outFileLoc, int index) {

        try {

            File file = new File(outFileLoc + "\\out2.stat");
            Scanner s = new Scanner(file);

            int c = 0;

            // read the file and seperate the data
            while (true) {

                if (s.hasNext()) {
                    s.nextInt();
                    if (c == index) {
                        double d = s.nextDouble();
                        s.close();
                        return d;
                    }
                    c++;
                }
                if (s.hasNextLine()) {
                    s.nextLine();
                } else {
                    // No more lines to read at the moment
                    return -0.0001337;
                }
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        return -0.02201337; // error
    }

    /**
     * Returns a list of doubles
     *
     * @param p 1 - median, 2 - best run, 3 - best gen, other generation #
     * @return list of doubles
     */
    public double[] getList(int p) {
        double[] d;
        int c;
        switch (p) {
            case 1:
                // median
                d = new double[median.size()];
                c = 0;
                for (double dd : median) {
                    d[c] = dd;
                    c++;
                }
                return d;

            case 2:
                // best run
                d = new double[bestRun.size()];
                c = 0;
                for (double dd : bestRun) {
                    d[c] = dd;
                    c++;
                }
                return d;

            case 3:
                // best gen
                d = new double[bestGen.size()];
                c = 0;
                for (double dd : bestGen) {
                    d[c] = dd;
                    c++;
                }
                return d;

            default:
                //generation number
                d = new double[gen.size()];
                c = 0;
                for (double dd : gen) {
                    d[c] = dd;
                    c++;
                }
                return d;
        }

    }

    /**
     * getBucketData returns a Bucket containing all the information needed
     * about the data specified
     *
     * @param ecjDirectory directory of ECJ
     * @param fileName file name to read
     * @param type 1 - median, 2 - best run, 3 - best gen
     * @return Bucket data
     */
    public Bucket getBucketData(String ecjDirectory, String fileName, int type) {
        Bucket b = new Bucket();
        String outfile = "";
        String dir = ecjDirectory + "\\Data";
        File f = new File(dir);
        f.mkdir();
        switch (type) {
            case 1: // median
                outfile = dir + "\\" + fileName + "-Median.txt";
                break;
            case 2: // best run
                outfile = dir + "\\" + fileName + "-Best Run.txt";
                break;
            case 3: // best gen
                outfile = dir + "\\" + fileName + "-Best Gen.txt";
                break;
        }

        // Try to read the file 
        try {

            File file = new File(outfile);
            boolean exist = file.exists();
            if (exist) {
                Scanner s = new Scanner(file);
                int readCount = 0;
                int runCount = s.nextInt();
                double[][] values = new double[maxGens][runCount + 1];
                s.nextLine();

                while (s.hasNextLine()) {
                    for (int i = 0; i < runCount; i++) {
                        values[readCount][i] = s.nextDouble();
                    }
                    if (s.hasNextLine()) {
                        s.nextLine();
                        readCount++;
                    }
                }
                s.close();

                // Adds the average to the final component
                double[] avg = new double[maxGens];
                for (int i = 0; i < maxGens; i++) {
                    avg[i] = 0;
                    for (int j = 0; j < runCount; j++) {
                        avg[i] += values[i][j];
                    }
                    avg[i] /= runCount;
                    values[i][runCount] = avg[i];
                    //System.out.println("\nAverage at " + i + "= " + avg[i]);
                }

                // get highest/lowest points of each run
                double[] high = new double[runCount + 1];
                Arrays.fill(high, -1000000);
                double[] low = new double[runCount + 1];
                Arrays.fill(low, 1000000);

                for (int i = 0; i < runCount + 1; i++) {
                    for (int j = 0; j < maxGens; j++) {
                        if (values[j][i] > high[i]) {
                            high[i] = values[j][i];
                        }
                        if (values[j][i] < low[i]) {
                            low[i] = values[j][i];
                        }
                    }
                }

                int[][] bucket = new int[runCount + 1][5];
                String[] ranges = new String[5];

                // Adjust the buckets by the average values
                double range = high[runCount] - low[runCount];
                double[] lower = new double[5];
                double[] higher = new double[5];
                DecimalFormat df = new DecimalFormat("#.000");

                for (int l1 = 0; l1 < 5; l1++) {
                    lower[l1] = low[runCount] + (l1 * (0.2 * range));          // bounds is between 0 and 10% for initial
                    higher[l1] = lower[l1] + (0.2 * range);
                    ranges[l1] = df.format(lower[l1]) + "-" + df.format(higher[l1]);
                }

                // Read the data into buckets
                for (int i = 0; i < runCount + 1; i++) {
                    //System.out.println(i + " low: " + low[i] + "\thigh: " + high[i]);                    

                    for (int j = 0; j < maxGens; j++) {
                        // Split them into 5 different buckets
                        for (int k = 0; k < 5; k++) {

                            if (values[j][i] >= lower[k] && values[j][i] <= higher[k]) {
                                bucket[i][k]++;
                                /*System.out.println("Added " + values[j][i] + 
                                 " to above bucket " + k + ", with " 
                                 + bucket[i][k] + " values in that bucket");*/
                                break;
                            }
                        }
                    } // generation done
                }

                b.setData(bucket);
                b.setRanges(ranges);
                return b;
            }

        } catch (FileNotFoundException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Problem doing bucket data! " + e.getMessage());
        }

        // error
        return null;

    }

    /**
     * Returns all run data for a specific list
     *
     * @param ecjDirectory ecj directory
     * @param fileName file name of what data you want to load
     * @param p what type of data you want, 1 for median, 2 for best run, 3 for
     * best gen
     * @return
     */
    public double[][] getAllData(String ecjDirectory, String fileName, int p) {
        String outfile = "";
        String dir = ecjDirectory + "\\Data";
        File f = new File(dir);
        f.mkdir();
        switch (p) {
            case 1: // median
                outfile = dir + "\\" + fileName + "-Median.txt";
                break;
            case 2: // best run
                outfile = dir + "\\" + fileName + "-Best Run.txt";
                break;
            case 3: // best gen
                outfile = dir + "\\" + fileName + "-Best Gen.txt";
                break;
        }

        // Try to read the file 
        try {

            File file = new File(outfile);
            boolean exist = file.exists();
            if (exist) {
                Scanner s = new Scanner(file);
                int readCount = 0;
                int runCount = s.nextInt();
                double[][] values = new double[maxGens][runCount + 1];
                s.nextLine();

                while (s.hasNextLine()) {
                    for (int i = 0; i < runCount; i++) {
                        values[readCount][i] = s.nextDouble();
                    }
                    if (s.hasNextLine()) {
                        s.nextLine();
                        readCount++;
                    }
                }
                s.close();

                // Adds the average to the final component
                double[] avg = new double[maxGens];
                for (int i = 0; i < maxGens; i++) {
                    avg[i] = 0;
                    int badCount = 0;
                    for (int j = 0; j < runCount; j++) {
                        if (values[i][j] == -1.23456) {
                            badCount++;
                        }
                        avg[i] += values[i][j];
                    }
                    avg[i] /= (runCount - badCount);
                    values[i][runCount] = avg[i];
                    //System.out.println("\nAverage at " + i + "= " + avg[i]);
                }
                return values;
            }

        } catch (FileNotFoundException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Problem reading all data! " + e.getMessage());
        }

        // error
        return null;

    }

    /**
     * writeOuts the three seperate text files containing median/best gen/best
     * run data only
     *
     * @param ecjDirectory directory of ecj
     * @param fileName file name of the parameter file
     */
    public void writeOut(String ecjDirectory, String fileName) {

        String dir = ecjDirectory + "\\Data";
        File f = new File(dir);
        f.mkdir();

        String outMedian = dir + "\\" + fileName + "-Median.txt";
        String outBestGen = dir + "\\" + fileName + "-Best Gen.txt";
        String outBestRun = dir + "\\" + fileName + "-Best Run.txt";

        gen = new ArrayList<Integer>();
        median = new ArrayList<Double>();
        bestRun = new ArrayList<Double>();
        bestGen = new ArrayList<Double>();
        int out2Gens;           // Number of generations in out2
        int forLoopCounter = 0; // forloop counter
        int runCount;           // number of runs        

        // Try to read the file 
        try {

            File file = new File(ecjDirectory + "\\out2.stat");
            Scanner s = new Scanner(file);

            // read the file and seperate the data
            while (s.hasNextLine()) {
                gen.add(s.nextInt());
                median.add(s.nextDouble());
                bestRun.add(s.nextDouble());
                bestGen.add(s.nextDouble());
                //System.out.println("Cleared " + gen.size());
                if (s.hasNextLine()) {
                    s.nextLine();
                }
            }
            s.close(); // close scanner            
            out2Gens = gen.size(); // number of generations            
            maxGens = out2Gens;

            // For each value type (median/best run/best gen) two steps are applied
            // Read the old file and save all the values
            // Make the new file have all the old values + all the new runs values

            // Median
            file = new File(outMedian);
            double[][] oldMedian = new double[maxGens][];
            boolean exist = file.exists();
            if (exist) {
                s = new Scanner(file);
                int readCount = 0; // number of gens found 
                runCount = s.nextInt();
                int fileGenCount = s.nextInt();
                if (fileGenCount > maxGens) {
                    maxGens = fileGenCount;
                }
                s.nextLine();

                oldMedian = new double[maxGens][runCount];
                for (int i = 0; i < maxGens; i++) {
                    Arrays.fill(oldMedian[i], -1.23456); // if runs dont reach max gens, their values must be ignored
                }
                // special value to be ignored
                while (s.hasNextLine()) {
                    for (int i = 0; i < runCount; i++) {
                        if (s.hasNextDouble()) {
                            oldMedian[readCount][i] = s.nextDouble();
                        }
                    }
                    if (s.hasNextLine()) {
                        s.nextLine();
                        readCount++;
                    }
                }
                runCount++;
                s.close();
            } else {
                runCount = 1;
            }

            // Median            
            PrintStream out = new PrintStream(new FileOutputStream(outMedian, false));
            out.print(runCount);
            out.print(" ");
            out.println(maxGens);
            for (forLoopCounter = 0; forLoopCounter < maxGens; forLoopCounter++) {
                if (exist) {
                    for (int i = 0; i < runCount - 1; i++) {
                        out.print(oldMedian[forLoopCounter][i]);
                        out.print(" ");
                    }
                }
                if (forLoopCounter < median.size()) {
                    out.println(median.get(forLoopCounter));
                }

            }
            out.close();

            // Best run
            file = new File(outBestRun);
            double[][] oldBestRun = new double[maxGens][];
            exist = file.exists();
            if (exist) {
                s = new Scanner(file);
                int readCount = 0; // number of gens found 
                runCount = s.nextInt();
                int fileGenCount = s.nextInt();
                if (fileGenCount > maxGens) {
                    maxGens = fileGenCount;
                }
                s.nextLine();

                oldBestRun = new double[maxGens][runCount];
                for (int i = 0; i < maxGens; i++) {
                    Arrays.fill(oldBestRun[i], -1.23456); // if runs dont reach max gens, their values must be ignored
                }
                // special value to be ignored
                while (s.hasNextLine()) {
                    for (int i = 0; i < runCount; i++) {
                        if (s.hasNextDouble()) {
                            oldBestRun[readCount][i] = s.nextDouble();
                        }
                    }
                    if (s.hasNextLine()) {
                        s.nextLine();
                        readCount++;
                    }
                }
                runCount++;
                s.close();
            } else {
                runCount = 1;
            }

            // best run            
            out = new PrintStream(new FileOutputStream(outBestRun, false));
            out.print(runCount);
            out.print(" ");
            out.println(maxGens);
            for (forLoopCounter = 0; forLoopCounter < maxGens; forLoopCounter++) {
                if (exist) {
                    for (int i = 0; i < runCount - 1; i++) {
                        out.print(oldBestRun[forLoopCounter][i]);
                        out.print(" ");
                    }
                }
                if (forLoopCounter < bestRun.size()) {
                    out.println(bestRun.get(forLoopCounter));
                }

            }
            out.close();

            // Best gen
            file = new File(outBestGen);
            double[][] oldBestGen = new double[maxGens][];
            exist = file.exists();
            if (exist) {
                s = new Scanner(file);
                int readCount = 0; // number of gens found 
                runCount = s.nextInt();
                int fileGenCount = s.nextInt();
                if (fileGenCount > maxGens) {
                    maxGens = fileGenCount;
                }
                s.nextLine();

                oldBestGen = new double[maxGens][runCount];
                for (int i = 0; i < maxGens; i++) {
                    Arrays.fill(oldBestGen[i], -1.23456); // if runs dont reach max gens, their values must be ignored
                }
                // special value to be ignored
                while (s.hasNextLine()) {
                    for (int i = 0; i < runCount; i++) {
                        if (s.hasNextDouble()) {
                            oldBestGen[readCount][i] = s.nextDouble();
                        }
                    }
                    if (s.hasNextLine()) {
                        s.nextLine();
                        readCount++;
                    }
                }
                runCount++;
                s.close();
            } else {
                runCount = 1;
            }

            // best gen            
            out = new PrintStream(new FileOutputStream(outBestGen, false));
            out.print(runCount);
            out.print(" ");
            out.println(maxGens);
            for (forLoopCounter = 0; forLoopCounter < maxGens; forLoopCounter++) {
                if (exist) {
                    for (int i = 0; i < runCount - 1; i++) {
                        out.print(oldBestGen[forLoopCounter][i]);
                        out.print(" ");
                    }
                }
                if (forLoopCounter < bestGen.size()) {
                    out.println(bestGen.get(forLoopCounter));
                }

            }
            out.close();

        } catch (FileNotFoundException | ArrayIndexOutOfBoundsException | InputMismatchException e) {
            System.out.println("Problem during write out! " + e.getMessage());

        }

    }

    /**
     * reads the median list
     *
     * @param ecjDirectory directory of ecj
     * @param fileName file name of the parameter file
     */
    public double[] readMedianList(String ecjDirectory, String fileName) {

        median = new ArrayList<Double>();

        // Try to read the file 
        try {

            File file = new File(ecjDirectory + "\\out2.stat");
            Scanner s = new Scanner(file);

            // read the file and seperate the data
            while (s.hasNextLine()) {
                s.nextInt();
                median.add(s.nextDouble());
                if (s.hasNextLine()) {
                    s.nextLine();
                }
            }
            s.close(); // close scanner    
            int c = 0;
            double[] d = new double[median.size()];
            for (double dd : median) {
                d[c] = dd;
                c++;
            }
            return d;

        } catch (FileNotFoundException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Problem reading median list! " + e.getMessage());
        }

        // Error
        return null;
    }

}
