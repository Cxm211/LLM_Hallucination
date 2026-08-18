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

        // Trim all trailing NULs and spaces safely without reading before start.
        while (start < end) {
            byte t = buffer[end - 1];
            if (t == 0 || t == ' ') {
                end--;
            } else {
                break;
            }
        }
        if (start == end) {
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, start, (byte) 0));
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