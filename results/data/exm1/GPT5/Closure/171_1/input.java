// buggy code
  private FlowScope traverseObjectLiteral(Node n, FlowScope scope) {
    JSType type = n.getJSType();
    Preconditions.checkNotNull(type);

    for (Node name = n.getFirstChild(); name != null; name = name.getNext()) {
      scope = traverse(name.getFirstChild(), scope);
    }

    // Object literals can be reflected on other types.
    // See CodingConvention#getObjectLiteralCase and goog.object.reflect.
    // Ignore these types of literals.
    ObjectType objectType = ObjectType.cast(type);
    if (objectType == null) {
      return scope;
    }
    boolean hasLendsName = n.getJSDocInfo() != null &&
        n.getJSDocInfo().getLendsName() != null;
    if (objectType.hasReferenceName() && !hasLendsName) {
      return scope;
    }

    String qObjName = NodeUtil.getBestLValueName(
        NodeUtil.getBestLValue(n));
    for (Node name = n.getFirstChild(); name != null;
         name = name.getNext()) {
      String memberName = NodeUtil.getObjectLitKeyName(name);
      if (memberName != null) {
        JSType rawValueType =  name.getFirstChild().getJSType();
        JSType valueType = NodeUtil.getObjectLitKeyTypeFromValueType(
            name, rawValueType);
        if (valueType == null) {
          valueType = unknownType;
        }
        objectType.defineInferredProperty(memberName, valueType, name);

        // Do normal flow inference if this is a direct property assignment.
        if (qObjName != null && name.isStringKey()) {
          String qKeyName = qObjName + "." + memberName;
          Var var = syntacticScope.getVar(qKeyName);
          JSType oldType = var == null ? null : var.getType();
          if (var != null && var.isTypeInferred()) {
            var.setType(oldType == null ?
                valueType : oldType.getLeastSupertype(oldType));
          }

          scope.inferQualifiedSlot(name, qKeyName,
              oldType == null ? unknownType : oldType,
              valueType);
        }
      } else {
        n.setJSType(unknownType);
      }
    }
    return scope;
  }

    private boolean isQualifiedNameInferred(
        String qName, Node n, JSDocInfo info,
        Node rhsValue, JSType valueType) {
      if (valueType == null) {
        return true;
      }

      // Prototype sets are always declared.

      boolean inferred = true;
      if (info != null) {
        inferred = !(info.hasType()
            || info.hasEnumParameterType()
            || (isConstantSymbol(info, n) && valueType != null
                && !valueType.isUnknownType())
            || FunctionTypeBuilder.isFunctionTypeDeclaration(info));
      }

      if (inferred && rhsValue != null && rhsValue.isFunction()) {
        if (info != null) {
          return false;
        } else if (!scope.isDeclared(qName, false) &&
            n.isUnscopedQualifiedName()) {

          // Check if this is in a conditional block.
          // Functions assigned in conditional blocks are inferred.
          for (Node current = n.getParent();
               !(current.isScript() || current.isFunction());
               current = current.getParent()) {
            if (NodeUtil.isControlStructure(current)) {
              return true;
            }
          }

          // Check if this is assigned in an inner scope.
          // Functions assigned in inner scopes are inferred.
          AstFunctionContents contents =
              getFunctionAnalysisResults(scope.getRootNode());
          if (contents == null ||
              !contents.getEscapedQualifiedNames().contains(qName)) {
            return false;
          }
        }
      }
      return inferred;
    }

// relevant test
// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedDispose
  public void testFreedDispose() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { this.eh = new goog.events.EventHandler();"
        + "this.eh.dispose(); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedGoogDispose1
  public void testFreedGoogDispose1() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { this.eh = new goog.events.EventHandler();"
        + "goog.dispose(this.eh); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testNotAllFreedGoogDispose
  public void testNotAllFreedGoogDispose() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() {"
        + "this.eh1 = new goog.events.EventHandler();"
        + "this.eh2 = new goog.events.EventHandler();"
        + "goog.dispose(this.eh1, this.eh2); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js, CheckEventfulObjectDisposal.EVENTFUL_OBJECT_NOT_DISPOSED, true);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedGoogDisposeAll
  public void testFreedGoogDisposeAll() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { "
        + "this.eh1 = new goog.events.EventHandler();"
        + "this.eh2 = new goog.events.EventHandler();"
        + "goog.disposeAll(this.eh1, this.eh2); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedRegisterDisposable
  public void testFreedRegisterDisposable() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { this.eh = new goog.events.EventHandler();"
        + "this.registerDisposable(this.eh); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testFreedRemoveAll
  public void testFreedRemoveAll() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { this.eh = new goog.events.EventHandler();"
        + "this.eh.removeAll(); };"
        + "goog.inherits(test, goog.Disposable);"
        + "var testObj = new test();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckEventfulObjectDisposalTest::testPrivateInheritance
  public void testPrivateInheritance() {
    String js = CLOSURE_DEFS
        + ""
        + "var test = function() { "
        + " this.eh = new goog.events.EventHandler();"
        + "this.eh.removeAll(); };"
        + "goog.inherits(test, goog.Disposable);"
        + ""
        + "var subclass = function() {"
        + " this.eh = new goog.events.EventHandler();"
        + "this.eh.dispose();"
        + "};"
        + "var testObj = new test();";
    testSame(js, CheckEventfulObjectDisposal.OVERWRITE_PRIVATE_EVENTFUL_OBJECT, true);
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

// com.google.javascript.jscomp.CheckGlobalNamesTest::testSuppressionOfUndefinedNamesWarning
  public void testSuppressionOfUndefinedNamesWarning() {
    testSame(new String[] {
        NAMES +
        " function Foo() { };" +
        "" +
        "Foo.prototype.bar = function() {" +
        "  alert(a.x);" +
        "  alert(a.x.b());" +
        "  a.x();" +
        "  var c = a.x.b;" +
        "  var c = a.x.b();" +
        "  a.x.b();" +
        "  a.x.b = 3;" +
        "};",
    });
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

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName1
  public void testLateDefinedName1() {
    testSame("x.y = {}; var x = {};", NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName2
  public void testLateDefinedName2() {
    testSame("var x = {}; x.y.z = {}; x.y = {};", NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName3
  public void testLateDefinedName3() {
    testSame("var x = {}; x.y.z = {}; x.y = {z: {}};",
        NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName4
  public void testLateDefinedName4() {
    testSame("var x = {}; x.y.z.bar = {}; x.y = {z: {}};",
        NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName5
  public void testLateDefinedName5() {
    testSame("var x = {};  x.y.z; x.y = {};",
        NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testLateDefinedName6
  public void testLateDefinedName6() {
    testSame(
        "var x = {}; x.y.prototype.z = 3;" +
        " x.y = function() {};",
        NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testOkLateDefinedName1
  public void testOkLateDefinedName1() {
    testSame("function f() { x.y = {}; } var x = {};");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testOkLateDefinedName2
  public void testOkLateDefinedName2() {
    testSame("var x = {}; function f() { x.y.z = {}; } x.y = {};");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testPathologicalCaseThatsOkAnyway
  public void testPathologicalCaseThatsOkAnyway() {
    testSame(
        "var x = {};" +
        "switch (x) { " +
        "  default: x.y.z = {}; " +
        "  case (x.y = {}): break;" +
        "}", NAME_DEFINED_LATE_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testOkGlobalDeclExpr
  public void testOkGlobalDeclExpr() {
    testSame("var x = {};  x.foo;");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testBadInterfacePropRef
  public void testBadInterfacePropRef() {
    testSame(
        " function F() {}" +
         "F.bar();",
         UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testInterfaceFunctionPropRef
  public void testInterfaceFunctionPropRef() {
    testSame(
        " function F() {}" +
         "F.call(); F.hasOwnProperty('z');");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testObjectPrototypeProperties
  public void testObjectPrototypeProperties() {
    testSame("var x = {}; var y = x.hasOwnProperty('z');");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testCustomObjectPrototypeProperties
  public void testCustomObjectPrototypeProperties() {
    testSame("Object.prototype.seal = function() {};" +
        "var x = {}; x.seal();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testFunctionPrototypeProperties
  public void testFunctionPrototypeProperties() {
    testSame("var x = {}; var y = x.hasOwnProperty('z');");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testIndirectlyDeclaredProperties
  public void testIndirectlyDeclaredProperties() {
    testSame(
        "Function.prototype.inherits = function(ctor) {" +
        "  this.superClass_ = ctor;" +
        "};" +
        " function Foo() {}" +
        "Foo.prototype.bar = function() {};" +
        " function SubFoo() {}" +
        "SubFoo.inherits(Foo);" +
        "SubFoo.superClass_.bar();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testGoogInheritsAlias
  public void testGoogInheritsAlias() {
    testSame(
        "Function.prototype.inherits = function(ctor) {" +
        "  this.superClass_ = ctor;" +
        "};" +
        " function Foo() {}" +
        "Foo.prototype.bar = function() {};" +
        " function SubFoo() {}" +
        "SubFoo.inherits(Foo);" +
        "SubFoo.superClass_.bar();");
  }

// com.google.javascript.jscomp.CheckGlobalNamesTest::testGoogInheritsAlias2
  public void testGoogInheritsAlias2() {
    testSame(
        CompilerTypeTestCase.CLOSURE_DEFS +
        " function Foo() {}" +
        "Foo.prototype.bar = function() {};" +
        " function SubFoo() {}" +
        "goog.inherits(SubFoo, Foo);" +
        "SubFoo.superClazz();",
         UNDEFINED_NAME_WARNING);
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

// com.google.javascript.jscomp.CheckGlobalThisTest::testSuppressWarning
  public void testSuppressWarning() {
    testFailure("var x = function() { this.complex = 5; };");
    testSame("" +
        "var x = function() { this.complex = 5; };");
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

// com.google.javascript.jscomp.CheckMissingReturnTest::testIssue779
  public void testIssue779() {
    testNotMissing(
        "var a = f(); try { alert(); if (a > 0) return 1; }" +
        "finally { a = 5; } return 2;");
  }

// com.google.javascript.jscomp.CheckMissingReturnTest::testConstructors
  public void testConstructors() {
    testSame(" function foo() {} ");

    final String ConstructorWithReturn = " function foo() {" +
        " if (!(this instanceof foo)) { return new foo; } }";
    testSame(ConstructorWithReturn);
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

// com.google.javascript.jscomp.CheckProvidesTest::testNoProvideInnerClass
  public void testNoProvideInnerClass() {
    testSame(
        "goog.provide('X');\n" +
        " function X(){};" +
        " X.Y = function(){};");
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

// com.google.javascript.jscomp.CheckRegExpTest::testRegExp
  public void testRegExp() {
    
    testReference("RegExp();", false);
    testReference("var x = RegExp();", false);
    testReference("new RegExp();", false);
    testReference("var x = new RegExp();", false);

    
    testReference("x instanceof RegExp;", false);

    
    testReference("RegExp.test();", true);
    testReference("var x = RegExp.test();", true);
    testReference("RegExp.exec();", true);
    testReference("RegExp.$1;", true);
    testReference("RegExp.foobar;", true);
    testReference("delete RegExp;", true);

    
    testReference("var x = RegExp;", true);
    testReference("f(RegExp);", true);
    testReference("new f(RegExp);", true);
    testReference("var x = RegExp; x.test()", true);

    
    testReference("var x;", false);

    
    testReference("function f() {var RegExp; RegExp.test();}", false);
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
    SourceFile input = SourceFile.fromCode("foo.js", js);
    Compiler compiler = new Compiler();
    CompilerOptions opts = new CompilerOptions();
    opts.checkRequires = CheckLevel.WARNING;
    opts.closurePass = true;

    Result result = compiler.compile(ImmutableList.<SourceFile>of(),
        ImmutableList.of(input), opts);
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

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testVarConstructorName
  public void testVarConstructorName() {
    String js = "var bar = Date;" +
        "new bar();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testVarConstructorFunction
  public void testVarConstructorFunction() {
    String js = "var bar = function() {};" +
        "new bar();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testAssignConstructorName
  public void testAssignConstructorName() {
    String js = "var foo = {};" +
        "foo.bar = Date;" +
        "new foo.bar();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testAssignConstructorFunction
  public void testAssignConstructorFunction() {
    String js = "var foo = {};" +
        "foo.bar = function() {};" +
        "new foo.bar();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckRequiresForConstructorsTest::testConstructorFunctionReference
  public void testConstructorFunctionReference() {
    String js = "function bar() {}" +
        "new bar();";
    testSame(js);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::test
  public void test(String js, String expected, DiagnosticType warning) {
    test(js, expected, null, warning);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::test
  public void test(String js, DiagnosticType warning) {
    test(js, js, null, warning);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testUselessCode
  public void testUselessCode() {
    test("function f(x) { if(x) return; }", ok);
    test("function f(x) { if(x); }", "function f(x) { if(x); }", e);

    test("if(x) x = y;", ok);
    test("if(x) x == bar();", "if(x) JSCOMPILER_PRESERVE(x == bar());", e);

    test("x = 3;", ok);
    test("x == 3;", "JSCOMPILER_PRESERVE(x == 3);", e);

    test("var x = 'test'", ok);
    test("var x = 'test'\n'str'",
         "var x = 'test'\nJSCOMPILER_PRESERVE('str')", e);

    test("", ok);
    test("foo();;;;bar();;;;", ok);

    test("var a, b; a = 5, b = 6", ok);
    test("var a, b; a = 5, b == 6",
         "var a, b; a = 5, JSCOMPILER_PRESERVE(b == 6)", e);
    test("var a, b; a = (5, 6)",
         "var a, b; a = (JSCOMPILER_PRESERVE(5), 6)", e);
    test("var a, b; a = (bar(), 6, 7)",
         "var a, b; a = (bar(), JSCOMPILER_PRESERVE(6), 7)", e);
    test("var a, b; a = (bar(), bar(), 7, 8)",
         "var a, b; a = (bar(), bar(), JSCOMPILER_PRESERVE(7), 8)", e);
    test("var a, b; a = (b = 7, 6)", ok);
    test("function x(){}\nfunction f(a, b){}\nf(1,(x(), 2));", ok);
    test("function x(){}\nfunction f(a, b){}\nf(1,(2, 3));",
         "function x(){}\nfunction f(a, b){}\n" +
         "f(1,(JSCOMPILER_PRESERVE(2), 3));", e);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testUselessCodeInFor
  public void testUselessCodeInFor() {
    test("for(var x = 0; x < 100; x++) { foo(x) }", ok);
    test("for(; true; ) { bar() }", ok);
    test("for(foo(); true; foo()) { bar() }", ok);
    test("for(void 0; true; foo()) { bar() }",
         "for(JSCOMPILER_PRESERVE(void 0); true; foo()) { bar() }", e);
    test("for(foo(); true; void 0) { bar() }",
         "for(foo(); true; JSCOMPILER_PRESERVE(void 0)) { bar() }", e);
    test("for(foo(); true; (1, bar())) { bar() }",
         "for(foo(); true; (JSCOMPILER_PRESERVE(1), bar())) { bar() }", e);

    test("for(foo in bar) { foo() }", ok);
    test("for (i = 0; el = el.previousSibling; i++) {}", ok);
    test("for (i = 0; el = el.previousSibling; i++);", ok);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testTypeAnnotations
  public void testTypeAnnotations() {
    test("x;", "JSCOMPILER_PRESERVE(x);", e);
    test("a.b.c.d;", "JSCOMPILER_PRESERVE(a.b.c.d);", e);
    test(" a.b.c.d;", ok);
    test("if (true) {  a.b.c.d; }", ok);

    test("function A() { this.foo; }",
         "function A() { JSCOMPILER_PRESERVE(this.foo); }", e);
    test("function A() {  this.foo; }", ok);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testJSDocComments
  public void testJSDocComments() {
    test("function A() {  this.foo; }", ok);
    test("function A() {  this.foo; }",
         "function A() { " +
         "  JSCOMPILER_PRESERVE(this.foo); }", e);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testIssue80
  public void testIssue80() {
    test("(0, eval)('alert');", ok);
    test("(0, foo)('alert');", "(JSCOMPILER_PRESERVE(0), foo)('alert');", e);
  }

// com.google.javascript.jscomp.CheckSideEffectsTest::testIsue504
  public void testIsue504() {
    test("void f();", "JSCOMPILER_PRESERVE(void f());", null, e,
        "Suspicious code. The result of the 'void' operator is not being used.");
  }

// com.google.javascript.jscomp.CheckSuspiciousCodeTest::test
  public void test(String js, DiagnosticType error) {
    test(js, js, null, error);
  }

// com.google.javascript.jscomp.CheckSuspiciousCodeTest::testSuspiciousSemi
  public void testSuspiciousSemi() {
    final DiagnosticType e = CheckSuspiciousCode.SUSPICIOUS_SEMICOLON;
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

// com.google.javascript.jscomp.CheckSuspiciousCodeTest::testComparison1
  public void testComparison1() {
    testReportNaN("x == NaN");
    testReportNaN("x != NaN");
    testReportNaN("x === NaN");
    testReportNaN("x !== NaN");
    testReportNaN("x < NaN");
    testReportNaN("x <= NaN");
    testReportNaN("x > NaN");
    testReportNaN("x >= NaN");
  }

// com.google.javascript.jscomp.CheckSuspiciousCodeTest::testComparison2
  public void testComparison2() {
    testReportNaN("NaN == x");
    testReportNaN("NaN != x");
    testReportNaN("NaN === x");
    testReportNaN("NaN !== x");
    testReportNaN("NaN < x");
    testReportNaN("NaN <= x");
    testReportNaN("NaN > x");
    testReportNaN("NaN >= x");
  }

// com.google.javascript.jscomp.CheckSuspiciousCodeTest::testComparison3
  public void testComparison3() {
    testReportNaN("x == 0/0");
    testReportNaN("x != 0/0");
    testReportNaN("x === 0/0");
    testReportNaN("x !== 0/0");
    testReportNaN("x < 0/0");
    testReportNaN("x <= 0/0");
    testReportNaN("x > 0/0");
    testReportNaN("x >= 0/0");
  }

// com.google.javascript.jscomp.CheckSuspiciousCodeTest::testComparison4
  public void testComparison4() {
    testReportNaN("0/0 == x");
    testReportNaN("0/0 != x");
    testReportNaN("0/0 === x");
    testReportNaN("0/0 !== x");
    testReportNaN("0/0 < x");
    testReportNaN("0/0 <= x");
    testReportNaN("0/0 > x");
    testReportNaN("0/0 >= x");
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

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testFalseCondition
  public void testFalseCondition() {
    assertUnreachable("if(false) { }");
    assertUnreachable("if(0) { }");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testUnreachableLoop
  public void testUnreachableLoop() {
    assertUnreachable("while(false) {}");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testInfiniteLoop
  public void testInfiniteLoop() {
    testSame("while (true) { foo(); break; }");

    
    assertUnreachable("while(true) {} foo()");
  }

// com.google.javascript.jscomp.CheckUnreachableCodeTest::testSuppression
  public void testSuppression() {
    assertUnreachable("if(false) { }");

    testSame(
        "\n" +
        "if(false) { }");

    testSame(
        "\n" +
        "function f() { if(false) { } }");

    testSame(
        "\n" +
        "function f() { if(false) { } }");

    assertUnreachable(
        "\n" +
        "function f() { if(false) { } }\n" +
        "function g() { if(false) { } }\n");

    testSame(
        "\n" +
        "function f() {\n" +
        "  function g() { if(false) { } }\n" +
        "  if(false) { } }\n");

    assertUnreachable(
        "function f() {\n" +
        "  \n" +
        "  function g() { if(false) { } }\n" +
        "  if(false) { } }\n");

    testSame(
        "function f() {\n" +
        "  \n" +
        "  function g() { if(false) { } }\n" +
        "}\n");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testRemoveAbstract
  public void testRemoveAbstract() {
    test("function Foo() {}; Foo.prototype.doSomething = goog.abstractMethod;",
        "function Foo() {};");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testRemoveMultiplySetAbstract
  public void testRemoveMultiplySetAbstract() {
    test("function Foo() {}; Foo.prototype.doSomething = " +
        "Foo.prototype.doSomethingElse = Foo.prototype.oneMore = " +
        "goog.abstractMethod;",
        "function Foo() {};");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testDoNotRemoveNormal
  public void testDoNotRemoveNormal() {
    testSame("function Foo() {}; Foo.prototype.doSomething = function() {};");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testDoNotRemoveOverride
  public void testDoNotRemoveOverride() {
    test("function Foo() {}; Foo.prototype.doSomething = goog.abstractMethod;" +
         "function Bar() {}; goog.inherits(Bar, Foo);" +
         "Bar.prototype.doSomething = function() {}",
         "function Foo() {}; function Bar() {}; goog.inherits(Bar, Foo);" +
         "Bar.prototype.doSomething = function() {}");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testDoNotRemoveNonQualifiedName
  public void testDoNotRemoveNonQualifiedName() {
    testSame("document.getElementById('x').y = goog.abstractMethod;");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testStopRemovalAtNonQualifiedName
  public void testStopRemovalAtNonQualifiedName() {
    test("function Foo() {}; function Bar() {};" +
         "Foo.prototype.x = document.getElementById('x').y = Bar.prototype.x" +
         " = goog.abstractMethod;",
         "function Foo() {}; function Bar() {};" +
         "Foo.prototype.x = document.getElementById('x').y = " +
         "goog.abstractMethod;");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testAssertionRemoval1
  public void testAssertionRemoval1() {
    test("var x = goog.asserts.assert(y(), 'message');", "var x = y();");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testAssertionRemoval2
  public void testAssertionRemoval2() {
    test("goog.asserts.assert(y(), 'message');", "");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testAssertionRemoval3
  public void testAssertionRemoval3() {
    test("goog.asserts.assert();", "");
  }

// com.google.javascript.jscomp.ClosureCodeRemovalTest::testAssertionRemoval4
  public void testAssertionRemoval4() {
    test("var x = goog.asserts.assert();", "var x = void 0;");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testVarAndOptionalParams
  public void testVarAndOptionalParams() {
    Node args = new Node(Token.PARAM_LIST,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"));
    Node optArgs = new Node(Token.PARAM_LIST,
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

    assertFalse(conv.isExported("$super", false));
    assertTrue(conv.isExported("$super", true));
    assertTrue(conv.isExported("$super"));
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

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection11
  public void testInheritanceDetection11() {
    assertNotClassDefining("A.mixin(B)");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection12
  public void testInheritanceDetection12() {
    assertNotClassDefining("goog.mixin(A.prototype, B)");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection13
  public void testInheritanceDetection13() {
    assertNotClassDefining("goog.mixin(A, B)");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetection14
  public void testInheritanceDetection14() {
    assertNotClassDefining("goog$mixin((function(){}).prototype)");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testInheritanceDetectionPostCollapseProperties
  public void testInheritanceDetectionPostCollapseProperties() {
    assertDefinesClasses("goog$inherits(A, B);", "A", "B");
    assertNotClassDefining("goog$inherits(A);");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testObjectLiteralCast
  public void testObjectLiteralCast() {
    assertNotObjectLiteralCast("goog.reflect.object();");
    assertNotObjectLiteralCast("goog.reflect.object(A);");
    assertNotObjectLiteralCast("goog.reflect.object(1, {});");
    assertObjectLiteralCast("goog.reflect.object(A, {});");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testFunctionBind
  public void testFunctionBind() {
    assertNotFunctionBind("goog.bind()");  
    assertFunctionBind("goog.bind(f)");
    assertFunctionBind("goog.bind(f, obj)");
    assertFunctionBind("goog.bind(f, obj, p1)");

    assertNotFunctionBind("goog$bind()");  
    assertFunctionBind("goog$bind(f)");
    assertFunctionBind("goog$bind(f, obj)");
    assertFunctionBind("goog$bind(f, obj, p1)");

    assertNotFunctionBind("goog.partial()");  
    assertFunctionBind("goog.partial(f)");
    assertFunctionBind("goog.partial(f, obj)");
    assertFunctionBind("goog.partial(f, obj, p1)");

    assertNotFunctionBind("goog$partial()");  
    assertFunctionBind("goog$partial(f)");
    assertFunctionBind("goog$partial(f, obj)");
    assertFunctionBind("goog$partial(f, obj, p1)");

    assertFunctionBind("(function(){}).bind()");
    assertFunctionBind("(function(){}).bind(obj)");
    assertFunctionBind("(function(){}).bind(obj, p1)");

    assertNotFunctionBind("Function.prototype.bind.call()");
    assertFunctionBind("Function.prototype.bind.call(obj)");
    assertFunctionBind("Function.prototype.bind.call(obj, p1)");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testRequire
  public void testRequire() {
    assertRequire("goog.require('foo')");
    assertNotRequire("goog.require(foo)");
    assertNotRequire("goog.require()");
    assertNotRequire("foo()");
  }

// com.google.javascript.jscomp.ClosureCodingConventionTest::testApplySubclassRelationship
  public void testApplySubclassRelationship() {
    JSTypeRegistry registry = new JSTypeRegistry(null);

    Node nodeA = new Node(Token.FUNCTION);
    FunctionType ctorA = registry.createConstructorType("A", nodeA,
        new Node(Token.PARAM_LIST), null, null);

    Node nodeB = new Node(Token.FUNCTION);
    FunctionType ctorB = registry.createConstructorType("B", nodeB,
        new Node(Token.PARAM_LIST), null, null);

    conv.applySubclassRelationship(ctorA, ctorB, SubclassType.INHERITS);

    assertTrue(ctorB.getPrototype().hasOwnProperty("constructor"));
    assertEquals(nodeB, ctorB.getPrototype().getPropertyNode("constructor"));

    assertTrue(ctorB.hasOwnProperty("superClass_"));
    assertEquals(nodeB, ctorB.getPropertyNode("superClass_"));
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreateNonConstKey
  public void testObjectCreateNonConstKey() {
    testSame("goog.object.create('a',1,2,3,foo,bar);");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreateOddParams
  public void testObjectCreateOddParams() {
    testSame("goog.object.create('a',1,2);");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreate1
  public void testObjectCreate1() {
    test("var a = goog.object.create()", "var a = {}");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreate2
  public void testObjectCreate2() {
    test("var a = goog$object$create('b',goog$object$create('c','d'))",
         "var a = {'b':{'c':'d'}};");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreate3
  public void testObjectCreate3() {
    test("var a = goog.object.create(1,2)", "var a = {1:2}");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreate4
  public void testObjectCreate4() {
    test("alert(goog.object.create(1,2).toString())",
         "alert({1:2}.toString())");
  }

// com.google.javascript.jscomp.ClosureOptimizePrimitivesTest::testObjectCreate5
  public void testObjectCreate5() {
    test("goog.object.create('a',2).toString()", "({'a':2}).toString()");
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDef1
  public void testGoogIsDef1() throws Exception {
    testClosureFunction("goog.isDef",
        createOptionalType(NUMBER_TYPE),
        NUMBER_TYPE,
        VOID_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDef2
  public void testGoogIsDef2() throws Exception {
    testClosureFunction("goog.isDef",
        createNullableType(NUMBER_TYPE),
        createNullableType(NUMBER_TYPE),
        NO_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDef3
  public void testGoogIsDef3() throws Exception {
    testClosureFunction("goog.isDef",
        ALL_TYPE,
        createUnionType(OBJECT_NUMBER_STRING_BOOLEAN,NULL_TYPE),
        VOID_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDef4
  public void testGoogIsDef4() throws Exception {
    testClosureFunction("goog.isDef",
        UNKNOWN_TYPE,
        UNKNOWN_TYPE,  
        UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsNull1
  public void testGoogIsNull1() throws Exception {
    testClosureFunction("goog.isNull",
        createOptionalType(NUMBER_TYPE),
        NO_TYPE,
        createOptionalType(NUMBER_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsNull2
  public void testGoogIsNull2() throws Exception {
    testClosureFunction("goog.isNull",
        createNullableType(NUMBER_TYPE),
        NULL_TYPE,
        NUMBER_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsNull3
  public void testGoogIsNull3() throws Exception {
    testClosureFunction("goog.isNull",
        ALL_TYPE,
        NULL_TYPE,
        createUnionType(OBJECT_NUMBER_STRING_BOOLEAN, VOID_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsNull4
  public void testGoogIsNull4() throws Exception {
    testClosureFunction("goog.isNull",
        UNKNOWN_TYPE,
        UNKNOWN_TYPE,
        UNKNOWN_TYPE); 
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull1
  public void testGoogIsDefAndNotNull1() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        createOptionalType(NUMBER_TYPE),
        NUMBER_TYPE,
        VOID_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull2
  public void testGoogIsDefAndNotNull2() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        createNullableType(NUMBER_TYPE),
        NUMBER_TYPE,
        NULL_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull3
  public void testGoogIsDefAndNotNull3() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        createOptionalType(createNullableType(NUMBER_TYPE)),
        NUMBER_TYPE,
        NULL_VOID);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull4
  public void testGoogIsDefAndNotNull4() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        ALL_TYPE,
        OBJECT_NUMBER_STRING_BOOLEAN,
        NULL_VOID);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsDefAndNotNull5
  public void testGoogIsDefAndNotNull5() throws Exception {
    testClosureFunction("goog.isDefAndNotNull",
        UNKNOWN_TYPE,
        UNKNOWN_TYPE,  
        UNKNOWN_TYPE);
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

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsBoolean3
  public void testGoogIsBoolean3() throws Exception {
    testClosureFunction("goog.isBoolean",
        ALL_TYPE,
        BOOLEAN_TYPE,
        ALL_TYPE); 
                   
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsBoolean4
  public void testGoogIsBoolean4() throws Exception {
    testClosureFunction("goog.isBoolean",
        UNKNOWN_TYPE,
        BOOLEAN_TYPE,
        CHECKED_UNKNOWN_TYPE);
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

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsFunction2
  public void testGoogIsFunction2() throws Exception {
    testClosureFunction("goog.isFunction",
        OBJECT_NUMBER_STRING_BOOLEAN,
        U2U_CONSTRUCTOR_TYPE,
        OBJECT_NUMBER_STRING_BOOLEAN);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsFunction3
  public void testGoogIsFunction3() throws Exception {
    testClosureFunction("goog.isFunction",
        createUnionType(U2U_CONSTRUCTOR_TYPE, NUMBER_STRING_BOOLEAN),
        U2U_CONSTRUCTOR_TYPE,
        NUMBER_STRING_BOOLEAN);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsFunctionOnNull
  public void testGoogIsFunctionOnNull() throws Exception {
    testClosureFunction("goog.isFunction",
        null,
        U2U_CONSTRUCTOR_TYPE,
        null);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsArray1
  public void testGoogIsArray1() throws Exception {
    testClosureFunction("goog.isArray",
        OBJECT_TYPE,
        ARRAY_TYPE,
        OBJECT_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsArray2
  public void testGoogIsArray2() throws Exception {
    testClosureFunction("goog.isArray",
        ALL_TYPE,
        ALL_TYPE, 
        ALL_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsArray3
  public void testGoogIsArray3() throws Exception {
    testClosureFunction("goog.isArray",
        UNKNOWN_TYPE,
        CHECKED_UNKNOWN_TYPE,
        CHECKED_UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsArray4
  public void testGoogIsArray4() throws Exception {
    testClosureFunction("goog.isArray",
        createUnionType(ARRAY_TYPE, NULL_TYPE),
        ARRAY_TYPE,
        NULL_TYPE);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsArrayOnNull
  public void testGoogIsArrayOnNull() throws Exception {
    testClosureFunction("goog.isArray",
        null,
        ARRAY_TYPE,
        null);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsObjectOnNull
  public void testGoogIsObjectOnNull() throws Exception {
    testClosureFunction("goog.isObject",
        null,
        OBJECT_TYPE,
        null);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsObject1
  public void testGoogIsObject1() throws Exception {
    testClosureFunction("goog.isObject",
        ALL_TYPE,
        NO_OBJECT_TYPE,
        createUnionType(NUMBER_STRING_BOOLEAN, NULL_TYPE, VOID_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsObject2
  public void testGoogIsObject2() throws Exception {
    testClosureFunction("goog.isObject",
          createUnionType(OBJECT_TYPE, NUMBER_STRING_BOOLEAN),
          OBJECT_TYPE,
          NUMBER_STRING_BOOLEAN);
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsObject3
  public void testGoogIsObject3() throws Exception {
    testClosureFunction("goog.isObject",
          createUnionType(
              OBJECT_TYPE, NUMBER_STRING_BOOLEAN, NULL_TYPE, VOID_TYPE),
          OBJECT_TYPE,
          createUnionType(NUMBER_STRING_BOOLEAN, NULL_TYPE, VOID_TYPE));
  }

// com.google.javascript.jscomp.ClosureReverseAbstractInterpreterTest::testGoogIsObject4
  public void testGoogIsObject4() throws Exception {
    testClosureFunction("goog.isObject",
        UNKNOWN_TYPE,
        NO_OBJECT_TYPE,  
        CHECKED_UNKNOWN_TYPE);
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testBasic1
  public void testBasic1() {
    test(
        "var x = goog.defineClass(null, {\n" +
        "  constructor: function(){}\n" +
        "});",

        "{var x = function() {};}");
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testBasic2
  public void testBasic2() {
    test(
        "var x = {};\n" +
        "x.y = goog.defineClass(null, {\n" +
        "  constructor: function(){}\n" +
        "});",

        "var x = {};" +
        "{x.y = function() {};}");
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testBasic3
  public void testBasic3() {
    test(
        "var x = goog.labs.classdef.defineClass(null, {\n" +
        "  constructor: function(){}\n" +
        "});",

        "{var x = function() {};}");
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testInnerClass1
  public void testInnerClass1() {
    test(
        "var x = goog.defineClass(some.Super, {\n" +
        "  constructor: function(){\n" +
        "    this.foo = 1;\n" +
        "  },\n" +
        "  statics: {\n" +
        "    inner: goog.defineClass(x,{\n" +
        "      constructor: function(){\n" +
        "        this.bar = 1;\n" +
        "      }\n" +
        "    })\n" +
        "  }\n" +
        "});",

        "{" +
        "var x=function(){this.foo=1};" +
        "goog.inherits(x,some.Super);" +
        "{" +
        "x.inner=function(){this.bar=1};" +
        "goog.inherits(x.inner,x);" +
        "}" +
        "}");
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testComplete1
  public void testComplete1() {
    test(
        "var x = goog.defineClass(some.Super, {\n" +
        "  constructor: function(){\n" +
        "    this.foo = 1;\n" +
        "  },\n" +
        "  statics: {\n" +
        "    prop1: 1,\n" +
        "    \n" +
        "    PROP2: 2\n" +
        "  },\n" +
        "  anotherProp: 1,\n" +
        "  aMethod: function() {}\n" +
        "});",

        "{" +
        "var x=function(){this.foo=1};" +
        "goog.inherits(x,some.Super);" +
        "x.prop1=1;" +
        "x.PROP2=2;" +
        "x.prototype.anotherProp=1;" +
        "x.prototype.aMethod=function(){};" +
        "}");
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testComplete2
  public void testComplete2() {
    test(
        "x.y = goog.defineClass(some.Super, {\n" +
        "  constructor: function(){\n" +
        "    this.foo = 1;\n" +
        "  },\n" +
        "  statics: {\n" +
        "    prop1: 1,\n" +
        "    \n" +
        "    PROP2: 2\n" +
        "  },\n" +
        "  anotherProp: 1,\n" +
        "  aMethod: function() {}\n" +
        "});",

        "{\n" +
        "\n" +
        "x.y=function(){this.foo=1};\n" +
        "goog.inherits(x.y,some.Super);" +
        "x.y.prop1=1;\n" +
        "\n" +
        "x.y.PROP2=2;\n" +
        "x.y.prototype.anotherProp=1;" +
        "x.y.prototype.aMethod=function(){};" +
        "}");
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testClassWithStaticInitFn
  public void testClassWithStaticInitFn() {
    test(
        "x.y = goog.defineClass(some.Super, {\n" +
        "  constructor: function(){\n" +
        "    this.foo = 1;\n" +
        "  },\n" +
        "  statics: function(cls) {\n" +
        "    cls.prop1 = 1;\n" +
        "    \n" +
        "    cls.PROP2 = 2;\n" +
        "  },\n" +
        "  anotherProp: 1,\n" +
        "  aMethod: function() {}\n" +
        "});",

        "{\n" +
        "\n" +
        "x.y=function(){this.foo=1};\n" +
        "goog.inherits(x.y,some.Super);" +
        "x.y.prototype.anotherProp=1;" +
        "x.y.prototype.aMethod=function(){};" +
        "(function(cls) {" +
        "  cls.prop1=1;\n" +
        "  \n" +
        "  cls.PROP2=2;" +
        "})(x.y);\n" +
        "}");
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testInvalid1
  public void testInvalid1() {
    testSame(
        "var x = goog.defineClass();",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass('foo');",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(foo());",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass({'foo':1});",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass({1:1});",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);

    this.enableEcmaScript5(true);

    testSame(
        "var x = goog.defineClass({get foo() {return 1}});",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass({set foo(a) {}});",
        GOOG_CLASS_SUPER_CLASS_NOT_VALID, true);
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testInvalid2
  public void testInvalid2() {
    testSame(
        "var x = goog.defineClass(null);",
        GOOG_CLASS_DESCRIPTOR_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, null);",
        GOOG_CLASS_DESCRIPTOR_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, foo());",
        GOOG_CLASS_DESCRIPTOR_NOT_VALID, true);
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testInvalid3
  public void testInvalid3() {
    testSame(
        "var x = goog.defineClass(null, {});",
        GOOG_CLASS_CONSTRUCTOR_MISING, true);
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testInvalid4
  public void testInvalid4() {
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}," +
        "  statics: null" +
        "});",
        GOOG_CLASS_STATICS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}," +
        "  statics: foo" +
        "});",
        GOOG_CLASS_STATICS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}," +
        "  statics: {'foo': 1}" +
        "});",
        GOOG_CLASS_STATICS_NOT_VALID, true);
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}," +
        "  statics: {1: 1}" +
        "});",
        GOOG_CLASS_STATICS_NOT_VALID, true);  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testInvalid5
  public void testInvalid5() {
    testSame(
        "var x = goog.defineClass(null, {" +
        "  constructor: function(){}" +
        "}, null);",
        GOOG_CLASS_UNEXPECTED_PARAMS, true);
  }

// com.google.javascript.jscomp.ClosureRewriteClassTest::testInvalid6
  public void testInvalid6() {
    testSame(
        "goog.defineClass();",
        GOOG_CLASS_TARGET_INVALID, true);

    testSame(
        "var x = goog.defineClass() || null;",
        GOOG_CLASS_TARGET_INVALID, true);

    testSame(
        "({foo: goog.defineClass()});",
        GOOG_CLASS_TARGET_INVALID, true);
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testSimple
  public void testSimple() {
    inFunction("var x; var y; x=1; x; y=1; y; return y",
               "var x;        x=1; x; x=1; x; return x");

    inFunction("var x,y; x=1; x; y=1; y",
               "var x  ; x=1; x; x=1; x");

    inFunction("var x,y; x=1; y=2; y; x");

    inFunction("y=0; var x, y; y; x=0; x",
               "y=0; var y   ; y; y=0;y");

    inFunction("var x,y; x=1; y=x; y",
               "var x  ; x=1; x=x; x");

    inFunction("var x,y; x=1; y=x+1; y",
               "var x  ; x=1; x=x+1; x");

    inFunction("x=1; x; y=2; y; var x; var y",
               "x=1; x; x=2; x; var x");

    inFunction("var x=1; var y=x+1; return y",
               "var x=1;     x=x+1; return x");

    inFunction("var x=1; var y=0; x+=1; y");

    inFunction("var x=1; x+=1; var y=0; y",
               "var x=1; x+=1;     x=0; x");

    inFunction("var x=1; foo(bar(x+=1)); var y=0; y",
               "var x=1; foo(bar(x+=1));     x=0; x");

    inFunction("var y, x=1; f(x+=1, y)");

    inFunction("var x; var y; y += 1, y, x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testMergeThreeVarNames
  public void testMergeThreeVarNames() {
    inFunction("var x,y,z; x=1; x; y=1; y; z=1; z",
               "var x    ; x=1; x; x=1; x; x=1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDifferentBlock
  public void testDifferentBlock() {
    inFunction("if(1) { var x = 0; x } else { var y = 0; y }",
               "if(1) { var x = 0; x } else {     x = 0; x }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLoops
  public void testLoops() {
    inFunction("var x; while(1) { x; x = 1; var y = 1; y }");
    inFunction("var y = 1; y; while(1) { var x = 1; x }",
               "var y = 1; y; while(1) {     y = 1; y }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testEscaped
  public void testEscaped() {
    inFunction("var x = 1; x; function f() { x };  var y = 0; y; f()");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testFor
  public void testFor() {
    inFunction("var x = 1; x; for (;;) var y; y = 1; y",
               "var x = 1; x; for (;;)      ; x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testForIn
  public void testForIn() {
    
    inFunction("var x = 1, k; x;      ; for (var y in k) { y }",
               "var x = 1, k; x;      ; for (var y in k) { y }");

    inFunction("var x = 1, k; x; y = 1; for (var y in k) { y }",
               "var x = 1, k; x; x = 1; for (    x in k) { x }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLoopInductionVar
  public void testLoopInductionVar() {
    inFunction(
        "for(var x = 0; x < 10; x++){}" +
        "for(var y = 0; y < 10; y++){}" +
        "for(var z = 0; z < 10; z++){}",

        "for(var x = 0; x < 10; x++){}" +
        "for(x = 0; x < 10; x++){}" +
        "for(x = 0; x < 10; x++){}");

    inFunction(
        "for(var x = 0; x < 10; x++){z}" +
        "for(var y = 0, z = 0; y < 10; y++){z}",

        "for(var x = 0; x < 10; x++){z}" +
        "for(var x = 0, z = 0; x < 10; x++){z}");

    inFunction("var x = 1; x; for (var y; y=1; ) {y}",
               "var x = 1; x; for (     ; x=1; ) {x}");

    inFunction("var x = 1; x; y = 1; while(y) var y; y",
               "var x = 1; x; x = 1; while(x); x");

    inFunction("var x = 1; x; f:var y; y=1",
               "var x = 1; x; x=1");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testSwitchCase
  public void testSwitchCase() {
    inFunction("var x = 1; switch(x) { case 1: var y; case 2: } y = 1; y",
               "var x = 1; switch(x) { case 1:        case 2: } x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDuplicatedVar
  public void testDuplicatedVar() {
    
    inFunction("z = 1; var x = 0; x; z; var y = 2, z = 1; y; z;",
               "z = 1; var x = 0; x; z; var x = 2, z = 1; x; z;");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testTryCatch
  public void testTryCatch() {
    inFunction("try {} catch (e) { } var x = 4; x;",
               "try {} catch (e) { } var x = 4; x;");
    inFunction("var x = 4; x; try {} catch (e) { }",
               "var x = 4; x; try {} catch (e) { }");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDeadAssignment
  public void testDeadAssignment() {
    inFunction("var x = 6; var y; y = 4 ; x");
    inFunction("var y = 3; var y; y += 4; x");
    inFunction("var y = 3; var y; y ++  ; x");
    inFunction("y = 3; var x; var y = 1 ; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter
  public void testParameter() {
    test("function FUNC(param) {var x = 0; x}",
         "function FUNC(param) {param = 0; param}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter2
  public void testParameter2() {
    
    test("function FUNC(x,y) {x = 0; x; y = 0; y}");
    test("function FUNC(x,y,z) {x = 0; x; y = 0; z = 0; z}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter3
  public void testParameter3() {
    
    test("function FUNC(x) {var y; y = 0; x; y}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter4
  public void testParameter4() {
    
    
    test("function FUNC(x, y) {var a,b; y; a=0; a; x; b=0; b}",
         "function FUNC(x, y) {var a; y; a=0; a; x; a=0; a}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testParameter4b
  public void testParameter4b() {
    
    test("function FUNC(x, y, z) {var a,b; y; a=0; a; x; b=0; b}",
         "function FUNC(x, y, z) {         y; y=0; y; x; x=0; x}");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLiveRangeChangeWithinCfgNode
  public void testLiveRangeChangeWithinCfgNode() {
    inFunction("var x, y; x = 1, y = 2, y, x");
    inFunction("var x, y; x = 1,x; y");

    
    inFunction("var x; var y; y = 1, y, x = 1; x");
    inFunction("var x; var y; y = 1; y, x = 1; x", "var x; x = 1; x, x = 1; x");
    inFunction("var x, y; y = 1, x = 1, x, y += 1, y");
    inFunction("var x, y; y = 1, x = 1, x, y ++, y");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testLiveRangeChangeWithinCfgNode2
  public void testLiveRangeChangeWithinCfgNode2() {
    inFunction("var x; var y; var a; var b;" +
               "y = 1, a = 1, y, a, x = 1, b = 1; x; b");
    inFunction("var x; var y; var a; var b;" +
               "y = 1, a = 1, y, a, x = 1; x; b = 1; b",
               "var x; var y; var a;       " +
               "y = 1, a = 1, y, a, x = 1; x; x = 1; x");
    inFunction("var x; var y; var a; var b;" +
               "y = 1, a = 1, y, x = 1; a; x; b = 1; b",
               "var x; var y; var a;       " +
               "y = 1, a = 1, y, x = 1; a; x; x = 1; x");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testFunctionNameReuse
  public void testFunctionNameReuse() {

  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testBug1401831
  public void testBug1401831() {
    
    
    String src = "function f(opt_a2) {" +
        "  var buffer;" +
        "  if (opt_a2) {" +
        "    for(var i = 0; i < arguments.length; i++) {" +
        "      buffer += arguments[i];" +
        "    }" +
        "  }" +
        "  return buffer;" +
        "}";
    test(src, src);
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testDeterministic
  public void testDeterministic() {
    
    
    
    
    
    
    
    
    
    
    inFunction("var a,b,c,d,e;" +
               "  a=1; b=1; a; b;" +
               "  b=1; c=1; b; c;" +
               "  c=1; d=1; c; d;" +
               "  d=1; e=1; d; e;" +
               "  e=1; a=1; e; a;",

               "var a,b,    e;" +
               "  a=1; b=1; a; b;" +
               "  b=1; a=1; b; a;" +
               "  a=1; b=1; a; b;" +
               "  b=1; e=1; b; e;" +
               "  e=1; a=1; e; a;");

    
    
    
    
    
    inFunction("var d,a,b,c,e;" +
               "  a=1; b=1; a; b;" +
               "  b=1; c=1; b; c;" +
               "  c=1; d=1; c; d;" +
               "  d=1; e=1; d; e;" +
               "  e=1; a=1; e; a;",

               "var d,  b,c  ;" +
               "  d=1; b=1; d; b;" +
               "  b=1; c=1; b; c;" +
               "  c=1; d=1; c; d;" +
               "  d=1; b=1; d; b;" +
               "  b=1; d=1; b; d;");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testVarLiveRangeCross
  public void testVarLiveRangeCross() {
    inFunction("var a={}; var b=a.S(); b",
               "var a={};     a=a.S(); a");
    inFunction("var a={}; var b=a.S(), c=b.SS(); b; c",
               "var a={}; var b=a.S(), a=b.SS(); b; a");
    inFunction("var a={}; var b=a.S(), c=a.SS(), d=a.SSS(); b; c; d",
               "var a={}; var b=a.S(), c=a.SS(), a=a.SSS(); b; c; a");
    inFunction("var a={}; var b=a.S(), c=a.SS(), d=a.SSS(); b; c; d",
               "var a={}; var b=a.S(), c=a.SS(), a=a.SSS(); b; c; a");
    inFunction("var a={}; d=1; d; var b=a.S(), c=a.SS(), d=a.SSS(); b; c; d");
  }

// com.google.javascript.jscomp.CoalesceVariableNamesTest::testBug1445366
  public void testBug1445366() {
    
    inFunction(
        " var iframe = getFrame();" +
        " try {" +
        "   var win = iframe.contentWindow;" +
        " } catch (e) {" +
        " } finally {" +
        "   if (win)" +
        "     this.setupWinUtil_();" +
        "   else" +
        "     this.load();" +
        " }");

    
    inFunction(
        " var iframe = getFrame();" +
        " var win = iframe.contentWindow;" +
        " if (win)" +
        "   this.setupWinUtil_();" +
        " else" +
        "   this.load();",

        " var iframe = getFrame();" +
        " iframe = iframe.contentWindow;" +
        " if (iframe)" +
        "   this.setupWinUtil_();" +
        " else" +
        "   this.load();");
  }
