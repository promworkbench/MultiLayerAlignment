package org.processmining.multilayeralignment.help;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

public class LogModification3 {
	 static Map<String,String> randomCases= new HashMap<String, String>();
	 public static void main(String[] args) throws Exception {
		 
		 //String logFile = "D:\\OrganizedFolder\\phd\\weekly meetings\\Implementation\\Motivation Example\\input files\\processLog.xes";
		 String logFile = "D:\\OrganizedFolder\\phd\\2021 weekly meetings\\Implementation V3\\Experiments\\Exp7-3\\processLog_updated.xes";
			XLog processLog;
			XEventClassifier eventClassifier;
			processLog = new XUniversalParser().parse(new File(logFile)).iterator().next();
			eventClassifier = new XEventNameClassifier();
			XLogInfo summary = XLogInfoFactory.createLogInfo(processLog, eventClassifier);
			
		//String dataLogFile = "D:\\OrganizedFolder\\phd\\weekly meetings\\Implementation\\Motivation Example\\input files\\DataLog.xes";
		String dataLogFile = "D:\\OrganizedFolder\\phd\\2021 weekly meetings\\Implementation V3\\Experiments\\Exp7-3\\DataLog_updated.xes";
			XLog dataLog;
			XEventClassifier dataEventClassifier= new XEventNameClassifier();
			dataLog = new XUniversalParser().parse(new File(dataLogFile)).iterator().next();
			XLogInfo dataLogSummary = XLogInfoFactory.createLogInfo(dataLog, dataEventClassifier);
			XEventClasses dataEvClasses = dataLogSummary.getEventClasses();
		
		//Generate random int value from 1 to 10000  
		  Random random = new Random();
		    ArrayList<Integer> arrayList = new ArrayList<Integer>();

		    while (arrayList.size() < 500) { // how many numbers u need - it will 500
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
		    
		  //------------------------------------------------------------------------------------------- 
		    //Scenario7:add all kinds of noise in the Trace level:
		     
			/*  
				Scanner scanner = new Scanner(new FileReader("RandomCases.txt"));

			    HashMap<String,Integer> map = new HashMap<String,Integer>();

			    while (scanner.hasNextLine()) {
			        String[] columns = scanner.nextLine().split("\t");

			        randomCases.put(columns[0],columns[0]);
			    }

			    System.out.println(randomCases); 
			     
		    */
		    
		    
		  //Scenario1:change the role of an activity and related data operations- totally sync.Move with wrong Role
		  //updateDataOperationsResource(dataLog, randomCases, "Read:(AddmissionID,PatientID,PaymentID)", "Collect:(PaymentReceipt)", "R104" );
		  //updateProcessEventsResource(processLog, randomCases, "Billing (bi)", "R104" ); 
			
		  //Scenario2:delete one of the operations of an activity to get partially move-  updating medical history in Treatment prescription activity- partially move
		  //deleteOperations(dataLog, randomCases, "Collect:(TreatmentPlan)", "");  
		    
		  //Scenario3:add one event and related data operations to the logs- move on both process log and data log 
		  //addDataOperations(dataLog, randomCases, "Read:(PatientID,MedicalHistoryID)", "");
		  //addProcessEvents(processLog, randomCases, "Clinical trial (ct)");
		    
		  //Scenario4:delete an activity and all of its operations to get model move- move on model
		  //deleteOperations(dataLog, randomCases, "Read:(ID)", "");
		  //deleteProcessEvents(processLog, randomCases, "Identify patient (ip)");
		    
		  //Scenario5:add not allowed data operation- move on data log
		  //addDataOperations(dataLog, randomCases, "Read:(PatientID,MedicalHistoryID)", "");  
		    
		  //Scenario6:add not allowed Role have done an activity with missing operation- partially sync. move with wrong role 
		  //updateProcessEventsResource(processLog, randomCases, "Admission (ad)", "S100" );
		  //deleteOperations(dataLog, randomCases, "Read:(ID,PatientID,Name,Gender,Age,Address,Phone number)", "Collect:(AddmissionID)");
		    
		
		    
		    System.out.println("test");
		  
		    
		    
		    //Scenario1 delete one of the operations of an activity to get partially move-  updating medical history in Treatment prescription activity
		    //deleteOperations(dataLog, randomCases, "Collect:(TreatmentPlan)", "");
		    
		    
		    //Scenario2 delete an activity and all of its operations to get model move
		    //deleteOperations(dataLog, randomCases, "Read:(ID)", "");
		    //deleteProcessEvents(processLog, randomCases, "Identify patient (ip)");
		    
		    		    
		    //Scenario3 add one event and related data operations to the logs 
		    //addDataOperations(dataLog, randomCases, "Read:(PatientID,MedicalHistoryID)", "");
		    //addProcessEvents(processLog, randomCases, "Clinical trial (ct)");
		    
		    //Scenario4 change the role of an activity and related data operations
		    //updateDataOperationsResource(dataLog, randomCases, "Read:(AddmissionID,PatientID,PaymentID)", "Collect:(PaymentReceipt)", "R104" );
		    //updateProcessEventsResource(processLog, randomCases, "Billing (bi)", "R104" );
		    
		    //Scenario5 : Sc1 ,Sc2 ,Sc3 ,Sc4  all together in trace level
		    
		    //Scenario6 deleting a data operation "Read:(AddmissionID,PatientID)" that happened in 3 activities "LapApo","ConsultantApo","Discharge"
		    //deleteOperations(dataLog, randomCases, "Read:(AddmissionID,PatientID)", "Collect:(Confirmation)");
		    //deleteProcessEvents(processLog, randomCases, "Discharge (di)");
		    
		    //Scenario7 add not allowed data operation
		    //addDataOperations(dataLog, randomCases, "Read:(PatientID,MedicalHistoryID)", "");
		    
		    //Scenario8 add not allowed Role have done an activity with missing operation
		    //updateProcessEventsResource(processLog, randomCases, "Admission (ad)", "S100" );
		    //deleteOperations(dataLog, randomCases, "Read:(ID,PatientID,Name,Gender,Age,Address,Phone number)", "Collect:(AddmissionID)");
		    
		    //Scenario8_2 add not allowed Role have done an activity with missing operation
		    //addProcessEvents(processLog, randomCases, "Admission (ad)");
		    
		    //Scenario9 : Sc1 ,Sc2 ,Sc3 ,Sc4 ,Sc7 ,Sc8 all together in log level
		    
		    //TODO 1)delete data operations from data trace
		    //deleteOperations(dataLog, randomCases, "Read:(ID)", "");
			 
			 //TODO 2)delete events from process trace
		    //deleteProcessEvents(processLog, randomCases, "Identify patient (ip)");
			 //TODO 3)add data operation to data trace
			 
			 //TODO 4)add events to process trace
			 
			 //TODO 5)change event attr (resource) in data trace
			 
			 //TODO 6)change event attr (resource) in process trace
		    
		    //S100-user behavior- user that always done DL deviation in 650 cases
		    //addDataOperations(dataLog, randomCases, "Read:(P_phone)", ""); //+user=R103
		    
	 }
	 
	 
	 // 1)delete data operations from data trace
	 
	 private static void deleteOperations(XLog dataLog, Map<String,String> randomCases, String readOpr, String collectOpr) throws IOException {
		 
		 XLog createdLog = XFactoryRegistry.instance().currentDefault().createLog(dataLog.getAttributes());
		 createdLog.getExtensions().addAll(dataLog.getExtensions());
		 createdLog.getGlobalEventAttributes().addAll(dataLog.getGlobalEventAttributes());
		 createdLog.getGlobalTraceAttributes().addAll(dataLog.getGlobalTraceAttributes());
		 createdLog.getClassifiers().addAll(dataLog.getClassifiers());
		 
		 for (int i = 0; i < dataLog.size(); i++) {
				
				
			 
				 if( randomCases.containsKey(dataLog.get(i).getAttributes().get("concept:name").toString())){
					 XTrace newTrace = XFactoryRegistry.instance().currentDefault().createTrace(dataLog.get(i).getAttributes());
					 
					 for (XEvent event : dataLog.get(i)) {
						    
						
						    if (event.getAttributes().get("concept:name").toString().equalsIgnoreCase(collectOpr) || event.getAttributes().get("concept:name").toString().equalsIgnoreCase(readOpr)) {
						        // do nothing, it will be deleted from the log
						    	
						    } else {
						        newTrace.add(event);
						        
						    }
						} 
					 createdLog.add(newTrace);
					 System.out.println("yes");
				 }
				 else {
					 createdLog.add(dataLog.get(i));
					 System.out.println("No");
					 }

			}       
		 
		 //Write new log to disk
		 FileOutputStream out = new FileOutputStream("DataLog_updated.xes");
		 XSerializer logSerializer = new XesXmlSerializer();
		 logSerializer.serialize(createdLog, out);
		 out.close();
		 System.out.println("delete opr successfully");
	 }
	 //2)delete events from process trace
     private static void deleteProcessEvents(XLog processLog, Map<String,String> randomCases, String eventName) throws IOException {
		 
		 XLog createdLog = XFactoryRegistry.instance().currentDefault().createLog(processLog.getAttributes());
		 createdLog.getExtensions().addAll(processLog.getExtensions());
		 createdLog.getGlobalEventAttributes().addAll(processLog.getGlobalEventAttributes());
		 createdLog.getGlobalTraceAttributes().addAll(processLog.getGlobalTraceAttributes());
		 createdLog.getClassifiers().addAll(processLog.getClassifiers());
		 
		 for (int i = 0; i < processLog.size(); i++) {
				
				
			 
			 if( randomCases.containsKey(processLog.get(i).getAttributes().get("concept:name").toString())){
				 XTrace newTrace = XFactoryRegistry.instance().currentDefault().createTrace(processLog.get(i).getAttributes());
				 
				 for (XEvent event : processLog.get(i)) {
					    
					 
					    if (event.getAttributes().get("concept:name").toString().equalsIgnoreCase(eventName)) {
					        // do nothing, it will be deleted from the log
					    	
					    } else {
					        newTrace.add(event);
					        
					    }
					} 
				 createdLog.add(newTrace);
				
			 }
			 else {
				 createdLog.add(processLog.get(i));
				 
				 }

		}       
	 
		
		//Write new log to disk
		 FileOutputStream out2 = new FileOutputStream("ProcessLog_updated.xes");
		 XSerializer logSerializer = new XesXmlSerializer();
		 logSerializer.serialize(createdLog, out2);
		 out2.close();
		 System.out.println("delete event successfully");
		 
	 }
	
    // 3)add data operation to data trace
    private static void addDataOperations(XLog dataLog, Map<String,String> randomCases, String readOpr, String collectOpr) throws IOException {
		 
		 XLog createdLog = XFactoryRegistry.instance().currentDefault().createLog(dataLog.getAttributes());
		 createdLog.getExtensions().addAll(dataLog.getExtensions());
		 createdLog.getGlobalEventAttributes().addAll(dataLog.getGlobalEventAttributes());
		 createdLog.getGlobalTraceAttributes().addAll(dataLog.getGlobalTraceAttributes());
		 createdLog.getClassifiers().addAll(dataLog.getClassifiers());
		 
		 for (int i = 0; i < dataLog.size(); i++) {
				
				
				
				 if( randomCases.containsKey(dataLog.get(i).getAttributes().get("concept:name").toString())){
					 XTrace newTrace = XFactoryRegistry.instance().currentDefault().createTrace(dataLog.get(i).getAttributes());
					 
					 for (XEvent event : dataLog.get(i)) {
						    //XEvent existingEvent = XFactoryRegistry.instance().currentDefault().createEvent(event.getAttributes());
						    newTrace.add(event);
						    					    
						} 
					 XEvent newEvent = XFactoryRegistry.instance().currentDefault().createEvent();
					 for (XAttribute globalAttribute : createdLog.getGlobalEventAttributes()) {
					     newEvent.getAttributes().put(globalAttribute.getKey(), globalAttribute);
					     
					 }
					 XConceptExtension.instance().assignName(newEvent, readOpr);
					 //for Scenario3
					 XOrganizationalExtension.instance().assignResource(newEvent, "D102");
					 //for scenario6
					 //XOrganizationalExtension.instance().assignResource(newEvent, "N102");
					 XLifecycleExtension.instance().assignTransition(newEvent, "COMPLETE");
					 //XTimeExtension.instance().assignTimestamp(newEvent, ?);
					 
					
					 newTrace.add(6, newEvent);
					 createdLog.add(newTrace);
					 System.out.println("yes");
				 }
				 else {
					 createdLog.add(dataLog.get(i));
					 System.out.println("No");
					 }

			}       
		 
		 
		 FileOutputStream out3 = new FileOutputStream("DataLog_updated.xes");
		 XSerializer logSerializer = new XesXmlSerializer();
		 logSerializer.serialize(createdLog, out3);
		 out3.close();
		 System.out.println("add opr successfully");
	 }
     
     // 4)process events  to process trace
    private static void addProcessEvents(XLog processLog, Map<String,String> randomCases, String eventName) throws IOException {
		 
		 XLog createdLog = XFactoryRegistry.instance().currentDefault().createLog(processLog.getAttributes());
		 createdLog.getExtensions().addAll(processLog.getExtensions());
		 createdLog.getGlobalEventAttributes().addAll(processLog.getGlobalEventAttributes());
		 createdLog.getGlobalTraceAttributes().addAll(processLog.getGlobalTraceAttributes());
		 createdLog.getClassifiers().addAll(processLog.getClassifiers());
		 
		 for (int i = 0; i < processLog.size(); i++) {
				
				
				
				 if( randomCases.containsKey(processLog.get(i).getAttributes().get("concept:name").toString())){
					 XTrace newTrace = XFactoryRegistry.instance().currentDefault().createTrace(processLog.get(i).getAttributes());
					 XEvent event1=null;
					 
					event1=processLog.get(i).get(5);
					 
					 for (XEvent event : processLog.get(i)) {
						    //XEvent existingEvent = XFactoryRegistry.instance().currentDefault().createEvent(event.getAttributes());
						    newTrace.add(event);					    
						} 
					 XEvent newEvent = XFactoryRegistry.instance().currentDefault().createEvent();
					 for (XAttribute globalAttribute : createdLog.getGlobalEventAttributes()) {
					     newEvent.getAttributes().put(globalAttribute.getKey(), globalAttribute);
					     
					 }
					 XConceptExtension.instance().assignName(newEvent, eventName);
					 //for Scenario3
					 XOrganizationalExtension.instance().assignResource(newEvent, "D102");
					 newEvent.getAttributes().put("id", XFactoryRegistry.instance().currentDefault().createAttributeLiteral("id", "1000", null));
					 //for Scenario 8_2
					 //XOrganizationalExtension.instance().assignResource(newEvent, "S100");
					 XLifecycleExtension.instance().assignTransition(newEvent, "START");
					 XTimeExtension.instance().assignTimestamp(newEvent, XTimeExtension.instance().extractTimestamp(event1));
					
					
					 newTrace.add(6, newEvent);
					 
					 XEvent newEvent2 = XFactoryRegistry.instance().currentDefault().createEvent();
					 for (XAttribute globalAttribute : createdLog.getGlobalEventAttributes()) {
					     newEvent2.getAttributes().put(globalAttribute.getKey(), globalAttribute);
					     
					 }
					 XConceptExtension.instance().assignName(newEvent2, eventName);
					 //for Scenario3
					 XOrganizationalExtension.instance().assignResource(newEvent2, "D102");
					 newEvent2.getAttributes().put("id", XFactoryRegistry.instance().currentDefault().createAttributeLiteral("id", "1000", null));
					 //for Scenario 8_2
					 //XOrganizationalExtension.instance().assignResource(newEvent, "S100");
					 XLifecycleExtension.instance().assignTransition(newEvent2, "COMPLETE");
					 XTimeExtension.instance().assignTimestamp(newEvent2, XTimeExtension.instance().extractTimestamp(event1));
					 
					 
					 newTrace.add(7, newEvent2);
		
					 createdLog.add(newTrace);
					 System.out.println("yes");
				 }
				 else {
					 createdLog.add(processLog.get(i));
					 System.out.println("No");
					 }

			}       
		 
		 
		 FileOutputStream out4 = new FileOutputStream("ProcessLog_updated.xes");
		 XSerializer logSerializer = new XesXmlSerializer();
		 logSerializer.serialize(createdLog, out4);
		 out4.close();
		 System.out.println("add process events successfully");
	 }
    private static void updateDataOperationsResource(XLog dataLog, Map<String,String> randomCases, String readOpr, String collectOpr, String resource) throws IOException {
		 
		 XLog createdLog = XFactoryRegistry.instance().currentDefault().createLog(dataLog.getAttributes());
		 createdLog.getExtensions().addAll(dataLog.getExtensions());
		 createdLog.getGlobalEventAttributes().addAll(dataLog.getGlobalEventAttributes());
		 createdLog.getGlobalTraceAttributes().addAll(dataLog.getGlobalTraceAttributes());
		 createdLog.getClassifiers().addAll(dataLog.getClassifiers());
		 
		 for (int i = 0; i < dataLog.size(); i++) {
				
				
			 
			 if( randomCases.containsKey(dataLog.get(i).getAttributes().get("concept:name").toString())){
				 XTrace newTrace = XFactoryRegistry.instance().currentDefault().createTrace(dataLog.get(i).getAttributes());
				 
				 for (XEvent event : dataLog.get(i)) {
					    
					
					    if (event.getAttributes().get("concept:name").toString().equalsIgnoreCase(collectOpr) || event.getAttributes().get("concept:name").toString().equalsIgnoreCase(readOpr)) {
					        // do nothing, it will be deleted from the log
					    	XEvent newEvent = XFactoryRegistry.instance().currentDefault().createEvent();
							 for (XAttribute globalAttribute : createdLog.getGlobalEventAttributes()) {
							     newEvent.getAttributes().put(globalAttribute.getKey(), globalAttribute);
							     
							 }
							 XConceptExtension.instance().assignName(newEvent, event.getAttributes().get("concept:name").toString());
							 XOrganizationalExtension.instance().assignResource(newEvent, resource);
							 XLifecycleExtension.instance().assignTransition(newEvent, "COMPLETE");
							 XTimeExtension.instance().assignTimestamp(newEvent, XTimeExtension.instance().extractTimestamp(event));
							 newTrace.add(newEvent);
					    	
					    } else {
					        newTrace.add(event);
					        
					    }
					} 
				 createdLog.add(newTrace);
				 System.out.println("yes");
			 }
			 else {
				 createdLog.add(dataLog.get(i));
				 System.out.println("No");
				 }

		}
		 
		 		 
		 FileOutputStream out5 = new FileOutputStream("DataLog_updated.xes");
		 XSerializer logSerializer = new XesXmlSerializer();
		 logSerializer.serialize(createdLog, out5);
		 out5.close();
		 System.out.println("update resource in data operations successfully");
	 }
    
    private static void updateProcessEventsResource(XLog processLog, Map<String,String> randomCases, String eventName, String resource) throws IOException {
		 
		 XLog createdLog = XFactoryRegistry.instance().currentDefault().createLog(processLog.getAttributes());
		 createdLog.getExtensions().addAll(processLog.getExtensions());
		 createdLog.getGlobalEventAttributes().addAll(processLog.getGlobalEventAttributes());
		 createdLog.getGlobalTraceAttributes().addAll(processLog.getGlobalTraceAttributes());
		 createdLog.getClassifiers().addAll(processLog.getClassifiers());
		 
		 for (int i = 0; i < processLog.size(); i++) {
				
				
			 
			 if( randomCases.containsKey(processLog.get(i).getAttributes().get("concept:name").toString())){
				 XTrace newTrace = XFactoryRegistry.instance().currentDefault().createTrace(processLog.get(i).getAttributes());
				 
				 for (XEvent event : processLog.get(i)) {
					    
					
					    if (event.getAttributes().get("concept:name").toString().equalsIgnoreCase(eventName)) {
					        // do nothing, it will be deleted from the log
					    	XEvent newEvent = XFactoryRegistry.instance().currentDefault().createEvent();
							 for (XAttribute globalAttribute : createdLog.getGlobalEventAttributes()) {
							     newEvent.getAttributes().put(globalAttribute.getKey(), globalAttribute);
							     
							 }
							 newEvent.getAttributes().put("id", XFactoryRegistry.instance().currentDefault().createAttributeLiteral("id", event.getAttributes().get("id").toString(), null));
							 XConceptExtension.instance().assignName(newEvent, event.getAttributes().get("concept:name").toString());
							 XOrganizationalExtension.instance().assignResource(newEvent, resource);
							 XLifecycleExtension.instance().assignTransition(newEvent, event.getAttributes().get("lifecycle:transition").toString());
							 XTimeExtension.instance().assignTimestamp(newEvent,  XTimeExtension.instance().extractTimestamp(event));
							 newTrace.add(newEvent);
					    	
					    } else {
					        newTrace.add(event);
					        
					    }
					} 
				 createdLog.add(newTrace);
				 System.out.println("yes");
			 }
			 else {
				 createdLog.add(processLog.get(i));
				 System.out.println("No");
				 }

		}
		 
		 		 
		 FileOutputStream out6 = new FileOutputStream("ProcessLog_updated.xes");
		 XSerializer logSerializer = new XesXmlSerializer();
		 logSerializer.serialize(createdLog, out6);
		 out6.close();
		 System.out.println("update resource in data operations successfully");
	 }
	 
}
