package exerciseSDProg;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import exercise2.*;

import exerciseSD.*;


public class GuessSDNormalApplet extends GuessSDApplet {
	private HorizAxis normalAxis;
	private NormalDragView normalView;
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			normalAxis = new HorizAxis(this);
		thePanel.add("Bottom", normalAxis);
		
			normalView = new NormalDragView(data, this, normalAxis, "y", "y");
			normalView.lockBackground(Color.white);
		thePanel.add("Center", normalView);
		
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		normalAxis.readNumLabels(getAxisInfo());
		normalAxis.setAxisName(getVarName());	
		int decimals = getDecimals();
		normalView.setMeanSdDecimals(decimals, decimals);
		
		normalView.setShow4s(false);
		normalView.resetClasses();
		
		normalAxis.invalidate();
		normalView.repaint();
		
		resultPanel.clear();
	}
	
	protected void setDataForQuestion() {
		NumValue mean = getMean();
		NumValue sd = getSD();
		
		NormalDistnVariable yVar = (NormalDistnVariable)data.getVariable("y");
		yVar.setMean(mean.toDouble());
		yVar.setSD(sd.toDouble());
		yVar.setDecimals(mean.decimals, sd.decimals);
	}
	
	
//-----------------------------------------------------------
	
	protected String distnOrDataString() {
		return "normal distribution";
	}
	
	protected String sdString() {
		return "#sigma#";
	}
	
	protected void insertHints(MessagePanel messagePanel) {
		messagePanel.insertText("The middle two thirds of the area spans about 2#sigma# (i.e. #mu# #plusMinus# #sigma#).\n");
		messagePanel.insertText("The middle 95% of the area spans about 4#sigma# (i.e. #mu# #plusMinus# 2#sigma#).\n");
		messagePanel.insertText("Virtually all the area spans about 6#sigma# (i.e. #mu# #plusMinus# 3#sigma#).");
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		NormalDistnVariable yVar = new NormalDistnVariable("Normal");
		data.addVariable("y", yVar);
		
		return data;
	}
	
	protected CoreDragView getCurrentView() {
		return normalView;
	}
	
	
}