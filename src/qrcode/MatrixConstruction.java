package qrcode;

import java.util.HashSet;
import java.util.Set;

public class MatrixConstruction {

	/*
	 * Constants defining the color in ARGB format
	 * 
	 * W = White integer for ARGB
	 * 
	 * B = Black integer for ARGB
	 * 
	 * both needs to have their alpha component to 255
	 */
	static int W = 0xFF_FF_FF_FF;
	static int B = (255 << 24);
	// ... MYDEBUGCOLOR = ...;
	// feel free to add your own colors for debugging purposes

	/**
	 * Create the matrix of a QR code with the given data.
	 * 
	 * @param version The version of the QR code
	 * @param data    The data to be written on the QR code
	 * @param mask    The mask used on the data. If not valid (e.g: -1), then no
	 *                mask is used.
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data, int mask) {

		/*
		 * PART 2
		 */
		int[][] matrix = constructMatrix(version, mask);
		/*
		 * PART 3
		 */
		addDataInformation(matrix, data, mask);

		return matrix;
	}

	/*
	 * =======================================================================
	 * 
	 * ****************************** PART 2 *********************************
	 * 
	 * =======================================================================
	 */

	/**
	 * Create a matrix (2D array) ready to accept data for a given version and mask
	 * 
	 * @param version the version number of QR code (has to be between 1 and 4
	 *                included)
	 * @param mask    the mask id to use to mask the data modules. Has to be between
	 *                0 and 7 included to have a valid matrix. If the mask id is not
	 *                valid, the modules would not be not masked later on, hence the
	 *                QRcode would not be valid
	 * @return the qrcode with the patterns and format information modules
	 *         initialized. The modules where the data should be remain empty.
	 */
	public static int[][] constructMatrix(int version, int mask) {
		int[][] matrix = initializeMatrix(version);
		MatrixConstruction.addFinderPatterns(matrix);
		MatrixConstruction.addAlignmentPatterns(matrix, version);
		MatrixConstruction.addTimingPatterns(matrix);
		MatrixConstruction.addDarkModule(matrix);
		MatrixConstruction.addFormatInformation(matrix, mask);
		return matrix;
	}

	/**
	 * Create an empty 2d array of integers of the size needed for a QR code of the
	 * given version
	 * 
	 * @param version the version number of the qr code (has to be between 1 and 4
	 *                included
	 * @return an empty matrix
	 */
	public static int[][] initializeMatrix(int version) {
		int matrixSize = QRCodeInfos.getMatrixSize(version);
		return new int[matrixSize][matrixSize];
	}

	/**
	 * Add all finder patterns to the given matrix with a border of White modules.
	 * 
	 * @param matrix the 2D array to modify: where to add the patterns
	 */
	public static void addFinderPatterns(int[][] matrix) {
		int numSquaresInSinglePattern = 4;
		Set<Integer> whiteSquares = new HashSet<Integer>();
		whiteSquares.add(3);

		matrix = AddSquares(matrix, 3, 3, numSquaresInSinglePattern, whiteSquares);
		matrix = AddSquares(matrix, 3, matrix.length - 4, numSquaresInSinglePattern, whiteSquares);
		matrix = AddSquares(matrix, matrix.length - 4, 3, numSquaresInSinglePattern, whiteSquares);
		matrix = addSeparator(matrix, 3, 3, false, true);
		matrix = addSeparator(matrix, 3, matrix.length - 4, true, true);
		matrix = addSeparator(matrix, matrix.length - 4, 3, false, false);
	}

	// center module is considered as the first square
	// each following square around it, no matter the color, is considered as
	// another square
	// this way I can reuse this code for the alignment pattern
	public static int[][] AddSquares(int[][] matrix, int centerX, int centerY, int numSquares,
			Set<Integer> whiteSquares) {
		for (int i = 0; i < numSquares; i++) {
			int coloring = B;
			if (whiteSquares.contains(i + 1))
				coloring = W;
			int edgeSize = i * 2 + 1;
			int startingX = centerX - i;
			int endingX = startingX + edgeSize - 1;
			int startingY = centerY - i;
			int endingY = startingY + edgeSize - 1;
			for (int j = startingX; j <= endingX; j++) {
				matrix[j][startingY] = coloring;
				matrix[j][endingY] = coloring;
			}
			for (int j = startingY + 1; j <= endingY; j++) {
				matrix[startingX][j] = coloring;
				matrix[endingX][j] = coloring;
			}
		}
		return matrix;
	}

	public static int[][] addSeparator(int[][] matrix, int centerX, int centerY, boolean up, boolean right) {
		int cornerX;
		int cornerY;
		if (up)
			cornerY = centerY - 4;
		else
			cornerY = centerY + 4;
		if (right)
			cornerX = centerX + 4;
		else
			cornerX = centerX - 4;
		for (int i = 0; i < 8; i++) {
			int xOffset = 0;
			int yOffset = 0;
			if (right)
				xOffset = 1;
			if (!up)
				yOffset = 1;
			matrix[cornerX][centerY - 4 + i + yOffset] = W;
			matrix[centerX - 4 + i + xOffset][cornerY] = W;
		}
		return matrix;
	}

	/**
	 * Add the alignment pattern if needed, does nothing for version 1
	 * 
	 * @param matrix  The 2D array to modify
	 * @param version the version number of the QR code needs to be between 1 and 4
	 *                included
	 */
	public static void addAlignmentPatterns(int[][] matrix, int version) {
		Set<Integer> whiteSquares = new HashSet<Integer>();
		whiteSquares.add(2);
		if (version > 1)
			matrix = AddSquares(matrix, matrix.length - 7, matrix.length - 7, 3, whiteSquares);
	}

	/**
	 * Add the timings patterns
	 * 
	 * @param matrix The 2D array to modify
	 */
	public static void addTimingPatterns(int[][] matrix) {
		for (int i = 8; i < (matrix.length - 8); i++) {
			int color = (i % 2 == 0) ? B : W;
			matrix[6][i] = color;
			matrix[i][6] = color;
		}
	}

	/**
	 * Add the dark module to the matrix
	 * 
	 * @param matrix the 2-dimensional array representing the QR code
	 */
	public static void addDarkModule(int[][] matrix) {
		matrix[8][matrix.length - 8] = B;
	}

	/**
	 * Add the format information to the matrix
	 * 
	 * @param matrix the 2-dimensional array representing the QR code to modify
	 * @param mask   the mask id
	 */
	public static void addFormatInformation(int[][] matrix, int mask) {
		boolean[] formatSequence = QRCodeInfos.getFormatSequence(mask);
		int xOffSet = 0;
		int yOffSet = 0;
		for (int i = 0; i < 15; i++) {
			if (i == 6)
				xOffSet = 1;
			if (i == 7) {
				yOffSet = 16 - matrix.length;
				xOffSet = 1 + -yOffSet;
			}
			if (i == 9)
				yOffSet -= 1;
			matrix[8][matrix.length - 1 - i + yOffSet] = formatSequence[i] ? B : W;
			matrix[i + xOffSet][8] = formatSequence[i] ? B : W;
		}
	}

	/*
	 * =======================================================================
	 * ****************************** PART 3 *********************************
	 * =======================================================================
	 */

	/**
	 * Choose the color to use with the given coordinate using the masking 0
	 * 
	 * @param col   x-coordinate
	 * @param row   y-coordinate
	 * @param color : initial color without masking
	 * @return the color with the masking
	 */
	public static int maskColor(int col, int row, boolean dataBit, int masking) {
		if (masking < 0 && masking > 7)
			return (dataBit ? B : W);
		boolean mask = false;
		switch (masking) {
		case 0:
			mask = ((row + col) % 2 == 0);
			break;
		case 1:
			mask = (row % 2 == 0);
			break;
		case 2:
			mask = (col % 3 == 0);
			break;
		case 3:
			mask = ((row + col) % 3 == 0);
			break;
		case 4:
			mask = (Math.floor(row / 2) + Math.floor(row / 3) % 2 == 0);
			break;
		case 5:
			mask = ((row*col)%2 + (row*col)%3)==0;
			break;
		case 6:
			mask = (((row*col)%2 + (row*col)%3)%2==0);
			break;
		case 7:
			mask = (((row+col)%2 + (row*col)%3)%2==0);
			break;
		}
		if(mask) dataBit=!dataBit;
		return dataBit? B:W;
	}

	/**
	 * Add the data bits into the QR code matrix
	 * 
	 * @param matrix a 2-dimensionnal array where the bits needs to be added
	 * @param data   the data to add
	 */
	public static void addDataInformation(int[][] matrix, boolean[] data, int mask) {
		//number of 2 wide columns
		int numberOfColumns=(matrix.length-1)/2;
		int dataIndex=0;
		for(int i=numberOfColumns-1;i>=0;i--) {
			boolean goingUp=true;
			if(i%2==0) goingUp=false;
			System.out.println(goingUp);
			for(int j=0;j<matrix.length*2;j++) {
				int moduleX= (i*2);
				int moduleY= (int) Math.floor(j/2) ;
				int xOffset=(j%2==0)?1:0;
				if(i>2) xOffset+=1;
				moduleX+=xOffset;
				if(goingUp) moduleY=matrix.length-moduleY-1;
				if(matrix[moduleX][moduleY]==0) {

					boolean dataBit=false;
					if(dataIndex<data.length) dataBit=data[dataIndex];
					matrix[moduleX][moduleY]=maskColor(moduleX, moduleY, dataBit , mask);
					System.out.println(moduleX + " " +moduleY + " " + j);
					dataIndex+=1;
				}
			}
		}
	}

	/*
	 * =======================================================================
	 * 
	 * ****************************** BONUS **********************************
	 * 
	 * =======================================================================
	 */

	/**
	 * Create the matrix of a QR code with the given data.
	 * 
	 * The mask is computed automatically so that it provides the least penalty
	 * 
	 * @param version The version of the QR code
	 * @param data    The data to be written on the QR code
	 * @return The matrix of the QR code
	 */
	public static int[][] renderQRCodeMatrix(int version, boolean[] data) {

		int mask = findBestMasking(version, data);

		return renderQRCodeMatrix(version, data, mask);
	}

	/**
	 * Find the best mask to apply to a QRcode so that the penalty score is
	 * minimized. Compute the penalty score with evaluate
	 * 
	 * @param data
	 * @return the mask number that minimize the penalty
	 */
	public static int findBestMasking(int version, boolean[] data) {
		// TODO BONUS
		return 0;
	}

	/**
	 * Compute the penalty score of a matrix
	 * 
	 * @param matrix: the QR code in matrix form
	 * @return the penalty score obtained by the QR code, lower the better
	 */
	public static int evaluate(int[][] matrix) {
		// TODO BONUS

		return 0;
	}

}
