// org/apache/commons/lang/text/StrBuilderTest.java
public void testIndexOfAfterModification() {
    StrBuilder sb = new StrBuilder("aaabbbccc");
    sb.delete(3, 6);
    assertEquals(3, sb.indexOf("ccc", 0));
    assertEquals(-1, sb.indexOf("bbb", 0));
}