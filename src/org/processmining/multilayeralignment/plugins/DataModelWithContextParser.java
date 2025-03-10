package org.processmining.multilayeralignment.plugins;

import bsh.ParseException;

public class DataModelWithContextParser {
	String activityName;
	String dataOperation;
	String dOType;
	String frequency;
	String line;
	
	
	DataModelWithContextParser(String line) throws ParseException {
		this.line=line;
        String[] lineSplit = line.split(",");
        
        activityName = lineSplit[0].trim();
        dataOperation= lineSplit[1].trim();
        dOType=lineSplit[2].trim();
        frequency=lineSplit[3].trim();
		
	}
	
	public String getActivityName() {
        return  activityName;
    }

    public String getDataOperation() {
        return dataOperation;
    }
    
    public String getDOType() {
        return  dOType;
    }
    
    public String getFrequency() {
        return  frequency;
    }

}
