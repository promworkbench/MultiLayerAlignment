package org.processmining.multilayeralignment.plugins;

import bsh.ParseException;

public class OrganisationalModel {
	String Role;
	String User;
	String line;
	
	OrganisationalModel(String line) throws ParseException {
		this.line=line;
        String[] lineSplit = line.split(",");
        
        Role = lineSplit[0].trim();
        User= lineSplit[1].trim();
		
	}
	
	public String getRole() {
        return Role;
    }

    public String getUser() {
        return User;
    }


}
