    private void writeCharsUpTo(
        int nextLine, int nextCol, int id)
        throws IOException {
      Preconditions.checkState(line <= nextLine, "");
      Preconditions.checkState(line < nextLine || col <= nextCol);

      if (line == nextLine && col == nextCol) {
        // Nothing to do.
        return;
      }

      String idString = (id == UNMAPPED) ? UNMAPPED_STRING : String.valueOf(id);
      for (int i = line; i <= nextLine; i++) {
        if (i == nextLine) {
          for (int j = col; j < nextCol; j++) {
            addCharEntry(idString);
          }
          break;
        }
        closeLine();
        line++;   // move to next line
        openLine();
        col = 0;  // reset column for the new line
      }

      line = nextLine;
      col = nextCol;
    }