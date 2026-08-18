// buggy code
  private void checkPropertyVisibility(NodeTraversal t,
      Node getprop, Node parent) {
    ObjectType objectType =
        ObjectType.cast(dereference(getprop.getFirstChild().getJSType()));
    String propertyName = getprop.getLastChild().getString();

    if (objectType != null) {
      // Is this a normal property access, or are we trying to override
      // an existing property?
      boolean isOverride = t.inGlobalScope() &&
          parent.getType() == Token.ASSIGN &&
          parent.getFirstChild() == getprop;

      // Find the lowest property defined on a class with visibility
      // information.
      if (isOverride) {
        objectType = objectType.getImplicitPrototype();
      }
      JSDocInfo docInfo = null;
      for (; objectType != null;
           objectType = objectType.getImplicitPrototype()) {
        docInfo = objectType.getOwnPropertyJSDocInfo(propertyName);
        if (docInfo != null &&
            docInfo.getVisibility() != Visibility.INHERITED) {
          break;
        }
      }

      if (objectType == null) {
        // We couldn't find a visibility modifier; assume it's public.
        return;
      }

      boolean sameInput =
          t.getInput().getName().equals(docInfo.getSourceName());
      Visibility visibility = docInfo.getVisibility();
      JSType ownerType = normalizeClassType(objectType);
      if (isOverride) {
        // Check an ASSIGN statement that's trying to override a property
        // on a superclass.
        JSDocInfo overridingInfo = parent.getJSDocInfo();
        Visibility overridingVisibility = overridingInfo == null ?
            Visibility.INHERITED : overridingInfo.getVisibility();

        // Check that (a) the property *can* be overridden, and
        // (b) that the visibility of the override is the same as the
        // visibility of the original property.
        if (visibility == Visibility.PRIVATE && !sameInput) {
          compiler.report(
              t.makeError(getprop, PRIVATE_OVERRIDE,
                  objectType.toString()));
        } else if (overridingVisibility != Visibility.INHERITED &&
            overridingVisibility != visibility) {
          compiler.report(
              t.makeError(getprop, VISIBILITY_MISMATCH,
                  visibility.name(), objectType.toString(),
                  overridingVisibility.name()));
        }
      } else {
        if (sameInput) {
          // private access is always allowed in the same file.
          return;
        } else if (visibility == Visibility.PRIVATE &&
            (currentClass == null || ownerType.differsFrom(currentClass))) {
          if (docInfo.isConstructor() &&
              isValidPrivateConstructorAccess(parent)) {
            return;
          }

          // private access is not allowed outside the file from a different
          // enclosing class.
          compiler.report(
              t.makeError(getprop,
                  BAD_PRIVATE_PROPERTY_ACCESS,
                  propertyName,
                  validator.getReadableJSTypeName(
                      getprop.getFirstChild(), true)));
        } else if (visibility == Visibility.PROTECTED) {
          // There are 3 types of legal accesses of a protected property:
          // 1) Accesses in the same file
          // 2) Overriding the property in a subclass
          // 3) Accessing the property from inside a subclass
          // The first two have already been checked for.
          if (currentClass == null || !currentClass.isSubtype(ownerType)) {
            compiler.report(
                t.makeError(getprop,  BAD_PROTECTED_PROPERTY_ACCESS,
                    propertyName,
                    validator.getReadableJSTypeName(
                        getprop.getFirstChild(), true)));
          }
        }
      }
    }
  }

// relevant test
// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison1
  public void testNumericComparison1() throws Exception {
    testTypes(" function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison2
  public void testNumericComparison2() throws Exception {
    testTypes(" function f(a) {return a < 3;}",
        "left side of numeric comparison\n" +
        "found   : Object\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison3
  public void testNumericComparison3() throws Exception {
    testTypes(" function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison4
  public void testNumericComparison4() throws Exception {
    testTypes(" " +
              "function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison5
  public void testNumericComparison5() throws Exception {
    testTypes(" function f(a) {return a < 3;}",
        "left side of numeric comparison\n" +
        "found   : *\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNumericComparison6
  public void testNumericComparison6() throws Exception {
    testTypes(" function foo() { if (3 >= foo()) return; }",
        "right side of numeric comparison\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison1
  public void testStringComparison1() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison2
  public void testStringComparison2() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison3
  public void testStringComparison3() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison4
  public void testStringComparison4() throws Exception {
    testTypes(" " +
                  "function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison5
  public void testStringComparison5() throws Exception {
    testTypes(" " +
                  "function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStringComparison6
  public void testStringComparison6() throws Exception {
    testTypes(" " +
        "function foo() { if ('a' >= foo()) return; }",
        "right side of comparison\n" +
        "found   : undefined\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueOfComparison1
  public void testValueOfComparison1() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.valueOf = function() { return 1; };" +
        " function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueOfComparison2
  public void testValueOfComparison2() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.valueOf = function() { return 1; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testValueOfComparison3
  public void testValueOfComparison3() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.toString = function() { return 'o'; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGenericRelationalExpression
  public void testGenericRelationalExpression() throws Exception {
    testTypes(" " +
                  "function f(a,b) {return a < b;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof1
  public void testInstanceof1() throws Exception {
    testTypes("function foo(){" +
        "if (bar instanceof 3)return;}",
        "instanceof requires an object\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof2
  public void testInstanceof2() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return;}",
        "deterministic instanceof yields false\n" +
        "found   : undefined\n" +
        "required: NoObject");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof3
  public void testInstanceof3() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof4
  public void testInstanceof4() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof5
  public void testInstanceof5() throws Exception {
    
    testTypes(" function foo(){" +
        "if (foo() instanceof Object)return;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceof6
  public void testInstanceof6() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInstanceOfReduction3
  public void testInstanceOfReduction3() throws Exception {
    testTypes(
        "\n" +
        "var f = function(x, y) {\n" +
        "  return x instanceof y;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping1
  public void testScoping1() throws Exception {
    testTypes(
        "function foo(a){" +
        "  function bar(a){" +
        "    if (a instanceof Array)return;" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping2
  public void testScoping2() throws Exception {
    testTypes(
        " var a;" +
        "function Foo() {" +
        "   var a;" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping3
  public void testScoping3() throws Exception {
    testTypes("\n\nvar b;\nvar b;",
        "variable b redefined with type String, original " +
        "definition at [testcode]:3 with type (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping4
  public void testScoping4() throws Exception {
    testTypes("var b; if (true) var b;",
        "variable b redefined with type String, original " +
        "definition at [testcode]:1 with type (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping5
  public void testScoping5() throws Exception {
    
    
    testTypes("if (true) var b; var b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping6
  public void testScoping6() throws Exception {
    
    
    testTypes("if (true) var b; if (true) var b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping7
  public void testScoping7() throws Exception {
    testTypes("function A() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of A\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping8
  public void testScoping8() throws Exception {
    testTypes("function A() {}" +
        "function B() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of B\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping9
  public void testScoping9() throws Exception {
    testTypes("function B() {" +
        "  this.a = null;" +
        "}" +
        "function A() {}",
        "assignment to property a of B\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping10
  public void testScoping10() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = function b(){};");

    
    assertTrue(p.scope.isDeclared("a", false));
    assertFalse(p.scope.isDeclared("b", false));

    
    assertEquals("function (): undefined",
        p.scope.getVar("a").getType().toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testScoping11
  public void testScoping11() throws Exception {
    
    
    testTypes(
        "var a = function b(){ return b };",
        "inconsistent return type\n" +
        "found   : function (): number\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments1
  public void testFunctionArguments1() throws Exception {
    testFunctionType(
        "" +
        "function f(a) {}",
        "function (number): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments2
  public void testFunctionArguments2() throws Exception {
    testFunctionType(
        "" +
        "function f(opt_a) {}",
        "function ((number|undefined)): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments3
  public void testFunctionArguments3() throws Exception {
    testFunctionType(
        "" +
        "function f(a,b) {}",
        "function (?, number): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments4
  public void testFunctionArguments4() throws Exception {
    testFunctionType(
        "" +
        "function f(a,opt_a) {}",
        "function (?, (number|undefined)): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments5
  public void testFunctionArguments5() throws Exception {
    testTypes(
        "function a(opt_a,a) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments6
  public void testFunctionArguments6() throws Exception {
    testTypes(
        "function a(var_args,a) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments7
  public void testFunctionArguments7() throws Exception {
    testTypes(
        "" +
        "function a(a,opt_a,var_args) {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments8
  public void testFunctionArguments8() throws Exception {
    testTypes(
        "function a(a,opt_a,var_args,b) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments9
  public void testFunctionArguments9() throws Exception {
    
    testTypes(
        "function a(a,opt_a,var_args,b,c) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments10
  public void testFunctionArguments10() throws Exception {
    
    testTypes(
        "function a(a,opt_a,b,c) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments11
  public void testFunctionArguments11() throws Exception {
    testTypes(
        "function a(a,opt_a,b,c,var_args,d) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments12
  public void testFunctionArguments12() throws Exception {
    testTypes("function bar(baz){}",
        "parameter foo does not appear in bar's parameter list");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments13
  public void testFunctionArguments13() throws Exception {
    
    testTypes(
        " function u() { return true; }" +
        "" +
        "function f(b) { if (u()) { b = null; } return b; }",
        "inconsistent return type\n" +
        "found   : (boolean|null)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments14
  public void testFunctionArguments14() throws Exception {
    testTypes(
        " function f(x, opt_y, var_args) {}" +
        "f('3'); f('3', 2); f('3', 2, true); f('3', 2, true, false);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments15
  public void testFunctionArguments15() throws Exception {
    testTypes(
        "" +
        "function g(f) { f(1, 2); }",
        "Function f: called with 2 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionArguments16
  public void testFunctionArguments16() throws Exception {
    testTypes(
        "" +
        "function g(var_args) {} g(1, true);",
        "actual parameter 2 of g does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: (number|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrintFunctionName1
  public void testPrintFunctionName1() throws Exception {
    
    testTypes(
        "var goog = {}; goog.run = function(f) {};" +
        "goog.run();",
        "Function goog.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPrintFunctionName2
  public void testPrintFunctionName2() throws Exception {
    testTypes(
        " var Foo = function() {}; " +
        "Foo.prototype.run = function(f) {};" +
        "(new Foo).run();",
        "Function Foo.prototype.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference1
  public void testFunctionInference1() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (?): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference2
  public void testFunctionInference2() throws Exception {
    testFunctionType(
        "function f(a,b) {}",
        "function (?, ?): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference3
  public void testFunctionInference3() throws Exception {
    testFunctionType(
        "function f(var_args) {}",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference4
  public void testFunctionInference4() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference5
  public void testFunctionInference5() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (this:Date, ?): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference6
  public void testFunctionInference6() throws Exception {
    testFunctionType(
        "function f(opt_a) {}",
        "function (this:Date, ?): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference7
  public void testFunctionInference7() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (this:Date, ?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference8
  public void testFunctionInference8() throws Exception {
    testFunctionType(
        "function f() {}",
        "function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference9
  public void testFunctionInference9() throws Exception {
    testFunctionType(
        "var f = function() {};",
        "function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference10
  public void testFunctionInference10() throws Exception {
    testFunctionType(
        "" +
        "var f = function(a,b) {};",
        "function (this:Date, ?, boolean): string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference11
  public void testFunctionInference11() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference12
  public void testFunctionInference12() throws Exception {
    testFunctionType(
        "var goog = {};" +
        "goog.f = function(){};",
        "goog.f",
        "function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference13
  public void testFunctionInference13() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.Foo = function(){};" +
        "function eatFoo(f){};",
        "eatFoo",
        "function (goog.Foo): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference14
  public void testFunctionInference14() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.Foo = function(){};" +
        "function eatFoo(){ return new goog.Foo; };",
        "eatFoo",
        "function (): goog.Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference15
  public void testFunctionInference15() throws Exception {
    testFunctionType(
        " function f() {};" +
        "f.prototype.foo = function(){};",
        "f.prototype.foo",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference16
  public void testFunctionInference16() throws Exception {
    testFunctionType(
        " function f() {};" +
        "f.prototype.foo = function(){};",
        "(new f).foo",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference17
  public void testFunctionInference17() throws Exception {
    testFunctionType(
        " function f() {}" +
        "function abstractMethod() {}" +
        " f.prototype.foo = abstractMethod;",
        "(new f).foo",
        "function (this:f, number): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference18
  public void testFunctionInference18() throws Exception {
    testFunctionType(
        "var goog = {};" +
        " goog.eatWithDate;",
        "goog.eatWithDate",
        "function (this:Date): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference19
  public void testFunctionInference19() throws Exception {
    testFunctionType(
        " var f;",
        "f",
        "function (string): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testFunctionInference20
  public void testFunctionInference20() throws Exception {
    testFunctionType(
        " var f;",
        "f",
        "function (this:Date): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction1
  public void testInnerFunction1() throws Exception {
    testTypes(
        "function f() {" +
        "  var x = 3;\n" +
        " function g() { x = null; }" +
        " return x;" +
        "}",
        "assignment\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction2
  public void testInnerFunction2() throws Exception {
    testTypes(
        "\n" +
        "function f() {" +
        " var x = null;\n" +
        " function g() { x = 3; }" +
        " g();" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : (null|number)\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction3
  public void testInnerFunction3() throws Exception {
    testTypes(
        "var x = null;" +
        "\n" +
        "function f() {" +
        " x = 3;\n" +
        " \n" +
        " function g() { x = true; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction4
  public void testInnerFunction4() throws Exception {
    testTypes(
        "var x = null;" +
        "\n" +
        "function f() {" +
        " x = '3';\n" +
        " \n" +
        " function g() { x = 3; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction5
  public void testInnerFunction5() throws Exception {
    testTypes(
        "\n" +
        "function f() {" +
        " var x = 3;\n" +
        " " +
        " function g() { var x = 3;x = true; return x; }" +
        " return x;" +
        "}",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction6
  public void testInnerFunction6() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " var x = 0 || function() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}", null);
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction7
  public void testInnerFunction7() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " " +
        " var x = 0 || function() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}",
        "Function x: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction8
  public void testInnerFunction8() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "function f() {" +
        " function x() {};\n" +
        " function g() { if (goog.isFunction(x)) { x(1); } }" +
        " g();" +
        "}",
        "Function x: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInnerFunction9
  public void testInnerFunction9() throws Exception {
    testTypes(
        "function f() {" +
        " var x = 3;\n" +
        " function g() { x = null; };\n" +
        " function h() { return x == null; }" +
        " return h();" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling1
  public void testAbstractMethodHandling1() throws Exception {
    testTypes(
        " var abstractFn = function() {};" +
        "abstractFn(1);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling2
  public void testAbstractMethodHandling2() throws Exception {
    testTypes(
        "var abstractFn = function() {};" +
        "abstractFn(1);",
        "Function abstractFn: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling3
  public void testAbstractMethodHandling3() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractFn = function() {};" +
        "goog.abstractFn(1);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling4
  public void testAbstractMethodHandling4() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.abstractFn = function() {};" +
        "goog.abstractFn(1);",
        "Function goog.abstractFn: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling5
  public void testAbstractMethodHandling5() throws Exception {
    testTypes(
        " var abstractFn = function() {};" +
        " var f = abstractFn;" +
        "f('x');",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAbstractMethodHandling6
  public void testAbstractMethodHandling6() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractFn = function() {};" +
        " goog.f = abstractFn;" +
        "goog.f('x');",
        "actual parameter 1 of goog.f does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference1
  public void testMethodInference1() throws Exception {
    testTypes(
        " function F() {}" +
        " F.prototype.foo = function() { return 3; };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference2
  public void testMethodInference2() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.F = function() {};" +
        " goog.F.prototype.foo = " +
        "    function() { return 3; };" +
        " " +
        "goog.G = function() {};" +
        " goog.G.prototype.foo = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference3
  public void testMethodInference3() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(x) { return x; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference4
  public void testMethodInference4() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 3; };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(y) { return y; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference5
  public void testMethodInference5() throws Exception {
    testTypes(
        " function F() {}" +
        " " +
        "F.prototype.foo = function(x) { return 'x'; };" +
        " " +
        "function G() {}" +
        " G.prototype.num = 3;" +
        " " +
        "G.prototype.foo = function(y) { return this.num + y; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference6
  public void testMethodInference6() throws Exception {
    testTypes(
        " function F() {}" +
        " F.prototype.foo = function(x) { };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function() { };" +
        "(new G()).foo(1);",
        "Function G.prototype.foo: called with 1 argument(s). " +
        "Function requires at least 0 argument(s) " +
        "and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference7
  public void testMethodInference7() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " G.prototype.foo = function(x, y) { };" +
        "(new G()).foo();",
        "Function G.prototype.foo: called with 0 argument(s). " +
        "Function requires at least 2 argument(s) " +
        "and no more than 2 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference8
  public void testMethodInference8() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(a, opt_b, var_args) { };" +
        "(new G()).foo();",
        "Function G.prototype.foo: called with 0 argument(s). " +
        "Function requires at least 1 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMethodInference9
  public void testMethodInference9() throws Exception {
    testTypes(
        " function F() {}" +
        "F.prototype.foo = function() { };" +
        " " +
        "function G() {}" +
        " " +
        "G.prototype.foo = function(a, var_args, opt_b) { };",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStaticMethodDeclaration1
  public void testStaticMethodDeclaration1() throws Exception {
    testTypes(
        " function F() { F.foo(true); }" +
        " F.foo = function(x) {};",
        "actual parameter 1 of F.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStaticMethodDeclaration2
  public void testStaticMethodDeclaration2() throws Exception {
    testTypes(
        "var goog = goog || {}; function f() { goog.foo(true); }" +
        " goog.foo = function(x) {};",
        "actual parameter 1 of goog.foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStaticMethodDeclaration3
  public void testStaticMethodDeclaration3() throws Exception {
    testTypes(
        "var goog = goog || {}; function f() { goog.foo(true); }" +
        "goog.foo = function() {};",
        "Function goog.foo: called with 1 argument(s). Function requires " +
        "at least 0 argument(s) and no more than 0 argument(s).");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl1
  public void testDuplicateStaticMethodDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        " goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (number): undefined, " +
        "original definition at [testcode]:1 " +
        "with type function (number): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl2
  public void testDuplicateStaticMethodDecl2() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        " " +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl3
  public void testDuplicateStaticMethodDecl3() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl4
  public void testDuplicateStaticMethodDecl4() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = function(x) {};" +
        "goog.foo = function(x) {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticMethodDecl5
  public void testDuplicateStaticMethodDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        "goog.foo = function(x) {};" +
        " goog.foo = function(x) {};",
        "variable goog.foo redefined with type function (?): undefined, " +
        "original definition at [testcode]:1 with type " +
        "function (?): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl1
  public void testDuplicateStaticPropertyDecl1() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl2
  public void testDuplicateStaticPropertyDecl2() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl3
  public void testDuplicateStaticPropertyDecl3() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl4
  public void testDuplicateStaticPropertyDecl4() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo = 'x';" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl5
  public void testDuplicateStaticPropertyDecl5() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo = 'x';" +
        " function Foo() {}",
        "variable goog.foo redefined with type string, " +
        "original definition at [testcode]:1 with type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl6
  public void testDuplicateStaticPropertyDecl6() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo = 'y';" +
        " goog.foo = 'x';");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl7
  public void testDuplicateStaticPropertyDecl7() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl8
  public void testDuplicateStaticPropertyDecl8() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " function EventCopy() {}" +
        " goog.foo;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateStaticPropertyDecl9
  public void testDuplicateStaticPropertyDecl9() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.foo;" +
        " goog.foo;" +
        " function EventCopy() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDuplicateLocalVarDecl
  public void testDuplicateLocalVarDecl() throws Exception {
    testTypes(
        "\n" +
        "function f(x) {  var x = ''; }",
        "variable x redefined with type string, " +
        "original definition at [testcode]:2 with type number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration1
  public void testStubFunctionDeclaration1() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.prototype.foo;",
        "(new f).foo",
        "function (this:f, number, string): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration2
  public void testStubFunctionDeclaration2() throws Exception {
    testExternFunctionType(
        
        " function f() {};" +
        " f.subclass;",
        "f.subclass",
        "function (new:f.subclass): ?");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration3
  public void testStubFunctionDeclaration3() throws Exception {
    testFunctionType(
        " function f() {};" +
        " f.foo;",
        "f.foo",
        "function (): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration4
  public void testStubFunctionDeclaration4() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        "function (this:f): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration5
  public void testStubFunctionDeclaration5() throws Exception {
    testFunctionType(
        " function f() { " +
        "   this.foo;" +
        "}",
        "(new f).foo",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration6
  public void testStubFunctionDeclaration6() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo;",
        "(new f).foo",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration7
  public void testStubFunctionDeclaration7() throws Exception {
    testFunctionType(
        " function f() {} " +
        " f.prototype.foo = function() {};",
        "(new f).foo",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration8
  public void testStubFunctionDeclaration8() throws Exception {
    testFunctionType(
        " var f = function() {}; ",
        "f",
        createNullableType(U2U_CONSTRUCTOR_TYPE).toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration9
  public void testStubFunctionDeclaration9() throws Exception {
    testFunctionType(
        " var f; ",
        "f",
        "function (): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testStubFunctionDeclaration10
  public void testStubFunctionDeclaration10() throws Exception {
    testFunctionType(
        " var f = function(x) {};",
        "f",
        "function (number): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testNestedFunctionInference1
  public void testNestedFunctionInference1() throws Exception {
    String nestedAssignOfFooAndBar =
        " function f() {};" +
        "f.prototype.foo = f.prototype.bar = function(){};";
    testFunctionType(nestedAssignOfFooAndBar, "(new f).bar",
        "function (this:f): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeRedefinition
  public void testTypeRedefinition() throws Exception {
    testTypes("a={}; a.A = {ZOR:'b'};"
        + " a.A = function() {}",
        "variable a.A redefined with type function (new:a.A): undefined, " +
        "original definition at [testcode]:1 with type enum{a.A}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn1
  public void testIn1() throws Exception {
    testTypes("'foo' in Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn2
  public void testIn2() throws Exception {
    testTypes("3 in Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn3
  public void testIn3() throws Exception {
    testTypes("undefined in Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn4
  public void testIn4() throws Exception {
    testTypes("Date in Object",
        "left side of 'in'\n" +
        "found   : function (new:Date, ?, ?, ?, ?, ?, ?, ?): string\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn5
  public void testIn5() throws Exception {
    testTypes("'x' in null",
        "'in' requires an object\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn6
  public void testIn6() throws Exception {
    testTypes(
        "" +
        "function g(x) {}" +
        "g(1 in {});",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testIn7
  public void testIn7() throws Exception {
    
    testTypes(
        "\n" +
        "function g(x) { return 5; }" +
        "function f() {" +
        "  var x = {};" +
        "  x.foo = '3';" +
        "  return g(x.foo) in {};" +
        "}",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForIn1
  public void testForIn1() throws Exception {
    testTypes(
        " function f(x) {}" +
        "for (var k in {}) {" +
        "  f(k);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForIn2
  public void testForIn2() throws Exception {
    testTypes(
        " function f(x) {}" +
        " var E = {FOO: 'bar'};" +
        " var obj = {};" +
        "var k = null;" +
        "for (k in obj) {" +
        "  f(k);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : E.<string>\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForIn3
  public void testForIn3() throws Exception {
    testTypes(
        " function f(x) {}" +
        " var obj = {};" +
        "for (var k in obj) {" +
        "  f(obj[k]);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForIn4
  public void testForIn4() throws Exception {
    testTypes(
        " function f(x) {}" +
        " var E = {FOO: 'bar'};" +
        " var obj = {};" +
        "for (var k in obj) {" +
        "  f(obj[k]);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : (Array|null)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testForIn5
  public void testForIn5() throws Exception {
    testTypes(
        " function f(x) {}" +
        " var E = function(){};" +
        " var obj = {};" +
        "for (var k in obj) {" +
        "  f(k);" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : string\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison2
  public void testComparison2() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "if (a!==b) {}",
        "condition always evaluates to the same value\n" +
        "left : number\n" +
        "right: Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison3
  public void testComparison3() throws Exception {
    
    testTypes("var a;" +
        "var b = a == null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison4
  public void testComparison4() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "var c = a == b");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison5
  public void testComparison5() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a == b",
        "condition always evaluates to true\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison6
  public void testComparison6() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a != b",
        "condition always evaluates to false\n" +
        "left : null\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison7
  public void testComparison7() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a == b",
        "condition always evaluates to true\n" +
        "left : undefined\n" +
        "right: undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison8
  public void testComparison8() throws Exception {
    testTypes(" var a = [];" +
        "a[0] == null || a[1] == undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison9
  public void testComparison9() throws Exception {
    testTypes(" var a = [];" +
        "a[0] == null",
        "condition always evaluates to true\n" +
        "left : undefined\n" +
        "right: null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison10
  public void testComparison10() throws Exception {
    testTypes(" var a = [];" +
        "a[0] === null");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison11
  public void testComparison11() throws Exception {
    testTypes(
        "(function(){}) == 'x'",
        "condition always evaluates to false\n" +
        "left : function (): undefined\n" +
        "right: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison12
  public void testComparison12() throws Exception {
    testTypes(
        "(function(){}) == 3",
        "condition always evaluates to false\n" +
        "left : function (): undefined\n" +
        "right: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testComparison13
  public void testComparison13() throws Exception {
    testTypes(
        "(function(){}) == false",
        "condition always evaluates to false\n" +
        "left : function (): undefined\n" +
        "right: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeleteOperator1
  public void testDeleteOperator1() throws Exception {
    testTypes(
        "var x = {};" +
        " function f() { return delete x['a']; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDeleteOperator2
  public void testDeleteOperator2() throws Exception {
    testTypes(
        "var obj = {};" +
        " function f(x) { return obj; }" +
        " function g(x) {" +
        "  if (x) { delete f(x)['a']; }" +
        "}",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnumStaticMethod1
  public void testEnumStaticMethod1() throws Exception {
    testTypes(
        " var Foo = {AAA: 1};" +
        " Foo.method = function(x) {};" +
        "Foo.method(true);",
        "actual parameter 1 of Foo.method does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnumStaticMethod2
  public void testEnumStaticMethod2() throws Exception {
    testTypes(
        " var Foo = {AAA: 1};" +
        " Foo.method = function(x) {};" +
        "function f() { Foo.method(true); }",
        "actual parameter 1 of Foo.method does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum1
  public void testEnum1() throws Exception {
    testTypes("var a={BB:1,CC:2};\n" +
        "var d;d=a.BB;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum2
  public void testEnum2() throws Exception {
    testTypes("var a={b:1}",
        "enum key b must be a syntactic constant");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum3
  public void testEnum3() throws Exception {
    testTypes("var a={BB:1,BB:2}",
        "enum element BB already defined", true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum4
  public void testEnum4() throws Exception {
    testTypes("var a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum5
  public void testEnum5() throws Exception {
    testTypes("var a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: (String|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum6
  public void testEnum6() throws Exception {
    testTypes("var a={BB:1,CC:2};\nvar d;d=a.BB;",
        "assignment\n" +
        "found   : a.<number>\n" +
        "required: Array");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum7
  public void testEnum7() throws Exception {
    testTypes("var a={AA:1,BB:2,CC:3};" +
        "var b=a.D;",
        "element D does not exist on this enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum8
  public void testEnum8() throws Exception {
    testTypes("var a=8;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum9
  public void testEnum9() throws Exception {
    testTypes(
        "var goog = {};" +
        "goog.a=8;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum10
  public void testEnum10() throws Exception {
    testTypes(
        "" +
        "goog.K = { A : 3 };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum11
  public void testEnum11() throws Exception {
    testTypes(
        "" +
        "goog.K = { 502 : 3 };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum12
  public void testEnum12() throws Exception {
    testTypes(
        " var a = {};" +
        " var b = a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum13
  public void testEnum13() throws Exception {
    testTypes(
        " var a = {};" +
        " var b = a;",
        "incompatible enum element types\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum14
  public void testEnum14() throws Exception {
    testTypes(
        " var a = {FOO:5};" +
        " var b = a;" +
        "var c = b.FOO;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum15
  public void testEnum15() throws Exception {
    testTypes(
        " var a = {FOO:5};" +
        " var b = a;" +
        "var c = b.BAR;",
        "element BAR does not exist on this enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum16
  public void testEnum16() throws Exception {
    testTypes("var goog = {};" +
        "goog.a={BB:1,BB:2}",
        "enum element BB already defined", true);
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum17
  public void testEnum17() throws Exception {
    testTypes("var goog = {};" +
        "goog.a={BB:'string'}",
        "element type must match enum's type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum18
  public void testEnum18() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        "\n" +
        "var f = function(x) { return x; };");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum19
  public void testEnum19() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        "\n" +
        "var f = function(x) { return x; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: E.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum20
  public void testEnum20() throws Exception {
    testTypes(" var E = {A: 1, B: 2}; var x = []; x[E.A] = 0;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum21
  public void testEnum21() throws Exception {
    Node n = parseAndTypeCheck(
        " var E = {A : 'a', B : 'b'};\n" +
        " function f(x) { return x; }");
    Node nodeX = n.getLastChild().getLastChild().getLastChild().getLastChild();
    JSType typeE = nodeX.getJSType();
    assertFalse(typeE.isObject());
    assertFalse(typeE.isNullable());
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum22
  public void testEnum22() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum23
  public void testEnum23() throws Exception {
    testTypes(" var E = {A: 1, B: 2};" +
        " function f(x) {return x}",
        "inconsistent return type\n" +
        "found   : E.<number>\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum24
  public void testEnum24() throws Exception {
    testTypes(" var E = {A: {}};" +
        " function f(x) {return x}",
        "inconsistent return type\n" +
        "found   : E.<(Object|null)>\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum25
  public void testEnum25() throws Exception {
    testTypes(" var E = {A: {}};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum26
  public void testEnum26() throws Exception {
    testTypes("var a = {};  a.B = {A: 1, B: 2};" +
        " function f(x) {return x}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum27
  public void testEnum27() throws Exception {
    
    testTypes(" var A = {B: 1, C: 2}; " +
        "function f(x) { return A == x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum28
  public void testEnum28() throws Exception {
    
    testTypes(" var A = {B: 1, C: 2}; " +
        "function f(x) { return A.B == x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum29
  public void testEnum29() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A; }",
        "inconsistent return type\n" +
        "found   : enum{A}\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum30
  public void testEnum30() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A.B; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum31
  public void testEnum31() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A; }",
        "inconsistent return type\n" +
        "found   : enum{A}\n" +
        "required: A.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum32
  public void testEnum32() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f() { return A.B; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum34
  public void testEnum34() throws Exception {
    testTypes(" var A = {B: 1, C: 2}; " +
        " function f(x) { return x == A.B; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum35
  public void testEnum35() throws Exception {
    testTypes("var a = a || {};  a.b = {C: 1, D: 2};" +
              " function f() { return a.b.C; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum36
  public void testEnum36() throws Exception {
    testTypes("var a = a || {};  a.b = {C: 1, D: 2};" +
              " function f() { return 1; }",
              "inconsistent return type\n" +
              "found   : number\n" +
              "required: a.b.<number>");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum37
  public void testEnum37() throws Exception {
    testTypes(
        "var goog = goog || {};" +
        " goog.a = {};" +
        " var b = goog.a;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum38
  public void testEnum38() throws Exception {
    testTypes(
        " var MyEnum = {};" +
        " function f(x) {}",
        "Parse error. Cycle detected in inheritance chain " +
        "of type MyEnum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum39
  public void testEnum39() throws Exception {
    testTypes(
        " var MyEnum = {FOO: new Number(1)};" +
        "" +
        "function f(x) { return x == MyEnum.FOO && MyEnum.FOO == x; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum40
  public void testEnum40() throws Exception {
    testTypes(
        " var MyEnum = {FOO: new Number(1)};" +
        "" +
        "function f(x) { return x == MyEnum.FOO && MyEnum.FOO == x; }",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testEnum41
  public void testEnum41() throws Exception {
    testTypes(
        " var MyEnum = { FOO: 1};" +
        "" +
        "function f() { return MyEnum.FOO; }",
        "inconsistent return type\n" +
        "found   : MyEnum.<number>\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum1
  public void testAliasedEnum1() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum2
  public void testAliasedEnum2() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum3
  public void testAliasedEnum3() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(YourEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum4
  public void testAliasedEnum4() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(YourEnum.FOO);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testAliasedEnum5
  public void testAliasedEnum5() throws Exception {
    testTypes(
        " var YourEnum = {FOO: 3};" +
        " var MyEnum = YourEnum;" +
        " function f(x) {} f(MyEnum.FOO);",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : YourEnum.<number>\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse1
  public void testBackwardsEnumUse1() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var MyEnum = {FOO: 'x'};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse2
  public void testBackwardsEnumUse2() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var MyEnum = {FOO: 'x'};",
        "inconsistent return type\n" +
        "found   : MyEnum.<string>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse3
  public void testBackwardsEnumUse3() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse4
  public void testBackwardsEnumUse4() throws Exception {
    testTypes(
        " function f() { return MyEnum.FOO; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;",
        "inconsistent return type\n" +
        "found   : YourEnum.<string>\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsEnumUse5
  public void testBackwardsEnumUse5() throws Exception {
    testTypes(
        " function f() { return MyEnum.BAR; }" +
        " var YourEnum = {FOO: 'x'};" +
        " var MyEnum = YourEnum;",
        "element BAR does not exist on this enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse1
  public void testBackwardsTypedefUse1() throws Exception {
    testTypes(
        " function f() {}" +
        " var MyTypedef;",
        "@this type of a function must be an object\n" +
        "Actual type: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse2
  public void testBackwardsTypedefUse2() throws Exception {
    testTypes(
        " function f() {}" +
        " var MyTypedef;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse3
  public void testBackwardsTypedefUse3() throws Exception {
    testTypes(
        " function f() {}" +
        " var MyTypedef;",
        "@this type of a function must be an object\n" +
        "Actual type: (Date|null|string)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse4
  public void testBackwardsTypedefUse4() throws Exception {
    testTypes(
        " function f() { return null; }" +
        " var MyTypedef;",
        "inconsistent return type\n" +
        "found   : null\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse5
  public void testBackwardsTypedefUse5() throws Exception {
    testTypes(
        " function f() { return null; }" +
        " var MyTypedef = goog.typedef;",
        "inconsistent return type\n" +
        "found   : null\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse6
  public void testBackwardsTypedefUse6() throws Exception {
    testTypes(
        " function f() { return null; }" +
        "var goog = {};" +
        " goog.MyTypedef;",
        "inconsistent return type\n" +
        "found   : null\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse7
  public void testBackwardsTypedefUse7() throws Exception {
    testTypes(
        " function f() { return null; }" +
        "var goog = {};" +
        " goog.MyTypedef;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse8
  public void testBackwardsTypedefUse8() throws Exception {
    
    
    testTypes(
        " function g(x) {}" +
        " function f() { g(this); }" +
        "var goog = {};" +
        " goog.MyTypedef;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsTypedefUse9
  public void testBackwardsTypedefUse9() throws Exception {
    testTypes(
        " function g(x) {}" +
        " function f() { g(this); }" +
        "var goog = {};" +
        " goog.MyTypedef;",
        "actual parameter 1 of g does not match formal parameter\n" +
        "found   : Error\n" +
        "required: Array");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsConstructor1
  public void testBackwardsConstructor1() throws Exception {
    testTypes(
        "function f() { (new Foo(true)); }" +
        "" +
        "var Foo = function(x) {};",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBackwardsConstructor2
  public void testBackwardsConstructor2() throws Exception {
    testTypes(
        "function f() { (new Foo(true)); }" +
        "" +
        "var YourFoo = function(x) {};" +
        "" +
        "var Foo = YourFoo;",
        "actual parameter 1 of Foo does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testMinimalConstructorAnnotation
  public void testMinimalConstructorAnnotation() throws Exception {
    testTypes("function Foo(){}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends1
  public void testGoodExtends1() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends2
  public void testGoodExtends2() throws Exception {
    testTypes("function derived() {}\n" +
        "function base() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends3
  public void testGoodExtends3() throws Exception {
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends4
  public void testGoodExtends4() throws Exception {
    
    
    
    Node n = parseAndTypeCheck(
        "var goog = {};\n" +
        "goog.Base = function(){};\n" +
        "goog.Derived = function(){};\n");
    Node subTypeName = n.getLastChild().getLastChild().getFirstChild();
    assertEquals("goog.Derived", subTypeName.getQualifiedName());

    FunctionType subCtorType =
        (FunctionType) subTypeName.getNext().getJSType();
    assertEquals("goog.Derived", subCtorType.getInstanceType().toString());

    JSType superType = subCtorType.getPrototype().getImplicitPrototype();
    assertEquals("goog.Base", superType.toString());
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends5
  public void testGoodExtends5() throws Exception {
    
    testTypes("function base() {}\n" +
        "function derived() {}\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends6
  public void testGoodExtends6() throws Exception {
    testFunctionType(
        CLOSURE_DEFS +
        "function base() {}\n" +
        " " +
        "  base.prototype.foo = function() { return 1; };\n" +
        "function derived() {}\n" +
        "goog.inherits(derived, base);",
        "derived.superClass_.foo",
        "function (this:base): number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends7
  public void testGoodExtends7() throws Exception {
    testFunctionType(
        "Function.prototype.inherits = function(x) {};" +
        "function base() {}\n" +
        "function derived() {}\n" +
        "derived.inherits(base);",
        "(new derived).constructor",
        "function (new:derived): undefined");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends8
  public void testGoodExtends8() throws Exception {
    testTypes(" function Sub() {}" +
        " function f() { return (new Sub()).foo; }" +
        " function Base() {}" +
        " Base.prototype.foo = true;",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends9
  public void testGoodExtends9() throws Exception {
    testTypes(
        " function Super() {}" +
        "Super.prototype.foo = function() {};" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        " Sub.prototype.foo = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends10
  public void testGoodExtends10() throws Exception {
    testTypes(
        " function Super() {}" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        " function foo() { return new Sub(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends11
  public void testGoodExtends11() throws Exception {
    testTypes(
        " function Super() {}" +
        " Super.prototype.foo = function(x) {};" +
        " function Sub() {}" +
        "Sub.prototype = new Super();" +
        "(new Sub()).foo(0);",
        "actual parameter 1 of Super.prototype.foo " +
        "does not match formal parameter\n" +
        "found   : number\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends12
  public void testGoodExtends12() throws Exception {
    testTypes(
        " function Sub() {}" +
        " function Sub2() {}" +
        " function Super() {}" +
        " function foo(x) {}" +
        "foo(new Sub2());");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodExtends13
  public void testGoodExtends13() throws Exception {
    testTypes(
        " function C() {}" +
        " function E() {}" +
        " function D() {}" +
        " function B() {}" +
        " function A() {}" +
        " function f(x) {} f(new E());",
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : E\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends1
  public void testBadExtends1() throws Exception {
    testTypes("function base() {}\n" +
        "function derived() {}\n",
        "Bad type annotation. Unknown type not_base");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends2
  public void testBadExtends2() throws Exception {
    testTypes("function base() {\n" +
        "\n" +
        "this.baseMember = new Number(4);\n" +
        "}\n" +
        "function derived() {}\n" +
        "\n" +
        "function foo(x){ }\n" +
        "var y;\n" +
        "foo(y.baseMember);\n",
        "actual parameter 1 of foo does not match formal parameter\n" +
        "found   : Number\n" +
        "required: String");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends3
  public void testBadExtends3() throws Exception {
    testTypes("function base() {}",
        "@extends used without @constructor or @interface for base");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadExtends4
  public void testBadExtends4() throws Exception {
    
    
    testTypes(
        " function Sub() {}" +
        " function Sub2() {}" +
        " function foo(x) {}" +
        "foo(new Sub2());",
        "Bad type annotation. Unknown type bad");
  }

// com.google.javascript.jscomp.TypeCheckTest::testLateExtends
  public void testLateExtends() throws Exception {
    testTypes(
        CLOSURE_DEFS +
        " function Foo() {}\n" +
        "Foo.prototype.foo = function() {};\n" +
        "function Bar() {}\n" +
        "goog.inherits(Foo, Bar);\n",
        "Missing @extends tag on type Foo");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMatch
  public void testSuperclassMatch() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMatchWithMixin
  public void testSuperclassMatchWithMixin() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Baz = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.mixin = function(y){};" +
        "Bar.inherits(Foo);\n" +
        "Bar.mixin(Baz);\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMismatch1
  public void testSuperclassMismatch1() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function() {};\n" +
        " var Bar = function() {};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);\n",
        "Missing @extends tag on type Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperclassMismatch2
  public void testSuperclassMismatch2() throws Exception {
    compiler.getOptions().setCodingConvention(new GoogleCodingConvention());
    testTypes(" var Foo = function(){};\n" +
        " var Bar = function(){};\n" +
        "Bar.inherits = function(x){};" +
        "Bar.inherits(Foo);",
        "Missing @extends tag on type Bar");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperClassDefinedAfterSubClass1
  public void testSuperClassDefinedAfterSubClass1() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " function Base() {}" +
        " " +
        "function foo(x) { return x; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSuperClassDefinedAfterSubClass2
  public void testSuperClassDefinedAfterSubClass2() throws Exception {
    testTypes(
        " function A() {}" +
        " function B() {}" +
        " " +
        "function foo(x) { return x; }" +
        " function Base() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment1
  public void testDirectPrototypeAssignment1() throws Exception {
    testTypes(
        " function Base() {}" +
        "Base.prototype.foo = 3;" +
        " function A() {}" +
        "A.prototype = new Base();" +
        " function foo() { return (new A).foo; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment2
  public void testDirectPrototypeAssignment2() throws Exception {
    
    
    testTypes(
        " function Base() {}" +
        " function A() {}" +
        "A.prototype = new Base();" +
        "A.prototype.foo = 3;" +
        " function foo() { return (new Base).foo; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testDirectPrototypeAssignment3
  public void testDirectPrototypeAssignment3() throws Exception {
    
    
    testTypes(
        " var MainWidgetCreator = function() {};" +
        "" +
        "function createMainWidget(ctor) {" +
        "   function tempCtor() {};" +
        "  tempCtor.prototype = ctor.prototype;" +
        "  MainWidgetCreator.superClass_ = ctor.prototype;" +
        "  MainWidgetCreator.prototype = new tempCtor();" +
        "}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements1
  public void testGoodImplements1() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements2
  public void testGoodImplements2() throws Exception {
    testTypes("function Base1() {}\n" +
        "function Base2() {}\n" +
        " function derived() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements3
  public void testGoodImplements3() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGoodImplements4
  public void testGoodImplements4() throws Exception {
    testTypes("var goog = {};" +
        "" +
        "goog.abstractMethod = function() {};" +
        "\n" +
        "goog.Disposable = goog.abstractMethod;" +
        "goog.Disposable.prototype.dispose = goog.abstractMethod;" +
        "" +
        "goog.SubDisposable = function() {};" +
        " " +
        "goog.SubDisposable.prototype.dispose = function() {};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements1
  public void testBadImplements1() throws Exception {
    testTypes("function Base1() {}\n" +
        "function Base2() {}\n" +
        " function derived() {}",
        "Bad type annotation. Unknown type nonExistent");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements2
  public void testBadImplements2() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}",
        "@implements used without @constructor or @interface for f");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements3
  public void testBadImplements3() throws Exception {
    testTypes(
        "var goog = {};" +
        " goog.abstractMethod = function(){};" +
        " var Disposable = goog.abstractMethod;" +
        "Disposable.prototype.method = goog.abstractMethod;" +
        "function f() {}",
        "property method on interface Disposable is not implemented by type f");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplements4
  public void testBadImplements4() throws Exception {
    testTypes("function Disposable() {}\n" +
        "function f() {}",
        "f cannot implement this type; an interface can only extend, " +
        "but not implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceExtends
  public void testInterfaceExtends() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        " function derived() {}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends1
  public void testBadInterfaceExtends1() throws Exception {
    testTypes("function A() {}",
        "Bad type annotation. Unknown type nonExistent");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends2
  public void testBadInterfaceExtends2() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "B cannot extend this type; a constructor can only extend objects " +
        "and an interface can only extend interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends3
  public void testBadInterfaceExtends3() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "B cannot extend this type; a constructor can only extend objects " +
        "and an interface can only extend interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends4
  public void testBadInterfaceExtends4() throws Exception {
    
    
    
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        "B.prototype = A;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadInterfaceExtends5
  public void testBadInterfaceExtends5() throws Exception {
    
    
    
    testTypes("function A() {}\n" +
        "function B() {}\n" +
        "B.prototype = A;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplementsAConstructor
  public void testBadImplementsAConstructor() throws Exception {
    testTypes("function A() {}\n" +
        "function B() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplementsNonInterfaceType
  public void testBadImplementsNonInterfaceType() throws Exception {
    testTypes("function B() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testBadImplementsNonObjectType
  public void testBadImplementsNonObjectType() throws Exception {
    testTypes("function S() {}",
        "can only implement interfaces");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment1
  public void testInterfaceAssignment1() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment2
  public void testInterfaceAssignment2() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;",
        "initializing variable\n" +
        "found   : T\n" +
        "required: I");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment3
  public void testInterfaceAssignment3() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment4
  public void testInterfaceAssignment4() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i = t;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment5
  public void testInterfaceAssignment5() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i1 = t;\n" +
        "var i2 = t;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment6
  public void testInterfaceAssignment6() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var i1 = new T();\n" +
        "var i2 = i1;\n",
        "initializing variable\n" +
        "found   : I1\n" +
        "required: I2");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment7
  public void testInterfaceAssignment7() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "var t = new T();\n" +
        "var i1 = t;\n" +
        "var i2 = t;\n" +
        "i1 = i2;\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment8
  public void testInterfaceAssignment8() throws Exception {
    testTypes("var I = function() {};\n" +
        "var i;\n" +
        "var o = i;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment9
  public void testInterfaceAssignment9() throws Exception {
    testTypes("var I = function() {};\n" +
        "function f() { return null; }\n" +
        "var i = f();\n",
        "initializing variable\n" +
        "found   : (I|null)\n" +
        "required: I");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment10
  public void testInterfaceAssignment10() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "function f() { return new T(); }\n" +
        "var i1 = f();\n",
        "initializing variable\n" +
        "found   : (I1|I2)\n" +
        "required: I1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment11
  public void testInterfaceAssignment11() throws Exception {
    testTypes("var I1 = function() {};\n" +
        "var I2 = function() {};\n" +
        "var T = function() {};\n" +
        "function f() { return new T(); }\n" +
        "var i1 = f();\n",
        "initializing variable\n" +
        "found   : (I1|I2|T)\n" +
        "required: I1");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment12
  public void testInterfaceAssignment12() throws Exception {
    testTypes("var I = function() {};\n" +
              "var T1 = function() {};\n" +
              "var T2 = function() {};\n" +
              "function f() { return new T2(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInterfaceAssignment13
  public void testInterfaceAssignment13() throws Exception {
    testTypes("var I = function() {};\n" +
        "var T = function() {};\n" +
        "function Super() {};\n" +
        "Super.prototype.foo = " +
        "function() { return new T(); };\n" +
        "function Sub() {}\n" +
        "Sub.prototype.foo = " +
        "function() { return new T(); };\n");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetprop1
  public void testGetprop1() throws Exception {
    testTypes("function foo(){foo().bar;}",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetprop2
  public void testGetprop2() throws Exception {
    testTypes("var x = null; x.alert();",
        "null has no properties\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testGetprop3
  public void testGetprop3() throws Exception {
    testTypes(
        " " +
        "function Foo() {  this.x = null; }" +
        "Foo.prototype.initX = function() { this.x = {foo: 1}; };" +
        "Foo.prototype.bar = function() {" +
        "  if (this.x == null) { this.initX(); alert(this.x.foo); }" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess1
  public void testArrayAccess1() throws Exception {
    testTypes("var a = []; var b = a['hi'];");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess2
  public void testArrayAccess2() throws Exception {
    testTypes("var a = []; var b = a[[1,2]];",
        "array access\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess3
  public void testArrayAccess3() throws Exception {
    testTypes("var bar = [];" +
        "function baz(){};" +
        "var foo = bar[baz()];",
        "array access\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess4
  public void testArrayAccess4() throws Exception {
    testTypes("function foo(){};var bar = foo()[foo()];",
        "array access\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess6
  public void testArrayAccess6() throws Exception {
    testTypes("var bar = null[1];",
        "only arrays or objects can be accessed\n" +
        "found   : null\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess7
  public void testArrayAccess7() throws Exception {
    testTypes("var bar = void 0; bar[0];",
        "only arrays or objects can be accessed\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testArrayAccess8
  public void testArrayAccess8() throws Exception {
    
    
    testTypes("var bar = void 0; bar[0]; bar[1];",
        "only arrays or objects can be accessed\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess
  public void testPropAccess() throws Exception {
    testTypes("var f = function(x) {\n" +
        "var o = String(x);\n" +
        "if (typeof o['a'] != 'undefined') { return o['a']; }\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess2
  public void testPropAccess2() throws Exception {
    testTypes("var bar = void 0; bar.baz;",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess3
  public void testPropAccess3() throws Exception {
    
    
    testTypes("var bar = void 0; bar.baz; bar.bax;",
        "undefined has no properties\n" +
        "found   : undefined\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.TypeCheckTest::testPropAccess4
  public void testPropAccess4() throws Exception {
    testTypes(" function f(x) { return x['hi']; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase1
  public void testSwitchCase1() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "switch(a){case b:;}",
        "case expression doesn't match switch\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testSwitchCase2
  public void testSwitchCase2() throws Exception {
    testTypes("var a = null; switch (typeof a) { case 'foo': }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar1
  public void testVar1() throws Exception {
    TypeCheckResult p =
        parseAndTypeCheckWithScope("var a = null");

    assertEquals(createUnionType(STRING_TYPE, NULL_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar2
  public void testVar2() throws Exception {
    testTypes(" var a = function(){}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar3
  public void testVar3() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = 3;");

    assertEquals(NUMBER_TYPE, p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar4
  public void testVar4() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope(
        "var a = 3; a = 'string';");

    assertEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        p.scope.getVar("a").getType());
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar5
  public void testVar5() throws Exception {
    testTypes("var goog = {};" +
        "goog.foo = 'hello';" +
        "var a = goog.foo;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar6
  public void testVar6() throws Exception {
    testTypes(
        "function f() {" +
        "  return function() {" +
        "    " +
        "    var a = 7;" +
        "  };" +
        "}",
        "initializing variable\n" +
        "found   : number\n" +
        "required: Date");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar7
  public void testVar7() throws Exception {
    testTypes("var a, b;",
        "declaration of multiple variables with shared type information");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar8
  public void testVar8() throws Exception {
    testTypes("var a, b;");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar9
  public void testVar9() throws Exception {
    testTypes("var a;",
        "enum initializer must be an object literal or an enum");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar10
  public void testVar10() throws Exception {
    testTypes("var foo = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testVar11
  public void testVar11() throws Exception {
    testTypes("var foo = 'abc';",
        "initializing variable\n" +
        "found   : string\n" +
        "required: Date");
  }
