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
      // Do not allow a forward slash to appear after a DIV.
      // For example,
      // REGEXP DIV REGEXP
      // is valid and should print like
      // / // / /
    } else if (c == '/' && last == '/') {
      append(" ");
    }

    append(newcode);
  }