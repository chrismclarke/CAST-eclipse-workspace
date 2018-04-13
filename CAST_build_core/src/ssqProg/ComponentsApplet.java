package ssqProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;


import ssq.*;

public class ComponentsApplet extends XApplet {
	
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String INITIAL_R2_PARAM = "initialR2";
	static final private String RESID_AXIS_PARAM = "residAxis";
	static final private String COMPONENT_NAME_PARAM = "componentName";
	static final private String EXPLAINED_IMAGES_PARAM = "explainedImages";
	
	protected boolean xNumNotCat, explainedNotWithin;
	protected DataSet data;
	protected R2Slider r2Slider;
	
	public void setupApplet() {
		xNumNotCat = (getParameter(X_LABELS_PARAM) == null);
		explainedNotWithin = xNumNotCat;
		String explainedImagesString = getParameter(EXPLAINED_IMAGES_PARAM);
		if (explainedImagesString != null)
			explainedNotWithin = explainedImagesString.equals("true");
			
		if (explainedNotWithin)
			AnovaImages.loadRegnImages(this);
		else
			AnovaImages.loadGroupImages(this);
		
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
			double initialR2 = Double.parseDouble(getParameter(INITIAL_R2_PARAM));
		add("South", controlPanel(data, initialR2));
		
		add("Center", dataDisplayPanel(data, null, false));
	}
	
	protected DataSet readData() {
		CoreModelDataSet data;
		if (xNumNotCat)
			data = new SimpleRegnDataSet(this);
		else
			data = new GroupsDataSet(this);
		
		data.addBasicComponents();
		
		return data;
	}
	
	protected XPanel dataDisplayPanel(DataSet data, ComponentEqnPanel equationPanel,
																																	boolean showSD) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																ProportionLayout.TOTAL));
			
			DataWithComponentsPanel scatterPanel = new DataWithComponentsPanel(this);
			scatterPanel.setupPanel(data, "x", "y", "ls", null, BasicComponentVariable.TOTAL, this);
			scatterPanel.getView().setStickyDrag(true);
		thePanel.add(ProportionLayout.LEFT, scatterPanel);
		
			String residAxisInfo = getParameter(RESID_AXIS_PARAM);
			StringTokenizer st = new StringTokenizer(getParameter(COMPONENT_NAME_PARAM), "#");
			String componentName[] = new String[3];
			for (int i=0 ; i<3 ; i++)
				componentName[i] = st.nextToken();
			ComponentsPanel componentPanel = new ComponentsPanel(data, residAxisInfo,
										componentName, BasicComponentVariable.kComponentKey,
										BasicComponentVariable.kComponentColor, BasicComponentVariable.kComponentType,
										0, scatterPanel.getView(), equationPanel, showSD, this);
			
		thePanel.add(ProportionLayout.RIGHT, componentPanel);
			
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, double initialR2) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.25, 0, ProportionLayout.HORIZONTAL,
															ProportionLayout.TOTAL));
		thePanel.add(ProportionLayout.LEFT, new XPanel());
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new ProportionLayout(0.67, 0, ProportionLayout.HORIZONTAL,
																ProportionLayout.TOTAL));
				r2Slider = new R2Slider(this, data, "y", "y", null, translate("Variability"), initialR2);
			innerPanel.add(ProportionLayout.LEFT, r2Slider);
			innerPanel.add(ProportionLayout.RIGHT, new XPanel());
		thePanel.add(ProportionLayout.RIGHT, innerPanel);
		
		return thePanel;
	}
}