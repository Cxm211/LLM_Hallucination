// org/apache/commons/lang/time/DateUtilsTest.java::testRoundUpLang59
public void testRoundUpLang59() throws Exception {
        TimeZone MST_MDT = TimeZone.getTimeZone("MST7MDT");
        TimeZone.setDefault(MST_MDT);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
        format.setTimeZone(MST_MDT);

        Date oct31_01MDT = new Date(1099206000000L);
        Date oct31_01_02MDT = new Date(oct31_01MDT.getTime() + 120000L);
        Date oct31_01_02_03MDT = new Date(oct31_01_02MDT.getTime() + 3000L);

        // Round SECOND up (millis >= 500)
        Date d_sec_up = new Date(oct31_01_02_03MDT.getTime() + 900L); // 01:02:03.900 -> 01:02:04.000
        assertEquals("Round up SECOND", new Date(oct31_01_02_03MDT.getTime() + 1000L), DateUtils.round(d_sec_up, Calendar.SECOND));

        // Round MINUTE up (seconds > 30 or ==30 with millis>=500)
        Date d_min_up = new Date(oct31_01MDT.getTime() + (2 * 60 * 1000L) + 30500L); // 01:02:30.500 -> 01:03:00.000
        Date expected_min_up = new Date(oct31_01MDT.getTime() + (3 * 60 * 1000L));
        assertEquals("Round up MINUTE", expected_min_up, DateUtils.round(d_min_up, Calendar.MINUTE));

        TimeZone.setDefault(defaultZone);
    }