package corrProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;

import corr.*;


public class AnscombApplet extends XApplet {
	static final private String X_AXIS_INFO_PARAM = "horizAxis";
	static final private String Y_AXIS_INFO_PARAM = "vertAxis";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String EXTRA_DECIMALS_PARAM = "decimals";
	static final private String DATASET_NAMES_PARAM = "dataSetNames";
	
	private DataSet data;
	
	private HorizAxis theHorizAxis;
	private VertAxis theVertAxis;
	private ScatterView theView;
	
	private XChoice dataSetChoice;
	private int currentDataSet;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout());
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add("Center", displayPanel(data));
		mainPanel.add("South", controlPanel(data));
		mainPanel.add("North", yLabelPanel(data));
		
		add("Center", mainPanel);
		add("East", summaryPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM + "1"));
		data.addNumVariable("x", getParameter(X_VAR_NAME_PARAM), getParameter(X_VALUES_PARAM + "1"));
		currentDataSet = 0;
		return data;
	}
	
	protected XPanel yLabelPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		XLabel yVariateName = new XLabel(getParameter(Y_VAR_NAME_PARAM), XLabel.LEFT, this);
		yVariateName.setFont(theVertAxis.getFont());
		thePanel.add(yVariateName);
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			theHorizAxis = createHorizAxis(data);
		thePanel.add("Bottom", theHorizAxis);
		
			theVertAxis = createVertAxis(data);
		thePanel.add("Left", theVertAxis);
		
			theView = new ScatterView(data, this, theHorizAxis, theVertAxis, "x", "y");
			theView.lockBackground(Color.white);
			theView.setCrossSize(DataView.LARGE_CROSS);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		HorizAxis axis;
		axis = new HorizAxis(this);
		
		String labelInfo = getParameter(X_AXIS_INFO_PARAM);
		axis.readNumLabels(labelInfo);
		return axis;
	}
	
	protected VertAxis createVertAxis(DataSet data) {
		VertAxis axis;
		axis = new VertAxis(this);
		String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
		axis.readNumLabels(labelInfo);
		return axis;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
//			XLabel label = new XLabel("Data set : ", XLabel.RIGHT, this);
//		thePanel.add(label);
			
			StringTokenizer st = new StringTokenizer(getParameter(DATASET_NAMES_PARAM), "#");
			dataSetChoice = new XChoice(this);
			for (int i=1 ; i<=4 ; i++)
				dataSetChoice.addItem(st.nextToken());
			dataSetChoice.select(0);
		thePanel.add(dataSetChoice);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(DataSet data) {
//		String extraDecimals = getParameter(EXTRA_DECIMALS_PARAM);
		StringTokenizer theExtras = new StringTokenizer(getParameter(EXTRA_DECIMALS_PARAM));
		int xMeanExtra = Integer.parseInt(theExtras.nextToken());
		int yMeanExtra = Integer.parseInt(theExtras.nextToken());
		int xSDExtra = Integer.parseInt(theExtras.nextToken());
		int ySDExtra = Integer.parseInt(theExtras.nextToken());
		int corrDecs = Integer.parseInt(theExtras.nextToken());
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 8));
		thePanel.add(new MeanView(data, "x", MeanView.DRAW_FORMULA, xMeanExtra, this));
		thePanel.add(new MeanView(data, "y", MeanView.DRAW_FORMULA, yMeanExtra, this));
		thePanel.add(new StDevnView(data, "x", MeanView.DRAW_FORMULA, xSDExtra, this));
		thePanel.add(new StDevnView(data, "y", MeanView.DRAW_FORMULA, ySDExtra, this));
		thePanel.add(new CorrelationView(data, "x", "y", CorrelationView.NO_FORMULA, corrDecs, this));
		
		return thePanel;
	}
	
//	static final private String[] keys = {"x", "y"};
	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			int newDataSet = dataSetChoice.getSelectedIndex();
			if (newDataSet != currentDataSet) {
				currentDataSet = newDataSet;
				NumVariable x = (NumVariable)data.getVariable("x");
				NumVariable y = (NumVariable)data.getVariable("y");
				x.readValues(getParameter(X_VALUES_PARAM + (newDataSet + 1)));
				y.readValues(getParameter(Y_VALUES_PARAM + (newDataSet + 1)));
				
//				ChangedVariableThread.markChanged(data, keys);
				synchronized (data) {
					data.variableChanged("x");
					data.variableChanged("y");
					theView.repaint();				//		repaint() should not be needed, but seems
															//		to be (sometimes) on the Mac at least.
				}
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}