// org/joda/time/format/TestDateTimeFormatter.java::testParseInto_monthOnly_withDefaultYear_ignored
public void testParseInto_monthOnly_withDefaultYear_ignored() {
        DateTimeFormatter f = DateTimeFormat.forPattern("M").withDefaultYear(2012);
        MutableDateTime result = new MutableDateTime(2004, 1, 9, 12, 20, 30, 0, LONDON);
        assertEquals(1, f.parseInto(result, "5", 0));
        assertEquals(new MutableDateTime(2004, 5, 9, 12, 20, 30, 0, LONDON), result);
    }