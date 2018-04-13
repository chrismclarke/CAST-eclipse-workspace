package variance;

import java.awt.*;

import dataView.*;
import axis.*;


public class SquaredHorizAxis extends HorizAxis {
	static final private String kSqrAxisInfo = "0 4 10 1 0 2.0 2.8284 3.4641 4.0";
	static final private NumValue kSqrLabels[] = {new NumValue(0, 0), new NumValue(4, 0),
																new NumValue(8, 0), new NumValue(12, 0), new NumValue(16, 0)};

	private Value rawLabel[];
	
	public SquaredHorizAxis(XApplet applet) {
		super(applet);
	}
	
	public void setSqrLabels() {
		readNumLabels(kSqrAxisInfo);
		
		int nLabels = labels.size();
		rawLabel = new Value[nLabels];
		for (int i=0 ; i<rawLabel.length ; i++)
			rawLabel[i] = ((AxisLabel)labels.elementAt(i)).label;
	}
	
	public void corePaint(Graphics g) {
		for (int i=0 ; i<rawLabel.length ; i++)
			((AxisLabel)labels.elementAt(i)).label = kSqrLabels[i];
		
		super.corePaint(g);
		
		for (int i=0 ; i<rawLabel.length ; i++)
			((AxisLabel)labels.elementAt(i)).label = rawLabel[i];
	}
	
}