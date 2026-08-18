// org/apache/commons/collections/TestExtendedProperties.java
public void testIncludeWithISO88591() throws IOException {
    ExtendedProperties ep = new ExtendedProperties();
    File includedFile = File.createTempFile("included", ".properties");
    includedFile.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(includedFile);
         OutputStreamWriter osw = new OutputStreamWriter(fos, "ISO-8859-1")) {
        osw.write("includedKey=\u00e9");
    }
    File mainFile = File.createTempFile("main", ".properties");
    mainFile.deleteOnExit();
    try (FileOutputStream fos = new FileOutputStream(mainFile);
         OutputStreamWriter osw = new OutputStreamWriter(fos, "ISO-8859-1")) {
        osw.write("include=" + includedFile.getAbsolutePath());
    }
    try (FileInputStream fis = new FileInputStream(mainFile)) {
        ep.load(fis, null);
    }
    assertEquals("\u00e9", ep.getProperty("includedKey"));
}
