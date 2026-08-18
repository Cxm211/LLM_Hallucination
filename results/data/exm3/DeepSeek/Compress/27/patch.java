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
        while (start < end && buffer[start] == ' '){
            start++;
        }

        // If after skipping spaces we have a NUL, treat as zero
        if (start < end && buffer[start] == 0) {
            return 0L;
        }

        // Trim all trailing NULs and spaces.
        // The ustar and POSIX tar specs require a trailing NUL or
        // space but some implementations use the extra digit for big
        // sizes/uids/gids ...
        while (start < end && (buffer[end - 1] == 0 || buffer[end - 1] == ' ')) {
            end--;
        }
        if (start == end) {
            // All bytes were trimmed, use the last byte of the original range for error message
            byte trailer = buffer[offset + length - 1];
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, start, trailer));
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