// buggy code
  void add(String newcode) {
    maybeEndStatement();

    if (newcode.length() == 0) {
      return;
    }

    char c = newcode.charAt(0);
    if ((isWordChar(c) || c == '\\') &&
        isWordChar(getLastChar())) {
      // need space to separate. This is not pretty printing.
      // For example: "return foo;"
      append(" ");
      // Do not allow a forward slash to appear after a DIV.
      // For example,
      // REGEXP DIV REGEXP
      // is valid and should print like
      // / // / /
    }

    // Ensure we don't create '//' which starts a comment when concatenating
    // tokens. If the last char is '/' and the new token also starts with '/',
    // insert a space between them.
    if (c == '/' && getLastChar() == '/') {
      append(" ");
    }

    append(newcode);
  }