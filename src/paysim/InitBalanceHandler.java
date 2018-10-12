package paysim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import static java.lang.Math.abs;

public class InitBalanceHandler {
    private ArrayList<String> fileContents = new ArrayList<>();
    private ArrayList<Double> probArr;
    private PaySim paysim;

    public InitBalanceHandler(String fileName) {
        try {
            File f = new File(fileName);
            FileReader fReader = new FileReader(f);
            BufferedReader reader = new BufferedReader(fReader);
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                fileContents.add(line);
            }
            reader.close();
            probArr = new ArrayList<>();
            for (String l : fileContents) {
                probArr.add(Double.parseDouble(l.split(",")[2]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getBalance() {
        double balance = 0;
        int index = 0;

        double randNr = paysim.random.nextDouble();
        double total = 0;

        //Get the index
        for (int i = 0; i < probArr.size(); i++) {
            double currProb = probArr.get(i);

            if (total <= randNr && randNr <= (total + currProb)) {
                //In the special case there are no debits
                index = i;
                break;
            } else {
                total += currProb;
            }
        }

        //With the index, get the high and the low end of the balance to be generated
        String record = fileContents.get(index);
        String split[] = record.split(",");
        double high = Double.parseDouble(split[1]);
        double low = Double.parseDouble(split[0]);
        double diff = high - low;
        double randed = abs(paysim.random.nextLong() % diff);

        balance = randed + low;
        return balance;
    }


    public void setPaysim(PaySim paysim) {
        this.paysim = paysim;
    }
}
