void add(String newcode) {
    maybeEndStatement();

    if (newcode.length() == 0) {
      return;
    }

    char c = newcode.charAt(0);
    if ((isWordChar(c) || c == '\\' || c == '/') &&
        (isWordChar(getLastChar()) || getLastChar() == '/')) {
      append(" ");
    }

    append(newcode);
  }