package org.processmining.multilayeralignment.plugins.replayer;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.log.csv.CSVFile;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
//import org.processmining.plugins.petrinet.replayresult.PNRepResult;
//import org.processmining.security.plugins.interDataProcessPolicy.CRUD;
//import org.processmining.security.plugins.replayer.InterLevelReplayResult;

public class MultiLayerResultConnection extends AbstractConnection {
	public final static String REPRESULT = "ReplayResult";
	public final static String DModel = "DataModel";
	public final static String OrgModel = "OrganisationalModel";
	public final static String PModel = "ProcessModel";
	public final static String PLog = "ProcessLog";
	public final static String DLog = "DataLog";

	public MultiLayerResultConnection(String label,MultiLayerReplayResult repResult,CSVFile DataModel,CSVFile OrganisationalModel,Petrinet net,XLog processLog, XLog dataLog)
	{
		super(label);
		put(REPRESULT, repResult);
		put(DModel, DataModel);
		put(OrgModel, OrganisationalModel);
		put(PModel, net);
		put(PLog, processLog);
		put(DLog, dataLog);
		
	}

}
