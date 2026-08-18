public static long parseOctal(final byte[] buffer, final int offset, final int length) {
    long    result = 0;
    int     end = offset + length;
    int     start = offset;

    // Skip leading spaces and nulls
    while (start < end && (buffer[start] == ' ' || buffer[start] == 0)) {
        start++;
    }

    // Skip trailing spaces and nulls
    while (start < end && (buffer[end-1] == ' ' || buffer[end-1] == 0)) {
        end--;
    }

    for ( ;start < end; start++) {
        final byte currentByte = buffer[start];
        if (currentByte < '0' || currentByte > '7'){
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, start, currentByte));
        }
        result = (result << 3) + (currentByte - '0');
    }

    return result;
}