public static long parseOctal(final byte[] buffer, final int offset, final int length) {
    long result = 0;
    int end = offset + length;
    int start = offset;

    // Skip leading NULs and spaces
    while (start < end && (buffer[start] == 0 || buffer[start] == ' ')) {
        start++;
    }

    // Trim trailing NULs and spaces
    while (end > start && (buffer[end-1] == 0 || buffer[end-1] == ' ')) {
        end--;
    }

    if (start == end) {
        return 0L;
    }

    for ( ; start < end; start++) {
        final byte currentByte = buffer[start];
        if (currentByte < '0' || currentByte > '7') {
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, start, currentByte));
        }
        result = (result << 3) + (currentByte - '0');
    }

    return result;
}