public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        if (length < 2) {
            throw new IllegalArgumentException();
        }

        final int end = offset + length;

        // If first byte is NUL, then all bytes must be NULs
        if (buffer[offset] == 0) {
            for (int i = offset; i < end; i++) {
                if (buffer[i] != 0) {
                    throw new IllegalArgumentException();
                }
            }
            return 0L;
        }

        long result = 0L;
        int i = offset;

        // Skip leading spaces
        while (i < end && buffer[i] == (byte) ' ') {
            i++;
        }

        // Skip leading zeros
        while (i < end && buffer[i] == (byte) '0') {
            i++;
        }

        boolean sawDigit = false;
        boolean sawTerminator = false;

        // Parse octal digits
        while (i < end) {
            final byte b = buffer[i];
            if (b == 0 || b == (byte) ' ') {
                sawTerminator = true;
                break;
            }
            if (b < '0' || b > '7') {
                throw new IllegalArgumentException();
            }
            sawDigit = true;
            result = (result << 3) + (b - '0');
            i++;
        }

        // Must have a trailing NUL or space terminator
        if (!sawTerminator) {
            throw new IllegalArgumentException();
        }

        // After terminator, only NULs or spaces are allowed
        while (i < end) {
            final byte b = buffer[i++];
            if (b != 0 && b != (byte) ' ') {
                throw new IllegalArgumentException();
            }
        }

        // It's acceptable if value was entirely padding zeros/spaces, treat as zero
        if (!sawDigit) {
            return 0L;
        }

        return result;
    }