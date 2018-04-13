package utils;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import dataView.*;


abstract public class XSlider extends XPanel implements ChangeListener {
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;					//	min at bottom
	public static final int VERTICAL_INVERSE = 2;	//	min at top
	
	static final public int STANDARD = 0;
	static final public int GREEN = 1;
	
	static final private int kHorizBorder = 5;
	static final private int kVertBorder = 8;
	
	static final private int kMaxTicks = 7;
	
	static final private Color kDarkGreen = new Color(0x009900);
	
	static private boolean isMac = XApplet.osType == XApplet.OS_MAC;
//	static private boolean isXP = XApplet.osType == XApplet.OS_XP;
	
	private CardLayout showHideLayout;
	
	private XPanel sliderPanel;
	
	protected JSlider slider;
	private JLabel title = null, value = null, min = null, max = null;
	
	private Font titleFont = null;
	
	private boolean postEvents = true;
	protected boolean addEquals = false;
	private boolean fixedMinMaxText = false;
	
	private Color enabledColor = Color.black;
	
	private boolean initialised = false;
	
	
	public XSlider(String minText, String maxText, String titleText, int minVal,
											int maxVal, int startVal, int orientation, XApplet applet,
											boolean addEquals) {
		showHideLayout = new CardLayout();
		setLayout(showHideLayout);
//		setOpaque(false);					//	sets background colour when first painted
		
			XPanel blankPanel = new XPanel();
			blankPanel.setOpaque(false);
		add("hide", blankPanel);
		
			sliderPanel = new XPanel();
			sliderPanel.setLayout(new BorderLayout(0, getVertSpacing()));
			sliderPanel.setOpaque(false);
			
				int jOrientation = (orientation == HORIZONTAL) ? SwingConstants.HORIZONTAL : SwingConstants.VERTICAL;
				slider = createSlider(minVal, maxVal, startVal, jOrientation);
			sliderPanel.add("Center", slider);
			
			if (minText != null || maxText != null) {
				int maxWidth = 0;
				if (minText != null)
					maxWidth = minText.length();
				if (maxText != null)
					maxWidth = Math.max(maxWidth, maxText.length());
				
				if (minText != null) {
					min = new JLabel(minText);
					min.setOpaque(false);
					min.setFont(applet.getStandardBoldFont());
					min.setHorizontalAlignment(JLabel.LEFT);
				}
				
				if (maxText != null) {
					max = new JLabel(maxText);
					max.setOpaque(false);
					max.setFont(applet.getStandardBoldFont());
					max.setHorizontalAlignment((orientation == HORIZONTAL) ? JLabel.RIGHT : JLabel.LEFT);
				}
				
				addMinMaxPanel(orientation, applet);
			}
		
			titleFont = applet.getStandardBoldFont();
			this.addEquals = addEquals;
			if (titleText != null)
				setTitle(titleText, applet);
			
//			sliderPanel.moveToBack(slider);
			
		add("show", sliderPanel);
		
		showHideLayout.show(this, "show");
	}
	
	public XSlider(String minText, String maxText, String titleText, int minVal,
											int maxVal, int startVal, int orientation, XApplet applet) {
		this(minText, maxText, titleText, minVal, maxVal, startVal, orientation,
																																applet, false);
	}
	
	public XSlider(String minText, String maxText, String titleText, int minVal,
														int maxVal, int startVal, XApplet applet) {
		this(minText, maxText, titleText, minVal, maxVal, startVal, HORIZONTAL, applet);
	}
	
	public XSlider(String minText, String maxText, String titleText, int minVal,
											int maxVal, int startVal, XApplet applet, boolean addEquals) {
		this(minText, maxText, titleText, minVal, maxVal, startVal, HORIZONTAL, applet, addEquals);
	}
	
	public void setAddEquals(boolean addEquals) {
		this.addEquals = addEquals;
	}
	
	public void setEnabled(boolean enabled) {
		if (enabled == slider.isEnabled())
			return;
		slider.setEnabled(enabled);
		if (enabled)
			adjustForeground(enabledColor);
		else
			adjustForeground(DataView.dimColor(enabledColor, 0.5));
	}
	
	private int getVertSpacing() {
		return isMac ? -4 : -2;
	}
	
	protected JSlider createSlider(int minVal, int maxVal, int startVal, int jOrientation) {
		JSlider sl = new JSlider(jOrientation, minVal, maxVal, startVal);
		sl.setPaintLabels(false);
//		slider.setOpaque(false);					//	sets background colour when first painted
//		slider.setRequestFocusEnabled(false);
		setTickSpacing(minVal, maxVal, sl);
		sl.setPaintTicks(true);
		sl.addChangeListener(this);
		sl.addMouseListener(new MouseAdapter() {
														public void mousePressed(MouseEvent e) {
															DoMousePressed(e);
														}
														public void mouseReleased(MouseEvent e) {
															DoMouseReleased(e);
														}
													});
		return sl;
	}
	
	private void setTickSpacing(int minVal, int maxVal, JSlider sl) {
		sl.setMajorTickSpacing(maxVal - minVal);
		if (maxVal - minVal < kMaxTicks) {
			sl.setMinorTickSpacing(1);
			sl.setSnapToTicks(true);
		}
		else
			sl.setMinorTickSpacing(0);
	}
	
	protected void setTickSpacing(int minVal, int maxVal) {
		setTickSpacing(minVal, maxVal, slider);
	}
	
	private void addMinMaxPanel(int orientation, XApplet applet) {
		if (orientation == HORIZONTAL) {
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new GridLayout(1, 2));
			Border border = BorderFactory.createEmptyBorder(0, kHorizBorder, 0, kHorizBorder);
			bottomPanel.setBorder(border);
			bottomPanel.setOpaque(false);
			
			if (min != null)
				bottomPanel.add(min);
			else {
				XPanel blankPanel = new XPanel();
				blankPanel.setOpaque(false);
				bottomPanel.add(blankPanel);
			}
			
			if (max != null)
				bottomPanel.add(max);
			else {
				XPanel blankPanel = new XPanel();
				blankPanel.setOpaque(false);
				bottomPanel.add(blankPanel);
			}
			
			sliderPanel.add("South", bottomPanel);
		}
		else {
			if (orientation == VERTICAL_INVERSE)
				slider.setInverted(true);
			XPanel sidePanel = new XPanel();
			sidePanel.setLayout(new BorderLayout(0, 0));
			Border border = BorderFactory.createEmptyBorder(kVertBorder, 0, kVertBorder, 0);
			sidePanel.setBorder(border);
			sidePanel.setOpaque(false);
			
			if (min != null)
//				sidePanel.add("North", min);
				sidePanel.add(orientation == VERTICAL_INVERSE ? "North" : "South", min);
			if (max != null)
//				sidePanel.add("South", max);
				sidePanel.add(orientation == VERTICAL_INVERSE ? "South" : "North", max);
				
				XPanel blankPanel = new XPanel();
				blankPanel.setOpaque(false);
			sidePanel.add("Center", blankPanel);
			
			sliderPanel.add("East", sidePanel);
		}
	}
	
	public void setFont(Font f) {
		if (min != null)
			min.setFont(f);
		if (max != null)
			max.setFont(f);
		if (title != null)
			title.setFont(f);
		if (value != null)
			value.setFont(f);
	}
	
	public void setSliderColor(int newColor) {
		Color c = (newColor == GREEN) ? kDarkGreen : Color.black;
		setForeground(c);
	}
	
	public void setForeground(Color c) {
		enabledColor = c;
		adjustForeground(c);
	}
	
	private void adjustForeground(Color c) {		//	does not change enabledColor
		super.setForeground(c);
		if (slider != null)
			slider.setForeground(c);
		if (title != null)
			title.setForeground(c);
		if (value != null)
			value.setForeground(c);
		if (min != null)
			min.setForeground(c);
		if (max != null)
			max.setForeground(c);
	}
	
	public void setSnapToExtremes() {
		slider.setMinorTickSpacing(slider.getMaximum() - slider.getMinimum());
		slider.setSnapToTicks(true);
	}
	
	public void setPostEvents(boolean postEvents) {
		this.postEvents = postEvents;
	}
	
	abstract protected Value translateValue(int val);
	abstract protected int getMaxValueWidth(Graphics g);
	
	public int getValue() {
		return slider.getValue();
	}
	
	public void setValue(int newVal) {
		int oldVal = getValue();
		slider.setValue(newVal);
		int setVal = getValue();
		if (setVal != oldVal) {
			if (value != null) {
				Value val = translateValue(setVal);
				if (val != null)
					value.setText(val.toString());
			}
		}
	}
	
	public void refreshValue() {			//		useful when the value does not change but its translated display changes
		Value val = translateValue(getValue());
		if (val != null)
			value.setText(val.toString());
	}
	
	public void setTitle(String newTitle, XApplet applet) {
		if (newTitle != null && addEquals)
			newTitle += " = ";
			
		if (title == null) {
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 0));
			Border border = BorderFactory.createEmptyBorder(0, kHorizBorder, 0, kHorizBorder);
			topPanel.setBorder(border);
			topPanel.setOpaque(false);
				title = new JLabel(newTitle);
				title.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));		//	to stop subscript from truncating
				title.setOpaque(false);
				title.setFont(titleFont);
			topPanel.add("West", title);
				
				value = new JLabel("");
				value.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));		//	to match title
				value.setOpaque(false);
				value.setFont(titleFont);
				if (initialised) {
					Value val = translateValue(slider.getValue());
					if (val != null)
						value.setText(val.toString());
				}
			topPanel.add("Center", value);
			
			sliderPanel.add("North", topPanel);
		}
		else
			title.setText(newTitle);
	}
	
	public void setTitleFont(Font newTitleFont) {
		titleFont = newTitleFont;
		if (title != null)
			title.setFont(newTitleFont);
		if (value != null)
			value.setFont(newTitleFont);
	}
	
	public int getMinValue() {
		return slider.getMinimum();
	}
	
	public int getMaxValue() {
		return slider.getMaximum();
	}
	
	public void setMinValue(String minText, int minVal) {
		if (min != null && !fixedMinMaxText)
			min.setText(minText);
		slider.setMinimum(minVal);
	}
	
	public void setMaxValue(String maxText, int maxVal) {
		if (max != null && !fixedMinMaxText)
			max.setText(maxText);
		slider.setMaximum(maxVal);
	}
	
	public void setMinMaxValues(String minText, int minVal, String maxText, int maxVal) {
		setMinValue(minText, minVal);
		setMaxValue(maxText, maxVal);
		setMinValue(minText, minVal);		//	in case new min was above old max
		setTickSpacing(minVal, maxVal);
	}
	
	public void fixMinMaxText(String minText, String maxText) {
		fixedMinMaxText = true;
		if (min != null)
			min.setText(minText);
		if (max != null)
			max.setText(maxText);
	}
	
	public void show(boolean newVisible) {
		showHideLayout.show(this, newVisible ? "show" : "hide");
	}
	
	@SuppressWarnings("deprecation")
	public void stateChanged(ChangeEvent e) {
		int newVal = getValue();
		if (value != null) {
			Value v = translateValue(newVal);
			if (v != null)
				value.setText(v.toString());
		}
		repaint();										//	On Windows, the slider seems to leave a track
		if (postEvents)								//	Don't create an event if slider changed by animation
			deliverEvent(new Event(this, Event.ACTION_EVENT, null));
	}
	
	
	protected void DoMousePressed(MouseEvent e) {
	}
	
	protected void DoMouseReleased(MouseEvent e) {
	}
	
	protected void paintComponent(Graphics g) {
		if (!initialised) {
//			Color bg = getParent().getBackground();
//			lockBackground(bg);
			Color bg = getBackground();
			slider.setBackground(bg);		//	should not be needed, but is grey in Windows
			if (value != null) {
				Value val = translateValue(slider.getValue());
				if (val != null)
					value.setText(val.toString());
			}
			initialised = true;
		}
		super.paintComponent(g);
	}

}