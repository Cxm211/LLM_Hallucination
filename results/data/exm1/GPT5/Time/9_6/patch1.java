public static DateTimeZone forOffsetMillis(int millisOffset) {
        if (millisOffset == 0) {
            return DateTimeZone.UTC;
        }
        if (millisOffset <= -DateTimeConstants.MILLIS_PER_DAY || millisOffset >= DateTimeConstants.MILLIS_PER_DAY) {
            throw new IllegalArgumentException("Offset is too large");
        }
        String id = printOffset(millisOffset);
        return fixedOffsetZone(id, millisOffset);
    }