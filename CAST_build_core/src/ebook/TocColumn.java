package ebook;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import utils.*;
import ebookStructure.*;


public class TocColumn extends JPanel {
	static final public Color kTocBackground = new Color(0xEEEEEE);
	static final public Color kStdTextColor = new Color(0x666666);
	static final public Color kSelectedTextColor = new Color(0xE01800);
	static final public Color kMouseoverBackground = new Color(0xE4E4E4);
	static final public Color kIndexColor = new Color(0x006600);
	
	static final private Font chFont = new Font("Arial", Font.BOLD, 15);
	static final private Font secFont = new Font("Arial", Font.PLAIN, 12);
	
	private CastEbook theEbook;
	private BookFrame theWindow;
	
	private JLabel bookTitle;
	private TocChapter[] tocChapters;
	
	public TocColumn(CastEbook theEbookParam, BookFrame theWindowParam) {
		this.theEbook = theEbookParam;
		this.theWindow = theWindowParam;
		final DomElement bookElement = theEbook.getDomBook();
		
		setBackground(kTocBackground);
		setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
		setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, CoreBannerTitles.kBorderColor));
		
			bookTitle = new JLabel(bookElement.getName(), JLabel.LEFT);
			bookTitle.setOpaque(true);
			bookTitle.setBackground(kTocBackground);
			bookTitle.setBorder(BorderFactory.createEmptyBorder(8, 20, 6, 10));
			bookTitle.addMouseListener(new MouseListener() {
								public void mouseReleased(MouseEvent e) {}
								public void mousePressed(MouseEvent e) {}
								public void mouseExited(MouseEvent e) {
									bookTitle.setBackground(kTocBackground);
								}
								public void mouseEntered(MouseEvent e) {
									bookTitle.setBackground(kMouseoverBackground);
								}
								public void mouseClicked(MouseEvent e) {
									theWindow.showPage(bookElement, BookFrame.FROM_TOC);
								}
							});
//			bookTitle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			bookTitle.setFont(chFont);
			
		add(bookTitle);
		
		int nElements = bookElement.noOfChildren();
		int nChapters = 0;
		for (int i=0 ; i<nElements ; i++)
			if (bookElement.getChild(i) instanceof DomChapter)
				nChapters ++;
		
		tocChapters = new TocChapter[nChapters];
		int chIndex = 0;
		for (int i=0 ; i<nElements ; i++) {
			DomElement ch = bookElement.getChild(i);
			if (ch instanceof DomChapter) {
				tocChapters[chIndex] = new TocChapter(ch, theWindow, chFont, secFont);
				add(tocChapters[chIndex]);
				chIndex ++;
			}
			else
				add(new TocPart(ch, theWindow, chFont));
		}
		
			JPanel linksPanel = new JPanel();
			linksPanel.setLayout(new VerticalLayout(VerticalLayout.FILL));
			linksPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 6, 0));
			
			linksPanel.add(indexLink(theWindow.translate("Index"), true));
			linksPanel.add(indexLink(theWindow.translate("Datasets"), false));
		add(linksPanel);
		
		String versionImage = theEbook.getLogoGif();
		if (versionImage != null) {
			File bookDir = theEbook.getBookDir();
			File imgFile = new File(bookDir, "images/" + versionImage);
			UiImage theImage = new UiImage(imgFile, false);
			theImage.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
			add(theImage);
		}
		
		updateHighlight();
	}
	
	private JLabel indexLink(String indexName, final boolean indexNotDatasets) {
		final JLabel indexLabel = new JLabel(indexName, JLabel.LEFT);
		indexLabel.setOpaque(true);
		indexLabel.setBackground(kTocBackground);
		indexLabel.setForeground(kIndexColor);
		indexLabel.setBorder(BorderFactory.createEmptyBorder(1, 20, 1, 10));
		indexLabel.addMouseListener(new MouseListener() {
							public void mouseReleased(MouseEvent e) {}
							public void mousePressed(MouseEvent e) {}
							public void mouseExited(MouseEvent e) {
								indexLabel.setBackground(kTocBackground);
							}
							public void mouseEntered(MouseEvent e) {
								indexLabel.setBackground(kMouseoverBackground);
							}
							public void mouseClicked(MouseEvent e) {
								if (indexNotDatasets)
									theWindow.showIndexWindow();
								else
									theWindow.showDatasetWindow();
							}
						});
//		indexLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		indexLabel.setFont(chFont);
		return indexLabel;
	}
	
	public void updateHighlight() {
		DomElement current = theWindow.currentElement;
		boolean bookIsSelected = current == theEbook.getDomBook();;
		bookTitle.setForeground(bookIsSelected ? kSelectedTextColor : kStdTextColor);
		
		for (int i=0 ; i<tocChapters.length ; i++)
			tocChapters[i].updateHighlight();
	}
}