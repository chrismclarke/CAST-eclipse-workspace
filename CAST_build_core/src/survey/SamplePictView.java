package survey;

import java.awt.*;
import java.util.*;

import dataView.*;
import random.RandomInteger;


public class SamplePictView extends DataView {
//	static final public String SAMPLE_PICT_PLOT = "samplePict";
	
	static final public int WHITE = 0;
	static final public int BLACK = 1;
	
	static final public int PICTURE = 0;
	static final public int SYMBOL = 1;
	static final public int VALUE = 2;
	
	static final protected int kHeadingGap = 3;
	
	static final protected int kTopBottomBorder = 6;
	static final protected int kLeftRightBorder = 10;
	
	static final protected Color kPink = new Color(0xFFDDDD);
	static final protected Color kPaleBlue = new Color(0xDDDDFF);
	
	static final private String kWhiteNames[] = {"man", "woman"};
	static final private String kBlackNames[] = {"aman", "awoman"};
	
	
	private LabelValue kPopulationName, kSampleName;
	
	private Random sampleGenerator;
	private Flags sampleFlags;
	private boolean[] inSample;
	protected int sampleSize, rows, cols, rowCycle;
	private boolean sampleSelected = false;
	
	private boolean popNotSamp = true;
	private int displayType = PICTURE;
	
	protected long popnRandomSeed;
	
	protected PeopleImages theImages;
	
	private RandomInteger horizOffsetGenerator = null;
	private RandomInteger vertOffsetGenerator = null;
	protected int horizOffset[], vertOffset[];
	
	protected RandomInteger generator[];
	protected int type[][];		//		index1 = top, mid, bottom
													//		index2 = individual
	
	protected boolean drawIndices = false;
	protected int highlightIndex = -1;
	
	private int peopleColor = WHITE;
	
	public SamplePictView(DataSet theData, XApplet applet,
						int sampleSize, long popnRandomSeed, long randomSeed, int rows, int cols,
						int rowCycle, int maxHorizOffset, int maxVertOffset) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		
		kPopulationName = new LabelValue(applet.translate("Population"));
		kSampleName = new LabelValue(applet.translate("Sample"));
		
		this.sampleSize = sampleSize;
		sampleGenerator = new Random(randomSeed);
		sampleFlags = new Flags(rows * cols);
		inSample = new boolean[rows * cols];
		this.rows = rows;
		this.cols = cols;
		this.rowCycle = rowCycle;
		
		CatVariable x = getCatVariable();
		if (maxHorizOffset > 0) {
			horizOffsetGenerator = new RandomInteger(-maxHorizOffset, maxHorizOffset,
																													x.noOfValues());
			horizOffsetGenerator.setSeed(popnRandomSeed);
		}
		
		this.popnRandomSeed = popnRandomSeed;
		if (maxVertOffset > 0) {
			vertOffsetGenerator = new RandomInteger(-maxVertOffset, maxVertOffset,
																													x.noOfValues());
			vertOffsetGenerator.setSeed(popnRandomSeed + 43489352L);
		}
	}
	
	public void setPeopleColor(int peopleColor) {
		this.peopleColor = peopleColor;
	}
	
	public void doInitialisation(XApplet applet) {
		theImages = loadPictures(applet);
		setupPictureGenerators(popnRandomSeed);
		generateNewPictures();
	}
	
	public void setDisplayType(int displayType) {
		this.displayType = displayType;
		repaint();
	}
	
	public void setDrawIndices(boolean drawIndices) {
		this.drawIndices = drawIndices;
		repaint();
	}
	
	protected PeopleImages loadPictures(XApplet applet) {
		int personWidth = 34;
		int personHeight = 67;
		
		int noOfOptions[] = {3, 3, 3};
		String optionName[] = (peopleColor == WHITE) ? kWhiteNames : kBlackNames;
		
		return new PeopleImages(optionName, noOfOptions, personWidth, personHeight, applet);
	}
	
	protected void setupPictureGenerators(long popnRandomSeed) {
		CatVariable x = getCatVariable();
		generator = new RandomInteger[theImages.noOfOptions.length];
		
		for (int i=0 ; i<theImages.noOfOptions.length ; i++) {
			generator[i] = new RandomInteger(0, theImages.noOfOptions[i] - 1, x.noOfValues());
			generator[i].setSeed(popnRandomSeed + (i + 1) * 49576225L);
		}
		
		type = new int[theImages.noOfOptions.length][];
	}
	
	public void generateNewPictures() {
		for (int i=0 ; i<generator.length ; i++)
			type[i] = generator[i].generate();
		
		if (horizOffsetGenerator != null)	
			horizOffset = horizOffsetGenerator.generate();
		if (vertOffsetGenerator != null)	
			vertOffset = vertOffsetGenerator.generate();
	}
	
	public boolean canShowSample() {
		return sampleSelected;
	}
	
//--------------------------------------------------------
	
	public void takeSample() {
		int sampleLeft = sampleSize;
		int popnLeft = rows * cols;
		for (int i=0 ; i<rows * cols ; i++) {
			inSample[i] = (sampleLeft > 0 && sampleLeft >= popnLeft * sampleGenerator.nextDouble());
			if (inSample[i])
				sampleLeft --;
			popnLeft --;
		}
		sampleFlags.setFlags(inSample);
		
		popNotSamp = false;
		getData().setSelection(sampleFlags);
	}
	
	public boolean addToSample (int index) {
		if (inSample[index])
			return false;
		
		inSample[index] = true;
		highlightIndex = index;
		sampleFlags.setFlags(inSample);
		
		getData().setSelection(sampleFlags);
		
		return true;
	}
	
	public void clearSample() {
		for (int i=0 ; i<inSample.length ; i++)
			inSample[i] = false;
		highlightIndex = -1;
		sampleFlags.clearFlags();
		getData().clearSelection();
	}
	
	public void clearHighlight() {
		highlightIndex = -1;
		repaint();
	}
	
//--------------------------------------------------------
	
	public void showPopNotSamp(boolean popNotSamp) {
		this.popNotSamp = popNotSamp;
		repaint();
	}
	
	protected void drawSymbol(Graphics g, int x, int y, int valueIndex, boolean dimmed,
																								boolean isSucceess) {
		g.setColor(isSucceess ? (dimmed ? kPaleBlue : Color.blue)
													: (dimmed ? kPink : Color.red));
		
		g.drawOval(x - 12, y - 12, 16, 16);
		g.drawOval(x - 11, y - 11, 14, 14);
		g.drawLine(x - 11, y - 9, x - 8, y - 12);
		g.drawLine(x - 11, y - 1, x - 8, y + 2);
		g.drawLine(x, y + 2, x + 3, y - 1);
		
		g.drawLine(x - 1, y - 10, x + 11, y - 22);
		g.drawLine(x, y - 10, x + 12, y - 22);
		g.drawLine(x, y - 9, x + 12, y - 21);
		
		if (isSucceess) {
			g.drawLine(x + 2, y - 21, x + 10, y - 21);
			g.drawLine(x + 3, y - 22, x + 11, y - 22);
			g.drawLine(x + 10, y - 21, x + 10, y - 13);
			g.drawLine(x + 11, y - 22, x + 11, y - 14);
		}
		else {
			g.drawLine(x + 2, y - 22, x + 12, y - 12);
			g.drawLine(x + 2, y - 21, x + 11, y - 12);
			g.drawLine(x + 3, y - 22, x + 12, y - 13);
		}
	}
	
	protected void drawPicture(Graphics g, int x, int y, int valueIndex, boolean dimmed,
																								boolean isSuccess) {
		int mainIndex = isSuccess ? 0 : 1;
		
		int left = x - theImages.personWidth / 2;
		if (horizOffset != null)
			left += horizOffset[valueIndex];
		int top = y - theImages.personHeight / 2;
		if (vertOffset != null)
			top += vertOffset[valueIndex];
		
		for (int i=0 ; i<theImages.pict[mainIndex].length ; i++) {
			int pictVersionI = type[i][valueIndex];
			g.drawImage(theImages.pict[mainIndex][i][pictVersionI][dimmed ? 1 : 0], left, top, this);
		}
	}
	
	protected void drawIndex(Graphics g, int x, int y, int valueIndex, NumValue index) {
		int horiz = x;
		if (horizOffset != null)
			horiz += horizOffset[valueIndex];
		int vert = y - 6;
		if (vertOffset != null)
			vert += vertOffset[valueIndex];
		
		index.setValue(valueIndex);
		index.drawCentred(g, horiz, vert);
	}
	
	protected void drawValue(Graphics g, int x, int y, int valueIndex, boolean dimmed,
																																			Value value) {
		int horiz = x;
		if (horizOffset != null)
			horiz += horizOffset[valueIndex];
		int vert = y - 6;
		if (vertOffset != null)
			vert += vertOffset[valueIndex];
		
		g.setColor(dimmed ? Color.lightGray : Color.black);
		value.drawCentred(g, horiz, vert);
	}
	
	protected int drawHeading(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		LabelValue heading = popNotSamp ? kPopulationName : kSampleName ;
		heading.drawCentred(g, getSize().width / 2, kHeadingGap + fm.getAscent());
		
		return 2 * kHeadingGap + fm.getAscent() + fm.getDescent();
	}
	
	public void paintView(Graphics g) {
		Font oldFont = g.getFont();
		g.setFont(getApplet().getBigBoldFont());
		int tableTop = drawHeading(g);
		g.setFont(oldFont);
		
		g.setColor(Color.white);
		g.fillRect(0, tableTop, getSize().width, getSize().height - tableTop);
		g.setColor(getForeground());
		
		CatVariable x = getCatVariable();
		Value successVal = x.getLabel(0);
		NumVariable y = getNumVariable();		//	can be null
		FlagEnumeration fe = getSelection().getEnumeration();
		
		int valueIndex = 0;
		int innerTop = tableTop + kTopBottomBorder + theImages.personHeight / 2;
		int innerBottom = getSize().height - kTopBottomBorder - theImages.personHeight / 2;
		int innerHeight = innerBottom - innerTop;
		
		int innerLeft = kLeftRightBorder + theImages.personWidth / 2;
		int innerRight = getSize().width - kLeftRightBorder - theImages.personWidth / 2;
		int innerWidth = innerRight - innerLeft;
		
		int noOfHorizParts = (cols - 1) * rowCycle + rowCycle - 1;
		
		NumValue index = null;
		if (drawIndices)
			index = new NumValue(0.0, 0);
		
		if (highlightIndex >= 0) {
			for (int rowIndex=0 ; rowIndex<rows ; rowIndex++) {
				int vertCenter = innerTop;
				if (rowIndex > 0)
					vertCenter += rowIndex * innerHeight / (rows - 1);
				
				for (int colIndex=0 ; colIndex<cols ; colIndex++) {
					if (highlightIndex == valueIndex) {
						int horizCenter = innerLeft + (rowIndex % rowCycle + colIndex * rowCycle) * innerWidth / noOfHorizParts;
						g.setColor(Color.yellow);
						int left = horizCenter - theImages.personWidth / 2;
						if (horizOffset != null)
							left += horizOffset[valueIndex];
						int top = vertCenter - theImages.personHeight / 2;
						if (vertOffset != null)
							top += vertOffset[valueIndex];
						g.fillRect(left - 1, top - 1, theImages.personWidth + 2, theImages.personHeight + 2);
						g.setColor(getForeground());
					}
					valueIndex ++;
				}
			}
		}
		
		valueIndex = 0;
		for (int rowIndex=0 ; rowIndex<rows ; rowIndex++) {
			int vertCenter = innerTop;
			if (rowIndex > 0)
				vertCenter += rowIndex * innerHeight / (rows - 1);
			
			for (int colIndex=0 ; colIndex<cols ; colIndex++) {
				int horizCenter = innerLeft + (rowIndex % rowCycle + colIndex * rowCycle) * innerWidth / noOfHorizParts;
				boolean selected = fe.nextFlag() || popNotSamp;
				switch (displayType) {
					case SYMBOL:
						drawSymbol(g, horizCenter, vertCenter, valueIndex, !selected, x.valueAt(valueIndex) == successVal);
						break;
					case PICTURE:
						drawPicture(g, horizCenter, vertCenter, valueIndex, !selected, x.valueAt(valueIndex) == successVal);
						break;
					case VALUE:
						drawValue(g, horizCenter, vertCenter, valueIndex, !selected, y.valueAt(valueIndex));
						break;
				}
				
				if (drawIndices)
					drawIndex(g, horizCenter, vertCenter, valueIndex, index);
				
				valueIndex ++;
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
	
