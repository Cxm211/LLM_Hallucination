    public void finish() throws IOException {
        writeEOFRecord();
        writeEOFRecord();
    }

    public void putArchiveEntry(ArchiveEntry archiveEntry) throws IOException {
        TarArchiveEntry entry = (TarArchiveEntry) archiveEntry;
        if (entry.getName().length() >= TarConstants.NAMELEN) {

            if (longFileMode == LONGFILE_GNU) {
                // create a TarEntry for the LongLink, the contents
                // of which are the entry's name
                TarArchiveEntry longLinkEntry = new TarArchiveEntry(TarConstants.GNU_LONGLINK,
                                                                    TarConstants.LF_GNUTYPE_LONGNAME);

                final byte[] nameBytes = entry.getName().getBytes(); // TODO is it correct to use the default charset here?
                longLinkEntry.setSize(nameBytes.length + 1); // +1 for NUL
                putArchiveEntry(longLinkEntry);
                write(nameBytes);
                write(0); // NUL terminator
                closeArchiveEntry();
            } else if (longFileMode != LONGFILE_TRUNCATE) {
                throw new RuntimeException("file name '" + entry.getName()
                                           + "' is too long ( > "
                                           + TarConstants.NAMELEN + " bytes)");
            }
        }

        entry.writeEntryHeader(recordBuf);
        buffer.writeRecord(recordBuf);

        currBytes = 0;

        if (entry.isDirectory()) {
            currSize = 0;
        } else {
            currSize = entry.getSize();
        }
        currName = entry.getName();
    }

    public void closeArchiveEntry() throws IOException {
        if (assemLen > 0) {
            for (int i = assemLen; i < assemBuf.length; ++i) {
                assemBuf[i] = 0;
            }

            buffer.writeRecord(assemBuf);

            currBytes += assemLen;
            assemLen = 0;
        }

        if (currBytes < currSize) {
            throw new IOException("entry '" + currName + "' closed at '"
                                  + currBytes
                                  + "' before the '" + currSize
                                  + "' bytes specified in the header were written");
        }
    }

// trigger testcase
public void testFinish() throws Exception {
        OutputStream out1 = new ByteArrayOutputStream();
        
        ArchiveOutputStream aos1 = factory.createArchiveOutputStream("zip", out1);
        aos1.putArchiveEntry(new ZipArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException io) {
            // Exception expected
        }
        
        aos1 = factory.createArchiveOutputStream("jar", out1);
        aos1.putArchiveEntry(new JarArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException io) {
            // Exception expected
        }
        
        aos1 = factory.createArchiveOutputStream("ar", out1);
        aos1.putArchiveEntry(new ArArchiveEntry("dummy", 100));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException io) {
            // Exception expected
        }
        
        aos1 = factory.createArchiveOutputStream("cpio", out1);
        aos1.putArchiveEntry(new CpioArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException io) {
            // Exception expected
        }
        
        aos1 = factory.createArchiveOutputStream("tar", out1);
        aos1.putArchiveEntry(new TarArchiveEntry("dummy"));
        try {
            aos1.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException io) {
            // Exception expected
        }
    }
