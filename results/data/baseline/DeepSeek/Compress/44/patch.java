public ChecksumCalculatingInputStream(final Checksum checksum, final InputStream in) {
    super(in);
    this.checksum = checksum;
    this.in = in;
}