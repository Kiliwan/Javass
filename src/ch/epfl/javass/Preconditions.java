package ch.epfl.javass;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 */ 
public final class Preconditions {
	private Preconditions() {}
	
	/**
	 * Check to see if given argument given is true, otherwise IllegalArgumentException
	 * @param b		argument to check
	 */
	public static void checkArgument(boolean b) {
		if(!b) {
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Check if index is in bounds of a tested array
	 * @param index		index to check
	 * @param size		size of the array tested
	 * @return			index if in bounds, if not IndexOutOfBoundsException
	 */
	public static int checkIndex(int index, int size) {
		if(index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		} else {
			return index;
		}
	}

}