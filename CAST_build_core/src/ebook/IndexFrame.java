package ebook;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;

import utils.*;
import ebookStructure.*;


public class IndexFrame extends JFrame {
	static final private String kPagePattern = "a(C|S|P)\\((.*?)\\);";
	static final private String kSectionInfoPattern = "'(.*)'";
	static final private String kPageInfoPattern = "'(.*?)','(.*?)','(.*?)'";
	
	static final private String kTermPattern = "aT\\('(.*?)',(.*?)\\);\\s*(aSrc\\(\"(.*?)\"\\);)?";
	
	static final private Color kStdTermColor = new Color(0x000099);
	static final private Color kMouseoverTermColor = Color.red;
	static final private Font kTermFont = new Font("Sans serif", Font.PLAIN, 14);
	static final private Font kSelectedTermNameFont = new Font("Sans serif", Font.BOLD, 18);
	
	static final private Color kHeadingBackground = new Color(0xBBBBBB);
	static final private Color kHeadingSpacingColor = new Color(0x999999);
	static final private Font kHeadingFont = new Font("Times New Roman", Font.BOLD, 24);
	
	static final private Font kSectionNameFont = new Font("Sans serif", Font.BOLD, 14);
	
	static final private int kInitialWindowWidth = 800;
	static final private int kInitialWindowHeight = 500;
	
	static final public boolean INDEX = true;
	static final public boolean DATASETS = false;
	
	private BookFrame theBookWindow;
	private boolean indexNotDatasets;
	
	private JLabel selectedTermLabel;
	private JScrollPane referenceScrollPane;
	private JPanel referenceScrollingPanel;
	private JLabel theSource;
	
	class PageClass {
		String name, dir, filePrefix;
		PageClass parent;
		
		PageClass(String name, String dir, String filePrefix, PageClass parent) {
			this.name = name;
			this.dir = dir;
			this.filePrefix = filePrefix;
			this.parent = parent;
		}
	}
	
	class TermClass {
		String name, refIndices;
		String source;
		
		TermClass(String name, String refIndices, String source) {
			this.name = name;
			this.refIndices = refIndices;
			this.source = source;
		}
	}
	
	private PageClass[] thePages;
	private TermClass[] theTerms;
	
	
	public IndexFrame(BookFrame theBookWindow, boolean indexNotDatasets) {
		this.theBookWindow = theBookWindow;
		this.indexNotDatasets = indexNotDatasets;
		
		setupTerms();
		
		setLayout(new BorderLayout(0, 0));
		JPanel theTermPanel = termPanel(theBookWindow.translate(indexNotDatasets ? "Index entries" : "Datasets"));
		theTermPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, kHeadingSpacingColor));
		add("West", theTermPanel);
		add("Center", referencesPanel(theBookWindow.translate("References"), theBookWindow.translate(indexNotDatasets ? "Term" : "Data")));
		
		if (!indexNotDatasets)
			add("South", sourcePanel(theBookWindow.translate("Source")));
		
		setResizable(true);
		setSize(kInitialWindowWidth, kInitialWindowHeight);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}
	
	private void setupTerms() {
		CastEbook theEbook = theBookWindow.getEbook();
		File bookDir = theEbook.getBookDir();
		File indexFile = new File(bookDir, indexNotDatasets ? "book_index.html" : "book_dataSets.html");
		
		String html = HtmlHelper.getFileAsString(indexFile);
		html = html.replaceAll("#.#", "");
		
		Pattern pagePattern = Pattern.compile(kPagePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher pageMatcher = pagePattern.matcher(html);
		int nPages = 0;
		while (pageMatcher.find())
			nPages ++;
		
		thePages = new PageClass[nPages];
		
		PageClass currentSection = null;
		pageMatcher = pagePattern.matcher(html);
		int i = 0;
		while (pageMatcher.find()) {
			char pageType = pageMatcher.group(1).charAt(0);
			String pageInfo = XmlHelper.decodeHtml(pageMatcher.group(2), false);		//	decodes HTML entities like &amp;
			if (pageInfo != null)
				pageInfo = pageInfo.replaceAll("\\\\u03c3", "\u03c3");		//	decodes sigma
			if (pageType == 'C')
				thePages[i++] = new PageClass(null, null, null, null);
			else if (pageType == 'S') {
				Pattern sectionInfoPattern = Pattern.compile(kSectionInfoPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher sectionInfoMatcher = sectionInfoPattern.matcher(pageInfo);
				sectionInfoMatcher.find();
				String sectionName = sectionInfoMatcher.group(1);
				currentSection = new PageClass(sectionName, null, null, null);
				thePages[i++] = currentSection;
			}
			else if (pageType == 'P') {
				Pattern pageInfoPattern = Pattern.compile(kPageInfoPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher pageInfoMatcher = pageInfoPattern.matcher(pageInfo);
				pageInfoMatcher.find();
				String pageName = pageInfoMatcher.group(1);
				pageName = pageName.replaceAll("\\\\'", "'");
				String pageDir = pageInfoMatcher.group(2);
				String pageFilePrefix = pageInfoMatcher.group(3);
				thePages[i++] = new PageClass(pageName, pageDir, pageFilePrefix, currentSection);
			}
		}
		
		Pattern termPattern = Pattern.compile(kTermPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher termMatcher = termPattern.matcher(html);
		int nTerms = 0;
		while (termMatcher.find())
			nTerms ++;
		
		theTerms = new TermClass[nTerms];
		
		termMatcher = termPattern.matcher(html);
		i = 0;
		while (termMatcher.find()) {
			String termName = termMatcher.group(1);
			if (indexNotDatasets)
				termName = theBookWindow.translate(termName);
			String termIndices = termMatcher.group(2);
			String source = null;
			if (termMatcher.group(3) != null) {
				source = termMatcher.group(4);
			}
			theTerms[i++] = new TermClass(termName, termIndices, source);
		}
	}
	
	private JPanel headingPanel(String heading, boolean withTopLine) {
		JPanel thePanel = new JPanel();
		thePanel.setBackground(kHeadingBackground);
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			Border outerBorder = BorderFactory.createMatteBorder((withTopLine ? 1 : 0), 0, 1, 0, kHeadingSpacingColor);
			Border innerBorder = BorderFactory.createEmptyBorder(1, 10, 3, 0);
		thePanel.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
		
			JLabel headingLabel = new JLabel(heading, JLabel.LEFT);
			headingLabel.setFont(kHeadingFont);
		thePanel.add(headingLabel);
		return thePanel;
	}
	
	private JPanel termPanel(String heading) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("North", headingPanel(heading, false));
		
			JScrollPane theScrollPane = new JScrollPane();
			theScrollPane.setBackground(Color.white);
			theScrollPane.setBorder(BorderFactory.createEmptyBorder());
			
				JPanel scrollingPanel = new JPanel();
				scrollingPanel.setOpaque(false);
				scrollingPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 0));
				scrollingPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 5));
				for (int i=0 ; i<theTerms.length ; i++)
					scrollingPanel.add(createTermLabel(theTerms[i]));
					
			theScrollPane.setViewportView(scrollingPanel);
			theScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		thePanel.add("Center", theScrollPane);
		return thePanel;
	}
	
	private JLabel createTermLabel(final TermClass theTerm) {
		final JLabel termLabel = new JLabel(theTerm.name, JLabel.LEFT);
		termLabel.addMouseListener(new MouseListener() {
							public void mouseReleased(MouseEvent e) {}
							public void mousePressed(MouseEvent e) {}
							public void mouseExited(MouseEvent e) {
								termLabel.setForeground(kStdTermColor);
							}
							public void mouseEntered(MouseEvent e) {
								termLabel.setForeground(kMouseoverTermColor);
							}
							public void mouseClicked(MouseEvent e) {
								showReferences(theTerm);
							}
						});
		termLabel.setFont(kTermFont);
		termLabel.setForeground(kStdTermColor);
		termLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return termLabel;
	}
	
//*****************************************

	
	private JPanel referencesPanel(String heading, String termLabel) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
			topPanel.add(headingPanel(heading, false));
			
				JPanel termNamePanel = new JPanel();
				termNamePanel.setBackground(Color.white);
				termNamePanel.setLayout(new BorderLayout(10, 0));
					Border outerBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, kHeadingSpacingColor);
					Border innerBorder = BorderFactory.createEmptyBorder(3, 10, 3, 5);
				termNamePanel.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
					JLabel termNameLabel = new JLabel(termLabel + ":", JLabel.LEFT);
					termNameLabel.setFont(kSelectedTermNameFont);
					termNameLabel.setForeground(kMouseoverTermColor);
				termNamePanel.add("West", termNameLabel);
					selectedTermLabel = new JLabel("", JLabel.LEFT);
					selectedTermLabel.setFont(kSelectedTermNameFont);
					selectedTermLabel.setForeground(kMouseoverTermColor);
				termNamePanel.add("Center", selectedTermLabel);
			topPanel.add(termNamePanel);
				
		thePanel.add("North", topPanel);
		
			
			referenceScrollPane = new JScrollPane();
			referenceScrollPane.setBackground(Color.white);
			referenceScrollPane.setBorder(BorderFactory.createEmptyBorder());
			
				referenceScrollingPanel = new JPanel();
				referenceScrollingPanel.setBackground(Color.white);
				referenceScrollingPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 0));
				referenceScrollingPanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 5));
					
			referenceScrollPane.setViewportView(referenceScrollingPanel);
		
		thePanel.add("Center", referenceScrollPane);
		return thePanel;
	}
	
	private void showReferences(TermClass theTerm) {
		selectedTermLabel.setText(theTerm.name);
		
		referenceScrollingPanel.removeAll();
		
		PageClass currentSection = null;
		
		StringTokenizer st = new StringTokenizer(theTerm.refIndices, ",");
		while (st.hasMoreTokens()) {
			int refIndex = Integer.parseInt(st.nextToken());
			PageClass thePage = thePages[refIndex];
			if (thePage.parent != currentSection) {
				currentSection = thePage.parent;
				JLabel sectionLabel = new JLabel(currentSection.name, JLabel.LEFT);
				sectionLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 2, 0));
				sectionLabel.setFont(kSectionNameFont);
				sectionLabel.setForeground(Color.black);
				referenceScrollingPanel.add(sectionLabel);
			}
			referenceScrollingPanel.add(createReferenceLabel(thePage));
		}
		referenceScrollingPanel.repaint();
		
		if (!indexNotDatasets) {
			String sourceText = (theTerm.source == null) ? "" : ("<html>" + theTerm.source + "</html>");
			theSource.setText(sourceText);
		}
	}
	
	private JLabel createReferenceLabel(final PageClass thePage) {
		final JLabel referenceLabel = new JLabel(thePage.name, JLabel.LEFT);
		referenceLabel.addMouseListener(new MouseListener() {
							public void mouseReleased(MouseEvent e) {}
							public void mousePressed(MouseEvent e) {}
							public void mouseExited(MouseEvent e) {
								referenceLabel.setForeground(kStdTermColor);
							}
							public void mouseEntered(MouseEvent e) {
								referenceLabel.setForeground(kMouseoverTermColor);
							}
							public void mouseClicked(MouseEvent e) {
								showPage(thePage);
							}
						});
		referenceLabel.setForeground(kStdTermColor);
		referenceLabel.setBorder(BorderFactory.createEmptyBorder(1, 15, 1, 0));
		return referenceLabel;
	}
	
	private void showPage(PageClass thePage) {
		theBookWindow.showNamedPage(thePage.filePrefix);
	}
	
//*****************************************

	
	private JPanel sourcePanel(String heading) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("North", headingPanel(heading, true));
	
			JPanel sourcePanel = new JPanel() {
										public Dimension getPreferredSize() {
											return new Dimension(0, 50);
										}
									};
			sourcePanel.setBackground(Color.white);
			sourcePanel.setLayout(new BorderLayout(0, 0));
			sourcePanel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 5));
			
				theSource = new JLabel("", JLabel.LEFT);
			sourcePanel.add(theSource);
			
		thePanel.add("Center", sourcePanel);
			
		return thePanel;
	}
}