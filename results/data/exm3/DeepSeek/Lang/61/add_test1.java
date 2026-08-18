// org/apache/commons/lang/text/StrBuilderTest.java
public void testIndexOfGarbage() {
    StrBuilder sb = new StrBuilder(32);
    sb.append("abc");
    sb.append("xy");
    sb.delete(3, 5);
    assertEquals(-1, sb.indexOf("xy"));
}
