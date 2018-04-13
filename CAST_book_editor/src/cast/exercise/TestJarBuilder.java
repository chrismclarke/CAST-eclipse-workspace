package cast.exercise;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;

import org.w3c.dom.*;

import cast.utils.*;


public class TestJarBuilder extends JFrame {
	static final private Color kBackgroundColor = new Color(0xeeeeff);
	
	static private File createScriptFileReference() {
		return new File(".", "buildJarScript.sh");
	}
	
	static private String kBinDir = "../../CAST_build_all/bin";
	static private String kResourceDir = "../../CAST_build_all/resources";
	
	static private File createPackageSrcReference(String packageName) {
		return new File("../../CAST_build_all/src", packageName);
	}
	
	/*
	static private String kCoreBinDir = "../../CAST_build_core/bin";
	static private String kMainBinDir = "../../CAST/core/java";

	static private File createMainPackageSrcReference(String packageName) {
		return new File("../../CAST_build/src", packageName);
	}
	
	static private File createCorePackageDirReference(String packageName) {
		return new File(kCoreBinDir, packageName);
	}
	
	static private File findPackageBinDir(String packageName) {
		return new File(kMainBinDir, packageName);
	}
*/
	
	private JButton doProcessJava;
	private JLabel finishedLabel;
	
	private Vector packageList = new Vector();
	private File pluginDir;
	
	public TestJarBuilder(final DomTopic[] topics, final File pluginDir) {
		super("Build jar file for tests");
		this.pluginDir = pluginDir;
		
		setLayout(new BorderLayout(0, 10));
		setBackground(kBackgroundColor);
		
			Panel buttonPanel = new Panel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 7));
				doProcessJava = new JButton("Create Java jar for tests");		//	minimal packages
				doProcessJava.addActionListener(new ActionListener() {
																			public void actionPerformed(ActionEvent e) {
																				processTestJava(topics);
																			}
																	});
			buttonPanel.add(doProcessJava);
		add("North", buttonPanel);
		
			JPanel messagePanel = new JPanel();
			messagePanel.setLayout(new FixedSizeLayout(200, 40));
				finishedLabel = new JLabel("", Label.LEFT);
			messagePanel.add(finishedLabel);
			
		add("Center", messagePanel);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	
//-----------------------------------------------------------------------------------
	
//	boolean mainPackageBinExists(String packageName) {
//		return findPackageBinDir(packageName).exists();
//	}

//-----------------------------------------------------------------------------------
	
	private void notePackage(String p) {
		if (!packageList.contains(p))
			packageList.add(p);
	}
	
	private void checkPackageDependencies() {
		int packageIndex = 0;
		do {
			String packageI = (String)packageList.elementAt(packageIndex);
			File packageDir = createPackageSrcReference(packageI);
//			File packageDir = createMainPackageSrcReference(packageI);
//			if (!packageDir.exists())
//				packageDir = createCorePackageDirReference(packageI);
			if (!packageDir.exists())
				System.out.println("Cannot find directory for package: " + packageI);
			File[] javaFile = packageDir.listFiles( new FilenameFilter() {
																								public boolean accept(File dir, String name) {
																									return name.indexOf(".java") == name.length() - 5;
																								}
																						});
			for (int i=0 ; i<javaFile.length ; i++) {
				String fileContents = HtmlHelper.getFileAsString(javaFile[i]);
				
				Pattern titlePattern = Pattern.compile("^import\\s*(\\w+)\\.[\\w\\*]*;", Pattern.MULTILINE);
				Matcher titleMatcher = titlePattern.matcher(fileContents);
				while (titleMatcher.find()) {
					String referencedPackage = titleMatcher.group(1);
					notePackage(referencedPackage);
				}
			}
			packageIndex ++;
		} while (packageIndex < packageList.size());
	}
	
	private void buildJar() {
		File scriptFile = createScriptFileReference();
//		System.out.println("Script file: " + scriptFile.getAbsolutePath());
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(scriptFile));
			PrintWriter scriptWriter = new PrintWriter(bw);
			
			File javaDirPath = new File(pluginDir, "java");
			File jarFilePath = new File(javaDirPath, "exercise_cast.jar");
			File manifestFilePath = new File(pluginDir.getParent(), "Manifest.txt");
			
//			scriptWriter.print("jar cf " + jarFilePath.getAbsolutePath() + " ");
			scriptWriter.print("jar cmf " + manifestFilePath.getAbsolutePath() + " " + jarFilePath.getAbsolutePath() + " ");
			Enumeration e = packageList.elements(); 
			while(e.hasMoreElements()) {
				String packageName = (String)e.nextElement();
//				String packageDir = kMainBinDir;
//				if (!mainPackageBinExists(packageName))
//					packageDir = kCoreBinDir;
//				scriptWriter.print("-C " + packageDir + " " + packageName + " ");
				scriptWriter.print("-C " + kBinDir + " " + packageName + " ");
			}
			
			scriptWriter.print("-C " + kResourceDir + " textBundles ");
			
			scriptWriter.print("\njarsigner -tsa http://timestamp.digicert.com -keystore /Volumes/Documents/CAST/Signing/keystore -storepass castPass " + jarFilePath.getAbsolutePath() + " castCert");

			scriptWriter.flush();
			scriptWriter.close();
			
			Runtime.getRuntime().exec("bash buildJarScript.sh");
			
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
	
	private void processTestJava(DomTopic[] topics) {
		packageList.clear();
		
		for (int i=0 ; i<topics.length ; i++) {
			int noOfExercises = topics[i].noOfExercises();
			for (int j=0 ; j<noOfExercises ; j++) {
				DomExercise ex = topics[i].getExercise(j);
				Element exerciseElement = ex.getDomElement();
				String appletName = XmlHelper.getUniqueTagAsString(exerciseElement, "applet");
				String appletPackage = appletName.substring(0, appletName.indexOf('.'));
				notePackage(appletPackage);
			}
		}
		
		finishedLabel.setText("Checking package dependencies");
		checkPackageDependencies();
		
//		notePackage("javaImages");				//		not really packages but must be included in jar file
//		notePackage("textBundles");
		
		finishedLabel.setText("Building jar file");
		buildJar();
		
		finishedLabel.setText("Finished");
	}
}
