// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testContainsNone_LoneHighSurrogateVsPairWithSameHigh() {
        // CharUSuppCharLow is a lone high surrogate (H1)
        // CharU20000 is a pair H1+L00
        // Should return true
        assertEquals(true, StringUtils.containsNone(CharUSuppCharLow, CharU20000.toCharArray()));
    }
