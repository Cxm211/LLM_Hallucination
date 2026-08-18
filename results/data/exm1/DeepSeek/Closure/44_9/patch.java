void add(String newcode) {
    maybeEndStatement();
    if (newcode.length() == 0) {
      return;
    }
    char c = newcode.charAt(0);
    char last = getLastChar();
    if ((isWordChar(c) || c == '\\') && isWordChar(last) ||
        (c == '/' && last == '/')) {
      append(" ");
    }
    append(newcode);
  }