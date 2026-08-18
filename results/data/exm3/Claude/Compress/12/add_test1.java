// org/apache/commons/compress/archivers/TarTestCase.java
public void testValidEntryAfterSkip() throws Exception {
    // Create a tar archive with a valid small entry
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
    // Create a simple valid tar header for a file named "test.txt" with 5 bytes
    byte[] header = new byte[512];
    // File name
    System.arraycopy("test.txt".getBytes(), 0, header, 0, 8);
    // File mode (octal 644 = 0000644)
    System.arraycopy("0000644 ".getBytes(), 0, header, 100, 8);
    // Owner ID
    System.arraycopy("0000000 ".getBytes(), 0, header, 108, 8);
    // Group ID
    System.arraycopy("0000000 ".getBytes(), 0, header, 116, 8);
    // File size (5 bytes in octal)
    System.arraycopy("00000000005 ".getBytes(), 0, header, 124, 12);
    // Modification time
    System.arraycopy("00000000000 ".getBytes(), 0, header, 136, 12);
    // Checksum placeholder
    System.arraycopy("        ".getBytes(), 0, header, 148, 8);
    // Type flag (regular file)
    header[156] = '0';
    // Magic
    System.arraycopy("ustar  ".getBytes(), 0, header, 257, 8);
    
    // Calculate checksum
    long checksum = 0;
    for (int i = 0; i < 512; i++) {
        checksum += (header[i] & 0xFF);
    }
    String checksumStr = String.format("%06o\0 ", checksum);
    System.arraycopy(checksumStr.getBytes(), 0, header, 148, 8);
    
    baos.write(header);
    // File content
    byte[] content = "hello".getBytes();
    baos.write(content);
    // Padding to 512 bytes
    baos.write(new byte[512 - content.length]);
    // EOF marker
    baos.write(new byte[1024]);
    
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    final TarArchiveInputStream in = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", bais);
    
    TarArchiveEntry entry = in.getNextTarEntry();
    assertNotNull("Expected valid entry", entry);
    assertEquals("test.txt", entry.getName());
    assertEquals(5, entry.getSize());
    
    // Read and verify content
    byte[] readContent = new byte[5];
    int bytesRead = in.read(readContent);
    assertEquals(5, bytesRead);
    assertEquals("hello", new String(readContent));
    
    // Next entry should be null (EOF)
    assertNull(in.getNextTarEntry());
    in.close();
}