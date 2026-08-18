// buggy code
    void maybeDeclareQualifiedName(NodeTraversal t, JSDocInfo info,
        Node n, Node parent, Node rhsValue) {
      Node ownerNode = n.getFirstChild();
      String ownerName = ownerNode.getQualifiedName();
      String qName = n.getQualifiedName();
      String propName = n.getLastChild().getString();
      Preconditions.checkArgument(qName != null && ownerName != null);

      // Precedence of type information on GETPROPs:
      // 1) @type annnotation / @enum annotation
      // 2) ASSIGN to FUNCTION literal
      // 3) @param/@return annotation (with no function literal)
      // 4) ASSIGN to something marked @const
      // 5) ASSIGN to anything else
      //
      // 1, 3, and 4 are declarations, 5 is inferred, and 2 is a declaration iff
      // the function has jsdoc or has not been declared before.
      //
      // FUNCTION literals are special because TypedScopeCreator is very smart
      // about getting as much type information as possible for them.

      // Determining type for #1 + #2 + #3 + #4
      JSType valueType = getDeclaredType(t.getSourceName(), info, n, rhsValue);
      if (valueType == null && rhsValue != null) {
        // Determining type for #5
        valueType = rhsValue.getJSType();
      }
      // Function prototypes are special.
      // It's a common JS idiom to do:
      // F.prototype = { ... };
      // So if F does not have an explicitly declared super type,
      // allow F.prototype to be redefined arbitrarily.
      if ("prototype".equals(propName)) {
        Var qVar = scope.getVar(qName);
        if (qVar != null) {
          // If the programmer has declared that F inherits from Super,
          // and they assign F.prototype to an object literal,
          // then they are responsible for making sure that the object literal's
          // implicit prototype is set up appropriately. We just obey
          // the @extends tag.
          if (!qVar.isTypeInferred()) {
            // If the programmer has declared that F inherits from Super,
            // and they assign F.prototype to some arbitrary expression,
            // there's not much we can do. We just ignore the expression,
            // and hope they've annotated their code in a way to tell us
            // what props are going to be on that prototype.
            return;
          }
          if (qVar.getScope() == scope) {
            scope.undeclare(qVar);
          }
        }
      }

      if (valueType == null) {
        if (parent.getType() == Token.EXPR_RESULT) {
          stubDeclarations.add(new StubDeclaration(
              n,
              t.getInput() != null && t.getInput().isExtern(),
              ownerName));
        }

        return;
      }

      boolean inferred = true;
      if (info != null) {
        // Determining declaration for #1 + #3 + #4
        inferred = !(info.hasType()
            || info.hasEnumParameterType()
            || (info.isConstant() && valueType != null
                && !valueType.isUnknownType())
            || FunctionTypeBuilder.isFunctionTypeDeclaration(info));
      }

      if (inferred) {
        // Determining declaration for #2
        inferred = !(rhsValue != null &&
            rhsValue.getType() == Token.FUNCTION &&
            (info != null || !scope.isDeclared(qName, false)));
      }

      if (!inferred) {
        ObjectType ownerType = getObjectSlot(ownerName);
        if (ownerType != null) {
          // Only declare this as an official property if it has not been
          // declared yet.
          boolean isExtern = t.getInput() != null && t.getInput().isExtern();
          if ((!ownerType.hasOwnProperty(propName) ||
               ownerType.isPropertyTypeInferred(propName)) &&
              ((isExtern && !ownerType.isNativeObjectType()) ||
               !ownerType.isInstanceType())) {
            // If the property is undeclared or inferred, declare it now.
            ownerType.defineDeclaredProperty(propName, valueType, n);
          }
        }

        // If the property is already declared, the error will be
        // caught when we try to declare it in the current scope.
        defineSlot(n, parent, valueType, inferred);
      } else if (rhsValue != null &&
          rhsValue.getType() == Token.TRUE) {
        // We declare these for delegate proxy method properties.
        FunctionType ownerType =
            JSType.toMaybeFunctionType(getObjectSlot(ownerName));
        if (ownerType != null) {
          JSType ownerTypeOfThis = ownerType.getTypeOfThis();
          String delegateName = codingConvention.getDelegateSuperclassName();
          JSType delegateType = delegateName == null ?
              null : typeRegistry.getType(delegateName);
          if (delegateType != null &&
              ownerTypeOfThis.isSubtype(delegateType)) {
            defineSlot(n, parent, getNativeType(BOOLEAN_TYPE), true);
          }
        }
      }
    }

  public void setPrototypeBasedOn(ObjectType baseType) {
    // This is a bit weird. We need to successfully handle these
    // two cases:
    // Foo.prototype = new Bar();
    // and
    // Foo.prototype = {baz: 3};
    // In the first case, we do not want new properties to get
    // added to Bar. In the second case, we do want new properties
    // to get added to the type of the anonymous object.
    //
    // We handle this by breaking it into two cases:
    //
    // In the first case, we create a new PrototypeObjectType and set
    // its implicit prototype to the type being assigned. This ensures
    // that Bar will not get any properties of Foo.prototype, but properties
    // later assigned to Bar will get inherited properly.
    //
    // In the second case, we just use the anonymous object as the prototype.
    if (baseType.hasReferenceName() ||
        baseType.isUnknownType() ||
        isNativeObjectType() ||
        baseType.isFunctionPrototypeType() ||
        !(baseType instanceof PrototypeObjectType)) {

      baseType = new PrototypeObjectType(
          registry, this.getReferenceName() + ".prototype", baseType);
    }
    setPrototype((PrototypeObjectType) baseType);
  }

  public boolean setPrototype(PrototypeObjectType prototype) {
    if (prototype == null) {
      return false;
    }
    // getInstanceType fails if the function is not a constructor
    if (isConstructor() && prototype == getInstanceType()) {
      return false;
    }

    boolean replacedPrototype = prototype != null;

    this.prototype = prototype;
    this.prototypeSlot = new SimpleSlot("prototype", prototype, true);
    this.prototype.setOwnerFunction(this);

      // Disassociating the old prototype makes this easier to debug--
      // we don't have to worry about two prototypes running around.

    if (isConstructor() || isInterface()) {
      FunctionType superClass = getSuperClassConstructor();
      if (superClass != null) {
        superClass.addSubType(this);
      }

      if (isInterface()) {
        for (ObjectType interfaceType : getExtendedInterfaces()) {
          if (interfaceType.getConstructor() != null) {
            interfaceType.getConstructor().addSubType(this);
          }
        }
      }
    }

    if (replacedPrototype) {
      clearCachedValues();
    }

    return true;
  }

// relevant test
// com.google.javascript.jscomp.VarCheckTest::testStarStrictModuleDependencyCheck
  public void testStarStrictModuleDependencyCheck() {
    strictModuleDepErrorLevel = CheckLevel.WARNING;
    testSame(createModuleStar("function a() {}", "function b() { a(); c(); }",
        "function c() { a(); }"),
        VarCheck.STRICT_MODULE_DEP_ERROR);
  }

// com.google.javascript.jscomp.VarCheckTest::testForwardVarReferenceInLocalScope1
  public void testForwardVarReferenceInLocalScope1() {
    testDependentModules("var x = 10; function a() {y++;}",
                         "var y = 11; a();", null);
  }

// com.google.javascript.jscomp.VarCheckTest::testForwardVarReferenceInLocalScope2
  public void testForwardVarReferenceInLocalScope2() {
    
    
    testDependentModules("var x = 10; function a() {y++;} a();",
                         "var y = 11;", null);
  }

// com.google.javascript.jscomp.VarCheckTest::testSimple
  public void testSimple() {
    checkSynthesizedExtern("x", "var x;");
    checkSynthesizedExtern("var x", "");
  }

// com.google.javascript.jscomp.VarCheckTest::testSimpleSanityCheck
  public void testSimpleSanityCheck() {
    sanityCheck = true;
    try {
      checkSynthesizedExtern("x", "");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().indexOf("Unexpected variable x") != -1);
    }
  }

// com.google.javascript.jscomp.VarCheckTest::testParameter
  public void testParameter() {
    checkSynthesizedExtern("function f(x){}", "");
  }

// com.google.javascript.jscomp.VarCheckTest::testLocalVar
  public void testLocalVar() {
    checkSynthesizedExtern("function f(){x}", "var x");
  }

// com.google.javascript.jscomp.VarCheckTest::testTwoLocalVars
  public void testTwoLocalVars() {
    checkSynthesizedExtern("function f(){x}function g() {x}", "var x");
  }

// com.google.javascript.jscomp.VarCheckTest::testInnerFunctionLocalVar
  public void testInnerFunctionLocalVar() {
    checkSynthesizedExtern("function f(){function g() {x}}", "var x");
  }

// com.google.javascript.jscomp.VarCheckTest::testNoCreateVarsForLabels
  public void testNoCreateVarsForLabels() {
    checkSynthesizedExtern("x:var y", "");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns1
  public void testVariableInNormalCodeUsedInExterns1() {
    checkSynthesizedExtern(
        "x.foo;", "var x;", "var x; x.foo;");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns2
  public void testVariableInNormalCodeUsedInExterns2() {
    checkSynthesizedExtern(
        "x;", "var x;", "var x; x;");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns3
  public void testVariableInNormalCodeUsedInExterns3() {
    checkSynthesizedExtern(
        "x.foo;", "function x() {}", "var x; x.foo; ");
  }

// com.google.javascript.jscomp.VarCheckTest::testVariableInNormalCodeUsedInExterns4
  public void testVariableInNormalCodeUsedInExterns4() {
    checkSynthesizedExtern(
        "x;", "function x() {}", "var x; x; ");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectCode
  public void testCorrectCode() {
    assertNoWarning("function foo(d) { (function() { d.foo(); }); d.bar(); } ");
    assertNoWarning("function foo() { bar(); } function bar() { foo(); } ");
    assertNoWarning("function f(d) { d = 3; }");
    assertNoWarning(VARIABLE_RUN);
    assertNoWarning("function f() { " + VARIABLE_RUN + "}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectShadowing
  public void testCorrectShadowing() {
    assertNoWarning(VARIABLE_RUN + "function f() { " + VARIABLE_RUN + "}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectRedeclare
  public void testCorrectRedeclare() {
    assertNoWarning(
        "function f() { if (1) { var a = 2; } else { var a = 3; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectRecursion
  public void testCorrectRecursion() {
    assertNoWarning("function f() { var x = function() { x(); }; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectCatch
  public void testCorrectCatch() {
    assertNoWarning("function f() { try { var x = 2; } catch (x) {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testRedeclare
  public void testRedeclare() {
    
    assertRedeclare("function f() { var a = 2; var a = 3; }");
    assertRedeclare("function f(a) { var a = 2; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testEarlyReference
  public void testEarlyReference() {
    assertUndeclared("function f() { a = 2; var a = 3; }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testCorrectEarlyReference
  public void testCorrectEarlyReference() {
    assertNoWarning("var goog = goog || {}");
    assertNoWarning("function f() { a = 2; } var a = 2;");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testUnreferencedBleedingFunction
  public void testUnreferencedBleedingFunction() {
    assertNoWarning("var x = function y() {}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testReferencedBleedingFunction
  public void testReferencedBleedingFunction() {
    assertNoWarning("var x = function y() { return y(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testDoubleDeclaration
  public void testDoubleDeclaration() {
    assertRedeclare("function x(y) { if (true) { var y; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testDoubleDeclaration2
  public void testDoubleDeclaration2() {
    assertRedeclare("function x() { var y; if (true) { var y; } }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testHoistedFunction1
  public void testHoistedFunction1() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("f(); function f() {}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testHoistedFunction2
  public void testHoistedFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction
  public void testNonHoistedFunction() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("if (true) { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction2
  public void testNonHoistedFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("if (false) { function f() {} f(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction3
  public void testNonHoistedFunction3() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() {} f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction4
  public void testNonHoistedFunction4() {
    enableAmbiguousFunctionCheck = true;
    assertAmbiguous("if (false) { function f() {} }  f();");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction5
  public void testNonHoistedFunction5() {
    enableAmbiguousFunctionCheck = true;
    assertAmbiguous("function g() { if (false) { function f() {} }  f(); }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction6
  public void testNonHoistedFunction6() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("if (false) { f(); function f() {} }");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedFunction7
  public void testNonHoistedFunction7() {
    enableAmbiguousFunctionCheck = true;
    assertUndeclared("function g() { if (false) { f(); function f() {} }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction1
  public void testNonHoistedRecursiveFunction1() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("if (false) { function f() { f(); }}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction2
  public void testNonHoistedRecursiveFunction2() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() { f(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNonHoistedRecursiveFunction3
  public void testNonHoistedRecursiveFunction3() {
    enableAmbiguousFunctionCheck = true;
    assertNoWarning("function g() { if (false) { function f() { f(); g(); }}}");
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNoWarnInExterns1
  public void testNoWarnInExterns1() {
    
    String externs =
       "var google;" +
       " var google";
    String code = "";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.VariableReferenceCheckTest::testNoWarnInExterns2
  public void testNoWarnInExterns2() {
    
    String externs =
       "window;" +
       "var window;";
    String code = "";
    test(externs, code, code, null, null);
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoWarnShadowGlobal
  public void testNoWarnShadowGlobal() {
    
    
    assertNoError("", "var x; function foo() { var x } ");
    assertNoError("var x", "function foo() { var x } ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testWarnShadowLocal1
  public void testWarnShadowLocal1() {
    assertError("", "function a(){ var x; function b() { var x = 1; } }");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testWarnShadowLocal2
  public void testWarnShadowLocal2() {
    assertError("",
                "function a(){" +
                "   var x;" +
                "  function b() {" +
                "    var x = 1;" +
                "  }" +
                "}");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testUseShadowGlobals1
  public void testUseShadowGlobals1() {
    assertNoError("", " var x; function foo() { x = 1 } ");
    assertNoError("", "function a() { var x; function b() { x = 1; } }");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotation
  public void testNoShadowAnnotation() {
    assertError("",
                " var x; function a() { var x } ");

    assertError("",
                " var x; function a() {function b(){var x}} ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotationInExterns1
  public void testNoShadowAnnotationInExterns1() {
    assertError(" var x",
                "function a() { var x } ");
  }

// com.google.javascript.jscomp.VariableShadowDeclarationCheckTest::testNoShadowAnnotationInExterns2
  public void testNoShadowAnnotationInExterns2() {
    assertError(" var x",
                "function a() {function b(){var x}} ");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testCapturedVariables
  public void testCapturedVariables() {
    String source =
        "global:var global;\n" +
        "function Outer() {\n" +
        "  captured:var captured;\n" +
        "  notcaptured:var notCaptured;\n" +
        "  function Inner() {\n" +
        "    alert(captured);" +
        "   }\n" +
        "}\n";

    analyze(source);

    assertIsCapturedLocal("captured");
    assertIsUncapturedLocal("notcaptured");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testGlobals
  public void testGlobals() {
    String source =
      "global:var global;";

    analyze(source);

    assertIsGlobal("global");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testParameters
  public void testParameters() {
    String source =
      "function A(a,b,c) {\n" +
      "}\n";

    analyze(source);

    assertIsParameter("a");
    assertIsParameter("b");
    assertIsParameter("c");
  }

// com.google.javascript.jscomp.VariableVisibilityAnalysisTest::testFunctions
  public void testFunctions() {
    String source =
        "function global() {\n" +
        "  function inner() {\n" +
        "  }\n" +
        "  function innerCaptured() {\n" +
        "    (function(){innerCaptured()})()\n" +
        "  }\n" +
        "}\n";

    analyze(source);

    assertFunctionHasVisibility("global",
        VariableVisibility.GLOBAL);

    assertFunctionHasVisibility("inner",
        VariableVisibility.LOCAL);

    assertFunctionHasVisibility("innerCaptured",
        VariableVisibility.CAPTURED_LOCAL);
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testArray
  public void testArray() throws Exception {
    testConversion("[,]");
    testConversion("[]");
    testConversion("[function (x) {}]");
    testConversion("[[], [a, [], [[[]], 1], f([a])], 1];");
    testConversion("x = [1, 2, 3]");
    testConversion("var x = [1, 2, 3]");
    testConversion("[, 1, Object(), , , 2]");
    testConversion("[{x: 'abc', y: 1}]");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testArray1
  public void testArray1() throws Exception {
    testConversion("[,]");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testAssignOperators
  public void testAssignOperators() throws Exception {
    testConversion("x += 1, x -= 1, x *= 1, x /= 1, x %= 1");
    testConversion("x |= 1, x ^= x, x &= 0");
    testConversion("x <<= 1, x >>= 1, x >>>= 1");
    testConversion("y = x += 1");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testCalls
  public void testCalls() throws Exception {
    testConversion("f()");
    testConversion("f(1)");
    testConversion("f('a')");
    testConversion("f(true)");
    testConversion("f(null)");
    testConversion("f(undefined)");

    testConversion("f(a + b)");
    testConversion("f(g(h(a)) * h(g(u(z('a')))))");

    testConversion("x = f()");
    testConversion("x = f(1)");
    testConversion("x = f(a + b)");
    testConversion("x = f(g(h(a)) * h(g(u(z('a')))))");

    testConversion("String('a')");
    testConversion("Number(1)");
    testConversion("Boolean(0)");
    testConversion("Object()");
    testConversion("Array('a', 1, false, null, Object(), String('a'))");

    testConversion("(function() {})()");
    testConversion("(function(x) {})(x)");
    testConversion("(function(x) {var y = x << 1; return y})(x)");
    testConversion("(function(x) {y = x << 1; return y})(x)");
    testConversion("var x = (function(x) {y = x << 1; return y})(x)");
    testConversion("var x = (function(x) {return x << 1})(x)");

    testConversion("eval()");
    testConversion("eval('x')");
    testConversion("x = eval('x')");
    testConversion("var x = eval('x')");
    testConversion("eval(Template('foo${bar}baz')); var Template;");

    testConversion("a.x()");
    testConversion("a[x]()");
    testConversion("z = a.x()");
    testConversion("var z = a.x()");
    testConversion("z = a[x]()");
    testConversion("z = a['x']()");
    testConversion("var z = a[x]()");
    testConversion("var z = a['x']()");
    testConversion("a.x(y)");
    testConversion("a[x](y)");
    testConversion("a['x'](y)");
    testConversion("a[x](y, z, 'a', null, true, f(y))");
    testConversion("a['x'](y, z, 'a', null, true, f(y))");
    testConversion("a[b[c[d]]()].x");

    testConversion("(f())()");
    testConversion("(f(x))(y)");
    testConversion("(f = getFn())()");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testConditionals
  public void testConditionals() throws Exception {
    testConversion("x ? y : z");
    testConversion("result = x ? y : z");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDecIncOperators
  public void testDecIncOperators() throws Exception {
    testConversion("x--");
    testConversion("--x");
    testConversion("x++");
    testConversion("++x");
    testConversion("var y=x++, z=++x; var s=y--, r=++y;");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDelete
  public void testDelete() throws Exception {
    testConversion("delete a");
    testConversion("delete a.x");
    testConversion("delete a[0]");
    testConversion("delete a.x[0]");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDirectives
  public void testDirectives() throws Exception {
    testConversion("'use strict'");
    testConversion("function foo() {'use strict'}");
    testConversion("'use strict'; function foo() {'use strict'}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testDoWhile
  public void testDoWhile() throws Exception {
  
     testConversion("do {} while (true)");
     testConversion("do {;} while (true)");
     testConversion("do {} while (f(x, y))");
     testConversion("do {} while (f(f(f(x, y))))");
     testConversion("do {} while ((f(f(f(x, y))))())");
     testConversion("do {2 + 3; q = 2 + 3; var v = y * z;"
         + "g = function(a) {true; var b = a + 1; return a * a}} while (--x)");
   }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testFor
  public void testFor() throws Exception {
     testConversion("for (;true;) {;}");
     testJsonMLToAstConversion("for (i = 0; i < 10; ++i) x++");
     testConversion("for (i = 0; i < 10; ++i) {x++}");
     testConversion("for (i = 0; i < 10; ++i) {2 + 3; q = 2 + 3; "
         + "var v = y * z; g = function(a) {true; var b = a + 1;"
         + "return a * a}}");

     testConversion("for(;true;) {break}");
     testConversion("for(i = 0; i < 10; ++i) {if (i > 5) {break}}");
     testConversion("s: for(i = 0; i < 10; ++i) {if (i > 5) {break s}}");
     testConversion("for (i = 0;true; ++i) {"
         + "if (i % 2) {continue} else {var x = i / 3; f(x)}}");
   }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testForIn
  public void testForIn() throws Exception {
    testConversion("for (var i in x) {}");
    testConversion("for (var i in x) {;}");
    testConversion("for (var i in x) {f(x)}");
    testConversion("s: for(var i in x) {if (i > 5) {break s}}");
    testConversion("for (var i in x) {if (i % 2) {"
        + "continue} else {var x = i / 3; f(x)}}");
    testConversion("for (var i in x) {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}}");

    testConversion("for (i in x) {}");
    testConversion("for (i in x) {;}");
    testConversion("for (i in x) {f(x)}");
    testConversion("s: for (i in x) {if (i > 5) {break s}}");
    testConversion("for (i in x) {if (i % 2) {"
        + "continue} else {var x = i / 3; f(x)}}");
    testConversion("for (i in x) {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}}");

  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testFunctions
  public void testFunctions() throws Exception {
    testConversion("(function () {})");
    testConversion("(function (x, y) {})");
    testConversion("(function () {})()");
    testConversion("(function (x, y) {})()");
    testConversion("[ function f() {} ]");
    testConversion("var f = function f() {};");
    testConversion("for (function f() {};true;) {}");
    testConversion("x = (function (x, y) {})");

    testConversion("function f() {}");
    testConversion("for (;true;) { function f() {} }");

    testConversion("function f() {;}");
    testConversion("function f() {x}");
    testConversion("function f() {x;y;z}");
    testConversion("function f() {{}}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testIfElse1
  public void testIfElse1() throws Exception {
    testConversion("if (true) {x = 1}");
    testConversion("if (true) {x = 1} else {x = 2}");
    testConversion("if (f(f(f()))) {x = 1} else {x = 2}");
    testConversion("if ((f(f(f())))()) {x = 1} else {x = 2}");
    testConversion("if (true) {x = 1}; x = 1;");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testLabels
  public void testLabels() throws Exception {
    testConversion("s: ;");
    testConversion("s: {;}");
    testConversion("s: while(true) {;}");
    testConversion("s: switch (x) {case 'a': break s;}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testLogicalExpr
  public void testLogicalExpr() throws Exception {
    testConversion("a && b");
    testConversion("a || b");
    testConversion("a && b || c");
    testConversion("a && (b || c)");
    testConversion("f(x) && (function (x) {"
        + "return x % 2 == 0 })(z) || z % 3 == 0 ? true : false");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testMathExpr
  public void testMathExpr() throws Exception {
    testConversion("2 + 3 * 4");
    testConversion("(2 + 3) * 4");
    testConversion("2 * (3 + 4)");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testMember
  public void testMember() throws Exception {
    testConversion("o.x");
    testConversion("a.b.c");
    testConversion("a.b.c.d");
    testConversion("o[x]");
    testConversion("o[0]");
    testConversion("o[2 + 3 * 4]");
    testConversion("o[(function (x){var y = g(x) << 1; return y * x})()]");
    testConversion("o[o.x]");
    testConversion("o.x[x]");
    testConversion("a.b[o.x]");
    testConversion("a.b[1]");
    testConversion("a[b[c[d]]].x");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testNew
  public void testNew() throws Exception {
    testConversion("new A");
    testConversion("new A()");

    testConversion("new A(x, y, z)");
    testConversion("new A(f(x), g(y), h(z))");
    testConversion("new A(x, new B(x, y), z)");
    testConversion("new A(1), new B()");
    testConversion("new A, B");

    testConversion("x = new A(a)");
    testConversion("var x = new A(a, b)");
    testConversion("var x = new A(1), y = new B()");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testObject0
  public void testObject0() throws Exception {
    
    
    
    
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testObject
  public void testObject() throws Exception {
    testConversion("x = {}");
    testConversion("var x = {}");
    testConversion("x = {x: 1, y: 2}");
    
    
    testConversion("x = {x: null}");
    testConversion("x = {a: function f() {}}");
    
    testConversion("x = {a: f()}");
    
    testConversion("x = {a: function f() {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}}}");
    
    
    testConversion("x = {get a() {return 1}}");
    testConversion("x = {set a(b) {}}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testOperators
  public void testOperators() throws Exception {
    testConversion("x instanceof Null");
    testConversion("!x instanceof A");
    testConversion("!(x instanceof A)");

    testConversion("'a' in x");
    testConversion("if('a' in x) {f(x)}");
    testConversion("undefined in A");
    testConversion("!(Number(1) in [2, 3, 4])");

    testConversion("true ? x : y");
    testConversion("(function() {var y = 2 + 3 * 4; return y >> 1})() ? x : y");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testReturnStatement
  public void testReturnStatement() throws Exception {
    testConversion("x = function f() {return}");
    testConversion("x = function f() {return 1}");
    testConversion("x = function f() {return 2 + 3 / 4}");
    testConversion("x = function f() {return function() {}}");
    testConversion("x = function f() {var y = 2; "
        + "return function() {return y * 3}}");
    testConversion("x = function f() {z = 2 + 3; "
        + "return (function(z) {return z * y})(z)}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testRegExp
  public void testRegExp() throws Exception {
    testConversion("/ab/");
    testConversion("/ab/g");
    testConversion("x = /ab/");
    testConversion("x = /ab/g");
    testConversion("var x = /ab/");
    testConversion("var x = /ab/g");
    testConversion("function f() {"
        + "/ab/; var x = /ab/; (function g() {/ab/; var x = /ab/})()}");
    testConversion("var f = function () {return /ab/g;}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testSimplePrograms
  public void testSimplePrograms() throws Exception {
    testConversion(";");
    testConversion("1");
    testConversion("x");
    testConversion("x=1");
    testConversion("{}");
    testConversion("{;}");
    testConversion("{x=1}");
    testConversion("x='a'");

    testConversion("true");
    testConversion("false");
    testConversion("x=true");
    testConversion("x=false");

    testConversion("undefined");
    testConversion("x=undefined");

    testConversion("null");
    testConversion("x = null");

    testConversion("this");
    testConversion("2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}");

    testConversion("a; b");
    testConversion("a; b; c; d");

    testConversion("x = function () {}");
    testConversion("x = function f() {}");

    testConversion("x = function (arg1, arg2) {}");
    testConversion("x = function f(arg1, arg2) {}");

    testConversion("x = function f(arg1, arg2) {1}");
    testConversion("x = function f(arg1, arg2) {x}");

    testConversion("x = function f(arg1, arg2) {x = 1 + 1}");

    testConversion("var re = new RegExp(document.a.b.c);"
        + "var m = re.exec(document.a.b.c);");

  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testSwitch
  public void testSwitch() throws Exception {
  testConversion("switch (x) {}");
  testConversion("switch (x) {case 'a':}");
  testConversion("switch (x) {case 'a':case 'b':}");
  testConversion("switch (x) {case 'a':case 'b': x}");
  testConversion("switch (x) {case 'a':case 'b': {;}}");
  testConversion("switch (x) {case 'a':case 'b': f()}");
  testConversion("switch (x) {case 'x': case 'y': {;} case 'a':case 'b': f()}");
  testConversion("switch (x) {case 'a': f(x)}");
  testConversion("switch (x) {case 'a': {f()} {g(x)}}");
  testConversion("switch (x) {case 'a': f(); g(x)}");
  testConversion("switch (x) {default: ;}");
  testConversion("switch (x) {default:case 'a': ;}");
  testConversion("switch (x) {case 'a':case'b':default: f()}");
  testConversion("switch (x) {default:f(x); g(); case 'a': ; case 'b': g(x)}");
  testConversion("switch (x) {case 'a': default: {f(x); g(z)} case 'b': g(x)}");
  testConversion("switch (x) {case x: {;}}");
}

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testType
  public void testType() throws Exception {
    testConversion("undefined");
    testConversion("null");

    testConversion("0");
    testConversion("+0");
    testConversion("0.0");

    testConversion("3.14");
    testConversion("+3.14");

    testConversion("true");
    testConversion("false");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testThis
  public void testThis() throws Exception {
    testConversion("this");
    testConversion("var x = this");
    testConversion("this.foo()");
    testConversion("var x = this.foo()");
    testConversion("this.bar");
    testConversion("var x = this.bar()");
    testConversion("switch(this) {}");
    testConversion("x + this");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testThrow
  public void testThrow() throws Exception {
    testConversion("throw e");
    testConversion("throw 2 + 3 * 4");
    testConversion("throw (function () {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; return a * a}})()");
    testConversion("throw f(x)");
    testConversion("throw f(f(f(x)))");
    testConversion("throw (f(f(x), y))()");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testTry
  public void testTry() throws Exception {
    testConversion("try {} catch (e) {}");
    testConversion("try {;} catch (e) {;}");
    testConversion("try {var x = 0; y / x} catch (e) {f(e)}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; "
        + "h(q)} catch (e) {f(x)}");

    testConversion("try {} finally {}");
    testConversion("try {;} finally {;}");
    testConversion("try {var x = 0; y / x} finally {f(y)}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; "
        + "h(q)} finally {f(x)}");

    testConversion("try {} catch (e) {} finally {}");
    testConversion("try {;} catch (e) {;} finally {;}");
    testConversion("try {var x = 0; y / x} catch (e) {;} finally {;}");
    testConversion("try {2 + 3; q = 2 + 3; var v = y * z; "
        + "g = function(a) {true; var b = a + 1; h(q); return a * a}; h(q)} "
        + "catch (e) {f(x)} finally {f(x)}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testTypeof
  public void testTypeof() throws Exception {
    testConversion("typeof undefined");
    testConversion("typeof null");
    testConversion("typeof 1");
    testConversion("typeof 'a'");
    testConversion("typeof false");

    testConversion("typeof Null()");
    testConversion("typeof Number(1)");
    testConversion("typeof String('a')");
    testConversion("typeof Boolean(0)");

    testConversion("typeof x");
    testConversion("typeof new A()");
    testConversion("typeof new A(x)");
    testConversion("typeof f(x)");
    testConversion("typeof (function() {})()");
    testConversion("typeof 2 + 3 * 4");

    testConversion("typeof typeof x");
    testConversion("typeof typeof typeof x");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testUnaryExpressions
  public void testUnaryExpressions() throws Exception {
    testConversion("!x");
    testConversion("!null");
    testConversion("!3.14");
    testConversion("!true");

    testConversion("~x");
    testConversion("~null");
    testConversion("~3.14");
    testConversion("~true");

    testConversion("+x");
    testConversion("+null");
    testConversion("+3.14");
    testConversion("+true");

    testConversion("-x");
    testConversion("-null");
    testConversion("-true");

    testConversion("!~+-z");
    testConversion("void x");
    testConversion("void null");
    testConversion("void void !x");
    testConversion("void (x + 1)");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testVarDeclarations
  public void testVarDeclarations() throws Exception {
    testConversion("var x");
    testConversion("var x = 1");
    testConversion("var x = 1 + 1");
    testConversion("var x = 'a' + 'b'");

    testConversion("var x, y, z");
    testConversion("var x = 2, y = 2 * x, z");

    testConversion("var x = function () {}");
    testConversion("var x = function f() {}");
    testConversion("var x = function f(arg1, arg2) {}");

    testConversion("var x = function f(arg1, arg2) {1}");
    testConversion("var x = function f(arg1, arg2) {x}");
    testConversion("var x = function f(arg1, arg2) {x = 2 * 3}");

    testConversion("var x = function f() {var x}");
    testConversion("var x = function f() {var y = (z + 2) * q}");

    testConversion("var x = function f(a, b) {"
        + "var y = function g(a, b) {z = a + b}}");
  }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testWhile
  public void testWhile() throws Exception {
     testConversion("while (true) {;}");
     testConversion("while (true) {f()}");
     testConversion("while (f(x, y)) {break;}");
     testConversion("while (f(f(f(x, y)))) {}");
     testConversion("while ((f(f(f(x, y))))()) {}");

     testConversion("while (x--) {2 + 3; q = 2 + 3; var v = y * z; "
         + "g = function(a) {true; var b = a + 1; return a * a}}");
   }

// com.google.javascript.jscomp.jsonml.JsonMLConversionTest::testWith
  public void testWith() throws Exception {
     testConversion("with ({}) {}");
     testConversion("with ({}) {;}");
     testConversion("with (x) {}");
     testConversion("with (x) {f(x)}");
     testConversion("with ({a: function f() {}}) {f(1)}");
     testConversion("with ({z: function f() {2 + 3; q = 2 + 3; var v = y * z;"
         + "g = function(a) {true; var b = a + 1; return a * a}}}) {f(1)}");
     testConversion("with (x in X) {x++}");
   }

// com.google.javascript.jscomp.jsonml.SecureCompilerTest::testCompilerInterface
  public void testCompilerInterface() throws Exception {
    testString(SIMPLE_SOURCE);
    testInvalidString(SYNTAX_ERROR);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testScript
  public void testScript() throws Exception {
    parse("");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testStrictScript
  public void testStrictScript() throws Exception {
    assertNull(newParse("").getDirectives());
    assertEquals(
        Sets.newHashSet("use strict"),
        newParse("'use strict'").getDirectives());
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testName
  public void testName() throws Exception {
    parse("a");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral
  public void testArrayLiteral() throws Exception {
    parse("[a, b]");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral2
  public void testArrayLiteral2() throws Exception {
    testNewParser("[a, , b]",
      "SCRIPT 0 [length: 8]\n" +
      "    EXPR_RESULT 0 [length: 8]\n" +
      "        ARRAYLIT 0 [length: 8]\n" +
      "            NAME a 0 [length: 1]\n" +
      "            EMPTY 0 [length: 1]\n" +
      "            NAME b 0 [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral3
  public void testArrayLiteral3() throws Exception {
    parse("[a, undefined, b]");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testArrayLiteral4
  public void testArrayLiteral4() throws Exception {
    testNewParser("[,,,a,,b]",
      "SCRIPT 0 [length: 9]\n" +
      "    EXPR_RESULT 0 [length: 9]\n" +
      "        ARRAYLIT 0 [length: 9]\n" +
      "            EMPTY 0 [length: 1]\n" +
      "            EMPTY 0 [length: 1]\n" +
      "            EMPTY 0 [length: 1]\n" +
      "            NAME a 0 [length: 1]\n" +
      "            EMPTY 0 [length: 1]\n" +
      "            NAME b 0 [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testAssignment
  public void testAssignment() throws Exception {
    parse("a = b");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testAssignment2
  public void testAssignment2() throws Exception {
    parse("a += b");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testInfix
  public void testInfix() throws Exception {
    parse("a + b");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testScope
  public void testScope() throws Exception {
    parse("{ a; b; c; }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testConditional
  public void testConditional() throws Exception {
    parse("a ? b : c");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testEmpty
  public void testEmpty() throws Exception {
    parse(";;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testIf
  public void testIf() throws Exception {
    parse("if (a) { b }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testIf2
  public void testIf2() throws Exception {
    parse("if (a) { b } else { c }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNumber
  public void testNumber() throws Exception {
    parse("0");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNumber2
  public void testNumber2() throws Exception {
    parse("1.2");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testString
  public void testString() throws Exception {
    parse("'a'");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testString2
  public void testString2() throws Exception {
    parse("\"a\"");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testUnary
  public void testUnary() throws Exception {
    parse("-a");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testUnary2
  public void testUnary2() throws Exception {
    parse("a++");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testUnary3
  public void testUnary3() throws Exception {
    parse("++a");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testVar
  public void testVar() throws Exception {
    parse("var a = 1");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testVar2
  public void testVar2() throws Exception {
    parse("var a = 1, b = 2");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testVar3
  public void testVar3() throws Exception {
    parse("var a, b = 1");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testElementGet
  public void testElementGet() throws Exception {
    parse("a[i]");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPropertyGet
  public void testPropertyGet() throws Exception {
    parse("a.b");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testRegexp
  public void testRegexp() throws Exception {
    parse("/ab+c/");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testRegexp2
  public void testRegexp2() throws Exception {
    parse("/ab+c/g");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunctionCall
  public void testFunctionCall() throws Exception {
    parse("a()");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunctionCall2
  public void testFunctionCall2() throws Exception {
    parse("a(b)");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunctionCall3
  public void testFunctionCall3() throws Exception {
    parse("a(b, c)");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNew
  public void testNew() throws Exception {
    parse("new A()");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNew2
  public void testNew2() throws Exception {
    parse("new A(b)");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNew3
  public void testNew3() throws Exception {
    parse("new A(b, c)");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction
  public void testFunction() {
    parse("function f() {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction2
  public void testFunction2() {
    parse("(function() {})");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction3
  public void testFunction3() {
    parse("function f(a) {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction4
  public void testFunction4() {
    parse("(function(a) {})");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction5
  public void testFunction5() {
    parse("function f(a, b) {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunction6
  public void testFunction6() {
    parse("(function(a, b) {})");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturn
  public void testReturn() {
    parse("(function() {return 1;})");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturn2
  public void testReturn2() {
    parse("function f() {return;}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturn3
  public void testReturn3() {
    parse("function f(){return x?1:2}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testThrow
  public void testThrow() {
    parse("throw e");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testWith
  public void testWith() {
    parse("with (a) { b }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral
  public void testObjectLiteral() {
    newParse("var o = {}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral2
  public void testObjectLiteral2() {
    newParse("var o = {a: 1}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral3
  public void testObjectLiteral3() {
    newParse("var o = {a: 1, b: 2}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral4
  public void testObjectLiteral4() {
    newParse("var o = {1: 'a'}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral5
  public void testObjectLiteral5() {
    newParse("var o = {'a': 'a'}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral6
  public void testObjectLiteral6() {
    testNewParser("({1: true})",
      "SCRIPT 0 [length: 11]\n" +
      "    EXPR_RESULT 0 [length: 10]\n" +
      "        OBJECTLIT 0 [parenthesized: true] [length: 9]\n" +
      "            STRING 1 0 [quoted: 1] [length: 1]\n" +
      "                TRUE 0 [length: 4]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral7
  public void testObjectLiteral7() {
    mode = LanguageMode.ECMASCRIPT5;

    testNewParser("({get 1() {}})",
        "SCRIPT 0 [length: 14]\n" +
        "    EXPR_RESULT 0 [length: 13]\n" +
        "        OBJECTLIT 0 [parenthesized: true] [length: 12]\n" +
        "            GET 1 0 [quoted: 1] [length: 1]\n" +
        "                FUNCTION  0 [length: 6]\n" +
        "                    NAME  0\n" +
        "                    LP 0\n" +
        "                    BLOCK 0 [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLiteral8
  public void testObjectLiteral8() {
    mode = LanguageMode.ECMASCRIPT5;

    testNewParser("({set 1(a) {}})",
        "SCRIPT 0 [length: 15]\n" +
        "    EXPR_RESULT 0 [length: 14]\n" +
        "        OBJECTLIT 0 [parenthesized: true] [length: 13]\n" +
        "            SET 1 0 [quoted: 1] [length: 1]\n" +
        "                FUNCTION  0 [length: 7]\n" +
        "                    NAME  0\n" +
        "                    LP 0\n" +
        "                        NAME a 0 [length: 1]\n" +
        "                    BLOCK 0 [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testKeywordLiteral
  public void testKeywordLiteral() {
    parse("true");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testWhile
  public void testWhile() {
    parse("while (!a) { a--; }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testParen
  public void testParen() {
    parse("(a)");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testParen2
  public void testParen2() {
    parse("(1+1)*2");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFor
  public void testFor() {
    parse("for (var i = 0; i < n; i++) { a(i); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testForIn
  public void testForIn() {
    parse("for (i in a) { b(i); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBreak
  public void testBreak() {
    parse("while (true) { break; }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testContinue
  public void testContinue() {
    parse("while (true) { continue; }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDoLoop
  public void testDoLoop() {
    parse("do { a() } while (b());");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel
  public void testLabel() {
    testNewParser("foo: bar",
        "SCRIPT 0 [length: 8]\n" +
        "    LABEL 0 [length: 4]\n" +
        "        LABEL_NAME foo 0 [length: 4]\n" +
        "        EXPR_RESULT 0 [length: 3]\n" +
        "            NAME bar 0 [length: 3]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel2
  public void testLabel2() {
    testNewParser("l: while (f()) { if (g()) { continue l; } }",
        "SCRIPT 0 [length: 43]\n" +
        "    LABEL 0 [length: 2]\n" +
        "        LABEL_NAME l 0 [length: 2]\n" +
        "        WHILE 0 [length: 40]\n" +
        "            CALL 0 [length: 3]\n" +
        "                NAME f 0 [length: 1]\n" +
        "            BLOCK 0 [length: 28]\n" +
        "                IF 0 [length: 24]\n" +
        "                    CALL 0 [length: 3]\n" +
        "                        NAME g 0 [length: 1]\n" +
        "                    BLOCK 0 [length: 15]\n" +
        "                        CONTINUE 0 [length: 11]\n" +
        "                            LABEL_NAME l 0 [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabel3
  public void testLabel3() {
    testNewParser("Foo:Bar:X:{ break Bar; }",
        "SCRIPT 0 [length: 24]\n" +
        "    LABEL 0 [length: 4]\n" +
        "        LABEL_NAME Foo 0 [length: 4]\n" +
        "        LABEL 0 [length: 4]\n" +
        "            LABEL_NAME Bar 0 [length: 4]\n" +
        "            LABEL 0 [length: 2]\n" +
        "                LABEL_NAME X 0 [length: 2]\n" +
        "                BLOCK 0 [length: 14]\n" +
        "                    BREAK 0 [length: 10]\n" +
        "                        LABEL_NAME Bar 0 [length: 3]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation1
  public void testNegation1() {
    testNewParser("-a",
        "SCRIPT 0 [length: 2]\n" +
        "    EXPR_RESULT 0 [length: 2]\n" +
        "        NEG 0 [length: 2]\n" +
        "            NAME a 0 [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation2
  public void testNegation2() {
    testNewParser("-2",
        "SCRIPT 0 [length: 2]\n" +
        "    EXPR_RESULT 0 [length: 2]\n" +
        "        NUMBER -2.0 0 [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNegation3
  public void testNegation3() {
    testNewParser("1 - -2",
        "SCRIPT 0 [length: 6]\n" +
        "    EXPR_RESULT 0 [length: 6]\n" +
        "        SUB 0 [length: 6]\n" +
        "            NUMBER 1.0 0 [length: 1]\n" +
        "            NUMBER -2.0 0 [length: 1]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testGetter
  public void testGetter() {
    mode = LanguageMode.ECMASCRIPT5;
    testNewParser("({get a() {}})",
        "SCRIPT 0 [length: 14]\n" +
        "    EXPR_RESULT 0 [length: 13]\n" +
        "        OBJECTLIT 0 [parenthesized: true] [length: 12]\n" +
        "            GET a 0 [length: 1]\n" +
        "                FUNCTION  0 [length: 6]\n" +
        "                    NAME  0\n" +
        "                    LP 0\n" +
        "                    BLOCK 0 [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSetter
  public void testSetter() {
    mode = LanguageMode.ECMASCRIPT5;
    testNewParser("({set a(x) {}})",
        "SCRIPT 0 [length: 15]\n" +
        "    EXPR_RESULT 0 [length: 14]\n" +
        "        OBJECTLIT 0 [parenthesized: true] [length: 13]\n" +
        "            SET a 0 [length: 1]\n" +
        "                FUNCTION  0 [length: 7]\n" +
        "                    NAME  0\n" +
        "                    LP 0\n" +
        "                        NAME x 0 [length: 1]\n" +
        "                    BLOCK 0 [length: 2]\n");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSwitch
  public void testSwitch() {
    parse("switch (e) {" +
        "case 'a': a(); break;" +
        "case 'b': b();" +
        "case 'c': c(); }");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSwitch2
  public void testSwitch2() {
    parse("switch (e) { case 'a': a(); break; default: b();}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSwitch3
  public void testSwitch3() {
    parse("function f(){switch(x){default:case 1:return 2}}");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDebugger
  public void testDebugger() {
    parse("debugger;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete1
  public void testDelete1() {
    testNoParseError("delete a.b;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete2
  public void testDelete2() {
    testNoParseError("delete a['b'];");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete3
  public void testDelete3() {
    
    
    testNoParseError("delete a;");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testDelete4
  public void testDelete4() {
    testParseError("delete 'x';",
        "Invalid delete operand. Only properties can be deleted.");
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommentPositions1
  public void testCommentPositions1() {
    Node root = newParse("function a(x) {};" +
        "function b(x) {}");
    Node a = root.getFirstChild();
    Node b = root.getLastChild();
    assertMarkerPosition(a, 0, 4);
    assertMarkerPosition(b, 0, 45);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommentPositions2
  public void testCommentPositions2() {
    Node root = newParse(
        "\n" +
        "\n" +
        "function a(x) {};\n" +
        "\n" +
        "\n" +
        "\n" +
        "\n" +
        "function b(x) {};");
    assertMarkerPosition(root.getFirstChild(), 3, 4);
    assertMarkerPosition(root.getFirstChild().getNext().getNext(), 10, 6);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLiteralLocation
   public void testLiteralLocation() {
    Node root = newParse(
        "\nvar d =\n" +
        "    \"foo\";\n" +
        "var e =\n" +
        "    1;\n" +
        "var f = \n" +
        "    1.2;\n" +
        "var g = \n" +
        "    2e5;\n" +
        "var h = \n" +
        "    'bar';\n");

    Node firstStmt = root.getFirstChild();
    Node firstLiteral = firstStmt.getFirstChild().getFirstChild();
    Node secondStmt = firstStmt.getNext();
    Node secondLiteral = secondStmt.getFirstChild().getFirstChild();
    Node thirdStmt = secondStmt.getNext();
    Node thirdLiteral = thirdStmt.getFirstChild().getFirstChild();
    Node fourthStmt = thirdStmt.getNext();
    Node fourthLiteral = fourthStmt.getFirstChild().getFirstChild();
    Node fifthStmt = fourthStmt.getNext();
    Node fifthLiteral = fifthStmt.getFirstChild().getFirstChild();

    assertNodePosition(2, 4, firstLiteral);
    assertNodePosition(4, 4, secondLiteral);
    assertNodePosition(6, 4, thirdLiteral);
    assertNodePosition(8, 4, fourthLiteral);
    assertNodePosition(10, 4, fifthLiteral);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testSwitchLocation
  public void testSwitchLocation() {
    Node root = newParse(
        "\nswitch (a) {\n" +
        "  
        "   case 1:\n" +
        "     b++;\n" +
        "   case 2:\n" +
        "   default:\n" +
        "     b--;\n" +
        "  }\n");

    Node switchStmt = root.getFirstChild();
    Node switchVar = switchStmt.getFirstChild();
    Node firstCase = switchVar.getNext();
    Node caseArg = firstCase.getFirstChild();
    Node caseBody = caseArg.getNext();
    Node caseExprStmt = caseBody.getFirstChild();
    Node incrExpr = caseExprStmt.getFirstChild();
    Node incrVar = incrExpr.getFirstChild();
    Node secondCase = firstCase.getNext();
    Node defaultCase = secondCase.getNext();

    assertNodePosition(1, 0, switchStmt);
    assertNodePosition(1, 8, switchVar);
    assertNodePosition(3, 3, firstCase);
    assertNodePosition(3, 8, caseArg);
    assertNodePosition(3, 3, caseBody);
    assertNodePosition(4, 5, caseExprStmt);
    assertNodePosition(4, 5, incrExpr);
    assertNodePosition(4, 5, incrVar);
    assertNodePosition(5, 3, secondCase);
    assertNodePosition(6, 3, defaultCase);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testFunctionParamLocation
  public void testFunctionParamLocation() {
    Node root = newParse(
        "\nfunction\n" +
        "     foo(a,\n" +
        "     b,\n" +
        "     c)\n" +
        "{}\n");

    Node function = root.getFirstChild();
    Node functionName = function.getFirstChild();
    Node params = functionName.getNext();
    Node param1 = params.getFirstChild();
    Node param2 = param1.getNext();
    Node param3 = param2.getNext();
    Node body = params.getNext();

    assertNodePosition(2, 5, function);
    assertNodePosition(2, 5, functionName);
    
    
    
    assertNodePosition(2, 8, params);
    assertNodePosition(2, 9, param1);
    assertNodePosition(3, 5, param2);
    assertNodePosition(4, 5, param3);
    assertNodePosition(5, 0, body);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testVarDeclLocation
  public void testVarDeclLocation() {
    Node root = newParse(
        "\nvar\n" +
        "    a =\n" +
        "    3\n");
    Node varDecl = root.getFirstChild();
    Node varName = varDecl.getFirstChild();
    Node varExpr = varName.getFirstChild();

    assertNodePosition(1, 0, varDecl);
    assertNodePosition(2, 4, varName);
    assertNodePosition(3, 4, varExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testReturnLocation
  public void testReturnLocation() {
    Node root = newParse(
        "\nfunction\n" +
        "    foo(\n" +
        "    a,\n" +
        "    b,\n" +
        "    c) {\n" +
        "    return\n" +
        "    4;\n" +
        "}\n");

    Node function = root.getFirstChild();
    Node functionName = function.getFirstChild();
    Node params = functionName.getNext();
    Node body = params.getNext();
    Node returnStmt = body.getFirstChild();
    Node exprStmt = returnStmt.getNext();
    Node returnVal = exprStmt.getFirstChild();

    assertNodePosition(6, 4, returnStmt);
    assertNodePosition(7, 4, exprStmt);
    assertNodePosition(7, 4, returnVal);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLinenoFor
  public void testLinenoFor() {
    Node root = newParse(
        "\nfor(\n" +
        ";\n" +
        ";\n" +
        ") {\n" +
        "}\n");

    Node forNode = root.getFirstChild();
    Node initClause= forNode.getFirstChild();
    Node condClause = initClause.getNext();
    Node incrClause = condClause.getNext();

    assertNodePosition(1, 0, forNode);
    assertNodePosition(2, 0, initClause);
    assertNodePosition(3, 0, condClause);
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBinaryExprLocation
  public void testBinaryExprLocation() {
    Node root = newParse(
        "\nvar d = a\n" +
        "    + \n" +
        "    b;\n" +
        "var\n" +
        "    e =\n" +
        "    a +\n" +
        "    c;\n" +
        "var f = b\n" +
        "    / c;\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node firstVarAdd = firstVar.getFirstChild();

    Node secondVarDecl = firstVarDecl.getNext();
    Node secondVar = secondVarDecl.getFirstChild();
    Node secondVarAdd = secondVar.getFirstChild();

    Node thirdVarDecl = secondVarDecl.getNext();
    Node thirdVar = thirdVarDecl.getFirstChild();
    Node thirdVarAdd = thirdVar.getFirstChild();

    assertNodePosition(1, 0, firstVarDecl);
    assertNodePosition(1, 4, firstVar);
    assertNodePosition(2, 4, firstVarAdd);
    assertNodePosition(1, 8, firstVarAdd.getFirstChild());
    assertNodePosition(3, 4, firstVarAdd.getLastChild());

    assertNodePosition(4, 0, secondVarDecl);
    assertNodePosition(5, 4, secondVar);
    assertNodePosition(6, 6, secondVarAdd);
    assertNodePosition(6, 4, secondVarAdd.getFirstChild());
    assertNodePosition(7, 4, secondVarAdd.getLastChild());

    assertNodePosition(8, 0, thirdVarDecl);
    assertNodePosition(8, 4, thirdVar);
    assertNodePosition(9, 4, thirdVarAdd);
    assertNodePosition(8, 8, thirdVarAdd.getFirstChild());
    assertNodePosition(9, 6, thirdVarAdd.getLastChild());
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPrefixLocation
  public void testPrefixLocation() {
    Node root = newParse(
         "\na++;\n" +
         "--\n" +
         "b;\n");

    Node firstStmt = root.getFirstChild();
    Node secondStmt = firstStmt.getNext();
    Node firstOp = firstStmt.getFirstChild();
    Node secondOp = secondStmt.getFirstChild();

    assertNodePosition(1, 0, firstOp);
    assertNodePosition(2, 0, secondOp);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testIfLocation
  public void testIfLocation() {
    Node root = newParse(
        "\nif\n" +
        "  (a == 3)\n" +
        "{\n" +
        "  b = 0;\n" +
        "}\n" +
        "  else\n" +
        "{\n" +
        "  c = 1;\n" +
        "}\n");

    Node ifStmt = root.getFirstChild();
    Node eqClause = ifStmt.getFirstChild();
    Node thenClause = eqClause.getNext();
    Node elseClause = thenClause.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(2, 5, eqClause);
    assertNodePosition(3, 0, thenClause);
    assertNodePosition(7, 0, elseClause);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryLocation
  public void testTryLocation() {
     Node root = newParse(
         "\ntry {\n" +
         "  var x = 1;\n" +
         "} catch\n" +
         "   (err)\n" +
         "{\n" +
         "} finally {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node catchVarBlock = catchBlock.getFirstChild();
    Node catchVar = catchVarBlock.getFirstChild();
    Node finallyBlock = catchBlock.getNext();
    Node finallyStmt = finallyBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 2, catchVarBlock);
    assertNodePosition(4, 4, catchVar);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(6, 10, finallyBlock);
    assertNodePosition(7, 2, finallyStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testHookLocation
  public void testHookLocation() {
    Node root = newParse(
        "\na\n" +
        "?\n" +
        "b\n" +
        ":\n" +
        "c\n" +
        ";\n");

    Node hookExpr = root.getFirstChild().getFirstChild();
    Node condExpr = hookExpr.getFirstChild();
    Node thenExpr = condExpr.getNext();
    Node elseExpr = thenExpr.getNext();

    assertNodePosition(2, 0, hookExpr);
    assertNodePosition(1, 0, condExpr);
    assertNodePosition(3, 0, thenExpr);
    assertNodePosition(5, 0, elseExpr);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLabelLocation
  public void testLabelLocation() {
    Node root = newParse(
        "\nfoo:\n" +
        "a = 1;\n" +
        "bar:\n" +
        "b = 2;\n");

    Node firstStmt = root.getFirstChild();
    Node secondStmt = firstStmt.getNext();

    assertNodePosition(1, 0, firstStmt);
    assertNodePosition(3, 0, secondStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCompareLocation
  public void testCompareLocation() {
    Node root = newParse(
        "\na\n" +
        "<\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(2, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
   }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testEqualityLocation
  public void testEqualityLocation() {
    Node root = newParse(
        "\na\n" +
        "==\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(2, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testPlusEqLocation
  public void testPlusEqLocation() {
    Node root = newParse(
        "\na\n" +
        "+=\n" +
        "b\n");

    Node condClause = root.getFirstChild().getFirstChild();
    Node lhs = condClause.getFirstChild();
    Node rhs = lhs.getNext();

    assertNodePosition(2, 0, condClause);
    assertNodePosition(1, 0, lhs);
    assertNodePosition(3, 0, rhs);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCommaLocation
  public void testCommaLocation() {
    Node root = newParse(
        "\na,\n" +
        "b,\n" +
        "c;\n");

    Node statement = root.getFirstChild();
    Node comma1 = statement.getFirstChild();
    Node comma2 = comma1.getFirstChild();
    Node cRef = comma2.getNext();
    Node aRef = comma2.getFirstChild();
    Node bRef = aRef.getNext();

    assertNodePosition(1, 1, comma2);
    assertNodePosition(1, 0, aRef);
    assertNodePosition(2, 0, bRef);
    assertNodePosition(3, 0, cRef);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testRegexpLocation
  public void testRegexpLocation() {
    Node root = newParse(
        "\nvar path =\n" +
        "replace(\n" +
        "/a/g," +
        "'/');\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node firstInitializer = firstVar.getFirstChild();
    Node callNode = firstVar.getFirstChild();
    Node fnName = callNode.getFirstChild();
    Node regexObject = fnName.getNext();
    Node aString = regexObject.getFirstChild();
    Node endRegexString = regexObject.getNext();

    assertNodePosition(1, 0, firstVarDecl);
    assertNodePosition(1, 4, firstVar);
    assertNodePosition(2, 7, callNode);
    assertNodePosition(2, 0, fnName);
    assertNodePosition(3, 0, regexObject);
    assertNodePosition(3, 0, aString);
    assertNodePosition(3, 5, endRegexString);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testNestedOr
  public void testNestedOr() {
    Node root = newParse(
        "\nif (a && \n" +
        "    b() || \n" +
        "    \n" +
        "    c) {\n" +
        "}\n"
    );

    Node ifStmt = root.getFirstChild();
    Node orClause = ifStmt.getFirstChild();
    Node andClause = orClause.getFirstChild();
    Node cName = andClause.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(2, 8, orClause);
    assertNodePosition(1, 6, andClause);
    assertNodePosition(4, 4, cName);

  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testBitwiseOps
  public void testBitwiseOps() {
      Node root = newParse(
        "\nif (a & \n" +
        "    b() | \n" +
        "    \n" +
        "    c) {\n" +
        "}\n"
    );

    Node ifStmt = root.getFirstChild();
    Node bitOr = ifStmt.getFirstChild();
    Node bitAnd = bitOr.getFirstChild();
    Node cName = bitAnd.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(2, 8, bitOr);
    assertNodePosition(1, 6, bitAnd);
    assertNodePosition(4, 4, cName);

  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testObjectLitLocation
  public void testObjectLitLocation() {
    Node root = newParse(
        "\nvar foo =\n" +
        "{ \n" +
        "'A' : 'A', \n" +
        "'B' : 'B', \n" +
        "'C' :\n" +
        "    'C' \n" +
        "};\n");

    Node firstVarDecl = root.getFirstChild();
    Node firstVar = firstVarDecl.getFirstChild();
    Node firstObjectLit = firstVar.getFirstChild();
    Node firstKey = firstObjectLit.getFirstChild();
    Node firstValue = firstKey.getFirstChild();

    Node secondKey = firstKey.getNext();
    Node secondValue = secondKey.getFirstChild();

    Node thirdKey = secondKey.getNext();
    Node thirdValue = thirdKey.getFirstChild();

    assertNodePosition(1, 4, firstVar);
    assertNodePosition(2, 0, firstObjectLit);

    assertNodePosition(3, 0, firstKey);
    assertNodePosition(3, 6, firstValue);

    assertNodePosition(4, 0, secondKey);
    assertNodePosition(4, 6, secondValue);

    assertNodePosition(5, 0, thirdKey);
    assertNodePosition(6, 4, thirdValue);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryWithoutCatchLocation
  public void testTryWithoutCatchLocation() {
     Node root = newParse(
         "\ntry {\n" +
         "  var x = 1;\n" +
         "} finally {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node finallyBlock = catchBlock.getNext();
    Node finallyStmt = finallyBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(3, 10, finallyBlock);
    assertNodePosition(4, 2, finallyStmt);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testTryWithoutFinallyLocation
  public void testTryWithoutFinallyLocation() {
     Node root = newParse(
         "\ntry {\n" +
         "  var x = 1;\n" +
         "} catch (ex) {\n" +
         "  var y = 2;\n" +
         "}\n");

    Node tryStmt = root.getFirstChild();
    Node tryBlock = tryStmt.getFirstChild();
    Node catchBlock = tryBlock.getNext();
    Node catchStmt = catchBlock.getFirstChild();
    Node exceptionVar = catchStmt.getFirstChild();
    Node exceptionBlock = exceptionVar.getNext();
    Node varDecl = exceptionBlock.getFirstChild();

    assertNodePosition(1, 0, tryStmt);
    assertNodePosition(1, 4, tryBlock);
    assertNodePosition(3, 0, catchBlock);
    assertNodePosition(3, 2, catchStmt);
    assertNodePosition(3, 9, exceptionVar);
    assertNodePosition(3, 13, exceptionBlock);
    assertNodePosition(4, 2, varDecl);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testMultilineEqLocation
  public void testMultilineEqLocation() {
    Node  root = newParse(
        "\nif\n" +
        "    (((a == \n" +
        "  3) && \n" +
        "  (b == 2)) || \n" +
        " (c == 1)) {\n" +
        "}\n");
    Node ifStmt = root.getFirstChild();
    Node orTest = ifStmt.getFirstChild();
    Node andTest = orTest.getFirstChild();
    Node cTest = andTest.getNext();
    Node aTest = andTest.getFirstChild();
    Node bTest = aTest.getNext();

    assertNodePosition(1, 0, ifStmt);
    assertNodePosition(4, 12, orTest);
    assertNodePosition(3, 5, andTest);
    assertNodePosition(2, 9, aTest);
    assertNodePosition(4, 5, bTest);
    assertNodePosition(5, 4, cTest);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testMultilineBitTestLocation
  public void testMultilineBitTestLocation() {
    Node root = newParse(
        "\nif (\n" +
        "      ((a \n" +
        "        | 3 \n" +
        "       ) == \n" +
        "       (b \n" +
        "        & 2)) && \n" +
        "      ((a \n" +
        "         ^ 0xffff) \n" +
        "       != \n" +
        "       (c \n" +
        "        << 1))) {\n" +
        "}\n");

    Node ifStmt = root.getFirstChild();
    Node andTest = ifStmt.getFirstChild();
    Node eqTest = andTest.getFirstChild();
    Node notEqTest = eqTest.getNext();

    Node bitOrTest = eqTest.getFirstChild();
    Node bitAndTest = bitOrTest.getNext();

    Node bitXorTest = notEqTest.getFirstChild();
    Node bitShiftTest = bitXorTest.getNext();

    assertNodePosition(1, 0, ifStmt);

    assertNodePosition(4, 9, eqTest);
    assertNodePosition(9, 7, notEqTest);

    assertNodePosition(3, 8, bitOrTest);
    assertNodePosition(6, 8, bitAndTest);
    assertNodePosition(8, 9, bitXorTest);
    assertNodePosition(11, 8, bitShiftTest);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testCallLocation
  public void testCallLocation() {
    Node root = newParse(
        "\na.\n" +
        "b.\n" +
        "cccc(1);\n");

    Node exprStmt = root.getFirstChild();
    Node functionCall = exprStmt.getFirstChild();
    Node functionProp = functionCall.getFirstChild();
    Node firstNameComponent = functionProp.getFirstChild();
    Node lastNameComponent = firstNameComponent.getNext();

    assertNodePosition(3, 4, functionCall);
    
    
    
    
    assertNodePosition(3, 0, lastNameComponent);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testLinenoDeclaration
  public void testLinenoDeclaration() {
    Node root = newParse(
        "\na.\n" +
        "b=\n" +
        "function() {};\n");

    Node exprStmt = root.getFirstChild();
    Node fnAssignment =  exprStmt.getFirstChild();
    Node aDotbName = fnAssignment.getFirstChild();
    Node aName = aDotbName.getFirstChild();
    Node bName = aName.getNext();
    Node fnNode = aDotbName.getNext();
    Node fnName = fnNode.getFirstChild();

    assertNodePosition(2, 1, fnAssignment);
    
    
    assertNodePosition(1, 0, aName);
    assertNodePosition(2, 0, bName);
    assertNodePosition(3, 8, fnNode);
    assertNodePosition(3, 8, fnName);
  }

// com.google.javascript.jscomp.parsing.IRFactoryTest::testAssignmentValidation
  public void testAssignmentValidation() {
    testNoParseError("x=1");
    testNoParseError("x.y=1");
    testNoParseError("f().y=1");
    testParseError("(x||y)=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("(x?y:z)=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("f()=1", INVALID_ASSIGNMENT_TARGET);

    testNoParseError("x+=1");
    testNoParseError("x.y+=1");
    testNoParseError("f().y+=1");
    testParseError("(x||y)+=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("(x?y:z)+=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("f()+=1", INVALID_ASSIGNMENT_TARGET);

    testParseError("f()++", INVALID_INCREMENT_TARGET);
    testParseError("f()--", INVALID_DECREMENT_TARGET);
    testParseError("++f()", INVALID_INCREMENT_TARGET);
    testParseError("--f()", INVALID_DECREMENT_TARGET);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic1
  public void testParseTypeViaStatic1() throws Exception {
    Node typeNode = parseType("null");
    assertTypeEquals(NULL_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic2
  public void testParseTypeViaStatic2() throws Exception {
    Node typeNode = parseType("string");
    assertTypeEquals(STRING_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic3
  public void testParseTypeViaStatic3() throws Exception {
    Node typeNode = parseType("!Date");
    assertTypeEquals(DATE_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic4
  public void testParseTypeViaStatic4() throws Exception {
    Node typeNode = parseType("boolean|string");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, STRING_TYPE), typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInvalidTypeViaStatic
  public void testParseInvalidTypeViaStatic() throws Exception {
    Node typeNode = parseType("sometype.<anothertype");
    assertNull(typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInvalidTypeViaStatic2
  public void testParseInvalidTypeViaStatic2() throws Exception {
    Node typeNode = parseType("");
    assertNull(typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType1
  public void testParseNamedType1() throws Exception {
    assertNull(parse("@type null", "Unexpected end of file"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType2
  public void testParseNamedType2() throws Exception {
    JSDocInfo info = parse("@type null*/");
    assertTypeEquals(NULL_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType3
  public void testParseNamedType3() throws Exception {
    JSDocInfo info = parse("@type {string}*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType4
  public void testParseNamedType4() throws Exception {
    
    JSDocInfo info = parse("@type \n {string}*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType5
  public void testParseNamedType5() throws Exception {
    JSDocInfo info = parse("@type {!goog.\nBar}*/");
    assertTypeEquals(
        registry.createNamedType("goog.Bar", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType6
  public void testParseNamedType6() throws Exception {
    JSDocInfo info = parse("@type {!goog.\n * Bar.\n * Baz}*/");
    assertTypeEquals(
        registry.createNamedType("goog.Bar.Baz", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedTypeError1
  public void testParseNamedTypeError1() throws Exception {
    
    
    parse("@type {!goog\n * .Bar} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedTypeError2
  public void testParseNamedTypeError2() throws Exception {
    parse("@type {!goog.\n * Bar\n * .Baz} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType1
  public void testTypedefType1() throws Exception {
    JSDocInfo info = parse("@typedef string */");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(STRING_TYPE, info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType2
  public void testTypedefType2() throws Exception {
    JSDocInfo info = parse("@typedef \n {string}*/");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(STRING_TYPE, info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType3
  public void testTypedefType3() throws Exception {
    JSDocInfo info = parse("@typedef \n {(string|number)}*/");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(
        createUnionType(NUMBER_TYPE, STRING_TYPE),
        info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseStringType1
  public void testParseStringType1() throws Exception {
    assertTypeEquals(STRING_TYPE, parse("@type {string}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseStringType2
  public void testParseStringType2() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE, parse("@type {!String}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBooleanType1
  public void testParseBooleanType1() throws Exception {
    assertTypeEquals(BOOLEAN_TYPE, parse("@type {boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBooleanType2
  public void testParseBooleanType2() throws Exception {
    assertTypeEquals(
        BOOLEAN_OBJECT_TYPE, parse("@type {!Boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNumberType1
  public void testParseNumberType1() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@type {number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNumberType2
  public void testParseNumberType2() throws Exception {
    assertTypeEquals(NUMBER_OBJECT_TYPE, parse("@type {!Number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullType1
  public void testParseNullType1() throws Exception {
    assertTypeEquals(NULL_TYPE, parse("@type {null}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullType2
  public void testParseNullType2() throws Exception {
    assertTypeEquals(NULL_TYPE, parse("@type {Null}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseAllType1
  public void testParseAllType1() throws Exception {
    testParseType("*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseAllType2
  public void testParseAllType2() throws Exception {
    testParseType("*?", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseObjectType
  public void testParseObjectType() throws Exception {
    assertTypeEquals(OBJECT_TYPE, parse("@type {!Object}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDateType
  public void testParseDateType() throws Exception {
    assertTypeEquals(DATE_TYPE, parse("@type {!Date}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionType
  public void testParseFunctionType() throws Exception {
    assertTypeEquals(
        createNullableType(U2U_CONSTRUCTOR_TYPE),
        parse("@type {Function}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRegExpType
  public void testParseRegExpType() throws Exception {
    assertTypeEquals(REGEXP_TYPE, parse("@type {!RegExp}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseErrorTypes
  public void testParseErrorTypes() throws Exception {
    assertTypeEquals(ERROR_TYPE, parse("@type {!Error}*/").getType());
    assertTypeEquals(URI_ERROR_TYPE, parse("@type {!URIError}*/").getType());
    assertTypeEquals(EVAL_ERROR_TYPE, parse("@type {!EvalError}*/").getType());
    assertTypeEquals(REFERENCE_ERROR_TYPE,
        parse("@type {!ReferenceError}*/").getType());
    assertTypeEquals(TYPE_ERROR_TYPE, parse("@type {!TypeError}*/").getType());
    assertTypeEquals(
        RANGE_ERROR_TYPE, parse("@type {!RangeError}*/").getType());
    assertTypeEquals(
        SYNTAX_ERROR_TYPE, parse("@type {!SyntaxError}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType1
  public void testParseUndefinedType1() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {undefined}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType2
  public void testParseUndefinedType2() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {Undefined}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType3
  public void testParseUndefinedType3() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {void}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType1
  public void testParseParametrizedType1() throws Exception {
    JSDocInfo info = parse("@type !Array.<number> */");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType2
  public void testParseParametrizedType2() throws Exception {
    JSDocInfo info = parse("@type {!Array.<number>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType3
  public void testParseParametrizedType3() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number,null)>*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType4
  public void testParseParametrizedType4() throws Exception {
    JSDocInfo info = parse("@type {!Array.<(number|null)>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType5
  public void testParseParametrizedType5() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Array.<(number|null)>>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType6
  public void testParseParametrizedType6() throws Exception {
    JSDocInfo info = parse("@type {!Array.<!Array.<(number|null)>>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType7
  public void testParseParametrizedType7() throws Exception {
    JSDocInfo info = parse("@type {!Array.<function():Date>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType8
  public void testParseParametrizedType8() throws Exception {
    JSDocInfo info = parse("@type {!Array.<function():!Date>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType9
  public void testParseParametrizedType9() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Date|number>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType10
  public void testParseParametrizedType10() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Date|number|boolean>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamterizedType11
  public void testParseParamterizedType11() throws Exception {
    JSDocInfo info = parse("@type {!Object.<number>}*/");
    assertTypeEquals(OBJECT_TYPE, info.getType());
    assertParameterTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamterizedType12
  public void testParseParamterizedType12() throws Exception {
    JSDocInfo info = parse("@type {!Object.<string,number>}*/");
    assertTypeEquals(OBJECT_TYPE, info.getType());
    assertParameterTypeEquals(NUMBER_TYPE, info.getType());
    assertIndexTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType1
  public void testParseUnionType1() throws Exception {
    JSDocInfo info = parse("@type {(boolean,null)}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType2
  public void testParseUnionType2() throws Exception {
    JSDocInfo info = parse("@type {boolean|null}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType3
  public void testParseUnionType3() throws Exception {
    JSDocInfo info = parse("@type {boolean||null}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType4
  public void testParseUnionType4() throws Exception {
    JSDocInfo info = parse("@type {(Array.<boolean>,null)}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType5
  public void testParseUnionType5() throws Exception {
    JSDocInfo info = parse("@type {(null, Array.<boolean>)}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType6
  public void testParseUnionType6() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>|null}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType7
  public void testParseUnionType7() throws Exception {
    JSDocInfo info = parse("@type {null|Array.<boolean>}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType8
  public void testParseUnionType8() throws Exception {
    JSDocInfo info = parse("@type {null||Array.<boolean>}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType9
  public void testParseUnionType9() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>||null}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType10
  public void testParseUnionType10() throws Exception {
    parse("@type {string|}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType11
  public void testParseUnionType11() throws Exception {
    parse("@type {(string,)}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType12
  public void testParseUnionType12() throws Exception {
    parse("@type {()}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType13
  public void testParseUnionType13() throws Exception {
    testParseType(
        "(function(this:Date),function(this:String):number)",
        "(function (this:Date): ?|function (this:String): number)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType14
  public void testParseUnionType14() throws Exception {
    testParseType(
        "(function(...[function(number):boolean]):number)|" +
        "function(this:String, string):number",
        "(function (...[function (number): boolean]): number|" +
        "function (this:String, string): number)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType15
  public void testParseUnionType15() throws Exception {
    testParseType("*|number", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType16
  public void testParseUnionType16() throws Exception {
    testParseType("number|*", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType17
  public void testParseUnionType17() throws Exception {
    testParseType("string|number|*", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType18
  public void testParseUnionType18() throws Exception {
    testParseType("(string,*,number)", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionTypeError1
  public void testParseUnionTypeError1() throws Exception {
    parse("@type {(string,|number)} */",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType1
  public void testParseUnknownType1() throws Exception {
    testParseType("?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType2
  public void testParseUnknownType2() throws Exception {
    testParseType("(?|number)", "?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnknownType3
  public void testParseUnknownType3() throws Exception {
    testParseType("(number|?)", "?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType1
  public void testParseFunctionalType1() throws Exception {
    testParseType("function (): number");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType2
  public void testParseFunctionalType2() throws Exception {
    testParseType("function (number, string): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType3
  public void testParseFunctionalType3() throws Exception {
    testParseType(
        "function(this:Array)", "function (this:Array): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType4
  public void testParseFunctionalType4() throws Exception {
    testParseType("function (...[number]): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType5
  public void testParseFunctionalType5() throws Exception {
    testParseType("function (number, ...[string]): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType6
  public void testParseFunctionalType6() throws Exception {
    testParseType(
        "function (this:Date, number): (boolean|number|string)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType7
  public void testParseFunctionalType7() throws Exception {
    testParseType("function()", "function (): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType8
  public void testParseFunctionalType8() throws Exception {
    testParseType(
        "function(this:Array,...[boolean])",
        "function (this:Array, ...[boolean]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType9
  public void testParseFunctionalType9() throws Exception {
    testParseType(
        "function(this:Array,!Date,...[boolean?])",
        "function (this:Array, Date, ...[(boolean|null)]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType10
  public void testParseFunctionalType10() throws Exception {
    testParseType(
        "function(...[Object?]):boolean?",
        "function (...[(Object|null)]): (boolean|null)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType11
  public void testParseFunctionalType11() throws Exception {
    testParseType(
        "function(...[[number]]):[number?]",
        "function (...[Array]): Array");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType12
  public void testParseFunctionalType12() throws Exception {
    testParseType(
        "function(...)",
        "function (...[?]): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType13
  public void testParseFunctionalType13() throws Exception {
    testParseType(
        "function(...): void",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType14
  public void testParseFunctionalType14() throws Exception {
    testParseType("function (*, string, number): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType15
  public void testParseFunctionalType15() throws Exception {
    testParseType("function (?, string): boolean");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType16
  public void testParseFunctionalType16() throws Exception {
    testParseType("function (string, ?): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType17
  public void testParseFunctionalType17() throws Exception {
    testParseType("(function (?): ?|number)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType18
  public void testParseFunctionalType18() throws Exception {
    testParseType("function (?): (?|number)", "function (?): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalType19
  public void testParseFunctionalType19() throws Exception {
    testParseType("function (new:Object)", "function (new:Object): ?");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug1419535
  public void testBug1419535() throws Exception {
    parse("@type {function(Object, string, *)?} */");
    parse("@type {function(Object, string, *)|null} */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIssue477
  public void testIssue477() throws Exception {
    parse("@type function */",
        "Bad type annotation. missing opening (");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testMalformedThisAnnotation
  public void testMalformedThisAnnotation() throws Exception {
    parse("@this */",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError1
  public void testParseFunctionalTypeError1() throws Exception {
    parse("@type {function number):string}*/",
        "Bad type annotation. missing opening (");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError2
  public void testParseFunctionalTypeError2() throws Exception {
    parse("@type {function( number}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError3
  public void testParseFunctionalTypeError3() throws Exception {
    parse("@type {function(...[number], string)}*/",
        "Bad type annotation. variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError4
  public void testParseFunctionalTypeError4() throws Exception {
    parse("@type {function(string, ...[number], boolean):string}*/",
        "Bad type annotation. variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError5
  public void testParseFunctionalTypeError5() throws Exception {
    parse("@type {function (thi:Array)}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError6
  public void testParseFunctionalTypeError6() throws Exception {
    resolve(parse("@type {function (this:number)}*/").getType(),
        "this type must be an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError7
  public void testParseFunctionalTypeError7() throws Exception {
    parse("@type {function(...[number)}*/",
        "Bad type annotation. missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError8
  public void testParseFunctionalTypeError8() throws Exception {
    parse("@type {function(...number])}*/",
        "Bad type annotation. missing opening [");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError9
  public void testParseFunctionalTypeError9() throws Exception {
    parse("@type {function (new:Array, this:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError10
  public void testParseFunctionalTypeError10() throws Exception {
    parse("@type {function (this:Array, new:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError11
  public void testParseFunctionalTypeError11() throws Exception {
    parse("@type {function (Array, new:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError12
  public void testParseFunctionalTypeError12() throws Exception {
    resolve(parse("@type {function (new:number)}*/").getType(),
        "constructed type must be an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayType1
  public void testParseArrayType1() throws Exception {
    testParseType("[number]", "Array");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayType2
  public void testParseArrayType2() throws Exception {
    testParseType("[(number,boolean,[Object?])]", "Array");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayType3
  public void testParseArrayType3() throws Exception {
    testParseType("[[number],[string]]?", "(Array|null)");
  }
