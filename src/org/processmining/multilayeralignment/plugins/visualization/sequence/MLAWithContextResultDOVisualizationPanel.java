package org.processmining.multilayeralignment.plugins.visualization.sequence;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMSplitPane;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.multilayeralignment.models.mlreplay.StepTypes;
import org.processmining.multilayeralignment.models.mlreplay.StepTypesVOL;
import org.processmining.multilayeralignment.models.mlreplay.SyncReplayResult;
import org.processmining.multilayeralignment.plugins.replayer.MLAWithContextReplayResult;
import org.processmining.multilayeralignment.plugins.visualization.tracealignment.MultiLayerAlignmentConstantsWithContext;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerScrollBarUI;

import info.clearthought.layout.TableLayout;

public class MLAWithContextResultDOVisualizationPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = -5897513989508944234L;

	/**
	 * Pointers to property variable
	 */
	protected static int RELIABLEMIN = 0;
	protected static int RELIABLEMAX = 1;
	protected static int MIN = 2;
	protected static int MAX = 3;

	// standard deviation is calculated based on http://mathcentral.uregina.ca/QQ/database/QQ.09.02/carlos1.html
	protected static int SVAL = 4;
	protected static int MVAL = 6;
	protected static int SVALRELIABLE = 5;
	protected static int MVALRELIABLE = 7;
	protected static int PERFECTCASERELIABLECOUNTER = 8;
	
	private Color bgColor = new Color(192,192,192); 
	protected int iconSize = 100, shiftX = 4, shiftY = 2, elementTriOffset = 4;
	
	// this value has to be stored because it is used by actionListener
	protected int numReliableCaseInvolved = 0; 
	
	// total calculated values
	protected Map<String, Double[]> calculations = new HashMap<String, Double[]>();
	protected ProMComboBox<String> interLevelLinkageComboBox;
	protected JPanel allAlignmentPanel;
	protected JButton pageButton;
	protected ProMTextField pageTextField;
	protected JLabel totalPage;
	protected final int numOfalignmentsPerPage = 20;
	protected int selectedPage = 1;
	public final static int SHOW_ALL_LINKS = 0, HIDE_LINKS = 1;	
	protected Progress progress;
	protected MLAWithContextReplayResult replayResult;
	protected XLog inProcessLog;
	protected XLog inDataLog;
	protected ScrollBar alignmentOanemWithScrollBar;
//	protected ProMPropertiesPanel logAlignmentPanel;
	protected final DefaultTableModel reliableCasesTModel = new DefaultTableModel() {
		private static final long serialVersionUID = -4303950078200984098L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	
	public MLAWithContextResultDOVisualizationPanel(MLAWithContextReplayResult replayResult, XLog inProcessLog, XLog inDataLog, Progress progress) { 
		this.replayResult = replayResult;
		this.inProcessLog= inProcessLog;
		this.inDataLog= inDataLog;
		this.progress = progress;
		TableLayout mainLayout = new TableLayout(new double[][] { { TableLayout.FILL }, { TableLayout.FILL } });
		setLayout(mainLayout);
		setBorder(BorderFactory.createEmptyBorder());
		if (progress != null) {
			progress.setMaximum(Math.min(numOfalignmentsPerPage, replayResult.size()));
		}
		pageTextField = new ProMTextField("1");
		pageButton = SlickerFactory.instance().createButton("Go");		
		add(createBottomPanel(), "0,0");
	}
	
	public class ScrollBar extends JScrollPane {
		private static final long serialVersionUID = 1L;

		public ScrollBar(JComponent component){
			super(component);
			setOpaque(true);
			setBackground(WidgetColors.PROPERTIES_BACKGROUND);
			getViewport().setOpaque(true);
			getViewport().setBackground(WidgetColors.PROPERTIES_BACKGROUND);
			setBorder(BorderFactory.createEmptyBorder());
			setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			JScrollBar hBar = getHorizontalScrollBar();
			hBar.setUI(new SlickerScrollBarUI(hBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
					WidgetColors.COLOR_NON_FOCUS, 4, 12));
			hBar.setOpaque(true);
			hBar.setBackground(WidgetColors.PROPERTIES_BACKGROUND);

			
			JScrollBar vBar = getVerticalScrollBar();
			vBar.setUI(new SlickerScrollBarUI(vBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
					WidgetColors.COLOR_NON_FOCUS, 4, 12));
			vBar.setOpaque(true);
			vBar.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		}
	}
	private Component createBottomPanel() {
		
		// clear maps
		calculations.clear();

		// add util
		SlickerFactory factory = SlickerFactory.instance();

		// for each case, create comparison panel

		final NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);

		final NumberFormat nfi = NumberFormat.getInstance();
		nfi.setMaximumFractionDigits(0);
		nfi.setMinimumFractionDigits(0);


		allAlignmentPanel = new JPanel();
		allAlignmentPanel.setLayout(new BoxLayout(allAlignmentPanel, BoxLayout.Y_AXIS));
		
		String link [] = {"Multi layer alignment visualization"};
		interLevelLinkageComboBox = new ProMComboBox<String>(link);
		interLevelLinkageComboBox.addActionListener(this);
		interLevelLinkageComboBox.setMaximumSize(new Dimension(270, 40));
		interLevelLinkageComboBox.setPreferredSize(new Dimension(200, interLevelLinkageComboBox.getPreferredSize().height));
		interLevelLinkageComboBox.setMinimumSize(new Dimension(200, interLevelLinkageComboBox.getPreferredSize().height));
		

		addInterLevelAlignments();
		int lineNumberRight = 0; 
		JPanel statisticPanel = new JPanel();
		
		statisticPanel.setBackground(bgColor);
		double[][] rightMainPanelSize = new double[][] {
				{ TableLayout.PREFERRED },
				{ TableLayout.PREFERRED, 30, TableLayout.PREFERRED, 30, TableLayout.PREFERRED, TableLayout.PREFERRED,
						60, TableLayout.PREFERRED } };

		statisticPanel.setLayout(new TableLayout(rightMainPanelSize));

		// add LEGEND
		statisticPanel.add(createLegendPanel(), "0, " + lineNumberRight++ + ", c, t");		
		JLabel linkLabel = SlickerFactory.instance().createLabel("Select visualization");
		statisticPanel.add(linkLabel, "0, " + lineNumberRight++ + ", c, t");
		statisticPanel.add(interLevelLinkageComboBox, "0, " + lineNumberRight++ + ", c, t");	
		// Page
		statisticPanel.add(createPagePanel(), "0, " + lineNumberRight++ + ", 0, " + lineNumberRight++ + ", c, t");	
		
		// MAIN PANEL
		ProMSplitPane splitPanel = new ProMSplitPane(ProMSplitPane.HORIZONTAL_SPLIT);
		splitPanel.setBorder(BorderFactory.createEmptyBorder());
		splitPanel.setOneTouchExpandable(true);
		
		alignmentOanemWithScrollBar = new ScrollBar(allAlignmentPanel);
		splitPanel.setLeftComponent(alignmentOanemWithScrollBar);//logAlignmentPanel.add(alignmentOanemWithScrollBar));
		splitPanel.setRightComponent(statisticPanel);
		statisticPanel.setBackground(Color.white);
		splitPanel.setResizeWeight(1.0);	
		return splitPanel;
	}
	protected void addInterLevelAlignments(){
		for (int i = (selectedPage - 1) * numOfalignmentsPerPage; i < Math.min(selectedPage * numOfalignmentsPerPage, replayResult.size()); i++) {
			SyncReplayResult res = replayResult.get(i);
			if (progress != null) {
				progress.inc();
			}
			List<Object> nodeInstance = res.getNodeInstance();
			List<StepTypes> stepTypes = res.getStepTypes();
			List<Object> nodeInstanceVOL = res.getNodeInstanceVOL();
			List<StepTypesVOL> stepTypesVOL = res.getStepTypesVOL();
			XTrace processTrace= inProcessLog.get(i);
			XTrace dataTrace= inDataLog.get(i);
			String traceIndex= res.getTraceIndex().first().toString();
			List<Object> rearrangedNodeInstance = res.getRearrangedNodeInstance();
			// ALIGNMENT PANEL

			MLAWithContextInstanceDOConformanceView alignmentPanel = createMLAWithContextInstanceDOView(traceIndex,nodeInstance,stepTypes,nodeInstanceVOL,stepTypesVOL,rearrangedNodeInstance,processTrace,dataTrace);
					

			// set scroll pane 
			JScrollPane hscrollPane = new JScrollPane(alignmentPanel);
			hscrollPane.setOpaque(true);
			hscrollPane.setBackground(bgColor);
			hscrollPane.getViewport().setOpaque(true);
			hscrollPane.getViewport().setBackground(bgColor);
			hscrollPane.setBorder(BorderFactory.createEmptyBorder());
			hscrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			hscrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			JScrollBar hBar = hscrollPane.getHorizontalScrollBar();
			hBar.setUI(new SlickerScrollBarUI(hBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
					WidgetColors.COLOR_NON_FOCUS, 4, 12));
			hBar.setOpaque(true);
			hBar.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
			hBar = hscrollPane.getVerticalScrollBar();
			hBar.setUI(new SlickerScrollBarUI(hBar, new Color(0, 0, 0, 0), new Color(160, 160, 160),
					WidgetColors.COLOR_NON_FOCUS, 4, 12));
			hBar.setOpaque(true);
			hBar.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
			allAlignmentPanel.add(hscrollPane);

		}
	}
	
	protected MLAWithContextInstanceDOConformanceView createMLAWithContextInstanceDOView(String traceIndex,List<Object> nodeInstance, List<StepTypes> stepTypes, List<Object> nodeInstanceVOL, List<StepTypesVOL> stepTypesVOL,List<Object> rearrangedNodeInstances,XTrace processTrace,XTrace dataTrace) {
		return new MLAWithContextInstanceDOConformanceView(traceIndex,nodeInstance,stepTypes,nodeInstanceVOL,stepTypesVOL,rearrangedNodeInstances,40,processTrace,dataTrace);
	}
	
	protected Component createPagePanel(){
		JPanel pagePanel = new JPanel();
		pagePanel.setMinimumSize(new Dimension(200, 400));
		pagePanel.setBorder(BorderFactory.createEmptyBorder());
		pagePanel.setBackground(Color.white);//new Color(192, 192, 192));
		TableLayout layout = new TableLayout(new double[][] { { 0.60, TableLayout.FILL }, {0.5, 0.5}});
		layout.setVGap(1);
		
		pagePanel.setLayout(layout);

		totalPage = SlickerFactory.instance().createLabel("Choose page number (total pages:" + (int)Math.ceil(((double)replayResult.size()) / ((double)numOfalignmentsPerPage)) + ")");
		pagePanel.add(totalPage, "0,0,1,0,c, c");
		pageTextField.addActionListener(this);
		pageTextField.setMaximumSize(new Dimension(150, 40));
		pageTextField.setPreferredSize(new Dimension(100, pageTextField.getPreferredSize().height));
		pageTextField.setMinimumSize(new Dimension(80, pageTextField.getPreferredSize().height));		
		
		pageButton.setPreferredSize(new Dimension(60, pageTextField.getPreferredSize().height));
		
		pageButton.setMinimumSize(new Dimension(40, pageTextField.getPreferredSize().height));
		pageButton.addActionListener(this);		
				
		pagePanel.add(pageTextField, "0,1,r,c");
		pagePanel.add(pageButton, "1,1,l,c");
		
		
		return pagePanel;
	}
	protected Component createLegendPanel() {
		
		SlickerFactory factory = SlickerFactory.instance();

		JPanel legendPanel = new JPanel();
		legendPanel.setBorder(BorderFactory.createEmptyBorder());
		legendPanel.setBackground(Color.white);//new Color(192, 192, 192));
		//TableLayout layout = new TableLayout(new double[][] { { TableLayout.FILL }, {} });
		//TableLayout layout = new TableLayout(new double[][] { { 0.10, TableLayout.FILL}, {} });
		TableLayout layout = new TableLayout(new double[][] { { TableLayout.FILL }, {0.5, 0.5}});
		layout.setVGap(1);
		
		legendPanel.setLayout(layout);

		layout.insertRow(0, 0.2);

		int row = 1;
        //Pattern1
		layout.insertRow(row, TableLayout.PREFERRED);
		JLabel legend = SlickerFactory.instance().createLabel("Basic Patterns");
		legendPanel.add(legend, "0,1,1,1,c, c");
		row++;

		layout.insertRow(row, 1);

		layout.insertRow(row, TableLayout.FILL);
		JPanel greenPanel = new JPanel(){
			protected void paintComponent(Graphics g2d) {
				int x = shiftX; 
				int width = 30;
				int y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				
				//privacy layer
				
				g2d.setColor(Color.green);
				g2d.fillOval(x + 38 , y+5, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 38 , y+5, 16, 16);
				
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				g2d.setColor(Color.blue);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				
				int[] xCoords2 = new int[] { x+30, x + width+30-2, midPointAX+30, x + width+30-2, x+30, midPointBX+30 };
				int[] yCoords2 = new int[] { y+5, y+5, midPointY, y + height-5, y + height-5, midPointY };
				
				
				
				g2d.setColor(Color.green);
				g2d.fillPolygon(xCoords2, yCoords2, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2,6);
				
				//
				int[] xCoords3 = new int[] { x+30+30, x + width+30+30-2, midPointAX+30+30, x + width+30+30-2, x+30+30, midPointBX+30+30 };
				int[] yCoords3 = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				
				
				g2d.setColor(Color.blue);
				g2d.fillPolygon(xCoords3, yCoords3, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords3, yCoords3,6);	
				
			}
		};
		greenPanel.setPreferredSize(new Dimension(iconSize, 90));
		greenPanel.setSize(new Dimension(iconSize,90));
		legendPanel.add(greenPanel, "0," + row + ",r, c");
		JLabel TSM = factory.createLabel(" -Expected data operation in the context of activity X");
		legendPanel.add(TSM, "1," + row++ + ",l, c");
        //pattern2
		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel orangePanel = new JPanel(){
			protected void paintComponent(Graphics g2d) {
				int x = shiftX; 
				int width = 30;
				int y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				Color pColorMM= new Color(102, 0 ,153);
				//privacy layer
				
				g2d.setColor( pColorMM);
				g2d.fillOval(x + 38 , y+5, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 38 , y+5, 16, 16);
				
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				g2d.setColor(Color.blue);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				
				int[] xCoords2 = new int[] { x+30, x + width+30-2, midPointAX+30, x + width+30-2, x+30, midPointBX+30 };
				int[] yCoords2 = new int[] { y+5, y+5, midPointY, y + height-5, y + height-5, midPointY };
				
				
				
				g2d.setColor( pColorMM);
				g2d.fillPolygon(xCoords2, yCoords2, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2,6);
				
				//
				int[] xCoords3 = new int[] { x+30+30, x + width+30+30-2, midPointAX+30+30, x + width+30+30-2, x+30+30, midPointBX+30+30 };
				int[] yCoords3 = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				
				
				g2d.setColor(Color.blue);
				g2d.fillPolygon(xCoords3, yCoords3, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords3, yCoords3,6);		
				
			}
		};
		orangePanel.setPreferredSize(new Dimension(iconSize, 90));
		orangePanel.setSize(new Dimension(iconSize, 90));
		legendPanel.add(orangePanel, "0," + row + ",r, c");		
		
		JLabel TSMP = factory.createLabel(" -Ignored data operation in the context of activity X");
		legendPanel.add(TSMP, "1," + row++ + ",l, c");
        //pattern3
		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel pinkPanel = new JPanel(){
			protected void paintComponent(Graphics g2d) {
				int x = shiftX; 
				int width = 30;
				int y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
			
				//privacy layer
				
				g2d.setColor(Color.orange);
				g2d.fillOval(x + 38 , y+5, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 38 , y+5, 16, 16);
				
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				g2d.setColor(Color.cyan);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				
				int[] xCoords2 = new int[] { x+30, x + width+30-2, midPointAX+30, x + width+30-2, x+30, midPointBX+30 };
				int[] yCoords2 = new int[] { y+5, y+5, midPointY, y + height-5, y + height-5, midPointY };
				
				
				
				g2d.setColor(Color.orange);
				g2d.fillPolygon(xCoords2, yCoords2, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2,6);
				
				//
				int[] xCoords3 = new int[] { x+30+30, x + width+30+30-2, midPointAX+30+30, x + width+30+30-2, x+30+30, midPointBX+30+30 };
				int[] yCoords3 = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				
				
				g2d.setColor(Color.cyan);
				g2d.fillPolygon(xCoords3, yCoords3, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords3, yCoords3,6);		
				
			}
		};
		pinkPanel.setPreferredSize(new Dimension(iconSize, 90));
		pinkPanel.setSize(new Dimension(iconSize, 90));
		legendPanel.add(pinkPanel, "0," + row + ",r, c");
		
		JLabel PSM = factory.createLabel(" -Illegal data operation in the context of activity X");
		legendPanel.add(PSM, "1," + row++ + ",l, c");
        //pattern4
		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel purplePanel = new JPanel(){
			protected void paintComponent(Graphics g2d) {
				int x = shiftX; 
				int width = 30;
				int y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				Color pColorMM= new Color(102, 0 ,153);
				//privacy layer
				
				g2d.setColor(pColorMM);
				g2d.fillOval(x + 38 , y+5, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 38 , y+5, 16, 16);
				
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				g2d.setColor(Color.cyan);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				
				int[] xCoords2 = new int[] { x+30, x + width+30-2, midPointAX+30, x + width+30-2, x+30, midPointBX+30 };
				int[] yCoords2 = new int[] { y+5, y+5, midPointY, y + height-5, y + height-5, midPointY };
				
				
				
				g2d.setColor(pColorMM);
				g2d.fillPolygon(xCoords2, yCoords2, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2,6);
				
				//
				int[] xCoords3 = new int[] { x+30+30, x + width+30+30-2, midPointAX+30+30, x + width+30+30-2, x+30+30, midPointBX+30+30 };
				int[] yCoords3 = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				
				
				g2d.setColor(Color.cyan);
				g2d.fillPolygon(xCoords3, yCoords3, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords3, yCoords3,6);	
				
			}
		};
		purplePanel.setPreferredSize(new Dimension(iconSize, 90));
		purplePanel.setSize(new Dimension(iconSize, 90));
		legendPanel.add(purplePanel, "0," + row + ",r, c");
		JLabel PSMP = factory.createLabel(" -Ignored data operation by an illegitimate user in the context of activity X");
		legendPanel.add(PSMP, "1," + row++ + ",l, c");
		
		//pattern5
		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel bluePanel = new JPanel(){
			protected void paintComponent(Graphics g2d) {
				int x = shiftX; 
				int width = 30;
				int y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				Color pColorMM= new Color(102, 0 ,153);
				//privacy layer
				
				g2d.setColor(pColorMM);
				g2d.fillOval(x + 38 , y+5, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 38 , y+5, 16, 16);
				
				
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				g2d.setColor(pColorMM);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				
				int[] xCoords2 = new int[] { x+30, x + width+30-2, midPointAX+30, x + width+30-2, x+30, midPointBX+30 };
				int[] yCoords2 = new int[] { y+5, y+5, midPointY, y + height-5, y + height-5, midPointY };
				
				
				
				g2d.setColor(pColorMM);
				g2d.fillPolygon(xCoords2, yCoords2, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2,6);
				
				//
				int[] xCoords3 = new int[] { x+30+30, x + width+30+30-2, midPointAX+30+30, x + width+30+30-2, x+30+30, midPointBX+30+30 };
				int[] yCoords3 = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				
				
				g2d.setColor(pColorMM);
				g2d.fillPolygon(xCoords3, yCoords3, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords3, yCoords3,6);	
				
			}
		};
		 bluePanel.setPreferredSize(new Dimension(iconSize, 90));
		 bluePanel.setSize(new Dimension(iconSize, 90));
		legendPanel.add( bluePanel, "0," + row + ",r, c");
		JLabel MM = factory.createLabel(" -Skipped Mandatory data operation and activity X the role R");
		legendPanel.add(MM, "1," + row++ + ",l, c");
		//pattern6
		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel yellowPanel = new JPanel(){
			protected void paintComponent(Graphics g2d) {
				int x = shiftX; 
				int width = 30;
				int y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				
				//privacy layer
				/*
				g2d.setColor(Color.green);
				g2d.fillOval(x + 38 , y+5, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 38 , y+5, 16, 16);
				
				*/
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				g2d.setColor(Color.yellow);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				
				//Data Layer
				
				int[] xCoords2 = new int[] { x+30, x + width+30-2, midPointAX+30, x + width+30-2, x+30, midPointBX+30 };
				int[] yCoords2 = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				
				
				g2d.setColor(Color.yellow);
				g2d.fillPolygon(xCoords2, yCoords2, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2,6);
				
				//
				/*
				int[] xCoords3 = new int[] { x+30+30, x + width+30+30-2, midPointAX+30+30, x + width+30+30-2, x+30+30, midPointBX+30+30 };
				int[] yCoords3 = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				
				
				g2d.setColor(Color.yellow);
				g2d.fillPolygon(xCoords3, yCoords3, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords3, yCoords3,6);
				*/
			}
		};
		yellowPanel.setPreferredSize(new Dimension(iconSize, 90));
		yellowPanel.setSize(new Dimension(iconSize, 90));
		legendPanel.add(yellowPanel, "0," + row + ",r, c");
		JLabel PLM = factory.createLabel(" -Unexpected activity X");
		legendPanel.add(PLM, "1," + row++ + ",l, c");
		//pattern7
		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel redPanel = new JPanel(){
			protected void paintComponent(Graphics g2d) {
				int x = shiftX; 
				int width = 30;
				int y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				
				//privacy layer
				
				g2d.setColor(Color.red);
				g2d.fillOval(x + 38 , y+5, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 38 , y+5, 16, 16);
				
				
				//process layer
				
				
				/*
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				g2d.setColor(Color.blue);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				*/
				//Data Layer
				midPointY+= 18;
				y+= 18;
				int[] xCoords2 = new int[] { x+30, x + width+30-2, midPointAX+30, x + width+30-2, x+30, midPointBX+30 };
				int[] yCoords2 = new int[] { y+5, y+5, midPointY, y + height-5, y + height-5, midPointY };
				
				
				
				g2d.setColor(Color.red);
				g2d.fillPolygon(xCoords2, yCoords2, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2,6);
				
				//
				/*
				int[] xCoords3 = new int[] { x+30+30, x + width+30+30-2, midPointAX+30+30, x + width+30+30-2, x+30+30, midPointBX+30+30 };
				int[] yCoords3 = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				
				
				g2d.setColor(Color.blue);
				g2d.fillPolygon(xCoords3, yCoords3, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords3, yCoords3,6);
				*/
			}
		};
		redPanel.setPreferredSize(new Dimension(iconSize, 60));
		redPanel.setSize(new Dimension(iconSize, 60));
		legendPanel.add(redPanel, "0," + row + ",r, c");
		JLabel DLM = factory.createLabel(" -Spurious data operation (without clear context)");
		legendPanel.add(DLM, "1," + row++ + ",l, c");
		
		return legendPanel;
	}
	protected Color attenuateColor(Color color) {
		int red = (int) (color.getRed() * 0.8);
		int green = (int) (color.getGreen() * 0.8);
		int blue = (int) (color.getBlue() * 0.8);
		return new Color(red, green, blue);
	}
	private int addLegendPanel(TableLayout layout, int row, JPanel legendPanel, final String label, Color color){
		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel greenPanel = new JPanel(){
			protected void paintComponent(Graphics g2d) {
				if(label.equalsIgnoreCase("Synchronous Data Move")){
					int x = shiftX, width = getWidth() - shiftX, y = 0, height = getWidth() - shiftX;
					int[] xCoords = new int[] { x, x + width / 2 , x + width, x + width / 2 };
					int[] yCoords = new int[] { y + height / 2, y, y + (height / 2), y + height};
					
					int[] xCoords2 = new int[] { x - shiftX, x - shiftX + width/2, x + width - shiftX, x - shiftX + width/2 };
					int[] yCoords2 = new int[] { y + height / 2, y, y + height / 2, y + height};
					g2d.setColor(attenuateColor(MultiLayerAlignmentConstantsWithContext.MOVEDATALOGCOLOR));
					g2d.fillPolygon(xCoords2, yCoords2, 4);
					g2d.setColor(Color.black);
					g2d.drawPolygon(xCoords2, yCoords2, 4);
					g2d.setColor(attenuateColor(MultiLayerAlignmentConstantsWithContext.MOVEMODELCOLOR));
					g2d.fillPolygon(xCoords, yCoords, 4);
					g2d.setColor(Color.black);
					g2d.drawPolygon(xCoords, yCoords, 4);					
				}else if(label.equalsIgnoreCase("Data Move on Model")){
					int x = shiftX/2, width = getWidth() - shiftX, y = 0, height = getWidth() - shiftX;
					
					int midPointBX = x + elementTriOffset;
					int midPointAX = x + width + elementTriOffset;
					int midPointY = y + (height / 2);
					
					int[] xCoords1 = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
					int[] yCoords1 = new int[] { y, y, midPointY, y + height, y + height, midPointY };
					
					g2d.setColor(attenuateColor(MultiLayerAlignmentConstantsWithContext.MOVEPROCESSLOGCOLOR));
					g2d.fillPolygon(xCoords1, yCoords1, 6);
					g2d.setColor(Color.black);
					g2d.drawPolygon(xCoords1, yCoords1, 6);
					
					y+= 100;
					int[] xCoords = new int[] { x, x + width/2, x + width, x + width/2 };
					int[] yCoords = new int[] { y + height / 2, y, y + height / 2, y + height};
					
					
					
					g2d.setColor(attenuateColor(MultiLayerAlignmentConstantsWithContext.MOVEPARTIALLYCOLOR));
					g2d.fillPolygon(xCoords, yCoords, 4);
					g2d.setColor(Color.black);
					g2d.drawPolygon(xCoords, yCoords, 4);
				}else if(label.equalsIgnoreCase("Data Move on Log")){
					int x = shiftX/2, width = getWidth() - shiftX, y = 0, height = getWidth() - shiftX;
					
					int[] xCoords2 = new int[] { x, x + width/2, x + width, x  + width/2 };
					int[] yCoords2 = new int[] { y + height / 2, y, y + height / 2, y + height};
					g2d.setColor(attenuateColor(MultiLayerAlignmentConstantsWithContext.MOVEPARTIALLYT2COLOR));
					g2d.fillPolygon(xCoords2, yCoords2, 4);	
					g2d.setColor(Color.black);
					g2d.drawPolygon(xCoords2, yCoords2, 4);
				}
			}
	
		};
		//greenPanel.setPreferredSize(new Dimension(iconSize, iconSize));
		//greenPanel.setSize(new Dimension(iconSize, iconSize));
		greenPanel.setPreferredSize(new Dimension(40, 40));
		greenPanel.setSize(new Dimension(40, 40));
		
		legendPanel.add(greenPanel, "0," + row + ",r, c");
		JLabel syncLbl = SlickerFactory.instance().createLabel(label);
		legendPanel.add(syncLbl, "1," + row++ + ",l, c");
		
		
		return row;
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JComboBox){
			JComboBox<String> cb =  (JComboBox<String>) e.getSource();
	        switch(cb.getSelectedIndex()){
	        	case 0:
	        		allAlignmentPanel.repaint();
	        		break;
	        	case 1:
	        		allAlignmentPanel.repaint();
	        		break;
	        }
		}else if(e.getSource() instanceof JButton){
			progress.setValue(0);
			selectedPage = Integer.parseInt(pageTextField.getText());
			allAlignmentPanel.removeAll();
			addInterLevelAlignments();
			alignmentOanemWithScrollBar.revalidate();
		}
	}
	public ProMComboBox<String> getLinkageComboBox(){
		return interLevelLinkageComboBox;
	}
}
