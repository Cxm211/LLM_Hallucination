// org/apache/commons/lang/text/StrBuilderTest.java
public void testIndexOfWithStartIndexBeyondSize() {
    StrBuilder sb = new StrBuilder("hello");
    sb.setLength(3);
    assertEquals("indexOf should return -1 when startIndex >= size", -1, sb.indexOf('l', 3));
    assertEquals("indexOf should return -1 when startIndex > size", -1, sb.indexOf('l', 5));
    assertEquals("indexOf should work when startIndex < size", 2, sb.indexOf('l', 2));
}