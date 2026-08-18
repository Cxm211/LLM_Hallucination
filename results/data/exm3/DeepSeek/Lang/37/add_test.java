// org/apache/commons/lang3/ArrayUtilsAddTest.java
public void testAddAllLongAndNumber() {
    Long[] longs = new Long[]{Long.valueOf(1)};
    Number[] numbers = new Number[]{Integer.valueOf(2)};
    Number[] result = ArrayUtils.addAll(longs, numbers);
    assertEquals(2, result.length);
    assertEquals(Number.class, result.getClass().getComponentType());
    assertEquals(Long.valueOf(1), result[0]);
    assertEquals(Integer.valueOf(2), result[1]);
}
