package org.processmining.multilayeralignment.algorithms.syncproduct;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.processmining.multilayeralignment.models.mlreplay.StepTypes;
import org.processmining.multilayeralignment.models.mlreplay.SyncReplayResult;


public class MLAVisualMoves {
	private final Map<String, Integer> RActivityOpr=new HashMap<>();//result abstract from Start/Complete
	private final Map<String, Integer> RActivity=new HashMap<>();//result abstract from operation
	public static final Map<String, String> RActivityID=new HashMap<>();//result in activity level(final result for visualization)
	public MLAVisualMoves(SyncReplayResult res) {
		
		List<StepTypes> stepTypes = new ArrayList<StepTypes>(res.getNodeInstance().size());
		List<Object> nodeInstance = new ArrayList<Object>();
		for (int traceIndex : res.getTraceIndex()) {
			System.out.print("-----traceIndex is: "+traceIndex+"\r\n");
		}
		for (int step=0; step < res.getNodeInstance().size(); step++) {
			StepTypes type = res.getStepTypes().get(step);
			Object node = res.getNodeInstance().get(step);

			
		}
		
		List<SyncReplayResult> allResults = new ArrayList<SyncReplayResult>();
		for (int traceIndex : res.getTraceIndex()) {
			SyncReplayResult result = new SyncReplayResult(nodeInstance, stepTypes, traceIndex);
			
			// copy additional information of each trace
			result.setReliable(res.isReliable());
			result.setInfo(res.getInfo());
			allResults.add(result);
		}
		
		//System.out.print("****SyncType is: "+type.toString()+"\r\n");
		
		for (int step=0; step < res.getNodeInstance().size(); step++) {
			
			StepTypes type = res.getStepTypes().get(step);
			Object node = res.getNodeInstance().get(step);
			String id="";
			String activity="";
			String operation="";
			String typeOfActivity="";
			System.out.print("****Node info is: "+ node.toString()+"\r\n");
			
			if(type.toString().equalsIgnoreCase("Totally Sync. Move") || type.toString().equalsIgnoreCase("Totally Sync. Move with penalty cost") ||
			   type.toString().equalsIgnoreCase("Partially Sync. Move") || type.toString().equalsIgnoreCase("Partially Sync. Move with penalty cost")){
				
				id=node.toString().substring(node.toString().indexOf("|")+1, node.toString().length());
				activity=node.toString().substring(0,node.toString().indexOf("+"));
				operation=node.toString().substring(node.toString().indexOf(":")+1 ,node.toString().indexOf("+", node.toString().indexOf("+") + 1));
				typeOfActivity=node.toString().substring(node.toString().indexOf("+"),node.toString().indexOf(":"));
				
			}
			
			if(type.toString().equalsIgnoreCase("Process Log Move")){
						
				id=node.toString().substring(node.toString().indexOf("|")+1, node.toString().length());
				activity=node.toString().substring(0,node.toString().indexOf("|"));
				operation="N";
				typeOfActivity="N";//???????????????????
					}
			
			if(type.toString().equalsIgnoreCase("Data Log Move")){
				
				id=node.toString().substring(node.toString().indexOf("|")+1, node.toString().length());
				activity=node.toString().substring(0,node.toString().indexOf("|"));// in case of DLM activity=OPr
				operation=activity;
				typeOfActivity="N";
					}
			if(type.toString().equalsIgnoreCase("Model Move")) {
			    id="N";//????????????????? we should solve this
				activity=node.toString().substring(0,node.toString().indexOf("+"));
				operation=node.toString().substring(node.toString().indexOf(":")+1 , node.toString().length() );
				typeOfActivity=node.toString().substring(node.toString().indexOf("+"),node.toString().indexOf(":"));
			}


			resultOfTypeAbstraction(id,activity,operation,type.toString(), typeOfActivity);
			
		}
		
		
		
		RActivityOpr.forEach((key1, value1) -> {

			int splitIndex1= 1+ key1.toString().indexOf("|", key1.toString().indexOf("|") + 1);
			
			
			String id1 = key1.toString().substring(0,key1.toString().indexOf("|") );
			String activity1 = key1.toString().substring(key1.toString().indexOf("|")+1 , key1.toString().indexOf("|", key1.toString().indexOf("|") + 1) ); 
			String operation1 = key1.toString().substring(splitIndex1 , key1.toString().lastIndexOf("|"));
			String moveType1 = key1.toString().substring(key1.toString().lastIndexOf("|")+1 , key1.length());
			
			resultOfOperationAbstraction(id1,activity1,operation1,moveType1); 	
			
    });
		
		RActivity.forEach((key2, value2) -> {
			
			String id2 = key2.toString().substring(0,key2.toString().indexOf("|") );
			String activity2 = key2.toString().substring(key2.toString().indexOf("|")+1 , key2.toString().indexOf("|", key2.toString().indexOf("|") + 1) ); 
			String moveType2 = key2.toString().substring(key2.toString().lastIndexOf("|")+1 , key2.length());
			
			resultInActivityLevel(id2,activity2,moveType2); 	
			

    });

		System.out.print("visResult");
		RActivityOpr.clear();
		RActivity.clear();
		RActivityID.clear();
	}
	
	
	private boolean resultOfTypeAbstraction(String id,String activity,String operation, String MoveType, String eventType  ) {
		
		   String key= id+"|"+activity+"|"+operation+"|"+MoveType;
		   //int velue=RActivityOpr.get(key);

		   if(!(RActivityOpr.containsKey(key)) ) {
			   RActivityOpr.put(key, 1);
	       	//System.out.println("start event"+"@"+actId+"*"+actName );
	       }
		   else if((RActivityOpr.containsKey(key)) ) {
			   int value= RActivityOpr.get(key);
			   value= value+1; 
			   RActivityOpr.put(key, value);      
			
		   }   
			   
		
		   return true;
		
		}	
	
	private boolean resultOfOperationAbstraction(String id1,String activity1,String operation1, String MoveType1) {
		
		                                     
	   String Key1=	id1+"|"+activity1+"|"+operation1+"|"+ MoveType1;
	   String key2= id1+"|"+activity1+"|"+MoveType1;
	
	   
	   if(MoveType1.equalsIgnoreCase("Totally Sync. Move") || MoveType1.equalsIgnoreCase("Totally Sync. Move with penalty cost") ||
			   MoveType1.equalsIgnoreCase("Partially Sync. Move") || MoveType1.equalsIgnoreCase("Partially Sync. Move with penalty cost")){
		   
		   int SC= RActivityOpr.get(Key1);
		   if(!( RActivity.containsKey(key2)) && SC==2) {
			   RActivity.put(key2, 1);
			   
		   }
		   else if(RActivity.containsKey(key2) && SC==2 ) {
			   int value= RActivity.get(key2);
			   value= value+1; 
			   RActivity.put(key2, value);
		   }
	   
	   }
	   else {

	   if(!( RActivity.containsKey(key2)) ) {
		   RActivity.put(key2, 1);

       }
	   else if((RActivity.containsKey(key2)) ) {
		   int value= RActivity.get(key2);
		   value= value+1; 
		   RActivity.put(key2, value);
		  
	   }
		
	}
	   return true;
	
	}	
	
private boolean resultInActivityLevel(String id2,String activity2, String MoveType2) {
		
                                   
	   String Key2=	id2+"|"+activity2+"|"+ MoveType2;
	   int numberOfOP= RActivity.get(Key2);
	   
	   String key3= id2+"|"+activity2;
	   String value3= MoveType2+":"+ String.valueOf(numberOfOP); 

	  

	   if(!( RActivityID.containsKey(key3)) ) {
		   RActivityID.put(key3, value3);

       }
	   else if((RActivityID.containsKey(key3)) ) {
		   String value= RActivityID.get(key3);
		   value= value+"|"+ value3 ; 
		   RActivityID.put(key3, value);
		  
	   }
		
	   return true;
	
	}	
	
}
