// buggy code
  private static boolean mayThrowException(Node n) {
    switch (n.getType()) {
      case Token.CALL:
      case Token.GETPROP:
      case Token.GETELEM:
      case Token.THROW:
      case Token.NEW:
      case Token.ASSIGN:
      case Token.INC:
      case Token.DEC:
        return true;
      case Token.FUNCTION:
        return false;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (!ControlFlowGraph.isEnteringNewCfgNode(c) && mayThrowException(c)) {
        return true;
      }
    }
    return false;
  }

    private Set<JSType> getTypesToSkipForTypeNonUnion(JSType type) {
      Set<JSType> types = Sets.newHashSet();
      JSType skipType = type;
      while (skipType != null) {
        types.add(skipType);

        ObjectType objSkipType = skipType.toObjectType();
        if (objSkipType != null) {
          skipType = objSkipType.getImplicitPrototype();
        } else {
          break;
        }
      }
      return types;
    }

// relevant test
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
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0;"
        + " function I() {};\n"
        + "I.prototype.a;\n";
    testSets(false, js, "{}");
    testSets(true, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testMultipleInterfaces
  public void testMultipleInterfaces() {
    String js = ""
        + " function I() {};\n"
        + " function I2() {};\n"
        + "I2.prototype.a;\n"
        + ""
        + "function Foo() {};\n"
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
        + "Foo.prototype.a = 0;\n"
        + "(new Foo).a = 0";
    testSets(false, js, "{a=[[Foo.prototype, I.prototype]]}");
    testSets(true, js, "{a=[[Foo.prototype], [I.prototype]]}");
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

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleAssign
  public void testSimpleAssign() {
    inline("var x; x = 1; print(x)", "var x; print(1)");
    inline("var x; x = 1; x", "var x; 1");
    inline("var x; x = 1; var a = x", "var x; var a = 1");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testSimpleVar
  public void testSimpleVar() {
    inline("var x = 1; print(x)", "var x; print(1)");
    inline("var x = 1; x", "var x; 1");
    inline("var x = 1; var a = x", "var x; var a = 1");    
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
    noInline("var x; x = 1 || x = 2; print(x)");
    noInline("var x; x = 1 && x = 2; print(x)");
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
    
    inline("var x = a + b; print(x);      x = a - b; print(x)",
           "var x;         print(a + b);             print(a - b)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineExpression9
  public void testInlineExpression9() {
    
    inline("var x; if (g) { x= a + b; print(x)    }  x = a - b; print(x)",
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
             "for(var x = 0; x < 10; x++) {j = x.next()};");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testRemoveWithLabels
  public void testRemoveWithLabels() {
    inline("var x = 1; L: x = 2; print(x)", "var x = 1; print(2)");
    inline("var x = 1; L: M: x = 2; print(x)", "var x = 1; print(2)");
    inline("var x = 1; L: M: N: x = 2; print(x)", "var x = 1; print(2)");
  }

// com.google.javascript.jscomp.FlowSensitiveInlineVariablesTest::testInlineArguments
  public void testInlineArguments() {
    testSame("function _func(x) { print(x) }");
    testSame("function _func(x,y) { if(y) { x = 1 }; print(x) }");
    
    test("function(x, y) { x = 1; print(x) }",
         "function(x, y) { print(1) }");

    test("function(x, y) { if (y) { x = 1; print(x) }}",
         "function(x, y) { if (y) { print(1) }}");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testValidBuiltInTypeRedefinition
  public void testValidBuiltInTypeRedefinition() throws Exception {
    testSame(ALL_NATIVE_EXTERN_TYPES, "", null);
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentReturnType
  public void testBuiltInTypeDifferentReturnType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (this:String, *): number\n"
        + "expected: function (this:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams
  public void testBuiltInTypeDifferentNumParams() throws Exception {
    testSame(
        "\n"
        + "function String() {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (this:String): string\n"
        + "expected: function (this:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams2
  public void testBuiltInTypeDifferentNumParams2() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str, opt_nothing) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (this:String, ?, ?): string\n"
        + "expected: function (this:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentParamType
  public void testBuiltInTypeDifferentParamType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (this:String, ?): string\n"
        + "expected: function (this:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBadFunctionTypeDefinition
  public void testBadFunctionTypeDefinition() throws Exception {
    testSame(
        "function Function(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type Function\n"
        + "found   : function (this:Function, ?): ?\n"
        + "expected: function (this:Function, ...[*]): ?");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testExternSubTypes
  public void testExternSubTypes() throws Exception {
    testSame(ALL_NATIVE_EXTERN_TYPES, "", null);

    List<FunctionType> subtypes = ((InstanceObjectType) getLastCompiler()
        .getTypeRegistry().getType("Error")).getConstructor().getSubTypes();
    for (FunctionType type : subtypes) {
      String typeName = type.getInstanceType().toString();
      FunctionType typeInRegistry = ((InstanceObjectType) getLastCompiler()
          .getTypeRegistry().getType(typeName)).getConstructor();
      assertTrue(typeInRegistry == type);
    }
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testNativeCtor
  public void testNativeCtor() {
    testSame(
        " " +
        "function Object(x) {};" +
        "var x = new Object();" +
        " var y = new Object();");
    assertEquals(
        "Object.",
        findGlobalNameType("x").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Object.",
        findGlobalNameType("y").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Object.",
        globalScope.getVar("y").getType().getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testStructuralFunctions
  public void testStructuralFunctions() {
    testSame(
        " " +
        "function Object(x) {};" +
        " " +
        "function fn(x) {};" +
        "var goog = {};" +
        " goog.x = new Object();" +
        " goog.y = fn;");
    assertEquals(
        "(Object|null)",
        globalScope.getVar("goog.x").getType().toString());
    assertEquals(
        "Object.",
        globalScope.getVar("goog.x").getType().restrictByNotNullOrUndefined()
        .getJSDocInfo().getBlockDescription());
    assertEquals(
        "Another function.",
        globalScope.getVar("goog.y").getType()
        .getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testInstanceObject
  public void testInstanceObject() {
    
    testSame(
        " function Foo() {}" +
        "var f = new Foo();" +
        " f.bar = 4;");
    ObjectType type = (ObjectType) globalScope.getVar("f").getType();
    assertEquals("Foo", type.toString());
    assertFalse(type.hasProperty("bar"));
    assertNull(type.getOwnPropertyJSDocInfo("bar"));
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testInterface
  public void testInterface() {
    testSame(
        " function Foo() {}" +
        "var f = new Foo();" +
        " f.bar = 4;");
    ObjectType type = (ObjectType) globalScope.getVar("Foo").getType();
    assertEquals(
        "An interface.",
        type.getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testNamespacedCtor
  public void testNamespacedCtor() {
    testSame(
        "var goog = {};" +
        " goog.Foo = function() {};" +
        "goog.Foo.bar = goog.Foo;" +
        "" +
        "goog.Foo.prototype.baz = goog.Foo;" +
        " var x = new goog.Foo();");
    assertEquals(
        "Hello!",
        findGlobalNameType("x").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Hello!",
        findGlobalNameType("goog.Foo").getJSDocInfo().getBlockDescription());
    assertEquals(
        "Hello!",
        findGlobalNameType(
            "goog.Foo.bar").getJSDocInfo().getBlockDescription());

    assertEquals(
        "Hello!",
        findGlobalNameType(
            "goog.Foo.prototype.baz").getJSDocInfo().getBlockDescription());

    ObjectType proto = (ObjectType) findGlobalNameType("goog.Foo.prototype");
    assertEquals(
        "Bye!",
        proto.getPropertyType("baz").getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testAbstractMethod
  public void testAbstractMethod() {
    testSame(
        " var abstractMethod;" +
        " function Foo() {}" +
        "" +
        "Foo.prototype.bar = abstractMethod;");
    FunctionType abstractMethod =
        (FunctionType) findGlobalNameType("abstractMethod");
    assertNull(abstractMethod.getJSDocInfo());

    FunctionType ctor = (FunctionType) findGlobalNameType("Foo");
    ObjectType proto = ctor.getInstanceType().getImplicitPrototype();
    FunctionType method = (FunctionType) proto.getPropertyType("bar");
    assertEquals(
        "Block description.",
        method.getJSDocInfo().getBlockDescription());
    assertEquals(
        "Block description.",
        proto.getOwnPropertyJSDocInfo("bar").getBlockDescription());
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

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testStraightLine
  public void testStraightLine() {
    assertMatch("D:var x=1; U: x");
    assertMatch("var x; D:x=1; U: x");
    assertNotMatch("D:var x=1; x = 2; U: x");
    assertMatch("var x=1; D:x=2; U: x");
    assertNotMatch("U:x; D:var x = 1");
    assertMatch("D: var x = 1; var y = 2; y; U:x");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testIf
  public void testIf() {
    assertMatch("var x; if(a){ D:x=1 }else { x=2 }; U:x");
    assertMatch("var x; if(a){ x=1 }else { D:x=2 }; U:x");
    assertMatch("D:var x=1; if(a){ U1: x }else { U2: x };");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testLoops
  public void testLoops() {
    assertMatch("var x=0; while(a){ D:x=1 }; U:x");
    assertMatch("var x=0; for(;;) { D:x=1 }; U:x");

    assertMatch("D:var x=1; while(a) { U:x }");
    assertMatch("D:var x=1; for(;;)  { U:x }");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testConditional
  public void testConditional() {
    assertMatch("var x=0; var y; D:(x=1)&&y; U:x");
    assertMatch("var x=0; var y; D:y&&(x=1); U:x");
    assertMatch("var x=0; var y=0; D:(x=1)&&(y=0); U:x");
    assertMatch("var x=0; var y=0; D:(y=0)&&(x=1); U:x");
    assertNotMatch("D: var x=0; var y=0; (x=1)&&(y=0); U:x");
    assertMatch("D: var x=0; var y=0; (y=1)&&((y=2)||(x=1)); U:x");
    assertMatch("D: var x=0; var y=0; (y=0)&&(x=1); U:x");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testUseAndDefInSameInstruction
  public void testUseAndDefInSameInstruction() {
    assertNotMatch("D:var x=0; U:x=1,x");
    assertMatch("D:var x=0; U:x,x=1");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testAssignmentInExpressions
  public void testAssignmentInExpressions() {
    assertMatch("var x=0; D:foo(bar(x=1)); U:x");
    assertMatch("var x=0; D:foo(bar + (x = 1)); U:x");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testHook
  public void testHook() {
    assertMatch("var x=0; D:foo() ? x=1 : bar(); U:x");
    assertMatch("var x=0; D:foo() ? x=1 : x=2; U:x");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testAssignmentOps
  public void testAssignmentOps() {
    assertNotMatch("D: var x = 0; U: x = 100");
    assertMatch("D: var x = 0; U: x += 100");
    assertMatch("D: var x = 0; U: x -= 100");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testInc
  public void testInc() {
    assertMatch("D: var x = 0; U:x++");
    assertMatch("var x = 0; D:x++; U:x");
  }

// com.google.javascript.jscomp.MaybeReachingVariableUseTest::testForIn
  public void testForIn() {
    assertMatch("D: var x = []; U: for (var y in x) { }");
    assertNotMatch("D: var x = [], foo; U: for (x in foo) { }");
    assertNotMatch("D: var x = [], foo; for (x in foo) { U:x }");
    assertMatch("var x = [], foo; D: for (x in foo) { U:x }");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testStraightLine
  public void testStraightLine() {
    assertMatch("D:var x=1; U: x");
    assertMatch("var x; D:x=1; U: x");
    assertNotMatch("D:var x=1; x = 2; U: x");
    assertMatch("var x=1; D:x=2; U: x");
    assertNotMatch("U:x; D:var x = 1");
    assertNotMatch("D:var x; U:x; x=1");
    assertNotMatch("D:var x; U:x; x=1; x");
    assertMatch("D: var x = 1; var y = 2; y; U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testIf
  public void testIf() {
    assertNotMatch("var x; if(a){ D:x=1 } else { x=2 }; U:x");
    assertNotMatch("var x; if(a){ x=1 } else { D:x=2 }; U:x");
    assertMatch("D:var x=1; if(a){ U:x } else { x };");
    assertMatch("D:var x=1; if(a){ x } else { U:x };");
    assertNotMatch("var x; if(a) { D: x = 1 }; U:x;");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testLoops
  public void testLoops() {
    assertNotMatch("var x=0; while(a){ D:x=1 }; U:x");
    assertNotMatch("var x=0; for(;;) { D:x=1 }; U:x");
    assertMatch("D:var x=1; while(a) { U:x }");
    assertMatch("D:var x=1; for(;;)  { U:x }");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testConditional
  public void testConditional() {
    assertMatch("var x=0,y; D:(x=1)&&y; U:x");
    assertNotMatch("var x=0,y; D:y&&(x=1); U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testUseAndDefInSameInstruction
  public void testUseAndDefInSameInstruction() {
    assertMatch("D:var x=0; U:x=1,x");
    assertMatch("D:var x=0; U:x,x=1");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testAssignmentInExpressions
  public void testAssignmentInExpressions() {
    assertMatch("var x=0; D:foo(bar(x=1)); U:x");
    assertMatch("var x=0; D:foo(bar + (x = 1)); U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testHook
  public void testHook() {
    assertNotMatch("var x=0; D:foo() ? x=1 : bar(); U:x");
    assertNotMatch("var x=0; D:foo() ? x=1 : x=2; U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testExpressionVariableReassignment
  public void testExpressionVariableReassignment() {
    assertMatch("var a,b; D: var x = a + b; U:x");
    assertNotMatch("var a,b,c; D: var x = a + b; a = 1; U:x");
    assertNotMatch("var a,b,c; D: var x = a + b; f(b = 1); U:x");
    assertMatch("var a,b,c; D: var x = a + b; c = 1; U:x");

    
    assertNotMatch("var a,b,c; D: var x = a + b; c ? a = 1 : 0; U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testMergeDefinitions
  public void testMergeDefinitions() {
    assertNotMatch("var x,y; D: y = x + x; if(x) { x = 1 }; U:y");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testMergesWithOneDefinition
  public void testMergesWithOneDefinition() {
    assertNotMatch(
        "var x,y; while(y) { if (y) { print(x) } else { D: x = 1 } } U:x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testRedefinitionUsingItself
  public void testRedefinitionUsingItself() {
    assertMatch("var x = 1; D: x = x + 1; U:x;");
    assertNotMatch("var x = 1; D: x = x + 1; x = 1; U:x;");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testMultipleDefinitionsWithDependence
  public void testMultipleDefinitionsWithDependence() {
    assertMatch("var x, a, b; D: x = a, x = b; U: x");
    assertMatch("var x, a, b; D: x = a, x = b; a = 1; U: x");
    assertNotMatch("var x, a, b; D: x = a, x = b; b = 1; U: x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testExterns
  public void testExterns() {
    assertNotMatch("D: goog = {}; U: goog");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testAssignmentOp
  public void testAssignmentOp() {
    assertMatch("var x = 0; D: x += 1; U: x");
    assertMatch("var x = 0; D: x *= 1; U: x");
    assertNotMatch("D: var x = 0; x += 1; U: x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testIncAndDec
  public void testIncAndDec() {
    assertMatch("var x; D: x++; U: x");
    assertMatch("var x; D: x--; U: x");
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testFunctionParams1
  public void testFunctionParams1() {
    computeDefUse("if (param2) { D: param1 = 1; U: param1 }");
    assertSame(def, defUse.getDef("param1", use));  
  }

// com.google.javascript.jscomp.MustBeReachingVariableDefTest::testFunctionParams2
  public void testFunctionParams2() {
    computeDefUse("if (param2) { D: param1 = 1} U: param1");
    assertNotSame(def, defUse.getDef("param1", use));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns1
  public void testAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externSef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns2
  public void testAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externSef2()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns3
  public void testAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externNsef1()", ImmutableList.of("externNsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns4
  public void testAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externNsef2()", ImmutableList.of("externNsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAnnotationInExterns5
  public void testAnnotationInExterns5() throws Exception {
    checkMarkedCalls("externNsef3()", ImmutableList.of("externNsef3"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns1
  public void testNamespaceAnnotationInExterns1() throws Exception {
    checkMarkedCalls("externObj.sef1()", ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns2
  public void testNamespaceAnnotationInExterns2() throws Exception {
    checkMarkedCalls("externObj.nsef1()", ImmutableList.of("externObj.nsef1"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns3
  public void testNamespaceAnnotationInExterns3() throws Exception {
    checkMarkedCalls("externObj.nsef2()", ImmutableList.of("externObj.nsef2"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns4
  public void testNamespaceAnnotationInExterns4() throws Exception {
    checkMarkedCalls("externObj.partialFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns5
  public void testNamespaceAnnotationInExterns5() throws Exception {
    
    
    
    String templateSrc = "var o = {}; o.<fnName> = function(){}; o.<fnName>()";

    
    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "notPartialFn"),
                     ImmutableList.of("o.notPartialFn"));

    checkMarkedCalls(templateSrc.replaceAll("<fnName>", "partialFn"),
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNamespaceAnnotationInExterns6
  public void testNamespaceAnnotationInExterns6() throws Exception {
    checkMarkedCalls("externObj.partialSharedFn()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns1
  public void testConstructorAnnotationInExterns1() throws Exception {
    checkMarkedCalls("new externSefConstructor()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns2
  public void testConstructorAnnotationInExterns2() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.sefFnOfSefObj()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns3
  public void testConstructorAnnotationInExterns3() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.nsefFnOfSefObj()",
                     ImmutableList.of("a.nsefFnOfSefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns4
  public void testConstructorAnnotationInExterns4() throws Exception {
    checkMarkedCalls("var a = new externSefConstructor();" +
                     "a.externShared()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns5
  public void testConstructorAnnotationInExterns5() throws Exception {
    checkMarkedCalls("new externNsefConstructor()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns6
  public void testConstructorAnnotationInExterns6() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.sefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns7
  public void testConstructorAnnotationInExterns7() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.nsefFnOfNsefObj()",
                     ImmutableList.of("externNsefConstructor",
                                      "a.nsefFnOfNsefObj"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorAnnotationInExterns8
  public void testConstructorAnnotationInExterns8() throws Exception {
    checkMarkedCalls("var a = new externNsefConstructor();" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName1
  public void testSharedFunctionName1() throws Exception {
    checkMarkedCalls("var a; " +
                     "if (true) {" +
                     "  a = new externNsefConstructor()" +
                     "} else {" +
                     "  a = new externSefConstructor()" +
                     "}" +
                     "a.externShared()",
                     ImmutableList.of("externNsefConstructor"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testSharedFunctionName2
  public void testSharedFunctionName2() throws Exception {
    
    
    boolean broken = true;
    if (broken) {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2"));
    } else {
      checkMarkedCalls("var a; " +
                       "if (true) {" +
                       "  a = new externNsefConstructor()" +
                       "} else {" +
                       "  a = new externNsefConstructor2()" +
                       "}" +
                       "a.externShared()",
                       ImmutableList.of("externNsefConstructor",
                                        "externNsefConstructor2",
                                        "a.externShared"));
    }
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testNoSideEffectsSimple
  public void testNoSideEffectsSimple() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";
    List<String> expected = ImmutableList.of("f");

    checkMarkedCalls(
        prefix + "" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1" + suffix, expected);
    checkMarkedCalls(
        prefix + "return 1 + 2" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; return a" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = 1; a = 2; return a + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo" + suffix, expected);
    checkMarkedCalls(
        prefix + "var a = {foo : 1}; return a.foo + 1" + suffix, expected);

    
    checkMarkedCalls(
        prefix + "return externObj" + suffix, expected);
    checkMarkedCalls(
        "function g(x) { x.foo = 3; }"  +
        prefix + "return externObj.foo" + suffix, expected);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testExternCalls
  public void testExternCalls() throws Exception {
    String prefix = "function f(){";
    String suffix = "} f()";

    checkMarkedCalls(prefix + "externNsef1()" + suffix,
                     ImmutableList.of("externNsef1", "f"));
    checkMarkedCalls(prefix + "externObj.nsef1()" + suffix,
                     ImmutableList.of("externObj.nsef1", "f"));

    checkMarkedCalls(prefix + "externSef1()" + suffix,
                     ImmutableList.<String>of());
    checkMarkedCalls(prefix + "externObj.sef1()" + suffix,
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testApply
  public void testApply() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.apply()",
                     ImmutableList.of("f.apply"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCall
  public void testCall() throws Exception {
    checkMarkedCalls("function f() {return 42}" +
                     "f.call()",
                     ImmutableList.<String>of("f.call"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference1
  public void testInference1() throws Exception {
    checkMarkedCalls("function f() {return g()}" +
                     "function g() {return 42}" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference2
  public void testInference2() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "function f() {g()}" +
                     "function g() {a=2}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference3
  public void testInference3() throws Exception {
    checkMarkedCalls("var f = function() {return g()};" +
                     "var g = function() {return 42};" +
                     "f()",
                     ImmutableList.of("g", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference4
  public void testInference4() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var f = function() {g()};" +
                     "var g = function() {a=2};" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference5
  public void testInference5() throws Exception {
    checkMarkedCalls("var goog = {};" +
                     "goog.f = function() {return goog.g()};" +
                     "goog.g = function() {return 42};" +
                     "goog.f()",
                     ImmutableList.of("goog.g", "goog.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInference6
  public void testInference6() throws Exception {
    checkMarkedCalls("var a = 1;" +
                     "var goog = {};" +
                     "goog.f = function() {goog.g()};" +
                     "goog.g = function() {a=2};" +
                     "goog.f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators1
  public void testUnaryOperators1() throws Exception {
    checkMarkedCalls("function f() {var x = 1; x++}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators2
  public void testUnaryOperators2() throws Exception {
    checkMarkedCalls("var x = 1;" +
                     "function f() {x++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators3
  public void testUnaryOperators3() throws Exception {
    checkMarkedCalls("function f() {var x = {foo : 0}; x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators4
  public void testUnaryOperators4() throws Exception {
    checkMarkedCalls("var x = {foo : 0};" +
                     "function f() {x.foo++}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testUnaryOperators5
  public void testUnaryOperators5() throws Exception {
    checkMarkedCalls("function f(x) {x.foo++}" +
                     "f({foo : 0})",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator1
  public void testDeleteOperator1() throws Exception {
    checkMarkedCalls("var x = {};" +
                     "function f() {delete x}" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testDeleteOperator2
  public void testDeleteOperator2() throws Exception {
    checkMarkedCalls("function f() {var x = {}; delete x}" +
                     "f()",
                     ImmutableList.of("f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator1
  public void testOrOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator2
  public void testOrOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} || externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperator3
  public void testOrOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testOrOperators4
  public void testOrOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} || function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator1
  public void testAndOperator1() throws Exception {
    checkMarkedCalls("var f = externNsef1 && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator2
  public void testAndOperator2() throws Exception {
    checkMarkedCalls("var f = function(){} && externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperator3
  public void testAndOperator3() throws Exception {
    checkMarkedCalls("var f = externNsef2 && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAndOperators4
  public void testAndOperators4() throws Exception {
    checkMarkedCalls("var f = function(){} && function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator1
  public void testHookOperator1() throws Exception {
    checkMarkedCalls("var f = true ? externNsef1 : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator2
  public void testHookOperator2() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : externNsef2;\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperator3
  public void testHookOperator3() throws Exception {
    checkMarkedCalls("var f = true ? externNsef2 : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testHookOperators4
  public void testHookOperators4() throws Exception {
    checkMarkedCalls("var f = true ? function(){} : function(){};\n" +
                     "f()",
                     ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow1
  public void testThrow1() throws Exception {
    checkMarkedCalls("function f(){throw Error()};\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testThrow2
  public void testThrow2() throws Exception {
    checkMarkedCalls("function A(){throw Error()};\n" +
                     "function f(){return new A()}\n" +
                     "f()",
                     ImmutableList.<String>of("Error"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testAssignmentOverride
  public void testAssignmentOverride() throws Exception {
    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var a = new A;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A", "a.foo"));

    checkMarkedCalls("function A(){}\n" +
                     "A.prototype.foo = function(){};\n" +
                     "var x = 1\n" +
                     "function f(){x = 10}\n" +
                     "var a = new A;\n" +
                     "a.foo = f;\n" +
                     "a.foo();\n",
                     ImmutableList.<String>of("A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance1
  public void testInheritance1() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){var data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source,
                     ImmutableList.of("this.foo", "goog.inherits",
                                      "I", "i.foo", "i.bar",
                                      "A", "a.foo", "a.bar"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInheritance2
  public void testInheritance2() throws Exception {
    String source =
        CompilerTypeTestCase.CLOSURE_DEFS +
        "function I(){}\n" +
        "I.prototype.foo = function(){};\n" +
        "I.prototype.bar = function(){this.foo()};\n" +
        "function A(){};\n" +
        "goog.inherits(A, I)\n;" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "var i = new I();i.foo();i.bar();\n" +
        "var a = new A();a.foo();a.bar();";

    checkMarkedCalls(source, ImmutableList.of("goog.inherits", "I", "A"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallBeforeDefinition
  public void testCallBeforeDefinition() throws Exception {
    checkMarkedCalls("f(); function f(){}",
                     ImmutableList.of("f"));

    checkMarkedCalls("var a = {}; a.f(); a.f = function (){}",
                     ImmutableList.of("a.f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis1
  public void testConstructorThatModifiesThis1() throws Exception {
    String source = "function A(){this.foo = 1}\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis2
  public void testConstructorThatModifiesThis2() throws Exception {
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis3
  public void testConstructorThatModifiesThis3() throws Exception {

    
    String source = "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){this.bar()};\n" +
        "A.prototype.bar = function(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesThis4
  public void testConstructorThatModifiesThis4() throws Exception {

    
    String source = "function A(){foo.call(this)}\n" +
        "function foo(){this.data=24};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.of("A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal1
  public void testConstructorThatModifiesGlobal1() throws Exception {
    String source = "var b = 0;" +
        "function A(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testConstructorThatModifiesGlobal2
  public void testConstructorThatModifiesGlobal2() throws Exception {
    String source = "var b = 0;" +
        "function A(){this.foo()}\n" +
        "A.prototype.foo = function(){b=1};\n" +
        "function f() {return new A}" +
        "f()";

    checkMarkedCalls(source, ImmutableList.<String>of());
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testCallFunctionThatModifiesThis
  public void testCallFunctionThatModifiesThis() throws Exception {
    String source = "function A(){}\n" +
        "A.prototype.foo = function(){this.data=24};\n" +
        "function f(){var a = new A; return a}\n" +
        "function g(){var a = new A; a.foo(); return a}\n" +
        "f(); g()";

    checkMarkedCalls(source, ImmutableList.<String>of("A", "A", "f"));
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation1
  public void testInvalidAnnotation1() throws Exception {
    test(" function foo() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation2
  public void testInvalidAnnotation2() throws Exception {
    test("var f =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation3
  public void testInvalidAnnotation3() throws Exception {
    test(" var f = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation4
  public void testInvalidAnnotation4() throws Exception {
    test("var f = function() {};" +
         " f.x = function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.PureFunctionIdentifierTest::testInvalidAnnotation5
  public void testInvalidAnnotation5() throws Exception {
    test("var f = function() {};" +
         "f.x =  function() {}",
         null, INVALID_NO_SIDE_EFFECT_ANNOTATION);
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties
  public void testAnalyzeUnusedPrototypeProperties() {
    
    test(" \n" +
        "function e(){} \n" +
        "e.prototype.a = function(){};" +
        "e.prototype.b = function(){};" +
        "var x = new e; x.a()",

        "function e(){}" +
        " e.prototype.a = function(){};" +
        "var x = new e; x.a()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties2
  public void testAnalyzeUnusedPrototypeProperties2() {
    
    
    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAnalyzeUnusedPrototypeProperties3
  public void testAnalyzeUnusedPrototypeProperties3() {
    
    
    test(" \n" +
        "function e(){} \n" +
           "e.prototype.a = function(){};" +
           "e.prototype.bExtern = function(){};" +
           "var x = new e;x.a()",
         "function e(){}" +
           "e.prototype.a = function(){};" +
           
           "var x = new e; x.a()");

    
    
    
    
    
    
    
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testAliasing
  public void testAliasing() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testStatement
  public void testStatement() {
    test(" \n" +
        "function e(){}" +
           "var x = e.prototype.method1 = function(){};" +
           "var y = new e; x()",
         "function e(){}" +
           "var x = function(){};" +
           "var y = new e; x()");
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConvention
  public void testExportedMethodsByNamingConvention() {
    
  }

// com.google.javascript.jscomp.RemoveUnusedNamesTest::testExportedMethodsByNamingConventionAlwaysExported
  public void testExportedMethodsByNamingConventionAlwaysExported() {
    
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValue
  public void testValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNullValue
  public void testNullValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, [jscomp.typecheck.nullChecker]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValues
  public void testValues() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "  jscomp.typecheck.checkType(j, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testSkipParamOK
  public void testSkipParamOK() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  jscomp.typecheck.checkType(j, " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUnion
  public void testUnion() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, [" +
        "      jscomp.typecheck.valueChecker('number'), " +
        "      jscomp.typecheck.valueChecker('string')" +
        "]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUntypedParam
  public void testUntypedParam() {
    testChecks(" function f(x) {}", "function f(x) {}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testReturn
  public void testReturn() {
    testChecks(" function f() { return 'x'; }",
        "function f() {" +
        "  return jscomp.typecheck.checkType('x', " +
        "      [jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNativeClass
  public void testNativeClass() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, " +
        "      [jscomp.typecheck.externClassChecker('String')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testQualifiedClass
  public void testQualifiedClass() {
    testChecks("var goog = {}; goog.Foo = function() {};" +
        " function f(x) {}",
        "var goog = {}; goog.Foo = function() {};" +
        "goog.Foo.prototype['instance_of__goog.Foo'] = true;" +
        "function f(x) {" +
        "  jscomp.typecheck.checkType(x, " +
        "    [jscomp.typecheck.classChecker('goog.Foo')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInterface
  public void testInterface() {
    testChecks("function I() {}" +
        "function f(i) {}",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "    [jscomp.typecheck.interfaceChecker('I')])" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterface
  public void testImplementedInterface() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testExtendedInterface
  public void testExtendedInterface() {
    testChecks("function I() {}" +
        "function J() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function J() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype['implements__J'] = true;");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterfaceOrdering
  public void testImplementedInterfaceOrdering() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}" +
        "C.prototype.f = function() {};",
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function C() {}" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterfaceOrderingGoogInherits
  public void testImplementedInterfaceOrderingGoogInherits() {
    testChecks("var goog = {}; goog.inherits = function(x, y) {};" +
        "function I() {}" +
        "function f(i) {}" +
        "function B() {}" +
        "function C() {}" +
        "goog.inherits(C, B);" +
        "C.prototype.f = function() {};",
        "var goog = {}; goog.inherits = function(x, y) {};" +
        "function I() {}" +
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.interfaceChecker('I')])" +
        "}" +
        "function B() {}" +
        "B.prototype['instance_of__B'] = true;" +
        "function C() {}" +
        "goog.inherits(C, B);" +
        "C.prototype['instance_of__C'] = true;" +
        "C.prototype['implements__I'] = true;" +
        "C.prototype.f = function() {};");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInnerConstructor
  public void testInnerConstructor() {
    testChecks("(function() {  function C() {} })()",
        "(function() {" +
        "  function C() {} C.prototype['instance_of__C'] = true;" +
        "})()");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testReturnNothing
  public void testReturnNothing() {
    testChecks("function f() { return; }", "function f() { return; }");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testFunctionType
  public void testFunctionType() {
    testChecks("function f() {}", "function f() {}");
  }

// com.google.javascript.jscomp.TightenTypesTest::testTopLevelVariables
  public void testTopLevelVariables() {
    testSame(" function Foo() {}\n"
             + "var a = new Foo();\n"
             + "var b = a;\n");

    assertTrue(getType("Foo").isFunction());
    assertTrue(getType("a").isInstance());
    assertType("function (this:Foo): ()", getType("Foo"));
    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));

    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = new Foo();\n"
             + "a = new Bar();\n"
             + "var b = a;\n");

    assertTrue(getType("a").isUnion());
    assertType("(Bar,Foo)", getType("a"));
    assertType("Bar", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testNamespacedVariables
  public void testNamespacedVariables() {
    testSame("var goog = goog || {}; goog.foo = {};\n"
             + " goog.foo.Foo = function() {};\n"
             + "goog.foo.Foo.prototype.blah = function() {};\n"
             + " goog.foo.Bar = function() {};\n"
             + "goog.foo.Bar.prototype.blah = function() {};\n"
             + "function bar(a) { a.blah(); }\n"
             + "var baz = bar;\n"
             + "bar(new goog.foo.Foo);\n"
             + "baz(new goog.foo.Bar);\n");

    assertType("(goog.foo.Bar,goog.foo.Foo)", getParamType(getType("bar"), 0));
    assertType("(goog.foo.Bar,goog.foo.Foo)", getParamType(getType("baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testReturnSlot
  public void testReturnSlot() {
    testSame(" function Foo() {}\n"
             + "function bar() {\n"
             + "  var a = new Foo();\n"
             + "  return a;\n"
             + "}\n"
             + "var b = bar();\n");

    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testParameterSlots
  public void testParameterSlots() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "function bar(a, b) {}\n"
             + "bar(new Foo, new Foo);\n"
             + "bar(new Bar, null);\n");

    assertType("(Bar,Foo)", getParamType(getType("bar"), 0));
    assertType("Foo", getParamType(getType("bar"), 1));
    assertNull(getParamVar(getType("bar"), 2));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAliasedFunction
  public void testAliasedFunction() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "function bar(a) {}\n"
             + "var baz = bar;\n"
             + "bar(new Foo);\n"
             + "baz(new Bar);\n");

    assertType("(Bar,Foo)", getParamType(getType("bar"), 0));
    assertType("(Bar,Foo)", getParamType(getType("baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCatchStatement
  public void testCatchStatement() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
             " function Bar() {}\n"
             + "function bar() { try { } catch (e) { return e; } }\n"
             + " function ID10TError() {}\n"
             + "var a = bar(); throw new ID10TError();\n", null, null);

    assertType("(Error,EvalError,ID10TError,RangeError,ReferenceError,"
        + "SyntaxError,TypeError,URIError)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testConstructorParameterSlots
  public void testConstructorParameterSlots() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz(a) {}\n"
             + "new Baz(new Foo);\n"
             + "new Baz(new Bar);\n");

    assertType("(Bar,Foo)", getParamType(getType("Baz"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallSlot
  public void testCallSlot() {
    testSame("function foo() {}\n"
             + "function bar() {}\n"
             + "function baz() {}\n"
             + "var a = foo;\n"
             + "a = bar;\n"
             + "a();\n");

    assertTrue(isCalled(getType("foo")));
    assertTrue(isCalled(getType("bar")));
    assertFalse(isCalled(getType("baz")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testObjectLiteralTraversal
  public void testObjectLiteralTraversal() {
    testSame("var foo = function() {}\n"
             + "function bar() { return { 'a': foo()} };\n"
             + "bar();");
    assertTrue(isCalled(getType("foo")));
   }

// com.google.javascript.jscomp.TightenTypesTest::testThis
  public void testThis() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this; }\n"
             + "var a = new Foo();\n"
             + "var b = a.foo();\n");

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAssign
  public void testAssign() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = new Foo();\n"
             + "var b = a = new Bar();\n");

    assertType("(Bar,Foo)", getType("a"));
    assertType("Bar", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testComma
  public void testComma() {
    testSame(" function Foo() {b=new Foo()}\n"
             + "var b;"
             + " function Bar() {}\n"
             + "var a = (new Foo, new Bar);\n");

    assertType("Bar", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAnd
  public void testAnd() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = (new Foo && new Bar);\n");

    assertType("Bar", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testOr
  public void testOr() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " var f = new Foo();\n"
             + " var b = new Bar();\n"
             + "var a = (f || b);\n");

    assertType("(Bar,Foo)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testHook
  public void testHook() {
    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = (1+1 == 2) ? new Foo : new Bar;\n");

    assertType("(Bar,Foo)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testFunctionLiteral
  public void testFunctionLiteral() {
    testSame(" function Foo() {}\n"
             + "var a = (function() { return new Foo; })();\n");

    assertType("Foo", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testNameLookup
  public void testNameLookup() {
    testSame(" function Foo() {}\n"
             + "var a = new Foo;\n"
             + "var b = (function() { return a; })();\n");

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetProp
  public void testGetProp() {
    testSame(" function Foo() {\n"
             + "  this.foo = new A();\n"
             + "}\n"
             + " function Bar() {\n"
             + "  this.foo = new B();\n"
             + "}\n"
             + " function Baz() {}\n"
             + " function A() {}\n"
             + " function B() {}\n"
             
             + " var foo = new Foo();\n"
             + " var bar = new Bar();\n"
             + " var baz = new Baz();\n" 
             + "var a = foo || bar || baz\n"
             + "var b = a.foo;\n");

    assertType("(A,B)", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetPrototypeProperty
  public void testGetPrototypeProperty() {
    testSame(" function Foo() {};\n"
             + " function Bar() {};\n"
             + "Bar.prototype.a = new Foo();\n"
             + "var a = Bar.prototype.a;\n");

    assertType("Foo", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetElem
  public void testGetElem() {
    testSame(""
             + "function Array(var_args) {}\n",
             " function Foo() {}\n"
             + " function Bar() {}\n"
             + "var a = [];\n"
             + "a[0] = new Foo;\n"
             + "a[1] = new Bar;\n"
             + "var b = a[0];\n"
             + "var c = [new Foo, new Bar];\n", null);

    assertType("Array", getType("a"));
    assertType("(Array,Bar,Foo)", getType("b"));
    assertType("Array", getType("c"));

    testSame(" function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz() {\n"
             + "  this.arr = [];\n"
             + "}\n"
             + "var b = new Baz;\n"
             + "b.arr[0] = new Foo;\n"
             + "b.arr[1] = new Bar;\n"
             + "var c = b.arr;\n");

    assertType("Array", getType("c"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testGetElem3
  public void testGetElem3() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
             " function Foo() {}\n"
             + " function Bar() {}\n"
             + " function Baz() {\n"
             + "  this.arr = [];\n"
             + "}\n"
             + "function foo(anarr) {"
             + "}\n"
             + "var ar = [];\n"
             + "foo(ar);\n", null);

    assertType("Array", getType("ar"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testScopeDiscovery
  public void testScopeDiscovery() {
    testSame("function spam() {}\n"
             + "function foo() {}\n"
             + "function bar() {\n"
             + "  return function() { foo(); };\n"
             + "}"
             + "function baz() {\n"
             + "  return function() { bar()(); };\n"
             + "}"
             + "baz()()();\n");

    assertFalse(isCalled(getType("spam")));
    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSheqDiscovery
  public void testSheqDiscovery() {
    testSame("function spam() {}\n"
             + "\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo1 = function() { f1(); }\n"
             + "Foo.prototype.foo2 = function() { f2(); }\n"
             + "Foo.prototype.foo3 = function() { f3(); }\n"
             + "function baz(a) {\n"
             + "  a === null || a instanceof Foo ?\n"
             + "  Foo.prototype.foo1.call(this) :\n"
             + "  Foo.prototype.foo2.call(this);\n"
             + "}\n"
             + "function f1() {}\n"
             + "function f2() {}\n"
             + "function f3() {}\n"
             + "baz(3);\n");

    assertFalse(isCalled(getType("spam")));
    assertFalse(isCalled(getType("f3")));
    assertTrue(isCalled(getType("f1")));
    assertTrue(isCalled(getType("f2")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSubclass
  public void testSubclass() {
    testSame("\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this.bar; };\n"
             + "Foo.prototype.bar = function() { return new A(); };\n"
             + "\n"
             + "function Bar() {}\n"
             + "\n"
             + "Bar.prototype.bar = function() { return new B(); };\n"
             + " function A() {}\n"
             + " function B() {}\n"
             + "var a = (new Foo()).foo()();\n"
             + "a = (new Bar()).foo()();\n");

    ConcreteType fooType =
        getPropertyType(getFunctionPrototype(getType("Foo")), "foo");
    assertType("(Bar,Foo)", getThisType(fooType));
    assertType("(A,B)", getType("a"));

    testSame("\n"
             + "function Foo() {}\n"
             + "Foo.prototype.foo = function() { return this.bar; };\n"
             + "Foo.prototype.bar = function() { return new A(); };\n"
             + "\n"
             + "function Bar() {}\n"
             + "\n"
             + "Bar.prototype.bar = function() { return new B(); };\n"
             + " function A() {}\n"
             + " function B() {}\n"
             + "var a = (new Bar()).foo()();\n");

    fooType = getPropertyType(getFunctionPrototype(getType("Foo")), "foo");
    assertType("Bar", getThisType(fooType));
    assertType("B", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testArrayAssignments
  public void testArrayAssignments() {
    testSame(" function Foo() {}\n"
             + "var a = [];\n"
             + "function foo() { return []; }\n"
             + "(a.length == 0 ? a : foo())[0] = new Foo;\n"
             + "var b = a[0];\n"
             + "var c = foo()[0];\n");

    assertType("(Array,Foo)", getType("b"));
    assertType("(Array,Foo)", getType("c"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testAllPropertyReference
  public void testAllPropertyReference() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.prop = function() { this.prop2(); }\n"
             + "Foo.prototype.prop2 = function() { b = new Foo; }\n"
             + "var a = new Foo;\n"
             + "a = [][0];\n"
             + "function fun(a) {\n"
             + "  return a.prop();\n"
             + "}\n"
             + "var b;\n"
             + "fun(a);\n"
             );

    assertType("Foo", getType("a"));
    assertType("Foo", getType("b"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallFunction
  public void testCallFunction() {
    testSame(" function Foo() { this.a = new A; }\n"
             + " function Bar() {\n"
             + "  Foo.call(this);\n"
             + "}\n"
             + " function A() {};\n"
             + "new Bar;");

    assertTrue(isCalled(getType("Foo")));
    assertTrue(isCalled(getType("A")));
    ConcreteType fooType = getThisType(getType("Foo"));
    assertType("A", getPropertyType(fooType, "a"));

    ConcreteType barType = getThisType(getType("Bar"));
    assertType("A", getPropertyType(barType, "a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallFunctionWithArgs
  public void testCallFunctionWithArgs() {
    testSame(" function Foo(o) { this.a = o; }\n"
             + " function Bar() {\n"
             + "  Foo.call(this, new A());\n"
             + "}\n"
             + " function A() {};\n"
             + "var b = new Bar;");

    assertTrue(isCalled(getType("Foo")));
    assertTrue(isCalled(getType("A")));

    ConcreteType barType = getThisType(getType("Bar"));
    assertType("A", getPropertyType(barType, "a"));

    ConcreteType fooType = getThisType(getType("Foo"));
    assertType("A", getPropertyType(fooType, "a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallPrototypeFunction
  public void testCallPrototypeFunction() {
    testSame(" function Foo() {}\n"
             + "Foo.prototype.a = function() { return new A; }\n"
             + "Foo.prototype.a = function() { return new A; };\n"
             + " function Bar() {}\n"
             + ""
             + "Bar.prototype.a = function() { return new B; };\n"
             + " function A() {};\n"
             + " function B() {};\n"
             + "var ret = Foo.prototype.a.call(new Bar);");

    assertType("A", getType("ret"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testCallPrototypeFunctionWithArgs
  public void testCallPrototypeFunctionWithArgs() {
    testSame(" function Foo() { this.p = null }\n"
             + "Foo.prototype.set = function(arg) { this.p = arg; };\n"
             + "Foo.prototype.get = function() { return this.p; };\n"
             + " function A() {};\n"
             + "Foo.prototype.set.call(new Foo, new A);\n"
             + "var ret = Foo.prototype.get.call(new Foo);");

    ConcreteType fooP = getFunctionPrototype(getType("Foo"));
    ConcreteFunctionType gFun = getPropertyType(fooP, "get").toFunction();
    ConcreteFunctionType sFun = getPropertyType(fooP, "set").toFunction();

    assertTrue(isCalled(sFun));
    assertTrue(isCalled(gFun));
    assertTrue(isCalled(getType("A")));
    assertType("A", getType("ret"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testSetTimeout
  public void testSetTimeout() {
    testSame(" function Window() {};\n"
             + "Window.prototype.setTimeout = function(f, t) {};\n"
             + " var window;",
             " function A() {}\n"
             + "A.prototype.handle = function() { foo(); };\n"
             + "function foo() {}\n"
             + "window.setTimeout((new A).handle, 3);", null);

    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternType
  public void testExternType() {
    testSame(" function T() {};\n"
             + " function Ext() {};\n"
             + "\n"
             + "Ext.prototype.getT = function() {};\n"
             + " Ext.prototype.prop;\n"
             + " var ext;",
             "var b = ext.getT();\n"
             + "var p = ext.prop;", null);

    assertType("Ext", getType("ext"));
    assertType("T", getType("b"));
    assertType("T", getType("p"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternSubTypes
  public void testExternSubTypes() {
    testSame(" function A() {};\n"
             + " function B() {};\n"
             + " function C() {};\n"
             + " function D() {};\n"
             + " function Ext() {};\n"
             + " Ext.prototype.a;\n"
             + " Ext.prototype.b;\n"
             + " Ext.prototype.d;\n"
             + " Ext.prototype.getA = function() {};\n"
             + " Ext.prototype.getB = function() {};\n",
             "var a = (new Ext).a;\n"
             + "var a2 = (new Ext).getA();\n"
             + "var b = (new Ext).b;\n"
             + "var b2 = (new Ext).getB();\n"
             + "var d = (new Ext).d;\n", null);

    assertType("(A,B,C,D)", getType("a"));
    assertType("(A,B,C,D)", getType("a2"));
    assertType("(B,D)", getType("b"));
    assertType("(B,D)", getType("b2"));
    assertType("D", getType("d"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testExternSubTypesForObject
  public void testExternSubTypesForObject() {
    testSame(BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES
             + " function A() {};\n"
             + " function B() {};\n"
             + " Object.prototype.eval = function(code) {};\n"
             + "\n"
             + "A.prototype.a;\n"
             + "\n"
             + "A.prototype.b = function(){};\n",
             "var a = (new A).b()", null, null);
    assertType("(A,Array,B,Boolean,Date,Error,EvalError,Function,Number,Object,"
               + "RangeError,ReferenceError,RegExp,String,SyntaxError,"
               + "TypeError,URIError)", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitPropCall
  public void testImplicitPropCall() {
    testSame(" function Window() {};\n"
             + "\n"
             + "Window.prototype.setTimeout = function(f, d) {};",
             "function foo() {};\n"
             + "(new Window).setTimeout(foo, 20);", null);

    assertTrue(isCalled(getType("foo")));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitPropCallWithArgs
  public void testImplicitPropCallWithArgs() {
    testSame(" function Window() {};\n"
             + " function EventListener() {};\n"
             + "\n"
             + "Window.prototype.addEventListener = function(t, f) {};\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Window).addEventListener('click', foo);", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
  }

// com.google.javascript.jscomp.TightenTypesTest::testUntypedImplicitCallFromProperty
  public void testUntypedImplicitCallFromProperty() {
    testSame(" function Element() {};\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};"
             + " Event.prototype.erv;",
             " function foo(evt) { return bar(evt); };\n"
             + "function bar(a) { return a.type() }\n"
             + " var ar = new Element;\n"
             + "ar.onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertTrue(isCalled(getType("bar")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Event", getParamType(getType("bar"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromProperty
  public void testImplicitCallFromProperty() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Element).onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromPropertyOfUnion
  public void testImplicitCallFromPropertyOfUnion() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "(new Element).onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testImplicitCallFromPropertyOfAllType
  public void testImplicitCallFromPropertyOfAllType() {
    testSame(" function Element() {};\n"
             + "\n"
             + "Element.prototype.onclick;\n"
             + " function Event() {};",
             "function foo(evt) {};\n"
             + "var elems = [];\n"
             + "var elem = elems[0];\n" 
             + "elem.onclick = foo;", null);

    assertTrue(isCalled(getType("foo")));
    assertType("Event", getParamType(getType("foo"), 0));
    assertType("Element", getThisType(getType("foo").toFunction()));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToCast
  public void testRestrictToCast() {
    testSame(" function Foo() {};\n"
             + "var a = [];\n"
             + "var foo = ( a[0]);\n"
             + "var u = a[0];\n"
             + "new Foo");

    assertType("Foo", getType("foo"));
    assertType("(Array,Foo)", getType("u"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToInterfaceCast
  public void testRestrictToInterfaceCast() {
    testSame(" function Foo() {};\n"
             + " function Int() {};\n"
             + "var a = [];\n"
             + "var foo = ( a[0]);\n"
             + "new Foo");

    assertType("Foo", getType("foo"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testRestrictToCastWithNonInstantiatedTypes
  public void testRestrictToCastWithNonInstantiatedTypes() {
    testSame(
             " function Super() {}\n"
             + " function Foo() {};\n"
             + "Foo.prototype.blah = function() { foofunc() };\n"
             + " function Bar() {};\n"
             + "Bar.prototype.blah = function() { barfunc() };\n"
             + "function barfunc() {}\n"
             + "function foofunc() {}\n"
             + "var a = [];\n"
             + "var u =  (a[0]);\n"
             + "u.blah()\n"
             + "new Foo");

    assertTrue(isCalled(getType("foofunc")));
    assertFalse(isCalled(getType("barfunc")));
    assertType("Array", getType("a"));
  }

// com.google.javascript.jscomp.TightenTypesTest::testFunctionToString
  public void testFunctionToString() {
    testSame(" function Foo() {}\n"
             + "\n"
             + "function Bar() { Foo.call(this); }\n"
             + "var a = function(a) { return new Foo; };\n;"
             + "a(new Foo);\n"
             + "a(new Bar);\n"
             + "new Bar;");

    assertType("function ((Bar,Foo)): Foo", getType("a"));
    assertType("function (this:(Bar,Foo)): ()", getType("Foo"));
    assertType("function (this:Bar): ()", getType("Bar"));
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionAritySimple
  public void testFunctionAritySimple() {
    assertOk("", "");
    assertOk("a", "'a'");
    assertOk("a,b", "10, 20");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionArityWithOptionalArgs
  public void testFunctionArityWithOptionalArgs() {
    assertOk("a,b,opt_c", "1,2");
    assertOk("a,b,opt_c", "1,2,3");
    assertOk("a,opt_b,opt_c", "1");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionArityWithVarArgs
  public void testFunctionArityWithVarArgs() {
    assertOk("var_args", "");
    assertOk("var_args", "1,2");
    assertOk("a,b,var_args", "1,2");
    assertOk("a,b,var_args", "1,2,3");
    assertOk("a,b,var_args", "1,2,3,4,5");
    assertOk("a,opt_b,var_args", "1");
    assertOk("a,opt_b,var_args", "1,2");
    assertOk("a,opt_b,var_args", "1,2,3");
    assertOk("a,opt_b,var_args", "1,2,3,4,5");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testWrongNumberOfArgs
  public void testWrongNumberOfArgs() {
    assertWarning("a,b,opt_c", "1",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,opt_c", "1,2,3,4",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,c,d", "1, 2, 3",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,var_args", "1",
        WRONG_ARGUMENT_COUNT);
    assertWarning("a,b,opt_c,var_args", "1",
        WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testVarArgsLast
  public void testVarArgsLast() {
    assertWarning("a,b,var_args,c", "1,2,3,4",
        VAR_ARGS_MUST_BE_LAST);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testOptArgsLast
  public void testOptArgsLast() {
    assertWarning("a,b,opt_d,c", "1, 2, 3",
        OPTIONAL_ARG_AT_END);
    assertWarning("a,b,opt_d,c", "1, 2",
        OPTIONAL_ARG_AT_END);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc1
  public void testFunctionsWithJsDoc1() {
    testSame(" function foo(a,b,c) {} foo(1,2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc2
  public void testFunctionsWithJsDoc2() {
    testSame(" function foo(a,b,c) {} foo(1,2,3);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc3
  public void testFunctionsWithJsDoc3() {
    testSame(" " +
             "function foo(a,b,c) {} foo(1);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc4
  public void testFunctionsWithJsDoc4() {
    testSame(" var foo = function(a) {}; foo();");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc5
  public void testFunctionsWithJsDoc5() {
    testSame(" var foo = function(a) {}; foo(1,2);");
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc6
  public void testFunctionsWithJsDoc6() {
    testSame(" var foo = function(a, b) {}; foo();",
             WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionsWithJsDoc7
  public void testFunctionsWithJsDoc7() {
    String fooDfn = " var foo = function(b) {};";
    testSame(fooDfn + "foo();");
    testSame(fooDfn + "foo(1);");
    testSame(fooDfn + "foo(1, 2);", WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.TypeCheckFunctionCheckTest::testFunctionWithDefaultCodingConvention
  public void testFunctionWithDefaultCodingConvention() {
    convention = new DefaultCodingConvention();
    testSame("var foo = function(x) {}; foo(1, 2);");
    testSame("var foo = function(opt_x) {}; foo(1, 2);");
    testSame("var foo = function(var_args) {}; foo(1, 2);");
  }

// com.google.javascript.jscomp.TypeCheckTest::testInitialTypingScope
  public void testInitialTypingScope() {
    Scope s = new TypedScopeCreator(compiler,
        new DefaultCodingConvention()).createInitialScope(null);

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

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck1
  public void testTypeCheck1() throws Exception {
    testTypes("function foo(){ if (foo()) return; }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck2
  public void testTypeCheck2() throws Exception {
    testTypes("function foo(){ foo()--; }",
        "increment/decrement\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck4
  public void testTypeCheck4() throws Exception {
    testTypes("function foo(){ !foo(); }");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck5
  public void testTypeCheck5() throws Exception {
    testTypes("function foo(){ var a = +foo(); }",
        "sign operator\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck6
  public void testTypeCheck6() throws Exception {
    testTypes(
        "function foo(){" +
        "var a;if (a == foo())return;}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck7
  public void testTypeCheck7() throws Exception {
    testTypes("function foo() {delete 'abc';}",
        TypeCheck.BAD_DELETE);
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck8
  public void testTypeCheck8() throws Exception {
    testTypes("function foo(){do {} while (foo());}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck9
  public void testTypeCheck9() throws Exception {
    testTypes("function foo(){while (foo());}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck10
  public void testTypeCheck10() throws Exception {
    testTypes("function foo(){for (;foo(););}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck11
  public void testTypeCheck11() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a = b;",
        "assignment\n" +
        "found   : String\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck12
  public void testTypeCheck12() throws Exception {
    testTypes("function foo(){var a = 3^foo();}",
        "bad right operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck13
  public void testTypeCheck13() throws Exception {
    testTypes("var i; i=/xx/;",
        "assignment\n" +
        "found   : RegExp\n" +
        "required: (Number|String)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck14
  public void testTypeCheck14() throws Exception {
    testTypes("function foo(opt_a){}");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck15
  public void testTypeCheck15() throws Exception {
    testTypes("var x;x=null;x=10;",
        "assignment\n" +
        "found   : number\n" +
        "required: (Number|null)");
  }

// com.google.javascript.jscomp.TypeCheckTest::testTypeCheck16
  public void testTypeCheck16() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null)");
  }
