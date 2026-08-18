// org/apache/commons/lang3/ArrayUtilsAddTest.java
public void testAddAllSameType() {
    Integer[] result = ArrayUtils.addAll(new Integer[]{Integer.valueOf(1), Integer.valueOf(2)}, new Integer[]{Integer.valueOf(3), Integer.valueOf(4)});
    assertEquals(4, result.length);
    assertEquals(Integer.class, result.getClass().getComponentType());
    assertEquals(Integer.valueOf(1), result[0]);
    assertEquals(Integer.valueOf(2), result[1]);
    assertEquals(Integer.valueOf(3), result[2]);
    assertEquals(Integer.valueOf(4), result[3]);
}