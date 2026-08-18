// org/apache/commons/compress/archivers/tar/TarArchiveInputStreamTest.java
@Test
    public void testParsePaxHeadersWithBlankLineAndDelete() throws Exception {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        java.lang.reflect.Method method = TarArchiveInputStream.class.getDeclaredMethod("parsePaxHeaders", java.io.InputStream.class);
        method.setAccessible(true);
        java.lang.reflect.Field globalPaxHeadersField = TarArchiveInputStream.class.getDeclaredField("globalPaxHeaders");
        globalPaxHeadersField.setAccessible(true);
        java.util.Map<String, String> global = new java.util.HashMap<>();
        global.put("comment", "dummy");
        globalPaxHeadersField.set(tais, global);
        byte[] data = "\n12 comment=\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        java.util.Map<String, String> result = (java.util.Map<String, String>) method.invoke(tais, new java.io.ByteArrayInputStream(data));
        assertFalse(result.containsKey("comment"));
    }
