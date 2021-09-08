package ch.epfl.javass.bits;

import ch.epfl.javass.Preconditions;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 */ 
public final class Bits32 {
	
	private Bits32() {}

	/**
	 * Creates a mask of 1 in an integer binary representation
	 * @param 	start 	starting index of the mask
	 * @param 	size	size of the mask
	 * @return			mask in integer form
	 */
	public static int mask(int start, int size) {
		Preconditions.checkArgument(start >= 0 && 
				                    size >= 0 && 
				                    start <= Integer.SIZE && 
				                    start + size <= Integer.SIZE);
		
		if(size == 32) {
			return -1; // Special case because shifts (<<) cannot shift more than 31 (5bits)
		} else {
			return ((1 << size) - 1) << start;
		}
	}

	/**
	 * Extract a part of an integer binary representation
	 * @param bits		source integer from which to extract
	 * @param start		starting index of the part to extract
	 * @param size		size of the part to extract
	 * @return			extracted part in integer form
	 */
	public static int extract(int bits, int start, int size) {
		Preconditions.checkArgument(start >= 0 && 
									size >= 0 && 
									start <= Integer.SIZE && 
									start + size <= Integer.SIZE);
		
		return (bits >>> start) & mask(0, size);
	}
	
	/**
	 * Check to see if size given is possible and if value will fit size allocated for it in an integer
	 * @param size		size allocated for a certain value
	 * @param value		value of the integer
	 * @return			true if it fits, false if it doesn't
	 */
	private static boolean checking(int size, int value) {
	    int value_size = (int)(Math.log(value)/Math.log(2)+1);
	    
	    return size < 0 || size > Integer.SIZE || value_size > size;
	}

	/**
	 * Pack two given values and their respective sizes in an integer if they are correct
	 * @param v1	first value
	 * @param s1	size allocated to first value
	 * @param v2	second value
	 * @param s2	size allocated to second value
	 * @return		"packed" integer with 
	 */
	public static int pack(int v1, int s1, int v2, int s2) {
			Preconditions.checkArgument(!checking(s1,v1) && 
										!checking(s2,v2) && 
										s1 + s2 <= Integer.SIZE);
			
			return v1 | (v2 << s1);
	}
	
	/**
	 * Overload of pack value with 3 given values
	 */
	public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) {
		Preconditions.checkArgument(!checking(s1,v1) && 
									!checking(s2,v2) && 
									!checking(s3,v3) && 
									s1 + s2 + s3 <= Integer.SIZE);
		
		return v1 | (v2 << s1) | (v3 << s1+s2);
	}

	/**
	 * Overload of pack value with 7 given values
	 */
	public static int pack(int v1, int s1, int v2, int s2, int v3, int s3, int v4, int s4,
						   int v5, int s5, int v6, int s6, int v7, int s7) {
		Preconditions.checkArgument(!checking(s1,v1) && 
									!checking(s2,v2) && 
									!checking(s3,v3) && 
									!checking(s4,v4) && 
									!checking(s5,v5) && 
									!checking(s6,v6) && 
									!checking(s7,v7) && 
									s1 + s2 + s3 + s4 + s5 + s6 + s7 <= Integer.SIZE);
		
		return v1 | (v2 << s1) | (v3 << s1+s2) | (v4 << s1+s2+s3) | (v5 << s1+s2+s3+s4) | (v6 << s1+s2+s3+s4+s5) | (v7 << s1+s2+s3+s4+s5+s6);
	}


}