// org/apache/commons/lang/time/DateUtilsTest.java
public void testRoundSemiMonth() throws Exception {
        TimeZone MST_MDT = TimeZone.getTimeZone("MST7MDT");
        TimeZone.setDefault(MST_MDT);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
        format.setTimeZone(MST_MDT);

        Date jan1 = new Date(1104566400000L); // 2005-01-01 00:00:00.000 MST
        Date jan8 = new Date(1105171200000L); // 2005-01-08 00:00:00.000 MST
        Date jan9 = new Date(1105257600000L); // 2005-01-09 00:00:00.000 MST
        Date jan16 = new Date(1105862400000L); // 2005-01-16 00:00:00.000 MST
        Date jan17 = new Date(1105948800000L); // 2005-01-17 00:00:00.000 MST
        Date jan23 = new Date(1106467200000L); // 2005-01-23 00:00:00.000 MST
        Date jan24 = new Date(1106553600000L); // 2005-01-24 00:00:00.000 MST
        Date feb1 = new Date(1107244800000L); // 2005-02-01 00:00:00.000 MST

        assertEquals("Round up from day 1", jan16, DateUtils.round(jan1, DateUtils.SEMI_MONTH));
        assertEquals("Round down from day 8", jan1, DateUtils.round(jan8, DateUtils.SEMI_MONTH));
        assertEquals("Round up from day 9", jan16, DateUtils.round(jan9, DateUtils.SEMI_MONTH));
        assertEquals("Round down from day 23", jan16, DateUtils.round(jan23, DateUtils.SEMI_MONTH));
        assertEquals("Round up from day 24", feb1, DateUtils.round(jan24, DateUtils.SEMI_MONTH));

        assertEquals("Truncate day 17", jan16, DateUtils.truncate(jan17, DateUtils.SEMI_MONTH));

        TimeZone.setDefault(defaultZone);
    }