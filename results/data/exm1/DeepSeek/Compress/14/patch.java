public static long parseOctal(final byte[] buffer, final int offset, final int length) {
    long    result = 0;
    int     end = offset + length;
    int     start = offset;

    if (length < 2){
        throw new IllegalArgumentException("Length "+length+" must be at least 2");
    }

    boolean allNUL = true;
    for (int i = start; i < end; i++){
        if (buffer[i] != 0){
            allNUL = false;
            break;
        }
    }
    if (allNUL) {
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

    // Remove all trailing NULs and spaces
    while (end > start && (buffer[end-1] == 0 || buffer[end-1] == ' ')) {
        end--;
    }

    for ( ;start < end; start++) {
        final byte currentByte = buffer[start];
        if (currentByte < '0' || currentByte > '7'){
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, start, currentByte));
        }
        result = (result << 3) + (currentByte - '0'); // convert from ASCII
    }

    return result;
}