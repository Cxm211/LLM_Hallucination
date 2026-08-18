public static boolean isJSIdentifier(String s) {
      if (s == null) {
        return false;
      }
      int length = s.length();
      if (length == 0) {
        return false;
      }

      // Disallow JavaScript reserved words and literals.
      if (isJSReservedWord(s)) {
        return false;
      }

      if (!Character.isJavaIdentifierStart(s.charAt(0))) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        if (!Character.isJavaIdentifierPart(s.charAt(i))) {
          return false;
        }
      }

      return true;
    }

    private static boolean isJSReservedWord(String s) {
      switch (s) {
        // Keywords
        case "break": case "case": case "catch": case "continue": case "debugger":
        case "default": case "delete": case "do": case "else": case "finally":
        case "for": case "function": case "if": case "in": case "instanceof":
        case "new": case "return": case "switch": case "this": case "throw":
        case "try": case "typeof": case "var": case "void": case "while":
        case "with":
        // Future reserved words / strict-reserved (ES5+)
        case "class": case "const": case "enum": case "export": case "extends":
        case "import": case "super": case "implements": case "interface":
        case "let": case "package": case "private": case "protected": case "public":
        case "static": case "yield":
        // Literals
        case "null": case "true": case "false":
          return true;
        default:
          return false;
      }
    }