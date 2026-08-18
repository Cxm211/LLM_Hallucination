public static byte[] encodeBase64(byte[] binaryData, boolean isChunked, boolean urlSafe, int maxResultSize) {
    if (binaryData == null || binaryData.length == 0) {
        return binaryData;
    }

    long len = getEncodeLength(binaryData, MIME_CHUNK_SIZE, CHUNK_SEPARATOR);
    if (len > maxResultSize) {
        throw new IllegalArgumentException("Input array too big, the output array would be bigger (" +
            len +
            ") than the specified maxium size of " +
            maxResultSize);
    }
                
    Base64 b64;
    if (isChunked) {
        b64 = new Base64(MIME_CHUNK_SIZE, CHUNK_SEPARATOR, urlSafe);
    } else {
        b64 = new Base64(0, null, urlSafe);
    }
    return b64.encode(binaryData);
}