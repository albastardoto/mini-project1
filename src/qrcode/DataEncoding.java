package qrcode;

import java.nio.charset.StandardCharsets;
import reedsolomon.ErrorCorrectionEncoding;

public final class DataEncoding {

	/**
	 * @param input
	 * @param version
	 * @return
	 */
	public static boolean[] byteModeEncoding(String input, int version) {
		int maxInputLength= QRCodeInfos.getMaxInputLength(version);
		int CodeWordsLength=QRCodeInfos.getCodeWordsLength(version);
		int errorCorrectionLength=QRCodeInfos.getECCLength(version);
		int[] encodedString=encodeString(input,maxInputLength);
		int[] dataWithHeaders=addInformations(encodedString);
		int[] filledSequence=fillSequence(dataWithHeaders, CodeWordsLength);
		int[] sequenceWithEC=addErrorCorrection(filledSequence, errorCorrectionLength);
		return bytesToBinaryArray(sequenceWithEC);
	}

	/**
	 * @param input     The string to convert to ISO-8859-1
	 * @param maxLength The maximal number of bytes to encode (will depend on the
	 *                  version of the QR code)
	 * @return A array that represents the input in ISO-8859-1. The output is
	 *         truncated to fit the version capacity
	 */
	public static int[] encodeString(String input, int maxLength) {
		byte[] encodedStringInBytes = input.getBytes(StandardCharsets.ISO_8859_1);
		if (encodedStringInBytes.length <= maxLength) {
			return getIntsArray(encodedStringInBytes);
		} else {
			byte[] truncatedArray = new byte[maxLength];
			for (int i = 0; i < maxLength; i++) {
				truncatedArray[i] = encodedStringInBytes[i];
			}
			return getIntsArray(truncatedArray);
		}
	}

	public static int[] getIntsArray(byte[] byteArray) {
		int[] intArray = new int[byteArray.length];
		for (int i = 0; i < byteArray.length; i++) {
			intArray[i] = byteArray[i] & 0xFF;
		}
		return intArray;
	}

	/**
	 * Add the 12 bits information data and concatenate the bytes to it
	 * 
	 * @param inputBytes the data byte sequence
	 * @return The input bytes with an header giving the type and size of the data
	 */
	public static int[] addInformations(int[] inputBytes) {
		// TODO Implementer
		byte[] resultsArray = new byte[inputBytes.length + 2];
		// sets first 4 bits of first Byte to 0100 because 0100=4 and 0100<<4= 01000000
		resultsArray[0] = (byte) ((byte) (4 << 4) + (byte) (inputBytes.length >> 4));
		// set second byte which has part of the input length information in it
		resultsArray[1] = ((byte) ((byte) (inputBytes.length << 4) + (byte) (inputBytes[0] >> 4)));
		for (int i = 0; i < inputBytes.length; i++) {
			if (i < (inputBytes.length - 1))
				resultsArray[i + 2] = ((byte) ((byte) (inputBytes[i] << 4) + (byte) (inputBytes[i + 1] >> 4)));
			else
				resultsArray[i + 2] = ((byte) (inputBytes[i] << 4));
		}
		return getIntsArray(resultsArray);
	}

	/**
	 * Add padding bytes to the data until the size of the given array matches the
	 * finalLength
	 * 
	 * @param encodedData the initial sequence of bytes
	 * @param finalLength the minimum length of the returned array
	 * @return an array of length max(finalLength,encodedData.length) padded with
	 *         bytes 236,17
	 */
	public static int[] fillSequence(int[] encodedData, int finalLength) {
		int[] finalData = new int[finalLength];
		int numberOfAddedBytes = 0;
		for (int i = 0; i < finalLength; i++) {
			if (i < encodedData.length) {
				finalData[i] = encodedData[i];
			} else {
				if (numberOfAddedBytes % 2 == 0) {
					finalData[i] = 236;
				} else {
					finalData[i] = 17;
				}
				numberOfAddedBytes++;
			}
		}
		return finalData;
	}

	/**
	 * Add the error correction to the encodedData
	 * 
	 * @param encodedData The byte array representing the data encoded
	 * @param eccLength   the version of the QR code
	 * @return the original data concatenated with the error correction
	 */
	public static int[] addErrorCorrection(int[] encodedData, int eccLength) {
		int fullLength = encodedData.length + eccLength;
		int[] encodedDataWithEC = new int[fullLength];
		int[] errorCorrection = ErrorCorrectionEncoding.encode(encodedData, eccLength);
		for (int i = 0; i < fullLength; i++) {
			if (i < encodedData.length) {
				encodedDataWithEC[i] = encodedData[i];
			} else {
				encodedDataWithEC[i] = errorCorrection[i - encodedData.length];
			}
		}

		return encodedDataWithEC;
	}

	/**
	 * Encode the byte array into a binary array represented with boolean using the
	 * most significant bit first.
	 * 
	 * @param data an array of bytes
	 * @return a boolean array representing the data in binary
	 */
	public static boolean[] bytesToBinaryArray(int[] data) {
		boolean[] binaryArray= new boolean[data.length*8];
		for(int i=0;i<data.length;i++) {
			boolean[] bitsToAdd=byteToBits(data[i]);
			for(int j=0;j<8;j++) {
				binaryArray[i*8+j]=bitsToAdd[j];
			}
		}
		return binaryArray;
	}

	public static boolean[] byteToBits(int data) {
		boolean[] bitForm = new boolean[8];
		for (int i = 0; i<8  ;i++) {
			if (data >= Math.pow(2, 7-i)) {
				data -= Math.pow(2, 7-i);
				bitForm[i] = true;
			} else
				bitForm[i] = false;
		}
		return bitForm;
	}

}
