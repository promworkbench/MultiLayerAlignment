package org.processmining.multilayeralignment.plugins;


import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.csv.CSVFile;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.multilayeralignment.plugins.replayer.MLAWithContextReplayResult;
import org.processmining.multilayeralignment.plugins.replayer.MLAWithContextResultConnection;



@Plugin(name = "MLA With Context",
returnLabels = { "Multi-Layer Alignment with Context Results" },
returnTypes = {MLAWithContextReplayResult.class},
parameterLabels = {"Data Model", "Organisational Model", "Process Model", "Process Log", "Data Log"},
help = "Multi-perspective conformance checking(includes privacy, process and data perspectives) with Context",
userAccessible = true)

public class MLAWithContext_Plugin {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Azadeh S. Mozafari Mehr", email = "a.s.mozafari.mehr@tue.nl",
			uiLabel = UITopiaVariant.USEVARIANT, pack = "")
	@PluginVariant(variantLabel = "", requiredParameterLabels = { 0, 1, 2, 3, 4})

	public MLAWithContextReplayResult  replay1(PluginContext context, CSVFile DataModelCSVFile, CSVFile OrganisationalModelCSVFile, Petrinet net, XLog processLog , XLog dataLog) throws Exception {
		
		//TODO  check inputs
		return replay(context, DataModelCSVFile, OrganisationalModelCSVFile, net, processLog, dataLog );
	}
	
    private MLAWithContextReplayResult replay(PluginContext context, CSVFile DataModelCSVFile, CSVFile OrganisationalModelCSVFile, Petrinet net, XLog processLog , XLog dataLog)throws Exception {
	//try {	
    	context.log("MultiLayerAlignment Start");
    	MLAWithContextReplayResult res = new MLAWithContext().perform(context, DataModelCSVFile, OrganisationalModelCSVFile, net, processLog, dataLog);
		context.log("MultiLayerAlignment End");
		
		context.addConnection(new MLAWithContextResultConnection("RepResults", res, DataModelCSVFile, OrganisationalModelCSVFile,net,processLog, dataLog));
		return res;
   //} catch (Exception exc){
		//context.log("Check the order and formats of inputs");
		//context.getFutureResult(0).cancel(true);
		//return null;
	//}
		
		
	}
	
	

}


