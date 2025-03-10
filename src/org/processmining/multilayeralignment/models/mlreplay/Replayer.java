package org.processmining.multilayeralignment.models.mlreplay;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XEvent;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.multilayeralignment.algorithms.syncproduct.MlBasicSyncProductFactory;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import nl.tue.alignment.Progress;
import nl.tue.alignment.algorithms.constraints.ConstraintSet;
import nl.tue.astar.Trace;

public class Replayer {

	public static final String MAXMODELMOVECOST = "Model move cost empty trace";
	public static final String TRACEEXITCODE = "Exit code of alignment for trace";
	public static final String MEMORYUSED = "Approximate memory used (kb)";
	public static final String PREPROCESSTIME = "Pre-processing time (ms)";
	public static final String HEURISTICSCOMPUTED = "Number of LPs solved";

	final TObjectIntMap<Trace> trace2FirstIdenticalTrace;

	private final ReplayerParameters parameters;
	private final Map<XEventClass, Integer> costLM;
	private final Map<XEventClass, Integer> costDM;
	final MlBasicSyncProductFactory factory;
	final XEventClasses classesP;
	final XEventClasses classesD;
	final private Map<Transition, Integer> costMM;
	private Progress progress;
	final boolean mergeDuplicateTraces;

	protected TObjectIntMap<XEventClass> processEvClass2id;
	protected TObjectIntMap<XEventClass> dataEvClass2id;
	private ConstraintSet constraintSet;

      
	public Replayer(ReplayerParameters parameters, Petrinet net, Marking initialMarking, Marking finalMarking,
			XEventClasses classesP, XEventClasses classesD, Map<Transition, Integer> costMM,Map<Transition, Integer> costPM, Map<XEventClass, Integer> costLM,
			Map<Transition, Integer> costSM,Map<XEventClass, Integer> costDM ,TransEvClassMapping mapping1,TransEvClassMapping mapping2, boolean mergeDuplicateTraces) {
		this.parameters = parameters;
		this.classesP = classesP;
		this.classesD = classesD;
		this.mergeDuplicateTraces = mergeDuplicateTraces;
		if (costMM == null) {
			costMM = new HashMap<>();
			for (Transition t : net.getTransitions()) {
				if (t.isInvisible()) {
					costMM.put(t, 0);
				} else {
					costMM.put(t, 2);
				}
			}
		}
		if (costSM == null) {
			costSM = new HashMap<>();
			for (Transition t : net.getTransitions()) {
				costSM.put(t, 0);
			}
		}
		if (costPM == null) {
			costPM = new HashMap<>();
			for (Transition t : net.getTransitions()) {
				costPM.put(t, 1);
			}
		}
		if (costLM == null) {
			costLM = new HashMap<>();
			for (XEventClass clazz : classesP.getClasses()) {
				costLM.put(clazz, 2);
			}
		}
		if (costDM == null) {
			costDM = new HashMap<>();
			for (XEventClass clazz : classesD.getClasses()) {
				costDM.put(clazz, 2);
			}
		}
		this.costMM = costMM;
		this.costLM = costLM;
		this.costDM = costDM;
		
		processEvClass2id = Utils.createClass2IDProcessEvent(costLM);
		if (parameters.preProcessUsingPlaceBasedConstraints) {
			constraintSet = new ConstraintSet(net, initialMarking, classesP, processEvClass2id, mapping1);
		} else {
			constraintSet = null;
		}
   
		dataEvClass2id = Utils.createClass2IDDataEvent(classesD);
		if (parameters.preProcessUsingPlaceBasedConstraints) {
			constraintSet = new ConstraintSet(net, initialMarking, classesD, dataEvClass2id, mapping2);
		} else {
			constraintSet = null;
		}
	
		factory = createSyncProductFactory(parameters, net, initialMarking, finalMarking, classesP,classesD,costLM,costMM,costPM, costLM, costSM,costDM,
				mapping1,mapping2);

		if (mergeDuplicateTraces) {
			trace2FirstIdenticalTrace = new TObjectIntHashMap<>(10, 0.7f, -1);
		} else {
			trace2FirstIdenticalTrace = null;
		}
	}
    
	
	protected MlBasicSyncProductFactory createSyncProductFactory(ReplayerParameters parameters, Petrinet net, Marking initialMarking,
			Marking finalMarking, XEventClasses classesP,XEventClasses classesD,Map<XEventClass, Integer> convertedEventstoOperations,
			Map<Transition, Integer> costMM,
			Map<Transition, Integer> costPM,
			Map<XEventClass, Integer> costLM,
			Map<Transition, Integer> costSM,
			Map<XEventClass, Integer> costDM,
			TransEvClassMapping mapping1,TransEvClassMapping mapping2) {
			return new MlBasicSyncProductFactory(net, classesP, processEvClass2id,classesD, dataEvClass2id ,mapping1,mapping2 ,costLM,costMM,costPM, costLM, costSM,costDM,
					initialMarking, finalMarking);
	}


	private boolean isCancelled() {
		return getProgress().isCanceled();
	}

	public int getCostLM(XEventClass classOf) {
		return costLM != null && costLM.containsKey(classOf) ? costLM.get(classOf) : 1;
	}
	public int getCostDM(XEventClass classOf) {
		return costDM != null && costDM.containsKey(classOf) ? costDM.get(classOf) : 1;
	}
	public int getCostMM(Transition transition) {
		return costMM != null && costMM.containsKey(transition) ? costMM.get(transition) : 1;
	}

	public Progress getProgress() {
		return progress == null ? Progress.INVISIBLE : progress;
	}

	public int getConstraintSetSize() {
		return constraintSet == null ? 0 : constraintSet.size();
	}

	public XEventClass getPEventClass(XEvent e) {
		return classesP.getClassOf(e);
	}
	public XEventClass getDEventClass(XEvent e) {
		return classesD.getClassOf(e);
	}
}

