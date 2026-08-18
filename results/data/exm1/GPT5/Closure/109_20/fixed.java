// ===== FIXED com.google.javascript.jscomp.parsing.JsDocInfoParser :: parseContextTypeExpression(JsDocToken) [lines 1907-1913] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-109-fixed/src/com/google/javascript/jscomp/parsing/JsDocInfoParser.java =====
  private Node parseContextTypeExpression(JsDocToken token) {
    if (token == JsDocToken.QMARK) {
      return newNode(Token.QMARK);
    } else {
      return parseBasicTypeExpression(token);
    }
  }
