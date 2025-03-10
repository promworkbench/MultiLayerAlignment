package org.processmining.multilayeralignment.help;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

public class LogModification5 {
	 static Map<String,String> randomCases= new HashMap<String, String>();
	 public static void main(String[] args) throws Exception {
		 
		 //String logFile = "D:\\OrganizedFolder\\phd\\weekly meetings\\Implementation\\Motivation Example\\input files\\processLog.xes";
		 //String logFile = "D:\\OrganizedFolder\\phd\\2021 weekly meetings\\Implementation V3\\Experiments\\Exp8\\processLog_updated.xes";
		 String logFile= "D:\\OrganizedFolder\\phd\\2022 weekly meetings\\2022-03-25\\Analysis\\log after S_C\\OurFinalProcessLog.xes";
			XLog processLog;
			XEventClassifier eventClassifier;
			processLog = new XUniversalParser().parse(new File(logFile)).iterator().next();
			eventClassifier = new XEventNameClassifier();
			XLogInfo summary = XLogInfoFactory.createLogInfo(processLog, eventClassifier);
			
		//String dataLogFile = "D:\\OrganizedFolder\\phd\\weekly meetings\\Implementation\\Motivation Example\\input files\\DataLog.xes";
		//String dataLogFile = "D:\\OrganizedFolder\\phd\\2021 weekly meetings\\Implementation V3\\Experiments\\Exp8\\DataLog_updated.xes";
		String dataLogFile= "D:\\OrganizedFolder\\phd\\2022 weekly meetings\\2022-03-25\\Analysis\\log after S_C\\OurFinalDataLog.xes";
			XLog dataLog;
			XEventClassifier dataEventClassifier= new XEventNameClassifier();
			dataLog = new XUniversalParser().parse(new File(dataLogFile)).iterator().next();
			XLogInfo dataLogSummary = XLogInfoFactory.createLogInfo(dataLog, dataEventClassifier);
			XEventClasses dataEvClasses = dataLogSummary.getEventClasses();
	
		//Generate random int value from 1 to 10000  
		  Random random = new Random();
		    ArrayList<Integer> arrayList = new ArrayList<Integer>();

		    while (arrayList.size() < 5000) { // how many numbers u need - it will 500
		        int a = random.nextInt(10000)+1; // this will give numbers between 1 and 10000.

		        if (!arrayList.contains(a)) {
		            arrayList.add(a);
		        }
		    }
		    
		    arrayList.forEach((n)-> randomCases.put(Integer.toString(n), Integer.toString(n)));
		    
		    FileWriter writer = new FileWriter("RandomCases.txt");
		    
		    for(Integer rCase: arrayList) {
		        writer.write(rCase.toString() + System.lineSeparator());
		    }
		    writer.close();
		    
		  
		
		    
		    System.out.println("test");
		  
		    
		    
		    
		    //To update event attributes
		    //updateProcessEvents(processLog);
		    //To update Trace attribute
		    //updateProcessTraces(processLog);
		    updateProcessTraces(dataLog);
		    //updateDataEvents(dataLog);
		    
	 }
	 
	    private static void updateProcessTraces(XLog processLog) throws IOException {
			 
			 XLog createdLog = XFactoryRegistry.instance().currentDefault().createLog(processLog.getAttributes());
			 createdLog.getExtensions().addAll(processLog.getExtensions());
			 createdLog.getGlobalEventAttributes().addAll(processLog.getGlobalEventAttributes());
			 createdLog.getGlobalTraceAttributes().addAll(processLog.getGlobalTraceAttributes());
			 createdLog.getClassifiers().addAll(processLog.getClassifiers());
			 
			 //for (int i = 0; i < processLog.size(); i++) {
				 for (int i = 0; i < 5000; i++) {		
					
				 
				 
					 XTrace newTrace = XFactoryRegistry.instance().currentDefault().createTrace(processLog.get(i).getAttributes());
					 //String newTraceID= newTrace.getAttributes().get("concept:name").toString();
					 //newTraceID= newTraceID.substring(newTraceID.indexOf("_")+1, newTraceID.length());
					 //newTrace.getAttributes().put("concept:name", XFactoryRegistry.instance().currentDefault().createAttributeLiteral("concept:name", newTraceID, null));
					 
					 for (XEvent event : processLog.get(i)) {
						    //XEvent existingEvent = XFactoryRegistry.instance().currentDefault().createEvent(event.getAttributes());
						    newTrace.add(event);					    
						}
					 createdLog.add(newTrace);
					 System.out.println("yes");
				 

			}
			 
			 		 
			 //FileOutputStream out6 = new FileOutputStream("ProcessLog_updated.xes");
			 FileOutputStream out6 = new FileOutputStream("SDataLog_updated.xes");
			 XSerializer logSerializer = new XesXmlSerializer();
			 logSerializer.serialize(createdLog, out6);
			 out6.close();
			 System.out.println("update resource in data operations successfully");
		 }	 
	 

    
    private static void updateProcessEvents(XLog processLog) throws IOException {
		 
		 XLog createdLog = XFactoryRegistry.instance().currentDefault().createLog(processLog.getAttributes());
		 createdLog.getExtensions().addAll(processLog.getExtensions());
		 createdLog.getGlobalEventAttributes().addAll(processLog.getGlobalEventAttributes());
		 createdLog.getGlobalTraceAttributes().addAll(processLog.getGlobalTraceAttributes());
		 createdLog.getClassifiers().addAll(processLog.getClassifiers());
		 
		 for (int i = 0; i < processLog.size(); i++) {
				
				
			 
			 
				 XTrace newTrace = XFactoryRegistry.instance().currentDefault().createTrace(processLog.get(i).getAttributes());
	             int j=0;
				 for (XEvent event : processLog.get(i)) {
					    
					 //processLog.get(i).get(index)
					        
					//Start
					 XEvent newEvent1 = XFactoryRegistry.instance().currentDefault().createEvent();
					 for (XAttribute globalAttribute : createdLog.getGlobalEventAttributes()) {
					     newEvent1.getAttributes().put(globalAttribute.getKey(), globalAttribute);
					     
					 }
					 String eventID= event.getAttributes().get("EventID").toString();
					 String newID=eventID.substring(eventID.indexOf("_")+1, eventID.length());
					 
					 newEvent1.getAttributes().put("id", XFactoryRegistry.instance().currentDefault().createAttributeLiteral("id", newID, null));
					 XConceptExtension.instance().assignName(newEvent1, event.getAttributes().get("concept:name").toString());
					 XOrganizationalExtension.instance().assignResource(newEvent1, event.getAttributes().get("org:resource").toString());
					 XLifecycleExtension.instance().assignTransition(newEvent1, event.getAttributes().get("lifecycle:transition").toString());
					 XTimeExtension.instance().assignTimestamp(newEvent1,  XTimeExtension.instance().extractTimestamp(event));
					 newTrace.add(newEvent1);
					 
					 //Complete
					 XEvent newEvent2 = XFactoryRegistry.instance().currentDefault().createEvent();
					 for (XAttribute globalAttribute : createdLog.getGlobalEventAttributes()) {
					     newEvent2.getAttributes().put(globalAttribute.getKey(), globalAttribute);
					     
					 }
					 newEvent2.getAttributes().put("id", XFactoryRegistry.instance().currentDefault().createAttributeLiteral("id", newID, null));
					 XConceptExtension.instance().assignName(newEvent2, event.getAttributes().get("concept:name").toString());
					 XOrganizationalExtension.instance().assignResource(newEvent2, event.getAttributes().get("org:resource").toString());
					 XLifecycleExtension.instance().assignTransition(newEvent2, "COMPLETE");
					 
					 LocalDateTime myStartingDate;
					 if(j>=processLog.get(i).size()-1 ) {
					       myStartingDate = convertToLocalDateTimeViaMilisecond(XTimeExtension.instance().extractTimestamp(event));
					 }else {
				           myStartingDate = convertToLocalDateTimeViaMilisecond(XTimeExtension.instance().extractTimestamp(processLog.get(i).get(j+1)));
					 }
					 //minus 5 Seconds
					 myStartingDate= myStartingDate.minusNanos(10);
					 Date startDate= convertToDateViaInstant(myStartingDate);
					 
					 
					 XTimeExtension.instance().assignTimestamp(newEvent2, startDate);
					 newTrace.add(newEvent2);
					  
					 j++;   	
					   
					} 
				 createdLog.add(newTrace);
				 System.out.println("yes");
			 

		}
		 
		 		 
		 FileOutputStream out6 = new FileOutputStream("ProcessLog_updated.xes");
		 XSerializer logSerializer = new XesXmlSerializer();
		 logSerializer.serialize(createdLog, out6);
		 out6.close();
		 System.out.println("update resource in data operations successfully");
	 }
    
    private static void updateDataEvents(XLog dataLog) throws IOException {
		 
		 XLog createdLog = XFactoryRegistry.instance().currentDefault().createLog(dataLog.getAttributes());
		 createdLog.getExtensions().addAll(dataLog.getExtensions());
		 createdLog.getGlobalEventAttributes().addAll(dataLog.getGlobalEventAttributes());
		 createdLog.getGlobalTraceAttributes().addAll(dataLog.getGlobalTraceAttributes());
		 createdLog.getClassifiers().addAll(dataLog.getClassifiers());
		 
		 for (int i = 0; i < dataLog.size(); i++) {
				
				
			 
			 
				 XTrace newTrace = XFactoryRegistry.instance().currentDefault().createTrace(dataLog.get(i).getAttributes());
	             int j=0;
				 for (XEvent event : dataLog.get(i)) {
					    
					 //processLog.get(i).get(index)
					        
					//Start
					 XEvent newEvent1 = XFactoryRegistry.instance().currentDefault().createEvent();
					 for (XAttribute globalAttribute : createdLog.getGlobalEventAttributes()) {
					     newEvent1.getAttributes().put(globalAttribute.getKey(), globalAttribute);
					     
					 }
					 String eventID= event.getAttributes().get("EventID").toString();
					 String newID=eventID.substring(eventID.indexOf("_")+1, eventID.length());
					 
					 
					 XConceptExtension.instance().assignName(newEvent1, event.getAttributes().get("concept:name").toString());
					 XOrganizationalExtension.instance().assignResource(newEvent1, event.getAttributes().get("org:resource").toString());
					 XLifecycleExtension.instance().assignTransition(newEvent1, event.getAttributes().get("lifecycle:transition").toString());
					 LocalDateTime myStartingDate;
					 myStartingDate = convertToLocalDateTimeViaMilisecond(XTimeExtension.instance().extractTimestamp(event));
					 System.out.println("1_myStartingDate"+myStartingDate);
					 myStartingDate= myStartingDate.minusNanos(3000000);
					 System.out.println("2_myStartingDate"+myStartingDate);
					 Date eventDate= convertToDateViaInstant(myStartingDate);
					 XTimeExtension.instance().assignTimestamp(newEvent1, eventDate);
					 
					 newTrace.add(newEvent1);
					 
					 
					   
					} 
				 createdLog.add(newTrace);
				 System.out.println("yes");
			 

		}
		 
		 		 
		 FileOutputStream out6 = new FileOutputStream("dataLog_updated.xes");
		 XSerializer logSerializer = new XesXmlSerializer();
		 logSerializer.serialize(createdLog, out6);
		 out6.close();
		 System.out.println("update resource in data operations successfully");
	 }
   
    public static LocalDateTime convertToLocalDateTimeViaMilisecond(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
          .atZone(ZoneId.systemDefault())
          .toLocalDateTime();
    }
    
    public static Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date
          .from(dateToConvert.atZone(ZoneId.systemDefault())
          .toInstant());
    }
	 
}
