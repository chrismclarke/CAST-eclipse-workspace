package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.ProportionView;
import distn.*;
import random.RandomNormal;
import coreGraphics.*;
import formula.*;

import sampling.*;


public class DensityAreaApplet extends XApplet {
	static final private String HORIZ_AXIS_PARAM = "horizAxis";
	static final protected String PROB_AXIS_PARAM = "probAxis";
	static final private String AXIS_NAME_PARAM = "axisName";
	
	static final private String RANDOM_NORMAL_PARAM = "random";
	
	static final private String DISTN_NAME_PARAM = "distnName";
	static final private String PARAM_PARAM = "parameters";
	
	static final private String CLASS_PARAM = "classes";
	
	protected VertAxis theProbAxis;
	protected DataSet data;
	
	protected String yKey = null;
	protected String distnKey = null;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("North", topPanel(data));
		add("South", probPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String distnName = getParameter(DISTN_NAME_PARAM);
		if (distnName != null) {
			NormalDistnVariable y = new NormalDistnVariable(distnName);
			y.setParams(getParameter(PARAM_PARAM));
			data.addVariable("distn", y);
			distnKey = "distn";
		}
		
		String yName = getParameter(VAR_NAME_PARAM);
		if (yName != null) {
			NumVariable y = new NumVariable(yName);
			String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
			
			if (randomInfo != null) {
				RandomNormal generator = new RandomNormal(randomInfo);
				double vals[] = generator.generate();
				y.setValues(vals);
			}
			else
				y.readValues(getParameter(VALUES_PARAM));
				
			data.addVariable("y", y);
			yKey = "y";
		}
		
		return data;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		XLabel probLabel = new XLabel(translate("Density"), XLabel.LEFT, this);
		probLabel.setFont(theProbAxis.getFont());
		thePanel.add(probLabel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(HORIZ_AXIS_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			String axisName = getParameter(AXIS_NAME_PARAM);
			if (axisName != null)
				theHorizAxis.setAxisName(MText.expandText(axisName));
		thePanel.add("Bottom", theHorizAxis);
		
		theProbAxis = new VertAxis(this);
		labelInfo = getParameter(PROB_AXIS_PARAM);
		theProbAxis.readNumLabels(labelInfo);
		thePanel.add("Left", theProbAxis);
		
		double class0Start = 0.0;
		double classWidth = 0.0;
		if (yKey != null) {
			StringTokenizer st = new StringTokenizer(getParameter(CLASS_PARAM));
			class0Start = Double.parseDouble(st.nextToken());
			classWidth = Double.parseDouble(st.nextToken());
		}
		
		DataView theView = getDataView(data, theHorizAxis, theProbAxis, yKey, distnKey,
																							class0Start, classWidth);
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected DataView getDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theProbAxis,
								String yKey, String distnKey, double class0Start, double classWidth) {
		DataView theView;
		if (yKey == null)
			theView = new DistnDensityView(data, this, theHorizAxis, theProbAxis, distnKey,
														DistnDensityView.NO_SHOW_MEANSD, DistnDensityView.MIN_MAX_DRAG);
		else
			theView = new HistoDragView(data, this, theHorizAxis, theProbAxis, yKey, class0Start, classWidth,
																														HistoAndNormalView.SHOW_VALUES);
		return theView;
	}
	
	protected XPanel probPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 2));
		
		
		
		ProportionView pView = new ProportionView(data, (distnKey != null) ? distnKey : yKey, this);
		pView.setLabel(translate("Probability") + " = " + translate("Area") + " =");
		pView.setFont(getBigFont());
		pView.setHighlight(true);
		thePanel.add(pView);
		
		return thePanel;
	}
}