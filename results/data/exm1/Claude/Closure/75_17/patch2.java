static TernaryValue isStrWhiteSpaceChar(int c) {
  switch (c) {
    case '\u000B':
      return TernaryValue.TRUE;
    case ' ':
    case '\n':
    case '\r':
    case '\t':
    case '\u00A0':
    case '\u000C':
    case '\u2028':
    case '\u2029':
    case '\uFEFF':
      return TernaryValue.TRUE;
    default:
      return (Character.getType(c) == Character.SPACE_SEPARATOR)
          ? TernaryValue.TRUE : TernaryValue.FALSE;
  }
}