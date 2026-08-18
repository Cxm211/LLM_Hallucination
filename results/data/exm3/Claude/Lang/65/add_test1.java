// org/apache/commons/lang/time/DateUtilsTest.java
public void testRoundAmPm() throws Exception {
        TimeZone MST_MDT = TimeZone.getTimeZone("MST7MDT");
        TimeZone.setDefault(MST_MDT);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
        format.setTimeZone(MST_MDT);

        Date jan1_00 = new Date(1104566400000L); // 2005-01-01 00:00:00.000 MST
        Date jan1_06 = new Date(1104588000000L); // 2005-01-01 06:00:00.000 MST
        Date jan1_07 = new Date(1104591600000L); // 2005-01-01 07:00:00.000 MST
        Date jan1_12 = new Date(1104609600000L); // 2005-01-01 12:00:00.000 MST
        Date jan1_18 = new Date(1104631200000L); // 2005-01-01 18:00:00.000 MST
        Date jan1_19 = new Date(1104634800000L); // 2005-01-01 19:00:00.000 MST
        Date jan2_00 = new Date(1104652800000L); // 2005-01-02 00:00:00.000 MST

        assertEquals("Round down from hour 6", jan1_00, DateUtils.round(jan1_06, Calendar.AM_PM));
        assertEquals("Round up from hour 7", jan1_12, DateUtils.round(jan1_07, Calendar.AM_PM));
        assertEquals("Round down from hour 18", jan1_12, DateUtils.round(jan1_18, Calendar.AM_PM));
        assertEquals("Round up from hour 19", jan2_00, DateUtils.round(jan1_19, Calendar.AM_PM));

        assertEquals("Truncate hour 7", jan1_00, DateUtils.truncate(jan1_07, Calendar.AM_PM));
        assertEquals("Truncate hour 19", jan1_12, DateUtils.truncate(jan1_19, Calendar.AM_PM));

        TimeZone.setDefault(defaultZone);
    }