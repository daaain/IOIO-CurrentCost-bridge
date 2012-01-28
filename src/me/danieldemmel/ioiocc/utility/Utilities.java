package me.danieldemmel.ioiocc.utility;

import java.math.BigInteger;

public class Utilities
{
	// from http://stackoverflow.com/questions/332079/in-java-how-do-i-convert-a-byte-array-to-a-string-of-hex-digits-while-keeping-l
	public static String toHex(byte[] bytes)
	{
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}
	
	// from http://www.velocityreviews.com/forums/t129673-search-byte-for-pattern.html
	/**
	 * Knuth-Morris-Pratt Algorithm for Pattern Matching
	 */
	public static class KMPMatch
	{
		/**
		 * Finds the first occurrence of the pattern in the text.
		 */
		public static int indexOf(byte[] data, byte[] pattern)
		{
			int[] failure = computeFailure(pattern);
	
			int j = 0;
			if (data.length == 0) return -1;
	
			for (int i = 0; i < data.length; i++)
			{
				while (j > 0 && pattern[j] != data[i])
				{
					j = failure[j - 1];
				}
				if (pattern[j] == data[i]) { j++; }
				if (j == pattern.length)
				{
					return i - pattern.length + 1;
				}
			}
			return -1;
		}
	
		/**
		 * Computes the failure function using a boot-strapping process,
		 * where the pattern is matched against itself.
		 */
		public static int[] computeFailure(byte[] pattern)
		{
			int[] failure = new int[pattern.length];
	
			int j = 0;
			for (int i = 1; i < pattern.length; i++) {
				while (j > 0 && pattern[j] != pattern[i])
				{
					j = failure[j - 1];
				}
				if (pattern[j] == pattern[i])
				{
					j++;
				}
				failure[i] = j;
			}
			return failure;
		}
	}
}
