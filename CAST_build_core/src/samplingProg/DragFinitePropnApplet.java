package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import random.*;

import sampling.*;


public class DragFinitePropnApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String MAX_VALUE_PARAM = "maxValue";
	static final private String INITIAL_SELECTION_PARAM = "initialSelection";
	
	private DataSet data;
	private int decimals;
	
	private DragPropnDotPlotView theView;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		NumVariable y = new NumVariable(getParameter(VAR_NAME_PARAM));
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		if (randomInfo != null) {
		RandomNormal generator = new RandomNormal(randomInfo);
			double vals[] = generator.generate();
			y.setValues(vals);
		}
		else
			y.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", y);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", theHorizAxis);
		
			StringTokenizer st = new StringTokenizer(getParameter(INITIAL_SELECTION_PARAM));
			double minSelection = Double.parseDouble(st.nextToken());
			double maxSelection = Double.parseDouble(st.nextToken());
			data.setSelection("y", minSelection, maxSelection);
			decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			theView = new DragPropnDotPlotView(data, this, theHorizAxis, decimals, "y", minSelection, maxSelection);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			double maxValue = Double.parseDouble(getParameter(MAX_VALUE_PARAM));
		
			FractionValueView fraction = new FractionValueView(data, this, theView, decimals, maxValue);
			fraction.setFont(getBigFont());
		controlPanel.add(fraction);
		
		return controlPanel;
	}
}