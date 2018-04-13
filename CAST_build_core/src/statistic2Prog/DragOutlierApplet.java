package statistic2Prog;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import statistic2.*;


public class DragOutlierApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String DECIMALS_PARAM = "decimals";
	
	static final private String LOW_FIXED_OUTLIER_PARAM = "lowFixedOutlier";
	static final private String HIGH_FIXED_OUTLIER_PARAM = "highFixedOutlier";
	static final private String LOW_DRAG_OUTLIER_PARAM = "lowDragOutlier";
	static final private String HIGH_DRAG_OUTLIER_PARAM = "highDragOutlier";
	
	private String lowFixedOutlierString, highFixedOutlierString;
	private double lowFixedOutlier, highFixedOutlier;
	private double startLowOutlier, startHighOutlier;
	private double minLowDragOutlier, maxLowDragOutlier, minHighDragOutlier, maxHighDragOutlier;
	
	private DataSet data;
	
	private XCheckbox highOutlierCheck, lowOutlierCheck, highOutlierFixed, lowOutlierFixed;
	
	public void setupApplet() {
		data = getData();
		readOutlierInfo();
		
		setLayout(new BorderLayout());
		
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			NumVariable yVar = new NumVariable(getParameter(VAR_NAME_PARAM));
			yVar.readValues(getParameter(VALUES_PARAM));
			
				int n = yVar.noOfValues();
				NumValue y = (NumValue)yVar.valueAt(n - 2);
				startLowOutlier = y.toDouble();
				y.setValue(Double.NaN);
				
				y = (NumValue)yVar.valueAt(n - 1);
				startHighOutlier = y.toDouble();
				y.setValue(Double.NaN);
			
		data.addVariable("y", yVar);
		return data;
	}
	
	private void readOutlierInfo() {
		StringTokenizer st = new StringTokenizer(getParameter(LOW_FIXED_OUTLIER_PARAM), "#");
		lowFixedOutlier = Double.parseDouble(st.nextToken());
		lowFixedOutlierString = st.nextToken();
		
		st = new StringTokenizer(getParameter(HIGH_FIXED_OUTLIER_PARAM), "#");
		highFixedOutlier = Double.parseDouble(st.nextToken());
		highFixedOutlierString = st.nextToken();
		
		st = new StringTokenizer(getParameter(LOW_DRAG_OUTLIER_PARAM));
		minLowDragOutlier = Double.parseDouble(st.nextToken());
		maxLowDragOutlier = Double.parseDouble(st.nextToken());
		
		st = new StringTokenizer(getParameter(HIGH_DRAG_OUTLIER_PARAM));
		minHighDragOutlier = Double.parseDouble(st.nextToken());
		maxHighDragOutlier = Double.parseDouble(st.nextToken());
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			horizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			horizAxis.setAxisName(data.getVariable("y").name);
		thePanel.add("Bottom", horizAxis);
		
			StringTokenizer st = new StringTokenizer(getParameter(DECIMALS_PARAM));
			int meanDecimals = Integer.parseInt(st.nextToken());
			int sdDecimals = Integer.parseInt(st.nextToken());
		
			DragOutlierStackedView theView = new DragOutlierStackedView(data, this, horizAxis, meanDecimals,
											sdDecimals, minLowDragOutlier, maxLowDragOutlier, minHighDragOutlier, maxHighDragOutlier);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL,
																																	ProportionLayout.TOTAL));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 2));
			
				lowOutlierCheck = new XCheckbox(translate("Low outlier"), this);
			leftPanel.add(lowOutlierCheck);
				
				XPanel lowSubPanel = new InsetPanel(16, 0, 0, 0);
					lowOutlierFixed = new XCheckbox(lowFixedOutlierString, this);
					lowOutlierFixed.disable();
				lowSubPanel.add(lowOutlierFixed);
				
			leftPanel.add(lowSubPanel);
		
		controlPanel.add(ProportionLayout.LEFT, leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 2));
			
				highOutlierCheck = new XCheckbox(translate("High outlier"), this);
			rightPanel.add(highOutlierCheck);
				
				XPanel highSubPanel = new InsetPanel(16, 0, 0, 0);
					highOutlierFixed = new XCheckbox(highFixedOutlierString, this);
					highOutlierFixed.disable();
				highSubPanel.add(highOutlierFixed);
				
			rightPanel.add(highSubPanel);
		
		controlPanel.add(ProportionLayout.RIGHT, rightPanel);
		return controlPanel;
	}

	
	private boolean localAction(Object target) {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		int n = yVar.noOfValues();
		
		if (target == lowOutlierCheck) {
			double newOutlier;
			if (lowOutlierCheck.getState()) {
				lowOutlierFixed.enable();
				newOutlier = startLowOutlier;
			}
			else {
				if (!lowOutlierFixed.getState())
					startLowOutlier = yVar.doubleValueAt(n - 2);
				lowOutlierFixed.setState(false);
				lowOutlierFixed.disable();
				newOutlier = Double.NaN;
			}
			((NumValue)yVar.valueAt(n - 2)).setValue(newOutlier);
			
			data.variableChanged("y");
			return true;
		}
		else if (target == lowOutlierFixed) {
			double newOutlier;
			if (lowOutlierFixed.getState()) {
				newOutlier = lowFixedOutlier;
				startLowOutlier = yVar.doubleValueAt(n - 2);
			}
			else
				newOutlier = startLowOutlier;
			((NumValue)yVar.valueAt(n - 2)).setValue(newOutlier);
			
			data.variableChanged("y");
			return true;
		}
		else if (target == highOutlierCheck) {
			double newOutlier;
			if (highOutlierCheck.getState()) {
				highOutlierFixed.enable();
				newOutlier = startHighOutlier;
			}
			else {
				if (!highOutlierFixed.getState())
					startHighOutlier = yVar.doubleValueAt(n - 1);
				highOutlierFixed.setState(false);
				highOutlierFixed.disable();
				newOutlier = Double.NaN;
			}
			((NumValue)yVar.valueAt(n - 1)).setValue(newOutlier);
			
			data.variableChanged("y");
			return true;
		}
		else if (target == highOutlierFixed) {
			double newOutlier;
			if (highOutlierFixed.getState()) {
				newOutlier = highFixedOutlier;
				startHighOutlier = yVar.doubleValueAt(n - 1);
			}
			else
				newOutlier = startHighOutlier;
			((NumValue)yVar.valueAt(n - 1)).setValue(newOutlier);
			
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