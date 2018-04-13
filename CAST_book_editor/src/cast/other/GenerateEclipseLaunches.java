package cast.other;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;

import cast.utils.*;


public class GenerateEclipseLaunches extends JFrame {
	static private final Color kBackgroundColor = new Color(0xeeeeff);
	
	static private String kLauncherString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
			+ "<launchConfiguration type=\"org.eclipse.jdt.launching.javaApplet\">\n"
			+ "<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_PATHS\">\n"
			+ "<listEntry value=\"/CAST_build_core/src/!!1!!.java\"/>\n"
			+ "</listAttribute>\n"
			+ "<listAttribute key=\"org.eclipse.debug.core.MAPPED_RESOURCE_TYPES\">\n"
			+ "<listEntry value=\"1\"/>\n"
			+ "</listAttribute>\n"
			+ "<intAttribute key=\"org.eclipse.jdt.launching.APPLET_HEIGHT\" value=\"!!2!!\"/>\n"
			+ "<intAttribute key=\"org.eclipse.jdt.launching.APPLET_WIDTH\" value=\"!!3!!\"/>\n"
			+ "<stringAttribute key=\"org.eclipse.jdt.launching.APPLET_NAME\" value=\"\"/>\n"
			+ "<mapAttribute key=\"org.eclipse.jdt.launching.APPLET_PARAMETERS\">\n"
			+ "!!4!!"
			+ "</mapAttribute>\n"
			+ "<booleanAttribute key=\"org.eclipse.jdt.launching.ATTR_USE_START_ON_FIRST_THREAD\" value=\"true\"/>\n"
			+ "<stringAttribute key=\"org.eclipse.jdt.launching.MAIN_TYPE\" value=\"!!5!!\"/>\n"
			+ "<stringAttribute key=\"org.eclipse.jdt.launching.PROJECT_ATTR\" value=\"CAST_build_core\"/>\n"
			+ "</launchConfiguration>";
	
	static private String kParamString = "<mapEntry key=\"!!1!!\" value=\"!!2!!\"/>\n";

	
	private JButton encodeButton;
	private JLabel finishedLabel;
//	private File coreDir;
	private File launchDir;
	
	private HashMap appletMap = new HashMap();
	
	public GenerateEclipseLaunches(File coreDir) {
		super("Generage Eclipse launch files");
//		this.coreDir = coreDir;
		launchDir = new File("../../../CAST eclipse workspace/.metadata/.plugins/org.eclipse.debug.core/.launches");
		if (!launchDir.exists())
			System.out.println("Cannot find Eclipse launch folder:\n" + launchDir.getAbsolutePath());
		
		setLayout(new BorderLayout(0, 10));
		setBackground(kBackgroundColor);
		
			Panel buttonPanel = new Panel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				encodeButton = new JButton("Pick source folder containing HTML...");
				encodeButton.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										File dir = chooseFolder();
										if (dir != null) {
											if (dir.getName().equals("src") || dir.getParentFile().getName().equals("src")) {
												finishedLabel.setText("Starting");
												generateAllLaunchFiles(dir);
												finishedLabel.setText("Finished");
											}
											else
												JOptionPane.showMessageDialog(GenerateEclipseLaunches.this, "This is not a source code folder.", "Error!", JOptionPane.ERROR_MESSAGE);
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
		fc.setDialogTitle("Select source folder with HTML files");
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
	
  private void generateLaunchFiles(File inFile) {
		try {
			String s = HtmlHelper.getFileAsString(inFile);
			s = s.replaceAll("<!", "<");
			s = s.replaceAll("APPLET", "applet");
			s = s.replaceAll("CODE", "code");
			s = s.replaceAll("WIDTH", "width");
			s = s.replaceAll("HEIGHT", "height");
			while (true) {
				int startAppletIndex = s.indexOf("<applet");
//				System.out.println("Start index for applet in String:" + startAppletIndex);
				if (startAppletIndex < 0)
					break;
				int endAppletIndex = s.indexOf("</applet>", startAppletIndex);
			
				String applet = s.substring(startAppletIndex, endAppletIndex);
//				System.out.println("Applet code:\n" + applet);
				
				Pattern appletPattern = Pattern.compile("code=\"(.*)\\.class\"", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher appletMatcher = appletPattern.matcher(applet);
				appletMatcher.find();
				String appletName = appletMatcher.group(1);
			
				Pattern widthPattern = Pattern.compile("width=\"(\\d*)\"", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher widthMatcher = widthPattern.matcher(applet);
				widthMatcher.find();
				String widthString = widthMatcher.group(1);
			
				Pattern heightPattern = Pattern.compile("height=\"(\\d*)\"", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher heightMatcher = heightPattern.matcher(applet);
				heightMatcher.find();
				String heightString = heightMatcher.group(1);
				
				generateOneLaunchFile(applet, appletName, widthString, heightString);
				
				s = s.substring(endAppletIndex);
			}
		} catch (Exception e) {
			System.out.println("Error in generateLaunchFiles() for: " + inFile.toString());
			e.printStackTrace();
		}
  }
	
  private void generateOneLaunchFile(String applet, String appletName, String widthString,
																																			String heightString) {
    Integer appletCount = (Integer)appletMap.get(appletName);
		int count = (appletCount == null) ? 1 : (appletCount.intValue() + 1);
		appletMap.put(appletName, Integer.valueOf(count));
		
		try {
			String outFileName = appletName;
			if (count > 1)
				outFileName += "_" + count;
			outFileName += ".launch";
			File outFile = new File(launchDir, outFileName);
//			System.out.println("Generating output file: " + outFile.getAbsolutePath());
			OutputStream out = new FileOutputStream(outFile);
			Writer w = new OutputStreamWriter(out, "UTF-8");
			
			String parameters = "";		
			Pattern paramPattern = Pattern.compile("<param\\s*name=\"([^\"]*)\"\\s*value=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
			Matcher paramMatcher = paramPattern.matcher(applet);
			while (paramMatcher.find()) {
				String paramName = paramMatcher.group(1);
				String paramValue = paramMatcher.group(2);
				String oneParam = kParamString.replace("!!1!!", paramName);
				oneParam = oneParam.replace("!!2!!", paramValue);
//				System.out.println("param: " + oneParam);
				parameters += oneParam;
			}
			
			String s = kLauncherString.replaceAll("!!5!!", appletName);
			String appletPath = appletName.replace(".", "/");
			s = s.replaceAll("!!1!!", appletPath);
			s = s.replaceAll("!!2!!", heightString);
			s = s.replaceAll("!!3!!", widthString);
			s = s.replaceAll("!!4!!", parameters);
			
			w.write(s);
			
			w.flush();
			w.close();
		} catch (Exception e) {
			System.out.println("Error in generateOneLaunchFile() for: " + appletName);
			e.printStackTrace();
		}
  }
	
	private void generateAllLaunchFiles(File file) {
		if (file.isDirectory()) {
//			System.out.println("looking in directory: " + file.getName());
			File contents[] = file.listFiles();
			for (int i=0 ; i<contents.length ; i++)
				generateAllLaunchFiles(contents[i]);
		}
		else if (file.isFile() && file.getName().endsWith(".html")) {
//			System.out.println("Source file: " + file.getName());
			generateLaunchFiles(file);
		}
	}
	
}
