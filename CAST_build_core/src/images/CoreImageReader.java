package images;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;

abstract public class CoreImageReader {
  static protected HashMap<String,byte[]> theBytes = new HashMap<String,byte[]>();
  static protected HashMap<String,Image> theImages = new HashMap<String,Image>();
  
	static private ImageReader theImageReader = new ImageReader();
	
	static protected void addImage(String key, byte[] byteArray) {
		theBytes.put(key, byteArray);
		theImages.put(key, null);
	}
	
	static protected void appendImage(String key, byte[] byteArray) {
		byte[] oldByteArray = theBytes.get(key);
		if (oldByteArray != null) {
			byte[] newByteArray = new byte[oldByteArray.length + byteArray.length];
			System.arraycopy(oldByteArray, 0, newByteArray, 0, oldByteArray.length);
			System.arraycopy(byteArray, 0, newByteArray, oldByteArray.length, byteArray.length);
			theBytes.put(key, newByteArray);
		}
		theImages.put(key, null);
	}

  static public Image getImage(String key) {
    Image img = theImages.get(key);
    if (img == null) {
      byte[] imgBytes = theBytes.get(key);
      if (imgBytes == null) {
        int slashIndex = key.indexOf("/");
        if (slashIndex > 0) {
          String delegateName = key.substring(0, slashIndex);
          @SuppressWarnings("unused")
		CoreImageReader ir = theImageReader.getSubImageReader(delegateName);			//	initialiser adds images to theBytes
          imgBytes = theBytes.get(key);
        }
      }
      if (imgBytes != null) {		//	it should never be null if the image actually exists
        ByteArrayInputStream is = new ByteArrayInputStream(imgBytes);
        try {
          img = ImageIO.read(is);
          theImages.put(key, img);
        } catch (IOException e) {
          System.err.println("Could not import image");
          e.printStackTrace();
        }
      }
    }
    return img;
  }

}