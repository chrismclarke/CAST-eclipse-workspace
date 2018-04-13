package normal;

import java.util.*;

import dataView.*;
import axis.*;


public class StdNormalAxis extends HorizAxis {
	
	public StdNormalAxis(XApplet applet) {
		super(applet);
		setFont(applet.getBigFont());
	}
	
	public void setupStdAxis() {
		readNumLabels("-3.5 3.5 -3 1");
		
		Enumeration e = labels.elements();
		
		AxisLabel l = (AxisLabel)e.nextElement();
		l.label = new LabelValue("\u03BC \u2013 3\u03C3");
		
		l = (AxisLabel)e.nextElement();
		l.label = new LabelValue("\u03BC \u2013 2\u03C3");
		l = (AxisLabel)e.nextElement();
		l.label = new LabelValue("\u03BC \u2013 \u03C3");
		
		l = (AxisLabel)e.nextElement();
		l.label = new LabelValue("\u03BC");
		
		l = (AxisLabel)e.nextElement();
		l.label = new LabelValue("\u03BC + \u03C3");
		l = (AxisLabel)e.nextElement();
		l.label = new LabelValue("\u03BC + 2\u03C3");
		
		l = (AxisLabel)e.nextElement();
		l.label = new LabelValue("\u03BC + 3\u03C3");
	}
	
	public void setupStdAxis(NumValue mean, NumValue sd) {
		readNumLabels("-3.5 3.5 -3 1");
		
		Enumeration e = labels.elements();
		
		int decimals = Math.max(mean.decimals, sd.decimals);
		for (int z=-3 ; z<=3 ; z++) {
			AxisLabel l = (AxisLabel)e.nextElement();
			l.label = new NumValue(mean.toDouble() + z * sd.toDouble(), decimals);
		}
	}
}