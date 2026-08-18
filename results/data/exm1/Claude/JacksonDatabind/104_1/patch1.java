private static void pad4(StringBuffer buffer, int value) {
        int h = value / 100;
        if (h < 10) {
            buffer.append('0');
            if (h == 0) {
                buffer.append('0');
            } else {
                buffer.append((char) ('0' + h));
            }
        } else {
            pad2(buffer, h);
        }
        pad2(buffer, value % 100);
    }