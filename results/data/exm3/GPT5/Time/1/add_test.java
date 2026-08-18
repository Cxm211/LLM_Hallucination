// org/joda/time/TestPartial_Constructors.java::testConstructorEx_DuplicateTypes
public void testConstructorEx_DuplicateTypes() throws Throwable {
        int[] values = new int[] {1, 2, 3};
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.monthOfYear() };
        try {
            new Partial(types, values);
            fail();
        } catch (IllegalArgumentException ex) {
            assertMessageContains(ex, "must not contain duplicate");
        }
    }