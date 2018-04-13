package stdError;

import java.awt.*;

import dataView.*;
import imageUtils.*;


public class StDevnValueView extends ValueImageView {
	
	static final private String kSampSdImageName = "ci/sampSD.png";		//	uses s
	static final private String kPopSdImageName = "ci/popnSD.png";	//	uses sigma
	
	static final private int kImageAscent = 12;
	
	private String yKey;
	private NumValue maxSD;
	
	public StDevnValueView(DataSet theData, XApplet applet, String yKey,
																									boolean popNotSamp, NumValue maxSD) {
		super(theData, applet, popNotSamp ? kPopSdImageName : kSampSdImageName, kImageAscent);
		this.yKey = yKey;
		this.maxSD = maxSD;
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return maxSD.stringWidth(g);
	}
	
	protected String getValueString() {
		CoreVariable yCore = getVariable(yKey);
		NumValue value;
		if (yCore instanceof DistnVariable) {
			DistnVariable y = (DistnVariable)getVariable(yKey);
			value = y.getSD();
		}
		else {
			NumVariable yVar = (NumVariable)yCore;
			ValueEnumeration ye = yVar.values();
			int n = 0;
			double sy = 0.0;
			double syy = 0.0;
			while (ye.hasMoreValues()) {
				double y = ye.nextDouble();
				n ++;
				sy += y;
				syy += y * y;
			}
			value = new NumValue(Math.sqrt((syy - sy * sy / n) / (n - 1)), maxSD.decimals);
		}
		return value.toString();
	}
	
	protected boolean highlightValue() {
		return false;
	}
}