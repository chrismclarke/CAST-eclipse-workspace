package indicator;

import java.awt.*;

import dataView.*;


public class TermRecord {
	static final private String kPlusMinusString = "\u00B1";
	
	static final private int kBoxVertGap = 6;
	static final private int kBoxVertBorder = 3;
	static final private int kBoxHorizBorder = 6;
	static final private int kInterceptLinesGap = 2;
	static final private int kHeadingBoxGap = 4;
	static final private int kCatBoxGap = 6;
	static final private int kPlusMinusGap = 5;
	
//	static final private int baseLevel = 0;
	
	protected Variable var;
	private NumValue maxParam, maxPlusMinus;
	protected String heading1String, heading2String;
	
	protected int boxWidth, boxHeight, box1Top, boxLeft, catNameLeft;
	protected int heading1Baseline, heading2Baseline, param1Baseline;
	
	protected int width, height;
	
	private boolean varNameOverCats = false;
	
	private boolean initialised = false;
	
	public TermRecord(Variable var, NumValue maxParam, NumValue maxPlusMinus) {
		this.var = var;
		this.maxParam = maxParam;
		this.maxPlusMinus = maxPlusMinus;
		heading1String = var.name;
		heading2String = null;
	}
	
	public TermRecord(String baselineCats, NumValue maxParam, NumValue maxPlusMinus, XApplet applet) {	//	intercept
		this.maxParam = maxParam;
		this.maxPlusMinus = maxPlusMinus;
		heading1String = (baselineCats == null) ? applet.translate("Intercept") : applet.translate("Baseline");
		heading2String = baselineCats;
	}
	
	public void setVarNameOverCats(boolean varNameOverCats) {
		this.varNameOverCats = varNameOverCats;
	}
	
	public void setBaselineHeading(String heading2String) {
		this.heading2String = heading2String;
	}
	
	protected void initialise(Graphics g) {
		if (!initialised) {
			doInitialisation(g);
			initialised = true;
		}
	}
	
	protected void doInitialisation(Graphics g) {
		Font standardFont = g.getFont();
		g.setFont(new Font(standardFont.getName(), Font.BOLD, standardFont.getSize()));
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		
		boxWidth = maxParam.stringWidth(g) + 2 * kBoxHorizBorder + 2;
		boxHeight = ascent + 2 * kBoxVertBorder + 2;
		
		int plusMinusWidth = (maxPlusMinus == null) ? 0 : 2 * kPlusMinusGap
														+ fm.stringWidth(kPlusMinusString) + maxPlusMinus.stringWidth(g);
		
		height = ascent + descent;
		heading1Baseline = height - descent;
		width = fm.stringWidth(heading1String);
		
		g.setFont(standardFont);
		fm = g.getFontMetrics();
		ascent = fm.getAscent();
		descent = fm.getDescent();
		
		if (heading2String != null) {
			width = Math.max(width, fm.stringWidth(heading2String));
			height += (ascent + descent + kInterceptLinesGap);
			heading2Baseline = height - descent;
		}
		
		height += kHeadingBoxGap;
		box1Top = height;
		param1Baseline = height + kBoxVertBorder + ascent + 1;
		
		if (var != null && var instanceof CatVariable) {
			CatVariable catVar = (CatVariable)var;
			int nCats = catVar.noOfCategories();
			int maxCatWidth = 0;
			for (int i=0 ; i<nCats ; i++)
				maxCatWidth = Math.max(maxCatWidth, catVar.getLabel(i).stringWidth(g));
			
			if (varNameOverCats)
				maxCatWidth = Math.max(maxCatWidth, width);		//	for first column of interaction table
			width = Math.max(width, boxWidth + maxCatWidth + kCatBoxGap + plusMinusWidth);
			catNameLeft = (width - boxWidth - maxCatWidth - kCatBoxGap - plusMinusWidth) / 2;
			boxLeft = catNameLeft + maxCatWidth + kCatBoxGap;
			height += nCats * boxHeight + (nCats - 1) * kBoxVertGap;
		}
		else {
			width = Math.max(width, boxWidth + plusMinusWidth);
			boxLeft = (width - boxWidth - plusMinusWidth) / 2;
			height += boxHeight;
		}
	}
	
	public void drawParameters(Graphics g, int left, int top, NumValue singleParam,
																					 								NumValue[] catParams, Color c) {
		drawParameters(g, left, top, singleParam, null, catParams, null, c);
	}
	
	protected void drawCatNameColumn(Graphics g, int left, int top, Color c) {
		CatVariable catVar = (CatVariable)var;
		
		int paramBaseline = param1Baseline;
//		int boxTop = top + box1Top;
		
		for (int i=0 ; i<catVar.noOfCategories() ; i++) {
			g.setColor(c);
			catVar.getLabel(i).drawRight(g, left + catNameLeft, top + paramBaseline);
				
			paramBaseline += (boxHeight + kBoxVertGap);
//			boxTop += (boxHeight + kBoxVertGap);
		}
	}
	
	protected void drawCatValueColumn(Graphics g, int left, int top, NumValue[] catParams,
																												NumValue[] catPlusMinus, Color c) {
		CatVariable catVar = (CatVariable)var;
		
		int paramBaseline = param1Baseline;
		int boxTop = top + box1Top;
		int plusMinusSymbolLeft = left + boxWidth + kPlusMinusGap;
		FontMetrics fm = g.getFontMetrics();
		int plusMinusLeft = plusMinusSymbolLeft + fm.stringWidth(kPlusMinusString) + kPlusMinusGap;
		
		for (int i=0 ; i<catVar.noOfCategories() ; i++) {
//			boolean constrainedParam = (i == baseLevel || catParams[i] == null);
			boolean constrainedParam = (catParams[i] == null);
			g.setColor(c);
			if (constrainedParam)
				g.setColor(Color.gray);
			g.drawRect(left, boxTop, boxWidth, boxHeight);
			
			g.setColor(constrainedParam ? Color.lightGray : Color.white);
			g.fillRect(left + 1, boxTop + 1, boxWidth - 1, boxHeight - 1);
			g.setColor(constrainedParam ? Color.gray : c);
			
			if (constrainedParam)
				g.drawString("0", left + kBoxHorizBorder + 1, top + paramBaseline);
			else
				catParams[i].drawRight(g, left + kBoxHorizBorder + 1, top + paramBaseline);
		
			boolean drawPlusMinus = (catPlusMinus != null && catPlusMinus[i] != null);
			if (drawPlusMinus) {
				g.drawString(kPlusMinusString, plusMinusSymbolLeft, top + paramBaseline);
				catPlusMinus[i].drawRight(g, plusMinusLeft, top + paramBaseline);
			}
			
			paramBaseline += (boxHeight + kBoxVertGap);
			boxTop += (boxHeight + kBoxVertGap);
		}
	}
	
	public void drawParameters(Graphics g, int left, int top, NumValue singleParam,
												NumValue singlePlusMinus, NumValue[] catParams, NumValue[] catPlusMinus,
												Color c) {
		initialise(g);
		
		Font standardFont = g.getFont();
		g.setFont(new Font(standardFont.getName(), Font.BOLD, standardFont.getSize()));
		FontMetrics fm = g.getFontMetrics();
		g.setColor(c);
		
		int horizCentre = left + width / 2;
//		int headingWidth = fm.stringWidth(heading1String);
		g.drawString(heading1String, horizCentre - fm.stringWidth(heading1String) / 2,
																																	top + heading1Baseline);
		
		g.setFont(standardFont);
		fm = g.getFontMetrics();
		if (heading2String != null) {
//			headingWidth = fm.stringWidth(heading2String);
			g.drawString(heading2String, horizCentre - fm.stringWidth(heading2String) / 2,
																																	top + heading2Baseline);
		}
		
		if (catParams != null) {
			drawCatNameColumn(g, left, top, c);
			drawCatValueColumn(g, left + boxLeft, top, catParams, catPlusMinus, c);
		}
		else {
			int paramBaseline = param1Baseline;
			int boxTop = top + box1Top;
			
			boolean constrainedParam = (singleParam == null);
			if (constrainedParam)
					g.setColor(Color.gray);
			g.drawRect(left + boxLeft, boxTop, boxWidth, boxHeight);
			g.setColor(constrainedParam ? Color.lightGray : Color.white);
			g.fillRect(left + boxLeft + 1, boxTop + 1, boxWidth - 1, boxHeight - 1);
			g.setColor(constrainedParam ? Color.gray : c);
			if (constrainedParam)
				g.drawString("0", left + boxLeft + kBoxHorizBorder + 1, top + paramBaseline);
			else
				singleParam.drawRight(g, left + boxLeft + kBoxHorizBorder + 1, top + paramBaseline);
			
			if (singlePlusMinus != null) {
				int plusMinusLeft = left + boxLeft + boxWidth + kPlusMinusGap;
				g.drawString(kPlusMinusString, plusMinusLeft, top + paramBaseline);
				plusMinusLeft += fm.stringWidth(kPlusMinusString) + kPlusMinusGap;
				singlePlusMinus.drawRight(g, plusMinusLeft, top + paramBaseline);
			}
		}
	}
	
//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize(Graphics g) {
		initialise(g);
		return new Dimension(width + 1, height + 1);
	}
}
	
