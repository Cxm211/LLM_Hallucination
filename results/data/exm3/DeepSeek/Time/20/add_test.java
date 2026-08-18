// org/joda/time/format/TestDateTimeFormatterBuilder.java
public void test_printParseZoneEtcGMT() {
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        // Test Etc/GMT+0
        DateTime dt1 = new DateTime(2007, 3, 4, 12, 30, 0, DateTimeZone.forID("Etc/GMT+0"));
        assertEquals("2007-03-04 12:30 Etc/GMT+0", f.print(dt1));
        assertEquals(dt1, f.parseDateTime("2007-03-04 12:30 Etc/GMT+0"));
        
        // Test Etc/GMT-5
        DateTime dt2 = new DateTime(2007, 3, 4, 12, 30, 0, DateTimeZone.forID("Etc/GMT-5"));
        assertEquals("2007-03-04 12:30 Etc/GMT-5", f.print(dt2));
        assertEquals(dt2, f.parseDateTime("2007-03-04 12:30 Etc/GMT-5"));
    }
