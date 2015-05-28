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
    private static void generateCsvFile(ArrayList<ArrayList<String>> completeData, String fileName)
    {
        try
        {
            // directory and filename of new csv file to be created
            FileWriter writer = new FileWriter("/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/" + fileName + ".csv");

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

            // completes creation of csv file
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

                // splits currentLine into array by commas (columns)
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

    public static void populateSMA(ArrayList<ArrayList<String>> table){

        // Adds 20 Day SMA and 50 Day SMA columns to first Line
        ArrayList<String> firstLine = table.get(0);
        firstLine.add("20 Day SMA");
        firstLine.add("50 Day SMA");

        for(int i = 1; i < 20; i++){
            ArrayList<String> currentLine = table.get(i);
            currentLine.add("");
        }

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

        for(int i = 1; i < 50; i++){
            ArrayList<String> currentLine = table.get(i);
            currentLine.add("");
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

    }

    public static void addTradeColumn(ArrayList<ArrayList<String>> table){

        // adds new column called "Trade"
        ArrayList<String> firstLine = table.get(0);
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
        for(int i = 3; i < table.size(); i++)
        {
            ArrayList<String> currentDay = table.get(i);
            ArrayList<String> twoDaysBefore = table.get(i - 2);
            ArrayList<String> oneDayBefore = table.get(i - 1);

            // storing and parsing info from each of the necessary cells
            double highTwoDaysBefore = Double.parseDouble(twoDaysBefore.get(2));
            double lowTwoDaysBefore = Double.parseDouble(twoDaysBefore.get(3));
            double highPrevDay = Double.parseDouble(oneDayBefore.get(2));
            double lowPrevDay = Double.parseDouble(oneDayBefore.get(3));
            double highCurrentDay = Double.parseDouble(currentDay.get(3));

            // if statement checks for pattern and labels current cell with "TRADE" if found or blank if not
            if(highPrevDay < highTwoDaysBefore && lowPrevDay < lowTwoDaysBefore && highCurrentDay > highPrevDay)
            {
                currentDay.add("TRADE");
            }
            else
            {
                currentDay.add("");
            }
        }
    }

    public static ArrayList<ArrayList<String>> generateProfitLoss(ArrayList<ArrayList<String>> completeTable){

        double sumOfPercent = 0.0;
        int numOfWinners = 0;
        int numOfLosers = 0;
        String tradeCloseAmt = "";

        ArrayList<ArrayList<String>> tradeTable = new ArrayList<ArrayList<String>>();

        ArrayList<String> firstLine = new ArrayList<String>();
        firstLine.add("Date of Trade");
        firstLine.add("Entry Point");
        firstLine.add("Take Profit");
        firstLine.add("Stop Loss");
        firstLine.add("Date Closed");
        firstLine.add("Trade Close");
        firstLine.add("Win/Loss");
        firstLine.add("Percent Win/Loss");
        tradeTable.add(firstLine);

        for(int i = 1; i < completeTable.size(); i++)
        {

            ArrayList<String> currentLine = completeTable.get(i);
            if(currentLine.get(9).equalsIgnoreCase("TRADE") && completeTable.size() > i){
                ArrayList<String> nextLine = completeTable.get(i+1);
                ArrayList<String> tradeLine = new ArrayList<String>();
                ArrayList<String> secondCandle = completeTable.get(i-1);

                tradeLine.add(nextLine.get(0)); //date of trade

                double entryPoint = Double.parseDouble(nextLine.get(1));
                entryPoint = (double) Math.round(entryPoint * 10000) / 10000;
                tradeLine.add(nextLine.get(1)); //entry point (open of next day)

                double takeProfit = entryPoint + (entryPoint * .06);
                takeProfit = (double)Math.round(takeProfit * 10000) / 10000;
                tradeLine.add(Double.toString(takeProfit));

                double stopLoss = entryPoint - (entryPoint * .05);
                stopLoss = (double)Math.round(stopLoss * 10000) / 10000;
                tradeLine.add(Double.toString(stopLoss));

                Double winLoss = null;
                String closeDate = "";

                for(int j = 0; j < completeTable.size() - i; j++)
                {
                    ArrayList<String> lineToCheck = completeTable.get(i+j);
                    double checkOpen = Double.parseDouble(lineToCheck.get(1));
                    double checkClose = Double.parseDouble(lineToCheck.get(4));
                    double checkHigh = Double.parseDouble(lineToCheck.get(2));
                    double checkLow = Double.parseDouble(lineToCheck.get(3));
                    closeDate = lineToCheck.get(0);

                    if(checkOpen >= takeProfit){
                        winLoss = checkOpen - entryPoint;
                        tradeCloseAmt = lineToCheck.get(1);
                        numOfWinners++;
                        break;
                    }else if(checkHigh >= takeProfit){
                        winLoss = checkHigh - entryPoint;
                        tradeCloseAmt = lineToCheck.get(2);
                        numOfWinners++;
                        break;
                    }else if(checkClose >= takeProfit){
                        winLoss = checkClose - entryPoint;
                        tradeCloseAmt = lineToCheck.get(4);
                        numOfWinners++;
                        break;
                    }else if(checkOpen <= stopLoss){
                        winLoss = checkOpen - entryPoint;
                        tradeCloseAmt = lineToCheck.get(1);
                        numOfLosers++;
                        break;
                    }else if(checkLow <= stopLoss){
                        winLoss = checkLow - entryPoint;
                        tradeCloseAmt = lineToCheck.get(3);
                        numOfLosers++;
                        break;
                    }else if(checkClose <= stopLoss){
                        winLoss = checkClose - entryPoint;
                        tradeCloseAmt = lineToCheck.get(4);
                        numOfLosers++;
                        break;
                    }

                }

                if(winLoss == null){
                    tradeLine.add("Trade in Progress");
                    tradeLine.add("N/A");
                    tradeLine.add("N/A");
                    tradeLine.add("N/A");
                }else{

                    tradeLine.add(closeDate);
                    tradeLine.add(tradeCloseAmt);

                    winLoss = (double) Math.round(winLoss * 10000) / 10000;
                    tradeLine.add(Double.toString(winLoss));
                    double percentChange = winLoss * 100 / entryPoint;

                    if(!(i-2 < 20 || i-2 < 50))
                    {
                        double twentySMA = Double.parseDouble(secondCandle.get(7));
                        double fiftySMA = Double.parseDouble(secondCandle.get(8));

                        System.out.println(fiftySMA);
                        if(twentySMA < fiftySMA)
                        {
                            percentChange = percentChange * (- 1);

                            if(percentChange > 0)
                            {
                                numOfWinners++;
                                numOfLosers--;
                            }
                            else if(percentChange < 0)
                            {
                                numOfWinners--;
                                numOfLosers++;
                            }
                        }
                    }

                    sumOfPercent += percentChange;

                    percentChange = (double)Math.round(percentChange * 10000) / 10000;
                    tradeLine.add(Double.toString(percentChange) + " %");
                }

                tradeTable.add(tradeLine);
            }
        }

        ArrayList<String> emptyLine = new ArrayList<String>();
        for(int i = 0; i < 7; i++)
        {
            emptyLine.add("");
        }

        tradeTable.add(emptyLine);

        ArrayList<String> winnerLine = new ArrayList<String>();
        winnerLine.add("Number of Winners:");
        winnerLine.add(Integer.toString(numOfWinners));
        tradeTable.add(winnerLine);

        ArrayList<String> loserLine = new ArrayList<String>();
        loserLine.add("Number of Losers:");
        loserLine.add(Integer.toString(numOfLosers));
        tradeTable.add(loserLine);

        ArrayList<String> avgPercent = new ArrayList<String>();
        avgPercent.add("Average Percent Change:");
        int totalTrades = numOfLosers + numOfWinners;
        double avgPercentChange = sumOfPercent / totalTrades;
        avgPercentChange = (double)Math.round(avgPercentChange * 10000) / 10000;
        avgPercent.add(Double.toString(avgPercentChange) + " %");
        tradeTable.add(avgPercent);


        return tradeTable;
    }

    public static ArrayList<ArrayList<String>> reverseTable(ArrayList<ArrayList<String>> table){

        ArrayList<ArrayList<String>> revTable = new ArrayList<ArrayList<String>>();

        revTable.add(table.get(0));

        for(int i = table.size() - 1; i > 0 ; i--)
        {
            revTable.add(table.get(i));
        }

        return revTable;

    }

    public static void main(String[] args) {

        // file path of original CSV file with stock info

        String bbyPath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/bby.csv";
        ArrayList<ArrayList<String>> bbyTable = fileParser(bbyPath);
        populateSMA(bbyTable);
        addTradeColumn(bbyTable);
        generateCsvFile(bbyTable, "bbyComplete");
        generateCsvFile(generateProfitLoss(bbyTable), "bbyWinLoss");

        String cscoPath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/csco.csv";
        ArrayList<ArrayList<String>> cscoTable = fileParser(cscoPath);
        populateSMA(cscoTable);
        addTradeColumn(cscoTable);
        generateCsvFile(cscoTable, "cscoComplete");
        generateCsvFile(generateProfitLoss(cscoTable), "cscoWinLoss");

        String dowPath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/dow.csv";
        ArrayList<ArrayList<String>> dowTable = fileParser(dowPath);
        populateSMA(dowTable);
        addTradeColumn(dowTable);
        generateCsvFile(dowTable, "dowComplete");
        generateCsvFile(generateProfitLoss(dowTable), "dowWinLoss");

        String jpmPath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/jpm.csv";
        ArrayList<ArrayList<String>> jpmTable = fileParser(jpmPath);
        populateSMA(jpmTable);
        addTradeColumn(jpmTable);
        generateCsvFile(jpmTable, "jpmComplete");
        generateCsvFile(generateProfitLoss(jpmTable), "jpmWinLoss");

        String msftPath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/msft.csv";
        ArrayList<ArrayList<String>> msftTable = fileParser(msftPath);
        populateSMA(msftTable);
        addTradeColumn(msftTable);
        generateCsvFile(msftTable, "msftComplete");
        generateCsvFile(generateProfitLoss(msftTable), "msftWinLoss");


        String tslaPath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/tsla.csv";
        ArrayList<ArrayList<String>> tslaTable = fileParser(tslaPath);
        populateSMA(tslaTable);
        addTradeColumn(tslaTable);
        generateCsvFile(tslaTable, "tslaComplete");
        generateCsvFile(generateProfitLoss(tslaTable), "tslaWinLoss");

        String ibmPath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/ibm.csv";
        ArrayList<ArrayList<String>> ibmTable = fileParser(ibmPath);
        populateSMA(ibmTable);
        addTradeColumn(ibmTable);
        generateCsvFile(ibmTable, "ibmComplete");
        generateCsvFile(generateProfitLoss(ibmTable), "ibmWinLoss");

        String gldPath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/gld.csv";
        ArrayList<ArrayList<String>> gldTable = fileParser(gldPath);
        populateSMA(gldTable);
        addTradeColumn(gldTable);
        generateCsvFile(gldTable, "gldComplete");
        generateCsvFile(generateProfitLoss(gldTable), "gldWinLoss");

        String glwPath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/glw.csv";
        ArrayList<ArrayList<String>> glwTable = fileParser(glwPath);
        populateSMA(glwTable);
        addTradeColumn(glwTable);
        generateCsvFile(glwTable, "glwComplete");
        generateCsvFile(generateProfitLoss(glwTable), "glwWinLoss");

        String nflxPath = "/Users/c4q-anthonyf/Desktop/School/MAC286/StockProgram/res/nflx.csv";
        ArrayList<ArrayList<String>> nflxTable = fileParser(nflxPath);
        populateSMA(nflxTable);
        addTradeColumn(nflxTable);
        generateCsvFile(nflxTable, "nflxComplete");
        generateCsvFile(generateProfitLoss(nflxTable), "nflxWinLoss");





    }
}
