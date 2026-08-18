void appendLineMappings() throws IOException {
      if (mappings.isEmpty()) {
        out.append("[]\n");
        return;
      }
      openLine();

      Deque<Mapping> stack = new ArrayDeque<Mapping>();
      for (Mapping m : mappings) {
        while (!stack.isEmpty() && !isOverlapped(stack.peek(), m)) {
          Mapping previous = stack.pop();
          writeClosedMapping(previous);
        }

        Mapping parent = stack.peek();
        writeCharsBetween(parent, m);

        stack.push(m);
      }

      while (!stack.isEmpty()) {
        Mapping m = stack.pop();
        writeClosedMapping(m);
      }
      closeLine();
    }