private static String cacheString(final char[] charBuf, final String[] stringCache, final int start, final int count) {
        if (count > maxStringCacheLen)
            return new String(charBuf, start, count);

        int hash = 0;
        int offset = start;
        for (int i = 0; i < count; i++) {
            hash = 31 * hash + charBuf[offset++];
        }

        final int index = (hash & 0x7FFFFFFF) % stringCache.length;
        String cached = stringCache[index];

        if (cached == null) {
            cached = new String(charBuf, start, count);
            stringCache[index] = cached;
        } else {
            if (rangeEquals(charBuf, start, count, cached)) {
                return cached;
            } else {
                cached = new String(charBuf, start, count);
                stringCache[index] = cached;
            }
        }
        return cached;
    }