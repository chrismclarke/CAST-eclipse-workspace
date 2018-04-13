package factorial;

import java.awt.*;

import dataView.*;

public class TreatmentsView extends DataView {
	static final private int kTopBottomBorder = 4;
	static final private int kHeadingGap = 4;
	static final private int kRowGap = 4;
	static final private int kTreatmentGap = 20;
	
	private LabelValue kTreatmentTitle;
	
	private String[] highName;
	private FractionalDesignMatrix designView;
	private int nFactors;
	private int nCols, nRows, nUnits;
	
	private boolean initialised = false;
	
	private int ascent, descent, boldAscent;
	private int maxTreatLength;
	private int bestWidth, bestHeight;
	private Font stdFont, boldFont;
	
	public TreatmentsView(DataSet data, String[] highName, FractionalDesignMatrix designView,
																													int nCols, int nRows, XApplet applet) {
		super(data, applet, null);
		this.highName = highName;			//	names for high factor levels
		nFactors = highName.length;
		
		this.designView = designView;
		this.nCols = nCols;
		this.nRows = nRows;
		
		nUnits = nCols * nRows;
		kTreatmentTitle = new LabelValue(applet.translate("Treatments"));
	}
	
	final protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		stdFont = g.getFont();
		boldFont = new Font(stdFont.getName(), Font.BOLD, stdFont.getSize());
		
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		maxTreatLength = 0;
		for (int i=0 ; i<highName.length ; i++)
			maxTreatLength += fm.stringWidth(highName[i]);
		bestWidth = (nCols + 1) * kTreatmentGap + nCols * maxTreatLength;
		
		g.setFont(boldFont);
		fm = g.getFontMetrics();
		boldAscent = fm.getAscent();
		
		bestHeight = 2 * kTopBottomBorder + boldAscent + kHeadingGap + nRows * ascent
																																	+ (nRows - 1) * kRowGap;
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		g.setFont(boldFont);
		int baseline = kTopBottomBorder + boldAscent;
		kTreatmentTitle.drawCentred(g, getSize().width / 2, baseline);
		
		g.setFont(stdFont);
		FontMetrics fm = g.getFontMetrics();
		baseline += kHeadingGap + ascent;
		int vertStep = ascent + kRowGap;
		int col0Left = (getSize().width - bestWidth) / 2;
		int horizStep = maxTreatLength + kTreatmentGap;
		
		boolean[] mainEffects = null;
		int selection = getSelection().findSingleSetFlag();
		for (int i=0 ; i<nUnits ; i++) {
			mainEffects = designView.getMainEffects(i, mainEffects);
			
			String treat = "";
			for (int j=0 ; j<nFactors ; j++)
				if (mainEffects[j])
					treat += highName[j];
			if (treat.length() == 0)
				treat = "(1)";
			
			int row = i / nCols;
			int col = i % nCols;
			
			int treatBaseline = baseline + vertStep * row;
			int horiz = col0Left + col * horizStep + kTreatmentGap / 2;
			
			if (i == selection) {
				g.setColor(Color.yellow);
				g.fillRect(horiz, treatBaseline - ascent - 2,
																		maxTreatLength + kTreatmentGap, ascent + descent + 4);
				g.setColor(getForeground());
			}
			horiz += (maxTreatLength + kTreatmentGap - fm.stringWidth(treat)) / 2;
			g.drawString(treat, horiz, treatBaseline);
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getPreferredSize() {
		initialise(getGraphics());
		return new Dimension(bestWidth, bestHeight);
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

//-----------------------------------------------------------------------------------

	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	

	protected PositionInfo getPosition(int x, int y) {
		int col0Left = (getSize().width - bestWidth) / 2;
		int horizStep = maxTreatLength + kTreatmentGap;
		
		int row0Top = kTopBottomBorder + boldAscent + kHeadingGap - 2;
		int vertStep = ascent + kRowGap;
		
		if (x < col0Left || x >= col0Left + nCols * horizStep || y < row0Top
																					|| y >= row0Top + nRows * vertStep)
			return null;
		
		int hitCol = (x - col0Left) / horizStep;
		int hitRow = (y - row0Top) / vertStep;
		
		return new IndexPosInfo(hitCol + hitRow * nCols);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo != null) {
			int hitIndex = ((IndexPosInfo)startInfo).itemIndex;
			getData().setSelection(hitIndex);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		getData().clearSelection();
	}

}