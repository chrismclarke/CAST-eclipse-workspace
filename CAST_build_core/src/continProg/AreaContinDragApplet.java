package continProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import imageUtils.*;

import contin.*;


public class AreaContinDragApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	
	static final private String Y_LABELS_PARAM = "yLabels";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String X_MARGIN_PARAM = "xMargin";
	static final private String Y_CONDIT_PARAM = "yCondit";
	
	
	static final private String kProbAxisInfo = "0.0 1.0 0.0 0.2";
	
	private DataSet data;
	private AreaContinDragView theView;
	
//	private ImageCanvas yAxisLabel, xAxisLabel;
	
	private ParameterSlider pxSlider;
	
	public void setupApplet() {
		ContinImages.loadLabels(this);
		
		data = readData();
		
		
		setLayout(new BorderLayout(0, 30));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
				XPanel rightDisplayPanel = new XPanel();
				rightDisplayPanel.setLayout(new BorderLayout(0, 30));
				rightDisplayPanel.add("West", yAxisLabelPanel(data, AreaContinDragView.X_MARGIN));
				rightDisplayPanel.add("Center", displayPanel(data, AreaContinDragView.X_MARGIN));
			
			topPanel.add(ProportionLayout.LEFT, rightDisplayPanel);
				
				XPanel leftDisplayPanel = new XPanel();
				leftDisplayPanel.setLayout(new BorderLayout(0, 30));
				leftDisplayPanel.add("West", yAxisLabelPanel(data, AreaContinDragView.Y_MARGIN));
				leftDisplayPanel.add("Center", displayPanel(data, AreaContinDragView.Y_MARGIN));
			
			topPanel.add(ProportionLayout.RIGHT, leftDisplayPanel);
		
		add("Center", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new ProportionLayout(0.5, 20, ProportionLayout.HORIZONTAL,
																						ProportionLayout.TOTAL));
			bottomPanel.add(ProportionLayout.LEFT, controlPanel(data));
			bottomPanel.add(ProportionLayout.RIGHT, new XPanel());
		
		add("South", bottomPanel);
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		CatDistnVariable xVariable = new CatDistnVariable(getParameter(X_VAR_NAME_PARAM));
		xVariable.readLabels(getParameter(X_LABELS_PARAM));
		xVariable.setParams(getParameter(X_MARGIN_PARAM));
		data.addVariable("x", xVariable);
		
		ContinResponseVariable yVariable = new ContinResponseVariable(getParameter(Y_VAR_NAME_PARAM), data, "x");
		yVariable.readLabels(getParameter(Y_LABELS_PARAM));
		yVariable.setProbs(getParameter(Y_CONDIT_PARAM), ContinResponseVariable.CONDITIONAL);
		data.addVariable("y", yVariable);
		
		return data;
	}
	
	private XPanel yAxisLabelPanel(DataSet data, boolean marginForY) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		ImageCanvas yAxisLabel = new ImageCanvas(marginForY ? ContinImages.yMarginLabel : ContinImages.yConditLabel,
																		ContinImages.kYLabelWidth, ContinImages.kYLabelHeight, this);
		thePanel.add(yAxisLabel);
		return thePanel;
	}
	
	private XPanel xAxisLabelPanel(DataSet data, boolean marginForY) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		ImageCanvas xAxisLabel = new ImageCanvas(marginForY ? ContinImages.xConditLabel
											: ContinImages.xMarginLabel, ContinImages.kXLabelWidth,
											ContinImages.kXLabelHeight, this);
		thePanel.add(xAxisLabel);
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data, boolean marginForY) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("Center", tablePanel(data, marginForY));
		thePanel.add("South", xAxisLabelPanel(data, marginForY));
		
		return thePanel;
	}
	
	private XPanel tablePanel(DataSet data, boolean marginForY) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		theHorizAxis.readNumLabels(kProbAxisInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theVertAxis = new VertAxis(this);
		theVertAxis.readNumLabels(kProbAxisInfo);
		thePanel.add("Left", theVertAxis);
		
		theView = new AreaContinDragView(data, this, theVertAxis, theHorizAxis, "y", "x", marginForY);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(100, 0));
		
		String xProbString = getParameter(X_MARGIN_PARAM);
		StringTokenizer st = new StringTokenizer(xProbString);
		NumValue startProb = new NumValue(st.nextToken());
		
		CatDistnVariable xVar = (CatDistnVariable)data.getVariable("x");
		String cat0 = xVar.getLabel(0).toString();
		
		pxSlider = new ParameterSlider(new NumValue(0.01, 2), new NumValue(0.99, 2),
																startProb, "P(" + cat0 + ")", this);
		thePanel.add("Center", pxSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == pxSlider) {
			CatDistnVariable xVar = (CatDistnVariable)data.getVariable("x");
			
			double newP0 = pxSlider.getParameter().toDouble();
			double newRemainder = 1.0 - newP0;
			
			double[] probs = xVar.getProbs();
			probs[0] = newP0;
			
			double oldRemainder = 0.0;
			for (int i=1 ; i<probs.length ; i++)
				oldRemainder += probs[i];
			if (oldRemainder <= 0.0)
				for (int i=1 ; i<probs.length ; i++)
					probs[i] = newRemainder / (probs.length - 1);
			else
				for (int i=1 ; i<probs.length ; i++)
					probs[i] *= newRemainder / oldRemainder;
			
			data.variableChanged("x");
			return true;
		}
		else
			return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}