// org/apache/commons/lang/text/StrBuilderTest.java
public void testContainsWithEmptyBuffer() {
    StrBuilder sb = new StrBuilder("test");
    sb.clear();
    assertFalse("Empty buffer should not contain any character", sb.contains('t'));
    assertEquals("Empty buffer should return -1 for indexOf", -1, sb.indexOf('t'));
}