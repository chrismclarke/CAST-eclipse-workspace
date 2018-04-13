package stemLeaf;

import java.awt.*;

import dataView.*;


public class CreateStemLeafView extends StemAndLeafView {
	static final private int kArrowWidth = 12;
	
	private int stemRight, leaf0Left;
	private int headingHt;
	private int plotLeaves[][];
	private int nPlotLeaves[];
	private int stemBottom[];
	
	private int selectedStemIndex = -1;
	
	private int xPoly[] = new int[8];
	private int yPoly[] = new int[8];
	
	public CreateStemLeafView(DataSet theData, XApplet applet, String axisInfo) {
		super(theData, applet, axisInfo);
	}
	
	public void clearPlotLeaves() {
		for (int i=0 ; i<plotLeaves.length ; i++)
			nPlotLeaves[i] = 0;
		selectedStemIndex = -1;
		repaint();
	}
	
	private void sortList(int[] list, int n) {
		for (int i=1 ; i<n ; i++)
			for (int j=i ; j>0 ; j--)
				if (list[j] < list[j-1]) {
					int temp = list[j];
					list[j] = list[j-1];
					list[j-1] = temp;
				}
				else
					break;
	}
	
	public void sortLeaves() {
		for (int i=0 ; i<nPlotLeaves.length ; i++)
			sortList(plotLeaves[i], nPlotLeaves[i]);
		repaint();
	}
	
	public void addLeaf(int i) {
		if (i < 0)
			selectedStemIndex = -1;
		else {
			int stemAndLeaf = axis.findStemAndLeaf((NumValue)getNumVariable().valueAt(i));
			int stemIndex = (stemAndLeaf - axis.minStemAndLeaf) / axis.leavesPerBin;
			int leaf = (stemAndLeaf >= 0) ? stemAndLeaf % 10 : -((stemAndLeaf + 1) % 10);
			
			plotLeaves[stemIndex][nPlotLeaves[stemIndex] ++] = leaf;
			selectedStemIndex = stemIndex;
		}
		repaint();
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			axis.leafStart += kArrowWidth;		//	offsets stem and leaf plot
			
			plotLeaves = new int[axis.noOfBins][];
			nPlotLeaves = new int[axis.noOfBins];
			for (int i=0 ; i<axis.noOfBins ; i++) {
				int nLeafMax = 0;
				for (int j=0 ; j<axis.leavesPerBin ; j++)
					nLeafMax += leafCount[i*axis.leavesPerBin + j];
				plotLeaves[i] = new int[nLeafMax];
				clearPlotLeaves();
			}
			
			leaf0Left = axis.leafStart;
			stemRight = leaf0Left - 2 * StemAndLeafAxis.kLineGap - 3;
			
			headingHt = axis.getHeadingHt();
			stemBottom = new int[leafCount.length];
			int lineHt = axis.getLineHt();
			for (int i=0 ; i<axis.noOfBins ; i++)
				stemBottom[i] = headingHt + LeafDigitImages.kDigitHeight
																												+ (axis.noOfBins - i - 1) * lineHt;
			
			return true;
		}
		else
			return false;
	}
	
	public void drawArrow(Graphics g, int vertCenter) {
		g.setColor(Color.red);
		xPoly[0] = xPoly[6] = xPoly[7] = 1;
		xPoly[1] = xPoly[2] = xPoly[4] = xPoly[5] = 6;
		xPoly[3] = 11;
		yPoly[0] = yPoly[1] = yPoly[7] = vertCenter - 3;
		yPoly[2] = vertCenter - 5;
		yPoly[3] = vertCenter;
		yPoly[4] = vertCenter + 5;
		yPoly[5] = yPoly[6] = vertCenter + 3;
		g.fillPolygon(xPoly, yPoly, 8);
		g.drawPolygon(xPoly, yPoly, 8);
	}
	
	public void paintView(Graphics g) {
		initialise(g);
		
		drawHeading(g);
		
		g.setColor(Color.blue);
		int linePos = axis.leafStart - StemAndLeafAxis.kLineGap - 1;
		
		g.drawLine(linePos, headingHt, linePos, stemBottom[0] + LeafDigitImages.kVertSpace);
		g.setColor(getForeground());
		
		int currentStem = axis.minStem;
		int currentRepeat = axis.minStemRepeat;
		
		for (int i = 0 ; i<axis.noOfBins ; i++) {
			int baseline = stemBottom[i];
			axis.drawStem(g, currentStem, baseline, stemRight, this);
			
			if (i == selectedStemIndex)
				drawArrow(g, baseline - LeafDigitImages.kDigitHeight / 2);
			
			int currentCharPos = leaf0Left;
			
			for (int j=0 ; j<nPlotLeaves[i] ; j++) {
				int leafInt = plotLeaves[i][j];
				int leafColor = ((i == selectedStemIndex) && (j == nPlotLeaves[i] - 1))
															? LeafDigitImages.RED_DIGITS : LeafDigitImages.BLACK_DIGITS;
				Image leaf = LeafDigitImages.digit[leafColor][leafInt];
				axis.drawLeaf(g, leaf, currentCharPos, baseline, this); 
				currentCharPos += axis.getDigitWidth();
			}
			
			currentRepeat++;
			if (currentRepeat >= axis.repeatsPerStem) {
				currentStem++;
				currentRepeat = 0;
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}