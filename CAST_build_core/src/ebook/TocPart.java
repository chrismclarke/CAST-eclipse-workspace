package ebook;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import utils.*;
import ebookStructure.*;


public class TocPart extends JPanel {
	static final private Color kPartColor = new Color(0x000033);
	
	public TocPart(DomElement partElementParam, BookFrame theWindowParam, Font chFont) {
		setBackground(TocColumn.kTocBackground);
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		setBorder(new EmptyBorder(10, 10, 2, 10));
		
			JLabel partTitle = new JLabel(partElementParam.getName(), JLabel.LEFT);
			partTitle.setOpaque(true);
			partTitle.setBackground(TocColumn.kTocBackground);
			partTitle.setFont(chFont);
			partTitle.setForeground(kPartColor);
			
		add(partTitle);
	}
}