public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long result = 0;
        int end = offset + length;
        int start = offset;

        if (length < 2) {
            throw new IllegalArgumentException("Length " + length + " must be at least 2");
        }

        // Skip leading spaces
        while (start < end && buffer[start] == ' ') {
            start++;
        }

        // Trim trailing NULs or spaces
        while (end > start && (buffer[end - 1] == 0 || buffer[end - 1] == ' ')) {
            end--;
        }

        // If field is all spaces/NULs
        if (start == end) {
            return 0L;
        }

        for (; start < end; start++) {
            final byte currentByte = buffer[start];
            if (currentByte < '0' || currentByte > '7') {
                throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, start, currentByte));
            }
            result = (result << 3) + (currentByte - '0');
        }

        return result;
    }