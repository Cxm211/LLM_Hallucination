// com/fasterxml/jackson/databind/TestStdDateFormat.java
public void testLenientWithLocaleChange() throws Exception
{
    StdDateFormat f = StdDateFormat.instance;
    f = f.clone();
    f.setLenient(false);
    assertFalse(f.isLenient());
    
    // Trigger a format clone with non-default locale
    f = f.withLocale(Locale.FRENCH);
    
    // Verify leniency is preserved after locale change
    assertFalse(f.isLenient());
    
    // Verify non-lenient behavior still works
    try {
        f.parse("2015-11-32");
        fail("Should not pass");
    } catch (ParseException e) {
        verifyException(e, "can not parse date");
    }
}