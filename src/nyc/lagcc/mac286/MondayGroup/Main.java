package nyc.lagcc.mac286.MondayGroup;

/**
 * MAC286 Monday Group: Anthony Fermin, Sukanta Deb, Shourave Barua
 * Program reads stock data from a CSV file and generates a new CSV file with
 * 20 day and 50 day simple moving averages appended to the charts.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    // generates a CSV file using an ArrayList<ArrayList<String>> data structure
    private static void generateCsvFile(ArrayList<ArrayList<String>> completeData)
    {
        try
        {
            // directory and filename of new csv file to be created
            FileWriter writer = new FileWriter("/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/src/nyc/lagcc/mac286/MondayGroup/tslaComplete.csv");

            // loops through each line of data
            for(int i = 0; i < completeData.size(); i++)
            {

                // saves currentLine and loops through each index(which corresponds to a column) in this ArrayList
                ArrayList<String> currentLine = completeData.get(i);
                for(int j = 0; j < currentLine.size(); j++){

                    // takes data from currentLine and appends to csv file
                    writer.append(currentLine.get(j));
                    writer.append(',');

                    /**
                     * goes to next line if current index(column) is the last in the currentLine
                     * AND if currentLine is not the last line in completeData
                     */
                    if(j == currentLine.size() - 1 && !(i == completeData.size())){
                        writer.append('\n');
                    }

                }
            }

            // finishes creation of csv file
            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }



    public static ArrayList<ArrayList<String>> fileParser(String fileName){

        // empty ArrayList of ArrayList<String> which stores each line of data
        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

        File file = new File(fileName);

        try {

            // new scanner for the file
            Scanner dataSearch = new Scanner(file);

            while(dataSearch.hasNextLine()) {

                // splits current line into columns
                String[] columns = dataSearch.nextLine().split(",");


                ArrayList<String> currentLine = new ArrayList<String>();

                // stores the separate columns pertaining to a line into ArrayList<String> currentLine
                for (int i = 0; i < columns.length; i++) {

                    currentLine.add(columns[i]);

                }

                // adds currentLine to data ArrayList
                data.add(currentLine);
            }

        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        return data;
    }

    public static void main(String[] args) {

        // file path of original CSV file with stock info
        String filePath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/src/nyc/lagcc/mac286/MondayGroup/tsla.csv";
        ArrayList<ArrayList<String>> table = fileParser(filePath);

        // Adds 20 Day SMA and 50 Day SMA columns to first Line
        ArrayList<String> firstLine = table.get(0);
        firstLine.add("20 Day SMA");
        firstLine.add("50 Day SMA");


        // computing and adding 20 day SMA
        for(int i = 20; i < table.size(); i++ ){

            // gets sum of 20 previous closes
            double sum = 0;
            for(int j = 0; j < 20; j++){
                sum+= Double.parseDouble(table.get(i - j).get(6));
            }

            // calculates average and formats to a maximum of 4 decimal places
            double average = sum / 20;
            average = (double)Math.round(average * 10000) / 10000;

            // adds current average to its corresponding line in data
            ArrayList<String> currentLine = table.get(i);
            currentLine.add(Double.toString(average));
        }


        // computing and adding 50 day SMA
        for(int i = 50; i < table.size(); i++ ){

            // gets sum of 50 previous closes
            double sum = 0;
            for(int j = 0; j < 50; j++){
                sum+= Double.parseDouble(table.get(i - j).get(6));
            }

            // calculates average and formats to a maximum of 4 decimal places
            double average = sum / 50;
            average = (double)Math.round(average * 10000) / 10000;

            // adds current average to its corresponding line in data
            ArrayList<String> currentLine = table.get(i);
            currentLine.add(Double.toString(average));
        }

        // adds new column called "Trade"
        firstLine = table.get(0);
        firstLine.add("Trade");

        // fills first two lines with blank cells
        ArrayList<String> tempLine = table.get(1);
        tempLine.add("");
        tempLine = table.get(2);
        tempLine.add("");

        /**
         * Labels cell as "Trade" if pattern is found
         * Example Pattern: =IF(C60<C59,IF(D60<D59,IF(D61>C60,"trade","n"),"n"),"n”)   w/0 Bull case
         */
        for(int i = 3; i < table.size(); i++){
            ArrayList<String> currentLine = table.get(i);
            ArrayList<String> twoDaysBeforeLine = table.get(i-2);
            ArrayList<String> oneDayBeforeLine = table.get(i-1);

            // storing and parsing info from each of the necessary cells
            double columnCTwoDaysBefore = Double.parseDouble(twoDaysBeforeLine.get(2));
            double columnDTwoDaysBefore = Double.parseDouble(twoDaysBeforeLine.get(3));
            double columnCOneDayBefore = Double.parseDouble(oneDayBeforeLine.get(2));
            double columnDOneDayBefore = Double.parseDouble(oneDayBeforeLine.get(3));
            double columnDCurrentDay = Double.parseDouble(currentLine.get(3));

            // if statement checks for pattern and labels current cell with "TRADE" if found or blank if not
            if(columnCOneDayBefore < columnCTwoDaysBefore
                    && columnDOneDayBefore < columnDTwoDaysBefore
                    && columnDCurrentDay > columnCOneDayBefore){

                currentLine.add("TRADE");

            }else{
                currentLine.add("");
            }

        }

        generateCsvFile(table);

    }
}
