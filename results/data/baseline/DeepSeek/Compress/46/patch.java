    private static ZipLong unixTimeToZipLong(long l) {
        if (l >= 0x80000000L) {
            throw new IllegalArgumentException("X5455 timestamps must fit in a signed 32 bit integer: " + l);
        }
        return new ZipLong(l);
    }