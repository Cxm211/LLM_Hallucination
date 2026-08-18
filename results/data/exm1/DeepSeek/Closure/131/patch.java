public static boolean isJSIdentifier(String s) {
  int length = s.length();

  if (length == 0) {
    return false;
  }

  // Check if the first character is a valid JavaScript identifier start
  char first = s.charAt(0);
  if (!(Character.isLetter(first) || first == '_' || first == '$' ||
        Character.isUnicodeIdentifierStart(first))) {
    return false;
  }

  for (int i = 1; i < length; i++) {
    char c = s.charAt(i);
    if (!(Character.isLetterOrDigit(c) || c == '_' || c == '$' ||
          Character.isUnicodeIdentifierPart(c))) {
      return false;
    }
  }

  // Check against JavaScript reserved words
  if (isReservedWord(s)) {
    return false;
  }

  return true;
}

private static boolean isReservedWord(String s) {
  // Use a static set of reserved words for performance
  switch (s) {
    case "break":
    case "case":
    case "catch":
    case "continue":
    case "debugger":
    case "default":
    case "delete":
    case "do":
    case "else":
    case "finally":
    case "for":
    case "function":
    case "if":
    case "in":
    case "instanceof":
    case "new":
    case "return":
    case "switch":
    case "this":
    case "throw":
    case "try":
    case "typeof":
    case "var":
    case "void":
    case "while":
    case "with":
    case "class":
    case "const":
    case "enum":
    case "export":
    case "extends":
    case "import":
    case "super":
    case "implements":
    case "interface":
    case "let":
    case "package":
    case "private":
    case "protected":
    case "public":
    case "static":
    case "yield":
      return true;
    default:
      return false;
  }
}