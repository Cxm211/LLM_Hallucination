// org/apache/commons/compress/archivers/cpio/CpioArchiveInputStreamTest.java::testModeZeroNonTrailerNewAscii
public void testModeZeroNonTrailerNewAscii() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // helper to write one newc header+name
        class W {
            void writeNewc(String name, int ino, int mode, int nlink) throws IOException {
                String magic = "070701";
                String inoS = String.format("%08x", ino);
                String modeS = String.format("%08x", mode);
                String uidS = String.format("%08x", 0);
                String gidS = String.format("%08x", 0);
                String nlinkS = String.format("%08x", nlink);
                String mtimeS = String.format("%08x", 0);
                String fsizeS = String.format("%08x", 0);
                String devmajS = String.format("%08x", 0);
                String devminS = String.format("%08x", 0);
                String rdevmajS = String.format("%08x", 0);
                String rdevminS = String.format("%08x", 0);
                int namesize = name.getBytes("US-ASCII").length + 1;
                String namesizeS = String.format("%08x", namesize);
                String checkS = String.format("%08x", 0);
                String header = magic + inoS + modeS + uidS + gidS + nlinkS + mtimeS + fsizeS + devmajS + devminS + rdevmajS + rdevminS + namesizeS + checkS;
                bos.write(header.getBytes("US-ASCII"));
                bos.write(name.getBytes("US-ASCII"));
                bos.write(0); // NUL
                // pad to 4-byte boundary from start of header+name
                int pad = (4 - (header.length() + namesize) % 4) % 4;
                for (int i = 0; i < pad; i++) bos.write(0);
                // filesize is 0 so no data or data padding
            }
        }
        W w = new W();
        // first entry: non-trailer with mode 0 (would fail before)
        w.writeNewc("foo", 1, 0, 1);
        // trailer entry
        w.writeNewc("TRAILER!!!", 0, 0, 1);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        CpioArchiveInputStream in = new CpioArchiveInputStream(bis);
        int count = 0;
        CpioArchiveEntry e;
        while ((e = (CpioArchiveEntry) in.getNextEntry()) != null) {
            count++;
        }
        in.close();
        assertEquals(2, count);
    }