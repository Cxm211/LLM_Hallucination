// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testIndexOfAny_LoneHighSurrogateVsPairWithSameHigh() {
        // CharUSuppCharLow is a lone high surrogate (H1)
        // CharU20000 is a pair H1+L00
        // Should return -1
        assertEquals(-1, StringUtils.indexOfAny(CharUSuppCharLow, CharU20000.toCharArray()));
    }
