// buggy code
  public void enterScope(NodeTraversal t) {
    Node declarationRoot = t.getScopeRoot();
    Renamer renamer;
    if (nameStack.isEmpty()) {
      // If the contextual renamer is being used the starting context can not
      // be a function.
      Preconditions.checkState(
          declarationRoot.getType() != Token.FUNCTION ||
          !(rootRenamer instanceof ContextualRenamer));
      Preconditions.checkState(t.inGlobalScope());
      renamer = rootRenamer;
    } else {
      renamer = nameStack.peek().forChildScope();
    }

    if (declarationRoot.getType() == Token.FUNCTION) {
      for (Node c = declarationRoot.getFirstChild().getNext().getFirstChild(); c != null; c = c.getNext()) {
        String name = c.getString();
        renamer.addDeclaredName(name);
      }
      Node functionBody = declarationRoot.getLastChild();
      findDeclaredNames(functionBody, null, renamer);
    }  else 
    if (declarationRoot.getType() != Token.FUNCTION) {
      // Add the block declarations
      findDeclaredNames(declarationRoot, null, renamer);
    }
    nameStack.push(renamer);
  }

  public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    switch (n.getType()) {
      case Token.FUNCTION:
        {
          // Add recursive function name, if needed.
          // NOTE: "enterScope" is called after we need to pick up this name.
          Renamer renamer = nameStack.peek().forChildScope();

          // If needed, add the function recursive name.
          String name = n.getFirstChild().getString();
          if (name != null && !name.isEmpty() && parent != null
              && !NodeUtil.isFunctionDeclaration(n)) {
            renamer.addDeclaredName(name);
          }



          // Add the function parameters

          // Add the function body declarations

          nameStack.push(renamer);
        }
        break;

      case Token.CATCH:
        {
          Renamer renamer = nameStack.peek().forChildScope();

          String name = n.getFirstChild().getString();
          renamer.addDeclaredName(name);

          nameStack.push(renamer);
        }
        break;
    }

    return true;
  }

  public void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()) {
      case Token.NAME:
        String newName = getReplacementName(n.getString());
        if (newName != null) {
          Renamer renamer = nameStack.peek();
          if (renamer.stripConstIfReplaced()) {
            // TODO(johnlenz): Do we need to do anything about the javadoc?
            n.removeProp(Node.IS_CONSTANT_NAME);
          }
          n.setString(newName);
          t.getCompiler().reportCodeChange();
        }
        break;

      case Token.FUNCTION:
        // Remove the function body scope
        // Remove function recursive name (if any).
        nameStack.pop();
        break;

        // Note: The parameters and function body variables live in the
        // same scope, we introduce the scope when in the "shouldTraverse"
        // visit of LP, but remove it when when we exit the function above.

      case Token.CATCH:
        // Remove catch except name from the stack of names.
        nameStack.pop();
        break;
    }
  }

// relevant test
// com.google.javascript.jscomp.AliasExternalsTest::testGlobalAlias
  public void testGlobalAlias() {
    test("window.setTimeout(function() {}, 0);" +
         "var doc=window.document;" +
         "window.alert(\"foo\");" +
         "window.eval(\"1\");" +
         "window.location.href=\"http://www.example.com\";" +
         "function foo() {var window = \"bar\"; return window}foo();",

         "var GLOBAL_window=window;" +
         formatPropNameDecl("setTimeout") +
         "GLOBAL_window[$$PROP_setTimeout](function() {}, 0);" +
         "var doc=GLOBAL_window.document;" +
         "GLOBAL_window.alert(\"foo\");" +
         "GLOBAL_window.eval(\"1\");" +
         "GLOBAL_window.location.href=\"http://www.example.com\";" +
         "function foo() {var window = \"bar\"; return window}foo();");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testUnaliasable
  public void testUnaliasable() {
    test("function foo() {" +
          "var x=arguments.length;" +
          "var y=arguments.length;" +
          "var z=arguments.length;" +
          "var w=arguments.length;" +
          "return x + y + z + w" +
         "};foo();",

         formatPropNameDecl("length") +
         "function foo() {" +
          "var x=arguments[$$PROP_length];" +
          "var y=arguments[$$PROP_length];" +
          "var z=arguments[$$PROP_length];" +
          "var w=arguments[$$PROP_length];" +
          "return x + y + z + w" +
         "};foo();");

    test("var x=new ActiveXObject();" +
         "x.foo=\"bar\";" +
         "var y=new ActiveXObject();" +
         "y.foo=\"bar\";" +
         "var z=new ActiveXObject();" +
         "z.foo=\"bar\";",

         "var x=new ActiveXObject();" +
         "x.foo=\"bar\";" +
         "var y=new ActiveXObject();" +
         "y.foo=\"bar\";" +
         "var z=new ActiveXObject();" +
         "z.foo=\"bar\";");

    test("var _a=eval('foo'),_b=eval('foo'),_c=eval('foo'),_d=eval('foo')," +
             "_e=eval('foo'),_f=eval('foo'),_g=eval('foo');",
         "var _a=eval('foo'),_b=eval('foo'),_c=eval('foo'),_d=eval('foo')," +
             "_e=eval('foo'),_f=eval('foo'),_g=eval('foo');");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testAliasableGlobals
  public void testAliasableGlobals() {
    aliasableGlobals = "notused,length";
    test("function foo() {" +
          "var x=arguments.length;" +
          "var y=arguments.length;" +
          "var z=arguments.length;" +
          "var w=arguments.length;" +
          "return x + y + z + w" +
         "};foo();",

         formatPropNameDecl("length") +
         "function foo() {" +
          "var x=arguments[$$PROP_length];" +
          "var y=arguments[$$PROP_length];" +
          "var z=arguments[$$PROP_length];" +
          "var w=arguments[$$PROP_length];" +
          "return x + y + z + w" +
         "};foo();");

    aliasableGlobals = "notused,notlength";
    test("function foo() {" +
          "var x=arguments.length;" +
          "var y=arguments.length;" +
          "var z=arguments.length;" +
          "var w=arguments.length;" +
          "return x + y + z + w" +
         "};foo();",

         "function foo() {" +
          "var x=arguments.length;" +
          "var y=arguments.length;" +
          "var z=arguments.length;" +
          "var w=arguments.length;" +
          "return x + y + z + w" +
         "};foo();");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testAliasableAndUnaliasableGlobals
  public void testAliasableAndUnaliasableGlobals() {
    
    aliasableGlobals = "foo,bar";
    unaliasableGlobals = "";
    test("var x;", "var x;");

    
    aliasableGlobals = "";
    unaliasableGlobals = "baz,qux";
    test("var x;", "var x;");

    
    aliasableGlobals = "foo,bar";
    unaliasableGlobals = "baz,qux";
    try {
      test("var x;", "var x;");
      fail("Expected an IllegalArgumentException");
    } catch (IllegalArgumentException ex) {
      
    }
  }

// com.google.javascript.jscomp.AliasExternalsTest::testGlobalAssigment
  public void testGlobalAssigment() {
    test("var x=_USER_ID+window;" +
         "var y=_USER_ID+window;" +
         "var z=_USER_ID+window;" +
         "var w=x+y+z;" +
         "_USER_ID = \"foo\";" +
         "window++;",

         "var x=_USER_ID+window;" +
         "var y=_USER_ID+window;" +
         "var z=_USER_ID+window;" +
         "var w=x+y+z;" +
         "_USER_ID = \"foo\";" +
         "window++");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testNewOperator
  public void testNewOperator() {
    test("var x;new x(window);window;window;window;window;window",

         "var GLOBAL_window=window; var x;" +
         "  new x(GLOBAL_window);GLOBAL_window;GLOBAL_window;" +
         "  GLOBAL_window;GLOBAL_window;GLOBAL_window");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testGetProp
  public void testGetProp() {
    test("function foo(a,b){return a.length > b.length;}",
         formatPropNameDecl("length") +
         "function foo(a, b){return a[$$PROP_length] > b[$$PROP_length];}");
    test("Foo.prototype.bar = function() { return 'foo'; }",
         formatPropNameDecl("prototype") +
         "Foo[$$PROP_prototype].bar = function() { return 'foo'; }");
    test("Foo.notreplaced = 5", "Foo.notreplaced=5");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testIgnoredOps
  public void testIgnoredOps() {
    testSame("function foo() { this.length-- }");
    testSame("function foo() { this.length++ }");
    testSame("function foo() { this.length+=5 }");
    testSame("function foo() { this.length-=5 }");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testSetProp
  public void testSetProp() {
    test("function foo() { this.innerHTML = 'hello!'; }",
      formatSetPropFn("innerHTML")
        + "function foo() { SETPROP_innerHTML(this, 'hello!'); }");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testParentChild
  public void testParentChild() {
    test("a.length = b.length = c.length;", formatSetPropFn("length")
      + formatPropNameDecl("length")
      + "SETPROP_length(a, SETPROP_length(b, c[$$PROP_length]))");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testModulesWithoutDependencies
  public void testModulesWithoutDependencies() {
    test(createModules(MODULE_SRC_ONE, MODULE_SRC_TWO),
         new String[] {
           "var $$PROP_length=\"length\";a=b[$$PROP_length];" +
           "a=b[$$PROP_length];a=b[$$PROP_length];",
           "c=d[$$PROP_length];"});
  }

// com.google.javascript.jscomp.AliasExternalsTest::testModulesWithDependencies
  public void testModulesWithDependencies() {
    test(createModuleChain(MODULE_SRC_ONE, MODULE_SRC_TWO),
         new String[] {
           "var $$PROP_length=\"length\";a=b[$$PROP_length];" +
           "a=b[$$PROP_length];a=b[$$PROP_length];",
           "c=d[$$PROP_length];"});
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropAccessorPushedDeeper1
  public void testPropAccessorPushedDeeper1() {
    test(createModuleChain("var a = \"foo\";", "var b = a.length;"),
         new String[] {
           "var a = \"foo\";",
           formatPropNameDecl("length") + "var b = a[$$PROP_length]" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropAccessorPushedDeeper2
  public void testPropAccessorPushedDeeper2() {
    test(createModuleChain(
             "var a = \"foo\";", "var b = a.length;", "var c = a.length;"),
         new String[] {
           "var a = \"foo\";",
           formatPropNameDecl("length") + "var b = a[$$PROP_length]",
           "var c = a[$$PROP_length]" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropAccessorPushedDeeper3
  public void testPropAccessorPushedDeeper3() {
    test(createModuleStar(
             "var a = \"foo\";", "var b = a.length;", "var c = a.length;"),
         new String[] {
           formatPropNameDecl("length") + "var a = \"foo\";",
           "var b = a[$$PROP_length]",
           "var c = a[$$PROP_length]" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropAccessorNotPushedDeeper
  public void testPropAccessorNotPushedDeeper() {
    test(createModuleChain("var a = \"foo\"; var b = a.length;",
                                    "var c = a.length;"),
         new String[] {
           formatPropNameDecl("length") +
           "var a = \"foo\"; var b = a[$$PROP_length]",
           "var c = a[$$PROP_length]" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropMutatorPushedDeeper
  public void testPropMutatorPushedDeeper() {
    test(createModuleChain("var a = [1];", "a.length = 0;"),
         new String[] {
           "var a = [1];",
           formatSetPropFn("length") + "SETPROP_length(a, 0);" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropMutatorNotPushedDeeper
  public void testPropMutatorNotPushedDeeper() {
    test(createModuleChain(
             "var a = [1]; a.length = 1;", "a.length = 0;"),
         new String[] {
           formatSetPropFn("length") +  "var a = [1]; SETPROP_length(a, 1);",
           "SETPROP_length(a, 0);" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testGlobalAliasPushedDeeper
  public void testGlobalAliasPushedDeeper() {
    test(createModuleChain(
             "var a = 1;",
             "var b = window, c = window, d = window, e = window;"),
         new String[] { "var a = 1;",
                        "var GLOBAL_window = window;" +
                        "var b = GLOBAL_window, c = GLOBAL_window, " +
                        "    d = GLOBAL_window, e = GLOBAL_window;" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testGlobalAliasNotPushedDeeper
  public void testGlobalAliasNotPushedDeeper() {
    test(createModuleChain(
             "var a = 1, b = window;",
             "var c = window, d = window, e = window;"),
         new String[] { "var GLOBAL_window = window;" +
                        "var a = 1, b = GLOBAL_window;",
                        "var c = GLOBAL_window, " +
                        "    d = GLOBAL_window, e = GLOBAL_window;" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testNoAliasAnnotationForSingleVar
  public void testNoAliasAnnotationForSingleVar() {
    testSame("[RangeObject, RangeObject, RangeObject]");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testNoAliasAnnotationForMultiVarDeclaration
  public void testNoAliasAnnotationForMultiVarDeclaration() {
    test("[RuntimeObject, RuntimeObject, RuntimeObject," +
         " SelectionObject, SelectionObject, SelectionObject]",
         "var GLOBAL_SelectionObject = SelectionObject;" +
         "[RuntimeObject, RuntimeObject, RuntimeObject," +
         " GLOBAL_SelectionObject, GLOBAL_SelectionObject," +
         " GLOBAL_SelectionObject]");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testNoAliasAnnotationForFunction
  public void testNoAliasAnnotationForFunction() {
    testSame("[NoAliasFunction(), NoAliasFunction(), NoAliasFunction()]");
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testDontAlias
  public void testDontAlias() {
    testSame(generateCode("true", TOO_FEW_TO_ALIAS_LITERAL));
    testSame(generateCode("false", TOO_FEW_TO_ALIAS_LITERAL));
    testSame(generateCode("null", TOO_FEW_TO_ALIAS_LITERAL));
    testSame(generateCode("void 0", TOO_FEW_TO_ALIAS_LITERAL));
    testSame(generatePreProcessThrowCode(TOO_FEW_TO_ALIAS_THROW, "1"));

    
    testSame(generateCode("void 1", ENOUGH_TO_ALIAS_LITERAL));
    testSame(generateCode("void x", ENOUGH_TO_ALIAS_LITERAL));
    testSame(generateCode("void f()", ENOUGH_TO_ALIAS_LITERAL));
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testAlias
  public void testAlias() {
    test(generateCode("true", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_TRUE, ENOUGH_TO_ALIAS_LITERAL,
                      "var JSCompiler_alias_TRUE=true;"));

    test(generateCode("false", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_FALSE, ENOUGH_TO_ALIAS_LITERAL,
                      "var JSCompiler_alias_FALSE=false;"));

    test(generateCode("null", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_NULL, ENOUGH_TO_ALIAS_LITERAL,
                      "var JSCompiler_alias_NULL=null;"));

    test(generateCode("void 0", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_VOID, ENOUGH_TO_ALIAS_LITERAL,
                     "var JSCompiler_alias_VOID=void 0;"));

    test(generatePreProcessThrowCode(ENOUGH_TO_ALIAS_THROW, "1"),
         generatePostProcessThrowCode(ENOUGH_TO_ALIAS_THROW, "", "1"));
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testAliasTrueFalseNull
  public void testAliasTrueFalseNull() {
    StringBuilder actual = new StringBuilder();
    actual.append(generateCode("true", ENOUGH_TO_ALIAS_LITERAL));
    actual.append(generateCode("false", ENOUGH_TO_ALIAS_LITERAL));
    actual.append(generateCode("null", ENOUGH_TO_ALIAS_LITERAL));
    actual.append(generateCode("void 0", ENOUGH_TO_ALIAS_LITERAL));

    StringBuilder expected = new StringBuilder();
    expected.append(
        "var JSCompiler_alias_VOID=void 0;" +
        "var JSCompiler_alias_TRUE=true;" +
        "var JSCompiler_alias_NULL=null;" +
        "var JSCompiler_alias_FALSE=false;");
    expected.append(
        generateCode(AliasKeywords.ALIAS_TRUE, ENOUGH_TO_ALIAS_LITERAL));
    expected.append(
        generateCode(AliasKeywords.ALIAS_FALSE, ENOUGH_TO_ALIAS_LITERAL));
    expected.append(
        generateCode(AliasKeywords.ALIAS_NULL, ENOUGH_TO_ALIAS_LITERAL));
    expected.append(
        generateCode(AliasKeywords.ALIAS_VOID, ENOUGH_TO_ALIAS_LITERAL));

    test(actual.toString(), expected.toString());
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testAliasThrowKeywordLiteral
  public void testAliasThrowKeywordLiteral() {
    int repitions = Math.max(ENOUGH_TO_ALIAS_THROW, ENOUGH_TO_ALIAS_LITERAL);
    String afterCode = generatePostProcessThrowCode(
          repitions, "var JSCompiler_alias_TRUE=true;",
          AliasKeywords.ALIAS_TRUE);
    test(generatePreProcessThrowCode(repitions, "true"), afterCode);
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testExistingAliasDefinitionFails
  public void testExistingAliasDefinitionFails() {
    try {
      testSame("var JSCompiler_alias_TRUE='foo';");
      fail();
    } catch (RuntimeException expected) {
      
      assertTrue(-1 != expected.getMessage().indexOf(
              "Existing alias definition"));
    }
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testWithNoInputs
  public void testWithNoInputs() {
    testSame(new String[] {});
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar1
  public void testOneVar1() {
    test(" var Foo = function(){};Foo.prototype.b = 0;",
         "var Foo = function(){};Foo.prototype.a = 0;");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar2
  public void testOneVar2() {
    testSame(" var Foo = function(){};" +
             "Foo.prototype = {b: 0};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar3
  public void testOneVar3() {
    testSame(" var Foo = function(){};" +
             "Foo.prototype = {get b() {return 0}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar4
  public void testOneVar4() {
    testSame(" var Foo = function(){};" +
             "Foo.prototype = {set b(a) {}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoVar1
  public void testTwoVar1() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.x=0;";
    String output = ""
        + "var Foo = function(){};\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.b=0;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoVar2
  public void testTwoVar2() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype={z:0, z:1, x:0};\n";
    
    testSame(js);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoIndependentVar
  public void testTwoIndependentVar() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.b = 0;\n"
        + " var Bar = function(){};\n"
        + "Bar.prototype.c = 0;";
    String output = ""
        + "var Foo = function(){};"
        + "Foo.prototype.a=0;"
        + "var Bar = function(){};"
        + "Bar.prototype.a=0;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoTypesTwoVar
  public void testTwoTypesTwoVar() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.r = 0;\n"
        + "Foo.prototype.g = 0;\n"
        + " var Bar = function(){};\n"
        + "Bar.prototype.c = 0;"
        + "Bar.prototype.r = 0;";
    String output = ""
        + "var Foo = function(){};"
        + "Foo.prototype.a=0;"
        + "Foo.prototype.b=0;"
        + "var Bar = function(){};"
        + "Bar.prototype.b=0;"
        + "Bar.prototype.a=0;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testUnion
  public void testUnion() {
    String js = ""
        + " var Foo = function(){};\n"
        + " var Bar = function(){};\n"
        + "Foo.prototype.foodoo=0;\n"
        + "Bar.prototype.bardoo=0;\n"
        + "\n"
        + "var U;\n"
        + "U.joint;"
        + "U.joint";
    String output = ""
        + "var Foo = function(){};\n"
        + "var Bar = function(){};\n"
        + "Foo.prototype.b=0;\n"
        + "Bar.prototype.b=0;\n"
        + "var U;\n"
        + "U.a;"
        + "U.a";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testUnions
  public void testUnions() {
    String js = ""
        + " var Foo = function(){};\n"
        + " var Bar = function(){};\n"
        + " var Baz = function(){};\n"
        + " var Bat = function(){};\n"
        + "Foo.prototype.lone1=0;\n"
        + "Bar.prototype.lone2=0;\n"
        + "Baz.prototype.lone3=0;\n"
        + "Bat.prototype.lone4=0;\n"
        + "\n"
        + "var U1;\n"
        + "U1.j1;"
        + "U1.j2;"
        + "\n"
        + "var U2;\n"
        + "U2.j3;"
        + "U2.j4;"
        + "\n"
        + "var U3;"
        + "U3.j5;"
        + "U3.j6";
    String output = ""
        + "var Foo = function(){};\n"
        + "var Bar = function(){};\n"
        + "var Baz = function(){};\n"
        + "var Bat = function(){};\n"
        + "Foo.prototype.c=0;\n"
        + "Bar.prototype.e=0;\n"
        + "Baz.prototype.e=0;\n"
        + "Bat.prototype.c=0;\n"
        + "var U1;\n"
        + "U1.a;"
        + "U1.b;"
        + "var U2;\n"
        + "U2.c;"
        + "U2.d;"
        + "var U3;"
        + "U3.a;"
        + "U3.b";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExtends
  public void testExtends() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.x=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.prototype.y=0;\n"
        + "Bar.prototype.z=0;\n"
        + " var Baz = function(){};\n"
        + "Baz.prototype.l=0;\n"
        + "Baz.prototype.m=0;\n"
        + "Baz.prototype.n=0;\n"
        + "(new Baz).m\n";
    String output = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.a=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.prototype.b=0;\n"
        + "Bar.prototype.c=0;\n"
        + " var Baz = function(){};\n"
        + "Baz.prototype.b=0;\n"
        + "Baz.prototype.a=0;\n"
        + "Baz.prototype.c=0;\n"
        + "(new Baz).a\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testLotsOfVars
  public void testLotsOfVars() {
    StringBuilder js = new StringBuilder();
    StringBuilder output = new StringBuilder();
    js.append(" var Foo = function(){};\n");
    js.append(" var Bar = function(){};\n");
    output.append(js.toString());

    int vars = 10;
    for (int i = 0; i < vars; i++) {
      js.append("Foo.prototype.var" + i + " = 0;");
      js.append("Bar.prototype.var" + (i + 10000) + " = 0;");
      output.append("Foo.prototype." + (char) ('a' + i) + "=0;");
      output.append("Bar.prototype." + (char) ('a' + i) + "=0;");
    }
    test(js.toString(), output.toString());
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testLotsOfClasses
  public void testLotsOfClasses() {
    StringBuilder b = new StringBuilder();
    int classes = 10;
    for (int i = 0; i < classes; i++) {
      String c = "Foo" + i;
      b.append(" var " + c + " = function(){};\n");
      b.append(c + ".prototype.varness" + i + " = 0;");
    }
    String js = b.toString();
    test(js, js.replaceAll("varness\\d+", "a"));
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testFunctionType
  public void testFunctionType() {
    String js = ""
        + " function Foo(){};\n"
        + "\n"
        + "Foo.prototype.fun = function() { return new Bar(); };\n"
        + " function Bar(){};\n"
        + "Bar.prototype.bazz;\n"
        + "(new Foo).fun().bazz();";
    String output = ""
        + "function Foo(){};\n"
        + "Foo.prototype.a = function() { return new Bar(); };\n"
        + "function Bar(){};\n"
        + "Bar.prototype.a;\n"
        + "(new Foo).a().a();";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPrototypePropertiesAsObjLitKeys1
  public void testPrototypePropertiesAsObjLitKeys1() {
    test(" function Bar() {};" +
             "Bar.prototype = {2: function(){}, getA: function(){}};",
             " function Bar() {};" +
             "Bar.prototype = {2: function(){}, a: function(){}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPrototypePropertiesAsObjLitKeys2
  public void testPrototypePropertiesAsObjLitKeys2() {
    testSame(" function Bar() {};" +
             "Bar.prototype = {2: function(){}, 'getA': function(){}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testQuotedPrototypeProperty
  public void testQuotedPrototypeProperty() {
    testSame(" function Bar() {};" +
             "Bar.prototype['getA'] = function(){};" +
             "var bar = new Bar();" +
             "bar['getA']();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOverlappingOriginalAndGeneratedNames
  public void testOverlappingOriginalAndGeneratedNames() {
    test(" function Bar(){};"
         + "Bar.prototype.b = function(){};"
         + "Bar.prototype.a = function(){};"
         + "var bar = new Bar();"
         + "bar.b();",
         "function Bar(){};"
         + "Bar.prototype.a = function(){};"
         + "Bar.prototype.b = function(){};"
         + "var bar = new Bar();"
         + "bar.a();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyAddedToObject
  public void testPropertyAddedToObject() {
    testSame("var foo = {}; foo.prop = '';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyAddedToFunction
  public void testPropertyAddedToFunction() {
    test("var foo = function(){}; foo.prop = '';",
         "var foo = function(){}; foo.a = '';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyOfObjectOfUnknownType
  public void testPropertyOfObjectOfUnknownType() {
    testSame("var foo = x(); foo.prop = '';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyOnParamOfUnknownType
  public void testPropertyOnParamOfUnknownType() {
    testSame(" function Foo(){};\n"
             + "Foo.prototype.prop = 0;"
             + "function go(aFoo){\n"
             + "  aFoo.prop = 1;"
             + "}");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testSetPropertyOfGlobalThis
  public void testSetPropertyOfGlobalThis() {
    testSame("this.prop = 'bar'");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testReadPropertyOfGlobalThis
  public void testReadPropertyOfGlobalThis() {
    testSame("f(this.prop);");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testSetQuotedPropertyOfThis
  public void testSetQuotedPropertyOfThis() {
    testSame("this['prop'] = 'bar';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExternedPropertyName
  public void testExternedPropertyName() {
    test(" var Bar = function(){};"
         + " Bar.prototype.toString = function(){};"
         + "Bar.prototype.func = function(){};"
         + "var bar = new Bar();"
         + "bar.toString();",
         "var Bar = function(){};"
         + "Bar.prototype.toString = function(){};"
         + "Bar.prototype.a = function(){};"
         + "var bar = new Bar();"
         + "bar.toString();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExternedPropertyNameDefinedByObjectLiteral
  public void testExternedPropertyNameDefinedByObjectLiteral() {
    testSame("function Bar(){};Bar.prototype.factory");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticAndInstanceMethodWithSameName
  public void testStaticAndInstanceMethodWithSameName() {
    test("function Bar(){}; Bar.getA = function(){}; " +
         "Bar.prototype.getA = function(){}; Bar.getA();" +
         "var bar = new Bar(); bar.getA();",
         "function Bar(){}; Bar.a = function(){};" +
         "Bar.prototype.a = function(){}; Bar.a();" +
         "var bar = new Bar(); bar.a();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticAndInstanceProperties
  public void testStaticAndInstanceProperties() {
    test("function Bar(){};" +
         "Bar.getA = function(){}; " +
         "Bar.prototype.getB = function(){};",
         "function Bar(){}; Bar.a = function(){};" +
         "Bar.prototype.a = function(){};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticAndSubInstanceProperties
  public void testStaticAndSubInstanceProperties() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.x=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.y=0;\n"
        + "Bar.prototype.z=0;\n";
    String output = ""
        + " var Foo = function(){};\n"
        + "Foo.a=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.a=0;\n"
        + "Bar.prototype.a=0;\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticWithFunctions
  public void testStaticWithFunctions() {
    String js = ""
      + " var Foo = function() {};\n"
      + "Foo.x = 0;"
      + " function f(x) { x.y = 1 }"
      + "f(Foo)";
    String output = ""
      + " var Foo = function() {};\n"
      + "Foo.a = 0;"
      + " function f(x) { x.y = 1 }"
      + "f(Foo)";
    test(js, output);

    js = ""
      + " var Foo = function() {};\n"
      + "Foo.x = 0;"
      + " function f(x) { x.y = 1; x.x = 2;}"
      + "f(Foo)";
    test(js, js);

    js = ""
      + " var Foo = function() {};\n"
      + "Foo.x = 0;"
      + " var Bar = function() {};\n"
      + "Bar.y = 0;";

    output = ""
      + " var Foo = function() {};\n"
      + "Foo.a = 0;"
      + " var Bar = function() {};\n"
      + "Bar.a = 0;";
    test(js, output);

  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTypeMismatch
  public void testTypeMismatch() {
    testSame(EXTERNS, "var Foo = function(){};\n"
             + "var Bar = function(){};\n"
             + "Foo.prototype.b = 0;\n"
             + "\n"
             + "var F = new Bar();",
             TypeValidator.TYPE_MISMATCH_WARNING,
             "initializing variable\n"
             + "found   : Bar\n"
             + "required: (Foo|null)");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testRenamingMap
  public void testRenamingMap() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.x=0;\n"
        + "Foo.prototype.y=0;";
    String output = ""
        + "var Foo = function(){};\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.b=0;\n"
        + "Foo.prototype.c=0;";
    test(js, output);

    Map<String, String> answerMap = Maps.newHashMap();
    answerMap.put("x", "b");
    answerMap.put("y", "c");
    answerMap.put("z", "a");
    assertEquals(answerMap, lastPass.getRenamingMap());
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testInline
  public void testInline() {
    String js = ""
        + " function Foo(){}\n"
        + "Foo.prototype.x = function(){};\n"
        + "\n"
        + "function Bar(){}\n"
        + "\n"
        + "Bar.prototype.x = function() { return this.y; };\n"
        + "Bar.prototype.z = function() {};\n"
        
        + " (new Bar).y;";
    String output = ""
        + "function Foo(){}\n"
        + "Foo.prototype.a = function(){};\n"
        + "function Bar(){}\n"
        + "Bar.prototype.a = function() { return this.b; };\n"
        + "Bar.prototype.c = function() {};\n"
        
        + "(new Bar).b;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testImplementsAndExtends
  public void testImplementsAndExtends() {
    String js = ""
        + " function Foo() {}\n"
        + "\n"
        + "function Bar(){}\n"
        + "Bar.prototype.y = function() { return 3; };\n"
        + "\n"
        + "function SubBar(){ }\n"
        + " function f(x) { x.z = 3; }\n"
        + " function g(x) { x.z = 3; }";
    String output = ""
        + "function Foo(){}\n"
        + "function Bar(){}\n"
        + "Bar.prototype.b = function() { return 3; };\n"
        + "function SubBar(){}\n"
        + "function f(x) { x.a = 3; }\n"
        + "function g(x) { x.a = 3; }";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testImplementsAndExtends2
  public void testImplementsAndExtends2() {
    String js = ""
        + " function A() {}\n"
        + "\n"
        + "function C1(){}\n"
        + "\n"
        + "function C2(){}\n"
        + " function f(x) { x.y = 3; }\n"
        + " function g(x) { x.z = 3; }\n";
    String output = ""
        + "function A(){}\n"
        + "function C1(){}\n"
        + "function C2(){}\n"
        + "function f(x) { x.a = 3; }\n"
        + "function g(x) { x.b = 3; }\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExtendsInterface
  public void testExtendsInterface() {
    String js = ""
        + " function A() {}\n"
        + " function B() {}\n"
        + " function f(x) { x.y = 3; }\n"
        + " function g(x) { x.z = 3; }\n";
    String output = ""
        + "function A(){}\n"
        + "function B(){}\n"
        + "function f(x) { x.a = 3; }\n"
        + "function g(x) { x.b = 3; }\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testFunctionSubType
  public void testFunctionSubType() {
    String js = ""
        + "Function.prototype.a = 1;\n"
        + "function f() {}\n"
        + "f.y = 2;\n";
    String output = ""
        + "Function.prototype.a = 1;\n"
        + "function f() {}\n"
        + "f.b = 2;\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testFunctionSubType2
  public void testFunctionSubType2() {
    String js = ""
        + "Function.prototype.a = 1;\n"
        + " function F() {}\n"
        + "F.y = 2;\n";
    String output = ""
        + "Function.prototype.a = 1;\n"
        + "function F() {}\n"
        + "F.b = 2;\n";
    test(js, output);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithNoNewNodes
  public void testPassWithNoNewNodes() {
    String js = "var str = 'g4'; ";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithOneNew
  public void testPassWithOneNew() {
    String js =
        "var goog = {};" +
        "goog.require('foo.bar.goo'); var bar = new foo.bar.goo();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithOneNewOuterClass
  public void testPassWithOneNewOuterClass() {
    String js =
        "var goog = {};" +
        "goog.require('goog.foo.Bar'); var bar = new goog.foo.Bar.Baz();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithOneNewOuterClassWithUpperPrefix
  public void testPassWithOneNewOuterClassWithUpperPrefix() {
    String js =
        "var goog = {};" +
        "goog.require('goog.foo.IDBar'); var bar = new goog.foo.IDBar.Baz();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithOneNew
  public void testFailWithOneNew() {
    String[] js = new String[] {"var foo = {}; var bar = new foo.bar();"};
    String warning = "'foo.bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithTwoNewNodes
  public void testPassWithTwoNewNodes() {
    String js =
        "var goog = {};" +
        "goog.require('goog.foo.Bar');goog.require('goog.foo.Baz');" +
        "var str = new goog.foo.Bar('g4'), num = new goog.foo.Baz(5); ";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithNestedNewNodes
  public void testPassWithNestedNewNodes() {
    String js =
        "var goog = {}; goog.require('goog.foo.Bar'); " +
        "var str = new goog.foo.Bar(new goog.foo.Bar('5')); ";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithNestedNewNodes
  public void testFailWithNestedNewNodes() {
    String[] js =
        new String[] {"var goog = {}; goog.require('goog.foo.Bar'); "
            + "var str = new goog.foo.Bar(new goog.foo.Baz('5')); "};
    String warning = "'goog.foo.Baz' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithLocalFunctions
  public void testPassWithLocalFunctions() {
    String js =
        " function tempCtor() {}; var foo = new tempCtor();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithLocalVariables
  public void testPassWithLocalVariables() {
    String js =
        " var nodeCreator = function() {};"
            + "var newNode = new nodeCreator();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithLocalVariableInMoreThanOneFile
  public void testFailWithLocalVariableInMoreThanOneFile() {
    
    
    String localVar =
        " function tempCtor() {}" +
        "function baz(){" + "  function tempCtor() {}; "
            + "var foo = new tempCtor();}";
    String[] js = new String[] {localVar, " var foo = new tempCtor();"};
    String warning = "'tempCtor' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNewNodesMetaTraditionalFunctionForm
  public void testNewNodesMetaTraditionalFunctionForm() {
    
    
    
    String js =
        " function Bar(){}; "
            + "Bar.prototype.bar = function(){ return new Bar();};";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNewNodesMeta
  public void testNewNodesMeta() {
    String js =
        "var goog = {};" +
        "goog.ui.Option = function(){};"
            + "goog.ui.Option.optionDecorator = function(){"
            + "  return new goog.ui.Option(); };";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testShouldWarnWhenInstantiatingObjectsDefinedInGlobalScope
  public void testShouldWarnWhenInstantiatingObjectsDefinedInGlobalScope() {
    
    
    String good =
        " function Bar(){}; "
            + "Bar.prototype.bar = function(){return new Bar();};";
    String bad = " function Foo(){ var bar = new Bar();}";
    String[] js = new String[] {good, bad};
    String warning = "'Bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testShouldWarnWhenInstantiatingGlobalClassesFromGlobalScope
  public void testShouldWarnWhenInstantiatingGlobalClassesFromGlobalScope() {
    
    
    String good =
      " function Baz(){}; "
          + "Baz.prototype.bar = function(){return new Baz();};";
    String bad = "var baz = new Baz()";
    String[] js = new String[] {good, bad};
    String warning = "'Baz' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testIgnoresNativeObject
  public void testIgnoresNativeObject() {
    String externs = " function String(val) {}";
    String js = "var str = new String('4');";
    test(externs, js, js, null, null);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNewNodesWithMoreThanOneFile
  public void testNewNodesWithMoreThanOneFile() {
    
    String[] js = new String[] {
        "var goog = {};" +
        " function Bar() {}" +
        "goog.require('Bar');",
        "var bar = new Bar();"};
    String warning = "'Bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testPassWithoutWarningsAndMultipleFiles
  public void testPassWithoutWarningsAndMultipleFiles() {
    String[] js = new String[] {
        "var goog = {};" +
        "goog.require('Foo'); var foo = new Foo();",
        "goog.require('Bar'); var bar = new Bar();"};
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testFailWithWarningsAndMultipleFiles
  public void testFailWithWarningsAndMultipleFiles() {
    
    String[] js = new String[] {
        "var goog = {};" +
        " function Bar() {}" +
        "goog.require('Bar');",
        "var bar = new Bar();"};
    String warning = "'Bar' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testCanStillCallNumberWithoutNewOperator
  public void testCanStillCallNumberWithoutNewOperator() {
    String externs = " function Number(opt_value) {}";
    String js = "var n = Number('42');";
    test(externs, js, js, null, null);
    js = "var n = Number();";
    test(externs, js, js, null, null);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testRequiresAreCaughtBeforeProcessed
  public void testRequiresAreCaughtBeforeProcessed() {
    String js = "var foo = {}; var bar = new foo.bar.goo();";
    JSSourceFile input = JSSourceFile.fromCode("foo.js", js);
    Compiler compiler = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkRequires = CheckLevel.WARNING;
    opts.closurePass = true;

    Result result = compiler.compile(new JSSourceFile[] {},
        new JSSourceFile[] {input}, opts);
    JSError[] warnings = result.warnings;
    assertNotNull(warnings);
    assertTrue(warnings.length > 0);

    String expectation = "'foo.bar.goo' used but not goog.require'd";

    for (JSError warning : warnings) {
      if (expectation.equals(warning.description)) {
        return;
      }
    }

    fail("Could not find the following warning:" + expectation);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testNoWarningsForThisConstructor
  public void testNoWarningsForThisConstructor() {
    String js =
      "var goog = {};" +
      "goog.Foo = function() {};" +
      "goog.Foo.bar = function() {" +
      "  return new this.constructor; " +
      "};";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testBug2062487
  public void testBug2062487() {
    testSame(
      "var goog = {};" +
      "goog.Foo = function() {" +
      "   this.x_ = function() {};" +
      "  this.y_ = new this.x_();" +
      "};");
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testIgnoreDuplicateWarningsForSingleClasses
  public void testIgnoreDuplicateWarningsForSingleClasses(){
    
    String[] js = new String[]{
      "var goog = {};" +
      "goog.Foo = function() {};" +
      "goog.Foo.bar = function(){" +
      "  var first = new goog.Forgot();" +
      "  var second = new goog.Forgot();" +
      "};"};
    String warning = "'goog.Forgot' used but not goog.require'd";
    test(js, js, null, MISSING_REQUIRE_WARNING, warning);
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testGlobalScope
  public void testGlobalScope() {
    test("var f = function(){}", "function f(){}");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testLocalScope1
  public void testLocalScope1() {
    test("function f(){ var x = function(){}; x() }",
         "function f(){ function x(){} x() }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testLocalScope2
  public void testLocalScope2() {
    test("function f(){ var x = function(){}; return x }",
         "function f(){ function x(){} return x }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testVarNotImmediatelyBelowScriptOrBlock1
  public void testVarNotImmediatelyBelowScriptOrBlock1() {
    testSame("if (x) var f = function(){}");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testVarNotImmediatelyBelowScriptOrBlock2
  public void testVarNotImmediatelyBelowScriptOrBlock2() {
    testSame("var x = 1;" +
             "if (x == 1) {" +
             "  var f = function () { alert('b')}" +
             "} else {" +
             "  f = function() { alert('c')}" +
             "}" +
             "f();");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testVarNotImmediatelyBelowScriptOrBlock3
  public void testVarNotImmediatelyBelowScriptOrBlock3() {
    testSame("var x = 1; if (x) {var f = function(){return x}; f(); x--;}");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testMultipleVar
  public void testMultipleVar() {
    test("var f = function(){}; var g = f", "function f(){} var g = f");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testMultipleVar2
  public void testMultipleVar2() {
    test("var f = function(){}; var g = f; var h = function(){}",
         "function f(){}var g = f;function h(){}");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testBothScopes
  public void testBothScopes() {
    test("var x = function() { var y = function(){} }",
         "function x() { function y(){} }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testLocalScopeOnly1
  public void testLocalScopeOnly1() {
    test("if (x) var f = function(){ var g = function(){} }",
         "if (x) var f = function(){ function g(){} }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testLocalScopeOnly2
  public void testLocalScopeOnly2() {
    test("if (x) var f = function(){ var g = function(){} };",
         "if (x) var f = function(){ function g(){} }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testReturn
  public void testReturn() {
    test("var f = function(x){return 2*x}; var g = f(2);",
         "function f(x){return 2*x} var g = f(2)");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testAlert
  public void testAlert() {
    test("var x = 1; var f = function(){alert(x)};",
         "var x = 1; function f(){alert(x)}");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testRecursiveInternal1
  public void testRecursiveInternal1() {
    testSame("var f = function foo() { foo() }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testRecursiveInternal2
  public void testRecursiveInternal2() {
    testSame("var f = function foo() { function g(){foo()} g() }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testRecursiveExternal1
  public void testRecursiveExternal1() {
    test("var f = function foo() { f() }",
         "function f() { f() }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testRecursiveExternal2
  public void testRecursiveExternal2() {
    test("var f = function foo() { function g(){f()} g() }",
         "function f() { function g(){f()} g() }");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testConstantFunction1
  public void testConstantFunction1() {
    test("var FOO = function(){};FOO()",
         "function FOO(){}FOO()");
  }

// com.google.javascript.jscomp.CollapseAnonymousFunctionsTest::testInnerFunction1
  public void testInnerFunction1() {
    test(
        "function f() { " +
        "  var x = 3; var y = function() { return 4; }; return x + y();" +
        "}",
        "function f() { " +
        "  function y() { return 4; } var x = 3; return x + y();" +
        "}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCollapse
  public void testCollapse() {
    test("var a = {}; a.b = {}; var c = a.b;",
         "var a$b = {}; var c = a$b");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testMultiLevelCollapse
  public void testMultiLevelCollapse() {
    test("var a = {}; a.b = {}; a.b.c = {}; var d = a.b.c;",
         "var a$b$c = {}; var d = a$b$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDecrement
  public void testDecrement() {
    test("var a = {}; a.b = 5; a.b--; a.b = 5",
         "var a$b = 5; a$b--; a$b = 5");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testIncrement
  public void testIncrement() {
    test("var a = {}; a.b = 5; a.b++; a.b = 5",
         "var a$b = 5; a$b++; a$b = 5");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclaration
  public void testObjLitDeclaration() {
    test("var a = {b: {}, c: {}}; var d = a.b; var e = a.c",
         "var a$b = {}; var a$c = {}; var d = a$b; var e = a$c");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithGet1
  public void testObjLitDeclarationWithGet1() {
    testSame("var a = {get b(){}};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithGet2
  public void testObjLitDeclarationWithGet2() {
    test("var a = {b: {}, get c(){}}; var d = a.b; var e = a.c",
         "var a$b = {};var a = {get c(){}};var d = a$b; var e = a.c");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithGet3
  public void testObjLitDeclarationWithGet3() {
    test("var a = {b: {get c() { return 3; }}};",
         "var a$b = {get c() { return 3; }};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithSet1
  public void testObjLitDeclarationWithSet1() {
    testSame("var a = {set b(a){}};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithSet2
  public void testObjLitDeclarationWithSet2() {
    test("var a = {b: {}, set c(a){}}; var d = a.b; var e = a.c",
         "var a$b = {};var a = {set c(a){}};var d = a$b; var e = a.c");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithSet3
  public void testObjLitDeclarationWithSet3() {
    test("var a = {b: {set c(d) {}}};",
         "var a$b = {set c(d) {}};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithGetAndSet1
  public void testObjLitDeclarationWithGetAndSet1() {
    test("var a = {b: {get c() { return 3; },set c(d) {}}};",
         "var a$b = {get c() { return 3; },set c(d) {}};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationWithDuplicateKeys
  public void testObjLitDeclarationWithDuplicateKeys() {
    test("var a = {b: 0, b: 1}; var c = a.b;",
         "var a$b = 0; var a$b = 1; var c = a$b;",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignmentDepth1
  public void testObjLitAssignmentDepth1() {
    test("var a = {b: {}, c: {}}; var d = a.b; var e = a.c",
         "var a$b = {}; var a$c = {}; var d = a$b; var e = a$c");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignmentDepth2
  public void testObjLitAssignmentDepth2() {
    test("var a = {}; a.b = {c: {}, d: {}}; var e = a.b.c; var f = a.b.d",
         "var a$b$c = {}; var a$b$d = {}; var e = a$b$c; var f = a$b$d");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignmentDepth3
  public void testObjLitAssignmentDepth3() {
    test("var a = {}; a.b = {}; a.b.c = {d: 1, e: 2}; var f = a.b.c.d",
         "var a$b$c$d = 1; var a$b$c$e = 2; var f = a$b$c$d");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignmentDepth4
  public void testObjLitAssignmentDepth4() {
    test("var a = {}; a.b = {}; a.b.c = {}; a.b.c.d = {e: 1, f: 2}; " +
         "var g = a.b.c.d.e",
         "var a$b$c$d$e = 1; var a$b$c$d$f = 2; var g = a$b$c$d$e");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectDeclaredToPreserveItsPreviousValue1
  public void testGlobalObjectDeclaredToPreserveItsPreviousValue1() {
    test("var a = a ? a : {}; a.c = 1;",
         "var a = a ? a : {}; var a$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectDeclaredToPreserveItsPreviousValue2
  public void testGlobalObjectDeclaredToPreserveItsPreviousValue2() {
    test("var a = a || {}; a.c = 1;",
         "var a = a || {}; var a$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectDeclaredToPreserveItsPreviousValue3
  public void testGlobalObjectDeclaredToPreserveItsPreviousValue3() {
    test("var a = a || {get b() {}}; a.c = 1;",
         "var a = a || {get b() {}}; var a$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_1
  public void testGlobalObjectNameInBooleanExpressionDepth1_1() {
    test("var a = {b: 0}; a.c = 1; if (a) x();",
         "var a$b = 0; var a = {}; var a$c = 1; if (a) x();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_2
  public void testGlobalObjectNameInBooleanExpressionDepth1_2() {
    test("var a = {b: 0}; a.c = 1; if (!(a && a.c)) x();",
         "var a$b = 0; var a = {}; var a$c = 1; if (!(a && a$c)) x();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_3
  public void testGlobalObjectNameInBooleanExpressionDepth1_3() {
    test("var a = {b: 0}; a.c = 1; while (a || a.c) x();",
         "var a$b = 0; var a = {}; var a$c = 1; while (a || a$c) x();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_4
  public void testGlobalObjectNameInBooleanExpressionDepth1_4() {
    testSame("var a = {}; a.c = 1; var d = a || {}; a.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_5
  public void testGlobalObjectNameInBooleanExpressionDepth1_5() {
    testSame("var a = {}; a.c = 1; var d = a.c || a; a.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth1_6
  public void testGlobalObjectNameInBooleanExpressionDepth1_6() {
    test("var a = {b: 0}; a.c = 1; var d = !(a.c || a); a.c;",
         "var a$b = 0; var a = {}; var a$c = 1; var d = !(a$c || a); a$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth2
  public void testGlobalObjectNameInBooleanExpressionDepth2() {
    test("var a = {b: {}}; a.b.c = 1; if (a.b) x(a.b.c);",
         "var a$b = {}; var a$b$c = 1; if (a$b) x(a$b$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalObjectNameInBooleanExpressionDepth3
  public void testGlobalObjectNameInBooleanExpressionDepth3() {
    
    
    
    
    
    test("var a = {}; a.b = {};  a.b.c = function(){};" +
         " a.b.z = 1; var d = a.b && a.b.c;",
         "var a$b = {}; var a$b$c = function(){};" +
         " a$b.z = 1; var d = a$b && a$b$c;", null,
         CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalFunctionNameInBooleanExpressionDepth1
  public void testGlobalFunctionNameInBooleanExpressionDepth1() {
    test("function a() {} a.c = 1; if (a) x(a.c);",
         "function a() {} var a$c = 1; if (a) x(a$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalFunctionNameInBooleanExpressionDepth2
  public void testGlobalFunctionNameInBooleanExpressionDepth2() {
    test("var a = {b: function(){}}; a.b.c = 1; if (a.b) x(a.b.c);",
         "var a$b = function(){}; var a$b$c = 1; if (a$b) x(a$b$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForObjectDepth1_1
  public void testAliasCreatedForObjectDepth1_1() {
    
    
    testSame("var a = {b: 0}; var c = a; c.b = 1; a.b == c.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForObjectDepth1_2
  public void testAliasCreatedForObjectDepth1_2() {
    testSame("var a = {b: 0}; f(a); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForObjectDepth1_3
  public void testAliasCreatedForObjectDepth1_3() {
    testSame("var a = {b: 0}; new f(a); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForObjectDepth2_1
  public void testAliasCreatedForObjectDepth2_1() {
    test("var a = {}; a.b = {c: 0}; var d = a.b; a.b.c == d.c;",
         "var a$b = {c: 0}; var d = a$b; a$b.c == d.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForObjectDepth2_2
  public void testAliasCreatedForObjectDepth2_2() {
    test("var a = {}; a.b = {c: 0}; for (var p in a.b) { e(a.b[p]); }",
         "var a$b = {c: 0}; for (var p in a$b) { e(a$b[p]); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth1_1
  public void testAliasCreatedForEnumDepth1_1() {
    
    
    test(" var a = {b: 0}; var c = a; c.b = 1; a.b != c.b;",
         "var a$b = 0; var a = {b: a$b}; var c = a; c.b = 1; a$b != c.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth1_2
  public void testAliasCreatedForEnumDepth1_2() {
    test(" var a = {b: 0}; f(a); a.b;",
         "var a$b = 0; var a = {b: a$b}; f(a); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth1_3
  public void testAliasCreatedForEnumDepth1_3() {
    test(" var a = {b: 0}; new f(a); a.b;",
         "var a$b = 0; var a = {b: a$b}; new f(a); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth1_4
  public void testAliasCreatedForEnumDepth1_4() {
    test(" var a = {b: 0}; for (var p in a) { f(a[p]); }",
         "var a$b = 0; var a = {b: a$b}; for (var p in a) { f(a[p]); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth2_1
  public void testAliasCreatedForEnumDepth2_1() {
    test("var a = {};  a.b = {c: 0};" +
         "var d = a.b; d.c = 1; a.b.c != d.c;",
         "var a$b$c = 0; var a$b = {c: a$b$c};" +
         "var d = a$b; d.c = 1; a$b$c != d.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth2_2
  public void testAliasCreatedForEnumDepth2_2() {
    test("var a = {};  a.b = {c: 0};" +
         "for (var p in a.b) { f(a.b[p]); }",
         "var a$b$c = 0; var a$b = {c: a$b$c};" +
         "for (var p in a$b) { f(a$b[p]); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumDepth2_3
  public void testAliasCreatedForEnumDepth2_3() {
    test("var a = {}; var d = a;  a.b = {c: 0};" +
         "for (var p in a.b) { f(a.b[p]); }",
         "var a = {}; var d = a; var a$b$c = 0; var a$b = {c: a$b$c};" +
         "for (var p in a$b) { f(a$b[p]); }",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumOfObjects
  public void testAliasCreatedForEnumOfObjects() {
    test("var a = {}; " +
         " a.b = {c: {d: 1}}; a.b.c;" +
         "searchEnum(a.b);",
         "var a$b$c = {d: 1};var a$b = {c: a$b$c}; a$b$c; " +
         "searchEnum(a$b)");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForEnumOfObjects2
  public void testAliasCreatedForEnumOfObjects2() {
    test("var a = {}; " +
         " a.b = {c: {d: 1}}; a.b.c.d;" +
         "searchEnum(a.b);",
         "var a$b$c = {d: 1};var a$b = {c: a$b$c}; a$b$c.d; " +
         "searchEnum(a$b)");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForPropertyOfEnumOfObjects
  public void testAliasCreatedForPropertyOfEnumOfObjects() {
    test("var a = {}; " +
         " a.b = {c: {d: 1}}; a.b.c;" +
         "searchEnum(a.b.c);",
         "var a$b$c = {d: 1}; a$b$c; searchEnum(a$b$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForPropertyOfEnumOfObjects2
  public void testAliasCreatedForPropertyOfEnumOfObjects2() {
    test("var a = {}; " +
         " a.b = {c: {d: 1}}; a.b.c.d;" +
         "searchEnum(a.b.c);",
         "var a$b$c = {d: 1}; a$b$c.d; searchEnum(a$b$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testMisusedEnumTag
  public void testMisusedEnumTag() {
    testSame("var a = {}; var d = a; a.b = function() {};" +
             " a.b.c = 0; a.b.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testMisusedConstructorTag
  public void testMisusedConstructorTag() {
    testSame("var a = {}; var d = a; a.b = function() {};" +
             " a.b.c = 0; a.b.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth1_1
  public void testAliasCreatedForFunctionDepth1_1() {
    testSame("var a = function(){}; a.b = 1; var c = a; c.b = 2; a.b != c.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForCtorDepth1_1
  public void testAliasCreatedForCtorDepth1_1() {
    
    
    
    
    
    
    test(" var a = function(){}; a.b = 1; " +
         "var c = a; c.b = 2; a.b != c.b;",
         "var a = function(){}; var a$b = 1; var c = a; c.b = 2; a$b != c.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth1_2
  public void testAliasCreatedForFunctionDepth1_2() {
    testSame("var a = function(){}; a.b = 1; f(a); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForCtorDepth1_2
  public void testAliasCreatedForCtorDepth1_2() {
    test(" var a = function(){}; a.b = 1; f(a); a.b;",
         "var a = function(){}; var a$b = 1; f(a); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth1_3
  public void testAliasCreatedForFunctionDepth1_3() {
    testSame("var a = function(){}; a.b = 1; new f(a); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForCtorDepth1_3
  public void testAliasCreatedForCtorDepth1_3() {
    test(" var a = function(){}; a.b = 1; new f(a); a.b;",
         "var a = function(){}; var a$b = 1; new f(a); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForFunctionDepth2
  public void testAliasCreatedForFunctionDepth2() {
    test(
        "var a = {}; a.b = function() {}; a.b.c = 1; var d = a.b;" +
        "a.b.c != d.c;",
        "var a$b = function() {}; a$b.c = 1; var d = a$b;" +
        "a$b.c != d.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForCtorDepth2
  public void testAliasCreatedForCtorDepth2() {
    test("var a = {};  a.b = function() {}; " +
         "a.b.c = 1; var d = a.b;" +
         "a.b.c != d.c;",
         "var a$b = function() {}; var a$b$c = 1; var d = a$b;" +
         "a$b$c != d.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth1_1
  public void testAliasCreatedForClassDepth1_1() {
    
    
    test("var a = {};  a.b = function(){};" +
         "var c = a; c.b = 0; a.b != c.b;",
         "var a = {}; var a$b = function(){};" +
         "var c = a; c.b = 0; a$b != c.b;", null,
         CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth1_2
  public void testAliasCreatedForClassDepth1_2() {
    test("var a = {};  a.b = function(){}; f(a); a.b;",
         "var a = {}; var a$b = function(){}; f(a); a$b;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth1_3
  public void testAliasCreatedForClassDepth1_3() {
    test("var a = {};  a.b = function(){}; new f(a); a.b;",
         "var a = {}; var a$b = function(){}; new f(a); a$b;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth2_1
  public void testAliasCreatedForClassDepth2_1() {
    test("var a = {}; a.b = {};  a.b.c = function(){};" +
         "var d = a.b; a.b.c != d.c;",
         "var a$b = {}; var a$b$c = function(){};" +
         "var d = a$b; a$b$c != d.c;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth2_2
  public void testAliasCreatedForClassDepth2_2() {
    test("var a = {}; a.b = {};  a.b.c = function(){};" +
         "f(a.b); a.b.c;",
         "var a$b = {}; var a$b$c = function(){}; f(a$b); a$b$c;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassDepth2_3
  public void testAliasCreatedForClassDepth2_3() {
    test("var a = {}; a.b = {};  a.b.c = function(){};" +
         "new f(a.b); a.b.c;",
         "var a$b = {}; var a$b$c = function(){}; new f(a$b); a$b$c;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasCreatedForClassProperty
  public void testAliasCreatedForClassProperty() {
    test("var a = {};  a.b = function(){};" +
         "a.b.c = {d: 3}; new f(a.b.c); a.b.c.d;",
         "var a$b = function(){}; var a$b$c = {d:3}; new f(a$b$c); a$b$c.d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNestedObjLit
  public void testNestedObjLit() {
    test("var a = {}; a.b = {f: 0, c: {d: 1}}; var e = a.b.c.d",
         "var a$b$f = 0; var a$b$c$d = 1; var e = a$b$c$d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDeclarationUsedInSameVarList
  public void testObjLitDeclarationUsedInSameVarList() {
    
    
    test("var a = {b: {}, c: {}}; var d = a.b; var e = a.c;",
         "var a$b = {}; var a$c = {}; var d = a$b; var e = a$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropGetInsideAnObjLit
  public void testPropGetInsideAnObjLit() {
    test("var x = {}; x.y = 1; var a = {}; a.b = {c: x.y}",
         "var x$y = 1; var a$b$c = x$y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitWithQuotedKeyThatDoesNotGetRead
  public void testObjLitWithQuotedKeyThatDoesNotGetRead() {
    test("var a = {}; a.b = {c: 0, 'd': 1}; var e = a.b.c;",
         "var a$b$c = 0; var a$b$d = 1; var e = a$b$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitWithQuotedKeyThatGetsRead
  public void testObjLitWithQuotedKeyThatGetsRead() {
    test("var a = {}; a.b = {c: 0, 'd': 1}; var e = a.b['d'];",
         "var a$b = {c: 0, 'd': 1}; var e = a$b['d'];");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionWithQuotedPropertyThatDoesNotGetRead
  public void testFunctionWithQuotedPropertyThatDoesNotGetRead() {
    test("var a = {}; a.b = function() {}; a.b['d'] = 1;",
         "var a$b = function() {}; a$b['d'] = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionWithQuotedPropertyThatGetsRead
  public void testFunctionWithQuotedPropertyThatGetsRead() {
    test("var a = {}; a.b = function() {}; a.b['d'] = 1; f(a.b['d']);",
         "var a$b = function() {}; a$b['d'] = 1; f(a$b['d']);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignedToMultipleNames1
  public void testObjLitAssignedToMultipleNames1() {
    
    testSame("var a = b = {c: 0, d: 1}; var e = a.c; var f = b.d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignedToMultipleNames2
  public void testObjLitAssignedToMultipleNames2() {
    testSame("a = b = {c: 0, d: 1}; var e = a.c; var f = b.d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitRedefinedInGlobalScope
  public void testObjLitRedefinedInGlobalScope() {
    testSame("a = {b: 0}; a = {c: 1}; var d = a.b; var e = a.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitRedefinedInLocalScope
  public void testObjLitRedefinedInLocalScope() {
    test("var a = {}; a.b = {c: 0}; function d() { a.b = {c: 1}; } e(a.b.c);",
         "var a$b = {c: 0}; function d() { a$b = {c: 1}; } e(a$b.c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignedInTernaryExpression1
  public void testObjLitAssignedInTernaryExpression1() {
    testSame("a = x ? {b: 0} : d; var c = a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitAssignedInTernaryExpression2
  public void testObjLitAssignedInTernaryExpression2() {
    testSame("a = x ? {b: 0} : {b: 1}; var c = a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalVarSetToObjLitConditionally1
  public void testGlobalVarSetToObjLitConditionally1() {
    testSame("var a; if (x) a = {b: 0}; var c = x ? a.b : 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalVarSetToObjLitConditionally1b
  public void testGlobalVarSetToObjLitConditionally1b() {
    test("if (x) var a = {b: 0}; var c = x ? a.b : 0;",
         "if (x) var a$b = 0; var c = x ? a$b : 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalVarSetToObjLitConditionally2
  public void testGlobalVarSetToObjLitConditionally2() {
    test("if (x) var a = {b: 0}; var c = a.b; var d = a.c;",
         "if (x){ var a$b = 0; var a = {}; }var c = a$b; var d = a.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalVarSetToObjLitConditionally3
  public void testGlobalVarSetToObjLitConditionally3() {
    testSame("var a; if (x) a = {b: 0}; else a = {b: 1}; var c = a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjectPropertySetToObjLitConditionally
  public void testObjectPropertySetToObjLitConditionally() {
    test("var a = {}; if (x) a.b = {c: 0}; var d = a.b ? a.b.c : 0;",
         "if (x){ var a$b$c = 0; var a$b = {} } var d = a$b ? a$b$c : 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionPropertySetToObjLitConditionally
  public void testFunctionPropertySetToObjLitConditionally() {
    test("function a() {} if (x) a.b = {c: 0}; var d = a.b ? a.b.c : 0;",
         "function a() {} if (x){ var a$b$c = 0; var a$b = {} }" +
         "var d = a$b ? a$b$c : 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPrototypePropertySetToAnObjectLiteral
  public void testPrototypePropertySetToAnObjectLiteral() {
    test("var a = {b: function(){}}; a.b.prototype.c = {d: 0};",
         "var a$b = function(){}; a$b.prototype.c = {d: 0};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjectPropertyResetInLocalScope
  public void testObjectPropertyResetInLocalScope() {
    test("var z = {}; z.a = 0; function f() {z.a = 5; return z.a}",
         "var z$a = 0; function f() {z$a = 5; return z$a}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionPropertyResetInLocalScope
  public void testFunctionPropertyResetInLocalScope() {
    test("function z() {} z.a = 0; function f() {z.a = 5; return z.a}",
         "function z() {} var z$a = 0; function f() {z$a = 5; return z$a}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNamespaceResetInGlobalScope1
  public void testNamespaceResetInGlobalScope1() {
    test("var a = {}; a.b = function() {}; a = {};",
         "var a = {}; var a$b = function() {}; a = {};",
         null, CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNamespaceResetInGlobalScope2
  public void testNamespaceResetInGlobalScope2() {
    test("var a = {}; a = {}; a.b = function() {};",
         "var a = {}; a = {}; var a$b = function() {};",
         null, CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNamespaceResetInLocalScope1
  public void testNamespaceResetInLocalScope1() {
    test("var a = {}; a.b = function() {};" +
         " function f() { a = {}; }",
         "var a = {};var a$b = function() {};" +
         " function f() { a = {}; }",
         null, CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNamespaceResetInLocalScope2
  public void testNamespaceResetInLocalScope2() {
    test("var a = {}; function f() { a = {}; }" +
         " a.b = function() {};",
         "var a = {}; function f() { a = {}; }" +
         " var a$b = function() {};",
         null, CollapseProperties.NAMESPACE_REDEFINED_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNamespaceDefinedInLocalScope
  public void testNamespaceDefinedInLocalScope() {
    test("var a = {}; (function() { a.b = {}; })();" +
         " a.b.c = function() {};",
         "var a$b; (function() { a$b = {}; })(); var a$b$c = function() {};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToObjectInLocalScopeDepth1
  public void testAddPropertyToObjectInLocalScopeDepth1() {
    test("var a = {b: 0}; function f() { a.c = 5; return a.c; }",
         "var a$b = 0; var a$c; function f() { a$c = 5; return a$c; }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToObjectInLocalScopeDepth2
  public void testAddPropertyToObjectInLocalScopeDepth2() {
    test("var a = {}; a.b = {}; (function() {a.b.c = 0;})(); x = a.b.c;",
         "var a$b$c; (function() {a$b$c = 0;})(); x = a$b$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToFunctionInLocalScopeDepth1
  public void testAddPropertyToFunctionInLocalScopeDepth1() {
    test("function a() {} function f() { a.c = 5; return a.c; }",
         "function a() {} var a$c; function f() { a$c = 5; return a$c; }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToFunctionInLocalScopeDepth2
  public void testAddPropertyToFunctionInLocalScopeDepth2() {
    test("var a = {}; a.b = function() {}; function f() {a.b.c = 0;}",
         "var a$b = function() {}; var a$b$c; function f() {a$b$c = 0;}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleObjectInLocalScopeDepth1
  public void testAddPropertyToUncollapsibleObjectInLocalScopeDepth1() {
    testSame("var a = {}; var c = a; (function() {a.b = 0;})(); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleFunctionInLocalScopeDepth1
  public void testAddPropertyToUncollapsibleFunctionInLocalScopeDepth1() {
    testSame("function a() {} var c = a; (function() {a.b = 0;})(); a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleNamedCtorInLocalScopeDepth1
  public void testAddPropertyToUncollapsibleNamedCtorInLocalScopeDepth1() {
    testSame(
          " function a() {} var a$b; var c = a; " +
          "(function() {a$b = 0;})(); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleCtorInLocalScopeDepth1
  public void testAddPropertyToUncollapsibleCtorInLocalScopeDepth1() {
    test(" var a = function() {}; var c = a; " +
         "(function() {a.b = 0;})(); a.b;",
         "var a = function() {}; var a$b; " +
         "var c = a; (function() {a$b = 0;})(); a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleObjectInLocalScopeDepth2
  public void testAddPropertyToUncollapsibleObjectInLocalScopeDepth2() {
    test("var a = {}; a.b = {}; var d = a.b;" +
         "(function() {a.b.c = 0;})(); a.b.c;",
         "var a$b = {}; var d = a$b;" +
         "(function() {a$b.c = 0;})(); a$b.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleFunctionInLocalScopeDepth2
  public void testAddPropertyToUncollapsibleFunctionInLocalScopeDepth2() {
    test("var a = {}; a.b = function (){}; var d = a.b;" +
         "(function() {a.b.c = 0;})(); a.b.c;",
         "var a$b = function (){}; var d = a$b;" +
         "(function() {a$b.c = 0;})(); a$b.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToUncollapsibleCtorInLocalScopeDepth2
  public void testAddPropertyToUncollapsibleCtorInLocalScopeDepth2() {
    test("var a = {};  a.b = function (){}; var d = a.b;" +
         "(function() {a.b.c = 0;})(); a.b.c;",
         "var a$b = function (){}; var a$b$c; var d = a$b;" +
         "(function() {a$b$c = 0;})(); a$b$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOfChildFuncOfUncollapsibleObjectDepth1
  public void testPropertyOfChildFuncOfUncollapsibleObjectDepth1() {
    testSame("var a = {}; var c = a; a.b = function (){}; a.b.x = 0; a.b.x;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOfChildFuncOfUncollapsibleObjectDepth2
  public void testPropertyOfChildFuncOfUncollapsibleObjectDepth2() {
    test("var a = {}; a.b = {}; var c = a.b;" +
         "a.b.c = function (){}; a.b.c.x = 0; a.b.c.x;",
         "var a$b = {}; var c = a$b;" +
         "a$b.c = function (){}; a$b.c.x = 0; a$b.c.x;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToChildFuncOfUncollapsibleObjectInLocalScope
  public void testAddPropertyToChildFuncOfUncollapsibleObjectInLocalScope() {
    testSame("var a = {}; a.b = function (){}; a.b.x = 0;" +
             "var c = a; (function() {a.b.y = 1;})(); a.b.x; a.b.y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToChildTypeOfUncollapsibleObjectInLocalScope
  public void testAddPropertyToChildTypeOfUncollapsibleObjectInLocalScope() {
    test("var a = {};  a.b = function (){}; a.b.x = 0;" +
         "var c = a; (function() {a.b.y = 1;})(); a.b.x; a.b.y;",
         "var a = {}; var a$b = function (){}; var a$b$y; var a$b$x = 0;" +
         "var c = a; (function() {a$b$y = 1;})(); a$b$x; a$b$y;",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToChildOfUncollapsibleFunctionInLocalScope
  public void testAddPropertyToChildOfUncollapsibleFunctionInLocalScope() {
    testSame(
        "function a() {} a.b = {x: 0}; var c = a;" +
        "(function() {a.b.y = 0;})(); a.b.y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAddPropertyToChildOfUncollapsibleCtorInLocalScope
  public void testAddPropertyToChildOfUncollapsibleCtorInLocalScope() {
    test(" var a = function() {}; a.b = {x: 0}; var c = a;" +
         "(function() {a.b.y = 0;})(); a.b.y;",
         "var a = function() {}; var a$b$x = 0; var a$b$y; var c = a;" +
         "(function() {a$b$y = 0;})(); a$b$y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testResetObjectPropertyInLocalScope
  public void testResetObjectPropertyInLocalScope() {
    test("var a = {b: 0}; a.c = 1; function f() { a.c = 5; }",
         "var a$b = 0; var a$c = 1; function f() { a$c = 5; }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testResetFunctionPropertyInLocalScope
  public void testResetFunctionPropertyInLocalScope() {
    test("function a() {}; a.c = 1; function f() { a.c = 5; }",
         "function a() {}; var a$c = 1; function f() { a$c = 5; }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalNameReferencedInLocalScopeBeforeDefined1
  public void testGlobalNameReferencedInLocalScopeBeforeDefined1() {
    
    
    
    
    test("var a = {b: 0}; function f() { a.c = 5; } a.c = 1;",
         "var a$b = 0; function f() { a$c = 5; } var a$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalNameReferencedInLocalScopeBeforeDefined2
  public void testGlobalNameReferencedInLocalScopeBeforeDefined2() {
    test("var a = {b: 0}; function f() { return a.c; } a.c = 1;",
         "var a$b = 0; function f() { return a$c; } var a$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTwiceDefinedGlobalNameDepth1_1
  public void testTwiceDefinedGlobalNameDepth1_1() {
    testSame("var a = {}; function f() { a.b(); }" +
             "a = function() {}; a.b = function() {};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTwiceDefinedGlobalNameDepth1_2
  public void testTwiceDefinedGlobalNameDepth1_2() {
    testSame("var a = {};  a = function() {};" +
             "a.b = {}; a.b.c = 0; function f() { a.b.d = 1; }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTwiceDefinedGlobalNameDepth2
  public void testTwiceDefinedGlobalNameDepth2() {
    test("var a = {}; a.b = {}; function f() { a.b.c(); }" +
         "a.b = function() {}; a.b.c = function() {};",
         "var a$b = {}; function f() { a$b.c(); }" +
         "a$b = function() {}; a$b.c = function() {};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionCallDepth1
  public void testFunctionCallDepth1() {
    test("var a = {}; a.b = function(){}; var c = a.b();",
         "var a$b = function(){}; var c = a$b()");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionCallDepth2
  public void testFunctionCallDepth2() {
    test("var a = {}; a.b = {}; a.b.c = function(){}; a.b.c();",
         "var a$b$c = function(){}; a$b$c();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionAlias
  public void testFunctionAlias() {
    test("var a = {}; a.b = {}; a.b.c = function(){}; a.b.d = a.b.c;",
         "var a$b$c = function(){}; var a$b$d = a$b$c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCallToRedefinedFunction
  public void testCallToRedefinedFunction() {
    test("var a = {}; a.b = function(){}; a.b = function(){}; a.b();",
         "var a$b = function(){}; a$b = function(){}; a$b();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCollapsePrototypeName
  public void testCollapsePrototypeName() {
    test("var a = {}; a.b = {}; a.b.c = function(){}; " +
         "a.b.c.prototype.d = function(){}; (new a.b.c()).d();",
         "var a$b$c = function(){}; a$b$c.prototype.d = function(){}; " +
         "new a$b$c().d();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferencedPrototypeProperty
  public void testReferencedPrototypeProperty() {
    test("var a = {b: {}}; a.b.c = function(){}; a.b.c.prototype.d = {};" +
         "e = a.b.c.prototype.d;",
         "var a$b$c = function(){}; a$b$c.prototype.d = {};" +
         "e = a$b$c.prototype.d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testSetStaticAndPrototypePropertiesOnFunction
  public void testSetStaticAndPrototypePropertiesOnFunction() {
    test("var a = {}; a.b = function(){}; a.b.prototype.d = 0; a.b.c = 1;",
         "var a$b = function(){}; a$b.prototype.d = 0; var a$b$c = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReadUndefinedPropertyDepth1
  public void testReadUndefinedPropertyDepth1() {
    test("var a = {b: 0}; var c = a.d;",
         "var a$b = 0; var a = {}; var c = a.d;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReadUndefinedPropertyDepth2
  public void testReadUndefinedPropertyDepth2() {
    test("var a = {b: {c: 0}}; f(a.b.c); f(a.b.d);",
         "var a$b$c = 0; var a$b = {}; f(a$b$c); f(a$b.d);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCallUndefinedMethodOnObjLitDepth1
  public void testCallUndefinedMethodOnObjLitDepth1() {
    test("var a = {b: 0}; a.c();",
         "var a$b = 0; var a = {}; a.c();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCallUndefinedMethodOnObjLitDepth2
  public void testCallUndefinedMethodOnObjLitDepth2() {
    test("var a = {b: {}}; a.b.c = function() {}; a.b.c(); a.b.d();",
         "var a$b = {}; var a$b$c = function() {}; a$b$c(); a$b.d();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertiesOfAnUndefinedVar
  public void testPropertiesOfAnUndefinedVar() {
    testSame("a.document = d; f(a.document.innerHTML);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOfAnObjectThatIsNeitherFunctionNorObjLit
  public void testPropertyOfAnObjectThatIsNeitherFunctionNorObjLit() {
    testSame("var a = window; a.document = d; f(a.document)");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testStaticFunctionReferencingThis1
  public void testStaticFunctionReferencingThis1() {
    
    
    test("var a = {}; a.b = function() {this.c}; var d = a.b;",
         "var a$b = function() {this.c}; var d = a$b;", null, UNSAFE_THIS);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testStaticFunctionReferencingThis2
  public void testStaticFunctionReferencingThis2() {
    
    
    test("var a = {}; " +
         "a.b = function() { return function(){ return this; }; };",
         "var a$b = function() { return function(){ return this; }; };");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testStaticFunctionReferencingThis3
  public void testStaticFunctionReferencingThis3() {
    test("var a = {b: function() {this.c}};",
         "var a$b = function() { this.c };", null, UNSAFE_THIS);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testStaticFunctionReferencingThis4
  public void testStaticFunctionReferencingThis4() {
    test("var a = { b: function() {this.c}};",
         "var a$b = function() { this.c };");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPrototypeMethodReferencingThis
  public void testPrototypeMethodReferencingThis() {
    testSame("var A = function(){}; A.prototype = {b: function() {this.c}};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testConstructorReferencingThis
  public void testConstructorReferencingThis() {
    test("var a = {}; " +
         " a.b = function() { this.a = 3; };",
         "var a$b = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testSafeReferenceOfThis
  public void testSafeReferenceOfThis() {
    test("var a = {}; " +
         " a.b = function() { this.a = 3; };",
         "var a$b = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalFunctionReferenceOfThis
  public void testGlobalFunctionReferenceOfThis() {
    testSame("var a = function() { this.a = 3; };");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testFunctionGivenTwoNames
  public void testFunctionGivenTwoNames() {
    
    
    test("var f = function g() {}; f.a = 1; h(f.a);",
         "var f = function g() {}; var f$a = 1; h(f$a);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitWithUsedNumericKey
  public void testObjLitWithUsedNumericKey() {
    testSame("a = {40: {}, c: {}}; var d = a[40]; var e = a.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitWithUnusedNumericKey
  public void testObjLitWithUnusedNumericKey() {
    test("var a = {40: {}, c: {}}; var e = a.c;",
         "var a$1 = {}; var a$c = {}; var e = a$c");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitWithNonIdentifierKeys
  public void testObjLitWithNonIdentifierKeys() {
    testSame("a = {' ': 0, ',': 1}; var c = a[' '];");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments1
  public void testChainedAssignments1() {
    test("var x = {}; x.y = a = 0;",
         "var x$y = a = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments2
  public void testChainedAssignments2() {
    test("var x = {}; x.y = a = b = c();",
         "var x$y = a = b = c();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments3
  public void testChainedAssignments3() {
    test("var x = {y: 1}; a = b = x.y;",
         "var x$y = 1; a = b = x$y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments4
  public void testChainedAssignments4() {
    test("var x = {}; a = b = x.y;",
         "var x = {}; a = b = x.y;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments5
  public void testChainedAssignments5() {
    test("var x = {}; a = x.y = 0;", "var x$y; a = x$y = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments6
  public void testChainedAssignments6() {
    test("var x = {}; a = x.y = b = c();",
         "var x$y; a = x$y = b = c();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedAssignments7
  public void testChainedAssignments7() {
    test("var x = {}; a = x.y = {};  x.y.z = function() {};",
         "var x$y; a = x$y = {}; var x$y$z = function() {};",
         null, CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedVarAssignments1
  public void testChainedVarAssignments1() {
    test("var x = {y: 1}; var a = x.y = 0;",
         "var x$y = 1; var a = x$y = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedVarAssignments2
  public void testChainedVarAssignments2() {
    test("var x = {y: 1}; var a = x.y = b = 0;",
         "var x$y = 1; var a = x$y = b = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedVarAssignments3
  public void testChainedVarAssignments3() {
    test("var x = {y: {z: 1}}; var b = 0; var a = x.y.z = 1; var c = 2;",
         "var x$y$z = 1; var b = 0; var a = x$y$z = 1; var c = 2;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedVarAssignments4
  public void testChainedVarAssignments4() {
    test("var x = {}; var a = b = x.y = 0;",
         "var x$y; var a = b = x$y = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testChainedVarAssignments5
  public void testChainedVarAssignments5() {
    test("var x = {y: {}}; var a = b = x.y.z = 0;",
         "var x$y$z; var a = b = x$y$z = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPeerAndSubpropertyOfUncollapsibleProperty
  public void testPeerAndSubpropertyOfUncollapsibleProperty() {
    test("var x = {}; var a = x.y = 0; x.w = 1; x.y.z = 2;" +
         "b = x.w; c = x.y.z;",
         "var x$y; var a = x$y = 0; var x$w = 1; x$y.z = 2;" +
         "b = x$w; c = x$y.z;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testComplexAssignmentAfterInitialAssignment
  public void testComplexAssignmentAfterInitialAssignment() {
    test("var d = {}; d.e = {}; d.e.f = 0; a = b = d.e.f = 1;",
         "var d$e$f = 0; a = b = d$e$f = 1;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testRenamePrefixOfUncollapsibleProperty
  public void testRenamePrefixOfUncollapsibleProperty() {
    test("var d = {}; d.e = {}; a = b = d.e.f = 0;",
         "var d$e$f; a = b = d$e$f = 0;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNewOperator
  public void testNewOperator() {
    
    
    test("var a = {}; a.b = function() {}; a.b.c = 1; var d = new a.b();",
         "var a$b = function() {}; var a$b$c = 1; var d = new a$b();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testMethodCall
  public void testMethodCall() {
    test("var a = {}; a.b = function() {}; var d = a.b();",
         "var a$b = function() {}; var d = a$b();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjLitDefinedInLocalScopeIsLeftAlone
  public void testObjLitDefinedInLocalScopeIsLeftAlone() {
    test("var a = {}; a.b = function() {};" +
         "a.b.prototype.f_ = function() {" +
         "  var x = { p: '', q: '', r: ''}; var y = x.q;" +
         "};",
         "var a$b = function() {};" +
         "a$b.prototype.f_ = function() {" +
         "  var x = { p: '', q: '', r: ''}; var y = x.q;" +
         "};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertiesOnBothSidesOfAssignment
  public void testPropertiesOnBothSidesOfAssignment() {
    
    
    
    test("var a = {b: 0}; a.c = a.b;", "var a$b = 0; var a$c = a$b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCallOnUndefinedProperty
  public void testCallOnUndefinedProperty() {
    
    
    
    
    test("var a = {}; a.b = function(){}; a.b.inherits(x);",
         "var a$b = function(){}; a$b.inherits(x);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGetPropOnUndefinedProperty
  public void testGetPropOnUndefinedProperty() {
    
    
    
    
    test("var a = {b: function(){}}; a.b.prototype.c =" +
         "function() { a.b.superClass_.c.call(this); }",
         "var a$b = function(){}; a$b.prototype.c =" +
         "function() { a$b.superClass_.c.call(this); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias1
  public void testLocalAlias1() {
    test("var a = {b: 3}; function f() { var x = a; f(x.b); }",
         "var a$b = 3; function f() { var x = null; f(a$b); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias2
  public void testLocalAlias2() {
    test("var a = {b: 3, c: 4}; function f() { var x = a; f(x.b); f(x.c);}",
         "var a$b = 3; var a$c = 4; " +
         "function f() { var x = null; f(a$b); f(a$c);}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias3
  public void testLocalAlias3() {
    test("var a = {b: 3, c: {d: 5}}; " +
         "function f() { var x = a; f(x.b); f(x.c); f(x.c.d); }",
         "var a$b = 3; var a$c = {d: 5}; " +
         "function f() { var x = null; f(a$b); f(a$c); f(a$c.d);}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias4
  public void testLocalAlias4() {
    test("var a = {b: 3}; var c = {d: 5}; " +
         "function f() { var x = a; var y = c; f(x.b); f(y.d); }",
         "var a$b = 3; var c$d = 5; " +
         "function f() { var x = null; var y = null; f(a$b); f(c$d);}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias5
  public void testLocalAlias5() {
    test("var a = {b: {c: 5}}; " +
         "function f() { var x = a; var y = x.b; f(a.b.c); f(y.c); }",
         "var a$b$c = 5; " +
         "function f() { var x = null; var y = null; f(a$b$c); f(a$b$c);}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias6
  public void testLocalAlias6() {
    test("var a = {b: 3}; function f() { var x = a; if (x.b) { f(x.b); } }",
         "var a$b = 3; function f() { var x = null; if (a$b) { f(a$b); } }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAlias7
  public void testLocalAlias7() {
    test("var a = {b: {c: 5}}; function f() { var x = a.b; f(x.c); }",
         "var a$b$c = 5; function f() { var x = null; f(a$b$c); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalWriteToAncestor
  public void testGlobalWriteToAncestor() {
    testSame("var a = {b: 3}; function f() { var x = a; f(a.b); } a = 5;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalWriteToNonAncestor
  public void testGlobalWriteToNonAncestor() {
    test("var a = {b: 3}; function f() { var x = a; f(a.b); } a.b = 5;",
         "var a$b = 3; function f() { var x = null; f(a$b); } a$b = 5;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalWriteToAncestor
  public void testLocalWriteToAncestor() {
    testSame("var a = {b: 3}; function f() { a = 5; var x = a; f(a.b); } ");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalWriteToNonAncestor
  public void testLocalWriteToNonAncestor() {
    test("var a = {b: 3}; " +
         "function f() { a.b = 5; var x = a; f(a.b); }",
         "var a$b = 3; function f() { a$b = 5; var x = null; f(a$b); } ");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNonWellformedAlias1
  public void testNonWellformedAlias1() {
    testSame("var a = {b: 3}; function f() { f(x); var x = a; f(x.b); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNonWellformedAlias2
  public void testNonWellformedAlias2() {
    testSame("var a = {b: 3}; " +
             "function f() { if (false) { var x = a; f(x.b); } f(x); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAliasOfAncestor
  public void testLocalAliasOfAncestor() {
    testSame("var a = {b: {c: 5}}; function g() { f(a); } " +
             "function f() { var x = a.b; f(x.c); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testGlobalAliasOfAncestor
  public void testGlobalAliasOfAncestor() {
    testSame("var a = {b: {c: 5}}; var y = a; " +
             "function f() { var x = a.b; f(x.c); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAliasOfOtherName
  public void testLocalAliasOfOtherName() {
    testSame("var foo = function() { return {b: 3}; };" +
             "var a = foo(); a.b = 5; " +
             "function f() { var x = a.b; f(x); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testLocalAliasOfFunction
  public void testLocalAliasOfFunction() {
    test("var a = function() {}; a.b = 5; " +
         "function f() { var x = a.b; f(x); }",
         "var a = function() {}; var a$b = 5; " +
         "function f() { var x = null; f(a$b); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNoInlineGetpropIntoCall
  public void testNoInlineGetpropIntoCall() {
    test("var b = x; function f() { var a = b; a(); }",
         "var b = x; function f() { var a = null; b(); }");
    test("var b = {}; b.c = x; function f() { var a = b.c; a(); }",
         "var b$c = x; function f() { var a = null; b$c(); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testInlineAliasWithModifications
  public void testInlineAliasWithModifications() {
    testSame("var x = 10; function f() { var y = x; x++; alert(y)} ");
    testSame("var x = 10; function f() { var y = x; x+=1; alert(y)} ");
    test("var x = {}; x.x = 10; function f() {var y=x.x; x.x++; alert(y)}",
         "var x$x = 10; function f() {var y=x$x; x$x++; alert(y)}");
    test("var x = {}; x.x = 10; function f() {var y=x.x; x.x+=1; alert(y)}",
         "var x$x = 10; function f() {var y=x$x; x$x+=1; alert(y)}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCollapsePropertyOnExternType
  public void testCollapsePropertyOnExternType() {
    collapsePropertiesOnExternTypes = true;
    test("String.myFunc = function() {}; String.myFunc();",
         "var String$myFunc = function() {}; String$myFunc()");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCollapseForEachWithoutExterns
  public void testCollapseForEachWithoutExterns() {
    collapsePropertiesOnExternTypes = true;
    test("function Array(){};\n",
         "if (!Array.forEach) {\n" +
         "  Array.forEach = function() {};\n" +
         "}",
         "if (!Array$forEach) {\n" +
         "  var Array$forEach = function() {};\n" +
         "}", null, null);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testNoCollapseForEachInExterns
  public void testNoCollapseForEachInExterns() {
    collapsePropertiesOnExternTypes = true;
    test(" function Array() {}" +
         "Array.forEach = function() {}",
         "if (!Array.forEach) {\n" +
         "  Array.forEach = function() {};\n" +
         "}",
         "if (!Array.forEach) {\n" +
         "  Array.forEach = function() {};\n" +
         "}", null, null);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDoNotCollapsePropertyOnExternType
  public void testDoNotCollapsePropertyOnExternType() {
    collapsePropertiesOnExternTypes = false;
    test("String.myFunc = function() {}; String.myFunc()",
         "String.myFunc = function() {}; String.myFunc()");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testBug1704733
  public void testBug1704733() {
    String prelude =
        "function protect(x) { return x; }" +
        "function O() {}" +
        "protect(O).m1 = function() {};" +
        "protect(O).m2 = function() {};" +
        "protect(O).m3 = function() {};";

    testSame(prelude +
        "alert(O.m1); alert(O.m2()); alert(!O.m3);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testBug1956277
  public void testBug1956277() {
    test("var CONST = {}; CONST.URL = 3;",
         "var CONST$URL = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testBug1974371
  public void testBug1974371() {
    test(
        " var Foo = {A: {c: 2}, B: {c: 3}};" +
        "for (var key in Foo) {}",
        "var Foo$A = {c: 2}; var Foo$B = {c: 3};" +
        "var Foo = {A: Foo$A, B: Foo$B};" +
         "for (var key in Foo) {}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects1
  public void testEnumOfObjects1() {
    test(
        COMMON_ENUM +
        "for (var key in Foo.A) {}",
         "var Foo$A = {c: 2}; var Foo$B$c = 3; for (var key in Foo$A) {}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects2
  public void testEnumOfObjects2() {
    test(
        COMMON_ENUM +
        "foo(Foo.A.c);",
         "var Foo$A$c = 2; var Foo$B$c = 3; foo(Foo$A$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects3
  public void testEnumOfObjects3() {
    test(
        "var x = {c: 2}; var y = {c: 3};" +
        " var Foo = {A: x, B: y};" +
        "for (var key in Foo) {}",
        "var x = {c: 2}; var y = {c: 3};" +
        "var Foo$A = x; var Foo$B = y; var Foo = {A: Foo$A, B: Foo$B};" +
        "for (var key in Foo) {}");
  }
