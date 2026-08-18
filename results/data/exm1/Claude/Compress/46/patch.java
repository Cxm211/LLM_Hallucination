private static ZipLong unixTimeToZipLong(long l) {
    final long TWO_TO_31 = 0x80000000L;
    if (l >= TWO_TO_31) {
        throw new IllegalArgumentException("X5455 timestamps must fit in a signed 32 bit integer: " + l);
    }
    return new ZipLong(l);
}