public static long parseOctal(final byte[] buffer, final int offset, final int length) {
    long result = 0;
    int end = offset + length;
    int start = offset;

    if (length < 2) {
        throw new IllegalArgumentException("Length " + length + " must be at least 2");
    }

    // Skip leading spaces
    while (start < end) {
        if (buffer[start] == ' ') {
            start++;
        } else {
            break;
        }
    }

    // After skipping leading spaces, check if the first byte is NUL (empty field)
    if (start < end && buffer[start] == 0) {
        return 0L;
    }

    // Trim all trailing NULs and spaces.
    byte trailer = buffer[end - 1];
    while (start < end && (trailer == 0 || trailer == ' ')) {
        end--;
        if (start < end) {
            trailer = buffer[end - 1];
        }
    }

    if (start == end) {
        throw new IllegalArgumentException(
                exceptionMessage(buffer, offset, length, start, trailer));
    }

    for (; start < end; start++) {
        final byte currentByte = buffer[start];
        // CheckStyle:MagicNumber OFF
        if (currentByte < '0' || currentByte > '7') {
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, start, currentByte));
        }
        result = (result << 3) + (currentByte - '0'); // convert from ASCII
        // CheckStyle:MagicNumber ON
    }

    return result;
}