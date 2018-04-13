package twoGroup;

import java.awt.*;


public class PersonPicture {
	static final private double maleX[] = {94.8642, 102.6541, 105.2347, 107.6721, 108.5801, 108.5801, 99.6432, 97.875, 96.5847, 96.5847, 97.4927, 96.967, 96.967, 106.5251, 113.7892, 120.4321, 126.2148, 134.7693, 140.2652, 145.8567, 151.0659, 155.6537, 156.1794, 156.9441, 158.7123, 163.7781, 163.7781, 162.0577, 160.385, 158.9991, 159.5248, 156.514, 151.0659, 144.9487, 143.3238, 143.3238, 144.0884, 144.9487, 144.9487, 144.5663, 140.2652, 138.5925, 137.6845, 138.2102, 140.2652, 140.2652, 137.6845, 137.6845, 132.7143, 128.2698, 126.9316, 126.6449, 125.7369, 124.4943, 124.8289, 128.6999, 133.8135, 134.7693, 133.2878, 130.4681, 128.6999, 129.0822, 129.0822, 131.2328, 145.4744, 145.4744, 141.5078, 129.5601, 112.8812, 102.1284, 99.6432, 100.6469, 97.875, 96.5847, 96.2501, 93.6694, 89.8462, 84.637, 81.674, 80.8138, 80.4315, 78.6632, 77.3729, 70.9211, 68.8184, 65.8076, 64.1349, 63.2747, 63.2747, 60.3116, 60.694, 59.9293, 42.3902, 27.3362, 23.6085, 17.8259, 17.2046, 21.4579, 24.8033, 32.4976, 33.7401, 32.8799, 32.0674, 31.5895, 30.0125, 26.5237, 31.2072, 36.4642, 35.9385, 36.7509, 35.9385, 34.6481, 32.8799, 28.2442, 25.7113, 22.7483, 20.8845, 18.7339, 24.2298, 25.09, 24.2298, 22.7483, 19.2118, 17.5391, 15.7709, 16.2966, 18.3516, 17.8259, 14.0982, 8.6023, 6.7863, 5.4959, 5.4959, 3.4409, 1.2903, 0, 1.2903, 3.4409, 5.8782, 9.0324, 11.9476, 18.3516, 20.8845, 24.0386, 27.3362, 34.218, 37.9457, 44.9231, 56.1061, 63.2747, 67.5758, 68.5316, 67.9581, 67.5758, 66.6678, 62.4622, 59.0213, 56.8707, 57.1575, 59.9293, 64.1349, 70.9211, 77.7552, 88.9382, 94.8642};
	static private double maleY[] = {4.8746, 14.0982, 19.2118, 32.9755, 39.2838, 41.1954, 47.5516, 50.0367, 56.2017, 56.3928, 57.3964, 59.0213, 60.4072, 68.436, 72.4026, 73.693, 77.4684, 81.8174, 87.3611, 99.2609, 122.3915, 148.4374, 148.8197, 156.6573, 164.6862, 205.3081, 218.1638, 224.3765, 227.7219, 246.2646, 248.6063, 253.9111, 258.499, 261.2708, 261.2708, 256.8741, 253.0509, 250.4702, 248.6063, 248.4152, 250.4702, 249.6099, 245.6911, 236.2286, 231.2584, 223.5163, 209.657, 207.0764, 175.3912, 159.1425, 156.6573, 156.1794, 156.1794, 157.422, 179.5968, 212.5723, 233.409, 234.2214, 240.5775, 246.9337, 268.8217, 365.0719, 426.2915, 430.1148, 442.0624, 444.213, 447.2716, 445.8856, 441.2022, 437.2833, 433.5557, 429.6369, 421.8948, 409.1347, 395.0843, 353.5065, 312.8846, 280.0047, 270.9723, 270.1121, 270.1121, 273.5052, 283.828, 322.2993, 341.9412, 383.1367, 407.0319, 410.3773, 423.1373, 428.3465, 434.5593, 436.901, 441.2022, 443.3527, 442.4925, 442.4925, 440.0074, 434.7504, 432.2653, 423.1373, 399.3377, 332.5265, 291.4744, 261.462, 243.1104, 234.2214, 216.5867, 171.1857, 168.9873, 168.0315, 153.9811, 152.6429, 153.0253, 164.6862, 180.1225, 197.9484, 204.9258, 227.7219, 240.5775, 242.2502, 244.4008, 246.2646, 246.2646, 245.261, 245.261, 250.4702, 254.7713, 257.2564, 255.9661, 251.2348, 249.6099, 243.1104, 240.5775, 228.3432, 224.9022, 218.1638, 189.1071, 159.5248, 142.9415, 129.5601, 116.3221, 94.9598, 90.3241, 85.1149, 82.4865, 79.6668, 77.8508, 73.693, 70.5388, 66.8112, 61.2197, 58.2567, 54.0511, 53.4298, 49.7021, 44.0151, 42.438, 36.3686, 22.2704, 12.4255, 6.9774, 2.1506, 0, 1.2903, 4.8746};
	
	static final private double femaleX[] = {77.5162, 84.3503, 87.3133, 91.1365, 93.0959, 94.9598, 95.82, 94.1473, 91.1365, 86.6442, 81.1961, 81.1961, 90.2763, 99.1653, 105.1392, 109.1058, 109.4881, 114.2672, 118.9028, 124.3987, 127.9352, 126.1192, 124.3987, 124.112, 122.8217, 123.6819, 122.296, 119.3807, 112.4511, 111.5431, 112.021, 112.8812, 111.2563, 109.4881, 102.7018, 98.6874, 96.2501, 95.3421, 94.4341, 92.2835, 94.0518, 94.9598, 94.4341, 90.2763, 85.8318, 83.7768, 82.4865, 86.4053, 87.3133, 85.8318, 81.5784, 75.2223, 71.5424, 67.4802, 66.62, 70.6344, 70.2521, 68.436, 68.1015, 65.4252, 65.9987, 65.1385, 65.9987, 67.4802, 65.9987, 64.565, 64.565, 62.8445, 61.602, 59.547, 57.7787, 59.9293, 60.7418, 59.0213, 56.0105, 56.0105, 54.7202, 56.0105, 57.3964, 57.3964, 58.6868, 58.1611, 54.7202, 50.419, 41.8645, 36.7031, 35.0305, 36.7031, 41.4822, 41.0043, 36.4164, 34.5048, 32.0674, 32.4498, 32.0674, 30.2992, 29.9169, 29.9169, 27.0016, 20.2154, 15.293, 14.146, 16.6789, 16.1532, 7.3597, 2.9152, 1.8638, 3.9188, 0, 0.3823, 1.8638, 2.2462, 3.9188, 6.0694, 5.5915, 6.8818, 9.4147, 12.8557, 17.0612, 24.421, 34.3136, 35.9863, 38.9493, 41.4822, 45.3054, 50.419, 52.6652, 51.7094, 44.5408, 40.2874, 37.4678, 36.7031, 37.6589, 41.8645, 45.8311, 51.4226, 55.437, 60.3116, 64.4694, 70.4432, 77.5162};
	static private double femaleY[] = {1.9594, 5.7827, 9.128, 13.5247, 17.7781, 24.0386, 32.7843, 41.8645, 48.5552, 54.8157, 59.6426, 64.4694, 69.5352, 75.0789, 77.7074, 81.0527, 83.2033, 89.1771, 111.0652, 144.5186, 184.5192, 223.0862, 226.8616, 226.8616, 229.299, 242.059, 249.7533, 255.3926, 263.0391, 264.7117, 283.2545, 296.0145, 297.6872, 296.3969, 296.0145, 296.3969, 297.6872, 298.882, 302.8964, 321.2479, 349.8745, 355.0836, 359.2892, 381.9897, 395.1321, 404.6424, 411.572, 431.2139, 436.7099, 441.8234, 444.9298, 444.9298, 442.7793, 432.5043, 417.4981, 397.2827, 383.7102, 375.8725, 369.9943, 353.7933, 353.3154, 351.6427, 328.5121, 316.9946, 299.7422, 296.827, 296.3969, 296.3969, 298.882, 311.7376, 323.8764, 340.4597, 355.466, 367.0313, 381.1773, 384.5226, 396.5658, 405.1203, 407.5576, 412.8624, 417.9282, 429.9236, 441.8234, 445.8378, 448.2752, 446.4113, 444.4519, 432.9822, 408.848, 396.5658, 380.7949, 375.4902, 354.2234, 328.5121, 310.1127, 304.1389, 298.2607, 296.827, 295.5366, 295.5366, 294.2463, 293.0037, 274.6522, 264.7117, 257.9733, 250.1834, 244.6875, 228.0086, 218.785, 208.7012, 197.3749, 173.5752, 161.5798, 147.3382, 146.9559, 143.6105, 127.314, 100.2645, 87.9346, 79.4756, 74.5532, 74.5532, 72.4982, 70.8256, 69.0573, 65.7598, 61.1241, 56.2017, 49.8455, 44.5408, 36.7031, 27.5751, 19.9286, 11.2786, 7.073, 3.3453, 1.4815, 0.2867, 0, 0, 1.9594};
	
	static final private double femaleGap1X[] = {34.3136, 37.2766, 37.2766, 29.9169, 24.8033, 24.421, 21.6491, 21.4579, 21.4579, 20.5977, 18.925, 22.6527, 25.2812, 30.7771, 31.6851, 33.3578, 34.3136};
	static private double femaleGap1Y[] = {147.3382, 156.8485, 165.7853, 178.641, 205.5948, 210.7084, 228.4387, 228.9166, 229.8247, 229.8247, 224.3765, 197.3749, 176.8727, 144.8053, 143.1326, 143.6105, 147.3382};
	
	static final private double femaleGap2X[] = {102.7018, 106.9074, 108.6757, 106.9074, 103.9922, 103.5621, 97.2059, 93.1915, 92.8092, 98.4963, 100.1212, 102.7018};
	static private double femaleGap2Y[] = {161.5798, 202.8708, 227.7219, 227.7219, 217.4947, 213.6714, 176.1081, 167.0757, 161.9621, 145.1876, 146.0479, 161.5798};
	
	static final private double kLeft = 0.0;
//	static final private double kTop = 0.0;
	static final private double kMaleWidth = 163.7781;
	static final private double kMaleHeight = 447.2716;
	static final private double kMaleCenter = kMaleWidth / 2;
	static final private double kFemaleWidth = 127.9352;
	static final private double kFemaleHeight = 448.2752;
	static final private double kFemaleCenter = kFemaleWidth / 2;
	
	static final public Color kMaleFillColor = Color.lightGray;
	static final public Color kMaleBorderColor = Color.blue;
	
	static final public Color kFemaleFillColor = Color.lightGray;
	static final public Color kFemaleBorderColor = new Color(0x006600);
		
	static public int drawMale(Graphics g, int bottom, int left, double height) {
		double scaling = height / kMaleHeight;
		
		drawShape(g, bottom, left, kLeft, height, maleX, maleY, scaling, scaling, kMaleFillColor,
																									kMaleBorderColor);
		
		return left + (int)Math.round(scaling * kMaleWidth);
	}
		
	static public void drawCenteredMale(Graphics g, int bottom, int center, double height,
																					double widthScaleFactor) {
		double yScaling = height / kMaleHeight;
		double xScaling = yScaling * widthScaleFactor;
		
		drawShape(g, bottom, center, kMaleCenter, height, maleX, maleY, xScaling,
																	yScaling, kMaleFillColor, kMaleBorderColor);
	}
	
	static public int drawFemale(Graphics g, int bottom, int left, double height) {
		double scaling = height / kFemaleHeight;
		
		drawShape(g, bottom, left, kLeft, height, femaleX, femaleY, scaling, scaling,
																			kFemaleFillColor, kFemaleBorderColor);
		drawShape(g, bottom, left, kLeft, height, femaleGap1X, femaleGap1Y, scaling, scaling,
																			Color.white, kFemaleBorderColor);
		drawShape(g, bottom, left, kLeft, height, femaleGap2X, femaleGap2Y, scaling, scaling,
																			Color.white, kFemaleBorderColor);
		
		return left + (int)Math.round(scaling * kFemaleWidth);
	}
	
	static public void drawCenteredFemale(Graphics g, int bottom, int center, double height,
								double widthScaleFactor, Color leftHoleColor, Color rightHoleColor) {
		double yScaling = height / kFemaleHeight;
		double xScaling = yScaling * widthScaleFactor;
		
		drawShape(g, bottom, center, kFemaleCenter, height, femaleX, femaleY, xScaling,
																yScaling, kFemaleFillColor, kFemaleBorderColor);
		drawShape(g, bottom, center, kFemaleCenter, height, femaleGap1X, femaleGap1Y, xScaling,
																yScaling, leftHoleColor, kFemaleBorderColor);
		drawShape(g, bottom, center, kFemaleCenter, height, femaleGap2X, femaleGap2Y, xScaling,
																yScaling, rightHoleColor, kFemaleBorderColor);
	}
	
	static private void drawShape(Graphics g, int bottom, int screenH, double coordH, double height,
								double[] xCoord, double[] yCoord, double xScaling, double yScaling,
								Color fillColor, Color borderColor) {
		int[] x = new int[xCoord.length];
		int[] y = new int[xCoord.length];
		for (int i=0 ; i<xCoord.length ; i++) {
			x[i] = screenH + (int)Math.round((xCoord[i] - coordH) * xScaling);
			y[i] = bottom + (int)Math.round(yCoord[i] * yScaling - height);
		}
		g.setColor(fillColor);
		g.fillPolygon(x, y, xCoord.length);
		
		g.setColor(borderColor);
		g.drawPolygon(x, y, xCoord.length);
	}
}