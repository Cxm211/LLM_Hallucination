// org/joda/time/TestDateTimeZone.java
public void testForID_String_old_additional1() {
    TimeZone juZoneCET = TimeZone.getTimeZone("CET");
    DateTimeZone zoneCET = DateTimeZone.forTimeZone(juZoneCET);
    assertEquals("CET", zoneCET.getID());
}