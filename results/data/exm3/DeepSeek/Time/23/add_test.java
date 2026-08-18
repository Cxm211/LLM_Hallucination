// org/joda/time/TestDateTimeZone.java
public void testGetConvertedId_ChangedKeys() throws Exception {
        Field field = DateTimeZone.class.getDeclaredField("cZoneIdConversion");
        field.setAccessible(true);
        Object original = field.get(null);
        try {
            field.set(null, null);
            Map<String, String> expected = new HashMap<String, String>();
            expected.put("WET", "WET");
            expected.put("EET", "EET");
            expected.put("MET", "CET");
            expected.put("ECT", "CET");
            expected.put("IET", "America/Indiana/Indianapolis");
            expected.put("AGT", "America/Argentina/Buenos_Aires");
            expected.put("IST", "Asia/Kolkata");
            expected.put("VST", "Asia/Ho_Chi_Minh");
            for (Map.Entry<String, String> entry : expected.entrySet()) {
                String key = entry.getKey();
                String expectedId = entry.getValue();
                TimeZone juZone = TimeZone.getTimeZone(key);
                DateTimeZone zone = DateTimeZone.forTimeZone(juZone);
                assertEquals("Key: " + key, expectedId, zone.getID());
            }
        } finally {
            field.set(null, original);
        }
    }
