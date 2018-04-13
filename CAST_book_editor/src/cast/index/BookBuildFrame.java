package cast.index;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import cast.bookEditor.*;
import cast.bookManager.*;
import cast.utils.*;


public class BookBuildFrame extends JFrame {
	static final public Color kBackgroundColor = new Color(0xddddee);
	static final public Color kHeadingBackground = new Color(0x221166);
	
	private CoreBookProcessor buildMaker, printMaker;
	
	private Button doBuild;
	
	public BookBuildFrame(CastEbook castEbook, final OneBook bookFrame) {
		super("Build " + castEbook.getShortBookName());
		
		BookReader theBook = new BookReader(castEbook);
		theBook.setupBook();
		
		setLayout(new BorderLayout(0, 5));
		setBackground(kBackgroundColor);
		BookEditor.offsetFrameFromParent(this, bookFrame);
			
			JPanel titlePanel = new JPanel() {public Insets getInsets() {return new Insets(6, 10, 6, 10);}};
			titlePanel.setBackground(kHeadingBackground);
//			titlePanel.setOpaque(false);
			titlePanel.setLayout(new BorderLayout(0, 0));
		
				JLabel versionName = new JLabel(theBook.getLongBookName(), JLabel.CENTER);
				versionName.setOpaque(false);
				versionName.setFont(new Font("SansSerif", Font.BOLD, 18));
				versionName.setForeground(Color.white);
			titlePanel.add("Center", versionName);
		
		add("North", titlePanel);
		
			JPanel mainPanel = new JPanel() {public Insets getInsets() {return new Insets(0, 0, 20, 0);}};
			mainPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 24, 0));
			
			mainPanel.add(buildPanel(theBook, castEbook));
			if (!theBook.isLecturingVersion() && !theBook.isModule())
				mainPanel.add(printBookPanel(theBook, castEbook));
		
		add("Center", mainPanel);
		
			Panel buttonPanel = new Panel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
			doBuild = new Button("Build e-book");
			buttonPanel.add(doBuild);
		add("South", buttonPanel);

		
		addWindowListener( new WindowAdapter() {
									public void windowClosed(WindowEvent e) {
										bookFrame.enableBuildButton();
									}
								} );
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (evt.target == doBuild) {
			buildMaker.doIndex();
			if (printMaker != null)
				printMaker.doIndex();
			return true;
		}
		return false;
	}
	
	private JPanel buildPanel(BookReader theBook, CastEbook castEbook) {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 12));
		
			JLabel title = new JLabel("E-book structure", JLabel.CENTER);
			title.setFont(new Font("SansSerif", Font.BOLD, 16));
		thePanel.add(title);
			
			JPanel instructionPanel = new JPanel() {public Insets getInsets() {return new Insets(3, 10, 3, 10);}};
			instructionPanel.setBackground(Color.white);
			instructionPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
				
			instructionPanel.add(oneLineLabel("Building updates the e-book for various changes:"));
			instructionPanel.add(oneLineLabel(" "));
			instructionPanel.add(oneLineLabel("   *  a new structure for the e-book"));
			instructionPanel.add(oneLineLabel("   *  any new titles for chapters, sections & pages"));
			instructionPanel.add(oneLineLabel("   *  any new section pages"));
			instructionPanel.add(oneLineLabel("   *  a new index and list of data sets"));
			instructionPanel.add(oneLineLabel(" "));
		thePanel.add(instructionPanel);
		
			buildMaker = new BookIndexer(theBook, castEbook);
		thePanel.add(buildMaker);
		
		return thePanel;
	}
	
	private JPanel printBookPanel(BookReader theBook, CastEbook castEbook) {
		JPanel thePanel = new JPanel();
		thePanel.setOpaque(false);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 12));
		
			JLabel title = new JLabel("E-book for printing", JLabel.CENTER);
			title.setFont(new Font("SansSerif", Font.BOLD, 16));
		thePanel.add(title);
		
			JPanel instructionPanel = new JPanel() {public Insets getInsets() {return new Insets(3, 10, 3, 10);}};
			instructionPanel.setBackground(Color.white);
			instructionPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
				
			instructionPanel.add(oneLineLabel("Building the e-book generates versions of"));
			instructionPanel.add(oneLineLabel("the chapters that are suitable for printing."));
			if (theBook.hasSummaries()) {
				instructionPanel.add(oneLineLabel("Summary versions of the chapters"));
				instructionPanel.add(oneLineLabel("will be separately generated."));
			}
			instructionPanel.add(oneLineLabel(" "));
			instructionPanel.add(oneLineLabel("These HTML files can be accessed by"));
			instructionPanel.add(oneLineLabel("clicking an icon on the right of the"));
			instructionPanel.add(oneLineLabel("e-book's banner."));
			instructionPanel.add(oneLineLabel(" "));
		thePanel.add(instructionPanel);
		
		printMaker = new BookPrintMaker(theBook, castEbook);
		thePanel.add(printMaker);
		
		return thePanel;
	}
	
	private JLabel oneLineLabel(String text) {
		JLabel label = new JLabel(text, JLabel.LEFT);
		label.setBackground(Color.white);
		label.setFont(new Font("SansSerif", Font.PLAIN, 12));
		return label;
	}
	
}
