package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;
import imageGroups.*;
import graphics3D.*;
import twoGroup.*;


public class AnovaModelApplet extends RotateAnovaPDFApplet {
	static final private String MEAN_PARAM = "means";
	static final private String SD_PARAM = "stDevns";
	
//	static final private Color kGreenColor = new Color(0x006600);
	
	private ParameterSlider meanSlider[];
	private ParameterSlider sdSlider;
	
	private NumValue minMean, maxMean, minSD, maxSD;
	private int sdSteps;
	
	public void setupApplet() {
		StringTokenizer theParams = new StringTokenizer(getParameter(MEAN_PARAM));
		minMean = new NumValue(theParams.nextToken());
		maxMean = new NumValue(theParams.nextToken());
		@SuppressWarnings("unused")
		int meanSteps = Integer.parseInt(theParams.nextToken());
		
		theParams = new StringTokenizer(getParameter(SD_PARAM));
		minSD = new NumValue(theParams.nextToken());
		maxSD = new NumValue(theParams.nextToken());
		sdSteps = Integer.parseInt(theParams.nextToken());
		
		super.setupApplet();
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		RotateAnovaPDFView theView = (RotateAnovaPDFView)super.getView(data, xAxis, yAxis, densityAxis);
		theView.setShowSDBand(true);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
//		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		thePanel.add(rotationPanel());
		
		addSampleButton(thePanel);
		
		return thePanel;
	}
	
	protected int rotationPanelOrientation() {
		return RotateButton.HORIZONTAL;
	}
	
	protected XPanel eastPanel(DataSet data) {
		MeanSDImages.loadMeanSD(this);
		
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int noOfGroups = xVar.noOfCategories();
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL,
																	VerticalLayout.VERT_CENTER, 10));
		
		meanSlider = new ParameterSlider[noOfGroups];
		for (int i=0 ; i<noOfGroups ; i++) {
			meanSlider[i] = new ParameterSlider(minMean, maxMean, model.getMean(i),
													translate("Mean") + " " + xVar.getLabel(i).toString(),
													ParameterSlider.NO_SHOW_MIN_MAX, this);
			meanSlider[i].setForeground(Color.blue);
			thePanel.add(meanSlider[i]);
		}
		
		thePanel.add(new Separator(0.3, 5));
		
			String sigma = MText.expandText("#sigma#");
			sdSlider = new ParameterSlider(minSD, maxSD, model.evaluateSD(), sdSteps, sigma,
															ParameterSlider.NO_SHOW_MIN_MAX, this);
			sdSlider.setForeground(Color.red);
		thePanel.add(sdSlider);
		
		return thePanel;
	}
	
	
	private boolean localAction(Object target) {
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		
		if (target == sdSlider) {
			double newSD = sdSlider.getParameter().toDouble();
			model.setSD(newSD);
			data.variableChanged("model");
			return true;
		}
		else
			for (int i=0 ; i<meanSlider.length ; i++)
				if (target == meanSlider[i]) {
					model.setMean(meanSlider[i].getParameter().toDouble(), i);
					data.variableChanged("model");
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