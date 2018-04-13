package histoProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import histo.*;


public class HistoFreqApplet extends XApplet {
	static final private String CLASS_INFO_PARAM = "classInfo";
	static final private String X_AXIS_PARAM = "xAxis";
	static final private String FREQ_AXIS_PARAM = "freqAxis";
	static final private String SHOW_CROSSES_PARAM = "showCrosses";
	
	static final private int kStartGrouping = 2;
	
	private int maxGrouping;
	private int currentGrouping = kStartGrouping;
	private MultiVertAxis freqAxis;
	private HistoFreqView theView;
	
	private XButton widerButton, narrowerButton;
	private XCheckbox crossesCheck;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 0));
		
		add("North", new XLabel(translate("Frequency"), XLabel.LEFT, this));
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			String labelInfo = getParameter(X_AXIS_PARAM);
			horizAxis.readNumLabels(labelInfo);
			horizAxis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", horizAxis);
		
			StringTokenizer st = new StringTokenizer(getParameter(FREQ_AXIS_PARAM), "*");
			maxGrouping = st.countTokens();
			freqAxis = new MultiVertAxis(this, maxGrouping);
			freqAxis.setChangeMinMax(true);
			freqAxis.readNumLabels(st.nextToken());
			while (st.hasMoreTokens())
				freqAxis.readExtraNumLabels(st.nextToken());
			freqAxis.setStartAlternate(kStartGrouping - 1);
			
		thePanel.add("Left", freqAxis);
		
			theView = new HistoFreqView(data, this, horizAxis, freqAxis,
																			getParameter(CLASS_INFO_PARAM), kStartGrouping);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		String showCrossesString = getParameter(SHOW_CROSSES_PARAM);
		if (showCrossesString == null || showCrossesString.equals("true")) {
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
			
				XPanel leftPanel = new XPanel();
				leftPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 2));
				
					XLabel freqLabel = new XLabel(translate("Make class width") + ":", XLabel.LEFT, this);
					freqLabel.setFont(getStandardBoldFont());
				leftPanel.add(freqLabel);
				
					XPanel buttonPanel = new XPanel();
					buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
						
						narrowerButton = new XButton(translate("Narrower"), this);
					buttonPanel.add(narrowerButton);
					
						widerButton = new XButton(translate("Wider"), this);
					buttonPanel.add(widerButton);
					
				leftPanel.add(buttonPanel);
				
			thePanel.add(leftPanel);
			
				XPanel rightPanel = new XPanel();
				rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
					crossesCheck = new XCheckbox(translate("Show crosses"), this);
					crossesCheck.setState(true);
				
				rightPanel.add(crossesCheck);
				
			thePanel.add(rightPanel);
		}
		else {
			theView.setShowCrosses(false);
			
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				XLabel freqLabel = new XLabel(translate("Make class width") + ":", XLabel.LEFT, this);
				freqLabel.setFont(getStandardBoldFont());
			thePanel.add(freqLabel);
			
				narrowerButton = new XButton(translate("Narrower"), this);
			thePanel.add(narrowerButton);
			
				widerButton = new XButton(translate("Wider"), this);
			thePanel.add(widerButton);
		}
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == crossesCheck) {
			theView.setShowCrosses(crossesCheck.getState());
			return true;
		}
		else if (target == narrowerButton) {
			if (currentGrouping == maxGrouping)
				widerButton.enable();
			currentGrouping --;
			if (currentGrouping == 1)
				narrowerButton.disable();
			freqAxis.setAlternateLabels(currentGrouping - 1);
			theView.changeClassGrouping(currentGrouping);
			return true;
		}
		else if (target == widerButton) {
			if (currentGrouping == 1)
				narrowerButton.enable();
			currentGrouping ++;
			if (currentGrouping == maxGrouping)
				widerButton.disable();
			freqAxis.setAlternateLabels(currentGrouping - 1);
			theView.changeClassGrouping(currentGrouping);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}