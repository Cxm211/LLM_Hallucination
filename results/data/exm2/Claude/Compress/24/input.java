    public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long    result = 0;
        int     end = offset + length;
        int     start = offset;

        if (length < 2){
            throw new IllegalArgumentException("Length "+length+" must be at least 2");
        }

        if (buffer[start] == 0) {
            return 0L;
        }

        // Skip leading spaces
        while (start < end){
            if (buffer[start] == ' '){
                start++;
            } else {
                break;
            }
        }

        // Trim all trailing NULs and spaces.
        // The ustar and POSIX tar specs require a trailing NUL or
        // space but some implementations use the extra digit for big
        // sizes/uids/gids ...
        byte trailer = buffer[end - 1];
        if (trailer == 0 || trailer == ' '){
            end--;
        } else {
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, end-1, trailer));
        }
        trailer = buffer[end - 1];
        while (start < end - 1 && (trailer == 0 || trailer == ' ')) {
            end--;
            trailer = buffer[end - 1];
        }

        for ( ;start < end; start++) {
            final byte currentByte = buffer[start];
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
public void testParseOctal() throws Exception{
        long value; 
        byte [] buffer;
        final long MAX_OCTAL  = 077777777777L; // Allowed 11 digits
        final long MAX_OCTAL_OVERFLOW  = 0777777777777L; // in fact 12 for some implementations
        final String maxOctal = "777777777777"; // Maximum valid octal
        buffer = maxOctal.getBytes(CharsetNames.UTF_8);
        value = TarUtils.parseOctal(buffer,0, buffer.length);
        assertEquals(MAX_OCTAL_OVERFLOW, value);
        buffer[buffer.length - 1] = ' ';
        value = TarUtils.parseOctal(buffer,0, buffer.length);
        assertEquals(MAX_OCTAL, value);
        buffer[buffer.length-1]=0;
        value = TarUtils.parseOctal(buffer,0, buffer.length);
        assertEquals(MAX_OCTAL, value);
        buffer=new byte[]{0,0};
        value = TarUtils.parseOctal(buffer,0, buffer.length);
        assertEquals(0, value);
        buffer=new byte[]{0,' '};
        value = TarUtils.parseOctal(buffer,0, buffer.length);
        assertEquals(0, value);
    }
