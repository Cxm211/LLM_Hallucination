private void writeCharsBetween(Mapping prev, Mapping next)
        throws IOException {
      int nextLine = getAdjustedLine(next.startPosition);
      int nextCol = getAdjustedCol(next.startPosition);
      int id = (prev != null) ? prev.id : UNMAPPED;
      writeCharsUpTo(nextLine, nextCol, id);
    }