package utils;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import dataView.*;
//import exercise2.*;

public class XChoice extends XPanel implements ActionListener, StatusInterface {
	static final public int HORIZONTAL = 0;
	static final public int VERTICAL_LEFT = 1;
	static final public int VERTICAL_CENTER = 2;
	
	private JComboBox theChoice;
	private JLabel label = null;
	private JLabel units = null;
	
	public XChoice(XApplet applet) {
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		setOpaque(false);
		setFont(applet.getStandardBoldFont());
		theChoice = new JComboBox();
		theChoice.addActionListener(this);
		theChoice.setLightWeightPopupEnabled(false);
		add(theChoice);
		theChoice.setFont(applet.getStandardBoldFont());
		theChoice.setOpaque(false);
		theChoice.setMaximumRowCount(99);				//		never use scroll bars in menu
	}
	
	public XChoice(String labelString, int orientation, XApplet applet) {
		this(labelString, null, orientation, applet);
	}
	
	public XChoice(String labelString, String unitsString, int orientation, XApplet applet) {
		if (orientation == HORIZONTAL)
			setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		else if (orientation == VERTICAL_LEFT)
			setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
		else
			setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
//		setOpaque(false);
		setFont(applet.getStandardBoldFont());
		
		if (labelString != null) {
			label = new JLabel(labelString, JLabel.LEFT);
			if (orientation == VERTICAL_LEFT) {
				Border leftBorder = BorderFactory.createEmptyBorder(0, 10, 0, 0);
				label.setBorder(leftBorder);
			}
			label.setFont(applet.getStandardBoldFont());
			label.setLabelFor(theChoice);
			add(label);
		}
		
		theChoice = new JComboBox();
		theChoice.addActionListener(this);
		add(theChoice);
		theChoice.setFont(applet.getStandardBoldFont());
		theChoice.setOpaque(false);
		theChoice.setMaximumRowCount(99);				//		never use scroll bars in menu
		
		if (unitsString != null) {
			units = new JLabel(unitsString, JLabel.LEFT);
			if (orientation == VERTICAL_LEFT) {
				Border leftBorder = BorderFactory.createEmptyBorder(0, 10, 0, 0);
				units.setBorder(leftBorder);
			}
			
			units.setFont(applet.getStandardBoldFont());
			units.setLabelFor(theChoice);
			add(units);
		}
	}
	
	public void changeLabel(String labelString) {
		if (label == null)
			throw new RuntimeException("Cannot change label string if XChoice is created without one.");
		label.setText(labelString);
		label.invalidate();
	}
	
	public void setBackground(Color c) {
		setOpaque(true);
		super.setBackground(c);
	}
	
	public String getStatus() {
		return String.valueOf(getSelectedIndex());
	}
	
	public void setStatus(String status) {
		theChoice.setSelectedIndex(Integer.parseInt(status));		//	fires actionPerformed event
	}
	
//	public Insets insets() {
//		return new Insets(2, 2, 2, 2);
//	}

	public int countItems() {
		return theChoice.getModel().getSize();
	}
	
	public String getItem(int index) {
		return (String)theChoice.getModel().getElementAt(index);
	}
	
	public void add(String item) {
		addItem(item);
	}
	
	public void addItem(String item) {
		theChoice.removeActionListener(this);		//	don't want events when items are cleared and re-entered
		theChoice.addItem(item);
		theChoice.addActionListener(this);
	}
	
	public void insert(String item, int index) {
		theChoice.insertItemAt(item, index);
	}
	
	public void remove(String item) {
		theChoice.removeItem(item);
	}
	
	public void remove(int position) {
    theChoice.removeItemAt(position);
	}
	
	public void changeItem(int position, String newText) {
		int currentChoice = theChoice.getSelectedIndex();
		theChoice.removeItemAt(position);
		theChoice.insertItemAt(newText, position);
		theChoice.setSelectedIndex(currentChoice);
	}
	
	public void clearItems() {
		theChoice.removeActionListener(this);	//	don't want events when items are cleared and re-entered
		theChoice.removeAllItems();
		theChoice.addActionListener(this);
	}
	
	public String getSelectedItem() {
		return (String)theChoice.getSelectedItem();
	}
	
	public Object[] getSelectedObjects() {
		return theChoice.getSelectedObjects();
	}
	
	public int getSelectedIndex() {
		return theChoice.getSelectedIndex();
	}
	
	public void select(int pos) {
		theChoice.removeActionListener(this);	//	don't want events when item is manually selected
		theChoice.setSelectedIndex(pos);
		theChoice.addActionListener(this);
	}
	
	public void select(String str) {
		theChoice.setSelectedItem(str);
	}
	
//	public void addItemListener(ItemListener l) {
//		theChoice.addItemListener(l);
//	}
//	
//	public void removeItemListener(ItemListener l) {
//		theChoice.removeItemListener(l);
//	}
	
	public void enable() {
		setEnabled(true);
	}
	
	public void disable() {
		setEnabled(false);
	}
	
	public void setEnabled(boolean enabled) {
		theChoice.setEnabled(enabled);
		if (label != null)
			label.setForeground(enabled ? Color.black : Color.gray);
		if (units != null)
			units.setForeground(enabled ? Color.black : Color.gray);
	}
	
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
		postEvent(new Event(this, Event.ACTION_EVENT, getSelectedItem()));
//		System.out.println("Option selected " + e.getActionCommand());
	}
}