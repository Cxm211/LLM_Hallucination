// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testIndexOfAnyBut_PairVsLoneHighSurrogate() {
        // CharU20000 is a pair H1+L00
        // CharUSuppCharLow is a lone high surrogate (H1)
        // Should return 0 because the pair is not in searchChars
        assertEquals(0, StringUtils.indexOfAnyBut(CharU20000, CharUSuppCharLow.toCharArray()));
    }
