public int nextInt() throws IOException {
    int p = peeked;
    if (p == PEEKED_NONE) {
      p = doPeek();
    }

    int result;
    if (p == PEEKED_LONG) {
      result = (int) peekedLong;
      if (peekedLong != result) {
        throw new NumberFormatException("Expected an int but was " + peekedLong
            + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
      }
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return result;
    }

    if (p == PEEKED_NUMBER) {
      peekedString = new String(buffer, pos, peekedNumberLength);
      pos += peekedNumberLength;
    } else if (p == PEEKED_SINGLE_QUOTED || p == PEEKED_DOUBLE_QUOTED) {
      peekedString = nextQuotedValue(p == PEEKED_SINGLE_QUOTED ? '\'' : '"');
    } else {
      throw new IllegalStateException("Expected an int but was " + peek()
          + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
    }

    peeked = PEEKED_BUFFERED;
    try {
      result = Integer.parseInt(peekedString);
      peeked = PEEKED_NONE;
      pathIndices[stackSize - 1]++;
      return result;
    } catch (NumberFormatException ignored) {
      // Fall back to parse as a double below.
    }
    double asDouble = Double.parseDouble(peekedString);
    result = (int) asDouble;
    if (result != asDouble) {
      throw new NumberFormatException("Expected an int but was " + peekedString
          + " at line " + getLineNumber() + " column " + getColumnNumber() + " path " + getPath());
    }
    peekedString = null;
    peeked = PEEKED_NONE;
    pathIndices[stackSize - 1]++;
    return result;
  }