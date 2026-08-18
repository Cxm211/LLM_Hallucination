public static boolean isJSIdentifier(String s) {
      if (s == null) {
        return false;
      }
      int length = s.length();

      if (length == 0) {
        return false;
      }

      // Disallow JS reserved words and literals as identifiers
      String lower = s;
      switch (lower) {
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
        case "null":
        case "true":
        case "false":
          return false;
      }

      char ch0 = s.charAt(0);
      if (!(ch0 == '$' || ch0 == '_' || Character.isLetter(ch0))) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        char ch = s.charAt(i);
        if (!(ch == '$' || ch == '_' || Character.isLetterOrDigit(ch))) {
          return false;
        }
      }

      return true;
    }