// org/apache/commons/compress/archivers/ArTestCase.java::testArPadding
public void testArPadding() throws Exception {
        final File output = new File(dir, "pad.ar");

        // create two small files, first has odd length to force padding
        final File odd = new File(dir, "odd.txt");
        final File even = new File(dir, "even.txt");
        {
            final FileOutputStream fos = new FileOutputStream(odd);
            fos.write("hello".getBytes("US-ASCII")); // 5 bytes (odd)
            fos.close();
        }
        {
            final FileOutputStream fos = new FileOutputStream(even);
            fos.write("data".getBytes("US-ASCII")); // 4 bytes (even)
            fos.close();
        }

        {
            final OutputStream out = new FileOutputStream(output);
            final ArchiveOutputStream os = new ArchiveStreamFactory().createArchiveOutputStream("ar", out);
            os.putArchiveEntry(new ArArchiveEntry("odd.txt", odd.length()));
            IOUtils.copy(new FileInputStream(odd), os);
            os.closeArchiveEntry();

            os.putArchiveEntry(new ArArchiveEntry("even.txt", even.length()));
            IOUtils.copy(new FileInputStream(even), os);
            os.closeArchiveEntry();
            os.close();
            out.close();
        }

        assertEquals(8 + 60 + odd.length() + (odd.length() % 2)
                     + 60 + even.length() + (even.length() % 2),
                     output.length());

        final File filtered = new File(dir, "pad_filtered.ar");
        int copied = 0;
        int deleted = 0;
        {
            final InputStream is = new FileInputStream(output);
            final OutputStream os = new FileOutputStream(filtered);
            final ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream("ar", os);
            final ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is));
            while (true) {
                final ArArchiveEntry entry = (ArArchiveEntry) ais.getNextEntry();
                if (entry == null) {
                    break;
                }
                if ("even.txt".equals(entry.getName())) {
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

        long files = 0;
        long sum = 0;
        {
            final InputStream is = new FileInputStream(filtered);
            final ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(is));
            while (true) {
                final ArArchiveEntry entry = (ArArchiveEntry) ais.getNextEntry();
                if (entry == null) {
                    break;
                }
                IOUtils.copy(ais, new ByteArrayOutputStream());
                sum += entry.getLength();
                files++;
            }
            ais.close();
            is.close();
        }

        assertEquals(1, files);
        assertEquals(4, sum);
    }