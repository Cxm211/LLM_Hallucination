public static boolean isJSIdentifier(String s) {
  int length = s.length();
  if (length == 0) {
    return false;
  }
  for (int i = 0; i < length; i++) {
    char c = s.charAt(i);
    if (c > 0x7F) {
      return false;
    }
    if (i == 0) {
      if (!((c == '$' || c == '_') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
        return false;
      }
    } else {
      if (!((c == '$' || c == '_') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9'))) {
        return false;
      }
    }
  }
  String lower = s;
  switch (lower) {
    case "break": case "case": case "catch": case "continue":
    case "debugger": case "default": case "delete": case "do":
    case "else": case "finally": case "for": case "function":
    case "if": case "in": case "instanceof": case "new":
    case "return": case "switch": case "this": case "throw":
    case "try": case "typeof": case "var": case "void":
    case "while": case "with": case "class": case "const":
    case "enum": case "export": case "extends": case "import":
    case "super": case "implements": case "interface": case "let":
    case "package": case "private": case "protected": case "public":
    case "static": case "yield": case "null": case "true": case "false":
      return false;
    default:
      return true;
  }
}