package org.processmining.multilayeralignment.algorithms.syncproduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.processmining.multilayeralignment.models.mlreplay.StepTypes;
import org.processmining.multilayeralignment.models.mlreplay.StepTypesVOL;
import org.processmining.multilayeralignment.models.mlreplay.SyncReplayResult;

public class Old_MLAVisualMovesOnLogWithContext {
	private final Map<String, String> RActivityTypeMove=new HashMap<>();//result abstract from activity and operation- Key is activity*transitionIDofActivityOnModel| Valueis as/c@eventID*activity|Movetype+MatchedID*dataoperation|MoveType
	private final Map<String, String> RActivityType=new HashMap<>();//result abstract from Moves(final result for visualization)- Key is id-activity-eventType
	private final Map<String, String> RActivityTypeOprlist=new HashMap<>();
	public static SyncReplayResult resultForVOL;
	
	public Old_MLAVisualMovesOnLogWithContext(SyncReplayResult res) {
		
		//System.out.println(res.getTraceIndex().toString());
		
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

			//String alignmentLabel= node.toString();
			String originalModelTransitionId="";
			String activity="";
			String matchedProcessEventID="";
			String activitySCType="";
			String operation="";
			String operationType="";
			
			String activityPart="";
			String modelMoveType="";
			String moveType="";
			
			String originalAlignmentLable="";
			String newNodeName="";
			
			String value1="";
			
			//1!a:A+START|1@t:4,e:0
			if(type.toString().equalsIgnoreCase("PSM") || type.toString().equalsIgnoreCase("PSMP")){
				
				originalModelTransitionId = node.toString().substring(0, node.toString().indexOf("!"));
				activity=node.toString().substring(node.toString().indexOf("!")+3,node.toString().indexOf("+"));
				activitySCType=node.toString().substring(node.toString().indexOf("+")+1,node.toString().indexOf("|"));
				matchedProcessEventID= node.toString().substring(node.toString().indexOf("|")+1, node.toString().indexOf("@"));
				operation="N";
				operationType="N";
				moveType=type.toString();
				
				originalAlignmentLable=matchedProcessEventID+"|"+activity+"|"+operation+"#"+activitySCType;
				stepTypesOriginalAlignment.add(res.getStepTypes().get(step));
				nodeInstanceOriginalAlignment.add(originalAlignmentLable);
				
				//newNodeName=activity+"*"+matchedProcessEventID+"#"+activitySCType;
				newNodeName=originalModelTransitionId+":"+activity+"*"+matchedProcessEventID+"#"+activitySCType;
				rearrangedNodeInstance.add(newNodeName);
				if(activitySCType.equalsIgnoreCase("start") || activitySCType.equalsIgnoreCase("s") ) {
				value1="@as:"+moveType+"!" +"#"+matchedProcessEventID+"|"+activity;
				    }
				if(activitySCType.equalsIgnoreCase("complete") || activitySCType.equalsIgnoreCase("c") ) {
					value1="@ac:"+moveType+"!" +"#"+matchedProcessEventID+"|"+activity;
					}
			}
			//1!d:Read(x)_M*FALSE|A*1@t:6,d:0
			//there is a plroblem with * in operation type
           if(type.toString().equalsIgnoreCase("TSM") || type.toString().equalsIgnoreCase("TSMP")){
        	    originalModelTransitionId = node.toString().substring(0, node.toString().indexOf("!"));
        	    activityPart=node.toString().substring(node.toString().indexOf("|")+1,node.toString().indexOf("@"));
				activity=activityPart.substring(0,activityPart.indexOf("*"));
				activitySCType="N";
				matchedProcessEventID= activityPart.substring(activityPart.indexOf("*")+1, activityPart.length());
				//operation=node.toString().substring(node.toString().indexOf("!")+3,node.toString().indexOf("_") );
				operation=node.toString().substring(node.toString().indexOf("!")+3,ordinalIndexOf(node.toString(), "*", 1));
				operationType=node.toString().substring(node.toString().indexOf("_")+1,node.toString().indexOf("*"));
				moveType=type.toString();
				
				originalAlignmentLable=matchedProcessEventID+"|"+activity+"|"+operation+"#"+activitySCType;
				stepTypesOriginalAlignment.add(res.getStepTypes().get(step));
				nodeInstanceOriginalAlignment.add(originalAlignmentLable);
				
				//newNodeName=activity+"*"+matchedProcessEventID+"#"+activitySCType;
				newNodeName=originalModelTransitionId+":"+activity+"*"+matchedProcessEventID+"#"+activitySCType;
				rearrangedNodeInstance.add(newNodeName);
				
				value1="@d*"+operationType+operation+":"+moveType+"!";
			}
           
       	   if(type.toString().equalsIgnoreCase("MM")) {
       		    
       		    modelMoveType=node.toString().substring(node.toString().indexOf("!")+1,node.toString().indexOf(":"));
       		    // 1!d:Read(x)_M*FALSE|A
       		    if(modelMoveType.equalsIgnoreCase("d")) {
       		    	originalModelTransitionId = node.toString().substring(0, node.toString().indexOf("!"));
       		    	activity=node.toString().substring(node.toString().indexOf("|")+1,node.toString().length());
       		    	activitySCType="N";
       		    	matchedProcessEventID= "N";
       		    	//operation=node.toString().substring(node.toString().indexOf("!")+3,node.toString().indexOf("_"));
       		    	operation=node.toString().substring(node.toString().indexOf("!")+3,node.toString().indexOf("*"));
       		    	operationType=node.toString().substring(node.toString().indexOf("_")+1,node.toString().indexOf("*"));
       		    	if(operationType.equalsIgnoreCase("M")) {
       		    		moveType="MM_dm";	
       		    		value1="@d*"+operationType+operation+":"+moveType;
       		    	}else if(operationType.equalsIgnoreCase("O") && !operation.contains("Na(Na)") ){
       		    		
       		    		moveType="MM_do";
       		    		value1="@d*"+operationType+operation+":"+moveType;
       		    		
       		    	}else if(operationType.equalsIgnoreCase("O") && operation.contains("Na(Na)") ){
       		    		
       		    		moveType="MM_do";
       		    		value1="@d*"+"ONN_O"+":"+"TSM";
       		    	}
       		    	originalAlignmentLable=matchedProcessEventID+"|"+activity+"|"+operation+"#"+activitySCType;
    				stepTypesOriginalAlignment.add(res.getStepTypes().get(step));
    				nodeInstanceOriginalAlignment.add(originalAlignmentLable);
    				
    				//newNodeName=activity+"*"+matchedProcessEventID+"#"+activitySCType;
    				newNodeName=originalModelTransitionId+":"+activity+"*"+matchedProcessEventID+"#"+activitySCType;
    				rearrangedNodeInstance.add(newNodeName);
    				//value1="@d*"+operationType+operation+":"+moveType;
       		    	
       		    }
       		    // 1!a:A+START
       		    if(modelMoveType.equalsIgnoreCase("a")) {
       		    	originalModelTransitionId = node.toString().substring(0, node.toString().indexOf("!"));
       		    	//System.out.println("node**"+node.toString());
       		    	if(!node.toString().contains("+")) {
       		    		activity=node.toString().substring(node.toString().indexOf("!")+3,node.toString().indexOf("_"));
       		    		activitySCType=node.toString().substring(node.toString().indexOf("_")+1,node.toString().length());
       		    	}
       		    	else {
       		    	    activity=node.toString().substring(node.toString().indexOf("!")+3,node.toString().indexOf("+"));
       		    	 activitySCType=node.toString().substring(node.toString().indexOf("+")+1,node.toString().length());
       		    	}
       		    	//activitySCType=node.toString().substring(node.toString().indexOf("+")+1,node.toString().length());
        		    matchedProcessEventID= "N";
        		    operation="N";
        		    operationType="N";
        		    moveType="MM_a";
        		    
        		    originalAlignmentLable=matchedProcessEventID+"|"+activity+"|"+operation+"#"+activitySCType;
    				stepTypesOriginalAlignment.add(res.getStepTypes().get(step));
    				nodeInstanceOriginalAlignment.add(originalAlignmentLable);
    				
    				//newNodeName=activity+"*"+matchedProcessEventID+"#"+activitySCType;
    				newNodeName=originalModelTransitionId+":"+activity+"*"+matchedProcessEventID+"#"+activitySCType;
    				rearrangedNodeInstance.add(newNodeName);
    				
    				if(activitySCType.equalsIgnoreCase("start") || activitySCType.equalsIgnoreCase("s") ) {
    					value1="@as:"+moveType +"#"+matchedProcessEventID+"|"+activity;
    					    }
    					if(activitySCType.equalsIgnoreCase("Complete") || activitySCType.equalsIgnoreCase("c") ) {
    						value1="@ac:"+moveType +"#"+matchedProcessEventID+"|"+activity;
    						}
        		    }
       		    
       	   }
       	      
			// e(0):A+START|1@Sara
			if(type.toString().equalsIgnoreCase("PLM")){
						
				originalModelTransitionId = node.toString().substring(0,node.toString().indexOf(":"));
   		    	activity=node.toString().substring(node.toString().indexOf(":")+1,node.toString().indexOf("|"));
    		    matchedProcessEventID= node.toString().substring(node.toString().indexOf("|")+1, node.toString().indexOf("@"));
    		    activitySCType=node.toString().substring(node.toString().indexOf("+")+1,node.toString().indexOf("|"));
    		    operation="N";
    		    operationType="N";
    		    moveType=type.toString();
    		    
    		    originalAlignmentLable=matchedProcessEventID+"|"+activity+"|"+operation+"#"+activitySCType;
				stepTypesOriginalAlignment.add(res.getStepTypes().get(step));
				nodeInstanceOriginalAlignment.add(originalAlignmentLable);
				
				newNodeName=originalModelTransitionId+":"+activity+"*"+matchedProcessEventID;
				rearrangedNodeInstance.add(newNodeName);
				
				value1="@ac:"+moveType +"#"+matchedProcessEventID+"|"+activity;
					}
			// problem
			// d(0):Read:(x)@Sara
			if(type.toString().equalsIgnoreCase("DLM")){
				String dataEventID="";
				originalModelTransitionId = node.toString().substring(0,node.toString().indexOf(":"));
   		    	activity="N";
    		    matchedProcessEventID= "N";
    		    activitySCType="N";
    		    //operation=node.toString().substring(node.toString().indexOf(":")+1,node.toString().indexOf("@"));
    		    dataEventID=node.toString().substring(node.toString().indexOf("(")+1, node.toString().indexOf(")"));
    		    operation=node.toString().substring(ordinalIndexOf(node.toString(), ":", 1),node.toString().indexOf("@"))+"_"+dataEventID;
    		    
    		    operationType="N";
    		    moveType=type.toString();
    		    
    		    originalAlignmentLable=matchedProcessEventID+"|"+activity+"|"+operation+"#"+activitySCType;
				stepTypesOriginalAlignment.add(res.getStepTypes().get(step));
				nodeInstanceOriginalAlignment.add(originalAlignmentLable);
				
				newNodeName=originalModelTransitionId+":"+activity+"*"+matchedProcessEventID;
				rearrangedNodeInstance.add(newNodeName);
				value1="@d*"+operationType+operation+":"+moveType;
					}
		


			resultOfOprAbstraction(originalModelTransitionId,matchedProcessEventID,activity,operation,moveType,activitySCType,value1 );
			
		}
		
		
		
		RActivityTypeMove.forEach((key1, value1) -> {
			String key2="";
			String peventID="";
			int count_MM_do= 0;
			if(!( value1.contains("DLM")) && !(value1.startsWith("@d")) && value1.contains("++") ) {
				//System.out.println("Key:+"+key1);
				System.out.println("value:+"+value1);
			    peventID = value1.substring(ordinalIndexOf(value1, "#", 1)+1 , ordinalIndexOf(value1, "|", 1));
			}
			else {
				peventID="N";
			}
			int count_TSM = StringUtils.countMatches(value1, "TSM!");
			int count_TSMP = StringUtils.countMatches(value1, "TSMP");
			int count_MM_dm = StringUtils.countMatches(value1, "MM_dm");
			if(value1.contains("Na(Na)")) {
			   count_MM_do= 0;
			}else {
			   count_MM_do= StringUtils.countMatches(value1, "MM_do");
			}
			int count_PSM= StringUtils.countMatches(value1, "PSM!");
			int count_PSMP= StringUtils.countMatches(value1, "PSMP");
			int count_MM_a= StringUtils.countMatches(value1, "MM_a");
			
			String value2="TSM:"+count_TSM+"|"+"TSMP:"+count_TSMP+"|"+"MM_dm:"+count_MM_dm+"|"+"MM_do:"+count_MM_do;
			
			if(value1.contains("@as:PSM!#"))
			{ key2= "PSM|"+key1+"*"+peventID+"#START";
			  RActivityType.put(key2, value2);
			}
			
			if(value1.contains("@ac:PSM!#"))
			{ key2= "PSM|"+key1+"*"+peventID+"#COMPLETE";
			  RActivityType.put(key2, value2);
			}
			
			if(value1.contains("@as:PSMP!#"))
			{ key2= "PSMP|"+key1+"*"+peventID+"#START";
			  RActivityType.put(key2, value2);
			}
			
			if(value1.contains("@ac:PSMP!#"))
			{ key2= "PSMP|"+key1+"*"+peventID+"#COMPLETE";
			  RActivityType.put(key2, value2);
			}
			
			if(value1.contains("@as:MM_a#"))
			{ key2= "MM_a|"+key1+"*"+peventID+"#START";
			  RActivityType.put(key2, value2);
			}
			
			if(value1.contains("@ac:MM_a"))
			{ key2= "MM_a|"+key1+"*"+peventID+"#COMPLETE";
			  RActivityType.put(key2, value2);
			}
			
			if(value1.contains("PLM"))
			{ key2= "PLM|"+key1+"*"+peventID;
			  RActivityType.put(key2, "NA");
			}
			
			if(value1.contains("DLM"))
			{ key2= "DLM|"+key1+"*"+peventID;
			  RActivityType.put(key2, "NA");
			}
			
			
			
    });
		
		RActivityType.forEach((key2, value2) -> {
			String MoveTypes="";
	        String Operations="";
			if(value2.contains("|")) {
				
				StringTokenizer strings = new StringTokenizer(value2, "|");
				while(strings.hasMoreTokens()){
				    String MoveOPR = strings.nextToken();
				    String oprCount=MoveOPR.substring(MoveOPR.indexOf(":")+1,MoveOPR.length());
				    if(!oprCount.equalsIgnoreCase("0")) {
				    MoveTypes+= key2.substring(0, key2.indexOf("|"))+"+"+MoveOPR.substring(0, MoveOPR.indexOf(":"))+"+";
				    Operations+= "+"+MoveOPR;
				    }

				}
		        resultOfActivityTypeOPrList_VisOnLog(key2,Operations,MoveTypes); 
	        
			}
	        else {
	        	MoveTypes+= key2.substring(0, key2.indexOf("|"))+"+";
	        	Operations="+"+value2;
	        	resultOfActivityTypeOPrList_VisOnLog(key2,Operations,MoveTypes); 	
	        }
			
    });
		
		
		resultForVOL=calculateResultForVOL(nodeInstanceOriginalAlignment,stepTypesOriginalAlignment,rearrangedNodeInstance ,res );
		
	}
	
	
	private boolean resultOfOprAbstraction(String modelId,String id,String activity,String operation, String MoveType, String eventType,String resultValue  ) {
		
		   //String key= modelId+"|"+activity+"|"+eventType+"|"+MoveType;

		   //String key= modelId+":"+activity;
		   String key= modelId+":"+activity+"$"+id;
		  
		   if(!key.contains(":")) {
		   if(!(RActivityTypeMove.containsKey(key)) ) {
			   RActivityTypeMove.put(key, resultValue);
			   
	       }
		   else if((RActivityTypeMove.containsKey(key)) ) {
			   String value= RActivityTypeMove.get(key);
			   value= value+"+"+resultValue; 
			   RActivityTypeMove.put(key, value);      
			
		   }   
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
		
		//for activities without data operation in data model :-OOOOOOO
		if(key2.contains("PSM") && operations.equalsIgnoreCase("") && moveTypes.equalsIgnoreCase("") ) {
		RActivityTypeOprlist.put(key3+"+TSM:0", "PSM+TSM+");
		}
		else {
			RActivityTypeOprlist.put(key3, value3);
		}
	}
	
	private SyncReplayResult calculateResultForVOL(List<Object> nodeInstanceOriginalAlignment, List<StepTypes> stepTypesOriginalAlignment,List<Object> newNodeNames ,SyncReplayResult res ) {
		
		List<StepTypesVOL> stepTypesVOL = new ArrayList<StepTypesVOL>();
		List<Object> nodeInstanceVOL = new ArrayList<Object>();
		
		RActivityTypeOprlist.forEach((key, value) -> {
			
			boolean PSM=false;
			boolean PSMP=false;
			boolean TSM=false;
			boolean TSMP=false;
			boolean MM_dm=false;
			boolean MM_do=false;
			boolean MM_a=false;
			boolean PLM=false;
			boolean DLM=false;
			
			if(value.contains("PSM+")) {
				PSM=true;
			}
			if(value.contains("PSMP+")) {
				PSMP=true;
			}
			
			if(value.contains("TSM+")) {
				TSM=true;
			}
			if(value.contains("TSMP+")) {
				TSMP=true;
			}
			if(value.contains("MM_dm+")) {
				MM_dm=true;
			}
			if(value.contains("MM_do+")) {
				MM_do=true;
			}
			if(value.contains("MM_a+")) {
				MM_a=true;
			}
			if(value.contains("PLM+")) {
				PLM=true;
			}
			if(value.contains("DLM+")) {
				DLM=true;
			}
			
			
			//all data opr sync.
			if(!(PSMP || TSMP || MM_dm || MM_do|| MM_a ) & PSM & TSM ) {
				nodeInstanceVOL.add(key);
			    stepTypesVOL.add(StepTypesVOL.TSM);	
			}
			
			//all data opr sync. by wrong role
			if(!(PSM || TSM || MM_dm || MM_do|| MM_a) & PSMP & TSMP ){
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMP);	
				}
			//all data opr missing(mandatory)
			if(!(PSMP || TSMP || MM_do|| MM_a ||TSM )& MM_dm & PSM) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PSM);	
				}
			//all data opr missing(optional)
			if(!(PSMP || TSMP || MM_dm|| MM_a ||TSM )& MM_do & PSM) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.OPSM);	
				}
			//???all data opr missing(optional)
			if(!(PSMP || TSMP || MM_a ||TSM )& MM_do & MM_dm & PSM) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PSM);	
				}
			//all data opr(mandatory) missing & activity done by wrong role
			if(!(PSM || TSMP || MM_do|| MM_a || TSM )& MM_dm & PSMP) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PSMP);	
				}
			//all data opr(optional) missing & activity done by wrong role
			if(!(PSM || TSMP || MM_dm|| MM_a || TSM )& MM_do & PSMP) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.OPSMP);	
				}
			//???all data opr(optional) missing & activity done by wrong role
			if(!(PSM || TSMP || MM_a || TSM )& MM_do& MM_dm & PSMP) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PSMP);	
				}
			//Some data opr sync. some mandatory missing
			if(!(PSMP || TSMP || MM_a || MM_do )& MM_dm & PSM & TSM ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMPSM);	
				}
			//Some data opr sync. some mandatory missing with wrong role
			if(!(PSM ||TSM || MM_do|| MM_a )& MM_dm & PSMP & TSMP ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMPPSMP);	
				}
			//Some data opr sync. some optional missing
			if(!(PSMP || TSMP || MM_dm|| MM_a )& MM_do & PSM & TSM ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMOPSM);	
				}
			//Some data opr sync. some optional missing with wrong role
			if(!(PSM || TSM || MM_dm|| MM_a )& MM_do & PSMP & TSMP ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMPOPSMP);	
				}
			//??Some data opr sync.,some mandatory and some optional missing with wrong role
			if(!(PSMP || TSMP || MM_a )& MM_dm & MM_do & PSM & TSM ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMPSM);	
				}
			//???Some data opr sync.,some mandatory and some optional missing with wrong role
			if(!(PSM || TSM || MM_a )& MM_dm & MM_do & PSMP & TSMP ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMPPSMP);	
				}
			
			//move on model- only activity
			if(!(PSMP || TSMP || MM_dm || MM_do  || PSM || TSM) & MM_a ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.MM);	
				}
			//move on model- activity and mandatory data operation
			if(!(PSMP || TSMP  || MM_do  || PSM || TSM) & MM_a & MM_dm ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.MM);	
				}
			//move on model- activity and optional data operation
			if(!(PSMP || TSMP  || MM_dm  || PSM || TSM) & MM_a & MM_do ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.MM);	
				}
			//???move on model- activity and optional and mandatory data operation
			if(!(PSMP || TSMP || PSM || TSM) & MM_a & MM_dm & MM_do ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.MM);	
				}
			//move on process log
			if(!(PSMP || TSMP || MM_dm || MM_do  || PSM || TSM || MM_a ) & PLM ) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PLM);	
				}
			//move on data log
			if(!(PSMP || TSMP || MM_dm || MM_do  || PSM || TSM || MM_a ) & DLM) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.DLM);	
				}
			
			
			/*
			if(value.equalsIgnoreCase("+PSM")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PSM);	
				}
			if(value.equalsIgnoreCase("+OPSM")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.OPSM);	
				}
			if(value.equalsIgnoreCase("+PSMP")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.PSMP);	
				}
			if(value.equalsIgnoreCase("+OPSMP")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.OPSMP);	
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

			if(value.equalsIgnoreCase("+TSM+OPSM")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMOPSM);	
				}
			if(value.equalsIgnoreCase("+OPSM+TSM")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.OPSMTSM);	
				}
			if(value.equalsIgnoreCase("+TSMP+OPSMP")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.TSMPOPSMP);	
				}
			if(value.equalsIgnoreCase("+OPSMP+TSMP")) {
				nodeInstanceVOL.add(key);
				stepTypesVOL.add(StepTypesVOL.OPSMPTSMP);	
				}
				*/
    });
		
		
		SyncReplayResult resultForVOL=null;
		for (int traceIndex : res.getTraceIndex()) {
			resultForVOL = new SyncReplayResult(nodeInstanceOriginalAlignment,stepTypesOriginalAlignment,nodeInstanceVOL, stepTypesVOL, traceIndex,newNodeNames,"VisOnLogh");
			
			resultForVOL.setReliable(res.isReliable());
			resultForVOL.setInfo(res.getInfo());
			
		}

		return resultForVOL;
	}
	
	public static int ordinalIndexOf(String str, String substr, int n) {
	    int pos = str.indexOf(substr);
	    while (--n > 0 && pos != -1)
	        pos = str.indexOf(substr, pos + 1);
	    return pos;
	}
	
}
