package timeProg;

import java.awt.*;

import dataView.*;
import utils.*;


public class Simple2SeasonalApplet extends SeasonalApplet {
	static final private String EXP_CONST_PARAM = "expConst";
	
	private XCheckbox seasonalCheck, trendCheck, residCheck;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		String constString = getParameter(EXP_CONST_PARAM);
		smoothVariable.setExpSmoothConst((new NumValue(constString)).toDouble());
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, 3));
		
		trendCheck = new XCheckbox("Trend component", this);
		trendCheck.setState(false);
		thePanel.add(trendCheck);
		seasonalCheck = new XCheckbox("Seasonal component", this);
		seasonalCheck.setState(false);
		thePanel.add(seasonalCheck);
		residCheck = new XCheckbox("Residual component", this);
		residCheck.setState(false);
		thePanel.add(residCheck);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == seasonalCheck) {
			smoothVariable.setSeasonShow(seasonalCheck.getState());
			return true;
		}
		if (target == trendCheck) {
			synchronized (getData()) {
				smoothVariable.setLinearShow(trendCheck.getState());
				smoothVariable.setExpSmoothShow(trendCheck.getState());
			}
			return true;
		}
		if (target == residCheck) {
			smoothVariable.setResidShow(residCheck.getState());
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