package org.processmining.multilayeralignment.models.mlreplay;
import java.util.ArrayList;

import org.deckfour.xes.model.XTrace;
import org.processmining.multilayeralignment.models.mlreplay.Utils.Statistic;

import gnu.trove.map.TObjectIntMap;
import nl.tue.astar.Trace;

public interface SyncProductFactory<T> {

	public SyncProduct getSyncProduct(XTrace xTrace, ArrayList<? super T> transitionList,
			boolean partiallyOrderSameTimestamp);

	public SyncProduct getSyncProductForEmptyTrace(ArrayList<? super T> transitionList);

	public Trace getTrace(XTrace xTrace, boolean partiallyOrderSameTimestamp);

	public SyncReplayResult toSyncReplayResult(Replayer replayer, SyncProduct product,
			TObjectIntMap<Statistic> statistics, int[] alignment, XTrace trace, int traceIndex,
			ArrayList<? super T> transitionList);
	
	public SyncReplayResult toSyncReplayResult(Replayer replayer, SyncProduct product,
			TObjectIntMap<Statistic> statistics, int[] alignment, XTrace traceOfProcess, int traceIndexOfProcess,XTrace traceOfData, int traceIndexOfData,
			ArrayList<? super T> transitionList);
	
}