// org/joda/time/TestPartial_Constructors.java
public void testConstructorEx7_TypeArray_intArray_AdditionalCase2() throws Throwable {
    int[] values = new int[] {1, 1};
    DateTimeFieldType[] types = new DateTimeFieldType[] {
        DateTimeFieldType.dayOfYear(), DateTimeFieldType.year() };
    try {
        new Partial(types, values);
        fail();
    } catch (IllegalArgumentException ex) {
        assertMessageContains(ex, "must be in order", "largest-smallest");
    }
}