// com/fasterxml/jackson/databind/TestStdDateFormat.java
public void testLenientWithTimeZoneChange() throws Exception
{
    StdDateFormat f = StdDateFormat.instance;
    f = f.clone();
    f.setLenient(false);
    assertFalse(f.isLenient());
    
    // Change timezone
    f.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
    
    // Verify leniency is preserved after timezone change
    assertFalse(f.isLenient());
    
    // Verify non-lenient behavior still works
    try {
        f.parse("2015-11-32");
        fail("Should not pass");
    } catch (ParseException e) {
        verifyException(e, "can not parse date");
    }
}