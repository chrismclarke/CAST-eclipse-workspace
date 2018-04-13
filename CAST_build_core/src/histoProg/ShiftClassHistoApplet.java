package histoProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.*;
import random.*;

import histo.*;


public class ShiftClassHistoApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String CLASS_INFO_PARAM = "classInfo";
	static final private String INIT_GROUPING_PARAM = "groupSize";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String SHIFT_PARAM = "shift";		//	added to each value after squaring

	static final private Color kDarkRed = new Color(0x990000);
	
	private RandomNormal generator;
	protected DataSet data;
	
	protected ShiftClassHistoView theHisto;
	private DensityAxis theDensityAxis;
	
	private XButton leftButton, rightButton;
	private XButton narrowerButton, widerButton;
	
	private FixedValueView classWidthView = null;
	private FixedValueView class0StartView = null;
	private NumValue class0Start, classWidth;
	private int maxGroupSize, startGroupSize;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
		add("Center", createHisto(data));
		add("South", createControls());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String yValuesString = getParameter(VALUES_PARAM);
		if (yValuesString != null)
			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), yValuesString);
		else {
			String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
			generator = new RandomNormal(randomInfo);
			double vals[] = generateData();

			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
		}
		return data;
	}
	
	protected double[] generateData() {
		double vals[] = generator.generate();
			
		String shiftString = getParameter(SHIFT_PARAM);
		double shift = 0.0;
		if (shiftString != null)
			shift = Double.parseDouble(shiftString);
			
		for (int i=0 ; i<vals.length ; i++)
			vals[i] = vals[i] * vals[i] + shift;			//		square every value to give skew distn
		return vals;
	}
	
	protected int initialDensityAxisLabel() {
		return DensityAxis.NO_LABELS;
	}
	
	protected int initialBarType() {
		return HistoView.VERT_BARS;
	}
	
	private XPanel createHisto(DataSet data) {
		XPanel histoPanel = new XPanel();
		histoPanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			String varName = getParameter(VAR_NAME_PARAM);
			if (varName != null)
				theHorizAxis.setAxisName(varName);
		histoPanel.add("Bottom", theHorizAxis);
		
			String classInfo = getParameter(CLASS_INFO_PARAM);
			StringTokenizer theParams = new StringTokenizer(classInfo);
			class0Start = new NumValue(theParams.nextToken());
			classWidth = new NumValue(theParams.nextToken());
			double maxDensity = Double.parseDouble(theParams.nextToken());
		
			theDensityAxis = new DensityAxis(initialDensityAxisLabel(), maxDensity, classWidth.toDouble(),
																data.getNumVariable().noOfValues(), this);
			theDensityAxis.show(false);						//	to stop vertical line from being drawn
		histoPanel.add("Left", theDensityAxis);
		
			String groupingInfo = getParameter(INIT_GROUPING_PARAM);
			StringTokenizer initGroups = new StringTokenizer(groupingInfo);
			maxGroupSize = Integer.parseInt(initGroups.nextToken());
			startGroupSize = Integer.parseInt(initGroups.nextToken());
			
			theHisto = new ShiftClassHistoView(data, this, theHorizAxis, theDensityAxis, class0Start.toDouble(),
																	classWidth.toDouble(), maxGroupSize, startGroupSize);
			theHisto.setBarType(initialBarType());
			theHisto.lockBackground(Color.white);
			
		histoPanel.add("Center", theHisto);
		
		return histoPanel;
	}
	
	protected XPanel shiftControls() {
		XPanel shiftPanel = new XPanel();
		shiftPanel.setLayout(new GridLayout(1, 2));
		leftButton = new XButton("<- " + translate("Left"), this);
		shiftPanel.add(leftButton);
		rightButton = new XButton(translate("Right") + " ->", this);
		shiftPanel.add(rightButton);
		return shiftPanel;
	}
	
	protected XPanel widthControls() {
		XPanel widthPanel = new XPanel();
		widthPanel.setLayout(new GridLayout(1, 2));
		narrowerButton = new XButton(translate("Narrower"), this);
		widthPanel.add(narrowerButton);
		widerButton = new XButton(translate("Wider"), this);
		widthPanel.add(widerButton);
		return widthPanel;
	}
	
	protected FixedValueView createClassWidthView() {
		NumValue maxClassWidth = new NumValue(classWidth.toDouble() * maxGroupSize, classWidth.decimals);
		double startWidth = classWidth.toDouble() * startGroupSize;
		classWidthView = new FixedValueView(translate("Class width") + " =", maxClassWidth, startWidth, this);
		classWidthView.setFont(getBigFont());
		classWidthView.setForeground(kDarkRed);
		return classWidthView;
	}
	
	protected FixedValueView createClass0StartView() {
		double maxAbs = Math.max(Math.abs(class0Start.toDouble()),
									Math.abs(class0Start.toDouble() + classWidth.toDouble() * maxGroupSize));
		double max = (class0Start.toDouble() >= 0) ? maxAbs : -maxAbs;
		NumValue maxClass0Start = new NumValue(max, Math.max(class0Start.decimals, classWidth.decimals));
		class0StartView = new FixedValueView(translate("Class start") + " =", maxClass0Start, class0Start.toDouble(), this);
		class0StartView.setFont(getBigFont());
		class0StartView.setForeground(kDarkRed);
		return class0StartView;
	}
	
	protected XPanel createControls() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																			VerticalLayout.VERT_CENTER, 5));
			XPanel valuePanel = new XPanel();
			valuePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			valuePanel.add(createClass0StartView());
			valuePanel.add(createClassWidthView());
		thePanel.add(valuePanel);
		
			XPanel adjustPanel = new XPanel();
			adjustPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			adjustPanel.add(shiftControls());
			adjustPanel.add(widthControls());
		thePanel.add(adjustPanel);
		
		return thePanel;
	}
	
	private void updateClassValues() {
		if (classWidthView != null)
			classWidthView.setValue(theHisto.findClass1Width());
		if (class0StartView != null)
			class0StartView.setValue(theHisto.findClass0Start());
	}
	
	private boolean localAction(Object target) {
		if (target == leftButton) {
			theHisto.shiftGroups(ShiftClassHistoView.LEFT);
			updateClassValues();
			return true;
		}
		else if (target == rightButton) {
			theHisto.shiftGroups(ShiftClassHistoView.RIGHT);
			updateClassValues();
			return true;
		}
		else if (target == narrowerButton) {
			theHisto.changeWidth(ShiftClassHistoView.NARROWER);
			if (theHisto.isMinClassWidth()) {
				narrowerButton.disable();
				if (leftButton != null)
					leftButton.disable();
				if (rightButton != null)
					rightButton.disable();
			}
			widerButton.enable();
			updateClassValues();
			return true;
		}
		else if (target == widerButton) {
			theHisto.changeWidth(ShiftClassHistoView.WIDER);
			if (theHisto.isMaxClassWidth())
				widerButton.disable();
			narrowerButton.enable();
			if (leftButton != null)
				leftButton.enable();
			if (rightButton != null)
				rightButton.enable();
			updateClassValues();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}