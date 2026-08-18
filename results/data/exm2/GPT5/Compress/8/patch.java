public static long parseOctal(final byte[] buffer, final int offset, final int length) {
    if (length < 2) {
        throw new IllegalArgumentException();
    }
    final int end = offset + length;
    // Check if all NULs
    boolean allNul = true;
    for (int i = offset; i < end; i++) {
        if (buffer[i] != 0) {
            allNul = false;
            break;
        }
    }
    if (allNul) {
        return 0L;
    }

    long result = 0L;
    boolean seenTrailer = false;
    int trailerPos = -1;
    int i = offset;
    int digitsRead = 0;

    // Skip leading spaces only
    while (i < end && buffer[i] == (byte) ' ') {
        i++;
    }

    for (; i < end; i++) {
        final byte b = buffer[i];
        if (b == 0 || b == (byte) ' ') {
            seenTrailer = true;
            trailerPos = i;
            break;
        }
        if (b < (byte) '0' || b > (byte) '7') {
            throw new IllegalArgumentException();
        }
        result = (result << 3) + (b - (byte) '0');
        digitsRead++;
    }

    if (digitsRead == 0) {
        throw new IllegalArgumentException();
    }
    if (!seenTrailer) {
        throw new IllegalArgumentException();
    }
    // Remaining bytes must be NUL or space
    for (int j = trailerPos; j < end; j++) {
        final byte b = buffer[j];
        if (b != 0 && b != (byte) ' ') {
            throw new IllegalArgumentException();
        }
    }
    return result;
}