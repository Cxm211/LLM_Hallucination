public String getLine(int lineNumber) {
    String js = "";
    try {
      // NOTE(nicksantos): Right now, this is optimized for few warnings.
      // This is probably the right trade-off, but will be slow if there
      // are lots of warnings in one file.
      js = getCode();
    } catch (IOException e) {
      return null;
    }

    int pos = 0;
    int startLine = 1;

    // If we've saved a previous offset and it's for a line less than the
    // one we're searching for, then start at that point.
    if (lineNumber >= lastLine) {
      pos = lastOffset;
      startLine = lastLine;
    }

    for (int n = startLine; n < lineNumber; n++) {
      int nextpos = js.indexOf('\n', pos);
      if (nextpos == -1) {
        return null;
      }
      pos = nextpos + 1;
    }

    // Remember this offset for the next search we do.
    lastOffset = pos;
    lastLine = lineNumber;

    if (pos >= js.length()) {
      // Reached end of file; no content for this line.
      return null;
    }

    int end = js.indexOf('\n', pos);
    if (end == -1) {
      // No further newline; return until EOF.
      return js.substring(pos);
    } else {
      return js.substring(pos, end);
    }
  }