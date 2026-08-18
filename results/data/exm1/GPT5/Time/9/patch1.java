public static DateTimeZone forOffsetMillis(int millisOffset) {
        if (millisOffset == 0) {
            return DateTimeZone.UTC;
        }
        int max = DateTimeConstants.MILLIS_PER_DAY - 1;
        if (millisOffset < -max || millisOffset > max) {
            throw new IllegalArgumentException("Millis out of range: " + millisOffset);
        }
        String id = printOffset(millisOffset);
        return fixedOffsetZone(id, millisOffset);
    }