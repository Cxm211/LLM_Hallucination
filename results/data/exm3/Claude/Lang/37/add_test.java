// org/apache/commons/lang3/ArrayUtilsAddTest.java
public void testAddAllCompatibleTypes() {
    Number[] n = ArrayUtils.addAll(new Number[]{Integer.valueOf(1), Double.valueOf(2.5)}, new Integer[]{Integer.valueOf(3)});
    assertEquals(3, n.length);
    assertEquals(Number.class, n.getClass().getComponentType());
    assertEquals(Integer.valueOf(1), n[0]);
    assertEquals(Double.valueOf(2.5), n[1]);
    assertEquals(Integer.valueOf(3), n[2]);
}