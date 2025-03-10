package org.processmining.multilayeralignment.algorithms.syncproduct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.multilayeralignment.models.mlreplay.Replayer;
import org.processmining.multilayeralignment.models.mlreplay.StepTypes;
import org.processmining.multilayeralignment.models.mlreplay.SyncProduct;
import org.processmining.multilayeralignment.models.mlreplay.SyncProductFactory;
import org.processmining.multilayeralignment.models.mlreplay.SyncProductImpl;
import org.processmining.multilayeralignment.models.mlreplay.SyncReplayResult;
import org.processmining.multilayeralignment.models.mlreplay.Utils;
import org.processmining.multilayeralignment.plugins.MultiLayerAlignment;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.TByteList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import nl.tue.alignment.Utils.Statistic;
import nl.tue.alignment.algorithms.syncproduct.GenericMap2Int;
import nl.tue.alignment.algorithms.syncproduct.ObjectList;
import nl.tue.astar.Trace;
import nl.tue.astar.util.LinearTrace;

public class MlBasicSyncProductFactory implements SyncProductFactory<Transition> {

	private final int transitions;

	// transition to model moves
	private final TIntList t2mmCost;
	
	// eventClassSequence 2 partially sync move cost
	private final TIntList t2pmCost;

	// eventClassSequence 2 totally sync move cost
	private final TIntList t2smCost;

	
		
	// maps transitions to IDs
	//	private final TObjectIntMap<Transition> t2id;
	private final ObjectList<String> t2name;
	private final ObjectList<int[]> t2input;
	private final ObjectList<int[]> t2output;
	private final ObjectList<int[]> t2eid;
	private final TByteList t2type;
	private final Transition[] t2transition;

	private final int pclassCount;
	private final int[] pc2lmCost;
	private final int dclassCount;
	private final int[] dc2dmCost;
	// maps classes to sets of transitions representing these classes
	private final TIntObjectMap<TIntSet> pc2t;
	private final TIntObjectMap<TIntSet> dc2t;
	
	//map to find the name of activity from artificial process event net
	private final Map<Integer, String> convertedArtificialTraceActivityMap=new HashMap<>(); 
	private final Map<Integer, String> convertedArtificialOrigMap=new HashMap<>(); 
	private final Map<String, String> activitySCMatchMap=new HashMap<>();
	
	private final int places;
	private int placesInMiddle;
	private int placesInMiddle2;
	private final ObjectList<String> p2name;
	private final byte[] initMarking;
	private final byte[] finMarking;
	private final XEventClasses classesP;
	private final XEventClasses classesD;
	private final TObjectIntMap<XEventClass> processEvClass2id;
	private final TObjectIntMap<XEventClass> dataEvClass2id;
	
	private final Map<XEventClass, Integer> convertedEventstoOperations;
	private final Map<String,String> totalSyncMoves= new HashMap<String, String>();

	

	public MlBasicSyncProductFactory(Petrinet net, XEventClasses classesP, TObjectIntMap<XEventClass> pc2id,XEventClasses classesD, TObjectIntMap<XEventClass> dc2id,
			TransEvClassMapping map1,TransEvClassMapping map2,Map<XEventClass, Integer> convertedEventstoOperations,
			Map<Transition, Integer> mapTrans2Cost,
			Map<Transition, Integer> mapPSync2Cost,
			Map<XEventClass, Integer> mapPEvClass2Cost,
			Map<Transition, Integer> mapTSync2Cost,
			Map<XEventClass, Integer> mapDEvClass2Cost,
			Marking initialMarking, Marking finalMarking) {
		this(net, classesP, pc2id,classesD, dc2id ,map1,map2, convertedEventstoOperations,
				new GenericMap2Int<>(mapTrans2Cost, 2),
				new GenericMap2Int<>(mapPSync2Cost, 1),
				new GenericMap2Int<>(mapPEvClass2Cost, 2),
				new GenericMap2Int<>(mapTSync2Cost, 0),
				new GenericMap2Int<>(mapDEvClass2Cost, 2),
				initialMarking, finalMarking);
	
	}

	private MlBasicSyncProductFactory(Petrinet net, XEventClasses classesP, TObjectIntMap<XEventClass> pc2id,XEventClasses classesD, TObjectIntMap<XEventClass> dc2id,
			TransEvClassMapping map1, TransEvClassMapping map2,Map<XEventClass, Integer> convertedEventstoOperations,
			GenericMap2Int<Transition> mapTrans2Cost,
			GenericMap2Int<Transition> mapPSync2Cost,
			GenericMap2Int<XEventClass> mapPEvClass2Cost,
			GenericMap2Int<Transition> mapTSync2Cost,
			GenericMap2Int<XEventClass> mapDEvClass2Cost,
			Marking initialMarking, Marking finalMarking) {

		this.processEvClass2id = pc2id;
		this.classesP = classesP;
		this.dataEvClass2id = dc2id;
		this.classesD = classesD;
		this.convertedEventstoOperations=convertedEventstoOperations; 
		
		

		// find the highest class number
		int mx = 0;
		TObjectIntIterator<XEventClass> its = pc2id.iterator();
		while (its.hasNext()) {
			its.advance();
			if (mx < its.value()) {
				mx = its.value();
			}
		}
		mx++;

		this.pclassCount = mx;
		pc2lmCost = new int[pclassCount];
		pc2t = new TIntObjectHashMap<>(this.pclassCount);
		
		for (Map.Entry<XEventClass, Integer> entry : convertedEventstoOperations.entrySet()) {
            XEventClass k = entry.getKey();
            pc2lmCost[pc2id.get(k)] = mapPEvClass2Cost.get(k);   
        }

		int mx2 = 0;
		TObjectIntIterator<XEventClass> its2 = dc2id.iterator();
		while (its2.hasNext()) {
			its2.advance();
			if (mx2 < its2.value()) {
				mx2 = its2.value();
			}
		}
		mx2++;

		this.dclassCount = mx2;
		dc2dmCost = new int[dclassCount];
		dc2t = new TIntObjectHashMap<>(this.dclassCount);
		for (XEventClass clazz : classesD.getClasses()) {
			dc2dmCost[dc2id.get(clazz)] = mapDEvClass2Cost.get(clazz);
		}

		transitions = net.getTransitions().size();
		t2mmCost = new TIntArrayList(transitions * 2);
		t2smCost = new TIntArrayList(transitions * 2);
		t2pmCost = new TIntArrayList(transitions * 2);
		
		t2eid = new ObjectList<>(transitions * 2);
		t2type = new TByteArrayList(transitions * 2);
		t2name = new ObjectList<>(transitions * 2);
		t2input = new ObjectList<>(transitions * 2);
		t2output = new ObjectList<>(transitions * 2);
		t2transition = new Transition[transitions];

		places = net.getPlaces().size();
		p2name = new ObjectList<>(places * 2);
		TObjectIntMap<Place> p2id = new TObjectIntHashMap<>(net.getPlaces().size(), 0.75f, -1);


		// build list of move_model transitions of sync. product 
		Integer cost;
		Iterator<Transition> it = net.getTransitions().iterator();
		int x=0;
		while (it.hasNext()) {
			
			Transition t = it.next();
			t2transition[t2name.size()] = t;

			// update mapping from event class to transitions from partial map
			XEventClass clazzP = map1.get(t);
			if (clazzP != null) {
				TIntSet set = pc2t.get(pc2id.get(clazzP));
				if (set == null) {
					set = new TIntHashSet(3);
					pc2t.put(pc2id.get(clazzP), set);
				}
				set.add(t2name.size());
			}
			//update mapping from event class to transitions from total map
			XEventClass clazzD = map2.get(t);
			if (clazzD != null) {
				TIntSet set = dc2t.get(dc2id.get(clazzD));
				if (set == null) {
					set = new TIntHashSet(3);
  					dc2t.put(dc2id.get(clazzD), set);
				}
				set.add(t2name.size());
			}
			
			cost = mapTrans2Cost.get(t);
			t2mmCost.add(cost);
 			cost = mapTSync2Cost.get(t);
			t2smCost.add(cost);
			cost = mapPSync2Cost.get(t);
			t2pmCost.add(cost);
			
			t2name.add(t.getLabel()); 
			t2eid.add(SyncProduct.NOEVENT);
			t2type.add(t.isInvisible() ? SyncProduct.TAU_MOVE : SyncProduct.MODEL_MOVE);

			TIntList input = new TIntArrayList(2 * net.getInEdges(t).size());
			for (PetrinetEdge<?, ?> e : net.getInEdges(t)) {
				Place p = (Place) e.getSource();
				int id = p2id.get(p);
				if (id == -1) {
					id = p2id.size();
					p2id.put(p, id);
					p2name.add(p.getLabel());
				}
				for (int w = 0; w < ((Arc) e).getWeight(); w++) {
					input.add(id);
				}
			}
			t2input.add(input.toArray());

			TIntList output = new TIntArrayList(2 * net.getOutEdges(t).size());
			for (PetrinetEdge<?, ?> e : net.getOutEdges(t)) {
				Place p = (Place) e.getTarget();
				int id = p2id.get(p);
				if (id == -1) {
					id = p2id.size();
					p2id.put(p, id);
					p2name.add(p.getLabel());
				}
				for (int w = 0; w < ((Arc) e).getWeight(); w++) {
					output.add(id);
				}
			}
			t2output.add(output.toArray());
			x++;
		}

		// mark the initial places
		initMarking = new byte[p2name.size()];
		for (Place p : initialMarking) {
			int id = p2id.get(p);
			if (id >= 0) {
				initMarking[id]++;
			}
		}

		// indicate the desired final marking
		finMarking = new byte[p2name.size()];
		for (Place p : finalMarking) {
			int id = p2id.get(p);
			if (id >= 0) {
				finMarking[id]++;
			}
		}

	}

	public Trace getProcessTrace(XTrace xTrace, boolean partiallyOrderSameTimestamp) {
		String traceLabel = XConceptExtension.instance().extractName(xTrace);
		if (traceLabel == null) {
			traceLabel = "XTrace@" + Integer.toHexString(xTrace.hashCode());
		}

		if (partiallyOrderSameTimestamp) {		
			return getLinearProcessTrace(xTrace, traceLabel);
		} else {
		
			return getLinearProcessTrace(xTrace, traceLabel);
		}
	}
	
	public Trace getDataTrace(XTrace xTrace, boolean partiallyOrderSameTimestamp) {
		String traceLabel = XConceptExtension.instance().extractName(xTrace);
		if (traceLabel == null) {
			traceLabel = "XTrace@" + Integer.toHexString(xTrace.hashCode());
		}

		if (partiallyOrderSameTimestamp) {

			return getLinearDataTrace(xTrace, traceLabel);
		} else {
			return getLinearDataTrace(xTrace, traceLabel);
		}
	}
	

	public synchronized SyncProduct getSyncProduct(XTrace pxTrace,XTrace dxTrace, ArrayList<? super Transition> transitionList,
			boolean partiallyOrderSameTimestamp) {
		
		String pTraceLabel = XConceptExtension.instance().extractName(pxTrace);
		if (pTraceLabel == null) {
			pTraceLabel = "XTrace@" + Integer.toHexString(pxTrace.hashCode());
		}
		
		String dTraceLabel = XConceptExtension.instance().extractName(dxTrace);
		if (dTraceLabel == null) {
			dTraceLabel = "XTrace@" + Integer.toHexString(dxTrace.hashCode());
		}

			SyncProduct result = getLinearSyncProduct(getLinearProcessTrace(pxTrace, pTraceLabel),getLinearDataTrace(dxTrace,dTraceLabel ), transitionList,pxTrace,dxTrace );
							
			return result;


	}

	private SyncProduct getLinearSyncProduct(LinearTrace processTrace,LinearTrace dataTrace ,List<? super Transition> transitionList, XTrace pxTrace,XTrace dxTrace ) {
		
		int penaltyCost=1;
		transitionList.clear();
		// for this trace, compute the log-moves
		// compute the sync moves
		TIntList ranks = new TIntArrayList();
		for (int t = 0; t < transitions; t++) {
			
			ranks.add(SyncProduct.NORANK);
			
		}
		
       //construct process event net
		Map<String,String> partialMoves= new HashMap<String, String>();
		for (int e = 0; e < processTrace.getSize(); e++) {

			int processLcid = processTrace.get(e); 
			String origEventIdAttr= getOriginalEventIdAttr(e, pxTrace);// find the id attr. of originalevent (phase2)
			// add a place
			p2name.add("pe_" + e);
			// add process log move
			
			t2name.add("e" + e + "(" + processLcid + ")"+"|"+ origEventIdAttr);//clazz.toString());
			t2mmCost.add(pc2lmCost[processLcid]);
			t2eid.add(new int[] { e });
			t2type.add(SyncProduct.LOG_MOVE);
			ranks.add(e);
			TIntSet set = pc2t.get(processLcid);
			if (set != null) {
				TIntIterator it = set.iterator();
				while (it.hasNext()) {
					// add partially partially sync move
					int t = it.next();
					
					t2name.add(t2name.get(t) + " + e" + e + "*(" + t + ")"+ "|"+ origEventIdAttr);
					//get partial moves to use for Sync moves
					partialMoves.put( ""+t+"|" + e + "", t2name.get(t) + " + e" + e + "*(" + t + ")"+ "|"+ origEventIdAttr);
					
					boolean firstCheckPrivacyLayer= privacyCheckForProcessLayer(e, pxTrace);
		
					//check for privacy layer
					if(firstCheckPrivacyLayer) {
					t2type.add(SyncProduct.PSYNC_MOVE);
					t2mmCost.add(t2pmCost.get(t));
					}else {
						t2type.add(SyncProduct.PSYNC_MOVE_T2);
						t2mmCost.add(t2pmCost.get(t)+penaltyCost);

						boolean secondCheckPrivacyLayer= privacyCheckForProcessLayer(e, pxTrace);

					}
					t2eid.add(new int[] { e });
					
					ranks.add(e);
					
					
				}
			}
		}
				
		if (processTrace.getSize() > 0) {
			p2name.add("pe_" + processTrace.getSize());
		}
			
		
		//construct data event net
		int dataEvPlaceId=0;
		for (int e = 0; e < dataTrace.getSize(); e++) {

			int dataLcid = dataTrace.get(e); // c2id.get(clazz);
			// add two places
			
			p2name.add("pd_" + dataEvPlaceId);
			p2name.add("pd_" + ++dataEvPlaceId);
			dataEvPlaceId++;
			
			// add data log move
			t2name.add("d" + e + "*(" + dataLcid + ")"+ "|"+ e);//there is no process event ID We use data event id
			t2mmCost.add(dc2dmCost[dataLcid]);
			t2eid.add(new int[] { e });
			t2type.add(SyncProduct.DLOG_MOVE);
			ranks.add(e);
			
			
			TIntSet set = dc2t.get(dataLcid);
			if (set != null) {
				TIntIterator it = set.iterator();
				while (it.hasNext()) {
					// add totally sync move
					int t = it.next();
					
					for (Map.Entry<String,String> entry : partialMoves.entrySet()) {
						
						int avtivityIndex= Integer.parseInt(entry.getKey().substring(0, entry.getKey().indexOf("|")));
						int processEventIndex= Integer.parseInt(entry.getKey().substring(entry.getKey().indexOf("|")+1 , entry.getKey().length())); 
						
						String origEventIdAttr2=entry.getValue().substring(entry.getValue().indexOf("|")+1 , entry.getValue().length()); // find the id attr of originalProcessEvent (phase2);						//String origEventIdAttr2="xxx";
						if(avtivityIndex==t) {
							
							
							//check for privacy layer
							boolean secondCheckPrivacyLayer= privacyCheckForDataLayer(e, dxTrace,t2name.get(t) );
							boolean TotallySycConditions = checkTotallySycConditions(e, dxTrace,t2name.get(t), processEventIndex, pxTrace  );// e is event index in data trace
							

							if(TotallySycConditions) {
							if(secondCheckPrivacyLayer) {
								t2name.add(t2name.get(t) + " + d" + e +"(" + processEventIndex + ")" +"(" + t + ")"+"|"+origEventIdAttr2);
								totalSyncMoves.put("d"+e+""+"*"+entry.getKey(),t2name.get(t) + " + d" + e +"(" + processEventIndex + ")" +"(" + t + ")"+"|"+origEventIdAttr2);
								t2mmCost.add(t2smCost.get(t));
								t2type.add(SyncProduct.SYNC_MOVE);
								t2eid.add(new int[] { e });
								ranks.add(e);
								t2eid.add(new int[] { e });
								ranks.add(e);
							}else{
								t2name.add(t2name.get(t) + " + d" + e +"(" + processEventIndex + ")" +"(" + t + ")"+"|"+origEventIdAttr2);
								totalSyncMoves.put("d"+e+""+"*"+entry.getKey(),t2name.get(t) + " + d" + e +"(" + processEventIndex + ")" +"(" + t + ")"+"|"+origEventIdAttr2);
								t2mmCost.add(t2smCost.get(t)+penaltyCost);
							    t2type.add(SyncProduct.SYNC_MOVE_T2);
							    t2eid.add(new int[] { e });
								ranks.add(e);
								t2eid.add(new int[] { e });
								ranks.add(e);
							    
							}
							
							}
				
							
						}//end of checkTotallySync.Conditions
						
					}
						
				}//end of while
			}
		}
		
		int[] pathLengths = new int[t2name.size()];
		Arrays.fill(pathLengths, 1);

		SyncProductImpl product = new SyncProductImpl(processTrace.getLabel(), //label
				this.pclassCount, // number of classes
				t2name.toArray(new String[t2name.size()]), //transition labels
				p2name.toArray(new String[p2name.size()]), // place labels
				t2eid.toArray(new int[t2eid.size()][]), //event numbers
				ranks.toArray(), // ranks
				pathLengths, t2type.toArray(), //types
				t2mmCost.toArray());

		int t = 0;
		for (; t < transitions; t++) {
			// first the model moves
			product.setInput(t, t2input.get(t));
			product.setOutput(t, t2output.get(t));
			transitionList.add(t2transition[t]);
		}
		//set initial and final marking related to process model net 
		product.setInitialMarking(Arrays.copyOf(initMarking, p2name.size()));
	    product.setFinalMarking(Arrays.copyOf(finMarking, p2name.size()));
	    //---------------------------------------------------------------------------
	    for (int e = 0; e < processTrace.getSize(); e++) {
			// then the process log moves

			int processLcid = processTrace.get(e);
			product.setInput(t, places + e);
			product.setOutput(t, places + e + 1);
			transitionList.add(null);
			t++;

			TIntSet set = pc2t.get(processLcid);
			 
			if (set != null) {
				TIntIterator itP = set.iterator();
				while (itP.hasNext()) {
					// add partially sync move
					int t2 = itP.next();
					//the inputs/outputs of partially sync moves that come from process model-t2input/t2output is on process model 

					
					product.setInput(t, t2input.get(t2));
					product.setOutput(t, t2output.get(t2));
                    //the the inputs/outputs of partially sync moves that come from process log 
					product.addToInput(t, (places + e));
					product.addToOutput(t, (places + e + 1));

					transitionList.add(t2transition[t2]);
					t++;
					placesInMiddle= places + e + 1;
				}
			}
		}
		//set initial and final marking related to process event net
		if (processTrace.getSize() > 0) {
			product.addToInitialMarking(places);
			product.addToFinalMarking(places + processTrace.getSize());
		}
		//-------------------------------------------------------------------------------------------
		//collect partial moves indexes
		Map<Integer,String> partialMovesIndexes= new HashMap<Integer, String>();
		int productnumTransitions=product.numTransitions();
		for (int i = 0; i < product.numTransitions(); i++) {
					
			if(product.getTransitionLabel(i).contains("+ e"))
			partialMovesIndexes.put(i, product.getTransitionLabel(i));
		}
				
		placesInMiddle= places + processTrace.getSize()+1;
		boolean checkConditions=false;
	
		for (int e = 0; e < dataTrace.getSize(); e++) {
			
			  			
			// then the data log moves
			int dataLcid = dataTrace.get(e);// c2id.get(clazz);
			String dataevent=product.getTransitionLabel(t).substring(0, product.getTransitionLabel(t).indexOf("*"));
			
			product.setInput(t, (placesInMiddle) );
			product.setOutput(t, (placesInMiddle+ 1));
			
			product.addToInitialMarking(placesInMiddle);
			product.addToFinalMarking((placesInMiddle+ 1));
					
			transitionList.add(null);
			t++;
			
			//find all related totalSyncMoves to current data event
			Map<String,String> allRelatedtotalMoves= new HashMap<String, String>();
			for (Map.Entry<String,String> entry : totalSyncMoves.entrySet()) {
				
				String dataEventIndexonMap= entry.getKey().substring(0, entry.getKey().indexOf("*"));
								
				if(dataEventIndexonMap.equalsIgnoreCase(dataevent)) {
					allRelatedtotalMoves.put(entry.getKey(), entry.getValue());
				}
				
			}
			//connect all totalSyncMoves to data event and rest of the model
			for(int i=0;i<allRelatedtotalMoves.size() ;i++) {
				
				for (Map.Entry<String,String> entry : allRelatedtotalMoves.entrySet()) {
					
					  String activityPevent= entry.getKey().substring(entry.getKey().indexOf("*")+1, entry.getKey().length());
					   int activitIndex= Integer.parseInt(activityPevent.substring(0, activityPevent.indexOf("|")));
					   			   
					   if(t<productnumTransitions) {
					   if(entry.getValue().equalsIgnoreCase(product.getTransitionLabel(t))) {
						  
						   //find index of same partial move
						   String partialMoveLable= partialMoves.get(activityPevent);
						   
						   int productTransitionIndex= -1;
						   for (Map.Entry<Integer,String> entry2 : partialMovesIndexes.entrySet()) {
							  if (entry2.getValue().equalsIgnoreCase(partialMoveLable)){
								  
								  productTransitionIndex=entry2.getKey();
								  //connect total sync move to the rest of model by finding the mapped partial move and using the Input/output of the  mapped partial move to connect new total move to the rest of model
								  product.setInput(t, product.getInput(productTransitionIndex));
								  product.setOutput(t, product.getOutput(productTransitionIndex));

								  
								  //connect total sync move to data event
								  if(product.getTransitionLabel(t).contains("START")) {
										
					                    //the the inputs/outputs of partially sync moves that come from process log 
										product.addToInput(t, (placesInMiddle));
									
										}
									if(product.getTransitionLabel(t).contains("COMPLETE")){
										
					                    //the the inputs/outputs of partially sync moves that come from process log 
										product.addToOutput(t, (placesInMiddle+1));
									}
									transitionList.add(t2transition[activitIndex]);
									t++;
									
									
							  }
							  
				
						   }
						   
						   
						   
						   
					   }
		        	   
				}
						
					}
			}
           
			
			

			placesInMiddle+=2;
			//--------------------------------------------------------------------------------------
		
			
		}
	
		
		// trim to size;
		p2name.truncate(places);
		t2name.truncate(transitions);
		t2mmCost.remove(transitions, t2mmCost.size() - transitions);
		t2eid.truncate(transitions);//, t2eid.size() - transitions);
		t2type.remove(transitions, t2type.size() - transitions);
	
		return product;
	}

	private LinearTrace getLinearProcessTrace_2(XTrace xTrace, String label) {
		LinearTrace pTrace = new LinearTrace(label, xTrace.size());
		for (int e = 0; e < xTrace.size(); e++) {
			
			XEventClass clazz = classesP.getClassOf(xTrace.get(e));
			
			
			pTrace.set(e, processEvClass2id.get(clazz));
		}

		return pTrace;

	}
	
	private LinearTrace getLinearProcessTrace(XTrace xTrace, String label) {

		
		Map<Integer, Integer> convertedPTrace = new HashMap<>();
		
		int k=0;
		for (int e = 0; e < xTrace.size(); e++) {

            
          
			
			//--------------------------------------------------------------
	            String activityName=xTrace.get(e).getAttributes().get("concept:name")+"+"+xTrace.get(e).getAttributes().get("lifecycle:transition");

	            List<String> convertActivity2Operations=MultiLayerAlignment.convertActivitiesToOperations(activityName);
	            
	            
	            if(!convertActivity2Operations.isEmpty()) {
               	 for (String Op :convertActivity2Operations) {
               		 XEventClass convertedEvClass= new XEventClass(Op, k);
               		convertedPTrace.put(k, processEvClass2id.get(convertedEvClass));
               		// "e" is the index of activity in original trace
               		//convertedArtificialTraceActivityMap.put(k,e+":"+activityName);
               		//convertedArtificialOrigMap.put(k ,e+"("+actId+")"+":"+activityName);// k is artificial event index, e is orig. event index, actId is the id of original event - orig. start and complete events have same id

               		 k++;
               	 }
               	
               }
               else{
               	XEventClass convertedEvClass= new XEventClass(activityName, k);
               	convertedPTrace.put(k, processEvClass2id.get(convertedEvClass));

               	//convertedArtificialOrigMap.put(k,e+"("+actId+")"+":"+activityName);//K is event index in artificial trace- e is event index in original trace- actId is original event ID
               	k++;
               }
	            
		}
		
		LinearTrace pTrace = new LinearTrace(label, convertedPTrace.size());
		for(int p=0;p<convertedPTrace.size();p++) {
			pTrace.set(p, convertedPTrace.get(p));	
		}
		return pTrace;
	}
	
	private LinearTrace getLinearDataTrace(XTrace xTrace, String label) {
		
		LinearTrace dTrace = new LinearTrace(label, xTrace.size());
		for (int e = 0; e < xTrace.size(); e++) {
			XEventClass clazz = classesD.getClassOf(xTrace.get(e));
			dTrace.set(e, dataEvClass2id.get(clazz));
		}

		return dTrace;

	}
	
	public void fillMaps(XTrace xTrace) {
		String traceLabel = XConceptExtension.instance().extractName(xTrace);
		if (traceLabel == null) {
			traceLabel = "XTrace@" + Integer.toHexString(xTrace.hashCode());
		}

		convertedArtificialTraceActivityMap.clear();
		convertedArtificialOrigMap.clear();
		activitySCMatchMap.clear();

		
		Map<Integer, Integer> convertedPTrace = new HashMap<>();
		
		int k=0;
		for (int e = 0; e < xTrace.size(); e++) {
			
			//create activity start/complete match
            String actName= xTrace.get(e).getAttributes().get("concept:name").toString();
            String actId=xTrace.get(e).getAttributes().get("id").toString();
            String actDatetime=xTrace.get(e).getAttributes().get("time:timestamp").toString();
            String actLifesycle=xTrace.get(e).getAttributes().get("lifecycle:transition").toString();
            
          
			
			//--------------------------------------------------------------
	            String activityName=xTrace.get(e).getAttributes().get("concept:name")+"+"+xTrace.get(e).getAttributes().get("lifecycle:transition");
	            List<String> convertActivity2Operations=MultiLayerAlignment.convertActivitiesToOperations(activityName);
	            
	            
	            if(!convertActivity2Operations.isEmpty()) {
               	 for (String Op :convertActivity2Operations) {

               		// "e" is the index of activity in original trace
               		convertedArtificialTraceActivityMap.put(k,e+":"+activityName);
               		convertedArtificialOrigMap.put(k ,e+"("+actId+")"+":"+activityName);// k is artificial event index, e is orig. event index, actId is the id of original event - orig. start and complete events have same id

               		 k++;
               	 }
               	
               }
               else{

               	convertedArtificialTraceActivityMap.put(k,e+":"+activityName);
               	convertedArtificialOrigMap.put(k,e+"("+actId+")"+":"+activityName);//K is event index in artificial trace- e is event index in original trace- actId is original event ID
               	k++;
               }
	            
	            
	            String val="";
	            if(actLifesycle.equalsIgnoreCase("start") ) {
	            	activitySCMatchMap.put(actId+"*"+actName, actDatetime);

	            }
	            if(actLifesycle.equalsIgnoreCase("complete") && activitySCMatchMap.containsKey(actId+"*"+actName) ) {
	            	val = activitySCMatchMap.get(actId+"*"+actName);
	            	activitySCMatchMap.put(actId+"*"+actName, val+"*"+actDatetime);
	 
	            }
	           
			    
		}
		
	}
	private boolean privacyCheckForProcessLayer(int e,XTrace origPTrace ) {
		boolean result=false;
		boolean checkAllowedUser=false;
		boolean checkAllowedRole=false;
		

		try {
		int evIndex = convertedArtificialTraceActivityMap.get(e).indexOf(":");
		int eventIndex = Integer.parseInt(convertedArtificialTraceActivityMap.get(e).substring(0, evIndex)) ;//original event Index
		
		
		String activity= origPTrace.get(eventIndex).getAttributes().get("concept:name").toString();
		String actor= origPTrace.get(eventIndex).getAttributes().get("org:resource").toString();

		
		//check with process model
		ArrayList<String> roleOnModel=new ArrayList<String>();
		roleOnModel.addAll(MultiLayerAlignment.getRolefromSODModelMap(activity));
		
 
		//check with Org Model
		ArrayList<String> roleListOnOrgModel=new ArrayList<String>();
		roleListOnOrgModel.addAll(MultiLayerAlignment.getRolefromOrganisationalModelMap(actor));
		
		if(!roleListOnOrgModel.isEmpty()) {
			checkAllowedUser=true;
		}
		
		
		for (String userRole : roleListOnOrgModel){
		 for (String activityRole : roleOnModel){
	         if (activityRole.equalsIgnoreCase(userRole)){
	               checkAllowedRole=true;
	         }
		 }    
		}
		
		if( checkAllowedUser && checkAllowedRole)
		      result=true;
		
		}
		catch(Exception e2 ) {

		}
		
		return result;
	}
	
	private String getOriginalEventIdAttr(int e,XTrace origPTrace ) {
		String origEventId="";
		

		try {
		int evIndex = convertedArtificialTraceActivityMap.get(e).indexOf(":");
		int eventIndex = Integer.parseInt(convertedArtificialTraceActivityMap.get(e).substring(0, evIndex)) ;//original event Index
		
		
		origEventId= origPTrace.get(eventIndex).getAttributes().get("id").toString();
		
		}
		catch(Exception e2 ) {

		}
		
		return origEventId;
	}
	
		
	private boolean privacyCheckForDataLayer(int e,XTrace origDTrace,String matchedActivityName ) {
		boolean result=false;
		boolean checkAllowedUser=false;
		boolean checkAllowedRole=false;
		
		
		
		String  activity = matchedActivityName.substring(0, matchedActivityName.indexOf("+")) ;
		
		String actor= origDTrace.get(e).getAttributes().get("org:resource").toString();
		
		//check with process model
				ArrayList<String> roleOnModel=new ArrayList<String>();
				roleOnModel= MultiLayerAlignment.getRolefromSODModelMap(activity);
				
		 
				//check with Org Model
				ArrayList<String> roleListOnOrgModel=new ArrayList<String>();
				roleListOnOrgModel=MultiLayerAlignment.getRolefromOrganisationalModelMap(actor);
				
				if(!roleListOnOrgModel.isEmpty()) {
					checkAllowedUser=true;
				}
				

				for (String userRole : roleListOnOrgModel){
				 for (String activityRole : roleOnModel){
			         if (activityRole.equalsIgnoreCase(userRole)){
			               checkAllowedRole=true;
			         }
				 }    
				}
				
				if( checkAllowedUser && checkAllowedRole)
				      result=true;
		
		return result;
	}
	private boolean checkTotallySycConditions(int dataevent,XTrace origDTrace,String matchedActivityName, int processEventIndex, XTrace pxTrace) {
		boolean result=false;
		try {
			int evIndex = convertedArtificialTraceActivityMap.get(processEventIndex).indexOf(":");
			int eventIndex = Integer.parseInt(convertedArtificialTraceActivityMap.get(processEventIndex).substring(0, evIndex)) ;//original event Index
			String processActor= pxTrace.get(eventIndex).getAttributes().get("org:resource").toString();
				
		String dataEventTimeStamp= origDTrace.get(dataevent).getAttributes().get("time:timestamp").toString();
		dataEventTimeStamp= dataEventTimeStamp.substring(0,dataEventTimeStamp.indexOf("+")).replace("T", " ");
		String oprActor= origDTrace.get(dataevent).getAttributes().get("org:resource").toString();
	
		String processId="";

		if(convertedArtificialOrigMap.containsKey(processEventIndex)){
			processId= convertedArtificialOrigMap.get(processEventIndex).substring(convertedArtificialOrigMap.get(processEventIndex).indexOf("(")+1, convertedArtificialOrigMap.get(processEventIndex).indexOf(")") );
		}

		
		
		String processIDActivity=processId+"*"+matchedActivityName.substring(0,matchedActivityName.indexOf("+"));

		String processStartTime="";
		String processCompleteTime="";

		if(activitySCMatchMap.containsKey(processIDActivity)) {
			
		 processStartTime= activitySCMatchMap.get(processIDActivity).substring(0, activitySCMatchMap.get(processIDActivity).indexOf("*"));
		 processCompleteTime= activitySCMatchMap.get(processIDActivity).substring(activitySCMatchMap.get(processIDActivity).indexOf("*")+1, activitySCMatchMap.get(processIDActivity).length() );
		}
	
		if (processStartTime.contains("+")){
		processStartTime= processStartTime.substring(0,processStartTime.indexOf("+")).replace("T", " ");
		processCompleteTime= processCompleteTime.substring(0,processCompleteTime.indexOf("+")).replace("T", " ");
		}
		
		Date oprEvent = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
                .parse(dataEventTimeStamp);
		Date start = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
                .parse(processStartTime);
        Date end = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
                .parse(processCompleteTime);
     


        if (((start.compareTo(oprEvent) < 0 || start.compareTo(oprEvent)==0) && (oprEvent.compareTo(end) < 0 ||oprEvent.compareTo(end)==0)) && processActor.equalsIgnoreCase(oprActor)){
        	result=true;
        } 
        else {
        	result=false;
        }
	} catch (ParseException e) {
		System.out.println("the format of time is wrong-->");
		
    }
	
		return result;
	}
	
	

	
	public SyncReplayResult toMLSyncReplayResult(Replayer replayer,
			SyncProduct product,
			TObjectIntMap<org.processmining.multilayeralignment.models.mlreplay.Utils.Statistic> stats,
			int[] alignment,
			XTrace traceOfProcess,
			int traceIndexOfProcess,
			XTrace traceOfData,
			int traceIndexOfData,
			ArrayList<Object> transitionList) {
		
		List<Object> nodeInstance = new ArrayList<>(alignment.length);
		List<StepTypes> stepTypes = new ArrayList<>(alignment.length);
		
		int mm = 0, plm = 0,dlm = 0, tsm = 0, psm = 0, tm=0;
		for (int i = 0; i < alignment.length; i++) {
			
			int t = alignment[i];
			
			if (product.getTypeOf(t) == SyncProduct.LOG_MOVE) {
				int[] events = product.getEventOf(t);
				
					nodeInstance.add(product.getTransitionLabel(t));
					stepTypes.add(StepTypes.PLM);
					plm += product.getCost(t);
			
			} 
			if (product.getTypeOf(t) == SyncProduct.DLOG_MOVE) {
				int[] devents = product.getEventOf(t);
				
				    nodeInstance.add(product.getTransitionLabel(t));
					stepTypes.add(StepTypes.DLM);
					dlm += product.getCost(t);

			}		
			
			else {
				
				if (product.getTypeOf(t) == SyncProduct.MODEL_MOVE) {
					nodeInstance.add(product.getTransitionLabel(t));
					stepTypes.add(StepTypes.MM);
					mm += product.getCost(t);
				} else if (product.getTypeOf(t) == SyncProduct.SYNC_MOVE) {
					nodeInstance.add(product.getTransitionLabel(t));
					int[] events = product.getEventOf(t);
					
						stepTypes.add(StepTypes.TSM);
						tsm += product.getCost(t);

				} 
				else if (product.getTypeOf(t) == SyncProduct.PSYNC_MOVE) {
					nodeInstance.add(product.getTransitionLabel(t));
					int[] events = product.getEventOf(t);
					
						stepTypes.add(StepTypes.PSM);
						psm += product.getCost(t);
				}else if (product.getTypeOf(t) == SyncProduct.TAU_MOVE) {
					nodeInstance.add(product.getTransitionLabel(t));
					stepTypes.add(StepTypes.TM);
					tm += product.getCost(t);
				}
				
				else if (product.getTypeOf(t) == SyncProduct.PSYNC_MOVE_T2) {
					nodeInstance.add(product.getTransitionLabel(t));
					stepTypes.add(StepTypes.PSMP);
					tm += product.getCost(t);
				}
				else if (product.getTypeOf(t) == SyncProduct.SYNC_MOVE_T2) {
					nodeInstance.add(product.getTransitionLabel(t));
					stepTypes.add(StepTypes.TSMP);
					tm += product.getCost(t);
				}
			}
		}

		traceIndexOfProcess= Integer.valueOf(product.getLabel());
		SyncReplayResult srr = new SyncReplayResult(nodeInstance, stepTypes, traceIndexOfProcess);
		srr.addInfo(PNRepResult.RAWFITNESSCOST, 1.0 * stats.get(Statistic.COST));
		srr.addInfo(PNRepResult.TIME, (stats.get(Statistic.TOTALTIME)) / 1000.0);
		srr.addInfo(PNRepResult.QUEUEDSTATE, 1.0 * stats.get(Statistic.QUEUEACTIONS));
		if (plm + tsm == 0) {
			srr.addInfo(PNRepResult.MOVELOGFITNESS, 1.0);
		} else {
			srr.addInfo(PNRepResult.MOVELOGFITNESS, 1.0 - (1.0 * plm) / (plm + tsm));
		}
		if (mm + psm == 0) {
			srr.addInfo(PNRepResult.MOVEMODELFITNESS, 1.0);
		} else {
			srr.addInfo(PNRepResult.MOVEMODELFITNESS, 1.0 - (1.0 * mm) / (mm + tsm));
		}
		srr.addInfo(PNRepResult.NUMSTATEGENERATED, 1.0 * stats.get(Statistic.MARKINGSREACHED));
		srr.addInfo(PNRepResult.ORIGTRACELENGTH, 1.0 * traceOfProcess.size());
		srr.addInfo(Replayer.TRACEEXITCODE, new Double(stats.get(Statistic.EXITCODE)));
		srr.addInfo(Replayer.MEMORYUSED, new Double(stats.get(Statistic.MEMORYUSED)));
		srr.addInfo(Replayer.PREPROCESSTIME, (stats.get(Statistic.PREPROCESSTIME)) / 1000.0);
		srr.addInfo(Replayer.HEURISTICSCOMPUTED, (double) stats.get(Statistic.HEURISTICSCOMPUTED));
		srr.setReliable(stats.get(Statistic.EXITCODE) == Utils.OPTIMALALIGNMENT);
		return srr;
	}
	
	
public SyncProduct getSyncProduct(XTrace xTrace, ArrayList<? super Transition> transitionList,
		boolean partiallyOrderSameTimestamp) {
	// TODO Auto-generated method stub
	return null;
}

public SyncProduct getSyncProductForEmptyTrace(ArrayList<? super Transition> transitionList) {
	// TODO Auto-generated method stub
	return null;
}

public Trace getTrace(XTrace xTrace, boolean partiallyOrderSameTimestamp) {
	// TODO Auto-generated method stub
	return null;
}

public SyncReplayResult toSyncReplayResult(Replayer replayer, SyncProduct product,
		TObjectIntMap<org.processmining.multilayeralignment.models.mlreplay.Utils.Statistic> statistics,
		int[] alignment, XTrace traceOfProcess, int traceIndexOfProcess, XTrace traceOfData, int traceIndexOfData,
		ArrayList<? super Transition> transitionList) {
	// TODO Auto-generated method stub
	return null;
}

public SyncReplayResult toSyncReplayResult(Replayer replayer, SyncProduct product,
		TObjectIntMap<org.processmining.multilayeralignment.models.mlreplay.Utils.Statistic> statistics,
		int[] alignment, XTrace trace, int traceIndex, ArrayList<? super Transition> transitionList) {
	// TODO Auto-generated method stub
	return null;
}
	
}