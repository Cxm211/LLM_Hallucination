    public void reparseCentralDirectoryData(boolean hasUncompressedSize,
                                            boolean hasCompressedSize,
                                            boolean hasRelativeHeaderOffset,
                                            boolean hasDiskStart)
        throws ZipException {
        if (rawCentralDirectoryData != null) {
            int expectedLength = (hasUncompressedSize ? 8 : 0)
                + (hasCompressedSize ? 8 : 0)
                + (hasRelativeHeaderOffset ? 8 : 0)
                + (hasDiskStart ? 4 : 0);
            if (rawCentralDirectoryData.length != expectedLength) {
                throw new ZipException("central directory zip64 extended"
                                       + " information extra field's length"
                                       + " doesn't match central directory"
                                       + " data.  Expected length "
                                       + expectedLength + " but is "
                                       + rawCentralDirectoryData.length);
            }
            int offset = 0;
            if (hasUncompressedSize) {
                size = new ZipEightByteInteger(rawCentralDirectoryData, offset);
                offset += 8;
            }
            if (hasCompressedSize) {
                compressedSize = new ZipEightByteInteger(rawCentralDirectoryData,
                                                         offset);
                offset += 8;
            }
            if (hasRelativeHeaderOffset) {
                relativeHeaderOffset =
                    new ZipEightByteInteger(rawCentralDirectoryData, offset);
                offset += 8;
            }
            if (hasDiskStart) {
                diskStart = new ZipLong(rawCentralDirectoryData, offset);
                offset += 4;
            }
        }
    }