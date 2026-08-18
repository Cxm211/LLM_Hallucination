// org/joda/time/TestPartial_Basics.java
public void testWithNewFieldValidationFailure() {
    Partial test = createHourMinPartial();
    try {
        test.with(DateTimeFieldType.secondOfMinute(), 65);
        fail();
    } catch (IllegalArgumentException ex) {}
    check(test, 10, 20);
}