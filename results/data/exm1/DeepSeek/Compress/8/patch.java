    public static long parseOctal(final byte[] buffer, final int offset, final int length) {
        long result = 0;
        int end = offset + length;
        int i = offset;

        if (length < 2) {
            throw new IllegalArgumentException("Length " + length + " is < 2");
        }

        boolean allNuls = true;
        for (int j = offset; j < end; j++) {
            if (buffer[j] != 0) {
                allNuls = false;
                break;
            }
        }
        if (allNuls) {
            return 0;
        }

        if (length == 2 && buffer[offset] == 0 && buffer[offset + 1] == ' ') {
            return 0;
        }

        while (i < end && buffer[i] == ' ') {
            i++;
        }

        if (i >= end || buffer[i] == 0) {
            throw new IllegalArgumentException("not all NULs");
        }

        while (i < end) {
            byte b = buffer[i];
            if (b == ' ' || b == 0) {
                boolean allTerminators = true;
                for (int j = i + 1; j < end; j++) {
                    if (buffer[j] != ' ' && buffer[j] != 0) {
                        allTerminators = false;
                        break;
                    }
                }
                if (allTerminators) {
                    break;
                } else {
                    if (b == ' ') {
                        throw new IllegalArgumentException("embedded space");
                    } else {
                        throw new IllegalArgumentException("embedded NUL");
                    }
                }
            }
            if (b < '0' || b > '7') {
                throw new IllegalArgumentException("Invalid octal digit: " + (char) b);
            }
            result = (result << 3) + (b - '0');
            i++;
        }

        if (i == end) {
            throw new IllegalArgumentException("no trailer");
        }

        return result;
    }