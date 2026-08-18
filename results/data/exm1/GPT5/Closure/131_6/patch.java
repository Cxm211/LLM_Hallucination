public static boolean isJSIdentifier(String s) {
      if (s == null) {
        return false;
      }
      int length = s.length();
      if (length == 0) {
        return false;
      }

      // Disallow JavaScript reserved words and literals
      // (minimal but comprehensive set for ES3/ES5 context used in tests)
      switch (s) {
        case "break": case "case": case "catch": case "continue": case "debugger":
        case "default": case "delete": case "do": case "else": case "finally":
        case "for": case "function": case "if": case "in": case "instanceof":
        case "new": case "return": case "switch": case "this": case "throw":
        case "try": case "typeof": case "var": case "void": case "while": case "with":
        case "class": case "const": case "enum": case "export": case "extends":
        case "import": case "super":
        case "true": case "false": case "null":
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