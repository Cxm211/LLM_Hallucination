private static void pad4(StringBuffer buffer, int value) {
        if (value < 0) {
            buffer.append('-');
            value = -value;
        }
        if (value < 10) {
            buffer.append('0').append('0').append('0');
        } else if (value < 100) {
            buffer.append('0').append('0');
        } else if (value < 1000) {
            buffer.append('0');
        }
        buffer.append(value);
    }