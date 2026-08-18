// buggy code
  private void updateSimpleDeclaration(String alias, Name refName, Ref ref) {
    Node rvalue = ref.node.getNext();
    Node parent = ref.node.getParent();
    Node gramps = parent.getParent();
    Node greatGramps = gramps.getParent();
    Node greatGreatGramps = greatGramps.getParent();


    // Create the new alias node.
    Node nameNode = NodeUtil.newName(
        compiler.getCodingConvention(), alias, gramps.getFirstChild(),
        refName.fullName());
    NodeUtil.copyNameAnnotations(ref.node.getLastChild(), nameNode);

    if (gramps.getType() == Token.EXPR_RESULT) {
      // BEFORE: a.b.c = ...;
      //   exprstmt
      //     assign
      //       getprop
      //         getprop
      //           name a
      //           string b
      //         string c
      //       NODE
      // AFTER: var a$b$c = ...;
      //   var
      //     name a$b$c
      //       NODE

      // Remove the rvalue (NODE).
      parent.removeChild(rvalue);
      nameNode.addChildToFront(rvalue);

      Node varNode = new Node(Token.VAR, nameNode);
      greatGramps.replaceChild(gramps, varNode);
    } else {
      // This must be a complex assignment.
      Preconditions.checkNotNull(ref.getTwin());

      // BEFORE:
      // ... (x.y = 3);
      //
      // AFTER:
      // var x$y;
      // ... (x$y = 3);

      Node current = gramps;
      Node currentParent = gramps.getParent();
      for (; currentParent.getType() != Token.SCRIPT &&
             currentParent.getType() != Token.BLOCK;
           current = currentParent,
           currentParent = currentParent.getParent()) {}

      // Create a stub variable declaration right
      // before the current statement.
      Node stubVar = new Node(Token.VAR, nameNode.cloneTree())
          .copyInformationFrom(nameNode);
      currentParent.addChildBefore(stubVar, current);

      parent.replaceChild(ref.node, nameNode);
    }

    compiler.reportCodeChange();
  }

    boolean canCollapseUnannotatedChildNames() {
      if (type == Type.OTHER || globalSets != 1 || localSets != 0) {
        return false;
      }

      // Don't try to collapse if the one global set is a twin reference.
      // We could theoretically handle this case in CollapseProperties, but
      // it's probably not worth the effort.
      Preconditions.checkNotNull(declaration);
      if (declaration.getTwin() != null) {
        return false;
      }

      if (isClassOrEnum) {
        return true;
      }

      // If this is a key of an aliased object literal, then it will be aliased
      // later. So we won't be able to collapse its properties.
      if (parent != null && parent.shouldKeepKeys()) {
        return false;
      }

      // If this is aliased, then its properties can't be collapsed either.
      if (type != Type.FUNCTION && aliasingGets > 0) {
        return false;
      }

      return (parent == null || parent.canCollapseUnannotatedChildNames());
    }

// relevant test
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
          " function a() {} var c = a; " +
          "(function() {a.b = 0;})(); a.b;");
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

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects4
  public void testEnumOfObjects4() {
    
    
    
    test(
        COMMON_ENUM +
        "for (var key in Foo) {} Foo.A = 3; alert(Foo.A);",
        "var Foo$A = {c: 2}; var Foo$B = {c: 3};" +
        "var Foo = {A: Foo$A, B: Foo$B};" +
        "for (var key in Foo) {} Foo$A = 3; alert(Foo$A);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjectOfObjects1
  public void testObjectOfObjects1() {
    
    
    testSame(
        "var Foo = {a: {c: 2}, b: {c: 3}};" +
        "for (var key in Foo) {} Foo.a = 3; alert(Foo.a);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject0
  public void testReferenceInAnonymousObject0() {
    test("var a = {};" +
         "a.b = function(){};" +
         "a.b.prototype.c = function(){};" +
         "var d = a.b.prototype.c;",
         "var a$b = function(){};" +
         "a$b.prototype.c = function(){};" +
         "var d = a$b.prototype.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject1
  public void testReferenceInAnonymousObject1() {
    test("var a = {};" +
         "a.b = function(){};" +
         "var d = a.b.prototype.c;",
         "var a$b = function(){};" +
         "var d = a$b.prototype.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject2
  public void testReferenceInAnonymousObject2() {
    test("var a = {};" +
         "a.b = function(){};" +
         "a.b.prototype.c = function(){};" +
         "var d = {c: a.b.prototype.c};",
         "var a$b = function(){};" +
         "a$b.prototype.c = function(){};" +
         "var d$c = a$b.prototype.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject3
  public void testReferenceInAnonymousObject3() {
    test("function CreateClass(a$$1) {}" +
         "var a = {};" +
         "a.b = function(){};" +
         "a.b.prototype.c = function(){};" +
         "a.d = CreateClass({c: a.b.prototype.c});",
         "function CreateClass(a$$1) {}" +
         "var a$b = function(){};" +
         "a$b.prototype.c = function(){};" +
         "var a$d = CreateClass({c: a$b.prototype.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject4
  public void testReferenceInAnonymousObject4() {
    test("function CreateClass(a) {}" +
         "var a = {};" +
         "a.b = CreateClass({c: function() {}});" +
         "a.d = CreateClass({c: a.b.c});",
         "function CreateClass(a$$1) {}" +
         "var a$b = CreateClass({c: function() {}});" +
         "var a$d = CreateClass({c: a$b.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject5
  public void testReferenceInAnonymousObject5() {
    test("function CreateClass(a) {}" +
         "var a = {};" +
         "a.b = CreateClass({c: function() {}});" +
         "a.d = CreateClass({c: a.b.prototype.c});",
         "function CreateClass(a$$1) {}" +
         "var a$b = CreateClass({c: function() {}});" +
         "var a$d = CreateClass({c: a$b.prototype.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCrashInCommaOperator
  public void testCrashInCommaOperator() {
    test("var a = {}; a.b = function() {},a.b();",
         "var a$b; a$b=function() {},a$b();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCrashInNestedAssign
  public void testCrashInNestedAssign() {
    test("var a = {}; if (a.b = function() {}) a.b();",
         "var a$b; if (a$b=function() {}) { a$b(); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTwinReferenceCancelsChildCollapsing
  public void testTwinReferenceCancelsChildCollapsing() {
    test("var a = {}; if (a.b = function() {}) { a.b.c = 3; a.b(a.b.c); }",
         "var a$b; if (a$b = function() {}) { a$b.c = 3; a$b(a$b.c); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign
  public void testPropWithDollarSign() {
    test("var a = {$: 3}", "var a$$0 = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign2
  public void testPropWithDollarSign2() {
    test("var a = {$: function(){}}", "var a$$0 = function(){};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign3
  public void testPropWithDollarSign3() {
    test("var a = {b: {c: 3}, b$c: function(){}}",
         "var a$b$c = 3; var a$b$0c = function(){};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign4
  public void testPropWithDollarSign4() {
    test("var a = {$$: {$$$: 3}};", "var a$$0$0$$0$0$0 = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign5
  public void testPropWithDollarSign5() {
    test("var a = {b: {$0c: true}, b$0c: false};",
         "var a$b$$00c = true; var a$b$00c = false;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testConstKey
  public void testConstKey() {
    test("var foo = {A: 3};", "var foo$A = 3;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOffByDefault
  public void testTypeCheckingOffByDefault() {
    test("function f(x) { return x; } f();",
         "function f(a) { return a; } f();");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOnWithVerbose
  public void testTypeCheckingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test("function f(x) { return x; } f();", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride1
  public void testTypeCheckOverride1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=checkTypes");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride2
  public void testTypeCheckOverride2() {
    args.add("--warning_level=DEFAULT");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");

    args.add("--jscomp_warning=checkTypes");
    test("var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOffForDefault
  public void testCheckSymbolsOffForDefault() {
    args.add("--warning_level=DEFAULT");
    test("x = 3; var y; var y;", "x=3; var y;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOnForVerbose
  public void testCheckSymbolsOnForVerbose() {
    args.add("--warning_level=VERBOSE");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
    test("var y; var y;", SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForVerbose
  public void testCheckSymbolsOverrideForVerbose() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=undefinedVars");
    testSame("x = 3;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties1
  public void testCheckUndefinedProperties1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_error=missingProperties");
    test("var x = {}; var y = x.bar;", TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties2
  public void testCheckUndefinedProperties2() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=missingProperties");
    test("var x = {}; var y = x.bar;", CheckGlobalNames.UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties3
  public void testCheckUndefinedProperties3() {
    args.add("--warning_level=VERBOSE");
    test("function f() {var x = {}; var y = x.bar;}",
        TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDuplicateParams
  public void testDuplicateParams() {
    test("function (a, a) {}", RhinoErrorReporter.DUPLICATE_PARAM);
    assertTrue(lastCompiler.hasHaltingErrors());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag
  public void testDefineFlag() {
    args.add("--define=FOO");
    args.add("--define=\"BAR=5\"");
    args.add("--D"); args.add("CCC");
    args.add("-D"); args.add("DDD");
    test(" var FOO = false;" +
         " var BAR = 3;" +
         " var CCC = false;" +
         " var DDD = false;",
         "var FOO = true, BAR = 5, CCC = true, DDD = true;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag2
  public void testDefineFlag2() {
    args.add("--define=FOO='x\"'");
    test(" var FOO = \"a\";",
         "var FOO = \"x\\\"\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag3
  public void testDefineFlag3() {
    args.add("--define=FOO=\"x'\"");
    test(" var FOO = \"a\";",
         "var FOO = \"x'\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testScriptStrictModeNoWarning
  public void testScriptStrictModeNoWarning() {
    test("'use strict';", "");
    test("'no use strict';", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testFunctionStrictModeNoWarning
  public void testFunctionStrictModeNoWarning() {
    test("function f() {'use strict';}", "function f() {}");
    test("function f() {'no use strict';}",
         CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testQuietMode
  public void testQuietMode() {
    args.add("--warning_level=DEFAULT");
    test(" var x;",
         RhinoErrorReporter.PARSE_ERROR);
    args.add("--warning_level=QUIET");
    testSame(" var x;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessClosurePrimitives
  public void testProcessClosurePrimitives() {
    test("var goog = {}; goog.provide('goog.dom');",
         "var goog = {}; goog.dom = {};");
    args.add("--process_closure_primitives=false");
    testSame("var goog = {}; goog.provide('goog.dom');");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue70
  public void testIssue70() {
    test("function foo({}) {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue81
  public void testIssue81() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    useStringComparison = true;
    test("eval('1'); var x = eval; x('2');",
         "eval(\"1\");(0,eval)(\"2\");");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue115
  public void testIssue115() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--warning_level=VERBOSE");
    test("function f() { " +
         "  var arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}",
         "function f() { " +
         "  arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag1
  public void testDebugFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=false");
    test("function foo(a) {}",
         "function foo() {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag2
  public void testDebugFlag2() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=true");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag3
  public void testDebugFlag3() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=false");
    test("function Foo() {}" +
         "Foo.x = 1;" +
         "function f() {throw new Foo().x;} f();",
         "throw (new function() {}).a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag4
  public void testDebugFlag4() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=true");
    test("function Foo() {}" +
        "Foo.x = 1;" +
        "function f() {throw new Foo().x;} f();",
        "throw (new function Foo() {}).$x$;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag1
  public void testBooleanFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag2
  public void testBooleanFlag2() {
    args.add("--debug");
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testHelpFlag
  public void testHelpFlag() {
    args.add("--help");
    assertFalse(
        createCommandLineRunner(
            new String[] {"function f() {}"}).shouldRunCompiler());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting1
  public void testExternsLifting1() throws Exception{
    String code = " function f() {}";
    test(new String[] {code},
         new String[] {});

    assertEquals(2, lastCompiler.getExternsForTesting().size());

    CompilerInput extern = lastCompiler.getExternsForTesting().get(1);
    assertNull(extern.getModule());
    assertTrue(extern.isExtern());
    assertEquals(code, extern.getCode());

    assertEquals(1, lastCompiler.getInputsForTesting().size());

    CompilerInput input = lastCompiler.getInputsForTesting().get(0);
    assertNotNull(input.getModule());
    assertFalse(input.isExtern());
    assertEquals("", input.getCode());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting2
  public void testExternsLifting2() {
    args.add("--warning_level=VERBOSE");
    test(new String[] {" function f() {}", "f(3);"},
         new String[] {"f(3);"},
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOff
  public void testSourceSortingOff() {
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         }, ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn
  public void testSourceSortingOn() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps1
  public void testSourceSortingCircularDeps1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps2
  public void testSourceSortingCircularDeps2() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('roses.lime.juice');",
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');",
          "goog.provide('gimlet');" +
          "     goog.require('gin'); goog.require('roses.lime.juice');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn1
  public void testSourcePruningOn1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn2
  public void testSourcePruningOn2() {
    args.add("--closure_entry_point=guinness");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var guinness = {};"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn3
  public void testSourcePruningOn3() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn4
  public void testSourcePruningOn4() {
    args.add("--closure_entry_point=scotch");
    args.add("--closure_entry_point=beer");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn5
  public void testSourcePruningOn5() {
    args.add("--closure_entry_point=shiraz");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         Compiler.MISSING_ENTRY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn6
  public void testSourcePruningOn6() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testForwardDeclareDroppedTypes
  public void testForwardDeclareDroppedTypes() {
    args.add("--manage_closure_dependencies=true");

    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}",
          "goog.provide('Scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         });

    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         },
         RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion1
  public void testSourceMapExpansion1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    args.add("--create_source_map=%outname%.map");
    testSame("var x = 3;");
    assertEquals("/path/to/out.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion2
  public void testSourceMapExpansion2() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion3
  public void testSourceMapExpansion3() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo_");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo_m0.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(),
            lastCompiler.getModuleGraph().getRootModule()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat1
  public void testSourceMapFormat1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.LEGACY,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCharSetExpansion
  public void testCharSetExpansion() {
    testSame("");
    assertEquals("US-ASCII", lastCompiler.getOptions().outputCharset);
    args.add("--charset=UTF-8");
    testSame("");
    assertEquals("UTF-8", lastCompiler.getOptions().outputCharset);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testChainModuleManifest
  public void testChainModuleManifest() throws Exception {
    useModules = ModulePattern.CHAIN;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestTo(
        lastCompiler.getModuleGraph(), builder);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m1}\n" +
        "i2\n" +
        "\n" +
        "{m3:m2}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testStarModuleManifest
  public void testStarModuleManifest() throws Exception {
    useModules = ModulePattern.STAR;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestTo(
        lastCompiler.getModuleGraph(), builder);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m0}\n" +
        "i2\n" +
        "\n" +
        "{m3:m0}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag
  public void testVersionFlag() {
    args.add("--version");
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testPrintAstFlag
  public void testPrintAstFlag() {
    args.add("--print_ast=true");
    testSame("");
    assertEquals(
        "digraph AST {\n" +
        "  node [color=lightblue2, style=filled];\n" +
        "  node0 [label=\"BLOCK\"];\n" +
        "  node1 [label=\"SCRIPT\"];\n" +
        "  node0 -> node1 [weight=1];\n" +
        "  node1 -> RETURN [label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> RETURN [label=\"SYN_BLOCK\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> node1 [label=\"UNCOND\", fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "}\n\n",
        new String(outReader.toByteArray()));
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbol
  public void testExportSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n}");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolDefinedInVar
  public void testExportSymbolDefinedInVar() throws Exception {
    compileAndCheck("var a = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n}");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportProperty
  public void testExportProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'cprop', a.b.c)",
                    "var a = {};\n" +
                    "a.b = {};\n" +
                    "\n" +
                    "a.b.cprop = function(d, e, f) {\n}");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple
  public void testExportMultiple() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('a.b', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var a = {};\n" +
                    "\n" +
                    "a.b = function(p1) {\n};\n" +
                    "\n" +
                    "a.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "a.b.prototype.c = function(g, h, i) {\n}");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple2
  public void testExportMultiple2() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('hello', a);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var hello = {};\n" +
                    "hello.b = {};\n" +
                    "\n" +
                    "hello.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "hello.b.prototype.c = function(g, h, i) {\n}");
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
                    "prefix.c = function(d, e, f) {\n}");
  }
