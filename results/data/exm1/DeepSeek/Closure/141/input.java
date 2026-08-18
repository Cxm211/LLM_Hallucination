// buggy code
  static boolean canBeSideEffected(Node n, Set<String> knownConstants) {
    switch (n.getType()) {
      case Token.CALL:
      case Token.NEW:
        // Function calls or constructor can reference changed values.
        // TODO(johnlenz): Add some mechanism for determining that functions
        // are unaffected by side effects.
        return true;
      case Token.NAME:
        // Non-constant names values may have been changed.
        return !NodeUtil.isConstantName(n)
            && !knownConstants.contains(n.getString());

      // Properties on constant NAMEs can still be side-effected.
      case Token.GETPROP:
      case Token.GETELEM:
        return true;

        // Anonymous functions definitions are not changed by side-effects,
        // and named functions are not part of expressions.
    }

    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (canBeSideEffected(c, knownConstants)) {
        return true;
      }
    }

    return false;
  }

  private static Collection<Definition> getCallableDefinitions(
      DefinitionProvider definitionProvider, Node name) {
      List<Definition> result = Lists.newArrayList();

      if (!NodeUtil.isGetProp(name) && !NodeUtil.isName(name)) {
        return null;
      }
      Collection<Definition> decls =
          definitionProvider.getDefinitionsReferencedAt(name);
      if (decls == null) {
        return null;
      }

      for (Definition current : decls) {
        Node rValue = current.getRValue();
        if ((rValue != null) && NodeUtil.isFunction(rValue)) {
          result.add(current);
        } else {
          return null;
        }
      }

      return result;
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
    testSame(generatePreProcessThrowCode(TOO_FEW_TO_ALIAS_THROW, "1"));
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testAlias
  public void testAlias() {
    test(generateCode("true", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_TRUE, ENOUGH_TO_ALIAS_LITERAL,
                      "var $$ALIAS_TRUE=true;"));

    test(generateCode("false", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_FALSE, ENOUGH_TO_ALIAS_LITERAL,
                      "var $$ALIAS_FALSE=false;"));

    test(generateCode("null", ENOUGH_TO_ALIAS_LITERAL),
         generateCode(AliasKeywords.ALIAS_NULL, ENOUGH_TO_ALIAS_LITERAL,
                      "var $$ALIAS_NULL=null;"));
    test(generatePreProcessThrowCode(ENOUGH_TO_ALIAS_THROW, "1"),
         generatePostProcessThrowCode(ENOUGH_TO_ALIAS_THROW, "", "1"));
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testAliasTrueFalseNull
  public void testAliasTrueFalseNull() {
    StringBuffer actual = new StringBuffer();
    actual.append(generateCode("true", ENOUGH_TO_ALIAS_LITERAL));
    actual.append(generateCode("false", ENOUGH_TO_ALIAS_LITERAL));
    actual.append(generateCode("null", ENOUGH_TO_ALIAS_LITERAL));

    StringBuffer expected = new StringBuffer();
    expected.append(
        "var $$ALIAS_TRUE=true;var $$ALIAS_NULL=null;var $$ALIAS_FALSE=false;");
    expected.append(
        generateCode(AliasKeywords.ALIAS_TRUE, ENOUGH_TO_ALIAS_LITERAL));
    expected.append(
        generateCode(AliasKeywords.ALIAS_FALSE, ENOUGH_TO_ALIAS_LITERAL));
    expected.append(
        generateCode(AliasKeywords.ALIAS_NULL, ENOUGH_TO_ALIAS_LITERAL));

    test(actual.toString(), expected.toString());
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testAliasThrowKeywordLiteral
  public void testAliasThrowKeywordLiteral() {
    int repitions = Math.max(ENOUGH_TO_ALIAS_THROW, ENOUGH_TO_ALIAS_LITERAL);
    String afterCode = generatePostProcessThrowCode(
          repitions, "var $$ALIAS_TRUE=true;", AliasKeywords.ALIAS_TRUE);
    test(generatePreProcessThrowCode(repitions, "true"), afterCode);
  }

// com.google.javascript.jscomp.AliasKeywordsTest::testExistingAliasDefinitionFails
  public void testExistingAliasDefinitionFails() {
    try {
      testSame("var $$ALIAS_TRUE='foo';");
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

// com.google.javascript.jscomp.AliasStringsTest::testAssignment
  public void testAssignment() {
    strings = ImmutableSet.of("none", "width", "overimaginative");

    
    testSame("var foo='foo'");
    testSame("a='titanium',b='titanium',c='titanium',d='titanium'");

    
    testSame("myStr='width'");
    testSame("Bar.prototype.start='none'");
    
    test("a='overimaginative';b='overimaginative'",
         "var $$S_overimaginative='overimaginative';" +
         "a=$$S_overimaginative;b=$$S_overimaginative");

    testSame("var width=1234");
    testSame("width=1234;width=10000;width=9900;width=17;");
  }

// com.google.javascript.jscomp.AliasStringsTest::testSeveral
  public void testSeveral() {
    strings = ImmutableSet.of("", "px", "none", "width");

    test("function() {var styles=['width',100,'px','display','none'].join('')}",
         "var $$S_width='width';" +
         "var $$S_px='px';" +
         "var $$S_none='none';" +
         "var $$S_='';" +
         "function() {var styles=[$$S_width,100,$$S_px,'display'," +
         "$$S_none].join($$S_)}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testObjectLiterals
  public void testObjectLiterals() {
    strings = ImmutableSet.of("px", "!@#$%^&*()");

    test("var foo={px:435}", "var foo={px:435}");

    
    test("var foo={'px':435}", "var foo={px:435}");
    test("bar=function(){return {'px':435}}",
         "bar=function(){return {px:435}}");

    test("function() {var foo={bar:'!@#$%^&*()'}}",
         "var $$S_$21$40$23$24$25$5e$26$2a$28$29='!@#$%^&*()';" +
         "function() {var foo={bar:$$S_$21$40$23$24$25$5e$26$2a$28$29}}");

    test("function() {var foo={px:435,foo:'px',bar:'baz'}}",
         "var $$S_px='px';" +
         "function() {var foo={px:435,foo:$$S_px,bar:'baz'}}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testGetProp
  public void testGetProp() {
    strings = ImmutableSet.of("px", "width");

    testSame("function(){element.style.px=1234}");

    test("function f(){shape.width.units='px'}",
        "var $$S_px='px';function f(){shape.width.units=$$S_px}");

    test("function f(){shape['width'].units='pt'}",
         "var $$S_width='width';" +
         "function f(){shape[$$S_width].units='pt'}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testFunctionCalls
  public void testFunctionCalls() {
    strings = ImmutableSet.of("", ",", "overimaginative");

    
    testSame("alert('')");
    testSame("var a=[1,2,3];a.join(',')");
    
    test("f('overimaginative', 'overimaginative')",
         "var $$S_overimaginative='overimaginative';" +
         "f($$S_overimaginative,$$S_overimaginative)");
 }

// com.google.javascript.jscomp.AliasStringsTest::testRegularExpressions
  public void testRegularExpressions() {
    strings = ImmutableSet.of("px");

    testSame("/px/.match('10px')");
  }

// com.google.javascript.jscomp.AliasStringsTest::testBlackList
  public void testBlackList() {
    test("function(){var f=\'sec ret\';g=\"TOPseCreT\"}",
         "var $$S_sec$20ret='sec ret';" +
         "function(){var f=$$S_sec$20ret;g=\"TOPseCreT\"}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testLongStableAlias
  public void testLongStableAlias() {
    strings = ALL_STRINGS;

    

    test("a='Antidisestablishmentarianism';" +
         "b='Antidisestablishmentarianism';",
         "var $$S_Antidisestablishment_506eaf9c=" +
         "  'Antidisestablishmentarianism';"      +
         "a=$$S_Antidisestablishment_506eaf9c;"   +
         "b=$$S_Antidisestablishment_506eaf9c");

    

    test("a='AntidisestablishmentarianIsm';" +
         "b='AntidisestablishmentarianIsm';",
         "var $$S_Antidisestablishment_6823e97c=" +
         "  'AntidisestablishmentarianIsm';"      +
         "a=$$S_Antidisestablishment_6823e97c;"   +
         "b=$$S_Antidisestablishment_6823e97c");

    
  }

// com.google.javascript.jscomp.AliasStringsTest::testLongStableAliasHashCollision
  public void testLongStableAliasHashCollision() {
    strings = ALL_STRINGS;
    hashReduction = true;

    
    

    test("f('Antidisestablishmentarianism');"  +
         "f('Antidisestablishmentarianism');"  +
         "f('Antidisestablishmentarianismo');" +
         "f('Antidisestablishmentarianismo');",

         "var $$S_Antidisestablishment_0="     +
         "  'Antidisestablishmentarianism';"   +
         "f($$S_Antidisestablishment_0);"      +
         "f($$S_Antidisestablishment_0);"      +

         "var $$S_Antidisestablishment_0_1="   +
         "  'Antidisestablishmentarianismo';"  +
         "f($$S_Antidisestablishment_0_1);"    +
         "f($$S_Antidisestablishment_0_1);");
  }

// com.google.javascript.jscomp.AliasStringsTest::testStringsThatAreGlobalVarValues
  public void testStringsThatAreGlobalVarValues() {
    strings = ALL_STRINGS;

    testSame("var foo='foo'; var bar='';");

    
    testSame("var foo=['foo','bar'];");

    
    testSame("var foo=['foo',['bar']];");

    
    test("var foo=['foo', 'bar'];function bar() {return 'foo';}",
        "var $$S_foo='foo';" +
        "var foo=[$$S_foo, 'bar'];function bar() {return $$S_foo;}");

    
    testSame("var foo={'foo': 'bar'};");

    
    testSame("var foo={'foo': {'bar': 'baz'}};");

    
    
    test("var foo={'foo': 'bar'};function bar() {return 'foo';}",
        "var foo={'foo': 'bar'};" +
        "var $$S_foo='foo';function bar() {return $$S_foo;}");

    
    
    test("var foo={'foo': 'foo'};function bar() {return 'foo';}",
         "var $$S_foo='foo';" +
         "var foo={'foo': $$S_foo};function bar() {return $$S_foo;}");

  }

// com.google.javascript.jscomp.AliasStringsTest::testStringsInModules
  public void testStringsInModules() {
    strings = ALL_STRINGS;

    
    
    

    JSModule[] modules =
        createModules(
            
            "function f(a) { alert('f:' + a); }" +
            "function g() { alert('ciao'); }",
            
            "f('-------hi-------');" +
            "f('bye');" +
            "function h(a) { alert('h:' + a); }",
            
            "f('-------hi-------');" +
            "h('ciao' + '------adios------');" +
            "(function() { alert('zzz'); })();",
            
            "f('-------hi-------'); alert('------adios------');" +
            "h('-----peaches-----'); h('-----peaches-----');");

    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[1]);
    modules[3].addDependency(modules[1]);

    moduleGraph = new JSModuleGraph(modules);

    test(modules,
         new String[] {
             
             "var $$S_f$3a = 'f:';" +
             "function f(a) { alert($$S_f$3a + a); }" +
             "var $$S_ciao = 'ciao';" +
             "function g() { alert($$S_ciao); }",
             
             "var $$S_$2d$2d$2d$2d$2d$2d$2dhi$2d$2d$2d$2d$2d$2d$2d" +
             " = '-------hi-------';" +
             "var $$S_$2d$2d$2d$2d$2d$2d_adios$2d$2d$2d$2d$2d$2d" +
             " = '------adios------'; " +
             "f($$S_$2d$2d$2d$2d$2d$2d$2dhi$2d$2d$2d$2d$2d$2d$2d);" +
             "f('bye');" +
             "var $$S_h$3a = 'h:';" +
             "function h(a) { alert($$S_h$3a + a); }",
             
             "f($$S_$2d$2d$2d$2d$2d$2d$2dhi$2d$2d$2d$2d$2d$2d$2d);" +
             "h($$S_ciao + $$S_$2d$2d$2d$2d$2d$2d_adios$2d$2d$2d$2d$2d$2d);" +
             "var $$S_zzz = 'zzz';" +
             "(function() { alert($$S_zzz) })();",
             
             "f($$S_$2d$2d$2d$2d$2d$2d$2dhi$2d$2d$2d$2d$2d$2d$2d);" +
             "alert($$S_$2d$2d$2d$2d$2d$2d_adios$2d$2d$2d$2d$2d$2d);" +
             "var $$S_$2d$2d$2d$2d$2dpeaches$2d$2d$2d$2d$2d" +
             " = '-----peaches-----';" +
             "h($$S_$2d$2d$2d$2d$2dpeaches$2d$2d$2d$2d$2d);" +
             "h($$S_$2d$2d$2d$2d$2dpeaches$2d$2d$2d$2d$2d);",
         });
    moduleGraph = null;
  }

// com.google.javascript.jscomp.AliasStringsTest::testEmptyModules
  public void testEmptyModules() {
    JSModule[] modules =
      createModules(
          
          "",
          
          "function foo() { f('good') }",
          
          "function foo() { f('good') }");
    modules[1].addDependency(modules[0]);
    modules[2].addDependency(modules[0]);

    moduleGraph = new JSModuleGraph(modules);
    test(modules,
        new String[] {
        
        "var $$S_good='good'",
        
        "function foo() {f($$S_good)}",
        
        "function foo() {f($$S_good)}",});

    moduleGraph = null;
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar
  public void testOneVar() {
    test(" var Foo = function(){};Foo.prototype.b = 0;",
         "var Foo = function(){};Foo.prototype.a = 0;");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoVar
  public void testTwoVar() {
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
        + "Foo.prototype.fun = function() { new Bar };\n"
        + " function Bar(){};\n"
        + "Bar.prototype.bazz;\n"
        + "(new Foo).fun().bazz();";
    String output = ""
        + "function Foo(){};\n"
        + "Foo.prototype.a = function() { new Bar };\n"
        + "function Bar(){};\n"
        + "Bar.prototype.a;\n"
        + "(new Foo).a().a();";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPrototypePropertiesAsObjLitKeys
  public void testPrototypePropertiesAsObjLitKeys() {
    testSame(" function Bar() {};" +
             "Bar.prototype = {2: function(){}, getA: function(){}};");
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

// com.google.javascript.jscomp.AstParallelizerTest::testNoSplit
  public void testNoSplit() {
    splitFunctions("", "");
    splitFunctions("var x", "var x");
    splitFunctions("var x", "var x");
    splitFunctions("x()", "x()");
  }

// com.google.javascript.jscomp.AstParallelizerTest::testSplitNamedFuntion
  public void testSplitNamedFuntion() {
    splitFunctions("function foo() { foo() } foo()",
                   "function " + HOLDER + "() {} foo()",
                   "function foo() { foo() }"); 
  }

// com.google.javascript.jscomp.AstParallelizerTest::testSplitNamedFuntionWithArgs
  public void testSplitNamedFuntionWithArgs() {
    splitFunctions("function foo(x) { foo(1) } foo(1)",
                   "function " + HOLDER + "() {} foo(1)",
                   "function foo(x) { foo(1) }"); 
  }

// com.google.javascript.jscomp.AstParallelizerTest::testSplitAnonFuntion
  public void testSplitAnonFuntion() {
    splitFunctions("var foo = function(x) { foo(1) }; foo(1)",
                   "var foo = function " + HOLDER + "() {}; foo(1)",
                   "function(x) { foo(1) }"); 
  }

// com.google.javascript.jscomp.AstParallelizerTest::testSplitInplaceCall
  public void testSplitInplaceCall() {
    splitFunctions("(function() { print('hi') })()",
                   "(function " + HOLDER + "() {})()",
                   "function() { print('hi') }"); 
  }

// com.google.javascript.jscomp.AstParallelizerTest::testSplitMupltiFuntions
  public void testSplitMupltiFuntions() {
    splitFunctions("var foo = function(x) { foo(1) }; foo();" + 
                   "var bar = function(x,y) { bar(1,2) }; bar(1,2)",
                   
                   "var foo = function " + HOLDER + "() {}; foo();" +
                   "var bar = function " + HOLDER + "() {}; bar(1,2)",
                   
                   "function(x) { foo(1) }",
                   
                   "function(x,y) { bar(1,2) }"); 
  }

// com.google.javascript.jscomp.AstParallelizerTest::testInnerFunctions
  public void testInnerFunctions() {
    splitFunctions("var foo = function() {var bar = function() {}}",
                   "var foo = function " + HOLDER + "() {}",
                   "function() {var bar = function() {}}");
  }

// com.google.javascript.jscomp.AstParallelizerTest::testSplitFileLevel
  public void testSplitFileLevel() {
    splitFiles(new String[] { "var a", "var b", "var c"});
    splitFiles(new String[] {
        "var a", "var b", "var c", "var d", "function e() {}"});
  }

// com.google.javascript.jscomp.ChainCallsTest::testUnchainedCalls
  public void testUnchainedCalls() {
    test(
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + "var f = new Foo();\n"
        + "f.bar();\n"
        + "f.bar();\n",
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + "var f = new Foo();\n"
        + "f.bar().bar();\n");

  }

// com.google.javascript.jscomp.ChainCallsTest::testSecondCallReturnNotThis
  public void testSecondCallReturnNotThis() {
    test(
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + "Foo.prototype.baz = function() {};\n"
        + "var f = new Foo();\n"
        + "f.bar();\n"
        + "f.baz();\n",
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + "Foo.prototype.baz = function() {};\n"
        + "var f = new Foo();\n"
        + "f.bar().baz();\n");
  }

// com.google.javascript.jscomp.ChainCallsTest::testDifferentInstance
  public void testDifferentInstance() {
    testSame(
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + "new Foo().bar();\n"
        + "new Foo().bar();\n");
  }

// com.google.javascript.jscomp.ChainCallsTest::testSubclass
  public void testSubclass() {
    testSame(
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + " function Baz() {}\n"
        + "Baz.prototype.bar = function() {};\n"
        + "( new Baz()).bar();\n"
        + "( new Baz()).bar();\n");
  }

// com.google.javascript.jscomp.ChainCallsTest::testSimpleDefinitionFinder
  public void testSimpleDefinitionFinder() {
    String defs =
        " function Foo() {}\n" +
        "Foo.prototype.a = function() { return this; };" +
        " function Bar() {}\n" +
        "Bar.prototype.a = function() {};";
    testSame(
        defs +
        "var o = new Foo; o.a(); o.a();");
    testSame(
        defs +
        "var o = new Bar; o.a(); o.a();");
  }

// com.google.javascript.jscomp.ChainCallsTest::testSimpleDefinitionFinder2
  public void testSimpleDefinitionFinder2() {
    String defs =
        " function Foo() {}\n" +
        "Foo.prototype.a = function() { return this; };" +
        " function Bar() {}\n" +
        "Bar.prototype.a = function() { return this; };";
    testSame(
        defs +
        "var o = new Foo; o.a().a();");
    testSame(
        defs +
        "var o = new Bar; o.a().a();");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testDeprecatedFunction
  public void testDeprecatedFunction() {
    testDep(" function f() {} function g() { f(); }",
            "Some Reason",
            DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningOnDeprecatedConstVariable
  public void testWarningOnDeprecatedConstVariable() {
    testDep(" var f = 4; function g() { alert(f); }",
            "Another reason",
            DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testThatNumbersArentDeprecated
  public void testThatNumbersArentDeprecated() {
    testSame(" var f = 4; var h = 3; " +
             "function g() { alert(h); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testDeprecatedFunctionVariable
  public void testDeprecatedFunctionVariable() {
    testDep(" var f = function() {}; " +
            "function g() { f(); }", "I like g...",
            DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInGlobalScope
  public void testNoWarningInGlobalScope() {
    testSame("var goog = {}; goog.makeSingleton = function(x) {};" +
        " function f() {} goog.makeSingleton(f);");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInGlobalScopeForCall
  public void testNoWarningInGlobalScopeForCall() {
    testDep(" function f() {} f();",
            "Some global scope", DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedFunction
  public void testNoWarningInDeprecatedFunction() {
    testSame(" function f() {} " +
             " function g() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningInNormalClass
  public void testWarningInNormalClass() {
    testDep(" function f() {}" +
            "  var Foo = function() {}; " +
            "Foo.prototype.bar = function() { f(); }",
            "FooBar", DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForProperty1
  public void testWarningForProperty1() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = 3;" +
            "Foo.prototype.baz = function() { alert((new Foo()).bar); };",
            "A property is bad",
            DEPRECATED_PROP, DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForProperty2
  public void testWarningForProperty2() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = 3;" +
            "Foo.prototype.baz = function() { alert(this.bar); };",
            "Zee prop, it is deprecated!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedClass
  public void testWarningForDeprecatedClass() {
    testDep(" function Foo() {} " +
            "function f() { new Foo(); }",
            "Use the class 'Bar'",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningForDeprecatedClassInstance
  public void testNoWarningForDeprecatedClassInstance() {
    testSame(" function Foo() {} " +
             " function f(x) { return x; }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedSuperClass
  public void testWarningForDeprecatedSuperClass() {
    testDep(" function Foo() {} " +
            " function SubFoo() {}" +
            "function f() { new SubFoo(); }",
            "Superclass to the rescue!",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedSuperClass2
  public void testWarningForDeprecatedSuperClass2() {
    testDep(" function Foo() {} " +
            "var namespace = {}; " +
            " " +
            "namespace.SubFoo = function() {}; " +
            "function f() { new namespace.SubFoo(); }",
            "Its only weakness is Kryptoclass",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForPrototypeProperty
  public void testWarningForPrototypeProperty() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = 3;" +
            "Foo.prototype.baz = function() { alert(Foo.prototype.bar); };",
            "It is now in production, use that model...",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningForNumbers
  public void testNoWarningForNumbers() {
    testSame(" function Foo() {}" +
             " Foo.prototype.bar = 3;" +
             "Foo.prototype.baz = function() { alert(3); };");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForMethod1
  public void testWarningForMethod1() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = function() {};" +
            "Foo.prototype.baz = function() { this.bar(); };",
            "There is a madness to this method",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForMethod2
  public void testWarningForMethod2() {
    testDep(" function Foo() {} " +
            " Foo.prototype.bar; " +
            "Foo.prototype.baz = function() { this.bar(); };",
            "Stop the ringing!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedClass
  public void testNoWarningInDeprecatedClass() {
    testSame(" function f() {} " +
             " " +
             "var Foo = function() {}; " +
             "Foo.prototype.bar = function() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedClass2
  public void testNoWarningInDeprecatedClass2() {
    testSame(" function f() {} " +
             " " +
             "var Foo = function() {}; " +
             "Foo.bar = function() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedStaticMethod
  public void testNoWarningInDeprecatedStaticMethod() {
    testSame(" function f() {} " +
             " " +
             "var Foo = function() {}; " +
             " Foo.bar = function() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningInStaticMethod
  public void testWarningInStaticMethod() {
    testDep(" function f() {} " +
            " " +
            "var Foo = function() {}; " +
            "Foo.bar = function() { f(); }",
            "crazy!",
            DEPRECATED_NAME,
            DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testDeprecatedObjLitKey
  public void testDeprecatedObjLitKey() {
    testDep("var f = {};  f.foo = 3; " +
            "function g() { return f.foo; }",
            "It is literally not used anymore",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSubclassMethod
  public void testWarningForSubclassMethod() {
    testDep(" function Foo() {}" +
            "Foo.prototype.bar = function() {};" +
            " function SubFoo() {}" +
            " SubFoo.prototype.bar = function() {};" +
            "function f() { (new SubFoo()).bar(); };",
            "I have a parent class!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSuperClassWithDeprecatedSubclassMethod
  public void testWarningForSuperClassWithDeprecatedSubclassMethod() {
    testSame(" function Foo() {}" +
             "Foo.prototype.bar = function() {};" +
             " function SubFoo() {}" +
             " SubFoo.prototype.bar = function() {};" +
             "function f() { (new Foo()).bar(); };");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSuperclassMethod
  public void testWarningForSuperclassMethod() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = function() {};" +
            " function SubFoo() {}" +
            "SubFoo.prototype.bar = function() {};" +
            "function f() { (new SubFoo()).bar(); };",
            "I have a child class!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSuperclassMethod2
  public void testWarningForSuperclassMethod2() {
    testDep(" function Foo() {}" +
            "" +
            "Foo.prototype.bar = function() {};" +
            " function SubFoo() {}" +
            "SubFoo.prototype.bar = function() {};" +
            "function f() { (new SubFoo()).bar(); };",
            "I have another child class...",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForBind
  public void testWarningForBind() {
    testDep(" Function.prototype.bind = function() {};" +
            "(function() {}).bind();",
            "I'm bound to this method...",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedClassInGlobalScope
  public void testWarningForDeprecatedClassInGlobalScope() {
    testDep(" var Foo = function() {};" +
            "new Foo();",
            "I'm a very worldly object!",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningForPrototypeCopying
  public void testNoWarningForPrototypeCopying() {
    testSame(" var Foo = function() {};" +
             "Foo.prototype.bar = function() {};" +
             " Foo.prototype.baz = Foo.prototype.bar;" +
             "(new Foo()).bar();");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningOnDeprecatedPrototype
  public void testNoWarningOnDeprecatedPrototype() {
    
    testSame(" var Foo = function() {};" +
        " Foo.prototype = {};" +
        "Foo.prototype.bar = function() {};");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForNames
  public void testPrivateAccessForNames() {
    testSame(" function foo_() {}; foo_();");
    test(new String[] {
      " function foo_() {};",
      "foo_();"
    }, null, BAD_PRIVATE_GLOBAL_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties1
  public void testPrivateAccessForProperties1() {
    testSame(" function Foo() {}" +
        " Foo.prototype.bar_ = function() {};" +
        "Foo.prototype.baz = function() { this.bar_(); }; (new Foo).bar_();");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties2
  public void testPrivateAccessForProperties2() {
    testSame(new String[] {
      " function Foo() {}",
      " Foo.prototype.bar_ = function() {};" +
      "Foo.prototype.baz = function() { this.bar_(); }; (new Foo).bar_();"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties3
  public void testPrivateAccessForProperties3() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar_ = function() {}; (new Foo).bar_();",
      "Foo.prototype.baz = function() { this.bar_(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties1
  public void testNoPrivateAccessForProperties1() {
    test(new String[] {
      " function Foo() {} (new Foo).bar_();",
      " Foo.prototype.bar_ = function() {};" +
      "Foo.prototype.baz = function() { this.bar_(); };"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties2
  public void testNoPrivateAccessForProperties2() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};" +
      "Foo.prototype.baz = function() { this.bar_(); };",
      "(new Foo).bar_();"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties3
  public void testNoPrivateAccessForProperties3() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " function OtherFoo() { (new Foo).bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties4
  public void testNoPrivateAccessForProperties4() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " " +
      "function SubFoo() { this.bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties5
  public void testNoPrivateAccessForProperties5() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " " +
      "function SubFoo() {};" +
      "SubFoo.prototype.baz = function() { this.bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties6
  public void testNoPrivateAccessForProperties6() {
    
    
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " " +
      "function SubFoo() {};" +
      "SubFoo.prototype.bar_ = function() {};"
    }, null, PRIVATE_OVERRIDE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties7
  public void testNoPrivateAccessForProperties7() {
    
    
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};" +
      " " +
      "function SubFoo() {};" +
      " SubFoo.prototype.bar_ = function() {};",
      "SubFoo.prototype.baz = function() { this.bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties1
  public void testProtectedAccessForProperties1() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "Foo.prototype.baz = function() { this.bar(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties2
  public void testProtectedAccessForProperties2() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "" +
      "function SubFoo() { this.bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties3
  public void testProtectedAccessForProperties3() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "" +
      "function SubFoo() { }" +
      "SubFoo.baz = function() { (new Foo).bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties4
  public void testProtectedAccessForProperties4() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.bar = function() {};",
      "" +
      "function SubFoo() { Foo.bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties5
  public void testProtectedAccessForProperties5() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "" +
      "var SubFoo = function() { this.bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties6
  public void testProtectedAccessForProperties6() {
    testSame(new String[] {
      "var goog = {};" +
      " goog.Foo = function() {};" +
      " goog.Foo.prototype.bar = function() {};",
      "" +
      "goog.SubFoo = function() { this.bar(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties1
  public void testNoProtectedAccessForProperties1() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar = function() {};",
      "(new Foo).bar();"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties2
  public void testNoProtectedAccessForProperties2() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar = function() {};",
      " function OtherFoo() { (new Foo).bar(); }"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties3
  public void testNoProtectedAccessForProperties3() {
    test(new String[] {
      " function Foo() {} " +
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
      " " +
      "function SubberFoo() { (new SubFoo).bar(); }"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties4
  public void testNoProtectedAccessForProperties4() {
    test(new String[] {
      " function Foo() { (new SubFoo).bar(); } ",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties5
  public void testNoProtectedAccessForProperties5() {
    test(new String[] {
      "var goog = {};" +
      " goog.Foo = function() {};" +
      " goog.Foo.prototype.bar = function() {};",
      "" +
      "goog.NotASubFoo = function() { (new goog.Foo).bar(); };"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoExceptionsWithBadConstructors1
  public void testNoExceptionsWithBadConstructors1() {
    testSame(new String[] {
      "function Foo() { (new SubFoo).bar(); } " +
      " function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoExceptionsWithBadConstructors2
  public void testNoExceptionsWithBadConstructors2() {
    testSame(new String[] {
      " function Foo() {} " +
      "Foo.prototype.bar = function() {};" +
      "" +
      "function SubFoo() {}" +
      " " +
      "SubFoo.prototype.bar = function() { (new Foo).bar(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testGoodOverrideOfProtectedProperty
  public void testGoodOverrideOfProtectedProperty() {
    testSame(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testBadOverrideOfProtectedProperty
  public void testBadOverrideOfProtectedProperty() {
    test(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    }, null, VISIBILITY_MISMATCH);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testBadOverrideOfPrivateProperty
  public void testBadOverrideOfPrivateProperty() {
    test(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    }, null, PRIVATE_OVERRIDE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAccessOfStaticMethodOnPrivateConstructor
  public void testAccessOfStaticMethodOnPrivateConstructor() {
    testSame(new String[] {
      " function Foo() { } " +
      "Foo.create = function() { return new Foo(); };",
      "Foo.create()",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAccessOfStaticMethodOnPrivateQualifiedConstructor
  public void testAccessOfStaticMethodOnPrivateQualifiedConstructor() {
    testSame(new String[] {
      "var goog = {};" +
      " goog.Foo = function() { }; " +
      "goog.Foo.create = function() { return new goog.Foo(); };",
      "goog.Foo.create()",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testInstanceofOfPrivateConstructor
  public void testInstanceofOfPrivateConstructor() {
    testSame(new String[] {
      "var goog = {};" +
      " goog.Foo = function() { }; " +
      "goog.Foo.create = function() { return new goog.Foo(); };",
      "goog instanceof goog.Foo",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testOkAssignmentOfDeprecatedProperty
  public void testOkAssignmentOfDeprecatedProperty() {
    testSame(
        " function Foo() {" +
        "  this.bar = 3;" +
        "}");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testBadReadOfDeprecatedProperty
  public void testBadReadOfDeprecatedProperty() {
    testDep(
        " function Foo() {" +
        "  this.bar = 3;" +
        "  this.baz = this.bar;" +
        "}",
        "GRR",
        DEPRECATED_PROP,
        DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAutoboxedDeprecatedProperty
  public void testAutoboxedDeprecatedProperty() {
    testDep(
        " function String() {}" +
        " String.prototype.length;" +
        "function f() { return 'x'.length; }",
        "GRR",
        DEPRECATED_PROP,
        DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAutoboxedPrivateProperty
  public void testAutoboxedPrivateProperty() {
    test(new String[] {
        " function String() {}" +
        " String.prototype.length;",
        "function f() { return 'x'.length; }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNullableDeprecatedProperty
  public void testNullableDeprecatedProperty() {
    testDep(
        " function Foo() {}" +
        " Foo.prototype.length;" +
        " function f(x) { return x.length; }",
        "GRR",
        DEPRECATED_PROP,
        DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNullablePrivateProperty
  public void testNullablePrivateProperty() {
    test(new String[] {
        " function Foo() {}" +
        " Foo.prototype.length;",
        " function f(x) { return x.length; }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccidentalSemicolonTest::test
  public void test(String js, DiagnosticType error) {
    test(js, error == null ? js : null, error);
  }

// com.google.javascript.jscomp.CheckAccidentalSemicolonTest::testSuspiciousSemi
  public void testSuspiciousSemi() {
    final DiagnosticType e = CheckAccidentalSemicolon.SUSPICIOUS_SEMICOLON;
    final DiagnosticType ok = null;  

    test("if(x()) x = y;", ok);
    test("if(x()); x = y;", e);  
    test("if(x()){} x = y;", ok);

    test("if(x()) x = y; else y=z;", ok);
    test("if(x()); else y=z;", e);
    test("if(x()){} else y=z;", ok);
    test("if(x()) x = y; else;", e);
    test("if(x()) x = y; else {}", ok);

    test("while(x()) x = y;", ok);
    test("while(x()); x = y;", e);
    test("while(x()){} x = y;", ok);
    test("while(x()); {x = y}", e);
    test("while(x()){} {x = y}", ok);

    test("for(;;) x = y;", ok);
    test("for(;;); x = y;", e);
    test("for(;;){} x = y;", ok);
    test("for(x in y) x = y;", ok);
    test("for(x in y); x = y;", e);
    test("for(x in y){} x = y;", ok);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDefinedProperties1
  public void testRefToDefinedProperties1() {
    testSame(NAMES + "alert(a.b); alert(a.c.e);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDefinedProperties2
  public void testRefToDefinedProperties2() {
    testSame(NAMES + "a.x={}; alert(a.x);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDefinedProperties3
  public void testRefToDefinedProperties3() {
    testSame(NAMES + "alert(a.d);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToMethod1
  public void testRefToMethod1() {
    testSame("function foo() {}; foo.call();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToMethod2
  public void testRefToMethod2() {
    testSame("function foo() {}; foo.call.call();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testCallUndefinedFunctionGivesNoWaring
  public void testCallUndefinedFunctionGivesNoWaring() {
    
    
    testSame("foo();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToPropertyOfAliasedName
  public void testRefToPropertyOfAliasedName() {
    
    testSame(NAMES + "alert(a); alert(a.x);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToUndefinedProperty1
  public void testRefToUndefinedProperty1() {
    testSame(NAMES + "alert(a.x);", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToUndefinedProperty2
  public void testRefToUndefinedProperty2() {
    testSame(NAMES + "a.x();", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToUndefinedProperty3
  public void testRefToUndefinedProperty3() {
    testSame(NAMES + "alert(a.c.x);", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDescendantOfUndefinedProperty1
  public void testRefToDescendantOfUndefinedProperty1() {
    testSame(NAMES + "var c = a.x.b;", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDescendantOfUndefinedProperty2
  public void testRefToDescendantOfUndefinedProperty2() {
    testSame(NAMES + "a.x.b();", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDescendantOfUndefinedProperty3
  public void testRefToDescendantOfUndefinedProperty3() {
    testSame(NAMES + "a.x.b = 3;", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testUndefinedPrototypeMethodRefGivesNoWarning
  public void testUndefinedPrototypeMethodRefGivesNoWarning() {
    testSame("function Foo() {} var a = new Foo(); a.bar();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testComplexPropAssignGivesNoWarning
  public void testComplexPropAssignGivesNoWarning() {
    testSame("var a = {}; var b = a.b = 3;");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testTypedefGivesNoWarning
  public void testTypedefGivesNoWarning() {
    testSame("var a = {};  a.b;");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDescendantOfUndefinedPropertyGivesCorrectWarning
  public void testRefToDescendantOfUndefinedPropertyGivesCorrectWarning() {
    testSame("", NAMES + "a.x.b = 3;", UNDEFINED_NAME_WARNING,
             UNDEFINED_NAME_WARNING.format("a.x"));
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testNamespaceInjection
  public void testNamespaceInjection() {
    injectNamespace = true;
    testSame(NAMES + "var c = a.x.b;", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testNoWarningForSimpleVarModuleDep1
  public void testNoWarningForSimpleVarModuleDep1() {
    testSame(createModuleChain(
        NAMES,
        "var c = a;"
    ));
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testNoWarningForSimpleVarModuleDep2
  public void testNoWarningForSimpleVarModuleDep2() {
    testSame(createModuleChain(
        "var c = a;",
        NAMES
    ));
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testNoWarningForGoodModuleDep1
  public void testNoWarningForGoodModuleDep1() {
    testSame(createModuleChain(
        NAMES,
        "var c = a.b;"
    ));
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testBadModuleDep1
  public void testBadModuleDep1() {
    testSame(createModuleChain(
        "var c = a.b;",
        NAMES
    ), STRICT_MODULE_DEP_QNAME);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testBadModuleDep2
  public void testBadModuleDep2() {
    testSame(createModuleStar(
        NAMES,
        "a.xxx = 3;",
        "var x = a.xxx;"
    ), STRICT_MODULE_DEP_QNAME);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testSelfModuleDep
  public void testSelfModuleDep() {
    testSame(createModuleChain(
        NAMES + "var c = a.b;"
    ));
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testUndefinedModuleDep1
  public void testUndefinedModuleDep1() {
    testSame(createModuleChain(
        "var c = a.xxx;",
        NAMES
    ), UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis1
  public void testGlobalThis1() throws Exception {
    testSame("var a = this;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis2
  public void testGlobalThis2() {
    testFailure("this.foo = 5;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis3
  public void testGlobalThis3() {
    testFailure("this[foo] = 5;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis4
  public void testGlobalThis4() {
    testFailure("this['foo'] = 5;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis5
  public void testGlobalThis5() {
    testFailure("(a = this).foo = 4;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis6
  public void testGlobalThis6() {
    testSame("a = this;");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction1
  public void testStaticFunction1() {
    testSame("function a() { return this; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction2
  public void testStaticFunction2() {
    testFailure("function a() { this.complex = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction3
  public void testStaticFunction3() {
    testSame("var a = function() { return this; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction4
  public void testStaticFunction4() {
    testFailure("var a = function() { this.foo.bar = 6; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction5
  public void testStaticFunction5() {
    testSame("function a() { return function() { return this; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction6
  public void testStaticFunction6() {
    testFailure("function a() { return function() { this = 8; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction7
  public void testStaticFunction7() {
    testFailure("var a = function() { return function() { this = 8; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testConstructor1
  public void testConstructor1() {
    testSame("function A() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testConstructor2
  public void testConstructor2() {
    testSame("var A = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testConstructor3
  public void testConstructor3() {
    testSame("a.A = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc1
  public void testThisJSDoc1() throws Exception {
    testSame("function h() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc2
  public void testThisJSDoc2() throws Exception {
    testSame("var h = function() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc3
  public void testThisJSDoc3() throws Exception {
    testSame("foo.bar = function() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc4
  public void testThisJSDoc4() throws Exception {
    testSame("function() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc5
  public void testThisJSDoc5() throws Exception {
    testSame("function a() { function() { this.foo = 56; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod1
  public void testMethod1() {
    testSame("A.prototype.m1 = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod2
  public void testMethod2() {
    testSame("a.B.prototype.m1 = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod3
  public void testMethod3() {
    testSame("a.b.c.D.prototype.m1 = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod1
  public void testStaticMethod1() {
    testFailure("a.b = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod2
  public void testStaticMethod2() {
    testFailure("a.b = function() { return function() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod3
  public void testStaticMethod3() {
    testFailure("a.b.c = function() { return function() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethodInStaticFunction
  public void testMethodInStaticFunction() {
    testSame("function f() { A.prototype.m1 = function() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunctionInMethod1
  public void testStaticFunctionInMethod1() {
    testSame("A.prototype.m1 = function() { function me() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunctionInMethod2
  public void testStaticFunctionInMethod2() {
    testSame("A.prototype.m1 = function() {" +
        "  function me() {" +
        "    function myself() {" +
        "      function andI() { this.m2 = 5; } } } }");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testMissingGetCssName
  public void testMissingGetCssName() {
    testMissing("var s = 'goog-inline-block'");
    testMissing("var s = 'CSS_FOO goog-menu'");
    testMissing("alert('goog-inline-block ' + goog.getClassName('CSS_FOO'))");
    testMissing("html = '<div class=\"goog-special-thing\">Hello</div>'");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testRecognizeGetCssName
  public void testRecognizeGetCssName() {
    testNotMissing("var s = goog.getCssName('goog-inline-block')");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testIgnoreGetUniqueIdArguments
  public void testIgnoreGetUniqueIdArguments() {
    testNotMissing("var s = goog.events.getUniqueId('goog-some-event')");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testIgnoreAssignmentsToIdConstant
  public void testIgnoreAssignmentsToIdConstant() {
    testNotMissing("SOME_ID = 'goog-some-id'");
    testNotMissing("SOME_PRIVATE_ID_ = 'goog-some-id'");
    testNotMissing("var SOME_ID_ = 'goog-some-id'");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testNotMissingGetCssName
  public void testNotMissingGetCssName() {
    testNotMissing("s = 'not-a-css-name'");
    testNotMissing("s = 'notagoog-css-name'");
  }

// com.google.javascript.jscomp.CheckMissingGetCssNameTest::testDontCrashIfTheresNoQualifiedName
  public void testDontCrashIfTheresNoQualifiedName() {
    testMissing("things[2].DONT_CARE_ABOUT_THIS_KIND_OF_ID = "
                + "'goog-inline-block'");
    testMissing("objects[3].doSomething('goog-inline-block')");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testMissingReturn
  public void testMissingReturn() {
    
    testMissing("if (a) { return 1; }");

    
    testMissing("switch(1) { case 12: return 5; }");
    
    
    testMissing("try { foo() } catch (e) { return 5; } finally { }");

    
    testMissing(" function f() { var x; }; return 1;");
    testMissing(" function f() { return 1; };");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testReturnNotMissing
  public void testReturnNotMissing()  {
    
    
    testNotMissing("");

    
    testSame("function f() { var x; }");
    testNotMissing("return 1;");

    
    testNotMissing("void", "var x;");
    testNotMissing("undefined", "var x;");

    
    testNotMissing("number|undefined", "var x;");
    testNotMissing("number|void", "var x;");
    testNotMissing("(number,void)", "var x;");
    testNotMissing("(number,undefined)", "var x;");
    testNotMissing("*", "var x;");
    
    
    testNotMissing("try { return foo() } catch (e) { } finally { }");

    
    testNotMissing(
        " function f() { return 1; }; return 1;");

    
    testNotMissing("try { return 12; } finally { return 62; }");
    testNotMissing("try { } finally { return 1; }");
    testNotMissing("switch(1) { default: return 1; }");
    testNotMissing("switch(g) { case 1: return 1; default: return 2; }");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testFinallyStatements
  public void testFinallyStatements() {
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    
    testNotMissing("try { return 1; } finally { }");
    testNotMissing("try { } finally { return 1; }");
    testMissing("try { } finally { }");
    
    
    testNotMissing("try { return 1; } finally { while (true) { } }");
    testMissing("try { } finally { while (x) { } }");
    testMissing("try { } finally { while (x) { if (x) { break; } } }");
    testNotMissing(
        "try { return 2; } finally { while (x) { if (x) { break; } } }");

    
    testMissing("try { } finally { try { } finally { } }");
    testNotMissing("try { } finally { try { return 1; } finally { } }");
    testNotMissing("try { return 1; } finally { try { } finally { } }");

    
    
    
    
    
    testNotMissing("try { g(); return 1; } finally { }");

    
    
    
    
    testNotMissing(
        "try {" +
        "    function f() {" +
        "       try { return 1; }" +
        "       finally { }" +
        "   };" +
        "   return 1;" +
        "}" +
        "finally { }");
    testMissing(
        "try {" +
        "    function f() {" +
        "       try { }" +
        "       finally { }" +
        "   };" +
        "   return 1;" +
        "}" +
        "finally { }");
    testMissing(
        "try {" +
        "    function f() {" +
        "       try { return 1; }" +
        "       finally { }" +
        "   };" +
        "}" +
        "finally { }");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testKnownConditions
  public void testKnownConditions() {
    testNotMissing("if (true) return 1");
    testMissing("if (true) {} else {return 1}");
    
    testMissing("if (false) return 1");
    testNotMissing("if (false) {} else {return 1}");
    
    testNotMissing("if (1) return 1");
    testMissing("if (1) {} else {return 1}");
    
    testMissing("if (0) return 1");
    testNotMissing("if (0) {} else {return 1}");

    testNotMissing("if (3) return 1");
    testMissing("if (3) {} else {return 1}");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testKnownWhileLoop
  public void testKnownWhileLoop() {
    testNotMissing("while (1) return 1");
    testNotMissing("while (1) { if (x) {return 1} else {return 1}}");
    testNotMissing("while (0) {} return 1");
    
    
    
    testNotMissing("while (1) {} return 0");
    testMissing("while (false) return 1");
    
    
    testMissing("while(x) { return 1 }");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testMultiConditions
  public void testMultiConditions() {
    testMissing("if (a) { } else { while (1) {return 1} }");
    testNotMissing("if (a) { return 1} else { while (1) {return 1} }");
  }

// com.google.javascript.jscomp.CheckProvidesTest::testIrrelevant
  public void testIrrelevant() {
    testSame("var str = 'g4';");
  }

// com.google.javascript.jscomp.CheckProvidesTest::testHarmlessProcedural
  public void testHarmlessProcedural() {
    testSame("goog.provide('X');  function X(){};");
  }

// com.google.javascript.jscomp.CheckProvidesTest::testHarmless
  public void testHarmless() {
    String js = "goog.provide('X');  X = function(){};";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckProvidesTest::testMissingGoogProvide
  public void testMissingGoogProvide(){
    String[] js = new String[]{" X = function(){};"};
    String warning = "missing goog.provide('X')";
    test(js, js, null, MISSING_PROVIDE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckProvidesTest::testMissingGoogProvideWithNamespace
  public void testMissingGoogProvideWithNamespace(){
    String[] js = new String[]{"goog = {}; " +
                               " goog.X = function(){};"};
    String warning = "missing goog.provide('goog.X')";
    test(js, js, null, MISSING_PROVIDE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckProvidesTest::testGoogProvideInWrongFileShouldCreateWarning
  public void testGoogProvideInWrongFileShouldCreateWarning(){
    String bad = " X = function(){};";
    String good = "goog.provide('X'); goog.provide('Y');" +
                  " X = function(){};" +
                  " Y = function(){};";
    String[] js = new String[] {good, bad};
    String warning = "missing goog.provide('X')";
    test(js, js, null, MISSING_PROVIDE_WARNING, warning);
  }

// com.google.javascript.jscomp.CheckProvidesTest::testGoogProvideMissingConstructorIsOkForNow
  public void testGoogProvideMissingConstructorIsOkForNow(){
    
    
    testSame(new String[]{"goog.provide('Y'); X = function(){};"});
  }

// com.google.javascript.jscomp.CheckProvidesTest::testIgnorePrivateConstructor
  public void testIgnorePrivateConstructor() {
    String js = " X_ = function(){};";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckProvidesTest::testIgnorePrivatelyAnnotatedConstructor
  public void testIgnorePrivatelyAnnotatedConstructor() {
    testSame(" X = function(){};");
    testSame(" X = function(){};");
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

// com.google.javascript.jscomp.CheckSideEffectsTest::test
  public void test(String js, DiagnosticType error) {
    test(js, error == null ? js : null, error);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testUselessCode
  public void testUselessCode() {
    test("function f(x) { if(x) return; }", ok);
    test("function f(x) { if(x); }", e);

    test("if(x) x = y;", ok);
    test("if(x) x == bar();", e);

    test("x = 3;", ok);
    test("x == 3;", e);

    test("var x = 'test'", ok);
    test("var x = 'test'\n'str'", e);

    test("", ok);
    test("foo();;;;bar();;;;", ok);

    test("var a, b; a = 5, b = 6", ok);
    test("var a, b; a = 5, b == 6", e);
    test("var a, b; a = (5, 6)", e);      
    test("var a, b; a = (b = 7, 6)", ok);
    test("function x(){}\nfunction f(a, b){}\nf(1,(x(), 2));", ok);
    test("function x(){}\nfunction f(a, b){}\nf(1,(2, 3));", e);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testUselessCodeInFor
  public void testUselessCodeInFor() {
    test("for(var x = 0; x < 100; x++) { foo(x) }", ok);
    test("for(; true; ) { bar() }", ok);
    test("for(foo(); true; foo()) { bar() }", ok);
    test("for(void 0; true; foo()) { bar() }", e);
    test("for(foo(); true; void 0) { bar() }", e);

    test("for(foo in bar) { foo() }", ok);
    test("for (i = 0; el = el.previousSibling; i++) {}", ok);
    test("for (i = 0; el = el.previousSibling; i++);", ok);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testTypeAnnotations
  public void testTypeAnnotations() {
    test("x;", e);
    test("a.b.c.d;", e);
    test(" a.b.c.d;", ok);
    test("if (true) {  a.b.c.d; }", ok);

    test("function A() { this.foo; }", e);
    test("function A() {  this.foo; }", ok);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testJSDocComments
  public void testJSDocComments() {
    test("function A() {  this.foo; }", ok);
    test("function A() {  this.foo; }", e);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testIssue80
  public void testIssue80() {
    test("(0, eval)('alert');", ok);
    test("(0, foo)('alert');", e);
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testCorrectSimple
  public void testCorrectSimple() {
    testSame("var x");
    testSame("var x = 1");
    testSame("var x = 1; x = 2;");
    testSame("if (x) { var x = 1 }");
    testSame("if (x) { var x = 1 } else { var y = 2 }");
    testSame("while(x) {}");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testIncorrectSimple
  public void testIncorrectSimple() {
    assertUnreachable("function f() { return; x=1; }");
    assertUnreachable("function f() { return; x=1; x=1; }");
    assertUnreachable("function f() { return; var x = 1; }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testCorrectIfReturns
  public void testCorrectIfReturns() {
    testSame("function f() { if (x) { return } }");
    testSame("function f() { if (x) { return } return }");
    testSame("function f() { if (x) { if (y) { return } } else { return }}");
    testSame("function f()" +
        "{ if (x) { if (y) { return } return } else { return }}");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testInCorrectIfReturns
  public void testInCorrectIfReturns() {
    assertUnreachable(
        "function f() { if (x) { return } else { return } return }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testCorrectSwitchReturn
  public void testCorrectSwitchReturn() {
    testSame("function f() { switch(x) { default: return; case 1: x++; }}");
    testSame("function f() {" +
        "switch(x) { default: return; case 1: x++; } return }");
    testSame("function f() {" +
        "switch(x) { default: return; case 1: return; }}");
    testSame("function f() {" +
        "switch(x) { case 1: return; } return }");
    testSame("function f() {" +
        "switch(x) { case 1: case 2: return; } return }");
    testSame("function f() {" +
        "switch(x) { case 1: return; case 2: return; } return }");
    testSame("function f() {" +
        "switch(x) { case 1 : return; case 2: return; } return }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testInCorrectSwitchReturn
  public void testInCorrectSwitchReturn() {
    assertUnreachable("function f() {" +
        "switch(x) { default: return; case 1: return; } return }");
    assertUnreachable("function f() {" +
        "switch(x) { default: return; return; case 1: return; } }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testCorrectLoopBreaksAndContinues
  public void testCorrectLoopBreaksAndContinues() {
    testSame("while(1) { foo(); break }");
    testSame("while(1) { foo(); continue }");
    testSame("for(;;) { foo(); break }");
    testSame("for(;;) { foo(); continue }");
    testSame("for(;;) { if (x) { break } }");
    testSame("for(;;) { if (x) { continue } }");
    testSame("do { foo(); continue} while(1)");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testInCorrectLoopBreaksAndContinues
  public void testInCorrectLoopBreaksAndContinues() {
    assertUnreachable("while(1) { foo(); break; bar()}");
    assertUnreachable("while(1) { foo(); continue; bar() }");
    assertUnreachable("for(;;) { foo(); break; bar() }");
    assertUnreachable("for(;;) { foo(); continue; bar() }");
    assertUnreachable("for(;;) { if (x) { break; bar() } }");
    assertUnreachable("for(;;) { if (x) { continue; bar() } }");
    assertUnreachable("do { foo(); continue; bar()} while(1)");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testUncheckedWhileInDo
  public void testUncheckedWhileInDo() {
    assertUnreachable("do { foo(); break} while(1)");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testUncheckedConditionInFor
  public void testUncheckedConditionInFor() {
    assertUnreachable("for(var x = 0; x < 100; x++) { break };");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testFunctionDeclaration
  public void testFunctionDeclaration() {
    
    testSame("function f() { return; function ff() { }}");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testVarDeclaration
  public void testVarDeclaration() {
    assertUnreachable("function f() { return; var x = 1 }");
    
    assertUnreachable("function f() { return; var x }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testReachableTryCatchFinally
  public void testReachableTryCatchFinally() {
    testSame("try { } finally {  }");
    testSame("try { foo(); } finally bar(); ");
    testSame("try { foo() } finally { bar() }");
    testSame("try { foo(); } catch (e) {e()} finally bar(); ");
    testSame("try { foo() } catch (e) {e()} finally { bar() }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testUnreachableCatch
  public void testUnreachableCatch() {
    assertUnreachable("try { var x = 0 } catch (e) { }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testSpuriousBreak
  public void testSpuriousBreak() {
    testSame("switch (x) { default: throw x; break; }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testInstanceOfThrowsException
  public void testInstanceOfThrowsException() {
    testSame("function f() {try { if (value instanceof type) return true; } " +
             "catch (e) { }}");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testVarAndOptionalParams
  public void testVarAndOptionalParams() {
    Node args = new Node(Token.LP,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    Node optArgs = new Node(Token.LP,
        Node.newString(Token.NAME, "opt_a"),
        Node.newString(Token.NAME, "opt_b"));

    assertFalse(conv.isVarArgsParameter(args.getFirstChild()));
    assertFalse(conv.isVarArgsParameter(args.getLastChild()));
    assertFalse(conv.isVarArgsParameter(optArgs.getFirstChild()));
    assertFalse(conv.isVarArgsParameter(optArgs.getLastChild()));

    assertFalse(conv.isOptionalParameter(args.getFirstChild()));
    assertFalse(conv.isOptionalParameter(args.getLastChild()));
    assertFalse(conv.isOptionalParameter(optArgs.getFirstChild()));
    assertFalse(conv.isOptionalParameter(optArgs.getLastChild()));
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInlineName
  public void testInlineName() {
    assertFalse(conv.isConstant("a"));
    assertFalse(conv.isConstant("XYZ123_"));
    assertFalse(conv.isConstant("ABC"));
    assertFalse(conv.isConstant("ABCdef"));
    assertFalse(conv.isConstant("aBC"));
    assertFalse(conv.isConstant("A"));
    assertFalse(conv.isConstant("_XYZ123"));
    assertFalse(conv.isConstant("a$b$XYZ123_"));
    assertFalse(conv.isConstant("a$b$ABC_DEF"));
    assertFalse(conv.isConstant("a$b$A"));
    assertFalse(conv.isConstant("a$b$a"));
    assertFalse(conv.isConstant("a$b$ABCdef"));
    assertFalse(conv.isConstant("a$b$aBC"));
    assertFalse(conv.isConstant("a$b$"));
    assertFalse(conv.isConstant("$"));
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testExportedName
  public void testExportedName() {
    assertFalse(conv.isExported("_a"));
    assertFalse(conv.isExported("_a_"));
    assertFalse(conv.isExported("a"));
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testPrivateName
  public void testPrivateName() {
    assertFalse(conv.isPrivate("a_"));
    assertFalse(conv.isPrivate("a"));
    assertFalse(conv.isPrivate("_a_"));
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testEnumKey
  public void testEnumKey() {
    assertTrue(conv.isValidEnumKey("A"));
    assertTrue(conv.isValidEnumKey("123"));
    assertTrue(conv.isValidEnumKey("FOO_BAR"));

    assertTrue(conv.isValidEnumKey("a"));
    assertTrue(conv.isValidEnumKey("someKeyInCamelCase"));
    assertTrue(conv.isValidEnumKey("_FOO_BAR"));
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection1
  public void testInheritanceDetection1() {
    assertNotClassDefining("goog.foo(A, B);");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection2
  public void testInheritanceDetection2() {
    assertDefinesClasses("goog.inherits(A, B);", "A", "B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection3
  public void testInheritanceDetection3() {
    assertDefinesClasses("A.inherits(B);", "A", "B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection4
  public void testInheritanceDetection4() {
    assertDefinesClasses("goog.inherits(goog.A, goog.B);", "goog.A", "goog.B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection5
  public void testInheritanceDetection5() {
    assertDefinesClasses("goog.A.inherits(goog.B);", "goog.A", "goog.B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection6
  public void testInheritanceDetection6() {
    assertNotClassDefining("A.inherits(this.B);");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection7
  public void testInheritanceDetection7() {
    assertNotClassDefining("this.A.inherits(B);");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection8
  public void testInheritanceDetection8() {
    assertNotClassDefining("goog.inherits(A, B, C);");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection9
  public void testInheritanceDetection9() {
    assertDefinesClasses("A.mixin(B.prototype);",
        "A", "B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection10
  public void testInheritanceDetection10() {
    assertDefinesClasses("goog.mixin(A.prototype, B.prototype);",
        "A", "B");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetectionPostCollapseProperties
  public void testInheritanceDetectionPostCollapseProperties() {
    assertDefinesClasses("goog$inherits(A, B);", "A", "B");
    assertNotClassDefining("goog$inherits(A);");
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDef1
  public void testGoogIsDef1() throws Exception {
    testClosureFunction("goog.isDef",
        createOptionalType(NUMBER_TYPE),
        NUMBER_TYPE,
        createOptionalType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDef2
  public void testGoogIsDef2() throws Exception {
    testClosureFunction("goog.isDef",
        createNullableType(NUMBER_TYPE),
        createNullableType(NUMBER_TYPE),
        createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsNull1
  public void testGoogIsNull1() throws Exception {
    testClosureFunction("goog.isNull",
        createOptionalType(NUMBER_TYPE),
        NULL_TYPE,
        createOptionalType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsNull2
  public void testGoogIsNull2() throws Exception {
    testClosureFunction("goog.isNull",
        createNullableType(NUMBER_TYPE),
        NULL_TYPE,
        NUMBER_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull1
  public void testGoogIsDefAndNotNull1() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        createOptionalType(NUMBER_TYPE),
        NUMBER_TYPE,
        createOptionalType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull2
  public void testGoogIsDefAndNotNull2() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        createNullableType(NUMBER_TYPE),
        NUMBER_TYPE,
        createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull3
  public void testGoogIsDefAndNotNull3() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        createOptionalType(createNullableType(NUMBER_TYPE)),
        NUMBER_TYPE,
        createOptionalType(createNullableType(NUMBER_TYPE)));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsString1
  public void testGoogIsString1() throws Exception {
    testClosureFunction("goog.isString",
        createNullableType(STRING_TYPE),
        STRING_TYPE,
        NULL_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsString2
  public void testGoogIsString2() throws Exception {
    testClosureFunction("goog.isString",
        createNullableType(NUMBER_TYPE),
        createNullableType(NUMBER_TYPE),
        createNullableType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsBoolean1
  public void testGoogIsBoolean1() throws Exception {
    testClosureFunction("goog.isBoolean",
        createNullableType(BOOLEAN_TYPE),
        BOOLEAN_TYPE,
        NULL_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsBoolean2
  public void testGoogIsBoolean2() throws Exception {
    testClosureFunction("goog.isBoolean",
        createUnionType(BOOLEAN_TYPE, STRING_TYPE, NO_OBJECT_TYPE),
        BOOLEAN_TYPE,
        createUnionType(STRING_TYPE, NO_OBJECT_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsNumber
  public void testGoogIsNumber() throws Exception {
    testClosureFunction("goog.isNumber",
        createNullableType(NUMBER_TYPE),
        NUMBER_TYPE,
        NULL_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsFunction
  public void testGoogIsFunction() throws Exception {
    testClosureFunction("goog.isFunction",
        createNullableType(OBJECT_FUNCTION_TYPE),
        OBJECT_FUNCTION_TYPE,
        NULL_TYPE);
  }
