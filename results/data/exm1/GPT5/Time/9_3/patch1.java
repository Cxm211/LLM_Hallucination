public static DateTimeZone forOffsetMillis(int millisOffset) {
        if (millisOffset == 0) {
            return DateTimeZone.UTC;
        }
        String id = printOffset(millisOffset);
        return fixedOffsetZone(id, millisOffset);
    }