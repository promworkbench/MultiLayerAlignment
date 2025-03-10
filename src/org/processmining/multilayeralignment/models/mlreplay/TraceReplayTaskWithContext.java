package org.processmining.multilayeralignment.models.mlreplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
//import nl.tue.alignment.algorithms.ReplayAlgorithm;
import org.processmining.multilayeralignment.algorithms.ReplayAlgorithm;
//import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;
import org.processmining.multilayeralignment.algorithms.ReplayAlgorithm.Debug;
//import nl.tue.alignment.algorithms.implementations.AStar;
import org.processmining.multilayeralignment.algorithms.implementations.AStar;
//import nl.tue.alignment.algorithms.implementations.AStarLargeLP;
import org.processmining.multilayeralignment.algorithms.implementations.AStarLargeLP;
//import nl.tue.alignment.algorithms.implementations.Dijkstra;
import org.processmining.multilayeralignment.algorithms.implementations.Dijkstra;
//import nl.tue.alignment.Utils.Statistic;
import org.processmining.multilayeralignment.models.mlreplay.Utils.Statistic;

import gnu.trove.map.TObjectIntMap;
import nl.tue.astar.Trace;
import nl.tue.astar.util.ilp.LPMatrixException;

public class TraceReplayTaskWithContext implements Callable<TraceReplayTaskWithContext> {
	//**need to change
	
	public static enum TraceReplayResult {
		FAILED, DUPLICATE, SUCCESS
	}

	// transient fields. Cleared after call();
	private transient ReplayerWithContext replayer;
	private transient XTrace processTrace;
	private transient XTrace dataTrace;
	private transient ReplayerParameters parameters;
	private transient SyncProduct product;
	private transient ReplayAlgorithm algorithm;
    
	// Available after "call()"
	private int original;
	private final int processTraceIndex;
	private final int dataTraceIndex;
	private SyncReplayResult srr;
	private TraceReplayResult result;
	private int processTraceLogMoveCost;
	private int dataTraceLogMoveCost;
	private boolean withContext;

	// internal variables
	private int[] alignment;
	private int maximumNumberOfStates;
	private int[] eventsWithErrors;
	private final long preProcessTimeNanoseconds;
	private final int timeoutMilliseconds;

	public TraceReplayTaskWithContext(ReplayerWithContext replayer, ReplayerParameters parameters, int timeoutMilliseconds,
			int maximumNumberOfStates, long preProcessTimeNanoseconds, int... eventsWithErrors) {
		
		this.replayer = replayer;
		this.parameters = parameters;
		this.maximumNumberOfStates = maximumNumberOfStates;
		this.preProcessTimeNanoseconds = preProcessTimeNanoseconds;
		this.processTrace = XFactoryRegistry.instance().currentDefault().createTrace();
		XConceptExtension.instance().assignName(processTrace, "Empty");
		this.dataTrace = XFactoryRegistry.instance().currentDefault().createTrace();
		XConceptExtension.instance().assignName(dataTrace, "Empty");
		this.processTraceIndex = -1;
		this.dataTraceIndex = -1;
		this.timeoutMilliseconds = timeoutMilliseconds;
		this.eventsWithErrors = eventsWithErrors;
		Arrays.sort(eventsWithErrors);

	}



	public TraceReplayTaskWithContext( boolean withContext, ReplayerWithContext replayer, ReplayerParameters parameters, XTrace pTrace, int pTraceIndex,XTrace dTrace, int dTraceIndex,
			int timeoutMilliseconds, int maximumNumberOfStates, long preProcessTimeNanoseconds,
			int... eventsWithErrors) {

		this.replayer = replayer;
		this.parameters = parameters;
		this.processTrace = pTrace;
		this.dataTrace = dTrace;
		this.processTraceIndex = -1;
		this.dataTraceIndex = -1;
		this.timeoutMilliseconds = timeoutMilliseconds;
		this.maximumNumberOfStates = maximumNumberOfStates;
		this.preProcessTimeNanoseconds = preProcessTimeNanoseconds;
		this.eventsWithErrors = eventsWithErrors;
		this.withContext= withContext;
		Arrays.sort(eventsWithErrors);
	}



	private int getPTraceCost(XTrace trace) {
		int cost = 0;
		for (XEvent e : trace) {
			cost += replayer.getCostLM(replayer.getPEventClass(e));
		}
		return cost;
	}
	private int getDTraceCost(XTrace trace) {
		int cost = 0;
		for (XEvent e : trace) {
			cost += replayer.getCostDM(replayer.getPEventClass(e));
		}
		return cost;
	}
	public TraceReplayTaskWithContext call() throws LPMatrixException {
		if (replayer == null) {
			return this;
		}
		
		Trace processTraceAsList = this.replayer.factory.getProcessTrace(processTrace, parameters.partiallyOrderEvents);
		this.processTraceLogMoveCost = getPTraceCost(processTrace);

		Trace dataTraceAsList = this.replayer.factory.getDataTrace(dataTrace, parameters.partiallyOrderEvents);
		this.dataTraceLogMoveCost = getDTraceCost(dataTrace);

		if (this.replayer.mergeDuplicateTraces) {
			synchronized (this.replayer.trace2FirstIdenticalTrace) {
				original = this.replayer.trace2FirstIdenticalTrace.get(processTraceAsList);
				if (original < 0) {
					this.replayer.trace2FirstIdenticalTrace.put(processTraceAsList, processTraceIndex);
				}
			}
		} else {
			original = -1;
		}
		
		
		if (original < 0) {

			ArrayList<Object> transitionList = new ArrayList<>();
			long pt;
			synchronized (this.replayer.factory) {
				long startSP = System.nanoTime();
				this.replayer.factory.fillMaps(processTrace);
				product = this.replayer.factory.getSyncProduct(processTrace,dataTrace, transitionList, parameters.partiallyOrderEvents, withContext);
				//System.out.println("*******************This is SyncProduct: ");
				//Utils.toDot(product, System.out);
				long endSP = System.nanoTime();
				// compute the pre-process time based on the fixed overhead, plus time for this replayer...
				pt = preProcessTimeNanoseconds + (endSP - startSP);
			}

			if (product != null) {
				if (parameters.debug == Debug.DOT) {
					//Utils.toDot(product, ReplayAlgorithm.Debug.getOutputStream());
				}

				algorithm = getAlgorithm(product);

				algorithm.putStatistic(Statistic.PREPROCESSTIME, (int) (pt / 1000));
				algorithm.putStatistic(Statistic.CONSTRAINTSETSIZE, replayer.getConstraintSetSize());

				alignment = algorithm.run(this.replayer.getProgress(), timeoutMilliseconds, maximumNumberOfStates,
						parameters.costUpperBound);
//for test   
				//System.out.println("*******************This is SyncProduct: ");
				//Utils.toDot(product, System.out);
				//System.out.println("*******************This is Alignment2: ");
				//Utils.toDot(product, alignment, ReplayAlgorithm.Debug.getOutputStream());
			
				
				//System.out.println("*******************for test2 ");
				if (parameters.debug == Debug.DOT) {
					//Utils.toDot(product, alignment, ReplayAlgorithm.Debug.getOutputStream());
				}
				TObjectIntMap<Statistic> stats = algorithm.getStatistics();

				srr = this.replayer.factory.toMLSyncReplayResult(replayer, product, stats, alignment, processTrace, processTraceIndex,dataTrace, dataTraceIndex,
						transitionList);
			
				this.replayer.getProgress().inc();
				result = TraceReplayResult.SUCCESS;
			} else {
				this.replayer.getProgress().inc();
				result = TraceReplayResult.FAILED;
			}
		} else {
			this.replayer.getProgress().inc();
			result = TraceReplayResult.DUPLICATE;
		}

		replayer = null;
		processTrace = null;
		dataTrace = null;
		parameters = null;
		algorithm = null;
		product = null;

		return this;
	}

	private ReplayAlgorithm getAlgorithm(SyncProduct product) throws LPMatrixException {
		switch (parameters.algorithm) {
			case ASTAR :
				if (parameters.buildFullStatespace) {
					return new AStar.Full(product, parameters.moveSort, parameters.queueSort, parameters.preferExact,
							parameters.useInt, parameters.debug);
				} else {

					return new AStar(product, parameters.moveSort, parameters.queueSort, parameters.preferExact, //
							parameters.useInt, parameters.debug);
				}
			case INCREMENTALASTAR :
				if (parameters.buildFullStatespace) {
					if (parameters.initialSplits > 0 && eventsWithErrors.length == 0) {
						return new AStarLargeLP.Full(product, parameters.moveSort, parameters.useInt,
								parameters.initialSplits, parameters.debug);
					} else {
						return new AStarLargeLP.Full(product, parameters.moveSort, parameters.useInt, parameters.debug,
								eventsWithErrors);
					}
				} else {
					if (parameters.initialSplits > 0 && eventsWithErrors.length == 0) {
						return new AStarLargeLP(product, parameters.moveSort, parameters.useInt,
								parameters.initialSplits, parameters.debug);
					} else {
						return new AStarLargeLP(product, parameters.moveSort, parameters.useInt, parameters.debug,
								eventsWithErrors);
					}
				}
			case DIJKSTRA :
				if (parameters.buildFullStatespace) {
					return new Dijkstra.Full(product, parameters.moveSort, parameters.queueSort, parameters.debug);
				} else {
					return new Dijkstra(product, parameters.moveSort, parameters.queueSort, parameters.debug);
				}
		}
		assert false;
		return null;
	}

	public TraceReplayResult getResult() {
		return result;
	}

	public SyncReplayResult getSuccesfulResult() {
		return srr;
	}

	public int getOriginalTraceIndex() {
		return original;
	}

	public int getProcessTraceIndex() {
		return processTraceIndex;
	}
	public int getDataTraceIndex() {
		return dataTraceIndex;
	}
	public int getProcessTraceLogMoveCost() {
		return processTraceLogMoveCost;
	}
	public int getdataTraceLogMoveCost() {
		return processTraceLogMoveCost;
	}

}