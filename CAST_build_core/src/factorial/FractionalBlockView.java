package factorial;

import java.awt.*;

import dataView.*;

public class FractionalBlockView extends DataView {
	static final private int kTreatmentGap = 20;
	static final private int kBlockTreatGap = 4;
	static final private int kMinBlockGap = 20;
	
	static final private Color kBlockColor[] = FractionalDesignMatrix.kBlockColor;
	
	private String[] highName;
	private String[] blockName;
	private FractionalDesignMatrix designView;
	private int nBlocks, nComplete, nUnits, blockSize;
	
	private int blockColorOffset = 0;
	
	private boolean initialised = false;
	
	private int ascent, descent, boldAscent;
	private int maxTreatLength;
	private int bestWidth, bestHeight;
	private Font stdFont, boldFont;
	
	private int blockIndex[];
	
	public FractionalBlockView(DataSet data, String[] highName, String[] blockName,
															FractionalDesignMatrix designView, XApplet applet) {
		super(data, applet, null);
		this.highName = highName;			//	names for high factor levels
		nComplete = highName.length;
		this.blockName = blockName;
		nBlocks = blockName.length;
		
		int nTemp = nBlocks;
		while (nTemp > 1)
			nTemp /= 2;
		
		this.designView = designView;
		
		nUnits = (int)Math.pow(2, highName.length);
		blockSize = nUnits / nBlocks;
		
		blockIndex = new int[nBlocks];
	}
	
	public void setBlockColorOffset(int blockColorOffset) {
		this.blockColorOffset = blockColorOffset;
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
		bestWidth = (blockSize + 1) * kTreatmentGap + blockSize * maxTreatLength;
		
		g.setFont(boldFont);
		fm = g.getFontMetrics();
		boldAscent = fm.getAscent();
		
		bestHeight = nBlocks * (boldAscent + kBlockTreatGap + ascent + kMinBlockGap);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		boolean[] mainEffects = null;
		for (int i=0 ; i<nBlocks ; i++)
			blockIndex[i] = 0;
		
		int blockGap = kMinBlockGap + (getSize().height - bestHeight) / nBlocks;
		int halfBlockGap = blockGap / 2;
		g.setFont(boldFont);
		int baseline = halfBlockGap + boldAscent;
		int step = boldAscent + ascent + kBlockTreatGap + blockGap;
		FontMetrics fm = g.getFontMetrics();
		for (int i=0 ; i<nBlocks ; i++) {
			g.setColor(kBlockColor[blockColorOffset + i]);
			g.fillRect(0, baseline - boldAscent - kMinBlockGap / 2, getSize().width,
																kMinBlockGap + boldAscent + ascent + kBlockTreatGap);
			g.setColor(Color.white);
			g.drawRect(0, baseline - boldAscent - kMinBlockGap / 2, getSize().width - 1,
																kMinBlockGap + boldAscent + ascent + kBlockTreatGap - 1);
			
			g.setColor(getForeground());
			int left = (getSize().width - fm.stringWidth(blockName[i])) / 2;
			g.drawString(blockName[i], left, baseline);
			baseline += step;
		}
		
		g.setFont(stdFont);
		fm = g.getFontMetrics();
		baseline = halfBlockGap + boldAscent + ascent + kBlockTreatGap;
		int selection = getSelection().findSingleSetFlag();
		for (int i=0 ; i<nUnits ; i++) {
			mainEffects = designView.getMainEffects(i, mainEffects);
			
			int block = designView.getBlock(i);
			
			String treat = "";
			for (int j=0 ; j<nComplete ; j++)
				if (mainEffects[j])
					treat += highName[j];
			if (treat.length() == 0)
				treat = "(1)";
			
			int treatBaseline = baseline + step * block;
			int horiz = (maxTreatLength + kTreatmentGap) * blockIndex[block] + kTreatmentGap / 2;
			
			if (i == selection) {
				g.setColor(Color.yellow);
				g.fillRect(horiz, treatBaseline - ascent - 2,
																		maxTreatLength + kTreatmentGap, ascent + descent + 4);
				g.setColor(getForeground());
			}
			horiz += (maxTreatLength + kTreatmentGap - fm.stringWidth(treat)) / 2;
			g.drawString(treat, horiz, treatBaseline);
			
			blockIndex[block] ++;
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
		for (int i=0 ; i<nBlocks ; i++)
			blockIndex[i] = 0;
		
		int blockGap = kMinBlockGap + (getSize().height - bestHeight) / nBlocks;
		int halfBlockGap = blockGap / 2;
		int step = boldAscent + ascent + kBlockTreatGap + blockGap;
		int baseline = halfBlockGap + boldAscent + ascent + kBlockTreatGap;
		
		for (int i=0 ; i<nUnits ; i++) {
			int block = designView.getBlock(i);
			
			int top = baseline + step * block - ascent - 2;
			int left = (maxTreatLength + kTreatmentGap) * blockIndex[block] + kTreatmentGap / 2;
			
			if (x >= left && x <= left + maxTreatLength + kTreatmentGap
																					&& y >= top && y <= top + ascent + descent + 4)
				return new IndexPosInfo(i);
			
			blockIndex[block] ++;
		}
		
		return null;
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