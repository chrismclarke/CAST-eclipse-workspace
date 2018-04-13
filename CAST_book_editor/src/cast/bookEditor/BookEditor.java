package cast.bookEditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.pageEditor.*;
import cast.server.*;
import cast.utils.*;


public class BookEditor extends JFrame {
	static final public Color kHeadingBackground = new Color(0xDDDDFF);
	
	static final private int kHorizOffset = 30;
	static final private int kVertOffset = 30;
	
	static public void offsetFrameFromParent(JFrame frame, Component callPanel) {
		while (!(callPanel instanceof JFrame))
			callPanel = callPanel.getParent();
		Point parentTopLeft = callPanel.getLocation();
		frame.setLocation(parentTopLeft.x + kHorizOffset, parentTopLeft.y + kVertOffset);
	}
	
	private CastEbook castEbook;
	private BookContents bookContents;
	
	public BookEditor(final CastEbook castEbook, final OneBook bookFrame) {
		super("Edit " + castEbook.getShortBookName());
		setBackground(Color.white);
		
		setPreferredSize(new Dimension(600, 800));
		
		offsetFrameFromParent(this, bookFrame);
		
		this.castEbook = castEbook;
		castEbook.setupDom();
		String bookName = castEbook.getLongBookName();
		
		validateFiles(castEbook);
		
		setLayout(new BorderLayout(0, 0));
		
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout(0, 0));
			topPanel.setBackground(kHeadingBackground);
			
				final JLabel bookNameLabel = new JLabel(bookName, JLabel.CENTER);
				bookNameLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
			topPanel.add("Center", bookNameLabel);
			
			if (castEbook.canEditBook() && castEbook.canChangeStructure()) {
				JPanel editPanel = new JPanel() {
																						public Insets getInsets() {
																							return new Insets(10, 3, 3, 0);
																						}
																			};
				editPanel.setOpaque(false);
				editPanel.setLayout(new BorderLayout(10, 0));
				
					editPanel.add("Center", new TextBox("Drag chapters and sections to rearrange or copy them from other e-books. "
																+ "Drag the \"New\" items at the bottom to add them to the e-book. "
																+ "Right-click to delete or edit items.", 3));
					
						final JPanel buttonPanel = new JPanel();
						buttonPanel.setOpaque(false);
						buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
						
							JButton settingsButton = new JButton("Settings");
							settingsButton.addActionListener(new ActionListener() {
																						public void actionPerformed(ActionEvent e) {
																							BookSettings.editSettings(BookEditor.this, castEbook);
																							bookNameLabel.setText(castEbook.getLongBookName());
																							buttonPanel.revalidate();
																							buttonPanel.repaint();
																						}
																				});
						buttonPanel.add(settingsButton);
					
					editPanel.add("East", buttonPanel);
					
//					editPanel.add("South", new NewPanel());
				
				topPanel.add("South", editPanel);
			}
			else if (castEbook.canEditBook() && castEbook.canOnlyTranslate() && !"en".equals(castEbook.getLanguage())) {
				JPanel termsPanel = new JPanel();
				termsPanel.setOpaque(false);
				termsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
				
					JLabel translateLabel = new JLabel("Translate...", JLabel.LEFT);
					translateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
				termsPanel.add(translateLabel);
					
					JButton termsButton = new JButton("Terms");
					termsButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					LanguageTerms.editTerms(BookEditor.this, castEbook);
																				}
																		});
				termsPanel.add(termsButton);
					
					final JButton pagesButton = new JButton("Pages");
					pagesButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					File[] chapterFiles = getChapterFiles();
																					new PageEditFrame(chapterFiles, pagesButton);
																					pagesButton.setEnabled(false);
																				}
																		});
				termsPanel.add(pagesButton);
				
				topPanel.add("South", termsPanel);
			}
				
		add("North", topPanel);
		
		bookContents = new BookContents(castEbook);
		JScrollPane scrollPane = new JScrollPane(bookContents);
		scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.gray));
		add("Center", scrollPane);
		
		addWindowListener( new WindowAdapter() {
									public void windowOpened( WindowEvent e ){
										bookContents.requestFocus();
									}
	
									public void windowClosing(WindowEvent e) {
										if (castEbook.domHasChanged()) {
											Object[] options = {"Save", "Don't save", "Cancel"};
											int result = JOptionPane.showOptionDialog(BookEditor.this, "Save changes to book?", "Save?",
																	JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[2]);

											switch (result) {
												case JOptionPane.YES_OPTION:
													castEbook.saveDom();
																//	then dispose()
												case JOptionPane.NO_OPTION:
													dispose();
													break;
												case JOptionPane.CANCEL_OPTION:
												default:
													break;
											}
										}
										else
											dispose();
									}
	
									public void windowClosed(WindowEvent e) {
										bookFrame.enableEditButton();
										castEbook.clearDom();
									}
								} ); 
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		if (castEbook.canEditBook() && castEbook.canChangeStructure())
			add("South", new NewPanel());
	}
	
	private void validateFiles(CastEbook castEbook) {
		DomBook bookDom = castEbook.getDomBook();
		boolean hasSummaries = bookDom.hasSummaries();
		
		String problems = "";
		int nChildren = bookDom.noOfChildren();
		for (int i=0 ; i<nChildren ; i++)
			problems += problemsInElement(bookDom.getChild(i), castEbook, hasSummaries);
		
		if (problems.length() == 0)
			System.out.println("Book validated");
		else
			System.out.println("Some book files cannot be found:\n" + problems);
	}
	
	private String problemsInElement(DomElement e, CastEbook castEbook, boolean hasSummaries) {
		String problems = "";
		if (e instanceof DomChapter) {
			String dir = ((DomChapter)e).getDir();
			String filePrefix = ((DomChapter)e).getFilePrefix();
			File f = castEbook.getPageHtmlFile(dir, filePrefix);
			if (!f.exists())
				problems = "chapter(" + dir + ", " + filePrefix + ".html)";
			
			int nChildren = e.noOfChildren();
			for (int i=0 ; i<nChildren ; i++) {
				String subProblem = problemsInElement(e.getChild(i), castEbook, hasSummaries);
				if (subProblem.length() > 0)
					problems += "\n" + subProblem;
			}
		}
		else if (e instanceof DomSection) {
			String dir = ((DomSection)e).getDir();
			String filePrefix = ((DomSection)e).getFilePrefix();
			File f = castEbook.getXmlFile(dir, filePrefix);
			if (!f.exists())
				problems = "section(" + dir + "/xml, " + filePrefix + ".xml)";
			else {
				CastSection castSection = new CastSection(dir, filePrefix, castEbook);
				Dom2Section sectionDom = castSection.getDomSection();
				int nChildren = sectionDom.noOfChildren();
				for (int i=0 ; i<nChildren ; i++) {
					Dom2Page pageDom = sectionDom.getChild(i);
					String pageDir = pageDom.getDirFromXml();
					String pageFilePrefix = pageDom.getFilePrefixFromXml();
					File fp = castEbook.getPageHtmlFile(pageDir, pageFilePrefix);
					if (!fp.exists())
						problems += "\npage(" + pageDir + ", " + pageFilePrefix + ".html)";
				}
			}
		}
		else if (e instanceof DomPage) {
			String dir = ((DomPage)e).getDir();
			String filePrefix = ((DomPage)e).getFilePrefix();
			File f = castEbook.getPageHtmlFile(dir, filePrefix);
			if (!f.exists())
				problems = "page(" + dir + ", " + filePrefix + ".html)";
		}
		return problems;
	}
	
	private File[] getChapterFiles() {
		ArrayList<File> theFiles = new ArrayList<File>();
		File bookDir = castEbook.getBookDir();
		File bookSplash = new File(bookDir, "book_splash.html");
		theFiles.add(bookSplash);
		
		
		DomBook domBook = castEbook.getDomBook();
		int nChildren = domBook.noOfChildren();
		
		for (int i=0 ; i<nChildren ; i++) {
			DomElement child = domBook.getChild(i);
			if (child instanceof DomChapter) {
				DomChapter ch = (DomChapter)child;
				String dir = ch.getDir();
				String filePrefix = ch.getFilePrefix();
				File page = castEbook.getPageHtmlFile(dir, filePrefix);
				
				theFiles.add(page);
			}
		}
		
		File sectionsDir = new File(castEbook.getCoreDir(), castEbook.getLanguage());		//	assumes language is not null (English)
		theFiles.add(new File(sectionsDir, DatesHash.kSystemAdviceFileName));
		
		File[] fileArray = new File[theFiles.size()];
		for (int i=0 ; i<fileArray.length ; i++)
			fileArray[i] = theFiles.get(i);
		return fileArray;
	}
	
}
