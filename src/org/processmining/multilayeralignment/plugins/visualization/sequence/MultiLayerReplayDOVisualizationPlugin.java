package org.processmining.multilayeralignment.plugins.visualization.sequence;



import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.multilayeralignment.plugins.replayer.MultiLayerReplayResult;


@Plugin(name = "MLA with Context Result Visualization",
returnLabels = { "Visualized Log-Model Alignment Projection to Log" },
returnTypes = { JComponent.class },
parameterLabels = { "Log-Model alignment" },
userAccessible = true)
@Visualizer
public class MultiLayerReplayDOVisualizationPlugin {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize2(PluginContext context, MultiLayerReplayResult logReplayResult) {
		context.log("Visualization start");
		return new MultiLayerReplayResultDOVisualizationPanel (logReplayResult, context.getProgress());
	}	
}