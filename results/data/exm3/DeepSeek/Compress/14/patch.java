    public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long    result = 0;
        int     end = offset + length;
        int     start = offset;

        if (length <= 0) {
            return 0L;
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

        // Skip trailing NUL or space
        while (start < end) {
            byte trailer = buffer[end - 1];
            if (trailer == 0 || trailer == ' ') {
                end--;
            } else {
                break;
            }
        }

        for ( ;start < end; start++) {
            final byte currentByte = buffer[start];
            // CheckStyle:MagicNumber OFF
            if (currentByte < '0' || currentByte > '7'){
                throw new IllegalArgumentException(
                        exceptionMessage(buffer, offset, length, start, currentByte));
            }
            result = (result << 3) + (currentByte - '0'); // convert from ASCII
            // CheckStyle:MagicNumber ON
        }

        return result;
    }