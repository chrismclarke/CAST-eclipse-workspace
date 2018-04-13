package experProg;

import java.awt.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import multivar.*;
import exper.*;


public class SliceInteractionApplet extends RotateInteractionApplet {
	
	private XCheckbox slice1Check, slice2Check;
	private SliceSlider slice1Slider, slice2Slider;
	
	protected XPanel threeDPanel(DataSet data) {
		XPanel thePanel = super.threeDPanel(data);
		theView.setDrawData(false);
		return thePanel;
	}
	
	private String[] getTreatNames(CatVariable catVar) {
		String names[] = new String[catVar.noOfCategories()];
		for (int i=0 ; i<names.length ; i++)
			names[i] = catVar.getLabel(i).toString();
		return names;
	}
	
	protected XPanel lowerControlPanel(DataSet data) {
		InsetPanel thePanel = new InsetPanel(4, 4);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 6));
		thePanel.setBorderColor(new Color(0xCC6633));
			
			CatVariable treat1 = (CatVariable)data.getVariable("treat1");
			String treat1Name = treat1.name;
			slice1Check = new XCheckbox("Slice by " + treat1Name, this);
			slice1Check.setState(false);
			slice1Check.setForeground(D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.BACKGROUND]);
		thePanel.add(slice1Check);
		
			String treat1Names[] = getTreatNames(treat1);
			slice1Slider = new SliceSlider(treat1Name, 0, treat1Names.length - 1, treat1Names, this);
			slice1Slider.setForeground(D3Axis.axisColor[D3Axis.X_AXIS][D3Axis.BACKGROUND]);
			slice1Slider.show(false);
		thePanel.add(slice1Slider);
			
			CatVariable treat2 = (CatVariable)data.getVariable("treat2");
			String treat2Name = treat2.name;
			slice2Check = new XCheckbox("Slice by " + treat2Name, this);
			slice2Check.setState(false);
			slice2Check.setForeground(D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.BACKGROUND]);
		thePanel.add(slice2Check);
		
			String treat2Names[] = getTreatNames(treat2);
			slice2Slider = new SliceSlider(treat2Name, 0, treat2Names.length - 1, treat2Names, this);
			slice2Slider.setForeground(D3Axis.axisColor[D3Axis.Z_AXIS][D3Axis.BACKGROUND]);
			slice2Slider.show(false);
		thePanel.add(slice2Slider);
		
		return thePanel;
	}
	
	private void processSliceClick(XCheckbox thisSliceCheck, SliceSlider thisSliceSlider,
							String thisTreatKey, TreatmentLabelsView thisLabels, XCheckbox otherSliceCheck,
							SliceSlider otherSliceSlider, String otherTreatKey,
							TreatmentLabelsView otherLabels) {
		if (thisSliceCheck.getState()) {
			if (otherSliceCheck.getState()) {
				otherSliceCheck.setState(false);
				processSliceClick(otherSliceCheck, otherSliceSlider, otherTreatKey, otherLabels,
									thisSliceCheck, thisSliceSlider, thisTreatKey, thisLabels);
			}
			thisSliceSlider.show(true);
			thisLabels.setSelectedCat(thisSliceSlider.getValue());
		}
		else {
			thisSliceSlider.show(false);
			thisLabels.setSelectedCat(-1);
		}
	}
	
	private boolean localAction(Object target) {
		if (target == slice1Check) {
			processSliceClick(slice1Check, slice1Slider, "treat1", treat1Labels, slice2Check, slice2Slider,
							"treat2", treat2Labels);
			theView.setSliceVariable(slice1Check.getState() ? RotateTwoFactorView.TREAT1_SLICE
																														: RotateTwoFactorView.NO_SLICE);
			return true;
		}
		else if (target == slice2Check) {
			processSliceClick(slice2Check, slice2Slider, "treat2", treat2Labels, slice1Check, slice1Slider,
							"treat1", treat1Labels);
			theView.setSliceVariable(slice2Check.getState() ? RotateTwoFactorView.TREAT2_SLICE
																														: RotateTwoFactorView.NO_SLICE);
			return true;
		}
		else if (target == slice1Slider) {
			theView.setSlice(slice1Slider.getValue());
			treat1Labels.setSelectedCat(slice1Slider.getValue());
			return true;
		}
		else if (target == slice2Slider) {
			theView.setSlice(slice2Slider.getValue());
			treat2Labels.setSelectedCat(slice2Slider.getValue());
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