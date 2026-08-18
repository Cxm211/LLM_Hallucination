// org/apache/commons/collections/map/TestCaseInsensitiveMap.java::testLocaleIndependence
public void testLocaleIndependenceNonStringKey() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            CaseInsensitiveMap map = new CaseInsensitiveMap();
            map.put(new StringBuilder("TITLE"), "value");
            assertEquals("value", map.get("title"));
        } finally {
            Locale.setDefault(orig);
        }
    }