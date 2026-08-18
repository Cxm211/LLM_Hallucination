// buggy code
  static final DiagnosticGroup ALL_DIAGNOSTICS = new DiagnosticGroup(
      DETERMINISTIC_TEST,
      DETERMINISTIC_TEST_NO_RESULT,
      INEXISTENT_ENUM_ELEMENT,
      INEXISTENT_PROPERTY,
      NOT_A_CONSTRUCTOR,
      BIT_OPERATION,
      NOT_CALLABLE,
      CONSTRUCTOR_NOT_CALLABLE,
      FUNCTION_MASKS_VARIABLE,
      MULTIPLE_VAR_DEF,
      ENUM_DUP,
      ENUM_NOT_CONSTANT,
      INVALID_INTERFACE_MEMBER_DECLARATION,
      INTERFACE_FUNCTION_NOT_EMPTY,
      CONFLICTING_EXTENDED_TYPE,
      BAD_IMPLEMENTED_TYPE,
      HIDDEN_SUPERCLASS_PROPERTY,
      HIDDEN_INTERFACE_PROPERTY,
      HIDDEN_SUPERCLASS_PROPERTY_MISMATCH,
      HIDDEN_INTERFACE_PROPERTY_MISMATCH,
      UNKNOWN_OVERRIDE,
      INTERFACE_METHOD_OVERRIDE,
      UNKNOWN_EXPR_TYPE,
      UNRESOLVED_TYPE,
      WRONG_ARGUMENT_COUNT,
      ILLEGAL_IMPLICIT_CAST,
      TypedScopeCreator.UNKNOWN_LENDS,
      TypedScopeCreator.LENDS_ON_NON_OBJECT,
      TypedScopeCreator.CTOR_INITIALIZER,
      TypedScopeCreator.IFACE_INITIALIZER,
      FunctionTypeBuilder.THIS_TYPE_NON_OBJECT);

  private void checkDeclaredPropertyInheritance(
      NodeTraversal t, Node n, FunctionType ctorType, String propertyName,
      JSDocInfo info, JSType propertyType) {
    // If the supertype doesn't resolve correctly, we've warned about this
    // already.
    if (hasUnknownOrEmptySupertype(ctorType)) {
      return;
    }

    FunctionType superClass = ctorType.getSuperClassConstructor();
    boolean superClassHasProperty = superClass != null &&
        superClass.getPrototype().hasProperty(propertyName);
    boolean declaredOverride = info != null && info.isOverride();

    boolean foundInterfaceProperty = false;
    if (ctorType.isConstructor()) {
      for (JSType implementedInterface : ctorType.getImplementedInterfaces()) {
        if (implementedInterface.isUnknownType() ||
            implementedInterface.isEmptyType()) {
          continue;
        }
        FunctionType interfaceType =
            implementedInterface.toObjectType().getConstructor();
        Preconditions.checkNotNull(interfaceType);
        boolean interfaceHasProperty =
            interfaceType.getPrototype().hasProperty(propertyName);
        foundInterfaceProperty = foundInterfaceProperty || interfaceHasProperty;
        if (reportMissingOverride.isOn() && !declaredOverride &&
            interfaceHasProperty) {
          // @override not present, but the property does override an interface
          // property
          compiler.report(t.makeError(n, reportMissingOverride,
              HIDDEN_INTERFACE_PROPERTY, propertyName,
              interfaceType.getTopMostDefiningType(propertyName).toString()));
        }
        if (interfaceHasProperty) {
          JSType interfacePropType =
              interfaceType.getPrototype().getPropertyType(propertyName);
          if (!propertyType.canAssignTo(interfacePropType)) {
            compiler.report(t.makeError(n,
                HIDDEN_INTERFACE_PROPERTY_MISMATCH, propertyName,
                interfaceType.getTopMostDefiningType(propertyName).toString(),
                interfacePropType.toString(), propertyType.toString()));
          }
        }
      }
    }

    if (!declaredOverride && !superClassHasProperty) {
      // nothing to do here, it's just a plain new property
      return;
    }

    JSType topInstanceType = superClassHasProperty ?
        superClass.getTopMostDefiningType(propertyName) : null;
    if (reportMissingOverride.isOn() && ctorType.isConstructor() &&
        !declaredOverride && superClassHasProperty) {
      // @override not present, but the property does override a superclass
      // property
      compiler.report(t.makeError(n, reportMissingOverride,
          HIDDEN_SUPERCLASS_PROPERTY, propertyName,
          topInstanceType.toString()));
    }
    if (!declaredOverride) {
      // there's no @override to check
      return;
    }
    // @override is present and we have to check that it is ok
    if (superClassHasProperty) {
      // there is a superclass implementation
      JSType superClassPropType =
          superClass.getPrototype().getPropertyType(propertyName);
      if (!propertyType.canAssignTo(superClassPropType)) {
        compiler.report(
            t.makeError(n, HIDDEN_SUPERCLASS_PROPERTY_MISMATCH,
                propertyName, topInstanceType.toString(),
                superClassPropType.toString(), propertyType.toString()));
      }
    } else if (!foundInterfaceProperty) {
      // there is no superclass nor interface implementation
      compiler.report(
          t.makeError(n, UNKNOWN_OVERRIDE,
              propertyName, ctorType.getInstanceType().toString()));
    }
  }

  static final DiagnosticGroup ALL_DIAGNOSTICS = new DiagnosticGroup(
      INVALID_CAST,
      TYPE_MISMATCH_WARNING,
      MISSING_EXTENDS_TAG_WARNING,
      DUP_VAR_DECLARATION,
      HIDDEN_PROPERTY_MISMATCH,
      INTERFACE_METHOD_NOT_IMPLEMENTED);

  private void expectInterfaceProperty(NodeTraversal t, Node n,
      ObjectType instance, ObjectType implementedInterface, String prop) {
    if (!instance.hasProperty(prop)) {
      // Not implemented
      String sourceName = (String) n.getProp(Node.SOURCENAME_PROP);
      sourceName = sourceName == null ? "" : sourceName;
      if (shouldReport) {
        compiler.report(JSError.make(sourceName, n,
            INTERFACE_METHOD_NOT_IMPLEMENTED,
            prop, implementedInterface.toString(), instance.toString()));
      }
      registerMismatch(instance, implementedInterface);
        // Implemented, but not correctly typed
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

    test("function f() {" +
         "var styles=['width',100,'px','display','none'].join('')}",
         "var $$S_='';" +
         "var $$S_none='none';" +
         "var $$S_px='px';" +
         "var $$S_width='width';" +
         "function f() {var styles=[$$S_width,100,$$S_px,'display'," +
         "$$S_none].join($$S_)}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testSortedOutput
  public void testSortedOutput() {
    strings = ImmutableSet.of("aba", "aaa", "aca", "bca", "bba");
    test("function f() {return ['aba', 'aaa', 'aca', 'bca', 'bba']}",
         "var $$S_aaa='aaa';" +
         "var $$S_aba='aba';" +
         "var $$S_aca='aca';" +
         "var $$S_bba='bba';" +
         "var $$S_bca='bca';" +
         "function f() {" +
         "  return [$$S_aba, $$S_aaa, $$S_aca, $$S_bca, $$S_bba]}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testObjectLiterals
  public void testObjectLiterals() {
    strings = ImmutableSet.of("px", "!@#$%^&*()");

    test("var foo={px:435}", "var foo={px:435}");

    
    test("var foo={'px':435}", "var foo={px:435}");
    test("bar=function f(){return {'px':435}}",
         "bar=function f(){return {px:435}}");

    test("function f() {var foo={bar:'!@#$%^&*()'}}",
         "var $$S_$21$40$23$24$25$5e$26$2a$28$29='!@#$%^&*()';" +
         "function f() {var foo={bar:$$S_$21$40$23$24$25$5e$26$2a$28$29}}");

    test("function f() {var foo={px:435,foo:'px',bar:'baz'}}",
         "var $$S_px='px';" +
         "function f() {var foo={px:435,foo:$$S_px,bar:'baz'}}");
  }

// com.google.javascript.jscomp.AliasStringsTest::testGetProp
  public void testGetProp() {
    strings = ImmutableSet.of("px", "width");

    testSame("function f(){element.style.px=1234}");

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
    test("(function (){var f=\'sec ret\';g=\"TOPseCreT\"})",
         "var $$S_sec$20ret='sec ret';" +
         "(function (){var f=$$S_sec$20ret;g=\"TOPseCreT\"})");
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

// com.google.javascript.jscomp.AstParallelizerTest::testSplitFileLevel
  public void testSplitFileLevel() {
    splitFiles(new String[] { "var a", "var b", "var c"});
    splitFiles(new String[] {
        "var a", "var b", "var c", "var d", "function e() {}"});
  }

// com.google.javascript.jscomp.CallGraphTest::testGetFunctionForAstNode
  public void testGetFunctionForAstNode() {
    String source = "function A() {};\n";
    
    CallGraph callgraph = compileAndRunForward(source);
    
    CallGraph.Function functionA = callgraph.getUniqueFunctionWithName("A");
    
    Node functionANode = functionA.getAstNode();
    
    assertEquals(functionA, callgraph.getFunctionForAstNode(functionANode));
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllFunctions
  public void testGetAllFunctions() {  
    String source = 
        "function A() {}\n" +
        "var B = function() {\n" +
        "(function C(){A()})()\n" + 
        "};\n";
      
    CallGraph callgraph = compileAndRunForward(source);
    
    Collection<CallGraph.Function> functions = callgraph.getAllFunctions();
    
    
    assertEquals(4, functions.size());
    
    CallGraph.Function functionA = 
        callgraph.getUniqueFunctionWithName("A");
    CallGraph.Function functionB =
        callgraph.getUniqueFunctionWithName("B");
    CallGraph.Function functionC =
        callgraph.getUniqueFunctionWithName("C");
    
    assertEquals("A", NodeUtil.getFunctionName(functionA.getAstNode()));
    assertEquals("B", NodeUtil.getFunctionName(functionB.getAstNode()));
    assertEquals("C", NodeUtil.getFunctionName(functionC.getAstNode()));
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllFunctionsContainsNormalFunction
  public void testGetAllFunctionsContainsNormalFunction() {
    String source = "function A(){}\n";
    
    CallGraph callgraph = compileAndRunForward(source);
        
    Collection<CallGraph.Function> allFunctions = callgraph.getAllFunctions();
    
    
    assertEquals(2, allFunctions.size());
    
   assertTrue(allFunctions.contains(callgraph.getUniqueFunctionWithName("A")));
   assertTrue(allFunctions.contains(callgraph.getMainFunction()));
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllFunctionsContainsVarAssignedLiteralFunction
  public void testGetAllFunctionsContainsVarAssignedLiteralFunction() {
    String source = "var A = function(){}\n";
    
    CallGraph callgraph = compileAndRunForward(source);
        
    Collection<CallGraph.Function> allFunctions = callgraph.getAllFunctions();
    
    
    assertEquals(2, allFunctions.size());

    Function functionA = callgraph.getUniqueFunctionWithName("A");
    assertTrue(allFunctions.contains(functionA));  
    assertTrue(allFunctions.contains(callgraph.getMainFunction()));
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllFunctionsContainsNamespaceAssignedLiteralFunction
  public void testGetAllFunctionsContainsNamespaceAssignedLiteralFunction() {
    String source = 
        "var namespace = {};\n" +
        "namespace.A = function(){};\n";
    
    CallGraph callgraph = compileAndRunForward(source);
        
    Collection<CallGraph.Function> allFunctions = callgraph.getAllFunctions();
    
    
    assertEquals(2, allFunctions.size());

    assertTrue(allFunctions.contains(
        callgraph.getUniqueFunctionWithName("namespace.A")));  
    assertTrue(allFunctions.contains(callgraph.getMainFunction()));
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllFunctionsContainsLocalFunction
  public void testGetAllFunctionsContainsLocalFunction() {
    String source = 
        "var A = function(){var B = function(){}};\n";
    
    CallGraph callgraph = compileAndRunForward(source);
        
    Collection<CallGraph.Function> allFunctions = callgraph.getAllFunctions();
    
    
    assertEquals(3, allFunctions.size());

    assertTrue(allFunctions.contains(callgraph.getUniqueFunctionWithName("A")));
    assertTrue(allFunctions.contains(callgraph.getUniqueFunctionWithName("B")));
    assertTrue(allFunctions.contains(callgraph.getMainFunction()));
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllFunctionsContainsAnonymousFunction
  public void testGetAllFunctionsContainsAnonymousFunction() {
    String source = 
        "var A = function(){(function(){})();};\n";
    
    CallGraph callgraph = compileAndRunForward(source);
        
    Collection<CallGraph.Function> allFunctions = callgraph.getAllFunctions();
    
    
    assertEquals(3, allFunctions.size());

    assertTrue(allFunctions.contains(callgraph.getUniqueFunctionWithName("A")));
    assertTrue(
        allFunctions.contains(callgraph.getUniqueFunctionWithName(null)));
    assertTrue(allFunctions.contains(callgraph.getMainFunction()));
  }

// com.google.javascript.jscomp.CallGraphTest::testGetCallsiteForAstNode
  public void testGetCallsiteForAstNode() {
    String source =
        "function A() {B()};\n" +
        "function B(){};\n";
    
    CallGraph callgraph = compileAndRunBackward(source);
    
    CallGraph.Function functionA = callgraph.getUniqueFunctionWithName("A");   
    CallGraph.Callsite callToB =
        functionA.getCallsitesInFunction().iterator().next();
    
    Node callsiteNode = callToB.getAstNode();
    
    assertEquals(callToB, callgraph.getCallsiteForAstNode(callsiteNode));
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionGetCallsites
  public void testFunctionGetCallsites() {  
    String source = 
        "function A() {var x; x()}\n" +
        "var B = function() {\n" +
        "(function C(){A()})()\n" + 
        "};\n";
      
    CallGraph callgraph = compileAndRunForward(source);
    
    CallGraph.Function functionA = callgraph.getUniqueFunctionWithName("A");  
    Collection<CallGraph.Callsite> callsitesInA =
        functionA.getCallsitesInFunction();
    
    assertEquals(1, callsitesInA.size());
    
    CallGraph.Callsite firstCallsiteInA =
        callsitesInA.iterator().next();
    
    Node aTargetExpression = firstCallsiteInA.getAstNode().getFirstChild();
    assertEquals(Token.NAME, aTargetExpression.getType());
    assertEquals("x", aTargetExpression.getString());
       
    CallGraph.Function functionB =
        callgraph.getUniqueFunctionWithName("B");
    
    Collection<CallGraph.Callsite> callsitesInB =
        functionB.getCallsitesInFunction();
    
    assertEquals(1, callsitesInB.size());
    
    CallGraph.Callsite firstCallsiteInB =
      callsitesInB.iterator().next();
    
    Node bTargetExpression = firstCallsiteInB.getAstNode().getFirstChild();
    assertEquals(Token.FUNCTION, bTargetExpression.getType());
    assertEquals("C", NodeUtil.getFunctionName(bTargetExpression));
    
    CallGraph.Function functionC =
        callgraph.getUniqueFunctionWithName("C");
    
    Collection<CallGraph.Callsite> callsitesInC =
        functionC.getCallsitesInFunction();
    assertEquals(1, callsitesInC.size());
    
    CallGraph.Callsite firstCallsiteInC =
      callsitesInC.iterator().next();
    
    Node cTargetExpression = firstCallsiteInC.getAstNode().getFirstChild();
    assertEquals(Token.NAME, aTargetExpression.getType());
    assertEquals("A", cTargetExpression.getString());
  }

// com.google.javascript.jscomp.CallGraphTest::testFindNewInFunction
  public void testFindNewInFunction() {
    String source = "function A() {var x; new x(1,2)}\n;";
      
    CallGraph callgraph = compileAndRunForward(source);
  
    CallGraph.Function functionA =
        callgraph.getUniqueFunctionWithName("A");
    Collection<CallGraph.Callsite> callsitesInA =
        functionA.getCallsitesInFunction();
    assertEquals(1, callsitesInA.size());
    
    Node callsiteInA = callsitesInA.iterator().next().getAstNode();
    assertEquals(Token.NEW, callsiteInA.getType());
    
    Node aTargetExpression = callsiteInA.getFirstChild();
    assertEquals(Token.NAME, aTargetExpression.getType());
    assertEquals("x", aTargetExpression.getString());
  }

// com.google.javascript.jscomp.CallGraphTest::testFindCallsiteTargetGlobalName
  public void testFindCallsiteTargetGlobalName() {
    String source = 
      "function A() {}\n" +
      "function B() {}\n" +
      "function C() {A()}\n";
    
    CallGraph callgraph = compileAndRunForward(source);
    
    CallGraph.Function functionC =
        callgraph.getUniqueFunctionWithName("C");
    assertNotNull(functionC);
    
    CallGraph.Callsite callsiteInC =
        functionC.getCallsitesInFunction().iterator().next();
    assertNotNull(callsiteInC);
    
    Collection<CallGraph.Function> targetsOfCallsiteInC =
        callsiteInC.getPossibleTargets();
    
    assertNotNull(targetsOfCallsiteInC);
    assertEquals(1, targetsOfCallsiteInC.size());    
  }

// com.google.javascript.jscomp.CallGraphTest::testFindCallsiteTargetAliasedGlobalProperty
  public void testFindCallsiteTargetAliasedGlobalProperty() {
    String source = 
        "var namespace = {};\n" +
        "namespace.A = function() {};\n" +
        "function C() {namespace.A()}\n";
    
    CallGraph callgraph = compileAndRunForward(source);
    
    CallGraph.Function functionC =
        callgraph.getUniqueFunctionWithName("C");
    assertNotNull(functionC);
    
    CallGraph.Callsite callsiteInC =
        functionC.getCallsitesInFunction().iterator().next();
    
    assertNotNull(callsiteInC);
    
    Collection<CallGraph.Function> targetsOfCallsiteInC =
        callsiteInC.getPossibleTargets();
    
    assertNotNull(targetsOfCallsiteInC);
    assertEquals(1, targetsOfCallsiteInC.size());    
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllCallsitesContainsMultiple
  public void testGetAllCallsitesContainsMultiple() {
    String source = 
        "function A() {}\n" +
        "var B = function() {\n" +
        "(function (){A()})()\n" + 
        "};\n" +
        "A();\n" +
        "B();\n";
    
    CallGraph callgraph = compileAndRunBackward(source);
    
    Collection<CallGraph.Callsite> allCallsites = callgraph.getAllCallsites();
    
    assertEquals(4, allCallsites.size());
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllCallsitesContainsGlobalSite
  public void testGetAllCallsitesContainsGlobalSite() {
    String source =
        "function A(){}\n" +
        "A();\n";
    
    CallGraph callgraph = compileAndRunBackward(source);
    
    Collection<CallGraph.Callsite> allCallsites = callgraph.getAllCallsites();
    assertEquals(1, allCallsites.size());
    
    Node callsiteNode = allCallsites.iterator().next().getAstNode();
    assertEquals(Token.CALL, callsiteNode.getType());
    assertEquals("A", callsiteNode.getFirstChild().getString()); 
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllCallsitesContainsLocalSite
  public void testGetAllCallsitesContainsLocalSite() {
    String source =
        "function A(){}\n" +
        "function B(){A();}\n";
  
    CallGraph callgraph = compileAndRunBackward(source);
  
    Collection<CallGraph.Callsite> allCallsites = callgraph.getAllCallsites();
    assertEquals(1, allCallsites.size());
  
    Node callsiteNode = allCallsites.iterator().next().getAstNode();
    assertEquals(Token.CALL, callsiteNode.getType());
    assertEquals("A", callsiteNode.getFirstChild().getString());
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllCallsitesContainsLiteralSite
  public void testGetAllCallsitesContainsLiteralSite() {
    String source = "function A(){(function(a){})();}\n";
    
    CallGraph callgraph = compileAndRunBackward(source);

    Collection<CallGraph.Callsite> allCallsites = callgraph.getAllCallsites();
    assertEquals(1, allCallsites.size());

    Node callsiteNode = allCallsites.iterator().next().getAstNode();
    assertEquals(Token.CALL, callsiteNode.getType());
    assertEquals(Token.FUNCTION, callsiteNode.getFirstChild().getType());    
  }

// com.google.javascript.jscomp.CallGraphTest::testGetAllCallsitesContainsConstructorSite
  public void testGetAllCallsitesContainsConstructorSite() {
    String source =
        "function A(){}\n" +
        "function B(){new A();}\n";

    CallGraph callgraph = compileAndRunBackward(source);

    Collection<CallGraph.Callsite> allCallsites = callgraph.getAllCallsites();
    assertEquals(1, allCallsites.size());

    Node callsiteNode = allCallsites.iterator().next().getAstNode();
    assertEquals(Token.NEW, callsiteNode.getType());
    assertEquals("A", callsiteNode.getFirstChild().getString());
  }

// com.google.javascript.jscomp.CallGraphTest::testGetDirectedGraph_backwardOnBackward
  public void testGetDirectedGraph_backwardOnBackward() { 
    
    
    
    
    
    String source =
        "function A(){};\n" +
        "function B(){ExternalFunction(6); C(); D();}\n" +
        "function C(){B(); A();};\n" +
        "function D(){A();};\n" +
        "function E(){C()};\n" +
        "A();\n";
        
    CallGraph callgraph = compileAndRunBackward(source);
    
    final Set<Function> poisonedFunctions = Sets.newHashSet();
    
    
    for (Callsite callsite : callgraph.getAllCallsites()) {
      if (callsite.hasExternTarget()) {
        poisonedFunctions.add(callsite.getContainingFunction());
      }
    }
    
    
    EdgeCallback<CallGraph.Function, CallGraph.Callsite> edgeCallback =
        new EdgeCallback<CallGraph.Function, CallGraph.Callsite>() {
          @Override
          public boolean traverseEdge(Function callee, Callsite callsite,
              Function caller) {
            boolean changed;
            
            if (poisonedFunctions.contains(callee)) {
              changed = poisonedFunctions.add(caller); 
            } else {
              changed = false;
            }
            
            return changed;
          }
    };
    
    FixedPointGraphTraversal.newTraversal(edgeCallback)
        .computeFixedPoint(callgraph.getBackwardDirectedGraph());  
    
    
    assertEquals(3, poisonedFunctions.size());
    
    assertTrue(poisonedFunctions.contains(
        callgraph.getUniqueFunctionWithName("B")));
    assertTrue(poisonedFunctions.contains(
        callgraph.getUniqueFunctionWithName("C")));
    assertTrue(poisonedFunctions.contains(
        callgraph.getUniqueFunctionWithName("E")));   
  }

// com.google.javascript.jscomp.CallGraphTest::testGetDirectedGraph_backwardOnForward
  public void testGetDirectedGraph_backwardOnForward() { 
    
    
    
    
    
    String source =
        "function A(){};\n" +
        "function B(){ExternalFunction(6); C(); D();}\n" +
        "function C(){B(); A();};\n" +
        "function D(){A();};\n" +
        "function E(){C()};\n" +
        "A();\n";
        
    CallGraph callgraph = compileAndRunForward(source);
    
    final Set<Function> poisonedFunctions = Sets.newHashSet();
    
    
    for (Callsite callsite : callgraph.getAllCallsites()) {
      if (callsite.hasExternTarget()) {
        poisonedFunctions.add(callsite.getContainingFunction());
      }
    }
    
    
    EdgeCallback<CallGraph.Function, CallGraph.Callsite> edgeCallback =
        new EdgeCallback<CallGraph.Function, CallGraph.Callsite>() {
          @Override
          public boolean traverseEdge(Function callee, Callsite callsite,
              Function caller) {
            boolean changed;
            
            if (poisonedFunctions.contains(callee)) {
              changed = poisonedFunctions.add(caller); 
            } else {
              changed = false;
            }
            
            return changed;
          }
    };
    
    FixedPointGraphTraversal.newTraversal(edgeCallback)
        .computeFixedPoint(callgraph.getBackwardDirectedGraph());  
    
    
    assertEquals(3, poisonedFunctions.size());
    
    assertTrue(poisonedFunctions.contains(
        callgraph.getUniqueFunctionWithName("B")));
    assertTrue(poisonedFunctions.contains(
        callgraph.getUniqueFunctionWithName("C")));
    assertTrue(poisonedFunctions.contains(
        callgraph.getUniqueFunctionWithName("E")));   
  }

// com.google.javascript.jscomp.CallGraphTest::testGetDirectedGraph_forwardOnForward
  public void testGetDirectedGraph_forwardOnForward() { 
    
    
    
    
    String source =
        "function A(){B()};\n" +
        "function B(){C();D()}\n" +
        "function C(){B()};\n" +
        "function D(){};\n" +
        "function E(){C()};\n" +
        "function X(){Y()};\n" +
        "function Y(){Z()};\n" + 
        "function Z(){};" +
        "B();\n";
        
    CallGraph callgraph = compileAndRunForward(source);
    
    final Set<Function> reachableFunctions = Sets.newHashSet();
    
    
    reachableFunctions.add(callgraph.getMainFunction());
    reachableFunctions.add(callgraph.getUniqueFunctionWithName("X"));
    
    
    
    EdgeCallback<CallGraph.Function, CallGraph.Callsite> edgeCallback =
        new EdgeCallback<CallGraph.Function, CallGraph.Callsite>() {
          @Override
          public boolean traverseEdge(Function caller, Callsite callsite,
              Function callee) {
            boolean changed;
            
            if (reachableFunctions.contains(caller)) {
              changed = reachableFunctions.add(callee); 
            } else {
              changed = false;
            }
            
            return changed;
          }
    };
    
    FixedPointGraphTraversal.newTraversal(edgeCallback)
        .computeFixedPoint(callgraph.getForwardDirectedGraph());  
    
    
    
    
    assertEquals(7, reachableFunctions.size());
  
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("B")));
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("C")));
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("D")));
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("X")));
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("Y")));
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("Z")));
    assertTrue(reachableFunctions.contains(
        callgraph.getMainFunction()));
    
    assertFalse(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("A")));
    assertFalse(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("E"))); 
  }

// com.google.javascript.jscomp.CallGraphTest::testGetDirectedGraph_forwardOnBackward
  public void testGetDirectedGraph_forwardOnBackward() { 
    
    
    
    
    String source =
        "function A(){B()};\n" +
        "function B(){C();D()}\n" +
        "function C(){B()};\n" +
        "function D(){};\n" +
        "function E(){C()};\n" +
        "function X(){Y()};\n" +
        "function Y(){Z()};\n" + 
        "function Z(){};" +
        "B();\n";
        
    CallGraph callgraph = compileAndRunBackward(source);
    
    final Set<Function> reachableFunctions = Sets.newHashSet();
    
    
    reachableFunctions.add(callgraph.getMainFunction());
    reachableFunctions.add(callgraph.getUniqueFunctionWithName("X"));
    
    
    
    EdgeCallback<CallGraph.Function, CallGraph.Callsite> edgeCallback =
        new EdgeCallback<CallGraph.Function, CallGraph.Callsite>() {
          @Override
          public boolean traverseEdge(Function caller, Callsite callsite,
              Function callee) {
            boolean changed;
            
            if (reachableFunctions.contains(caller)) {
              changed = reachableFunctions.add(callee); 
            } else {
              changed = false;
            }
            
            return changed;
          }
    };
    
    FixedPointGraphTraversal.newTraversal(edgeCallback)
        .computeFixedPoint(callgraph.getForwardDirectedGraph());  
    
    
    
    
    assertEquals(7, reachableFunctions.size());
  
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("B")));
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("C")));
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("D")));
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("X")));
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("Y")));
    assertTrue(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("Z")));
    assertTrue(reachableFunctions.contains(
        callgraph.getMainFunction()));
    
    assertFalse(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("A")));
    assertFalse(reachableFunctions.contains(
        callgraph.getUniqueFunctionWithName("E"))); 
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionIsMain
  public void testFunctionIsMain() {
    String source =
        "function A(){};\n" +
        "A();\n";
      
    CallGraph callgraph = compileAndRunForward(source);

    CallGraph.Function mainFunction = callgraph.getMainFunction();
    
    assertTrue(mainFunction.isMain());
    assertNotNull(mainFunction.getBodyNode());
    assertTrue(mainFunction.getBodyNode().getType() == Token.BLOCK);
    
    CallGraph.Function functionA = callgraph.getUniqueFunctionWithName("A");
    
    assertFalse(functionA.isMain());
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionGetAstNode
  public void testFunctionGetAstNode() {
    String source =
        "function A(){};\n" +
        "A();\n";
      
    CallGraph callgraph = compileAndRunForward(source);
  
    CallGraph.Function mainFunction = callgraph.getMainFunction();
    
    
    assertTrue(mainFunction.getAstNode().getType() == Token.BLOCK);
    
    CallGraph.Function functionA = callgraph.getUniqueFunctionWithName("A");
    
    
    assertTrue(functionA.getAstNode().getType() == Token.FUNCTION);
    assertEquals("A", NodeUtil.getFunctionName(functionA.getAstNode()));
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionGetBodyNode
  public void testFunctionGetBodyNode() {
    String source =
        "function A(){};\n" +
        "A();\n";
      
    CallGraph callgraph = compileAndRunForward(source);
  
    CallGraph.Function mainFunction = callgraph.getMainFunction();
    
    
    assertEquals(mainFunction.getAstNode(), mainFunction.getBodyNode());
    
    CallGraph.Function functionA = callgraph.getUniqueFunctionWithName("A");
    
    
    assertTrue(functionA.getBodyNode().getType() == Token.BLOCK);
    assertEquals(NodeUtil.getFunctionBody(functionA.getAstNode()),
        functionA.getBodyNode());
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionGetName
  public void testFunctionGetName() {
    String source =
        "function A(){};\n" +
        "A();\n";
      
    CallGraph callgraph = compileAndRunForward(source);
  
    CallGraph.Function mainFunction = callgraph.getMainFunction();
    
    
    assertEquals(CallGraph.MAIN_FUNCTION_NAME, mainFunction.getName());
    
    CallGraph.Function functionA = callgraph.getUniqueFunctionWithName("A");
    
    
    assertEquals(NodeUtil.getFunctionName(functionA.getAstNode()),
        functionA.getName());
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionGetCallsitesInFunction
  public void testFunctionGetCallsitesInFunction() {
    String source =
        "function A(){};\n" +
        "function B(){A()};\n" +
        "A();\n" +
        "B();\n";
      
    CallGraph callgraph = compileAndRunForward(source);
  
    
    CallGraph.Function mainFunction = callgraph.getMainFunction();
    List<String> callsiteNamesInMain =
        getCallsiteTargetNames(mainFunction.getCallsitesInFunction());
     
    assertEquals(2, callsiteNamesInMain.size());
    assertTrue(callsiteNamesInMain.contains("A"));    
    assertTrue(callsiteNamesInMain.contains("B"));
    
    
    CallGraph.Function functionA = callgraph.getUniqueFunctionWithName("A");  
    assertEquals(0, functionA.getCallsitesInFunction().size());
    
    
    CallGraph.Function functionB = callgraph.getUniqueFunctionWithName("B");
    List<String> callsiteNamesInB =
        getCallsiteTargetNames(functionB.getCallsitesInFunction());
    
    assertEquals(1, callsiteNamesInB.size());
    assertTrue(callsiteNamesInMain.contains("A"));
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionGetCallsitesInFunction_ignoreInnerFunction
  public void testFunctionGetCallsitesInFunction_ignoreInnerFunction() {
    String source =
        "function A(){var B = function(){C();}};\n" +
        "function C(){};\n";
      
    CallGraph callgraph = compileAndRunForward(source);
  
    
    CallGraph.Function functionA = callgraph.getUniqueFunctionWithName("A");  
    assertEquals(0, functionA.getCallsitesInFunction().size());
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionGetCallsitesPossiblyTargetingFunction
  public void testFunctionGetCallsitesPossiblyTargetingFunction() {
    String source =
        "function A(){B()};\n" +
        "function B(){C();C();};\n" +
        "function C(){C()};\n" +
        "A();\n";
    
    CallGraph callgraph = compileAndRunBackward(source);
    
    Function main = callgraph.getMainFunction();
    Function functionA = callgraph.getUniqueFunctionWithName("A");
    Function functionB = callgraph.getUniqueFunctionWithName("B");
    Function functionC = callgraph.getUniqueFunctionWithName("C");
    
    assertEquals(0, main.getCallsitesPossiblyTargetingFunction().size());
    
    Collection<Callsite> callsitesTargetingA =
        functionA.getCallsitesPossiblyTargetingFunction();
       
    
    assertEquals(1, callsitesTargetingA.size());
    assertEquals(main,
        callsitesTargetingA.iterator().next().getContainingFunction());
    
    Collection<Callsite> callsitesTargetingB =
      functionB.getCallsitesPossiblyTargetingFunction();
  
    
    assertEquals(1, callsitesTargetingB.size());
    assertEquals(functionA,
        callsitesTargetingB.iterator().next().getContainingFunction());
    
    Collection<Callsite> callsitesTargetingC =
      functionC.getCallsitesPossiblyTargetingFunction();
    
    
    assertEquals(3, callsitesTargetingC.size());
    
    Collection<Callsite> expectedFunctionsCallingC =
        Sets.newHashSet(functionB.getCallsitesInFunction());
    expectedFunctionsCallingC.addAll(functionC.getCallsitesInFunction());
    
    assertTrue(callsitesTargetingC.containsAll(expectedFunctionsCallingC)); 
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionGetCallsitesInFunction_newIsCallsite
  public void testFunctionGetCallsitesInFunction_newIsCallsite() {
    String source =
        "function A(){};\n" +
        "function C(){new A()};\n";
      
    CallGraph callgraph = compileAndRunForward(source);
  
    
    CallGraph.Function functionC = callgraph.getUniqueFunctionWithName("C");  
    assertEquals(1, functionC.getCallsitesInFunction().size());
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionGetIsAliased
  public void testFunctionGetIsAliased() { 
    
    String source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "var D = function() {}\n" +
        "var aliasA = A;\n" +
        "var aliasB = ns.B;\n" +
        "var aliasC = C;\n" +
        "D();";
      
    compileAndRunForward(source);
  
    assertFunctionAliased(true, "A");
    assertFunctionAliased(true, "ns.B");
    assertFunctionAliased(true, "C");
    assertFunctionAliased(false, "D");
    
    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "ns.D = function() {}\n" +
        "var aliasA;\n" +
        "aliasA = A;\n" +
        "var aliasB = {};\n" +
        "aliasB.foo = ns.B;\n" +
        "var aliasC;\n" +
        "aliasC = C;\n" +
        "ns.D();";
      
    compileAndRunForward(source);
  
    assertFunctionAliased(true, "A");
    assertFunctionAliased(true, "ns.B");
    assertFunctionAliased(true, "C");
    assertFunctionAliased(false, "ns.D");
    
    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "function D() {}\n" +
        "var foo = function(a) {}\n" +
        "foo(A);\n" +
        "foo(ns.B)\n" +
        "foo(C);\n" +
        "D();";
      
    compileAndRunForward(source);
  
    assertFunctionAliased(true, "A");
    assertFunctionAliased(true, "ns.B");
    assertFunctionAliased(true, "C");
    assertFunctionAliased(false, "D");
    
    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "A();\n" +
        "ns.B();\n" +
        "C();\n";
        
    compileAndRunForward(source);
    
    assertFunctionAliased(false, "A");
    assertFunctionAliased(false, "ns.B");
    assertFunctionAliased(false, "C");
    
    
    source =
        "function A(){};\n" +
        "var ns = {};\n" +
        "ns.B = function() {};\n" +
        "var C = function() {}\n" +
        "A.foo;\n" +
        "ns.B.prototype;\n" +
        "C[0];\n";
        
    compileAndRunForward(source);
    
    assertFunctionAliased(false, "A");
    assertFunctionAliased(false, "ns.B");
    assertFunctionAliased(false, "C");
  }

// com.google.javascript.jscomp.CallGraphTest::testFunctionGetIsExposedToCallOrApply
  public void testFunctionGetIsExposedToCallOrApply() { 
    
    String source =
        "function A(){};\n" +
        "function B(){};\n" +
        "function C(){};\n" +
        "var x;\n" +
        "A.call(x);\n" +
        "B.apply(x);\n" +
        "C();\n";
    
    CallGraph callGraph = compileAndRunForward(source);
  
    Function functionA = callGraph.getUniqueFunctionWithName("A");
    Function functionB = callGraph.getUniqueFunctionWithName("B");
    Function functionC = callGraph.getUniqueFunctionWithName("C");
    
    assertTrue(functionA.isExposedToCallOrApply());
    assertTrue(functionB.isExposedToCallOrApply());
    assertFalse(functionC.isExposedToCallOrApply());
  }

// com.google.javascript.jscomp.CallGraphTest::testCallsiteGetAstNode
  public void testCallsiteGetAstNode() {
    String source =
      "function A(){B()};\n" +
      "function B(){};\n";
  
    CallGraph callgraph = compileAndRunForward(source);
    
    Function functionA = callgraph.getUniqueFunctionWithName("A");   
    Callsite callToB = functionA.getCallsitesInFunction().iterator().next();
    
    assertTrue(callToB.getAstNode().getType() == Token.CALL);
  }

// com.google.javascript.jscomp.CallGraphTest::testCallsiteGetContainingFunction
  public void testCallsiteGetContainingFunction() {
    String source =
      "function A(){B()};\n" +
      "function B(){};\n" +
      "A();\n";
  
    CallGraph callgraph = compileAndRunForward(source);
    
    Function mainFunction = callgraph.getMainFunction();
    Callsite callToA = mainFunction.getCallsitesInFunction().iterator().next();
    assertEquals(mainFunction, callToA.getContainingFunction());
    
    Function functionA = callgraph.getUniqueFunctionWithName("A");
    Callsite callToB = functionA.getCallsitesInFunction().iterator().next();
    assertEquals(functionA, callToB.getContainingFunction());
  }

// com.google.javascript.jscomp.CallGraphTest::testCallsiteGetKnownTargets
  public void testCallsiteGetKnownTargets() {
    String source =
      "function A(){B()};\n" +
      "function B(){};\n" +
      "A();\n";
  
    CallGraph callgraph = compileAndRunForward(source);
    
    Function mainFunction = callgraph.getMainFunction();
    Function functionA = callgraph.getUniqueFunctionWithName("A");
    Function functionB = callgraph.getUniqueFunctionWithName("B");
    
    Callsite callInMain = mainFunction.getCallsitesInFunction().iterator()
        .next();
    
    Collection<Function> targetsOfCallInMain = callInMain.getPossibleTargets();
    
    assertEquals(1, targetsOfCallInMain.size());
    assertTrue(targetsOfCallInMain.contains(functionA));
    
    Callsite callInA = functionA.getCallsitesInFunction().iterator().next();
    Collection<Function> targetsOfCallInA = callInA.getPossibleTargets();
    
    assertTrue(targetsOfCallInA.contains(functionB));
  }

// com.google.javascript.jscomp.CallGraphTest::testCallsiteHasUnknownTarget
  public void testCallsiteHasUnknownTarget() {
    String source =
      "var A = externalnamespace.prop;\n" +
      "function B(){A();};\n" +
      "B();\n";
  
    CallGraph callgraph = compileAndRunForward(source);
    
    Function mainFunction = callgraph.getMainFunction();
    Function functionB = callgraph.getUniqueFunctionWithName("B");
    
    Callsite callInMain =
        mainFunction.getCallsitesInFunction().iterator().next();
    
    
    assertFalse(callInMain.hasUnknownTarget());
    assertEquals("B", callInMain.getAstNode().getFirstChild().getString());
        
    Callsite callInB = functionB.getCallsitesInFunction().iterator().next();
    
    
    assertTrue(callInB.hasUnknownTarget());
    assertEquals(0, callInB.getPossibleTargets().size());
  }

// com.google.javascript.jscomp.CallGraphTest::testCallsiteHasExternTarget
  public void testCallsiteHasExternTarget() {
    String source =
      "var A = function(){}\n" +
      "function B(){ExternalFunction(6);};\n" +
      "A();\n";
  
    CallGraph callgraph = compileAndRunForward(source);
    
    Function mainFunction = callgraph.getMainFunction();
    Function functionB = callgraph.getUniqueFunctionWithName("B");
    
    Callsite callInMain =
        mainFunction.getCallsitesInFunction().iterator().next();
    
    
    assertFalse(callInMain.hasExternTarget());
    
    Callsite callInB = functionB.getCallsitesInFunction().iterator().next();
    
    assertEquals("ExternalFunction",
        callInB.getAstNode().getFirstChild().getString());
    
    
    assertTrue(callInB.hasExternTarget());
    assertEquals(0, callInB.getPossibleTargets().size());  
  }

// com.google.javascript.jscomp.CallGraphTest::testThrowForBackwardOpOnForwardGraph
  public void testThrowForBackwardOpOnForwardGraph() {
    String source =
      "function A(){B()};\n" +
      "function B(){C();C();};\n" +
      "function C(){C()};\n" +
      "A();\n";

    CallGraph callgraph = compileAndRunForward(source);

    Function functionA = callgraph.getUniqueFunctionWithName("A");

    UnsupportedOperationException caughtException = null;
    
    try {
      functionA.getCallsitesPossiblyTargetingFunction();
    } catch (UnsupportedOperationException e) {
      caughtException = e;
    }
   
    assertNotNull(caughtException);    
  }

// com.google.javascript.jscomp.CallGraphTest::testThrowForForwardOpOnBackwardGraph
  public void testThrowForForwardOpOnBackwardGraph() {
    String source =
      "function A(){B()};\n" +
      "function B(){};\n" +
      "A();\n";
  
    CallGraph callgraph = compileAndRunBackward(source);
    
    Function mainFunction = callgraph.getMainFunction();
    Function functionA = callgraph.getUniqueFunctionWithName("A");
    
    Callsite callInMain = mainFunction.getCallsitesInFunction().iterator()
        .next();
      
    UnsupportedOperationException caughtException = null;
    
    try {
      callInMain.getPossibleTargets();
    } catch (UnsupportedOperationException e) {
      return;
    }
    fail();   
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
             " SubFoo.prototype.bar = " +
             "function() {};" +
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
      "SubFoo.prototype.bar_ = function() {};",
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

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty1
  public void testConstantProperty1() {
    test(" function A() {" +
        " this.bar = 3;}" +
        " function B() {" +
        " this.bar = 3;this.bar += 4;}",
        null, CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty2
  public void testConstantProperty2() {
    test(" function Foo() {}" +
        " Foo.prototype.prop = 2;" +
        "var foo = new Foo();" +
        "foo.prop = 3;",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty3
  public void testConstantProperty3() {
    testSame("var o = {  x: 1 };" +
        "o.x = 2;");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty4
  public void testConstantProperty4() {
    test(" function cat(name) {}" +
        " cat.test = 1;" +
        "cat.test *= 2;",
        null, CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty5
  public void testConstantProperty5() {
    test(" function Foo() { this.prop = 1;}" +
        " Foo.prototype.prop;" +
        "Foo.prototype.prop = 2",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty6
  public void testConstantProperty6() {
    test(" function Foo() { this.prop = 1;}" +
        " Foo.prototype.prop = 2;",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty7
  public void testConstantProperty7() {
    testSame(" function Foo() {} " +
      "Foo.prototype.bar_ = function() {};" +
      " " +
      "function SubFoo() {};" +
      "  SubFoo.prototype.bar_ = function() {};" +
      "SubFoo.prototype.baz = function() { this.bar_(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty8
  public void testConstantProperty8() {
    testSame("var o = {  x: 1 };" +
        "var y = o.x;");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty9
  public void testConstantProperty9() {
    testSame(" function A() {" +
        " this.bar = 3;}" +
        " function B() {" +
        "this.bar = 4;}");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty10
  public void testConstantProperty10() {
    testSame(" function Foo() { this.prop = 1;}" +
        " Foo.prototype.prop;");
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
    testSame(GET_NAMES + "alert(a.b); alert(a.c.e);");
    testSame(SET_NAMES + "alert(a.b); alert(a.c.e);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDefinedProperties2
  public void testRefToDefinedProperties2() {
    testSame(NAMES + "a.x={}; alert(a.c);");
    testSame(GET_NAMES + "a.x={}; alert(a.c);");
    testSame(SET_NAMES + "a.x={}; alert(a.c);");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToDefinedProperties3
  public void testRefToDefinedProperties3() {
    testSame(NAMES + "alert(a.d);");
    testSame(GET_NAMES + "alert(a.d);");
    testSame(SET_NAMES + "alert(a.d);");
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
    testSame(GET_NAMES + "alert(a.c.x);", UNDEFINED_NAME_WARNING);
    testSame(SET_NAMES + "alert(a.c.x);", UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testRefToUndefinedProperty4
  public void testRefToUndefinedProperty4() {
    testSame(NAMES + "alert(a.d.x);");
    testSame(GET_NAMES + "alert(a.d.x);");
    testSame(SET_NAMES + "alert(a.d.x);");
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

// com.google.javascript.jscomp.CheckGlobalThisTest::testGlobalThis7
  public void testGlobalThis7() {
    testFailure("var a = this.foo;");
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
    testSame("function a() { return function() { this.x = 8; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction7
  public void testStaticFunction7() {
    testSame("var a = function() { return function() { this.x = 8; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticFunction8
  public void testStaticFunction8() {
    testFailure("var a = function() { return this.foo; };");
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

// com.google.javascript.jscomp.CheckGlobalThisTest::testInterface1
  public void testInterface1() {
    testSame(
        "function A() {  this.m2; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testOverride1
  public void testOverride1() {
    testSame("function A() { } var a = new A();" +
             " a.foo = function() { this.bar = 5; };");
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
    testSame("function f() { this.foo = 56; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testThisJSDoc5
  public void testThisJSDoc5() throws Exception {
    testSame("function a() { function f() { this.foo = 56; } }");
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

// com.google.javascript.jscomp.CheckGlobalThisTest::testMethod4
  public void testMethod4() {
    testSame("a.prototype['x' + 'y'] =  function() { this.foo = 3; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testPropertyOfMethod
  public void testPropertyOfMethod() {
    testFailure("a.protoype.b = {}; " +
        "a.prototype.b.c = function() { this.foo = 3; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod1
  public void testStaticMethod1() {
    testFailure("a.b = function() { this.m2 = 5; }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod2
  public void testStaticMethod2() {
    testSame("a.b = function() { return function() { this.m2 = 5; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testStaticMethod3
  public void testStaticMethod3() {
    testSame("a.b.c = function() { return function() { this.m2 = 5; } }");
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

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction1
  public void testInnerFunction1() {
    testFailure("function f() { function g() { return this.x; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction2
  public void testInnerFunction2() {
    testFailure("function f() { var g = function() { return this.x; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction3
  public void testInnerFunction3() {
    testFailure(
        "function f() { var x = {}; x.y = function() { return this.x; } }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testInnerFunction4
  public void testInnerFunction4() {
    testSame(
        "function f() { var x = {}; x.y(function() { return this.x; }); }");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182a
  public void testIssue182a() {
    testFailure("var NS = {read: function() { return this.foo; }};");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182b
  public void testIssue182b() {
    testFailure("var NS = {write: function() { this.foo = 3; }};");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182c
  public void testIssue182c() {
    testFailure("var NS = {}; NS.write2 = function() { this.foo = 3; };");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testIssue182d
  public void testIssue182d() {
    testSame("function Foo() {} " +
        "Foo.prototype = {write: function() { this.foo = 3; }};");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testLendsAnnotation1
  public void testLendsAnnotation1() {
    testFailure(" function F() {}" +
        "dojo.declare(F, {foo: function() { return this.foo; }});");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testLendsAnnotation2
  public void testLendsAnnotation2() {
    testFailure(" function F() {}" +
        "dojo.declare(F,  (" +
        "    {foo: function() { return this.foo; }}));");
  }

// com.google.javascript.jscomp.CheckGlobalThisTest::testLendsAnnotation3
  public void testLendsAnnotation3() {
    testSame(" function F() {}" +
        "dojo.declare(F,  (" +
        "    {foo: function() { return this.foo; }}));");
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
    testNotMissing("var s = joe.random.getUniqueId('joe-is-a-goob')");
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

// com.google.javascript.jscomp.CheckPropertyOrderTest::testNoBranches
  public void testNoBranches() {
    testSame("var a = {};"
             + ""
             + "a.F = function() {"
             + "  this.a = 'a';"
             + "  this.b = 3;"
             + "  this.c = null;"
             + "};");
  }

// com.google.javascript.jscomp.CheckPropertyOrderTest::testIfBranchDifference
  public void testIfBranchDifference() {
    testSame("var a = {};"
             + ""
             + "a.F = function(a) {"
             + "  if (a < 10) {"
             + "    this.a = a;"
             + "  }"
             + "};",
             CheckPropertyOrder.UNASSIGNED_PROPERTY);
  }

// com.google.javascript.jscomp.CheckPropertyOrderTest::testIfBranchNoDifference
  public void testIfBranchNoDifference() {
    testSame("var a = {};"
             + ""
             + "a.F = function(a) {"
             + "  if (a < 10) {"
             + "    this.a = a;"
             + "  } else {"
             + "    this.a = 10;"
             + "  }"
             + "};");
  }

// com.google.javascript.jscomp.CheckPropertyOrderTest::testHookBranchDifference
  public void testHookBranchDifference() {
    testSame("var a = {};"
             + ""
             + "a.F = function(a) {"
             + "  var b = (a < 10) ? (this.a = 1) : 2"
             + "};",
             CheckPropertyOrder.UNASSIGNED_PROPERTY);
  }

// com.google.javascript.jscomp.CheckPropertyOrderTest::testHookBranchNoDifference
  public void testHookBranchNoDifference() {
    testSame("var a = {};"
             + ""
             + "a.F = function(a) {"
             + "  var b = (a < 10) ? (this.a = 1) : (this.a = 2)"
             + "};");
  }

// com.google.javascript.jscomp.CheckPropertyOrderTest::testAndBranchDifference
  public void testAndBranchDifference() {
    testSame("var a = {};"
             + ""
             + "a.F = function(a) {"
             + "  (a < 10) && (this.a = 1);"
             + "};",
             CheckPropertyOrder.UNASSIGNED_PROPERTY);
  }

// com.google.javascript.jscomp.CheckPropertyOrderTest::testOrBranchDifference
  public void testOrBranchDifference() {
    testSame("var a = {};"
             + ""
             + "a.F = function(a) {"
             + "  (a < 10) || (this.a = 1);"
             + "};",
             CheckPropertyOrder.UNASSIGNED_PROPERTY);
  }
