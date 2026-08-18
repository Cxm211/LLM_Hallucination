// org/apache/commons/lang3/StringUtilsTest.java
public void testJoin_ArrayChar_MultipleElements() {
    Object[] threeElements = new Object[]{"a", "b", "c"};
    assertEquals("a,b,c", StringUtils.join(threeElements, ',', 0, 3));
    assertEquals("a,b", StringUtils.join(threeElements, ',', 0, 2));
    assertEquals("b,c", StringUtils.join(threeElements, ',', 1, 3));
}