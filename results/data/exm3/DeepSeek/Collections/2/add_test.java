// org/apache/commons/collections/TestExtendedProperties.java
public void testLoadISO88591Encoding() throws IOException {
    ExtendedProperties ep = new ExtendedProperties();
    File tempFile = File.createTempFile("test", ".properties");
    tempFile.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(tempFile);
         OutputStreamWriter osw = new OutputStreamWriter(fos, "ISO-8859-1")) {
        osw.write("key=\u00e9");
    }
    try (FileInputStream fis = new FileInputStream(tempFile)) {
        ep.load(fis, null);
    }
    assertEquals("\u00e9", ep.getProperty("key"));
}
