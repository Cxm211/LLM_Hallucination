// org/apache/commons/lang3/StringUtilsTest.java
@Test
public void testEscapeSurrogatePairsWithEscaping() throws Exception {
    // Test surrogate pair followed by a character that needs escaping
    assertEquals("\uD83D\uDE30,", StringEscapeUtils.escapeCsv("\uD83D\uDE30,"));
    // Test character that needs escaping followed by surrogate pair
    assertEquals(",\uD800\uDC00", StringEscapeUtils.escapeCsv(",\uD800\uDC00"));
    // Test multiple surrogate pairs with characters needing escaping
    assertEquals("\uD834\uDD1E,\uDBFF\uDFFD", StringEscapeUtils.escapeCsv("\uD834\uDD1E,\uDBFF\uDFFD"));
}