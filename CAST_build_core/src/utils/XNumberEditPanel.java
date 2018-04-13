package utils;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import dataView.*;



public class XNumberEditPanel extends XPanel implements KeyListener, Runnable, StatusInterface {
//	static private boolean isMac = XApplet.osType == XApplet.OS_MAC;
//	static private boolean isXP = XApplet.osType == XApplet.OS_XP;
	
	static final private int kKeyWait = 2000;		//	sec / 1,000
	
	static final private double kDisabledPropn = 0.3;
	
	static final private Boolean kPendingArg = Boolean.valueOf(true);
	static final private Boolean kNotPendingArg = Boolean.valueOf(false);
	
	private JTextField edit;
	private JLabel editLabel = null;
	private JLabel editUnits = null;
	
	private boolean isInteger = false;
	private int minInteger = Integer.MIN_VALUE;
	private int maxInteger = Integer.MAX_VALUE;
	private double minDouble = Double.NEGATIVE_INFINITY;
	private double maxDouble = Double.POSITIVE_INFINITY;
	
	private Thread runner = null;
	private String oldText = null;
	
//	private boolean initialised = false;
	
	public XNumberEditPanel(String label, String startText, XApplet applet) {
		this(label, startText, 0, applet);
	}
	
	public XNumberEditPanel(String label, int columns, XApplet applet) {
		this(label, null, columns, applet);
	}
	
	public XNumberEditPanel(String label, String startText, int columns, XApplet applet) {
		this(label, null, startText, columns, applet);
	}
	
	public XNumberEditPanel(String label, String units, String startText, int columns, XApplet applet) {
		int horizGap = (label == null && units == null) ? 0 : 4;
		setLayout(new FlowLayout(FlowLayout.CENTER, horizGap, 0));
//		setOpaque(false);
		
		edit = new JTextField(startText, columns);
		edit.addKeyListener(this);
		edit.setBackground(Color.white);
		edit.setDisabledTextColor(DataView.dimColor(getForeground(), kDisabledPropn));
//		setStandardMargin();			//	Changing the border (or margin) seems to make a white border on outside of box
		
		if (label != null) {
			editLabel = new JLabel(label);
			editLabel.setLabelFor(edit);
			add(editLabel);
		}
		
		add(edit);
		
		if (units != null) {
			editUnits = new JLabel(units);
			editUnits.setLabelFor(edit);
			add(editUnits);
		}
		
		if (applet != null)
			setFont(applet.getStandardFont());		//	formula.Edit sets font immediately
	}
	
	public String getStatus() {
		return edit.getText();
	}
	
	@SuppressWarnings("deprecation")
	public void setStatus(String status) {
		edit.setText(status);
		postEvent(new Event(this, Event.ACTION_EVENT, null));		//	to allow other parts of applet to reflect this value
	}
	
	public boolean isEnabled() {
		return edit.isEnabled();
	}
	
	public void enable() {
		edit.setEnabled(true);
		if (editLabel != null)
			editLabel.setForeground(edit.getForeground());
		if (editUnits != null)
			editUnits.setForeground(edit.getForeground());
	}
	
	public void disable() {
		edit.setEnabled(false);
		if (editLabel != null)
			editLabel.setForeground(edit.getDisabledTextColor());
		if (editUnits != null)
			editUnits.setForeground(edit.getDisabledTextColor());
	}
	
	public void setIntegerType() {
		isInteger = true;
	}
	
	public void setIntegerType(int minInteger, int maxInteger) {
		isInteger = true;
		this.minInteger = minInteger;
		this.maxInteger = maxInteger;
	}
	
	public void setDoubleType(double minDouble, double maxDouble) {
		isInteger = false;
		this.minDouble = minDouble;
		this.maxDouble = maxDouble;
	}
	
	public void setLabelText(String label) {
		editLabel.setText(label);
	}
	
	public void setUnits(String units) {
		if (units == null)
			units = "";
		editUnits.setText(units);
	}
	
	public void setIntegerValue(int value) {
		edit.setText(String.valueOf(value));
	}
	
	public void setDoubleValue(NumValue value) {
		edit.setText(value.toString());
	}
	
	public void clearValue() {
		edit.setText("");
	}
	
	public boolean isClear() {
		return edit.getText().length() == 0;
	}
	
	public void requestFocus() {
		edit.requestFocus();
	}
	
	public void selectAll() {
		edit.selectAll();
	}
	
	public int getIntValue() {
		try {
			String newText = edit.getText();
			int result = (newText.length() == 0 || newText.equals("-")) ? 0 : Integer.parseInt(newText);
			if (result < minInteger || result > maxInteger)
				throw new NumberFormatException();
			return result;
		} catch (NumberFormatException ex) {
			return minInteger;
		}
	}
	
	public double getDoubleValue() {
		try {
			NumValue tempDouble = new NumValue(edit.getText());
			double result = tempDouble.toDouble();
			if (result < minDouble || result > maxDouble)
				throw new NumberFormatException();
			return result;
		} catch (NumberFormatException ex) {
			return minDouble;			//	should never be returned
		}
	}
	
	public NumValue getNumValue() {
		try {
			NumValue tempDouble = new NumValue(edit.getText());
			double result = tempDouble.toDouble();
			if (result < minDouble || result > maxDouble)
				throw new NumberFormatException();
			return tempDouble;
		} catch (NumberFormatException ex) {
			return new NumValue(minDouble);			//	should never be returned
		}
	}
	
	public void setFont(Font f) {
		if (edit != null)
			edit.setFont(f);
		if (editLabel != null)
			editLabel.setFont(f);
		if (editUnits != null)
			editUnits.setFont(f);
		super.setFont(f);
	}
	
	public void setLabelFont(Font f) {
		if (editLabel != null)
			editLabel.setFont(f);
		if (editUnits != null)
			editUnits.setFont(f);
	}
	
	public void setForeground(Color c) {
		if (edit != null) {
			edit.setForeground(c);
			edit.setDisabledTextColor(DataView.dimColor(c, kDisabledPropn));
		}
		if (editLabel != null)
			editLabel.setForeground(c);
		if (editUnits != null)
			editUnits.setForeground(c);
		super.setForeground(c);
	}
	
/*
	public void lockBackground(Color c) {
		super.lockBackground(c);
		initialised = true;
	}
*/
	
	public void setColumns(int columns) {
		edit.setColumns(columns);
	}
	
//	protected void setStandardMargin() {
//		if (isMac)
//			setValueMargin(1, 1, 0, 1);
//		else
//			setValueMargin(0, 1, 0, 1);
//	}
//	
//	private void setValueMargin(int top, int left, int bottom, int right) {
//		Border border = edit.getViewBorder();
//		Border innerBorder = new EmptyBorder(top, left, bottom, right);
//		edit.setBorder(new CompoundBorder(border, innerBorder));
//	}
	
	public void keyTyped(KeyEvent e) {
//		String newText = edit.getText();
//		System.out.println("key typed: now = " + newText);
	}

	public void keyPressed(KeyEvent e) {
		if (oldText == null)
			oldText = edit.getText();
//		System.out.println("key pressed: old = " + oldText);
	}

	@SuppressWarnings("deprecation")
	public void keyReleased(KeyEvent e) {
		if (simpleEdit())
			checkValue();
		else {
			if (runner != null)
				runner.stop();
			runner = new Thread(this);
			runner.start();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		try {
			Thread.sleep(kKeyWait / 3);
			postEvent(new Event(this, Event.ACTION_EVENT, kPendingArg));
			Thread.sleep(kKeyWait * 2 / 3);
		} catch (InterruptedException e) {
			System.out.println("Edit wait interrupted: " + e);
		}
		checkValue();
		runner = null;
	}
	
	private boolean simpleEdit() {
		if (isInteger)
			return minInteger == Integer.MIN_VALUE || minInteger == 0;
		else
			return minDouble == Double.NEGATIVE_INFINITY || minDouble == 0.0;
	}

	@SuppressWarnings("deprecation")
	private void checkValue() {
		String newText = edit.getText();
//		System.out.println("key released: text = " + newText);
		try {
			if (isInteger) {
				int tempInt = (newText.length() == 0 || newText.equals("-")) ? 0 : Integer.parseInt(newText);
				if (tempInt < minInteger || tempInt > maxInteger)
					throw new NumberFormatException();
			}
			else {
				NumValue tempDouble = new NumValue(newText);
				if (tempDouble.toDouble() < minDouble || tempDouble.toDouble() > maxDouble)
					throw new NumberFormatException();
			}
			postEvent(new Event(this, Event.ACTION_EVENT, null));
		} catch (NumberFormatException ex) {
			edit.setText(oldText);
			postEvent(new Event(this, Event.ACTION_EVENT, kNotPendingArg));
		}
		oldText = null;
	}
	
	protected void paintComponent(Graphics g) {
/*
		if (!initialised) {
			Color bg = getParent().getBackground();
			setBackground(bg);
		}
	*/
		
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
	
		super.paintComponent(g);
	}
	
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}
}
