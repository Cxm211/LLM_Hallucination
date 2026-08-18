// org/apache/commons/lang/enums/ValuedEnumTest.java
public void testCompareTo_otherEnumTypeDifferentValue() {
    try {
        ValuedColorEnum.RED.compareTo(ValuedLanguageEnum.FRENCH);
        fail("Expected ClassCastException when comparing different enum types");
    } catch (ClassCastException ex) {
        // expected
    }
}
