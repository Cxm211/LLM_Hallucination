  void add(String newcode) {
    maybeEndStatement();

    if (newcode.length() == 0) {
      return;
    }

    char c = newcode.charAt(0);
    char last = getLastChar();
    if ((isWordChar(c) || c == '\\') &&
        isWordChar(last)) {
      // need space to separate. This is not pretty printing.
      // For example: "return foo;"
      append(" ");
    } else if (c == '/' && last == '/') {
      // Avoid creating '//' which would start a comment when a division
      // operator is followed by a token starting with '/'.
      append(" ");
    }

    append(newcode);
  }