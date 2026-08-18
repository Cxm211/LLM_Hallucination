// org/joda/time/TestPartial_Constructors.java
public void testConstructorAdditionalScenarios() {
    // Test duplicate types with both range duration types null
    DateTimeFieldType[] types1 = new DateTimeFieldType[] {
        DateTimeFieldType.year(),
        DateTimeFieldType.year()
    };
    int[] values1 = new int[] {1, 2};
    try {
        new Partial(types1, values1);
        fail();
    } catch (IllegalArgumentException ex) {
        assertMessageContains(ex, "must not contain duplicate");
    }

    // Test compare == 0 with last range not null and loop range null
    DateTimeFieldType[] types2 = new DateTimeFieldType[] {
        DateTimeFieldType.yearOfEra(),
        DateTimeFieldType.year()
    };
    int[] values2 = new int[] {1, 1};
    try {
        new Partial(types2, values2);
        fail();
    } catch (IllegalArgumentException ex) {
        assertMessageContains(ex, "must be in order largest-smallest");
    }

    // Test compare == 0 with both range not null and range compare < 0
    DateTimeFieldType[] types3 = new DateTimeFieldType[] {
        DateTimeFieldType.dayOfMonth(),
        DateTimeFieldType.dayOfYear()
    };
    int[] values3 = new int[] {1, 1};
    try {
        new Partial(types3, values3);
        fail();
    } catch (IllegalArgumentException ex) {
        assertMessageContains(ex, "must be in order largest-smallest");
    }
}
