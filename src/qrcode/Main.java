package qrcode;

public class Main {

	public static final String INPUT =  "hueygaidshfbaklh";

	/*
	 * Parameters
	 */
	public static final int VERSION = 2;
	public static final int MASK = 0;
	public static final int SCALING = 20;

	public static void main(String[] args) {

		/*
		 * Encoding
		 */
		boolean[] encodedData = DataEncoding.byteModeEncoding(INPUT, VERSION);
		
		/*
		 * image
		 */
		int[][] qrCode = MatrixConstruction.renderQRCodeMatrix(VERSION, encodedData,MASK);

		/*
		 * Visualization
		 */
		System.out.println(qrCode);
		Helpers.show(qrCode, SCALING);
	}

}
