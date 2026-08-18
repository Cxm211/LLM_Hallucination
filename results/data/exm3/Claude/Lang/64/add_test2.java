// org/apache/commons/lang/enums/ValuedEnumTest.java
public void testCompareTo_valueOrdering() {
    int result = ValuedColorEnum.RED.compareTo(ValuedColorEnum.BLUE);
    assertTrue(result != 0);
}