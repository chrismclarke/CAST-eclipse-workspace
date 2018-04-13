package twoGroupProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;
import graphics3D.*;

import twoGroup.*;


public class RotateAnovaParamApplet extends RotateAnovaPDFApplet {
	static final private String MEAN_PARAM = "means";
	static final private String SD_PARAM = "stDevns";
	static final private String MIN_SD_PARAM = "minSD";
	static final private String EQUAL_SD_CHECK_PARAM = "equalSDCheck";
	static final private String SHOW_50BAND_PARAM = "show50PercentBand";
	
	static final private Color kGreenColor = new Color(0x006600);
	
	static final private Color kMiddleColor = new Color(0xCCCCCC);
	static final private Color kTailColor = new Color(0x999999);
	
	protected ParameterSlider meanSlider[] = null;
	protected ParameterSlider sdSlider[] = null;
	
	private NumValue minMean, maxMean, minSD, maxSD;
	private int meanSteps, sdSteps;
	
	protected XCheckbox equalSDCheck;
	
	public void setupApplet() {
		StringTokenizer theParams = new StringTokenizer(getParameter(MEAN_PARAM));
		minMean = new NumValue(theParams.nextToken());
		maxMean = new NumValue(theParams.nextToken());
		meanSteps = Integer.parseInt(theParams.nextToken());
		
		theParams = new StringTokenizer(getParameter(SD_PARAM));
		minSD = new NumValue(theParams.nextToken());
		maxSD = new NumValue(theParams.nextToken());
		sdSteps = Integer.parseInt(theParams.nextToken());
		
		super.setupApplet();
	}
	
	protected Rotate3DView getView(DataSet data, D3Axis xAxis, D3Axis yAxis, D3Axis densityAxis) {
		RotateAnovaPDFView theView = (RotateAnovaPDFView)super.getView(data, xAxis, yAxis, densityAxis);
		
		String show50BandString = getParameter(SHOW_50BAND_PARAM);
		if (show50BandString != null && show50BandString.equals("true")) {
			theView.setShow50PercentBand(true);
			theView.setDensityColors(kMiddleColor, kTailColor);
		}
		
		String minSDString = getParameter(MIN_SD_PARAM);
		if (minSDString != null) {
			double minSD = Double.parseDouble(minSDString);
			theView.setFixedMinSD(minSD);
		}
		
		theView.setShowSDBand(true);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
//		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new BorderLayout(10, 0));
			
			CatVariable xVar = (CatVariable)data.getVariable("x");
			int nGroups = xVar.noOfCategories();
			meanSlider = new ParameterSlider[nGroups];
			sdSlider = new ParameterSlider[nGroups];
		
		if (nGroups == 2) {
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new ProportionLayout(0.5, 8, ProportionLayout.HORIZONTAL,
																					ProportionLayout.TOTAL));
				sliderPanel.add(ProportionLayout.LEFT, oneGroupPanel(data, 0));
				sliderPanel.add(ProportionLayout.RIGHT, oneGroupPanel(data, 1));
				
			thePanel.add("Center", sliderPanel);
		}
		else {
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new ProportionLayout(0.5, 8, ProportionLayout.HORIZONTAL,
																					ProportionLayout.TOTAL));
				sliderPanel.add(ProportionLayout.LEFT, meanPanel(data, nGroups));
				sliderPanel.add(ProportionLayout.RIGHT, sdPanel(data, nGroups));
				
			thePanel.add("Center", sliderPanel);
		}
		
		String equalSDString = getParameter(EQUAL_SD_CHECK_PARAM);
		if (equalSDString == null || equalSDString.equals("true")) {
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																				VerticalLayout.VERT_CENTER, 0));
			equalSDCheck = new XCheckbox(translate("Equal st devn"), this);
			checkPanel.add(equalSDCheck);
			
			thePanel.add("East", checkPanel);
		}
		
		return thePanel;
	}
	
	private XPanel meanPanel(DataSet data, int nGroups) {
		GroupsImages.loadGroups(this);
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
		
		for (int group=0 ; group<nGroups ; group++) {
			String muGroup = MText.expandText("#mu##sub" + group + "#");
			meanSlider[group] = new ParameterSlider(minMean, maxMean,
																	model.getMean(group), meanSteps, muGroup,
																	ParameterSlider.NO_SHOW_MIN_MAX, this);
			meanSlider[group].setForeground(Color.blue);
			thePanel.add(meanSlider[group]);
		}
		return thePanel;
	}
	
	private XPanel sdPanel(DataSet data, int nGroups) {
		GroupsImages.loadGroups(this);
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
		
		for (int group=0 ; group<nGroups ; group++) {
			String sigGroup = MText.expandText("#sigma##sub" + group + "#");
			sdSlider[group] = new ParameterSlider(minSD, maxSD,
																	model.getSD(group), sdSteps, sigGroup,
																	ParameterSlider.NO_SHOW_MIN_MAX, this);
			sdSlider[group].setForeground(kGreenColor);
//			sdSlider[group].setSliderColor(XSlider.GREEN);
			thePanel.add(sdSlider[group]);
		}
		return thePanel;
	}
	
	private XPanel oneGroupPanel(DataSet data, int group) {
		GroupsImages.loadGroups(this);
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			String muGroup = MText.expandText("#mu##sub" + group + "#");
			meanSlider[group] = new ParameterSlider(minMean, maxMean,
																	model.getMean(group), meanSteps, muGroup,
																	ParameterSlider.NO_SHOW_MIN_MAX, this);
			meanSlider[group].setForeground(Color.blue);
			thePanel.add(meanSlider[group]);
			
			String sigGroup = MText.expandText("#sigma##sub" + group + "#");
			sdSlider[group] = new ParameterSlider(minSD, maxSD,
																	model.getSD(group), sdSteps, sigGroup,
																	ParameterSlider.NO_SHOW_MIN_MAX, this);
			sdSlider[group].setForeground(kGreenColor);
//			sdSlider[group].setSliderColor(XSlider.GREEN);
			thePanel.add(sdSlider[group]);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		GroupsModelVariable model = (GroupsModelVariable)data.getVariable("model");
		CatVariable xVar = (CatVariable)data.getVariable("x");
		int nGroups = xVar.noOfCategories();
		
		if (target == equalSDCheck) {
			if (equalSDCheck.getState()) {
				for (int i=1 ; i<nGroups ; i++)
					sdSlider[i].show(false);
				String sig = MText.expandText("#sigma#");
				sdSlider[0].setTitle(sig, this);
				model.setSD(sdSlider[0].getParameter().toDouble());
				data.variableChanged("model");
			}
			else {
				double commonSd = sdSlider[0].getParameter().toDouble();
				for (int i=1 ; i<nGroups ; i++)
					sdSlider[i].setParameter(commonSd);
				String sig1 = MText.expandText("#sigma##sub1#");
				sdSlider[0].setTitle(sig1, this);
				for (int i=1 ; i<nGroups ; i++)
					sdSlider[i].show(true);
			}
			
			return true;
		}
		else
			for (int i=0 ; i<nGroups ; i++)
				if (target == meanSlider[i]) {
					model.setMean(meanSlider[i].getParameter().toDouble(), i);
					data.variableChanged("model");
					return true;
				}
				else if (target == sdSlider[i]) {
					double newSD = sdSlider[i].getParameter().toDouble();
					if (equalSDCheck != null && equalSDCheck.getState())
						model.setSD(newSD);
					else
						model.setSD(newSD, i);
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