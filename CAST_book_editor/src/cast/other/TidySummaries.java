package cast.other;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;

import javax.swing.*;

import cast.utils.*;


//	Deletes <index> and <dataset> tags from summary HTML files


public class TidySummaries extends JFrame {
	public static final Color kBackgroundColor = new Color(0xeeeeff);
	
  static public void tidyFile(File inFile) {
    try {
			String s = HtmlHelper.getFileAsString(inFile);
			Pattern thePattern = Pattern.compile("<meta\\s*name=\"index\"[^>]*>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher theMatcher = thePattern.matcher(s);
			String newS = theMatcher.replaceAll("");
			
			thePattern = Pattern.compile("<meta\\s*name=\"dataset\"[^>]*>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			theMatcher = thePattern.matcher(newS);
			newS = theMatcher.replaceAll("");
			
			thePattern = Pattern.compile("\\s*<script type=\"text/javascript\">.*</script>\\s*</head>", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			theMatcher = thePattern.matcher(newS);
			newS = theMatcher.replaceAll("\n</head>");
			
			if (!s.equals(newS)) {
				File dir = inFile.getParentFile();
				String fileName = inFile.getName();
				File outFile = new File(dir, fileName + "__X");
				
				OutputStream out = new FileOutputStream(outFile);
				Writer w = new OutputStreamWriter(out, "UTF-8");
				w.write(newS);
				w.flush();
				w.close();
				
				inFile.delete();
				outFile.renameTo(inFile);
			}
		} catch (Exception e) {
			System.out.println("Error in tidyFile() for: " + inFile.toString());
			e.printStackTrace();
		}
  }
	
	private JButton encodeButton;
	private JLabel finishedLabel;
//	private File coreDir;
	
	public TidySummaries(File coreDir) {
		super("Tidy summary HTML files");
//		this.coreDir = coreDir;
		
		setLayout(new BorderLayout(0, 10));
		setBackground(kBackgroundColor);
		
			Panel buttonPanel = new Panel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				encodeButton = new JButton("Pick section folder contining summaries...");
				encodeButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										File dir = chooseFolder();
										if (dir != null) {
											if (dir.getParentFile().getParentFile().getName().equals("core")) {
												finishedLabel.setText("Starting");
												tidySummaries(dir);
												finishedLabel.setText("Finished");
											}
											else
												JOptionPane.showMessageDialog(TidySummaries.this, "This is not a section folder.", "Error!", JOptionPane.ERROR_MESSAGE);
										}
									}
								});
			buttonPanel.add(encodeButton);
		add("North", buttonPanel);
		
			Panel messagePanel = new Panel();
			messagePanel.setLayout(new FixedSizeLayout(200, 40));
				finishedLabel = new JLabel("", Label.LEFT);
			messagePanel.add(finishedLabel);
		add("Center", messagePanel);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private File chooseFolder() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setDialogTitle("Select folder to tidy");
		fc.setFileHidingEnabled(true);

		int result = fc.showOpenDialog(this);
	
		switch (result) {
			case JFileChooser.APPROVE_OPTION:
				return fc.getSelectedFile();
			case JFileChooser.CANCEL_OPTION:
			case JFileChooser.ERROR_OPTION:
				System.exit(0);
		}
		return null;
	}
	
	private void tidySummaries(File file) {
		if (file.isDirectory()) {
			File contents[] = file.listFiles();
			for (int i=0 ; i<contents.length ; i++) {
				File f = contents[i];
				if (f.isFile() && f.getName().endsWith(".html") && f.getName().startsWith("s_")) {
					System.out.println("Tidying " + f.getName());
					tidyFile(f);
				}
			}
		}
	}
	
}
