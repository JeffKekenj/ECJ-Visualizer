/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visuallizer;

/**
 * Bucket class provides an easy way to transfer the data about the buckets
 * from the output reader to the graphcreator. It saves information of the each
 * run and how many items are in each of the 5 buckets, as well as strings
 * which display the range of the bucket.
 * @author Eric
 * @author Jeff
 */
public class Bucket {
    
    int [][] data;      // First array is for each of the runs, second is size 5
                        // where 0-4 are buckets containing the number of items
    String [] ranges;   // Range of the bucket

    
    public Bucket() {
    }
    
    public int[][] getData() {
        return data;
    }

    public void setData(int[][] data) {
        this.data = data;
    }

    public String[] getRanges() {
        return ranges;
    }

    public void setRanges(String[] ranges) {
        this.ranges = ranges;
    }
    
    
    
}
