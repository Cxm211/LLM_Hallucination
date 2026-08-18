// buggy function
    public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long    result = 0;
        boolean stillPadding = true;
        int     end = offset + length;
        int     start = offset;

        for (int i = start; i < end; i++){
            final byte currentByte = buffer[i];
            if (currentByte == 0) {
                break;
            }

        // Skip leading spaces
            if (currentByte == (byte) ' ' || currentByte == '0') {
                if (stillPadding) {
                   continue;
            }
                if (currentByte == (byte) ' ') {
                break;
                }
            }

        // Must have trailing NUL or space
        // May have additional NUL or space

            stillPadding = false;
            // CheckStyle:MagicNumber OFF
            if (currentByte < '0' || currentByte > '7'){
                throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, start, currentByte));
            }
            result = (result << 3) + (currentByte - '0'); // convert from ASCII
            // CheckStyle:MagicNumber ON
        }

        return result;
    }

// trigger testcase
// org/apache/commons/compress/archivers/tar/TarUtilsTest.java::testParseOctalInvalid
public void testParseOctalInvalid() throws Exception{
        byte [] buffer;
        buffer=new byte[0]; // empty byte array
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - should be at least 2 bytes long");
        } catch (IllegalArgumentException expected) {
        }
        buffer=new byte[]{0}; // 1-byte array
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - should be at least 2 bytes long");
        } catch (IllegalArgumentException expected) {
        }
        buffer=new byte[]{0,0,' '}; // not all NULs
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - not all NULs");
        } catch (IllegalArgumentException expected) {
        }
        buffer=new byte[]{' ',0,0,0}; // not all NULs
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - not all NULs");
        } catch (IllegalArgumentException expected) {
        }
        buffer = "abcdef ".getBytes("UTF-8"); // Invalid input
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        buffer = "77777777777".getBytes("UTF-8"); // Invalid input - no trailer
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - no trailer");
        } catch (IllegalArgumentException expected) {
        }
        buffer = " 0 07 ".getBytes("UTF-8"); // Invalid - embedded space
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - embedded space");
        } catch (IllegalArgumentException expected) {
        }
        buffer = " 0\00007 ".getBytes("UTF-8"); // Invalid - embedded NUL
        try {
            TarUtils.parseOctal(buffer,0, buffer.length);
            fail("Expected IllegalArgumentException - embedded NUL");
        } catch (IllegalArgumentException expected) {
        }
    }
