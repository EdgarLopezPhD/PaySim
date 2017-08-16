package paysim;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class AggregateParamFileCreator {
	
	
	/*
	 * ALGORITHM
	 * 
	 * FOR EACH TYPE
	 * 		FOR EACH DAY
	 * 			FOR EACH HOUR
	 * 				AGGRECORD = GETAGGRECORD(TYPE, DAY, HOUR, TRANSACTIONLIST)
	 * 				STORE(AGGRECORD)
	 * 			END
	 * 		END
	 * END
	 * 
	 */
	public ArrayList<AggregateTransactionRecord> generateAggregateParamFile(ArrayList<Transaction> transactionList){
		ArrayList<String> actions = new ArrayList<String>();
		ArrayList<AggregateTransactionRecord> aggrTransRecord = new ArrayList<AggregateTransactionRecord>();
		actions.add("CASH_IN");
		actions.add("CASH_OUT");
		actions.add("DEBIT");
		actions.add("DEPOSIT");
		actions.add("PAYMENT");
		actions.add("TRANSFER");
		
		for(int i=0; i<6; i++){
			for(int j=0; j<31; j++){
				for(int h = 0; h<24; h++){
					int day = j;
					int hour = h;
					
					AggregateTransactionRecord tempRecord = getAggregateRecord((short)(i+1), day, hour, transactionList);
					if(tempRecord != null){		
						//System.out.println("\n\nTempRecordGenerated\n" + tempRecord.toString() + "");
						aggrTransRecord.add(tempRecord);
					}
				}
			}
		}
		
		java.util.Collections.sort(aggrTransRecord);
		
		return aggrTransRecord;
	}
	
	public ArrayList<AggregateTransactionRecord> reformat(ArrayList<AggregateTransactionRecord> list){
		ArrayList<AggregateTransactionRecord> reformedList = new ArrayList<AggregateTransactionRecord>();
		ArrayList<AggregateTransactionRecord> tempList = new ArrayList<AggregateTransactionRecord>();
		
		for(int i=0; i<list.size(); i++){
			tempList = new ArrayList<AggregateTransactionRecord>();
			AggregateTransactionRecord temp = list.get(i);
			tempList.add(temp);
			int counter = i+1;
			
			//Get all records looking alike temp
			while(counter <list.size()){
				AggregateTransactionRecord toCheckRecord = list.get(counter);
				if(temp.equals(toCheckRecord)){
					tempList.add(toCheckRecord);
					counter++;
				}else{
					break;
				}
			}
			i = counter -1;
			if(tempList.size() > 1){
				
				AggregateTransactionRecord compacted = compactAggrRecord(tempList);
				reformedList.add(compacted);
			}else{
				reformedList.add(temp);
			}			
		}
		
		return reformedList;
	}

	private AggregateTransactionRecord compactAggrRecord(ArrayList<AggregateTransactionRecord> recordList){
		AggregateTransactionRecord compacted = new AggregateTransactionRecord();
		DecimalFormat df = new DecimalFormat("#.##");
		long hour = 0;
		long month = 0;
		long day = 0;
		double count = 0;
		double sum = 0;
		double avg = 0;
		double std = 0;
		double type = 0;
		hour = Long.parseLong(recordList.get(0).gettHour());
		month = Long.parseLong(recordList.get(0).getMonth());
		day = Long.parseLong(recordList.get(0).gettDay());
		type = Double.parseDouble(recordList.get(0).getType());
		
		//Getting the total count
		for(int i=0; i<recordList.size(); i++){
			count += Double.parseDouble(recordList.get(i).gettCount());
		}
		
		//Getting the sum
		for(int i=0; i<recordList.size(); i++){
			sum += Double.parseDouble(recordList.get(i).gettSum());
		}
		
		
		//Getting the total std
		for(int i=0; i<recordList.size(); i++){
			std += Double.parseDouble(recordList.get(i).gettStd());
		}
		
		
		avg = sum / count;
		std = std / ((double)recordList.size());
		
		//Setting all of the variables to the compacted
		compacted.setType(String.valueOf(type));
		compacted.setMonth(String.valueOf(month));
		compacted.settDay(String.valueOf(day));
		compacted.settHour(String.valueOf(hour));
		compacted.settCount(String.valueOf(count));
		compacted.settSum(String.valueOf(sum));
		compacted.settAvg(String.valueOf(avg));
		compacted.settStd(String.valueOf(std));
		
		return compacted;
	}

	public static ArrayList<Transaction> getRandomTransaction(){
		Client c = new Client();
		ArrayList<Transaction> transactionList = new ArrayList<Transaction>();
		Transaction temp = new Transaction((long)1, c, (short)1, (double)1348, "CASH_IN");
		temp.setDay(1);
		temp.setHour(3);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)1, (double)1848, "CASH_IN");
		temp.setDay(2);
		temp.setHour(5);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)1, (double)1248, "CASH_IN");
		temp.setDay(2);
		temp.setHour(5);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)1, (double)7848, "CASH_IN");
		temp.setDay(1);
		temp.setHour(3);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)1, (double)2348, "CASH_IN");
		temp.setDay(1);
		temp.setHour(3);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)1, (double)6348, "CASH_IN");
		temp.setDay(1);
		temp.setHour(3);
		transactionList.add(temp);
		
		
		
		
		temp = new Transaction((long)1, c, (short)2, (double)3912, "CASH_OUT");
		temp.setDay(1);
		temp.setHour(3);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)2, (double)13431, "CASH_OUT");
		temp.setDay(1);
		temp.setHour(3);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)2, (double)3731, "CASH_OUT");
		temp.setDay(1);
		temp.setHour(3);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)2, (double)9731, "CASH_OUT");
		temp.setDay(2);
		temp.setHour(7);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)2, (double)13731, "CASH_OUT");
		temp.setDay(2);
		temp.setHour(7);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)2, (double)6731, "CASH_OUT");
		temp.setDay(2);
		temp.setHour(7);
		transactionList.add(temp);
		
		
		
		temp = new Transaction((long)1, c, (short)2, (double)11731, "CASH_OUT");
		temp.setDay(2);
		temp.setHour(6);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)2, (double)3731, "CASH_OUT");
		temp.setDay(2);
		temp.setHour(6);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)2, (double)1731, "CASH_OUT");
		temp.setDay(2);
		temp.setHour(6);
		transactionList.add(temp);
		
		
		
		temp = new Transaction((long)1, c, (short)4, (double)2348, "DEPOSIT");
		temp.setDay(6);
		temp.setHour(4);
		transactionList.add(temp);		
		
		temp = new Transaction((long)1, c, (short)4, (double)5731, "DEPOSIT");
		temp.setDay(6);
		temp.setHour(4);
		transactionList.add(temp);
		
		temp = new Transaction((long)1, c, (short)4, (double)15731, "DEPOSIT");
		temp.setDay(6);
		temp.setHour(4);
		transactionList.add(temp);
		
		return transactionList;
	}
	
	public AggregateTransactionRecord getAggregateRecord(short type, int day, int hour, ArrayList<Transaction> transactionList){
		ArrayList<Transaction> subsetTransList = new ArrayList<Transaction>();
		AggregateTransactionRecord recordToReturn = new AggregateTransactionRecord();
		
		for(int i=0; i<transactionList.size(); i++)
		{
			Transaction currTrans = transactionList.get(i);
			if(currTrans.getDay() == day &&
					currTrans.getHour() == hour &&
					currTrans.getType() == type)
			{
				
				subsetTransList.add(currTrans);
			}
			
		}
		
		// 1) count
		// 2) Sum
		// 3) Avg
		// 4) tstd
		
		if(subsetTransList.size() > 0){
			double sum = getTotalAmount(subsetTransList);
			int count = subsetTransList.size();
			double average = getDoublePrecision(2, (( sum / (double)count )));
			double tstd = getStdv(subsetTransList, average);
			
			recordToReturn.setType(String.valueOf(type));
			recordToReturn.settSum(String.valueOf(sum));
			recordToReturn.settCount(String.valueOf(count));
			recordToReturn.settAvg(String.valueOf(getDoublePrecision(2, average)));
			recordToReturn.settStd(String.valueOf(getDoublePrecision(2, tstd)));
			recordToReturn.setMonth(String.valueOf(10));
			recordToReturn.settDay(String.valueOf(day));
			recordToReturn.settHour(String.valueOf(hour));
			return recordToReturn;
		}else{
			return null;
		}
		
	}
	
	
	private double getDoublePrecision(int precision, double d){
		try {
			Double toBeTruncated = new Double(d);
			Double truncatedDouble=new BigDecimal(toBeTruncated).
					setScale(precision, BigDecimal.ROUND_HALF_UP).
					doubleValue();
			return truncatedDouble;
		} catch (Exception e) {
			return 0;
		}
		
	}

	public static double getStdv(ArrayList<Transaction> list, double average){
		DecimalFormat df = new DecimalFormat("#.##");
		double stdv = 0;
		double squaredMeanSum = 0;
		
		//For each number, subtract the mean and square the result
		for(int i=0; i<list.size(); i++){
			double currVal = list.get(i).getAmount() - average;
			currVal *= currVal;
			//System.out.println("Adding :\t" + currVal + "\n");
			squaredMeanSum += currVal;
		}
		//System.out.println("Squred diff  sum\n" + squaredMeanSum + "\n");
		squaredMeanSum /= (double) list.size()-1;
		//System.out.println("Dividing with:\t" + list.size() + "\n");
		
		return Math.sqrt(squaredMeanSum);
	}
	
	//Generator functions
	private double getTotalAmount(ArrayList<Transaction> transactionList){
		double amount = 0;
		for(Transaction t: transactionList){
			amount += t.getAmount();
		}
		return amount;
	}
	
	
	
	
}

















