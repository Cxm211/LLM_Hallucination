public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long result = 0;
        int end = offset + length;
        int start = offset;

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
        while (start < end && buffer[start] == ' '){
            start++;
        }

        // Strip all trailing NULs or spaces
        while (start < end) {
            final byte trailer = buffer[end - 1];
            if (trailer == 0 || trailer == ' ') {
                end--;
            } else {
                break;
            }
        }

        // If nothing remains, treat as zero
        if (start >= end) {
            return 0L;
        }

        for (; start < end; start++) {
            final byte currentByte = buffer[start];
            if (currentByte < '0' || currentByte > '7'){
                throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, start, currentByte));
            }
            result = (result << 3) + (currentByte - '0'); // convert from ASCII
        }

        return result;
    }