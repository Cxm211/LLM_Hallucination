// org/apache/commons/compress/archivers/ArTestCase.java
public void testArReadOddLengthEntry() throws Exception {
    final File output = new File(dir, "odd.ar");
    final File file1 = getFile("test1.xml");
    
    {
        final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("ar", out);
        
        byte[] oddData = new byte[75];
        for (int i = 0; i < oddData.length; i++) {
            oddData[i] = (byte) ('A' + (i % 26));
        }
        
        os.putArchiveEntry(new ArArchiveEntry("odd.txt", oddData.length));
        os.write(oddData);
        os.closeArchiveEntry();
        
        os.putArchiveEntry(new ArArchiveEntry("test1.xml", file1.length()));
        IOUtils.copy(new FileInputStream(file1), os);
        os.closeArchiveEntry();
        
        os.close();
        out.close();
    }
    
    {
        final InputStream is = new FileInputStream(output);
        final ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is));
        
        ArArchiveEntry entry1 = (ArArchiveEntry)ais.getNextEntry();
        assertNotNull(entry1);
        assertEquals("odd.txt", entry1.getName());
        assertEquals(75, entry1.getLength());
        IOUtils.copy(ais, new ByteArrayOutputStream());
        
        ArArchiveEntry entry2 = (ArArchiveEntry)ais.getNextEntry();
        assertNotNull(entry2);
        assertEquals("test1.xml", entry2.getName());
        IOUtils.copy(ais, new ByteArrayOutputStream());
        
        ArArchiveEntry entry3 = (ArArchiveEntry)ais.getNextEntry();
        assertNull(entry3);
        
        ais.close();
        is.close();
    }
}