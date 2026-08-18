// org/joda/time/TestPartial_Basics.java
public void testWith_mixedSupportedAndUnsupportedFields() {
    Partial test = new Partial(DateTimeFieldType.year(), 2000);
    Partial result = test.with(DateTimeFieldType.monthOfYear(), 6);
    result = result.with(DateTimeFieldType.era(), 1);
    assertEquals(3, result.size());
    assertEquals(0, result.indexOf(DateTimeFieldType.era()));
    assertEquals(1, result.indexOf(DateTimeFieldType.year()));
    assertEquals(2, result.indexOf(DateTimeFieldType.monthOfYear()));
}