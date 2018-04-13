package exper;

import java.awt.*;

import dataView.*;

import experProg.*;


public class TwoTreatPictView extends CoreMovePictView {
//	static final public String TWO_TREAT_PICT_PLOT = "twoTreatPict";
	
	static final private Color kBgColor[][] = {{new Color(0xFFFFFF), new Color(0xBBFFFF)},
																				{new Color(0xFFDDFF), new Color(0xD4D4FF)}};
	static final private Color kColColour = new Color(0x000099);
	static final public Color kRowColour = new Color(0x990000);
	
	private String responseKey;
	private NumValue maxRowTreat;
	private double treatEffectConst, treatEffectScaling;
	
//	private int oldPerm[] = null;
//	private boolean doingAnimation = false;
	
	public TwoTreatPictView(DataSet data, CoreMultiFactorApplet applet, long randomSeed, String permKey,
											String rowTreatKey, String colTreatKey, String responseKey,
											NumValue maxRowTreat, double treatEffectConst, double treatEffectScaling) {
		super(data, applet, randomSeed, permKey, rowTreatKey, colTreatKey);
		this.responseKey = responseKey;
		this.maxRowTreat = maxRowTreat;
		this.treatEffectConst = treatEffectConst;
		this.treatEffectScaling = treatEffectScaling;
	}
	
	protected void drawFooting(Graphics g, int leftBorder) {
		FontMetrics fm = g.getFontMetrics();
		CatVariable colVar = (CatVariable)getVariable(colTreatKey);
		int n = colVar.noOfCategories();
		
		int baseline = getSize().height - kHeadingGap - fm.getDescent();
		g.setColor(kColColour);
		
		LabelValue varName = new LabelValue(colVar.name);
		varName.drawCentred(g, leftBorder + (getSize().width - leftBorder) / 2, baseline);
		baseline -= (fm.getAscent() + fm.getDescent() + fm.getLeading());
		for (int i=0 ; i<n ; i++) {
			int horizCenter = leftBorder + ((getSize().width - leftBorder) * (2 * i + 1)) / (2 * n);
			colVar.getLabel(i).drawCentred(g, horizCenter, baseline);
		}
		
		g.setColor(getForeground());
	}
	
	protected int getFootingHt(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		return 2 * kHeadingGap + 2 * (fm.getAscent() + fm.getDescent()) + fm.getLeading();
	}
	
	protected int drawLefting(Graphics g, int bottomBorder) {
		FontMetrics fm = g.getFontMetrics();
		CatVariable rowVar = (CatVariable)getVariable(rowTreatKey);
		int n = rowVar.noOfCategories();
		
		FactorResponseVariable resp = (FactorResponseVariable)getVariable(responseKey);
		double effect[] = resp.getEffects(1);
		if (effect.length != n)
			System.out.println("Wrong index for row effect");
		NumValue temp = new NumValue(0, maxRowTreat.decimals);
		
		int maxWidth = maxRowTreat.stringWidth(g);
		int ascent = fm.getAscent();
		g.setColor(kRowColour);
		for (int i=0 ; i<n ; i++) {
			int vertCenter = ((getSize().height - bottomBorder) * (2 * i + 1)) / (2 * n);
			temp.setValue(treatEffectConst + treatEffectScaling * effect[i]);
			temp.drawLeft(g, kHeadingGap + maxWidth, vertCenter + ascent / 2);
		}
		
		g.setColor(getForeground());
		return 2 * kHeadingGap + maxWidth;
	}
	
	protected void drawBackground(Graphics g, int tableLeft, int tableBottom) {
		int top = kTopBottomBorder;
		int bottom = getSize().height - tableBottom - kTopBottomBorder;
		int left = tableLeft + kLeftRightBorder;
		int right = getSize().width - kLeftRightBorder;
		
		CatVariable colVar = (CatVariable)getVariable(colTreatKey);
		int nColCats = colVar.noOfCategories();
		
		for (int i=0 ; i<rows ; i++) {
			int y0 = top + i * (bottom - top) / rows;
			int y1 = top + (i+1) * (bottom - top) / rows;
			for (int j=0 ; j<nColCats ; j++) {
				g.setColor(kBgColor[i % 2][j % 2]);
				int x0 = left + j * (right - left) / nColCats;
				int x1 = left + (j+1) * (right - left) / nColCats;
				g.fillRect(x0, y0, (x1 - x0), (y1 - y0));
			}
		}
	}
	
	protected int getPictureIndex(int i) {
		return i;
	}
}
	
