private void writeClosedMapping(Mapping m) throws IOException {
      int nextLine = getAdjustedLine(m.endPosition);
      int nextCol = getAdjustedCol(m.endPosition);
      if (line < nextLine || (line == nextLine && col < nextCol)) {
        writeCharsUpTo(nextLine, nextCol, m.id);
      }
    }