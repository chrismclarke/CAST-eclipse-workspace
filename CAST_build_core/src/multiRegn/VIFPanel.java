package multiRegn;

import dataView.*;
import formula.*;



public class VIFPanel extends Binary {
	static private FormulaPanel rightPanel(DataSet data, String distnKey, NumValue minSDValue,
												NumValue maxSDValue, NumValue maxVIF, FormulaContext context) {
			XApplet applet = context.getApplet();
			SlopeSDValueView actualSDView = new SlopeSDValueView(data, applet, distnKey, maxSDValue);
			actualSDView.setShowLabel(false);
			actualSDView.setHighlightValue(false);
			for (int i=0 ; i<3 ; i++)
				actualSDView.unboxValue();
		
		SummaryValue actualSD = new SummaryValue(actualSDView, context);
		Square actualVar = new Square(actualSD, context);
		
		Const minSD = new Const(minSDValue, context);
		Square minVar = new Square(minSD, context);
		
		Ratio vifRatio = new Ratio(actualVar, minVar, context);
		
			VIFValueView vifView = new VIFValueView(data, applet, distnKey, minSDValue, maxVIF);
			vifView.setShowLabel(false);
		SummaryValue vif = new SummaryValue(vifView, context);
		
		return new Binary(Binary.EQUALS, vifRatio, vif, context);
	}
	
	static private TextLabel vifPicture(FormulaContext context) {
//		int ascent = AnovaImages.kRSquaredAscent;
//		int descent = AnovaImages.kRSquaredDescent;
//		int width = AnovaImages.kRSquaredWidth;
//		Image image = AnovaImages.rSquared;
//		
//		return new Picture(image, width, ascent, descent, c, f);
		
		return new TextLabel(new LabelValue(context.getApplet().translate("VIF")), context);
	}
	
	public VIFPanel(DataSet data, String distnKey, NumValue minSD,
																NumValue maxSD, NumValue maxVIF, FormulaContext context) {
		super(Binary.EQUALS, vifPicture(context),
												rightPanel(data, distnKey, minSD, maxSD, maxVIF, context), context);
	}
}