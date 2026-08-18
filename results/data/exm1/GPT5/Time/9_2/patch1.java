public static DateTimeZone forOffsetMillis(int millisOffset) {
        // zero offset must return the UTC singleton
        if (millisOffset == 0) {
            return DateTimeZone.UTC;
        }
        String id = printOffset(millisOffset);
        return fixedOffsetZone(id, millisOffset);
    }