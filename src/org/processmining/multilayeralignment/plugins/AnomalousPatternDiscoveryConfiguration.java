package org.processmining.multilayeralignment.plugins;

import java.io.FileNotFoundException;

import javax.script.ScriptException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.processmining.framework.util.ui.widgets.ProMTextField;

public class AnomalousPatternDiscoveryConfiguration extends JComponent {
    private ProMTextField minThresholdField;
    private ProMTextField minSupportField;
    private ProMTextField supportThresholdField;
    private ProMTextField confidenceThresholdField;

    public AnomalousPatternDiscoveryConfiguration() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        minThresholdField = new ProMTextField("0.001");
        minSupportField = new ProMTextField("0.01");
        supportThresholdField = new ProMTextField("0.001");
        confidenceThresholdField = new ProMTextField("0.25");

        add(createFieldPanel("min_threshold:", minThresholdField));
        add(createFieldPanel("min_support:", minSupportField));
        add(createFieldPanel("support_threshold:", supportThresholdField));
        add(createFieldPanel("confidence_threshold:", confidenceThresholdField));

        JButton nextButton = new JButton("Next");
        add(nextButton);

        // Add an action listener to the Next button to handle the button click
        nextButton.addActionListener(e -> {
            double minThreshold = Double.parseDouble(minThresholdField.getText());
            double minSupport = Double.parseDouble(minSupportField.getText());
            double supportThreshold = Double.parseDouble(supportThresholdField.getText());
            double confidenceThreshold = Double.parseDouble(confidenceThresholdField.getText());

            // Call the next form or perform your desired action
            //String inputPath1, float min_support1,float min_threshold1,float support_threshold1, float confidence_threshold1
            try {
				String res = new AnomalousPatternDiscovery().perform( "D:\\OrganizedFolder\\FinalExperiments\\RuleMining\\Inputs\\Experiment1.csv" ,(float)minSupport,(float) minThreshold, (float) supportThreshold,(float) confidenceThreshold);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ScriptException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            SwingUtilities.getWindowAncestor(this).dispose();
        });
    }

    private JPanel createFieldPanel(String label, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JLabel labelComponent = new JLabel(label);
        panel.add(labelComponent);
        panel.add(field);
        return panel;
    }
}


