// org/apache/commons/lang/enums/ValuedEnumTest.java
public void testCompareTo_null() {
    try {
        ValuedColorEnum.BLUE.compareTo(null);
        fail();
    } catch (NullPointerException ex) {
        // expected
    }
}