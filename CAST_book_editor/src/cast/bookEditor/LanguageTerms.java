package cast.bookEditor;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import cast.bookManager.*;
import cast.utils.*;


class OneTerm {
	private String originalTranslation;
	private JTextField termEdit = null;
	private boolean canEdit;
	
	OneTerm(String originalTranslation, boolean canEdit) {
		this.originalTranslation = originalTranslation;
		this.canEdit = canEdit;
	}
	
	JTextField createTextEdit() {
		if (canEdit)
			termEdit = new JTextField(originalTranslation);
		return termEdit;
	}
	
	boolean changed() {
		return termEdit != null && !termEdit.getText().equals(originalTranslation);
	}
	
	String getTranslation() {
		return termEdit == null ? null : termEdit.getText();
	}
}

//----------------------------------------------------------


public class LanguageTerms extends JDialog {
	static final private Color kTitleBackground = new Color(0xDDDDFF);
	
	static final private String termTypes[] = {"Java Button terms", "Java Graphics terms", "Other Java terms",
																				"Index entries", "User interface entries", "Index entries (browser only)",
																				"User interface entries (browser only)"};
	
	public static void editTerms(Frame parent, CastEbook castEbook) {
		@SuppressWarnings("unused")
		DomBook domBook = castEbook.getDomBook();
		
		String language = castEbook.getLanguage();
		String languageName = language.equals("de") ? "German"
													: language.equals("es") ? "Spanish"
													: language.equals("fr") ? "French"
													: language.equals("zh") ? "Chinese"
													: language.equals("gd") ? "Gaelic"
													: "???";
		File javaDir = new File(castEbook.getCoreDir(), "java");
		File javaTermsDir = new File(javaDir, "textBundles");
		File buttonFile = new File(javaTermsDir, "Buttons_" + language + ".properties");
		File graphicsFile = new File(javaTermsDir, "Graphics_" + language + ".properties");
		File generalFile = new File(javaTermsDir, "Terms_" + language + ".properties");
		
		Map buttonTerms = readTerms(buttonFile, true);
		Map graphicsTerms = readTerms(graphicsFile, true);
		Map generalTerms = readTerms(generalFile, true);
		
		File uiTermsDir = new File(javaDir, "uiBundles");
		File indexFile = new File(uiTermsDir, "IndexTerms_" + language + ".properties");
		File uiTermsFile = new File(uiTermsDir, "UiTerms_" + language + ".properties");
		
		Map indexTerms = readTerms(indexFile, true);
		Map uiTerms = readTerms(uiTermsFile, true);
		
		File htmlTermsDir = new File(castEbook.getCoreDir(), "terms");
		File htmlIndexFile = new File(htmlTermsDir, "index_" + language + ".properties");
		File htmlUiTermsFile = new File(htmlTermsDir, "uiTerms_" + language + ".properties");
		
		Map htmlIndexTerms = readTerms(htmlIndexFile, false);
		Map htmlUiTerms = readTerms(htmlUiTermsFile, false);
		
		LanguageTerms dialog = new LanguageTerms(languageName, castEbook.getShortBookName(), buttonTerms,
											graphicsTerms, generalTerms, indexTerms, uiTerms, htmlIndexTerms, htmlUiTerms, parent);

		Point p1 = parent.getLocation();
		Dimension d1 = parent.getSize();
		Dimension d2 = dialog.getSize();

		int x = p1.x + (d1.width - d2.width) / 2;
		int y = p1.y + (d1.height - d2.height) / 2;

		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;

		dialog.setLocation(x,y);
		dialog.setVisible(true);

		if (dialog.ok) {
			saveTerms(buttonTerms, buttonFile, true);
			saveTerms(graphicsTerms, graphicsFile, true);
			saveTerms(generalTerms, generalFile, true);
			saveTerms(indexTerms, indexFile, true);
			saveTerms(uiTerms, uiTermsFile, true);
			
			saveTerms(indexTerms, indexFile, false);
			saveTerms(uiTerms, uiTermsFile, false);
		}

		dialog.dispose();
	}
	
	static private Map readTerms(File termFile, boolean withHashesInKey) {
		String termString = HtmlHelper.getFileAsString(termFile);
		String allTerms[] = termString.split("[\\r\\n]+");
		Map theMap = new HashMap(allTerms.length);
		for (int i=0 ; i<allTerms.length ; i++)
			if (!allTerms[i].matches("\\s*#.*")) {					//	comment starts with spaces then #
				String oneTerm[] = allTerms[i].split("=");
				if (oneTerm.length == 2) {
					String key = oneTerm[0];
					if (withHashesInKey)
						key = key.replaceAll("#", " ");
					theMap.put(key, new OneTerm(oneTerm[1], true));
				}
				else
					System.out.println("Error: term \"" + allTerms[i] + "\" does not have a single \"=\"");
			}
		return new TreeMap(theMap);			//	TreeMap sorts by key
	}
	
	static private void saveTerms(Map terms, File termFile, boolean withHashesInKey) {       
		Iterator iterator = terms.keySet().iterator();
		boolean changed = false;
		
		while(iterator.hasNext()){        
			String key = (String)iterator.next();
			OneTerm term = (OneTerm)terms.get(key);
			if (term.changed())
				changed = true;
		}
		
		if (changed)
			try {
//				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(termFile),"UTF8"));
//				PrintWriter termWriter = new PrintWriter(bw);
				PrintWriter termWriter = FileFinder.createUTF8Writer(termFile);
				
				iterator = terms.keySet().iterator();
				while(iterator.hasNext()){        
					String key = (String)iterator.next();
					OneTerm term = (OneTerm)terms.get(key);
					String value = term.getTranslation();
					
					if (withHashesInKey)
						key = key.replaceAll(" ", "#");
					termWriter.println(key + "=" + value);
				}
				
				termWriter.flush();
				termWriter.close();
			} catch (IOException e) {
				System.err.println(e.toString());
			}
	}
	
	private boolean ok = false;
	
	private LanguageTerms(String languageName, String shortBookName, Map buttonTerms, Map graphicsTerms,
										Map generalTerms, Map indexTerms, Map uiTerms, Map htmlIndexTerms, Map htmlUiTerms, Frame parent) {
		super(parent, "Translation for e-book \"" + shortBookName + "\"", true);
		
		setLayout(new BorderLayout(0, 0));
		
			final JPanel mainPanel = new JPanel();
			final CardLayout mainPanelLayout = new CardLayout();
			mainPanel.setLayout(mainPanelLayout);
			
			mainPanel.add(createTermEditor(buttonTerms, true), termTypes[0]);
			mainPanel.add(createTermEditor(graphicsTerms, true), termTypes[1]);
			mainPanel.add(createTermEditor(generalTerms, true), termTypes[2]);
			mainPanel.add(createTermEditor(indexTerms, true), termTypes[3]);
			mainPanel.add(createTermEditor(uiTerms, true), termTypes[4]);
			
			mainPanel.add(createTermEditor(htmlIndexTerms, false), termTypes[5]);
			mainPanel.add(createTermEditor(htmlUiTerms, false), termTypes[6]);
			
		add("Center", mainPanel);
		
			JPanel topPanel = new JPanel() {
																				public Insets getInsets() {
																					return new Insets(2, 10, 3, 10);
																				}
																			};
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 5));
			topPanel.setBackground(kTitleBackground);
				JLabel heading = new JLabel("Terms in " + languageName + " e-books", JLabel.CENTER);
				heading.setFont(new Font("SansSerif", Font.BOLD, 24));
			topPanel.add(heading);
			
				JPanel indexChoicePanel = new JPanel();
				indexChoicePanel.setOpaque(false);
				indexChoicePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					JLabel choiceLabel = new JLabel("Terms of type:", JLabel.LEFT);
					
					JComboBox indexTypeChoice = new JComboBox(termTypes);
					indexTypeChoice.addActionListener(new ActionListener() {
																												public void actionPerformed(ActionEvent e) {
																													JComboBox cb = (JComboBox)e.getSource();
																													String termType = (String)cb.getSelectedItem();
																													mainPanelLayout.show(mainPanel, termType);
																												}
																						});
					choiceLabel.setLabelFor(indexTypeChoice);
					
				indexChoicePanel.add(choiceLabel);
				indexChoicePanel.add(indexTypeChoice);
					
			topPanel.add(indexChoicePanel);
			
				JLabel warning = new JLabel("Warning: any changes here will affect all " + languageName + " e-books.", JLabel.LEFT);
				warning.setFont(new Font("SansSerif", Font.BOLD, 12));
				warning.setForeground(Color.red);
			topPanel.add(warning);
		
		add("North", topPanel);
		
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			
				JButton saveButton = new JButton("Save");
				saveButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					ok = true;
																					setVisible(false);
																				}
																		});
			bottomPanel.add(saveButton);
			
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					ok = false;
																					setVisible(false);
																				}
																		});
			bottomPanel.add(cancelButton);
			
		add("South", bottomPanel);
			
		pack();
	}
	
	
	private JPanel createTermEditor(Map terms, boolean withHashesInKey) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		     
			JPanel innerPanel = new JPanel();
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
			
			Iterator iterator = terms.keySet().iterator();
			while(iterator.hasNext()){     
				JPanel termPanel = new JPanel();
				termPanel.setLayout(new GridLayout(0, 2));   
				String key = (String)iterator.next();
				if (withHashesInKey)
					key = key.replaceAll("#", " ");
				OneTerm term = (OneTerm)terms.get(key);
				JLabel keyLabel = new JLabel(key, JLabel.RIGHT);
				thePanel.add(keyLabel);
				
				JTextField valueEdit = term.createTextEdit();
				termPanel.add(keyLabel);
				termPanel.add(valueEdit);
				keyLabel.setLabelFor(valueEdit);
				
				innerPanel.add(termPanel);
			}
			
			JScrollPane scrollPane = new JScrollPane(innerPanel);
		
		thePanel.add("Center", scrollPane);
//		thePanel.add("Center", new JPanel());
		
		return thePanel;
	}
	

/*	
	private JPanel createTermEditor(Map terms, boolean withHashesInKey) {
		JPanel thePanel = new JPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		     
			JPanel innerPanel = new JPanel();
			innerPanel.setLayout(new GridLayout(0, 2));
					 
				Iterator iterator = terms.keySet().iterator();
				while(iterator.hasNext()){        
					String key = (String)iterator.next();
					if (withHashesInKey)
						key = key.replaceAll("#", " ");
					OneTerm term = (OneTerm)terms.get(key);
					JLabel keyLabel = new JLabel(key, JLabel.RIGHT);
					thePanel.add(keyLabel);
					
					JTextField valueEdit = term.createTextEdit();
					innerPanel.add(keyLabel);
					innerPanel.add(valueEdit);
					keyLabel.setLabelFor(valueEdit);
				}
			
			JScrollPane scrollPane = new JScrollPane(innerPanel);
		
		thePanel.add("Center", scrollPane);
//		thePanel.add("Center", new JPanel());
		
		return thePanel;
	}
*/
}
