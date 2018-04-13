package twoFactor;

import java.awt.*;

import dataView.*;
import valueList.*;


public class SliceEffectValueView extends ProportionView {
//	static final private String SLICE_EFFECT_VIEW = "selectEffectView";
	
	private String sliceKey, explanKey;
	private int sliceIndex;
	private NumValue maxValue;
	
	public SliceEffectValueView(DataSet theData, XApplet applet, String yKey, String sliceKey,
																					int sliceIndex, String explanKey, NumValue maxValue) {
		super(theData, yKey, applet);
		this.sliceKey = sliceKey;
		this.explanKey = explanKey;
		this.sliceIndex = sliceIndex;
		this.maxValue = maxValue;
		setLabel(applet.translate("Estimate") + " =");
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxValue.stringWidth(g);
	}
	
	protected String getValueString() {
		NumVariable yVar = (NumVariable)getVariable(variableKey);
		CatVariable sliceVar = (CatVariable)getVariable(sliceKey);
		CatVariable explanVar = (CatVariable)getVariable(explanKey);
		
		ValueEnumeration ye = yVar.values();
		double sy[] = new double[2];
		int n[] = new int[2];
		int index = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			int slice = sliceVar.getItemCategory(index);
			if (slice == sliceIndex) {
				int x = explanVar.getItemCategory(index);
				n[x] ++;
				sy[x] += y;
			}
			
			index ++;
		}
		double effect = sy[1] / n[1] - sy[0] / n[0];
		
		return new NumValue(effect, maxValue.decimals).toString();	
	}
}
