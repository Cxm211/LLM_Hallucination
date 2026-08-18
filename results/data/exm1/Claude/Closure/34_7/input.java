// buggy code
  void add(Node n, Context context) {
    if (!cc.continueProcessing()) {
      return;
    }

    int type = n.getType();
    String opstr = NodeUtil.opToStr(type);
    int childCount = n.getChildCount();
    Node first = n.getFirstChild();
    Node last = n.getLastChild();

    // Handle all binary operators
    if (opstr != null && first != last) {
      Preconditions.checkState(
          childCount == 2,
          "Bad binary operator \"%s\": expected 2 arguments but got %s",
          opstr, childCount);
      int p = NodeUtil.precedence(type);

      // For right-hand-side of operations, only pass context if it's
      // the IN_FOR_INIT_CLAUSE one.
      Context rhsContext = getContextForNoInOperator(context);

      // Handle associativity.
      // e.g. if the parse tree is a * (b * c),
      // we can simply generate a * b * c.
      if (last.getType() == type &&
          NodeUtil.isAssociative(type)) {
        addExpr(first, p, context);
        cc.addOp(opstr, true);
        addExpr(last, p, rhsContext);
      } else if (NodeUtil.isAssignmentOp(n) && NodeUtil.isAssignmentOp(last)) {
        // Assignments are the only right-associative binary operators
        addExpr(first, p, context);
        cc.addOp(opstr, true);
        addExpr(last, p, rhsContext);
      } else {
        addExpr(first, p, context);
        cc.addOp(opstr, true);
        addExpr(last, p + 1, rhsContext);
      }
      return;
    }

    cc.startSourceMapping(n);

    switch (type) {
      case Token.TRY: {
        Preconditions.checkState(first.getNext().isBlock() &&
                !first.getNext().hasMoreThanOneChild());
        Preconditions.checkState(childCount >= 2 && childCount <= 3);

        add("try");
        add(first, Context.PRESERVE_BLOCK);

        // second child contains the catch block, or nothing if there
        // isn't a catch block
        Node catchblock = first.getNext().getFirstChild();
        if (catchblock != null) {
          add(catchblock);
        }

        if (childCount == 3) {
          add("finally");
          add(last, Context.PRESERVE_BLOCK);
        }
        break;
      }

      case Token.CATCH:
        Preconditions.checkState(childCount == 2);
        add("catch(");
        add(first);
        add(")");
        add(last, Context.PRESERVE_BLOCK);
        break;

      case Token.THROW:
        Preconditions.checkState(childCount == 1);
        add("throw");
        add(first);

        // Must have a ';' after a throw statement, otherwise safari can't
        // parse this.
        cc.endStatement(true);
        break;

      case Token.RETURN:
        add("return");
        if (childCount == 1) {
          add(first);
        } else {
          Preconditions.checkState(childCount == 0);
        }
        cc.endStatement();
        break;

      case Token.VAR:
        if (first != null) {
          add("var ");
          addList(first, false, getContextForNoInOperator(context));
        }
        break;

      case Token.LABEL_NAME:
        Preconditions.checkState(!n.getString().isEmpty());
        addIdentifier(n.getString());
        break;

      case Token.NAME:
        if (first == null || first.isEmpty()) {
          addIdentifier(n.getString());
        } else {
          Preconditions.checkState(childCount == 1);
          addIdentifier(n.getString());
          cc.addOp("=", true);
          if (first.isComma()) {
            addExpr(first, NodeUtil.precedence(Token.ASSIGN), Context.OTHER);
          } else {
            // Add expression, consider nearby code at lowest level of
            // precedence.
            addExpr(first, 0, getContextForNoInOperator(context));
          }
        }
        break;

      case Token.ARRAYLIT:
        add("[");
        addArrayList(first);
        add("]");
        break;

      case Token.PARAM_LIST:
        add("(");
        addList(first);
        add(")");
        break;

      case Token.COMMA:
        Preconditions.checkState(childCount == 2);
        unrollBinaryOperator(n, Token.COMMA, ",", context, Context.OTHER, 0, 0);
        break;

      case Token.NUMBER:
        Preconditions.checkState(childCount == 0);
        cc.addNumber(n.getDouble());
        break;

      case Token.TYPEOF:
      case Token.VOID:
      case Token.NOT:
      case Token.BITNOT:
      case Token.POS: {
        // All of these unary operators are right-associative
        Preconditions.checkState(childCount == 1);
        cc.addOp(NodeUtil.opToStrNoFail(type), false);
        addExpr(first, NodeUtil.precedence(type), Context.OTHER);
        break;
      }

      case Token.NEG: {
        Preconditions.checkState(childCount == 1);

        // It's important to our sanity checker that the code
        // we print produces the same AST as the code we parse back.
        // NEG is a weird case because Rhino parses "- -2" as "2".
        if (n.getFirstChild().isNumber()) {
          cc.addNumber(-n.getFirstChild().getDouble());
        } else {
          cc.addOp(NodeUtil.opToStrNoFail(type), false);
          addExpr(first, NodeUtil.precedence(type), Context.OTHER);
        }

        break;
      }

      case Token.HOOK: {
        Preconditions.checkState(childCount == 3);
        int p = NodeUtil.precedence(type);
        addExpr(first, p + 1, context);
        cc.addOp("?", true);
        addExpr(first.getNext(), 1, Context.OTHER);
        cc.addOp(":", true);
        addExpr(last, 1, Context.OTHER);
        break;
      }

      case Token.REGEXP:
        if (!first.isString() ||
            !last.isString()) {
          throw new Error("Expected children to be strings");
        }

        String regexp = regexpEscape(first.getString(), outputCharsetEncoder);

        // I only use one .add because whitespace matters
        if (childCount == 2) {
          add(regexp + last.getString());
        } else {
          Preconditions.checkState(childCount == 1);
          add(regexp);
        }
        break;

      case Token.FUNCTION:
        if (n.getClass() != Node.class) {
          throw new Error("Unexpected Node subclass.");
        }
        Preconditions.checkState(childCount == 3);
        boolean funcNeedsParens = (context == Context.START_OF_EXPR);
        if (funcNeedsParens) {
          add("(");
        }

        add("function");
        add(first);

        add(first.getNext());
        add(last, Context.PRESERVE_BLOCK);
        cc.endFunction(context == Context.STATEMENT);

        if (funcNeedsParens) {
          add(")");
        }
        break;

      case Token.GETTER_DEF:
      case Token.SETTER_DEF:
        Preconditions.checkState(n.getParent().isObjectLit());
        Preconditions.checkState(childCount == 1);
        Preconditions.checkState(first.isFunction());

        // Get methods are unnamed
        Preconditions.checkState(first.getFirstChild().getString().isEmpty());
        if (type == Token.GETTER_DEF) {
          // Get methods have no parameters.
          Preconditions.checkState(!first.getChildAtIndex(1).hasChildren());
          add("get ");
        } else {
          // Set methods have one parameter.
          Preconditions.checkState(first.getChildAtIndex(1).hasOneChild());
          add("set ");
        }

        // The name is on the GET or SET node.
        String name = n.getString();
        Node fn = first;
        Node parameters = fn.getChildAtIndex(1);
        Node body = fn.getLastChild();

        // Add the property name.
        if (!n.isQuotedString() &&
            TokenStream.isJSIdentifier(name) &&
            // do not encode literally any non-literal characters that were
            // unicode escaped.
            NodeUtil.isLatin(name)) {
          add(name);
        } else {
          // Determine if the string is a simple number.
          double d = getSimpleNumber(name);
          if (!Double.isNaN(d)) {
            cc.addNumber(d);
          } else {
            addJsString(n);
          }
        }

        add(parameters);
        add(body, Context.PRESERVE_BLOCK);
        break;

      case Token.SCRIPT:
      case Token.BLOCK: {
        if (n.getClass() != Node.class) {
          throw new Error("Unexpected Node subclass.");
        }
        boolean preserveBlock = context == Context.PRESERVE_BLOCK;
        if (preserveBlock) {
          cc.beginBlock();
        }

        boolean preferLineBreaks =
            type == Token.SCRIPT ||
            (type == Token.BLOCK &&
                !preserveBlock &&
                n.getParent() != null &&
                n.getParent().isScript());
        for (Node c = first; c != null; c = c.getNext()) {
          add(c, Context.STATEMENT);

          // VAR doesn't include ';' since it gets used in expressions
          if (c.isVar()) {
            cc.endStatement();
          }

          if (c.isFunction()) {
            cc.maybeLineBreak();
          }

          // Prefer to break lines in between top-level statements
          // because top level statements are more homogeneous.
          if (preferLineBreaks) {
            cc.notePreferredLineBreak();
          }
        }
        if (preserveBlock) {
          cc.endBlock(cc.breakAfterBlockFor(n, context == Context.STATEMENT));
        }
        break;
      }

      case Token.FOR:
        if (childCount == 4) {
          add("for(");
          if (first.isVar()) {
            add(first, Context.IN_FOR_INIT_CLAUSE);
          } else {
            addExpr(first, 0, Context.IN_FOR_INIT_CLAUSE);
          }
          add(";");
          add(first.getNext());
          add(";");
          add(first.getNext().getNext());
          add(")");
          addNonEmptyStatement(
              last, getContextForNonEmptyExpression(context), false);
        } else {
          Preconditions.checkState(childCount == 3);
          add("for(");
          add(first);
          add("in");
          add(first.getNext());
          add(")");
          addNonEmptyStatement(
              last, getContextForNonEmptyExpression(context), false);
        }
        break;

      case Token.DO:
        Preconditions.checkState(childCount == 2);
        add("do");
        addNonEmptyStatement(first, Context.OTHER, false);
        add("while(");
        add(last);
        add(")");
        cc.endStatement();
        break;

      case Token.WHILE:
        Preconditions.checkState(childCount == 2);
        add("while(");
        add(first);
        add(")");
        addNonEmptyStatement(
            last, getContextForNonEmptyExpression(context), false);
        break;

      case Token.EMPTY:
        Preconditions.checkState(childCount == 0);
        break;

      case Token.GETPROP: {
        Preconditions.checkState(
            childCount == 2,
            "Bad GETPROP: expected 2 children, but got %s", childCount);
        Preconditions.checkState(
            last.isString(),
            "Bad GETPROP: RHS should be STRING");
        boolean needsParens = (first.isNumber());
        if (needsParens) {
          add("(");
        }
        addExpr(first, NodeUtil.precedence(type), context);
        if (needsParens) {
          add(")");
        }
        add(".");
        addIdentifier(last.getString());
        break;
      }

      case Token.GETELEM:
        Preconditions.checkState(
            childCount == 2,
            "Bad GETELEM: expected 2 children but got %s", childCount);
        addExpr(first, NodeUtil.precedence(type), context);
        add("[");
        add(first.getNext());
        add("]");
        break;

      case Token.WITH:
        Preconditions.checkState(childCount == 2);
        add("with(");
        add(first);
        add(")");
        addNonEmptyStatement(
            last, getContextForNonEmptyExpression(context), false);
        break;

      case Token.INC:
      case Token.DEC: {
        Preconditions.checkState(childCount == 1);
        String o = type == Token.INC ? "++" : "--";
        int postProp = n.getIntProp(Node.INCRDECR_PROP);
        // A non-zero post-prop value indicates a post inc/dec, default of zero
        // is a pre-inc/dec.
        if (postProp != 0) {
          addExpr(first, NodeUtil.precedence(type), context);
          cc.addOp(o, false);
        } else {
          cc.addOp(o, false);
          add(first);
        }
        break;
      }

      case Token.CALL:
        // We have two special cases here:
        // 1) If the left hand side of the call is a direct reference to eval,
        // then it must have a DIRECT_EVAL annotation. If it does not, then
        // that means it was originally an indirect call to eval, and that
        // indirectness must be preserved.
        // 2) If the left hand side of the call is a property reference,
        // then the call must not a FREE_CALL annotation. If it does, then
        // that means it was originally an call without an explicit this and
        // that must be preserved.
        if (isIndirectEval(first)
            || n.getBooleanProp(Node.FREE_CALL) && NodeUtil.isGet(first)) {
          add("(0,");
          addExpr(first, NodeUtil.precedence(Token.COMMA), Context.OTHER);
          add(")");
        } else {
          addExpr(first, NodeUtil.precedence(type), context);
        }
        add("(");
        addList(first.getNext());
        add(")");
        break;

      case Token.IF:
        boolean hasElse = childCount == 3;
        boolean ambiguousElseClause =
            context == Context.BEFORE_DANGLING_ELSE && !hasElse;
        if (ambiguousElseClause) {
          cc.beginBlock();
        }

        add("if(");
        add(first);
        add(")");

        if (hasElse) {
          addNonEmptyStatement(
              first.getNext(), Context.BEFORE_DANGLING_ELSE, false);
          add("else");
          addNonEmptyStatement(
              last, getContextForNonEmptyExpression(context), false);
        } else {
          addNonEmptyStatement(first.getNext(), Context.OTHER, false);
          Preconditions.checkState(childCount == 2);
        }

        if (ambiguousElseClause) {
          cc.endBlock();
        }
        break;

      case Token.NULL:
        Preconditions.checkState(childCount == 0);
        add("null");
        break;

      case Token.THIS:
        Preconditions.checkState(childCount == 0);
        add("this");
        break;

      case Token.FALSE:
        Preconditions.checkState(childCount == 0);
        add("false");
        break;

      case Token.TRUE:
        Preconditions.checkState(childCount == 0);
        add("true");
        break;

      case Token.CONTINUE:
        Preconditions.checkState(childCount <= 1);
        add("continue");
        if (childCount == 1) {
          if (!first.isLabelName()) {
            throw new Error("Unexpected token type. Should be LABEL_NAME.");
          }
          add(" ");
          add(first);
        }
        cc.endStatement();
        break;

      case Token.DEBUGGER:
        Preconditions.checkState(childCount == 0);
        add("debugger");
        cc.endStatement();
        break;

      case Token.BREAK:
        Preconditions.checkState(childCount <= 1);
        add("break");
        if (childCount == 1) {
          if (!first.isLabelName()) {
            throw new Error("Unexpected token type. Should be LABEL_NAME.");
          }
          add(" ");
          add(first);
        }
        cc.endStatement();
        break;

      case Token.EXPR_RESULT:
        Preconditions.checkState(childCount == 1);
        add(first, Context.START_OF_EXPR);
        cc.endStatement();
        break;

      case Token.NEW:
        add("new ");
        int precedence = NodeUtil.precedence(type);

        // If the first child contains a CALL, then claim higher precedence
        // to force parentheses. Otherwise, when parsed, NEW will bind to the
        // first viable parentheses (don't traverse into functions).
        if (NodeUtil.containsType(
            first, Token.CALL, NodeUtil.MATCH_NOT_FUNCTION)) {
          precedence = NodeUtil.precedence(first.getType()) + 1;
        }
        addExpr(first, precedence, Context.OTHER);

        // '()' is optional when no arguments are present
        Node next = first.getNext();
        if (next != null) {
          add("(");
          addList(next);
          add(")");
        }
        break;

      case Token.STRING:
        if (childCount !=
            ((n.getParent() != null &&
              n.getParent().isObjectLit()) ? 1 : 0)) {
          throw new IllegalStateException(
              "Unexpected String children: " + n.getParent().toStringTree());
        }
        addJsString(n);
        break;

      case Token.DELPROP:
        Preconditions.checkState(childCount == 1);
        add("delete ");
        add(first);
        break;

      case Token.OBJECTLIT: {
        boolean needsParens = (context == Context.START_OF_EXPR);
        if (needsParens) {
          add("(");
        }
        add("{");
        for (Node c = first; c != null; c = c.getNext()) {
          if (c != first) {
            cc.listSeparator();
          }

          if (c.isGetterDef() || c.isSetterDef()) {
            add(c);
          } else {
            Preconditions.checkState(c.isString());
            String key = c.getString();
            // Object literal property names don't have to be quoted if they
            // are not JavaScript keywords
            if (!c.isQuotedString() &&
                !TokenStream.isKeyword(key) &&
                TokenStream.isJSIdentifier(key) &&
                // do not encode literally any non-literal characters that
                // were unicode escaped.
                NodeUtil.isLatin(key)) {
              add(key);
            } else {
              // Determine if the string is a simple number.
              double d = getSimpleNumber(key);
              if (!Double.isNaN(d)) {
                cc.addNumber(d);
              } else {
                addExpr(c, 1, Context.OTHER);
              }
            }
            add(":");
            addExpr(c.getFirstChild(), 1, Context.OTHER);
          }
        }
        add("}");
        if (needsParens) {
          add(")");
        }
        break;
      }

      case Token.SWITCH:
        add("switch(");
        add(first);
        add(")");
        cc.beginBlock();
        addAllSiblings(first.getNext());
        cc.endBlock(context == Context.STATEMENT);
        break;

      case Token.CASE:
        Preconditions.checkState(childCount == 2);
        add("case ");
        add(first);
        addCaseBody(last);
        break;

      case Token.DEFAULT_CASE:
        Preconditions.checkState(childCount == 1);
        add("default");
        addCaseBody(first);
        break;

      case Token.LABEL:
        Preconditions.checkState(childCount == 2);
        if (!first.isLabelName()) {
          throw new Error("Unexpected token type. Should be LABEL_NAME.");
        }
        add(first);
        add(":");
        addNonEmptyStatement(
            last, getContextForNonEmptyExpression(context), true);
        break;

      default:
        throw new Error("Unknown type " + type + "\n" + n.toStringTree());
    }

    cc.endSourceMapping(n);
  }

  private void unrollBinaryOperator(
      Node n, int op, String opStr, Context context,
      Context rhsContext, int leftPrecedence, int rightPrecedence) {
    Node firstNonOperator = n.getFirstChild();
    while (firstNonOperator.getType() == op) {
      firstNonOperator = firstNonOperator.getFirstChild();
    }

    addExpr(firstNonOperator, leftPrecedence, context);

    Node current = firstNonOperator;
    do {
      current = current.getParent();
      cc.listSeparator();
      addExpr(current.getFirstChild().getNext(), rightPrecedence, rhsContext);
    } while (current != n);
  }

    void appendOp(String op, boolean binOp) {
      if (binOp) {
        if (getLastChar() != ' ') {
          append(" ");
        }
        append(op);
        append(" ");
      } else {
        append(op);
      }
    }

// relevant test
// com.google.javascript.jscomp.IntegrationTest::testVarDeclarationsIntoFor
  public void testVarDeclarationsIntoFor() {
    CompilerOptions options = createCompilerOptions();

    options.collapseVariableDeclarations = false;

    String code = "var a = 1; for (var b = 2; ;) {}";

    testSame(options, code);

    options.collapseVariableDeclarations = false;

    test(options, code, "for (var a = 1, b = 2; ;) {}");
  }

// com.google.javascript.jscomp.IntegrationTest::testExploitAssigns
  public void testExploitAssigns() {
    CompilerOptions options = createCompilerOptions();

    options.collapseVariableDeclarations = false;

    String code = "a = 1; b = a; c = b";

    testSame(options, code);

    options.collapseVariableDeclarations = true;

    test(options, code, "c=b=a=1");
  }

// com.google.javascript.jscomp.IntegrationTest::testRecoverOnBadExterns
  public void testRecoverOnBadExterns() throws Exception {
    
    
    
    
    
    
    
    
    
    CompilerOptions options = createCompilerOptions();

    options.aliasExternals = true;
    externs = ImmutableList.of(
        SourceFile.fromCode("externs", "extern.foo"));

    test(options,
         "var extern; " +
         "function f() { return extern + extern + extern + extern; }",
         "var extern; " +
         "function f() { return extern + extern + extern + extern; }",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testDuplicateVariablesInExterns
  public void testDuplicateVariablesInExterns() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = true;
    externs = ImmutableList.of(
        SourceFile.fromCode("externs",
            "var externs = {};  var externs = {};"));
    testSame(options, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testLanguageMode
  public void testLanguageMode() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);

    String code = "var a = {get f(){}}";

    Compiler compiler = compile(options, code);
    checkUnexpectedErrorsOrWarnings(compiler, 1);
    assertEquals(
        "JSC_PARSE_ERROR. Parse error. " +
        "getters are not supported in Internet Explorer " +
        "at i0 line 1 : 0",
        compiler.getErrors()[0].toString());

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);

    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testLanguageMode2
  public void testLanguageMode2() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    options.setWarningLevel(DiagnosticGroups.ES5_STRICT, CheckLevel.OFF);

    String code = "var a  = 2; delete a;";

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    testSame(options, code);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);

    test(options,
        code,
        code,
        StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue598
  public void testIssue598() {
    CompilerOptions options = createCompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    WarningLevel.VERBOSE.setOptionsForWarningLevel(options);

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);

    String code =
        "'use strict';\n" +
        "function App() {}\n" +
        "App.prototype = {\n" +
        "  get appData() { return this.appData_; },\n" +
        "  set appData(data) { this.appData_ = data; }\n" +
        "};";

    Compiler compiler = compile(options, code);
    testSame(options, code);
  }

// com.google.javascript.jscomp.IntegrationTest::testCoaleseVariables
  public void testCoaleseVariables() {
    CompilerOptions options = createCompilerOptions();

    options.foldConstants = false;
    options.coalesceVariableNames = true;

    String code =
        "function f(a) {" +
        "  if (a) {" +
        "    return a;" +
        "  } else {" +
        "    var b = a;" +
        "    return b;" +
        "  }" +
        "  return a;" +
        "}";
    String expected =
        "function f(a) {" +
        "  if (a) {" +
        "    return a;" +
        "  } else {" +
        "    a = a;" +
        "    return a;" +
        "  }" +
        "  return a;" +
        "}";

    test(options, code, expected);

    options.foldConstants = true;
    options.coalesceVariableNames = false;

    code =
        "function f(a) {" +
        "  if (a) {" +
        "    return a;" +
        "  } else {" +
        "    var b = a;" +
        "    return b;" +
        "  }" +
        "  return a;" +
        "}";
    expected =
        "function f(a) {" +
        "  if (!a) {" +
        "    var b = a;" +
        "    return b;" +
        "  }" +
        "  return a;" +
        "}";

    test(options, code, expected);

    options.foldConstants = true;
    options.coalesceVariableNames = true;

    expected =
      "function f(a) {" +
      "  return a;" +
      "}";

    test(options, code, expected);
  }

// com.google.javascript.jscomp.IntegrationTest::testLateStatementFusion
  public void testLateStatementFusion() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    test(options,
        "while(a){a();if(b){b();b()}}",
        "for(;a;)a(),b&&(b(),b())");
  }

// com.google.javascript.jscomp.IntegrationTest::testLateConstantReordering
  public void testLateConstantReordering() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    test(options,
        "if (x < 1 || x > 1 || 1 < x || 1 > x) { alert(x) }",
        "   (1 > x || 1 < x || 1 < x || 1 > x) && alert(x) ");
  }

// com.google.javascript.jscomp.IntegrationTest::testsyntheticBlockOnDeadAssignments
  public void testsyntheticBlockOnDeadAssignments() {
    CompilerOptions options = createCompilerOptions();
    options.deadAssignmentElimination = true;
    options.removeUnusedVars = true;
    options.syntheticBlockStartMarker = "START";
    options.syntheticBlockEndMarker = "END";
    test(options, "var x; x = 1; START(); x = 1;END();x()",
                  "var x; x = 1;{START();{x = 1}END()}x()");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug4152835
  public void testBug4152835() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    options.syntheticBlockStartMarker = "START";
    options.syntheticBlockEndMarker = "END";
    test(options, "START();END()", "{START();{}END()}");
  }

// com.google.javascript.jscomp.IntegrationTest::testBug5786871
  public void testBug5786871() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    test(options, "function () {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue378
  public void testIssue378() {
    CompilerOptions options = createCompilerOptions();
    options.inlineVariables = true;
    options.flowSensitiveInlineVariables = true;
    testSame(options, "function f(c) {var f = c; arguments[0] = this;" +
                      "    f.apply(this, arguments); return this;}");
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue550
  public void testIssue550() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.SIMPLE_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.foldConstants = true;
    options.inlineVariables = true;
    options.flowSensitiveInlineVariables = true;
    test(options,
        "function f(h) {\n" +
        "  var a = h;\n" +
        "  a = a + 'x';\n" +
        "  a = a + 'y';\n" +
        "  return a;\n" +
        "}",
        "function f(a) {return a + 'xy'}");
  }

// com.google.javascript.jscomp.IntegrationTest::testIssue284
  public void testIssue284() {
    CompilerOptions options = createCompilerOptions();
    options.smartNameRemoval = true;
    test(options,
        "var goog = {};" +
        "goog.inherits = function(x, y) {};" +
        "var ns = {};" +
        "" +
        "ns.PageSelectionModel = function() {};" +
        "" +
        "ns.PageSelectionModel.FooEvent = function() {};" +
        "" +
        "ns.PageSelectionModel.SelectEvent = function() {};" +
        "goog.inherits(ns.PageSelectionModel.ChangeEvent," +
        "    ns.PageSelectionModel.FooEvent);",
        "");
  }

// com.google.javascript.jscomp.IntegrationTest::testCodingConvention
  public void testCodingConvention() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    assertEquals(
      compiler.getCodingConvention().getClass().toString(),
      ClosureCodingConvention.class.toString());
  }

// com.google.javascript.jscomp.IntegrationTest::testJQueryStringSplitLoops
  public void testJQueryStringSplitLoops() {
    CompilerOptions options = createCompilerOptions();
    options.foldConstants = true;
    test(options,
      "var x=['1','2','3','4','5','6','7']",
      "var x='1,2,3,4,5,6,7'.split(',')");

    options = createCompilerOptions();
    options.foldConstants = true;
    options.computeFunctionSideEffects = false;
    options.removeUnusedVars = true;

    
    test(options,
      "var x=['1','2','3','4','5','6','7']",
      "");

  }

// com.google.javascript.jscomp.IntegrationTest::testAlwaysRunSafetyCheck
  public void testAlwaysRunSafetyCheck() {
    CompilerOptions options = createCompilerOptions();
    options.checkSymbols = false;
    options.customPasses = ArrayListMultimap.create();
    options.customPasses.put(
        CustomPassExecutionTime.BEFORE_OPTIMIZATIONS,
        new CompilerPass() {
          @Override public void process(Node externs, Node root) {
            Node var = root.getLastChild().getFirstChild();
            assertEquals(Token.VAR, var.getType());
            var.detachFromParent();
          }
        });
    try {
      test(options,
           "var x = 3; function f() { return x + z; }",
           "function f() { return x + z; }");
      fail("Expected runtime exception");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().indexOf("Unexpected variable x") != -1);
    }
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressEs5StrictWarning
  public void testSuppressEs5StrictWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.ES5_STRICT, CheckLevel.WARNING);
    testSame(options,
        "\n" +
        "function f() { var arguments; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testCheckProvidesWarning
  public void testCheckProvidesWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_PROVIDES, CheckLevel.WARNING);
    options.setCheckProvides(CheckLevel.WARNING);
    test(options,
        "\n" +
        "function f() { var arguments; }",
        DiagnosticType.warning("JSC_MISSING_PROVIDE", "missing goog.provide(''{0}'')"));
  }

// com.google.javascript.jscomp.IntegrationTest::testSuppressCheckProvidesWarning
  public void testSuppressCheckProvidesWarning() {
    CompilerOptions options = createCompilerOptions();
    options.setWarningLevel(DiagnosticGroups.CHECK_PROVIDES, CheckLevel.WARNING);
    options.setCheckProvides(CheckLevel.WARNING);
    testSame(options,
        "\n" +
        "function f() { var arguments; }");
  }

// com.google.javascript.jscomp.IntegrationTest::testRenamePrefixNamespace
  public void testRenamePrefixNamespace() {
    String code =
        "var x = {}; x.FOO = 5; x.bar = 3;";

    CompilerOptions options = createCompilerOptions();
    testSame(options, code);

    options.collapseProperties = true;
    options.renamePrefixNamespace = "_";
    test(options, code, "_.x$FOO = 5; _.x$bar = 3;");
  }

// com.google.javascript.jscomp.IntegrationTest::testRenamePrefixNamespaceActivatesMoveFunctionDeclarations
  public void testRenamePrefixNamespaceActivatesMoveFunctionDeclarations() {
    CompilerOptions options = createCompilerOptions();
    String code = "var x = f; function f() { return 3; }";
    testSame(options, code);
    assertFalse(options.moveFunctionDeclarations);
    options.renamePrefixNamespace = "_";
    test(options, code, "_.f = function() { return 3; }; _.x = _.f;");
  }

// com.google.javascript.jscomp.IntegrationTest::testBrokenNameSpace
  public void testBrokenNameSpace() {
    CompilerOptions options = createCompilerOptions();
    String code = "var goog; goog.provide('i.am.on.a.Horse');" +
                  "i.am.on.a.Horse = function() {};" +
                  "i.am.on.a.Horse.prototype.x = function() {};" +
                  "i.am.on.a.Boat.prototype.y = function() {}";
    options.closurePass = true;
    options.collapseProperties = true;
    options.smartNameRemoval = true;
    test(options, code, "");
  }

// com.google.javascript.jscomp.IntegrationTest::testNamelessParameter
  public void testNamelessParameter() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    String code =
        "var impl_0;" +
        "$load($init());" +
        "function $load(){" +
        "  window['f'] = impl_0;" +
        "}" +
        "function $init() {" +
        "  impl_0 = {};" +
        "}";
    String result =
        "window.f = {};";
    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testHiddenSideEffect
  public void testHiddenSideEffect() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.setAliasExternals(true);
    String code =
        "window.offsetWidth;";
    String result =
        "window.offsetWidth;";
    test(options, code, result);
  }

// com.google.javascript.jscomp.IntegrationTest::testNegativeZero
  public void testNegativeZero() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    test(options,
        "function bar(x) { return x; }\n" +
        "function foo(x) { print(x / bar(0));\n" +
        "                 print(x / bar(-0)); }\n" +
        "foo(3);",
        "print(3/0);print(3/-0);");
  }

// com.google.javascript.jscomp.IntegrationTest::testSingletonGetter1
  public void testSingletonGetter1() {
    CompilerOptions options = createCompilerOptions();
    CompilationLevel.ADVANCED_OPTIMIZATIONS
        .setOptionsForCompilationLevel(options);
    options.setCodingConvention(new ClosureCodingConvention());
    test(options,
        "\n" +
        "var goog = goog || {};\n" +
        "goog.addSingletonGetter = function(ctor) {\n" +
        "  ctor.getInstance = function() {\n" +
        "    return ctor.instance_ || (ctor.instance_ = new ctor());\n" +
        "  };\n" +
        "};" +
        "function Foo() {}\n" +
        "goog.addSingletonGetter(Foo);" +
        "Foo.prototype.bar = 1;" +
        "function Bar() {}\n" +
        "goog.addSingletonGetter(Bar);" +
        "Bar.prototype.bar = 1;",
        "");
  }

// com.google.javascript.jscomp.IntegrationTest::testIncompleteFunction1
  public void testIncompleteFunction1() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "var foo = {bar: function(e) }" },
        new String[] { "var foo = {bar: function(e){}};" },
        warnings
    );
  }

// com.google.javascript.jscomp.IntegrationTest::testIncompleteFunction2
  public void testIncompleteFunction2() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    DiagnosticType[] warnings = new DiagnosticType[]{
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR,
        RhinoErrorReporter.PARSE_ERROR};
    test(options,
        new String[] { "function hi" },
        new String[] { "function hi() {}" },
        warnings
    );
  }

// com.google.javascript.jscomp.IntegrationTest::testSortingOff
  public void testSortingOff() {
    CompilerOptions options = new CompilerOptions();
    options.closurePass = true;
    options.setCodingConvention(new ClosureCodingConvention());
    test(options,
         new String[] {
           "goog.require('goog.beer');",
           "goog.provide('goog.beer');"
         },
         ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testUnboundedArrayLiteralInfiniteLoop
  public void testUnboundedArrayLiteralInfiniteLoop() {
    CompilerOptions options = createCompilerOptions();
    options.ideMode = true;
    test(options,
         "var x = [1, 2",
         "var x = [1, 2]",
         RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.IntegrationTest::testProvideRequireSameFile
  public void testProvideRequireSameFile() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.setDependencyOptions(
        new DependencyOptions()
        .setDependencySorting(true));
    options.closurePass = true;
    test(
        options,
        "goog.provide('x');\ngoog.require('x');",
        "var x = {};");
  }

// com.google.javascript.jscomp.IntegrationTest::testStrictWarningsGuard
  public void testStrictWarningsGuard() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.addWarningsGuard(new StrictWarningsGuard());

    Compiler compiler = compile(options,
        " function f() { return true; }");
    assertEquals(1, compiler.getErrors().length);
    assertEquals(0, compiler.getWarnings().length);
  }

// com.google.javascript.jscomp.IntegrationTest::testStrictWarningsGuardEmergencyMode
  public void testStrictWarningsGuardEmergencyMode() throws Exception {
    CompilerOptions options = createCompilerOptions();
    options.checkTypes = true;
    options.addWarningsGuard(new StrictWarningsGuard());
    options.useEmergencyFailSafe();

    Compiler compiler = compile(options,
        " function f() { return true; }");
    assertEquals(0, compiler.getErrors().length);
    assertEquals(1, compiler.getWarnings().length);
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext1
  public void testMakeLocalNamesUniqueWithContext1() {
    
    this.useDefaultRenamer = true;

    invert = true;
    test(
        "var a;function foo(){var a$$inline_1; a = 1}",
        "var a;function foo(){var a$$0; a = 1}");
    test(
        "var a;function foo(){var a$$inline_1;}",
        "var a;function foo(){var a;}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext2
  public void testMakeLocalNamesUniqueWithContext2() {
    
    this.useDefaultRenamer = true;

    
    testSameWithInversion("var a;");

    
    testSameWithInversion("a;");

    
    testWithInversion(
        "var a;function foo(a){var b;a}",
        "var a;function foo(a$$1){var b;a$$1}");
    testWithInversion(
        "var a;function foo(){var b;a}function boo(){var b;a}",
         "var a;function foo(){var b;a}function boo(){var b$$1;a}");
    testWithInversion(
        "function foo(a){var b}" +
         "function boo(a){var b}",
         "function foo(a){var b}" +
         "function boo(a$$1){var b$$1}");

    
    testWithInversion(
        "var a = function foo(){foo()};var b = function foo(){foo()};",
        "var a = function foo(){foo()};var b = function foo$$1(){foo$$1()};");

    
    testWithInversion(
        "try { } catch(e) {e;}",
         "try { } catch(e) {e;}");

    
    test(
        "try { } catch(e) {e;}; try { } catch(e) {e;}",
        "try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}");
    test(
        "try { } catch(e) {e; try { } catch(e) {e;}};",
        "try { } catch(e) {e; try { } catch(e$$1) {e$$1;} }; ");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext3
  public void testMakeLocalNamesUniqueWithContext3() {
    
    this.useDefaultRenamer = true;

    String externs = "var extern1 = {};";

    
    testSameWithInversion(externs, "var extern1 = extern1 || {};");

    
    testSame(externs, "var extern1 = extern1 || {};", null);
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext4
  public void testMakeLocalNamesUniqueWithContext4() {
    
    this.useDefaultRenamer = true;

    
    testInFunction(
        "var e; try { } catch(e) {e;}; try { } catch(e) {e;}",
        "var e; try { } catch(e$$1) {e$$1;}; try { } catch(e$$2) {e$$2;}");
    testInFunction(
        "var e; try { } catch(e) {e; try { } catch(e) {e;}}",
        "var e; try { } catch(e$$1) {e$$1; try { } catch(e$$2) {e$$2;} }");
    testInFunction(
        "try { } catch(e) {e;}; try { } catch(e) {e;} var e;",
        "try { } catch(e$$1) {e$$1;}; try { } catch(e$$2) {e$$2;} var e;");
    testInFunction(
        "try { } catch(e) {e; try { } catch(e) {e;}} var e;",
        "try { } catch(e$$1) {e$$1; try { } catch(e$$2) {e$$2;} } var e;");

    invert = true;

    testInFunction(
        "var e; try { } catch(e$$0) {e$$0;}; try { } catch(e$$1) {e$$1;}",
        "var e; try { } catch(e$$2) {e$$2;}; try { } catch(e$$0) {e$$0;}");
    testInFunction(
        "var e; try { } catch(e$$1) {e$$1; try { } catch(e$$2) {e$$2;} };",
        "var e; try { } catch(e$$0) {e$$0; try { } catch(e$$1) {e$$1;} };");
    testInFunction(
        "try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;};var e$$2;",
        "try { } catch(e) {e;}; try { } catch(e$$0) {e$$0;};var e$$1;");
    testInFunction(
        "try { } catch(e) {e; try { } catch(e$$1) {e$$1;} };var e$$2",
        "try { } catch(e) {e; try { } catch(e$$0) {e$$0;} };var e$$1");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithContext5
  public void testMakeLocalNamesUniqueWithContext5() {
    
    this.useDefaultRenamer = true;

    testWithInversion(
        "function f(){var f; f = 1}",
        "function f(){var f$$1; f$$1 = 1}");
    testWithInversion(
        "function f(f){f = 1}",
        "function f(f$$1){f$$1 = 1}");
    testWithInversion(
        "function f(f){var f; f = 1}",
        "function f(f$$1){var f$$1; f$$1 = 1}");

    test(
        "var fn = function f(){var f; f = 1}",
        "var fn = function f(){var f$$1; f$$1 = 1}");
    test(
        "var fn = function f(f){f = 1}",
        "var fn = function f(f$$1){f$$1 = 1}");
    test(
        "var fn = function f(f){var f; f = 1}",
        "var fn = function f(f$$1){var f$$1; f$$1 = 1}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testArguments
  public void testArguments() {
    
    this.useDefaultRenamer = true;

    
    testSameWithInversion(
        "function foo(){var arguments;function bar(){var arguments;}}");

    invert = true;

    
    test(
        "function foo(){var arguments$$1;}",
        "function foo(){var arguments$$0;}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testMakeLocalNamesUniqueWithoutContext
  public void testMakeLocalNamesUniqueWithoutContext() {
    
    this.useDefaultRenamer = false;

    test("var a;",
         "var a$$unique_0");

    
    testSame("a;");

    
    test("var a;" +
         "function foo(a){var b;a}",
         "var a$$unique_0;" +
         "function foo$$unique_1(a$$unique_2){var b$$unique_3;a$$unique_2}");
    test("var a;" +
         "function foo(){var b;a}" +
         "function boo(){var b;a}",
         "var a$$unique_0;" +
         "function foo$$unique_1(){var b$$unique_3;a$$unique_0}" +
         "function boo$$unique_2(){var b$$unique_4;a$$unique_0}");

    
    test("var a = function foo(){foo()};",
         "var a$$unique_0 = function foo$$unique_1(){foo$$unique_1()};");

    
    test("try { } catch(e) {e;}",
         "try { } catch(e$$unique_0) {e$$unique_0;}");
    test("try { } catch(e) {e;};" +
         "try { } catch(e) {e;}",
         "try { } catch(e$$unique_0) {e$$unique_0;};" +
         "try { } catch(e$$unique_1) {e$$unique_1;}");
    test("try { } catch(e) {e; " +
         "try { } catch(e) {e;}};",
         "try { } catch(e$$unique_0) {e$$unique_0; " +
            "try { } catch(e$$unique_1) {e$$unique_1;} }; ");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testOnlyInversion
  public void testOnlyInversion() {
    invert = true;
    test("function f(a, a$$1) {}",
         "function f(a, a$$0) {}");
    test("function f(a$$1, b$$2) {}",
         "function f(a, b) {}");
    test("function f(a$$1, a$$2) {}",
         "function f(a, a$$0) {}");
    testSame("try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}");
    testSame("try { } catch(e) {e; try { } catch(e$$1) {e$$1;} }; ");
    testSame("var a$$1;");
    testSame("function f() { var $$; }");
    test("var CONST = 3; var b = CONST;",
         "var CONST = 3; var b = CONST;");
    test("function f() {var CONST = 3; var ACONST$$1 = 2;}",
         "function f() {var CONST = 3; var ACONST = 2;}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testOnlyInversion2
  public void testOnlyInversion2() {
    invert = true;
    test("function f() {try { } catch(e) {e;}; try { } catch(e$$0) {e$$0;}}",
        "function f() {try { } catch(e) {e;}; try { } catch(e$$1) {e$$1;}}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testOnlyInversion3
  public void testOnlyInversion3() {
    invert = true;
    test(
        "function x1() {" +
        "  var a$$1;" +
        "  function x2() {" +
        "    var a$$2;" +
        "  }" +
        "  function x3() {" +
        "    var a$$3;" +
        "  }" +
        "}",
        "function x1() {" +
        "  var a$$0;" +
        "  function x2() {" +
        "    var a;" +
        "  }" +
        "  function x3() {" +
        "    var a;" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testOnlyInversion4
  public void testOnlyInversion4() {
    invert = true;
    test(
        "function x1() {" +
        "  var a$$0;" +
        "  function x2() {" +
        "    var a;a$$0++" +
        "  }" +
        "}",
        "function x1() {" +
        "  var a$$1;" +
        "  function x2() {" +
        "    var a;a$$1++" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testConstRemovingRename1
  public void testConstRemovingRename1() {
    removeConst = true;
    test("(function () {var CONST = 3; var ACONST$$1 = 2;})",
         "(function () {var CONST$$unique_0 = 3; var ACONST$$unique_1 = 2;})");
  }

// com.google.javascript.jscomp.MakeDeclaredNamesUniqueTest::testConstRemovingRename2
  public void testConstRemovingRename2() {
    removeConst = true;
    test("var CONST = 3; var b = CONST;",
         "var CONST$$unique_0 = 3; var b$$unique_1 = CONST$$unique_0;");
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testFunctionAnnotation
  public void testFunctionAnnotation() throws Exception {
    testMarkCalls("function f(){}", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f = function(){};", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f = function(){};", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f; f = function(){};", "f()",
                  ImmutableList.of("f"));
    testMarkCalls("var f;  f = function(){};", "f()",
                  ImmutableList.of("f"));

    
    testMarkCalls("function f(){}", Collections.<String>emptyList());
    testMarkCalls("function f(){} f()", Collections.<String>emptyList());

    
    testMarkCalls("var f = " +
                  "function(){};",
                  "f()",
                  ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testNamespaceAnnotation
  public void testNamespaceAnnotation() throws Exception {
    testMarkCalls("var o = {}; o.f = function(){};",
        "o.f()", ImmutableList.of("o.f"));
    testMarkCalls("var o = {}; o.f = function(){};",
        "o.f()", ImmutableList.of("o.f"));
    testMarkCalls("var o = {}; o.f = function(){}; o.f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testConstructorAnnotation
  public void testConstructorAnnotation() throws Exception {
    testMarkCalls("function c(){};", "new c",
                  ImmutableList.of("c"));
    testMarkCalls("var c = function(){};", "new c",
                  ImmutableList.of("c"));
    testMarkCalls("var c = function(){};", "new c",
                  ImmutableList.of("c"));
    testMarkCalls("function c(){}; new c", Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testMultipleDefinition
  public void testMultipleDefinition() throws Exception {
    testMarkCalls("function f(){}" +
                  "f = function(){};",
                  "f()",
                  ImmutableList.of("f"));
    testMarkCalls("function f(){}" +
                  "f = function(){};",
                  "f()",
                  Collections.<String>emptyList());
    testMarkCalls("function f(){}",
                  "f = function(){};" +
                  "f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testAssignNoFunction
  public void testAssignNoFunction() throws Exception {
    testMarkCalls("function f(){}", "f = 1; f()",
                  ImmutableList.of("f"));
    testMarkCalls("function f(){}", "f = 1 || 2; f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testPrototype
  public void testPrototype() throws Exception {
    testMarkCalls("function c(){};" +
                  "c.prototype.g = function(){};",
                  "var o = new c; o.g()",
                  ImmutableList.of("o.g"));
    testMarkCalls("function c(){};" +
                  "c.prototype.g = function(){};",
                  "function f(){}" +
                  "var o = new c; o.g(); f()",
                  ImmutableList.of("o.g"));

    
    testMarkCalls("function c(){};" +
                  "c.prototype.g = function(){};",
                  "var o = new c;" +
                  "o.g = function(){};" +
                  "o.g()",
                  ImmutableList.<String>of());
    
    testMarkCalls("function c1(){};" +
                  "c1.prototype.f = function(){};" +
                  "function c2(){};" +
                  "c2.prototype.f = function(){};",
                  "var o = new c1;" +
                  "o.f()",
                  ImmutableList.of("o.f"));

    
    testMarkCalls("function c1(){};" +
                  "c1.prototype.f = function(){};",
                  "function c2(){};" +
                  "c2.prototype.f = function(){};" +
                  "var o = new c1;" +
                  "o.f()",
                  Collections.<String>emptyList());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testAnnotationInExterns
  public void testAnnotationInExterns() throws Exception {
    testMarkCalls("externSef1()", Collections.<String>emptyList());
    testMarkCalls("externSef2()", Collections.<String>emptyList());
    testMarkCalls("externNsef1()", ImmutableList.of("externNsef1"));
    testMarkCalls("externNsef2()", ImmutableList.of("externNsef2"));
    testMarkCalls("externNsef3()", ImmutableList.of("externNsef3"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testNamespaceAnnotationInExterns
  public void testNamespaceAnnotationInExterns() throws Exception {
    testMarkCalls("externObj.sef1()", Collections.<String>emptyList());
    testMarkCalls("externObj.sef2()", Collections.<String>emptyList());
    testMarkCalls("externObj.nsef1()", ImmutableList.of("externObj.nsef1"));
    testMarkCalls("externObj.nsef2()", ImmutableList.of("externObj.nsef2"));

    testMarkCalls("externObj.nsef3()", ImmutableList.of("externObj.nsef3"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testOverrideDefinitionInSource
  public void testOverrideDefinitionInSource() throws Exception {
    
    testMarkCalls("var obj = {}; obj.sef1 = function(){}; obj.sef1()",
                  Collections.<String>emptyList());

    
    testMarkCalls("var obj = {};" +
                  "obj.sef1 = function(){};",
                  "obj.sef1()",
                  Collections.<String>emptyList());

    
    testMarkCalls("var obj = {}; obj.nsef1 = function(){}; obj.nsef1()",
                  Collections.<String>emptyList());

    
    testMarkCalls("var obj = {};" +
                  "obj.nsef1 = function(){};",
                  "obj.nsef1()",
                  ImmutableList.of("obj.nsef1"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testApply1
  public void testApply1() throws Exception {
    testMarkCalls(" var f = function() {}",
                  "f.apply()",
                  ImmutableList.of("f.apply"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testApply2
  public void testApply2() throws Exception {
    testMarkCalls("var f = function() {}",
                  "f.apply()",
                  ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testCall1
  public void testCall1() throws Exception {
    testMarkCalls(" var f = function() {}",
                  "f.call()",
                  ImmutableList.of("f.call"));
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testCall2
  public void testCall2() throws Exception {
    testMarkCalls("var f = function() {}",
                  "f.call()",
                  ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation1
  public void testInvalidAnnotation1() throws Exception {
    test(" function foo() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation2
  public void testInvalidAnnotation2() throws Exception {
    test("var f =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation3
  public void testInvalidAnnotation3() throws Exception {
    test(" var f = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation4
  public void testInvalidAnnotation4() throws Exception {
    test("var f = function() {};" +
         " f.x = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MarkNoSideEffectCallsTest::testInvalidAnnotation5
  public void testInvalidAnnotation5() throws Exception {
    test("var f = function() {};" +
         "f.x =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testBreakOptimization
  public void testBreakOptimization() throws Exception {
    fold("f:{if(true){a();break f;}else;b();}",
         "f:{if(true){a()}else{b()}}");
    fold("f:{if(false){a();break f;}else;b();break f;}",
         "f:{if(false){a()}else{b()}}");
    fold("f:{if(a()){b();break f;}else;c();}",
         "f:{if(a()){b();}else{c();}}");
    fold("f:{if(a()){b()}else{c();break f;}}",
         "f:{if(a()){b()}else{c();}}");
    fold("f:{if(a()){b();break f;}else;}",
         "f:{if(a()){b();}else;}");
    fold("f:{if(a()){break f;}else;}",
         "f:{if(a()){}else;}");

    fold("f:while(a())break f;",
         "f:while(a())break f");
    foldSame("f:for(x in a())break f");

    fold("f:{while(a())break;}",
         "f:{while(a())break;}");
    foldSame("f:{for(x in a())break}");

    fold("f:try{break f;}catch(e){break f;}",
         "f:try{}catch(e){}");
    fold("f:try{if(a()){break f;}else{break f;} break f;}catch(e){}",
         "f:try{if(a()){}else{}}catch(e){}");

    fold("f:g:break f",
         "");
    fold("f:g:{if(a()){break f;}else{break f;} break f;}",
         "f:g:{if(a()){}else{}}");
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testFunctionReturnOptimization
  public void testFunctionReturnOptimization() throws Exception {
    fold("function f(){if(a()){b();if(c())return;}}",
         "function f(){if(a()){b();if(c());}}");
    fold("function f(){if(x)return; x=3; return; }",
         "function f(){if(x); else x=3}");
    fold("function f(){if(true){a();return;}else;b();}",
         "function f(){if(true){a();}else{b();}}");
    fold("function f(){if(false){a();return;}else;b();return;}",
         "function f(){if(false){a();}else{b();}}");
    fold("function f(){if(a()){b();return;}else;c();}",
         "function f(){if(a()){b();}else{c();}}");
    fold("function f(){if(a()){b()}else{c();return;}}",
         "function f(){if(a()){b()}else{c();}}");
    fold("function f(){if(a()){b();return;}else;}",
         "function f(){if(a()){b();}else;}");
    fold("function f(){if(a()){return;}else{return;} return;}",
         "function f(){if(a()){}else{}}");
    fold("function f(){if(a()){return;}else{return;} b();}",
         "function f(){if(a()){}else{return;b()}}");

    fold("function f(){while(a())return;}",
         "function f(){while(a())return}");
    foldSame("function f(){for(x in a())return}");

    fold("function f(){while(a())break;}",
         "function f(){while(a())break}");
    foldSame("function f(){for(x in a())break}");

    fold("function f(){try{return;}catch(e){return;}finally{return}}",
         "function f(){try{}catch(e){}finally{}}");
    fold("function f(){try{return;}catch(e){return;}}",
         "function f(){try{}catch(e){}}");
    fold("function f(){try{return;}finally{return;}}",
         "function f(){try{}finally{}}");
    fold("function f(){try{if(a()){return;}else{return;} return;}catch(e){}}",
         "function f(){try{if(a()){}else{}}catch(e){}}");

    fold("function f(){g:return}",
         "function f(){}");
    fold("function f(){g:if(a()){return;}else{return;} return;}",
         "function f(){g:if(a()){}else{}}");
    fold("function f(){try{g:if(a()){} return;}finally{return}}",
         "function f(){try{g:if(a()){}}finally{}}");
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testWhileContinueOptimization
  public void testWhileContinueOptimization() throws Exception {
    fold("while(true){if(x)continue; x=3; continue; }",
         "while(true)if(x);else x=3");
    foldSame("while(true){a();continue;b();}");
    fold("while(true){if(true){a();continue;}else;b();}",
         "while(true){if(true){a();}else{b()}}");
    fold("while(true){if(false){a();continue;}else;b();continue;}",
         "while(true){if(false){a()}else{b();}}");
    fold("while(true){if(a()){b();continue;}else;c();}",
         "while(true){if(a()){b();}else{c();}}");
    fold("while(true){if(a()){b();}else{c();continue;}}",
         "while(true){if(a()){b();}else{c();}}");
    fold("while(true){if(a()){b();continue;}else;}",
         "while(true){if(a()){b();}else;}");
    fold("while(true){if(a()){continue;}else{continue;} continue;}",
         "while(true){if(a()){}else{}}");
    fold("while(true){if(a()){continue;}else{continue;} b();}",
         "while(true){if(a()){}else{continue;b();}}");

    fold("while(true)while(a())continue;",
         "while(true)while(a());");
    fold("while(true)for(x in a())continue",
         "while(true)for(x in a());");

    fold("while(true)while(a())break;",
         "while(true)while(a())break");
    fold("while(true)for(x in a())break",
         "while(true)for(x in a())break");

    fold("while(true){try{continue;}catch(e){continue;}}",
         "while(true){try{}catch(e){}}");
    fold("while(true){try{if(a()){continue;}else{continue;}" +
         "continue;}catch(e){}}",
         "while(true){try{if(a()){}else{}}catch(e){}}");

    fold("while(true){g:continue}",
         "while(true){}");
    
    fold("while(true){g:if(a()){continue;}else{continue;} continue;}",
         "while(true){g:if(a());else;}");
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testDoContinueOptimization
  public void testDoContinueOptimization() throws Exception {
    fold("do{if(x)continue; x=3; continue; }while(true)",
         "do if(x); else x=3; while(true)");
    foldSame("do{a();continue;b()}while(true)");
    fold("do{if(true){a();continue;}else;b();}while(true)",
         "do{if(true){a();}else{b();}}while(true)");
    fold("do{if(false){a();continue;}else;b();continue;}while(true)",
         "do{if(false){a();}else{b();}}while(true)");
    fold("do{if(a()){b();continue;}else;c();}while(true)",
         "do{if(a()){b();}else{c()}}while(true)");
    fold("do{if(a()){b();}else{c();continue;}}while(true)",
         "do{if(a()){b();}else{c();}}while(true)");
    fold("do{if(a()){b();continue;}else;}while(true)",
         "do{if(a()){b();}else;}while(true)");
    fold("do{if(a()){continue;}else{continue;} continue;}while(true)",
         "do{if(a()){}else{}}while(true)");
    fold("do{if(a()){continue;}else{continue;} b();}while(true)",
         "do{if(a()){}else{continue; b();}}while(true)");

    fold("do{while(a())continue;}while(true)",
         "do while(a());while(true)");
    fold("do{for(x in a())continue}while(true)",
         "do for(x in a());while(true)");

    fold("do{while(a())break;}while(true)",
         "do while(a())break;while(true)");
    fold("do for(x in a())break;while(true)",
         "do for(x in a())break;while(true)");

    fold("do{try{continue;}catch(e){continue;}}while(true)",
         "do{try{}catch(e){}}while(true)");
    fold("do{try{if(a()){continue;}else{continue;}" +
         "continue;}catch(e){}}while(true)",
         "do{try{if(a()){}else{}}catch(e){}}while(true)");

    fold("do{g:continue}while(true)",
         "do{}while(true)");
    
    fold("do{g:if(a()){continue;}else{continue;} continue;}while(true)",
         "do{g:if(a());else;}while(true)");

    fold("do { foo(); continue; } while(false)",
         "do { foo(); } while(false)");
    fold("do { foo(); break; } while(false)",
         "do { foo(); } while(false)");
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testForContinueOptimization
  public void testForContinueOptimization() throws Exception {
    fold("for(x in y){if(x)continue; x=3; continue; }",
         "for(x in y)if(x);else x=3");
    foldSame("for(x in y){a();continue;b()}");
    fold("for(x in y){if(true){a();continue;}else;b();}",
         "for(x in y){if(true)a();else b();}");
    fold("for(x in y){if(false){a();continue;}else;b();continue;}",
         "for(x in y){if(false){a();}else{b()}}");
    fold("for(x in y){if(a()){b();continue;}else;c();}",
         "for(x in y){if(a()){b();}else{c();}}");
    fold("for(x in y){if(a()){b();}else{c();continue;}}",
         "for(x in y){if(a()){b();}else{c();}}");
    fold("for(x=0;x<y;x++){if(a()){b();continue;}else;}",
         "for(x=0;x<y;x++){if(a()){b();}else;}");
    fold("for(x=0;x<y;x++){if(a()){continue;}else{continue;} continue;}",
         "for(x=0;x<y;x++){if(a()){}else{}}");
    fold("for(x=0;x<y;x++){if(a()){continue;}else{continue;} b();}",
         "for(x=0;x<y;x++){if(a()){}else{continue; b();}}");

    fold("for(x=0;x<y;x++)while(a())continue;",
         "for(x=0;x<y;x++)while(a());");
    fold("for(x=0;x<y;x++)for(x in a())continue",
         "for(x=0;x<y;x++)for(x in a());");

    fold("for(x=0;x<y;x++)while(a())break;",
         "for(x=0;x<y;x++)while(a())break");
    foldSame("for(x=0;x<y;x++)for(x in a())break");

    fold("for(x=0;x<y;x++){try{continue;}catch(e){continue;}}",
         "for(x=0;x<y;x++){try{}catch(e){}}");
    fold("for(x=0;x<y;x++){try{if(a()){continue;}else{continue;}" +
         "continue;}catch(e){}}",
         "for(x=0;x<y;x++){try{if(a()){}else{}}catch(e){}}");

    fold("for(x=0;x<y;x++){g:continue}",
         "for(x=0;x<y;x++){}");
    
    fold("for(x=0;x<y;x++){g:if(a()){continue;}else{continue;} continue;}",
         "for(x=0;x<y;x++){g:if(a());else;}");
  }

// com.google.javascript.jscomp.MinimizeExitPointsTest::testCodeMotionDoesntBreakFunctionHoisting
  public void testCodeMotionDoesntBreakFunctionHoisting() throws Exception {
    fold("function f() { if (x) return; foo(); function foo() {} }",
         "function f() { if (x); else { function foo() {} foo(); } }");
  }

// com.google.javascript.jscomp.MoveFunctionDeclarationsTest::testFunctionDeclarations
  public void testFunctionDeclarations() {
    test("a; function f(){} function g(){}", "function f(){} function g(){} a");
  }

// com.google.javascript.jscomp.MoveFunctionDeclarationsTest::testFunctionDeclarationsInModule
  public void testFunctionDeclarationsInModule() {
    test(createModules("a; function f(){} function g(){}"),
         new String[] { "function f(){} function g(){} a" });
  }

// com.google.javascript.jscomp.MoveFunctionDeclarationsTest::testFunctionsExpression
  public void testFunctionsExpression() {
    testSame("a; f = function(){}");
  }

// com.google.javascript.jscomp.MoveFunctionDeclarationsTest::testNoMoveDeepFunctionDeclarations
  public void testNoMoveDeepFunctionDeclarations() {
    testSame("a; if (a) function f(){};");
    testSame("a; if (a) { function f(){} }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion1
  public void testRemoveVarDeclartion1() {
    test("var foo = 3;", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion2
  public void testRemoveVarDeclartion2() {
    test("var foo = 3, bar = 4; externfoo = foo;",
         "var foo = 3; externfoo = foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion3
  public void testRemoveVarDeclartion3() {
    test("var a = f(), b = 1, c = 2; b; c", "f();var b = 1, c = 2; b; c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion4
  public void testRemoveVarDeclartion4() {
    test("var a = 0, b = f(), c = 2; a; c", "var a = 0;f();var c = 2; a; c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion5
  public void testRemoveVarDeclartion5() {
    test("var a = 0, b = 1, c = f(); a; b", "var a = 0, b = 1; f(); a; b");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion6
  public void testRemoveVarDeclartion6() {
    test("var a = 0, b = a = 1; a", "var a = 0; a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion7
  public void testRemoveVarDeclartion7() {
    test("var a = 0, b = a = 1", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveVarDeclartion8
  public void testRemoveVarDeclartion8() {
    test("var a;var b = 0, c = a = b = 1", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveFunction
  public void testRemoveFunction() {
    test("var foo = {}; foo.bar = function() {};", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testReferredToByWindow
  public void testReferredToByWindow() {
    testSame("var foo = {}; foo.bar = function() {}; window['fooz'] = foo.bar");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExtern
  public void testExtern() {
    testSame("externfoo = 5");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveNamedFunction
  public void testRemoveNamedFunction() {
    test("function foo(){}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction1
  public void testRemoveRecursiveFunction1() {
    test("function f(){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction2
  public void testRemoveRecursiveFunction2() {
    test("var f = function (){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction3
  public void testRemoveRecursiveFunction3() {
    test("var f;f = function (){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction4
  public void testRemoveRecursiveFunction4() {
    
    testSame("f = function (){f()}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction5
  public void testRemoveRecursiveFunction5() {
    test("function g(){f()}function f(){g()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction6
  public void testRemoveRecursiveFunction6() {
    test("var f=function(){g()};function g(){f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction7
  public void testRemoveRecursiveFunction7() {
    test("var g = function(){f()};var f = function(){g()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction8
  public void testRemoveRecursiveFunction8() {
    test("var o = {};o.f = function(){o.f()}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveRecursiveFunction9
  public void testRemoveRecursiveFunction9() {
    testSame("var o = {};o.f = function(){o.f()};o.f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification1
  public void testSideEffectClassification1() {
    test("foo();", "foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification2
  public void testSideEffectClassification2() {
    test("var a = foo();", "foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification3
  public void testSideEffectClassification3() {
    testSame("var a = foo();window['b']=a;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification4
  public void testSideEffectClassification4() {
    testSame("function sef(){} sef();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification5
  public void testSideEffectClassification5() {
    testSame("function nsef(){} var a = nsef();window['b']=a;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification6
  public void testSideEffectClassification6() {
    test("function sef(){} sef();", "function sef(){} sef();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSideEffectClassification7
  public void testSideEffectClassification7() {
    testSame("function sef(){} var a = sef();window['b']=a;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation1
  public void testNoSideEffectAnnotation1() {
    test("function f(){} var a = f();",
         "function f(){} f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation2
  public void testNoSideEffectAnnotation2() {
    test("function f(){}", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation3
  public void testNoSideEffectAnnotation3() {
    test("var f = function(){}; var a = f();",
         "var f = function(){}; f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation4
  public void testNoSideEffectAnnotation4() {
    test("var f = function(){};", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation5
  public void testNoSideEffectAnnotation5() {
    test("var f; f = function(){}; var a = f();",
         "var f; f = function(){}; f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation6
  public void testNoSideEffectAnnotation6() {
    test("var f; f = function(){};", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation7
  public void testNoSideEffectAnnotation7() {
    test("var f;" +
         "f = function(){};",
         "f = function(){};" +
         "var a = f();",
         "f = function(){}; f();", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation8
  public void testNoSideEffectAnnotation8() {
    test("var f;" +
         "f = function(){};" +
         "f = function(){};",
         "var a = f();",
         "f();", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation9
  public void testNoSideEffectAnnotation9() {
    test("var f;" +
         "f = function(){};" +
         "f = function(){};",
         "var a = f();",
         "", null, null);

    test("var f; f = function(){};", "var a = f();",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation10
  public void testNoSideEffectAnnotation10() {
    test("var o = {}; o.f = function(){}; var a = o.f();",
         "var o = {}; o.f = function(){}; o.f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation11
  public void testNoSideEffectAnnotation11() {
    test("var o = {}; o.f = function(){};",
         "var a = o.f();", "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation12
  public void testNoSideEffectAnnotation12() {
    test("function c(){} var a = new c",
         "function c(){} new c");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation13
  public void testNoSideEffectAnnotation13() {
    test("function c(){}", "var a = new c",
         "", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation14
  public void testNoSideEffectAnnotation14() {
    String common = "function c(){};" +
        "c.prototype.f = function(){};";
    test(common, "var o = new c; var a = o.f()", "new c", null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation15
  public void testNoSideEffectAnnotation15() {
    test("function c(){}; c.prototype.f = function(){}; var a = (new c).f()",
         "function c(){}; c.prototype.f = function(){}; (new c).f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNoSideEffectAnnotation16
  public void testNoSideEffectAnnotation16() {
    test("function c(){}" +
         "c.prototype.f = function(){};",
         "var a = (new c).f()",
         "",
         null, null);
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctionPrototype
  public void testFunctionPrototype() {
    testSame("var a = 5; Function.prototype.foo = function() {return a;}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass1
  public void testTopLevelClass1() {
    test("var Point = function() {}; Point.prototype.foo = function() {}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass2
  public void testTopLevelClass2() {
    testSame("var Point = {}; Point.prototype.foo = function() {};" +
             "externfoo = new Point()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass3
  public void testTopLevelClass3() {
    test("function Point() {this.me_ = Point}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass4
  public void testTopLevelClass4() {
    test("function f(){} function A(){} A.prototype = {x: function() {}}; f();",
         "function f(){} f();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass5
  public void testTopLevelClass5() {
    testSame("function f(){} function A(){}" +
             "A.prototype = {x: function() { f(); }}; new A();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass6
  public void testTopLevelClass6() {
    testSame("function f(){} function A(){}" +
             "A.prototype = {x: function() { f(); }}; new A().x();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testTopLevelClass7
  public void testTopLevelClass7() {
    test("A.prototype.foo = function(){}; function A() {}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass1
  public void testNamespacedClass1() {
    test("var foo = {};foo.bar = {};foo.bar.prototype.baz = {}", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass2
  public void testNamespacedClass2() {
    testSame("var foo = {};foo.bar = {};foo.bar.prototype.baz = {};" +
             "window.z = new foo.bar()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass3
  public void testNamespacedClass3() {
    test("var a = {}; a.b = function() {}; a.b.prototype = {x: function() {}};",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass4
  public void testNamespacedClass4() {
    testSame("function f(){} var a = {}; a.b = function() {};" +
             "a.b.prototype = {x: function() { f(); }}; new a.b();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNamespacedClass5
  public void testNamespacedClass5() {
    testSame("function f(){} var a = {}; a.b = function() {};" +
             "a.b.prototype = {x: function() { f(); }}; new a.b().x();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToThisPrototype
  public void testAssignmentToThisPrototype() {
    testSame("Function.prototype.inherits = function(parentCtor) {" +
             "  function tempCtor() {};" +
             "  tempCtor.prototype = parentCtor.prototype;" +
             "  this.superClass_ = parentCtor.prototype;" +
             "  this.prototype = new tempCtor();" +
             "  this.prototype.constructor = this;" +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToCallResultPrototype
  public void testAssignmentToCallResultPrototype() {
    testSame("function f() { return function(){}; } f().prototype = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToExternPrototype
  public void testAssignmentToExternPrototype() {
    testSame("externfoo.prototype = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignmentToUnknownPrototype
  public void testAssignmentToUnknownPrototype() {
    testSame(
        " var window;" +
        "window['a'].prototype = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testBug2099540
  public void testBug2099540() {
    testSame(
        " var document;\n" +
        " var window;\n" +
        "var klass;\n" +
        "window[klass].prototype = " +
            "document.createElement(tagName)['__proto__'];");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testOtherGlobal
  public void testOtherGlobal() {
    testSame("goog.global.foo = bar(); function bar(){}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternName1
  public void testExternName1() {
    testSame("top.z = bar(); function bar(){}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExternName2
  public void testExternName2() {
    testSame("top['z'] = bar(); function bar(){}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits1
  public void testInherits1() {
    test("var a = {}; var b = {}; b.inherits(a)", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits2
  public void testInherits2() {
    test("var a = {}; var b = {}; var goog = {}; goog.inherits(b, a)", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits3
  public void testInherits3() {
    testSame("var a = {}; this.b = {}; b.inherits(a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits4
  public void testInherits4() {
    testSame("var a = {}; this.b = {}; var goog = {}; goog.inherits(b, a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits5
  public void testInherits5() {
    test("this.a = {}; var b = {}; b.inherits(a);",
         "this.a = {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits6
  public void testInherits6() {
    test("this.a = {}; var b = {}; var goog = {}; goog.inherits(b, a);",
         "this.a = {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits7
  public void testInherits7() {
    testSame("var a = {}; this.b = {}; var goog = {};" +
        " goog.inherits = function() {}; goog.inherits(b, a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testInherits8
  public void testInherits8() {
    
    
    test("this.a = {}; var b = {}; var c = b.inherits(a);", "this.a = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin1
  public void testMixin1() {
    testSame("var goog = {}; goog.mixin = function() {};" +
             "Function.prototype.mixin = function(base) {" +
             "  goog.mixin(this.prototype, base); " +
             "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin2
  public void testMixin2() {
    testSame("var a = {}; this.b = {}; var goog = {};" +
        " goog.mixin = function() {}; goog.mixin(b.prototype, a.prototype);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin3
  public void testMixin3() {
    test("this.a = {}; var b = {}; var goog = {};" +
         " goog.mixin = function() {}; goog.mixin(b.prototype, a.prototype);",
         "this.a = {};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin4
  public void testMixin4() {
    testSame("this.a = {}; var b = {}; var goog = {};" +
             "goog.mixin = function() {};" +
             "goog.mixin(b.prototype, a.prototype);" +
             "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin5
  public void testMixin5() {
    test("this.a = {}; var b = {}; var c = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "goog.mixin(b.prototype, a.prototype);" +
         "goog.mixin(c.prototype, a.prototype);" +
         "new b()",
         "this.a = {}; var b = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "goog.mixin(b.prototype, a.prototype);" +
         "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin6
  public void testMixin6() {
    testSame("this.a = {}; var b = {}; var c = {}; var goog = {};" +
             "goog.mixin = function() {};" +
             "goog.mixin(c.prototype, a.prototype) + " +
             "goog.mixin(b.prototype, a.prototype);" +
             "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testMixin7
  public void testMixin7() {
    test("this.a = {}; var b = {}; var c = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "var d = goog.mixin(c.prototype, a.prototype) + " +
         "goog.mixin(b.prototype, a.prototype);" +
         "new b()",
         "this.a = {}; var b = {}; var goog = {};" +
         "goog.mixin = function() {};" +
         "goog.mixin(b.prototype, a.prototype);" +
         "new b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConstants1
  public void testConstants1() {
    testSame("var bar = function(){}; var EXP_FOO = true; if (EXP_FOO) bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConstants2
  public void testConstants2() {
    test("var bar = function(){}; var EXP_FOO = true; var EXP_BAR = true;" +
         "if (EXP_FOO) bar();",
         "var bar = function(){}; var EXP_FOO = true; if (EXP_FOO) bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExpressions1
  public void testExpressions1() {
    test("var foo={}; foo.A='A'; foo.AB=foo.A+'B'; foo.ABC=foo.AB+'C'",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExpressions2
  public void testExpressions2() {
    testSame("var foo={}; foo.A='A'; foo.AB=foo.A+'B'; this.ABC=foo.AB+'C'");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testExpressions3
  public void testExpressions3() {
    testSame("var foo = 2; window.bar(foo + 3)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetCreatingReference
  public void testSetCreatingReference() {
    testSame("var foo; var bar = function(){foo=6;}; bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous1
  public void testAnonymous1() {
    testSame("function foo() {}; function bar() {}; foo(function() {bar()})");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous2
  public void testAnonymous2() {
    test("var foo;(function(){foo=6;})()", "(function(){})()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous3
  public void testAnonymous3() {
    testSame("var foo; (function(){ if(!foo)foo=6; })()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous4
  public void testAnonymous4() {
    testSame("var foo; (function(){ foo=6; })(); externfoo=foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous5
  public void testAnonymous5() {
    testSame("var foo;" +
             "(function(){ foo=function(){ bar() }; function bar(){} })();" +
             "foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous6
  public void testAnonymous6() {
    testSame("function foo(){}" +
             "function bar(){}" +
             "foo(function(){externfoo = bar});");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous7
  public void testAnonymous7() {
    testSame("var foo;" +
             "(function (){ function bar(){ externfoo = foo; } bar(); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous8
  public void testAnonymous8() {
    testSame("var foo;" +
             "(function (){ var g=function(){ externfoo = foo; }; g(); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAnonymous9
  public void testAnonymous9() {
    testSame("function foo(){}" +
             "function bar(){}" +
             "foo(function(){ function baz(){ externfoo = bar; } baz(); });");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctions1
  public void testFunctions1() {
    testSame("var foo = null; function baz() {}" +
             "function bar() {foo=baz();} bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFunctions2
  public void testFunctions2() {
    testSame("var foo; foo = function() {var a = bar()};" +
             "var bar = function(){}; foo();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem1
  public void testGetElem1() {
    testSame("var foo = {}; foo.bar = {}; foo.bar.baz = {a: 5, b: 10};" +
             "var fn = function() {window[foo.bar.baz.a] = 5;}; fn()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem2
  public void testGetElem2() {
    testSame("var foo = {}; foo.bar = {}; foo.bar.baz = {a: 5, b: 10};" +
             "var fn = function() {this[foo.bar.baz.a] = 5;}; fn()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElem3
  public void testGetElem3() {
    testSame("var foo = {'i': 0, 'j': 1}; foo['k'] = 2; top.foo = foo;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf1
  public void testIf1() {
    test("var foo = {};if(e)foo.bar=function(){};", "if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf2
  public void testIf2() {
    test("var e = false;var foo = {};if(e)foo.bar=function(){};",
         "var e = false;if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf3
  public void testIf3() {
    test("var e = false;var foo = {};if(e + 1)foo.bar=function(){};",
         "var e = false;if(e + 1);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf4
  public void testIf4() {
    test("var e = false, f;var foo = {};if(f=e)foo.bar=function(){};",
         "var e = false;if(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIf5
  public void testIf5() {
    test("var e = false, f;var foo = {};if(f = e + 1)foo.bar=function(){};",
         "var e = false;if(e + 1);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testIfElse
  public void testIfElse() {
    test("var foo = {};if(e)foo.bar=function(){};else foo.bar=function(){};",
         "if(e);else;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testWhile
  public void testWhile() {
    test("var foo = {};while(e)foo.bar=function(){};", "while(e);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testFor
  public void testFor() {
    test("var foo = {};for(e in x)foo.bar=function(){};", "for(e in x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDo
  public void testDo() {
    test("var cond = false;do {var a = 1} while (cond)",
         "var cond = false;do {} while (cond)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct1
  public void testSetterInForStruct1() {
    test("var j = 0; for (var i = 1; i = 0; j++);",
         "var j = 0; for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct2
  public void testSetterInForStruct2() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.prototype.property_ = 0; i++);",
         "for (var i = 1; 0; i++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct3
  public void testSetterInForStruct3() {
    test("var j = 0; for (var i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; f(); g(); h(); for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct4
  public void testSetterInForStruct4() {
    test("var i = 0;var j = 0; for (i = 1 + f() + g() + h(); i = 0; j++);",
         "var j = 0; f(); g(); h(); for (; 0; j++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct5
  public void testSetterInForStruct5() {
    test("var i = 0, j = 0; for (i = f(), j = g(); 0;);",
         "for (f(), g(); 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct6
  public void testSetterInForStruct6() {
    test("var i = 0, j = 0, k = 0; for (i = f(), j = g(), k = h(); i = 0;);",
         "for (f(), g(), h(); 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct7
  public void testSetterInForStruct7() {
    test("var i = 0, j = 0, k = 0; for (i = 1, j = 2, k = 3; i = 0;);",
         "for (1, 2, 3; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct8
  public void testSetterInForStruct8() {
    test("var i = 0, j = 0, k = 0; for (i = 1, j = i, k = 2; i = 0;);",
         "var i = 0; for(i = 1, i , 2; i = 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct9
  public void testSetterInForStruct9() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.property_ = 0; i++);",
         "for (var i = 1; 0; i++);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct10
  public void testSetterInForStruct10() {
    test("var Class = function() {}; " +
         "for (var i = 1; Class.property_ = 0; i = 2);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct11
  public void testSetterInForStruct11() {
    test("var Class = function() {}; " +
         "for (;Class.property_ = 0;);",
         "for (;0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct12
  public void testSetterInForStruct12() {
    test("var a = 1; var Class = function() {}; " +
         "for (;Class.property_ = a;);",
         "var a = 1; for (; a;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct13
  public void testSetterInForStruct13() {
    test("var a = 1; var Class = function() {}; " +
         "for (Class.property_ = a; 0 ;);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct14
  public void testSetterInForStruct14() {
    test("var a = 1; var Class = function() {}; " +
         "for (; 0; Class.property_ = a);",
         "for (; 0;);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct15
  public void testSetterInForStruct15() {
    test("var Class = function() {}; " +
         "for (var i = 1; 0; Class.prototype.property_ = 0);",
         "for (; 0; 0);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForStruct16
  public void testSetterInForStruct16() {
    test("var Class = function() {}; " +
         "for (var i = 1; i = 0; Class.prototype.property_ = 0);",
         "for (; 0; 0);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn1
  public void testSetterInForIn1() {
    test("var foo = {}; var bar; for(e in bar = foo.a);",
         "var foo = {}; for(e in foo.a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn2
  public void testSetterInForIn2() {
    testSame("var foo = {}; var bar; for(e in bar = foo.a); bar");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn3
  public void testSetterInForIn3() {
    
    
    test("var foo = {}; var bar; for(e in bar = foo.a); bar.b = 3",
         "var foo = {}; for(e in foo.a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn4
  public void testSetterInForIn4() {
    
    
    test("var foo = {}; var bar; for (e in bar = foo.a); bar.b = 3; foo.a",
         "var foo = {}; for (e in foo.a); foo.a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn5
  public void testSetterInForIn5() {
    
    
    test("var foo = {}; var bar; for (e in foo.a) { bar = e } bar.b = 3; foo.a",
         "var foo={};for(e in foo.a);foo.a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInForIn6
  public void testSetterInForIn6() {
    testSame("var foo = {};for(e in foo);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInIfPredicate
  public void testSetterInIfPredicate() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "if (Class.property_ = a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInWhilePredicate
  public void testSetterInWhilePredicate() {
    test("var a = 1;" +
         "var Class = function() {}; " +
         "while (Class.property_ = a);",
         "var a = 1; for (;a;) {}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInDoWhilePredicate
  public void testSetterInDoWhilePredicate() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "do {} while(Class.property_ = a);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testSetterInSwitchInput
  public void testSetterInSwitchInput() {
    
    testSame("var a = 1;" +
             "var Class = function() {}; " +
             "switch (Class.property_ = a) {" +
             "  default:" +
             "}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexAssigns
  public void testComplexAssigns() {
    
    testSame("var x = 0; x += 3; x *= 5;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssigns
  public void testNestedAssigns() {
    test("var x = 0; var y = x = 3; window.alert(y);",
         "var y = 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns1
  public void testComplexNestedAssigns1() {
    
    testSame("var x = 0; var y = 2; y += x = 3; window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns2
  public void testComplexNestedAssigns2() {
    test("var x = 0; var y = 2; y += x = 3; window.alert(y);",
         "var y = 2; y += 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns3
  public void testComplexNestedAssigns3() {
    test("var x = 0; var y = x += 3; window.alert(x);",
         "var x = 0; x += 3; window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testComplexNestedAssigns4
  public void testComplexNestedAssigns4() {
    testSame("var x = 0; var y = x += 3; window.alert(y);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope1
  public void testUnintendedUseOfInheritsInLocalScope1() {
    testSame("goog.mixin = function() {}; " +
             "(function() { var x = {}; var y = {}; goog.mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope2
  public void testUnintendedUseOfInheritsInLocalScope2() {
    testSame("goog.mixin = function() {}; " +
             "var x = {}; var y = {}; (function() { goog.mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope3
  public void testUnintendedUseOfInheritsInLocalScope3() {
    testSame("goog.mixin = function() {}; " +
             "var x = {}; var y = {}; (function() { goog.mixin(x, y); })(); " +
             "window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnintendedUseOfInheritsInLocalScope4
  public void testUnintendedUseOfInheritsInLocalScope4() {
    
    
    testSame("var goog$mixin = function() {}; " +
             "(function() { var x = {}; var y = {}; goog$mixin(x, y); })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope1
  public void testPrototypePropertySetInLocalScope1() {
    testSame("(function() { var x = function(){}; x.prototype.bar = 3; })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope2
  public void testPrototypePropertySetInLocalScope2() {
    testSame("var x = function(){}; (function() { x.prototype.bar = 3; })();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope3
  public void testPrototypePropertySetInLocalScope3() {
    test("var x = function(){ x.prototype.bar = 3; };", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope4
  public void testPrototypePropertySetInLocalScope4() {
    test("var x = {}; x.foo = function(){ x.foo.prototype.bar = 3; };", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope5
  public void testPrototypePropertySetInLocalScope5() {
    test("var x = {}; x.prototype.foo = 3;", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope6
  public void testPrototypePropertySetInLocalScope6() {
    testSame("var x = {}; x.prototype.foo = 3; bar(x.prototype.foo)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPrototypePropertySetInLocalScope7
  public void testPrototypePropertySetInLocalScope7() {
    testSame("var x = {}; x.foo = 3; bar(x.foo)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference1
  public void testRValueReference1() {
    testSame("var a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference2
  public void testRValueReference2() {
    testSame("var a = 1; 1+a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference3
  public void testRValueReference3() {
    testSame("var x = {}; x.prototype.foo = 3; var a = x.prototype.foo; 1+a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference4
  public void testRValueReference4() {
    testSame("var x = {}; x.prototype.foo = 3; x.prototype.foo");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference5
  public void testRValueReference5() {
    testSame("var x = {}; x.prototype.foo = 3; 1+x.prototype.foo");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRValueReference6
  public void testRValueReference6() {
    testSame("var x = {}; var idx = 2; x[idx]");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testUnhandledTopNode
  public void testUnhandledTopNode() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
             "function Bar() {}; Bar.prototype.isFoo = function() {};" +
             "var foo = new Foo(); var bar = new Bar();" +
             
             
             "var cond = foo.isBar() && bar.isFoo();" +
             "if (cond) {window.alert('hello');}");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testPropertyDefinedInGlobalScope
  public void testPropertyDefinedInGlobalScope() {
    testSame("function Foo() {}; var x = new Foo(); x.cssClass = 'bar';" +
             "window.alert(x);");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConditionallyDefinedFunction1
  public void testConditionallyDefinedFunction1() {
    testSame("var g; externfoo.x || (externfoo.x = function() { g; })");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testConditionallyDefinedFunction2
  public void testConditionallyDefinedFunction2() {
    testSame("var g; 1 || (externfoo.x = function() { g; })");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testGetElemOnThis
  public void testGetElemOnThis() {
    testSame("var a = 3; this['foo'] = a;");
    testSame("this['foo'] = 3;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveInstanceOfOnly
  public void testRemoveInstanceOfOnly() {
    test("function Foo() {}; Foo.prototype.isBar = function() {};" +
         "var x; if (x instanceof Foo) { window.alert(x); }",
         ";var x; if (false) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveLocalScopedInstanceOfOnly
  public void testRemoveLocalScopedInstanceOfOnly() {
    test("function Foo() {}; function Bar(x) { this.z = x instanceof Foo; };" +
        "externfoo.x = new Bar({});",
        ";function Bar(x) { this.z = false }; externfoo.x = new Bar({});");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRemoveInstanceOfWithReferencedMethod
  public void testRemoveInstanceOfWithReferencedMethod() {
    test("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "var x; if (x instanceof Foo) { window.alert(x.isBar()); }",
        ";var x; if (false) { window.alert(x.isBar()); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeReferencedInstanceOf
  public void testDoNotChangeReferencedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
             "var x = new Foo(); if (x instanceof Foo) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeReferencedLocalScopedInstanceOf
  public void testDoNotChangeReferencedLocalScopedInstanceOf() {
    testSame("function Foo() {}; externfoo.x = new Foo();" +
        "function Bar() { if (x instanceof Foo) { window.alert(x); } };" +
        "externfoo.y = new Bar();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeLocalScopeReferencedInstanceOf
  public void testDoNotChangeLocalScopeReferencedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "function Bar() { this.z = new Foo(); }; externfoo.x = new Bar();" +
        "if (x instanceof Foo) { window.alert(x); }");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeLocalScopeReferencedLocalScopedInstanceOf
  public void testDoNotChangeLocalScopeReferencedLocalScopedInstanceOf() {
    testSame("function Foo() {}; Foo.prototype.isBar = function() {};" +
        "function Bar() { this.z = new Foo(); };" +
        "Bar.prototype.func = function(x) {" +
          "if (x instanceof Foo) { window.alert(x); }" +
        "}; new Bar().func();");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testDoNotChangeInstanceOfGetElem
  public void testDoNotChangeInstanceOfGetElem() {
    testSame("var goog = {};" +
        "function f(obj, name) {" +
        "  if (obj instanceof goog[name]) {" +
        "    return name;" +
        "  }" +
        "}" +
        "window['f'] = f;");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testWeirdnessOnLeftSideOfPrototype
  public void testWeirdnessOnLeftSideOfPrototype() {
    
    
    testSame("var x = 3; " +
        "(function() { this.bar = 3; }).z = function() {" +
        "  return x;" +
        "};");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit1
  public void testShortCircuit1() {
    test("var a = b() || 1", "b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit2
  public void testShortCircuit2() {
    test("var a = 1 || c()", "1 || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit3
  public void testShortCircuit3() {
    test("var a = b() || c()", "b() || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit4
  public void testShortCircuit4() {
    test("var a = b() || 3 || c()", "b() || 3 || c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit5
  public void testShortCircuit5() {
    test("var a = b() && 1", "b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit6
  public void testShortCircuit6() {
    test("var a = 1 && c()", "1 && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit7
  public void testShortCircuit7() {
    test("var a = b() && c()", "b() && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testShortCircuit8
  public void testShortCircuit8() {
    test("var a = b() && 3 && c()", "b() && 3 && c()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference1
  public void testRhsReference1() {
    testSame("var a = 1; a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference2
  public void testRhsReference2() {
    testSame("var a = 1; a || b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference3
  public void testRhsReference3() {
    testSame("var a = 1; 1 || a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference4
  public void testRhsReference4() {
    test("var a = 1; var b = a || foo()", "var a = 1; a || foo()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsReference5
  public void testRhsReference5() {
    test("var a = 1, b = 5; a; foo(b)", "var a = 1, b = 5; a; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign1
  public void testRhsAssign1() {
    test("var foo, bar; foo || (bar = 1)",
         "var foo; foo || 1");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign2
  public void testRhsAssign2() {
    test("var foo, bar, baz; foo || (baz = bar = 1)",
         "var foo; foo || 1");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign3
  public void testRhsAssign3() {
    testSame("var foo = null; foo || (foo = 1)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign4
  public void testRhsAssign4() {
    test("var foo = null; foo = (foo || 1)", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign5
  public void testRhsAssign5() {
    test("var a = 3, foo, bar; foo || (bar = a)", "var a = 3, foo; foo || a");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign6
  public void testRhsAssign6() {
    test("function Foo(){} var foo = null;" +
         "var f = function () {foo || (foo = new Foo()); return foo}",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign7
  public void testRhsAssign7() {
    testSame("function Foo(){} var foo = null;" +
             "var f = function () {foo || (foo = new Foo())}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign8
  public void testRhsAssign8() {
    testSame("function Foo(){} var foo = null;" +
             "var f = function () {(foo = new Foo()) || g()}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRhsAssign9
  public void testRhsAssign9() {
    test("function Foo(){} var foo = null;" +
         "var f = function () {1 + (foo = new Foo()); return foo}",
         "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithOr1
  public void testAssignWithOr1() {
    testSame("var foo = null;" +
        "var f = window.a || function () {return foo}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithOr2
  public void testAssignWithOr2() {
    test("var foo = null;" +
        "var f = window.a || function () {return foo};",
        "var foo = null"); 
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithAnd1
  public void testAssignWithAnd1() {
    testSame("var foo = null;" +
        "var f = window.a && function () {return foo}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithAnd2
  public void testAssignWithAnd2() {
    test("var foo = null;" +
        "var f = window.a && function () {return foo};",
        "var foo = null;");  
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook1
  public void testAssignWithHook1() {
    testSame("function Foo(){} var foo = null;" +
        "var f = window.a ? " +
        "    function () {return new Foo()} : function () {return foo}; f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook2
  public void testAssignWithHook2() {
    test("function Foo(){} var foo = null;" +
        "var f = window.a ? " +
        "    function () {return new Foo()} : function () {return foo};",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook3
  public void testAssignWithHook3() {
    testSame("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? " +
        "    function () {return new Foo()} : function () {return foo}; f.b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook4
  public void testAssignWithHook4() {
    test("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? " +
        "    function () {return new Foo()} : function () {return foo};",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook5
  public void testAssignWithHook5() {
    testSame("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? function () {return new Foo()} :" +
        "    window.b ? function () {return foo} :" +
        "    function() { return Foo }; f.b()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testAssignWithHook6
  public void testAssignWithHook6() {
    test("function Foo(){} var foo = null; var f = {};" +
        "f.b = window.a ? function () {return new Foo()} :" +
        "    window.b ? function () {return foo} :" +
        "    function() { return Foo };",
        "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign1
  public void testNestedAssign1() {
    test("var a, b = a = 1, c = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign2
  public void testNestedAssign2() {
    test("var a, b = a = 1; foo(b)",
         "var b = 1; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign3
  public void testNestedAssign3() {
    test("var a, b = a = 1; a = b = 2; foo(b)",
         "var b = 1; b = 2; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign4
  public void testNestedAssign4() {
    test("var a, b = a = 1; b = a = 2; foo(b)",
         "var b = 1; b = 2; foo(b)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign5
  public void testNestedAssign5() {
    test("var a, b = a = 1; b = a = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign15
  public void testNestedAssign15() {
    test("var a, b, c; c = b = a = 2", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign6
  public void testNestedAssign6() {
    testSame("var a, b, c; a = b = c = 1; foo(a, b, c)");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign7
  public void testNestedAssign7() {
    testSame("var a = 0; a = i[j] = 1; b(a, i[j])");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testNestedAssign8
  public void testNestedAssign8() {
    testSame("function f(){" +
             "this.lockedToken_ = this.lastToken_ = " +
             "SETPROP_value(this.hiddenInput_, a)}f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain1
  public void testRefChain1() {
    test("var a = 1; var b = a; var c = b; var d = c", "");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain2
  public void testRefChain2() {
    test("var a = 1; var b = a; var c = b; var d = c || f()",
         "var a = 1; var b = a; var c = b; c || f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain3
  public void testRefChain3() {
    test("var a = 1; var b = a; var c = b; var d = c + f()", "f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain4
  public void testRefChain4() {
    test("var a = 1; var b = a; var c = b; var d = f() || c",
         "f()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain5
  public void testRefChain5() {
    test("var a = 1; var b = a; var c = b; var d = f() ? g() : c",
         "f() && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain6
  public void testRefChain6() {
    test("var a = 1; var b = a; var c = b; var d = c ? f() : g()",
         "var a = 1; var b = a; var c = b; c ? f() : g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain7
  public void testRefChain7() {
    test("var a = 1; var b = a; var c = b; var d = (b + f()) ? g() : c",
         "var a = 1; var b = a; (b+f()) && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain8
  public void testRefChain8() {
    test("var a = 1; var b = a; var c = b; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain9
  public void testRefChain9() {
    test("var a = 1; var b = a; var c = 5; var d = f()[b+c] ? g() : 0",
         "var a = 1; var b = a; var c = 5; f()[b+c] && g()");
  }

// com.google.javascript.jscomp.NameAnalyzerTest::testRefChain10
  public void testRefChain10() {
    test("var a = 1; var b = a; var c = b; var d = f()[b] ? g() : 0",
         "var a = 1; var b = a; f()[b] && g()");
  }
