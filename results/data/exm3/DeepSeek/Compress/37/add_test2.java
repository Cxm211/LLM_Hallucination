// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
    public void testParsePaxHeadersValueWithNewlineAndBlankLine() throws Exception {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        java.lang.reflect.Method method = TarArchiveInputStream.class.getDeclaredMethod("parsePaxHeaders", java.io.InputStream.class);
        method.setAccessible(true);
        java.lang.reflect.Field globalPaxHeadersField = TarArchiveInputStream.class.getDeclaredField("globalPaxHeaders");
        globalPaxHeadersField.setAccessible(true);
        globalPaxHeadersField.set(tais, new java.util.HashMap<String, String>());
        byte[] data = "\n15 foo=bar\nbaz\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        java.util.Map<String, String> result = (java.util.Map<String, String>) method.invoke(tais, new java.io.ByteArrayInputStream(data));
        assertEquals("bar\nbaz", result.get("foo"));
    }
