// org/apache/commons/collections/map/TestCaseInsensitiveMap.java
public void testNonStringKeyWithLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            CaseInsensitiveMap map = new CaseInsensitiveMap();
            Object key = new Object() {
                @Override
                public String toString() {
                    return "I";
                }
            };
            map.put(key, "value");
            assertNull("Map should not find value for string I when key is object with I", map.get("I"));
        } finally {
            Locale.setDefault(original);
        }
    }
