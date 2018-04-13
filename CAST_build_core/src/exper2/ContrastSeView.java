package exper2;

import java.awt.*;

import dataView.*;
import models.*;
import formula.*;
import valueList.*;


public class ContrastSeView extends ValueView {
	private DataSet data;
	private String modelKey;
	private String label;
	private Value maxSe;
	private double[] contrast;
	
	public ContrastSeView(DataSet data, String modelKey, double[] contrast, String label,
																											Value maxSe, XApplet applet) {
		super(data, applet);
		this.data = data;
		this.modelKey = modelKey;
		this.label = label;
		this.maxSe = maxSe;
		this.contrast = contrast;
		setUnitsString(MText.expandText("#sigma#"));
	}
	
	protected int getLabelWidth(Graphics g) {
		return (label == null) ? 0 : g.getFontMetrics().stringWidth(label);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxSe.stringWidth(g);
	}
	
	protected String getValueString() {
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable(modelKey);
		double contrastSe = Math.sqrt(model.getContrastVar(contrast, 1.0));
		
		return new NumValue(contrastSe, ((NumValue)maxSe).decimals).toString();
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		if (label != null)
			g.drawString(label, startHoriz, baseLine);
	}
	
	protected boolean highlightValue() {
		return false;
	}
}
