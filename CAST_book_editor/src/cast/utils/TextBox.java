package cast.utils;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;


public class TextBox extends JTextArea {
	static final private Color kTextBoxColor = new Color(0x666666);
	static final private Color kTextColor = new Color(0x888888);
	static final private Color kTextBackground = new Color(0xEEEEFF);
	
	public TextBox(String s, int nLines) {
		super(s, nLines, 0);
		setLineWrap(true);
		setWrapStyleWord(true);
		setEditable(false);
		setFont(new Font("SansSerif", Font.ITALIC, 13));
		setForeground(kTextColor);
		setBackground(kTextBackground);
		
		Border blackline = BorderFactory.createLineBorder(kTextBoxColor);
		Border spacingBorder = BorderFactory.createEmptyBorder(2, 5, 2, 5);
		setBorder(BorderFactory.createCompoundBorder(blackline, spacingBorder));
		
		setTransferHandler(null);
		
		setEnabled(false);
	}
}
