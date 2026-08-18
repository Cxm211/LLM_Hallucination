// buggy code
  private Node tryFoldArrayAccess(Node n, Node left, Node right) {
    Node parent = n.getParent();
    // If GETPROP/GETELEM is used as assignment target the array literal is
    // acting as a temporary we can't fold it here:
    //    "[][0] += 1"
    if (isAssignmentTarget(n)) {
      return n;
    }

    if (!right.isNumber()) {
      // Sometimes people like to use complex expressions to index into
      // arrays, or strings to index into array methods.
      return n;
    }

    double index = right.getDouble();
    int intIndex = (int) index;
    if (intIndex != index) {
      error(INVALID_GETELEM_INDEX_ERROR, right);
      return n;
    }

    if (intIndex < 0) {
      error(INDEX_OUT_OF_BOUNDS_ERROR, right);
      return n;
    }

    Node current = left.getFirstChild();
    Node elem = null;
    for (int i = 0; current != null && i < intIndex; i++) {
        elem = current;

      current = current.getNext();
    }

    if (elem == null) {
      error(INDEX_OUT_OF_BOUNDS_ERROR, right);
      return n;
    }

    if (elem.isEmpty()) {
      elem = NodeUtil.newUndefinedNode(elem);
    } else {
      left.removeChild(elem);
    }

    // Replace the entire GETELEM with the value
    n.getParent().replaceChild(n, elem);
    reportCodeChange();
    return elem;
  }

// relevant test
// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmetic3
  public void testFoldArithmetic3() {
    fold("x = null * undefined", "x = NaN");
    fold("x = null * 1", "x = 0");
    fold("x = (null - 1) * 2", "x = -2");
    fold("x = (null + 1) * 2", "x = 2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmeticInfinity
  public void testFoldArithmeticInfinity() {
    fold("x=-Infinity-2", "x=-Infinity");
    fold("x=Infinity-2", "x=Infinity");
    fold("x=Infinity*5", "x=Infinity");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArithmeticStringComp
  public void testFoldArithmeticStringComp() {
    
    assertResultString("x = 10 - 20", "x=-10");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComparison
  public void testFoldComparison() {
    fold("x = 0 == 0", "x = true");
    fold("x = 1 == 2", "x = false");
    fold("x = 'abc' == 'def'", "x = false");
    fold("x = 'abc' == 'abc'", "x = true");
    fold("x = \"\" == ''", "x = true");
    fold("x = foo() == bar()", "x = foo()==bar()");

    fold("x = 1 != 0", "x = true");
    fold("x = 'abc' != 'def'", "x = true");
    fold("x = 'a' != 'a'", "x = false");

    fold("x = 1 < 20", "x = true");
    fold("x = 3 < 3", "x = false");
    fold("x = 10 > 1.0", "x = true");
    fold("x = 10 > 10.25", "x = false");
    fold("x = y == y", "x = y==y");
    fold("x = y < y", "x = false");
    fold("x = y > y", "x = false");
    fold("x = 1 <= 1", "x = true");
    fold("x = 1 <= 0", "x = false");
    fold("x = 0 >= 0", "x = true");
    fold("x = -1 >= 9", "x = false");

    fold("x = true == true", "x = true");
    fold("x = false == false", "x = true");
    fold("x = false == null", "x = false");
    fold("x = false == true", "x = false");
    fold("x = true == null", "x = false");

    fold("0 == 0", "true");
    fold("1 == 2", "false");
    fold("'abc' == 'def'", "false");
    fold("'abc' == 'abc'", "true");
    fold("\"\" == ''", "true");
    foldSame("foo() == bar()");

    fold("1 != 0", "true");
    fold("'abc' != 'def'", "true");
    fold("'a' != 'a'", "false");

    fold("1 < 20", "true");
    fold("3 < 3", "false");
    fold("10 > 1.0", "true");
    fold("10 > 10.25", "false");
    foldSame("x == x");
    fold("x < x", "false");
    fold("x > x", "false");
    fold("1 <= 1", "true");
    fold("1 <= 0", "false");
    fold("0 >= 0", "true");
    fold("-1 >= 9", "false");

    fold("true == true", "true");
    fold("false == null", "false");
    fold("false == true", "false");
    fold("true == null", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComparison2
  public void testFoldComparison2() {
    fold("x = 0 === 0", "x = true");
    fold("x = 1 === 2", "x = false");
    fold("x = 'abc' === 'def'", "x = false");
    fold("x = 'abc' === 'abc'", "x = true");
    fold("x = \"\" === ''", "x = true");
    fold("x = foo() === bar()", "x = foo()===bar()");

    fold("x = 1 !== 0", "x = true");
    fold("x = 'abc' !== 'def'", "x = true");
    fold("x = 'a' !== 'a'", "x = false");

    fold("x = y === y", "x = y===y");

    fold("x = true === true", "x = true");
    fold("x = false === false", "x = true");
    fold("x = false === null", "x = false");
    fold("x = false === true", "x = false");
    fold("x = true === null", "x = false");

    fold("0 === 0", "true");
    fold("1 === 2", "false");
    fold("'abc' === 'def'", "false");
    fold("'abc' === 'abc'", "true");
    fold("\"\" === ''", "true");
    foldSame("foo() === bar()");

    
    foldSame("1 === '1'");
    foldSame("1 === true");
    foldSame("1 !== '1'");
    foldSame("1 !== true");

    fold("1 !== 0", "true");
    fold("'abc' !== 'def'", "true");
    fold("'a' !== 'a'", "false");

    foldSame("x === x");

    fold("true === true", "true");
    fold("false === null", "false");
    fold("false === true", "false");
    fold("true === null", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComparison3
  public void testFoldComparison3() {
    fold("x = !1 == !0", "x = false");

    fold("x = !0 == !0", "x = true");
    fold("x = !1 == !1", "x = true");
    fold("x = !1 == null", "x = false");
    fold("x = !1 == !0", "x = false");
    fold("x = !0 == null", "x = false");

    fold("!0 == !0", "true");
    fold("!1 == null", "false");
    fold("!1 == !0", "false");
    fold("!0 == null", "false");

    fold("x = !0 === !0", "x = true");
    fold("x = !1 === !1", "x = true");
    fold("x = !1 === null", "x = false");
    fold("x = !1 === !0", "x = false");
    fold("x = !0 === null", "x = false");

    fold("!0 === !0", "true");
    fold("!1 === null", "false");
    fold("!1 === !0", "false");
    fold("!0 === null", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldGetElem
  public void testFoldGetElem() {
    fold("x = [,10][0]", "x = void 0");
    fold("x = [10, 20][0]", "x = 10");
    fold("x = [10, 20][1]", "x = 20");
    fold("x = [10, 20][0.5]", "",
        PeepholeFoldConstants.INVALID_GETELEM_INDEX_ERROR);
    fold("x = [10, 20][-1]",    "",
        PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);
    fold("x = [10, 20][2]",     "",
        PeepholeFoldConstants.INDEX_OUT_OF_BOUNDS_ERROR);

    foldSame("x = [foo(), 0][1]");
    fold("x = [0, foo()][1]", "x = foo()");
    foldSame("x = [0, foo()][0]");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldComplex
  public void testFoldComplex() {
    fold("x = (3 / 1.0) + (1 * 2)", "x = 5");
    fold("x = (1 == 1.0) && foo() && true", "x = foo()&&true");
    fold("x = 'abc' + 5 + 10", "x = \"abc510\"");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLeft
  public void testFoldLeft() {
    foldSame("(+x - 1) + 2"); 
    fold("(+x + 1) + 2", "+x + 3");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldArrayLength
  public void testFoldArrayLength() {
    
    fold("x = [].length", "x = 0");
    fold("x = [1,2,3].length", "x = 3");
    fold("x = [a,b].length", "x = 2");

    
    fold("x = [,,1].length", "x = 3");

    
    fold("x = [foo(), 0].length", "x = [foo(),0].length");
    fold("x = y.length", "x = y.length");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldStringLength
  public void testFoldStringLength() {
    
    fold("x = ''.length", "x = 0");
    fold("x = '123'.length", "x = 3");

    
    fold("x = '123\u01dc'.length", "x = 4");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldTypeof
  public void testFoldTypeof() {
    fold("x = typeof 1", "x = \"number\"");
    fold("x = typeof 'foo'", "x = \"string\"");
    fold("x = typeof true", "x = \"boolean\"");
    fold("x = typeof false", "x = \"boolean\"");
    fold("x = typeof null", "x = \"object\"");
    fold("x = typeof undefined", "x = \"undefined\"");
    fold("x = typeof void 0", "x = \"undefined\"");
    fold("x = typeof []", "x = \"object\"");
    fold("x = typeof [1]", "x = \"object\"");
    fold("x = typeof [1,[]]", "x = \"object\"");
    fold("x = typeof {}", "x = \"object\"");
    fold("x = typeof function() {}", "x = 'function'");

    foldSame("x = typeof[1,[foo()]]");
    foldSame("x = typeof{bathwater:baby()}");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldInstanceOf
  public void testFoldInstanceOf() {
    
    fold("64 instanceof Object", "false");
    fold("64 instanceof Number", "false");
    fold("'' instanceof Object", "false");
    fold("'' instanceof String", "false");
    fold("true instanceof Object", "false");
    fold("true instanceof Boolean", "false");
    fold("!0 instanceof Object", "false");
    fold("!0 instanceof Boolean", "false");
    fold("false instanceof Object", "false");
    fold("null instanceof Object", "false");
    fold("undefined instanceof Object", "false");
    fold("NaN instanceof Object", "false");
    fold("Infinity instanceof Object", "false");

    
    fold("[] instanceof Object", "true");
    fold("({}) instanceof Object", "true");

    
    foldSame("new Foo() instanceof Object");
    
    foldSame("[] instanceof Foo");
    foldSame("({}) instanceof Foo");

    fold("(function() {}) instanceof Object", "true");

    
    foldSame("x instanceof Foo");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testDivision
  public void testDivision() {
    
    fold("print(1/3)", "print(1/3)");

    
    
    fold("print(1/2)", "print(0.5)");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testAssignOpsLate
  public void testAssignOpsLate() {
    late = true;
    fold("x=x+y", "x+=y");
    foldSame("x=y+x");
    fold("x=x*y", "x*=y");
    fold("x=y*x", "x*=y");
    fold("x.y=x.y+z", "x.y+=z");
    foldSame("next().x = next().x + 1");

    fold("x=x-y", "x-=y");
    foldSame("x=y-x");
    fold("x=x|y", "x|=y");
    fold("x=y|x", "x|=y");
    fold("x=x*y", "x*=y");
    fold("x=y*x", "x*=y");
    fold("x.y=x.y+z", "x.y+=z");
    foldSame("next().x = next().x + 1");
    
    fold("({a:1}).a = ({a:1}).a + 1", "({a:1}).a = 2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testAssignOpsEarly
 public void testAssignOpsEarly() {
    late = false;
    foldSame("x=x+y");
    foldSame("x=y+x");
    foldSame("x=x*y");
    foldSame("x=y*x");
    foldSame("x.y=x.y+z");
    foldSame("next().x = next().x + 1");

    foldSame("x=x-y");
    foldSame("x=y-x");
    foldSame("x=x|y");
    foldSame("x=y|x");
    foldSame("x=x*y");
    foldSame("x=y*x");
    foldSame("x.y=x.y+z");
    foldSame("next().x = next().x + 1");
    
    fold("({a:1}).a = ({a:1}).a + 1", "({a:1}).a = 2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldAdd1
  public void testFoldAdd1() {
    fold("x=false+1","x=1");
    fold("x=true+1","x=2");
    fold("x=1+false","x=1");
    fold("x=1+true","x=2");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLiteralNames
  public void testFoldLiteralNames() {
    foldSame("NaN == NaN");
    foldSame("Infinity == Infinity");
    foldSame("Infinity == NaN");
    fold("undefined == NaN", "false");
    fold("undefined == Infinity", "false");

    foldSame("Infinity >= Infinity");
    foldSame("NaN >= NaN");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLiteralsTypeMismatches
  public void testFoldLiteralsTypeMismatches() {
    fold("true == true", "true");
    fold("true == false", "false");
    fold("true == null", "false");
    fold("false == null", "false");

    
    fold("null <= null", "true"); 
    fold("null >= null", "true");
    fold("null > null", "false");
    fold("null < null", "false");

    fold("false >= null", "true"); 
    fold("false <= null", "true");
    fold("false > null", "false");
    fold("false < null", "false");

    fold("true >= null", "true");  
    fold("true <= null", "false");
    fold("true > null", "true");
    fold("true < null", "false");

    fold("true >= false", "true");  
    fold("true <= false", "false");
    fold("true > false", "true");
    fold("true < false", "false");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLeftChildConcat
  public void testFoldLeftChildConcat() {
    foldSame("x +5 + \"1\"");
    fold("x+\"5\" + \"1\"", "x + \"51\"");
    
    fold("\"a\"+(\"b\"+c)","\"ab\"+c");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLeftChildOp
  public void testFoldLeftChildOp() {
    fold("x * Infinity * 2", "x * Infinity");
    foldSame("x - Infinity - 2"); 
    foldSame("x - 1 + Infinity");
    foldSame("x - 2 + 1");
    foldSame("x - 2 + 3");
    foldSame("1 + x - 2 + 1");
    foldSame("1 + x - 2 + 3");
    foldSame("1 + x - 2 + 3 - 1");
    foldSame("f(x)-0");
    foldSame("x-0-0");
    foldSame("x+2-2+2");
    foldSame("x+2-2+2-2");
    foldSame("x-2+2");
    foldSame("x-2+2-2");
    foldSame("x-2+2-2+2");

    foldSame("1+x-0-NaN");
    foldSame("1+f(x)-0-NaN");
    foldSame("1+x-0+NaN");
    foldSame("1+f(x)-0+NaN");

    foldSame("1+x+NaN"); 
    foldSame("x+2-2");   
    foldSame("x+2");  
    foldSame("x-2");  
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldSimpleArithmeticOp
  public void testFoldSimpleArithmeticOp() {
    foldSame("x*NaN");
    foldSame("NaN/y");
    foldSame("f(x)-0");
    foldSame("f(x)*1");
    foldSame("1*f(x)");
    foldSame("0+a+b");
    foldSame("0-a-b");
    foldSame("a+b-0");
    foldSame("(1+x)*NaN");

    foldSame("(1+f(x))*NaN"); 
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldLiteralsAsNumbers
  public void testFoldLiteralsAsNumbers() {
    fold("x/'12'","x/12");
    fold("x/('12'+'6')", "x/126");
    fold("true*x", "1*x");
    fold("x/false", "x/0");  
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testNotFoldBackToTrueFalse
  public void testNotFoldBackToTrueFalse() {
    late = false;
    fold("!0", "true");
    fold("!1", "false");
    fold("!3", "false");

    late = true;
    foldSame("!0");
    foldSame("!1");
    fold("!3", "false");
    foldSame("false");
    foldSame("true");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldBangConstants
  public void testFoldBangConstants() {
    fold("1 + !0", "2");
    fold("1 + !1", "1");
    fold("'a ' + !1", "'a false'");
    fold("'a ' + !0", "'a true'");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldMixed
  public void testFoldMixed() {
    fold("''+[1]", "'1'");
    foldSame("false+[]"); 
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldVoid
  public void testFoldVoid() {
    foldSame("void 0");
    fold("void 1", "void 0");
    fold("void x", "void 0");
    fold("void x()", "void x()");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testObjectLiteral
  public void testObjectLiteral() {
    test("(!{})", "false");
    test("(!{a:1})", "false");
    testSame("(!{a:foo()})");
    testSame("(!{'a':foo()})");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testArrayLiteral
  public void testArrayLiteral() {
    test("(![])", "false");
    test("(![1])", "false");
    test("(![a])", "false");
    testSame("(![foo()])");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testIssue601
  public void testIssue601() {
    testSame("'\\v' == 'v'");
    testSame("'v' == '\\v'");
    testSame("'\\u000B' == '\\v'");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldObjectLiteralRef1
  public void testFoldObjectLiteralRef1() {
    
    testSame("var x = ({a:foo(),b:bar()}).a");
    testSame("var x = ({a:1,b:bar()}).a");
    testSame("function f() { return {b:foo(), a:2}.a; }");

    
    testSame("({a:x}).a = 1");
    test("({a:x}).a += 1", "({a:x}).a = x + 1");
    testSame("({a:x}).a ++");
    testSame("({a:x}).a --");

    
    testSame("({a:function(){return this}}).a");
    testSame("({get a() {return this}}).a");
    testSame("({set a(b) {return this}}).a");

    
    testSame("({}).a");

    
    testSame("({}).a");
    testSame("({set a(b) {}}).a");
    
    test("({a:1,set a(b) {}}).a", "1");

    
    test("({get a() {}}).a", "(function (){})()");
    
    test("({get a() {},set a(b) {}}).a", "(function (){})()");

    
    test("var x = ({a:function(){return 1}}).a",
         "var x = function(){return 1}");

    test("var x = ({a:1}).a", "var x = 1");
    test("var x = ({a:1, a:2}).a", "var x = 2");
    test("var x = ({a:1, a:foo()}).a", "var x = foo()");
    test("var x = ({a:foo()}).a", "var x = foo()");

    test("function f() { return {a:1, b:2}.a; }",
         "function f() { return 1; }");

    
    test("var x = ({'a':1})['a']", "var x = 1");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testFoldObjectLiteralRef2
  public void testFoldObjectLiteralRef2() {
    late = false;
    test("({a:x}).a += 1", "({a:x}).a = x + 1");
    late = true;
    testSame("({a:x}).a += 1");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testIEString
  public void testIEString() {
    testSame("!+'\\v1'");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testIssue522
  public void testIssue522() {
    testSame("[][1] = 1;");
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testInvertibleOperators
  public void testInvertibleOperators() {
    Map<String, String> inverses = ImmutableMap.<String, String>builder()
        .put("==", "!=")
        .put("===", "!==")
        .put("<=", ">")
        .put("<", ">=")
        .put(">=", "<")
        .put(">", "<=")
        .put("!=", "==")
        .put("!==", "===")
        .build();
    Set<String> comparators = ImmutableSet.of("<=", "<", ">=", ">");
    Set<String> equalitors = ImmutableSet.of("==", "===");
    Set<String> uncomparables = ImmutableSet.of("undefined", "void 0");
    List<String> operators = ImmutableList.copyOf(inverses.values());
    for (int iOperandA = 0; iOperandA < LITERAL_OPERANDS.size(); iOperandA++) {
      for (int iOperandB = 0;
           iOperandB < LITERAL_OPERANDS.size();
           iOperandB++) {
        for (int iOp = 0; iOp < operators.size(); iOp++) {
          String a = LITERAL_OPERANDS.get(iOperandA);
          String b = LITERAL_OPERANDS.get(iOperandB);
          String op = operators.get(iOp);
          String inverse = inverses.get(op);

          
          if (comparators.contains(op) &&
              (uncomparables.contains(a) || uncomparables.contains(b))) {
            assertSameResults(join(a, op, b), "false");
            assertSameResults(join(a, inverse, b), "false");
          } else if (a.equals(b) && equalitors.contains(op)) {
            if (a.equals("NaN") ||
                a.equals("Infinity") ||
                a.equals("-Infinity")) {
              foldSame(join(a, op, b));
              foldSame(join(a, inverse, b));
            } else {
              assertSameResults(join(a, op, b), "true");
              assertSameResults(join(a, inverse, b), "false");
            }
          } else {
            assertNotSameResults(join(a, op, b), join(a, inverse, b));
          }
        }
      }
    }
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testCommutativeOperators
  public void testCommutativeOperators() {
    late = true;
    List<String> operators =
        ImmutableList.of(
            "==",
            "!=",
            "===",
            "!==",
            "*",
            "|",
            "&",
            "^");
    for (int iOperandA = 0; iOperandA < LITERAL_OPERANDS.size(); iOperandA++) {
      for (int iOperandB = iOperandA;
           iOperandB < LITERAL_OPERANDS.size();
           iOperandB++) {
        for (int iOp = 0; iOp < operators.size(); iOp++) {
          String a = LITERAL_OPERANDS.get(iOperandA);
          String b = LITERAL_OPERANDS.get(iOperandB);
          String op = operators.get(iOp);

          
          
          assertSameResultsOrUncollapsed(join(a, op, b), join(b, op, a));
        }
      }
    }
  }

// com.google.javascript.jscomp.PeepholeFoldConstantsTest::testConvertToNumberNegativeInf
  public void testConvertToNumberNegativeInf() {
    foldSame("var x = 3 * (r ? Infinity : -Infinity);");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testTrueFalse
  public void testTrueFalse() {
    late = false;
    foldSame("x = true");
    foldSame("x = false");
    fold("x = !1", "x = false");
    fold("x = !0", "x = true");
    late = true;
    fold("x = true", "x = !0");
    fold("x = false", "x = !1");
    foldSame("x = !1");
    foldSame("x = !0");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldOneChildBlocksIntegration
  public void testFoldOneChildBlocksIntegration() {
     fold("function f(){switch(foo()){default:{break}}}",
          "function f(){foo()}");

     fold("function f(){switch(x){default:{break}}}",
          "function f(){}");

     fold("function f(){switch(x){default:x;case 1:return 2}}",
          "function f(){switch(x){default:case 1:return 2}}");

     
     fold("if(x){if(true){foo();foo()}else{bar();bar()}}",
          "if(x){foo();foo()}");

     fold("if(x){if(false){foo();foo()}else{bar();bar()}}",
          "if(x){bar();bar()}");

     
     fold("if(x()){}", "x()");

     fold("if(x()){} else {x()}", "x()||x()");
     fold("if(x){}", ""); 
     fold("if(a()){A()} else if (b()) {} else {C()}", "a()?A():b()||C()");

     fold("if(a()){} else if (b()) {} else {C()}",
          "a()||b()||C()");
     fold("if(a()){A()} else if (b()) {} else if (c()) {} else{D()}",
          "a()?A():b()||c()||D()");
     fold("if(a()){} else if (b()) {} else if (c()) {} else{D()}",
          "a()||b()||c()||D()");
     fold("if(a()){A()} else if (b()) {} else if (c()) {} else{}",
          "a()?A():b()||c()");

     
     fold("function foo(){if(x()){}}", "function foo(){x()}");

  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldOneChildBlocksStringCompare
  public void testFoldOneChildBlocksStringCompare() {
    
    assertResultString("if(x){if(y){var x;}}else{var z;}",
        "if(x){if(y)var x}else var z");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testNecessaryDanglingElse
  public void testNecessaryDanglingElse() {
    
    
    
    assertResultString(
        "if(x)if(y){y();z()}else;else x()", "if(x){if(y){y();z()}}else x()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldReturnsIntegration
  public void testFoldReturnsIntegration() {
    
    fold("function f(){if(x)return;else return}",
         "function f(){}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBug1059649
  public void testBug1059649() {
    
    fold("if(x){var y=3;}var z=5", "if(x)var y=3;var z=5");

    
    foldSame("if(x){var y=3;}else{var y=4;}var z=5");
    fold("while(x){var y=3;}var z=5", "while(x)var y=3;var z=5");
    fold("for(var i=0;i<10;i++){var y=3;}var z=5",
         "for(var i=0;i<10;i++)var y=3;var z=5");
    fold("for(var i in x){var y=3;}var z=5",
         "for(var i in x)var y=3;var z=5");
    fold("do{var y=3;}while(x);var z=5", "do var y=3;while(x);var z=5");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testHookIfIntegration
  public void testHookIfIntegration() {
    fold("if (false){ x = 1; } else if (cond) { x = 2; } else { x = 3; }",
         "x=cond?2:3");

    fold("x?void 0:y()", "x||y()");
    fold("!x?void 0:y()", "(!x)||y()");
    fold("x?y():void 0", "x&&y()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testRemoveDuplicateStatementsIntegration
  public void testRemoveDuplicateStatementsIntegration() {
    fold("function z() {if (a) { return true }" +
         "else if (b) { return true }" +
         "else { return true }}",
         "function z() {return true;}");

    fold("function z() {if (a()) { return true }" +
         "else if (b()) { return true }" +
         "else { return true }}",
         "function z() {a()||b();return true;}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldLogicalOpIntegration
  public void testFoldLogicalOpIntegration() {
    test("if(x && true) z()", "x&&z()");
    test("if(x && false) z()", "");
    fold("if(x || 3) z()", "z()");
    fold("if(x || false) z()", "x&&z()");
    test("if(x==y && false) z()", "");
    
    fold("if(y() || x || 3) z()", "(y()||1)&&z()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldBitwiseOpStringCompareIntegration
  public void testFoldBitwiseOpStringCompareIntegration() {
    assertResultString("while(-1 | 0){}", "while(1);");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testVarLiftingIntegration
  public void testVarLiftingIntegration() {
    fold("if(true);else var a;", "var a");
    fold("if(false) foo();else var a;", "var a");
    fold("if(true)var a;else;", "var a");
    fold("if(false)var a;else;", "var a");
    fold("if(false)var a,b;", "var b; var a");
    fold("if(false){var a;var a;}", "var a");
    fold("if(false)var a=function(){var b};", "var a");
    fold("if(a)if(false)var a;else var b;", "var a;if(a)var b");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBug1438784
  public void testBug1438784() throws Exception {
    fold("for(var i=0;i<10;i++)if(x)x.y;", "for(var i=0;i<10;i++);");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldUselessWhileIntegration
  public void testFoldUselessWhileIntegration() {
    fold("while(!true) { foo() }", "");
    fold("while(!false) foo() ", "while(1) foo()");
    fold("while(!void 0) foo()", "while(1) foo()");

    
    fold("if(foo())while(false){foo()}else bar()", "foo()||bar()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldUselessForIntegration
  public void testFoldUselessForIntegration() {
    fold("for(;!true;) { foo() }", "");
    fold("for(;void 0;) { foo() }", "");
    fold("for(;undefined;) { foo() }", "");
    fold("for(;1;) foo()", "for(;;) foo()");
    fold("for(;!void 0;) foo()", "for(;;) foo()");

    
    fold("if(foo())for(;false;){foo()}else bar()", "foo()||bar()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldUselessDoIntegration
  public void testFoldUselessDoIntegration() {
    test("do { foo() } while(!true);", "foo()");
    fold("do { foo() } while(void 0);", "foo()");
    fold("do { foo() } while(undefined);", "foo()");
    fold("do { foo() } while(!void 0);", "do { foo() } while(1);");

    
    test("if(foo())do {foo()} while(false) else bar()", "foo()?foo():bar()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testMinimizeWhileConstantConditionIntegration
  public void testMinimizeWhileConstantConditionIntegration() {
    fold("while(!false) foo()", "while(1) foo()");
    fold("while(202) foo()", "while(1) foo()");
    fold("while(Infinity) foo()", "while(1) foo()");
    fold("while('text') foo()", "while(1) foo()");
    fold("while([]) foo()", "while(1) foo()");
    fold("while({}) foo()", "while(1) foo()");
    fold("while(/./) foo()", "while(1) foo()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testMinimizeExpr
  public void testMinimizeExpr() {
    test("!!true", "");

    fold("!!x()", "x()");
    test("!(!x()&&!y())", "x()||y()");
    fold("x()||!!y()", "x()||y()");

    
    fold("!!x()&&y()", "x()&&y()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBug1509085
  public void testBug1509085() {
    PeepholeIntegrationTest oneRepetitiontest = new PeepholeIntegrationTest() {
      @Override
      protected int getNumRepetitions() {
        return 1;
      }
    };

    oneRepetitiontest.test("x ? x() : void 0", "x&&x();");
    oneRepetitiontest.foldSame("y = x ? x() : void 0");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBugIssue3
  public void testBugIssue3() {
    foldSame("function foo() {" +
             "  if(sections.length != 1) children[i] = 0;" +
             "  else var selectedid = children[i]" +
             "}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testBugIssue43
  public void testBugIssue43() {
    foldSame("function foo() {" +
             "  if (a) { var b = 1; } else { a.b = 1; }" +
             "}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldNegativeBug
  public void testFoldNegativeBug() {
    fold("while(-3){};", "while(1);");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testNoNormalizeLabeledExpr
  public void testNoNormalizeLabeledExpr() {
    enableNormalize(true);
    foldSame("var x; foo:{x = 3;}");
    foldSame("var x; foo:x = 3;");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit1
  public void testShortCircuit1() {
    test("1 && a()", "a()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit2
  public void testShortCircuit2() {
    test("1 && a() && 2", "a()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit3
  public void testShortCircuit3() {
    test("a() && 1 && 2", "a()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testShortCircuit4
  public void testShortCircuit4() {
    test("a() && (1 && b())", "a() && b()");
    test("a() && 1 && b()", "a() && b()");
    test("(a() && 1) && b()", "a() && b()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testMinimizeExprCondition
  public void testMinimizeExprCondition() {
    fold("(x || true) && y()", "y()");
    fold("(x || false) && y()", "x&&y()");
    fold("(x && true) && y()", "x && y()");
    fold("(x && false) && y()", "");
    fold("a = x || false ? b : c", "a=x?b:c");
    fold("do {x()} while((x && false) && y())", "x()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testTrueFalseFolding
  public void testTrueFalseFolding() {
    late = true;
    fold("x = true", "x = !0");
    fold("x = false", "x = !1");
    fold("x = !3", "x = !1");
    fold("x = true && !0", "x = !0");
    fold("x = !!!!!!!!!!!!3", "x = !0");
    fold("if(!3){x()}", "");
    fold("if(!!3){x()}", "x()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testCommaSplitingConstantCondition
  public void testCommaSplitingConstantCondition() {
    late = false;
    fold("(b=0,b=1);if(b)x=b;", "b=0;b=1;x=b;");
    fold("(b=0,b=1);if(b)x=b;", "b=0;b=1;x=b;");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testAvoidCommaSplitting
  public void testAvoidCommaSplitting() {
    late = false;
    fold("x(),y(),z()", "x();y();z()");
    late = true;
    foldSame("x(),y(),z()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testObjectLiteral
  public void testObjectLiteral() {
    test("({})", "");
    test("({a:1})", "");
    test("({a:foo()})", "foo()");
    test("({'a':foo()})", "foo()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testArrayLiteral
  public void testArrayLiteral() {
    test("([])", "");
    test("([1])", "");
    test("([a])", "");
    test("([foo()])", "foo()");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldIfs1
  public void testFoldIfs1() {
    fold("function f() {if (x) return 1; else if (y) return 1;}",
         "function f() {if (x||y) return 1;}");
    fold("function f() {if (x) return 1; else {if (y) return 1; else foo();}}",
         "function f() {if (x||y) return 1; foo();}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldIfs2
  public void testFoldIfs2() {
    fold("function f() {if (x) { a(); } else if (y) { a() }}",
         "function f() {x?a():y&&a();}");
  }

// com.google.javascript.jscomp.PeepholeIntegrationTest::testFoldHook2
  public void testFoldHook2() {
    fold("function f(a) {if (!a) return a; else return a;}",
         "function f(a) {return a}");
  }
