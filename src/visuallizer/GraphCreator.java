/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visuallizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This class was majorily based off these two implementations found below. This
 * class uses JFreeCharts to help create good looking charts/graphs of the data
 * provided
 *
 * @author Eric
 * @author Jeff
 *
 * @see http://stackoverflow.com/a/15715096/230513
 * @see http://stackoverflow.com/a/11949899/230513
 * @see
 * http://stackoverflow.com/questions/5048852/using-jfreechart-to-display-recent-changes-in-a-time-series/6517440#6517440
 * @see
 * http://stackoverflow.com/questions/15707496/adding-chartpanel-to-jtabbedpane-using-jpanel/15715096#15715096
 */
public class GraphCreator implements Runnable {

    // Data to be used
    private double[][] currentFitnessData;  // current median for live logging
    private double[][] medianRunsData;      // median data
    private double[][] bestRunsData;        // best runs data
    private double[][] bestGensData;        // best gens data
    private int[][] bucketData;             // bucket data

    public OutputReader or;                 // outputReader
    public Thread readingThread;            // reading from the OR
    public boolean readingRunning;          // is the OR running
    public boolean[] runFinished;           // Live runs status
    private boolean[] dataFinished;         // Timers cant start if all data has been plotted
    public int liveCount = 0;               // number of values added to the live log

    private static String TITLE = "";           // title of plot
    private static final String START = "Start";    // start button text
    private static final String STOP = "Stop";      // stop button text
    private static final String FIN = "Finished";   // finished button text
    private static final int REALTIME = 25;          // live log speed
    private static final int FAST = 125;        // Speed of the graph changes
    private static final int SLOW = FAST * 4;   // Speed of the graph changes

    // list of the timers for each tab
    private ArrayList<Timer> timer;         // list of the timers
    // List of the charts
    private ArrayList<JFreeChart> charts;   // list of the charts

    private String ecjDirectory;        // ecj directory
    private String fileName;            // file name of params file
    private String printDir;            // print folder

    // Swing components
    public JFrame f;                // frame
    public JTabbedPane jtp;         // tabbedpane (charts)
    public JComboBox combo;         // fast/slow timer
    public JButton run;             // start/stop button
    public JButton decUpperRange;   // range of charts buttons
    public JButton incUpperRange;   // range of charts buttons
    public JButton incLowerRange;   // range of charts buttons
    public JButton decLowerRange;   // range of charts buttons
    public JButton print;           // print button

    // Number of tabs/runs so far
    private int runCounter;         // number of runs on the live logs
    private int typeOfGraphs;       // type of graph to make

    /**
     * Setup the variables of the graph creator
     */
    private void setup(String title) {

        timer = new ArrayList<>();
        f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jtp = new JTabbedPane();
        runCounter = 0;

        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(false);

    }

    /**
     * Display the live log charts
     */
    private void display() {

        // Range bounds for the graph
        incLowerRange = new JButton("Inc Lower Bound");
        incLowerRange.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int tab = jtp.getSelectedIndex();
                ValueAxis range = charts.get(tab).getXYPlot().getRangeAxis();
                //range.setRange(0.5, 1);
                double upper = range.getUpperBound();
                double lower = range.getLowerBound();
                double rang = upper + lower;
                range.setRange(lower + (rang * 0.025), upper); // decrease by 2.5%

            }
        });

        decLowerRange = new JButton("Dec Lower Bound");
        decLowerRange.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int tab = jtp.getSelectedIndex();
                ValueAxis range = charts.get(tab).getXYPlot().getRangeAxis();
                //range.setRange(0.5, 1);
                double upper = range.getUpperBound();
                double lower = range.getLowerBound();
                double rang = upper + lower;
                range.setRange(lower - (rang * 0.025), upper); // decrease by 2.5%

            }
        });

        incUpperRange = new JButton("Inc Upper Bound");
        incUpperRange.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int tab = jtp.getSelectedIndex();
                ValueAxis range = charts.get(tab).getXYPlot().getRangeAxis();
                //range.setRange(0.5, 1);
                double upper = range.getUpperBound();
                double lower = range.getLowerBound();
                double rang = upper + lower;
                range.setRange(lower, upper + (rang * 0.025)); // decrease by 2.5%

            }
        });

        decUpperRange = new JButton("Dec Upper Bound");
        decUpperRange.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int tab = jtp.getSelectedIndex();
                ValueAxis range = charts.get(tab).getXYPlot().getRangeAxis();
                //range.setRange(0.5, 1);
                double upper = range.getUpperBound();
                double lower = range.getLowerBound();
                double rang = upper + lower;
                range.setRange(lower, upper - (rang * 0.025)); // decrease by 5%

            }
        });

        //System.out.println("upper: " + chart.getXYPlot().getRangeAxis().getUpperBound());
        //System.out.println("lower: " + chart.getXYPlot().getRangeAxis().getLowerBound());
        // Start/Stop and Fast/Slow
        //---------------------------------------
        run = new JButton(START);
        run.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                int tab = jtp.getSelectedIndex();
                if (STOP.equals(cmd)) {
                    timer.get(tab).stop();
                    run.setText(START);
                } else if (START.equals(cmd)) {
                    timer.get(tab).start();
                    run.setText(STOP);
                } else {
                    // Finish button doing ???
                    /*
                     if(dataFinished[tab]){
                     System.out.println("Finished " + tab);
                     }*/
                }
            }
        });

        // start stop change for each tab
        jtp.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                int tab = jtp.getSelectedIndex();
                if ("Fast".equals(combo.getSelectedItem())) {
                    timer.get(tab).setDelay(FAST);
                } else {
                    timer.get(tab).setDelay(SLOW);
                }

                if (timer.get(tab).isRunning()) {
                    run.setText(STOP);
                } else if (dataFinished[tab]) {
                    run.setText(FIN);
                } else {
                    run.setText(START);
                }
            }
        });

        combo = new JComboBox();
        combo.addItem("Fast");
        combo.addItem("Slow");
        combo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int tab = jtp.getSelectedIndex();
                if ("Fast".equals(combo.getSelectedItem())) {
                    timer.get(tab).setDelay(FAST);
                } else {
                    timer.get(tab).setDelay(SLOW);
                }
            }
        });

        print = new JButton("Print");
        print.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int tab = jtp.getSelectedIndex();
                createChartPic(tab);
            }
        });

        //---------------------------------------
        f.add(jtp, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(decLowerRange);
        btnPanel.add(incLowerRange);
        btnPanel.add(decUpperRange);
        btnPanel.add(incUpperRange);
        btnPanel.add(run);
        btnPanel.add(combo);
        btnPanel.add(print);
        f.add(btnPanel, BorderLayout.SOUTH);
        //----------------------

        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    /**
     * Display for the non updating charts
     */
    private void display2() {

        // Range bounds for the graph
        incLowerRange = new JButton("Inc Lower Bound");
        incLowerRange.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                int tab = jtp.getSelectedIndex();
                ValueAxis range = charts.get(tab).getXYPlot().getRangeAxis();
                //range.setRange(0.5, 1);
                double upper = range.getUpperBound();
                double lower = range.getLowerBound();
                double rang = upper + lower;
                range.setRange(lower + (rang * 0.025), upper); // decrease by 2.5%

            }
        });

        decLowerRange = new JButton("Dec Lower Bound");
        decLowerRange.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                int tab = jtp.getSelectedIndex();
                ValueAxis range = charts.get(tab).getXYPlot().getRangeAxis();
                //range.setRange(0.5, 1);
                double upper = range.getUpperBound();
                double lower = range.getLowerBound();
                double rang = upper + lower;
                range.setRange(lower - (rang * 0.025), upper); // decrease by 2.5%

            }
        });

        incUpperRange = new JButton("Inc Upper Bound");
        incUpperRange.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                int tab = jtp.getSelectedIndex();
                ValueAxis range = charts.get(tab).getXYPlot().getRangeAxis();
                //range.setRange(0.5, 1);
                double upper = range.getUpperBound();
                double lower = range.getLowerBound();
                double rang = upper + lower;
                range.setRange(lower, upper + (rang * 0.025)); // decrease by 2.5%

            }
        });

        decUpperRange = new JButton("Dec Upper Bound");
        decUpperRange.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
                int tab = jtp.getSelectedIndex();
                ValueAxis range = charts.get(tab).getXYPlot().getRangeAxis();
                //range.setRange(0.5, 1);
                double upper = range.getUpperBound();
                double lower = range.getLowerBound();
                double rang = upper + lower;
                range.setRange(lower, upper - (rang * 0.025)); // decrease by 5%

            }
        });

        print = new JButton("Print");
        print.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int tab = jtp.getSelectedIndex();
                createChartPic(tab);
            }
        });

        //---------------------------------------
        f.add(jtp, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(decLowerRange);
        btnPanel.add(incLowerRange);
        btnPanel.add(decUpperRange);
        btnPanel.add(incUpperRange);
        btnPanel.add(print);
        f.add(btnPanel, BorderLayout.SOUTH);
        //----------------------
        //----------------------
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    /**
     * Display for the non updating buckets
     */
    private void display3() {

        print = new JButton("Print");
        print.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int tab = jtp.getSelectedIndex();
                createChartPic(tab);
            }
        });

        //---------------------------------------
        f.add(jtp, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(print);
        f.add(btnPanel, BorderLayout.SOUTH);
        //----------------------
        //----------------------
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    /**
     * Create a XYLineChart at position tab on the jtabbedpane Used for the
     * updating charts
     *
     * @param tab the tab position
     * @return
     */
    private ChartPanel createPane1(int tab) {

        // Create a series of daya
        XYSeries series = new XYSeries("Data");

        // Start the data off to be 10% of the total data
        int initData = currentFitnessData[tab].length / 10;
        for (int i = 0; i < initData; i++) {
            if (currentFitnessData[tab][i] != -1.23456) {
                series.add(i, currentFitnessData[tab][i]);
            }

        }

        // Create the dataset for the graph
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Add a timer to add more data to the graph
        timer.add(new Timer(FAST, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int thisIndex = tab;

                // Make sure the graph only graphs available data
                if (series.getItemCount() < currentFitnessData[thisIndex].length) {
                    // Add one item at a time
                    //System.out.println("adding to " + thisIndex + ", tab is " + tab + "\tSI=" + jtp.getSelectedIndex());
                    if (currentFitnessData[thisIndex][series.getItemCount()] != -1.23456) {
                        series.add(series.getItemCount(), currentFitnessData[thisIndex][series.getItemCount()]);
                    }

                } else {
                    // No more data, stop the timer
                    timer.get(thisIndex).stop();
                    dataFinished[thisIndex] = true;
                    //System.out.println("Data finished for " + thisIndex);
                    if (jtp.getSelectedIndex() == thisIndex) {
                        run.setText(FIN);
                    }
                }
            }
        }));

        // Create the XY Line chart, and return it with preferred dimensions
        JFreeChart chart = ChartFactory.createXYLineChart(TITLE, "Generations",
                "Fitness", dataset, PlotOrientation.VERTICAL, true, true, false);
        charts.add(chart);
        //System.out.println("upper: " + chart.getXYPlot().getRangeAxis().getUpperBound());
        //System.out.println("lower: " + chart.getXYPlot().getRangeAxis().getLowerBound());
        //ValueAxis range = chart.getXYPlot().getRangeAxis();
        //range.setRange(0.5, 1);
        return new ChartPanel(chart) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(700, 350);
            }
        };
    }

    /**
     * Create a XYLineChart at position tab on the jtabbedpane This is for the
     * non updating chart
     *
     * @param tab
     * @return
     */
    private ChartPanel createPane2(int tab, int type) {
        // Create a series of daya
        XYSeries series = new XYSeries("Data");

        switch (type) {
            case 1: // median
                for (int i = 0; i < medianRunsData.length; i++) {
                    if (medianRunsData[i][tab] != -1.23456) {
                        series.add(i, medianRunsData[i][tab]);
                    }
                }
                break;
            case 2: // best Run
                for (int i = 0; i < bestRunsData.length; i++) {
                    if (bestRunsData[i][tab] != -1.23456) {
                        series.add(i, bestRunsData[i][tab]);
                    }
                }
                break;
            case 3: // best Gen
                for (int i = 0; i < bestGensData.length; i++) {
                    if (bestGensData[i][tab] != -1.23456) {
                        series.add(i, bestGensData[i][tab]);
                    }
                }
                break;
        }

        // Create the dataset for the graph
        XYSeriesCollection dataset = new XYSeriesCollection(series);

        // Create the XY Line chart, and return it with preferred dimensions
        JFreeChart chart = ChartFactory.createXYLineChart(TITLE, "Generations",
                "Fitness", dataset, PlotOrientation.VERTICAL, true, true, false);
        charts.add(chart);
        return new ChartPanel(chart) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(700, 350);
            }
        };
    }

    /**
     * Create a XYLineChart at position tab on the jtabbedpane This is for the
     * non updating chart
     *
     * @param tab
     * @return
     */
    private ChartPanel createPane3(Bucket b) {

        // Create the dataset for the graph        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < bucketData.length; i++) {
            for (int j = 0; j < bucketData[i].length; j++) {
                String runS = (i == (bucketData.length - 1) ? "Avg of all Runs" : ("Run " + String.valueOf(i)));
                dataset.addValue(bucketData[i][j],
                        b.getRanges()[j], runS);
            }
        }

        // Create the XY Line chart, and return it with preferred dimensions
        JFreeChart chart = ChartFactory.createBarChart(TITLE, "Runs",
                "Size of Buckets", dataset, PlotOrientation.VERTICAL, true, true, false);
        charts.add(chart);
        return new ChartPanel(chart) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(700, 350);
            }
        };
    }

    /**
     * Create a XYLineChart at position tab on the jtabbedpane Used for the
     * updating charts
     *
     * @param tab the tab position
     * @return
     */
    private ChartPanel createPane4(int tab) {

        // Create a series of daya
        XYSeries series = new XYSeries("Data");

        // Create the dataset for the graph
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        or.setReading(true);
        liveCount = 0;

        // Add a timer to add more data to the graph
        timer.add(new Timer(REALTIME, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                int thisIndex = tab;

                // Check to see if program is still on the specific run         
                if (!runFinished[thisIndex]) {

                    // Run is still going, try to fetch some data
                    //System.out.print("get " + liveCount);
                    double val = or.readOut2(ecjDirectory, liveCount);

                    if (val == -0.0001337) {
                        // Its still reading, but no input is ready yet, do nothing
                        //System.out.println("reached limit at " + liveCount);
                    } else if (val == 1337 || val == -1.23456) {
                        // Done reading, no more input
                        //System.out.println("Fin.? " + liveCount);
                        //dataFinished[thisIndex] = true;                        
                        //timer.get(thisIndex).stop();
                        //run.setText(FIN);
                    } else if (val == -0.02201337) {
                        // Error
                        //System.out.println("Problem -22 at " + liveCount);
                        timer.get(thisIndex).stop();
                        readingRunning = false;
                    } else {
                        // No problems, add the value
                        //System.out.println("\t" + val);
                        series.add(series.getItemCount(), val);
                        liveCount++;
                    }

                } else {
                    // Run is over, stop the timer and add the entire series of data

                    /*
                     System.out.println("Curr data size [" + thisIndex + "] "
                     + " = " + currentFitnessData[thisIndex].length);
                     System.out.println("Currently there are "
                     + series.getItemCount() + " items in the graph");
                     */
                    // Add items if the series isnt full
                    if (series.getItemCount() < currentFitnessData[thisIndex].length) {
                        // Add one item at a time
                        //System.out.println("adding to " + thisIndex + ", tab is " + tab + "\tSI=" + jtp.getSelectedIndex());
                        //System.out.println("adding values now since run is done");
                        if (currentFitnessData[thisIndex][series.getItemCount()] != -1.23456) {
                            series.add(series.getItemCount(), currentFitnessData[thisIndex][series.getItemCount()]);
                        }

                    } else {

                        // No more data, stop the timer
                        timer.get(thisIndex).stop();
                        dataFinished[thisIndex] = true;
                        //System.out.println("Data finished for " + thisIndex);
                        if (jtp.getSelectedIndex() == thisIndex) {
                            run.setText(FIN);
                        }
                    }

                }
            }

        }
        ));

        readingRunning = true;
        // Create the XY Line chart, and return it with preferred dimensions
        JFreeChart chart = ChartFactory.createXYLineChart(TITLE, "Generations",
                "Fitness", dataset, PlotOrientation.VERTICAL, true, true, false);

        charts.add(chart);
        timer.get(tab).start();
        //System.out.println("upper: " + chart.getXYPlot().getRangeAxis().getUpperBound());
        //System.out.println("lower: " + chart.getXYPlot().getRangeAxis().getLowerBound());
        //ValueAxis range = chart.getXYPlot().getRangeAxis();
        //range.setRange(0.5, 1);

        return new ChartPanel(chart) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(700, 350);
            }
        };
    }

    public boolean getReadingStatus() {
        return readingRunning;
    }

    public void runFinished(int index, double[] currData) {
        runFinished[index] = true;
        currentFitnessData[index] = currData;
        if (timer.size() > index) {
            timer.get(index).stop();
        }
        int ind = jtp.getSelectedIndex();
        if (ind >= 0) {
            if (timer.get(ind).isRunning()) {
                run.setText(START);
            }
        }

    }

    /**
     * Constructor
     *
     * @param numRuns number of runs being executed
     * @param type
     * @param title
     *
     */
    public GraphCreator(int numRuns, int type, String title, String dir, String fn) {
        // create picture directory

        printDir = dir + "//ChartPics//";
        File f = new File(printDir);
        f.mkdir();

        runFinished = new boolean[numRuns];
        Arrays.fill(runFinished, false);
        ecjDirectory = dir;
        TITLE = title;
        fileName = fn;
        typeOfGraphs = type;
        currentFitnessData = new double[numRuns][];
        medianRunsData = new double[numRuns][];
        bestRunsData = new double[numRuns][];
        bestGensData = new double[numRuns][];
        dataFinished = new boolean[numRuns];
        charts = new ArrayList<>();
        Arrays.fill(dataFinished, false);
        readingRunning = false;
        setup(title);

    }

    public void liveFitnessLogging(String dir, int run) {

        or = new OutputReader();

        // This is reading the out file determining, if its a new file yet      
        readingThread = new Thread(new Runnable() {
            public void run() {

                if (!or.outDataReady(dir)) {
                    //System.out.print("Not Ready\n");
                } else {
                    //System.out.print("Ready\n");                        
                    runFinished[run] = false;
                    liveLog();

                }
            }
        });
        readingThread.start();
    }

    public void liveLog() {

        // This is reading the out file determining, if its a new file yet      
        readingThread.interrupt();
        // Now live log
        jtp.add("Run " + String.valueOf(runCounter + 1), createPane4(runCounter));
        jtp.setSelectedIndex(runCounter);
        if (runCounter == 0) {
            this.run();
        }
        runCounter++;

    }

    /**
     * Add a run worth of data, creates a new tab
     *
     * @param fitData
     */
    public void addCurrentFitnessData(double[] fitData) {
        currentFitnessData[runCounter] = fitData;
        jtp.add("Run " + String.valueOf(runCounter + 1), createPane1(runCounter));
        jtp.setSelectedIndex(runCounter);
        runCounter++;
    }

    /**
     * Add a run worth of data, creates a new tab
     *
     * @param fitData
     */
    public void addCurrentFitnessData(double[] fitData, int run) {
        currentFitnessData[run] = fitData;
    }

    public void addBarData(Bucket b, int type) {
        String label = "";

        bucketData = b.getData();
        switch (type) {
            case 1:
                label = "Median Bucket";
                break;
            case 2:
                label = "Best Run Bucket";
                break;
            case 3:
                label = "Best Gen Bucket";
                break;
        }

        jtp.add(label, createPane3(b));

    }

    public boolean getDataFinished(int index) {
        return dataFinished[index];
    }

    public void setDataFinished(boolean[] dataFinished) {
        this.dataFinished = dataFinished;
    }

    /**
     * addFullRunData adds data
     *
     * @param data double [runs][numgens]
     * @param type 1 - median, 2 - best run, 3 - best gen
     */
    public void addFullRunData(double[][] data, int type) {

        switch (type) {
            case 1: // median
                medianRunsData = data;
                break;
            case 2: // best Run
                bestRunsData = data;
                break;
            case 3: // best Gen
                bestGensData = data;
                break;
        }

        for (int i = 0; i < data[0].length; i++) {
            String label = "";
            if (i == data[0].length - 1) {
                label = "Avg";
            } else {
                label = "Run " + (i + 1);
            }
            jtp.add(label, createPane2(i, type));

        }

    }

    /**
     * Create a picture of the current chart
     *
     * @param tab
     */
    public void createChartPic(int tab) {
        String runNum = jtp.getTitleAt(tab);
        String fn = printDir + fileName + "-" + TITLE + "-" + runNum + ".jpg";
        try {
            ChartUtilities.saveChartAsJPEG(new File(fn), charts.get(tab), 700, 350);
        } catch (Exception e) {
            System.out.println("Error creating chart picture: " + e.getMessage());
        }
    }

    /**
     * Run method
     */
    @Override
    public void run() {

        // Updating
        if (typeOfGraphs == 1) {
            this.display();
        } else if (typeOfGraphs == 2) {
            // other type
            this.display2();
        } else {
            this.display3();
        }

    }
}
