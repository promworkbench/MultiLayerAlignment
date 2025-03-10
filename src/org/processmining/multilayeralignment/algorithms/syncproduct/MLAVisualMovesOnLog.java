package org.processmining.multilayeralignment.algorithms.syncproduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.processmining.multilayeralignment.models.mlreplay.StepTypes;
import org.processmining.multilayeralignment.models.mlreplay.StepTypesVOL;
import org.processmining.multilayeralignment.models.mlreplay.SyncReplayResult;

public class MLAVisualMovesOnLog {
	private final Map<String, String> RActivityTypeMove=new HashMap<>();//result abstract from operation- Key is id-activity-eventType-MoveType
	private final Map<String, String> RActivityType=new HashMap<>();//result abstract from Moves(final result for visualization)- Key is id-activity-eventType
	private final Map<String, String> RActivityTypeOprlist=new HashMap<>();
	public static SyncReplayResult resultForVOL;
	
	public MLAVisualMovesOnLog(SyncReplayResult res) {
		
		
		
		List<StepTypes> stepTypesOriginalAlignment = new ArrayList<StepTypes>(res.getNodeInstance().size());
		List<Object> nodeInstanceOriginalAlignment = new ArrayList<Object>();
		List<Object> rearrangedNodeInstance= new LinkedList<Object>();

		
		RActivityTypeMove.clear();
		RActivityType.clear();
		RActivityTypeOprlist.clear();
		rearrangedNodeInstance.clear();
		
		for (int step=0; step < res.getNodeInstance().size(); step++) {
			
			StepTypes type = res.getStepTypes().get(step);
			Object node = res.getNodeInstance().get(step);
			String id="";
			String activity="";
			String operation="";
			String typeOfActivity="";
			String originalAlignmentLable="";
			String newNodeName="";
			
			
			if(type.toString().equalsIgnoreCase("TSM") || type.toString().equalsIgnoreCase("TSMP") ||
			   type.toString().equalsIgnoreCase("PSM") || type.toString().equalsIgnoreCase("PSMP")){
				
				id=node.toString().substring(node.toString().indexOf("|")+1, node.toString().length());
				activity=node.toString().substring(0,node.toString().indexOf("+"));
				operation=node.toString().substring(node.toString().indexOf(":")+1 ,node.toString().indexOf("+", node.toString().indexOf("+") + 1));
				typeOfActivity=node.toString().substring(node.toString().indexOf("+")+1,node.toString().indexOf(":"));
				
				originalAlignmentLable=id+"|"+activity+"|"+operation+"#"+typeOfActivity;
				stepTypesOriginalAlignment.add(res.getStepTypes().get(step));
				nodeInstanceOriginalAlignment.add(originalAlignmentLable);
				
				newNodeName=id+"|"+activity+"|"+typeOfActivity+"|";
				rearrangedNodeInstance.add(newNodeName);

				
			}
			
			if(type.toString().equalsIgnoreCase("PLM")){
						
				id=node.toString().substring(node.toString().indexOf("|")+1, node.toString().length());
				activity=node.toString().substring(0,node.toString().indexOf("|"));
				operation="N";
				typeOfActivity="N";
				
				originalAlignmentLable=id+"|"+activity+"|"+operation+"#"+typeOfActivity;
				stepTypesOriginalAlignment.add(res.getStepTypes().get(step));
				nodeInstanceOriginalAlignment.add(originalAlignmentLable);
				
				newNodeName=id+"|"+activity+"|"+typeOfActivity+"|";
				rearrangedNodeInstance.add(newNodeName);
					}
			
			if(type.toString().equalsIgnoreCase("DLM")){
				
				id=node.toString().substring(node.toString().indexOf("|")+1, node.toString().length());
				activity="N";
				operation=node.toString().substring(0,node.toString().indexOf("|"));
				typeOfActivity="N";
				
				originalAlignmentLable=id+"|"+activity+"|"+operation+"#"+typeOfActivity;
				stepTypesOriginalAlignment.add(res.getStepTypes().get(step));
				nodeInstanceOriginalAlignment.add(originalAlignmentLable);
				
				newNodeName=id+"|"+activity+"|"+typeOfActivity+"|";
				rearrangedNodeInstance.add(newNodeName);
					}
			if(type.toString().equalsIgnoreCase("MM")) {
			    id="N";
				activity=node.toString().substring(0,node.toString().indexOf("+"));
				operation=node.toString().substring(node.toString().indexOf(":")+1 , node.toString().length() );
				typeOfActivity="N";
				
				originalAlignmentLable=id+"|"+activity+"|"+operation+"#"+typeOfActivity;
				stepTypesOriginalAlignment.add(res.getStepTypes().get(step));
				nodeInstanceOriginalAlignment.add(originalAlignmentLable);
				
				newNodeName=id+"|"+activity+"|"+typeOfActivity+"|";
				rearrangedNodeInstance.add(newNodeName);
			}


			resultOfOprAbstraction(id,activity,operation,type.toString(), typeOfActivity);
			
		}
		
		
		
		RActivityTypeMove.forEach((key1, value1) -> {

			int splitIndex1= 1+ key1.toString().indexOf("|", key1.toString().indexOf("|") + 1);
			
			
			String id1 = key1.toString().substring(0,key1.toString().indexOf("|") );
			String activity1 = key1.toString().substring(key1.toString().indexOf("|")+1 , key1.toString().indexOf("|", key1.toString().indexOf("|") + 1) ); 
			String eventType1 = key1.toString().substring(splitIndex1 , key1.toString().lastIndexOf("|"));
			String moveType1 = key1.toString().substring(key1.toString().lastIndexOf("|")+1 , key1.length());
			
			resultOfMoveAbstraction(id1,activity1,eventType1,moveType1); 	
			
    });
		
		RActivityType.forEach((key2, value2) -> {

			
			
			String MoveTypes="";
	        String Operations="";
			if(value2.contains("|")) {
				
				StringTokenizer strings = new StringTokenizer(value2, "|");
				while(strings.hasMoreTokens()){
				    String MoveOPR = strings.nextToken();
				    MoveTypes+= "+"+MoveOPR.substring(0, MoveOPR.indexOf(":"));
				    Operations+= "+"+MoveOPR;

				}
				
				
			
	       
	        
		        resultOfActivityTypeOPrList_VisOnLog(key2,Operations,MoveTypes); 
	        
			}
	        else {
	        	MoveTypes+= "+"+value2.substring(0, value2.indexOf(":"));
	        	Operations="+"+value2;
	        	resultOfActivityTypeOPrList_VisOnLog(key2,Operations,MoveTypes); 	
	        }
			
    });
		
		
		resultForVOL=calculateResultForVOL(nodeInstanceOriginalAlignment,  stepTypesOriginalAlignment,rearrangedNodeInstance ,res );
		
	}
	
	
	private boolean resultOfOprAbstraction(String id,String activity,String operation, String MoveType, String eventType  ) {
		
		   String key= id+"|"+activity+"|"+eventType+"|"+MoveType;


		   if(!(RActivityTypeMove.containsKey(key)) ) {
			   RActivityTypeMove.put(key, operation);
	       }
		   else if((RActivityTypeMove.containsKey(key)) ) {
			   String value= RActivityTypeMove.get(key);
			   value= value+"+"+operation; 
			   RActivityTypeMove.put(key, value);      
			
		   }   
		   	   
		
		   return true;
		
		}	
	
	private boolean resultOfMoveAbstraction(String id1,String activity1,String eventType1, String MoveType1) {
		
		                                     
	   String Key1=	id1+"|"+activity1+"|"+eventType1+"|"+ MoveType1;
	   String numberOfOP= RActivityTypeMove.get(Key1);
	   
	   String key2= id1+"|"+activity1+"|"+eventType1;
	   String value3= MoveType1+":"+ numberOfOP;
	   
	   if(!( RActivityType.containsKey(key2)) ) {
		   RActivityType.put(key2, value3);

       }
	   else if((RActivityType.containsKey(key2)) ) {
		   String value= RActivityType.get(key2);
		   value= value+"|"+ value3 ; 
		   RActivityType.put(key2, value);
		  
	   }
		
	   return true;
	
	  
	}	
	
	private void resultOfActivityTypeOPrList_VisOnLog(String key2,String operations,String moveTypes) {
		String key3=key2+"|"+operations;
		String value3=	moveTypes;	
		RActivityTypeOprlist.put(key3, value3); 
	}
	
	private SyncReplayResult calculateResultForVOL(List<Object> nodeInstanceOriginalAlignment, List<StepTypes> stepTypesOriginalAlignment,List<Object> newNodeNames ,SyncReplayResult res ) {
		
		List<StepTypesVOL> stepTypesVOL = new ArrayList<StepTypesVOL>();
		List<Object> nodeInstanceVOL = new ArrayList<Object>();
		
		RActivityTypeOprlist.forEach((key, value) -> {
			if(value.equalsIgnoreCase("+TSM")) {
				nodeInstanceVOL.add(key);
			stepTypesVOL.add(StepTypesVOL.TSM);	
			}
			if(value.equalsIgnoreCase("+TSMP")){
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMP);	
				}
			if(value.equalsIgnoreCase("+PSM")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PSM);	
				}
			if(value.equalsIgnoreCase("+PSMP")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PSMP);	
				}
			if(value.equalsIgnoreCase("+MM")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.MM);	
				}
			if(value.equalsIgnoreCase("+PLM")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PLM);	
				}
			if(value.equalsIgnoreCase("+DLM")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.DLM);	
				}
			
			if(value.equalsIgnoreCase("+TSM+PSM")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMPSM);	
				}
			if(value.equalsIgnoreCase("+PSM+TSM")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PSMTSM);	
				}
			if(value.equalsIgnoreCase("+TSMP+PSMP")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMPPSMP);	
				}
			if(value.equalsIgnoreCase("+PSMP+TSMP")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PSMPTSMP);	
				}
    });
		
		
		SyncReplayResult resultForVOL=null;
		for (int traceIndex : res.getTraceIndex()) {
			resultForVOL = new SyncReplayResult(nodeInstanceOriginalAlignment,stepTypesOriginalAlignment,nodeInstanceVOL, stepTypesVOL, traceIndex,newNodeNames,"VisOnLogh");
			
			resultForVOL.setReliable(res.isReliable());
			resultForVOL.setInfo(res.getInfo());
			
		}

		return resultForVOL;
	}
}
