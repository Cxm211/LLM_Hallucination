// com/fasterxml/jackson/databind/TestStdDateFormat.java::testTimeZoneNull
public void testTimeZoneNull() throws Exception {
        StdDateFormat f = StdDateFormat.instance.clone();
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        // Should not throw NPE when setting null timezone
        f.setTimeZone(null);
        // Parsing should still respect leniency
        f.setLenient(false);
        try {
            f.parse("2015-11-32");
            fail("Should not pass");
        } catch (ParseException e) {
            // expected
        }
    }