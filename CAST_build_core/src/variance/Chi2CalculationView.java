package variance;

import java.awt.*;

import dataView.*;
import coreSummaries.*;
import images.*;


public class Chi2CalculationView extends DataView {
	
	static final private int kMaxWait = 30000;		//		30 seconds
	static final private int kTemplateWidth = 450;
	static final private int kTemplateHeight = 317;
	
	static final private Rectangle sd1Rect = new Rectangle(196, 244, 68, 27);
	static final private Rectangle sd2Rect = new Rectangle(382, 244, 68, 27);
	static final private Rectangle ci1Rect = new Rectangle(120, 289, 68, 27);
	static final private Rectangle ci2Rect = new Rectangle(262, 289, 68, 27);
	
	private String sdKey, ciKey;
	
	private Image template[];
	private int currentTemplate = 0;
	
	public Chi2CalculationView(DataSet theData, XApplet applet, String[] templateFile,
																																String sdKey, String ciKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		this.sdKey = sdKey;
		this.ciKey = ciKey;
		
		template = new Image[templateFile.length];
		
		MediaTracker tracker = new MediaTracker(this);
		for (int i=0 ; i<templateFile.length ; i++) {
			template[i] = CoreImageReader.getImage(templateFile[i]);
			tracker.addImage(template[i], 0);
		}
		try {
			tracker.waitForAll(kMaxWait);
		} catch (InterruptedException e) {
		}
	}
	
	public void setCurrentTemplate(int templateIndex) {
		currentTemplate = templateIndex;
	}
	
	public void paintView(Graphics g) {
		int xOffset = (getSize().width - kTemplateWidth) / 2;
		int yOffset = (getSize().height - kTemplateHeight) / 2;
		g.drawImage(template[currentTemplate], xOffset, yOffset, this);
		
		NumVariable sdVar = (NumVariable)getVariable(sdKey);
		NumValue sd = (NumValue)sdVar.valueAt(0);
		
		IntervalSummaryVariable ciVar = (IntervalSummaryVariable)getVariable(ciKey);
		IntervalValue ci = (IntervalValue)ciVar.valueAt(0);
		
		int ascent = g.getFontMetrics().getAscent();
		int sdBaseline = yOffset + sd1Rect.y + (sd1Rect.height + ascent) / 2;
		g.setColor(Color.blue);
		sd.drawCentred(g, xOffset + sd1Rect.x + sd1Rect.width / 2, sdBaseline);
		sd.drawCentred(g, xOffset + sd2Rect.x + sd2Rect.width / 2, sdBaseline);
		
		int ciBaseline = yOffset + ci1Rect.y + (ci1Rect.height + ascent) / 2;
		g.setColor(getForeground());
		ci.lowValue.drawCentred(g, xOffset + ci1Rect.x + ci1Rect.width / 2, ciBaseline);
		ci.highValue.drawCentred(g, xOffset + ci2Rect.x + ci2Rect.width / 2, ciBaseline);
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(kTemplateWidth, kTemplateHeight);
	}
}