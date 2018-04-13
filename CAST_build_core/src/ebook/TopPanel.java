package ebook;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import ebookStructure.*;


public class TopPanel extends JLayeredPane {
	private CastEbook theEbook;
//	private BookFrame theWindow;
	
	private CoreBannerTitles bannerTitles;
	private BannerNavigation bannerNavigation;
	private TabPanel tabPanel;
	
	private boolean initialised = false;
	
	public TopPanel(CastEbook theEbookParam, BookFrame theWindowParam) {
		this.theEbook = theEbookParam;
//		this.theWindow = theWindowParam;

		bannerTitles = theEbook.isModule() ? new ModuleBannerTitles(theWindowParam)
																				: new BookBannerTitles(theWindowParam);
		add(bannerTitles, Integer.valueOf(0), 0);
		
		bannerNavigation = new BannerNavigation(theWindowParam, theEbookParam);
		add(bannerNavigation, Integer.valueOf(1), 0);
		
		tabPanel = new TabPanel(theWindowParam);
		add(tabPanel, Integer.valueOf(2), 0);
		
		addComponentListener(new ComponentAdapter() {
										public void componentResized(ComponentEvent e) {
											bannerTitles.setBounds(0, 0, getSize().width, getSize().height);
											bannerNavigation.setBounds(0, 0, getSize().width, getSize().height);
											tabPanel.setBounds(0, 0, getSize().width, getSize().height);
										}
										public void componentShown(ComponentEvent e) {
											componentResized(e);
										}
								});
	}
	
	public Dimension getMinimumSize() {
		Dimension titleMin = bannerTitles.getMinimumSize();
		Dimension navMin = bannerNavigation.getMinimumSize();
		return new Dimension(Math.max(titleMin.width, navMin.width), Math.max(titleMin.height, navMin.height));
	}
	
	public Dimension getPreferredSize() {
		Dimension titlePref = bannerTitles.getPreferredSize();
		Dimension navPref = bannerNavigation.getPreferredSize();
		return new Dimension(Math.max(titlePref.width, navPref.width), Math.max(titlePref.height, navPref.height));
	}
	
/*
	public CoreBannerTitles getBannerTitles() {
		return bannerTitles;
	}
*/
	
	public BannerNavigation getBannerNavigation() {
		return bannerNavigation;
	}
	
	public TabPanel getTabPanel() {
		return tabPanel;
	}
	
	public void updateContents(int sourceOfCommand) {
		if (sourceOfCommand != BookFrame.FROM_TAB) {
			bannerTitles.updateTitles();
			bannerNavigation.updateArrows();
		}
		
		tabPanel.updateTabs();
		tabPanel.revalidate();
	}
	
	public void paintComponent(Graphics g) {
		if (!initialised) {
			bannerTitles.setBounds(0, 0, getSize().width, getSize().height);
			bannerNavigation.setBounds(0, 0, getSize().width, getSize().height);
			tabPanel.setBounds(0, 0, getSize().width, getSize().height);
			initialised = true;
		}
		super.paintComponent(g);
	}
}