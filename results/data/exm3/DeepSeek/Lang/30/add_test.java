// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testContainsAny_LoneHighSurrogateVsPairWithSameHigh() {
        // CharUSuppCharLow is a lone high surrogate (H1)
        // CharU20000 is a pair H1+L00
        // Should return false because lone high surrogate should not match pair's high surrogate
        assertEquals(false, StringUtils.containsAny(CharUSuppCharLow, CharU20000.toCharArray()));
    }
