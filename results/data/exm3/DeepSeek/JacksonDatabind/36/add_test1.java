// com/fasterxml/jackson/databind/TestStdDateFormat.java
public void testCloneWithNonDefaultLocaleAndLeniency() throws Exception {
        StdDateFormat f = StdDateFormat.instance;
        Locale nonDefault = Locale.FRENCH;
        StdDateFormat f2 = f.withLocale(nonDefault);
        f2.setLenient(false);
        StdDateFormat f3 = f2.clone();
        assertFalse(f3.isLenient());
        try {
            f3.parse("2015-11-32");
            fail("Should not pass");
        } catch (ParseException e) {
        }
        Date dt = f3.parse("2015-11-30");
        assertNotNull(dt);
    }
