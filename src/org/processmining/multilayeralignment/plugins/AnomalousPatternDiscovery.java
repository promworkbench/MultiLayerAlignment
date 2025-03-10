package org.processmining.multilayeralignment.plugins;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

public class AnomalousPatternDiscovery {
	//public static void main(String[] args) throws Exception {
	public static String perform(String inputPath1, float min_support1,float min_threshold1,float support_threshold1, float confidence_threshold1) throws FileNotFoundException, ScriptException {			
		String s = null;

        try {
            
        // run the Unix "ps -ef" command
            // using the Runtime exec method:
        	//String inputDeviationsPath="D:\\OrganizedFolder\\FinalExperiments\\RuleMining\\Inputs\\Experiment1.csv";
        	String inputDeviationsPath="D:\\OrganizedFolder\\FinalExperiments\\RuleMining\\Inputs\\Experiment2.csv";
            float min_support= min_support1;//(float) 0.01;
            float min_threshold = min_threshold1;//(float) 0.001;
            float support_threshold= support_threshold1; //(float) 0.001;
        	float confidence_threshold= confidence_threshold1; //(float) 0.25;
        	String x = "python "+ System.getProperty("user.dir") + "\\PythonScripts\\AnomalousBehavioralPatternsDiscovery.py "+inputDeviationsPath+" "+min_support+" "+min_threshold+" "+support_threshold+" "+confidence_threshold;
        	System.out.println(x);
            Process p = Runtime.getRuntime().exec(x);
            
            BufferedReader stdInput = new BufferedReader(new 
                 InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                 InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            
            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
            
            System.exit(0);
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
		
		
		StringWriter writer = new StringWriter(); //ouput will be stored here
	    
	    ScriptEngineManager manager = new ScriptEngineManager();
	    ScriptContext context = new SimpleScriptContext();
	    
	    
	    context.setWriter(writer); //configures output redirection
	    ScriptEngine engine = manager.getEngineByName("python");
	  
	    //engine.put(ScriptEngine.FILENAME, "D:\\OrganizedFolder\\phd\\2022 weekly meetings\\My paper_rule Mining\\Paper_file\\SourceCode\\Input Files\\Experiment1.csv");
	
	    engine.eval(new FileReader(System.getProperty("user.dir") + "PythonScripts\\AnomalousBehavioralPatternsDiscovery.py"), context);
	    System.out.println(writer.toString());
	    String output=writer.toString();
	    return output;
	    
	}

}
