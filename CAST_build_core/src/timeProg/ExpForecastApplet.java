package timeProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import time.*;


class LagSlider extends XValueAdjuster {
	public LagSlider(String title, int maxLag, XApplet applet) {
		super(title, 0, maxLag, 0, applet);
	}
	
	protected Value translateValue(int scrollValue) {
		return new NumValue(getLag(scrollValue), 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return translateValue(getMaxValue()).stringWidth(g);
	}
	
	public int getLag(int scrollValue) {
		return scrollValue;
	}
	
	public int getLag() {
		return getLag(getValue());
	}
}

public class ExpForecastApplet extends ExpSmoothApplet {
	static final private String FORECAST_VAR_NAME_PARAM = "forecastName";
	static final private String MAX_LAG_PARAM = "maxLag";
	static final private String MAX_SSQ_PARAM = "maxErrorSsq";
	
	private LagSlider lagSlider;
	
	
	public void setupApplet() {
		super.setupApplet();
		
		getView().setSourceShading(true);
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		smoothVariable = new ExpForecastVariable(getParameter(FORECAST_VAR_NAME_PARAM),
																						data, "y", 0);
		smoothVariable.setExtraDecimals(2);
		data.addVariable("smooth", smoothVariable);
		
		return data;
	}
	
	protected TimeView createTimeView(TimeAxis theHorizAxis, VertAxis theVertAxis) {
		return new ForecastView(getData(), this, theHorizAxis, theVertAxis);
	}
	
	protected boolean showErrorSsq() {
		return true;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = super.controlPanel(data);
		
		thePanel.setLayout(new BorderLayout(10, 3));
		
		thePanel.add("Center", createExpSmoothSlider());
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 7));
				int maxLag = Integer.parseInt(getParameter(MAX_LAG_PARAM));
				lagSlider = new LagSlider("Look-ahead time:", maxLag, this);
			rightPanel.add(lagSlider);
			if (showErrorSsq()) {
				ErrorSsqValueView ssq = new ErrorSsqValueView(data, this, "y", "smooth", new NumValue(getParameter(MAX_SSQ_PARAM)));
				ssq.setFont(getStandardBoldFont());
				ssq.setForeground(Color.red);
				rightPanel.add(ssq);
			}
		
		thePanel.add("East", rightPanel);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == lagSlider) {
			((ExpForecastVariable)smoothVariable).setLag(lagSlider.getLag());
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean  action(Event  evt, Object  what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}