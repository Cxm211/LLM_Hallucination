// org/joda/time/TestPartial_Basics.java
public void testWith_unsupportedField() {
    Partial test = new Partial(DateTimeFieldType.year(), 2000);
    Partial result = test.with(DateTimeFieldType.dayOfMonth(), 15);
    assertEquals(2, result.size());
    assertEquals(0, result.indexOf(DateTimeFieldType.year()));
    assertEquals(1, result.indexOf(DateTimeFieldType.dayOfMonth()));
}