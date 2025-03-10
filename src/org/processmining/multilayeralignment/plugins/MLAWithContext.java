package org.processmining.multilayeralignment.plugins;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Map.entrySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.csv.CSVFile;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
//import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;
import org.processmining.multilayeralignment.algorithms.ReplayAlgorithm.Debug;
import org.processmining.multilayeralignment.algorithms.syncproduct.MLAVisualMovesOnLogWithContext;
import org.processmining.multilayeralignment.models.mlreplay.Replayer;
//import nl.tue.alignment.ReplayerParameters;
import org.processmining.multilayeralignment.models.mlreplay.ReplayerParameters;
import org.processmining.multilayeralignment.models.mlreplay.ReplayerWithContext;
//import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.multilayeralignment.models.mlreplay.StepTypes;
//import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.multilayeralignment.models.mlreplay.SyncReplayResult;
import org.processmining.multilayeralignment.models.mlreplay.TraceReplayTaskWithContext;
import org.processmining.multilayeralignment.models.mlreplay.Utils;
import org.processmining.multilayeralignment.plugins.replayer.MLAWithContextReplayResult;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;





public class MLAWithContext {
	
	
	public static Map<String,String> dataModelMap= new HashMap<String, String>();
	static Map<Integer,String> SODModelMap= new HashMap<Integer, String>();
	static Map<String,String> orgModelMap= new HashMap<String, String>(); 
	static MLAWithContextReplayResult list = new MLAWithContextReplayResult();	

	
	public static MLAWithContextReplayResult perform(PluginContext context, CSVFile DataModelCSVFile, CSVFile OrganisationalModelCSVFile, Petrinet inNet, XLog inProcessLog , XLog inDataLog)throws Exception{

		

		//LpSolve.lpSolveVersion();
		
		//*Read Data Model
				String row;
				
				CSVFile csvFile = DataModelCSVFile;

					BufferedReader csvReader = new BufferedReader(new InputStreamReader(DataModelCSVFile.getInputStream()));
					int i=1;
					while ((row = csvReader.readLine()) != null) {
						
						DataModelWithContextParser dmEntry = new DataModelWithContextParser(row);
						if(!(dmEntry.getActivityName().isEmpty() || dmEntry.getDataOperation().isEmpty()) ) {
							dataModelMap.put("d"+i ,dmEntry.getActivityName()+"|"+dmEntry.getDataOperation().replace("-", ",")+"$"+dmEntry.getDOType()+"*"+dmEntry.getFrequency() );
						i++;}
					}
					csvReader.close();
					
				
					CSVFile csvFile2 = OrganisationalModelCSVFile;
				
						BufferedReader csvReader2 = new BufferedReader(new InputStreamReader(OrganisationalModelCSVFile.getInputStream()));

						while ((row = csvReader2.readLine()) != null) {
							
							OrganisationalModel omEntry = new OrganisationalModel(row);
							if(!(omEntry.getRole().isEmpty() || omEntry.getRole().equalsIgnoreCase("-"))) {
								orgModelMap.put(omEntry.getUser(), omEntry.getRole());
							}
							
						}
						csvReader2.close();
		
		
        //*Read process model file in pnml format
		
		Petrinet net = constructNet(inNet);
		Marking initialMarking = getInitialMarking(net);
		Marking finalMarking = getFinalMarking(net);
		
		
		//*Read process log in XES format
		
		XLog log;
		XEventClassifier eventClassifier;

		log = inProcessLog;
		eventClassifier = new XEventNameClassifier();
		eventClassifier= XLogInfoImpl.STANDARD_CLASSIFIER;
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		XEventClasses processEvClasses= summary.getEventClasses();
		//read standard format of process log
		XEventClassifier eventClassifier2;
		eventClassifier2 = new XEventNameClassifier();
		eventClassifier2 = XLogInfoImpl.STANDARD_CLASSIFIER;
		XLogInfo summary2 = XLogInfoFactory.createLogInfo(log, eventClassifier2);
		XEventClasses processEvClasses2 = summary2.getEventClasses();

		processEvClasses.getClasses();
		//----------------------------------
		
		//*Read Data log in XES format
		
		XLog dataLog;
		XEventClassifier dataEventClassifier= new XEventNameClassifier();
		dataLog = inDataLog;
		XLogInfo dataLogSummary = XLogInfoFactory.createLogInfo(dataLog, dataEventClassifier);
		XEventClasses dataEvClasses = dataLogSummary.getEventClasses();
		
		//creating parts of Sync. product
		Map<Transition, Integer> costModelMove = new HashMap<>();
		Map<Transition, Integer> costProcessSyncMove = new HashMap<>();
		Map<XEventClass, Integer> costLogMove = new HashMap<>();
		Map<Transition, Integer> costDataSyncMove = new HashMap<>();
		Map<XEventClass, Integer> costDataLogMove = new HashMap<>();
		
		//*creating the costs of process/data model moves
				for (Transition t : net.getTransitions()) {
					costDataSyncMove.put(t, 0);
					costProcessSyncMove.put(t, t.isInvisible() ? 0 : 0);
					if (t.getLabel().contains("_O") || t.getLabel().contains("_o") ) {
						costModelMove.put(t, t.isInvisible() ? 0 : 0);
						}else {
							costModelMove.put(t, t.isInvisible() ? 0 : 2);	
						}
					//System.out.println("*******t is: " + t.getLabel().toString());
				}
				
		//*creating the cost of process log moves
				for (XEventClass c : summary.getEventClasses().getClasses()) {
					costLogMove.put(c, 2);
				}		

		//*creating the cost of Data log moves
	    		for (XEventClass c : dataLogSummary.getEventClasses().getClasses()) {
	    			costDataLogMove.put(c, 2);

	    		}

	    		

		//*create a partially sync moves( a map between transitions in model and event classes in log)
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		TransEvClassMapping mapping1 = constructMappingBasedOnLabelEquality(net, log, dummyEvClass, eventClassifier,costLogMove);
		TransEvClassMapping mapping2= constructMappingDataLayerBasedOnLabelEquality(net, dataLog, dummyEvClass, dataEventClassifier);                   

		
		System.out.println(String.format("Process Log size: %d events, %d traces, %d classes", summary.getNumberOfEvents(),
				summary.getNumberOfTraces(), (summary.getEventClasses().size() + 1)));
		System.out.println(String.format("Data Log size: %d events, %d traces, %d classes", dataLogSummary.getNumberOfEvents(),
				dataLogSummary.getNumberOfTraces(), (dataLogSummary.getEventClasses().size() + 1)));
		System.out.println(String.format("Model size: %d transitions, %d places", net.getTransitions().size(),
				net.getPlaces().size()));	//doReplay(log, net, initialMarking, finalMarking, classes, mapping, costModelMove, costSyncMove, costLogMove);
		doReplay(log,dataLog, net, initialMarking, finalMarking, processEvClasses, dataEvClasses ,mapping1,mapping2 ,costModelMove, costProcessSyncMove, costLogMove, costDataSyncMove, costDataLogMove);
		System.out.println("end of processing");
		

		
		 return list;
	}

	public static void doReplay(XLog log,XLog dataLog, Petrinet net, Marking initialMarking, Marking finalMarking,
			XEventClasses classesP,XEventClasses classesD, TransEvClassMapping mapping1,TransEvClassMapping mapping2, Map<Transition, Integer> costModelMove,
			Map<Transition, Integer> costPartialSyncMove, Map<XEventClass, Integer> costLogMove,Map<Transition, Integer> costSyncMove, Map<XEventClass, Integer> costDataLogMove ) {

		int nThreads = 2;
		int costUpperBound = Integer.MAX_VALUE;

		int timeoutMilliseconds = 10 * 1000;

		int maximumNumberOfStates = Integer.MAX_VALUE;
		ReplayerParameters parameters;

		//parameters = new ReplayerParameters.Dijkstra(false, false, nThreads, Debug.DOT, timeoutMilliseconds,
		//		maximumNumberOfStates, costUpperBound, false, 2, true);

		//Current: 
		//		parameters = new ReplayerParameters.IncrementalAStar(false, nThreads, false, Debug.DOT, timeoutMilliseconds,
		//				maximumNumberOfStates, costUpperBound, false, false, 0, 3);

		//		//BPM2018: 
		//		parameters = new ReplayerParameters.IncrementalAStar(false, nThreads, false, Debug.DOT,
		//						timeoutMilliseconds, maximumNumberOfStates, costUpperBound, false, false);
		//Traditional
				parameters = new ReplayerParameters.AStar(true, true, true, nThreads, true, Debug.NONE,
					timeoutMilliseconds, maximumNumberOfStates, costUpperBound, false);
				

				ReplayerWithContext replayer = new ReplayerWithContext(parameters, net, initialMarking, finalMarking, classesP, classesD, costModelMove,costPartialSyncMove,
				costLogMove, costSyncMove,costDataLogMove, mapping1 ,mapping2,false);

		// preprocessing time to be added to the statistics if necessary
		long preProcessTimeNanoseconds = 0;

		int success = 0;
		int failed = 0;
		ExecutorService service = Executors.newFixedThreadPool(parameters.nThreads);

		@SuppressWarnings("unchecked")
		Future<TraceReplayTaskWithContext>[] futures = new Future[log.size()];

		for (int i = 0; i < log.size(); i++) {
			XAttribute caseID = (log.get(i)).getAttributes().get("concept:name");
			
			if (i==2271)
				System.out.println(i+"case,****************"+caseID);
			
			XTrace dataTrace= null;
			for(int j=0 ;  j < dataLog.size(); j++) { 
			 if(dataLog.get(j).getAttributes().get("concept:name").equals(caseID)){
				 dataTrace=dataLog.get(j);
				 break;
			 }
			}

			// Setup the trace replay task
			boolean withContext=true;
			TraceReplayTaskWithContext task = new TraceReplayTaskWithContext(withContext, replayer, parameters, log.get(i), i,dataTrace, i, timeoutMilliseconds,
					parameters.maximumNumberOfStates, preProcessTimeNanoseconds);
			
			
			futures[i] = service.submit(task);
			//System.out.println(i);
		}
		// initiate shutdown and wait for termination of all submitted tasks.
		service.shutdown();

		// obtain the results one by one.


		
	
		Map<String,String> traceResultMap= new HashMap<String, String>();
		Map<Integer,Integer> cacesWithDeviation= new HashMap<Integer,Integer>();
		
		for (int i = 0; i < log.size(); i++) {

			TraceReplayTaskWithContext result;
			
			try {
				//System.out.println(i+",");
				result = futures[i].get();
				

			} catch (Exception e) {
				// execution on the service has terminated.
				throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			}
			
			String traceResult="";
			SyncReplayResult replayResultForVOL;
			switch (result.getResult()) {
				case DUPLICATE :
					assert false; // cannot happen in this setting
					throw new RuntimeException("Result cannot be a duplicate in per-trace computations.");
				case FAILED :
					// internal error in the construction of synchronous product or other error.
					throw new RuntimeException("Error in alignment computations");
				case SUCCESS :
					// process succcesful execution of the replayer
					SyncReplayResult replayResult = result.getSuccesfulResult();
					//SyncReplayResult replayResultForVOL;
					//********************To Edit
					//********************************************working
					
					//System.out.println(i+"case,****************"+caseID);
					
					MLAVisualMovesOnLogWithContext MultiLayeralignmentResult=new MLAVisualMovesOnLogWithContext(replayResult);
				
					replayResultForVOL= MultiLayeralignmentResult.resultForVOL;
					list.add(replayResultForVOL);
					
					
					
					int exitCode = replayResult.getInfo().get(Replayer.TRACEEXITCODE).intValue();

						if ((Utils.OPTIMALALIGNMENT) == Utils.OPTIMALALIGNMENT) {
						// Optimal alignment found.
						success++;
                        //find original traceID
						
						//Start_Export results for rule mining
						int k=0;
						boolean dO_NoDev=false;
						String Activity="" , DataOperation="", ExpectedRole="" , ExecutedRole="" , User="" ,Deviation="" ;	
						for (int step=0; step < replayResult.getNodeInstance().size(); step++) {
							
							StepTypes type =  replayResult.getStepTypes().get(step);
							Object node =  replayResult.getNodeInstance().get(step);
							String nodeLable=node.toString();
							
							if(type.toString().equalsIgnoreCase("MM")){
								String aORd= nodeLable.substring(nodeLable.indexOf("!")+1, nodeLable.indexOf(":"));
								if(aORd.equalsIgnoreCase("a")) {
									if(nodeLable.contains("+")) {
								String activityName=nodeLable.substring(nodeLable.indexOf("!")+1, nodeLable.indexOf("+"));
								String lifeSycle=nodeLable.substring(nodeLable.indexOf("+")+1, nodeLable.length());	
								String activityOrigTrabsitionID= nodeLable.substring(0,nodeLable.indexOf("!"));
								
								Activity=activityName+"_"+ lifeSycle.charAt(0);
								DataOperation="d:*NA";
								ExpectedRole="r:"+ SODModelMap.get(Integer.parseInt(activityOrigTrabsitionID));
								ExecutedRole="r_e:"+"missing";
								User="u:"+"missing";
								Deviation="dev:"+"MMa";
									}// in case the activity has no data operation in data model
									else {
										String activityName=nodeLable.substring(nodeLable.indexOf("!")+1, nodeLable.indexOf("_"));
										String lifeSycle=nodeLable.substring(nodeLable.indexOf("_")+1, nodeLable.length());	
										String activityOrigTrabsitionID= nodeLable.substring(0,nodeLable.indexOf("!"));
										
										Activity=activityName+"_"+ lifeSycle.charAt(0);
										DataOperation="d:*NA";
										ExpectedRole="r:"+ SODModelMap.get(Integer.parseInt(activityOrigTrabsitionID));
										ExecutedRole="r_e:"+"missing";
										User="u:"+"missing";
										Deviation="dev:"+"MMa";
										
										
									}
								}
								
								if(aORd.equalsIgnoreCase("d")) {
									String activityName=nodeLable.substring(nodeLable.indexOf("|")+1, nodeLable.length());
									String Mandatory=nodeLable.substring(nodeLable.indexOf("_")+1, nodeLable.indexOf("*"));	
									String activityOrigTrabsitionID= nodeLable.substring(0,nodeLable.indexOf("!"));
									
									Activity="ca:"+activityName;
									DataOperation=nodeLable.substring(nodeLable.indexOf("!")+1,nodeLable.indexOf("*")).replace(",", "-");
									if (DataOperation.equalsIgnoreCase("d:Na(Na)_O")){
										dO_NoDev= true;
									}
							
									ExpectedRole="r:"+SODModelMap.get(Integer.parseInt(activityOrigTrabsitionID));
									ExecutedRole="r_e:"+"missing";
									User="u:"+"missing";
									if(Mandatory.equalsIgnoreCase("M")) {
									Deviation="dev:"+"MMdm";
									}
									if(Mandatory.equalsIgnoreCase("O")) {
									Deviation="dev:"+"MMdo";	
									}
									}
								
								
							}
							else if (type.toString().equalsIgnoreCase("PSMP")) {
								String activityName=nodeLable.substring(nodeLable.indexOf("!")+1, nodeLable.indexOf("+"));
								String lifeSycle=nodeLable.substring(nodeLable.indexOf("+")+1, nodeLable.indexOf("|"));	
								String activityOrigTrabsitionID= nodeLable.substring(0,nodeLable.indexOf("!"));
								String matchedEventID1=nodeLable.substring(nodeLable.indexOf(",")+1, nodeLable.length()); 
								String matchedEventID=matchedEventID1.substring(matchedEventID1.indexOf(":")+1,matchedEventID1.length());
								
								//get actor of process event from process log
								
								String actor= log.get(i).get(Integer.parseInt(matchedEventID)).getAttributes().get("org:resource").toString();
								
								//get the role of user from orgmodel
								ArrayList<String> roleListOnOrgModel=new ArrayList<String>();
								roleListOnOrgModel.addAll(MLAWithContext.getRolefromOrganisationalModelMap(actor));
								
								Activity="c"+activityName+"_"+ lifeSycle.charAt(0);
								DataOperation="d:*NA";
								ExpectedRole="r:"+ SODModelMap.get(Integer.parseInt(activityOrigTrabsitionID));
								ExecutedRole="r_e:"+roleListOnOrgModel.get(0);
								User="u:"+actor;
								Deviation="dev:"+"PSMP";
							}
							else if (type.toString().equalsIgnoreCase("TSMP")) {
								//System.out.println(nodeLable);
								String activityName1=nodeLable.substring(nodeLable.indexOf("|")+1, nodeLable.indexOf("@"));
								String activityName=activityName1.substring(0, activityName1.indexOf("*"));
								
								String matchedEventID1=nodeLable.substring(nodeLable.indexOf("@")+1, nodeLable.length()); 
								String matchedEventID2=matchedEventID1.substring(matchedEventID1.indexOf(",")+1, matchedEventID1.length()); 
								String matchedEventID=matchedEventID2.substring(matchedEventID2.indexOf(":")+1,matchedEventID2.length());
								
                                //get actor of process event from process log
								String actor= dataLog.get(i).get(Integer.parseInt(matchedEventID)).getAttributes().get("org:resource").toString();
								
								//get the role of user from orgmodel
								ArrayList<String> roleListOnOrgModel=new ArrayList<String>();
								roleListOnOrgModel.addAll(MLAWithContext.getRolefromOrganisationalModelMap(actor));
								
								String activityOrigTrabsitionID= nodeLable.substring(0,nodeLable.indexOf("!"));
								String dataOpr1=nodeLable.substring(nodeLable.indexOf("!")+1,nodeLable.indexOf("|"));
								String dataOpr2= dataOpr1.replace(":", "");
								Activity="ca:"+activityName;
								
								DataOperation="cd:"+dataOpr2.substring(1,dataOpr2.indexOf("*")).replace(",", "-");
								ExpectedRole="r:"+SODModelMap.get(Integer.parseInt(activityOrigTrabsitionID));
								ExecutedRole="r_e:"+roleListOnOrgModel.get(0);
								User="u:"+actor;
								Deviation="dev:"+"TSMP";
							}
							else if (type.toString().equalsIgnoreCase("PLM")) {
								//String eventID=nodeLable.substring(nodeLable.indexOf("(")+1, nodeLable.indexOf(")"));
								String activityName=nodeLable.substring(nodeLable.indexOf(":")+1,nodeLable.indexOf("+"));
								String lifeSycle=nodeLable.substring(nodeLable.indexOf("+")+1, nodeLable.indexOf("|"));	
								String actor= nodeLable.substring(nodeLable.indexOf("@")+1, nodeLable.length());
								
								//get the role of user from orgmodel
								ArrayList<String> roleListOnOrgModel=new ArrayList<String>();
								roleListOnOrgModel.addAll(MLAWithContext.getRolefromOrganisationalModelMap(actor));

								//***********************
								Activity="PLa:"+activityName+"_"+ lifeSycle.charAt(0);
								DataOperation="d:*NA";
								ExpectedRole="r:*NA";
								if(!roleListOnOrgModel.isEmpty()) {
								ExecutedRole="r_e:"+roleListOnOrgModel.get(0);
								}else {
									ExecutedRole="r_e:*NN";	
								}
								User="u:"+actor;
								Deviation="dev:"+"PL";
							}
							else if (type.toString().equalsIgnoreCase("DLM")) {
								
								String actor= nodeLable.substring(nodeLable.indexOf("@")+1, nodeLable.length());
								
								//get the role of user from orgmodel
								ArrayList<String> roleListOnOrgModel=new ArrayList<String>();
								roleListOnOrgModel.addAll(MLAWithContext.getRolefromOrganisationalModelMap(actor));
								
								String dataOpr1=nodeLable.substring(nodeLable.indexOf(")")+2,nodeLable.indexOf("@"));
								String dataOpr2= dataOpr1.replace(":", "");
								Activity="ca:*NA";
								
								DataOperation="DLd:"+dataOpr2.replace(",", "-").substring(0, dataOpr2.length());
								
								ExpectedRole="r:*NA";
								if(!roleListOnOrgModel.isEmpty()) {
								ExecutedRole="r_e:"+roleListOnOrgModel.get(0);
								}else {
									ExecutedRole="r_e:*NN";	
								}
								User="u:"+actor;
								Deviation="dev:"+"DL";
							}
							
							//traceResult= "\""+i+","+Activity+","+DataOperation+","+ExpectedRole+","+ExecutedRole+","+User+","+Deviation+"\"";
							if(!(type.toString().equalsIgnoreCase("PSM") || type.toString().equalsIgnoreCase("TSM") || type.toString().equalsIgnoreCase("TM") || dO_NoDev) ) {
								k++;
							traceResult= "\""+Activity+","+DataOperation+","+ExpectedRole+","+ExecutedRole+","+User+","+Deviation+"\"";
							//System.out.println(i+",-"+k+traceResult);
							traceResultMap.put(i+"-"+k,traceResult);
							cacesWithDeviation.put(i,i);
						
							}
							dO_NoDev= false;
							
						}
						
                        //End_Export results for rule mining
						/*
						int processlogMove = 0, dataLogMove=0, totallySyncMove = 0, totallySyncMoveWPenalty = 0, partiallySyncMove=0 ,partiallySyncMoveWPenalty=0, modelMove = 0, tauMove = 0;
						
						for (StepTypes step : result.getSuccesfulResult().getStepTypes()) {
							//Object node= result.getSuccesfulResult().getNodeInstance(step);
							//StepTypes type = res.getStepTypes().get(step);
							//Object node = res.getNodeInstance().get(step);
							
							if (step == StepTypes.PLM) {
								processlogMove++;
							}else if (step == StepTypes.DLM) {
								dataLogMove++;
							}  else if (step == StepTypes.TSM) {
								totallySyncMove++;
							} else if (step == StepTypes.TSMP) {
								totallySyncMoveWPenalty++;
							}else if (step == StepTypes.PSM) {
								partiallySyncMove++;
							}else if (step == StepTypes.PSMP) {
								partiallySyncMoveWPenalty++;
							} else if (step == StepTypes.MM) {
								modelMove++;
							}else if (step == StepTypes.TM) {
								tauMove++;
							}
						}
						
						traceResult= String.format("Trace %d,#ProcessLogM %d, #DataLogM %d,#TotallySync.M %d,#PartiallySync.M %d,#ModelM %d,#TauM %d,#totallySyncMoveWPenalty %d, #partiallySyncMoveWPenalty %d", i, processlogMove, dataLogMove , totallySyncMove, partiallySyncMove, modelMove, tauMove,totallySyncMoveWPenalty, partiallySyncMoveWPenalty);
						traceResultMap.put(i,traceResult);
						*/
						
					} else if ((exitCode & Utils.FAILEDALIGNMENT) == Utils.FAILEDALIGNMENT) {
						// failure in the alignment. Error code shows more details.
						failed++;
					}
					if ((exitCode & Utils.ENABLINGBLOCKEDBYOUTPUT) == Utils.ENABLINGBLOCKEDBYOUTPUT) {
						// in some marking, there were too many tokens in a place, blocking the addition of more tokens. Current upper limit is 128
					}
					if ((exitCode & Utils.COSTFUNCTIONOVERFLOW) == Utils.COSTFUNCTIONOVERFLOW) {
						// in some marking, the cost function went through the upper limit of 2^24
					}
					if ((exitCode & Utils.HEURISTICFUNCTIONOVERFLOW) == Utils.HEURISTICFUNCTIONOVERFLOW) {
						// in some marking, the heuristic function went through the upper limit of 2^24
					}
					if ((exitCode & Utils.TIMEOUTREACHED) == Utils.TIMEOUTREACHED
							|| (exitCode & Utils.SOLVERTIMEOUTREACHED) == Utils.SOLVERTIMEOUTREACHED) {
						// alignment failed with a timeout (caused in the solver if SOLVERTIMEOUTREACHED is set)
					}
					if ((exitCode & Utils.STATELIMITREACHED) == Utils.STATELIMITREACHED) {
						// alignment failed due to reacing too many states.
					}
					if ((exitCode & Utils.COSTLIMITREACHED) == Utils.COSTLIMITREACHED) {
						// no optimal alignment found with cost less or equal to the given limit.
					}
					if ((exitCode & Utils.CANCELLED) == Utils.CANCELLED) {
						// user-cancelled.
					}
					if ((exitCode & Utils.FINALMARKINGUNREACHABLE) == Utils.FINALMARKINGUNREACHABLE) {
						// user-cancelled.
						System.err.println("final marking unreachable.");
					}

					break;
			}
		}

		System.out.println("**********"+cacesWithDeviation.size());
		//Write TxnCount Report
        String reportNamePath="D:\\OrganizedFolder\\phd\\2021 weekly meetings\\Implementation V3\\output files"   ;
        String reportTitle=""+log.size()+"";
        try {
			Write2File ReportFiles = new Write2File(traceResultMap,"getRecord",reportNamePath,true,reportTitle,false);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  
		System.out.println("Successful: " + success);
		System.out.println("Failed:     " + failed);

	}

	public static TransEvClassMapping constructMappingProcessLayerBasedOnLabelEquality(PetrinetGraph net, XLog log,
			XEventClass dummyEvClass, XEventClassifier eventClassifier) {
		
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		
		for (Transition t : net.getTransitions()) {
			boolean mapped = false;
			for (XEventClass evClass : summary.getEventClasses().getClasses()) {

				                
                int lableOfTransition = t.getLabel().indexOf(":");
                if(lableOfTransition != -1)
                	
				if (t.getLabel().substring(0, lableOfTransition).equals(evClass.getId())) {
					mapping.put(t, evClass);
					mapped = true;

				}
			}

			if (!mapped && !t.isInvisible()) {
				mapping.put(t, dummyEvClass);
			}

		}

		return mapping;
	}
	
	public static TransEvClassMapping constructMappingBasedOnLabelEquality(PetrinetGraph net, XLog log,
			XEventClass dummyEvClass, XEventClassifier eventClassifier,Map<XEventClass, Integer> processLogConvertedEvents ) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);



		for (Transition t : net.getTransitions()) {
			boolean mapped = false;
			String transitionLabeleOnModel = t.getLabel().substring(t.getLabel().indexOf(":")+1 ,t.getLabel().length() );
	 
    	   for (Map.Entry<XEventClass, Integer> entry : processLogConvertedEvents.entrySet()) {
               XEventClass k = entry.getKey();               
               String id = k.getId();
               
   			if (transitionLabeleOnModel.equals(id)) {
   				mapping.put(t, k);
   				mapped = true;
   			
   				break;
   				
   			}

               
               
           }


			if (!mapped && !t.isInvisible()) {
				mapping.put(t, dummyEvClass);
			
				
			}

		}

		return mapping;
	}

	
	public static List<String> convertActivitiesToOperations(String eventActivityName) {

		List<String> activityOperations = new ArrayList<String>();
		int activityOfEvent = eventActivityName.indexOf("+");
		String id =eventActivityName.substring(0, activityOfEvent);
		
		
	    dataModelMap.forEach((key, value) -> {
	    	int activityOfDataModelIndex = value.indexOf("|");
	    	String OperationOfActivity="";
	        if (value.substring(0,activityOfDataModelIndex).equals(id)) {

	            OperationOfActivity=eventActivityName+":"+value.substring(activityOfDataModelIndex+1, value.indexOf("*"));
	            activityOperations.add(OperationOfActivity);
	        }
	    });
		return activityOperations;
	}
	
	
	
	public static List<String> convertActivitiesToOperationsBytransition(String ActivityName) {

		List<String> activityOperations = new ArrayList<String>();
			
	    dataModelMap.forEach((key, value) -> {
	    	int activityOfDataModelIndex = value.indexOf("|");
	    	String OperationOfActivity="";
	        if (value.substring(0,activityOfDataModelIndex).equals(ActivityName)) {
	     
	            OperationOfActivity=ActivityName+":"+value.substring(activityOfDataModelIndex+1, value.length());// e.g. activity:DO_M/O*Frequency(true/False) --> A:Read(x)_M*true
	            activityOperations.add(OperationOfActivity);
	        }
	    });
		return activityOperations;
	}
	
	
	public static TransEvClassMapping constructMappingDataLayerBasedOnLabelEquality(PetrinetGraph net, XLog log,
			XEventClass dummyEvClass, XEventClassifier eventClassifier) {
		
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);

		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		
		for (Transition t : net.getTransitions()) {
			boolean mapped = false;
			String transitionLabelOnModel="";
			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				
				String oprationId="";
				String fieldsId="";
				if(!t.isInvisible()) {
				int dataEvIndex = evClass.getId().indexOf(":");
				
				oprationId = evClass.getId().substring(0, dataEvIndex);
				fieldsId = evClass.getId().substring(dataEvIndex+1,evClass.getId().length());
				
				if(t.getLabel().substring(t.getLabel().indexOf("!")+1, t.getLabel().indexOf(":")).equalsIgnoreCase("d")) {
					transitionLabelOnModel= t.getLabel().substring(t.getLabel().indexOf(":")+1, t.getLabel().indexOf("$"));
					}
				
                if (transitionLabelOnModel.equals(oprationId+fieldsId)) {
					
					//XEventClassifier newTransitionClass = new XEventNameClassifier();
					//newTransitionClass.setName(evClass.getId() +t.getLabel().substring(lableOfTransition,t.getLabel().length()));
					
				
					mapping.put(t, evClass);
					mapped = true;
				
				  }
				}
				
                //int lableOfTransition = t.getLabel().indexOf(":")+1;
                
                //if(lableOfTransition != -1)
                
				
			}

			if (!mapped && !t.isInvisible()) {
				mapping.put(t, dummyEvClass);
			}

		}

		return mapping;
	}
	
	public static Petrinet constructNet(Petrinet netFile) {
		Petrinet net = PetrinetFactory.newPetrinet("Converted Net");
		List<Transition> transitionList = new ArrayList<>(netFile.getTransitions());
		List<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> netFlows= new ArrayList<>(netFile.getEdges());
	
		/*
		int r=0;
		for (Transition t : transitionList) {
			SODModelMap.put(r, t.getLabel());	
			r++;
		}		
*/
		// places
		
		
		
		Map<Place, Place> p2p = new HashMap<Place, Place>();
		List<Place> placesList = new ArrayList<>(netFile.getPlaces());
		
		
		for (Place p : placesList) {
			Place pp = net.addPlace(p.toString());
			p2p.put(p, pp);
	
		}

		// transitions
		Map<Transition, Transition> t2tStart = new HashMap<Transition, Transition>();
		Map<Transition, Transition> t2tComplete = new HashMap<Transition, Transition>();
		int j=0;
		
		int transitionID=0;
		for (Transition t : transitionList) {
			
			
			SODModelMap.put(transitionID, t.getLabel().substring(t.getLabel().indexOf(":")+1,t.getLabel().length() ));
			
			String dataOpName="";
			String completeName="";
			String activityName="";
			
			Transition ttActivityStart=null;
			Transition ttActivityComplete=null;
			Transition ttOPTau=null;
			Transition tt1=null;
			Transition tt2 = null;

			if (t.isInvisible() || t.getLabel().startsWith("tau")) {
				tt1 = net.addTransition(t.getLabel());
				tt1.setInvisible(true);
				t2tStart.put(t, tt1);
				t2tComplete.put(t, tt1);
			}
			else
			{
				activityName=t.getLabel().substring(0,t.getLabel().indexOf(":"));
				List<String> ActivityOperations=convertActivitiesToOperationsBytransition(activityName);//e.g. A:Read(x)*M, A:Collect(y):O
				//Structure1:An activity with several data operations
				if (ActivityOperations.size()>=1) {
				ttActivityStart=net.addTransition(""+transitionID+"!"+"a:"+activityName+"+START");
				
				ttActivityComplete=net.addTransition(""+transitionID+"!"+"a:"+activityName+"+COMPLETE");
			
				
				if(!ActivityOperations.isEmpty()) {
			
					for (String Op :ActivityOperations) {
	               		 
	               		String MOFlag= Op.substring(Op.indexOf("$")+1, Op.indexOf("*"));
	               		String opFrequency=  Op.substring(Op.indexOf("*")+1, Op.length()); 
	               		//structure for Mandatory D.O with no frequency
	               		if(MOFlag.equalsIgnoreCase("M") && opFrequency.equalsIgnoreCase("false")){ 
	               		Place pp1 = net.addPlace("P"+j);
	               		dataOpName= ""+transitionID+"!"+"d:"+Op.substring(Op.indexOf(":")+1, Op.length()) +"|"+activityName;
	               		tt1 = net.addTransition(dataOpName);
	               		Place pp2 = net.addPlace("P"+(j+1));
	               		
	               		net.addArc(ttActivityStart, pp1);
	               		net.addArc(pp1, tt1);
	               		net.addArc(tt1, pp2);
	               		net.addArc(pp2, ttActivityComplete);
	               		j+=2;
	               	 }
	               	    //structure for Mandatory D.O with frequency
	               		if(MOFlag.equalsIgnoreCase("M") && opFrequency.equalsIgnoreCase("true")){ 
	                   		Place pp1 = net.addPlace("P"+j);
	                   		dataOpName= ""+transitionID+"!"+"d:"+Op.substring(Op.indexOf(":")+1, Op.length()) +"|"+activityName;
	                   		tt1 = net.addTransition(dataOpName);
	                   		Place pp2 = net.addPlace("P"+(j+1));
	                   		
	                   		net.addArc(ttActivityStart, pp1);
	                   		net.addArc(pp1, tt1);
	                   		net.addArc(tt1, pp2);
	                   		net.addArc(pp2, ttActivityComplete);
	                   		
	                   		ttOPTau=net.addTransition("tau");
	                   		ttOPTau.setInvisible(true);
	                   		net.addArc(pp2, ttOPTau);
	                   		net.addArc(ttOPTau, pp1);
	                   		
	                   		j+=2;
	                   	 }
	               	   
               	    //structure for Optional D.O with no frequency
               		if(MOFlag.equalsIgnoreCase("O") && opFrequency.equalsIgnoreCase("false")){ 
                   		Place pp1 = net.addPlace("P"+j);
                   		dataOpName= ""+transitionID+"!"+"d:"+Op.substring(Op.indexOf(":")+1, Op.length()) +"|"+activityName;
                   		tt1 = net.addTransition(dataOpName);
                   		Place pp2 = net.addPlace("P"+(j+1));
                   		
                   		net.addArc(ttActivityStart, pp1);
                   		net.addArc(pp1, tt1);
                   		net.addArc(tt1, pp2);
                   		net.addArc(pp2, ttActivityComplete);
                   		
                   		
                   		j+=2;
                   	 }
               		////structure for Optional D.O with frequency ????????????????????
               		if(MOFlag.equalsIgnoreCase("O") && opFrequency.equalsIgnoreCase("true")){ 
               			Place pp1 = net.addPlace("P"+j);
                   		dataOpName= ""+transitionID+"!"+"d:"+Op.substring(Op.indexOf(":")+1, Op.length()) +"|"+activityName;
                   		tt1 = net.addTransition(dataOpName);
                   		Place pp2 = net.addPlace("P"+(j+1));
                   		
                   		net.addArc(ttActivityStart, pp1);
                   		net.addArc(pp1, tt1);
                   		net.addArc(tt1, pp2);
                   		net.addArc(pp2, ttActivityComplete);
                   		
                   		ttOPTau=net.addTransition("tau");
                   		ttOPTau.setInvisible(true);
                   		net.addArc(ttOPTau,pp2);
                   		net.addArc(pp1,ttOPTau);
                   		
                   		j+=2;
                   	 }
               		

               	 }
               	 
               	t2tStart.put(t, ttActivityStart);
    			t2tComplete.put(t, ttActivityComplete);
    		
               	
               }
               else{
            	   System.out.println("***********There is no operation in data model for the activity");
            	   
            	   }

				
			}
				
				//Structure3:An activity with no data operation	
				if (ActivityOperations.size()==0) {

					ttActivityStart=net.addTransition(""+transitionID+"!"+"a:"+activityName+"_START");
					Place pp1 = net.addPlace("P"+(j));
					ttActivityComplete=net.addTransition(""+transitionID+"!"+"a:"+activityName+"_COMPLETE");
	               		

	    			net.addArc(ttActivityStart, pp1);
	    			net.addArc(pp1, ttActivityComplete);
	    			
	               	 
	               	t2tStart.put(t, ttActivityStart);
	    			t2tComplete.put(t, ttActivityComplete);
	    			j+=1; 			
				}
							}
			transitionID++;
		}
	
		// flow 
		        System.out.println();
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> f : netFlows) {
			
					if (f.getSource() instanceof Place) {
						
						net.addArc(p2p.get(f.getSource()), t2tStart.get(f.getTarget()));
					} else {
						net.addArc(t2tComplete.get(f.getSource()), p2p.get(f.getTarget()));
					}
				}
		return net;
	}
	
	private static Marking getFinalMarking(PetrinetGraph net) {
		Marking finalMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}

		return finalMarking;
	}

	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initMarking = new Marking();

		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initMarking.add(p);
		}

		return initMarking;
	}
	
	public static ArrayList<String> getRolefromSODModelMap(String activity) {
		ArrayList<String> activity2Roles=new ArrayList<String>();
		SODModelMap.forEach((key, value) -> {
	    	int index=value.indexOf(":");
	    	if(index>0) {
			if(value.substring(0, value.indexOf(":")).equalsIgnoreCase(activity))
				activity2Roles.add(value.substring(value.indexOf(":")+1, value.length()));
   
	    	}
    });	
		return activity2Roles;
	}
	
	public static String getRolefromSODModelMapWithContext(int transitionIDOnModel) {
		String activity2Role= SODModelMap.get(transitionIDOnModel);
		return activity2Role;
	}
	
	public static ArrayList<String> getRolefromOrganisationalModelMap(String user) {
		ArrayList<String> user2Roles=new ArrayList<String>();
		
		orgModelMap.forEach((key, value) -> {
			if(key.equalsIgnoreCase(user))
				user2Roles.add(value);
       
    });	

		return user2Roles;
	}
	
	
}
