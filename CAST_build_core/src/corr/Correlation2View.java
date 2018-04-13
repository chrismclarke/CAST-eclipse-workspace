package corr;

import java.awt.*;

import dataView.*;
import images.*;


public class Correlation2View extends CorrelationView {
	
	private Image corrImage2;
	
	public Correlation2View(DataSet theData, String xKey, String yKey,
										boolean drawFormula, int decimals, XApplet applet) {
		super(theData, xKey, yKey, drawFormula, decimals, applet);
		if (drawFormula) {
			corrImage2 = CoreImageReader.getImage("corr/corr2.gif");
			MediaTracker tracker = new MediaTracker(applet);
			tracker.addImage(corrImage2, 0);
			try {
				tracker.waitForAll(kMaxWait);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public Correlation2View(DataSet theData, String xKey, String yKey, boolean drawFormula,
																																				XApplet applet) {
		this(theData, xKey, yKey, drawFormula, kDefaultDecimals, applet);
	}
	
	protected Image getCorrImage() {
		return corrImage2;
	}
}
