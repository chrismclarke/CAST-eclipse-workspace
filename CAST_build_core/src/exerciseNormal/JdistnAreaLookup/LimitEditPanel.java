package exerciseNormal.JdistnAreaLookup;

import java.awt.*;

import dataView.*;
import utils.*;


public class LimitEditPanel extends XPanel {
	private int decimals;
	private DataSet data;
	private String distnKey;
	private CoreLookupPanel normalPanel;
	
	private XNumberEditPanel lowEdit, highEdit;
	
	public LimitEditPanel(XApplet applet, DataSet data, String distnKey,
																				CoreLookupPanel normalPanel, boolean highAndLow) {
		this.data = data;
		this.distnKey = distnKey;
		this.normalPanel = normalPanel;
		
		if (highAndLow) {
			setLayout(new ProportionLayout(0.5, 0));
				
				XPanel leftPanel = new XPanel();
				leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					lowEdit = new XNumberEditPanel("low =", 0, applet);
				leftPanel.add(lowEdit);
			add(ProportionLayout.LEFT, leftPanel);
			 
				XPanel rightPanel = new XPanel();
				rightPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					highEdit = new XNumberEditPanel("high =", 0, applet);
				rightPanel.add(highEdit);
			add(ProportionLayout.RIGHT, rightPanel);
		}
		else {
			setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				highEdit = new XNumberEditPanel(null, 0, applet);
			add(highEdit);
		}
	}
	
	public void setFont(Font f) {
		if (lowEdit != null)
			lowEdit.setFont(f);
		if (highEdit != null)
			highEdit.setFont(f);
		super.setFont(f);
	}
	
	public void setForeground(Color c) {
		if (lowEdit != null)
			lowEdit.setForeground(c);
		if (highEdit != null)
			highEdit.setForeground(c);
		super.setForeground(c);
	}
	
	public void setInitialValues(NumValue maxResultValue) {
		int maxChars = maxResultValue.toString().length();
		decimals = maxResultValue.decimals;
		if (lowEdit != null)
			lowEdit.setColumns(maxChars);
		if (highEdit != null)
			highEdit.setColumns(maxChars);
		DistnVariable distn = (DistnVariable)data.getVariable(distnKey);
		setLowValue(distn.getMinSelection());
		setHighValue(distn.getMaxSelection());
	}
	
	public void setLowValue(double lowVal) {
		if (highEdit != null)
			highEdit.setDoubleType(lowVal, normalPanel.getMax());
		if (lowEdit != null)
			lowEdit.setDoubleValue(new NumValue(lowVal, decimals));
	}
	
	public void setHighValue(double highVal) {
		if (lowEdit != null)
			lowEdit.setDoubleType(normalPanel.getMin(), highVal);
		if (highEdit != null)
			highEdit.setDoubleValue(new NumValue(highVal, decimals));
	}
	
	public void setValues(NumValue lowVal, NumValue highVal) {
		if (highEdit != null)
			highEdit.setDoubleType(lowVal.toDouble(), normalPanel.getMax());
		if (lowEdit != null)
			lowEdit.setDoubleValue(lowVal);
		
		if (lowEdit != null)
			lowEdit.setDoubleType(normalPanel.getMin(), highVal.toDouble());
		if (highEdit != null)
			highEdit.setDoubleValue(highVal);
	}
	
	public void noteChanged() {
		normalPanel.noteChanged();
	}
	
	public NumValue getLowValue() {
		if (lowEdit == null)
			return null;
		else
			return lowEdit.getNumValue();
	}
	
	public NumValue getHighValue() {
		if (highEdit == null)
			return null;
		else
			return highEdit.getNumValue();
	}
	
	private boolean localAction(Object target, Object arg) {
		if (arg != null) {
			normalPanel.setPending(target != highEdit, ((Boolean)arg).booleanValue());
			data.variableChanged(distnKey);
		}
		else {
			DistnVariable distn = (DistnVariable)data.getVariable(distnKey);
			if (target == lowEdit) {
				double lowVal = lowEdit.getDoubleValue();
				if (highEdit != null)
					highEdit.setDoubleType(lowVal, normalPanel.getMax());
				distn.setMinSelection(lowVal);
			}
			else {
				double highVal = highEdit.getDoubleValue();
				if (lowEdit != null)
					lowEdit.setDoubleType(normalPanel.getMin(), highVal);
				distn.setMaxSelection(highVal);
			}
			normalPanel.setPending(target == lowEdit, false);
			data.variableChanged(distnKey);
			noteChanged();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target, evt.arg);
	}
}