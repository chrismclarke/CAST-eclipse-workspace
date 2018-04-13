package cast.bookManager;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import cast.server.*;
import cast.core.*;


public class BookListFrame extends JFrame {
	static final public Color kBackgroundColor = new Color(0x666666);
	static final public Color kButtonBackground = new Color(0x9999FF);
	
	private boolean translateOnly;
	
	private String[] bookName;
	private JPanel bookList;
	private JButton collectionsButton;
	
	public BookListFrame(String[] bookName, File coreDir, final JButton callingButton,
																									final AdvancedWindow callingFrame, boolean translateOnly) {
		super("List of CAST e-books");
		this.translateOnly = translateOnly;
		
		this.bookName = bookName;
		setLayout(new BorderLayout(0, 0));
		
		bookList = innerList(bookName, coreDir);
		JScrollPane scrollPane = new JScrollPane(bookList);
		scrollPane.setBorder(null);
		add("Center", scrollPane);
		
		if (!translateOnly) {
			add("South", creationPanel(coreDir));
//			add("North", utilitiesPanel(coreDir));
		}
		
		addWindowListener(new WindowAdapter() {
										public void windowClosing(WindowEvent e) {
											callingFrame.reenable(callingButton);
											dispose();
										}
									});
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private JPanel innerList(String[] bookName, File coreDir) {
		JPanel thePanel = new JPanel();
		thePanel.setBackground(kBackgroundColor);
		thePanel.setLayout(new GridLayout(0, 1, 3, 6));
		
		for (int i=0 ; i<bookName.length ; i++) {
			CastEbook theEbook = new CastEbook(coreDir, bookName[i], translateOnly);
			thePanel.add(new OneBook(theEbook));
		}
		
		return thePanel;
	}
	
	private JPanel creationPanel(final File coreDir) {
		JPanel thePanel = new JPanel() {
															public Insets getInsets() { return new Insets(9, 0, 6, 0); }
														};
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		thePanel.setBackground(kButtonBackground);
		
			JButton newButton = new JButton("New E-book...");
			newButton.addActionListener(new ActionListener() {
																			public void actionPerformed(ActionEvent e) {
																				String newName = BookCreator.createBook(BookListFrame.this, bookName, coreDir, false);
																				if (newName != null)
																					addBook(newName, coreDir);
																			}
																	});
		thePanel.add(newButton);
		
			JButton duplicateButton = new JButton("Duplicate E-book...");
			duplicateButton.addActionListener(new ActionListener() {
																			public void actionPerformed(ActionEvent e) {
																				String newName = BookCreator.createBook(BookListFrame.this, bookName, coreDir, true);
																				if (newName != null)
																					addBook(newName, coreDir);
																			}
																	});
		thePanel.add(duplicateButton);
		
		return thePanel;
	}
	
/*
	private JPanel utilitiesPanel(final File coreDir) {
		JPanel thePanel = new JPanel() {
															public Insets getInsets() { return new Insets(9, 0, 6, 0); }
														};
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		thePanel.setBackground(kButtonBackground);
		
			collectionsButton = new JButton("Collections...");
			collectionsButton.addActionListener(new ActionListener() {
																			public void actionPerformed(ActionEvent e) {
																				CollectionsFrame theWindow = new CollectionsFrame(coreDir, BookListFrame.this);
																				theWindow.pack();
																				theWindow.show();
																				theWindow.toFront();
																				collectionsButton.setEnabled(false);
																			}
																	});
		thePanel.add(collectionsButton);
		
		return thePanel;
	}
*/
	
	private void addBook(String newName, File coreDir) {
		CastEbook newEbook = new CastEbook(coreDir, newName, false);
		bookList.add(new OneBook(newEbook));
		
		String newBookName[] = new String[bookName.length + 1];
		System.arraycopy(bookName, 0, newBookName, 0, bookName.length);
		CoreDownloadTask.updateInstalledBook(newName, coreDir);
		newBookName[bookName.length] = newName;
		
		bookName = newBookName;
		
		layoutAgain();
		
		SwingUtilities.invokeLater(new Runnable(){
																	public void run(){
																		bookList.scrollRectToVisible(new Rectangle(0, bookList.getHeight() - 2, 1, 1));
																	}
																});
	}
	
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		
		Toolkit toolkit =  Toolkit.getDefaultToolkit();
		int screenHeight = toolkit.getScreenSize().height;
		
		d.height = Math.min(d.height, screenHeight - 50);
		d.width += 30;		//		so that horizontal scroll bars are not also shown when vertical scroll bars appear
		
		return d;
	}
	
	private void layoutAgain() {
		Component[] books = bookList.getComponents();
		for (int i=0 ; i<books.length ; i++)
			books[i].invalidate();
		bookList.revalidate();
		pack();
	}
	
	public void enableCollectionsButton() {
		collectionsButton.setEnabled(true);
	}
}
