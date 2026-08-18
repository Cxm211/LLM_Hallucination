public ChecksumCalculatingInputStream(final Checksum checksum, final InputStream in) {
    if (checksum == null || in == null) {
        throw new NullPointerException();
    }
    super(in);
    this.checksum = checksum;
    this.in = in;
}