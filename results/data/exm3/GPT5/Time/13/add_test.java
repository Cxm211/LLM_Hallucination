// org/joda/time/format/TestISOPeriodFormat.java::testFormatStandard_negative
public void testFormatStandard_negative_zeroSecondsMillis() {
        Period p = Period.millis(-1);
        assertEquals("PT-0.001S", ISOPeriodFormat.standard().print(p));
    }