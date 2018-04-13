package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;


public class FView extends Chi2View {
	
	private String labelMiddle = null;
	
	public FView(DataSet theData, XApplet applet, NumCatAxis theAxis, String distnKey, String valueKey) {
		super(theData, applet, theAxis, distnKey, valueKey);
	}
	
	public FView(DataSet theData, XApplet applet, NumCatAxis theAxis, String distnKey) {
		this(theData, applet, theAxis, distnKey, null);
	}
	
	protected LabelValue getLabel(ContinDistnVariable distn) {
		if (labelSuffix == null)
			return new LabelValue(labelPrefix);
		else {
			FDistnVariable fDistn = (FDistnVariable)distn;
			int df1 = fDistn.getDF1();
			int df2 = fDistn.getDF2();
			return new LabelValue(labelPrefix + df1 + labelMiddle + df2 + labelSuffix);
		}
	}
	
	public void setDistnLabel(LabelValue label, Color labelColor) {
		super.setDistnLabel(label, labelColor);
		if (labelSuffix != null) {
			int nIndex = labelSuffix.indexOf("#");
			labelMiddle = labelSuffix.substring(0, nIndex);
			labelSuffix = labelSuffix.substring(nIndex+1);
		}
	}
}