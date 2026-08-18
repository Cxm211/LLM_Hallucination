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
                
        Base64 b64 = isChunked ? new Base64(urlSafe) : new Base64(0, CHUNK_SEPARATOR, urlSafe);
        return b64.encode(binaryData);
    }

// trigger testcase
public void testCodec112() { // size calculation assumes always chunked
        byte[] in = new byte[] {0};
        byte[] out=Base64.encodeBase64(in);
        Base64.encodeBase64(in, false, false, out.length);
    }
