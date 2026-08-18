// buggy function
        public int parseInto(DateTimeParserBucket bucket, String text, int position) {
            String str = text.substring(position);
            for (String id : ALL_IDS) {
                if (str.startsWith(id)) {
                    bucket.setZone(DateTimeZone.forID(id));
                    return position + id.length();
                }
            }
            return ~position;
        }

// trigger testcase
// org/joda/time/format/TestDateTimeFormatterBuilder.java::test_printParseZoneDawsonCreek
public void test_printParseZoneDawsonCreek() {  // clashes with shorter Dawson
        DateTimeFormatterBuilder bld = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm ").appendTimeZoneId();
        DateTimeFormatter f = bld.toFormatter();
        
        DateTime dt = new DateTime(2007, 3, 4, 12, 30, 0, DateTimeZone.forID("America/Dawson_Creek"));
        assertEquals("2007-03-04 12:30 America/Dawson_Creek", f.print(dt));
        assertEquals(dt, f.parseDateTime("2007-03-04 12:30 America/Dawson_Creek"));
    }
