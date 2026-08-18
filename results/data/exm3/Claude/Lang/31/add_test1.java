// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testContainsAnyCharArrayWithLoneSurrogates() {
	// Test with lone surrogates (not paired)
	char highSurrogate = CharU20000.charAt(0);
	char lowSurrogate = CharU20000.charAt(1);
	// Search with just high surrogate should match if cs contains that high surrogate
	assertEquals(true, StringUtils.containsAny(CharU20000, new char[]{highSurrogate}));
	// Search with just low surrogate should match if cs contains that low surrogate  
	assertEquals(true, StringUtils.containsAny(CharU20000, new char[]{lowSurrogate}));
	// But mismatched surrogate pairs should not match the supplementary character
	char highSurrogate2 = CharU20001.charAt(0);
	char lowSurrogate2 = CharU20001.charAt(1);
	assertEquals(false, StringUtils.containsAny(CharU20000, new char[]{highSurrogate, lowSurrogate2}));
	assertEquals(false, StringUtils.containsAny(CharU20000, new char[]{highSurrogate2, lowSurrogate}));
}