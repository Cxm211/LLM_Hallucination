void appendLineMappings() throws IOException {
      // If there are no mappings, emit a single empty line map.
      if (mappings.isEmpty()) {
        openLine();
        closeLine();
        return;
      }

      // Start the first line.
      openLine();

      // Append the line mapping entries using the mapping stack algorithm.
      Deque<Mapping> stack = new ArrayDeque<Mapping>();
      for (Mapping m : mappings) {
        // Find the closest ancestor of the current mapping:
        // An overlapping mapping is an ancestor of the current mapping, any
        // non-overlapping mappings are siblings (or cousins) and must be
        // closed in the reverse order of when they encountered.
        while (!stack.isEmpty() && !isOverlapped(stack.peek(), m)) {
          Mapping previous = stack.pop();
          writeClosedMapping(previous);
        }

        // Any gaps between the current line position and the start of the
        // current mapping belong to the parent.
        Mapping parent = stack.peek();
        writeCharsBetween(parent, m);

        stack.push(m);
      }

      // There are no more children to be had, simply close the remaining
      // mappings in the reverse order of when they encountered.
      while (!stack.isEmpty()) {
        Mapping m = stack.pop();
        writeClosedMapping(m);
      }
      closeLine();
    }