package cast.other;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import cast.utils.*;


public class ConvertImagesFrame extends JFrame {
	static final private Color kBackgroundColor = new Color(0xeeeeff);
	
	static final private int kMaxMethodSize = 8000;
	
	public ConvertImagesFrame(final File projectDir) {
		super("Tidy summary HTML files");
		
		setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		setBackground(kBackgroundColor);
			JLabel infoLabel = new JLabel("This command converts PNG and GIF images to Java code.", JLabel.LEFT);
		add(infoLabel);
		
			final JLabel messageLabel = new JLabel("Click to start", JLabel.LEFT);
			messageLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
			messageLabel.setForeground(Color.red);
			
			JButton convertButton = new JButton("Convert Images");
			convertButton.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									messageLabel.setText("Converting...");
									doConversion(projectDir);
									messageLabel.setText("Finished");
								}
							});
		add(convertButton);
		add(messageLabel);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private void doConversion(File projectDir) {
		File javaImagesDir = new File(projectDir, "javaImages");
		
		try {
			File buildDir = new File(projectDir.getParent(), "CAST_build_all");
			File srcDir = new File(buildDir, "src");
			File imagesDir = new File(srcDir, "images");
			File outFile = new File(imagesDir, "ImageReader.java");
			OutputStream out = new FileOutputStream(outFile);
			Writer w = new OutputStreamWriter(out);
			
			w.write("package images;\n\n");
			
			w.write("public class ImageReader extends CoreImageReader {\n");
			
			String initCalls = outputFiles(javaImagesDir, w);

			w.write("  protected ImageReader() {\n");
			w.write(initCalls);
			w.write("  }\n\n");
			
			
			w.write("  public CoreImageReader getSubImageReader(String theDir) {\n");
			w.write("    return\n");
			
			outputDirs(javaImagesDir, w, imagesDir);
			
			w.write("      null;			//	initialiser adds images to theBytes\n");
			w.write("  }\n\n");
			
			w.write("}");
			
			w.flush();
			w.close();
			
		} catch (IOException e) {
			System.err.println("Could not save Java file");
			e.printStackTrace();
		}
	}
	
	private String outputFiles(File javaImagesDir, Writer w) throws IOException {
		String initCalls = "";
		File[] files = javaImagesDir.listFiles();
		for (int i=0 ; i<files.length ; i++) {
			File f = files[i];
			if (f.isFile()) {
				String fileName = f.getName();
				if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif"))
					initCalls += outputInitialisers(f, fileName, w);
			}
		}
		return initCalls;
	}
	
	private void outputDirs(File javaImagesDir, Writer w, File imagesDir) throws IOException {
		File[] files = javaImagesDir.listFiles();
		for (int i=0 ; i<files.length ; i++) {
			File f = files[i];
			if (f.isDirectory()) {
				String dirName = f.getName();
				String imageClassName = "ImageReader_" + dirName;
				w.write("      theDir.equals(\"" + dirName + "\") ? new " + imageClassName + "() :\n");
				
				File outFile = new File(imagesDir, imageClassName + ".java");
				OutputStream out = new FileOutputStream(outFile);
				Writer w2 = new OutputStreamWriter(out);
				
				w2.write("package images;\n\n");
				
				w2.write("public class " + imageClassName + " extends CoreImageReader {\n\n");
				
				String initCalls = outputDirInitialisers(f, w2);
				
				w2.write("  protected " + imageClassName + "() {\n");
				w2.write(initCalls);
				
				w2.write("  }\n");
				w2.write("}\n");
		
				w2.flush();
				w2.close();
			}
		}
	}
	
	private String outputDirInitialisers(File imagesDir, Writer w2) throws IOException {
		String initCalls = "";
		String dirPath = imagesDir.getName() + "/";
		File[] contents = imagesDir.listFiles();
		for (int i=0 ; i<contents.length ; i++) {
			File f = contents[i];
			
			String fileName = f.getName();
			if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".gif")) {
				String key = dirPath + fileName;
				initCalls += outputInitialisers(f, key, w2);
			}
		}
		return initCalls;
	}
	
	
	private String outputInitialisers(File f, String key, Writer w) throws IOException {
		String fileName = f.getName().replace(".", "_");
		String initCalls = "";
		
		byte[] fileBytes = readFile(f);
		
		for (int segment=0 ; segment<=fileBytes.length/kMaxMethodSize ; segment++) {
			int startIndex = segment * kMaxMethodSize;
			int endIndex = Math.min(fileBytes.length, startIndex + kMaxMethodSize);
			String methodName = "init_" + fileName;
			String functionCall;
			if (segment == 0)
				functionCall = "addImage";
			else {
				methodName += "_" + segment;
				functionCall = "appendImage";
			}
			
			w.write("  private void " + methodName + "() {\n");
			
			StringBuffer sb = new StringBuffer();
			sb.append("    byte[] byteArray = {");
			
			for (int i=startIndex ; i<endIndex ; i++) {
				String byteString = "(byte)0x" + String.format("%02X", fileBytes[i]);
				if (i > startIndex)
					sb.append(",");
				sb.append(byteString);
			}
			sb.append("};\n");
			w.write(sb.toString());
			
			w.write("    " + functionCall + "(\"" + key + "\", byteArray);\n");
			w.write("  }\n\n");
			
			initCalls += "    " + methodName + "();\n";
		}
		
		return initCalls;
	}
	
  
	private byte[] readFile(File f) {
		byte[] result = new byte[(int)f.length()];
		try {
			InputStream input = null;
			try {
				int totalBytesRead = 0;
				input = new BufferedInputStream(new FileInputStream(f));
				while(totalBytesRead < result.length) {			//	usually only a single iteration
					int bytesRemaining = result.length - totalBytesRead;
																						//	input.read() returns -1, 0, or more :
					int bytesRead = input.read(result, totalBytesRead, bytesRemaining); 
					if (bytesRead > 0)
						totalBytesRead = totalBytesRead + bytesRead;
				}
			}
			finally {
				input.close();
			}
		}
		catch (FileNotFoundException ex) {
			System.out.println("Image file not found");
			ex.printStackTrace();
		}
		catch (IOException ex) {
			System.out.println("Could not read file");
			ex.printStackTrace();
		}
		return result;
	}
	
}
