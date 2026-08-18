// buggy code
    public ArArchiveEntry getNextArEntry() throws IOException {
                    // hit EOF before previous entry was complete
                    // TODO: throw an exception instead?

        if (offset == 0) {
            final byte[] expected = ArArchiveEntry.HEADER.getBytes();
            final byte[] realized = new byte[expected.length]; 
            final int read = read(realized);
            if (read != expected.length) {
                throw new IOException("failed to read header");
            }
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != realized[i]) {
                    throw new IOException("invalid header " + new String(realized));
                }
            }
        }

                // hit eof

        if (input.available() == 0) {
            return null;
        }

        if (offset % 2 != 0) {
            read();
        }
        final byte[] name = new byte[16];
        final byte[] lastmodified = new byte[12];
        final byte[] userid = new byte[6];
        final byte[] groupid = new byte[6];
        final byte[] filemode = new byte[8];
        final byte[] length = new byte[10];

        read(name);
        read(lastmodified);
        read(userid);
        read(groupid);
        read(filemode);
        read(length);

        {
            final byte[] expected = ArArchiveEntry.TRAILER.getBytes();
            final byte[] realized = new byte[expected.length]; 
            final int read = read(realized);
            if (read != expected.length) {
                throw new IOException("failed to read entry header");
            }
            for (int i = 0; i < expected.length; i++) {
                if (expected[i] != realized[i]) {
                    throw new IOException("invalid entry header. not read the content?");
                }
            }
        }

        return new ArArchiveEntry(new String(name).trim(),
                                          Long.parseLong(new String(length)
                                                         .trim()));
    }

    public void close() throws IOException {
        if (!closed) {
            closed = true;
            input.close();
        }
    }

    public int read() throws IOException {
        final int ret = input.read();
        offset += (ret > 0 ? 1 : 0);
        return ret;
    }

    public int read(byte[] b, final int off, final int len) throws IOException {
        int toRead = len;
        final int ret = this.input.read(b, off, toRead);
        offset += (ret > 0 ? ret : 0);
        return ret;
    }

// relevant test
// org.apache.commons.compress.DetectArchiverTestCase::testDetection
    public void testDetection() throws Exception {

        final ArchiveInputStream ar = getStreamFor("bla.ar"); 
        assertNotNull(ar);
        assertTrue(ar instanceof ArArchiveInputStream);

        final ArchiveInputStream tar = getStreamFor("bla.tar");
        assertNotNull(tar);
        assertTrue(tar instanceof TarArchiveInputStream);

        final ArchiveInputStream zip = getStreamFor("bla.zip");
        assertNotNull(zip);
        assertTrue(zip instanceof ZipArchiveInputStream);

        final ArchiveInputStream jar = getStreamFor("bla.jar");
        assertNotNull(jar);
        assertTrue(jar instanceof ZipArchiveInputStream);

        final ArchiveInputStream cpio = getStreamFor("bla.cpio");
        assertNotNull(cpio);
        assertTrue(cpio instanceof CpioArchiveInputStream);

    }

// org.apache.commons.compress.DetectArchiverTestCase::testEmptyCpioArchive
    public void testEmptyCpioArchive() throws Exception {
        checkEmptyArchive("cpio");
    }

// org.apache.commons.compress.DetectArchiverTestCase::testEmptyJarArchive
    public void testEmptyJarArchive() throws Exception {
        checkEmptyArchive("jar");
    }

// org.apache.commons.compress.DetectArchiverTestCase::testEmptyZipArchive
    public void testEmptyZipArchive() throws Exception {
        checkEmptyArchive("zip");
    }

// org.apache.commons.compress.archivers.ArTestCase::testArArchiveCreation
    public void testArArchiveCreation() throws Exception {
        final File output = new File(dir, "bla.ar");

        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");

        final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("ar", out);
        os.putArchiveEntry(new ArArchiveEntry("test1.xml", file1.length()));
        IOUtils.copy(new FileInputStream(file1), os);
        os.closeArchiveEntry();

        os.putArchiveEntry(new ArArchiveEntry("test2.xml", file2.length()));
        IOUtils.copy(new FileInputStream(file2), os);
        os.closeArchiveEntry();

        os.close();
    }

// org.apache.commons.compress.archivers.ArTestCase::testArUnarchive
    public void testArUnarchive() throws Exception {
        final File output = new File(dir, "bla.ar");
        {
            final File file1 = getFile("test1.xml");
            final File file2 = getFile("test2.xml");

            final OutputStream out = new FileOutputStream(output);
            final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("ar", out);
            os.putArchiveEntry(new ArArchiveEntry("test1.xml", file1.length()));
            IOUtils.copy(new FileInputStream(file1), os);
            os.closeArchiveEntry();

            os.putArchiveEntry(new ArArchiveEntry("test2.xml", file2.length()));
            IOUtils.copy(new FileInputStream(file2), os);
            os.closeArchiveEntry();
            os.close();
            out.close();
        }

        
        final File input = output;
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is));
        final ArArchiveEntry entry = (ArArchiveEntry)in.getNextEntry();

        File target = new File(dir, entry.getName());
        final OutputStream out = new FileOutputStream(target);

        IOUtils.copy(in, out);

        out.close();
        in.close();
        is.close();
    }

// org.apache.commons.compress.archivers.ArTestCase::testArDelete
    public void testArDelete() throws Exception {
        final File output = new File(dir, "bla.ar");

        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");
        {
            

            final OutputStream out = new FileOutputStream(output);
            final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("ar", out);
            os.putArchiveEntry(new ArArchiveEntry("test1.xml", file1.length()));
            IOUtils.copy(new FileInputStream(file1), os);
            os.closeArchiveEntry();

            os.putArchiveEntry(new ArArchiveEntry("test2.xml", file2.length()));
            IOUtils.copy(new FileInputStream(file2), os);
            os.closeArchiveEntry();
            os.close();
            out.close();
        }

        assertEquals(8
                     + 60 + file1.length() + (file1.length() % 2)
                     + 60 + file2.length() + (file2.length() % 2),
                     output.length());

        final File output2 = new File(dir, "bla2.ar");

        int copied = 0;
        int deleted = 0;

        {
            

            final InputStream is = new FileInputStream(output);
            final OutputStream os = new FileOutputStream(output2);
            final ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream("ar", os);
            final ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is));
            while(true) {
                final ArArchiveEntry entry = (ArArchiveEntry)ais.getNextEntry();
                if (entry == null) {
                    break;
                }

                if ("test1.xml".equals(entry.getName())) {
                    aos.putArchiveEntry(entry);
                    IOUtils.copy(ais, aos);
                    aos.closeArchiveEntry();
                    copied++;
                } else {
                    IOUtils.copy(ais, new ByteArrayOutputStream());
                    deleted++;
                }

            }
            ais.close();
            aos.close();
            is.close();
            os.close();
        }

        assertEquals(1, copied);
        assertEquals(1, deleted);
        assertEquals(144, output2.length());

        long files = 0;
        long sum = 0;

        {
            final InputStream is = new FileInputStream(output2);
            final ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is));
            while(true) {
                final ArArchiveEntry entry = (ArArchiveEntry)ais.getNextEntry();
                if (entry == null) {
                    break;
                }

                IOUtils.copy(ais, new ByteArrayOutputStream());

                sum +=  entry.getLength();
                files++;
            }
            ais.close();
            is.close();
        }

        assertEquals(1, files);
        assertEquals(76, sum);

    }

// org.apache.commons.compress.archivers.CpioTestCase::testCpioArchiveCreation
    public void testCpioArchiveCreation() throws Exception {
        final File output = new File(dir, "bla.cpio");

        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");

        final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("cpio", out);
        os.putArchiveEntry(new CpioArchiveEntry("test1.xml", file1.length()));
        IOUtils.copy(new FileInputStream(file1), os);
        os.closeArchiveEntry();

        os.putArchiveEntry(new CpioArchiveEntry("test2.xml", file2.length()));
        IOUtils.copy(new FileInputStream(file2), os);
        os.closeArchiveEntry();

        os.close();
        out.close();
    }

// org.apache.commons.compress.archivers.CpioTestCase::testCpioUnarchive
    public void testCpioUnarchive() throws Exception {
        final File output = new File(dir, "bla.cpio");
        {
            final File file1 = getFile("test1.xml");
            final File file2 = getFile("test2.xml");

            final OutputStream out = new FileOutputStream(output);
            final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("cpio", out);
            CpioArchiveEntry entry = new CpioArchiveEntry("test1.xml", file1.length());
            entry.setMode(CpioConstants.C_ISREG);
            os.putArchiveEntry(entry);
            IOUtils.copy(new FileInputStream(file1), os);
            os.closeArchiveEntry();

            entry = new CpioArchiveEntry("test2.xml", file2.length());
            entry.setMode(CpioConstants.C_ISREG);
            os.putArchiveEntry(entry);
            IOUtils.copy(new FileInputStream(file2), os);
            os.closeArchiveEntry();

            os.close();
            out.close();
        }

        
        final File input = output;
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("cpio", is);

        Map result = new HashMap();
        ArchiveEntry entry = null;
        while ((entry = in.getNextEntry()) != null) {
            File target = new File(dir, entry.getName());
            final OutputStream out = new FileOutputStream(target);
            IOUtils.copy(in, out);
            out.close();
            result.put(entry.getName(), target);
        }
        in.close();
        is.close();

        int lineSepLength = System.getProperty("line.separator").length();

        File t = (File)result.get("test1.xml");
        assertTrue("Expected " + t.getAbsolutePath() + " to exist", t.exists());
        assertEquals("length of " + t.getAbsolutePath(),
                     72 + 4 * lineSepLength, t.length());

        t = (File)result.get("test2.xml");
        assertTrue("Expected " + t.getAbsolutePath() + " to exist", t.exists());
        assertEquals("length of " + t.getAbsolutePath(),
                     73 + 5 * lineSepLength, t.length());
    }

// org.apache.commons.compress.archivers.JarTestCase::testJarArchiveCreation
	public void testJarArchiveCreation() throws Exception {
		final File output = new File(dir, "bla.jar");

		final File file1 = getFile("test1.xml");
		final File file2 = getFile("test2.xml");
		
        final OutputStream out = new FileOutputStream(output);
        
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("jar", out);

        os.putArchiveEntry(new ZipArchiveEntry("testdata/test1.xml"));
        IOUtils.copy(new FileInputStream(file1), os);
        os.closeArchiveEntry();
        
        os.putArchiveEntry(new ZipArchiveEntry("testdata/test2.xml"));
        IOUtils.copy(new FileInputStream(file2), os);
        os.closeArchiveEntry();

        os.close();
    }

// org.apache.commons.compress.archivers.JarTestCase::testJarUnarchive
	public void testJarUnarchive() throws Exception {
		final File input = getFile("bla.jar");
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("jar", is);
        
        ZipArchiveEntry entry = (ZipArchiveEntry)in.getNextEntry();
        File o = new File(dir, entry.getName());
        o.getParentFile().mkdirs();
        OutputStream out = new FileOutputStream(o);
        IOUtils.copy(in, out);
        out.close();
        
        entry = (ZipArchiveEntry)in.getNextEntry();
        o = new File(dir, entry.getName());
        o.getParentFile().mkdirs();
        out = new FileOutputStream(o);
        IOUtils.copy(in, out);
        out.close();
        
        entry = (ZipArchiveEntry)in.getNextEntry();
        o = new File(dir, entry.getName());
        o.getParentFile().mkdirs();
        out = new FileOutputStream(o);
        IOUtils.copy(in, out);
        out.close();
        
        in.close();
        is.close();
    }

// org.apache.commons.compress.archivers.JarTestCase::testJarUnarchiveAll
	public void testJarUnarchiveAll() throws Exception {
		final File input = getFile("bla.jar");
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("jar", is);
        
        ArchiveEntry entry = in.getNextEntry();
		while (entry != null) {
			File archiveEntry = new File(dir, entry.getName());
			archiveEntry.getParentFile().mkdirs();
			if(entry.isDirectory()){
				archiveEntry.mkdir();
				entry = in.getNextEntry();
				continue;
			}
			OutputStream out = new FileOutputStream(archiveEntry);
			IOUtils.copy(in, out);
			out.close();
			entry = in.getNextEntry();
		}
		
		in.close();
		is.close();
	}

// org.apache.commons.compress.archivers.TarTestCase::testTarArchiveCreation
    public void testTarArchiveCreation() throws Exception {
		final File output = new File(dir, "bla.tar");
		final File file1 = getFile("test1.xml");
    	final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("tar", out);
        final TarArchiveEntry entry = new TarArchiveEntry("testdata/test1.xml");
        entry.setModTime(0);
        entry.setSize(file1.length());
        entry.setUserId(0);
        entry.setGroupId(0);
        entry.setUserName("avalon");
        entry.setGroupName("excalibur");
        entry.setMode(0100000);
        os.putArchiveEntry(entry);
        IOUtils.copy(new FileInputStream(file1), os);
        os.closeArchiveEntry();
        os.close();
    }

// org.apache.commons.compress.archivers.TarTestCase::testTarArchiveLongNameCreation
    public void testTarArchiveLongNameCreation() throws Exception {
    	String name = "testdata/12345678901234567890123456789012345678901234567890123456789012345678901234567890123456.xml";
    	byte[] bytes = name.getBytes();
    	assertEquals(bytes.length, 99);
    	
		final File output = new File(dir, "bla.tar");
		final File file1 = getFile("test1.xml");
    	final OutputStream out = new FileOutputStream(output);
        final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("tar", out);
        final TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setModTime(0);
        entry.setSize(file1.length());
        entry.setUserId(0);
        entry.setGroupId(0);
        entry.setUserName("avalon");
        entry.setGroupName("excalibur");
        entry.setMode(0100000);
        os.putArchiveEntry(entry);
        FileInputStream in = new FileInputStream(file1);
        IOUtils.copy(in, os);
        os.closeArchiveEntry();
        os.close();
        out.close();
        in.close();
        
        
        ArchiveOutputStream os2 = null;
        try {
        	String toLongName = "testdata/123456789012345678901234567890123456789012345678901234567890123456789012345678901234567.xml";
        	final File output2 = new File(dir, "bla.tar");
        	final OutputStream out2 = new FileOutputStream(output2);
        	os2 = new ArchiveStreamFactory().createArchiveOutputStream("tar", out2);
        	final TarArchiveEntry entry2 = new TarArchiveEntry(toLongName);
        	entry2.setModTime(0);
        	entry2.setSize(file1.length());
        	entry2.setUserId(0);
        	entry2.setGroupId(0);
        	entry2.setUserName("avalon");
        	entry2.setGroupName("excalibur");
        	entry2.setMode(0100000);
        	os.putArchiveEntry(entry);
        	IOUtils.copy(new FileInputStream(file1), os2);
        } catch(IOException e) {
        	assertTrue(true);
        } finally {
        	if (os2 != null){
        	    os2.closeArchiveEntry();
        	    os2.close();
        	}
        }
    }

// org.apache.commons.compress.archivers.TarTestCase::testTarUnarchive
    public void testTarUnarchive() throws Exception {
		final File input = getFile("bla.tar");
		final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("tar", is);
        final TarArchiveEntry entry = (TarArchiveEntry)in.getNextEntry();
        final OutputStream out = new FileOutputStream(new File(dir, entry.getName()));
        IOUtils.copy(in, out);
        in.close();
        out.close();
    }

// org.apache.commons.compress.archivers.ZipTestCase::testZipArchiveCreation
    public void testZipArchiveCreation() throws Exception {
        
        final File output = new File(dir, "bla.zip");
        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");

        final OutputStream out = new FileOutputStream(output);
        ArchiveOutputStream os = null;
        try {
            os = new ArchiveStreamFactory()
                .createArchiveOutputStream("zip", out);
            os.putArchiveEntry(new ZipArchiveEntry("testdata/test1.xml"));
            IOUtils.copy(new FileInputStream(file1), os);
            os.closeArchiveEntry();

            os.putArchiveEntry(new ZipArchiveEntry("testdata/test2.xml"));
            IOUtils.copy(new FileInputStream(file2), os);
            os.closeArchiveEntry();
        } finally {
            if (os != null) {
                os.close();
            }
        }
        out.close();

        
        List results = new ArrayList();

        final InputStream is = new FileInputStream(output);
        ArchiveInputStream in = null;
        try {
            in = new ArchiveStreamFactory()
                .createArchiveInputStream("zip", is);

            ZipArchiveEntry entry = null;
            while((entry = (ZipArchiveEntry)in.getNextEntry()) != null) {
                File outfile = new File(resultDir.getCanonicalPath() + "/result/" + entry.getName());
                outfile.getParentFile().mkdirs();
                OutputStream o = new FileOutputStream(outfile);
                try {
                    IOUtils.copy(in, o);
                } finally {
                    o.close();
                }
                results.add(outfile);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        is.close();

        assertEquals(results.size(), 2);
        File result = (File)results.get(0);
        assertEquals(file1.length(), result.length());
        result = (File)results.get(1);
        assertEquals(file2.length(), result.length());
    }

// org.apache.commons.compress.archivers.ZipTestCase::testZipUnarchive
    public void testZipUnarchive() throws Exception {
        final File input = getFile("bla.zip");
        final InputStream is = new FileInputStream(input);
        final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("zip", is);
        final ZipArchiveEntry entry = (ZipArchiveEntry)in.getNextEntry();
        final OutputStream out = new FileOutputStream(new File(dir, entry.getName()));
        IOUtils.copy(in, out);
        out.close();
        in.close();
    }

// org.apache.commons.compress.archivers.cpio.CpioArchiveInputStreamTest::testCpioUnarchive
    public void testCpioUnarchive() throws Exception {
        StringBuffer expected = new StringBuffer();
        expected.append("./test1.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>./test2.xml<?xml version=\"1.0\"?>\n");
        expected.append("<empty/>\n");
        

        CpioArchiveInputStream in = 
                new CpioArchiveInputStream(new FileInputStream(getFile("bla.cpio")));
        CpioArchiveEntry entry= null;
        
        StringBuffer result = new StringBuffer();
        while ((entry = (CpioArchiveEntry) in.getNextEntry()) != null) {
            result.append(entry.getName());
            int tmp;
            while ((tmp = in.read()) != -1) {
                result.append((char) tmp);
             }
         }
         in.close();
         assertEquals(result.toString(), expected.toString());
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripExplicitUnicodeExtra
    public void testUtf8FileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, true, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripNoEFSExplicitUnicodeExtra
    public void testUtf8FileRoundtripNoEFSExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, false, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testCP437FileRoundtripExplicitUnicodeExtra
    public void testCP437FileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CP437, false, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testASCIIFileRoundtripExplicitUnicodeExtra
    public void testASCIIFileRoundtripExplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(US_ASCII, false, true);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripImplicitUnicodeExtra
    public void testUtf8FileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, true, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8FileRoundtripNoEFSImplicitUnicodeExtra
    public void testUtf8FileRoundtripNoEFSImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(UTF_8, false, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testCP437FileRoundtripImplicitUnicodeExtra
    public void testCP437FileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(CP437, false, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testASCIIFileRoundtripImplicitUnicodeExtra
    public void testASCIIFileRoundtripImplicitUnicodeExtra()
        throws IOException {
        testFileRoundtrip(US_ASCII, false, false);
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testRead7ZipArchive
    public void testRead7ZipArchive() throws IOException, URISyntaxException {
        URL zip = getClass().getResource("/utf8-7zip-test.zip");
        File archive = new File(new URI(zip.toString()));
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, CP437, false);
            assertNotNull(zf.getEntry(ASCII_TXT));
            assertNotNull(zf.getEntry(EURO_FOR_DOLLAR_TXT));
            assertNotNull(zf.getEntry(OIL_BARREL_TXT));
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testRead7ZipArchiveForStream
    public void testRead7ZipArchiveForStream() throws IOException,
                                                      URISyntaxException {
        URL zip = getClass().getResource("/utf8-7zip-test.zip");
        FileInputStream archive =
            new FileInputStream(new File(new URI(zip.toString())));
        ZipArchiveInputStream zi = null;
        try {
            zi = new ZipArchiveInputStream(archive, CP437, false);
            assertEquals(ASCII_TXT, zi.getNextEntry().getName());
            assertEquals(OIL_BARREL_TXT, zi.getNextEntry().getName());
            assertEquals(EURO_FOR_DOLLAR_TXT, zi.getNextEntry().getName());
        } finally {
            if (zi != null) {
                zi.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testReadWinZipArchive
    public void testReadWinZipArchive() throws IOException, URISyntaxException {
        URL zip = getClass().getResource("/utf8-winzip-test.zip");
        File archive = new File(new URI(zip.toString()));
        ZipFile zf = null;
        try {
            zf = new ZipFile(archive, null, true);
            assertNotNull(zf.getEntry(ASCII_TXT));
            assertNotNull(zf.getEntry(EURO_FOR_DOLLAR_TXT));
            assertNotNull(zf.getEntry(OIL_BARREL_TXT));
        } finally {
            ZipFile.closeQuietly(zf);
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testReadWinZipArchiveForStream
    public void testReadWinZipArchiveForStream() throws IOException,
                                                      URISyntaxException {
        URL zip = getClass().getResource("/utf8-winzip-test.zip");
        FileInputStream archive =
            new FileInputStream(new File(new URI(zip.toString())));
        ZipArchiveInputStream zi = null;
        try {
            zi = new ZipArchiveInputStream(archive, null, true);
            assertEquals(EURO_FOR_DOLLAR_TXT, zi.getNextEntry().getName());
            assertEquals(OIL_BARREL_TXT, zi.getNextEntry().getName());
            assertEquals(ASCII_TXT, zi.getNextEntry().getName());
        } finally {
            if (zi != null) {
                zi.close();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testZipFileReadsUnicodeFields
    public void testZipFileReadsUnicodeFields() throws IOException {
        File file = File.createTempFile("unicode-test", ".zip");
        ZipArchiveInputStream zi = null;
        try {
            createTestFile(file, US_ASCII, false, true);
            FileInputStream archive = new FileInputStream(file);
            zi = new ZipArchiveInputStream(archive, US_ASCII, true);
            assertEquals(OIL_BARREL_TXT, zi.getNextEntry().getName());
            assertEquals(EURO_FOR_DOLLAR_TXT, zi.getNextEntry().getName());
            assertEquals(ASCII_TXT, zi.getNextEntry().getName());
        } finally {
            if (zi != null) {
                zi.close();
            }
            if (file.exists()) {
                file.delete();
            }
        }
    }

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testZipArchiveInputStreamReadsUnicodeFields
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

// org.apache.commons.compress.archivers.zip.UTF8ZipFilesTest::testUtf8Interoperability
    public void testUtf8Interoperability() throws IOException {
        File file1 = super.getFile("utf8-7zip-test.zip");
        File file2 = super.getFile("utf8-winzip-test.zip");

        testFile(file1,CP437);
        testFile(file2,CP437);

    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteDir
    public void testDeleteDir() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("zip", is);

            out = factory.createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.delete("bla");
            archiveListDelete("bla");
            changes.perform(ais, out);
            is.close();

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFile
    public void testDeleteFile() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("zip", is);

            out = factory.createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.delete("bla/test5.xml");
            archiveListDelete("bla/test5.xml");
            changes.perform(ais, out);
            is.close();

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeletePlusAdd
    public void testDeletePlusAdd() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("zip", is);
            out = factory.createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.delete("bla");
            archiveListDelete("bla");

            
            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("bla/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("bla/test.txt");

            changes.perform(ais, out);
            is.close();

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToZip
    public void testDeleteFromAndAddToZip() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("zip", is);
            out = factory.createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("blub/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("blub/test.txt");

            changes.delete("testdata/test1.xml");
            archiveListDelete("testdata/test1.xml");

            changes.perform(ais, out);
            is.close();

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddDeleteAdd
    public void testAddDeleteAdd() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("zip", is);
            out = factory.createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("blub/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("blub/test.txt");

            changes.delete("blub");
            archiveListDelete("blub");

            changes.perform(ais, out);
            is.close();

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteAddDelete
    public void testDeleteAddDelete() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        result.deleteOnExit();
        try {

            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("zip", is);
            out = factory.createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            changes.delete("bla");

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("bla/test.txt");
            changes.add(entry, new FileInputStream(file1));
            archiveList.add("bla/test.txt");

            changes.delete("bla");
            archiveListDelete("bla");

            changes.perform(ais, out);
            is.close();

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromZip
    public void testDeleteFromZip() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File input = getFile("bla.zip");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("zip", is);

            temp = File.createTempFile("test", ".zip");
            temp.deleteOnExit();
            out = factory.createArchiveOutputStream("zip",
                    new FileOutputStream(temp));

            changes.perform(ais, out);
        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        List expected = new ArrayList();
        expected.add("test1.xml");

        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromTar
    public void testDeleteFromTar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File input = getFile("bla.tar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("tar", is);

            temp = new File(dir, "bla.tar");
            out = factory.createArchiveOutputStream("tar",
                    new FileOutputStream(temp));

            changes.perform(ais, out);
        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }
        List expected = new ArrayList();
        expected.add("test1.xml");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromJar
    public void testDeleteFromJar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");
            changes.delete("META-INF");
            changes.delete(".classpath");
            changes.delete(".project");

            final File input = getFile("bla.jar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("jar", is);

            temp = new File(dir, "bla.jar");
            out = factory.createArchiveOutputStream("jar",
                    new FileOutputStream(temp));

            changes.perform(ais, out);
        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }
        List expected = new ArrayList();
        expected.add("test1.xml");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToTar
    public void testDeleteFromAndAddToTar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");

            final File file1 = getFile("test.txt");

            final TarArchiveEntry entry = new TarArchiveEntry(
                    "testdata/test.txt");
            entry.setModTime(0);
            entry.setSize(file1.length());
            entry.setUserId(0);
            entry.setGroupId(0);
            entry.setUserName("avalon");
            entry.setGroupName("excalibur");
            entry.setMode(0100000);

            changes.add(entry, new FileInputStream(file1));

            final File input = getFile("bla.tar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("tar", is);

            temp = new File(dir, "bla.tar");
            out = factory.createArchiveOutputStream("tar",
                    new FileOutputStream(temp));

            changes.perform(ais, out);
        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }
        List expected = new ArrayList();
        expected.add("test1.xml");
        expected.add("testdata/test.txt");
        final ArchiveInputStream in = factory.createArchiveInputStream("tar", new FileInputStream(temp));
        this.checkArchiveContent(in, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToJar
    public void testDeleteFromAndAddToJar() throws Exception {
        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File temp = null;
        try {
            ChangeSet changes = new ChangeSet();
            changes.delete("test2.xml");
            changes.delete("META-INF");
            changes.delete(".classpath");
            changes.delete(".project");

            final File file1 = getFile("test.txt");
            JarArchiveEntry entry = new JarArchiveEntry("testdata/test.txt");
            changes.add(entry, new FileInputStream(file1));

            final File input = getFile("bla.jar");
            final InputStream is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("jar", is);

            temp = new File(dir, "bla.jar");
            out = factory.createArchiveOutputStream("jar",
                    new FileOutputStream(temp));

            changes.perform(ais, out);
        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }
        List expected = new ArrayList();
        expected.add("test1.xml");
        expected.add("testdata/test.txt");
        this.checkArchiveContent(temp, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testRenameAndDelete
    public void testRenameAndDelete() throws Exception {
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddMoveDelete
    public void testAddMoveDelete() throws Exception {
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddToEmptyArchive
    public void testAddToEmptyArchive() {}

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteAddToOneFileArchive
    public void testDeleteAddToOneFileArchive() {}

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddDeleteToOneFileArchive
    public void testAddDeleteToOneFileArchive() throws Exception {
        File input = this.createSingleEntryArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        InputStream is = null;
        File result = File.createTempFile("test", ".zip");
        result.deleteOnExit();
        ChangeSet changes = new ChangeSet();
        try {

            is = new FileInputStream(input);
            ais = factory.createArchiveInputStream("zip", is);

            out = factory.createArchiveOutputStream("zip",
                    new FileOutputStream(result));
            ArchiveEntry entry = new ZipArchiveEntry("bla/test.txt");
            changes.add(entry, new FileInputStream(getFile("test.txt")));
            archiveList.add("bla/test.txt");

            changes.delete("testdata");
            archiveListDelete("testdata");
            
            changes.perform(ais, out);
            is.close();

        } finally {
            if (out != null) {
                out.close();
            }
            if (ais != null) {
                ais.close(); 
            } else if (is != null){
                is.close();
            }
        }

        this.checkArchiveContent(result, archiveList);
    }

// org.apache.commons.compress.compressors.BZip2TestCase::testBzipCreation
	public void testBzipCreation()  throws Exception {
		final File input = getFile("test.txt");
		final File output = new File(dir, "test.txt.bz2");
		final OutputStream out = new FileOutputStream(output);
		final CompressorOutputStream cos = new CompressorStreamFactory().createCompressorOutputStream("bzip2", out);
		FileInputStream in = new FileInputStream(input);
		IOUtils.copy(in, cos);
		cos.close();
		in.close();
	}

// org.apache.commons.compress.compressors.BZip2TestCase::testBzip2Unarchive
	public void testBzip2Unarchive() throws Exception {
		final File input = getFile("bla.txt.bz2");
		final File output = new File(dir, "bla.txt");
        final InputStream is = new FileInputStream(input);
        final CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("bzip2", is);
        FileOutputStream os = new FileOutputStream(output);
        IOUtils.copy(in, os);
		is.close();
		os.close();
    }

// org.apache.commons.compress.compressors.GZipTestCase::testGzipCreation
	public void testGzipCreation()  throws Exception {
		final File input = getFile("test1.xml");
		final File output = new File(dir, "test1.xml.gz");
		final OutputStream out = new FileOutputStream(output);
		final CompressorOutputStream cos = new CompressorStreamFactory().createCompressorOutputStream("gz", out);
		IOUtils.copy(new FileInputStream(input), cos);
		cos.close();
	}

// org.apache.commons.compress.compressors.GZipTestCase::testGzipUnarchive
	public void testGzipUnarchive() throws Exception {
		final File input = getFile("bla.tgz");
		final File output = new File(dir, "bla.tar");
        final InputStream is = new FileInputStream(input);
        final CompressorInputStream in = new CompressorStreamFactory().createCompressorInputStream("gz", is);
        FileOutputStream out = new FileOutputStream(output);
        IOUtils.copy(in, out);
		in.close();
		is.close();
		out.close();
    }
