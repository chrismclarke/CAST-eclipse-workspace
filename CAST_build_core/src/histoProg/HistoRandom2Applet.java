package histoProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import histo.*;
import random.RandomNormal;


public class HistoRandom2Applet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String CLASS_INFO_PARAM = "classInfo";
	static final private String RANDOM_NORMAL_PARAM = "random";
	
	private DataSet data;
	
	private XButton takeSampleButton;
	
	private RandomNormal generator;
	
	protected HistoView theHisto;
	protected DensityAxis theDensityAxis;
	private XLabel densityAxisNameLabel;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
		add("Center", createHisto(data));
		add("South", createControls(data));
		
		densityAxisNameLabel = new XLabel(theDensityAxis.getAxisName(), XLabel.LEFT, this);
		densityAxisNameLabel.setFont(theDensityAxis.getFont());
		add("North", densityAxisNameLabel);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		generator = new RandomNormal(randomInfo);
		double vals[] = generateData();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
		return data;
	}
	
	private double[] generateData() {
		double vals[] = generator.generate();
		return vals;
	}
	
	private XPanel createHisto(DataSet data) {
		XPanel histoPanel = new XPanel();
		histoPanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			CoreVariable yVar = data.getVariable("y");
			theHorizAxis.setAxisName(yVar.name);
		
		histoPanel.add("Bottom", theHorizAxis);
		
			String classInfo = getParameter(CLASS_INFO_PARAM);
			StringTokenizer theParams = new StringTokenizer(classInfo);
			double class0Start = Double.parseDouble(theParams.nextToken());
			double classWidth = Double.parseDouble(theParams.nextToken());
			double maxDensity = Double.parseDouble(theParams.nextToken());
			
			theDensityAxis = new DensityAxis(DensityAxis.COUNT_LABELS, maxDensity,
														classWidth, data.getNumVariable().noOfValues(), this);
		histoPanel.add("Left", theDensityAxis);
		
			theHisto = new HistoView(data, this, theHorizAxis, theDensityAxis, class0Start, classWidth);
			theHisto.setBarType(HistoView.VERT_BARS);
		histoPanel.add("Center", theHisto);
			theHisto.lockBackground(Color.white);
		
		return histoPanel;
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
		
		takeSampleButton = new XButton(translate("Sample"), this);
		controlPanel.add(takeSampleButton);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == takeSampleButton) {
			double vals[] = generateData();
			data.getNumVariable().setValues(vals);
			data.variableChanged("y");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}