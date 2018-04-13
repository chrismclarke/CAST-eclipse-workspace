package coreSummaries;

import dataView.*;


abstract public class CoreHypothesisTest {
	protected XApplet applet;
	
	public CoreHypothesisTest(XApplet applet) {
		this.applet = applet;
	}
	
	abstract public double evaluateStatistic();
	abstract public double evaluatePValue();
}
	
