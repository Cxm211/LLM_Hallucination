    private static ZipLong unixTimeToZipLong(long l) {
        final long TWO_TO_32 = 0x100000000L;
        if (l < -0x80000000L || l > 0x7fffffffL) {
            throw new IllegalArgumentException("X5455 timestamps must fit in a signed 32 bit integer: " + l);
        }
        return new ZipLong(l);
    }