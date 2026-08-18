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
    }

    // Do not allow a forward slash to appear after a DIV.
    // Ensure we don't accidentally create a '//' comment when
    // a division operator is followed by a regexp literal.
    if (c == '/' && last == '/') {
      append(" ");
    }

    append(newcode);
  }