// org/joda/time/format/TestPeriodFormatterBuilder.java::testLeadingSeparatorTimeOnly
public void testLeadingSeparatorTimeOnly() {
        PeriodFormatter pfmt = new PeriodFormatterBuilder()
            .appendSeparatorIfFieldsAfter("T")
            .appendSecondsWithOptionalMillis()
            .appendSuffix("S")
            .toFormatter();
        pfmt.parsePeriod("T5S");
    }