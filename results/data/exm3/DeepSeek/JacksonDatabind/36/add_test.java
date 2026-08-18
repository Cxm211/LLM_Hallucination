// com/fasterxml/jackson/databind/TestStdDateFormat.java
public void testSetTimeZoneWithNull() throws Exception {
        StdDateFormat f = StdDateFormat.instance;
        TimeZone tz = TimeZone.getTimeZone("UTC");
        f.setTimeZone(tz);
        f.setTimeZone(null);
        f.setTimeZone(null);
    }
