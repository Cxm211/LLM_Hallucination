// org/joda/time/format/TestPeriodFormatterBuilder.java
public void testSeparatorWithPrinterOnly() {
    PeriodFormatter pfmt = new PeriodFormatterBuilder()
        .appendYears()
        .appendSuffix("Y")
        .appendSeparatorIfFieldsAfter("T")
        .appendHours()
        .appendSuffix("H")
        .toPrinter();
    String result = pfmt.print(new Period(1, 0, 0, 0, 2, 0, 0, 0));
    assertTrue(result.contains("Y"));
    assertTrue(result.contains("H"));
}