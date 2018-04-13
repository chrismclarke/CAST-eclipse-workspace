package ebook;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import utils.*;
import ebookStructure.*;


public class TocChapter extends JPanel {
	private DomElement chapterElement;
	private BookFrame theWindow;
	
	private JLabel chapterTitle;
	private JLabel[] sectionTitle;
	
	public TocChapter(DomElement chapterElementParam, BookFrame theWindowParam, Font chFont, Font secFont) {
		this.chapterElement = chapterElementParam;
		this.theWindow = theWindowParam;
		
		setBackground(TocColumn.kTocBackground);
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		setBorder(new EmptyBorder(3, 10, 2, 10));
		
			chapterTitle = new JLabel(chapterElement.getIndex() + ". " + chapterElement.getName(), JLabel.LEFT);
			chapterTitle.setOpaque(true);
			chapterTitle.setBackground(TocColumn.kTocBackground);
			chapterTitle.addMouseListener(new MouseListener() {
								public void mouseReleased(MouseEvent e) {}
								public void mousePressed(MouseEvent e) {}
								public void mouseExited(MouseEvent e) {
									chapterTitle.setBackground(TocColumn.kTocBackground);
								}
								public void mouseEntered(MouseEvent e) {
									chapterTitle.setBackground(TocColumn.kMouseoverBackground);
								}
								public void mouseClicked(MouseEvent e) {
									theWindow.showPage(chapterElement, BookFrame.FROM_TOC);
								}
							});
			chapterTitle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			chapterTitle.setFont(chFont);
			
		add(chapterTitle);
		
		sectionTitle = new JLabel[chapterElement.noOfChildren()];
		for (int i=0 ; i<sectionTitle.length ; i++) {
			final DomElement sect = chapterElement.getChild(i);
			final JLabel sectTitle = new JLabel(sect.getIndex() + ". " + sect.getName(), JLabel.LEFT);
			sectTitle.setOpaque(true);
			sectTitle.setVisible(false);
			sectTitle.setBackground(TocColumn.kTocBackground);
			sectTitle.setBorder(new EmptyBorder(1, 15, 1, 0));
			sectTitle.addMouseListener(new MouseListener() {
								public void mouseReleased(MouseEvent e) {}
								public void mousePressed(MouseEvent e) {}
								public void mouseExited(MouseEvent e) {
									sectTitle.setBackground(TocColumn.kTocBackground);
								}
								public void mouseEntered(MouseEvent e) {
									sectTitle.setBackground(TocColumn.kMouseoverBackground);
								}
								public void mouseClicked(MouseEvent e) {
									theWindow.showPage(sect, BookFrame.FROM_TOC);
								}
							});
			sectTitle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			sectTitle.setFont(secFont);
			add(sectTitle);
			sectionTitle[i] = sectTitle;
		}
	}
	
	public void updateHighlight() {
		DomElement current = theWindow.currentElement;
		boolean chapterIsSelected = current.hasAncestor(chapterElement);
		chapterTitle.setForeground(chapterIsSelected ? TocColumn.kSelectedTextColor : TocColumn.kStdTextColor);
		if (chapterIsSelected)
			for (int i=0 ; i<sectionTitle.length ; i++) {
				sectionTitle[i].setVisible(true);
				boolean sectionIsSelected = current.hasAncestor(chapterElement.getChild(i));
				sectionTitle[i].setForeground(sectionIsSelected ? TocColumn.kSelectedTextColor : TocColumn.kStdTextColor);
			}
		else
			for (int i=0 ; i<sectionTitle.length ; i++)
				sectionTitle[i].setVisible(false);
	}
}