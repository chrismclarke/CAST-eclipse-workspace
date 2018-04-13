package samplingProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import utils.*;
import coreGraphics.*;
import imageGroups.*;

import sampling.*;
import normal.NormStdProbView;

public class StdNormalApplet extends NormalStandardisedApplet {
	static final protected String BIGGEST_X_PARAM = "biggestX";
	
	protected NormStdProbView prob;
	
	public void setupApplet() {
		MeanSDImages.loadMeanSD(this);
		
		data = getData();
		setLayout(new BorderLayout());
		
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new ProportionLayout(0.6, 10, ProportionLayout.HORIZONTAL, ProportionLayout.REMAINDER));
		mainPanel.add(ProportionLayout.LEFT, displayPanel(data));
		mainPanel.add(ProportionLayout.RIGHT, probPanel(data));
		
		add("North", topPanel(data));
		
		add("Center", mainPanel);
		
		add("South", bottomPanel(data));
	}
	
	protected XPanel probPanel(DataSet data) {
		XPanel thePanel = super.probPanel(data);
		
		meanSlider.setForeground(Color.black);
		sdSlider.setForeground(Color.black);
		
		NormalDistnVariable y = (NormalDistnVariable)data.getVariable(distnKey);
		double startMean = meanSlider.getParameter().toDouble();
		double startSD = sdSlider.getParameter().toDouble();
		y.setMean(0.0);
		y.setSD(1.0);
		y.setMinSelection(Double.NEGATIVE_INFINITY);
		y.setMaxSelection(Double.NEGATIVE_INFINITY);
		theHorizAxis.setInitialMinMax(startMean - 3.0 * startSD, startMean + 3.0 * startSD);
		
		return thePanel;
	}
	
	protected XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 2));
		
		return thePanel;
	}
	
	protected int dragType() {
		return DistnDensityView.NO_DRAG;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		NormalDistnVariable y = (NormalDistnVariable)data.getVariable(distnKey);
		
		theHorizAxis = new StandardisingAxis(this);
		String labelInfo = getParameter(AXIS_STEPS_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setAxisName(y.name);
		theHorizAxis.setForeground(Color.blue);
		thePanel.add("Bottom", theHorizAxis);
		
		HorizAxis stdAxis = new HorizAxis(this);
		stdAxis.readNumLabels(StandardisingAxis.kStandardisedLabelInfo);
		stdAxis.setAxisName("Z");
		stdAxis.setForeground(Color.red);
		thePanel.add("Bottom", stdAxis);
		
		theProbAxis = new VertAxis(this);
		labelInfo = getParameter(PROB_AXIS_PARAM);
		theProbAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theProbAxis);
		
		DistnDensityView theView = new DistnDensityView(data, this, theHorizAxis, theProbAxis, distnKey,
																									DistnDensityView.SHOW_MEANSD, dragType());
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected void setAxisFromSlider() {
		super.setAxisFromSlider();
		if (prob != null)
			prob.repaint();
	}
}