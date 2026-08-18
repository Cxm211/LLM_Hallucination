// org/apache/commons/lang3/StringUtilsTest.java
@Test
    public void testTranslateSurrogatePairFollowedByChar() throws IOException {
        java.util.Map<CharSequence, CharSequence> map = new java.util.HashMap<>();
        map.put("\uD800\uDC00", "X");
        map.put("a", "Y");
        org.apache.commons.lang3.text.translate.CharSequenceTranslator translator = 
            new org.apache.commons.lang3.text.translate.LookupTranslator(map);
        java.io.StringWriter writer = new java.io.StringWriter();
        translator.translate("\uD800\uDC00a", writer);
        assertEquals("XY", writer.toString());
    }
