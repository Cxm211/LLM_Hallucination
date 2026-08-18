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
      addLeftExpr(first, p, context);
      cc.addOp(opstr, true);

      // For right-hand-side of operations, only pass context if it's
      // the IN_FOR_INIT_CLAUSE one.
      Context rhsContext = getContextForNoInOperator(context);

      // Handle associativity.
      // e.g. if the parse tree is a * (b * c),
      // we can simply generate a * b * c.
      if (last.getType() == type &&
          NodeUtil.isAssociative(type)) {
        addExpr(last, p, rhsContext);
      } else if (NodeUtil.isAssignmentOp(n) && NodeUtil.isAssignmentOp(last)) {
        // Assignments are the only right-associative binary operators
        addExpr(last, p, rhsContext);
      } else {
        addExpr(last, p + 1, rhsContext);
      }
      return;
    }

    cc.startSourceMapping(n);

    switch (type) {
      case Token.TRY: {
        Preconditions.checkState(first.getNext().getType() == Token.BLOCK &&
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
        if (first == null || first.getType() == Token.EMPTY) {
          addIdentifier(n.getString());
        } else {
          Preconditions.checkState(childCount == 1);
          addIdentifier(n.getString());
          cc.addOp("=", true);
          if (first.getType() == Token.COMMA) {
            addExpr(first, NodeUtil.precedence(Token.ASSIGN));
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

      case Token.LP:
        add("(");
        addList(first);
        add(")");
        break;

      case Token.COMMA:
        Preconditions.checkState(childCount == 2);
        addList(first, false, context);
        break;

      case Token.NUMBER:
        Preconditions.checkState(
            childCount ==
            ((n.getParent() != null &&
              n.getParent().getType() == Token.OBJECTLIT) ? 1 : 0));
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
        addExpr(first, NodeUtil.precedence(type));
        break;
      }

      case Token.NEG: {
        Preconditions.checkState(childCount == 1);

        // It's important to our sanity checker that the code
        // we print produces the same AST as the code we parse back.
        // NEG is a weird case because Rhino parses "- -2" as "2".
        if (n.getFirstChild().getType() == Token.NUMBER) {
          cc.addNumber(-n.getFirstChild().getDouble());
        } else {
          cc.addOp(NodeUtil.opToStrNoFail(type), false);
          addExpr(first, NodeUtil.precedence(type));
        }

        break;
      }

      case Token.HOOK: {
        Preconditions.checkState(childCount == 3);
        int p = NodeUtil.precedence(type);
        addLeftExpr(first, p + 1, context);
        cc.addOp("?", true);
        addExpr(first.getNext(), 1);
        cc.addOp(":", true);
        addExpr(last, 1);
        break;
      }

      case Token.REGEXP:
        if (first.getType() != Token.STRING ||
            last.getType() != Token.STRING) {
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

      case Token.GET_REF:
        add(first);
        break;

      case Token.REF_SPECIAL:
        Preconditions.checkState(childCount == 1);
        add(first);
        add(".");
        add((String) n.getProp(Node.NAME_PROP));
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

      case Token.GET:
      case Token.SET:
        Preconditions.checkState(n.getParent().getType() == Token.OBJECTLIT);
        Preconditions.checkState(childCount == 1);
        Preconditions.checkState(first.getType() == Token.FUNCTION);

        // Get methods are unnamed
        Preconditions.checkState(first.getFirstChild().getString().isEmpty());
        if (type == Token.GET) {
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
        if (TokenStream.isJSIdentifier(name) &&
            // do not encode literally any non-literal characters that were
            // unicode escaped.
            NodeUtil.isLatin(name)) {
          add(name);
        } else {
          // Determine if the string is a simple number.
          add(jsString(n.getString(), outputCharsetEncoder));
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
                n.getParent().getType() == Token.SCRIPT);
        for (Node c = first; c != null; c = c.getNext()) {
          add(c, Context.STATEMENT);

          // VAR doesn't include ';' since it gets used in expressions
          if (c.getType() == Token.VAR) {
            cc.endStatement();
          }

          if (c.getType() == Token.FUNCTION) {
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
          if (first.getType() == Token.VAR) {
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
            last.getType() == Token.STRING,
            "Bad GETPROP: RHS should be STRING");
        boolean needsParens = (first.getType() == Token.NUMBER);
        if (needsParens) {
          add("(");
        }
        addLeftExpr(first, NodeUtil.precedence(type), context);
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
        addLeftExpr(first, NodeUtil.precedence(type), context);
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
          addLeftExpr(first, NodeUtil.precedence(type), context);
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
          addExpr(first, NodeUtil.precedence(Token.COMMA));
          add(")");
        } else {
          addLeftExpr(first, NodeUtil.precedence(type), context);
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
      case Token.THIS:
      case Token.FALSE:
      case Token.TRUE:
        Preconditions.checkState(childCount == 0);
        add(Node.tokenToName(type));
        break;

      case Token.CONTINUE:
        Preconditions.checkState(childCount <= 1);
        add("continue");
        if (childCount == 1) {
          if (first.getType() != Token.LABEL_NAME) {
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
          if (first.getType() != Token.LABEL_NAME) {
            throw new Error("Unexpected token type. Should be LABEL_NAME.");
          }
          add(" ");
          add(first);
        }
        cc.endStatement();
        break;

      case Token.EXPR_VOID:
        throw new Error("Unexpected EXPR_VOID. Should be EXPR_RESULT.");

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
        if (NodeUtil.containsType(first, Token.CALL, new MatchNotFunction())) {
          precedence = NodeUtil.precedence(first.getType()) + 1;
        }
        addExpr(first, precedence);

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
              n.getParent().getType() == Token.OBJECTLIT) ? 1 : 0)) {
          throw new IllegalStateException(
              "Unexpected String children: " + n.getParent().toStringTree());
        }
        add(jsString(n.getString(), outputCharsetEncoder));
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

          if (c.getType() == Token.GET || c.getType() == Token.SET) {
            add(c);
          } else {
            // Object literal property names don't have to be quoted if they
            // are not JavaScript keywords
            if (c.getType() == Token.STRING &&
                !c.isQuotedString() &&
                !TokenStream.isKeyword(c.getString()) &&
                TokenStream.isJSIdentifier(c.getString()) &&
                // do not encode literally any non-literal characters that
                // were unicode escaped.
                NodeUtil.isLatin(c.getString())) {
              add(c.getString());
            } else {
              // Determine if the string is a simple number.
              addExpr(c, 1);
            }
            add(":");
            addExpr(c.getFirstChild(), 1);
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

      case Token.DEFAULT:
        Preconditions.checkState(childCount == 1);
        add("default");
        addCaseBody(first);
        break;

      case Token.LABEL:
        Preconditions.checkState(childCount == 2);
        if (first.getType() != Token.LABEL_NAME) {
          throw new Error("Unexpected token type. Should be LABEL_NAME.");
        }
        add(first);
        add(":");
        addNonEmptyStatement(
            last, getContextForNonEmptyExpression(context), true);
        break;

      // This node is auto generated in anonymous functions and should just get
      // ignored for our purposes.
      case Token.SETNAME:
        break;

      default:
        throw new Error("Unknown type " + type + "\n" + n.toStringTree());
    }

    cc.endSourceMapping(n);
  }

  private Node transformNameAsString(Name node) {
    JSDocInfo jsDocInfo = handleJsDoc(node);
    Node irNode = transformDispatcher.processName(node, true);
    if (jsDocInfo != null) {
      irNode.setJSDocInfo(jsDocInfo);
    }
    setSourceInfo(irNode, node);
    return irNode;
  }

    private Node transformAsString(AstNode n) {
      Node ret;
      if (n instanceof Name) {
        ret = transformNameAsString((Name)n);
      } else {
        ret = transform(n);
        Preconditions.checkState(ret.getType() == Token.NUMBER
            || ret.getType() == Token.STRING);
        if (ret.getType() == Token.STRING) {
        ret.putBooleanProp(Node.QUOTED_PROP, true);
        }
      }
      return ret;
    }

    public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()) {
        case Token.GETPROP:
        case Token.GETELEM:
          Node dest = n.getFirstChild().getNext();
          if (dest.getType() == Token.STRING) {
            String s = dest.getString();
            if (s.equals("prototype")) {
              processPrototypeParent(parent, t.getInput());
            } else {
              markPropertyAccessCandidate(dest, t.getInput());
            }
          }
          break;
        case Token.OBJECTLIT:
          if (!prototypeObjLits.contains(n)) {
            // Object literals have their property name/value pairs as a flat
            // list as their children. We want every other node in order to get
            // only the property names.
            for (Node child = n.getFirstChild();
                 child != null;
                 child = child.getNext()) {

              if (child.getType() != Token.NUMBER) {
                markObjLitPropertyCandidate(child, t.getInput());
              }
            }
          }
          break;
      }
    }

    private void processPrototypeParent(Node n, CompilerInput input) {
      switch (n.getType()) {
        // Foo.prototype.getBar = function() { ... }
        case Token.GETPROP:
        case Token.GETELEM:
          Node dest = n.getFirstChild().getNext();
          if (dest.getType() == Token.STRING) {
            markPrototypePropertyCandidate(dest, input);
          }
          break;

        // Foo.prototype = { "getBar" : function() { ... } }
        case Token.ASSIGN:
        case Token.CALL:
          Node map;
          if (n.getType() == Token.ASSIGN) {
            map = n.getFirstChild().getNext();
          } else {
            map = n.getLastChild();
          }
          if (map.getType() == Token.OBJECTLIT) {
            // Remember this node so that we can avoid processing it again when
            // the traversal reaches it.
            prototypeObjLits.add(map);

            for (Node key = map.getFirstChild();
                 key != null; key = key.getNext()) {
              if (key.getType() != Token.NUMBER) {
               // May be STRING, GET, or SET
                markPrototypePropertyCandidate(key, input);
              }
            }
          }
          break;
      }
    }

// relevant test
// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstantsJsDocStyle
  public void testInlineVariablesConstantsJsDocStyle() {
    test("var abc=2; var x = abc;", "var x=2");
    test("var aa = 'aa'; aa;", "'aa'");
    test("var a_a=10; a_a + a_a;", "10+10");
    test("var aa=1;", "");
    test("var aa; aa=1;", "1");
    test("var aa;(function () {aa=1})()", "(function () {1})()");
    test("var aa;(function () {aa=1})(); var z=aa",
         "(function () {1})(); var z=1");
    testSame("var aa;(function () {var y; aa=y})(); var z=aa");

    
    testSame("var aa = '1234567890'; foo(aa); foo(aa); foo(aa);");

    test("var aa = '123456789012345';aa;",
         "'123456789012345'");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant1
  public void testInlineConditionallyDefinedConstant1() {
    
    
    
    
    test("if (x) var ABC = 2; if (y) f(ABC);",
         "if (x); if (y) f(2);");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant2
  public void testInlineConditionallyDefinedConstant2() {
    test("if (x); else var ABC = 2; if (y) f(ABC);",
         "if (x); else; if (y) f(2);");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineConditionallyDefinedConstant3
  public void testInlineConditionallyDefinedConstant3() {
    test("if (x) { var ABC = 2; } if (y) { f(ABC); }",
         "if (x) {} if (y) { f(2); }");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineDefinedConstant
  public void testInlineDefinedConstant() {
    test(
        "\n" +
        "var aa = '1234567890';\n" +
        "foo(aa); foo(aa); foo(aa);",
        "foo('1234567890');foo('1234567890');foo('1234567890')");

    test(
        "\n" +
        "var ABC = '1234567890';\n" +
        "foo(ABC); foo(ABC); foo(ABC);",
        "foo('1234567890');foo('1234567890');foo('1234567890')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testInlineVariablesConstantsWithInlineAllStringsOn
  public void testInlineVariablesConstantsWithInlineAllStringsOn() {
    inlineAllStrings = true;
    test("var AA = '1234567890'; foo(AA); foo(AA); foo(AA);",
         "foo('1234567890'); foo('1234567890'); foo('1234567890')");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineWithoutConstDeclaration
  public void testNoInlineWithoutConstDeclaration() {
    testSame("var abc = 2; var x = abc;");
  }

// com.google.javascript.jscomp.InlineVariablesConstantsTest::testNoInlineAliases
  public void testNoInlineAliases() {
    testSame("var XXX = new Foo(); var yyy = XXX; bar(yyy)");
    testSame("var xxx = new Foo(); var YYY = xxx; bar(YYY)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineGlobal
  public void testInlineGlobal() {
    test("var x = 1; var z = x;", "var z = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineExportedName
  public void testNoInlineExportedName() {
    testSame("var _x = 1; var z = _x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineExportedName2
  public void testNoInlineExportedName2() {
    testSame("var f = function() {}; var _x = f;" +
             "var y = function() { _x(); }; var _y = f;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineIncrement
  public void testDoNotInlineIncrement() {
    testSame("var x = 1; x++;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineDecrement
  public void testDoNotInlineDecrement() {
    testSame("var x = 1; x--;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotInlineIntoLhsOfAssign
  public void testDoNotInlineIntoLhsOfAssign() {
    testSame("var x = 1; x += 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoRhsOfAssign
  public void testInlineIntoRhsOfAssign() {
    test("var x = 1; var y = x;", "var y = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction
  public void testInlineInFunction() {
    test("function baz() { var x = 1; var z = x; }",
        "function baz() { var z = 1; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction2
  public void testInlineInFunction2() {
    test("function baz() { " +
            "var a = new obj();"+
            "result = a;" +
         "}",
         "function baz() { " +
            "result = new obj()" +
         "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction3
  public void testInlineInFunction3() {
    testSame(
        "function baz() { " +
           "var a = new obj();" +
           "(function(){a;})();" +
           "result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction4
  public void testInlineInFunction4() {
    testSame(
        "function baz() { " +
           "var a = new obj();" +
           "foo.result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineInFunction5
  public void testInlineInFunction5() {
    testSame(
        "function baz() { " +
           "var a = (foo = new obj());" +
           "foo.x();" +
           "result = a;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAcrossModules
  public void testInlineAcrossModules() {
    
    test(createModules("var a = 2;", "var b = a;"),
        new String[] { "", "var b = 2;" });
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional1
  public void testDoNotExitConditional1() {
    testSame("if (true) { var x = 1; } var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional2
  public void testDoNotExitConditional2() {
    testSame("if (true) var x = 1; var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitConditional3
  public void testDoNotExitConditional3() {
    testSame("var x; if (true) x=1; var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitLoop
  public void testDoNotExitLoop() {
    testSame("while (z) { var x = 3; } var y = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitForLoop
  public void testDoNotExitForLoop() {
    test("for (var i = 1; false; false) var z = i;",
         "for (;false;false) var z = 1;");
    testSame("for (; false; false) var i = 1; var z = i;");
    testSame("for (var i in {}); var z = i;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterSubscope
  public void testDoNotEnterSubscope() {
    testSame(
        "var x = function() {" +
        "  var self = this; " +
        "  return function() { var y = self; };" +
        "}");
    testSame(
        "var x = function() {" +
        "  var y = [1]; " +
        "  return function() { var z = y; };" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotExitTry
  public void testDoNotExitTry() {
    testSame("try { var x = y; } catch (e) {} var z = y; ");
    testSame("try { throw e; var x = 1; } catch (e) {} var z = x; ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterCatch
  public void testDoNotEnterCatch() {
    testSame("try { } catch (e) { var z = e; } ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotEnterFinally
  public void testDoNotEnterFinally() {
    testSame("try { throw e; var x = 1; } catch (e) {} " +
             "finally  { var z = x; } ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideIfConditional
  public void testInsideIfConditional() {
    test("var a = foo(); if (a) { alert(3); }", "if (foo()) { alert(3); }");
    test("var a; a = foo(); if (a) { alert(3); }", "if (foo()) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testOnlyReadAtInitialization
  public void testOnlyReadAtInitialization() {
    test("var a; a = foo();", "foo();");
    test("var a; if (a = foo()) { alert(3); }", "if (foo()) { alert(3); }");
    test("var a; switch (a = foo()) {}", "switch(foo()) {}");
    test("var a; function f(){ return a = foo(); }",
         "function f(){ return foo(); }");
    test("function f(){ var a; return a = foo(); }",
         "function f(){ return foo(); }");
    test("var a; with (a = foo()) { alert(3); }", "with (foo()) { alert(3); }");

    test("var a; b = (a = foo());", "b = foo();");
    test("var a; while(a = foo()) { alert(3); }",
         "while(foo()) { alert(3); }");
    test("var a; for(;a = foo();) { alert(3); }",
         "for(;foo();) { alert(3); }");
    test("var a; do {} while(a = foo()) { alert(3); }",
         "do {} while(foo()) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testImmutableWithSingleReferenceAfterInitialzation
  public void testImmutableWithSingleReferenceAfterInitialzation() {
    test("var a; a = 1;", "1;");
    test("var a; if (a = 1) { alert(3); }", "if (1) { alert(3); }");
    test("var a; switch (a = 1) {}", "switch(1) {}");
    test("var a; function f(){ return a = 1; }",
         "function f(){ return 1; }");
    test("function f(){ var a; return a = 1; }",
         "function f(){ return 1; }");
    test("var a; with (a = 1) { alert(3); }", "with (1) { alert(3); }");

    test("var a; b = (a = 1);", "b = 1;");
    test("var a; while(a = 1) { alert(3); }",
         "while(1) { alert(3); }");
    test("var a; for(;a = 1;) { alert(3); }",
         "for(;1;) { alert(3); }");
    test("var a; do {} while(a = 1) { alert(3); }",
         "do {} while(1) { alert(3); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testSingleReferenceAfterInitialzation
  public void testSingleReferenceAfterInitialzation() {
    test("var a; a = foo();a;", "foo();");
    testSame("var a; if (a = foo()) { alert(3); } a;");
    testSame("var a; switch (a = foo()) {} a;");
    testSame("var a; function f(){ return a = foo(); } a;");
    testSame("function f(){ var a; return a = foo(); a;}");
    testSame("var a; with (a = foo()) { alert(3); } a;");
    testSame("var a; b = (a = foo()); a;");
    testSame("var a; while(a = foo()) { alert(3); } a;");
    testSame("var a; for(;a = foo();) { alert(3); } a;");
    testSame("var a; do {} while(a = foo()) { alert(3); } a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideIfBranch
  public void testInsideIfBranch() {
    testSame("var a = foo(); if (1) { alert(a); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideAndConditional
  public void testInsideAndConditional() {
    test("var a = foo(); a && alert(3);", "foo() && alert(3);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideAndBranch
  public void testInsideAndBranch() {
    testSame("var a = foo(); 1 && alert(a);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideOrBranch
  public void testInsideOrBranch() {
    testSame("var a = foo(); 1 || alert(a);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideHookBranch
  public void testInsideHookBranch() {
    testSame("var a = foo(); 1 ? alert(a) : alert(3)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideHookConditional
  public void testInsideHookConditional() {
    test("var a = foo(); a ? alert(1) : alert(3)",
         "foo() ? alert(1) : alert(3)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideOrBranchInsideIfConditional
  public void testInsideOrBranchInsideIfConditional() {
    testSame("var a = foo(); if (x || a) {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInsideOrBranchInsideIfConditionalWithConstant
  public void testInsideOrBranchInsideIfConditionalWithConstant() {
    
    testSame("var a = [false]; if (x || a) {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testCrossFunctionsAsLeftLeaves
  public void testCrossFunctionsAsLeftLeaves() {
    
    test(
        new String[] { "var x = function() {};", "",
            "function cow() {} var z = x;"},
        new String[] { "", "", "function cow() {} var z = function() {};" });
    test(
        new String[] { "var x = function() {};", "",
            "var cow = function() {}; var z = x;"},
        new String[] { "", "",
            "var cow = function() {}; var z = function() {};" });
    testSame(
        new String[] { "var x = a;", "",
            "(function() { a++; })(); var z = x;"});
    test(
        new String[] { "var x = a;", "",
            "function cow() { a++; }; cow(); var z = x;"},
        new String[] { "var x = a;", "",
            ";(function cow(){ a++; })(); var z = x;"});
    testSame(
        new String[] { "var x = a;", "",
            "cow(); var z = x; function cow() { a++; };"});
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoCrossFunction
  public void testDoCrossFunction() {
    
    
    test("var x = 1; foo(); var z = x;", "foo(); var z = 1;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossReferencingFunction
  public void testDoNotCrossReferencingFunction() {
    testSame(
        "var f = function() { var z = x; };" +
        "var x = 1;" +
        "f();" +
        "var z = x;" +
        "f();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testChainedAssignment
  public void testChainedAssignment() {
    test("var a = 2, b = 2; var c = b;", "var a = 2; var c = 2;");
    test("var a = 2, b = 2; var c = a;", "var b = 2; var c = 2;");
    test("var a = b = 2; var f = 3; var c = a;", "var f = 3; var c = b = 2;");
    testSame("var a = b = 2; var c = b;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testForIn
  public void testForIn() {
    testSame("for (var i in j) { var c = i; }");
    testSame("var i = 0; for (i in j) ;");
    testSame("var i = 0; for (i in j) { var c = i; }");
    testSame("i = 0; for (var i in j) { var c = i; }");
    testSame("var j = {'key':'value'}; for (var i in j) {print(i)};");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoCrossNewVariables
  public void testDoCrossNewVariables() {
    test("var x = foo(); var z = x;", "var z = foo();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossFunctionCalls
  public void testDoNotCrossFunctionCalls() {
    testSame("var x = foo(); bar(); var z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossAssignment
  public void testDoNotCrossAssignment() {
    testSame("var x = {}; var y = x.a; x.a = 1; var z = y;");
    testSame("var a = this.id; foo(this.id = 3, a);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossDelete
  public void testDoNotCrossDelete() {
    testSame("var x = {}; var y = x.a; delete x.a; var z = y;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossAssignmentPlus
  public void testDoNotCrossAssignmentPlus() {
    testSame("var a = b; b += 2; var c = a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossIncrement
  public void testDoNotCrossIncrement() {
    testSame("var a = b.c; b.c++; var d = a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoNotCrossConstructor
  public void testDoNotCrossConstructor() {
    testSame("var a = b; new Foo(); var c = a;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testDoCrossVar
  public void testDoCrossVar() {
    
    test("var a = b; var b = 3; alert(a)", "alert(3);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testOverlappingInlines
  public void testOverlappingInlines() {
    String source =
        "a = function(el, x, opt_y) { " +
        "  var cur = bar(el); " +
        "  opt_y = x.y; " +
        "  x = x.x; " +
        "  var dx = x - cur.x; " +
        "  var dy = opt_y - cur.y;" +
        "  foo(el, el.offsetLeft + dx, el.offsetTop + dy); " +
        "};";
    String expected =
      "a = function(el, x, opt_y) { " +
      "  var cur = bar(el); " +
      "  opt_y = x.y; " +
      "  x = x.x; " +
      "  foo(el, el.offsetLeft + (x - cur.x)," +
      "      el.offsetTop + (opt_y - cur.y)); " +
      "};";

    test(source, expected);
  }

// com.google.javascript.jscomp.InlineVariablesTest::testOverlappingInlineFunctions
  public void testOverlappingInlineFunctions() {
    String source =
        "a = function() { " +
        "  var b = function(args) {var n;}; " +
        "  var c = function(args) {}; " +
        "  d(b,c); " +
        "};";
    String expected =
      "a = function() { " +
      "  d(function(args){var n;}, function(args){}); " +
      "};";

    test(source, expected);
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoLoops
  public void testInlineIntoLoops() {
    test("var x = true; while (true) alert(x);",
         "while (true) alert(true);");
    test("var x = true; while (true) for (var i in {}) alert(x);",
         "while (true) for (var i in {}) alert(true);");
    testSame("var x = [true]; while (true) alert(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoFunction
  public void testInlineIntoFunction() {
    test("var x = false; var f = function() { alert(x); };",
         "var f = function() { alert(false); };");
    testSame("var x = [false]; var f = function() { alert(x); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineIntoNamedFunction
  public void testNoInlineIntoNamedFunction() {
    testSame("f(); var x = false; function f() { alert(x); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoNestedNonHoistedNamedFunctions
  public void testInlineIntoNestedNonHoistedNamedFunctions() {
    test("f(); var x = false; if (false) function f() { alert(x); };",
         "f(); if (false) function f() { alert(false); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineIntoNestedNamedFunctions
  public void testNoInlineIntoNestedNamedFunctions() {
    testSame("f(); var x = false; function f() { if (false) { alert(x); } };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineMutatedVariable
  public void testNoInlineMutatedVariable() {
    testSame("var x = false; if (true) { var y = x; x = true; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineImmutableMultipleTimes
  public void testInlineImmutableMultipleTimes() {
    test("var x = null; var y = x, z = x;",
         "var y = null, z = null;");
    test("var x = 3; var y = x, z = x;",
         "var y = 3, z = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineStringMultipleTimesIfNotWorthwhile
  public void testNoInlineStringMultipleTimesIfNotWorthwhile() {
    testSame("var x = 'abcdefghijklmnopqrstuvwxyz'; var y = x, z = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineStringMultipleTimesWhenAliasingAllStrings
  public void testInlineStringMultipleTimesWhenAliasingAllStrings() {
    inlineAllStrings = true;
    test("var x = 'abcdefghijklmnopqrstuvwxyz'; var y = x, z = x;",
         "var y = 'abcdefghijklmnopqrstuvwxyz', " +
         "    z = 'abcdefghijklmnopqrstuvwxyz';");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineBackwards
  public void testNoInlineBackwards() {
    testSame("var y = x; var x = null;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineOutOfBranch
  public void testNoInlineOutOfBranch() {
    testSame("if (true) var x = null; var y = x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInterferingInlines
  public void testInterferingInlines() {
    test("var a = 3; var f = function() { var x = a; alert(x); };",
         "var f = function() { alert(3); };");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineIntoTryCatch
  public void testInlineIntoTryCatch() {
    test("var a = true; " +
         "try { var b = a; } " +
         "catch (e) { var c = a + b; var d = true; } " +
         "finally { var f = a + b + c + d; }",
         "try { var b = true; } " +
         "catch (e) { var c = true + b; var d = true; } " +
         "finally { var f = true + b + c + d; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstants
  public void testInlineConstants() {
    test("function foo() { return XXX; } var XXX = true;",
         "function foo() { return true; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineStringWhenWorthwhile
  public void testInlineStringWhenWorthwhile() {
    test("var x = 'a'; foo(x, x, x);", "foo('a', 'a', 'a');");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstantAlias
  public void testInlineConstantAlias() {
    test("var XXX = new Foo(); q(XXX); var YYY = XXX; bar(YYY)",
         "var XXX = new Foo(); q(XXX); bar(XXX)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstantAliasWithAnnotation
  public void testInlineConstantAliasWithAnnotation() {
    test(" var xxx = new Foo(); q(xxx); var YYY = xxx; bar(YYY)",
         " var xxx = new Foo(); q(xxx); bar(xxx)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineConstantAliasWithNonConstant
  public void testInlineConstantAliasWithNonConstant() {
    test("var XXX = new Foo(); q(XXX); var y = XXX; bar(y); baz(y)",
         "var XXX = new Foo(); q(XXX); bar(XXX); baz(XXX)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testCascadingInlines
  public void testCascadingInlines() {
    test("var XXX = 4; " +
         "function f() { var YYY = XXX; bar(YYY); baz(YYY); }",
         "function f() { bar(4); baz(4); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineGetpropIntoCall
  public void testNoInlineGetpropIntoCall() {
    test("var a = b; a();", "b();");
    test("var a = b.c; f(a);", "f(b.c);");
    testSame("var a = b.c; a();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionDeclaration
  public void testInlineFunctionDeclaration() {
    test("var f = function () {}; var a = f;",
         "var a = function () {};");
    test("var f = function () {}; foo(); var a = f;",
         "foo(); var a = function () {};");
    test("var f = function () {}; foo(f);",
         "foo(function () {});");

    testSame("var f = function () {}; function g() {var a = f;}");
    testSame("var f = function () {}; function g() {h(f);}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::test2388531
  public void test2388531() {
    testSame("var f = function () {};" +
             "var g = function () {};" +
             "goog.inherits(f, g);");
    testSame("var f = function () {};" +
             "var g = function () {};" +
             "goog$inherits(f, g);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testRecursiveFunction1
  public void testRecursiveFunction1() {
    testSame("var x = 0; (function x() { return x ? x() : 3; })();");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testRecursiveFunction2
  public void testRecursiveFunction2() {
    testSame("function y() { return y(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testUnreferencedBleedingFunction
  public void testUnreferencedBleedingFunction() {
    testSame("var x = function y() {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testReferencedBleedingFunction
  public void testReferencedBleedingFunction() {
    testSame("var x = function y() { return y(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1
  public void testInlineAliases1() {
    test("var x = this.foo(); this.bar(); var y = x; this.baz(y);",
         "var x = this.foo(); this.bar(); this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1b
  public void testInlineAliases1b() {
    test("var x = this.foo(); this.bar(); var y; y = x; this.baz(y);",
         "var x = this.foo(); this.bar(); x; this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1c
  public void testInlineAliases1c() {
    test("var x; x = this.foo(); this.bar(); var y = x; this.baz(y);",
         "var x; x = this.foo(); this.bar(); this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases1d
  public void testInlineAliases1d() {
    test("var x; x = this.foo(); this.bar(); var y; y = x; this.baz(y);",
         "var x; x = this.foo(); this.bar(); x; this.baz(x);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2
  public void testInlineAliases2() {
    test("var x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.baz(y); }",
         "var x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2b
  public void testInlineAliases2b() {
    test("var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.baz(y); }",
         "var x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2c
  public void testInlineAliases2c() {
    test("var x; x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.baz(y); }",
         "var x; x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliases2d
  public void testInlineAliases2d() {
    test("var x; x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.baz(y); }",
         "var x; x = this.foo(); this.bar(); function f() { this.baz(x); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineAliasesInLoop
  public void testInlineAliasesInLoop() {
    test(
        "function f() { " +
        "  var x = extern();" +
        "  for (var i = 0; i < 5; i++) {" +
        "    (function() {" +
        "       var y = x; window.setTimeout(function() { extern(y); }, 0);" +
        "     })();" +
        "  }" +
        "}",
        "function f() { " +
        "  var x = extern();" +
        "  for (var i = 0; i < 5; i++) {" +
        "    (function() {" +
        "       window.setTimeout(function() { extern(x); }, 0);" +
        "     })();" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliasesInLoop
  public void testNoInlineAliasesInLoop() {
    testSame(
        "function f() { " +
        "  for (var i = 0; i < 5; i++) {" +
        "    var x = extern();" +
        "    (function() {" +
        "       var y = x; window.setTimeout(function() { extern(y); }, 0);" +
        "     })();" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases1
  public void testNoInlineAliases1() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; x = 3; this.baz(y);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases1b
  public void testNoInlineAliases1b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; x = 3; this.baz(y);");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases2
  public void testNoInlineAliases2() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; y = 3; this.baz(y); ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases2b
  public void testNoInlineAliases2b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; y = 3; this.baz(y); ");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases3
  public void testNoInlineAliases3() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; g(); this.baz(y); } " +
         "function g() { x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases3b
  public void testNoInlineAliases3b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; g(); this.baz(y); } " +
         "function g() { x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases4
  public void testNoInlineAliases4() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; y = 3; this.baz(y); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases4b
  public void testNoInlineAliases4b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; y = 3; this.baz(y); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases5
  public void testNoInlineAliases5() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; this.bing();" +
        "this.baz(y); x = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases5b
  public void testNoInlineAliases5b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; this.bing();" +
        "this.baz(y); x = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases6
  public void testNoInlineAliases6() {
    testSame(
        "var x = this.foo(); this.bar(); var y = x; this.bing();" +
        "this.baz(y); y = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases6b
  public void testNoInlineAliases6b() {
    testSame(
        "var x = this.foo(); this.bar(); var y; y = x; this.bing();" +
        "this.baz(y); y = 3;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases7
  public void testNoInlineAliases7() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.bing(); this.baz(y); x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases7b
  public void testNoInlineAliases7b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.bing(); this.baz(y); x = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases8
  public void testNoInlineAliases8() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y = x; this.baz(y); y = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testNoInlineAliases8b
  public void testNoInlineAliases8b() {
    testSame(
         "var x = this.foo(); this.bar(); " +
         "function f() { var y; y = x; this.baz(y); y = 3; }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testSideEffectOrder
  public void testSideEffectOrder() {
    
    String EXTERNS = "var z; function f(){}";
    test(EXTERNS,
         "var x = f(y.a, y); z = x;",
         "z = f(y.a, y);", null, null);
    
    testSame(EXTERNS, "var x = f(y.a, y); z.b = x;", null, null);
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineParameterAlias1
  public void testInlineParameterAlias1() {
    test(
      "function f(x) {" +
      "  var y = x;" +
      "  g();" +
      "  y;y;" +
      "}",
      "function f(x) {" +
      "  g();" +
      "  x;x;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineParameterAlias2
  public void testInlineParameterAlias2() {
    test(
      "function f(x) {" +
      "  var y; y = x;" +
      "  g();" +
      "  y;y;" +
      "}",
      "function f(x) {" +
      "  x;" +
      "  g();" +
      "  x;x;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias1a
  public void testInlineFunctionAlias1a() {
    test(
      "function f(x) {}" +
      "var y = f;" +
      "g();" +
      "y();y();",
      "var y = function f(x) {};" +
      "g();" +
      "y();y();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias1b
  public void testInlineFunctionAlias1b() {
    test(
      "function f(x) {};" +
      "f;var y = f;" +
      "g();" +
      "y();y();",
      "function f(x) {};" +
      "f;g();" +
      "f();f();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias2a
  public void testInlineFunctionAlias2a() {
    test(
      "function f(x) {}" +
      "var y; y = f;" +
      "g();" +
      "y();y();",
      "var y; y = function f(x) {};" +
      "g();" +
      "y();y();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineFunctionAlias2b
  public void testInlineFunctionAlias2b() {
    test(
      "function f(x) {};" +
      "f; var y; y = f;" +
      "g();" +
      "y();y();",
      "function f(x) {};" +
      "f; f;" +
      "g();" +
      "f();f();"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineCatchAlias1
  public void testInlineCatchAlias1() {
    test(
      "try {" +
      "} catch (e) {" +
      "  var y = e;" +
      "  g();" +
      "  y;y;" +
      "}",
      "try {" +
      "} catch (e) {" +
      "  g();" +
      "  e;e;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineCatchAlias2
  public void testInlineCatchAlias2() {
    test(
      "try {" +
      "} catch (e) {" +
      "  var y; y = e;" +
      "  g();" +
      "  y;y;" +
      "}",
      "try {" +
      "} catch (e) {" +
      "  e;" +
      "  g();" +
      "  e;e;" +
      "}"
      );
  }

// com.google.javascript.jscomp.InlineVariablesTest::testLocalsOnly1
  public void testLocalsOnly1() {
    inlineLocalsOnly = true;
    test(
        "var x=1; x; function f() {var x = 1; x;}",
        "var x=1; x; function f() {1;}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testLocalsOnly2
  public void testLocalsOnly2() {
    inlineLocalsOnly = true;
    test(
        "\n" +
        "var X=1; X;\n" +
        "function f() {\n" +
        "  \n" +
        "  var X = 1; X;\n" +
        "}",
        "var X=1; X; function f() {1;}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined1
  public void testInlineUndefined1() {
    test("var x; x;",
         "void 0;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined2
  public void testInlineUndefined2() {
    testSame("var x; x++;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined3
  public void testInlineUndefined3() {
    testSame("var x; var x;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined4
  public void testInlineUndefined4() {
    test("var x; x; x;",
         "void 0; void 0;");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineUndefined5
  public void testInlineUndefined5() {
    test("var x; for(x in a) {}",
         "var x; for(x in a) {}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue90
  public void testIssue90() {
    test("var x; x && alert(1)",
         "void 0 && alert(1)");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testRenamePropertyFunction
  public void testRenamePropertyFunction() {
    testSame("var JSCompiler_renameProperty; " +
             "JSCompiler_renameProperty('foo')");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testThisAlias
  public void testThisAlias() {
    test("function f() { var a = this; a.y(); a.z(); }",
         "function f() { this.y(); this.z(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testThisEscapedAlias
  public void testThisEscapedAlias() {
    testSame(
        "function f() { var a = this; var g = function() { a.y(); }; a.z(); }");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testInlineNamedFunction
  public void testInlineNamedFunction() {
    test("function f() {} f();", "(function f(){})()");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ModifiedArguments1
  public void testIssue378ModifiedArguments1() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  arguments[0] = this;\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ModifiedArguments2
  public void testIssue378ModifiedArguments2() {
    testSame(
        "function g(callback) {\n" +
        "  \n" +
        "  var f = callback;\n" +
        "  arguments[0] = this;\n" +
        "  f.apply(this, arguments);\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments1
  public void testIssue378EscapedArguments1() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  h(arguments,this);\n" +
        "  f.apply(this, arguments);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments2
  public void testIssue378EscapedArguments2() {
    testSame(
        "function g(callback) {\n" +
        "  \n" +
        "  var f = callback;\n" +
        "  h(arguments,this);\n" +
        "  f.apply(this);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments3
  public void testIssue378EscapedArguments3() {
    test(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  f.apply(this, arguments);\n" +
        "}\n",
        "function g(callback) {\n" +
        "  callback.apply(this, arguments);\n" +
        "}\n");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378EscapedArguments4
  public void testIssue378EscapedArguments4() {
    testSame(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  h(arguments[0],this);\n" +
        "  f.apply(this, arguments);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ArgumentsRead1
  public void testIssue378ArgumentsRead1() {
    test(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  var g = arguments[0];\n" +
        "  f.apply(this, arguments);\n" +
        "}",
        "function g(callback) {\n" +
        "  var g = arguments[0];\n" +
        "  callback.apply(this, arguments);\n" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testIssue378ArgumentsRead2
  public void testIssue378ArgumentsRead2() {
    test(
        "function g(callback) {\n" +
        "  var f = callback;\n" +
        "  h(arguments[0],this);\n" +
        "  f.apply(this, arguments[0]);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}",
        "function g(callback) {\n" +
        "  h(arguments[0],this);\n" +
        "  callback.apply(this, arguments[0]);\n" +
        "}\n" +
        "function h(a,b) {\n" +
        "  a[0] = b;" +
        "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testArgumentsModifiedInOuterFunction
  public void testArgumentsModifiedInOuterFunction() {
    test(
      "function g(callback) {\n" +
      "  var f = callback;\n" +
      "  arguments[0] = this;\n" +
      "  f.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    var x = callback;\n" +
      "    x.apply(this);\n" +
      "  }" +
      "}",
      "function g(callback) {\n" +
      "  var f = callback;\n" +
      "  arguments[0] = this;\n" +
      "  f.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    callback.apply(this);\n" +
      "  }" +
      "}");
  }

// com.google.javascript.jscomp.InlineVariablesTest::testArgumentsModifiedInInnerFunction
  public void testArgumentsModifiedInInnerFunction() {
    test(
      "function g(callback) {\n" +
      "  var f = callback;\n" +
      "  f.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    var x = callback;\n" +
      "    arguments[0] = this;\n" +
      "    x.apply(this);\n" +
      "  }" +
      "}",
      "function g(callback) {\n" +
      "  callback.apply(this, arguments);\n" +
      "  function inner(callback) {" +
      "    var x = callback;\n" +
      "    arguments[0] = this;\n" +
      "    x.apply(this);\n" +
      "  }" +
      "}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testInstrument
  public void testInstrument() {
    final String kPreamble =
        "var $$toRemoveDefinition1, $$notToRemove;\n" +
        "var $$toRemoveDefinition2, $$toRemoveDefinition3;\n";

    
    
    List<String> initCodeList = ImmutableList.of(
        "var $$Table = [];",
        "function $$TestDefine(id) {",
        "  $$Table[id] = 0;",
        "};",
        "function $$TestInstrument(id) {",
        "  $$Table[id]++;",
        "};");
    StringBuilder initCodeBuilder = new StringBuilder();
    StringBuilder pbBuilder = new StringBuilder();
    for (String line : initCodeList) {
      initCodeBuilder.append(line).append("\n");
      pbBuilder.append("init: \"").append(line).append("\"\n");
    }

    pbBuilder.append("report_call: \"$$testInstrument\"")
        .append("report_defined: \"$$testDefine\"")
        .append("declaration_to_remove: \"$$toRemoveDefinition1\"")
        .append("declaration_to_remove: \"$$toRemoveDefinition2\"")
        .append("declaration_to_remove: \"$$toRemoveDefinition3\"");

    final String initCode = initCodeBuilder.toString();
    this.instrumentationPb = pbBuilder.toString();

    
    test("function a(){b}",
         initCode + "$$testDefine(0);" +
         "function a(){$$testInstrument(0);b}");

    
    test(kPreamble + "function a(){b}",
         initCode +
         "$$testDefine(0);" +
         "var $$notToRemove;" +
         "function a(){$$testInstrument(0);b}");

    
    test(kPreamble + "var a = { b: function(){c} }",
         initCode +
         "var $$notToRemove;" +
         "$$testDefine(0);" +
         "var a = { b: function(){$$testInstrument(0);c} }");

    
    test(kPreamble +
         "var a = { b: function(){c}, d: function(){e} }",
         initCode +
         "var $$notToRemove;" +
         "$$testDefine(0);" +
         "$$testDefine(1);" +
         "var a={b:function(){$$testInstrument(0);c}," +
         "d:function(){$$testInstrument(1);e}}");

    
    test(kPreamble +
         "var a = { b: { f: function(){c} }, d: function(){e} }",
         initCode +
         "var $$notToRemove;" +
         "$$testDefine(0);" +
         "$$testDefine(1);" +
         "var a={b:{f:function(){$$testInstrument(0);c}}," +
         "d:function(){$$testInstrument(1);e}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testEmpty
  public void testEmpty() {
    this.instrumentationPb = "";
    test("function a(){b}", "function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testAppNameSetter
  public void testAppNameSetter() {
    this.instrumentationPb = "app_name_setter: \"setAppName\"";
    test("function a(){b}", "setAppName(\"testfile.js\");function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testInit
  public void testInit() {
    this.instrumentationPb = "init: \"var foo = 0;\"\n" +
        "init: \"function f(){g();}\"\n";
    test("function a(){b}",
         "var foo = 0;function f(){g()}function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testDeclare
  public void testDeclare() {
    this.instrumentationPb = "report_defined: \"$$testDefine\"";
    test("function a(){b}", "$$testDefine(0);function a(){b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testCall
  public void testCall() {
    this.instrumentationPb = "report_call: \"$$testCall\"";
    test("function a(){b}", "function a(){$$testCall(0);b}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testNested
  public void testNested() {
    this.instrumentationPb = "report_call: \"$$testCall\"\n" +
        "report_defined: \"$$testDefine\"";
    test("function a(){ function b(){}}",
         "$$testDefine(1);$$testDefine(0);" +
         "function a(){$$testCall(1);function b(){$$testCall(0)}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testExitPaths
  public void testExitPaths() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){return}",
         "function a(){return $$testExit(0)}");

    test("function b(){return 5}",
         "function b(){return $$testExit(0, 5)}");

    test("function a(){if(2 != 3){return}else{return 5}}",
         "function a(){if(2!=3){return $$testExit(0)}" +
         "else{return $$testExit(0,5)}}");

    test("function a(){if(2 != 3){return}else{return 5}}b()",
         "function a(){if(2!=3){return $$testExit(0)}" +
         "else{return $$testExit(0,5)}}b()");

    test("function a(){if(2 != 3){return}else{return 5}}",
         "function a(){if(2!=3){return $$testExit(0)}" +
         "else{return $$testExit(0,5)}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testExitNoReturn
  public void testExitNoReturn() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){}",
         "function a(){$$testExit(0);}");

    test("function a(){b()}",
         "function a(){b();$$testExit(0);}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testPartialExitPaths
  public void testPartialExitPaths() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){if (2 != 3) {return}}",
         "function a(){if (2 != 3){return $$testExit(0)}$$testExit(0)}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testExitTry
  public void testExitTry() {
    this.instrumentationPb = "report_exit: \"$$testExit\"";
    test("function a(){try{return}catch(err){}}",
         "function a(){try{return $$testExit(0)}catch(err){}$$testExit(0)}");

    test("function a(){try{}catch(err){return}}",
         "function a(){try{}catch(err){return $$testExit(0)}$$testExit(0)}");

    test("function a(){try{return}finally{}}",
         "function a(){try{return $$testExit(0)}finally{}$$testExit(0)}");

    test("function a(){try{return}catch(err){}finally{}}",
         "function a(){try{return $$testExit(0)}catch(err){}finally{}" +
         "$$testExit(0)}");

    test("function a(){try{return 1}catch(err){return 2}}",
         "function a(){try{return $$testExit(0, 1)}" +
         "catch(err){return $$testExit(0,2)}}");

    test("function a(){try{return 1}catch(err){return 2}finally{}}",
         "function a(){try{return $$testExit(0, 1)}" +
         "catch(err){return $$testExit(0,2)}" +
         "finally{}$$testExit(0)}");

    test("function a(){try{return 1}catch(err){return 2}finally{return}}",
         "function a(){try{return $$testExit(0, 1)}" +
         "catch(err){return $$testExit(0,2)}finally{return $$testExit(0)}}");

    test("function a(){try{}catch(err){}finally{return}}",
         "function a(){try{}catch(err){}finally{return $$testExit(0)}}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testNestedExit
  public void testNestedExit() {
    this.instrumentationPb = "report_exit: \"$$testExit\"\n" +
        "report_defined: \"$$testDefine\"";
    test("function a(){ return function(){ return c;}}",
         "$$testDefine(1);function a(){$$testDefine(0);" +
         "return $$testExit(1, function(){return $$testExit(0, c);});}");
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testProtobuffParseFail
  public void testProtobuffParseFail() {
    this.instrumentationPb = "not an ascii pb\n";
    test("function a(){b}", "", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.InstrumentFunctionsTest::testInitJsParseFail
  public void testInitJsParseFail() {
    this.instrumentationPb = "init: \"= assignWithNoLhs();\"";
    test("function a(){b}", "", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testSyntaxError1
  public void testSyntaxError1() {
    try {
      extractMessage("if (true) {}}");
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertEquals("JSCompiler errors\n" +
          "testcode:1: ERROR - Parse error. syntax error\n", e.getMessage());
    }
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testSyntaxError2
  public void testSyntaxError2() {
    try {
      extractMessage("", "if (true) {}}");
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertEquals("JSCompiler errors\n" +
          "testcode:2: ERROR - Parse error. syntax error\n", e.getMessage());
    }
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractNewStyleMessage1
  public void testExtractNewStyleMessage1() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .build(),
        extractMessage("var MSG_SILLY = goog.getMsg('silly test message');"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractNewStyleMessage2
  public void testExtractNewStyleMessage2() {
    
    assertEquals(
        new JsMessage.Builder("MSG_WELCOME")
            .appendStringPart("Hi ")
            .appendPlaceholderReference("userName")
            .appendStringPart("! Welcome to ")
            .appendPlaceholderReference("product")
            .appendStringPart(".")
            .setDesc("The welcome message.")
            .setIsHidden(true)
            .build(),
        extractMessage(
            "",
            "var MSG_WELCOME = goog.getMsg(",
            "    'Hi {$userName}! Welcome to {$product}.',",
            "    {userName: someUserName, product: getProductName()});"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage1
  public void testExtractOldStyleMessage1() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .setDesc("Description.")
            .build(),
        extractMessage(
            "var MSG_SILLY_HELP = 'Description.';",
            "var MSG_SILLY = 'silly test message';"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage2
  public void testExtractOldStyleMessage2() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .setDesc("Description.")
            .build(),
        extractMessage(
            "var MSG_SILLY = 'silly test message';",
            "var MSG_SILLY_HELP = 'Descrip' + 'tion.';"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage3
  public void testExtractOldStyleMessage3() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendPlaceholderReference("one")
            .appendStringPart(", ")
            .appendPlaceholderReference("two")
            .appendStringPart(", buckle my shoe")
            .build(),
        extractMessage(
            "var MSG_SILLY = function(one, two) {",
            "  return one + ', ' + two + ', buckle my shoe';",
            "};"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractMixedMessages
  public void testExtractMixedMessages() {
    
    Iterator<JsMessage> msgs = extractMessages(
        "var MSG_MONEY = function(amount) {",
        "  return 'You owe $' + amount +",
        "         ' to the credit card company.';",
        "};",
        "var MSG_TIME = goog.getMsg('You need to finish your work in ' +",
        "                           '{$duration} hours.', {'duration': d});",
        "var MSG_NAG = 'Clean your room.\\n\\nWash your clothes.';",
        "var MSG_NAG_HELP = 'Just some ' +",
        "                   'nags.';").iterator();

    assertEquals(
        new JsMessage.Builder("MSG_MONEY")
            .appendStringPart("You owe $")
            .appendPlaceholderReference("amount")
            .appendStringPart(" to the credit card company.")
            .build(),
        msgs.next());
    assertEquals(
        new JsMessage.Builder("MSG_TIME")
            .appendStringPart("You need to finish your work in ")
            .appendPlaceholderReference("duration")
            .appendStringPart(" hours.")
            .build(),
        msgs.next());
    assertEquals(
        new JsMessage.Builder("MSG_NAG")
            .appendStringPart("Clean your room.\n\nWash your clothes.")
            .setDesc("Just some nags.")
            .build(),
        msgs.next());
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testDuplicateUnnamedVariables
  public void testDuplicateUnnamedVariables() {
    
    
    Collection<JsMessage> msgs = extractMessages(
        "function a() {",
        "  var MSG_UNNAMED_2 = goog.getMsg('foo');",
        "}",
        "function b() {",
        "  var MSG_UNNAMED_2 = goog.getMsg('bar');",
        "}");

    assertEquals(2, msgs.size());
    final Iterator<JsMessage> iter = msgs.iterator();
    assertEquals("foo", iter.next().toString());
    assertEquals("bar", iter.next().toString());
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testMeaningAnnotation
  public void testMeaningAnnotation() {
    List<JsMessage> msgs = Lists.newArrayList(
        extractMessages(
            "var MSG_UNNAMED_1 = goog.getMsg('foo');",
            "var MSG_UNNAMED_2 = goog.getMsg('foo');"));
    assertEquals(2, msgs.size());
    assertTrue(msgs.get(0).getId().equals(msgs.get(1).getId()));
    assertEquals(msgs.get(0), msgs.get(1));

    msgs = Lists.newArrayList(
        extractMessages(
            "var MSG_UNNAMED_1 = goog.getMsg('foo');",
            " var MSG_UNNAMED_2 = goog.getMsg('foo');"));
    assertEquals(2, msgs.size());
    assertFalse(msgs.get(0).getId().equals(msgs.get(1).getId()));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testJsMessageOnVar
  public void testJsMessageOnVar() {
    extractMessagesSafely(
        " var MSG_HELLO = goog.getMsg('a')");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_HELLO", msg.getKey());
    assertEquals("Hello", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testJsMessageOnProperty
  public void testJsMessageOnProperty() {
    extractMessagesSafely(" " +
        "pint.sub.MSG_MENU_MARK_AS_UNREAD = goog.getMsg('a')");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_MENU_MARK_AS_UNREAD", msg.getKey());
    assertEquals("a", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testOrphanedJsMessage
  public void testOrphanedJsMessage() {
    extractMessagesSafely("goog.getMsg('a')");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(0, messages.size());

    JSError warn = compiler.getWarnings()[0];
    assertEquals(JsMessageVisitor.MESSAGE_NODE_IS_ORPHANED, warn.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMessageWithoutDescription
  public void testMessageWithoutDescription() {
    extractMessagesSafely("var MSG_HELLO = goog.getMsg('a')");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_HELLO", msg.getKey());

    assertEquals(JsMessageVisitor.MESSAGE_HAS_NO_DESCRIPTION,
        compiler.getWarnings()[0].getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIncorrectMessageReporting
  public void testIncorrectMessageReporting() {
    extractMessages("var MSG_HELLO = goog.getMsg('a' + + 'b')");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, messages.size());

    JSError mailformedTreeError = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED,
        mailformedTreeError.getType());
    assertEquals("Message parse tree malformed. "
        + "STRING or ADD node expected; found: POS",
        mailformedTreeError.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyMessage
  public void testEmptyMessage() {
    
    extractMessagesSafely("var MSG_EMPTY = '';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_EMPTY", msg.getKey());
    assertEquals("", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testConcatOfStrings
  public void testConcatOfStrings() {
    extractMessagesSafely("var MSG_NOTEMPTY = 'aa' + 'bbb' \n + ' ccc';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_NOTEMPTY", msg.getKey());
    assertEquals("aabbb ccc", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyFormatDescription
  public void testLegacyFormatDescription() {
    extractMessagesSafely("var MSG_SILLY = 'silly test message';\n"
        + "var MSG_SILLY_HELP = 'help text';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("silly test message", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyFormatParametizedFunction
  public void testLegacyFormatParametizedFunction() {
    extractMessagesSafely("var MSG_SILLY = function(one, two) {"
        + "  return one + ', ' + two + ', buckle my shoe';"
        + "};");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals(null, msg.getDesc());
    assertEquals("{$one}, {$two}, buckle my shoe", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyMessageWithDescAnnotation
  public void testLegacyMessageWithDescAnnotation() {
    
    
    extractMessagesSafely(
        " var MSG_A = 'The Message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_A", msg.getKey());
    assertEquals("The Message", msg.toString());
    assertEquals("The description", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyMessageWithDescAnnotationAndHelpVar
  public void testLegacyMessageWithDescAnnotationAndHelpVar() {
    
    
    extractMessagesSafely(
        "var MSG_A_HELP = 'This is a help var';\n" +
        " var MSG_A = 'The Message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_A", msg.getKey());
    assertEquals("The Message", msg.toString());
    assertEquals("The description in @desc", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureMessageWithHelpPostfix
  public void testClosureMessageWithHelpPostfix() {
    extractMessagesSafely("\n"
        + "var MSG_FOO_HELP = goog.getMsg('Help!');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_FOO_HELP", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("Help!", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureMessageWithoutGoogGetmsg
  public void testClosureMessageWithoutGoogGetmsg() {
    allowLegacyMessages = false;

    extractMessages("var MSG_FOO_HELP = 'I am a bad message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_NOT_INITIALIZED_USING_NEW_SYNTAX,
        error.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureFormatParametizedFunction
  public void testClosureFormatParametizedFunction() {
    extractMessagesSafely(""
        + "var MSG_SILLY = goog.getMsg('{$adjective} ' + 'message', "
        + "{'adjective': 'silly'});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("{$adjective} message", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testHugeMessage
  public void testHugeMessage() {
    extractMessagesSafely("" +
        "var MSG_HUGE = goog.getMsg(" +
        "    '{$startLink_1}Google{$endLink}' +" +
        "    '{$startLink_2}blah{$endLink}{$boo}{$foo_001}{$boo}' +" +
        "    '{$foo_002}{$xxx_001}{$image}{$image_001}{$xxx_002}'," +
        "    {'startLink_1': '<a href=http://www.google.com/>'," +
        "     'endLink': '</a>'," +
        "     'startLink_2': '<a href=\"' + opt_data.url + '\">'," +
        "     'boo': opt_data.boo," +
        "     'foo_001': opt_data.foo," +
        "     'foo_002': opt_data.boo.foo," +
        "     'xxx_001': opt_data.boo + opt_data.foo," +
        "     'image': htmlTag7," +
        "     'image_001': opt_data.image," +
        "     'xxx_002': foo.callWithOnlyTopLevelKeys(" +
        "         bogusFn, opt_data, null, 'bogusKey1'," +
        "         opt_data.moo, 'bogusKey2', param10)});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_HUGE", msg.getKey());
    assertEquals("A message with lots of stuff.", msg.getDesc());
    assertTrue(msg.isHidden());
    assertEquals("{$startLink_1}Google{$endLink}{$startLink_2}blah{$endLink}" +
        "{$boo}{$foo_001}{$boo}{$foo_002}{$xxx_001}{$image}" +
        "{$image_001}{$xxx_002}", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnnamedGoogleMessage
  public void testUnnamedGoogleMessage() {
    extractMessagesSafely("var MSG_UNNAMED_2 = goog.getMsg('Hullo');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals(null, msg.getDesc());
    assertEquals("MSG_16LJMYKCXT84X", msg.getKey());
    assertEquals("MSG_16LJMYKCXT84X", msg.getId());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyTextMessage
  public void testEmptyTextMessage() {
    extractMessagesSafely(" var MSG_FOO = goog.getMsg('');");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    assertEquals("Message value of MSG_FOO is just an empty string. "
        + "Empty messages are forbidden.",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyTextComplexMessage
  public void testEmptyTextComplexMessage() {
    extractMessagesSafely(" var MSG_BAR = goog.getMsg("
        + "'' + '' + ''     + ''\n+'');");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    assertEquals("Message value of MSG_BAR is just an empty string. "
        + "Empty messages are forbidden.",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMessageIsNoUnnamed
  public void testMessageIsNoUnnamed() {
    extractMessagesSafely("var MSG_UNNAMED_ITEM = goog.getMsg('Hullo');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_UNNAMED_ITEM", msg.getKey());
    assertFalse(msg.isHidden());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMsgVarWithoutAssignment
  public void testMsgVarWithoutAssignment() {
    extractMessages("var MSG_SILLY;");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_HAS_NO_VALUE, error.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testRegularVarWithoutAssignment
  public void testRegularVarWithoutAssignment() {
    extractMessagesSafely("var SILLY;");

    assertTrue(messages.isEmpty());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMsgVarWithIncorrectRightSide
  public void testMsgVarWithIncorrectRightSide() {
    extractMessages("var MSG_SILLY = 0;");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. Cannot parse value of "
        + "message MSG_SILLY", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIncorrectMessage
  public void testIncorrectMessage() {
    extractMessages("DP_DatePicker.MSG_DATE_SELECTION = {};");

    assertEquals(0, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. "+
                 "Message must be initialized using goog.getMsg function.",
                 error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnrecognizedFunction
  public void testUnrecognizedFunction() {
    allowLegacyMessages = false;
    extractMessages("DP_DatePicker.MSG_DATE_SELECTION = somefunc('a')");

    assertEquals(0, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. "+
                 "Message initialized using unrecognized function. " +
                 "Please use goog.getMsg() instead.",
                 error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testExtractPropertyMessage
  public void testExtractPropertyMessage() {
    extractMessagesSafely(""
        + "a.b.MSG_SILLY = goog.getMsg(\n"
        + "    '{$adjective} ' + '{$someNoun}',\n"
        + "    {'adjective': adj, 'someNoun': noun});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("{$adjective} {$someNoun}", msg.toString());
    assertEquals("A message that demonstrates placeholders", msg.getDesc());
    assertTrue(msg.isHidden());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testAlmostButNotExternalMessage
  public void testAlmostButNotExternalMessage() {
    extractMessagesSafely(
        " var MSG_EXTERNAL = goog.getMsg('External');");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());
    assertFalse(messages.get(0).isExternal());
    assertEquals("MSG_EXTERNAL", messages.get(0).getKey());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testExternalMessage
  public void testExternalMessage() {
    extractMessagesSafely("var MSG_EXTERNAL_111 = goog.getMsg('Hello World');");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());
    assertTrue(messages.get(0).isExternal());
    assertEquals("111", messages.get(0).getId());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameStrict
  public void testIsValidMessageNameStrict() {
    JsMessageVisitor visitor = new DummyJsVisitor(CLOSURE);

    assertTrue(visitor.isMessageName("MSG_HELLO", true));
    assertTrue(visitor.isMessageName("MSG_", true));
    assertTrue(visitor.isMessageName("MSG_HELP", true));
    assertTrue(visitor.isMessageName("MSG_FOO_HELP", true));

    assertFalse(visitor.isMessageName("_FOO_HELP", true));
    assertFalse(visitor.isMessageName("MSGFOOP", true));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameRelax
  public void testIsValidMessageNameRelax() {
    JsMessageVisitor visitor = new DummyJsVisitor(RELAX);

    assertFalse(visitor.isMessageName("MSG_HELP", false));
    assertFalse(visitor.isMessageName("MSG_FOO_HELP", false));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameLegacy
  public void testIsValidMessageNameLegacy() {
    theseAreLegacyMessageNames(new DummyJsVisitor(RELAX));
    theseAreLegacyMessageNames(new DummyJsVisitor(LEGACY));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnexistedPlaceholders
  public void testUnexistedPlaceholders() {
    extractMessages("var MSG_FOO = goog.getMsg('{$foo}:', {});");

    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Unrecognized message "
        + "placeholder referenced: foo", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnusedReferenesAreNotOK
  public void testUnusedReferenesAreNotOK() {
    extractMessages(" "
        + "var MSG_FOO = goog.getMsg('lalala:', {foo:1});");
    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Unused message placeholder: "
        + "foo", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testDuplicatePlaceHoldersAreBad
  public void testDuplicatePlaceHoldersAreBad() {
    extractMessages("var MSG_FOO = goog.getMsg("
        + "'{$foo}:', {'foo': 1, 'foo' : 2});");

    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Duplicate placeholder "
        + "name: foo", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testDuplicatePlaceholderReferencesAreOk
  public void testDuplicatePlaceholderReferencesAreOk() {
    extractMessagesSafely("var MSG_FOO = goog.getMsg("
        + "'{$foo}:, {$foo}', {'foo': 1});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("{$foo}:, {$foo}", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testCamelcasePlaceholderNamesAreOk
  public void testCamelcasePlaceholderNamesAreOk() {
    extractMessagesSafely("var MSG_WITH_CAMELCASE = goog.getMsg("
        + "'Slide {$slideNumber}:', {'slideNumber': opt_index + 1});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_WITH_CAMELCASE", msg.getKey());
    assertEquals("Slide {$slideNumber}:", msg.toString());
    List<CharSequence> parts = msg.parts();
    assertEquals(3, parts.size());
    assertEquals("slideNumber",
        ((JsMessage.PlaceholderReference)parts.get(1)).getName());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testWithNonCamelcasePlaceholderNamesAreNotOk
  public void testWithNonCamelcasePlaceholderNamesAreNotOk() {
    extractMessages("var MSG_WITH_CAMELCASE = goog.getMsg("
        + "'Slide {$slide_number}:', {'slide_number': opt_index + 1});");

    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Placeholder name not in "
        + "lowerCamelCase: slide_number", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnquotedPlaceholdersAreOk
  public void testUnquotedPlaceholdersAreOk() {
    extractMessagesSafely(" "
        + "var MSG_FOO = goog.getMsg('foo {$unquoted}:', {unquoted: 12});");

    assertEquals(1, messages.size());
    assertEquals(0, compiler.getWarningCount());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsLowerCamelCaseWithNumericSuffixes
  public void testIsLowerCamelCaseWithNumericSuffixes() {
    assertTrue(isLowerCamelCaseWithNumericSuffixes("name"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("NAME"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("Name"));

    assertTrue(isLowerCamelCaseWithNumericSuffixes("a4Letter"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("A4_LETTER"));

    assertTrue(isLowerCamelCaseWithNumericSuffixes("startSpan_1_23"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("startSpan_1_23b"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("START_SPAN_1_23"));

    assertFalse(isLowerCamelCaseWithNumericSuffixes(""));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testToLowerCamelCaseWithNumericSuffixes
  public void testToLowerCamelCaseWithNumericSuffixes() {
    assertEquals("name", toLowerCamelCaseWithNumericSuffixes("NAME"));
    assertEquals("a4Letter", toLowerCamelCaseWithNumericSuffixes("A4_LETTER"));
    assertEquals("startSpan_1_23",
        toLowerCamelCaseWithNumericSuffixes("START_SPAN_1_23"));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testDuplicateMessageError
  public void testDuplicateMessageError() {
    extractMessages(
        "(function () { var MSG_HELLO = goog.getMsg('a')})" +
        "(function () { var MSG_HELLO = goog.getMsg('a')})");

    assertEquals(0, compiler.getWarningCount());

    String errors = Joiner.on("\n").join(compiler.getErrors());
    assertEquals("There should be one error. " + errors,
        1, compiler.getErrorCount());
    assertEquals(errors, JsMessageVisitor.MESSAGE_DUPLICATE_KEY,
        compiler.getErrors()[0].getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testNoDuplicateErrorOnExternMessage
  public void testNoDuplicateErrorOnExternMessage() {
    extractMessagesSafely(
        "(function () { " +
        "var MSG_EXTERNAL_2 = goog.getMsg('a')})" +
        "(function () { " +
        "var MSG_EXTERNAL_2 = goog.getMsg('a')})");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testStraightLine
  public void testStraightLine() {
    
    assertNotLiveBeforeX("X:var a;", "a");
    assertNotLiveAfterX("X:var a;", "a");
    assertNotLiveAfterX("X:var a=1;", "a");
    assertLiveAfterX("X:var a=1; a()", "a");
    assertNotLiveBeforeX("X:var a=1; a()", "a");
    assertLiveBeforeX("var a;X:a;", "a");
    assertLiveBeforeX("var a;X:a=a+1;", "a");
    assertLiveBeforeX("var a;X:a+=1;", "a");
    assertLiveBeforeX("var a;X:a++;", "a");
    assertNotLiveAfterX("var a,b;X:b();", "a");
    assertNotLiveBeforeX("var a,b;X:b();", "a");
    assertLiveBeforeX("var a,b;X:b(a);", "a");
    assertLiveBeforeX("var a,b;X:b(1,2,3,b(a + 1));", "a");
    assertNotLiveBeforeX("var a,b;X:a=1;b(a)", "a");
    assertNotLiveAfterX("var a,b;X:b(a);b()", "a");
    assertLiveBeforeX("var a,b;X:b();b=1;a()", "b");
    assertLiveAfterX("X:a();var a;a()", "a");
    assertNotLiveAfterX("X:a();var a=1;a()", "a");
    assertLiveBeforeX("var a,b;X:a,b=1", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testProperties
  public void testProperties() {
    
    assertLiveBeforeX("var a,b;X:a.P;", "a");

    
    assertLiveBeforeX("var a,b;X:a.P=1;b()", "a");
    assertLiveBeforeX("var a,b;X:a.P.Q=1;b()", "a");

    
    assertNotLiveAfterX("var a,b;X:b.P.Q.a=1;", "a");

    assertLiveBeforeX("var a,b;X:b.P.Q=a;", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testConditions
  public void testConditions() {
    
    assertLiveBeforeX("var a,b;X:if(a){}", "a");
    assertLiveBeforeX("var a,b;X:if(a||b) {}", "a");
    assertLiveBeforeX("var a,b;X:if(b||a) {}", "a");
    assertLiveBeforeX("var a,b;X:if(b||b(a)) {}", "a");
    assertNotLiveAfterX("var a,b;X:b();if(a) {}", "b");

    
    assertNotLiveAfterX("var a,b;X:a();if(a=b){}a()", "a");
    assertNotLiveAfterX("var a,b;X:a();while(a=b){}a()", "a");

    
    assertNotLiveAfterX("var a,b;X:a();if((a=b)&&b){}a()", "a");
    assertNotLiveAfterX("var a,b;X:a();while((a=b)&&b){}a()", "a");
    assertLiveBeforeX("var a,b;a();X:if(b&&(a=b)){}a()", "a"); 
    assertLiveBeforeX("var a,b;a();X:if(a&&(a=b)){}a()", "a");
    assertLiveBeforeX("var a,b;a();X:while(b&&(a=b)){}a()", "a");
    assertLiveBeforeX("var a,b;a();X:while(a&&(a=b)){}a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testArrays
  public void testArrays() {
    assertLiveBeforeX("var a;X:a[1]", "a");
    assertLiveBeforeX("var a,b;X:b[a]", "a");
    assertLiveBeforeX("var a,b;X:b[1,2,3,4,b(a)]", "a");
    assertLiveBeforeX("var a,b;X:b=[a,'a']", "a");
    assertNotLiveBeforeX("var a,b;X:a=[];b(a)", "a");

    
    assertLiveBeforeX("var a;X:a[1]=1", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testTwoPaths
  public void testTwoPaths() {
    
    assertLiveBeforeX("var a,b;X:if(b){b(a)}else{b(a)};", "a");

    
    assertLiveBeforeX("var a,b;X:if(b){b(b)}else{b(a)};", "a");
    assertLiveBeforeX("var a,b;X:if(b){b(a)}else{b(b)};", "a");

    
    assertNotLiveAfterX("var a,b;X:if(b){b(b)}else{b(b)};", "a");

    
    assertLiveBeforeX("var a,b;X:if(b){b(b)}else{b(b)}a();", "a");

    
    assertLiveBeforeX("var a;X:while(param1){a()};", "a");
    assertLiveBeforeX("var a;X:while(param1){a=1};a()", "a");

    
    assertLiveBeforeX("var a;X:if(param1){a()};", "a");
    assertLiveBeforeX("var a;X:if(param1){a=1};a()", "a");

    
    
    assertNotLiveAfterX("X:var a;do{a=1}while(param1);a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testThreePaths
  public void testThreePaths() {
    assertLiveBeforeX("var a;X:if(1){}else if(2){}else{a()};", "a");
    assertLiveBeforeX("var a;X:if(1){}else if(2){a()}else{};", "a");
    assertLiveBeforeX("var a;X:if(1){a()}else if(2){}else{};", "a");
    assertLiveBeforeX("var a;X:if(1){}else if(2){}else{};a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testHooks
  public void testHooks() {
    assertLiveBeforeX("var a;X:1?a=1:1;a()", "a");

    
    
    
    assertLiveBeforeX("var a,b;X:b=1?a:2", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testForLoops
  public void testForLoops() {
    
    assertNotLiveBeforeX("var a,b;for(a=0;a<9;a++){b(a)};X:b", "a");
    assertNotLiveBeforeX("var a,b;for(a in b){a()};X:b", "a");
    assertNotLiveBeforeX("var a,b;for(a in b){a()};X:a", "b");
    assertLiveBeforeX("var b;for(var a in b){X:a()};", "a");

    
    assertLiveBeforeX("var a,b;for(a=0;a<9;a++){X:1}", "a");
    assertLiveAfterX("var a,b;for(a in b){X:b};", "a");
    
    assertLiveBeforeX("var a,b; X:for(a in b){ }", "a");

    
    
    

    
    assertLiveBeforeX("var a,b;X:a();b();for(a in b){a()};", "a");

    
    assertLiveBeforeX("var a,b;X:b;for(b=a;;){};", "a");
    assertNotLiveBeforeX("var a,b;X:a;for(b=a;;){b()};b();", "b");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testNestedLoops
  public void testNestedLoops() {
    assertLiveBeforeX("var a;X:while(1){while(1){a()}}", "a");
    assertLiveBeforeX("var a;X:while(1){while(1){while(1){a()}}}", "a");
    assertLiveBeforeX("var a;X:while(1){while(1){a()};a=1}", "a");
    assertLiveAfterX("var a;while(1){while(1){a()};X:a=1;}", "a");
    assertLiveAfterX("var a;while(1){X:a=1;while(1){a()}}", "a");
    assertNotLiveBeforeX(
        "var a;X:1;do{do{do{a=1;}while(1)}while(1)}while(1);a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testSwitches
  public void testSwitches() {
    assertLiveBeforeX("var a,b;X:switch(a){}", "a");
    assertLiveBeforeX("var a,b;X:switch(b){case(a):break;}", "a");
    assertLiveBeforeX("var a,b;X:switch(b){case(b):case(a):break;}", "a");
    assertNotLiveBeforeX(
        "var a,b;X:switch(b){case 1:a=1;break;default:a=2;break};a()", "a");

    assertLiveBeforeX("var a,b;X:switch(b){default:a();break;}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testAssignAndReadInCondition
  public void testAssignAndReadInCondition() {
    
    
    
    assertLiveBeforeX("var a, b; X: if ((a = this) && (b = a)) {}", "a");
    assertNotLiveBeforeX("var a, b; X: a = 1, b = 1;", "a");
    assertNotLiveBeforeX("var a; X: a = 1, a = 1;", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testParam
  public void testParam() {
    
    assertNotLiveAfterX("var a;X:a()", "param1");
    assertLiveBeforeX("var a;X:a(param1)", "param1");
    assertNotLiveAfterX("var a;X:a();a(param2)", "param1");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testArgumentsArray
  public void testArgumentsArray() {
    
    
    assertEscaped("arguments[0]", "param1");
    assertEscaped("arguments[0]", "param2");
    assertEscaped("var args = arguments", "param1");
    assertEscaped("var args = arguments", "param2");
    assertNotEscaped("arguments = []", "param1");
    assertNotEscaped("arguments = []", "param2");
    assertEscaped("arguments[0] = 1", "param1");
    assertEscaped("arguments[0] = 1", "param2");
    assertEscaped("arguments[arguments[0]] = 1", "param1");
    assertEscaped("arguments[arguments[0]] = 1", "param2");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testTryCatchFinally
  public void testTryCatchFinally() {
    assertLiveAfterX("var a; try {X:a=1} finally {a}", "a");
    assertLiveAfterX("var a; try {a()} catch(e) {X:a=1} finally {a}", "a");
    
    
    assertNotLiveAfterX("var a = 1; try {" +
        "try {a()} catch(e) {X:1} } catch(E) {a}", "a");
    assertLiveAfterX("var a; while(1) { try {X:a=1;break} finally {a}}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testExceptionThrowingAssignments
  public void testExceptionThrowingAssignments() {
    assertLiveBeforeX("try{var a; X:a=foo();a} catch(e) {e()}", "a");
    assertLiveBeforeX("try{X:var a=foo();a} catch(e) {e()}", "a");
    assertLiveBeforeX("try{X:var a=foo()} catch(e) {e(a)}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testInnerFunctions
  public void testInnerFunctions() {
    assertLiveBeforeX("function a() {}; X: a()", "a");
    assertNotLiveBeforeX("X: function a() {}", "a");
    assertLiveBeforeX("a = function(){}; function a() {}; X: a()", "a");
    
    
    assertLiveAfterX("X: a = function(){}; function a() {}; a()", "a");
    assertNotLiveBeforeX("X: a = function(){}; function a() {}; a()", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testEscaped
  public void testEscaped() {
    assertEscaped("var a;function b(){a()}", "a");
    assertEscaped("var a;function b(){param1()}", "param1");
    assertEscaped("var a;function b(){function c(){a()}}", "a");
    assertEscaped("var a;function b(){param1.x = function() {a()}}", "a");
    assertEscaped("try{} catch(e){}", "e");
    assertNotEscaped("var a;function b(){var c; c()}", "c");
    assertNotEscaped("var a;function f(){function b(){var c;c()}}", "c");
    assertNotEscaped("var a;function b(){};a()", "a");
    assertNotEscaped("var a;function f(){function b(){}}a()", "a");
    assertNotEscaped("var a;function b(){var a;a()};a()", "a");

    
    assertEscaped("var _x", "_x");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testEscapedLiveness
  public void testEscapedLiveness() {
    assertNotLiveBeforeX("var a;X:a();function b(){a()}", "a");
  }

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testBug1449316
  public void testBug1449316() {
    assertLiveBeforeX("try {var x=[]; X:var y=x[0]} finally {foo()}", "x");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInitialTypingScope
  public void testInitialTypingScope() {
    Scope s = new TypedScopeCreator(compiler,
        new DefaultCodingConvention()).createInitialScope(
            new Node(Token.BLOCK));

    assertEquals(ARRAY_FUNCTION_TYPE, s.getVar("Array").getType());
    assertEquals(BOOLEAN_OBJECT_FUNCTION_TYPE,
        s.getVar("Boolean").getType());
    assertEquals(DATE_FUNCTION_TYPE, s.getVar("Date").getType());
    assertEquals(ERROR_FUNCTION_TYPE, s.getVar("Error").getType());
    assertEquals(EVAL_ERROR_FUNCTION_TYPE,
        s.getVar("EvalError").getType());
    assertEquals(NUMBER_OBJECT_FUNCTION_TYPE,
        s.getVar("Number").getType());
    assertEquals(OBJECT_FUNCTION_TYPE, s.getVar("Object").getType());
    assertEquals(RANGE_ERROR_FUNCTION_TYPE,
        s.getVar("RangeError").getType());
    assertEquals(REFERENCE_ERROR_FUNCTION_TYPE,
        s.getVar("ReferenceError").getType());
    assertEquals(REGEXP_FUNCTION_TYPE, s.getVar("RegExp").getType());
    assertEquals(STRING_OBJECT_FUNCTION_TYPE,
        s.getVar("String").getType());
    assertEquals(SYNTAX_ERROR_FUNCTION_TYPE,
        s.getVar("SyntaxError").getType());
    assertEquals(TYPE_ERROR_FUNCTION_TYPE,
        s.getVar("TypeError").getType());
    assertEquals(URI_ERROR_FUNCTION_TYPE,
        s.getVar("URIError").getType());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck1
  public void testTypeCheck1() throws Exception {
    testTypes("function foo(){ if (foo()) return; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck2
  public void testTypeCheck2() throws Exception {
    testTypes("function foo(){ var x=foo(); x--; }",
        "increment/decrement\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck4
  public void testTypeCheck4() throws Exception {
    testTypes("function foo(){ !foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck5
  public void testTypeCheck5() throws Exception {
    testTypes("function foo(){ var a = +foo(); }",
        "sign operator\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck6
  public void testTypeCheck6() throws Exception {
    testTypes(
        "function foo(){" +
        "var a;if (a == foo())return;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck7
  public void testTypeCheck7() throws Exception {
    testTypes("function foo() {delete 'abc';}",
        TypeCheck.BAD_DELETE);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck8
  public void testTypeCheck8() throws Exception {
    testTypes("function foo(){do {} while (foo());}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck9
  public void testTypeCheck9() throws Exception {
    testTypes("function foo(){while (foo());}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck10
  public void testTypeCheck10() throws Exception {
    testTypes("function foo(){for (;foo(););}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck11
  public void testTypeCheck11() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a = b;",
        "assignment\n" +
        "found   : String\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck12
  public void testTypeCheck12() throws Exception {
    testTypes("function foo(){var a = 3^foo();}",
        "bad right operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck13
  public void testTypeCheck13() throws Exception {
    testTypes("var i; i=/xx/;",
        "assignment\n" +
        "found   : RegExp\n" +
        "required: (Number|String)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck14
  public void testTypeCheck14() throws Exception {
    testTypes("function foo(opt_a){}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck15
  public void testTypeCheck15() throws Exception {
    testTypes("var x;x=null;x=10;",
        "assignment\n" +
        "found   : number\n" +
        "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck16a
  public void testTypeCheck16a() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck16b
  public void testTypeCheck16b() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck17
  public void testTypeCheck17() throws Exception {
    testTypes("\n" +
        "function a(opt_foo){\nreturn (opt_foo);\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck18
  public void testTypeCheck18() throws Exception {
    testTypes("\n function a(){return new RegExp();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck19
  public void testTypeCheck19() throws Exception {
    testTypes("\n function a(){return new Array();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck20
  public void testTypeCheck20() throws Exception {
    testTypes("\n function a(){return new Date();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckBasicDowncast
  public void testTypeCheckBasicDowncast() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckNoDowncastToNumber
  public void testTypeCheckNoDowncastToNumber() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n",
        "initializing variable\n" +
        "found   : foo\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck21
  public void testTypeCheck21() throws Exception {
    testTypes("var foo;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck22
  public void testTypeCheck22() throws Exception {
    testTypes("\nfunction foo(p){}\n" +
                  "function Element(){}\n" +
                  "var v;\n" +
                  "foo(v);\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck23
  public void testTypeCheck23() throws Exception {
    testTypes("var foo; foo = null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck24
  public void testTypeCheck24() throws Exception {
    testTypes("function MyType(){}\n" +
        "var foo; foo = null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckDefaultExterns
  public void testTypeCheckDefaultExterns() throws Exception {
    testTypes(" function f(x) {}" +
        "f([].length);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckCustomExterns
  public void testTypeCheckCustomExterns() throws Exception {
    testTypes(
        DEFAULT_EXTERNS + " Array.prototype.oogabooga;",
        " function f(x) {}" +
        "f([].oogabooga);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string", false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray1
  public void testParameterizedArray1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray2
  public void testParameterizedArray2() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray3
  public void testParameterizedArray3() throws Exception {
    testTypes(" var f = function(a) { a[1] = 0; return a[0]; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray4
  public void testParameterizedArray4() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray5
  public void testParameterizedArray5() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray6
  public void testParameterizedArray6() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : *\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray7
  public void testParameterizedArray7() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject1
  public void testParameterizedObject1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject2
  public void testParameterizedObject2() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject3
  public void testParameterizedObject3() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject4
  public void testParameterizedObject4() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: E.<string>");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnionOfFunctionAndType
  public void testUnionOfFunctionAndType() throws Exception {
    testTypes(" var a;" +
        " var b = null; a = b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalParameterComparedToUndefined
  public void testOptionalParameterComparedToUndefined() throws Exception {
    testTypes("function foo(opt_a)" +
        "{if (opt_a==undefined) var b = 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalAllType
  public void testOptionalAllType() throws Exception {
    testTypes("function f(opt_x) { return opt_x }\n" +
        "var y;\n" +
        "f(y);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalUnknownNamedType
  public void testOptionalUnknownNamedType() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { return opt_x; }\n" +
        "var T = function() {};",
        "inconsistent return type\n" +
        "found   : (T|undefined)\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam
  public void testOptionalArgFunctionParam() throws Exception {
    testTypes("" +
        "function f(a) {a()};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam2
  public void testOptionalArgFunctionParam2() throws Exception {
    testTypes("" +
        "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam3
  public void testOptionalArgFunctionParam3() throws Exception {
    testTypes("" +
        "function f(a) {a(undefined)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam4
  public void testOptionalArgFunctionParam4() throws Exception {
    String expectedWarning = "Function a: called with 2 argument(s). " +
        "Function requires at least 0 argument(s) and no more than 1 " +
        "argument(s).";

    testTypes("function f(a) {a(3,4)};",
              expectedWarning, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParamError
  public void testOptionalArgFunctionParamError() throws Exception {
    String expectedWarning =
        "Bad type annotation. variable length argument must be last";
    testTypes("" +
              "function f(a) {};", expectedWarning, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam
  public void testOptionalNullableArgFunctionParam() throws Exception {
    testTypes("" +
              "function f(a) {a()};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam2
  public void testOptionalNullableArgFunctionParam2() throws Exception {
    testTypes("" +
              "function f(a) {a(null)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam3
  public void testOptionalNullableArgFunctionParam3() throws Exception {
    testTypes("" +
              "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionReturn
  public void testOptionalArgFunctionReturn() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()()");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionReturn2
  public void testOptionalArgFunctionReturn2() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()({})");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanType
  public void testBooleanType() throws Exception {
    testTypes("var x = 1 < 2;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction1
  public void testBooleanReduction1() throws Exception {
    testTypes("var x; x = null || \"a\";");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction2
  public void testBooleanReduction2() throws Exception {
    
    
    testTypes("" +
        "(function(s) { return ((s == 'a') && s) || 'b'; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction3
  public void testBooleanReduction3() throws Exception {
    testTypes("" +
        "(function(s) { return s && null && 3; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction4
  public void testBooleanReduction4() throws Exception {
    testTypes("" +
        "(function(x) { return null || x || null ; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction5
  public void testBooleanReduction5() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!x || typeof x == 'string') {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction6
  public void testBooleanReduction6() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!(x && typeof x != 'string')) {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction7
   public void testBooleanReduction7() throws Exception {
     testTypes("var T = function() {};\n" +
         "\n" +
         "var f = function(x) {\n" +
         "if (!x) {\n" +
         "return x;\n" +
         "}\n" +
         "return null;\n" +
         "};");
   }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNullAnd
  public void testNullAnd() throws Exception {
    testTypes("var x;\n" +
        "var r = x && x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNullOr
  public void testNullOr() throws Exception {
    testTypes("var x;\n" +
        "var r = x || x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation1
  public void testBooleanPreservation1() throws Exception {
    testTypes("var x = \"a\";" +
        "x = ((x == \"a\") && x) || x == \"b\";",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation2
  public void testBooleanPreservation2() throws Exception {
    testTypes("var x = \"a\"; x = (x == \"a\") || x;",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation3
  public void testBooleanPreservation3() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "condition always evaluates to false\n" +
        "left : Function\n" +
        "right: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation4
  public void testBooleanPreservation4() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "inconsistent return type\n" +
        "found   : (boolean|null|undefined)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction1
  public void testTypeOfReduction1() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'number' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction2
  public void testTypeOfReduction2() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x != 'string' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction3
  public void testTypeOfReduction3() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'object' ? 1 : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction4
  public void testTypeOfReduction4() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'undefined' ? {} : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction5
  public void testTypeOfReduction5() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x != 'number' ? x : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction6
  public void testTypeOfReduction6() throws Exception {
    testTypes("\n" +
        "function f(x) {\n" +
        "return typeof x == 'string' && x.length == 3 ? x : 'a';\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction7
  public void testTypeOfReduction7() throws Exception {
    testTypes("var f = function(x) { " +
        "return typeof x == 'number' ? x : 'a'; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction8
  public void testTypeOfReduction8() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isString(x) && x.length == 3 ? x : 'a';\n" +
        "}", null);
  }
