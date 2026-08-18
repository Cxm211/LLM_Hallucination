// org/joda/time/TestPartial_Basics.java
public void testWithNewFieldInvalidRangeType() {
    Partial test = createHourMinPartial();
    try {
        test.with(DateTimeFieldType.hourOfHalfday(), 5);
        fail();
    } catch (IllegalArgumentException ex) {}
    check(test, 10, 20);
}