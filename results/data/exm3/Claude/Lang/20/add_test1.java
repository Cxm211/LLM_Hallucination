// org/apache/commons/lang3/StringUtilsTest.java
public void testJoin_ArrayString_MultipleElements() {
    Object[] fourElements = new Object[]{"x", "y", "z", "w"};
    assertEquals("x::y::z::w", StringUtils.join(fourElements, "::", 0, 4));
    assertEquals("y::z", StringUtils.join(fourElements, "::", 1, 3));
}