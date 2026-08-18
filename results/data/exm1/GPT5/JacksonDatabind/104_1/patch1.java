private static void pad4(StringBuffer buffer, int value) {
        // For years beyond 4 digits, ISO-8601 requires a leading '+' for positive years
        if (value > 9999) {
            buffer.append('+');
            buffer.append(value);
            return;
        }
        // For negative years, just append as-is (no zero-padding), as behavior isn't well specified
        if (value < 0) {
            buffer.append(Integer.toString(value));
            return;
        }
        int h = value / 100;
        if (h == 0) {
            buffer.append('0').append('0');
        } else {
            pad2(buffer, h);
            value -= (100 * h);
        }
        pad2(buffer, value);
    }