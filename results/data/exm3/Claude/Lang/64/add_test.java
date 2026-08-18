// org/apache/commons/lang/enums/ValuedEnumTest.java
public void testCompareTo_sameEnumType() {
    assertTrue(ValuedColorEnum.BLUE.compareTo(ValuedColorEnum.RED) != 0);
    assertEquals(0, ValuedColorEnum.BLUE.compareTo(ValuedColorEnum.BLUE));
}