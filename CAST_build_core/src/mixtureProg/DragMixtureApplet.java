package mixtureProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import valueList.*;
import utils.*;
import coreVariables.*;

import mixture.*;


public class DragMixtureApplet extends XApplet {
	static final protected String X_VAR_NAME_PARAM = "xVarName";
	static final protected String Y_VAR_NAME_PARAM = "yVarName";
	static final protected String Z_VAR_NAME_PARAM = "zVarName";
	static final protected String X_VALUES_PARAM = "xValues";
	static final protected String Y_VALUES_PARAM = "yValues";
	static final protected String Z_VALUES_PARAM = "zValues";
	
	static final protected String SUM_XYZ_PARAM = "sumXYZ";

	static final protected String LEFT_X_SCALE_PARAM = "leftXScale";
	static final protected String LEFT_Y_SCALE_PARAM = "leftYScale";
	static final protected String LEFT_Z_SCALE_PARAM = "leftZScale";
	
	static final protected String RIGHT_X_SCALE_PARAM = "rightXScale";
	static final protected String RIGHT_Y_SCALE_PARAM = "rightYScale";
	static final protected String RIGHT_Z_SCALE_PARAM = "rightZScale";
	
	static final protected String LEFT_X_LABELS_PARAM = "leftXLabels";
	static final protected String LEFT_Y_LABELS_PARAM = "leftYLabels";
	static final protected String LEFT_Z_LABELS_PARAM = "leftZLabels";
	
	static final protected String RIGHT_X_LABELS_PARAM = "rightXLabels";
	static final protected String RIGHT_Y_LABELS_PARAM = "rightYLabels";
	static final protected String RIGHT_Z_LABELS_PARAM = "rightZLabels";
	
	static final protected String RIGHT_SCALE_NAMES_PARAM = "rightScaleNames";
	
	static final protected String MAX_PERCENT_PARAM = "maxPercent";
	static final protected String HUNDRED_PERCENT_PARAM = "hundredPercent";
	
	static final protected String LEFT_HEADING_PARAM = "leftHeading";
	static final protected String RIGHT_HEADING_PARAM = "rightHeading";
	
//	static final private String kSingleNanString = "?";
	
	private NumValue maxPercent, hundredPercent;
	
	private DataSet data;
	
	private ConstrainedTriangleView leftView, rightView;
	
	private XChoice rightScaleChoice;
	private int currentScaleChoice = 0;
	
	private OneValueView xRightPercentView, yRightPercentView, zRightPercentView;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 0));
		add("Center", displayPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		maxPercent = new NumValue(getParameter(MAX_PERCENT_PARAM));
		hundredPercent = new NumValue(getParameter(HUNDRED_PERCENT_PARAM));
		
		createCoreVariables(data);
		
		NumVariable xVar = (NumVariable)data.getVariable("x");
		NumVariable yVar = (NumVariable)data.getVariable("y");
		NumVariable zVar = (NumVariable)data.getVariable("z");
		
			ScaledVariable xPercentVar = new ScaledVariable(xVar.name, xVar, "x", 0.0, 100.0, maxPercent.decimals);
		data.addVariable("xPercent", xPercentVar);
		
			ScaledVariable yPercentVar = new ScaledVariable(yVar.name, yVar, "y", 0.0, 100.0, maxPercent.decimals);
		data.addVariable("yPercent", yPercentVar);
		
			ScaledVariable zPercentVar = new ScaledVariable(zVar.name, zVar, "z", 0.0, 100.0, maxPercent.decimals);
		data.addVariable("zPercent", zPercentVar);
		
		addMixtureVariables(data, xVar, yVar, zVar);
		
		return data;
	}
	
	protected void createCoreVariables(DataSet data) {
			NumVariable xVar = new NumVariable(getParameter(X_VAR_NAME_PARAM));
			NumValue x = new NumValue(Double.NaN, 0);
			x.decimals = maxPercent.decimals + 2;
			xVar.addValue(x);
		data.addVariable("x", xVar);
		
			NumVariable yVar = new NumVariable(getParameter(Y_VAR_NAME_PARAM));
			NumValue y = new NumValue(Double.NaN, 0);
			y.decimals = maxPercent.decimals + 2;
			yVar.addValue(y);
		data.addVariable("y", yVar);
		
			NumVariable zVar = new NumVariable(getParameter(Z_VAR_NAME_PARAM));
			NumValue z = new NumValue(Double.NaN, 0);
			z.decimals = maxPercent.decimals + 2;
			zVar.addValue(z);
		data.addVariable("z", zVar);
		
		data.setSelection(0);
	}
	
	protected void addMixtureVariables(DataSet data, NumVariable xVar, NumVariable yVar,
																																					NumVariable zVar) {
			double sumXYZ = Double.parseDouble(getParameter(SUM_XYZ_PARAM));
			double scaleFactor = 100.0 / sumXYZ;
		
			ScaledVariable xMixPercentVar = new ScaledVariable(xVar.name, xVar, "x", 0.0, scaleFactor, hundredPercent.decimals);
		data.addVariable("xMixPercent", xMixPercentVar);
		
			ScaledVariable yMixPercentVar = new ScaledVariable(yVar.name, yVar, "y", 0.0, scaleFactor, hundredPercent.decimals);
		data.addVariable("yMixPercent", yMixPercentVar);
		
			ScaledVariable zMixPercentVar = new ScaledVariable(zVar.name, zVar, "z", 0.0, scaleFactor, hundredPercent.decimals);
		data.addVariable("zMixPercent", zMixPercentVar);
	}
	
	private void setScale(int varIndex, ConstrainedTriangleView theView, String scaleParam) {
		StringTokenizer st = new StringTokenizer(getParameter(scaleParam));
		double minConstraint = Double.parseDouble(st.nextToken());
		if (minConstraint < 0.0)
			minConstraint = Double.NaN;
		double maxConstraint = Double.parseDouble(st.nextToken());
		if (maxConstraint < 0.0)
			maxConstraint = Double.NaN;
		
		theView.setConstraints(varIndex, minConstraint, maxConstraint);
		
		double min = Double.parseDouble(st.nextToken());
		double max = Double.parseDouble(st.nextToken());
		theView.setAxisMinMax(varIndex, min, max);
	}
	
	private void setAxisLabels(int varIndex, ConstrainedTriangleView theView, String labelsParam) {
		StringTokenizer st = new StringTokenizer(getParameter(labelsParam), "() ");
		int nLabels = Integer.parseInt(st.nextToken());
		double propn[] = new double[nLabels];
		LabelValue label[] = new LabelValue[nLabels];
		for (int i=0 ; i<nLabels ; i++) {
			label[i] = new LabelValue(st.nextToken());
			propn[i] = Double.parseDouble(st.nextToken());
		}
		theView.setAxisLabels(varIndex, label, propn);
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
			
		thePanel.add(ProportionLayout.LEFT, leftDisplayPanel(data));
		thePanel.add(ProportionLayout.RIGHT, rightDisplayPanel(data));
		
		return thePanel;
	}
	
	protected ConstrainedTriangleView createTriangleView(DataSet data) {
		return new DragTriangleView(data, this, "x", "y", "z");
	}
	
	protected XPanel leftDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
			XLabel leftHeading = new XLabel(getParameter(LEFT_HEADING_PARAM), XLabel.CENTER, this);
			leftHeading.setFont(getStandardBoldFont());
		
		thePanel.add("North", leftHeading);
			
			leftView = createTriangleView(data);
			leftView.setCrossSize(DataView.LARGE_CROSS);
			setScale(DragTriangleView.X_AXIS, leftView, LEFT_X_SCALE_PARAM);
			setScale(DragTriangleView.Y_AXIS, leftView, LEFT_Y_SCALE_PARAM);
			setScale(DragTriangleView.Z_AXIS, leftView, LEFT_Z_SCALE_PARAM);
			setAxisLabels(DragTriangleView.X_AXIS, leftView, LEFT_X_LABELS_PARAM);
			setAxisLabels(DragTriangleView.Y_AXIS, leftView, LEFT_Y_LABELS_PARAM);
			setAxisLabels(DragTriangleView.Z_AXIS, leftView, LEFT_Z_LABELS_PARAM);
			double sumXYZ = Double.parseDouble(getParameter(SUM_XYZ_PARAM));
			leftView.setSumXYZ(sumXYZ);
			leftView.setGridOverTriangle(false);
		
		thePanel.add("Center", leftView);
		
			XPanel valuePanel = new XPanel();
			
			valuePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
				OneValueView xPercentView = new OneValueView(data, "xPercent", this, maxPercent);
				xPercentView.setUnitsString("%");
			valuePanel.add(xPercentView);
			
				OneValueView yPercentView = new OneValueView(data, "yPercent", this, maxPercent);
				yPercentView.setUnitsString("%");
			valuePanel.add(yPercentView);
			
				OneValueView zPercentView = new OneValueView(data, "zPercent", this, maxPercent);
				zPercentView.setUnitsString("%");
			valuePanel.add(zPercentView);
			
		thePanel.add("South", valuePanel);
		
		return thePanel;
	}
	
	protected XPanel rightDisplayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
			XLabel rightHeading = new XLabel(getParameter(RIGHT_HEADING_PARAM), XLabel.CENTER, this);
			rightHeading.setFont(getStandardBoldFont());
		
		thePanel.add("North", rightHeading);
		
			rightView = createTriangleView(data);
			rightView.setCrossSize(DataView.LARGE_CROSS);
			setScale(DragTriangleView.X_AXIS, rightView, RIGHT_X_SCALE_PARAM);
			setScale(DragTriangleView.Y_AXIS, rightView, RIGHT_Y_SCALE_PARAM);
			setScale(DragTriangleView.Z_AXIS, rightView, RIGHT_Z_SCALE_PARAM);
			setAxisLabels(DragTriangleView.X_AXIS, rightView, RIGHT_X_LABELS_PARAM);
			setAxisLabels(DragTriangleView.Y_AXIS, rightView, RIGHT_Y_LABELS_PARAM);
			setAxisLabels(DragTriangleView.Z_AXIS, rightView, RIGHT_Z_LABELS_PARAM);
			double sumXYZ = Double.parseDouble(getParameter(SUM_XYZ_PARAM));
			rightView.setSumXYZ(sumXYZ);
			rightView.setGridOverTriangle(false);
		
		thePanel.add("Center", rightView);
		
			XPanel valuePanel = new XPanel();
			
			valuePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
				String scaleNamesString = getParameter(RIGHT_SCALE_NAMES_PARAM);
				if (scaleNamesString != null) {
					StringTokenizer st = new StringTokenizer(scaleNamesString, "#");
					rightScaleChoice = new XChoice(this);
					while (st.hasMoreTokens())
						rightScaleChoice.addItem(st.nextToken());
					valuePanel.add(rightScaleChoice);
				}
				
				xRightPercentView = new OneValueView(data, "xMixPercent", this, hundredPercent);
				xRightPercentView.setUnitsString("%");
			valuePanel.add(xRightPercentView);
			
				yRightPercentView = new OneValueView(data, "yMixPercent", this, hundredPercent);
				yRightPercentView.setUnitsString("%");
			valuePanel.add(yRightPercentView);
			
				zRightPercentView = new OneValueView(data, "zMixPercent", this, hundredPercent);
				zRightPercentView.setUnitsString("%");
			valuePanel.add(zRightPercentView);
			
		thePanel.add("South", valuePanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == rightScaleChoice) {
			int newChoice = rightScaleChoice.getSelectedIndex();
			if (newChoice != currentScaleChoice) {
				currentScaleChoice = newChoice;
				
				if (newChoice == 0) {
					setAxisLabels(ConstrainedTriangleView.X_AXIS, rightView, RIGHT_X_LABELS_PARAM);
					setAxisLabels(ConstrainedTriangleView.Y_AXIS, rightView, RIGHT_Y_LABELS_PARAM);
					setAxisLabels(ConstrainedTriangleView.Z_AXIS, rightView, RIGHT_Z_LABELS_PARAM);
					rightView.reset();
					xRightPercentView.setVariableKey("xMixPercent");
					yRightPercentView.setVariableKey("yMixPercent");
					zRightPercentView.setVariableKey("zMixPercent");
				}
				else {
					setAxisLabels(ConstrainedTriangleView.X_AXIS, rightView, RIGHT_X_LABELS_PARAM + "2");
					setAxisLabels(ConstrainedTriangleView.Y_AXIS, rightView, RIGHT_Y_LABELS_PARAM + "2");
					setAxisLabels(ConstrainedTriangleView.Z_AXIS, rightView, RIGHT_Z_LABELS_PARAM + "2");
					rightView.reset();
					xRightPercentView.setVariableKey("xPercent");
					yRightPercentView.setVariableKey("yPercent");
					zRightPercentView.setVariableKey("zPercent");
				}
				
				data.valueChanged(0);
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