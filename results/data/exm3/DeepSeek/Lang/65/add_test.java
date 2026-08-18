// org/apache/commons/lang/time/DateUtilsTest.java
public void testRoundUpTimeFields() throws Exception {
    TimeZone defaultZone = TimeZone.getDefault();
    TimeZone utc = TimeZone.getTimeZone("UTC");
    TimeZone.setDefault(utc);
    
    Calendar cal = Calendar.getInstance();
    cal.set(2023, Calendar.JANUARY, 1, 12, 30, 30);
    cal.set(Calendar.MILLISECOND, 500);
    Date date = cal.getTime();
    
    Date roundedMinute = DateUtils.round(date, Calendar.MINUTE);
    Calendar calMinute = Calendar.getInstance();
    calMinute.setTime(roundedMinute);
    assertEquals(12, calMinute.get(Calendar.HOUR_OF_DAY));
    assertEquals(31, calMinute.get(Calendar.MINUTE));
    assertEquals(0, calMinute.get(Calendar.SECOND));
    assertEquals(0, calMinute.get(Calendar.MILLISECOND));
    
    Date roundedHour = DateUtils.round(date, Calendar.HOUR_OF_DAY);
    Calendar calHour = Calendar.getInstance();
    calHour.setTime(roundedHour);
    assertEquals(13, calHour.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, calHour.get(Calendar.MINUTE));
    assertEquals(0, calHour.get(Calendar.SECOND));
    assertEquals(0, calHour.get(Calendar.MILLISECOND));
    
    Date roundedDate = DateUtils.round(date, Calendar.DATE);
    Calendar calDate = Calendar.getInstance();
    calDate.setTime(roundedDate);
    assertEquals(2023, calDate.get(Calendar.YEAR));
    assertEquals(Calendar.JANUARY, calDate.get(Calendar.MONTH));
    assertEquals(2, calDate.get(Calendar.DATE));
    assertEquals(0, calDate.get(Calendar.HOUR_OF_DAY));
    assertEquals(0, calDate.get(Calendar.MINUTE));
    assertEquals(0, calDate.get(Calendar.SECOND));
    assertEquals(0, calDate.get(Calendar.MILLISECOND));
    
    TimeZone.setDefault(defaultZone);
}
