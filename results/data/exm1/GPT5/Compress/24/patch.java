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

        // Trim all trailing NULs and spaces if present.
        // Accept fields without a trailing NUL/space if they consist solely of octal digits.
        byte trailer = buffer[end - 1];
        if (trailer == 0 || trailer == ' '){
            end--;
            trailer = buffer[end - 1];
            while (start < end - 1 && (trailer == 0 || trailer == ' ')) {
                end--;
                trailer = buffer[end - 1];
            }
        } else if (trailer < '0' || trailer > '7') {
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, end-1, trailer));
        }

        // After trimming, there must be at least one digit unless the field starts with NUL
        if (start == end) {
            if (buffer[offset] == 0) {
                return 0L;
            }
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, Math.max(offset, end - 1), buffer[Math.max(offset, end - 1)]));
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