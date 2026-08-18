// org/apache/commons/lang/time/DateUtilsTest.java
public void testTruncateHourWithDST() throws Exception {
    TimeZone defaultZone = TimeZone.getDefault();
    TimeZone MST_MDT = TimeZone.getTimeZone("MST7MDT");
    TimeZone.setDefault(MST_MDT);
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
    format.setTimeZone(MST_MDT);
    
    Calendar cal = Calendar.getInstance();
    cal.set(2004, Calendar.OCTOBER, 31, 1, 30, 0);
    cal.set(Calendar.MILLISECOND, 0);
    Date date = cal.getTime();
    
    Date truncated = DateUtils.truncate(date, Calendar.HOUR_OF_DAY);
    assertEquals("Truncate to hour should give 01:00:00.000 MDT", "2004-10-31 01:00:00.000 MDT", format.format(truncated));
    
    TimeZone.setDefault(defaultZone);
}
