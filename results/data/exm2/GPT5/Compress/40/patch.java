public long readBits(final int count) throws IOException {
        if (count < 0 || count > MAXIMUM_CACHE_SIZE) {
            throw new IllegalArgumentException("count must not be negative or greater than " + MAXIMUM_CACHE_SIZE);
        }
        while (bitsCachedSize < count) {
            final long nextByte = in.read();
            if (nextByte < 0) {
                return nextByte;
            }
            if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                bitsCached |= (nextByte << bitsCachedSize);
                bitsCachedSize += 8;
            } else {
                if (bitsCachedSize > MAXIMUM_CACHE_SIZE - 8 && bitsCachedSize < count) {
                    final int deficit = count - bitsCachedSize; // 1..7
                    final long bitsOutSpecial = ((bitsCached & MASKS[bitsCachedSize]) << deficit) | (nextByte >> (8 - deficit));
                    bitsCached = nextByte & MASKS[8 - deficit];
                    bitsCachedSize = 8 - deficit;
                    return bitsOutSpecial;
                }
                bitsCached <<= 8;
                bitsCached |= nextByte;
                bitsCachedSize += 8;
            }
        }
        
        final long bitsOut;
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            bitsOut = (bitsCached & MASKS[count]);
            bitsCached >>>= count;
        } else {
            bitsOut = (bitsCached >> (bitsCachedSize - count)) & MASKS[count];
        }
        bitsCachedSize -= count;
        return bitsOut;
    }