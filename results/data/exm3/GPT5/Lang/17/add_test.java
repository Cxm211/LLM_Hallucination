// org/apache/commons/lang3/StringEscapeUtilsTest.java
public void testSupplementaryPairsUnchanged() {
        String input = new StringBuilder("\uD83D\uDE00").append("\uD83D\uDE01").append("B").toString();
        String escaped = StringEscapeUtils.escapeXml(input);
        assertEquals(input, escaped);
    }