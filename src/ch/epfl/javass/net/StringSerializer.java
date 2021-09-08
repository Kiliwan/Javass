package ch.epfl.javass.net;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author GARANDEL.Eloi (SCIPER : 300326) / D'ETERNOD.Kilian (SCIPER : 296357)
 */
public final class StringSerializer {

	private final static int BASE_16 = 16;
	
	private StringSerializer() {}
	
	/**
	 * Returns the textual representation in base 16 of the given integer
	 * @param  i		integer whose representation is wanted
	 * @return string	textual representation
	 */
	public static String serializeInt(int i) {
		return Integer.toUnsignedString(i, BASE_16);
	}
	
	/**
	 * Returns the integer corresponding to the given textual representation in base 16
	 * @param  s	textual representation in base 16 of the wanted integer
	 * @return int	wanted integer 
	 */
	public static int deserializeInt(String s) {
		return Integer.parseUnsignedInt(s, BASE_16);
	}
	
	/**
	 * Returns the textual representation in base 16 of the given long
	 * @param  i		long whose representation is wanted
	 * @return string	textual representation
	 */
	public static String serializeLong(long i) {
		return Long.toUnsignedString(i, BASE_16);
	}
	
	/**
	 * Returns the long corresponding to the given textual representation in base 16
	 * @param  s	textual representation in base 16 of the wanted long
	 * @return int	wanted long 
	 */
	public static long deserializeLong(String s) {
		return Long.parseLong(s, BASE_16);
	}
	
	/**
	 * Encodes the given string in base64
	 * @param  s 		string we want to encode
	 * @return string	encoded string
	 */
	public static String serializeString(String s) {
		return new String(Base64.getEncoder().encode(s.getBytes()), StandardCharsets.UTF_8);
	}
	
	/**
	 * Decodes the given string that is in base64
	 * @param  s 		string we want to decode
	 * @return string	decoded string
	 */
	public static String deseralizeString(String s) {
		return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
	}
	
	/**
	 * Combines all the strings contained in the table in one string, separated by the given separator
	 * @param   separator 	char that will separate the old strings in our new all-in-one string
	 * @param   strings		table containing all the strings we want to combine
	 * @return	string 		string formed by all the strings, separated by the separator
	 */
	public static String combine(String separator, String[] strings) {
		
		String s = new String();
		for(String toAdd : strings) {
			s = String.join(separator,s, toAdd);
		}
		return s.substring(1, s.length());
	}
	
	/**
	 * Split all the strings contained in the all-in-one string "s", separated by the given separator
	 * @param   separator 	char that will separate the strings in the all-in-one string "s"
	 * @param   string		string that contains all the strings that we want to separate
	 * @return	string 		table of string that contains all the string separated
	 */
	public static String[] split(String separator, String s) {
		return s.split(separator);
	}	
}
