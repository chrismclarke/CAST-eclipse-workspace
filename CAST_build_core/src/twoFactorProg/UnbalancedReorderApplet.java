package twoFactorProg;

import java.awt.*;

import dataView.*;
import utils.*;

import twoFactor.*;


public class UnbalancedReorderApplet extends FactorAnovaReorderApplet {
	static final private String NO_TO_DELETE_PARAM = "noToDelete";
	static final private String HIGH_REPS_NAME_PARAM = "highRepsName";
	
	private double allY[];
	private int allX[];
	private int allZ[];
	
	private IntegerSlider highRepsSlider;
	
	public void setupApplet() {
		super.setupApplet();
		
		add("North", deleteSliderPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		String xKeys[] = getXKeys();
		CatVariable xVar = (CatVariable)data.getVariable(xKeys[0]);
		CatVariable zVar = (CatVariable)data.getVariable(xKeys[1]);
		int n = yVar.noOfValues();
		allY = new double[n];
		allX = new int[n];
		allZ = new int[n];
		for (int i=0 ; i<n ; i++) {
			allY[i] = yVar.doubleValueAt(i);
			allX[i] = xVar.getItemCategory(i);
			allZ[i] = zVar.getItemCategory(i);
		}
		
		return data;
	}
	
	private XPanel deleteSliderPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(100, 0, 100, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
			
			String highRepsName = getParameter(HIGH_REPS_NAME_PARAM);
			int noToDelete = Integer.parseInt(getParameter(NO_TO_DELETE_PARAM));
			int maxHighReps = noToDelete + 1;
			highRepsSlider = new IntegerSlider(highRepsName, 1, maxHighReps,
															maxHighReps, XSlider.HORIZONTAL, this, true);
		thePanel.add("Center", highRepsSlider);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == highRepsSlider) {
			int noToDelete = highRepsSlider.getMaxValue() - highRepsSlider.getValue();
			NumVariable yVar = (NumVariable)data.getVariable("y");
			String xKeys[] = getXKeys();
			CatVariable xVar = (CatVariable)data.getVariable(xKeys[0]);
			CatVariable zVar = (CatVariable)data.getVariable(xKeys[1]);
			int n = allY.length;
			double newY[] = new double[n - noToDelete];
			int newX[] = new int[n - noToDelete];
			int newZ[] = new int[n - noToDelete];
			System.arraycopy(allY, 0, newY, 0, newY.length);
			System.arraycopy(allX, 0, newX, 0, newX.length);
			System.arraycopy(allZ, 0, newZ, 0, newZ.length);
			
			yVar.setValues(newY);
			xVar.setValues(newX);
			zVar.setValues(newZ);
			
			TwoFactorModel lsModel = (TwoFactorModel)data.getVariable("ls");
			lsModel.updateLSParams("y");
						
			data.variableChanged("y");
			
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}