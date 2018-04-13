package samplingProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import coreGraphics.*;

import sampling.*;

public class NormalIntervalApplet extends XApplet {
	static final private String HORIZ_AXIS_PARAM = "horizAxis";
	static final private String PROB_AXIS_PARAM = "probAxis";
	static final private String DISTN_NAME_PARAM = "distnName";
	
	static final private String Z_DISTN_KEY = "z";
	
	private String distnName[];
	private String axisLabels[];
	private int currentExample = 0;
	
	private HorizAxis theHorizAxis;
	private XChoice popnChoice;
	
	public void setupApplet() {
		DataSet data = getData();
		readAxes();
		setLayout(new BorderLayout());
		
		add("Center", displayPanel(data));
		add("East", choicePanel());
		add("South", probPanel(data));
	}
	
	private void readAxes() {
		int noOfAxes = 0;
		while (true) {
			if (getParameter(DISTN_NAME_PARAM + noOfAxes) == null)
				break;
			noOfAxes ++;
		}
		
		distnName = new String[noOfAxes];
		axisLabels = new String[noOfAxes];
		
		for (int i=0 ; i<noOfAxes ; i++) {
			distnName[i] = getParameter(DISTN_NAME_PARAM + i);
			axisLabels[i] = getParameter(HORIZ_AXIS_PARAM + i);
		}
		
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		NormalDistnVariable z = new NormalDistnVariable("Z");
		z.setParams("0.0 1.0");
		data.addVariable(Z_DISTN_KEY, z);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
//		NormalDistnVariable y = (NormalDistnVariable)data.getVariable(Z_DISTN_KEY);
		
		HorizAxis stdAxis = new HorizAxis(this);
		stdAxis.readNumLabels(StandardisingAxis.kStandardisedLabelInfo);
		stdAxis.setAxisName("Z");
		stdAxis.setForeground(Color.blue);
		thePanel.add("Bottom", stdAxis);
		
		theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(axisLabels[currentExample]);
		theHorizAxis.setAxisName(distnName[currentExample]);
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theProbAxis = new VertAxis(this);
		String labelInfo = getParameter(PROB_AXIS_PARAM);
		theProbAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theProbAxis);
		
		DistnDensityView theView = new DistnDensityView(data, this, stdAxis, theProbAxis, Z_DISTN_KEY,
																			DistnDensityView.NO_SHOW_MEANSD, DistnDensityView.SYMMETRIC_DRAG);
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel probPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		PlusMinusProportionView pView = new PlusMinusProportionView(data, Z_DISTN_KEY, this);
		thePanel.add(pView);
		
		return thePanel;
	}
	
	private XPanel choicePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		thePanel.add(new XLabel(translate("Population") + ": ", XLabel.CENTER, this));
		popnChoice = new XChoice(this);
		for (int i=1 ; i<=distnName.length ; i++)
			popnChoice.addItem(translate("Example") + " " + i);
		popnChoice.select(currentExample);
		thePanel.add(popnChoice);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == popnChoice) {
			int newExample = popnChoice.getSelectedIndex();
			if (newExample != currentExample) {
				currentExample = newExample;
				theHorizAxis.readNumLabels(axisLabels[currentExample]);
				theHorizAxis.setAxisName(distnName[currentExample]);
				theHorizAxis.repaint();
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