public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long result = 0;
        int end = offset + length;
        int start = offset;

        if (length < 2){
            throw new IllegalArgumentException("Length "+length+" must be at least 2");
        }

        // If the very first byte is NUL, treat field as zero (all-NULs case)
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

        // Must have at least one trailing NUL or space
        if (end <= start) {
            throw new IllegalArgumentException(
                exceptionMessage(buffer, offset, length, start, (byte) 0));
        }
        byte trailer = buffer[end - 1];
        if (trailer == 0 || trailer == ' '){
            // Trim all trailing NULs and spaces
            do {
                end--;
            } while (start < end && (buffer[end - 1] == 0 || buffer[end - 1] == ' '));
        } else {
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, end-1, trailer));
        }

        // If nothing remains after trimming trailers, this is invalid unless field started with NUL (handled above)
        if (start >= end) {
            throw new IllegalArgumentException(
                exceptionMessage(buffer, offset, length, start, (byte) 0));
        }

        for ( ; start < end; start++) {
            final byte currentByte = buffer[start];
            if (currentByte < '0' || currentByte > '7'){
                throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, start, currentByte));
            }
            result = (result << 3) + (currentByte - '0'); // convert from ASCII
        }

        return result;
    }