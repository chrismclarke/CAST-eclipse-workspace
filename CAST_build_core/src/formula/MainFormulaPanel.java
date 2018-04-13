package formula;

import java.awt.*;

import dataView.*;


public class MainFormulaPanel extends FormulaPanel {
	static final private int kImageFormulaGap = 6;
	
	static final private int kBorder = 3;
	
	static final private int kEqualsGap = 8;
	static final private int kEqualsWidth = 7 + 2 * kEqualsGap;
	
	static final private Color kResultColor = new Color(0x660000);
	static final private Color kResultBackground = new Color(0xF4FFDC);
	
	private Image leftImage = null;
	private int leftWidth, leftAscent, leftDescent;
	private String leftString = null;
	
	private FormulaPanel formula;
	private ResultValue result;
	
	private Dimension resultSize;
	private int resultAscent;
	
	public MainFormulaPanel(FormulaPanel formula, NumValue maxResultVal, FormulaContext context) {
		super(context);
		
		if (formula != null) {
			formula.setParent(this);
			this.formula = formula;
			add(formula);
		}
		
		Font mainFont = context.getFont();
		Font resultFont = new Font(mainFont.getName(), Font.BOLD, mainFont.getSize());
		FormulaContext resultContext = new FormulaContext(kResultColor, resultFont, context.getApplet());
		result = new ResultValue(new DataSet(), maxResultVal, resultContext);
		result.setParent(this);
		result.setValueBackground(kResultBackground);
		add(result);
	}
	
	public MainFormulaPanel(Image leftImage, int imageWidth, int imageAscent, int imageDescent,
							FormulaPanel formula, NumValue maxResultVal, FormulaContext context) {
		this(formula, maxResultVal, context);
		
		this.leftImage = leftImage;
		leftWidth = imageWidth;
		leftAscent = imageAscent;
		leftDescent = imageDescent;
	}
	
	public MainFormulaPanel(String leftString, FormulaPanel formula, NumValue maxResultVal,
																																FormulaContext context) {
		this(formula, maxResultVal, context);
		
		this.leftString = leftString;
	}
	
	public void setFormula(FormulaPanel formula) {
		formula.setParent(this);
		this.formula = formula;
		
		add(formula);
		add(result);
	}
	
	public void changeMaxValue(NumValue maxVal) {
		result.changeMaxValue(maxVal);		//		does invalidate()
	}
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			formula.initialise(formula.getGraphics());
			
			resultSize = result.getMinimumSize();
			resultAscent = result.getLabelBaseline(result.getGraphics());
			
			if (leftString != null) {
				leftWidth = g.getFontMetrics().stringWidth(leftString);
				leftAscent = ascent;
				leftDescent = descent;
			}
			
			layoutWidth = 0;
			if (leftImage != null || leftString != null)
				layoutWidth = leftWidth + kImageFormulaGap;
			layoutWidth += formula.layoutWidth + kEqualsWidth + resultSize.width;
			
			layoutAscent = Math.max(formula.layoutAscent, resultAscent);
			if (leftImage != null || leftString != null)
				layoutAscent = Math.max(layoutAscent, leftAscent);
			
			layoutDescent = Math.max(formula.layoutDescent, resultSize.height - resultAscent);
			if (leftImage != null || leftString != null)
				layoutDescent = Math.max(layoutDescent, leftDescent);
			return true;
		}
		else
			return false;
	}
	
	public void layoutContainer(Container parent) {
		initialise(getGraphics());
		
		int horizStart = (parent.getSize().width - layoutWidth) / 2;
		int baseline = (parent.getSize().height + layoutAscent - layoutDescent) / 2;
		
		if (leftImage != null || leftString != null)
			horizStart += leftWidth + kImageFormulaGap;
		
		formula.setBounds(horizStart, baseline - formula.layoutAscent, formula.layoutWidth,
															formula.layoutAscent + formula.layoutDescent);
		horizStart += formula.layoutWidth + kEqualsWidth;
		
		result.setBounds(horizStart, baseline - resultAscent, resultSize.width, resultSize.height);
	}
	
//	public void fixedText() {
//		result.displayResult();
//	}
//	
//	public void changedText() {
//		result.clearResult();
//	}
	
	public Insets insets() {
		return new Insets(kBorder, kBorder, kBorder, kBorder);
	}
	
	protected void paintAroundItems(Graphics g) {
		int horizStart = (getSize().width - layoutWidth) / 2;
		int baseline = (getSize().height + layoutAscent - layoutDescent) / 2;
		
		if (leftString != null) {
			g.drawString(leftString, horizStart, baseline);
			horizStart += leftWidth + kImageFormulaGap;
		}
		else if (leftImage != null) {
			g.drawImage(leftImage, horizStart, baseline - leftAscent, this);
			horizStart += leftWidth + kImageFormulaGap;
		}
		
		horizStart += formula.layoutWidth + kEqualsGap;
		
		g.drawLine(horizStart, baseline - 3, horizStart + 6, baseline - 3);
		g.drawLine(horizStart, baseline - 6, horizStart + 6, baseline - 6);
	}
	
	protected double evaluateFormula() {
		return formula.evaluateFormula();
	}
	
	public void displayResult() {
		result.displayResult();
	}
	
	public NumValue getResult() {
		return result.getResult();
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		displayResult();
		return true;
	}
}