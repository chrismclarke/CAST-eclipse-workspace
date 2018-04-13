package ssqProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import formula.*;


import ssq.*;

public class ComponentsSsqApplet extends ComponentsApplet {
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String R2_LABELS_PARAM = "r2Labels";
	
	static final private int kR2Decimals = 3;
	
	protected SummaryDataSet summaryData;
	protected NumValue maxSsq;
	
	private ComponentEqnPanel theEquation;
	
	private XChoice dataSetChoice;
	
	public void setupApplet() {
		super.setupApplet();
		
		dataSetChoice = ((CoreModelDataSet)data).dataSetChoice(this);
		if (dataSetChoice != null) {
			XPanel dataChoicePanel = new XPanel();
			dataChoicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			dataChoicePanel.add(dataSetChoice);
			add("North", dataChoicePanel);
		}
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		maxSsq = new NumValue(getParameter(MAX_SSQ_PARAM));
		summaryData = new AnovaSummaryData(data, "error", BasicComponentVariable.kComponentKey,
												maxSsq.decimals, kR2Decimals);
		
		summaryData.setSingleSummaryFromData();
		
		return data;
	}
	
	
	protected XPanel dataDisplayPanel(DataSet data, ComponentEqnPanel equationPanel,
																																				boolean showSD) {
		return super.dataDisplayPanel(data, theEquation, true);
	}
	
	protected XPanel leftControlPanel(DataSet data, double initialR2) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
			String r2Labels = getParameter(R2_LABELS_PARAM);
//			R2Slider r2Slider;
			if (r2Labels == null)
				r2Slider = new R2Slider(this, data, "y", "y", summaryData, translate("Variability"), initialR2);
			else {
				StringTokenizer st = new StringTokenizer(r2Labels, "#");
				r2Slider = new R2Slider(this, data, "y", "y", summaryData, st.nextToken(),
																							st.nextToken(), st.nextToken(), initialR2);
			}
			
		thePanel.add(r2Slider);
		return thePanel;
	}
	
	protected XPanel ssqPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 15));
			
				
			String keys[] = BasicComponentVariable.kComponentKey;
			if (explainedNotWithin)
				AnovaImages.loadRegnImages(this);
			else
				AnovaImages.loadGroupImages(this);
			Image[] compImage = explainedNotWithin ? AnovaImages.basicRegnSsqs : AnovaImages.basicGroupSsqs;
			Color[] compColor = BasicComponentVariable.kComponentColor;
			int imageWidth = explainedNotWithin ? AnovaImages.kSsqWidth : AnovaImages.kSsq2Width;
			
			FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
			theEquation = new ComponentEqnPanel(summaryData, keys, maxSsq, compImage, compColor, imageWidth,
																					AnovaImages.kSsqHeight, bigContext);
			
			thePanel.add(theEquation);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data, double initialR2) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																ProportionLayout.TOTAL));
		
		thePanel.add(ProportionLayout.LEFT, leftControlPanel(data, initialR2));
			
		thePanel.add(ProportionLayout.RIGHT, ssqPanel(data));
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			if (((CoreModelDataSet)data).changeDataSet(dataSetChoice.getSelectedIndex())) {
				summaryData.takeSample();
				r2Slider.updateForNewR2();
//				data.variableChanged("y");
//				summaryData.setSingleSummaryFromData();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}