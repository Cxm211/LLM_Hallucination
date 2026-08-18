// org/apache/commons/lang3/StringUtilsTest.java
@Test
public void testEscapeSurrogatePairsInQuotes() throws Exception {
    // Test surrogate pairs with newlines that trigger CSV quoting
    assertEquals("\"\uD83D\uDE30\n\"", StringEscapeUtils.escapeCsv("\uD83D\uDE30\n"));
    // Test surrogate pair with comma and quote
    assertEquals("\",\uD800\uDC00\"", StringEscapeUtils.escapeCsv(",\uD800\uDC00"));
}