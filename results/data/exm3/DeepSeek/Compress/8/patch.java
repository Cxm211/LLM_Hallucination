public static long parseOctal(final byte[] buffer, final int offset, final int length) {
    if (length < 2) {
        throw new IllegalArgumentException(
            exceptionMessage(buffer, offset, length, offset, (byte)0));
    }
    long result = 0;
    int i = offset;
    int end = offset + length;
    int start = offset;

    while (i < end && buffer[i] == ' ') {
        i++;
    }

    if (i == end) {
        throw new IllegalArgumentException(
            exceptionMessage(buffer, offset, length, start, (byte)0));
    }
    if (buffer[i] < '0' || buffer[i] > '7') {
        throw new IllegalArgumentException(
            exceptionMessage(buffer, offset, length, start, buffer[i]));
    }

    while (i < end && buffer[i] >= '0' && buffer[i] <= '7') {
        result = (result << 3) + (buffer[i] - '0');
        i++;
    }

    if (i == end) {
        throw new IllegalArgumentException(
            exceptionMessage(buffer, offset, length, start, (byte)0));
    }
    if (!(buffer[i] == 0 || buffer[i] == ' ')) {
        throw new IllegalArgumentException(
            exceptionMessage(buffer, offset, length, start, buffer[i]));
    }

    i++;
    while (i < end) {
        if (!(buffer[i] == 0 || buffer[i] == ' ')) {
            throw new IllegalArgumentException(
                exceptionMessage(buffer, offset, length, start, buffer[i]));
        }
        i++;
    }

    return result;
}