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
        unrollBinaryOperator(n, type, opstr, context, rhsContext, p, p + 1);
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
        unrollBinaryOperator(n, Token.COMMA, ",", context,
            getContextForNoInOperator(context), 0, 0);
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
        Context rhsContext = getContextForNoInOperator(context);
        addExpr(first, p + 1, context);
        cc.addOp("?", true);
        addExpr(first.getNext(), 1, rhsContext);
        cc.addOp(":", true);
        addExpr(last, 1, rhsContext);
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
            // Unicode escaped.
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
          // because top-level statements are more homogeneous.
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
        if (this.languageMode == LanguageMode.ECMASCRIPT3
            && TokenStream.isKeyword(last.getString())) {
          // Check for ECMASCRIPT3 keywords.
          add("[");
          add(last);
          add("]");
        } else {
          add(".");
          addIdentifier(last.getString());
        }
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
        cc.addConstant("null");
        break;

      case Token.THIS:
        Preconditions.checkState(childCount == 0);
        add("this");
        break;

      case Token.FALSE:
        Preconditions.checkState(childCount == 0);
        cc.addConstant("false");
        break;

      case Token.TRUE:
        Preconditions.checkState(childCount == 0);
        cc.addConstant("true");
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

      case Token.STRING_KEY:
        Preconditions.checkState(
            childCount == 1, "Object lit key must have 1 child");
        addJsString(n);
        break;

      case Token.STRING:
        Preconditions.checkState(
            childCount == 0, "A string may not have children");
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
            Preconditions.checkState(c.isStringKey());
            String key = c.getString();
            // Object literal property names don't have to be quoted if they
            // are not JavaScript keywords
            if (!c.isQuotedString()
                && !(languageMode == LanguageMode.ECMASCRIPT3
                    && TokenStream.isKeyword(key))
                && TokenStream.isJSIdentifier(key)
                // do not encode literally any non-literal characters that
                // were Unicode escaped.
                && NodeUtil.isLatin(key)) {
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

      case Token.CAST:
        add("(");
        add(first);
        add(")");
        break;

      default:
        throw new Error("Unknown type " + type + "\n" + n.toStringTree());
    }

    cc.endSourceMapping(n);
  }

  public Node optimizeSubtree(Node node) {
    switch(node.getType()) {
      case Token.TRUE:
      case Token.FALSE:
        return reduceTrueFalse(node);

      case Token.NEW:
        node = tryFoldStandardConstructors(node);
        if (!node.isCall()) {
          return node;
        }
        // Fall through on purpose because tryFoldStandardConstructors() may
        // convert a NEW node into a CALL node
      case Token.CALL:
        Node result =  tryFoldLiteralConstructor(node);
        if (result == node) {
          result = tryFoldSimpleFunctionCall(node);
          if (result == node) {
            result = tryFoldImmediateCallToBoundFunction(node);
          }
        }
        return result;

      case Token.RETURN:
        return tryReduceReturn(node);

      case Token.COMMA:
        return trySplitComma(node);

      case Token.NAME:
        return tryReplaceUndefined(node);

      case Token.ARRAYLIT:
        return tryMinimizeArrayLiteral(node);


      default:
        return node; //Nothing changed
    }
  }

// relevant test
// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSingleDefinition2
  public void testRewriteSingleDefinition2() throws Exception {
    test(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                       NoRewriteMultipleDefinitionTestInput.CALL),
         NoRewriteMultipleDefinitionTestInput.SINGLE_DEFINITION_EXPECTED);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition1
  public void testNoRewriteMultipleDefinition1() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition2
  public void testNoRewriteMultipleDefinition2() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition3
  public void testNoRewriteMultipleDefinition3() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeNoObjectLiterals
  public void testRewritePrototypeNoObjectLiterals() throws Exception {
    test(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.REGULAR,
                       NoRewritePrototypeObjectLiteralsTestInput.CALL),
         "var JSCompiler_StaticMethods_foo = " +
         "function(JSCompiler_StaticMethods_foo$self) {};" +
         "JSCompiler_StaticMethods_foo(o)");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeObjectLiterals1
  public void testRewritePrototypeObjectLiterals1() throws Exception {
    test(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.OBJ_LIT,
                       NoRewritePrototypeObjectLiteralsTestInput.CALL),
         "a.prototype={};" +
         "var JSCompiler_StaticMethods_foo=" +
         "function(JSCompiler_StaticMethods_foo$self){};" +
         "JSCompiler_StaticMethods_foo(o)");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewritePrototypeObjectLiterals2
  public void testNoRewritePrototypeObjectLiterals2() throws Exception {
    testSame(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.OBJ_LIT,
                           NoRewritePrototypeObjectLiteralsTestInput.REGULAR,
                           NoRewritePrototypeObjectLiteralsTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteExternalMethods1
  public void testNoRewriteExternalMethods1() throws Exception {
    testSame("a.externalMethod()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteExternalMethods2
  public void testNoRewriteExternalMethods2() throws Exception {
    testSame("A.prototype.externalMethod = function(){}; o.externalMethod()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteCodingConvention
  public void testNoRewriteCodingConvention() throws Exception {
    
    testSame("a.prototype._foo = function() {};");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteNoVarArgs
  public void testRewriteNoVarArgs() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");

    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "  function(JSCompiler_StaticMethods_foo$self, args) {return args};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o)");

    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteVarArgs
  public void testNoRewriteVarArgs() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(var_args) {return arguments};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteCallReference
  public void testRewriteCallReference() throws Exception {
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "function(JSCompiler_StaticMethods_foo$self) {",
        "  return JSCompiler_StaticMethods_foo$self.x",
        "};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o);");

    test(NoRewriteNonCallReferenceTestInput.BASE + "o.foo()", expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNoReferences
  public void testNoRewriteNoReferences() throws Exception {
    testSame(NoRewriteNonCallReferenceTestInput.BASE);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNonCallReference
  public void testNoRewriteNonCallReference() throws Exception {
    testSame(NoRewriteNonCallReferenceTestInput.BASE + "o.foo && o.foo()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteNoNestedFunction
  public void testRewriteNoNestedFunction() throws Exception {
    test(semicolonJoin(
             NoRewriteNestedFunctionTestInput.PREFIX + "}",
             NoRewriteNestedFunctionTestInput.SUFFIX,
             NoRewriteNestedFunctionTestInput.INNER),
         semicolonJoin(
             NoRewriteNestedFunctionTestInput.EXPECTED_PREFIX + "}",
             NoRewriteNestedFunctionTestInput.EXPECTED_SUFFIX,
             "var JSCompiler_StaticMethods_bar=" +
             "function(JSCompiler_StaticMethods_bar$self){}",
             "JSCompiler_StaticMethods_bar(o)"));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNestedFunction
  public void testNoRewriteNestedFunction() throws Exception {
    test(NoRewriteNestedFunctionTestInput.PREFIX +
         NoRewriteNestedFunctionTestInput.INNER + "};" +
         NoRewriteNestedFunctionTestInput.SUFFIX,
         NoRewriteNestedFunctionTestInput.EXPECTED_PREFIX +
         NoRewriteNestedFunctionTestInput.INNER + "};" +
         NoRewriteNestedFunctionTestInput.EXPECTED_SUFFIX);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod
  public void testRewriteImplementedMethod() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "  function(JSCompiler_StaticMethods_foo$self, args) {return args};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o)");
    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod2
  public void testRewriteImplementedMethod2() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype['foo'] = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod3
  public void testRewriteImplementedMethod3() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o['foo']");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod4
  public void testRewriteImplementedMethod4() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype['foo'] = function(args) {return args};",
        "var o = new a;",
        "o['foo']");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethodInObj
  public void testRewriteImplementedMethodInObj() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {foo: function(args) {return args}};",
        "var o = new a;",
        "o.foo()");
    test(source,
        "function a(){}" +
        "a.prototype={};" +
        "var JSCompiler_StaticMethods_foo=" +
        "function(JSCompiler_StaticMethods_foo$self,args){return args};" +
        "var o=new a;" +
        "JSCompiler_StaticMethods_foo(o)");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteGet1
  public void testNoRewriteGet1() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {get foo(){return f}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteGet2
  public void testNoRewriteGet2() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {get foo(){return 1}};",
        "var o = new a;",
        "o.foo");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteSet1
  public void testNoRewriteSet1() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {set foo(a){}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteSet2
  public void testNoRewriteSet2() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {set foo(a){}};",
        "var o = new a;",
        "o.foo = 1");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNotImplementedMethod
  public void testNoRewriteNotImplementedMethod() throws Exception {
    testSame(newlineJoin("function a(){}",
                         "var o = new a;",
                         "o.foo()"));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testWrapper
  public void testWrapper() {
    testSame("(function() {})()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule1
  public void testRewriteSameModule1() throws Exception {
    JSModule[] modules = createModuleStar(
        
        semicolonJoin(ModuleTestInput.DEFINITION,
                      ModuleTestInput.USE),
        
        "");

    test(modules, new String[] {
        
        semicolonJoin(ModuleTestInput.REWRITTEN_DEFINITION,
                      ModuleTestInput.REWRITTEN_USE),
        
        "",
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule2
  public void testRewriteSameModule2() throws Exception {
    JSModule[] modules = createModuleStar(
        
        "",
        
        semicolonJoin(ModuleTestInput.DEFINITION,
                      ModuleTestInput.USE));

    test(modules, new String[] {
        
        "",
        
        semicolonJoin(ModuleTestInput.REWRITTEN_DEFINITION,
                      ModuleTestInput.REWRITTEN_USE)
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule3
  public void testRewriteSameModule3() throws Exception {
    JSModule[] modules = createModuleStar(
        
        semicolonJoin(ModuleTestInput.USE,
                      ModuleTestInput.DEFINITION),
        
        "");

    test(modules, new String[] {
        
        semicolonJoin(ModuleTestInput.REWRITTEN_USE,
                      ModuleTestInput.REWRITTEN_DEFINITION),
        
        ""
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteDefinitionBeforeUse
  public void testRewriteDefinitionBeforeUse() throws Exception {
    JSModule[] modules = createModuleStar(
        
        ModuleTestInput.DEFINITION,
        
        ModuleTestInput.USE);

    test(modules, new String[] {
        
        ModuleTestInput.REWRITTEN_DEFINITION,
        
        ModuleTestInput.REWRITTEN_USE
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteUseBeforeDefinition
  public void testNoRewriteUseBeforeDefinition() throws Exception {
    JSModule[] modules = createModuleStar(
        
        ModuleTestInput.USE,
        
        ModuleTestInput.DEFINITION);

    testSame(modules);
  }

// com.google.javascript.jscomp.DisambiguatePrivatePropertiesTest::testNoRenaming1
  public void testNoRenaming1() {
    useGoogleCodingConvention = true;

    
    testSame("var x_");

    
    testSame("x.prop");

    
    testSame("var x = {};\n x.prop;");

    
    testSame("({})['prop_'];");
    testSame("({'prop_': 1});");
    testSame("({get 'prop_'(){ return 1} });");
    testSame("({set 'prop_'(a){this.a = 1} });");

    useGoogleCodingConvention = false;

    
    testSame("({}).prop_;");
    testSame("({prop_: 1});");
    testSame("({get prop_(){ return 1} });");
    testSame("({set prop_(a){this.a = 1} });");
  }

// com.google.javascript.jscomp.DisambiguatePrivatePropertiesTest::testRenaming1
  public void testRenaming1() {
    useGoogleCodingConvention = true;

    test(
        "({}).prop_;",
        "({}).prop_$0;");

    test(
        "({prop_: 1});",
        "({prop_$0: 1});");

    test(
        "({get prop_(){ return 1} });",
        "({get prop_$0(){ return 1} });");

    test(
        "({set prop_(a){this.a = 1} });",
        "({set prop_$0(a){this.a = 1} });");
  }

// com.google.javascript.jscomp.DisambiguatePrivatePropertiesTest::testNoRenameIndirectProps
  public void testNoRenameIndirectProps() {
    useGoogleCodingConvention = true;

    testSame("({}).superClass_;");
    testSame("({superClass_: 1});");
    testSame("({get superClass_(){ return 1} });");
    testSame("({set superClass_(a){this.a = 1} });");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType1
  public void testOneType1() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType2
  public void testOneType2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = {a: 0};\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    String expected = "{a=[[Foo.prototype]]}";
    testSets(false, js, js, expected);
    testSets(true, js, js, expected);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType3
  public void testOneType3() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    String expected = "{a=[[Foo.prototype]]}";
    testSets(false, js, js, expected);
    testSets(true, js, js, expected);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testPrototypeAndInstance
  public void testPrototypeAndInstance() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testPrototypeAndInstance2
  public void testPrototypeAndInstance2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "new Foo().a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes1
  public void testTwoTypes1() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes2
  public void testTwoTypes2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = {a: 0};"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype = {a: 0};"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";

    String output = ""
        + "function Foo(){}"
        + "Foo.prototype = {Foo_prototype$a: 0};"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype = {Bar_prototype$a: 0};"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";

    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes3
  public void testTwoTypes3() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";

    String output = ""
        + "function Foo(){}"
        + "Foo.prototype = { get Foo_prototype$a() {return  0},"
        + "                  set Foo_prototype$a(b) {} };\n"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype = { get Bar_prototype$a() {return  0},"
        + "                  set Bar_prototype$a(b) {} };\n"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";

    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoFields
  public void testTwoFields() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "Foo.prototype.b = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + "F.b = 0;";
    String output = "function Foo(){}Foo.prototype.a=0;Foo.prototype.b=0;"
        + "var F=new Foo;F.a=0;F.b=0";
    testSets(false, js, output, "{a=[[Foo.prototype]], b=[[Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Foo.prototype]], b=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoSeparateFieldsTwoTypes
  public void testTwoSeparateFieldsTwoTypes() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "Foo.prototype.b = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + "F.b = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "Bar.prototype.b = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;"
        + "B.b = 0;";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "Foo.prototype.Foo_prototype$b=0;"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "F.Foo_prototype$b=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.Bar_prototype$b=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0;"
        + "B.Bar_prototype$b=0";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
                                + " b=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
                               + " b=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionType
  public void testUnionType() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;\n"
        + "B = new Foo;\n"
        + "B.a = 0;\n"
        + " function Baz() {}\n"
        + "Baz.prototype.a = 0;\n";
    testSets(false, js,
             "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testIgnoreUnknownType
  public void testIgnoreUnknownType() {
    String js = ""
        + "\n"
        + "function Foo() {}\n"
        + "Foo.prototype.blah = 3;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.blah = 0;\n"
        + "var U = function() { return {} };\n"
        + "U().blah();";
    String expected = ""
        + "function Foo(){}Foo.prototype.blah=3;var F = new Foo;F.blah=0;"
        + "var U=function(){return{}};U().blah()";
    testSets(false, js, expected, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, expected, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testIgnoreUnknownType1
  public void testIgnoreUnknownType1() {
    String js = ""
        + "\n"
        + "function Foo() {}\n"
        + "Foo.prototype.blah = 3;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.blah = 0;\n"
        + "\n"
        + "var U = function() { return {} };\n"
        + "U().blah();";
    String expected = ""
        + "function Foo(){}Foo.prototype.blah=3;var F = new Foo;F.blah=0;"
        + "var U=function(){return{}};U().blah()";
    testSets(false, js, expected, "{blah=[[Foo.prototype]]}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, expected, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testIgnoreUnknownType2
  public void testIgnoreUnknownType2() {
    String js = ""
        + "\n"
        + "function Foo() {}\n"
        + "Foo.prototype.blah = 3;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.blah = 0;\n"
        + "\n"
        + "function Bar() {}\n"
        + "Bar.prototype.blah = 3;\n"
        + "\n"
        + "var U = function() { return {} };\n"
        + "U().blah();";
    String expected = ""
        + "function Foo(){}Foo.prototype.blah=3;var F = new Foo;F.blah=0;"
        + "function Bar(){}Bar.prototype.blah=3;"
        + "var U=function(){return{}};U().blah()";
    testSets(false, js, expected, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, expected, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionTypeTwoFields
  public void testUnionTypeTwoFields() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "Foo.prototype.b = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "Bar.prototype.b = 0;\n"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;\n"
        + "B.b = 0;\n"
        + "B = new Foo;\n"
        + " function Baz() {}\n"
        + "Baz.prototype.a = 0;\n"
        + "Baz.prototype.b = 0;\n";
    testSets(false, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]],"
                 + " b=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]],"
                 + " b=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testCast
  public void testCast() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "((F)).a = 0;";
    String output = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var F=new Foo;F.Bar_prototype$a=0;";
    String ttOutput = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var F=new Foo;F.Unique$1$a=0;";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, ttOutput,
        "{a=[[Bar.prototype], [Foo.prototype], [Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testConstructorFields
  public void testConstructorFields() {
    String js = ""
      + "\n"
      + "var Foo = function() { this.a = 0; };\n"
      + " function Bar() {}\n"
      + "Bar.prototype.a = 0;"
      + "new Foo";
    String output = ""
        + "var Foo=function(){this.Foo$a=0};"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "new Foo";
    String ttOutput = ""
        + "var Foo=function(){this.Foo_prototype$a=0};"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "new Foo";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo]]}");
    testSets(true, js, ttOutput, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testStaticProperty
  public void testStaticProperty() {
    String js = ""
      + " function Foo() {} \n"
      + " function Bar() {}\n"
      + "Foo.a = 0;"
      + "Bar.a = 0;";
    String output = ""
        + "function Foo(){}"
        + "function Bar(){}"
        + "Foo.function__new_Foo___undefined$a = 0;"
        + "Bar.function__new_Bar___undefined$a = 0;";

    testSets(false, js, output,
        "{a=[[function (new:Bar): undefined]," +
        " [function (new:Foo): undefined]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSupertypeWithSameField
  public void testSupertypeWithSameField() {
    String js = ""
      + " function Foo() {}\n"
      + "Foo.prototype.a = 0;\n"
      + " function Bar() {}\n"
      + "\n"
      + "Bar.prototype.a = 0;\n"
      + " var B = new Bar;\n"
      + "B.a = 0;"
      + " function Baz() {}\n"
      + "Baz.prototype.a = function(){};\n";

    String output = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Foo_prototype$a=0;"
        + "var B = new Bar;B.Foo_prototype$a=0;"
        + "function Baz(){}Baz.prototype.Baz_prototype$a=function(){};";
    String ttOutput = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var B = new Bar;B.Bar_prototype$a=0;"
        + "function Baz(){}Baz.prototype.Baz_prototype$a=function(){};";
    testSets(false, js, output, "{a=[[Baz.prototype], [Foo.prototype]]}");
    testSets(true, js, ttOutput,
        "{a=[[Bar.prototype], [Baz.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testScopedType
  public void testScopedType() {
    String js = ""
        + "var g = {};\n"
        + " g.Foo = function() {}\n"
        + "g.Foo.prototype.a = 0;"
        + " g.Bar = function() {}\n"
        + "g.Bar.prototype.a = 0;";
    String output = ""
        + "var g={};"
        + "g.Foo=function(){};"
        + "g.Foo.prototype.g_Foo_prototype$a=0;"
        + "g.Bar=function(){};"
        + "g.Bar.prototype.g_Bar_prototype$a=0;";
    testSets(false, js, output, "{a=[[g.Bar.prototype], [g.Foo.prototype]]}");
    testSets(true, js, output, "{a=[[g.Bar.prototype], [g.Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnresolvedType
  public void testUnresolvedType() {
    
    String js = ""
        + "var g = {};"
        + " "
        + "var Foo = function() {};\n"
        + "Foo.prototype.a = 0;"
        + " var Bar = function() {};\n"
        + "Bar.prototype.a = 0;";
    String output = ""
        + "var g={};"
        + "var Foo=function(){};"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var Bar=function(){};"
        + "Bar.prototype.Bar_prototype$a=0;";

    setExpectParseWarningsThisTest();
    testSets(false, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testNamedType
  public void testNamedType() {
    String js = ""
        + "var g = {};"
        + " var Foo = function() {}\n"
        + "Foo.prototype.a = 0;"
        + " var Bar = function() {}\n"
        + "Bar.prototype.a = 0;"
        + " g.Late = function() {}";
    String output = ""
        + "var g={};"
        + "var Foo=function(){};"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var Bar=function(){};"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "g.Late = function(){}";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnknownType
  public void testUnknownType() {
    String js = ""
        + " var Foo = function() {};\n"
        + " var Bar = function() {};\n"
        + " function fun() {}\n"
        + "Foo.prototype.a = fun();\n"
        + "fun().a;\n"
        + "Bar.prototype.a = 0;";
    String ttOutput = ""
        + "var Foo=function(){};\n"
        + "var Bar=function(){};\n"
        + "function fun(){}\n"
        + "Foo.prototype.Foo_prototype$a=fun();\n"
        + "fun().Unique$1$a;\n"
        + "Bar.prototype.Bar_prototype$a=0;";
    testSets(false, js, js, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES, js, ttOutput,
             "{a=[[Bar.prototype], [Foo.prototype], [Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnum
  public void testEnum() {
    String js = ""
        + " var En = {\n"
        + "  A: 'first',\n"
        + "  B: 'second'\n"
        + "};\n"
        + "var EA = En.A;\n"
        + "var EB = En.B;\n"
        + " function Foo(){};\n"
        + "Foo.prototype.A = 0;\n"
        + "Foo.prototype.B = 0;\n";
    String output = ""
        + "var En={A:'first',B:'second'};"
        + "var EA=En.A;"
        + "var EB=En.B;"
        + "function Foo(){};"
        + "Foo.prototype.Foo_prototype$A=0;"
        + "Foo.prototype.Foo_prototype$B=0";
    String ttOutput = ""
        + "var En={A:'first',B:'second'};"
        + "var EA=En.A;"
        + "var EB=En.B;"
        + "function Foo(){};"
        + "Foo.prototype.Foo_prototype$A=0;"
        + "Foo.prototype.Foo_prototype$B=0";
    testSets(false, js, output, "{A=[[Foo.prototype]], B=[[Foo.prototype]]}");
    testSets(true, js, ttOutput, "{A=[[Foo.prototype]], B=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnumOfObjects
  public void testEnumOfObjects() {
    String js = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter()\n"
        + "};\n"
        + "Enum.A.format();\n";
    String output = ""
        + " function Formatter() {}"
        + "Formatter.prototype.Formatter_prototype$format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.Unrelated_prototype$format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter()\n"
        + "};\n"
        + "Enum.A.Formatter_prototype$format();\n";
    testSets(false, js, output,
        "{format=[[Formatter.prototype], [Unrelated.prototype]]}");

    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnumOfObjects2
  public void testEnumOfObjects2() {
    String js = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = window.toString() ? Enum.A : Enum.B;\n"
        + "  formatter.format();\n"
        + "}";
    String output = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = window.toString() ? Enum.A : Enum.B;\n"
        + "  formatter.format();\n"
        + "}";
    testSets(false, js, output, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnumOfObjects3
  public void testEnumOfObjects3() {
    String js = ""
        + " function Formatter() {}"
        + "Formatter.prototype.format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + " var SubEnum = {\n"
        + "  C: Enum.A\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = SubEnum.C\n"
        + "  formatter.format();\n"
        + "}";
    String output = ""
        + " function Formatter() {}"
        + "Formatter.prototype.Formatter_prototype$format = function() {};"
        + " function Unrelated() {}"
        + "Unrelated.prototype.Unrelated_prototype$format = function() {};"
        + " var Enum = {\n"
        + "  A: new Formatter(),\n"
        + "  B: new Formatter()\n"
        + "};\n"
        + " var SubEnum = {\n"
        + "  C: Enum.A\n"
        + "};\n"
        + "function f() {\n"
        + "  var formatter = SubEnum.C\n"
        + "  formatter.Formatter_prototype$format();\n"
        + "}";
    testSets(false, js, output,
        "{format=[[Formatter.prototype], [Unrelated.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUntypedExterns
  public void testUntypedExterns() {
    String externs =
        BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES
        + "var window;"
        + "window.alert = function() {x};";
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "Foo.prototype.alert = 0;\n"
        + "Foo.prototype.window = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "Bar.prototype.alert = 0;\n"
        + "Bar.prototype.window = 0;\n"
        + "window.alert();";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "Foo.prototype.alert=0;"
        + "Foo.prototype.Foo_prototype$window=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.alert=0;"
        + "Bar.prototype.Bar_prototype$window=0;"
        + "window.alert();";

    testSets(false, externs, js, output, "{a=[[Bar.prototype], [Foo.prototype]]"
             + ", window=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, externs, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
             + " window=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionTypeInvalidation
  public void testUnionTypeInvalidation() {
    String externs = ""
        + " function Baz() {}"
        + "Baz.prototype.a";
    String js = ""
        + " function Ind() {this.a=0}\n"
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 1\n;"
        + "F = new Bar;\n"
        + "\n"
        + "var Z = new Baz;\n"
        + "Z.a = 1\n;"
        + "\n"
        + "var B = new Baz;\n"
        + "B.a = 1;\n"
        + "B = new Bar;\n";
    
    
    String output = ""
        + "function Ind() { this.Ind$a = 0; }"
        + "function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var F = new Foo;"
        + "F.a = 1;"
        + "F = new Bar;"
        + "var Z = new Baz;"
        + "Z.a = 1;"
        + "var B = new Baz;"
        + "B.a = 1;"
        + "B = new Bar;";
    String ttOutput = ""
        + "function Ind() { this.Unique$1$a = 0; }"
        + "function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var F = new Foo;"
        + "F.a = 1;"
        + "F = new Bar;"
        + "var Z = new Baz;"
        + "Z.a = 1;"
        + "var B = new Baz;"
        + "B.a = 1;"
        + "B = new Bar;";
    testSets(false, externs, js, output, "{a=[[Ind]]}");
    testSets(true, externs, js, ttOutput, "{a=[[Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionAndExternTypes
  public void testUnionAndExternTypes() {
    String externs = ""
      + " function Foo() { }"
      + "Foo.prototype.a = 4;\n";
    String js = ""
      + " function Bar() { this.a = 2; }\n"
      + " function Baz() { this.a = 3; }\n"
      + " function Buz() { this.a = 4; }\n"
      + " function T1() { this.a = 3; }\n"
      + " function T2() { this.a = 3; }\n"
      + " var b;\n"
      + " var c;\n"
      + " var d;\n"
      + "b.a = 5; c.a = 6; d.a = 7;";
    String output = ""
      + " function Bar() { this.a = 2; }\n"
      + " function Baz() { this.a = 3; }\n"
      + " function Buz() { this.a = 4; }\n"
      + " function T1() { this.T1$a = 3; }\n"
      + " function T2() { this.T2$a = 3; }\n"
      + " var b;\n"
      + " var c;\n"
      + " var d;\n"
      + "b.a = 5; c.a = 6; d.a = 7;";

    
    
    testSets(false, externs, js, output, "{a=[[T1], [T2]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTypedExterns
  public void testTypedExterns() {
    String externs = ""
        + " function Window() {};\n"
        + "Window.prototype.alert;"
        + ""
        + "var window;";
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.alert = 0;\n"
        + "window.alert('blarg');";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$alert=0;"
        + "window.alert('blarg');";
    testSets(false, externs, js, output, "{alert=[[Foo.prototype]]}");
    testSets(true, externs, js, output, "{alert=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSubtypesWithSameField
  public void testSubtypesWithSameField() {
    String js = ""
        + " function Top() {}\n"
        + " function Foo() {}\n"
        + "Foo.prototype.a;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + ""
        + "function foo(top) {\n"
        + "  var x = top.a;\n"
        + "}\n"
        + "foo(new Foo);\n"
        + "foo(new Bar);\n";
    testSets(false, js, "{}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSupertypeReferenceOfSubtypeProperty
  public void testSupertypeReferenceOfSubtypeProperty() {
    String externs = ""
        + " function Ext() {}"
        + "Ext.prototype.a;";
    String js = ""
        + " function Foo() {}\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + ""
        + "function foo(foo) {\n"
        + "  var x = foo.a;\n"
        + "}\n";
    String result = ""
        + "function Foo() {}\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a;\n"
        + "function foo(foo$$1) {\n"
        + "  var x = foo$$1.Bar_prototype$a;\n"
        + "}\n";
    testSets(false, externs, js, result, "{a=[[Bar.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralNotRenamed
  public void testObjectLiteralNotRenamed() {
    String js = ""
        + "var F = {a:'a', b:'b'};"
        + "F.a = 'z';";
    testSets(false, js, js, "{}");
    testSets(true, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralReflected
  public void testObjectLiteralReflected() {
    String js = ""
        + "var goog = {};"
        + "goog.reflect = {};"
        + "goog.reflect.object = function(x, y) { return y; };"
        + " function F() {}"
        + " F.prototype.foo = 3;"
        + " function G() {}"
        + " G.prototype.foo = 3;"
        + "goog.reflect.object(F, {foo: 5});";
    String result = ""
        + "var goog = {};"
        + "goog.reflect = {};"
        + "goog.reflect.object = function(x, y) { return y; };"
        + "function F() {}"
        + "F.prototype.F_prototype$foo = 3;"
        + "function G() {}"
        + "G.prototype.G_prototype$foo = 3;"
        + "goog.reflect.object(F, {F_prototype$foo: 5});";
    testSets(false, js, result, "{foo=[[F.prototype], [G.prototype]]}");
    testSets(true, js, result, "{foo=[[F.prototype], [G.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralLends
  public void testObjectLiteralLends() {
    String js = ""
        + "var mixin = function(x) { return x; };"
        + " function F() {}"
        + " F.prototype.foo = 3;"
        + " function G() {}"
        + " G.prototype.foo = 3;"
        + "mixin( ({foo: 5}));";
    String result = ""
        + "var mixin = function(x) { return x; };"
        + "function F() {}"
        + "F.prototype.F_prototype$foo = 3;"
        + "function G() {}"
        + "G.prototype.G_prototype$foo = 3;"
        + "mixin( ({F_prototype$foo: 5}));";
    testSets(false, js, result, "{foo=[[F.prototype], [G.prototype]]}");
    testSets(true, js, result, "{foo=[[F.prototype], [G.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testClosureInherits
  public void testClosureInherits() {
    String js = ""
        + "var goog = {};"
        + "\n"
        + "goog.inherits = function(childCtor, parentCtor) {\n"
        + "  \n"
        + "  function tempCtor() {};\n"
        + "  tempCtor.prototype = parentCtor.prototype;\n"
        + "  childCtor.superClass_ = parentCtor.prototype;\n"
        + "  childCtor.prototype = new tempCtor();\n"
        + "  childCtor.prototype.constructor = childCtor;\n"
        + "};"
        + " function Top() {}\n"
        + "Top.prototype.f = function() {};"
        + " function Foo() {}\n"
        + "goog.inherits(Foo, Top);\n"
        + "\n"
        + "Foo.prototype.f = function() {"
        + "  Foo.superClass_.f();"
        + "};\n"
        + " function Bar() {}\n"
        + "goog.inherits(Bar, Foo);\n"
        + "\n"
        + "Bar.prototype.f = function() {"
        + "  Bar.superClass_.f();"
        + "};\n"
        + "(new Bar).f();\n";
    testSets(false, js, "{f=[[Top.prototype]]}");
    testSets(true, js, "{constructor=[[Bar.prototype, Foo.prototype]], "
                 + "f=[[Bar.prototype], [Foo.prototype], [Top.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeFunctionMethod
  public void testSkipNativeFunctionMethod() {
    String externs = ""
        + ""
        + "function Function(var_args) {}"
        + "Function.prototype.call = function() {};";
    String js = ""
        + " function Foo(){};"
        + ""
        + "function Bar() { Foo.call(this); };"; 
    testSame(externs, js, null);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeObjectMethod
  public void testSkipNativeObjectMethod() {
    String externs = ""
        + " function Object(opt_v) {}"
        + "Object.prototype.hasOwnProperty;";
    String js = ""
        + " function Foo(){};"
        + "(new Foo).hasOwnProperty('x');";
    testSets(false, externs, js, js, "{}");
    testSets(true, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testExtendNativeType
  public void testExtendNativeType() {
    String externs = ""
        + ""
        + "function Date(opt_1, opt_2, opt_3, opt_4, opt_5, opt_6, opt_7) {}"
        + " Date.prototype.toString = function() {}";
    String js = ""
        + " function SuperDate() {};\n"
        + "(new SuperDate).toString();";
    testSets(true, externs, js, js, "{}");
    testSets(false, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testStringFunction
  public void testStringFunction() {
    
    
    String externs = ""
         + "function String(opt_str) {};\n"
         + "\n"
         + "String.prototype.toString = function() { };\n";
    String js = ""
         + " function Foo() {};\n"
         + "Foo.prototype.foo = function() {};\n"
         + "String.prototype.foo = function() {};\n"
         + "var a = 'str'.toString().foo();\n";
    String output = ""
         + "function Foo() {};\n"
         + "Foo.prototype.Foo_prototype$foo = function() {};\n"
         + "String.prototype.String_prototype$foo = function() {};\n"
         + "var a = 'str'.toString().String_prototype$foo();\n";

    testSets(false, externs, js, output,
             "{foo=[[Foo.prototype], [String.prototype]]}");
    testSets(true, externs, js, output,
             "{foo=[[Foo.prototype], [String.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnusedTypeInExterns
  public void testUnusedTypeInExterns() {
    String externs = ""
        + " function Foo() {};\n"
        + "Foo.prototype.a";
    String js = ""
        + " function Bar() {};\n"
        + "Bar.prototype.a;"
        + " function Baz() {};\n"
        + "Baz.prototype.a;";
    String output = ""
        + " function Bar() {};\n"
        + "Bar.prototype.Bar_prototype$a;"
        + " function Baz() {};\n"
        + "Baz.prototype.Baz_prototype$a";
    testSets(false, externs, js, output,
             "{a=[[Bar.prototype], [Baz.prototype]]}");
    testSets(true, externs, js, output,
             "{a=[[Bar.prototype], [Baz.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterface
  public void testInterface() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function Foo() {};\n"
        + "Foo.prototype.a;\n"
        + "\n"
        + "var F = new Foo;"
        + "var x = F.a;";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype]]}");
    testSets(true, js, "{a=[[Foo.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterfaceOfSuperclass
  public void testInterfaceOfSuperclass() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function Foo() {};\n"
        + "Foo.prototype.a;\n"
        + " function Bar() {};\n"
        + "Bar.prototype.a;\n"
        + "\n"
        + "var B = new Bar;"
        + "B.a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype]]}");
    testSets(true, js,
        "{a=[[Bar.prototype], [Foo.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoInterfacesWithSomeInheritance
  public void testTwoInterfacesWithSomeInheritance() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + " function Foo() {};\n"
        + "Foo.prototype.a;\n"
        + "\n"
        + "function Bar() {};\n"
        + "Bar.prototype.a;\n"
        + "\n"
        + "var B = new Bar;"
        + "B.a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype, I2.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype], [Foo.prototype], "
                       + "[I.prototype], [I2.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInvalidatingInterface
  public void testInvalidatingInterface() {
    String js = ""
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + " function Bar() {}\n"
        + "\n"
        + "var i = new Bar;\n" 
        + ""
        + "function Foo() {};\n"
        + "\n"
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0;"
        + " function I() {};\n"
        + "I.prototype.a;\n";
    testSets(false, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING);
    testSets(true, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMultipleInterfaces
  public void testMultipleInterfaces() {
    String js = ""
        + " function I() {};\n"
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + ""
        + "function Foo() {};\n"
        + ""
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I2.prototype]]}");
    testSets(true, js, "{a=[[Foo.prototype], [I2.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterfaceWithSupertypeImplementor
  public void testInterfaceWithSupertypeImplementor() {
    String js = ""
        + " function C() {}\n"
        + "C.prototype.foo = function() {};\n"
        + " function A (){}\n"
        + "A.prototype.foo = function() {};\n"
        + "\n"
        + "function B() {}\n"
        + " var b = new B();\n"
        + "b.foo();\n";
    testSets(false, js, "{foo=[[A.prototype, C.prototype]]}");
    testSets(true, js, "{foo=[[A.prototype], [C.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSuperInterface
  public void testSuperInterface() {
    String js = ""
        + " function I() {};\n"
        + "I.prototype.a;\n"
        + " function I2() {};\n"
        + ""
        + "function Foo() {};\n"
        + "\n"
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype]]}");
    testSets(true, js, "{a=[[Foo.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterfaceUnionWithCtor
  public void testInterfaceUnionWithCtor() {
    String js = ""
        + " function I() {};\n"
        + " I.prototype.addEventListener;\n"
        + " function Impl() {};\n"
        + " Impl.prototype.addEventListener;"
        + " function C() {};\n"
        + " C.prototype.addEventListener;"
        + ""
        + "function f(x) { x.addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(false, js, js,
        "{addEventListener=[[C.prototype, I.prototype, Impl.prototype]]}");

    
    
    String tightenedOutput = ""
        + "function I() {};\n"
        + "I.prototype.I_prototype$addEventListener;\n"
        + "function Impl() {};\n"
        + "Impl.prototype.C_prototype$addEventListener;"
        + "function C() {};\n"
        + "C.prototype.C_prototype$addEventListener;"
        + ""
        + "function f(x) { x.C_prototype$addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(true, js, tightenedOutput,
        "{addEventListener=[[C.prototype, Impl.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testExternInterfaceUnionWithCtor
  public void testExternInterfaceUnionWithCtor() {
    String externs = ""
        + " function I() {};\n"
        + " I.prototype.addEventListener;\n"
        + " function Impl() {};\n"
        + " Impl.prototype.addEventListener;";

    String js = ""
        + " function C() {};\n"
        + " C.prototype.addEventListener;"
        + ""
        + "function f(x) { x.addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(false, externs, js, js, "{}");
    testSets(true, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMismatchInvalidation
  public void testMismatchInvalidation() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "\n"
        + "var F = new Bar;\n"
        + "F.a = 0;";

    testSets(false, "", js, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING,
             "initializing variable\n"
             + "found   : Bar\n"
             + "required: (Foo|null)");
    testSets(true, "", js, js, "{}", TypeValidator.TYPE_MISMATCH_WARNING,
             "initializing variable\n"
             + "found   : Bar\n"
             + "required: (Foo|null)");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testBadCast
  public void testBadCast() {
    String js = " function Foo() {};\n"
        + "Foo.prototype.a = 0;\n"
        + " function Bar() {};\n"
        + "Bar.prototype.a = 0;\n"
        + "var a =  (new Bar);\n"
        + "a.a = 4;";
    testSets(false, "", js, js, "{}",
             TypeValidator.INVALID_CAST,
             "invalid cast - must be a subtype or supertype\n"
             + "from: Bar\n"
             + "to  : Foo");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testDeterministicNaming
  public void testDeterministicNaming() {
    String js =
        "function A() {}\n"
        + "A.prototype.f = function() {return 'a';};\n"
        + "function B() {}\n"
        + "B.prototype.f = function() {return 'b';};\n"
        + "function C() {}\n"
        + "C.prototype.f = function() {return 'c';};\n"
        + "var ab = 1 ? new B : new A;\n"
        + "var n = ab.f();\n";

    String output =
        "function A() {}\n"
        + "A.prototype.A_prototype$f = function() { return'a'; };\n"
        + "function B() {}\n"
        + "B.prototype.A_prototype$f = function() { return'b'; };\n"
        + "function C() {}\n"
        + "C.prototype.C_prototype$f = function() { return'c'; };\n"
        + "var ab = 1 ? new B : new A; var n = ab.A_prototype$f();\n";

    for (int i = 0; i < 5; i++) {
      testSets(false, js, output,
          "{f=[[A.prototype, B.prototype], [C.prototype]]}");

      testSets(true, js, output,
          "{f=[[A.prototype, B.prototype], [C.prototype]]}");
    }
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteral
  public void testObjectLiteral() {
    String js = " function Foo() {}\n"
        + "Foo.prototype.a;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + "var F = ({ a: 'a' });\n";

    String output = "function Foo() {}\n"
        + "Foo.prototype.Foo_prototype$a;\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a;\n"
        + "var F = { Foo_prototype$a: 'a' };\n";

    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testCustomInherits
  public void testCustomInherits() {
    String js = "Object.prototype.inheritsFrom = function(shuper) {\n" +
        "  \n" +
        "  function Inheriter() { }\n" +
        "  Inheriter.prototype = shuper.prototype;\n" +
        "  this.prototype = new Inheriter();\n" +
        "  this.superConstructor = shuper;\n" +
        "};\n" +
        "function Foo(var1, var2, strength) {\n" +
        "  Foo.superConstructor.call(this, strength);\n" +
        "}" +
        "Foo.inheritsFrom(Object);";

    String externs = "" +
        "function Function(var_args) {}" +
        "Function.prototype.call = function(var_args) {};";

    testSets(false, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeFunctionStaticProperty
  public void testSkipNativeFunctionStaticProperty() {
    String js = ""
      + "\n"
      + "function addSingletonGetter(ctor) { ctor.a; }\n"
      + " function Foo() {}\n"
      + "Foo.a = 0;"
      + " function Bar() {}\n"
      + "Bar.a = 0;";

    String output = ""
        + "function addSingletonGetter(ctor){ctor.a}"
        + "function Foo(){}"
        + "Foo.a=0;"
        + "function Bar(){}"
        + "Bar.a=0";

    testSets(false, js, output, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testErrorOnProtectedProperty
  public void testErrorOnProtectedProperty() {
    test("function addSingletonGetter(foo) { foo.foobar = 'a'; };", null,
         DisambiguateProperties.Warnings.INVALIDATION);
    assertTrue(getLastCompiler().getErrors()[0].toString().contains("foobar"));
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMismatchForbiddenInvalidation
  public void testMismatchForbiddenInvalidation() {
    test(" function F() {}" +
         " F.prototype.foobar = 3;" +
         " function g() { return new F(); }",
         null,
         DisambiguateProperties.Warnings.INVALIDATION);
    assertTrue(getLastCompiler().getErrors()[0].toString()
        .contains("Consider fixing errors"));
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionTypeInvalidationError
  public void testUnionTypeInvalidationError() {
    String externs = ""
        + " function Baz() {}"
        + "Baz.prototype.foobar";
    String js = ""
        + " function Ind() {this.foobar=0}\n"
        + " function Foo() {}\n"
        + "Foo.prototype.foobar = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.foobar = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.foobar = 1\n;"
        + "F = new Bar;\n"
        + "\n"
        + "var Z = new Baz;\n"
        + "Z.foobar = 1\n;";

    test(
        externs, js, "",
        DisambiguateProperties.Warnings.INVALIDATION_ON_TYPE, null);
    assertTrue(getLastCompiler().getErrors()[0].toString()
        .contains("foobar"));
   }

// com.google.javascript.jscomp.ExpandJqueryAliasesTest::testJqueryFnAliasExpansion
  public void testJqueryFnAliasExpansion() {
    String setupCode = "var jQuery={};jQuery.fn=jQuery.prototype;";

    testSame(setupCode);

    test(setupCode + "jQuery.fn.foo='bar';",
        setupCode + "jQuery.prototype.foo='bar';");

    test(setupCode + "jQuerySub.fn.foo='bar';",
        setupCode + "jQuerySub.prototype.foo='bar';");
  }

// com.google.javascript.jscomp.ExpandJqueryAliasesTest::testJqueryExtendExpansion
  public void testJqueryExtendExpansion() {
    String setupCode = "var jQuery={},obj2={};";

    
    testSame(setupCode + "jQuery.extend()");

    
    
    testSame(setupCode + "jQuery.extend({})");

    
    test(setupCode + "jQuery.extend({a:'test'})",
        setupCode + "{jQuery.a = 'test';}");

    
    test(setupCode + "jQuery.fn=jQuery.prototype;" +
        "jQuery.fn.extend({a:'test', b:'test2'});",
        setupCode + "jQuery.fn=jQuery.prototype;" +
        "{jQuery.prototype.a = 'test'; jQuery.prototype.b = 'test2';}");

    
    test(setupCode + "jQuery.extend(obj2, {a:'test', b:'test2'});",
        setupCode + "{obj2=obj2||{}; obj2.a='test'; obj2.b='test2';}");

    
    
    test(setupCode + "jQuery.extend(jQuery,{a:'test', b:'test2'});",
        setupCode + "{jQuery = jQuery || {}; jQuery.a = 'test';" +
        "jQuery.b = 'test2';}");

    
    testSame(setupCode + "obj2.meth=function() { return { a:{} }; };" +
        "jQuery.extend(obj2.meth().a, {a: 'test'});");

    
    test(setupCode + "obj2 = jQuery.extend(obj2, {a:'test', " +
        "b:'test2'});",
        setupCode + "obj2 = function() {obj2 = obj2 || {}; " + 
        "obj2.a = 'test';obj2.b = 'test2';return obj2;}.call(this);");
  }

// com.google.javascript.jscomp.ExpandJqueryAliasesTest::testJqueryExpandedEachExpansion
  public void testJqueryExpandedEachExpansion() {
    String setupCode = "var jQuery={};" +
        "jQuery.expandedEach=function(vals, callback){};";
    String resultCode =
        "var jQuery={ expandedEach: function(vals, callback){} };";

    testSame(setupCode);

    
    test(setupCode + "jQuery.expandedEach({'a': 1, 'b': 2, 'c': 8}," +
        "function(key, val) { var a = key; jQuery[key] = val; });",
        resultCode + "(function(){ var a = 'a'; jQuery.a = 1 })();" +
        "(function(){ var a = 'b'; jQuery.b = 2 })();" +
        "(function(){ var a = 'c'; jQuery.c = 8 })();");

    
    
    
    
    test(setupCode + "jQuery.expandedEach(['a', 'b', 'c']," +
        "function(key, val){ jQuery[val] = key; });",
        resultCode + "(function(){ jQuery.a = 0; })();" +
        "(function(){ jQuery.b = 1; })();" +
        "(function(){ jQuery.c = 2 })();");

    
    test(setupCode + "jQuery.expandedEach({'a': 1, 'b': 2, 'c': 8}," +
        "function(key, val) { var a = key; jQuery[key] = this; });",
        resultCode + "(function(){ var a = 'a'; jQuery.a = 1 })();" +
        "(function(){ var a = 'b'; jQuery.b = 2 })();" +
        "(function(){ var a = 'c'; jQuery.c = 8 })();");

    
    test(setupCode + "jQuery.expandedEach(['a', 'b', 'c']," +
        "function(key, val){ jQuery[this] = key; });",
        resultCode + "(function(){ jQuery.a = 0; })();" +
        "(function(){ jQuery.b = 1; })();" +
        "(function(){ jQuery.c = 2 })();");

    
    test(setupCode + "jQuery.expandedEach(['a'], function(key,val) {" +
        "jQuery[val] = key; (function(key) { jQuery[key] = 1;})('test'); })",
        resultCode + "(function(){ jQuery.a = 0;" +
         "(function(key){ jQuery[key] = 1})('test') })()");

    
    test(setupCode + "jQuery.expandedEach(['a'], function(key,val) {" +
        "jQuery[val] = key; (function(key) { var val = 2;" +
        "jQuery[key] = val;})('test');})",
        resultCode + "(function(){" +
        "jQuery.a=0;" +
        "(function(key){var val = 2; jQuery[key] = val;})('test')})()");

    
    test(setupCode + "jQuery.expandedEach(['a'], function(key,val) {" +
        "jQuery[val] = key; (function(key1) {" +
        "function key() {}; key();" +
        "})('test');})",
        resultCode + "(function(){" +
        "jQuery.a=0;(function(key1) {" +
        "function key() {} key(); })('test')})()");

    
    test(setupCode + "alert(jQuery.expandedEach(['a']," +
        "function(key,val) { jQuery[val] = key;})[0])",
        resultCode + "alert((function(){" +
        "(function(){ jQuery.a = 0;})(); return ['a']})()[0]);");

    
    String testCode = "var a = ['a'];" +
        "jQuery.expandedEach(a, function(key,val){ jQuery[key]=val; })";
    test(setupCode + testCode, resultCode + testCode, null, INVALID_LIT_ERROR);

    
    test(setupCode + "var obj2={};" +
        "jQuery.expandedEach(['foo','bar'], function(i, name) {" +
        "obj2[ '[object ' + name + ']' ] = 'a';});",
        resultCode + "var obj2={};" +
        "jQuery.expandedEach(['foo','bar'], function(i, name) {" +
        "obj2[ '[object foo]' ] = 'a';});",
        null, USELESS_EACH_ERROR);

    
    testCode =
        "var obj2={}; jQuery.expandedEach(['foo','bar'], function(i, name) {" +
        "obj2[i] = 1;});";
    test(setupCode + testCode, resultCode + testCode, null, USELESS_EACH_ERROR);
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testExprExploitationTypes
  public void testExprExploitationTypes() {
    test("a = true; b = true",
         "b = a = true");
    test("a = !0; b = !0",
         "b = a = !0");
    test("a = !1; b = !1",
         "b = a = !1");
    test("a = void 0; b = void 0",
         "b = a = void 0");
    test("a = -Infinity; b = -Infinity",
         "b = a = -Infinity");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testExprExploitationTypes2
  public void testExprExploitationTypes2() {
    test("a = !0; b = !0",
         "b = a = !0");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testExprExploitation
  public void testExprExploitation() {
    test("a = null; b = null; var c = b",
         "var c = b = a = null");
    test("a = null; b = null",
         "b = a = null");
    test("a = undefined; b = undefined",
         "b = a = undefined");
    test("a = 0; b = 0", "b=a=0");
    test("a = 'foo'; b = 'foo'",
         "b = a = \"foo\"");
    test("a = c; b = c", "b=a=c");

    testSame("a = 0; b = 1");
    testSame("a = \"foo\"; b = \"foox\"");

    test("a = null; a && b;", "(a = null)&&b");
    test("a = null; a || b;", "(a = null)||b");

    test("a = null; a ? b : c;", "(a = null) ? b : c");

    test("a = null; this.foo = null;",
         "this.foo = a = null");
    test("function f(){ a = null; return null; }",
         "function f(){return a = null}");

    test("a = true; if (a) { foo(); }",
         "if (a = true) { foo() }");
    test("a = true; if (a && a) { foo(); }",
         "if ((a = true) && a) { foo() }");
    test("a = false; if (a) { foo(); }",
         "if (a = false) { foo() }");

    test("a = !0; if (a) { foo(); }",
        "if (a = !0) { foo() }");
    test("a = !0; if (a && a) { foo(); }",
        "if ((a = !0) && a) { foo() }");
    test("a = !1; if (a) { foo(); }",
        "if (a = !1) { foo() }");

    testSame("a = this.foo; a();");
    test("a = b; b = a;",
         "b = a = b");
    testSame("a = b; a.c = a");
    test("this.foo = null; this.bar = null;",
         "this.bar = this.foo = null");
    test("this.foo = null; this.bar = null; this.baz = this.bar",
         "this.baz = this.bar = this.foo = null");
    test("this.foo = null; a = null;",
         "a = this.foo = null");
    test("this.foo = null; a = this.foo;",
         "a = this.foo = null");
    test("a.b.c=null; a=null;",
         "a = a.b.c = null");
    testSame("a = null; a.b.c = null");
    test("(a=b).c = null; this.b = null;",
         "this.b = (a=b).c = null");
    testSame("if(x) a = null; else b = a");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testNestedExprExploitation
  public void testNestedExprExploitation() {
    test("this.foo = null; this.bar = null; this.baz = null;",
         "this.baz = this.bar = this.foo = null");

    test("a = 3; this.foo = a; this.bar = a; this.baz = 3;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; this.bar = this.foo; this.baz = a;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; this.bar = 3; this.baz = this.foo;",
         "this.baz = this.bar = this.foo = a = 3");
    test("a = 3; this.foo = a; a = 3; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.baz = a = this.bar = a = this.foo = a = 3");

    test("a = 4; this.foo = a; a = 3; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.foo = a = 4; a = this.bar = a = 3; this.baz = this.foo");
    test("a = 3; this.foo = a; a = 4; this.bar = 3; " +
         "a = 3; this.baz = this.foo;",
         "this.foo = a = 3; a = 4; a = this.bar = 3; this.baz = this.foo");
    test("a = 3; this.foo = a; a = 3; this.bar = 3; " +
         "a = 4; this.baz = this.foo;",
         "this.bar = a = this.foo = a = 3; a = 4; this.baz = this.foo");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testBug1840071
  public void testBug1840071() {
    
    
    test("a.b = a.x; if (a.x) {}", "if (a.b = a.x) {}");
    testSame("a.b = a.x; if (a.b) {}");
    test("a.b = a.c = a.x; if (a.x) {}", "if (a.b = a.c = a.x) {}");
    testSame("a.b = a.c = a.x; if (a.c) {}");
    testSame("a.b = a.c = a.x; if (a.b) {}");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testBug2072343
  public void testBug2072343() {
    testSame("a = a.x;a = a.x");
    testSame("a = a.x;b = a.x");
    test("b = a.x;a = a.x", "a = b = a.x");
    testSame("a.x = a;a = a.x");
    testSame("a.b = a.b.x;a.b = a.b.x");
    testSame("a.y = a.y.x;b = a.y;c = a.y.x");
    test("a = a.x;b = a;c = a.x", "b = a = a.x;c = a.x");
    test("b = a.x;a = b;c = a.x", "a = b = a.x;c = a.x");
 }

// com.google.javascript.jscomp.ExploitAssignsTest::testBadCollapseIntoCall
  public void testBadCollapseIntoCall() {
    
    
    testSame("this.foo = function() {}; this.foo();");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testBadCollapse
  public void testBadCollapse() {
    testSame("this.$e$ = []; this.$b$ = null;");
  }

// com.google.javascript.jscomp.ExploitAssignsTest::testIssue1017
  public void testIssue1017() {
    testSame("x = x.parentNode.parentNode; x = x.parentNode.parentNode;");
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testFunctionsAreExported
  public void testFunctionsAreExported() {
    test(TEST_FUNCTIONS_WITH_NAMES,
        "function Foo(arg){}; "
        + "function setUp(arg3){} google_exportSymbol(\"setUp\",setUp);; "
        + "function tearDown(arg,arg2) {} "
        + "google_exportSymbol(\"tearDown\",tearDown);; "
        + "function testBar(arg){} google_exportSymbol(\"testBar\",testBar)"
    );
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testBasicTestFunctionsAreExported
  public void testBasicTestFunctionsAreExported() {
    test("function Foo() {function testA() {}}",
         "function Foo() {function testA(){}}");
    test("function setUp() {}",
         "function setUp(){} google_exportSymbol('setUp',setUp)");
    test("function setUpPage() {}",
         "function setUpPage(){} google_exportSymbol('setUpPage',setUpPage)");
    test("function tearDown() {}",
         "function tearDown(){} google_exportSymbol('tearDown',tearDown)");
    test("function tearDownPage() {}",
         "function tearDownPage(){} google_exportSymbol('tearDownPage'," +
         "tearDownPage)");
    test("function testBar() { function testB() {}}",
         "function testBar(){function testB(){}}"
             + "google_exportSymbol('testBar',testBar)");
    testSame("var testCase = {}; testCase.setUpPage = function() {}");
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testFunctionExpressionsAreExported
  public void testFunctionExpressionsAreExported() {
    test("var Foo = function() {var testA = function() {}}",
         "var Foo = function() {var testA = function() {}}");
    test("var setUp = function() {}",
         "var setUp = function() {}; " +
         "google_exportSymbol('setUp',setUp)");
    test("var setUpPage = function() {}",
         "var setUpPage = function() {}; " +
         "google_exportSymbol('setUpPage',setUpPage)");
    test("var tearDown = function() {}",
         "var tearDown = function() {}; " +
         "google_exportSymbol('tearDown',tearDown)");
    test("var tearDownPage = function() {}",
         "var tearDownPage = function() {}; " +
         "google_exportSymbol('tearDownPage', tearDownPage)");
    test("var testBar = function() { var testB = function() {}}",
         "var testBar = function(){ var testB = function() {}}; " +
         "google_exportSymbol('testBar',testBar)");
  }

// com.google.javascript.jscomp.ExportTestFunctionsTest::testFunctionAssignmentsAreExported
  public void testFunctionAssignmentsAreExported() {
    test("Foo = {}; Foo.prototype.bar = function() {};",
         "Foo = {}; Foo.prototype.bar = function() {};");

    test("Foo = {}; Foo.prototype.setUpPage = function() {};",
         "Foo = {}; Foo.prototype.setUpPage = function() {};"
         + "google_exportProperty(Foo.prototype, 'setUpPage', "
         + "Foo.prototype.setUpPage);");

    test("Foo = {}; Foo.prototype.testBar = function() {};",
         "Foo = {}; Foo.prototype.testBar = function() {};"
         + "google_exportProperty(Foo.prototype, 'testBar', "
         + "Foo.prototype.testBar);");

    test("Foo = {}; Foo.prototype.testBar = function() "
         + "{ var testBaz = function() {}};",
         "Foo = {}; Foo.prototype.testBar = function() "
         + "{ var testBaz = function() {}};"
         + "google_exportProperty(Foo.prototype, 'testBar', "
         + "Foo.prototype.testBar);");

    test("Foo = {}; Foo.baz.prototype.testBar = function() "
         + "{ var testBaz = function() {}};",
         "Foo = {}; Foo.baz.prototype.testBar = function() "
         + "{ var testBaz = function() {}};"
         + "google_exportProperty(Foo.baz.prototype, 'testBar', "
         + "Foo.baz.prototype.testBar);");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression1
  public void testCanExposeExpression1() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "while(foo());", "foo");
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "while(x = goo()&&foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "while(x += goo()&&foo()){}", "foo");

    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "do{}while(foo());", "foo");
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "for(;foo(););", "foo");
    
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "for(;;foo());", "foo");
    
    

    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "switch(1){case foo():;}", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression2
  public void testCanExposeExpression2() {
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "var x = foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "if(foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "switch(foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "switch(foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "function f(){ return foo();}", "foo");

    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() && 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() || 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() ? 0 : 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "(function(a){b = a})(foo())", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression3
  public void testCanExposeExpression3() {
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x = 0 && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x = 1 || foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "var x = 1 ? foo() : 0", "foo");

    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x = goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x += goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "var x = goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "if(goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "switch(goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "switch(goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "switch(x = goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE,
        "function f(){ return goo() && foo();}", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression4
  public void testCanExposeExpression4() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "if (goo.a(1, foo()));", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression5
  public void testCanExposeExpression5() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "if (goo['a'](foo()));", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression6
  public void testCanExposeExpression6() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "z:if (goo.a(1, foo()));", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression7
  public void testCanExposeExpression7() {
    
    helperCanExposeFunctionExpression(
        DecompositionType.MOVABLE,
        "(function(map){descriptions_=map})(\n" +
            "function(){\n" +
                "var ret={};\n" +
                "ret[INIT]='a';\n" +
                "ret[MIGRATION_BANNER_DISMISS]='b';\n" +
                "return ret\n" +
            "}()\n" +
        ");", 2);
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression8
  public void testCanExposeExpression8() {
    
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE,
        "HangoutStarter.prototype.launchHangout = function() {\n" +
        "  var self = a.b;\n" +
        "  var myUrl = new goog.Uri(getDomServices_(self).getDomHelper()." +
        "getWindow().location.href);\n" +
        "};",
        "getDomServices_");

    
    helperExposeExpression(
        "HangoutStarter.prototype.launchHangout = function() {\n" +
        "  var self = a.b;\n" +
        "  var myUrl = new goog.Uri(getDomServices_(self).getDomHelper()." +
        "getWindow().location.href);\n" +
        "};",
        "getDomServices_",
        "HangoutStarter.prototype.launchHangout = function() {" +
        "  var self = a.b;" +
        "  var temp_const$$0 = goog.Uri;" +
        "  var myUrl = new temp_const$$0(getDomServices_(self)." +
        "      getDomHelper().getWindow().location.href)}");

    
    helperMoveExpression(
        "HangoutStarter.prototype.launchHangout = function() {" +
        "  var self = a.b;" +
        "  var temp_const$$0 = goog.Uri;" +
        "  var myUrl = new temp_const$$0(getDomServices_(self)." +
        "      getDomHelper().getWindow().location.href)}",
        "getDomServices_",
        "HangoutStarter.prototype.launchHangout = function() {" +
        "  var self=a.b;" +
        "  var temp_const$$0=goog.Uri;" +
        "  var result$$0=getDomServices_(self);" +
        "  var myUrl=new temp_const$$0(result$$0.getDomHelper()." +
        "      getWindow().location.href)}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression1
  public void testMoveExpression1() {
    
    helperMoveExpression("foo()", "foo", "var result$$0 = foo(); result$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression2
  public void testMoveExpression2() {
    helperMoveExpression(
        "x = foo()",
        "foo",
        "var result$$0 = foo(); x = result$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression3
  public void testMoveExpression3() {
    helperMoveExpression(
        "var x = foo()",
        "foo",
        "var result$$0 = foo(); var x = result$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression4
  public void testMoveExpression4() {
    helperMoveExpression(
        "if(foo()){}",
        "foo",
        "var result$$0 = foo(); if (result$$0);");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression5
  public void testMoveExpression5() {
    helperMoveExpression(
        "switch(foo()){}",
        "foo",
        "var result$$0 = foo(); switch(result$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression6
  public void testMoveExpression6() {
    helperMoveExpression(
        "switch(1 + foo()){}",
        "foo",
        "var result$$0 = foo(); switch(1 + result$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression7
  public void testMoveExpression7() {
    helperMoveExpression(
        "function f(){ return foo();}",
        "foo",
        "function f(){ var result$$0 = foo(); return result$$0;}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression8
  public void testMoveExpression8() {
    helperMoveExpression(
        "x = foo() && 1",
        "foo",
        "var result$$0 = foo(); x = result$$0 && 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression9
  public void testMoveExpression9() {
    helperMoveExpression(
        "x = foo() || 1",
        "foo",
        "var result$$0 = foo(); x = result$$0 || 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression10
  public void testMoveExpression10() {
    helperMoveExpression(
        "x = foo() ? 0 : 1",
        "foo",
        "var result$$0 = foo(); x = result$$0 ? 0 : 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression1
  public void testExposeExpression1() {
    helperExposeExpression(
        "x = 0 && foo()",
        "foo",
        "var temp$$0; if (temp$$0 = 0) temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression2
  public void testExposeExpression2() {
    helperExposeExpression(
        "x = 1 || foo()",
        "foo",
        "var temp$$0; if (temp$$0 = 1); else temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression3
  public void testExposeExpression3() {
    helperExposeExpression(
        "var x = 1 ? foo() : 0",
        "foo",
        "var temp$$0;" +
        " if (1) temp$$0 = foo(); else temp$$0 = 0;var x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression4
  public void testExposeExpression4() {
    helperExposeExpression(
        "goo() && foo()",
        "foo",
        "if (goo()) foo();");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression5
  public void testExposeExpression5() {
    helperExposeExpression(
        "x = goo() && foo()",
        "foo",
        "var temp$$0; if (temp$$0 = goo()) temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression6
  public void testExposeExpression6() {
    helperExposeExpression(
        "var x = 1 + (goo() && foo())",
        "foo",
        "var temp$$0; if (temp$$0 = goo()) temp$$0 = foo();" +
        "var x = 1 + temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression7
  public void testExposeExpression7() {
    helperExposeExpression(
        "if(goo() && foo());",
        "foo",
        "var temp$$0;" +
        "if (temp$$0 = goo()) temp$$0 = foo();" +
        "if(temp$$0);");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression8
  public void testExposeExpression8() {
    helperExposeExpression(
        "switch(goo() && foo()){}",
        "foo",
        "var temp$$0;" +
        "if (temp$$0 = goo()) temp$$0 = foo();" +
        "switch(temp$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression9
  public void testExposeExpression9() {
    helperExposeExpression(
        "switch(1 + goo() + foo()){}",
        "foo",
        "var temp_const$$0 = 1 + goo();" +
        "switch(temp_const$$0 + foo()){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression10
  public void testExposeExpression10() {
    helperExposeExpression(
        "function f(){ return goo() && foo();}",
        "foo",
        "function f(){" +
          "var temp$$0; if (temp$$0 = goo()) temp$$0 = foo();" +
          "return temp$$0;" +
         "}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression11
  public void testExposeExpression11() {
    
    
    helperExposeExpression(
        "if (goo(1, goo(2), (1 ? foo() : 0)));",
        "foo",
        "var temp_const$$1 = goo;" +
        "var temp_const$$0 = goo(2);" +
        "var temp$$2;" +
        "if (1) temp$$2 = foo(); else temp$$2 = 0;" +
        "if (temp_const$$1(1, temp_const$$0, temp$$2));");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals1
  public void testExposePlusEquals1() {
    helperExposeExpression(
        "var x = 0; x += foo() + 1",
        "foo",
        "var x = 0; var temp_const$$0 = x;" +
        "x = temp_const$$0 + (foo() + 1);");

    helperExposeExpression(
        "var x = 0; y = (x += foo()) + x",
        "foo",
        "var x = 0; var temp_const$$0 = x;" +
        "y = (x = temp_const$$0 + foo()) + x");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals2
  public void testExposePlusEquals2() {
    helperExposeExpression(
        "var x = {}; x.a += foo() + 1",
        "foo",
        "var x = {}; var temp_const$$0 = x;" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "temp_const$$0.a = temp_const$$1 + (foo() + 1);");

    helperExposeExpression(
        "var x = {}; y = (x.a += foo()) + x.a",
        "foo",
        "var x = {}; var temp_const$$0 = x;" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "y = (temp_const$$0.a = temp_const$$1 + foo()) + x.a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals3
  public void testExposePlusEquals3() {
    helperExposeExpression(
        " var XX = {};\n" +
        "XX.a += foo() + 1",
        "foo",
        "var XX = {}; var temp_const$$0 = XX.a;" +
        "XX.a = temp_const$$0 + (foo() + 1);");

    helperExposeExpression(
        "var XX = {}; y = (XX.a += foo()) + XX.a",
        "foo",
        "var XX = {}; var temp_const$$0 = XX.a;" +
        "y = (XX.a = temp_const$$0 + foo()) + XX.a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals4
  public void testExposePlusEquals4() {
    helperExposeExpression(
        "var x = {}; goo().a += foo() + 1",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo();" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "temp_const$$0.a = temp_const$$1 + (foo() + 1);");

    helperExposeExpression(
        "var x = {}; y = (goo().a += foo()) + goo().a",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo();" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "y = (temp_const$$0.a = temp_const$$1 + foo()) + goo().a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals5
  public void testExposePlusEquals5() {
    helperExposeExpression(
        "var x = {}; goo().a.b += foo() + 1",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo().a;" +
        "var temp_const$$1 = temp_const$$0.b;" +
        "temp_const$$0.b = temp_const$$1 + (foo() + 1);");

    helperExposeExpression(
        "var x = {}; y = (goo().a.b += foo()) + goo().a",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo().a;" +
        "var temp_const$$1 = temp_const$$0.b;" +
        "y = (temp_const$$0.b = temp_const$$1 + foo()) + goo().a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeObjectLit1
  public void testExposeObjectLit1() {
    
    
    
    
    helperMoveExpression(
        "var x = {get a() {}, b: foo()};",
        "foo",
        "var result$$0=foo();var x = {get a() {}, b: result$$0};");

    helperMoveExpression(
        "var x = {set a(p) {}, b: foo()};",
        "foo",
        "var result$$0=foo();var x = {set a(p) {}, b: result$$0};");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbol
  public void testExportSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolDefinedInVar
  public void testExportSymbolDefinedInVar() throws Exception {
    compileAndCheck("var a = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportProperty
  public void testExportProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'cprop', a.b.c)",
                    "var a;\n" +
                    "a.b;\n" +
                    "\n" +
                    "a.b.cprop = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple
  public void testExportMultiple() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('a.b', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var a;\n" +
                    "\n" +
                    "a.b = function(p1) {\n};\n" +
                    "\n" +
                    "a.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "a.b.prototype.c = function(g, h, i) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple2
  public void testExportMultiple2() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('hello', a);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "\n" +
                    "var hello = {};\n" +
                    "hello.b;\n" +
                    "\n" +
                    "hello.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "hello.b.prototype.c = function(g, h, i) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple3
  public void testExportMultiple3() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('prefix', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);",

                    "\n" +
                    "var prefix = function(p1) {\n};\n" +
                    "\n" +
                    "prefix.c = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonStaticSymbol
  public void testExportNonStaticSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; var d = {}; a.b.c = d;" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "var foobar;\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonStaticSymbol2
  public void testExportNonStaticSymbol2() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; var d = null; a.b.c = d;" +
                    "goog.exportSymbol('foobar', a.b.c())",
                    "var foobar;\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonexistentProperty
  public void testExportNonexistentProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'none', a.b.none)",
                    "var a;\n" +
                    "a.b;\n" +
                    "a.b.none;\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithTypeAnnotation
  public void testExportSymbolWithTypeAnnotation() {

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1, param2) {" +
                      "return param1 + param2;" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function(param1, param2) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithTemplateAnnotation
  public void testExportSymbolWithTemplateAnnotation() {

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1) {" +
                      "return param1;" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function(param1) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithMultipleTemplateAnnotation
  public void testExportSymbolWithMultipleTemplateAnnotation() {

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1) {" +
                      "return  (param1);" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function(param1) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithoutTypeCheck
  public void testExportSymbolWithoutTypeCheck() {
    
    
    setRunCheckTypes(false);

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1, param2) {" +
                      "return param1 + param2;" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "var externalName = function(param1, param2) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithConstructor
  public void testExportSymbolWithConstructor() {
    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function() {" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function() {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithConstructorWithoutTypeCheck
  public void testExportSymbolWithConstructorWithoutTypeCheck() {
    
    
    
    
    

    setRunCheckTypes(false);

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function() {" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "var externalName = function() {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithOptionalArguments1
  public void testExportFunctionWithOptionalArguments1() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithOptionalArguments2
  public void testExportFunctionWithOptionalArguments2() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "  return 6;\n" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithOptionalArguments3
  public void testExportFunctionWithOptionalArguments3() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "  return a;\n" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithVariableArguments
  public void testExportFunctionWithVariableArguments() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "  return 6;\n" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportEnum
   public void testExportEnum() {
     
     
     
     
     compileAndCheck(
         " var E = {A:8, B:9};" +
         "goog.exportSymbol('E', E);",
         "\n" +
         "var E = {A:1, B:2};\n");
   }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportDontEmitPrototypePathPrefix
  public void testExportDontEmitPrototypePathPrefix() {
    compileAndCheck(
        "\n" +
        "var Foo = function() {};" +
        "\n" +
        "Foo.prototype.m = function() {return 6;};\n" +
        "goog.exportSymbol('Foo', Foo);\n" +
        "goog.exportProperty(Foo.prototype, 'm', Foo.prototype.m);",
        "\n" +
        "var Foo = function() {\n};\n" +
        "\n" +
        "Foo.prototype.m = function() {\n};\n"
    );
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testUseExportsAsExterns
  public void testUseExportsAsExterns() {
    String librarySource =
    "\n" +
    "var InternalName = function(a) {" +
    "};" +
    "goog.exportSymbol('ExternalName', InternalName)";

    String clientSource =
      "var a = new ExternalName(6);\n" +
      "\n" +
      "var b = function(x) {};";

    Result libraryCompileResult = compileAndExportExterns(librarySource);

    assertEquals(0, libraryCompileResult.warnings.length);
    assertEquals(0, libraryCompileResult.errors.length);

    String generatedExterns = libraryCompileResult.externExport;

    Result clientCompileResult = compileAndExportExterns(clientSource,
        generatedExterns);

    assertEquals(0, clientCompileResult.warnings.length);
    assertEquals(0, clientCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testWarnOnExportFunctionWithUnknownReturnType
  public void testWarnOnExportFunctionWithUnknownReturnType() {
    String librarySource =
      "var InternalName = function() {" +
      "  return 6;" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(1, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testDontWarnOnExportConstructorWithUnknownReturnType
  public void testDontWarnOnExportConstructorWithUnknownReturnType() {
    String librarySource =
      "\n " +
      "var InternalName = function() {" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(0, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testTypedef
  public void testTypedef() {
    compileAndCheck(
        " var Coord;\n" +
        "\n" +
        "var fn = function(a) {};" +
        "goog.exportSymbol('fn', fn);",
        "\n" +
        "var fn = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportParamWithNull
  public void testExportParamWithNull() throws Exception {
    compileAndCheck(
        "\n" +
        "var f = function(d) {};\n" +
        "goog.exportSymbol('foobar', f)\n",
        "\n" +
        "var foobar = function(d) {\n" +
        "};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportConstructor
  public void testExportConstructor() throws Exception {
    compileAndCheck(" var a = function() {};" +
                    "goog.exportSymbol('foobar', a)",
                    "\n" +
                    "var foobar = function() {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportParamWithSymbolDefinedInFunction
  public void testExportParamWithSymbolDefinedInFunction() throws Exception {
    compileAndCheck(
        "var id = function() {return 'id'};\n" +
        "var ft = function() {\n" +
        "  var id;\n" +
        "  return 1;\n" +
        "};\n" +
        "goog.exportSymbol('id', id);\n",
        "\n" +
        "var id = function() {\n" +
        "};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithFunctionDefinedAsFunction
  public void testExportSymbolWithFunctionDefinedAsFunction() {

    compileAndCheck("\n" +
                    "function internalName(param1) {" +
                      "return param1" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function(param1) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithFunctionAlias
  public void testExportSymbolWithFunctionAlias() {

    compileAndCheck("\n" +
                    "var y = function(param1) {" +
                    "};" +
                    "\n" +
                    "var x = function y(param1, param2) {" +
                    "};" +
                    "goog.exportSymbol('externalName', y)",
                    "\n" +
                    "var externalName = function(param1) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testWarnOnExportFunctionWithUnknownParameterTypes
  public void testWarnOnExportFunctionWithUnknownParameterTypes() {
    
    String librarySource =
      "\n " +
      "var InternalName = function(a,b,c) {" +
      "  return 6;" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(2, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testNotEnoughPrototypeToExtract
  public void testNotEnoughPrototypeToExtract() {
    
    for (int i = 0; i < 7; i++) {
      testSame(generatePrototypeDeclarations("x", i));
    }
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingSingleClassPrototype
  public void testExtractingSingleClassPrototype() {
    extract(generatePrototypeDeclarations("x", 7),
        loadPrototype("x") +
        generateExtractedDeclarations(7));
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingTwoClassPrototype
  public void testExtractingTwoClassPrototype() {
    extract(
        generatePrototypeDeclarations("x", 6) +
        generatePrototypeDeclarations("y", 6),
        loadPrototype("x") +
        generateExtractedDeclarations(6) +
        loadPrototype("y") +
        generateExtractedDeclarations(6));
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingTwoClassPrototypeInDifferentBlocks
  public void testExtractingTwoClassPrototypeInDifferentBlocks() {
    extract(
        generatePrototypeDeclarations("x", 6) +
        "if (foo()) {" +
        generatePrototypeDeclarations("y", 6) +
        "}",
        loadPrototype("x") +
        generateExtractedDeclarations(6) +
        "if (foo()) {" +
        loadPrototype("y") +
        generateExtractedDeclarations(6) +
        "}");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testNoMemberDeclarations
  public void testNoMemberDeclarations() {
    testSame(
        "x.prototype = {}; x.prototype = {}; x.prototype = {};" +
        "x.prototype = {}; x.prototype = {}; x.prototype = {};" +
        "x.prototype = {}; x.prototype = {}; x.prototype = {};");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingPrototypeWithQName
  public void testExtractingPrototypeWithQName() {
    extract(
        generatePrototypeDeclarations("com.google.javascript.jscomp.x", 7),
        loadPrototype("com.google.javascript.jscomp.x") +
        generateExtractedDeclarations(7));
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testInterweaved
  public void testInterweaved() {
    testSame(
        "x.prototype.a=1; y.prototype.a=1;" +
        "x.prototype.b=1; y.prototype.b=1;" +
        "x.prototype.c=1; y.prototype.c=1;" +
        "x.prototype.d=1; y.prototype.d=1;" +
        "x.prototype.e=1; y.prototype.e=1;" +
        "x.prototype.f=1; y.prototype.f=1;");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testExtractingPrototypeWithNestedMembers
  public void testExtractingPrototypeWithNestedMembers() {
    extract(
        "x.prototype.y.a = 1;" +
        "x.prototype.y.b = 1;" +
        "x.prototype.y.c = 1;" +
        "x.prototype.y.d = 1;" +
        "x.prototype.y.e = 1;" +
        "x.prototype.y.f = 1;" +
        "x.prototype.y.g = 1;",
        loadPrototype("x") +
        TMP + ".y.a = 1;" +
        TMP + ".y.b = 1;" +
        TMP + ".y.c = 1;" +
        TMP + ".y.d = 1;" +
        TMP + ".y.e = 1;" +
        TMP + ".y.f = 1;" +
        TMP + ".y.g = 1;");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testWithDevirtualization
  public void testWithDevirtualization() {
    extract(
        "x.prototype.a = 1;" +
        "x.prototype.b = 1;" +
        "function devirtualize1() { }" +
        "x.prototype.c = 1;" +
        "x.prototype.d = 1;" +
        "x.prototype.e = 1;" +
        "x.prototype.f = 1;" +
        "x.prototype.g = 1;",

        loadPrototype("x") +
        TMP + ".a = 1;" +
        TMP + ".b = 1;" +
        "function devirtualize1() { }" +
        TMP + ".c = 1;" +
        TMP + ".d = 1;" +
        TMP + ".e = 1;" +
        TMP + ".f = 1;" +
        TMP + ".g = 1;");

    extract(
        "x.prototype.a = 1;" +
        "x.prototype.b = 1;" +
        "function devirtualize1() { }" +
        "x.prototype.c = 1;" +
        "x.prototype.d = 1;" +
        "function devirtualize2() { }" +
        "x.prototype.e = 1;" +
        "x.prototype.f = 1;" +
        "function devirtualize3() { }" +
        "x.prototype.g = 1;",

        loadPrototype("x") +
        TMP + ".a = 1;" +
        TMP + ".b = 1;" +
        "function devirtualize1() { }" +
        TMP + ".c = 1;" +
        TMP + ".d = 1;" +
        "function devirtualize2() { }" +
        TMP + ".e = 1;" +
        TMP + ".f = 1;" +
        "function devirtualize3() { }" +
        TMP + ".g = 1;");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testAnonSimple
  public void testAnonSimple() {
    pattern = Pattern.USE_ANON_FUNCTION;

    extract(
        generatePrototypeDeclarations("x", 3),
        generateExtractedDeclarations(3) +
        loadPrototype("x"));

    testSame(generatePrototypeDeclarations("x", 1));
    testSame(generatePrototypeDeclarations("x", 2));

    extract(
        generatePrototypeDeclarations("x", 7),
        generateExtractedDeclarations(7) +
        loadPrototype("x"));

  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testAnonWithDevirtualization
  public void testAnonWithDevirtualization() {
    pattern = Pattern.USE_ANON_FUNCTION;

    extract(
        "x.prototype.a = 1;" +
        "x.prototype.b = 1;" +
        "function devirtualize() { }" +
        "x.prototype.c = 1;",

        "(function(" + TMP + "){" +
        TMP + ".a = 1;" +
        TMP + ".b = 1;" +
        TMP + ".c = 1;" +
        loadPrototype("x") +
        "function devirtualize() { }");

    extract(
        "x.prototype.a = 1;" +
        "function devirtualize1() { }" +
        "x.prototype.b = 1;" +
        "function devirtualize2() { }" +
        "x.prototype.c = 1;" +
        "function devirtualize3() { }",

        "(function(" + TMP + "){" +
        TMP + ".a = 1;" +
        TMP + ".b = 1;" +
        TMP + ".c = 1;" +
        loadPrototype("x") +
        "function devirtualize1() { }" +
        "function devirtualize2() { }" +
        "function devirtualize3() { }");
  }

// com.google.javascript.jscomp.ExtractPrototypeMemberDeclarationsTest::testAnonWithSideFx
  public void testAnonWithSideFx() {
    pattern = Pattern.USE_ANON_FUNCTION;
    testSame(
        "function foo() {};" +
        "foo.prototype.a1 = 1;" +
        "bar();;" +
        "foo.prototype.a2 = 2;" +
        "bar();;" +
        "foo.prototype.a3 = 3;" +
        "bar();;" +
        "foo.prototype.a4 = 4;" +
        "bar();;" +
        "foo.prototype.a5 = 5;" +
        "bar();;" +
        "foo.prototype.a6 = 6;" +
        "bar();;" +
        "foo.prototype.a7 = 7;" +
        "bar();");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleAssign
  public void testSimpleAssign() {
    inline("var x; x = 1; print(x)", "var x; print(1)");
    inline("var x; x = 1; x", "var x; 1");
    inline("var x; x = 1; var a = x", "var x; var a = 1");
    inline("var x; x = 1; x = x + 1", "var x; x = 1 + 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleVar
  public void testSimpleVar() {
    inline("var x = 1; print(x)", "var x; print(1)");
    inline("var x = 1; x", "var x; 1");
    inline("var x = 1; var a = x", "var x; var a = 1");
    inline("var x = 1; x = x + 1", "var x; x = 1 + 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleForIn
  public void testSimpleForIn() {
    inline("var a,b,x = a in b; x",
           "var a,b,x; a in b");
    noInline("var a, b; var x = a in b; print(1); x");
    noInline("var a,b,x = a in b; delete a[b]; x");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testExported
  public void testExported() {
    noInline("var _x = 1; print(_x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineIncrement
  public void testDoNotInlineIncrement() {
    noInline("var x = 1; x++;");
    noInline("var x = 1; x--;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineAssignmentOp
  public void testDoNotInlineAssignmentOp() {
    noInline("var x = 1; x += 1;");
    noInline("var x = 1; x -= 1;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineIntoLhsOfAssign
  public void testDoNotInlineIntoLhsOfAssign() {
    noInline("var x = 1; x += 3;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUse
  public void testMultiUse() {
    noInline("var x; x = 1; print(x); print (x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUseInSameCfgNode
  public void testMultiUseInSameCfgNode() {
    noInline("var x; x = 1; print(x) || print (x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiUseInTwoDifferentPath
  public void testMultiUseInTwoDifferentPath() {
    noInline("var x = 1; if (print) { print(x) } else { alert(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testAssignmentBeforeDefinition
  public void testAssignmentBeforeDefinition() {
    inline("x = 1; var x = 0; print(x)","x = 1; var x; print(0)" );
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testVarInConditionPath
  public void testVarInConditionPath() {
    noInline("if (foo) { var x = 0 } print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiDefinitionsBeforeUse
  public void testMultiDefinitionsBeforeUse() {
    inline("var x = 0; x = 1; print(x)", "var x = 0; print(1)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testMultiDefinitionsInSameCfgNode
  public void testMultiDefinitionsInSameCfgNode() {
    noInline("var x; (x = 1) || (x = 2); print(x)");
    noInline("var x; x = (1 || (x = 2)); print(x)");
    noInline("var x;(x = 1) && (x = 2); print(x)");
    noInline("var x;x = (1 && (x = 2)); print(x)");
    noInline("var x; x = 1 , x = 2; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNotReachingDefinitions
  public void testNotReachingDefinitions() {
    noInline("var x; if (foo) { x = 0 } print (x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineLoopCarriedDefinition
  public void testNoInlineLoopCarriedDefinition() {
    
    noInline("var x; while(true) { print(x); x = 1; }");

    
    noInline("var x = 0; while(true) { print(x); x = 1; }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotExitLoop
  public void testDoNotExitLoop() {
    noInline("while (z) { var x = 3; } var y = x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineWithinLoop
  public void testDoNotInlineWithinLoop() {
    noInline("var y = noSFX(); do { var z = y.foo(); } while (true);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression1
  public void testDoNotInlineCatchExpression1() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "}catch(err) {" +
        "   a = err;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression1a
  public void testDoNotInlineCatchExpression1a() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "}catch(err) {" +
        "   a = err + 1;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression2
  public void testDoNotInlineCatchExpression2() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  if (x) {throw Error(\"\");}\n" +
        "}catch(err) {" +
        "   a = err;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression3
  public void testDoNotInlineCatchExpression3() {
    noInline(
        "var a;\n" +
        "try {\n" +
        "  throw Error(\"\");\n" +
        "} catch(err) {" +
        "  err = x;\n" +
        "  a = err;\n" +
        "}\n" +
        "return a.stack\n");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDoNotInlineCatchExpression4
  public void testDoNotInlineCatchExpression4() {
    
    noInline(
        "try {\n" +
        " stuff();\n" +
        "} catch (e) {\n" +
        " x = e;\n" +
        " print(x);\n" +
        "}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDefinitionAfterUse
  public void testDefinitionAfterUse() {
    inline("var x = 0; print(x); x = 1", "var x; print(0); x = 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineSameVariableInStraightLine
  public void testInlineSameVariableInStraightLine() {
    inline("var x; x = 1; print(x); x = 2; print(x)",
        "var x; print(1); print(2)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineInDifferentPaths
  public void testInlineInDifferentPaths() {
    inline("var x; if (print) {x = 1; print(x)} else {x = 2; print(x)}",
        "var x; if (print) {print(1)} else {print(2)}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineInMergedPath
  public void testNoInlineInMergedPath() {
    noInline(
        "var x,y;x = 1;while(y) { if(y){ print(x) } else { x = 1 } } print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineIntoExpressions
  public void testInlineIntoExpressions() {
    inline("var x = 1; print(x + 1);", "var x; print(1 + 1)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions1
  public void testInlineExpressions1() {
    inline("var a, b; var x = a+b; print(x)", "var a, b; var x; print(a+b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions2
  public void testInlineExpressions2() {
    
    noInline("var a, b; var x = a + b; a = 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions3
  public void testInlineExpressions3() {
    inline("var a,b,x; x=a+b; x=a-b ; print(x)",
           "var a,b,x; x=a+b; print(a-b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions4
  public void testInlineExpressions4() {
    
    noInline("var a,b,x; x=a+b, x=a-b; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions5
  public void testInlineExpressions5() {
    noInline("var a; var x = a = 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions6
  public void testInlineExpressions6() {
    noInline("var a, x; a = 1 + (x = 1); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression7
  public void testInlineExpression7() {
    
    noInline("var x = foo() + 1; bar(); print(x)");

    
    
    
    noInline("var x = foo() + 1; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression8
  public void testInlineExpression8() {
    
    inline(
        "var a,b;" +
        "var x = a + b; print(x);      x = a - b; print(x)",
        "var a,b;" +
        "var x;         print(a + b);             print(a - b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression9
  public void testInlineExpression9() {
    
    inline(
        "var a,b;" +
        "var x; if (g) { x= a + b; print(x)    }  x = a - b; print(x)",
        "var a,b;" +
        "var x; if (g) {           print(a + b)}             print(a - b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression10
  public void testInlineExpression10() {
    
    noInline("var x, y; x = ((y = 1), print(y))");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions11
  public void testInlineExpressions11() {
    inline("var x; x = x + 1; print(x)", "var x; print(x + 1)");
    noInline("var x; x = x + 1; print(x); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions12
  public void testInlineExpressions12() {
    
    
    noInline("var x = 10; x = c++; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpressions13
  public void testInlineExpressions13() {
    inline("var a = 1, b = 2;" +
           "var x = a;" +
           "var y = b;" +
           "var z = x + y;" +
           "var i = z;" +
           "var j = z + y;" +
           "var k = i;",

           "var a, b;" +
           "var x;" +
           "var y = 2;" +
           "var z = 1 + y;" +
           "var i;" +
           "var j = z + y;" +
           "var k = z;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineIfDefinitionMayNotReach
  public void testNoInlineIfDefinitionMayNotReach() {
    noInline("var x; if (x=1) {} x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineEscapedToInnerFunction
  public void testNoInlineEscapedToInnerFunction() {
    noInline("var x = 1; function foo() { x = 2 }; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineLValue
  public void testNoInlineLValue() {
    noInline("var x; if (x = 1) { print(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSwitchCase
  public void testSwitchCase() {
    inline("var x = 1; switch(x) { }", "var x; switch(1) { }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testShadowedVariableInnerFunction
  public void testShadowedVariableInnerFunction() {
    inline("var x = 1; print(x) || (function() {  var x; x = 1; print(x)})()",
        "var x; print(1) || (function() {  var x; print(1)})()");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testCatch
  public void testCatch() {
    noInline("var x = 0; try { } catch (x) { }");
    noInline("try { } catch (x) { print(x) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp
  public void testNoInlineGetProp() {
    
    noInline("var x = a.b.c; j.c = 1; print(x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp2
  public void testNoInlineGetProp2() {
    noInline("var x = 1 * a.b.c; j.c = 1; print(x);");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetProp3
  public void testNoInlineGetProp3() {
    
    inline("var x = function(){1 * a.b.c}; print(x);",
           "var x; print(function(){1 * a.b.c});");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineGetEle
  public void testNoInlineGetEle() {
    
    noInline("var x = a[i]; a[j] = 2; print(x); ");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineConstructors
  public void testNoInlineConstructors() {
    noInline("var x = new Iterator(); x.next();");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineArrayLits
  public void testNoInlineArrayLits() {
    noInline("var x = []; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineObjectLits
  public void testNoInlineObjectLits() {
    noInline("var x = {}; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNoInlineRegExpLits
  public void testNoInlineRegExpLits() {
    noInline("var x = /y/; print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineConstructorCallsIntoLoop
  public void testInlineConstructorCallsIntoLoop() {
    
    noInline("var x = new Iterator();" +
             "for(i = 0; i < 10; i++) {j = x.next()}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testRemoveWithLabels
  public void testRemoveWithLabels() {
    inline("var x = 1; L: x = 2; print(x)", "var x = 1; L:{} print(2)");
    inline("var x = 1; L: M: x = 2; print(x)", "var x = 1; L:M:{} print(2)");
    inline("var x = 1; L: M: N: x = 2; print(x)",
           "var x = 1; L:M:N:{} print(2)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect1
  public void testInlineAcrossSideEffect1() {
    
    
    
    
    noInline("var y; var x = noSFX(y); print(x)");
    
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect2
  public void testInlineAcrossSideEffect2() {
    
    
    

    
    noInline("var y; var x = noSFX(y), z = hasSFX(y); print(x)");
    noInline("var y; var x = noSFX(y), z = new hasSFX(y); print(x)");
    noInline("var y; var x = new noSFX(y), z = new hasSFX(y); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect3
  public void testInlineAcrossSideEffect3() {
    
    noInline("var y; var x = noSFX(y); hasSFX(y), print(x)");
    noInline("var y; var x = noSFX(y); new hasSFX(y), print(x)");
    noInline("var y; var x = new noSFX(y); new hasSFX(y), print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineAcrossSideEffect4
  public void testInlineAcrossSideEffect4() {
    
    
    noInline("var y; var x = noSFX(y); hasSFX(y); print(x)");
    noInline("var y; var x = noSFX(y); new hasSFX(y); print(x)");
    noInline("var y; var x = new noSFX(y); new hasSFX(y); print(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testCanInlineAcrossNoSideEffect
  public void testCanInlineAcrossNoSideEffect() {
    
    
    
    noInline(
        "var y; var x = noSFX(y), z = noSFX(); noSFX(); noSFX(), print(x)");
    
    
    
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testDependOnOuterScopeVariables
  public void testDependOnOuterScopeVariables() {
    noInline("var x; function foo() { var y = x; x = 0; print(y) }");
    noInline("var x; function foo() { var y = x; x++; print(y) }");

    
    
    
    noInline("var x; function foo() { var y = x; print(y) }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineIfNameIsLeftSideOfAssign
  public void testInlineIfNameIsLeftSideOfAssign() {
    inline("var x = 1; x = print(x) + 1", "var x; x = print(1) + 1");
    inline("var x = 1; L: x = x + 2", "var x; L: x = 1 + 2");
    inline("var x = 1; x = (x = x + 1)", "var x; x = (x = 1 + 1)");

    noInline("var x = 1; x = (x = (x = 10) + x)");
    noInline("var x = 1; x = (f(x) + (x = 10) + x);");
    noInline("var x = 1; x=-1,foo(x)");
    noInline("var x = 1; x-=1,foo(x)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineArguments
  public void testInlineArguments() {
    testSame("function _func(x) { print(x) }");
    testSame("function _func(x,y) { if(y) { x = 1 }; print(x) }");

    test("function f(x, y) { x = 1; print(x) }",
         "function f(x, y) { print(1) }");

    test("function f(x, y) { if (y) { x = 1; print(x) }}",
         "function f(x, y) { if (y) { print(1) }}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInvalidInlineArguments1
  public void testInvalidInlineArguments1() {
    testSame("function f(x, y) { x = 1; arguments[0] = 2; print(x) }");
    testSame("function f(x, y) { x = 1; var z = arguments;" +
        "z[0] = 2; z[1] = 3; print(x)}");
    testSame("function g(a){a[0]=2} function f(x){x=1;g(arguments);print(x)}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInvalidInlineArguments2
  public void testInvalidInlineArguments2() {
    testSame("function f(c) {var f = c; arguments[0] = this;" +
             "f.apply(this, arguments); return this;}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testForIn
  public void testForIn() {
    noInline("var x; var y = {}; for(x in y){}");
    noInline("var x; var y = {}; var z; for(x in z = y){print(z)}");
    noInline("var x; var y = {}; var z; for(x in y){print(z)}");

  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testNotOkToSkipCheckPathBetweenNodes
  public void testNotOkToSkipCheckPathBetweenNodes() {
    noInline("var x; for(x = 1; foo(x);) {}");
    noInline("var x; for(; x = 1;foo(x)) {}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue698
  public void testIssue698() {
    
    
    
    inline(
        "var x = ''; "
        + "unknown.length < 2 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "unknown.length < 3 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "return x;",
        "var x; "
        + "unknown.length < 2 && (unknown='0' + unknown);"
        + "x = '' + unknown; "
        + "unknown.length < 3 && (unknown='0' + unknown);"
        + "x = x + unknown; "
        + "return x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue777
  public void testIssue777() {
    test(
        "function f(cmd, ta) {" +
        "  var temp = cmd;" +
        "  var temp2 = temp >> 2;" +
        "  cmd = STACKTOP;" +
        "  for (var src = temp2, dest = cmd >> 2, stop = src + 37;" +
        "       src < stop;" +
        "       src++, dest++) {" +
        "    HEAP32[dest] = HEAP32[src];" +
        "  }" +
        "  temp = ta;" +
        "  temp2 = temp >> 2;" +
        "  ta = STACKTOP;" +
        "  STACKTOP += 8;" +
        "  HEAP32[ta >> 2] = HEAP32[temp2];" +
        "  HEAP32[ta + 4 >> 2] = HEAP32[temp2 + 1];" +
        "}",
        "function f(cmd, ta){" +
        "  var temp;" +
        "  var temp2 = cmd >> 2;" +
        "  cmd = STACKTOP;" +
        "  var src = temp2;" +
        "  var dest = cmd >> 2;" +
        "  var stop = src + 37;" +
        "  for(;src<stop;src++,dest++)HEAP32[dest]=HEAP32[src];" +
        "  temp2 = ta >> 2;" +
        "  ta = STACKTOP;" +
        "  STACKTOP += 8;" +
        "  HEAP32[ta>>2] = HEAP32[temp2];" +
        "  HEAP32[ta+4>>2] = HEAP32[temp2+1];" +
        "}");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testTransitiveDependencies1
  public void testTransitiveDependencies1() {
    test(
        "function f(x) { var a = x; var b = a; x = 3; return b; }",
        "function f(x) { var a;     var b = x; x = 3; return b; }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testTransitiveDependencies2
  public void testTransitiveDependencies2() {
    test(
        "function f(x) { var a = x; var b = a; var c = b; x = 3; return c; }",
        "function f(x) { var a    ; var b = x; var c    ; x = 3; return b; }");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue794a
  public void testIssue794a() {
    noInline(
        "var x = 1; " +
        "try { x += someFunction(); } catch (e) {}" +
        "x += 1;" +
        "try { x += someFunction(); } catch (e) {}" +
        "return x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testIssue794b
  public void testIssue794b() {
    noInline(
        "var x = 1; " +
        "try { x = x + someFunction(); } catch (e) {}" +
        "x = x + 1;" +
        "try { x = x + someFunction(); } catch (e) {}" +
        "return x;");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testVarAssinInsideHookIssue965
  public void testVarAssinInsideHookIssue965() {
    noInline("var i = 0; return 1 ? (i = 5) : 0, i;");
    noInline("var i = 0; return (1 ? (i = 5) : 0) ? i : 0;");
    noInline("var i = 0; return (1 ? (i = 5) : 0) || i;");
    noInline("var i = 0; return (1 ? (i = 5) : 0) * i;");
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction1
  public void testIsSimpleFunction1() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction2
  public void testIsSimpleFunction2() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return 0;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction3
  public void testIsSimpleFunction3() {
    assertTrue(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return x ? 0 : 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction4
  public void testIsSimpleFunction4() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction5
  public void testIsSimpleFunction5() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){return 0; return 0;}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction6
  public void testIsSimpleFunction6() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){var x=true;return x ? 0 : 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testIsSimpleFunction7
  public void testIsSimpleFunction7() {
    assertFalse(getInjector().isDirectCallNodeReplacementPossible(
        prep("function f(){if (x) return 0; else return 1}")));
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction1
  public void testCanInlineReferenceToFunction1() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction2
  public void testCanInlineReferenceToFunction2() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction3
  public void testCanInlineReferenceToFunction3() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return;}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction4
  public void testCanInlineReferenceToFunction4() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return;}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction5
  public void testCanInlineReferenceToFunction5() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction6
  public void testCanInlineReferenceToFunction6() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction7
  public void testCanInlineReferenceToFunction7() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x=foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction8
  public void testCanInlineReferenceToFunction8() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x=foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction9
  public void testCanInlineReferenceToFunction9() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=foo();", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction10
  public void testCanInlineReferenceToFunction10() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction11
  public void testCanInlineReferenceToFunction11() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return true;}; var x; x=x+foo();", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction12
  public void testCanInlineReferenceToFunction12() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; var x; x=x+foo();", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction12b
  public void testCanInlineReferenceToFunction12b() {
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_PREPARATION,
        "function foo(){return true;}; var x; x=x+foo();",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction14
  public void testCanInlineReferenceToFunction14() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; foo(x);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction15
  public void testCanInlineReferenceToFunction15() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; foo(x);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction16
  public void testCanInlineReferenceToFunction16() {
    
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){var b;return a;}; foo(goo());", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction17
  public void testCanInlineReferenceToFunction17() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction18
  public void testCanInlineReferenceToFunction18() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a;} foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction19
  public void testCanInlineReferenceToFunction19() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo([]);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction20
  public void testCanInlineReferenceToFunction20() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo({});", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction21
  public void testCanInlineReferenceToFunction21() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(new Date);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction22
  public void testCanInlineReferenceToFunction22() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(true && new Date);", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction23
  public void testCanInlineReferenceToFunction23() {
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction24
  public void testCanInlineReferenceToFunction24() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction25
  public void testCanInlineReferenceToFunction25() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction26
  public void testCanInlineReferenceToFunction26() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction27
  public void testCanInlineReferenceToFunction27() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a+a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }
