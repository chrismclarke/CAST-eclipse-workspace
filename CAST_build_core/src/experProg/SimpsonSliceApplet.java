package experProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import multivar.*;
import exper.*;

public class SimpsonSliceApplet extends XApplet {
	static final protected String X_AXIS_INFO_PARAM = "horizAxis";
	
	static final private String X_VAR_NAME_PARAM = "xVarName";
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String Z_VAR_NAME_PARAM = "zVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String Z_LABELS_PARAM = "zLabels";
	
	static final private String SLICE_MEAN_PARAM = "sliceMean";
	static final private String OVERALL_PARAM = "overallMean";
	
	private DataSet data;
	
	private XCheckbox sliceCheck;
	private SliceSlider sliceSlider;
	private SimpsonNumerView theView;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		add("North", topPanel(data));
	}
	
	protected DataSet readData() {
		data = new DataSet();
		data.addNumVariable("y", getParameter(Y_VAR_NAME_PARAM), getParameter(Y_VALUES_PARAM));
			CatVariable xVar = new CatVariable(getParameter(X_VAR_NAME_PARAM), CatVariable.USES_REPEATS);
			xVar.readLabels(getParameter(X_LABELS_PARAM));
			xVar.readValues(getParameter(X_VALUES_PARAM));
		data.addVariable("x", xVar);
			CatVariable zVar = new CatVariable(getParameter(Z_VAR_NAME_PARAM), CatVariable.USES_REPEATS);
			zVar.readLabels(getParameter(Z_LABELS_PARAM));
			zVar.readValues(getParameter(Z_VALUES_PARAM));
		data.addVariable("z", zVar);
		return data;
	}
	
	private XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		thePanel.add(new XLabel(getParameter(X_VAR_NAME_PARAM), XLabel.LEFT, this));
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(X_AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(getParameter(Y_VAR_NAME_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
			VertAxis groupAxis = new VertAxis(this);
			CatVariable groupingVariable = (CatVariable)data.getVariable("x");
			groupAxis.setCatLabels(groupingVariable);
		thePanel.add("Left", groupAxis);
			
			theView = new SimpsonNumerView(data, this, theHorizAxis, groupAxis, getParameter(SLICE_MEAN_PARAM),
								new LabelValue(getParameter(OVERALL_PARAM)), "x", "z", SimpsonNumerView.SLICE_MODE);
			theView.lockBackground(Color.white);
									
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																																	ProportionLayout.TOTAL));
			
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				sliceCheck = new XCheckbox(translate("Slice"), this);
			checkPanel.add(sliceCheck);
		
		thePanel.add(ProportionLayout.LEFT, checkPanel);
		
			CatVariable zVar = (CatVariable)data.getVariable("z");
			String name[] = new String[zVar.noOfCategories()];
			for (int i=0 ; i<name.length ; i++)
				name[i] = zVar.getLabel(i).toString();
			sliceSlider = new SliceSlider(data.getVariable("z").name, 0, name.length-1, name, this);
			
			sliceSlider.setFont(getStandardBoldFont());
			
			sliceSlider.show(false);
		thePanel.add(ProportionLayout.RIGHT, sliceSlider);
		
		return thePanel;
	}
	
	private void selectFromSlider() {
		int z = sliceSlider.getValue();
		theView.setCurrentSlice(z);
	}
	

	
	private boolean localAction(Object target) {
		if (target == sliceCheck) {
			if (sliceCheck.getState()) {
				sliceSlider.show(true);
				theView.setShowSliced(true);
			}
			else {
				sliceSlider.show(false);
				theView.setShowSliced(false);
			}
			return true;
		}
		else if (target == sliceSlider) {
			selectFromSlider();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}