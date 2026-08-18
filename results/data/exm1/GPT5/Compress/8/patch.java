public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        if (length < 2) {
            throw new IllegalArgumentException("Length " + length + " must be at least 2");
        }

        long result = 0;
        int end = offset + length;

        // Handle leading NUL specially
        if (buffer[offset] == 0) {
            if (length == 2) { // allow {0, 0} or {0, ' '}
                return 0L;
            }
            // For longer fields starting with NUL, all remaining must be NUL
            for (int i = offset + 1; i < end; i++) {
                if (buffer[i] != 0) {
                    throw new IllegalArgumentException("Not all NULs");
                }
            }
            return 0L;
        }

        int i = offset;
        // Skip leading spaces only (zeros are digits, not padding)
        while (i < end && buffer[i] == (byte) ' ') {
            i++;
        }

        boolean foundDigit = false;
        int trailerPos = -1;

        for (; i < end; i++) {
            final byte currentByte = buffer[i];
            if (currentByte == 0 || currentByte == (byte) ' ') {
                trailerPos = i;
                break;
            }
            if (currentByte < '0' || currentByte > '7') {
                throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, offset, currentByte));
            }
            foundDigit = true;
            result = (result << 3) + (currentByte - '0');
        }

        if (!foundDigit) {
            // No digits found and not the special all-NUL case handled above
            throw new IllegalArgumentException("No octal digits found");
        }

        if (trailerPos == -1) {
            // Must have trailing NUL or space
            throw new IllegalArgumentException("Missing trailing NUL or space");
        }

        // Remaining bytes after trailer must be NUL or space only
        for (int j = trailerPos; j < end; j++) {
            byte b = buffer[j];
            if (b != 0 && b != (byte) ' ') {
                throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, offset, b));
            }
        }

        return result;
    }