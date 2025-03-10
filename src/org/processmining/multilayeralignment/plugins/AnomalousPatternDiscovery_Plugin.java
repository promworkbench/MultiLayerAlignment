package org.processmining.multilayeralignment.plugins;


import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.csv.CSVFile;



@Plugin(name = "Anomalous Pattern Discovery",
returnLabels = { "AnamalousBehavioralPatternsDiscovery" },
returnTypes = {String.class},
parameterLabels = {"Deviations Dataset"},
help = "Detecting Anamalous Behavioral Patterns from Deviations dataset",
userAccessible = true)

public class AnomalousPatternDiscovery_Plugin {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Azadeh S. Mozafari Mehr", email = "a.s.mozafari.mehr@tue.nl",
			uiLabel = UITopiaVariant.USEVARIANT, pack = "")
	@PluginVariant(variantLabel = "", requiredParameterLabels = {0})

	public String  replay1(PluginContext context, CSVFile deviationsDataset) throws Exception {
		
		//TODO  check inputs
		SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Anomalous Pattern Discovery Configuration");
            AnomalousPatternDiscoveryConfiguration entryForm = new AnomalousPatternDiscoveryConfiguration();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.getContentPane().add(entryForm);

            frame.setVisible(true);
        });
    	
		
		return "deviating behavioras";
		//return replay(context, deviationsDataset);
	}
	
	//*private CSVFile replay(PluginContext context, CSVFile DataModelCSVFile, CSVFile OrganisationalModelCSVFile, Petrinet net, XLog processLog , XLog dataLog)throws Exception {
	//try {	
    	//*context.log("MultiLayerAlignment Start");
    	//*MLAWithContextReplayResult res = new MLAWithContext().perform(context, DataModelCSVFile, OrganisationalModelCSVFile, net, processLog, dataLog);
    	//*context.log("MultiLayerAlignment End");
		
		//*context.addConnection(new MLAWithContextResultConnection("RepResults", res, DataModelCSVFile, OrganisationalModelCSVFile,net,processLog, dataLog));
		//*return res;
   //} catch (Exception exc){
		//context.log("Check the order and formats of inputs");
		//context.getFutureResult(0).cancel(true);
		//return null;
	//}
		
		
	//}
	
	

}


