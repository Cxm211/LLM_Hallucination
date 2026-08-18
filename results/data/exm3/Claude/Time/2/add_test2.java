// org/joda/time/TestPartial_Basics.java
public void testConstructor_withTwoFieldsNoRange() {
    DateTimeFieldType[] types = new DateTimeFieldType[] {
        DateTimeFieldType.era(),
        DateTimeFieldType.year()
    };
    int[] values = new int[] {1, 2000};
    Partial test = new Partial(types, values);
    assertEquals(2, test.size());
    assertEquals(0, test.indexOf(DateTimeFieldType.era()));
    assertEquals(1, test.indexOf(DateTimeFieldType.year()));
}