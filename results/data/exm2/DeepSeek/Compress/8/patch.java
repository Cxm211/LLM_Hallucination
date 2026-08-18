public static long parseOctal(final byte[] buffer, final int offset, final int length) {
    if (length < 2) {
        throw new IllegalArgumentException("Length " + length + " must be at least 2");
    }
    long result = 0;
    boolean stillPadding = true;
    byte firstPaddingChar = 0;
    int end = offset + length;
    int start = offset;

    // If the first byte is NUL, then all bytes must be NUL.
    if (buffer[offset] == 0) {
        for (int i = offset; i < end; i++) {
            if (buffer[i] != 0) {
                throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, i, buffer[i]));
            }
        }
        return 0;
    }

    boolean trailerFound = false;
    for (int i = start; i < end; i++) {
        final byte currentByte = buffer[i];
        if (currentByte == 0) {
            if (stillPadding) {
                throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, i, currentByte));
            } else {
                trailerFound = true;
                break;
            }
        }

        if (stillPadding) {
            if (currentByte == (byte) ' ' || currentByte == '0') {
                if (firstPaddingChar == 0) {
                    firstPaddingChar = currentByte;
                } else if (currentByte != firstPaddingChar) {
                    throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, i, currentByte));
                }
                continue;
            } else {
                stillPadding = false;
                // fall through to digit check
            }
        }

        // Now we are parsing digits
        if (currentByte == (byte) ' ') {
            trailerFound = true;
            break;
        }
        if (currentByte < '0' || currentByte > '7') {
            throw new IllegalArgumentException(
                exceptionMessage(buffer, offset, length, i, currentByte));
        }
        result = (result << 3) + (currentByte - '0');
    }

    if (!trailerFound && !stillPadding) {
        throw new IllegalArgumentException(
            exceptionMessage(buffer, offset, length, end-1, buffer[end-1]));
    }
    return result;
}