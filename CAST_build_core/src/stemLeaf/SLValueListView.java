package stemLeaf;

import java.awt.*;

import dataView.*;

import cat.CatSelection;


public class SLValueListView extends DataView {
	static final private int kLeftRightBorder = 20;
	static final private int kTopBottomBorder = 4;
	
	static final private int kValueHorizGap = 15;
	static final private int kValueVertGap = 6;
	
	static final private int kPointWidth = 3;
	
	private CreateStemLeafView stemLeafView;
	private int nRows, nCols;
	
	private boolean initialised = false;
	
	private CatSelection clickSelection;
	
	private int decimals, leftDigits, valueWidth;
	
	public SLValueListView(DataSet theData, XApplet applet, CreateStemLeafView stemLeafView,
																																			int nRows, int nCols) {
		super(theData, applet, new Insets(0,0,0,0));
		this.stemLeafView = stemLeafView;
		this.nRows = nRows;
		this.nCols = nCols;
	}
	
	public void resetList() {
		clickSelection.resetList();
		repaint();
		
		stemLeafView.clearPlotLeaves();
	}
	
	public void selectValue(int i) {
		clickSelection.selectIndex(i);
		repaint();
		
		stemLeafView.addLeaf(i);
	}
	
	protected boolean initialise() {
		if (!initialised) {
			clickSelection = new CatSelection(getNumVariable().noOfValues());
			
			NumVariable yVar = getNumVariable();
			decimals = yVar.getMaxDecimals();
			leftDigits = Math.max(1, yVar.getMaxLeftDigits());
			
			valueWidth = (leftDigits + decimals) * LeafDigitImages.getWidth();
			if (decimals > 0)
				valueWidth += kPointWidth;
			
			initialised = true;
			return true;
		}
		else
			return false;
	}
	
	private Image[] checkDigitColor(int ignoreChars, Image[] dig) {
		if (ignoreChars == 0)
			return LeafDigitImages.digit[LeafDigitImages.RED_DIGITS];
		else if (ignoreChars == -1)
			return LeafDigitImages.digit[LeafDigitImages.BLUE_DIGITS];
		else
			return dig;
	}
	
	private void drawValue(Graphics g, int intVal, int valueTop, int valueRight,
								int ignoreChars, int decimals, boolean neg, int valueIndex) {
		boolean selected = (valueIndex == clickSelection.selectedVal);
		
		Image dig[] = clickSelection.valueClicked[valueIndex] ? LeafDigitImages.digit[LeafDigitImages.GREY_DIGITS]
																: LeafDigitImages.digit[LeafDigitImages.BLACK_DIGITS];
		if (decimals > 0) {
			while (decimals > 0) {
				int d = intVal % 10;
				intVal /= 10;
				if (selected)
					dig = checkDigitColor(ignoreChars, dig);
				g.drawImage(dig[d], valueRight - LeafDigitImages.getWidth(), valueTop, this);
				valueRight -= LeafDigitImages.getWidth();
				decimals --;
				ignoreChars --;
			}
			g.setColor(clickSelection.valueClicked[valueIndex] ? Color.gray  :  Color.black);
			g.fillRect(valueRight - kPointWidth, valueTop + LeafDigitImages.kDigitHeight / 2, 1, 1);
			valueRight -= kPointWidth;
		}
		
		if (intVal == 0) {
			if (selected)
				dig = checkDigitColor(ignoreChars, dig);
			g.drawImage(dig[0], valueRight - LeafDigitImages.getWidth(), valueTop, this);
		}
		else
			while (intVal > 0) {
				int d = intVal % 10;
				intVal /= 10;
				if (selected)
					dig = checkDigitColor(ignoreChars, dig);
				g.drawImage(dig[d], valueRight - LeafDigitImages.getWidth(), valueTop, this);
				valueRight -= LeafDigitImages.getWidth();
				ignoreChars --;
			}
		if (selected && ignoreChars == -1) {
			g.drawImage(LeafDigitImages.digit[LeafDigitImages.BLUE_DIGITS][0],
																							valueRight - LeafDigitImages.getWidth(), valueTop, this);
			valueRight -= LeafDigitImages.getWidth();
		}
		if (neg)
			g.drawImage(LeafDigitImages.minusGrey, valueRight - LeafDigitImages.getWidth(), valueTop, this);
	
		if (selected)
			stemLeafView.drawArrow(g, valueTop + LeafDigitImages.kDigitHeight / 2);
	}
	
	public void paintView(Graphics g) {
		initialise();
		
		int valueLineHt = LeafDigitImages.kDigitHeight + kValueVertGap;
		int localRows = getSize().height / valueLineHt;
		
		int stemPos = stemLeafView.axis.stemPower;
		int ignoreChars = decimals + stemPos - 1;
		
		NumVariable yVar = getNumVariable();
		for (int i=0 ; i<yVar.noOfValues() ; i++) {
			double y = yVar.doubleValueAt(i);
			int rowIndex = i % localRows;
			int colIndex = i / localRows;
			
			int valueTop = kTopBottomBorder + rowIndex * valueLineHt;
			int valueRight = kLeftRightBorder + valueWidth + colIndex * (valueWidth + kValueHorizGap);
			
			double realVal = y;
			for (int j=0 ; j<decimals ; j++)
				realVal *= 10.0;
			int intVal = (int)Math.round(realVal);
			boolean neg = (intVal < 0);
			if (neg)
				intVal = -intVal;
			
			drawValue(g, intVal, valueTop, valueRight, ignoreChars, decimals, neg, i);
		}
	}

//-----------------------------------------------------------------------------------
	
	public Dimension getMinimumSize() {
		initialise();
		
		int width = 2 * kLeftRightBorder + nCols * valueWidth + (nCols - 1) * kValueHorizGap;
		int height = 2 * kTopBottomBorder + nRows * LeafDigitImages.kDigitHeight
																												+ (nRows - 1) * kValueVertGap;
		return new Dimension(width, height);
	}

//------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		int valueLineHt = LeafDigitImages.kDigitHeight + kValueVertGap;
		int localRows = getSize().height / valueLineHt;
		y -= (kTopBottomBorder - kValueVertGap / 2);
		x -= kLeftRightBorder;
		
		if (x < 0 || y < 0)
			return null;
		
		int row = y / valueLineHt;
		int col = x / (valueWidth + kValueHorizGap);
		int horizWithinRow = x - col * (valueWidth + kValueHorizGap);
		
		if (row >= localRows || horizWithinRow > valueWidth)
			return null;
		
		int index = col * localRows + row;
		
		if (index < clickSelection.noOfValues() && !clickSelection.valueClicked[index])
			return new IndexPosInfo(index);
		else
			return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		IndexPosInfo indexPos = (IndexPosInfo)startInfo;
		selectValue(indexPos.itemIndex);
		
		return false;
	}
	
}