// org/apache/commons/lang3/StringUtilsTest.java
@Test
    public void testTranslateSurrogatePairConsumed() throws IOException {
        java.util.Map<CharSequence, CharSequence> map = new java.util.HashMap<>();
        map.put("\uD800\uDC00", "XYZ");
        org.apache.commons.lang3.text.translate.CharSequenceTranslator translator = 
            new org.apache.commons.lang3.text.translate.LookupTranslator(map);
        java.io.StringWriter writer = new java.io.StringWriter();
        translator.translate("\uD800\uDC00", writer);
        assertEquals("XYZ", writer.toString());
    }
