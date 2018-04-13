package sampling;

import java.awt.*;

import dataView.*;
import axis.*;


public class TreatGeneratorView extends DataView {
	static final private Color kPink = new Color(0xFFCCFF);
	static final private Color kDarkRed = new Color(0x990000);
	static final private Color kPaleBlue = new Color(0x99CCFF);
	static final private Color kDarkBlue = new Color(0x000099);
	static final private Color kLightGreen = new Color(0xCCFFCC);
	static final private Color kDarkGreen = new Color(0x666600);
	static final private Color[] probBackground = {kPaleBlue, kPink, kLightGreen};
	static final private Color[] probForeground = {kDarkBlue, kDarkRed, kDarkGreen};
	
	static final private int kRightBorder = 4;
	static final private int kImageFractGap = 4;
	static final private int kArrowGap = 7;
	static final private int kFractLineGap = 2;
	
	private VertAxis theAxis;
	private int targetCount[];
	private Image treatImages[];
	
	private double currentRandomProb = -1.0;
	private int currentTreat = 0;
	
	private NumValue numerVal = new NumValue(0, 0);
	private NumValue denomVal = new NumValue(0, 0);
	
	public TreatGeneratorView(DataSet theData, XApplet applet, VertAxis theAxis, int targetCount[],
																															Image treatImages[]) {
		super(theData, applet, new Insets(15, 0, 15, 0));
		this.theAxis = theAxis;
		this.targetCount = targetCount;
		this.treatImages = treatImages;
	}
	
	public int generateRandomCat(double randomProb) {
		currentRandomProb = randomProb;
		
		CatVariable catVar = getCatVariable();
		int[] currentCount = catVar.getCounts();
		
		double denom = currentCount[0];
		int cum = 0;
		currentTreat = 0;
		for (int i=1 ; i<targetCount.length ; i++) {
			cum += targetCount[i] - currentCount[i];
			if (randomProb <= cum / denom) {
				currentTreat = i;
				break;
			}
		}
		return currentTreat;
	}
	
	public void updateProbs() {
		currentTreat = 0;
		repaint();
	}
	
	//----------------------------------------------------------------
	
	public void paintView(Graphics g) {
		CatVariable catVar = getCatVariable();
		int[] currentCount = catVar.getCounts();
		
		int denom = currentCount[0];
		int[] numer = new int[targetCount.length - 1];
		for (int i=0 ; i<targetCount.length - 1 ; i++)
			numer[i] = targetCount[i+1] - currentCount[i+1];
		
		if (currentTreat > 0) {		//	Show probs used before selecting current treatment
			denom ++;
			numer[currentTreat - 1] ++;
		}
		
		if (denom == 0)
			return;
		
		denomVal.setValue(denom);
		int maxCountWidth = denomVal.stringWidth(g);
		
		int fractCenter = getSize().width - maxCountWidth / 2 - kRightBorder;
		int imageLeft = getSize().width - maxCountWidth - kRightBorder - kImageFractGap - TreatmentImages.kWidth;
		int arrowRt = imageLeft - kArrowGap;
		int ascent = g.getFontMetrics().getAscent();
		
		int top = theAxis.numValToRawPosition(0.0);
		int cumCount = 0;
		
		Point p1 = null;
		Point p2 = null;
		
		for (int i=0 ; i<numer.length ; i++)
			if (numer[i] > 0) {
				int bottom = top;
				cumCount += numer[i];
				top = theAxis.numValToRawPosition(cumCount / (double)denom);
				p1 = translateToScreen(0, top, p1);
				p2 = translateToScreen(getSize().width, bottom, p2);
				g.setColor(probBackground[i]);
				g.fillRect(p1.x, p1.y, (p2.x - p1.x), (p2.y - p1.y));
				
				int midVert = (p1.y + p2.y) / 2;
				
				g.drawImage(treatImages[i], imageLeft, midVert - TreatmentImages.kHeight / 2, this);
				
				g.setColor(probForeground[i]);
				g.drawLine(fractCenter - maxCountWidth / 2, midVert, fractCenter + maxCountWidth / 2, midVert);
				numerVal.setValue(numer[i]);
				numerVal.drawCentred(g, fractCenter, midVert - kFractLineGap - 1);
				denomVal.drawCentred(g, fractCenter, midVert + kFractLineGap + ascent);
			}
		
		if (currentTreat > 0) {
			g.setColor(Color.red);
			
			int currentPt = theAxis.numValToRawPosition(currentRandomProb);
			p1 = translateToScreen(0, currentPt, p1);
			
			g.drawLine(0, p1.y, arrowRt, p1.y);
			g.drawLine(0, p1.y - 1, arrowRt - 1, p1.y - 1);
			g.drawLine(0, p1.y + 1, arrowRt - 1, p1.y + 1);
			for (int i=0 ; i<4 ; i++)
				g.drawLine(arrowRt - 2 - i, p1.y - 2 - i, arrowRt - 2 - i, p1.y + 2 + i);
			
			g.setColor(getForeground());
		}
	}
	
	//----------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
}
