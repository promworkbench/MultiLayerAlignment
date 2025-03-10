package org.processmining.multilayeralignment.models;

import org.deckfour.xes.model.XLog;
import org.processmining.log.csv.CSVFile;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class MultiLayerAlignmentInputs {

	private CSVFile DataModelCSVFile;
	private CSVFile OrganisationalModelCSVFile;
	private Petrinet net;
	private XLog processLog;
	private XLog dataLog;
	
	public MultiLayerAlignmentInputs(CSVFile DataModelCSVFile, CSVFile OrganisationalModelCSVFile, Petrinet net,
			XLog processLog, XLog dataLog) {
		this.setDataModelCSVFile(DataModelCSVFile);
		this.setOrganisationalModelCSVFile(OrganisationalModelCSVFile);
		this.setNet(net);
		this.setProcessLog(processLog);
		this.setDataLog(dataLog);
	}

	public CSVFile getDataModelCSVFile() {
		return DataModelCSVFile;
	}

	public void setDataModelCSVFile(CSVFile dataModelCSVFile) {
		DataModelCSVFile = dataModelCSVFile;
	}

	public CSVFile getOrganisationalModelCSVFile() {
		return OrganisationalModelCSVFile;
	}

	public void setOrganisationalModelCSVFile(CSVFile organisationalModelCSVFile) {
		OrganisationalModelCSVFile = organisationalModelCSVFile;
	}

	public Petrinet getNet() {
		return net;
	}

	public void setNet(Petrinet net) {
		this.net = net;
	}

	public XLog getProcessLog() {
		return processLog;
	}

	public void setProcessLog(XLog processLog) {
		this.processLog = processLog;
	}

	public XLog getDataLog() {
		return dataLog;
	}

	public void setDataLog(XLog dataLog) {
		this.dataLog = dataLog;
	}
	
}
