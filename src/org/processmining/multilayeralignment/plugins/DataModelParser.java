package org.processmining.multilayeralignment.plugins;

import bsh.ParseException;

public class DataModelParser {
	String activityName;
	String dataOperation;
	String line;
	
	
	DataModelParser(String line) throws ParseException {
		this.line=line;
        String[] lineSplit = line.split(",");
        
        activityName = lineSplit[0].trim();
        dataOperation= lineSplit[1].trim();
		
	}
	
	public String getActivityName() {
        return  activityName;
    }

    public String getDataOperation() {
        return dataOperation;
    }
    
    /*
     	String activityName;
	String dataOperation;
	String dOType;
	String line;
	
	
	DataModelParser(String line) throws ParseException {
		this.line=line;
        String[] lineSplit = line.split(",");
        
        activityName = lineSplit[0].trim();
        dataOperation= lineSplit[1].trim();
        dOType=lineSplit[2].trim();
		
	}
	
	public String getActivityName() {
        return  activityName;
    }

    public String getDataOperation() {
        return dataOperation;
    }
     */
}
