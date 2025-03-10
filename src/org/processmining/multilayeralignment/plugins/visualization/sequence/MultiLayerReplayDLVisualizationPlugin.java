package org.processmining.multilayeralignment.plugins.visualization.sequence;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.multilayeralignment.plugins.replayer.MultiLayerReplayResult;
import org.processmining.multilayeralignment.plugins.replayer.MultiLayerResultConnection;


@Plugin(name = "2.Projection to Data Log", 
returnLabels = { "Visualized Log-Model Alignment Projection to Log" },
returnTypes = { JComponent.class }, 
parameterLabels = { "Log-Model alignment" }, 
userAccessible = true)
@Visualizer
public class MultiLayerReplayDLVisualizationPlugin {
	@PluginVariant(requiredParameterLabels = {0})
	public JComponent visualize1(PluginContext context, MultiLayerReplayResult logReplayResult) {
		
		XLog inProcesslog = null;
		XLog inDatalog = null;
		try {
			MultiLayerResultConnection conn = context.getConnectionManager().getFirstConnection(
					MultiLayerResultConnection.class, context, logReplayResult);

			inProcesslog = conn.getObjectWithRole(MultiLayerResultConnection.PLog);
			inDatalog = conn.getObjectWithRole(MultiLayerResultConnection.DLog);
		} catch (Exception exc) {
			context.log("No net can be found for this log replay result");
			return null;
		}
		context.log("Visualization start");
		return new MultiLayerReplayResultDLVisualizationPanel (logReplayResult, inDatalog, context.getProgress());
	}	

}
