package paysim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;


public class ParameterizedPaySim extends PaySim{

	public static String simulatorName = "";
	ParameterizedPaySim parameterizedPaysim = null;
	ArrayList<String> paramFile = new ArrayList<String>();
	ArrayList<String> actions = new ArrayList<String>();
	BufferedWriter bufWriter;
	String filePath = "";
	long begin = 0;
	long end = 0;
	static int numOfSteps = 1;
	int numOfRepeat = 0;
	public static int currentLoop = 0;
	double incRepeat = 0;
	
	public ParameterizedPaySim(long seed) {
		super(seed);
		super.setTagName("1");
		// TODO Auto-generated constructor stub
	}
	
	public ParameterizedPaySim(){
		super(1);
		super.setTagName("1");
	}
	
	public void setCurrentLoop(int currentLoop){
		ParameterizedPaySim.currentLoop = currentLoop;
	}
	
	//Parse the arguments
	public void parseArgs(String args[]){
		//Parse the arguments given
		for (int x = 0; x < args.length - 1; x++){
			if (args[x].equals("-file")) {
				filePath = args[x + 1];
				super.setPropertiesFile(filePath);

				//Gets the number of steps
			} else if (args[x].equals("-for")) {
				numOfSteps = Integer.parseInt(args[x + 1]);
				//Gets the number of repetitions
			} else if (args[x].equals("-r")) {
				numOfRepeat = Integer.parseInt(args[x + 1]);
				//Gets the number of incrementations for each repetition
			} else if (args[x].equals("-inc")) {
				incRepeat = Double.parseDouble(args[x + 1]);
			}
		}
	}
	
	public void runSimulation(String args[]){
		parseArgs(args);
		//nrOfSteps = 744;
		numOfRepeat = 1;
		executeSimulation();
		
		
	}
	
	private void loadAggregatedFile(){
		// this function actually loads the aggregated parameter file
		paramFile = new ArrayList<String>();
		try {
			//System.out.println("Loading Parameters from:\t" + super.parameterFilePath);
			FileReader reader = new FileReader(new File(super.parameterFilePath));
			BufferedReader bufReader = new BufferedReader(reader);
			
			String tempLine = "";
			
			while((tempLine = bufReader.readLine()) != null){
				paramFile.add(tempLine);
			}
			bufReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.actions.add("CASH_IN");
		this.actions.add("CASH_OUT");
		this.actions.add("DEBIT");
		this.actions.add("DEPOSIT");
		this.actions.add("PAYMENT");
		this.actions.add("TRANSFER");
		
	}
	
	private void initBufWriter(String logFileName){
		long time = System.currentTimeMillis();
		try {
			FileWriter writer = new FileWriter(new File(logFileName));
			this.bufWriter = new BufferedWriter(writer);
			bufWriter.write("step,type,amount,nameOrig,oldbalanceOrg,newbalanceOrig,nameDest,oldbalanceDest,newbalanceDest,isFraud,isFlaggedFraud\n");
			bufWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	private void initSimulatorName(){
		Date d = new Date();
		ParameterizedPaySim.simulatorName = "PS_" + (d.getYear() + 1900) + (d.getMonth() + 1)  + d.getDate() + d.getHours() + d.getMinutes()
				+ d.getSeconds() + "_" + this.seed;
		//System.out.println(simulatorName + "\n");
		File f = new File(System.getProperty("user.dir")  +"//outputs//" + ParameterizedPaySim.simulatorName);
		f.mkdir();
	}
	
	public void executeSimulation(){
		//Load the parameters from the .property file
		super.loadParametersFromFile();
		
		// increase transfer limit with the current loop
		//this.transferLimit = this.transferLimit * (currentLoop+1)/2;
		initSimulatorName();
		loadAggregatedFile();	
		//Initiate the dumpfile output writer
		this.logFileName = System.getProperty("user.dir")  +"//outputs//" + ParameterizedPaySim.simulatorName
				+ "//" + ParameterizedPaySim.simulatorName + "_log.txt";
		initBufWriter(logFileName);
		//Input is day = i, h = hour
		//parameterizedPaysim = new ParameterizedPaySim();
		//System.out.println("NrOfMerchants:\t" + nrOfMerchants + "\nSeed:\t" + seed + "\nparameterFilePath\t" + parameterFilePath + "\n");
		
		//add the param list to the object
		if (debugFlag)
			System.out.println("Size of paramFile:\t" + this.paramFile.size() + "\n");
		setParamFileList(this.paramFile);
		
		//Set all of the possible actions that can be done
		setActionTypes(this.actions);
		
		//Add the writer to the simulator
		setWriter(this.bufWriter);
		
		//Starting the simulation
		setNrOfSteps(numOfSteps);
		super.start();
		begin = System.currentTimeMillis();
		System.out.println("Starting PaySim Running for "
				+ numOfSteps + " steps. Current loop:" + ParameterizedPaySim.currentLoop);

		
		long time;
		while ((time = (long) super.schedule.getSteps()) < numOfSteps) {
			if (!super.schedule.step(this))
				break;
			if (time % 100 == 0 && time != 0) {
				System.out.println("Time Step " + time);
			}
			else {
				System.out.print("*");	
			}
				
		}
		System.out.println(" - Finished running " + time + " steps ");	
		//Finishing the simulation
		super.finish();
		end = System.currentTimeMillis();
	
		double total = end - begin;
		total = total/1000;
		System.out.println("\nIt took:\t" + total/60 + " minutes to execute the simulation\n");
		System.out.println("Simulation name: " + this.simulatorName);
	}
	
	public void refresh(){
		System.out.println("Refreshing\n");
		super.refresh();
		this.paramFile = new ArrayList<String>();
		this.bufWriter = null;
	}
	
	
	public static void main(String args[]){
		
		int nrOfTimesRepeat = Integer.parseInt(args[5]);
		
		for(int i=0; i<nrOfTimesRepeat; i++){
			ParameterizedPaySim p = new ParameterizedPaySim(1);
			p.setCurrentLoop(i);
			p.runSimulation(args);
		}
		
			
	}	
}


































