void add(String newcode) {
    maybeEndStatement();

    if (newcode.length() == 0) {
      return;
    }

    char c = newcode.charAt(0);
    char last = getLastChar();

    if ((isWordChar(c) || c == '\\') && isWordChar(last)) {
      // need space to separate. This is not pretty printing.
      // For example: "return foo;"
      append(" ");
    } else if (last == '/' && (c == '/' || c == '*')) {
      // Do not allow a forward slash to appear after a DIV or form a comment start.
      // Ensure sequences like "/ /" or "/ *" instead of "//" or "/*".
      append(" ");
    }

    append(newcode);
  }