public long readBits(final int count) throws IOException {
        if (count < 0 || count > MAXIMUM_CACHE_SIZE) {
            throw new IllegalArgumentException("count must not be negative or greater than " + MAXIMUM_CACHE_SIZE);
        }
        // Fill the cache without risking overflow. In big-endian mode shifting the cache by 8 can overflow
        // once the cache holds more than MAXIMUM_CACHE_SIZE - 8 bits.
        while (bitsCachedSize < count && bitsCachedSize <= MAXIMUM_CACHE_SIZE - 8) {
            final long nextByte = in.read();
            if (nextByte < 0) {
                return nextByte;
            }
            if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                bitsCached |= (nextByte << bitsCachedSize);
            } else {
                bitsCached <<= 8;
                bitsCached |= nextByte;
            }
            bitsCachedSize += 8;
        }

        // If we still don't have enough bits and cannot add another full byte safely, handle the partial byte case
        if (bitsCachedSize < count) {
            final long nextByte = in.read();
            if (nextByte < 0) {
                return nextByte;
            }
            final int needed = count - bitsCachedSize; // 1..7
            if (byteOrder == ByteOrder.BIG_ENDIAN) {
                // Combine existing cache (as the higher-order bits) with the top 'needed' bits of nextByte
                final long bitsOut = (bitsCached << needed) | ((nextByte & 0xFFL) >>> (8 - needed));
                // Keep the remaining lower bits of nextByte in the cache for subsequent reads
                bitsCached = (nextByte & MASKS[8 - needed]);
                bitsCachedSize = 8 - needed;
                return bitsOut;
            } else { // LITTLE_ENDIAN
                // In little-endian the next bits come from the low-order bits of nextByte
                final int oldSize = bitsCachedSize;
                final long lowNeeded = (nextByte & MASKS[needed]);
                final long bitsOut = (bitsCached & MASKS[oldSize]) | (lowNeeded << oldSize);
                // Remaining bits of nextByte become the new cache
                bitsCached = ((nextByte & 0xFFL) >>> needed) & MASKS[8 - needed];
                bitsCachedSize = 8 - needed;
                return bitsOut;
            }
        }

        // Normal extraction when cache has enough bits
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