package valueList;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import dataView.*;


abstract public class ValueView extends DataView {
	static final private int kUnitsLeftBorder = 3;
	static final protected int kLabelLeftBorder = 3;
	static final private int kLabelRightBorder = 5;
	
	static final public Color kHighlightBackground = new Color(0xE8FFB9);
	
	static final private String kEqualsString = "=";
	
	static class MyJLabel extends JLabel {
		MyJLabel(String s) {
			super(s);
		}
	
		protected void paintComponent(Graphics g) {
			Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
		
			super.paintComponent(g);
		}
	}
	
	static class MyJTextField extends JTextField {
		MyJTextField() {
//			setEditable(false);
			setEnabled(false);
			setDisabledTextColor(Color.black);
			setOpaque(true);
		}
		
		protected void paintComponent(Graphics g) {
			Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
		
			super.paintComponent(g);
		}
	}
	
	private JTextField valueField;
	private JLabel unitsLabel, equalsLabel;
	private String unitsString;
	private boolean addEquals = false;
	
	private Font valueFont;		//	bold version of label font
	
	private boolean boxedValue = true;
	
	private Color valueBackground = Color.white;
	private Color highlightBackground = kHighlightBackground;
	
	protected boolean initialised = false;
	private int itemHeight, itemWidth, labelWidth, maxValueWidth, fontAscent, fontDescent;
	
	public ValueView(DataSet theData, XApplet applet) {
		super(theData, applet, null);
		
		setLayout(new BorderLayout(kUnitsLeftBorder, 0));
		setOpaque(true);
		
		valueField = new MyJTextField();
		setFieldBorder(Color.black, valueBackground);			//	**************
		add("Center", valueField);
		
		setFont(applet.getStandardFont());
	}
	
	private void setFieldBorder(Color textColor, Color textBackground) {
		if (valueField != null) {
			Border outerBorder = BorderFactory.createLineBorder(textColor, 1);
			Border innerBorder = BorderFactory.createMatteBorder(2, 3, 2, 2, textBackground);
			Border border = BorderFactory.createCompoundBorder(outerBorder, innerBorder);
			valueField.setBorder(border);
		}
	}
	
	public void unboxValue() {
		Border noBorder = new EmptyBorder(0, 0, 0, 0);
		valueField.setBorder(noBorder);
		boxedValue = false;
	}
	
	public void setCenterValue(boolean centerValue) {
		valueField.setHorizontalAlignment(centerValue ? SwingConstants.CENTER : SwingConstants.LEFT);
	}
	
	public void setValueBackground(Color c) {
		valueBackground = c;
		setFieldBorder(getForeground(), c);		//	****************
	}
	
	public void setHighlightBackground(Color c) {
		highlightBackground = c;
	}
	
	public double getValue() {
		String valueString = getValueString();
		try {
			return Double.parseDouble(valueString);
		} catch (NumberFormatException e) {
		}
		return Double.NaN;
	}
	
	public void setUnitsString(String unitsString) {
		this.unitsString = unitsString;
		if (unitsLabel == null) {
			unitsLabel = new MyJLabel(unitsString);
			unitsLabel.setLabelFor(valueField);
			unitsLabel.setForeground(getForeground());
			unitsLabel.setFont(getFont());
			add("East", unitsLabel);
		}
		else
			unitsLabel.setText(unitsString);
	}
	
	public void addEqualsSign() {
		addEquals = true;
		equalsLabel = new MyJLabel(kEqualsString);
		equalsLabel.setLabelFor(valueField);
		equalsLabel.setFont(getFont());
		add("West", equalsLabel);
	}
	
	public void setForeground(Color c) {
		super.setForeground(c);
		if (valueField != null) {
			valueField.setForeground(c);
			valueField.setDisabledTextColor(c);
			if (boxedValue)																						//*******
				setFieldBorder(c, valueBackground);											//*******
		}
		if (unitsLabel != null)
			unitsLabel.setForeground(c);
		if (equalsLabel != null)
			equalsLabel.setForeground(c);
	}
	
	public void setFont(Font f) {
		super.setFont(f);
		if (valueField != null) {
				valueFont = new Font(f.getName(), Font.BOLD, f.getSize());
				valueField.setFont(valueFont);
			}
		if (unitsLabel != null)
			unitsLabel.setFont(f);
		if (equalsLabel != null)
			equalsLabel.setFont(f);
	}
	
	protected void drawValueInterior(Graphics g, String theValue) {
		valueField.setText(theValue);
	}
	
	public void setAlignRight() {
		valueField.setHorizontalAlignment(JTextField.RIGHT);
	}
	
	public void setAlignCenter() {
		valueField.setHorizontalAlignment(JTextField.CENTER);
	}

//--------------------------------------------------------------------------------
	
	public void redrawValue() {
		if (isVisible) {
			adjustBackground();
			String newValue = getValueString();
			valueField.setText(newValue == null ? "" : newValue);
		}
	}
	
	public void redrawAll() {
		repaint();
	}
	
	public void revalidate() {
		if (valueField != null)
			valueField.revalidate();
		initialised = false;
	}

//--------------------------------------------------------------------------------
	
	abstract protected int getLabelWidth(Graphics g);
	abstract protected int getMaxValueWidth(Graphics g);
	abstract protected String getValueString();
	abstract protected void drawLabel(Graphics g, int startHoriz, int baseLine);
	abstract protected boolean highlightValue();

//--------------------------------------------------------------------------------
	
	public Dimension getPreferredSize() {
		if (!initialised) {
			Graphics g = getApplet().getGraphics();
			g.setFont(getFont());
			initialise(g);
		}
		return new Dimension(itemWidth, itemHeight);
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
	
	private void initialise(Graphics g) {
		Insets valueBorder = valueField.getBorder().getBorderInsets(valueField);
		
		FontMetrics fm = g.getFontMetrics();
		fontAscent = fm.getAscent();
		fontDescent = fm.getDescent();
		itemHeight = Math.max(fontAscent + valueBorder.top, getLabelAscent(g))
							+ Math.max(fontDescent + valueBorder.bottom, getLabelDescent(g));
		
		Font labelFont = getFont();
		g.setFont(valueFont);
		maxValueWidth = getMaxValueWidth(g) + 2;		//	extra 2 because value was clipped 1 pixel
		g.setFont(labelFont);
		labelWidth = getLabelWidth(g);
		
		itemWidth = valueBorder.left + valueBorder.right + maxValueWidth;
		int labelSpace = 0;
		if (labelWidth > 0)
			labelSpace += kLabelLeftBorder + labelWidth + kLabelRightBorder;
		
		int borderTop = Math.max(getLabelAscent(g) - fontAscent - valueBorder.top, 0);
		int borderBottom = Math.max(getLabelDescent(g) - fontDescent - valueBorder.bottom, 0);
		
		itemWidth += labelSpace;
		if (unitsString != null)
			itemWidth += fm.stringWidth(unitsString) + kUnitsLeftBorder;
		if (addEquals)
			itemWidth += fm.stringWidth(kEqualsString) + kUnitsLeftBorder;
		
		setBorder(new EmptyBorder(borderTop, labelSpace, borderBottom, 0));
		
		redrawValue();
		
		initialised = true;
	}
	
	public void resetSize() {
		initialised = false;
		revalidate();
	}
	
	protected int getLabelAscent(Graphics g) {
		return fontAscent;
	}
	
	protected int getLabelDescent(Graphics g) {
		return fontDescent;
	}
	
	public int getLabelBaseline(Graphics g) {
		Insets valueBorder = valueField.getBorder().getBorderInsets(valueField);
		return Math.max(valueBorder.top + fontAscent, getLabelAscent(g));
	}
	
	private void adjustBackground() {
		if (!boxedValue)
			valueField.setBackground(getBackground());
		else if (highlightValue()) {
			valueField.setBackground(highlightBackground);
			setFieldBorder(getForeground(), highlightBackground);		//	****************
		}
		else {
			valueField.setBackground(valueBackground);
			setFieldBorder(getForeground(), valueBackground);		//	****************
		}
	}
	
	public void paintChildren(Graphics g) {
		if (isVisible) {
			adjustBackground();
		
//			if (!initialised)
//				initialise(g);
				
			String newValue = getValueString();
			valueField.setText(newValue == null ? "" : newValue);
		}
		super.paintChildren(g);
	}
	
	public void paintView(Graphics g) {
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
//		if (!initialised)
//			initialise(g);
//			
//		String newValue = getValueString();
//		valueField.setText(newValue == null ? "" : newValue);
		
//		int valueBoxLeft = 0;
		int baseline = getLabelBaseline(g);
		if (labelWidth > 0) {
			g.setColor(getForeground());
			drawLabel(g, kLabelLeftBorder + labelWidth - getLabelWidth(g), baseline);
		}
	}
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return false;
	}
	
//------------------------------------------------------------
	
								//		These allow ValueView to be dragged -- all components generate mouse events
	public void addMouseListener(MouseListener l) {
		super.addMouseListener(l);
		if (valueField != null)
			valueField.addMouseListener(l);
		if (unitsLabel != null)
			unitsLabel.addMouseListener(l);
		if (equalsLabel != null)
			equalsLabel.addMouseListener(l);
	}
	
	public void addMouseMotionListener(MouseMotionListener l) {
		super.addMouseMotionListener(l);
		if (valueField != null)
			valueField.addMouseMotionListener(l);
		if (unitsLabel != null)
			unitsLabel.addMouseMotionListener(l);
		if (equalsLabel != null)
			equalsLabel.addMouseMotionListener(l);
	}
	
}
