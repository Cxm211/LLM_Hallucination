// org/apache/commons/collections/map/TestCaseInsensitiveMap.java
public void testTurkishDotlessI() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            CaseInsensitiveMap map = new CaseInsensitiveMap();
            map.put("I", "value");
            assertNull("Map should not associate I with dotless i", map.get("\u0131"));
        } finally {
            Locale.setDefault(original);
        }
    }
