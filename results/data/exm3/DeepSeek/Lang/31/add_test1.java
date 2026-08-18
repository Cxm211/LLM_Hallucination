// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testContainsAnyStringSupplementaryEdgeCases() {
    String suppChar1 = "\uD800\uDC00";
    String suppChar2 = "\uD800\uDC01";
    String highSurrogate = "\uD800";
    String lowSurrogate1 = "\uDC00";
    String lowSurrogate2 = "\uDC01";
    String bmpChar = "A";

    // Failing assertions
    assertEquals(false, StringUtils.containsAny(suppChar1, highSurrogate));
    assertEquals(false, StringUtils.containsAny(highSurrogate, suppChar1));
    assertEquals(false, StringUtils.containsAny(lowSurrogate1, suppChar1));
    assertEquals(false, StringUtils.containsAny(suppChar1, lowSurrogate1));
    assertEquals(false, StringUtils.containsAny(highSurrogate + lowSurrogate2, suppChar1));
    assertEquals(false, StringUtils.containsAny(suppChar1, suppChar2));

    // Passing assertions
    assertEquals(true, StringUtils.containsAny(bmpChar, "A"));
    assertEquals(false, StringUtils.containsAny(bmpChar, "B"));
    assertEquals(true, StringUtils.containsAny(suppChar1, suppChar1));
    assertEquals(true, StringUtils.containsAny(suppChar1 + bmpChar, "A"));
}
