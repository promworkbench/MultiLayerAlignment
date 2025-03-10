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
import org.processmining.multilayeralignment.algorithms.syncproduct.MLAVisualMovesOnLog;
import org.processmining.multilayeralignment.models.mlreplay.Replayer;
//import nl.tue.alignment.ReplayerParameters;
import org.processmining.multilayeralignment.models.mlreplay.ReplayerParameters;
//import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.multilayeralignment.models.mlreplay.StepTypes;
//import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.multilayeralignment.models.mlreplay.SyncReplayResult;
import org.processmining.multilayeralignment.models.mlreplay.TraceReplayTask;
import org.processmining.multilayeralignment.models.mlreplay.Utils;
import org.processmining.multilayeralignment.plugins.replayer.MultiLayerReplayResult;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import lpsolve.LpSolve;





public class MultiLayerAlignment {

	
	public static Map<String,String> dataModelMap= new HashMap<String, String>();
	static Map<Integer,String> SODModelMap= new HashMap<Integer, String>();
	static Map<String,String> orgModelMap= new HashMap<String, String>(); 
	static MultiLayerReplayResult list = new MultiLayerReplayResult();	
	
	
	public static MultiLayerReplayResult perform(PluginContext context, CSVFile DataModelCSVFile, CSVFile OrganisationalModelCSVFile, Petrinet inNet, XLog inProcessLog , XLog inDataLog)throws Exception{
	
		

		LpSolve.lpSolveVersion();
		
		//*Read Data Model
				String row;
				
				CSVFile csvFile = DataModelCSVFile;
				
				/*
					BufferedReader csvReader = new BufferedReader(new InputStreamReader(DataModelCSVFile.getInputStream()));
					int i=1;
					while ((row = csvReader.readLine()) != null) {
						
						DataModelParser dmEntry = new DataModelParser(row);
						if(!(dmEntry.getActivityName().isEmpty() || dmEntry.getDataOperation().isEmpty()) ) {
						dataModelMap.put("d"+i+"!"+dmEntry.dOType ,dmEntry.getActivityName()+"|"+dmEntry.getDataOperation().replace("-", ","));
						i++;}
						
					}
					csvReader.close();
					
				*/
				/*
				BufferedReader csvReader = new BufferedReader(new InputStreamReader(DataModelCSVFile.getInputStream()));
				int i=1;
				while ((row = csvReader.readLine()) != null) {
					System.out.println("test3 +++++++++++");
					DataModelParser dmEntry = new DataModelParser(row);
					if(!(dmEntry.getActivityName().isEmpty() || dmEntry.getDataOperation().isEmpty()) ) {
						
					dataModelMap.put("d"+i+"!"+dmEntry.dOType ,dmEntry.getActivityName()+"|"+dmEntry.getDataOperation().replace("-", ","));
					i++;}
					
					System.out.println("test4 +++++++++++");
				}
				csvReader.close();
				*/
				
				//CSVFile csvFile = DataModelCSVFile;

				BufferedReader csvReader = new BufferedReader(new InputStreamReader(DataModelCSVFile.getInputStream()));
				int i=1;
				while ((row = csvReader.readLine()) != null) {
					
					DataModelParser dmEntry = new DataModelParser(row);
					if(!(dmEntry.getActivityName().isEmpty() || dmEntry.getDataOperation().isEmpty()) ) {
					dataModelMap.put("d"+i ,dmEntry.getActivityName()+"|"+dmEntry.getDataOperation().replace("-", ","));
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
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		
		//read standard format of process log
		XEventClassifier eventClassifier2;
		eventClassifier2 = new XEventNameClassifier();
		eventClassifier2 = XLogInfoImpl.STANDARD_CLASSIFIER;
		XLogInfo summary2 = XLogInfoFactory.createLogInfo(log, eventClassifier2);
		XEventClasses processEvClasses = summary2.getEventClasses();

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
		Map<Transition, Integer> costPartialSyncMove = new HashMap<>();
		Map<XEventClass, Integer> costLogMove = new HashMap<>();
		Map<Transition, Integer> costSyncMove = new HashMap<>();
		Map<XEventClass, Integer> costDataLogMove = new HashMap<>();
		
		//*creating the costs of process/data model moves
				for (Transition t : net.getTransitions()) {
					costSyncMove.put(t, 0);
					costPartialSyncMove.put(t, t.isInvisible() ? 0 : 2);
					costModelMove.put(t, t.isInvisible() ? 0 : 4);
					//System.out.println("*******t is: " + t.getLabel().toString());
				}
				
		//*creating the cost of process log moves

				
		int k=0;
		 for (XEventClass c : summary2.getEventClasses().getClasses()) {
                List<String> ActivityOperations=convertActivitiesToOperations(c.getId());
                
                if(!ActivityOperations.isEmpty()) {
                	 for (String Op :ActivityOperations) {
                		 XEventClass convertedEvClass= new XEventClass(Op, k);
                		 costLogMove.put(convertedEvClass, 4);
                		 k++;
                	 }
                	
                }
                else{
                	XEventClass convertedEvClass= new XEventClass(c.getId(), k);
                	costLogMove.put(convertedEvClass, 4);}
			}

	    		for (XEventClass c : dataLogSummary.getEventClasses().getClasses()) {
	    			costDataLogMove.put(c, 4);

	    		}

	    		

		//*create a partially sync moves( a map between transitions in model and event classes in log)
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		TransEvClassMapping mapping1 = constructMappingBasedOnLabelEquality(net, log, dummyEvClass, eventClassifier2,costLogMove);
		TransEvClassMapping mapping2= constructMappingDataLayerBasedOnLabelEquality(net, dataLog, dummyEvClass, dataEventClassifier);                   

		
		System.out.println(String.format("Process Log size: %d events, %d traces, %d classes", summary.getNumberOfEvents(),
				summary.getNumberOfTraces(), (summary.getEventClasses().size() + 1)));
		System.out.println(String.format("Data Log size: %d events, %d traces, %d classes", dataLogSummary.getNumberOfEvents(),
				dataLogSummary.getNumberOfTraces(), (dataLogSummary.getEventClasses().size() + 1)));
		System.out.println(String.format("Model size: %d transitions, %d places", net.getTransitions().size(),
				net.getPlaces().size()));	//doReplay(log, net, initialMarking, finalMarking, classes, mapping, costModelMove, costSyncMove, costLogMove);
		doReplay(log,dataLog, net, initialMarking, finalMarking, processEvClasses, dataEvClasses ,mapping1,mapping2 ,costModelMove, costPartialSyncMove, costLogMove, costSyncMove, costDataLogMove);
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
				

		Replayer replayer = new Replayer(parameters, net, initialMarking, finalMarking, classesP, classesD, costModelMove,costPartialSyncMove,
				costLogMove, costSyncMove,costDataLogMove, mapping1 ,mapping2,false);

		// preprocessing time to be added to the statistics if necessary
		long preProcessTimeNanoseconds = 0;

		int success = 0;
		int failed = 0;
		ExecutorService service = Executors.newFixedThreadPool(parameters.nThreads);

		@SuppressWarnings("unchecked")
		Future<TraceReplayTask>[] futures = new Future[log.size()];

		for (int i = 0; i < log.size(); i++) {
			XAttribute caseID = (log.get(i)).getAttributes().get("concept:name");
			
			
			
			XTrace dataTrace= null;
			for(int j=0 ;  j < dataLog.size(); j++) { 
			 if(dataLog.get(j).getAttributes().get("concept:name").equals(caseID)){
				 dataTrace=dataLog.get(j);
				 break;
			 }
			}

			// Setup the trace replay task
			boolean withContext=false;
			TraceReplayTask task = new TraceReplayTask(withContext,replayer, parameters, log.get(i), i,dataTrace, i, timeoutMilliseconds,
					parameters.maximumNumberOfStates, preProcessTimeNanoseconds);
			
			
			futures[i] = service.submit(task);
		}
		// initiate shutdown and wait for termination of all submitted tasks.
		service.shutdown();

		// obtain the results one by one.


		

		 Map<Integer,String> traceResultMap= new HashMap<Integer, String>();
		for (int i = 0; i < log.size(); i++) {

			TraceReplayTask result;
			
			try {

				result = futures[i].get();

			} catch (Exception e) {
				// execution on the service has terminated.
				throw new RuntimeException("Error while executing replayer in ExecutorService. Interrupted maybe?", e);
			}
			
			String traceResult="";
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
					SyncReplayResult replayResultForVOL;
					MLAVisualMovesOnLog MultiLayeralignmentResult=new MLAVisualMovesOnLog(replayResult);
					replayResultForVOL= MultiLayeralignmentResult.resultForVOL;
					list.add(replayResultForVOL);
					
					
					
					int exitCode = replayResult.getInfo().get(Replayer.TRACEEXITCODE).intValue();

						if ((Utils.OPTIMALALIGNMENT) == Utils.OPTIMALALIGNMENT) {
						// Optimal alignment found.
						success++;
                        //find original traceID

                        
						int processlogMove = 0, dataLogMove=0, totallySyncMove = 0, totallySyncMoveWPenalty = 0, partiallySyncMove=0 ,partiallySyncMoveWPenalty=0, modelMove = 0, tauMove = 0;
						for (StepTypes step : result.getSuccesfulResult().getStepTypes()) {
							
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

 
		//Write TxnCount Report
        String reportNamePath="D:\\OrganizedFolder\\phd\\weekly meetings\\Implementation\\Motivation Example\\output files"   ;
        String reportTitle="Trace,#ProcessLogM, #DataLogM,#TotallySync.M,#PartiallySync.M,#ModelM,#TauM,#totallySyncMoveWPenalty, #partiallySyncMoveWPenalty"+"\r\n" ;
        try {
			Write2File ReportFiles = new Write2File(traceResultMap,"getRecord",reportNamePath,true,reportTitle);
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
	 
    	   for (Map.Entry<XEventClass, Integer> entry : processLogConvertedEvents.entrySet()) {
               XEventClass k = entry.getKey();               
               String id = k.getId();
               
   			if (t.getLabel().equals(id)) {
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

	            OperationOfActivity=eventActivityName+":"+value.substring(activityOfDataModelIndex+1, value.length());
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
	     
	            OperationOfActivity=ActivityName+":"+value.substring(activityOfDataModelIndex+1, value.length());
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
			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				int dataEvIndex = evClass.getId().indexOf(":");
				String oprationId = evClass.getId().substring(0, dataEvIndex);
				String fieldsId = evClass.getId().substring(dataEvIndex+1,evClass.getId().length());  
				
                int lableOfTransition = t.getLabel().indexOf(":")+1;
                if(lableOfTransition != -1)
                	
				if (t.getLabel().substring(lableOfTransition,t.getLabel().length()).equals(oprationId+fieldsId)) {
					
					XEventClassifier newTransitionClass = new XEventNameClassifier();
					newTransitionClass.setName(evClass.getId() +t.getLabel().substring(lableOfTransition,t.getLabel().length()));
					
				
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
	public static Petrinet constructNet(Petrinet netFile) {
		Petrinet net = PetrinetFactory.newPetrinet("Converted Net");
		List<Transition> transitionList = new ArrayList<>(netFile.getTransitions());
		List<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> netFlows= new ArrayList<>(netFile.getEdges());
	
		int r=0;
		for (Transition t : transitionList) {
			SODModelMap.put(r, t.getLabel());	
			r++;
		}		

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
		
		for (Transition t : transitionList) {
			
			
			String startName="";
			String completeName="";
			String activityName="";
			
			Transition ttTauStart=null;
			Transition ttTauComplete=null;
			Transition tt1=null;
			Transition tt2 = null;

			if (t.isInvisible() || t.getLabel().startsWith("tau_")) {
				tt1 = net.addTransition(t.getLabel());
				tt1.setInvisible(true);
				t2tStart.put(t, tt1);
				t2tComplete.put(t, tt1);
			}
			else
			{
				System.out.println(t.getLabel().toString());
				activityName=t.getLabel().substring(0,t.getLabel().indexOf(":"));
				List<String> ActivityOperations=convertActivitiesToOperationsBytransition(activityName);
				//Structure1:An activity with several data operations
				if (ActivityOperations.size()>1) {
				ttTauStart=net.addTransition("tau");
				ttTauStart.setInvisible(true);
				ttTauComplete=net.addTransition("tau");
				ttTauComplete.setInvisible(true);
				
				if(!ActivityOperations.isEmpty()) {
			
               	 for (String Op :ActivityOperations) {
               		Place pp1 = net.addPlace("P"+j);
               		startName= activityName+"+START"+":"+Op.substring(Op.indexOf(":")+1, Op.length());
               		tt1 = net.addTransition(startName);
               		Place pp2 = net.addPlace("P"+(j+1));
    				completeName=activityName+"+COMPLETE"+":"+Op.substring(Op.indexOf(":")+1, Op.length());
    				tt2 = net.addTransition(completeName);
    				Place pp3 = net.addPlace("P"+(j+2));
    				
    				net.addArc(ttTauStart, pp1);
    				net.addArc(pp1, tt1);
    				net.addArc(tt1, pp2);
    				net.addArc(pp2, tt2);
    				net.addArc(tt2, pp3);
    				net.addArc(pp3, ttTauComplete);
    				
    				
               	
               	 }
               	 
               	t2tStart.put(t, ttTauStart);
    			t2tComplete.put(t, ttTauComplete);
    			j+=3; 
               	
               }
               else{
            	   System.out.println("***********There is no operation in data model for the activity");
            	   
            	   }

				
			}
			//Structure2:An activity with one data operation	
				if (ActivityOperations.size()==1) {
					
					
					if(!ActivityOperations.isEmpty()) {
				
	               	 for (String Op :ActivityOperations) {
	          
	               		startName= activityName+"+START"+":"+Op.substring(Op.indexOf(":")+1, Op.length());
	               		tt1 = net.addTransition(startName);
	               		Place pp2 = net.addPlace("P"+(j));
	    				completeName=activityName+"+COMPLETE"+":"+Op.substring(Op.indexOf(":")+1, Op.length());
	    				tt2 = net.addTransition(completeName);

	    				net.addArc(tt1, pp2);
	    				net.addArc(pp2, tt2);

	               	 }
	               	 
	               	t2tStart.put(t, tt1);
	    			t2tComplete.put(t, tt2);
	    			j+=2; 
	               	
	               }
	               else{
	            	   System.out.println("***********There is no operation in data model for the activity");
	            	   }
					
				}
				
				//Structure3:An activity with no data operation	
				if (ActivityOperations.size()==0) {
					
					
					
	               		startName= activityName+"+START";
	               		tt1 = net.addTransition(startName);
	               		Place pp2 = net.addPlace("P"+(j));
	    				completeName=activityName+"+COMPLETE";
	    				tt2 = net.addTransition(completeName);

	    				net.addArc(tt1, pp2);
	    				net.addArc(pp2, tt2);
	    			
	               	 
	               	t2tStart.put(t, tt1);
	    			t2tComplete.put(t, tt2);
	    			j+=2; 			
				}
							}
			
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
	public static ArrayList<String> getRolefromOrganisationalModelMap(String user) {
		ArrayList<String> user2Roles=new ArrayList<String>();
		
		orgModelMap.forEach((key, value) -> {
			if(key.equalsIgnoreCase(user))
				user2Roles.add(value);
       
    });	

		return user2Roles;
	}
	
	
}
