// org/joda/time/TestDateTimeZone.java
public void testForID_String_old_additional2() {
    Map<String, String> additionalMap = new LinkedHashMap<String, String>();
    additionalMap.put("WET", "WET");
    additionalMap.put("EET", "EET");
    additionalMap.put("CET", "CET");
    for (String key : additionalMap.keySet()) {
        String value = additionalMap.get(key);
        TimeZone juZone = TimeZone.getTimeZone(key);
        DateTimeZone zone = DateTimeZone.forTimeZone(juZone);
        assertEquals(value, zone.getID());
    }
}