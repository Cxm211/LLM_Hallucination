// buggy code
    public void close() throws IOException {
        if (!this.closed) {
            super.close();
            this.closed = true;
        }
    }

// relevant test
// org.apache.commons.compress.DetectArchiverTestCase::testDetection
    public void testDetection() throws Exception {
        final ArchiveStreamFactory factory = new ArchiveStreamFactory();

        final ArchiveInputStream ar = factory.createArchiveInputStream(
                                                                       new BufferedInputStream(new FileInputStream(
                                                                                                                   new File(getClass().getClassLoader().getResource("bla.ar").getFile())))); 
        assertNotNull(ar);
        assertTrue(ar instanceof ArArchiveInputStream);

        final ArchiveInputStream tar = factory.createArchiveInputStream(
                                                                        new BufferedInputStream(new FileInputStream(
                                                                                                                    new File(getClass().getClassLoader().getResource("bla.tar").getFile()))));
        assertNotNull(tar);
        assertTrue(tar instanceof TarArchiveInputStream);

        final ArchiveInputStream zip = factory.createArchiveInputStream(
                                                                        new BufferedInputStream(new FileInputStream(
                                                                                                                    new File(getClass().getClassLoader().getResource("bla.zip").getFile()))));
        assertNotNull(zip);
        assertTrue(zip instanceof ZipArchiveInputStream);

        final ArchiveInputStream jar = factory.createArchiveInputStream(
                                                                        new BufferedInputStream(new FileInputStream(
                                                                                                                    new File(getClass().getClassLoader().getResource("bla.jar").getFile()))));
        assertNotNull(jar);
        assertTrue(jar instanceof ZipArchiveInputStream);

        final ArchiveInputStream cpio = factory.createArchiveInputStream(
                                                                         new BufferedInputStream(new FileInputStream(
                                                                                                                     new File(getClass().getClassLoader().getResource("bla.cpio").getFile()))));
        assertNotNull(cpio);
        assertTrue(cpio instanceof CpioArchiveInputStream);

        
        
        
        

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
		}
		
		
		final File input = output;
		final InputStream is = new FileInputStream(input);
		final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("ar", is);
		final ArArchiveEntry entry = (ArArchiveEntry)in.getNextEntry();
		
		File target = new File(dir, entry.getName());
        final OutputStream out = new FileOutputStream(target);
        
        IOUtils.copy(in, out);
    
        out.close();
        in.close();
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
    }

// org.apache.commons.compress.archivers.CpioTestCase::testCpioUnarchive
    public void testCpioUnarchive() throws Exception {
        final File output = new File(dir, "bla.cpio");
        {
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
        IOUtils.copy(new FileInputStream(file1), os);
        os.closeArchiveEntry();
        os.close();
        
        
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
        out.close();
        in.close();
    }

// org.apache.commons.compress.archivers.ZipTestCase::testZipArchiveCreation
    public void testZipArchiveCreation() throws Exception {
        
        final File output = new File(dir, "bla.zip");
        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");

        {
            final OutputStream out = new FileOutputStream(output);
            final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("zip", out);

            os.putArchiveEntry(new ZipArchiveEntry("testdata/test1.xml"));
            IOUtils.copy(new FileInputStream(file1), os);
            os.closeArchiveEntry();

            os.putArchiveEntry(new ZipArchiveEntry("testdata/test2.xml"));
            IOUtils.copy(new FileInputStream(file2), os);
            os.closeArchiveEntry();
            os.close();
        }

        
        List results = new ArrayList();

        {
            final InputStream is = new FileInputStream(output);
            final ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream("zip", is);

            File result = File.createTempFile("dir-result", "");
            result.delete();
            result.mkdir();

            ZipArchiveEntry entry = null;
            while((entry = (ZipArchiveEntry)in.getNextEntry()) != null) {
                File outfile = new File(result.getCanonicalPath() + "/result/" + entry.getName());
                outfile.getParentFile().mkdirs();
                OutputStream out = new FileOutputStream(outfile);
                IOUtils.copy(in, out);
                out.close();
                results.add(outfile);
            }
            in.close();
        }

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

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteDir
    public void testDeleteDir() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        try {

            final InputStream is = new FileInputStream(input);
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("zip", is);

            out = new ArchiveStreamFactory().createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.delete("bla");
            changes.perform(ais, out);

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        List expected = new ArrayList();
        expected.add("testdata/test1.xml");
        expected.add("testdata/test2.xml");
        expected.add("test/test3.xml");
        expected.add("test.txt");
        expected.add("something/bla");
        expected.add("test with spaces.txt");

        this.checkArchiveContent(result, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFile
    public void testDeleteFile() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        try {

            final InputStream is = new FileInputStream(input);
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("zip", is);

            out = new ArchiveStreamFactory().createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.delete("bla/test5.xml");
            changes.perform(ais, out);

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        List expected = new ArrayList();
        expected.add("testdata/test1.xml");
        expected.add("testdata/test2.xml");
        expected.add("test/test3.xml");
        expected.add("test.txt");
        expected.add("something/bla");
        expected.add("test with spaces.txt");
        expected.add("bla/test4.xml");
        expected.add("bla/blubber/test6.xml");
        this.checkArchiveContent(result, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeletePlusAdd
    public void testDeletePlusAdd() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        try {

            final InputStream is = new FileInputStream(input);
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("zip", is);
            out = new ArchiveStreamFactory().createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();
            changes.delete("bla");

            
            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("bla/test.txt");
            changes.add(entry, new FileInputStream(file1));

            changes.perform(ais, out);

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        List expected = new ArrayList();
        expected.add("testdata/test1.xml");
        expected.add("testdata/test2.xml");
        expected.add("test/test3.xml");
        expected.add("test.txt");
        expected.add("something/bla");
        expected.add("bla/test.txt");
        expected.add("test with spaces.txt");

        this.checkArchiveContent(result, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteFromAndAddToZip
    public void testDeleteFromAndAddToZip() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        try {

            final InputStream is = new FileInputStream(input);
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("zip", is);
            out = new ArchiveStreamFactory().createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("blub/test.txt");
            changes.add(entry, new FileInputStream(file1));

            changes.delete("testdata/test1.xml");

            changes.perform(ais, out);

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        List expected = new ArrayList();
        expected.add("testdata/test2.xml");
        expected.add("test/test3.xml");
        expected.add("blub/test.txt");
        expected.add("bla/test5.xml");
        expected.add("bla/blubber/test6.xml");
        expected.add("test.txt");
        expected.add("something/bla");
        expected.add("bla/test4.xml");
        expected.add("test with spaces.txt");

        this.checkArchiveContent(result, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testAddDeleteAdd
    public void testAddDeleteAdd() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        try {

            final InputStream is = new FileInputStream(input);
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("zip", is);
            out = new ArchiveStreamFactory().createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("blub/test.txt");
            changes.add(entry, new FileInputStream(file1));

            changes.delete("blub");

            changes.perform(ais, out);

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        List expected = new ArrayList();
        expected.add("testdata/test1.xml");
        expected.add("testdata/test2.xml");
        expected.add("test/test3.xml");
        expected.add("test.txt");
        expected.add("bla/test5.xml");
        expected.add("bla/blubber/test6.xml");
        expected.add("something/bla");
        expected.add("bla/test4.xml");
        expected.add("test with spaces.txt");

        this.checkArchiveContent(result, expected);
    }

// org.apache.commons.compress.changes.ChangeSetTestCase::testDeleteAddDelete
    public void testDeleteAddDelete() throws Exception {
        File input = this.createArchive("zip");

        ArchiveOutputStream out = null;
        ArchiveInputStream ais = null;
        File result = File.createTempFile("test", ".zip");
        try {

            final InputStream is = new FileInputStream(input);
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("zip", is);
            out = new ArchiveStreamFactory().createArchiveOutputStream("zip",
                    new FileOutputStream(result));

            ChangeSet changes = new ChangeSet();

            changes.delete("bla");

            final File file1 = getFile("test.txt");
            ArchiveEntry entry = new ZipArchiveEntry("bla/test.txt");
            changes.add(entry, new FileInputStream(file1));

            changes.delete("bla");

            changes.perform(ais, out);

        } finally {
            if (out != null)
                out.close();
            if (ais != null)
                ais.close();
        }

        List expected = new ArrayList();
        expected.add("testdata/test1.xml");
        expected.add("testdata/test2.xml");
        expected.add("test/test3.xml");
        expected.add("test.txt");
        expected.add("something/bla");
        expected.add("test with spaces.txt");

        this.checkArchiveContent(result, expected);
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
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("zip", is);

            temp = File.createTempFile("test", ".zip");
            out = new ArchiveStreamFactory().createArchiveOutputStream("zip",
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
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("tar", is);

            temp = new File(dir, "bla.tar");
            out = new ArchiveStreamFactory().createArchiveOutputStream("tar",
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
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("jar", is);

            temp = new File(dir, "bla.jar");
            out = new ArchiveStreamFactory().createArchiveOutputStream("jar",
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
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("tar", is);

            temp = new File(dir, "bla.tar");
            out = new ArchiveStreamFactory().createArchiveOutputStream("tar",
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
        
        final ArchiveInputStream in = new ArchiveStreamFactory()
                .createArchiveInputStream("tar", new FileInputStream(temp));
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
            ais = new ArchiveStreamFactory()
                    .createArchiveInputStream("jar", is);

            temp = new File(dir, "bla.jar");
            out = new ArchiveStreamFactory().createArchiveOutputStream("jar",
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
