package structureProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;
import coreVariables.*;
import imageUtils.*;

import structure.*;


public class MultiLevelDataApplet extends XApplet {
	private final static String NO_CORE_VARS_PARAM = "nCoreVars";
	private final static String INDEX_NAME_PARAM = "indexName";
	
	private final static String HIGHER_DATA_NAME_PARAM = "higherDataName";
	private final static String HIGHER_LABEL_NAME_PARAM = "higherLabelName";
	private final static String HIGHER_LABELS_PARAM = "higherLabels";
	private final static String HIGHER_CAT_LABELS_PARAM = "higherCatLabels";
	private final static String HIGHER_VAR_NAME_PARAM = "higherVarName";
	private final static String HIGHER_VALUES_PARAM = "higherValues";
	
	private final static String LOWER_DATA_NAME_PARAM = "lowerDataName";
	private final static String LINK_INDICES_PARAM = "linkIndices";
	private final static String LOWER_LABEL_NAME_PARAM = "lowerLabelName";
	private final static String LOWER_LABELS_PARAM = "lowerLabels";
	private final static String LOWER_CAT_LABELS_PARAM = "lowerCatLabels";
	private final static String LOWER_VAR_NAME_PARAM = "lowerVarName";
	private final static String LOWER_VALUES_PARAM = "lowerValues";
	
	private final static String NO_DERIVED_PARAM = "nDerivedVars";
	private final static String COPY_PARAM = "copy";
	private final static String SUMMARY_PARAM = "sum";
	
	static final private String kArrowImageName[] = {"greenDownArrow.png", "greenUpArrow.png"};
	
	private int nExtraLowVars, nExtraHighVars;
	private String highCopyKey[], copyName[];
	private String sumSourceKey[], sumName[];
	private int sumType[], decimals[];
	
	private MultiLevelScrollList highList, lowList;
	
	private XChoice linkChoice;
	private int currentLinkChoice = 0;
	private ImageSwapCanvas arrowCanvas1, arrowCanvas2;
	
	public void setupApplet() {
//		ScrollImages.loadScroll(this);
		
		StringTokenizer st = new StringTokenizer(getParameter(NO_CORE_VARS_PARAM));
		int noOfHigherVars = Integer.parseInt(st.nextToken());
		int noOfLowerVars = Integer.parseInt(st.nextToken());
		
		readExtraVarInfo();
		
		st = new StringTokenizer(getParameter(INDEX_NAME_PARAM), "#");
		String higherIndexName = st.nextToken();
		String lowerIndexName = st.nextToken();
		
		MultiLevelDataSet higherData = getHigherData(noOfHigherVars, higherIndexName);
		MultiLevelDataSet lowerData = getLowerData(noOfLowerVars, lowerIndexName, higherData);
		higherData.setLinkedData(lowerData);
		lowerData.setLinkedData(higherData);
		addExtraVariables(higherData, lowerData, nExtraHighVars, nExtraLowVars);
		higherData.setSelection(0);
		
		if (nExtraLowVars == 0 && nExtraHighVars == 0) {
			setLayout(new ProportionLayout(0.5, 10, ProportionLayout.HORIZONTAL,
																											ProportionLayout.REMAINDER));
			add(ProportionLayout.LEFT, dataTablePanel(higherData, getParameter(HIGHER_DATA_NAME_PARAM),
																												noOfHigherVars, nExtraHighVars, "sum"));
			add(ProportionLayout.RIGHT, dataTablePanel(lowerData, getParameter(LOWER_DATA_NAME_PARAM),
																												noOfLowerVars, nExtraLowVars, "copy"));
		}
		else {
			setLayout(new ProportionLayout(0.55, 10, ProportionLayout.VERTICAL,
																											ProportionLayout.TOTAL));
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new BorderLayout(0, 10));
				topPanel.add("Center", dataTablePanel(higherData, getParameter(HIGHER_DATA_NAME_PARAM),
																												noOfHigherVars, nExtraHighVars, "sum"));
					XPanel controlPanel = new XPanel();
					controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
						arrowCanvas1 = new ImageSwapCanvas(kArrowImageName, this, 46, 36);
					controlPanel.add(arrowCanvas1);
					
						linkChoice = new XChoice(this);
						for (int i=0 ; i<nExtraLowVars ; i++)
							linkChoice.addItem(copyName[i]);
						for (int i=0 ; i<nExtraHighVars ; i++)
							linkChoice.addItem(sumName[i]);
					controlPanel.add(linkChoice);
						
						arrowCanvas2 = new ImageSwapCanvas(kArrowImageName, this, 46, 36);
					controlPanel.add(arrowCanvas2);
					
				topPanel.add("South", controlPanel);
			
			add(ProportionLayout.TOP, topPanel);
			add(ProportionLayout.BOTTOM, dataTablePanel(lowerData, getParameter(LOWER_DATA_NAME_PARAM),
																												noOfLowerVars, nExtraLowVars, "copy"));
			hiliteLinkVariable(0);
		}
	}
	
	private void readExtraVarInfo() {
		String extraParamString = getParameter(NO_DERIVED_PARAM);
		if (extraParamString != null) {
			StringTokenizer st = new StringTokenizer(extraParamString);
			nExtraLowVars = Integer.parseInt(st.nextToken());
			nExtraHighVars = Integer.parseInt(st.nextToken());
		}
		
		highCopyKey = new String[nExtraLowVars];
		copyName = new String[nExtraLowVars];
		for (int i=0 ; i<nExtraLowVars ; i++) {
			StringTokenizer st = new StringTokenizer(getParameter(COPY_PARAM + i), "#");
			highCopyKey[i] = st.nextToken();
			copyName[i] = st.nextToken();
		}
		
		sumSourceKey = new String[nExtraHighVars];
		sumName = new String[nExtraHighVars];
		sumType = new int[nExtraHighVars];
		decimals = new int[nExtraHighVars];
		for (int i=0 ; i<nExtraHighVars ; i++) {
			StringTokenizer st = new StringTokenizer(getParameter(SUMMARY_PARAM + i), "#");
			sumType[i] = SummariseUpVariable.decodeSummaryType(st.nextToken());
			sumName[i] = st.nextToken();
			decimals[i] = Integer.parseInt(st.nextToken());
			if (st.hasMoreTokens())
				sumSourceKey[i] = st.nextToken();
		}
	}
	
	private MultiLevelDataSet getHigherData(int noOfVars, String higherIndexName) {
		MultiLevelDataSet data = new MultiLevelDataSet();
		
		readCoreVariables(data, HIGHER_LABEL_NAME_PARAM, HIGHER_LABELS_PARAM,
							noOfVars, HIGHER_CAT_LABELS_PARAM, HIGHER_VAR_NAME_PARAM, HIGHER_VALUES_PARAM);
		
		int noOfValues = ((Variable)data.getVariable("y0")).noOfValues();
		data.addVariable("index", new IndexVariable(higherIndexName, noOfValues));
		
		return data;
	}
	
	private MultiLevelDataSet getLowerData(int noOfVars, String lowerIndexName,
																													MultiLevelDataSet higherData) {
		MultiLevelDataSet data = new MultiLevelDataSet(getParameter(LINK_INDICES_PARAM));
		
		readCoreVariables(data, LOWER_LABEL_NAME_PARAM, LOWER_LABELS_PARAM,
							noOfVars, LOWER_CAT_LABELS_PARAM, LOWER_VAR_NAME_PARAM, LOWER_VALUES_PARAM);
		
		IndexVariable higherIndex = (IndexVariable)higherData.getVariable("index");
		data.addVariable("index", new CopyIndexedVariable(higherIndex, lowerIndexName, data));
		
		return data;
	}
	
	private void readCoreVariables(DataSet data, String labelNameParam, String labelsParam,
							int noOfVars, String catLabelsParam, String varNameParam, String valuesParam) {
		String labelNameString = getParameter(labelNameParam);
		if (labelNameString != null)
			data.addLabelVariable("label", labelNameString, getParameter(labelsParam));
		
		for (int i=0 ; i<noOfVars ; i++) {
			String labelsString = getParameter(catLabelsParam + i);
			String yKey = "y" + i;
			if (labelsString == null)
				data.addNumVariable(yKey, getParameter(varNameParam + i),
																										getParameter(valuesParam + i));
			else
				data.addCatVariable(yKey, getParameter(varNameParam + i),
															getParameter(valuesParam + i), labelsString);
		}
	}
	
	private void addExtraVariables(MultiLevelDataSet higherData, MultiLevelDataSet lowerData,
																						int nExtraHighVars, int nExtraLowVars) {
		NumVariable var0 = (NumVariable)higherData.getVariable("y0");
		int nHigherValues = var0.noOfValues();
		
		for (int i=0 ; i<nExtraLowVars ; i++) {
			NumVariable highVar = (NumVariable)higherData.getVariable(highCopyKey[i]);
			lowerData.addVariable("copy" + i, new CopyIndexedVariable(highVar, copyName[i],
																																						lowerData));
		}
		
		for (int i=0 ; i<nExtraHighVars ; i++) {
			higherData.addVariable("sum" + i, new SummariseUpVariable(sumName[i], lowerData,
															sumSourceKey[i], sumType[i], decimals[i], nHigherValues));
		}
	}
	
	private XPanel dataTablePanel(DataSet data, String dataName, int noOfMainVars,
																									int nExtraVars, String baseExtraKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			XPanel titlePanel = new XPanel();
			titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				XLabel title = new XLabel(dataName, XLabel.LEFT, this);
				title.setFont(getStandardBoldFont());
			titlePanel.add(title);
			
		thePanel.add("North", titlePanel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 0));
			
				MultiLevelScrollList theList = new MultiLevelScrollList(data, this, ScrollValueList.HEADING);
				theList.addVariableToList("index", ScrollValueList.RAW_VALUE);
				if (data.getVariable("label") != null)
					theList.addVariableToList("label", ScrollValueList.RAW_VALUE);
				
				for (int i=0 ; i<noOfMainVars ; i++)
					theList.addVariableToList("y" + i, ScrollValueList.RAW_VALUE);
				
				for (int i=0 ; i<nExtraVars ; i++)
					theList.addVariableToList(baseExtraKey + i, ScrollValueList.RAW_VALUE);
				
				if (highList == null)
					highList = theList;
				else
					lowList = theList;
				
			mainPanel.add("Center", theList);
		
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	private void hiliteLinkVariable(int linkIndex) {
		if (linkIndex < nExtraLowVars) {
			highList.setSelectedCols(highCopyKey[linkIndex], null);
			lowList.setSelectedCols("copy" + linkIndex, null);
		}
		else {
			linkIndex -= nExtraLowVars;
			lowList.setSelectedCols(sumSourceKey[linkIndex], null);
			highList.setSelectedCols("sum" + linkIndex, null);
		}
	}

	
	private boolean localAction(Object target) {
		if (target == linkChoice) {
			int newChoice = linkChoice.getSelectedIndex();
			if (newChoice != currentLinkChoice) {
				currentLinkChoice = newChoice;
				hiliteLinkVariable(newChoice);
				arrowCanvas1.showVersion(newChoice < nExtraLowVars ? 0 : 1);
				arrowCanvas2.showVersion(newChoice < nExtraLowVars ? 0 : 1);
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