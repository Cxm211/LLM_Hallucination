// org/joda/time/format/TestPeriodFormatterBuilder.java
public void testPrinterOnlyWithSeparator() {
    PeriodPrinter printer = new PeriodFormatterBuilder()
        .appendSeparatorIfFieldsAfter("T")
        .appendHours()
        .appendSuffix("H")
        .appendMinutes()
        .appendSuffix("M")
        .toPrinter();
    PeriodFormatter fmt = new PeriodFormatter(printer, null);
    Period p = new Period(0, 0, 0, 0, 5, 30, 0, 0);
    assertEquals("T5H30M", fmt.print(p));
}
