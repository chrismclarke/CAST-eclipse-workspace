package stdError;

import java.util.*;
import java.awt.*;

import dataView.*;

public class ErrorBoundsView extends TwoSdBoundsView {
	
//	static public final String ERROR_BOUNDS = "errorBounds";

	private String probTemplate;
	
	public ErrorBoundsView(DataSet theData, XApplet applet, Stacked2SdBoundsView stackedErrorView,
															NumValue maxErrorBound) {
		super(theData, applet, stackedErrorView, maxErrorBound);
		probTemplate = applet.translate("P( -* < error < * )");
	}
	
	public void paintView(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		StackedErrorBoundsView stackedView = (StackedErrorBoundsView)stackedErrorView;
		
		NumValue errorBound = new NumValue(stackedView.findError95(), maxErrorBound.decimals);
		String boundString = errorBound.toString();
		StringTokenizer st = new StringTokenizer(probTemplate, "*");
		String probFormula = st.nextToken() + boundString + st.nextToken() + boundString + st.nextToken();
		g.drawString(probFormula, kLeftRightBorder, kTopBottomBorder + ascent);
		
		kApprox95.drawLeft(g, getSize().width - kLeftRightBorder,
																		kTopBottomBorder + kLineGap + 2 * ascent + descent);
	}
}
