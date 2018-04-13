package statistic2Prog;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import multivar.*;
import statistic2.*;


public class GroupMeanSdApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
//	static final private String YEAR_PARAM = "year";
	static final private String S_DECIMALS_PARAM = "sDecimals";
	
	private SliceGroupDotView dataView;
	private MultiVertAxis groupAxis;
	
	private SliceSlider catSlider;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 2));
			
		add("Center", dataDisplay(data));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addCatVariable("month", getParameter(CAT_NAME_PARAM), getParameter(CAT_VALUES_PARAM),
																														getParameter(CAT_LABELS_PARAM));
		
		return data;
	}
	
	private XPanel dataDisplay(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis numAxis = new HorizAxis(this);
			numAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
			numAxis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", numAxis);
		
			groupAxis = new MultiVertAxis(this, 12);
			CatVariable catVar = (CatVariable)data.getVariable("month");
			CatVariable tempCatVar = new CatVariable("temp");
			for (int i=0 ; i<catVar.noOfCategories() ; i++) {
				String labelString = catVar.getLabel(i).toString() + " #" + translate("All months") + "#";
				tempCatVar.readLabels(labelString);
				if (i == 0)
					groupAxis.setCatLabels(tempCatVar);
				else
					groupAxis.readExtraCatLabels(tempCatVar);
			}
			
		thePanel.add("Left", groupAxis);
		
			int sDecimals = Integer.parseInt(getParameter(S_DECIMALS_PARAM));
			dataView = new SliceGroupDotView(data, this, "y", numAxis, groupAxis, sDecimals);
									
			dataView.lockBackground(Color.white);
			
		thePanel.add("Center", dataView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(100, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
			
			CatVariable catVar = (CatVariable)data.getVariable("month");
			int nCats = catVar.noOfCategories();
			String sliceName[] = new String[nCats];
			for (int i=0 ; i<nCats ; i++)
				sliceName[i] = catVar.getLabel(i).toString();
			catSlider = new SliceSlider(catVar.name, 0, nCats - 1,
															sliceName, sliceName[0], sliceName[nCats - 1], this);
			catSlider.setFont(getStandardBoldFont());
			
		thePanel.add(catSlider);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == catSlider) {
			int newDisplayCat = catSlider.getValue();
			groupAxis.setAlternateLabels(newDisplayCat);
			
			dataView.setDisplayCat(newDisplayCat);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}