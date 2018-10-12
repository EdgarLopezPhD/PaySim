package paysim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Properties;

import paysim.actors.Client;
import paysim.actors.Fraudster;
import paysim.actors.Merchant;
import paysim.aggregation.AggregateDumpAnalyzer;
import paysim.aggregation.AggregateDumpHandler;
import paysim.aggregation.AggregateParamFileCreator;
import paysim.aggregation.AggregateTransactionRecord;
import sim.engine.SimState;

public class PaySim extends SimState {
    public static final double PAYSIM_VERSION = 1.0;
    private static double multiplier = 1.0;
    ArrayList<AggregateTransactionRecord> aggrTransRecordList = new ArrayList<>();
    AggregateParamFileCreator aggregateCreator = new AggregateParamFileCreator();
    ProbabilityContainerHandler probabilityContainerHandler = new ProbabilityContainerHandler();

    ArrayList<Transaction> trans = new ArrayList<>();
    ArrayList<Merchant> merchants = new ArrayList<>();
    ArrayList<Fraudster> fraudsters = new ArrayList<>();
    public ArrayList<Client> clients = new ArrayList<>();

    ArrayList<String> paramFileList = new ArrayList<>();
    ArrayList<String> actionTypes = new ArrayList<>();
    BufferedWriter writer;
    String logFileName = "";

    InitBalanceHandler balanceHandler;
    TransferMaxHandler handlerMax;
    CurrentStepHandler stepHandler;

    long seed = 0;
    int nrOfSteps = 0;
    long startTime = 0;
    long endTime = 0;
    double probDoDeposit = 0;
    double probDoTransfer = 0;
    double probDoWithdraw = 0;
    double probDoNothing = 0;
    double totalErrorRate = 0;
    double totalTransactionsMade = 0;

    String propertiesPath = "";
    double probDoFromVoucher = 0;
    double probDoToVoucher = 0;
    double probDoPayment = 0;
    double probDoExpireVoucher = 0;
    double fraudProbability = 0.0;
    int nrOfClients = 0;
    int nrOfMerchants = 0;
    int numFraudsters = 0;
    public double transferLimit = 100000;
    Properties parameters;
    private double day, hour;
    String parameterFilePath = "";
    String pathOutput = "/outputs/";
    String tagName = "def";
    String logPath = "";
    String aggregateParameterPath = "";
    String transferMaxPath = "";
    String paramFileHistoryPath = "";
    String dbUrl = "";
    String dbUser = "";
    String dbPassword = "";
    String balanceHandlerFilePath = "";
    String networkPath = "";
    String transferFreqMod = "";
    String transferFreqModInit = "";
    public boolean debugFlag = false;
    boolean saveToDbFlag = false;
    boolean networkFlag = false;
    RepetitionFreqHandler repFreqHandler = new RepetitionFreqHandler();

    public static void main(String args[]) {
        doLoop(PaySim.class, args);
        System.exit(0);
    }

    public PaySim(long seed) {
        super(seed);
    }

    public void start() {
        System.out.println(this.welcome());
        startTime = System.currentTimeMillis();
        super.start();
        initSimulation();
    }

    public void refresh() {
        aggrTransRecordList = new ArrayList<>();
        aggregateCreator = new AggregateParamFileCreator();

        trans = new ArrayList<>();
        merchants = new ArrayList<>();
        clients = new ArrayList<>();
        paramFileList = new ArrayList<>();
        actionTypes = new ArrayList<>();
        balanceHandler = null;
        handlerMax = null;
        stepHandler = null;
        seed = 0;
        nrOfSteps = 0;
        startTime = 0;
        endTime = 0;
        probDoDeposit = 0;
        probDoTransfer = 0;
        probDoWithdraw = 0;
        probDoNothing = 0;
        probDoFromVoucher = 0;
        probDoToVoucher = 0;
        probDoPayment = 0;
        probDoExpireVoucher = 0;
        nrOfClients = 0;
        nrOfMerchants = 0;
        day = 0;
        hour = 0;
        parameterFilePath = "";
        tagName = "def";
        logPath = "";
        fraudProbability = 0.0;
        aggregateParameterPath = "";
        transferMaxPath = "";
        paramFileHistoryPath = "";
        dbUrl = "";
        dbUser = "";
        dbPassword = "";
        balanceHandlerFilePath = "";
    }

    public void finish() {
        writeLog();
        writeFraudsters();
        writeAggregateParamFile();

        writeParamfileHistory();
        if (networkFlag) {
            writeNetworkResults();
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writeErrorTable();
        writeSummaryFile();
        appendSimulationSummary();
        dumpRepetitionFreq();
        System.out.println("\nNrOfFailed\t" + Manager.nrFailed + "\n" +
                "NrOfTrueClients:\t" + (Manager.trueNrOfClients * this.getMultiplier()) + "\n"
                + "NrOfDaysParticipated\t" + Manager.nrOfDaysParticipated + "\n");

    }

    public Merchant getRandomMerchant() {
        Merchant m = getMerchants().get(random.nextInt(getMerchants().size()));
        return m;
    }

    public Client getRandomClient() {
        if (getClients().size() > 0) {
            Client c = getClients().get(random.nextInt(getClients().size()));
            return c;
        }
        return null;
    }

    private void dumpRepetitionFreq() {
        DecimalFormat format = new DecimalFormat("#.###");
        ArrayList<RepetitionFreqContainer> contList = this.repFreqHandler.freqContList;
        ArrayList<String> allProbs = listOfProbs();
        File f = new File(System.getProperty("user.dir") + this.pathOutput + ParameterizedPaySim.simulatorName
                + "//" + ParameterizedPaySim.simulatorName + "_repetitionFrequency.csv");
        try {
            FileWriter fWriter = new FileWriter(f);
            BufferedWriter bufWriter = new BufferedWriter(fWriter);
            bufWriter.write("type,high,low,total,freq" + "\n");

            //Loop through the entire list of repetionContainers and find the match
            for (int i = 1; i < allProbs.size(); i++) {
                String s = allProbs.get(i);
                String splitted[] = s.split(",");
                int low = Integer.parseInt(splitted[1]);
                int high = Integer.parseInt(splitted[2]);
                double total = getTotalFromType(splitted[0], contList);
                RepetitionFreqContainer contHarnessed = getCont(splitted[0], high, low, contList);
                if (contHarnessed != null) {
                    bufWriter.write(splitted[0] + "," + low + "," + high + ","
                            + contHarnessed.getFreq() + ","
                            + format.format((((double) contHarnessed.getFreq()) / total))
                            + "\n");
                } else {
                    bufWriter.write(splitted[0] + "," + low + "," + high + "," + "0" + "," + 0 + "\n");
                }
            }
            bufWriter.close();
            fWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getFraudProbability() {
        return fraudProbability;
    }

    private double getTotalFromType(String type, ArrayList<RepetitionFreqContainer> contList) {
        double total = 0;
        for (RepetitionFreqContainer s : contList) {
            if (s.getCont().getType().equals(type)) {
                total += s.getFreq();
            }
        }
        return total;
    }

    private ArrayList<String> listOfProbs() {
        ArrayList<String> allContents = new ArrayList<String>();
        File f = new File(transferFreqMod);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = reader.readLine()) != null) {
                allContents.add(line);
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return allContents;
    }

    private RepetitionFreqContainer getCont(String typeInput, int high, int low, ArrayList<RepetitionFreqContainer> freqCont) {
        for (RepetitionFreqContainer temp : freqCont) {
            if (temp.getCont().getHigh() == high &&
                    temp.getCont().getLow() == low &&
                    temp.getCont().getType().equals(typeInput)) {
                return temp;
            }
        }
        return null;
    }

    private void appendSimulationSummary() {
        //SimulationLogName, NumSteps, totalTransactions, totalError
        String header = "name,steps,totNrOfTransactions,totNrOfClients,totError\n";
        String summary = ParameterizedPaySim.simulatorName + "," + ParameterizedPaySim.numOfSteps + "," + this.totalTransactionsMade + ","
                + this.clients.size() + "," + this.totalErrorRate + "\n";
        File f = new File(System.getProperty("user.dir") + this.pathOutput + "summary.csv");

        //If the file exists
        if (f.exists() && !f.isDirectory()) {

            //Read everything first
            ArrayList<String> allContents = new ArrayList<String>();
            try {
                FileReader fWriter = new FileReader(f);
                BufferedReader bufReader = new BufferedReader(fWriter);
                String line = "";
                while ((line = bufReader.readLine()) != null) {
                    allContents.add(line + "\n");
                }
                bufReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            allContents.add(summary);

            try {
                FileWriter fWriter = new FileWriter(f);
                BufferedWriter bufWriter = new BufferedWriter(fWriter);
                for (String line : allContents) {
                    bufWriter.append(line);
                }
                bufWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //If the file does not exist
        } else {
            //Read everything first
            ArrayList<String> allContents = new ArrayList<String>();
            try {
                f.createNewFile();
                FileReader fWriter = new FileReader(f);
                BufferedReader bufReader = new BufferedReader(fWriter);
                String line = "";
                allContents.add(header);
                while ((line = bufReader.readLine()) != null) {
                    allContents.add(line + "\n");
                }
                bufReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            allContents.add(summary);
            try {
                FileWriter fWriter = new FileWriter(f);
                BufferedWriter bufWriter = new BufferedWriter(fWriter);
                for (String line : allContents) {
                    bufWriter.append(line);
                }
                bufWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private String getStringRepresentationFlags() {
        String flagRepresentation = "";
        if (this.debugFlag) {
            flagRepresentation += "debugFlag=" + "1\n";
        } else {
            flagRepresentation += "debugFlag=" + "0\n";
        }

        if (this.saveToDbFlag) {
            flagRepresentation += "saveToDbFlag=" + "1\n";
        } else {
            flagRepresentation += "saveToDbFlag=" + "0\n";
        }

        if (this.networkFlag) {
            flagRepresentation += "saveNetwork=" + "1\n";
        } else {
            flagRepresentation += "saveNetwork=" + "0\n";
        }
        return flagRepresentation;
    }

    private void writeParamfileHistory() {
        endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        try {
            File f = new File(System.getProperty("user.dir") + this.pathOutput + ParameterizedPaySim.simulatorName + "//"
                    + ParameterizedPaySim.simulatorName + "_ParamHistory" + ".txt");
            FileWriter writer = new FileWriter(f);
            BufferedWriter bufWrtier = new BufferedWriter(writer);

            String toWrite =
                    "nrOfMerchants=" + this.nrOfMerchants + "\n" +
                            "seed=" + this.seed + "\n" +
                            "multiplier=" + this.getMultiplier() + "\n" +
                            "parameterFilePath=" + this.parameterFilePath.replace(System.getProperty("user.dir"), "") + "\n" +
                            "aggregateParameterFilePath=" + this.aggregateParameterPath.replace(System.getProperty("user.dir"), "") + "\n" +
                            "transferMaxPath=" + this.transferMaxPath.replace(System.getProperty("user.dir"), "") + "\n" +
                            "logPath=" + this.logPath.replace(System.getProperty("user.dir"), "") + "\n" +
                            "paramFileHistory=" + this.paramFileHistoryPath.replace(System.getProperty("user.dir"), "") + "\n" +
                            "balanceHandler=" + this.balanceHandlerFilePath.replace(System.getProperty("user.dir"), "") + "\n" +
                            "transferFreqMod=" + this.transferFreqMod.replace(System.getProperty("user.dir"), "") + "\n" +
                            "networkFolderPath=" + this.networkPath + "\n" +
                            "dbUrl=" + this.dbUrl + "\n" +
                            "dbUser=" + this.dbUser + "\n" +
                            "dbPassword=" + this.dbPassword + "\n" +
                            "fraudProbability=" + this.fraudProbability + "\n" +
                            "transferLimit=" + this.transferLimit + "\n" +
                            "numFraudsters=" + this.numFraudsters + "\n" +
                            this.getStringRepresentationFlags() +
                            "TotalPassedTime=" + totalTime + " MS";
            bufWrtier.write(toWrite);
            bufWrtier.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeDatabaseLog() {
        if (debugFlag) {
            System.out.println("\nWriting databse logs...\n");
        }

        DatabaseHandler handler = new DatabaseHandler(this.dbUrl, this.dbUser, this.dbPassword);
        for (Transaction t : this.trans) {
            handler.insert(t);
        }

    }

    private void writeErrorTable() {
        String filePathOrig = System.getProperty("user.dir") + "//paramFiles//AggregateTransaction.csv";
        String filePathSynth = System.getProperty("user.dir") + this.pathOutput +
                ParameterizedPaySim.simulatorName + "//" + ParameterizedPaySim.simulatorName + "_AggregateParamDump.csv";

        if (debugFlag) {
            System.out.println("Orig:\t" + filePathOrig + "\nSynth:\t" + filePathSynth + "\n");
        }


        AggregateDumpHandler aggrHandler = new AggregateDumpHandler();
        AggregateDumpAnalyzer analyzerOrig = new AggregateDumpAnalyzer(filePathOrig);
        AggregateDumpAnalyzer analyzerSynth = new AggregateDumpAnalyzer(filePathSynth);
        analyzerOrig.analyze();
        analyzerSynth.analyze();
        String resultDump = aggrHandler.checkDelta(analyzerOrig, analyzerSynth);
        this.totalErrorRate = aggrHandler.getTotalErrorRate();

        try {
            File f = new File(System.getProperty("user.dir") + this.pathOutput + ParameterizedPaySim.simulatorName + "//"
                    + ParameterizedPaySim.simulatorName + "_ErrorTable" + ".txt");
            FileWriter writer = new FileWriter(f);
            BufferedWriter bufWrtier = new BufferedWriter(writer);
            bufWrtier.write(resultDump);
            bufWrtier.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void writeFraudsters() {
        String filePath = System.getProperty("user.dir") + this.pathOutput +
                ParameterizedPaySim.simulatorName + "//" + ParameterizedPaySim.simulatorName + "_Fraudsters.csv";

        String header = "fname,numVictims,profit";

        try {
            File f = new File(filePath);
            FileWriter writer = new FileWriter(f);
            BufferedWriter bufWrtier = new BufferedWriter(writer);

            bufWrtier.write(header);

            for (Fraudster fraud : this.fraudsters) {
                String row = fraud.getName() + "," + fraud.clientsAffected + "," + fraud.profit;
                bufWrtier.write(row);
            }


            bufWrtier.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getTypeFromNumber(int number) {
        switch (number) {
            case 1:
                return "CASH_IN";

            case 2:
                return "CASH_OUT";

            case 3:
                return "DEBIT";

            case 4:
                return "DEPOSIT";

            case 5:
                return "PAYMENT";

            case 6:
                return "TRANSFER";

            default:
                return null;

        }

    }

    public void writeLog() {
        try {
            //Write the header
            if (debugFlag) {
                System.out.println("Size of transactionList: " + this.trans.size() + "\n");
            }
            FileWriter writer1 = new FileWriter(new File(this.logFileName), true);
            this.writer = new BufferedWriter(writer1);
            //Append the logs to the file
            for (int i = 0; i < this.trans.size(); i++) {
                Transaction temp = this.trans.get(i);
                String typeFromNumber = getTypeFromNumber(temp.getType());
                if (typeFromNumber.equals("PAYMENT")) {


                    this.writer.write(
                            temp.getStep() +
                                    "," + getTypeFromNumber(temp.getType()) +
                                    "," + getDoublePrecision(2, temp.getAmount()) +
                                    "," + temp.getClientOrigBefore().getName() +
                                    "," + getDoublePrecision(2, temp.getClientOrigBefore().getBalance()) +
                                    "," + getDoublePrecision(2, temp.getClientOrigAfter().getBalance()) +
                                    "," + temp.getMerchantBefore().getName() +
                                    "," + getDoublePrecision(2, temp.getMerchantBefore().getBalance()) +
                                    "," + getDoublePrecision(2, temp.getMerchantAfter().getBalance()) +
                                    "," + (temp.isFraud() ? 1 : 0) +
                                    "," + (temp.isFlaggedFraud() ? 1 : 0) + "\n");
                } else {

                    this.writer.write(
                            temp.getStep() +
                                    "," + getTypeFromNumber(temp.getType()) +
                                    "," + getDoublePrecision(2, temp.getAmount()) +
                                    "," + temp.getClientOrigBefore().getName() +
                                    "," + getDoublePrecision(2, temp.getClientOrigBefore().getBalance()) +
                                    "," + getDoublePrecision(2, temp.getClientOrigAfter().getBalance()) +
                                    "," + temp.getClientDestAfter().getName() +
                                    "," + getDoublePrecision(2, temp.getClientDestBefore().getBalance()) +
                                    "," + getDoublePrecision(2, temp.getClientDestAfter().getBalance()) +
                                    "," + (temp.isFraud() ? 1 : 0) +
                                    "," + (temp.isFlaggedFraud() ? 1 : 0) + "\n");
                }

            }
            if (debugFlag) {
                System.out.println("Finished Writing: " + "\n");
            }

            this.writer.close();
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
        }

    }

    private void writeAggregateParamFile() {
        DecimalFormat df = new DecimalFormat("#.##");
        try {
            BufferedWriter paramDump = new BufferedWriter(new FileWriter(new File(
                    System.getProperty("user.dir") + this.pathOutput + ParameterizedPaySim.simulatorName
                            + "//" + ParameterizedPaySim.simulatorName + "_AggregateParamDump.csv")));
            paramDump.write("type,tmonth,tday,thour,tcount,tsum,tavg,tstd,step\n");

            //Get the aggregateparamrecords
//			AggregateParamFileCreator aggregateCreator = new AggregateParamFileCreator();
//			ArrayList<AggregateTransactionRecord> aggrRecordList = aggregateCreator.generateAggregateParamFile(this.trans);			
//			this.aggrTransRecordList = aggrRecordList;

            ArrayList<AggregateTransactionRecord> reformatted = new ArrayList<AggregateTransactionRecord>();
            java.util.Collections.sort(this.aggrTransRecordList);

            reformatted = this.aggregateCreator.reformat(this.aggrTransRecordList);
            java.util.Collections.sort(reformatted);
            double sizeOfTrans = 0;
            for (AggregateTransactionRecord record : reformatted) {
                sizeOfTrans += Double.parseDouble(record.gettCount());
            }
            if (debugFlag) {
                System.out.println("SIzeOfTransList:\t" + sizeOfTrans + "\n");
                System.out.println("TrueNrOfRepetitions:\t"
                        + Manager.trueNrOfRepetitions + "\n");

            }

            for (AggregateTransactionRecord record : reformatted) {
                paramDump.write(record.getType() + "," +
                        record.getMonth() + "," +
                        record.gettDay() + "," +
                        record.gettHour() + "," +
                        record.gettCount() + "," +
                        df.format(Double.parseDouble(record.gettSum())) + "," +
                        df.format(Double.parseDouble(record.gettAvg())) + "," +
                        df.format(Double.parseDouble(record.gettStd())) + "," +
                        generateStep(record) + "\n"
                );
            }

            paramDump.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int generateStep(AggregateTransactionRecord record) {
        int step = 0;
        step = (int) (((Double.parseDouble(record.gettDay()) - 1) * 24) + (Double.parseDouble(record.gettHour()) + 1));
        return step;
    }

    public void initSimulation() {
        System.out.println("InInit  TagName:\t" + this.tagName + "\nNrOfMerchants:\t" + nrOfMerchants + "\nSeed:\t" + seed + "\nparameterFilePath\t" + parameterFilePath + "\n");
        this.setSeed(this.seed);
        balanceHandler = new InitBalanceHandler(this.balanceHandlerFilePath);
        handlerMax = new TransferMaxHandler(transferMaxPath);
        System.out.println("Inputting this paramfile:\n");
        stepHandler = new CurrentStepHandler(this.paramFileList, getMultiplier());

        balanceHandler.setPaysim(this);
        this.probabilityContainerHandler.initRecordList(this.paramFileList);
        Manager manager = new Manager();
        manager.setBalanceHandler(balanceHandler);
        manager.setNrOfStepsTotal(this.nrOfSteps);
        manager.setTransferMaxHandler(handlerMax);
        manager.setStepHandler(stepHandler);
        manager.setProbabilityHandler(probabilityContainerHandler);
        manager.setRepFreqHandler(this.repFreqHandler);

        //Add the merchants
        System.out.println("NrOfMerchants:\t" + this.nrOfMerchants * this.getMultiplier() + "\n");
        for (int i = 0; i < this.nrOfMerchants * this.getMultiplier(); i++) {
            Merchant m = new Merchant();
            //m.setName(String.valueOf(String.valueOf(System.currentTimeMillis()).hashCode()));
            this.merchants.add(m);
        }

        //Add the merchants
        System.out.println("NrOfFraudsters:\t" + this.numFraudsters * this.getMultiplier() + "\n");
        for (int i = 0; i < this.numFraudsters * this.getMultiplier(); i++) {
            Fraudster f = new Fraudster();
            //m.setName(String.valueOf(String.valueOf(System.currentTimeMillis()).hashCode()));
            this.fraudsters.add(f);
            schedule.scheduleRepeating(f);
        }

        //Start the manager
        //System.out.println("Over Here\n");
        schedule.scheduleRepeating(manager);

    }

    public void resetVariables() {
        this.trans = new ArrayList<Transaction>();
        //this.clients = new ArrayList<Client>();
    }


    private double getDoublePrecision(int precision, double d) {
        Double toBeTruncated = new Double(d);
        Double truncatedDouble = new BigDecimal(toBeTruncated).
                setScale(precision, BigDecimal.ROUND_HALF_UP).
                doubleValue();
        return truncatedDouble;
    }

    public String toString() {
        return "ProbDoWithdraw: " + this.probDoWithdraw + "\nProbDoDeposit: " + this.probDoDeposit
                + "ProbDoNothing: " + this.probDoNothing + "\n" + "ProbDoPayment: " + this.probDoPayment + "\n" + "ProbDoTransfer: " + this.probDoTransfer
                + "\n" + "probDoFromVoucher: " + this.probDoFromVoucher + "\nProbDoToVoucher: " + this.probDoToVoucher + "\nProbDoPayment: " + this.probDoPayment
                + "\nprobDoExpireVoucher: " + this.probDoExpireVoucher + "\n"
                + "NrOfClients: " + this.nrOfClients + "\nNrOfMerchants: " + this.nrOfMerchants +
                "NrOfSteps: " + this.nrOfSteps + "\n";
    }

    public String welcome() {
        return "PAYSIM: Financial Simulator v" + this.PAYSIM_VERSION + " \n ";

    }

    public void setPropertiesFile(String s) {
        this.propertiesPath = s;
    }

    private void initSeed() {

        String seedString = String.valueOf(this.parameters.getProperty("seed"));
        if (seedString.equals("time")) {
            this.seed = System.currentTimeMillis();
        } else {
            this.seed = Long.parseLong(seedString);
        }
    }

    private void initFlags() {
        debugFlag = parameters.getProperty("debugFlag").equals("1");
        networkFlag = parameters.getProperty("saveNetwork").equals("1");
        saveToDbFlag = parameters.getProperty("saveToDbFlag").equals("1");
    }

    //Loads parameters from a given filepath in the argument list of the invocation
    public void loadParametersFromFile() {
        try {
            this.parameters = new Properties();
            InputStream inputStream = new FileInputStream(new File(this.propertiesPath));

            if (inputStream != null) {
                this.parameters.load(inputStream);
            }

            // get the property value and print it out
//			this.nrOfClients = Integer.parseInt(this.parameters.getProperty("nrOfClients"));
//			this.nrOfMerchants = Integer.parseInt(this.parameters.getProperty("nrOfMerchants"));
//			this.nrOfSteps = Integer.parseInt(this.parameters.getProperty("nrOfSteps"));
            this.nrOfMerchants = Integer.parseInt(this.parameters.getProperty("nrOfMerchants"));
            this.numFraudsters = Integer.parseInt(this.parameters.getProperty("numFraudsters"));
            this.fraudProbability = Double.parseDouble(this.parameters.getProperty("fraudProbability"));
            this.transferLimit = Double.parseDouble(this.parameters.getProperty("transferLimit"));
            this.parameterFilePath = System.getProperty("user.dir") + this.parameters.getProperty("parameterFilePath");
            this.logPath = this.parameters.getProperty("logPath");
            this.aggregateParameterPath = System.getProperty("user.dir") + this.parameters.getProperty("aggregateParameterFilePath");
            this.setMultiplier(Double.parseDouble(this.parameters.getProperty("multiplier")));
            this.transferMaxPath = System.getProperty("user.dir") + this.parameters.getProperty("transferMaxPath");
            this.paramFileHistoryPath = this.parameters.getProperty("paramFileHistory");
            this.dbUser = this.parameters.getProperty("dbUser");
            this.dbPassword = this.parameters.getProperty("dbPassword");
            this.dbUrl = this.parameters.getProperty("dbUrl");
            this.balanceHandlerFilePath = System.getProperty("user.dir") + this.parameters.getProperty("balanceHandler");
            this.networkPath = this.parameters.getProperty("networkFolderPath");
            this.transferFreqMod = System.getProperty("user.dir") + this.parameters.getProperty("transferFreqMod");
            this.transferFreqModInit = System.getProperty("user.dir") + this.parameters.getProperty("transferFreqModInit");

            initSeed();
            initFlags();

            //System.out.println("NrOfMerchants:\t" + nrOfMerchants + "\nSeed:\t" + seed + "\nparameterFilePath\t" + parameterFilePath + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //Load the probabilities into the probability array
    public double[] loadProbabilites(ArrayList<ActionProbability> list, int nrOfClients) {
        double ProbabilityArr[] = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            if (this.debugFlag) {
                System.out.println("Dividing:\t" + (list.get(i).getNrOfTransactions() * getMultiplier()) + "\tBy:\t" + ((double) nrOfClients + "\n"));
            }
            double currProb = (list.get(i).getNrOfTransactions() * getMultiplier()) / ((double) nrOfClients);
            ProbabilityArr[i] = currProb;
        }
        return ProbabilityArr;
    }

    private void writeNetworkResults() {
        ArrayList<String> fileContents = new ArrayList<String>();
        try {

            File f = new File(System.getProperty("user.dir") + this.pathOutput + ParameterizedPaySim.simulatorName
                    + "//" + ParameterizedPaySim.simulatorName + "_log.txt");
            FileReader reader = new FileReader(f);
            BufferedReader bufReader = new BufferedReader(reader);
            String line = "";
            bufReader.readLine();
            while ((line = bufReader.readLine()) != null) {
                fileContents.add(line);
            }
            bufReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Create the network contents
        ArrayList<String> networkResults = new ArrayList<String>();
        networkResults.add("type,from,to,amount");
        for (int i = 0; i < fileContents.size(); i++) {
            String line = fileContents.get(i);
            String splittedLine[] = line.split(",");


            try {
                String toAdd = splittedLine[0] + "," + splittedLine[1] + "," + splittedLine[5] + "," + splittedLine[2];
                networkResults.add(toAdd);
            } catch (Exception e) {

            }

        }

        //Create the dumpfile to write to
        BufferedWriter bufWriter = null;
        try {
            File f = new File(System.getProperty("user.dir") + this.pathOutput + ParameterizedPaySim.simulatorName
                    + "//" + ParameterizedPaySim.simulatorName + "_networkdump.txt");
            FileWriter writer = new FileWriter(f);
            bufWriter = new BufferedWriter(writer);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Write the actual network contents
        for (int i = 0; i < networkResults.size(); i++) {
            try {
                bufWriter.write(networkResults.get(i));
                bufWriter.newLine();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

    }

    public void writeSummaryFile() {
        String fileName = System.getProperty("user.dir")
                + "/paramFiles/AggregateTransaction.csv";

        ArrayList<String> fileContentsOrig = new ArrayList<String>();
        try {
            File f = new File(fileName);
            FileReader fReader = new FileReader(f);
            BufferedReader reader = new BufferedReader(fReader);
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                fileContentsOrig.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> fileContentsSynth = new ArrayList<String>();
        try {
            File f = new File(System.getProperty("user.dir") + this.pathOutput + ParameterizedPaySim.simulatorName
                    + "//" + ParameterizedPaySim.simulatorName + "_AggregateParamDump.csv");
            FileReader fReader = new FileReader(f);
            BufferedReader reader = new BufferedReader(fReader);
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                fileContentsSynth.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        String spaceBegin = "                    ";
        String result = spaceBegin + "Orig" + spaceBegin + "\tSynthetic" + spaceBegin + "\n";
        result += spaceBegin + "Sum" + spaceBegin + "\tSum" + "\n";
        double countOrig = 0;
        double countSynth = 0;
        double sumOrig = 0;
        double sumSynth = 0;
        ArrayList<String> types = new ArrayList<String>();
        DecimalFormat format = new DecimalFormat("#.##############");
        types.add("CASH_IN");
        types.add("CASH_OUT");
        types.add("TRANSFER");
        types.add("PAYMENT");
        types.add("DEBIT");

        for (int k = 0; k < types.size(); k++) {
            String currTypeOrig = types.get(k);
            sumOrig += getCumulative(currTypeOrig, 5, fileContentsOrig);
            sumSynth += getCumulative(String.valueOf(k + 1), 5, fileContentsSynth);
            result += currTypeOrig + spaceBegin.substring(currTypeOrig.length(), spaceBegin.length())
                    + format.format(sumOrig) + spaceBegin.substring(String.valueOf(sumOrig).length(), spaceBegin.length())
                    + "\t\t" + format.format(sumSynth) + "\n";
        }

        result += "-----------------------------------------------------------------------------\n";
        result += spaceBegin + "Count" + spaceBegin + "\tCount" + spaceBegin + "\n";

        for (int k = 0; k < types.size(); k++) {
            String currTypeOrig = types.get(k);
            countOrig += getCumulative(currTypeOrig, 4, fileContentsOrig);
            countSynth += getCumulative(String.valueOf(k + 1), 4, fileContentsSynth);

            totalTransactionsMade += countSynth;
            result += currTypeOrig + spaceBegin.substring(currTypeOrig.length(), spaceBegin.length())
                    + format.format(countOrig) + spaceBegin.substring(String.valueOf(sumOrig).length(), spaceBegin.length())
                    + "\t\t\t" + format.format(countSynth) + "\n";
        }


        try {
            File f = new File(System.getProperty("user.dir") + this.pathOutput + ParameterizedPaySim.simulatorName
                    + "//" + ParameterizedPaySim.simulatorName + "_Summary.csv");
            FileWriter fWriter = new FileWriter(f);
            BufferedWriter writer = new BufferedWriter(fWriter);
            writer.append(result);
            writer.close();
            fWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);

    }

    public static double getCumulative(String type, int rowIndex, ArrayList<String> fileContents) {
        boolean isInt = false;
        try {
            Integer.parseInt(type);
            isInt = true;
        } catch (Exception e) {

        }

        double aggr = 0;
        for (int i = 0; i < fileContents.size(); i++) {
            String row = fileContents.get(i);
            String split[] = row.split(",");
            String currType = split[0];
            if (isInt) {
                double alterDouble = Double.parseDouble(currType);
                int alterInt = (int) alterDouble;
                currType = String.valueOf(alterInt);
            }
            if (currType.equals(type)) {
                aggr += Double.parseDouble(split[rowIndex]);
            }
        }
        return aggr;

    }

    public ArrayList<Transaction> getTrans() {
        return trans;
    }

    public void setNrOfSteps(int numOfSteps) {
        this.nrOfSteps = numOfSteps;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public ArrayList<String> getParamFileList() {
        return paramFileList;
    }

    public void setActionTypes(ArrayList<String> actionTypes) {
        this.actionTypes = actionTypes;
    }

    public void setParamFileList(ArrayList<String> paramFileList) {
        this.paramFileList = paramFileList;
    }

    public void setWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    public ArrayList<Merchant> getMerchants() {
        return this.merchants;
    }

    public ArrayList<AggregateTransactionRecord> getAggrTransRecordList() {
        return aggrTransRecordList;
    }

    public AggregateParamFileCreator getAggregateCreator() {
        return aggregateCreator;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        PaySim.multiplier = multiplier;
    }
}