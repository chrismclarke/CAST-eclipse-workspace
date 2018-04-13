package timeProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;

import time.*;


public class SeasonalApplet extends BasicTimeApplet {
	static final protected String SMOOTH_VAR_NAME_PARAM = "smoothName";
	
	protected SeasonSmoothVariable smoothVariable;
	private ExpSmoothSlider aSlider;
	private XCheckbox seasonalCheck, linearCheck, smoothCheck, residCheck;
	
	public void setupApplet() {
		super.setupApplet();
		
		getView().setCrossSize(DataView.SMALL_CROSS);
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		smoothVariable = createSmoothedVariable(data, "y");
		smoothVariable.setExtraDecimals(2);
		data.addVariable("smooth", smoothVariable);
		
		smoothVariable.setSeasonShow(false);
		smoothVariable.setLinearShow(false);
		smoothVariable.setExpSmoothShow(false);
		smoothVariable.setResidShow(false);
		smoothVariable.setExpSmoothConst(0.3);
		
		return data;
	}
	
	protected SeasonSmoothVariable createSmoothedVariable(DataSet data, String key) {
		return new SeasonSmoothVariable(getParameter(SMOOTH_VAR_NAME_PARAM), data, key);
	}
	
	protected TimeAxis horizAxis(DataSet data) {
		TimeAxis ax = super.horizAxis(data);
		if (ax instanceof SeasonTimeAxis) {
			SeasonTimeAxis theHorizAxis = (SeasonTimeAxis)ax;
			smoothVariable.setSeasonInfo(theHorizAxis.getNoOfSeasons(), theHorizAxis.getFirstValSeason());
		}
		return ax;
	}
	
	protected String getCrossKey() {
		return "y";
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"smooth"};
		return keys;
	}
	
	protected boolean showDataValue() {
		return false;
	}
	
	protected boolean showSmoothedValue() {
		return false;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				XPanel innerCheckPanel = new XPanel();
				innerCheckPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 3));
				
					XPanel col1Panel = new XPanel();
					col1Panel.setLayout(new VerticalLayout());
					seasonalCheck = new XCheckbox("Seasonal", this);
					seasonalCheck.setState(smoothVariable.getSeasonShow());
					col1Panel.add(seasonalCheck);
					linearCheck = new XCheckbox("Linear", this);
					linearCheck.setState(smoothVariable.getLinearShow());
					col1Panel.add(linearCheck);
					
					XPanel col2Panel = new XPanel();
					col2Panel.setLayout(new VerticalLayout());
					smoothCheck = new XCheckbox("Exp smooth", this);
					smoothCheck.setState(smoothVariable.getExpSmoothShow());
					col2Panel.add(smoothCheck);
					residCheck = new XCheckbox(translate("Residual"), this);
					residCheck.setState(smoothVariable.getResidShow());
					col2Panel.add(residCheck);
					
				innerCheckPanel.add(col1Panel);
				innerCheckPanel.add(col2Panel);
				
			checkPanel.add(innerCheckPanel);
		
		thePanel.add("West", checkPanel);
		
//			XPanel col3Panel = new XPanel();
			aSlider = new ExpSmoothSlider(smoothVariable.getExpSmoothConst(), this);
//			col3Panel.add(aSlider);
		
		thePanel.add("Center", aSlider);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == aSlider) {
			smoothVariable.setExpSmoothConst(aSlider.getExpSmoothConst());
			return true;
		}
		if (target == seasonalCheck) {
			smoothVariable.setSeasonShow(seasonalCheck.getState());
			return true;
		}
		if (target == linearCheck) {
			smoothVariable.setLinearShow(linearCheck.getState());
			return true;
		}
		if (target == smoothCheck) {
			smoothVariable.setExpSmoothShow(smoothCheck.getState());
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