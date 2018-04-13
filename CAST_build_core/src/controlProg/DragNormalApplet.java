package controlProg;

import java.awt.*;

import axis.*;
import utils.*;
import dataView.*;
import control.*;
import distn.*;
import valueList.ProportionView;


public class DragNormalApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String NORM_DIST__PARAM = "normParams";
	static final private String GAMMA_DIST__PARAM = "gammaParams";
	
	static final private String kNormKey = "norm";
	static final private String kGammaKey = "gamma";
	
	private DensityView theView;
	private XChoice distnChoice;
	private XCheckbox axisDisplayCheck;
	private MeanSDAxis theHorizAxis;
	private ProportionView thePropn;
	
	public void setupApplet() {
		DataSet data = new DataSet();
		
		NormalDistnVariable normVar = new NormalDistnVariable(getParameter(VAR_NAME_PARAM));
		normVar.setParams(getParameter(NORM_DIST__PARAM));
		data.addVariable(kNormKey, normVar);
		
		GammaDistnVariable gammaVar = new GammaDistnVariable(getParameter(VAR_NAME_PARAM));
		gammaVar.setParams(getParameter(GAMMA_DIST__PARAM));
		data.addVariable(kGammaKey, gammaVar);
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		add("North", topPanel(data));
	}
	
	private XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		thePropn = new ProportionView(data, kNormKey, this);
		thePanel.add(thePropn);
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theHorizAxis = new MeanSDAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		theView = new DensityView(data, this, theHorizAxis, DensityView.SYMMETRIC);
		theView.setActiveDistnVariable(kNormKey);
		theHorizAxis.setView(theView);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
		distnChoice = new XChoice(this);
		distnChoice.addItem(translate("Symmetric distribution"));
		distnChoice.addItem(translate("Skew distribution"));
		distnChoice.select(0);
		thePanel.add(distnChoice);
		
		axisDisplayCheck = new XCheckbox(translate("Show mean and st devn"), this);
		axisDisplayCheck.setState(false);
		thePanel.add(axisDisplayCheck);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == distnChoice)
			switch (distnChoice.getSelectedIndex()) {
				case 1:
					theView.setActiveDistnVariable(kGammaKey);
					theView.resetDensities();
					theView.repaint();
					thePropn.setVariableKey(kGammaKey);
					return true;
				default:
					theView.setActiveDistnVariable(kNormKey);
					theView.resetDensities();
					theView.repaint();
					thePropn.setVariableKey(kNormKey);
					return true;
			}
		
		if (target == axisDisplayCheck) {
			int newState = axisDisplayCheck.getState() ? MeanSDAxis.MEAN_SD : MeanSDAxis.VALUES;
			theHorizAxis.setDrawMode(newState);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}