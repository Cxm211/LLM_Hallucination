private static ZipLong unixTimeToZipLong(long l) {
        final long MAX_SIGNED_32 = 0x7FFFFFFFL;
        if (l < 0 || l > MAX_SIGNED_32) {
            throw new IllegalArgumentException("X5455 timestamps must fit in a signed 32 bit integer: " + l);
        }
        return new ZipLong(l);
    }