public static boolean isJSIdentifier(String s) {
      if (s == null) {
        return false;
      }
      int length = s.length();

      if (length == 0 ||
          !Character.isJavaIdentifierStart(s.charAt(0))) {
        return false;
      }

      for (int i = 1; i < length; i++) {
        if (
            !Character.isJavaIdentifierPart(s.charAt(i))) {
          return false;
        }
      }

      // Disallow JavaScript reserved words
      String id = s;
      switch (id) {
        case "break": case "case": case "catch": case "continue":
        case "debugger": case "default": case "delete": case "do":
        case "else": case "finally": case "for": case "function":
        case "if": case "in": case "instanceof": case "new":
        case "return": case "switch": case "this": case "throw":
        case "try": case "typeof": case "var": case "void":
        case "while": case "with":
        // Future reserved (ES3/ES5)
        case "abstract": case "boolean": case "byte": case "char":
        case "class": case "const": case "debugger": case "double":
        case "enum": case "export": case "extends": case "final":
        case "float": case "goto": case "implements": case "import":
        case "int": case "interface": case "long": case "native":
        case "package": case "private": case "protected": case "public":
        case "short": case "static": case "super": case "synchronized":
        case "throws": case "transient": case "volatile":
        // Literals
        case "null": case "true": case "false":
          return false;
        default:
          // ok
      }

      return true;
    }