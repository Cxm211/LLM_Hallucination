// org/apache/commons/lang3/StringUtilsEqualsIndexOfTest.java
public void testContainsAnyCharArraySupplementaryEdgeCases() {
    // Define supplementary characters and surrogates
    String suppChar1 = "\uD800\uDC00"; // U+10000
    String suppChar2 = "\uD800\uDC01"; // U+10001
    String highSurrogate = "\uD800";
    String lowSurrogate1 = "\uDC00";
    String lowSurrogate2 = "\uDC01";
    String bmpChar = "A";

    // Failing assertions on buggy version
    assertEquals(false, StringUtils.containsAny(suppChar1, highSurrogate.toCharArray()));
    assertEquals(false, StringUtils.containsAny(highSurrogate, suppChar1.toCharArray()));
    assertEquals(false, StringUtils.containsAny(lowSurrogate1, suppChar1.toCharArray()));
    assertEquals(false, StringUtils.containsAny(suppChar1, lowSurrogate1.toCharArray()));
    assertEquals(false, StringUtils.containsAny(highSurrogate + lowSurrogate2, suppChar1.toCharArray()));
    assertEquals(false, StringUtils.containsAny(suppChar1, suppChar2.toCharArray()));

    // Additional passing assertions
    assertEquals(true, StringUtils.containsAny(bmpChar, new char[]{'A'}));
    assertEquals(false, StringUtils.containsAny(bmpChar, new char[]{'B'}));
    assertEquals(true, StringUtils.containsAny(suppChar1, suppChar1.toCharArray()));
    assertEquals(true, StringUtils.containsAny(suppChar1 + bmpChar, new char[]{'A'}));
}
