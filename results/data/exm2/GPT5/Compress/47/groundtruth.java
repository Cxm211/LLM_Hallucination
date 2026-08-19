// ===== FIXED org.apache.commons.compress.archivers.zip.ZipArchiveInputStream :: canReadEntryData(ArchiveEntry) [lines 411-419] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-47-fixed/src/main/java/org/apache/commons/compress/archivers/zip/ZipArchiveInputStream.java =====
    public boolean canReadEntryData(final ArchiveEntry ae) {
        if (ae instanceof ZipArchiveEntry) {
            final ZipArchiveEntry ze = (ZipArchiveEntry) ae;
            return ZipUtil.canHandleEntryData(ze)
                && supportsDataDescriptorFor(ze)
                && supportsCompressedSizeFor(ze);
        }
        return false;
    }

// ===== FIXED org.apache.commons.compress.archivers.zip.ZipArchiveInputStream :: read(byte[], int, int) [lines 422-466] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-47-fixed/src/main/java/org/apache/commons/compress/archivers/zip/ZipArchiveInputStream.java =====
    public int read(final byte[] buffer, final int offset, final int length) throws IOException {
        if (closed) {
            throw new IOException("The stream is closed");
        }

        if (current == null) {
            return -1;
        }

        // avoid int overflow, check null buffer
        if (offset > buffer.length || length < 0 || offset < 0 || buffer.length - offset < length) {
            throw new ArrayIndexOutOfBoundsException();
        }

        ZipUtil.checkRequestedFeatures(current.entry);
        if (!supportsDataDescriptorFor(current.entry)) {
            throw new UnsupportedZipFeatureException(UnsupportedZipFeatureException.Feature.DATA_DESCRIPTOR,
                    current.entry);
        }
        if (!supportsCompressedSizeFor(current.entry)) {
            throw new UnsupportedZipFeatureException(UnsupportedZipFeatureException.Feature.UNKNOWN_COMPRESSED_SIZE,
                    current.entry);
        }

        int read;
        if (current.entry.getMethod() == ZipArchiveOutputStream.STORED) {
            read = readStored(buffer, offset, length);
        } else if (current.entry.getMethod() == ZipArchiveOutputStream.DEFLATED) {
            read = readDeflated(buffer, offset, length);
        } else if (current.entry.getMethod() == ZipMethod.UNSHRINKING.getCode()
                || current.entry.getMethod() == ZipMethod.IMPLODING.getCode()
                || current.entry.getMethod() == ZipMethod.ENHANCED_DEFLATED.getCode()
                || current.entry.getMethod() == ZipMethod.BZIP2.getCode()) {
            read = current.in.read(buffer, offset, length);
        } else {
            throw new UnsupportedZipFeatureException(ZipMethod.getMethodByCode(current.entry.getMethod()),
                    current.entry);
        }

        if (read >= 0) {
            current.crc.update(buffer, offset, read);
        }

        return read;
    }
