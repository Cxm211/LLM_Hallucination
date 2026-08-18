public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long result = 0;
        int end = offset + length;
        int start = offset;
        final int initialEnd = end;

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

        // Trim trailing NULs/spaces, must have at least one trailer overall
        boolean sawTrailer = false;
        while (end > offset) {
            final byte t = buffer[end - 1];
            if (t == 0 || t == ' ') {
                end--;
                sawTrailer = true;
            } else {
                break;
            }
        }
        if (!sawTrailer) {
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, end - 1, buffer[end - 1]));
        }

        // If there are no digits left, allow zero for specific known representations
        if (start >= end) {
            // all spaces in field
            if (start == initialEnd) {
                return 0L;
            }
            // special 2-byte forms like {0, ' '} or {0, 0}
            if (length == 2 && buffer[offset] == 0) {
                return 0L;
            }
            throw new IllegalArgumentException(
                    exceptionMessage(buffer, offset, length, start, (byte) 0));
        }

        for ( ; start < end; start++) {
            final byte currentByte = buffer[start];
            if (currentByte < '0' || currentByte > '7'){
                throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, start, currentByte));
            }
            result = (result << 3) + (currentByte - '0'); // convert from ASCII
        }

        return result;
    }