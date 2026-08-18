// buggy function
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

// trigger testcase
// org/apache/commons/compress/archivers/ArTestCase.java::testArDelete
public void testArDelete() throws Exception {
        final File output = new File(dir, "bla.ar");

        final File file1 = getFile("test1.xml");
        final File file2 = getFile("test2.xml");
        {
            // create

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
            // remove all but one file

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
