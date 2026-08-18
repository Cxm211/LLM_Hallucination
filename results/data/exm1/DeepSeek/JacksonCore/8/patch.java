public char[] getTextBuffer()
    {
        // Are we just using shared input buffer?
        if (_inputStart >= 0) return _inputBuffer;
        // Check result string first, before result array, to avoid stale arrays
        if (_resultString != null) {
            return (_resultArray = _resultString.toCharArray());
        }
        if (_resultArray != null)  return _resultArray;
        // Nope; but does it fit in just one segment?
        if (!_hasSegments)  return _currentSegment;
        // Nope, need to have/create a non-segmented array and return it
        return contentsAsArray();
    }