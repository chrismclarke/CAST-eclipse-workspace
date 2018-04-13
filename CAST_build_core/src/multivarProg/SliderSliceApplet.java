package multivarProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;

import multivar.*;


public class SliderSliceApplet extends ScatterApplet {
	static final protected String Z_VAR_NAME_PARAM = "zVarName";
	static final protected String Z_VALUES_PARAM = "zValues";
	
	static final protected String SLIDER_EXTREME_PARAM = "slicerExtreme";
	static final protected String SLICE_NAMES_PARAM = "sliceNames";
	
	private XCheckbox sliceCheck;
	private SliceSlider sliceSlider;
	private ScatterSliceView theView;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		data.addNumVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM));
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		String sliderExtremes = getParameter(SLIDER_EXTREME_PARAM);
		if (sliderExtremes != null) {
			thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			sliceCheck = new XCheckbox(translate("Slice"), this);
			thePanel.add(sliceCheck);
			
			StringTokenizer limitsTokenizer = new StringTokenizer(sliderExtremes);
			int sliceMin = Integer.parseInt(limitsTokenizer.nextToken());
			int sliceMax = Integer.parseInt(limitsTokenizer.nextToken());
			
			String sliceNames = getParameter(SLICE_NAMES_PARAM);
			if (sliceNames == null)
				sliceSlider = new SliceSlider(data.getVariable("z").name, sliceMin, sliceMax, this);
			else {
				String name[] = new String[sliceMax - sliceMin + 1];
				LabelEnumeration le = new LabelEnumeration(sliceNames);
				for (int i=0 ; i<name.length ; i++)
					name[i] = (String)le.nextElement();
				sliceSlider = new SliceSlider(data.getVariable("z").name, sliceMin, sliceMax, name, this);
			}
			selectFromSlider();
			sliceSlider.show(false);
			thePanel.add(sliceSlider);
		}
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis,
																	VertAxis theVertAxis) {
		theView = new ScatterSliceView(data, this, theHorizAxis, theVertAxis, "x", "y");
		theView.setSlicing(false);
		return theView;
	}
	
	private void selectFromSlider() {
		int targetVal = sliceSlider.getValue();
		double minSel = targetVal - 0.5;
		double maxSel = targetVal + 0.5;
		data.setSelection("z", minSel, maxSel);
	}

	
	private boolean localAction(Object target) {
		if (target == sliceCheck) {
			if (sliceCheck.getState()) {
				sliceSlider.show(true);
				theView.setSlicing(true);
			}
			else {
				sliceSlider.show(false);
				theView.setSlicing(false);
			}
			return true;
		}
		else if (target == sliceSlider) {
			selectFromSlider();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}