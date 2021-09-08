package ch.epfl.javass.bits;

import ch.epfl.javass.Preconditions;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 */ 
public  final class Bits64 {
    
    private Bits64() {}
    
    /**
     * Creates a mask of 1 in an long binary representation
     * @param   start   starting index of the mask
     * @param   size    size of the mask
     * @return          mask in long form
     */
    public static long mask(int start, int size) {
        Preconditions.checkArgument(start >= 0 && 
        							size >= 0  && 
        							start <= Long.SIZE && 
        							start + size <= Long.SIZE);
           
        if(size == Long.SIZE) {
            return -1L; // Special case because shifts (<<) cannot shift more than 31 (5bits)
        } else {
            return ((1L << size) - 1L) << start;
        }
    }

    /**
     * Extract a part of an long binary representation
     * @param bits      source long from which to extract
     * @param start     starting index of the part to extract
     * @param size      size of the part to extract
     * @return          extracted part in long form
     */
    public static long extract(long bits, int start, int size) {
        Preconditions.checkArgument(start >= 0 && 
        							size >= 0  && 
        							start <= Long.SIZE && 
        							start + size <= Long.SIZE);
            
        return (bits >>> start) & mask(0, size);

    }
    
    /**
     * Check to see if size given is possible and if value will fit size allocated for it in a long
     * @param size      size allocated for a certain value
     * @param value     value of the long
     * @return          true if it fits, false if it doesn't
     */
    private static boolean checking(int size, long value) {
        int value_size = (int)(Math.log(value)/Math.log(2)+1);
        
        return size < 0 || size > Long.SIZE || value_size > size;
          
    }

    /**
     * Pack two given values and their respective sizes in a long if they are correct
     * @param v1    first value
     * @param s1    size allocated to first value
     * @param v2    second value
     * @param s2    size allocated to second value
     * @return      "packed" long 
     */
    public static long pack(long v1, int s1, long v2, int s2) {
            Preconditions.checkArgument(!checking(s1,v1) && 
            							!checking(s2,v2) && 
            							s1 + s2 <= Long.SIZE);
            
            return v1 | (v2 << s1);

    }

}
