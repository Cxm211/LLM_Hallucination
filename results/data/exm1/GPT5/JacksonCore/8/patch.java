public char[] getTextBuffer()
    {
        // Are we just using shared input buffer?
        if (_inputStart >= 0) {
            // If empty, return empty buffer, not the (possibly null) input buffer
            if (_inputLen <= 0) {
                return new char[0];
            }
            return _inputBuffer;
        }
        if (_resultArray != null)  return _resultArray;
        if (_resultString != null) {
            return (_resultArray = _resultString.toCharArray());
        }
        // Nope; but does it fit in just one segment?
        if (!_hasSegments)  {
            // If no content, return empty buffer instead of the current segment
            if (_currentSize <= 0) {
                return new char[0];
            }
            return _currentSegment;
        }
        // Nope, need to have/create a non-segmented array and return it
        return contentsAsArray();
    }