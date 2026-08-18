// buggy function
    public void close() throws IOException {
        if (!this.closed) {
            this.finish();
            out.close();
            this.closed = true;
        }
    }

    public void close() throws IOException {
        if (!closed) {
            finish();
            buffer.close();
            out.close();
            closed = true;
        }
    }

    public void close() throws IOException {
        finish();
        if (raf != null) {
            raf.close();
        }
        if (out != null) {
            out.close();
        }
    }

    public ChangeSetResults perform(ArchiveInputStream in, ArchiveOutputStream out)
            throws IOException {
        ChangeSetResults results = new ChangeSetResults();
        
        Set workingSet = new LinkedHashSet(changes);
        
        for (Iterator it = workingSet.iterator(); it.hasNext();) {
            Change change = (Change) it.next();

            if (change.type() == Change.TYPE_ADD && change.isReplaceMode()) {
                copyStream(change.getInput(), out, change.getEntry());
                it.remove();
                results.addedFromChangeSet(change.getEntry().getName());
            }
        }

        ArchiveEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            boolean copy = true;

            for (Iterator it = workingSet.iterator(); it.hasNext();) {
                Change change = (Change) it.next();

                final int type = change.type();
                final String name = entry.getName();
                if (type == Change.TYPE_DELETE && name != null) {
                    if (name.equals(change.targetFile())) {
                        copy = false;
                        it.remove();
                        results.deleted(name);
                        break;
                    }
                } else if(type == Change.TYPE_DELETE_DIR && name != null) {
                    if (name.startsWith(change.targetFile() + "/")) {
                        copy = false;
                        results.deleted(name);
                        break;
                    }
                }
            }

            if (copy) {
                if (!isDeletedLater(workingSet, entry) && !results.hasBeenAdded(entry.getName())) {
                    copyStream(in, out, entry);
                    results.addedFromStream(entry.getName());
                }
            }
        }
        
        // Adds files which hasn't been added from the original and do not have replace mode on
        for (Iterator it = workingSet.iterator(); it.hasNext();) {
            Change change = (Change) it.next();

            if (change.type() == Change.TYPE_ADD && 
                !change.isReplaceMode() && 
                !results.hasBeenAdded(change.getEntry().getName())) {
                copyStream(change.getInput(), out, change.getEntry());
                it.remove();
                results.addedFromChangeSet(change.getEntry().getName());
            }
        }
        return results;
    }

// trigger testcase
// org/apache/commons/compress/archivers/jar/JarArchiveOutputStreamTest.java::testJarMarker
public void testJarMarker() throws IOException {
        File testArchive = File.createTempFile("jar-aostest", ".jar");
        JarArchiveOutputStream out = null;
        ZipFile zf = null;
        try {

            out = new JarArchiveOutputStream(new FileOutputStream(testArchive));
            out.putArchiveEntry(new ZipArchiveEntry("foo/"));
            out.closeArchiveEntry();
            out.putArchiveEntry(new ZipArchiveEntry("bar/"));
            out.closeArchiveEntry();
            out.finish();
            out.close();
            out = null;

            zf = new ZipFile(testArchive);
            ZipArchiveEntry ze = zf.getEntry("foo/");
            assertNotNull(ze);
            ZipExtraField[] fes = ze.getExtraFields();
            assertEquals(1, fes.length);
            assertTrue(fes[0] instanceof JarMarker);

            ze = zf.getEntry("bar/");
            assertNotNull(ze);
            fes = ze.getExtraFields();
            assertEquals(0, fes.length);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) { /* swallow */ }
            }
            ZipFile.closeQuietly(zf);
            if (testArchive.exists()) {
                testArchive.delete();
            }
        }
    }

// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testASCIIFileRoundtripExplicitUnicodeExtra
public void testASCIIFileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(US_ASCII, false, true);
    }

// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testASCIIFileRoundtripImplicitUnicodeExtra
public void testASCIIFileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(US_ASCII, false, false);
    }

// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testCP437FileRoundtripExplicitUnicodeExtra
public void testCP437FileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CP437, false, true);
    }

// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testCP437FileRoundtripImplicitUnicodeExtra
public void testCP437FileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CP437, false, false);
    }

// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testUtf8FileRoundtripExplicitUnicodeExtra
public void testUtf8FileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, true, true);
    }

// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testUtf8FileRoundtripImplicitUnicodeExtra
public void testUtf8FileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, true, false);
    }

// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testUtf8FileRoundtripNoEFSExplicitUnicodeExtra
public void testUtf8FileRoundtripNoEFSExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, false, true);
    }

// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testUtf8FileRoundtripNoEFSImplicitUnicodeExtra
public void testUtf8FileRoundtripNoEFSImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, false, false);
    }

// org/apache/commons/compress/archivers/zip/UTF8ZipFilesTest.java::testZipArchiveInputStreamReadsUnicodeFields
public void testZipArchiveInputStreamReadsUnicodeFields()
        throws IOException {
        File file = File.createTempFile("unicode-test", ".zip");
        ZipFile zf = null;
        try {
            createTestFile(file, US_ASCII, false, true);
            zf = new ZipFile(file, US_ASCII, true);
            assertNotNull(zf.getEntry(ASCII_TXT));
            assertNotNull(zf.getEntry(EURO_FOR_DOLLAR_TXT));
            assertNotNull(zf.getEntry(OIL_BARREL_TXT));
        } finally {
            ZipFile.closeQuietly(zf);
            if (file.exists()) {
                file.delete();
            }
        }
    }
