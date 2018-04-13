package exper2;

import java.awt.*;

import dataView.*;


public class IncompleteBlockView extends DataView {
	static final private int kBlockNameGap = 6;
	static final private int kMinBlockGap = 7;
	
	private String yKey, blockKey, treatKey;
	
	public IncompleteBlockView(DataSet theData, XApplet applet, String yKey, String blockKey,
																																					String treatKey) {
		super(theData, applet, null);
		this.yKey = yKey;
		this.blockKey = blockKey;
		this.treatKey = treatKey;
	}
	
	public void paintView(Graphics g) {
		NumVariable yVar = (yKey == null) ? null : (NumVariable)getVariable(yKey);
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		CatVariable treatVar = (CatVariable)getVariable(treatKey);
		
		int maxBlockNameWidth = blockVar.getMaxWidth(g);
//		int maxTreatNameWidth = treatVar.getMaxWidth(g);
		
		int blockN[] = blockVar.getCounts();
		int maxBlockSize = 0;
		int nBlocks = blockN.length;
		for (int i=0 ; i<nBlocks ; i++)
			maxBlockSize = Math.max(maxBlockSize, blockN[i]);
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
//		int descent = fm.getDescent();
		
		int tableLeft = maxBlockNameWidth + kBlockNameGap;
		int blockHeight = getSize().height / nBlocks;
		int cellWidth = (getSize().width - tableLeft) / maxBlockSize;
		
		g.setColor(Color.white);
		g.fillRect(tableLeft, 0, cellWidth * maxBlockSize, blockHeight * nBlocks);
		g.setColor(getForeground());
		
		int baseline = (blockHeight + ascent) / 2;
		for (int i=0 ; i<nBlocks ; i++) {
			blockVar.getLabel(i).drawRight(g, 0, baseline);
			baseline += blockHeight;
		}
		
		for (int i=0 ; i<nBlocks ; i++)
			blockN[i] = 0;
		
		for (int i=0 ; i<blockVar.noOfValues() ; i++) {
			Value treat = treatVar.valueAt(i);
			String val = treat.toString();
			if (yVar != null) {
				Value y = yVar.valueAt(i);
				val = "(" + val + ") " + y;
			}
			int block = blockVar.getItemCategory(i);
			baseline = block * blockHeight + (blockHeight + ascent) / 2;
			int center = tableLeft + blockN[block] * cellWidth + cellWidth / 2;
			int left = center - fm.stringWidth(val) / 2;
			g.drawString(val, left, baseline);
			blockN[block] ++;
		}
	}

//-----------------------------------------------------------------------------------
	
	
	public Dimension getMinimumSize() {
		FontMetrics fm = getGraphics().getFontMetrics();
		CatVariable blockVar = (CatVariable)getVariable(blockKey);
		int nBlocks = blockVar.noOfCategories();
		
		int height = (kMinBlockGap + fm.getAscent() + fm.getDescent()) * nBlocks;
		return new Dimension(50, height);
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
}