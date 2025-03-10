package org.processmining.multilayeralignment.plugins.visualization.sequence;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import org.processmining.multilayeralignment.plugins.visualization.tracealignment.MultiLayerAlignmentConstants;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerScrollBarUI;

import info.clearthought.layout.TableLayout;

public class MLAWithContextResultDLVisualizationPanel  extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = -5897513989508944234L;

	/**
	 * Pointers to property variable
	 */
	protected static int RELIABLEMIN = 0;
	protected static int RELIABLEMAX = 1;
	protected static int MIN = 2;
	protected static int MAX = 3;


	protected static int SVAL = 4;
	protected static int MVAL = 6;
	protected static int SVALRELIABLE = 5;
	protected static int MVALRELIABLE = 7;
	protected static int PERFECTCASERELIABLECOUNTER = 8;
	
	private Color bgColor = new Color(192,192,192); 
	protected int iconSize = 40 , shiftX = 4, shiftY = 2, elementTriOffset = 4;
	

	protected int numReliableCaseInvolved = 0; 
	

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
	protected XLog inDataLog;
	protected ScrollBar alignmentOanemWithScrollBar;

	protected final DefaultTableModel reliableCasesTModel = new DefaultTableModel() {
		private static final long serialVersionUID = -4303950078200984098L;

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	};

	
	public MLAWithContextResultDLVisualizationPanel (MLAWithContextReplayResult replayResult,XLog inDataLog, Progress progress) {
		this.replayResult = replayResult;
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
		
		String link [] = {"Projection to Data log"};
		interLevelLinkageComboBox = new ProMComboBox<String>(link);
		interLevelLinkageComboBox.addActionListener(this);
		interLevelLinkageComboBox.setMaximumSize(new Dimension(270, 40));
		interLevelLinkageComboBox.setPreferredSize(new Dimension(200, interLevelLinkageComboBox.getPreferredSize().height));
		interLevelLinkageComboBox.setMinimumSize(new Dimension(200, interLevelLinkageComboBox.getPreferredSize().height));
		

		addMultiLayerAlignments();
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
		JLabel linkLabel = SlickerFactory.instance().createLabel("Selected visualization:");
		statisticPanel.add(linkLabel, "0, " + lineNumberRight++ + ", c, t");
		statisticPanel.add(interLevelLinkageComboBox, "0, " + lineNumberRight++ + ", c, t");	
		// Page
		statisticPanel.add(createPagePanel(), "0, " + lineNumberRight++ + ", 0, " + lineNumberRight++ + ", c, t");	
		
		// MAIN PANEL
		ProMSplitPane splitPanel = new ProMSplitPane(ProMSplitPane.HORIZONTAL_SPLIT);
		splitPanel.setBorder(BorderFactory.createEmptyBorder());
		splitPanel.setOneTouchExpandable(true);
		
		alignmentOanemWithScrollBar = new ScrollBar(allAlignmentPanel);
		splitPanel.setLeftComponent(alignmentOanemWithScrollBar);
		splitPanel.setRightComponent(statisticPanel);
		statisticPanel.setBackground(Color.white);
		splitPanel.setResizeWeight(1.0);	
		return splitPanel;
	}
	protected void addMultiLayerAlignments(){
		for (int i = (selectedPage - 1) * numOfalignmentsPerPage; i < Math.min(selectedPage * numOfalignmentsPerPage, replayResult.size()); i++) {
			SyncReplayResult res = replayResult.get(i);
			if (progress != null) {
				progress.inc();
			}
			List<Object> nodeInstance = res.getNodeInstance();
			List<StepTypes> stepTypes = res.getStepTypes();
			List<Object> nodeInstanceVOL = res.getNodeInstanceVOL();
			List<StepTypesVOL> stepTypesVOL = res.getStepTypesVOL();
			int traceIndexInteger= res.getTraceIndex().first();
			XTrace dataTrace= inDataLog.get(i);

			String traceIndex= res.getTraceIndex().first().toString();
			List<Object> rearrangedNodeInstance = res.getRearrangedNodeInstance();
			// ALIGNMENT PANEL
			MLAWithContextInstanceDLConformanceView alignmentPanel = createMLAWithContextInstanceDLView(traceIndex,nodeInstance,stepTypes,nodeInstanceVOL,stepTypesVOL,rearrangedNodeInstance,dataTrace);
					

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
	
	protected MLAWithContextInstanceDLConformanceView createMLAWithContextInstanceDLView(String traceIndex,List<Object> nodeInstance, List<StepTypes> stepTypes, List<Object> nodeInstanceVOL, List<StepTypesVOL> stepTypesVOL,List<Object> rearrangedNodeInstances, XTrace dataTrace) {
		return new MLAWithContextInstanceDLConformanceView(traceIndex,nodeInstance,stepTypes,nodeInstanceVOL,stepTypesVOL,rearrangedNodeInstances,40, dataTrace);
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
		TableLayout layout = new TableLayout(new double[][] { { 0.10, TableLayout.FILL }, {} });
		layout.setVGap(1);
		
		legendPanel.setLayout(layout);

		layout.insertRow(0, 0.2);

		int row = 1;

		layout.insertRow(row, TableLayout.PREFERRED);
		JLabel legend = SlickerFactory.instance().createLabel("LEGEND");
		legendPanel.add(legend, "0,1,1,1,c, c");
		row++;

		layout.insertRow(row, 1);

		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel greenPanel = new JPanel(){
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				int x = shiftX, width = getWidth() - shiftX - 2 - elementTriOffset, y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				
				//privacy layer
				g2d.setColor(Color.green);
				g2d.fillOval(x + 7 , y, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 7 , y, 16, 16);
				int y1=y+17;
				//process layer
				midPointY+= 18;
				y+= 18;

				//Data Layer
				y+= 25;
				int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
		        
				g2d.setColor(Color.green);
				g2d.fillPolygon(xCoords2, yCoords2, 3);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2, 3);	
				//drawLine
				g2d.setColor(Color.green);
				g2d.drawLine(x + width / 2, y1, x + width / 2, y);
				
			}
		};
		greenPanel.setPreferredSize(new Dimension(iconSize, 90));
		greenPanel.setSize(new Dimension(iconSize,90));
		legendPanel.add(greenPanel, "0," + row + ",r, c");
		JLabel TSM = factory.createLabel(" - Expected data operation with clear context was done by a legitimate role");
		legendPanel.add(TSM, "1," + row++ + ",l, c");
		

		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel orangePanel = new JPanel(){
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				int x = shiftX, width = getWidth() - shiftX - 2 - elementTriOffset, y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				
				//privacy layer
				g2d.setColor(Color.red);
				g2d.fillOval(x + 7 , y, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 7 , y, 16, 16);
				int y1=y+17;
				//process layer
				midPointY+= 18;
				y+= 18;

				//Data Layer
				y+= 25;
				int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
		        
				g2d.setColor(Color.green);
				g2d.fillPolygon(xCoords2, yCoords2, 3);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2, 3);	
				//drawLine
				g2d.setColor(Color.green);
				g2d.drawLine(x + width / 2, y1, x + width / 2, y);
				
			}
		};
		orangePanel.setPreferredSize(new Dimension(iconSize, 90));
		orangePanel.setSize(new Dimension(iconSize, 90));
		legendPanel.add(orangePanel, "0," + row + ",r, c");		
		//
		JLabel TSMP = factory.createLabel(" - Expected data operation with clear context was done by an illegitimate role");
		legendPanel.add(TSMP, "1," + row++ + ",l, c");
		
		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel redPanel = new JPanel(){
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				int x = shiftX, width = getWidth() - shiftX - 2 - elementTriOffset, y = 0, height = 40 - shiftY - 1;
				
				//privacy layer
				g2d.setColor(Color.red);
				g2d.fillOval(x + 7 , y, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 7 , y, 16, 16);
				int y1=y+17;
				
				
				y+= 18;
				y+= 25;
				//Data Layer
				
				int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
		        
				g2d.setColor(Color.red);
				g2d.fillPolygon(xCoords2, yCoords2, 3);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2, 3);
				
				//drawLine
				g2d.setPaint(Color.red);
				g2d.drawLine(x + width / 2, y1, x + width / 2, y);
			}
		};
		redPanel.setPreferredSize(new Dimension(iconSize, 90));
		redPanel.setSize(new Dimension(iconSize, 90));
		legendPanel.add(redPanel, "0," + row + ",r, c");
		JLabel DLM = factory.createLabel(" - Unexpected data operation with unclear context was done by an illegitimate role");
		legendPanel.add(DLM, "1," + row++ + ",l, c");

		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel pinkPanel = new JPanel(){
		
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				int x = shiftX, width = getWidth() - shiftX - 2 - elementTriOffset, y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				
				//privacy layer
				g2d.setColor(Color.green);
				g2d.fillOval(x +7 , y, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 7 , y, 16, 16);
				int y1=y+17;
				//process layer
				midPointY+= 18;
				y+= 18;
				/*
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				g2d.setColor(Color.green);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				*/
				//Data Layer
				GradientPaint redtowhite2 = new GradientPaint(25, 25,Color.red, 15, 25, Color.white, true);
				y+= 25;
				int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};

                g2d.setPaint(redtowhite2);
				//g2d.setColor(Color.red);
				g2d.fillPolygon(xCoords2, yCoords2, 3);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2, 3);
				
				//drawLine
				g2d.setColor(Color.green);
				g2d.drawLine(x + width / 2, y1, x + width / 2, y);
				
			}
		};
		pinkPanel.setPreferredSize(new Dimension(iconSize, 90));
		pinkPanel.setSize(new Dimension(iconSize, 90));
		legendPanel.add(pinkPanel, "0," + row + ",r, c");
		//PSM
		JLabel PSM = factory.createLabel(" - Ignored data operation -Category1 (expected activity was done by a legitimate role)");
		legendPanel.add(PSM, "1," + row++ + ",l, c");

		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel purplePanel = new JPanel(){
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				int x = shiftX, width = getWidth() - shiftX - 2 - elementTriOffset, y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				
				//privacy layer
				g2d.setColor(Color.red);
				g2d.fillOval(x +7 , y, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 7 , y, 16, 16);
				int y1=y+17;
				//process layer
				midPointY+= 18;
				y+= 18;
				/*
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				
				g2d.setColor(Color.green);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				*/
				//Data Layer
				y+= 25;
				int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
				GradientPaint redtowhite2 = new GradientPaint(25, 25,Color.red, 15, 25, Color.white, true);
				g2d.setPaint(redtowhite2);
				//g2d.setColor(Color.red);
				g2d.fillPolygon(xCoords2, yCoords2, 3);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2, 3);	
				//drawLine
				g2d.setColor(Color.green);
				g2d.drawLine(x + width / 2, y1, x + width / 2, y);
				//drawLine
				g2d.setColor(Color.green);
				g2d.drawLine(x + width / 2, y1, x + width / 2, y);
				
			}
		};
		purplePanel.setPreferredSize(new Dimension(iconSize, 90));
		purplePanel.setSize(new Dimension(iconSize, 90));
		legendPanel.add(purplePanel, "0," + row + ",r, c");
		//PSMP
		JLabel PSMP = factory.createLabel(" - Ignored data operation- Category2 (expected activity was done by an illegitimate role)");
		legendPanel.add(PSMP, "1," + row++ + ",l, c");
		
		
		layout.insertRow(row, TableLayout.PREFERRED);
		JPanel bluePanel = new JPanel(){
			protected void paintComponent(Graphics g) {
				Graphics2D g2d = (Graphics2D) g;
				int x = shiftX, width = getWidth() - shiftX - 2 - elementTriOffset, y = 0, height = 40 - shiftY - 1;
				int midPointBX = x + elementTriOffset;
				int midPointAX = x + width + elementTriOffset;
				int midPointY = y + (height / 2);
				GradientPaint redtowhite2 = new GradientPaint(25, 25,Color.red, 15, 25, Color.white, true);
				//privacy layer
				g2d.setPaint(redtowhite2);
				//g2d.setColor(Color.red);
				g2d.fillOval(x + 7 , y, 16, 16);
				g2d.setColor(Color.black);
				g2d.drawOval(x + 7 , y, 16, 16);
				int y1=y+17;
				//process layer
				midPointY+= 18;
				y+= 18;
				
				int[] xCoords = new int[] { x, x + width, midPointAX, x + width, x, midPointBX };
				int[] yCoords = new int[] { y, y, midPointY, y + height, y + height, midPointY };
				/*
				g2d.setColor(Color.red);
				g2d.fillPolygon(xCoords, yCoords, 6);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords, yCoords, 6);
				*/
				//Data Layer
				y+= 25;
				int[] xCoords2 = new int[] { x, x + width / 2 , x + width, };
				int[] yCoords2 = new int[] { y + height / 2, y, y + (height / 2)};
				
				g2d.setPaint(redtowhite2);
				//g2d.setColor(Color.red);
				g2d.fillPolygon(xCoords2, yCoords2, 3);
				g2d.setColor(Color.black);
				g2d.drawPolygon(xCoords2, yCoords2, 3);	
				//drawLine
				g2d.setPaint(redtowhite2);
				g2d.drawLine(x + width / 2, y1, x + width / 2, y);
				
			}
		};
		 bluePanel.setPreferredSize(new Dimension(iconSize, 90));
		 bluePanel.setSize(new Dimension(iconSize, 90));
		legendPanel.add( bluePanel, "0," + row + ",r, c");
		JLabel MM = factory.createLabel(" - Missing data operation");
		legendPanel.add(MM, "1," + row++ + ",l, c");
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
					g2d.setColor(attenuateColor(MultiLayerAlignmentConstants.MOVEDATALOGCOLOR));
					g2d.fillPolygon(xCoords2, yCoords2, 4);
					g2d.setColor(Color.black);
					g2d.drawPolygon(xCoords2, yCoords2, 4);
					g2d.setColor(attenuateColor(MultiLayerAlignmentConstants.MOVEMODELCOLOR));
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
					
					g2d.setColor(attenuateColor(MultiLayerAlignmentConstants.MOVEPROCESSLOGCOLOR));
					g2d.fillPolygon(xCoords1, yCoords1, 6);
					g2d.setColor(Color.black);
					g2d.drawPolygon(xCoords1, yCoords1, 6);
					
					y+= 100;
					int[] xCoords = new int[] { x, x + width/2, x + width, x + width/2 };
					int[] yCoords = new int[] { y + height / 2, y, y + height / 2, y + height};
					
					
					
					g2d.setColor(attenuateColor(MultiLayerAlignmentConstants.MOVEPARTIALLYCOLOR));
					g2d.fillPolygon(xCoords, yCoords, 4);
					g2d.setColor(Color.black);
					g2d.drawPolygon(xCoords, yCoords, 4);
				}else if(label.equalsIgnoreCase("Data Move on Log")){
					int x = shiftX/2, width = getWidth() - shiftX, y = 0, height = getWidth() - shiftX;
					
					int[] xCoords2 = new int[] { x, x + width/2, x + width, x  + width/2 };
					int[] yCoords2 = new int[] { y + height / 2, y, y + height / 2, y + height};
					g2d.setColor(attenuateColor(MultiLayerAlignmentConstants.MOVEPARTIALLYT2COLOR));
					g2d.fillPolygon(xCoords2, yCoords2, 4);	
					g2d.setColor(Color.black);
					g2d.drawPolygon(xCoords2, yCoords2, 4);
				}
			}
	
		};
		greenPanel.setPreferredSize(new Dimension(iconSize, iconSize));
		greenPanel.setSize(new Dimension(iconSize, iconSize));
		
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
			addMultiLayerAlignments();
			alignmentOanemWithScrollBar.revalidate();
		}
	}
	public ProMComboBox<String> getLinkageComboBox(){
		return interLevelLinkageComboBox;
	}
}
