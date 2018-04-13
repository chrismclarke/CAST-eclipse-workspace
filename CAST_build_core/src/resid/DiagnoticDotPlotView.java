package resid;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;


public class DiagnoticDotPlotView extends StackedDotPlotView {
//	static public final String DIAGNOSTIC_DOTPLOT = "diagnosticDotPlot";
	
	static final public int ST_RESID = 0;
	static final public int LEVERAGE = 1;
	static final public int DFITS = 2;
	
	static final private Color kLowDangerColor = new Color(0xDDDDFF);
	static final private Color kHighDangerColor = new Color(0xDDBBFF);
	
	private int diagnosticType;
	private int noOfParams;
	
	public DiagnoticDotPlotView(DataSet theData, XApplet applet, NumCatAxis theAxis,
																				int diagnosticType, int noOfParams) {
		super(theData, applet, theAxis, null, false);
		this.diagnosticType = diagnosticType;
		this.noOfParams = noOfParams;
	}
	
	protected void paintBackground(Graphics g) {
		NumVariable diagVar = getNumVariable();
		
		int n = diagVar.noOfValues();
		Point p = null;
		
//		System.out.println("\n\n" + ((diagnosticType == ST_RESID) ? "Stud Resid: "
//															: (diagnosticType == LEVERAGE) ? "Leverage: "
//															: "DFITS: "));
//		for (int i=0 ; i<n ; i++)
//			System.out.print(diagVar.valueAt(i).toString() + ", ");
		
		switch (diagnosticType) {
			case ST_RESID:
				g.setColor(kLowDangerColor);
				int horizPos = axis.numValToRawPosition(2.0);
				p = translateToScreen(horizPos, 0, p);
				g.fillRect(p.x, 0, getSize().width - p.x, getSize().height);
				horizPos = axis.numValToRawPosition(-2.0);
				p = translateToScreen(horizPos, 0, p);
				g.fillRect(0, 0, p.x, getSize().height);
				
				g.setColor(kHighDangerColor);
				horizPos = axis.numValToRawPosition(3.0);
				p = translateToScreen(horizPos, 0, p);
				g.fillRect(p.x, 0, getSize().width - p.x, getSize().height);
				horizPos = axis.numValToRawPosition(-3.0);
				p = translateToScreen(horizPos, 0, p);
				g.fillRect(0, 0, p.x, getSize().height);
				
				break;
			case LEVERAGE:
				g.setColor(kHighDangerColor);
				horizPos = axis.numValToRawPosition(2.0 * noOfParams / n);
				p = translateToScreen(horizPos, 0, p);
				g.fillRect(p.x, 0, getSize().width - p.x, getSize().height);
				break;
			case DFITS:
				g.setColor(kHighDangerColor);
				double dfitsCutoff = 2.0 * Math.sqrt(noOfParams / (double)(n - noOfParams));
				horizPos = axis.numValToRawPosition(dfitsCutoff);
				p = translateToScreen(horizPos, 0, p);
				g.fillRect(p.x, 0, getSize().width - p.x, getSize().height);
				horizPos = axis.numValToRawPosition(-dfitsCutoff);
				p = translateToScreen(horizPos, 0, p);
				g.fillRect(0, 0, p.x, getSize().height);
		}
	}
}