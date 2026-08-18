// buggy code
  static String strEscape(String s, char quote,
                          String doublequoteEscape,
                          String singlequoteEscape,
                          String backslashEscape,
                          CharsetEncoder outputCharsetEncoder) {
    StringBuilder sb = new StringBuilder(s.length() + 2);
    sb.append(quote);
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\0': sb.append("\\0"); break;
        case '\n': sb.append("\\n"); break;
        case '\r': sb.append("\\r"); break;
        case '\t': sb.append("\\t"); break;
        case '\\': sb.append(backslashEscape); break;
        case '\"': sb.append(doublequoteEscape); break;
        case '\'': sb.append(singlequoteEscape); break;
        case '>':                       // Break --> into --\> or ]]> into ]]\>
          if (i >= 2 &&
              ((s.charAt(i - 1) == '-' && s.charAt(i - 2) == '-') ||
               (s.charAt(i - 1) == ']' && s.charAt(i - 2) == ']'))) {
            sb.append("\\>");
          } else {
            sb.append(c);
          }
          break;
        case '<':
          // Break </script into <\/script
          final String END_SCRIPT = "/script";

          // Break <!-- into <\!--
          final String START_COMMENT = "!--";

          if (s.regionMatches(true, i + 1, END_SCRIPT, 0,
                              END_SCRIPT.length())) {
            sb.append("<\\");
          } else if (s.regionMatches(false, i + 1, START_COMMENT, 0,
                                     START_COMMENT.length())) {
            sb.append("<\\");
          } else {
            sb.append(c);
          }
          break;
        default:
          // If we're given an outputCharsetEncoder, then check if the
          //  character can be represented in this character set.
          if (outputCharsetEncoder != null) {
            if (outputCharsetEncoder.canEncode(c)) {
              sb.append(c);
            } else {
              // Unicode-escape the character.
              appendHexJavaScriptRepresentation(sb, c);
            }
          } else {
            // No charsetEncoder provided - pass straight latin characters
            // through, and escape the rest.  Doing the explicit character
            // check is measurably faster than using the CharsetEncoder.
            if (c > 0x1f && c <= 0x7f) {
              sb.append(c);
            } else {
              // Other characters can be misinterpreted by some js parsers,
              // or perhaps mangled by proxies along the way,
              // so we play it safe and unicode escape them.
              appendHexJavaScriptRepresentation(sb, c);
            }
          }
      }
    }
    sb.append(quote);
    return sb.toString();
  }

// relevant test
// com.google.javascript.jscomp.RenameVarsTest::testNamingBasedOnOrderOfOccurrence
  public void testNamingBasedOnOrderOfOccurrence() {
    test("var q,p,m,n,l,k; " +
             "(function (r) {}); try { } catch(s) {}; var t = q + q;",
         "var a,b,c,d,e,f; " +
             "(function(g) {}); try { } catch(h) {}; var i = a + a;"
         );
    test("(function(A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z," +
         "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,$){});" +
         "var a4,a3,a2,a1,b4,b3,b2,b1,ab,ac,ad,fg;function foo(){};",
         "(function(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$){});" +
         "var aa,ba,ca,da,ea,fa,ga,ha,ia,ja,ka,la;function ma(){};");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimple
  public void testStableRenameSimple() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c");
    testRenameMap("function Foo(v1, v2) {return v1;} Foo();",
                  "function a(b, c) {return b;} a();", expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c", "L 2", "d");
    testRenameMapUsingOldMap("function Foo(v1, v2, v3) {return v1;} Foo();",
         "function a(b, c, d) {return b;} a();", expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameGlobals
  public void testStableRenameGlobals() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "Bar", "b", "y", "c", "x", "d");
    testRenameMap("var Foo; var Bar, y; function x() { Bar++; }",
                  "var a; var b, c; function d() { b++; }",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "Foo", "a", "Bar", "b", "y", "c", "x", "d", "Baz", "f", "L 0" , "e");
    testRenameMapUsingOldMap(
        "var Foo, Baz; var Bar, y; function x(R) { return R + Bar++; }",
        "var a, f; var b, c; function d(e) { return e + b++; }",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPointlesslyAnonymousFunctions
  public void testStableRenameWithPointlesslyAnonymousFunctions() {
    VariableMap expectedVariableMap = makeVariableMap("L 0", "a", "L 1", "b");
    testRenameMap("(function (v1, v2) {}); (function (v3, v4) {});",
                  "(function (a, b) {}); (function (a, b) {});",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap("L 0", "a", "L 1", "b", "L 2", "c");
    testRenameMapUsingOldMap("(function (v0, v1, v2) {});" +
                             "(function (v3, v4) {});",
                             "(function (a, b, c) {});" +
                             "(function (a, b) {});",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameLocalsClashingWithGlobals
  public void testStableRenameLocalsClashingWithGlobals() {
    test("function a(v1, v2) {return v1;} a();",
         "function a(b, c) {return b;} a();");
    previouslyUsedMap = renameVars.getVariableMap();
    test("function bar(){return;}function a(v1, v2) {return v1;} a();",
         "function d(){return;}function a(b, c) {return b;} a();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameNested
  public void testStableRenameNested() {
    VariableMap expectedVariableMap = makeVariableMap(
        "f1", "a", "L 0", "b", "L 1", "c", "L 2", "d", "L 3", "e");
    testRenameMap("function f1(v1, v2) { (function(v3, v4) {}) }",
                  "function a(b, c) { (function(d, e) {}) }",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap(
        "f1", "a", "L 0", "b", "L 1", "c", "L 2", "d", "L 3", "e", "L 4", "f");
    testRenameMapUsingOldMap(
        "function f1(v1, v2) { (function(v3, v4, v5) {}) }",
        "function a(b, c) { (function(d, e, f) {}) }",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithExterns1
  public void testStableRenameWithExterns1() {
    String externs = "var foo;";
    test(externs, "var bar; foo(bar);", "var a; foo(a);", null, null);
    previouslyUsedMap = renameVars.getVariableMap();
    test(externs, "var bar, baz; foo(bar, baz);",
         "var a, b; foo(a, b);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithExterns2
  public void testStableRenameWithExterns2() {
    String externs = "var a;";
    test(externs, "var b = 5", "var b = 5", null, null);
    previouslyUsedMap = renameVars.getVariableMap();
    test(externs, "var b = 5, catty = 9;", "var b = 5, c=9;", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithNameOverlap
  public void testStableRenameWithNameOverlap() {
    test("var a = 1; var b = 2; b + b;",
         "var a = 1; var b = 2; b + b;");
    previouslyUsedMap = renameVars.getVariableMap();
    test("var a = 1; var c, b = 2; b + b;",
         "var a = 1; var c, b = 2; b + b;");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithAnonymousFunctions
  public void testStableRenameWithAnonymousFunctions() {
    VariableMap expectedVariableMap = makeVariableMap("L 0", "a", "foo", "b");
    testRenameMap("function foo(bar){return bar;}foo(function(h){return h;});",
                  "function b(a){return a}b(function(a){return a;})",
                  expectedVariableMap);

    expectedVariableMap = makeVariableMap("foo", "b", "L 0", "a", "L 1", "c");
    testRenameMapUsingOldMap(
        "function foo(bar) {return bar;}foo(function(g,h) {return g+h;});",
        "function b(a){return a}b(function(a,c){return a+c;})",
        expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleExternsChanges
  public void testStableRenameSimpleExternsChanges() {
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "a", "L 0", "b", "L 1", "c");
    testRenameMap("function Foo(v1, v2) {return v1;} Foo();",
                  "function a(b, c) {return b;} a();", expectedVariableMap);

    expectedVariableMap = makeVariableMap("L 0", "b", "L 1", "c", "L 2", "a");
    String externs = "var Foo;";
    testRenameMapUsingOldMap(externs,
                             "function Foo(v1, v2, v0) {return v1;} Foo();",
                             "function Foo(b, c, a) {return b;} Foo();",
                             expectedVariableMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleLocalNameExterned
  public void testStableRenameSimpleLocalNameExterned() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");

    previouslyUsedMap = renameVars.getVariableMap();

    String externs = "var b;";
    test(externs, "function Foo(v1, v2) {return v1;} Foo(b);",
         "function a(d, c) {return d;} a(b);", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameSimpleGlobalNameExterned
  public void testStableRenameSimpleGlobalNameExterned() {
    test("function Foo(v1, v2) {return v1;} Foo();",
         "function a(b, c) {return b;} a();");

    previouslyUsedMap = renameVars.getVariableMap();

    String externs = "var Foo;";
    test(externs, "function Foo(v1, v2, v0) {return v1;} Foo();",
         "function Foo(b, c, a) {return b;} Foo();", null, null);
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPrefix1AndUnstableLocalNames
  public void testStableRenameWithPrefix1AndUnstableLocalNames() {
    prefix = "PRE_";
    test("function Foo(v1, v2) {return v1} Foo();",
         "function PRE_(a, b) {return a} PRE_();");

    previouslyUsedMap = renameVars.getVariableMap();

    prefix = "PRE_";
    test("function Foo(v0, v1, v2) {return v1} Foo();",
         "function PRE_(a, b, c) {return b} PRE_();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testStableRenameWithPrefix2
  public void testStableRenameWithPrefix2() {
    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function a() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");

    previouslyUsedMap = renameVars.getVariableMap();

    prefix = "a";
    test("function Foo() {return 1;}" +
         "function Baz() {return 1;}" +
         "function Bar() {" +
         "  var a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z," +
         "      A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,aa,ab;" +
         "  Foo();" +
         "} Bar();",

         "function a() {return 1;}" +
         "function ab() {return 1;}" +
         "function aa() {" +
         "  var b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,A," +
         "      B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,$,ba,ca;" +
         "  a();" +
         "} aa();");
  }

// com.google.javascript.jscomp.RenameVarsTest::testContrivedExampleWhereConsistentRenamingIsWorse
  public void testContrivedExampleWhereConsistentRenamingIsWorse() {
    previouslyUsedMap = makeVariableMap(
        "Foo", "LongString", "L 0", "b", "L 1", "c");

    test("function Foo(v1, v2) {return v1;} Foo();",
         "function LongString(b, c) {return b;} LongString();");

    previouslyUsedMap = renameVars.getVariableMap();
    VariableMap expectedVariableMap = makeVariableMap(
        "Foo", "LongString", "L 0", "b", "L 1", "c");
    assertVariableMapsEqual(expectedVariableMap, previouslyUsedMap);
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportSimpleSymbolReservesName
  public void testExportSimpleSymbolReservesName() {
    test("var goog, x; goog.exportSymbol('a', x);",
         "var a, b; a.exportSymbol('a', b);");
    withClosurePass = true;
    test("var goog, x; goog.exportSymbol('a', x);",
         "var b, c; b.exportSymbol('a', c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportComplexSymbolReservesName
  public void testExportComplexSymbolReservesName() {
    test("var goog, x; goog.exportSymbol('a.b', x);",
         "var a, b; a.exportSymbol('a.b', b);");
    withClosurePass = true;
    test("var goog, x; goog.exportSymbol('a.b', x);",
         "var b, c; b.exportSymbol('a.b', c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testExportToNonStringDoesntExplode
  public void testExportToNonStringDoesntExplode() {
    withClosurePass = true;
    test("var goog, a, b; goog.exportSymbol(a, b);",
         "var a, b, c; a.exportSymbol(b, c);");
  }

// com.google.javascript.jscomp.RenameVarsTest::testDollarSignSuperExport1
  public void testDollarSignSuperExport1() {
    useGoogleCodingConvention = false;
    
    test("var x = function($super,duper,$fantastic){}",
         "var c = function($super,    a,        b){}");

    localRenamingOnly = false;
    test("var $super = 1", "var a = 1");

    useGoogleCodingConvention = true;
    test("var x = function($super,duper,$fantastic){}",
         "var c = function($super,a,b){}");
  }

// com.google.javascript.jscomp.RenameVarsTest::testDollarSignSuperExport2
  public void testDollarSignSuperExport2() {
    boolean normalizedExpectedJs = false;
    super.enableNormalize(false);

    useGoogleCodingConvention = false;
    
    test("var x = function($super,duper,$fantastic){};" +
            "var y = function($super,duper){};",
         "var c = function($super,    a,         b){};" +
            "var d = function($super,    a){};");

    localRenamingOnly = false;
    test("var $super = 1", "var a = 1");

    useGoogleCodingConvention = true;
    test("var x = function($super,duper,$fantastic){};" +
            "var y = function($super,duper){};",
         "var c = function($super,   a,    b         ){};" +
            "var d = function($super,a){};");

    super.disableNormalize();
  }

// com.google.javascript.jscomp.RenameVarsTest::testPseudoNames
  public void testPseudoNames() {
    generatePseudoNames = false;
    
    test("var foo = function(a, b, c){}",
         "var d = function(a, b, c){}");

    generatePseudoNames = true;
    test("var foo = function(a, b, c){}",
         "var $foo$$ = function($a$$, $b$$, $c$$){}");

    test("var a = function(a, b, c){}",
         "var $a$$ = function($a$$, $b$$, $c$$){}");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testDoNotUseReplacementMap
  public void testDoNotUseReplacementMap() {
    useReplacementMap = false;
    test("var x = goog.getCssName('goog-footer-active')",
         "var x = 'goog-footer-active'");
    test("el.className = goog.getCssName('goog-colorswatch-disabled')",
         "el.className = 'goog-colorswatch-disabled'");
    test("setClass(goog.getCssName('active-buttonbar'))",
         "setClass('active-buttonbar')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("goog", 2)
        .put("footer", 1)
        .put("active", 2)
        .put("colorswatch", 1)
        .put("disabled", 1)
        .put("buttonbar", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithUnknownStringLiterals
  public void testOneArgWithUnknownStringLiterals() {
    test("var x = goog.getCssName('unknown')",
         "var x = 'unknown'", null, UNKNOWN_SYMBOL_WARNING);
    test("el.className = goog.getCssName('ooo')",
         "el.className = 'ooo'", null, UNKNOWN_SYMBOL_WARNING);
    test("setClass(goog.getCssName('ab'))",
         "setClass('ab')", null, UNKNOWN_SYMBOL_WARNING);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithSimpleStringLiterals
  public void testOneArgWithSimpleStringLiterals() {
    test("var x = goog.getCssName('buttonbar')",
         "var x = 'b'");
    test("el.className = goog.getCssName('colorswatch')",
         "el.className = 'c'");
    test("setClass(goog.getCssName('elephant'))",
         "setClass('e')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("buttonbar", 1)
        .put("colorswatch", 1)
        .put("elephant", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithCompositeClassNames
  public void testOneArgWithCompositeClassNames() {
    test("var x = goog.getCssName('goog-footer-active')",
         "var x = 'g-f-a'");
    test("el.className = goog.getCssName('goog-colorswatch-disabled')",
         "el.className = 'g-c-d'");
    test("setClass(goog.getCssName('active-buttonbar'))",
         "setClass('a-b')");
    Map<String, Integer> expected =
        new ImmutableMap.Builder<String, Integer>()
        .put("goog", 2)
        .put("footer", 1)
        .put("active", 2)
        .put("colorswatch", 1)
        .put("disabled", 1)
        .put("buttonbar", 1)
        .build();
    assertEquals(expected, cssNames);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithCompositeClassNamesFull
  public void testOneArgWithCompositeClassNamesFull() {
    renamingMap = getFullMap();

    test("var x = goog.getCssName('long-prefix')",
         "var x = 'h'");
    test("var x = goog.getCssName('long-prefix-suffix1')",
         "var x = 'h-i'");
    test("var x = goog.getCssName('unrelated')",
         "var x = 'l'");
    test("var x = goog.getCssName('unrelated-word')",
         "var x = 'k'");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testOneArgWithCompositeClassNamesWithUnknownParts
  public void testOneArgWithCompositeClassNamesWithUnknownParts() {
    test("var x = goog.getCssName('goog-header-active')",
         "var x = 'goog-header-active'", null, UNKNOWN_SYMBOL_WARNING);
    test("el.className = goog.getCssName('goog-colorswatch-focussed')",
         "el.className = 'goog-colorswatch-focussed'",
         null, UNKNOWN_SYMBOL_WARNING);
    test("setClass(goog.getCssName('inactive-buttonbar'))",
         "setClass('inactive-buttonbar')", null, UNKNOWN_SYMBOL_WARNING);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testTwoArgsWithStringLiterals
  public void testTwoArgsWithStringLiterals() {
    test("var x = goog.getCssName('header', 'active')",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("el.className = goog.getCssName('footer', window)",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("setClass(goog.getCssName('buttonbar', 'disabled'))",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
    test("setClass(goog.getCssName(goog.getCssName('buttonbar'), 'active'))",
         null, UNEXPECTED_STRING_LITERAL_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testTwoArsWithVariableFirstArg
  public void testTwoArsWithVariableFirstArg() {
    test("var x = goog.getCssName(baseClass, 'active')",
         "var x = baseClass + '-a'");
    test("el.className = goog.getCssName(this.getClass(), 'disabled')",
         "el.className = this.getClass() + '-d'");
    test("setClass(goog.getCssName(BASE_CLASS, 'disabled'))",
         "setClass(BASE_CLASS + '-d')");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testTwoArgsWithVariableFirstArgFull
  public void testTwoArgsWithVariableFirstArgFull() {
    renamingMap = getFullMap();

    test("var x = goog.getCssName(baseClass, 'long-suffix')",
         "var x = baseClass + '-m'");
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testZeroArguments
  public void testZeroArguments() {
    test("goog.getCssName()", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testManyArguments
  public void testManyArguments() {
    test("goog.getCssName('a', 'b', 'c')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
    test("goog.getCssName('a', 'b', 'c', 'd')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
    test("goog.getCssName('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i')", null,
        ReplaceCssNames.INVALID_NUM_ARGUMENTS_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testNonStringArgument
  public void testNonStringArgument() {
    test("goog.getCssName(window);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(555);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName([]);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName({});", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(null);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(undefined);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);

    test("goog.getCssName(baseClass, window);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, 555);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, []);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, {});", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, null);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
    test("goog.getCssName(baseClass, undefined);", null,
        ReplaceCssNames.STRING_LITERAL_EXPECTED_ERROR);
  }

// com.google.javascript.jscomp.ReplaceCssNamesTest::testNoSymbolMapStripsCallAndDoesntIssueWarnings
  public void testNoSymbolMapStripsCallAndDoesntIssueWarnings() {
    String input = "[goog.getCssName('test'), goog.getCssName(base, 'active')]";
    Compiler compiler = new Compiler();
    ErrorManager errorMan = new BasicErrorManager() {
      @Override protected void printSummary() {}
      @Override public void println(CheckLevel level, JSError error) {}
    };
    compiler.setErrorManager(errorMan);
    Node root = compiler.parseTestCode(input);
    useReplacementMap = false;
    ReplaceCssNames replacer = new ReplaceCssNames(compiler, null);
    replacer.process(null, root);
    assertEquals("[\"test\",base+\"-active\"]", compiler.toSource(root));
    assertEquals("There should be no errors", 0, errorMan.getErrorCount());
    assertEquals("There should be no warnings", 0, errorMan.getWarningCount());
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testAssign
  public void testAssign() {
    test("foo.bar = goog.events.getUniqueId('foo_bar')",
         "foo.bar = 'a'");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testObjectLiteral
  public void testObjectLiteral() {
    test("foo = { bar : goog.events.getUniqueId('foo_bar')}",
         "foo = { bar : 'a' }");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testTwoNamespaces
  public void testTwoNamespaces() {
    test("foo.bar = goog.events.getUniqueId('foo_bar');\n"
         + "baz.blah = goog.place.getUniqueId('baz_blah');\n",
         "foo.bar = 'a';\n"
         + "baz.blah = 'a'\n");
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testLocalCall
  public void testLocalCall() {
    testSame(new String[] {
          "function Foo() { goog.events.getUniqueId('foo'); }"
        },
        ReplaceIdGenerators.NON_GLOBAL_ID_GENERATOR_CALL);
  }

// com.google.javascript.jscomp.ReplaceIdGeneratorsTest::testConditionalCall
  public void testConditionalCall() {
    testSame(new String[] {"if (x) foo = goog.events.getUniqueId('foo')"},
             ReplaceIdGenerators.CONDITIONAL_ID_GENERATOR_CALL);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testReplaceSimpleMessage
  public void testReplaceSimpleMessage() {
    registerMessage(new JsMessage.Builder("MSG_A")
        .appendStringPart("Hi\nthere")
        .build());

    assertOutputEquals("var MSG_A = goog.getMsg('asdf');",
        "var MSG_A=\"Hi\\nthere\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testNameReplacement
  public void testNameReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_B")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());

    assertOutputEquals(
        "var MSG_B = goog.getMsg('asdf {$measly}', {measly: x});",
        "var MSG_B=\"One \"+(x+\" ph\")");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testGetPropReplacement
  public void testGetPropReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_C")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_C = goog.getMsg('${$amount}', {amount: a.b.amount});",
        "var MSG_C=a.b.amount");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testFunctionCallReplacement
  public void testFunctionCallReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_D")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_D = goog.getMsg('${$amount}', {amount: getAmt()});",
        "var MSG_D=getAmt()");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testMethodCallReplacement
  public void testMethodCallReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_E")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_E = goog.getMsg('${$amount}', {amount: obj.getAmt()});",
        "var MSG_E=obj.getAmt()");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testHookReplacement
  public void testHookReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_F")
        .appendStringPart("#")
        .appendPlaceholderReference("amount")
        .appendStringPart(".")
        .build());

    assertOutputEquals(
        "var MSG_F = goog.getMsg('${$amount}', {amount: (a ? b : c)});",
        "var MSG_F=\"#\"+((a?b:c)+\".\")");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testAddReplacement
  public void testAddReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_G")
        .appendPlaceholderReference("amount")
        .build());

    assertOutputEquals(
        "var MSG_G = goog.getMsg('${$amount}', {amount: x + ''});",
        "var MSG_G=x+\"\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testPlaceholderValueReferencedTwice
  public void testPlaceholderValueReferencedTwice()  {
    registerMessage(new JsMessage.Builder("MSG_H")
        .appendPlaceholderReference("dick")
        .appendStringPart(", ")
        .appendPlaceholderReference("dick")
        .appendStringPart(" and ")
        .appendPlaceholderReference("jane")
        .build());

    assertOutputEquals(
        "var MSG_H = goog.getMsg('{$dick}{$jane}', {jane: x, dick: y});",
        "var MSG_H=y+(\", \"+(y+(\" and \"+x)))");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testPlaceholderNameInLowerCamelCase
  public void testPlaceholderNameInLowerCamelCase()  {
    registerMessage(new JsMessage.Builder("MSG_I")
        .appendStringPart("Sum: $")
        .appendPlaceholderReference("amtEarned")
        .build());

    assertOutputEquals(
        "var MSG_I = goog.getMsg('${$amtEarned}', {amtEarned: x});",
        "var MSG_I=\"Sum: $\"+x");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testQualifiedMessageName
  public void testQualifiedMessageName()  {
    registerMessage(new JsMessage.Builder("MSG_J")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());

    assertOutputEquals(
        "a.b.c.MSG_J = goog.getMsg('asdf {$measly}', {measly: x});",
        "a.b.c.MSG_J=\"One \"+(x+\" ph\")");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testSimpleMessageReplacementMissing
  public void testSimpleMessageReplacementMissing()  {
    assertOutputEquals("var MSG_E = 'd*6a0@z>t';", "var MSG_E=\"d*6a0@z>t\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testStrictModeAndMessageReplacementAbsentInBundle
  public void testStrictModeAndMessageReplacementAbsentInBundle()  {
    strictReplacement = true;
    process("var MSG_E = 'Hello';");
    assertEquals(1, compiler.getErrors().length);
    assertEquals(BUNDLE_DOES_NOT_HAVE_THE_MESSAGE,
        compiler.getErrors()[0].getType());
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testStrictModeAndMessageReplacementAbsentInNonEmptyBundle
  public void testStrictModeAndMessageReplacementAbsentInNonEmptyBundle()  {
    registerMessage(new JsMessage.Builder("MSG_J")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());

    strictReplacement = true;
    process("var MSG_E = 'Hello';");
    assertEquals(1, compiler.getErrors().length);
    assertEquals(BUNDLE_DOES_NOT_HAVE_THE_MESSAGE,
        compiler.getErrors()[0].getType());
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testFunctionReplacementMissing
  public void testFunctionReplacementMissing()  {
    assertOutputEquals("var MSG_F = function() {return 'asdf'};",
        "var MSG_F=function(){return\"asdf\"}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testFunctionWithParamReplacementMissing
  public void testFunctionWithParamReplacementMissing()  {
    assertOutputEquals(
        "var MSG_G = function(measly) {return 'asdf' + measly};",
        "var MSG_G=function(measly){return\"asdf\"+measly}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testPlaceholderNameInLowerUnderscoreCase
  public void testPlaceholderNameInLowerUnderscoreCase()  {
    process("var MSG_J = goog.getMsg('${$amt_earned}', {amt_earned: x});");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(MESSAGE_TREE_MALFORMED, error.getType());
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testBadPlaceholderReferenceInReplacement
  public void testBadPlaceholderReferenceInReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_K")
        .appendPlaceholderReference("amount")
        .build());

    process("var MSG_K = goog.getMsg('Hi {$jane}', {jane: x});");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(MESSAGE_TREE_MALFORMED, error.getType());
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleNoPlaceholdersVarSyntax
  public void testLegacyStyleNoPlaceholdersVarSyntax()  {
    registerMessage(new JsMessage.Builder("MSG_A")
        .appendStringPart("Hi\nthere")
        .build());
    assertOutputEquals("var MSG_A = 'd*6a0@z>t';",
        "var MSG_A=\"Hi\\nthere\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleNoPlaceholdersFunctionSyntax
  public void testLegacyStyleNoPlaceholdersFunctionSyntax()  {
    registerMessage(new JsMessage.Builder("MSG_B")
        .appendStringPart("Hi\nthere")
        .build());

    assertOutputEquals("var MSG_B = function() {return 'asdf'};",
        "var MSG_B=function(){return\"Hi\\nthere\"}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleOnePlaceholder
  public void testLegacyStyleOnePlaceholder()  {
    registerMessage(new JsMessage.Builder("MSG_C")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());
    assertOutputEquals(
        "var MSG_C = function(measly) {return 'asdf' + measly};",
        "var MSG_C=function(measly){return\"One \"+(measly+\" ph\")}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleTwoPlaceholders
  public void testLegacyStyleTwoPlaceholders()  {
    registerMessage(new JsMessage.Builder("MSG_D")
        .appendPlaceholderReference("dick")
        .appendStringPart(" and ")
        .appendPlaceholderReference("jane")
        .build());
    assertOutputEquals(
        "var MSG_D = function(jane, dick) {return jane + dick};",
        "var MSG_D=function(jane,dick){return dick+(\" and \"+jane)}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStylePlaceholderNameInLowerCamelCase
  public void testLegacyStylePlaceholderNameInLowerCamelCase() {
    registerMessage(new JsMessage.Builder("MSG_E")
        .appendStringPart("Sum: $")
        .appendPlaceholderReference("amtEarned")
        .build());
    assertOutputEquals(
        "var MSG_E = function(amtEarned) {return amtEarned + 'x'};",
        "var MSG_E=function(amtEarned){return\"Sum: $\"+amtEarned}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStylePlaceholderNameInLowerUnderscoreCase
  public void testLegacyStylePlaceholderNameInLowerUnderscoreCase() {
    registerMessage(new JsMessage.Builder("MSG_F")
        .appendStringPart("Sum: $")
        .appendPlaceholderReference("amt_earned")
        .build());

    
    assertOutputEquals(
        "var MSG_F = function(amt_earned) {return amt_earned + 'x'};",
        "var MSG_F=function(amt_earned){return\"Sum: $\"+amt_earned}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleBadPlaceholderReferenceInReplacemen
  public void testLegacyStyleBadPlaceholderReferenceInReplacemen() {
    registerMessage(new JsMessage.Builder("MSG_B")
        .appendStringPart("Ola, ")
        .appendPlaceholderReference("chimp")
        .build());

    process("var MSG_B = function(chump) {return chump + 'x'};");
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. "
        + "Unrecognized message placeholder referenced: chimp",
        error.description);
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError1
  public void testThrowError1() {
    testDebugStrings(
        "throw Error('xyz');",
        "throw Error('a');",
        (new String[] { "a", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError2
  public void testThrowError2() {
    testDebugStrings(
        "throw Error('x' +\n    'yz');",
        "throw Error('a');",
        (new String[] { "a", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError3
  public void testThrowError3() {
    testDebugStrings(
        "throw Error('Unhandled mail' + ' search type ' + type);",
        "throw Error('a' + '`' + type);",
        (new String[] { "a", "Unhandled mail search type `" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError4
  public void testThrowError4() {
    testDebugStrings(
        "\n" +
        "var A = function() {};\n" +
        "A.prototype.m = function(child) {\n" +
        "  if (this.haveChild(child)) {\n" +
        "    throw Error('Node: ' + this.getDataPath() +\n" +
        "                ' already has a child named ' + child);\n" +
        "  } else if (child.parentNode) {\n" +
        "    throw Error('Node: ' + child.getDataPath() +\n" +
        "                ' already has a parent');\n" +
        "  }\n" +
        "  child.parentNode = this;\n" +
        "};",

        "var A = function(){};\n" +
        "A.prototype.m = function(child) {\n" +
        "  if (this.haveChild(child)) {\n" +
        "    throw Error('a' + '`' + this.getDataPath() + '`' + child);\n" +
        "  } else if (child.parentNode) {\n" +
        "    throw Error('b' + '`' + child.getDataPath());\n" +
        "  }\n" +
        "  child.parentNode = this;\n" +
        "};",
        (new String[] {
            "a",
            "Node: ` already has a child named `",
            "b",
            "Node: ` already has a parent",
            }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowNonStringError
  public void testThrowNonStringError() {
    
    
    testDebugStrings(
        "throw Error(x('abc'));",
        "throw Error(x('abc'));",
        (new String[] { }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowConstStringError
  public void testThrowConstStringError() {
    testDebugStrings(
        "var AA = 'uvw', AB = 'xyz'; throw Error(AB);",
        "var AA = 'uvw', AB = 'xyz'; throw Error('a');",
        (new String [] { "a", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowNewError1
  public void testThrowNewError1() {
    testDebugStrings(
        "throw new Error('abc');",
        "throw new Error('a');",
        (new String[] { "a", "abc" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowNewError2
  public void testThrowNewError2() {
    testDebugStrings(
        "throw new Error();",
        "throw new Error();",
        new String[] {});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer1
  public void testStartTracer1() {
    testDebugStrings(
        "goog.debug.Trace.startTracer('HistoryManager.updateHistory');",
        "goog.debug.Trace.startTracer('a');",
        (new String[] { "a", "HistoryManager.updateHistory" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer2
  public void testStartTracer2() {
    testDebugStrings(
        "goog$debug$Trace.startTracer('HistoryManager', 'updateHistory');",
        "goog$debug$Trace.startTracer('a', 'b');",
        (new String[] {
            "a", "HistoryManager",
            "b", "updateHistory" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer3
  public void testStartTracer3() {
    testDebugStrings(
        "goog$debug$Trace.startTracer('ThreadlistView',\n" +
        "                             'Updating ' + array.length + ' rows');",
        "goog$debug$Trace.startTracer('a', 'b' + '`' + array.length);",
        new String[] { "a", "ThreadlistView", "b", "Updating ` rows" });
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStartTracer4
  public void testStartTracer4() {
    testDebugStrings(
        "goog.debug.Trace.startTracer(s, 'HistoryManager.updateHistory');",
        "goog.debug.Trace.startTracer(s, 'a');",
        (new String[] { "a", "HistoryManager.updateHistory" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerInitialization
  public void testLoggerInitialization() {
    testDebugStrings(
        "goog$debug$Logger$getLogger('my.app.Application');",
        "goog$debug$Logger$getLogger('a');",
        (new String[] { "a", "my.app.Application" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject1
  public void testLoggerOnObject1() {
    testDebugStrings(
        "var x = {};" +
        "x.logger_ = goog.debug.Logger.getLogger('foo');" +
        "x.logger_.info('Some message');",
        "var x$logger_ = goog.debug.Logger.getLogger('a');" +
        "x$logger_.info('b');",
        new String[] {
            "a", "foo",
            "b", "Some message"});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject2
  public void testLoggerOnObject2() {
    test(
        "var x = {};" +
        "x.info = function(a) {};" +
        "x.info('Some message');",
        "var x$info = function(a) {};" +
        "x$info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject3a
  public void testLoggerOnObject3a() {
    testSame(
        "\n" +
        "var x = function() {};\n" +
        "x.prototype.info = function(a) {};" +
        "(new x).info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject3b
  public void testLoggerOnObject3b() {
    testSame(
      "\n" +
      "var x = function() {};\n" +
      "x.prototype.info = function(a) {};" +
      "var y = (new x); this.info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject4
  public void testLoggerOnObject4() {
    testSame("(new x).info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnObject5
  public void testLoggerOnObject5() {
    testSame("my$Thing.logger_.info('Some message');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnVar
  public void testLoggerOnVar() {
    testDebugStrings(
        "var logger = goog.debug.Logger.getLogger('foo');" +
        "logger.info('Some message');",
        "var logger = goog.debug.Logger.getLogger('a');" +
        "logger.info('b');",
        new String[] {
            "a", "foo",
            "b", "Some message"});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testLoggerOnThis
  public void testLoggerOnThis() {
    testDebugStrings(
        "function f() {" +
        "  this.logger_ = goog.debug.Logger.getLogger('foo');" +
        "  this.logger_.info('Some message');" +
        "}",
        "function f() {" +
        "  this.logger_ = goog.debug.Logger.getLogger('a');" +
        "  this.logger_.info('b');" +
        "}",
        new String[] {
            "a", "foo",
            "b", "Some message"});
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedErrorString1
  public void testRepeatedErrorString1() {
    testDebugStrings(
        "Error('abc');Error('def');Error('abc');",
        "Error('a');Error('b');Error('a');",
        (new String[] { "a", "abc", "b", "def" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedErrorString2
  public void testRepeatedErrorString2() {
    testDebugStrings(
        "Error('a:' + u + ', b:' + v); Error('a:' + x + ', b:' + y);",
        "Error('a' + '`' + u + '`' + v); Error('a' + '`' + x + '`' + y);",
        (new String[] { "a", "a:`, b:`" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedErrorString3
  public void testRepeatedErrorString3() {
    testDebugStrings(
        "var AB = 'b'; throw Error(AB); throw Error(AB);",
        "var AB = 'b'; throw Error('a'); throw Error('a');",
        (new String[] { "a", "b" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedTracerString
  public void testRepeatedTracerString() {
    testDebugStrings(
        "goog$debug$Trace.startTracer('A', 'B', 'A');",
        "goog$debug$Trace.startTracer('a', 'b', 'a');",
        (new String[] { "a", "A", "b", "B" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedLoggerString
  public void testRepeatedLoggerString() {
    testDebugStrings(
        "goog$debug$Logger$getLogger('goog.net.XhrTransport');" +
        "goog$debug$Logger$getLogger('my.app.Application');" +
        "goog$debug$Logger$getLogger('my.app.Application');",
        "goog$debug$Logger$getLogger('a');" +
        "goog$debug$Logger$getLogger('b');" +
        "goog$debug$Logger$getLogger('b');",
        new String[] {
            "a", "goog.net.XhrTransport","b", "my.app.Application" });
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testRepeatedStringsWithDifferentMethods
  public void testRepeatedStringsWithDifferentMethods() {
    test(
        "throw Error('A');"
            + "goog$debug$Trace.startTracer('B', 'A');"
            + "goog$debug$Logger$getLogger('C');"
            + "goog$debug$Logger$getLogger('B');"
            + "goog$debug$Logger$getLogger('A');"
            + "throw Error('D');"
            + "throw Error('C');"
            + "throw Error('B');"
            + "throw Error('A');",
        "throw Error('a');"
            + "goog$debug$Trace.startTracer('b', 'a');"
            + "goog$debug$Logger$getLogger('c');"
            + "goog$debug$Logger$getLogger('b');"
            + "goog$debug$Logger$getLogger('a');"
            + "throw Error('d');"
            + "throw Error('c');"
            + "throw Error('b');"
            + "throw Error('a');");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testReserved
  public void testReserved() {
    testDebugStrings(
        "throw Error('xyz');",
        "throw Error('a');",
        (new String[] { "a", "xyz" }));
    reserved = ImmutableSet.of("a", "b", "c");
    testDebugStrings(
        "throw Error('xyz');",
        "throw Error('d');",
        (new String[] { "d", "xyz" }));
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValue
  public void testValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  jscomp.typecheck.checkType(i, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testConstValue
  public void testConstValue() {
    
    
    testChecks(" function f(CONST) {}",
        "function f(CONST) {" +
        "  jscomp.typecheck.checkType(CONST, " +
        "      [jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValueWithInnerFn
  public void testValueWithInnerFn() {
    testChecks(" function f(i) { function g() {} }",
        "function f(i) {" +
        "  function g() {}" +
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

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testInnerClasses
  public void testInnerClasses() {
    testChecks(
        "function f() {  function inner() {} }" +
        "function g() {  function inner() {} }",
        "function f() {" +
        "   function inner() {}" +
        "  inner.prototype['instance_of__inner'] = true;" +
        "}" +
        "function g() {" +
        "   function inner$$1() {}" +
        "  inner$$1.prototype['instance_of__inner$$1'] = true;" +
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

// com.google.javascript.jscomp.ScopedAliasesTest::testOneLevel
  public void testOneLevel() {
    testScoped("var g = goog;g.dom.createElement(g.dom.TagName.DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTwoLevel
  public void testTwoLevel() {
    testScoped("var d = goog.dom;d.createElement(d.TagName.DIV);",
               "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTransitive
  public void testTransitive() {
    testScoped("var d = goog.dom;var DIV = d.TagName.DIV;d.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTransitiveInSameVar
  public void testTransitiveInSameVar() {
    testScoped("var d = goog.dom, DIV = d.TagName.DIV;d.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testMultipleTransitive
  public void testMultipleTransitive() {
    testScoped(
        "var g=goog;var d=g.dom;var t=d.TagName;var DIV=t.DIV;" +
            "d.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testFourLevel
  public void testFourLevel() {
    testScoped("var DIV = goog.dom.TagName.DIV;goog.dom.createElement(DIV);",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testWorksInClosures
  public void testWorksInClosures() {
    testScoped(
        "var DIV = goog.dom.TagName.DIV;" +
            "goog.x = function() {goog.dom.createElement(DIV);};",
        "goog.x = function() {goog.dom.createElement(goog.dom.TagName.DIV);};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testOverridden
  public void testOverridden() {
    
    
    testScopedNoChanges(
        "var g = goog;", "goog.x = function(g) {g.z()};");
    
    testScopedNoChanges(
        "var g = goog;", "goog.x = function() {var g = {}; g.z()};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTwoScopes
  public void testTwoScopes() {
    test(
        "goog.scope(function() {var g = goog;g.method()});" +
        "goog.scope(function() {g.method();});",
        "goog.method();g.method();");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTwoSymbolsInTwoScopes
  public void testTwoSymbolsInTwoScopes() {
    test(
        "var goog = {};" +
        "goog.scope(function() { var g = goog; g.Foo = function() {}; });" +
        "goog.scope(function() { " +
        "  var Foo = goog.Foo; goog.bar = function() { return new Foo(); };" +
        "});",
        "var goog = {};" +
        "goog.Foo = function() {};" +
        "goog.bar = function() { return new goog.Foo(); };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasOfSymbolInGoogScope
  public void testAliasOfSymbolInGoogScope() {
    test(
        "var goog = {};" +
        "goog.scope(function() {" +
        "  var g = goog;" +
        "  g.Foo = function() {};" +
        "  var Foo = g.Foo;" +
        "  Foo.prototype.bar = function() {};" +
        "});",
        "var goog = {}; goog.Foo = function() {};" +
        "goog.Foo.prototype.bar = function() {};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedFunctionReturnThis
  public void testScopedFunctionReturnThis() {
    test("goog.scope(function() { " +
         "  var g = goog; g.f = function() { return this; };" +
         "});",
         "goog.f = function() { return this; };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedFunctionAssignsToVar
  public void testScopedFunctionAssignsToVar() {
    test("goog.scope(function() { " +
         "  var g = goog; g.f = function(x) { x = 3; return x; };" +
         "});",
         "goog.f = function(x) { x = 3; return x; };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedFunctionThrows
  public void testScopedFunctionThrows() {
    test("goog.scope(function() { " +
         "  var g = goog; g.f = function() { throw 'error'; };" +
         "});",
         "goog.f = function() { throw 'error'; };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testPropertiesNotChanged
  public void testPropertiesNotChanged() {
    testScopedNoChanges("var x = goog.dom;", "y.x();");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testShadowedVar
  public void testShadowedVar() {
    test("var Popup = {};" +
         "var OtherPopup = {};" +
         "goog.scope(function() {" +
         "  var Popup = OtherPopup;" +
         "  Popup.newMethod = function() { return new Popup(); };" +
         "});",
         "var Popup = {};" +
         "var OtherPopup = {};" +
         "OtherPopup.newMethod = function() { return new OtherPopup(); };");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocType
  public void testJsDocType() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocParameter
  public void testJsDocParameter() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocExtends
  public void testJsDocExtends() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocImplements
  public void testJsDocImplements() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocEnum
  public void testJsDocEnum() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocReturn
  public void testJsDocReturn() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocThis
  public void testJsDocThis() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocThrows
  public void testJsDocThrows() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocSubType
  public void testJsDocSubType() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testJsDocTypedef
  public void testJsDocTypedef() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testArrayJsDoc
  public void testArrayJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testObjectJsDoc
  public void testObjectJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testUnionJsDoc
  public void testUnionJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testFunctionJsDoc
  public void testFunctionJsDoc() {
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
    testTypes(
        "var x = goog.Timer;",
        ""
        + " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testTestTypes
  public void testTestTypes() {
    try {
      testTypes(
          "var x = goog.Timer;",
          ""
          + " types.actual;"
          + " types.expected;");
      fail("Test types should fail here.");
    } catch (AssertionError e) {
    }
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testNullType
  public void testNullType() {
    testTypes(
        "var x = goog.Timer;",
        " types.actual;"
        + " types.expected;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedThis
  public void testScopedThis() {
    testScopedFailure("this.y = 10;", ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
    testScopedFailure("var x = this;",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
    testScopedFailure("fn(this);", ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasRedefinition
  public void testAliasRedefinition() {
    testScopedFailure("var x = goog.dom; x = goog.events;",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testAliasNonRedefinition
  public void testAliasNonRedefinition() {
    test("var y = {}; goog.scope(function() { goog.dom = y; });",
         "var y = {}; goog.dom = y;");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedReturn
  public void testScopedReturn() {
    testScopedFailure("return;", ScopedAliases.GOOG_SCOPE_USES_RETURN);
    testScopedFailure("var x = goog.dom; return;",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testScopedThrow
  public void testScopedThrow() {
    testScopedFailure("throw 'error';", ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testUsedImproperly
  public void testUsedImproperly() {
    testFailure("var x = goog.scope(function() {});",
        ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testBadParameters
  public void testBadParameters() {
    testFailure("goog.scope()", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(10)", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(function() {}, 10)",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(function z() {})",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    testFailure("goog.scope(function(a, b, c) {})",
        ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testNonAliasLocal
  public void testNonAliasLocal() {
    testScopedFailure("var x = 10", ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
    testScopedFailure("var x = goog.dom + 10",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
    testScopedFailure("var x = goog['dom']",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
    testScopedFailure("var x = goog.dom, y = 10",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testNoGoogScope
  public void testNoGoogScope() {
    String fullJsCode =
        "var g = goog;\n g.dom.createElement(g.dom.TagName.DIV);";
    TransformationHandlerSpy spy = new TransformationHandlerSpy();
    transformationHandler = spy;
    test(fullJsCode, fullJsCode);

    assertTrue(spy.observedPositions.isEmpty());
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testRecordOneAlias
  public void testRecordOneAlias() {
    String fullJsCode = GOOG_SCOPE_START_BLOCK
        + "var g = goog;\n g.dom.createElement(g.dom.TagName.DIV);\n"
        + GOOG_SCOPE_END_BLOCK;
    String expectedJsCode = "goog.dom.createElement(goog.dom.TagName.DIV);\n";

    TransformationHandlerSpy spy = new TransformationHandlerSpy();
    transformationHandler = spy;
    test(fullJsCode, expectedJsCode);

    assertTrue(spy.observedPositions.containsKey("testcode"));
    List<SourcePosition<AliasTransformation>> positions =
        spy.observedPositions.get("testcode");
    assertEquals(1, positions.size());
    verifyAliasTransformationPosition(
        1, GOOG_SCOPE_LEN, 2, 1, positions.get(0));

    assertEquals(1, spy.constructedAliases.size());
    AliasSpy aliasSpy = (AliasSpy) spy.constructedAliases.get(0);
    assertEquals("goog", aliasSpy.observedDefinitions.get("g"));
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testRecordMultipleAliases
  public void testRecordMultipleAliases() {
    String fullJsCode = GOOG_SCOPE_START_BLOCK
        + "var g = goog;\n var b= g.bar;\n var f = goog.something.foo;"
        + "g.dom.createElement(g.dom.TagName.DIV);\n b.foo();"
        + GOOG_SCOPE_END_BLOCK;
    String expectedJsCode =
        "goog.dom.createElement(goog.dom.TagName.DIV);\n goog.bar.foo();";
    TransformationHandlerSpy spy = new TransformationHandlerSpy();
    transformationHandler = spy;
    test(fullJsCode, expectedJsCode);

    assertTrue(spy.observedPositions.containsKey("testcode"));
    List<SourcePosition<AliasTransformation>> positions =
        spy.observedPositions.get("testcode");
    assertEquals(1, positions.size());
    verifyAliasTransformationPosition(
        1, GOOG_SCOPE_LEN, 3, 1, positions.get(0));

    assertEquals(1, spy.constructedAliases.size());
    AliasSpy aliasSpy = (AliasSpy) spy.constructedAliases.get(0);
    assertEquals("goog", aliasSpy.observedDefinitions.get("g"));
    assertEquals("g.bar", aliasSpy.observedDefinitions.get("b"));
    assertEquals("goog.something.foo", aliasSpy.observedDefinitions.get("f"));
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testRecordAliasFromMultipleGoogScope
  public void testRecordAliasFromMultipleGoogScope() {
    String firstGoogScopeBlock = GOOG_SCOPE_START_BLOCK
        + "\n var g = goog;\n g.dom.createElement(g.dom.TagName.DIV);\n"
        + GOOG_SCOPE_END_BLOCK;
    String fullJsCode = firstGoogScopeBlock + "\n\nvar l = abc.def;\n\n"
        + GOOG_SCOPE_START_BLOCK
        + "\n var z = namespace.Zoo;\n z.getAnimals(l);\n"
        + GOOG_SCOPE_END_BLOCK;

    String expectedJsCode = "goog.dom.createElement(goog.dom.TagName.DIV);\n"
        + "\n\nvar l = abc.def;\n\n" + "\n namespace.Zoo.getAnimals(l);\n";

    TransformationHandlerSpy spy = new TransformationHandlerSpy();
    transformationHandler = spy;
    test(fullJsCode, expectedJsCode);

    assertTrue(spy.observedPositions.containsKey("testcode"));
    List<SourcePosition<AliasTransformation>> positions =
        spy.observedPositions.get("testcode");
    assertEquals(2, positions.size());

    verifyAliasTransformationPosition(
        1, GOOG_SCOPE_LEN, 6, 0, positions.get(0));

    verifyAliasTransformationPosition(
        8, GOOG_SCOPE_LEN, 11, 4, positions.get(1));

    assertEquals(2, spy.constructedAliases.size());
    AliasSpy aliasSpy = (AliasSpy) spy.constructedAliases.get(0);
    assertEquals("goog", aliasSpy.observedDefinitions.get("g"));

    aliasSpy = (AliasSpy) spy.constructedAliases.get(1);
    assertEquals("namespace.Zoo", aliasSpy.observedDefinitions.get("z"));
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowSimple1
  public void testShadowSimple1() {
    test("function foo(x) { return function (y) {} }",
         "function   b(a) { return function (a) {} }");

    generatePseudoNames = true;

    test("function  foo  ( x  ) { return function ( y  ) {} }",
         "function $foo$$($x$$) { return function ($x$$) {} }");

  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowSimple2
  public void testShadowSimple2() {
    test("function foo(x,y) { return function (y,z) {} }",
         "function   c(a,b) { return function (a,b) {} }");

    generatePseudoNames = true;

    test("function  foo  ( x  , y  ) { return function ( y  , z  ) {} }",
         "function $foo$$($x$$,$y$$) { return function ($x$$,$y$$) {} }");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowMostUsedVar
  public void testShadowMostUsedVar() {
    generatePseudoNames = true;
    test("function  foo  () {var  x  ; var  y  ;  y  ; y  ; y  ; x  ;" +
         "  return function ( k  ) {} }",

         "function $foo$$() {var $x$$; var $y$$; $y$$;$y$$;$y$$;$x$$;" +
         "  return function ($y$$) {} }");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testNoShadowReferencedVariables
  public void testNoShadowReferencedVariables() {
    generatePseudoNames = true;
    test("function  f1  () { var  x  ; x  ; x  ; x  ;" +
         "  return function  f2  ( y  ) {" +
         "    return function  f3  () { x  } }}",
         "function $f1$$() { var $x$$;$x$$;$x$$;$x$$;" +
         "  return function $f2$$($y$$) {" +
         "    return function $f3$$() {$x$$} }}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testNoShadowGlobalVariables
  public void testNoShadowGlobalVariables() {
    generatePseudoNames = true;
    test("var  x  ;  x  ; function  foo  () { return function ( y  ) {}}",
         "var $x$$; $x$$; function $foo$$() { return function ($y$$) {}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowBleedInFunctionName
  public void testShadowBleedInFunctionName() {
    generatePseudoNames = true;
    test("function  foo  () { function  b  ( y  ) { y  }  b  ;  b  ;}",
         "function $foo$$() { function $b$$($b$$) {$b$$} $b$$; $b$$;}");
   }

// com.google.javascript.jscomp.ShadowVariablesTest::testNoShadowLessPopularName
  public void testNoShadowLessPopularName() {
    generatePseudoNames = true;
    
    
    
    
    
    test("function  f1  ( x  ) {" +
         "  function  f2  ( y  ) {}  x  ; x  ;}" +
         "function  f3  ( i  ) {" +
         "  var  k  ; var  j  ; j  ; j  ; j  ; j  ; j  ; j  ;}",

         "function $f1$$($x$$) {" +
         "  function $f2$$($y$$) {} $x$$;$x$$;}" +
         "function $f3$$($i$$) {" +
         "  var $k$$; var $j$$;$j$$;$j$$;$j$$;$j$$;$j$$;$j$$;}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowFunctionName
  public void testShadowFunctionName() {
    generatePseudoNames = true;
    test("var  g   = function() {" +
         "  var  x  ; return function(){function  y  (){}}}",
         "var $g$$ = function() {" +
         "  var $x$$; return function(){function $x$$(){}}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowLotsOfScopes1
  public void testShadowLotsOfScopes1() {
    generatePseudoNames = true;
    test("var  g   = function( x  ) { return function() { return function() {" +
         " return function() { var  y   }}}}",
         "var $g$$ = function($x$$) { return function() { return function() {" +
         " return function() { var $x$$ }}}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowLotsOfScopes2
  public void testShadowLotsOfScopes2() {
    generatePseudoNames = true;
    
    test("var  g   = function( x  ) { return function( y  ) " +
         " {return function() {return function() {  x   }}}}",
         "var $g$$ = function($x$$) { return function($y$$) " +
         " {return function() {return function() { $x$$ }}}}");

    test("var  g   = function( x  ) { return function() " +
        " {return function( y  ) {return function() {  x   }}}}",
        "var $g$$ = function($x$$) { return function() " +
        " {return function($y$$) {return function() { $x$$ }}}}");

    test("var  g   = function( x  ) { return function() " +
        " {return function() {return function( y  ) {  x   }}}}",
        "var $g$$ = function($x$$) { return function() " +
        " {return function() {return function($y$$) { $x$$ }}}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowLotsOfScopes3
  public void testShadowLotsOfScopes3() {
    generatePseudoNames = true;
    
    test("var  g   = function( x  ) { return function() " +
        " {return function() {return function() {  x   }; var  y   }}}",
        "var $g$$ = function($x$$) { return function() " +
        " {return function() {return function() { $x$$ }; var $y$$}}}");
    test("var  g   = function( x  ) { return function() " +
        " {return function() {return function() {  x   }}; var  y   }}",
        "var $g$$ = function($x$$) { return function() " +
        " {return function() {return function() { $x$$ }}; var $y$$}}");
    test("var  g   = function( x  ) { return function() " +
        " {return function() {return function() {  x   }}}; var  y   }",
        "var $g$$ = function($x$$) { return function() " +
        " {return function() {return function() { $x$$ }}}; var $y$$}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowLotsOfScopes4
  public void testShadowLotsOfScopes4() {
    
    test("var g = function(x) { return function() { return function() {" +
         " return function(){return function(){};var m};var n};var o}}",
         "var b = function(a) { return function() { return function() {" +
         " return function(){return function(){};var a};var a};var a}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowLotsOfScopes5
  public void testShadowLotsOfScopes5() {
    generatePseudoNames = true;
    test("var  g   = function( x  ) {" +
         " return function() { return function() {" +
         " return function() { return function() {" +
         "      x  }; o  };var  n  };var  o  };var  p  }",
         "var $g$$ = function($x$$) {" +
         " return function() { return function() {" +
         " return function() { return function() {" +
         "     $x$$};$o$$};var $p$$};var $o$$};var $p$$}");

    test("var  g   = function( x  ) {" +
        " return function() { return function() {" +
        " return function() { return function() {" +
        "      x  }; p  };var  n  };var  o  };var  p  }",
        "var $g$$ = function($x$$) {" +
        " return function() { return function() {" +
        " return function() { return function() {" +
        "     $x$$};$p$$};var $o$$};var $o$$};var $p$$}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowWithShadowAlready
  public void testShadowWithShadowAlready() {
    test("var g = function(x) { return function() { return function() {" +
         " return function(){return function(){x}};var p};var o};var p}",
         "var c = function(b) { return function() { return function() {" +
         " return function(){return function(){b}};var a};var a};var a}");

    test("var g = function(x) { return function() { return function() {" +
         " return function(){return function(){x};p};var p};var o};var p}",
         "var c = function(b) { return function() { return function() {" +
         " return function(){return function(){b};a};var a};var a};var a}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testShadowBug1
  public void testShadowBug1() {
    generatePseudoNames = true;
    test("function  f  ( x  ) { return function( y  ) {" +
         "    return function( x  ) {  x   +  y  ; }}}",
         "function $f$$($x$$) { return function($y$$) {" +
         "    return function($x$$) { $x$$ + $y$$; }}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testOptimal
  public void testOptimal() {
    
    test("function f(x) { function g(y) { function h(x) {}}}",
         "function c(a) { function b(a) { function b(a) {}}}");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testSharingAcrossInnerScopes
  public void testSharingAcrossInnerScopes() {
    test("function f() {var f=function g(){g()}; var x=function y(){y()}}",
         "function c() {var d=function a(){a()}; var e=function b(){b()}}");
    test("function f(x) { return x ? function(y){} : function(z) {} }",
         "function b(a) { return a ? function(a){} : function(a) {} }");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testExportedLocal1
  public void testExportedLocal1() {
    test("function f(a) { a();a();a(); return function($super){} }",
         "function b(a) { a();a();a(); return function($super){} }");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testExportedLocal2
  public void testExportedLocal2() {
    test("function f($super) { $super();$super(); return function(a){} }",
         "function a($super) { $super();$super(); return function(b){} }");
  }

// com.google.javascript.jscomp.ShadowVariablesTest::testBug4172539
  public void testBug4172539() {
    
    
    
    
    
    

    generatePseudoNames = true;
    test("function f(x) {" +
         "  x;x;x;" +
         "  return function (y) { y; x };" +
         "  return function (y) {" +
         "    y;" +
         "    return function (m, n) {" +
         "       m;m;m;" +
         "    };" +
         "  };" +
         "}",

         "function $f$$($x$$) {" +
         "  $x$$;$x$$;$x$$;" +
         "  return function ($y$$) { $y$$; $x$$ };" +
         "  return function ($x$$) {" +
         "    $x$$;" +
         "    return function ($x$$, $y$$) {" +
         "       $x$$;$x$$;$x$$;" +
         "    };" +
         "  };" +
         "}");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testDegenerateSafeMoves
  public void testDegenerateSafeMoves() {
    
    assertSafeMoveDegenerate("src: 1; env: ; dest: 3;");

    
    assertSafeMoveDegenerate("src: 1; env: 2; dest: 3;");

    
    assertSafeMoveDegenerate("src: 1; env: x; dest: 3;");
    assertSafeMoveDegenerate("src: x; env: 1; dest: 3;");

    
    assertSafeMoveDegenerate("src: 1; env: x++; dest: 3;");

    assertSafeMoveDegenerate("src: x++; env: 1; dest: 3;");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilitySafeMoves
  public void testVisibilitySafeMoves() {
    
    assertSafeMoveVisibility("src: 1; env: ; dest: 3;");

    
    assertSafeMoveVisibility("src: 1; env: 2; dest: 3;");

    
    assertSafeMoveVisibility("var x; src: 1; env: x; dest: 3;");
    assertSafeMoveVisibility("var x; src: x; env: 1; dest: 3;");

    
    assertSafeMoveVisibility("var x; src: 1; env: x++; dest: 3;");
    assertSafeMoveVisibility("var x; src: x++; env: 1; dest: 3;");

    
    assertSafeMoveVisibility(
        "var x;" +
        "function f(){" +
          "var y;" +
          "src: x;" +
          "env: y++;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "var x;" +
        "function f(){" +
          "var y;" +
          "src: x++;" +
          "env: y;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "var x;" +
        "var y;" +
        "function f(){" +
          "var y;" +
          "src: x;" +
          "env: y++;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "var x;" +
        "var y;" +
        "function f(){" +
          "var y;" +
          "src: x++;" +
          "env: y;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "function f(){" +
          "var x;" +
          "var y;" +
          "src: x;" +
          "env: y++;" +
          "dest: 3;" +
          "function inner() {" +
            "x" +
          "}" +
         "}");

    
    assertSafeMoveVisibility(
        "function f(){" +
          "var x;" +
          "var y;" +
          "src: x++;" +
          "env: y;" +
          "dest: 3;" +
          "function inner() {" +
            "x" +
          "}" +
        "}");

    
    assertSafeMoveVisibility(
        "var x = {};" +
        "function f(){" +
          "var y;" +
          "src: x.a;" +
          "env: y++;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "var x = {};" +
        "function f(){" +
          "var y;" +
          "src: x.a++;" +
          "env: y;" +
          "dest: 3;" +
          "}");

    
    assertSafeMoveVisibility(
        "var x = {};" +
        "src: x.a;" +
        "env: (function() {" +
          "x.a++;" +
        "});" +
        "dest: 3;");

    
    assertSafeMoveVisibility(
        "var x = {};" +
        "src: x.a++;" +
        "env: (function() {" +
          "x.a;" +
        "});" +
        "dest: 3;");

  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testDegenerateUnsafeMoves
  public void testDegenerateUnsafeMoves() {

    
    assertUnsafeMoveDegenerate("src: x++; env: foo(y); dest: 3;");

    
    assertUnsafeMoveDegenerate("src: foo(y); env: x++; dest: 3;");

    
    assertUnsafeMoveDegenerate("src: x = 7; env: y = 3; dest:3;");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilityUnsafeMoves
  public void testVisibilityUnsafeMoves() {

    
    assertUnsafeMoveVisibility("var x,y; src: x++; env: y; dest: 3;");

    
    assertUnsafeMoveVisibility("function f() {" +
        "var x,y; src: x++; env: y; dest: 3;" +
        "}");

    
    assertUnsafeMoveVisibility(
        "function f() {" +
          "var x,y; src: x++; env: y; dest: 3;" +
          "function inner() {" +
            "x; y;" +
          "}" +
         "}");

    
    assertUnsafeMoveVisibility("var x,y; src: x.a++; env: y.b; dest: 3;");

    
    assertUnsafeMoveVisibility("var x,y; src: y; env: x++; dest: 3;");

    
    assertUnsafeMoveVisibility("function f() {" +
        "var x,y; src: x; env: y++; dest: 3;" +
        "}");

    
    assertUnsafeMoveVisibility(
        "function f() {" +
          "var x,y; src: x; env: y++; dest: 3;" +
          "function inner() {" +
            "x; y;" +
          "}" +
         "}");

    
    assertUnsafeMoveVisibility("var x,y; src: x.a; env: y.b++; dest: 3;");

    
    assertUnsafeMoveVisibility("var x,y; src: x = 7; env: y = 3; dest: 3;");

    
    assertUnsafeMoveVisibility("function f() {" +
        "var x,y; src: x = 7; env: y = 3; dest: 3;" +
        "}");

    
    assertUnsafeMoveVisibility(
        "function f() {" +
          "var x,y; src: x = 7; env: y = 3; dest: 3;" +
          "function inner() {" +
            "x; y;" +
          "}" +
         "}");

    
    assertUnsafeMoveVisibility("var x,y; src: x.a = 7; env: y.b = 3; dest: 3;");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilityMoveCalls
  public void testVisibilityMoveCalls() {
    
    
    

    

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "var g = function(){};" +
        "function f(){" +
          "var y;" +
          "src: g();" +
          "env: x;" +
          "dest: 3;" +
          "}");

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "var g = function(){};" +
        "function f(){" +
          "var y;" +
          "src: x;" +
          "env: g();" +
          "dest: 3;" +
          "}");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testVisibilityMergesParametersWithHeap
  public void testVisibilityMergesParametersWithHeap() {
    
    
    

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: x[0]++;" +
          "env: y;" +
          "dest: 3;" +
          "}");

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: x[0];" +
          "env: y++;" +
          "dest: 3;" +
          "}");

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: arguments[0]++;" +
          "env: y;" +
          "dest: 3;" +
          "}");

    
    assertUnsafeMoveVisibility(
        "var x = {};" +
        "function f(y){" +
          "src: arguments[0];" +
          "env: y++;" +
          "dest: 3;" +
          "}");
  }

// com.google.javascript.jscomp.SideEffectsAnalysisTest::testMovedSideEffectsMustHaveSameControlFlow
  public void testMovedSideEffectsMustHaveSameControlFlow() {

    
    assertSafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "if (l) {" +
            "src: a++;" +
            "env: 3;" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "if (l) {" +
            "src: a++;" +
            "env: 3;" +
          "}" +
          "if (l) {" +
          "dest: 3;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "if (l) {" +
            "src: a++;" +
            "env: 3;" +
          "} else {" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertSafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: 3;" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: l;" +
            "break;" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: 3;" +
            "continue;" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "while (l) {" +
            "src: a++;" +
            "env: 3;" +
            "return;" +
            "dest: 3;" +
          "}" +
        "}"
    );

    
    assertSafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "do {" +
            "src: a++;" +
            "env: 3;" +
            "dest: 3;" +
          "} while(l)" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "do {" +
            "src: a++;" +
            "env: 3;" +
          "} while(l)" +
          "dest: 3;" +
        "}"
    );

    
    
    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "switch(l) {" +
            "case 17:" +
              "src: a++;" +
              "env: 3;" +
              "dest: 3;" +
            "break;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "var l;" +
          "switch(l) {" +
            "case 17:" +
              "src: a++;" +
              "env: 3;" +
            "break;" +
            "case 18:" +
              "dest: 3;" +
            "break;" +
          "}" +
        "}"
    );

    
    assertUnsafeMoveVisibility(
        "var a;" +
        "function f() {" +
          "src: a++;" +
          "env: 3;" +
        "}" +
        "function g() {" +
          "dest: 3;" +
        "}"
    );
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineNumber
  public void testDefineNumber() throws Exception {
    checkDefinitionsInJs(
        "var a = 1",
        ImmutableSet.of("DEF NAME a -> NUMBER"));

    checkDefinitionsInJs(
        "a = 1",
        ImmutableSet.of("DEF NAME a -> NUMBER"));

    checkDefinitionsInJs(
        "a.b = 1",
        ImmutableSet.of("DEF GETPROP a.b -> NUMBER"));

    
    checkDefinitionsInJs(
        "a[\"b\"] = 1",
        ImmutableSet.<String>of());

    checkDefinitionsInJs(
        "f().b = 1",
        ImmutableSet.of("DEF GETPROP null -> NUMBER"));

    checkDefinitionsInJs(
        "({a : 1}); o.a",
        ImmutableSet.of("DEF STRING null -> NUMBER",
                        "USE GETPROP o.a -> [NUMBER]"));

    
    checkDefinitionsInJs(
      "({'a' : 1}); o['a']",
      ImmutableSet.<String>of("DEF STRING null -> NUMBER"));

    checkDefinitionsInJs(
      "({1 : 1}); o[1]",
      ImmutableSet.<String>of("DEF STRING null -> NUMBER"));

    checkDefinitionsInJs(
        "var a = {b : 1}; a.b",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "DEF STRING null -> NUMBER",
                        "USE NAME a -> [<null>]",
                        "USE GETPROP a.b -> [NUMBER]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineGet
  public void testDefineGet() throws Exception {
    
    checkDefinitionsInJs(
      "({get a() {}}); o.a",
      ImmutableSet.of("DEF GET null -> FUNCTION",
                      "USE GETPROP o.a -> [FUNCTION]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineSet
  public void testDefineSet() throws Exception {
    
    checkDefinitionsInJs(
      "({set a(b) {}}); o.a",
      ImmutableSet.of("DEF NAME b -> <null>",
                      "DEF SET null -> FUNCTION",
                      "USE GETPROP o.a -> [FUNCTION]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineFunction
  public void testDefineFunction() throws Exception {
    checkDefinitionsInJs(
        "var a = function(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "var a = function f(){}",
        ImmutableSet.of("DEF NAME f -> FUNCTION", "DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "function a(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "a = function(){}",
        ImmutableSet.of("DEF NAME a -> FUNCTION"));

    checkDefinitionsInJs(
        "a.b = function(){}",
        ImmutableSet.of("DEF GETPROP a.b -> FUNCTION"));

    
    checkDefinitionsInJs(
        "a[\"b\"] = function(){}",
        ImmutableSet.<String>of());

    checkDefinitionsInJs(
        "f().b = function(){}",
        ImmutableSet.of("DEF GETPROP null -> FUNCTION"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testFunctionArgumentsBasic
  public void testFunctionArgumentsBasic() throws Exception {
    checkDefinitionsInJs(
        "function f(a){return a}",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "USE NAME a -> [<null>]",
                        "DEF NAME f -> FUNCTION"));

    checkDefinitionsInJs(
        "var a = 1; function f(a){return a}",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME a -> <null>",
                        "USE NAME a -> [<null>, NUMBER]",
                        "DEF NAME f -> FUNCTION"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testFunctionArgumentsInExterns
  public void testFunctionArgumentsInExterns() throws Exception {
    final String DEF = "var f = function(arg1, arg2){}";
    final String USE = "f(1, 2)";

    
    checkDefinitionsInJs(
        DEF + ";" + USE,
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "DEF NAME arg1 -> <null>",
                        "DEF NAME arg2 -> <null>",
                        "USE NAME f -> [FUNCTION]"));

    
    checkDefinitions(
        DEF, USE,
        ImmutableSet.of("DEF NAME f -> EXTERN FUNCTION",
                        "USE NAME f -> [EXTERN FUNCTION]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testMultipleDefinition
  public void testMultipleDefinition() throws Exception {
    checkDefinitionsInJs(
        "a = 1; a = 2; a",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "USE NAME a -> [NUMBER x 2]"));

    checkDefinitionsInJs(
        "a = 1; a = 'a'; a",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME a -> STRING",
                        "USE NAME a -> [NUMBER, STRING]"));

    checkDefinitionsInJs(
        "a = 1; b = 2; a = b; a",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "DEF NAME a -> NUMBER",
                        "DEF NAME b -> NUMBER",
                        "USE NAME a -> [<null>, NUMBER]",
                        "USE NAME b -> [NUMBER]"));

    checkDefinitionsInJs(
        "a = 1; b = 2; c = b; c = a; c",
        ImmutableSet.of("DEF NAME a -> NUMBER",
                        "DEF NAME b -> NUMBER",
                        "DEF NAME c -> <null>",
                        "USE NAME a -> [NUMBER]",
                        "USE NAME b -> [NUMBER]",
                        "USE NAME c -> [<null> x 2]"));

    checkDefinitionsInJs(
        "function f(){} f()",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.call(null)",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.call -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.apply(null, [])",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.apply -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f.foobar()",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]"));

    checkDefinitionsInJs(
        "function f(){} f(); f.call(null)",
        ImmutableSet.of("DEF NAME f -> FUNCTION",
                        "USE NAME f -> [FUNCTION]",
                        "USE GETPROP f.call -> [FUNCTION]"));

  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefinitionInExterns
  public void testDefinitionInExterns() throws Exception {
    String externs = "var a = 1";

    checkDefinitionsInExterns(
        externs,
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER"));

    checkDefinitions(
        externs,
        "var b = 1",
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER", "DEF NAME b -> NUMBER"));

    checkDefinitions(
        externs,
        "a = \"foo\"; a",
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER",
                        "DEF NAME a -> STRING",
                        "USE NAME a -> [EXTERN NUMBER, STRING]"));

    checkDefinitionsInExterns(
        "var a = {}; a.b = 10",
        ImmutableSet.of("DEF GETPROP a.b -> EXTERN NUMBER",
                        "DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitionsInExterns(
        "var a = {}; a.b",
        ImmutableSet.of("DEF GETPROP a.b -> EXTERN <null>",
                        "DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitions(
        "var a = {}",
        "a.b = 1",
        ImmutableSet.of("DEF GETPROP a.b -> NUMBER",
                        "DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitions(
        "var a = {}",
        "a.b",
        ImmutableSet.of("DEF NAME a -> EXTERN <null>",
                        "USE NAME a -> [EXTERN <null>]"));

    checkDefinitionsInExterns(
        externs,
        ImmutableSet.of("DEF NAME a -> EXTERN NUMBER"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testObjectLitInExterns
  public void testObjectLitInExterns() {
    checkDefinitions(
        "var goog = {};" +
        " goog.HYBRID;" +
        " goog.Enum = {HYBRID: 0, ROADMAP: 1};",
        "goog.HYBRID; goog.Enum.ROADMAP;",
        ImmutableSet.of(
            "DEF GETPROP goog.Enum -> EXTERN <null>",
            "DEF GETPROP goog.HYBRID -> EXTERN <null>",
            "DEF NAME goog -> EXTERN <null>",
            "DEF STRING null -> EXTERN NUMBER",
            "USE GETPROP goog.Enum -> [EXTERN <null>]",
            "USE GETPROP goog.Enum.ROADMAP -> [EXTERN NUMBER]",
            "USE GETPROP goog.HYBRID -> [EXTERN <null>, EXTERN NUMBER]",
            "USE NAME goog -> [EXTERN <null>]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testCallInExterns
  public void testCallInExterns() {
    checkDefinitionsInExterns(
        "var goog = {};" +
        " goog.Response = function() {};" +
        "goog.Response.prototype.get;" +
        "goog.Response.prototype.get().get;",
        ImmutableSet.of(
            "DEF GETPROP goog.Response -> EXTERN FUNCTION",
            "DEF GETPROP goog.Response.prototype.get -> EXTERN <null>",
            "DEF GETPROP null -> EXTERN <null>",
            "DEF NAME goog -> EXTERN <null>",
            "USE GETPROP goog.Response -> [EXTERN FUNCTION]",
            "USE GETPROP goog.Response.prototype.get -> [EXTERN <null> x 2]",
            "USE NAME goog -> [EXTERN <null>]"));
  }

// com.google.javascript.jscomp.SimpleFunctionAliasAnalysisTest::testFunctionGetIsAliased
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

    compileAndRun(source);

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

    compileAndRun(source);

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

    compileAndRun(source);

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

    compileAndRun(source);

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

    compileAndRun(source);

    assertFunctionAliased(false, "A");
    assertFunctionAliased(false, "ns.B");
    assertFunctionAliased(false, "C");
  }

// com.google.javascript.jscomp.SimpleFunctionAliasAnalysisTest::testFunctionGetIsExposedToCallOrApply
  public void testFunctionGetIsExposedToCallOrApply() {
    
    String source =
        "function A(){};\n" +
        "function B(){};\n" +
        "function C(){};\n" +
        "var x;\n" +
        "A.call(x);\n" +
        "B.apply(x);\n" +
        "C();\n";

    compileAndRun(source);

    assertFunctionExposedToCallOrApply(true, "A");
    assertFunctionExposedToCallOrApply(true, "B");
    assertFunctionExposedToCallOrApply(false, "C");

    source =
      "var ns = {};" +
      "ns.A = function(){};\n" +
      "ns.B = function(){};\n" +
      "ns.C = function(){};\n" +
      "var x;\n" +
      "ns.A.call(x);\n" +
      "ns.B.apply(x);\n" +
      "ns.C();\n";

    compileAndRun(source);

    assertFunctionExposedToCallOrApply(true, "ns.A");
    assertFunctionExposedToCallOrApply(true, "ns.B");
    assertFunctionExposedToCallOrApply(false, "ns.C");
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInline
  public void testSpecializeInline() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return 6};" +
        "A();",
        
        "A();" +
        "B();" +
        "B = function() {return 7};" +
        "A();" +
        "B();"
        );

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "A();" +
        "B();" +
        "B = function() {return 7};" +
        "A();" +
        "B();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeCascadedInline
  public void testSpecializeCascadedInline() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return C()};" +
        "var C = function() {return 6};" +
        "A();",
        
        "B = function() {return 7};" +
    "A();");

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B, C;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return C()};" + 
        "C = function() {return 6};" + 
        "B = function() {return 7};" +
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithMultipleDependents
  public void testSpecializeInlineWithMultipleDependents() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B());A()};" +
        "var B = function() {return 6};" +
        "A();",
        
        "B = function() {return 7};" +
        "A();",
        
        "A();"
    );

    test(modules, new String[] {
        
        "var A = function() {alert(6);A()};" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "B = function() {return 7};" +
        "A();",
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        "A();",

    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithNamespaces
  public void testSpecializeInlineWithNamespaces() {
    JSModule[] modules = createModuleStar(
        
        "var ns = {};" +
        
        "ns.A = function() {alert(B());ns.A()};" +
        "var B = function() {return 6};" +
        "ns.A();",
        
        "B = function() {return 7};" +
    "ns.A();");

    test(modules, new String[] {
        
        "var ns = {};" +
        "ns.A = function() {alert(6);ns.A()};" + 
        "ns.A();" +
        "var B;",
        
        "ns.A = function() {alert(B());ns.A()};" + 
        "B = function() {return 6};" + 
        "B = function() {return 7};" +
        "ns.A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeInlineWithRegularFunctions
  public void testSpecializeInlineWithRegularFunctions() {
    JSModule[] modules = createModuleStar(
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "A();",
        
        "B = function() {return 7};" +
    "A();");

    test(modules, new String[] {
        
        "function A() {alert(6);A()}" + 
        "A();" +
        "var B;",
        
        "A = function() {alert(B());A()};" + 
        "B = function() {return 6};" + 
        
        "B = function() {return 7};" +
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeLocalNonAnonymousFunctions
  public void testDontSpecializeLocalNonAnonymousFunctions() {
    
    enableNormalize(false);

    JSModule[] modules = createModuleStar(
        
        "(function(){var noSpecialize = " +
            "function() {alert(6)};noSpecialize()})()",
        
        "");

    test(modules, new String[] {
        
        "(function(){var noSpecialize = " +
            "function() {alert(6)};noSpecialize()})()",
        
        ""
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testAddDummyVarsForRemovedFunctions
  public void testAddDummyVarsForRemovedFunctions() {
    JSModule[] modules = createModuleStar(
        
        
        "var A = function() {alert(B() + C());A()};" +
        "var B = function() {return 6};" +
        "var C = function() {return 8};" +
        "A();",
        
        "" +
    "A();");

    test(modules, new String[] {
        
        "var A = function() {alert(6 + 8);A()};" + 
        "A();" +
        "var B, C;",
        
        "A = function() {alert(B() + C());A()};" + 
        "B = function() {return 6};" + 
        "C = function() {return 8};" + 
        "A();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeRemoveUnusedProperties
  public void testSpecializeRemoveUnusedProperties() {
    JSModule[] modules = createModuleStar(
        
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "Foo.prototype.c = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "var aliasA = Foo.prototype.a;" +
        "var x = new Foo();" +
        "x.a();",
        
        "Foo.prototype.b = function() {return 6};" +
        "Foo.prototype.c = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeAliasedFunctions_inline
  public void testDontSpecializeAliasedFunctions_inline() {
    JSModule[] modules = createModuleStar(
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "var aliasA = A;" +
        "A();",
        
        "B = function() {return 7};" +
        "B();");

    test(modules, new String[] {
        
        
        "function A() {alert(B());A()}" +
        "function B() {return 6}" +
        "var aliasA = A;" +
        "A();",
        
        "B = function() {return 7};" +
        "B();"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testDontSpecializeAliasedFunctions_remove_unused_properties
  public void testDontSpecializeAliasedFunctions_remove_unused_properties() {
    JSModule[] modules = createModuleStar(
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "var aliasB = Foo.prototype.b;" +
        "Foo.prototype.c = function() {return 7};" +
        "Foo.prototype.d = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();" +
        "var aliasC = (new Foo).c",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a()};" +
        "Foo.prototype.b = function() {return 6};" +
        "var aliasB = Foo.prototype.b;" +
        "Foo.prototype.c = function() {return 7};" +
        "var aliasA = Foo.prototype.a;" + 
        "var x = new Foo();" +
        "x.a();" +
        "var aliasC = (new Foo).c",
        
        "Foo.prototype.d = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeDevirtualizePrototypeMethods
  public void testSpecializeDevirtualizePrototypeMethods() {
    JSModule[] modules = createModuleStar(
        
        "" +
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {this.a();return 7};" +
        "Foo.prototype.b = function() {this.a()};" +
        "var x = new Foo();" +
        "x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "var JSCompiler_StaticMethods_a =" +
              "function(JSCompiler_StaticMethods_a$self) {" +
           "JSCompiler_StaticMethods_a(JSCompiler_StaticMethods_a$self);" +
           "return 7" +
        "};" +
        "var x = new Foo();" +
        "JSCompiler_StaticMethods_a(x);",
        
        "Foo.prototype.a = function() {this.a();return 7};" +
        "Foo.prototype.b = function() {this.a()};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializeDevirtualizePrototypeMethodsWithInline
  public void testSpecializeDevirtualizePrototypeMethodsWithInline() {
    JSModule[] modules = createModuleStar(
        
        "" +
        "var Foo = function(){};" + 
        "Foo.prototype.a = function() {return 7};" +
        "var x = new Foo();" +
        "var z = x.a();",
        
        "");

    test(modules, new String[] {
        
        "var Foo = function(){};" + 
        "var x = new Foo();" +
        "var z = 7;",
        
        "Foo.prototype.a = function() {return 7};"
    });
  }

// com.google.javascript.jscomp.SpecializeModuleTest::testRemovedFunctions
    public void testRemovedFunctions() {
      testSame("function F(){}\nvar G = function(a){};");

      assertEquals(ImmutableSet.of(), lastState.getRemovedFunctions());

      Node functionF = findFunction("F");

      lastState.reportRemovedFunction(functionF, functionF.getParent());
      assertEquals(ImmutableSet.of(functionF), lastState.getRemovedFunctions());

      Node functionG = findFunction("F");

      lastState.reportRemovedFunction(functionG, functionF.getParent());
      assertEquals(ImmutableSet.of(functionF, functionG),
          lastState.getRemovedFunctions());

      assertEquals(ImmutableSet.of(), lastState.getSpecializedFunctions());
    }

// com.google.javascript.jscomp.SpecializeModuleTest::testSpecializedFunctions
    public void testSpecializedFunctions() {
      testSame("function F(){}\nvar G = function(a){};");

      assertEquals(ImmutableSet.of(), lastState.getSpecializedFunctions());

      Node functionF = findFunction("F");

      lastState.reportSpecializedFunction(functionF);
      assertEquals(ImmutableSet.of(functionF),
          lastState.getSpecializedFunctions());

      Node functionG = findFunction("F");

      lastState.reportSpecializedFunction(functionG);
      assertEquals(ImmutableSet.of(functionF, functionG),
          lastState.getSpecializedFunctions());

      assertEquals(ImmutableSet.of(), lastState.getRemovedFunctions());
    }

// com.google.javascript.jscomp.SpecializeModuleTest::testCanFixupFunction
    public void testCanFixupFunction() {
      testSame("function F(){}\n" +
               "var G = function(a){};\n" +
               "var ns = {};" +
               "ns.H = function(){};" +
               "var ns2 = {I : function anon1(){}};" +
               "(function anon2(){})();");

      assertTrue(lastState.canFixupFunction(findFunction("F")));
      assertTrue(lastState.canFixupFunction(findFunction("G")));
      assertTrue(lastState.canFixupFunction(findFunction("ns.H")));
      assertFalse(lastState.canFixupFunction(findFunction("anon1")));
      assertFalse(lastState.canFixupFunction(findFunction("anon2")));

      
      testSame("function A(){}\n" +
          "var aliasA = A;\n");

      assertFalse(lastState.canFixupFunction(findFunction("A")));
    }

// com.google.javascript.jscomp.StatementFusionTest::testNothingToDo
  public void testNothingToDo() {
    fuseSame("");
    fuseSame("a");
    fuseSame("a()");
    fuseSame("if(a()){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFoldBlockWithStatements
  public void testFoldBlockWithStatements() {
    fuse("a;b;c", "a,b,c");
    fuse("a();b();c();", "a(),b(),c()");
    fuse("a(),b();c(),d()", "a(),b(),c(),d()");
    fuse("a();b(),c(),d()", "a(),b(),c(),d()");
    fuse("a(),b(),c();d()", "a(),b(),c(),d()");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFoldBlockIntoIf
  public void testFoldBlockIntoIf() {
    fuse("a;b;c;if(x){}", "if(a,b,c,x){}");
    fuse("a;b;c;if(x,y){}else{}", "if(a,b,c,x,y){}else{}");
    fuse("a;b;c;if(x,y){}", "if(a,b,c,x,y){}");
    fuse("a;b;c;if(x,y,z){}", "if(a,b,c,x,y,z){}");

    
    fuseSame("a();if(a()){}a()");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFoldBlockReturn
  public void testFoldBlockReturn() {
    fuse("a;b;c;return x", "return a,b,c,x");
    fuse("a;b;c;return x+y", "return a,b,c,x+y");

    
    fuseSame("a;b;c;return x;a;b;c");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFoldBlockThrow
  public void testFoldBlockThrow() {
    fuse("a;b;c;throw x", "throw a,b,c,x");
    fuse("a;b;c;throw x+y", "throw a,b,c,x+y");
    fuseSame("a;b;c;throw x;a;b;c");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFoldSwitch
  public void testFoldSwitch() {
    fuse("a;b;c;switch(x){}", "switch(a,b,c,x){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testFuseIntoForIn
  public void testFuseIntoForIn() {
    fuse("a;b;c;for(x in y){}", "for(x in a,b,c,y){}");
    fuseSame("a();for(var x = b() in y){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testNoFuseIntoWhile
  public void testNoFuseIntoWhile() {
    fuseSame("a;b;c;while(x){}");
  }

// com.google.javascript.jscomp.StatementFusionTest::testNoFuseIntoDo
  public void testNoFuseIntoDo() {
    fuseSame("a;b;c;do{}while(x)");
  }

// com.google.javascript.jscomp.StatementFusionTest::testNoGlobalSchopeChanges
  public void testNoGlobalSchopeChanges() {
    testSame("a,b,c");
  }

// com.google.javascript.jscomp.StatementFusionTest::testNoFunctionBlockChanges
  public void testNoFunctionBlockChanges() {
    testSame("function foo() { a,b,c }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testWith
  public void testWith() {
    test("var a; function foo(obj) { with (obj) { a = 3; }}", null,
         StrictModeCheck.WITH_DISALLOWED);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval
  public void testEval() {
    test("function foo() { eval('a'); }", null,
         StrictModeCheck.EVAL_USE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval2
  public void testEval2() {
    test("function foo(eval) {}", null,
         StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval3
  public void testEval3() {
    testSame("function foo() {} foo.eval = 3;");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval4
  public void testEval4() {
    test("function foo() { var eval = 3; }", null,
         StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval5
  public void testEval5() {
    test("function eval() {}", null, StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval6
  public void testEval6() {
    test("try {} catch (eval) {}", null, StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval7
  public void testEval7() {
    testSame("var o = {eval: 3};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval8
  public void testEval8() {
    testSame("var a; eval: while (true) { a = 3; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable
  public void testUnknownVariable() {
    test("function foo(a) { a = b; }", null, StrictModeCheck.UNKNOWN_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable2
  public void testUnknownVariable2() {
    test("a: while (true) { a = 3; }", null, StrictModeCheck.UNKNOWN_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable3
  public void testUnknownVariable3() {
    testSame("try {} catch (ex) { ex = 3; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments
  public void testArguments() {
    test("function foo(arguments) {}", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments2
  public void testArguments2() {
    test("function foo() { var arguments = 3; }", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments3
  public void testArguments3() {
    test("function arguments() {}", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments4
  public void testArguments4() {
    test("try {} catch (arguments) {}", null,
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments5
  public void testArguments5() {
    testSame("var o = {arguments: 3};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEvalAssignment
  public void testEvalAssignment() {
    noCajaChecks = true;
    test("function foo() { eval = []; }", null,
         StrictModeCheck.EVAL_ASSIGNMENT);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEvalAssignment2
  public void testEvalAssignment2() {
    test("function foo() { eval = []; }", null, StrictModeCheck.EVAL_USE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testAssignToArguments
  public void testAssignToArguments() {
    test("function foo() { arguments = []; }", null,
         StrictModeCheck.ARGUMENTS_ASSIGNMENT);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteVar
  public void testDeleteVar() {
    test("var a; delete a", null, StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteFunction
  public void testDeleteFunction() {
    test("function a() {} delete a", null, StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteArgument
  public void testDeleteArgument() {
    test("function b(a) { delete a; }", null, StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteProperty
  public void testDeleteProperty() {
    testSame("function f(obj) { delete obj.a; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName
  public void testIllegalName() {
    test("var a__ = 3;", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName2
  public void testIllegalName2() {
    test("function a__() {}", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName3
  public void testIllegalName3() {
    test("function f(a__) {}", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName4
  public void testIllegalName4() {
    test("try {} catch (a__) {}", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName5
  public void testIllegalName5() {
    noVarCheck = true;
    test("var a = b__;", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName6
  public void testIllegalName6() {
    test("function f(obj) { return obj.a__; }", null,
         StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName7
  public void testIllegalName7() {
    noCajaChecks = true;
    testSame("var a__ = 3;");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName8
  public void testIllegalName8() {
    test("var o = {a__: 3};", null, StrictModeCheck.ILLEGAL_NAME);
    test("var o = {b: 3, a__: 4};", null, StrictModeCheck.ILLEGAL_NAME);
    test("var o = {b: 3, get a__() {}};", null, StrictModeCheck.ILLEGAL_NAME);
    test("var o = {b: 3, set a__(c) {}};", null, StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName9
  public void testIllegalName9() {
    test("a__: while (true) { var b = 3; }", null,
         StrictModeCheck.ILLEGAL_NAME);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testIllegalName10
  public void testIllegalName10() {
    
    testSame("var o = {1: 3, 2: 4};");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInConstructor
  public void testLoggerDefinedInConstructor() {
    test("a.b.c = function() {" +
         "  this.logger = goog.debug.Logger.getLogger('a.b.c');" +
         "};",
         "a.b.c=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype1
  public void testLoggerDefinedInPrototype1() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype.logger = goog.debug.Logger.getLogger('a.b.c');",
         "a.b.c=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype2
  public void testLoggerDefinedInPrototype2() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype = {logger: goog.debug.Logger.getLogger('a.b.c')}",
         "a.b.c = function() {};" +
         "a.b.c.prototype = {}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype3
  public void testLoggerDefinedInPrototype3() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype = { " +
         "  get logger() {return goog.debug.Logger.getLogger('a.b.c')}" +
         "}",
         "a.b.c = function() {};" +
         "a.b.c.prototype = {}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype4
  public void testLoggerDefinedInPrototype4() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype = { " +
         "  set logger(a) {this.x = goog.debug.Logger.getLogger('a.b.c')}" +
         "}",
         "a.b.c = function() {};" +
         "a.b.c.prototype = {}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototype5
  public void testLoggerDefinedInPrototype5() {
    test("a.b.c = function() {};" +
         "a.b.c.prototype = { " +
         "  get f() {return this.x;}," +
         "  set f(a) {this.x = goog.debug.Logger.getLogger('a.b.c')}" +
         "}",
         "a.b.c = function() {};" +
         "a.b.c.prototype = { " +
         "  get f() {return this.x;}," +
         "  set f(a) {this.x = null}" +
         "}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedStatically
  public void testLoggerDefinedStatically() {
    test("a.b.c = function() {};" +
         "a.b.c.logger = goog.debug.Logger.getLogger('a.b.c');",
         "a.b.c=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInObjectLiteral1
  public void testLoggerDefinedInObjectLiteral1() {
    test("a.b.c = {" +
         "  x: 0," +
         "  logger: goog.debug.Logger.getLogger('a.b.c')" +
         "};",
         "a.b.c={x:0}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInObjectLiteral2
  public void testLoggerDefinedInObjectLiteral2() {
    test("a.b.c = {" +
         "  x: 0," +
         "  get logger() {return goog.debug.Logger.getLogger('a.b.c')}" +
         "};",
         "a.b.c={x:0}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInObjectLiteral3
  public void testLoggerDefinedInObjectLiteral3() {
    test("a.b.c = {" +
         "  x: null," +
         "  get logger() {return this.x}," +
         "  set logger(a) {this.x  = goog.debug.Logger.getLogger(a)}" +
         "};",
         "a.b.c={x:null}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInObjectLiteral4
  public void testLoggerDefinedInObjectLiteral4() {
    test("a.b.c = {" +
         "  x: null," +
         "  get y() {return this.x}," +
         "  set y(a) {this.x  = goog.debug.Logger.getLogger(a)}" +
         "};",
         "a.b.c = {" +
         "  x: null," +
         "  get y() {return this.x}," +
         "  set y(a) {this.x  = null}" +
         "};");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedInPrototypeAndUsedInConstructor
  public void testLoggerDefinedInPrototypeAndUsedInConstructor() {
    test("a.b.c = function(level) {" +
         "  if (!this.logger.isLoggable(level)) {" +
         "    this.logger.setLevel(level);" +
         "  }" +
         "  this.logger.log(level, 'hi');" +
         "};" +
         "a.b.c.prototype.logger = goog.debug.Logger.getLogger('a.b.c');" +
         "a.b.c.prototype.go = function() { this.logger.finer('x'); };",
         "a.b.c=function(level){if(!null);};" +
         "a.b.c.prototype.go=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerDefinedStaticallyAndUsedInConstructor
  public void testLoggerDefinedStaticallyAndUsedInConstructor() {
    test("a.b.c = function(level) {" +
         "  if (!a.b.c.logger.isLoggable(level)) {" +
         "    a.b.c.logger.setLevel(level);" +
         "  }" +
         "  a.b.c.logger.log(level, 'hi');" +
         "};" +
         "a.b.c.logger = goog.debug.Logger.getLogger('a.b.c');",
         "a.b.c=function(level){if(!null);}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerVarDeclaration
  public void testLoggerVarDeclaration() {
    test("var logger = opt_logger || goog.debug.LogManager.getRoot();", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerMethodCallByVariableType
  public void testLoggerMethodCallByVariableType() {
    test("var x = goog.debug.Logger.getLogger('a.b.c'); y.info(a); x.info(a);",
         "y.info(a)");
  }

// com.google.javascript.jscomp.StripCodeTest::testSubPropertyAccessByVariableName
  public void testSubPropertyAccessByVariableName() {
    test("var x, y = goog.debug.Logger.getLogger('a.b.c');" +
         "var logger = x;" +
         "var curlevel = logger.level_ ? logger.getLevel().name : 3;",
         "var x;var curlevel=null?null:3");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrefixedVariableName
  public void testPrefixedVariableName() {
    test("this.blcLogger_ = goog.debug.Logger.getLogger('a.b.c');" +
         "this.blcLogger_.fine('Raised dirty states.');", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrefixedPropertyName
  public void testPrefixedPropertyName() {
    test("a.b.c.staticLogger_ = goog.debug.Logger.getLogger('a.b.c');" +
         "a.b.c.staticLogger_.fine('-' + a.b.c.d_())", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrefixedClassName
  public void testPrefixedClassName() {
    test("a.b.MyLogger = function(logger) {" +
         "  this.logger_ = logger;" +
         "};" +
         "a.b.MyLogger.prototype.shout = function(msg, opt_x) {" +
         "  this.logger_.log(goog.debug.Logger.Level.SHOUT, msg, opt_x);" +
         "};",
         "a.b.MyLogger=function(logger){};" +
         "a.b.MyLogger.prototype.shout=function(msg,opt_x){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testLoggerClassDefinition
  public void testLoggerClassDefinition() {
    test("goog.debug.Logger=function(name){this.name_=name}", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStaticLoggerPropertyDefinition
  public void testStaticLoggerPropertyDefinition() {
    test("goog.debug.Logger.Level.SHOUT=" +
         "new goog.debug.Logger.Level(x,1200)", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStaticLoggerMethodDefinition
  public void testStaticLoggerMethodDefinition() {
    test("goog.debug.Logger.getLogger=function(name){" +
         "return goog.debug.LogManager.getLogger(name)" +
         "};", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrototypeFieldDefinition
  public void testPrototypeFieldDefinition() {
    test("goog.debug.Logger.prototype.level_=null;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrototypeFieldDefinitionWithoutAssignment
  public void testPrototypeFieldDefinitionWithoutAssignment() {
    test("goog.debug.Logger.prototype.level_;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPrototypeMethodDefinition
  public void testPrototypeMethodDefinition() {
    test("goog.debug.Logger.prototype.addHandler=" +
         "function(handler){this.handlers_.push(handler)};", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPublicPropertyAssignment
  public void testPublicPropertyAssignment() {
    
    
    
    testSame("rootLogger.someProperty=3");
    testSame("this.blcLogger_.level=x");
    testSame("goog.ui.Component.logger.prop=y");
  }

// com.google.javascript.jscomp.StripCodeTest::testGlobalCallWithStrippedType
  public void testGlobalCallWithStrippedType() {
    testSame("window.alert(goog.debug.Logger)");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType1
  public void testClassDefiningCallWithStripType1() {
    test("goog.debug.Logger.inherits(Object)", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType2
  public void testClassDefiningCallWithStripType2() {
    test("goog.formatter=function(){};" +
         "goog.inherits(goog.debug.Formatter,goog.formatter)",
         "goog.formatter=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType3
  public void testClassDefiningCallWithStripType3() {
    test("goog.formatter=function(){};" +
         "goog.inherits(goog.formatter,goog.debug.Formatter)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType4
  public void testClassDefiningCallWithStripType4() {
    test("goog.formatter=function(){};" +
         "goog.formatter.inherits(goog.debug.Formatter)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType5
  public void testClassDefiningCallWithStripType5() {
    testSame("goog.formatter=function(){};" +
             "goog.formatter.inherits(goog.debug.FormatterFoo)");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType6
  public void testClassDefiningCallWithStripType6() {
    test("goog.formatter=function(){};" +
         "goog.formatter.inherits(goog.debug.Formatter.Foo)",
         null, StripCode.STRIP_TYPE_INHERIT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType7
  public void testClassDefiningCallWithStripType7() {
    test("goog.inherits(goog.debug.TextFormatter,goog.debug.Formatter)", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testClassDefiningCallWithStripType8
  public void testClassDefiningCallWithStripType8() {
    
    test("goog.debug.DebugWindow = function(){}", "");
    test("goog.inherits(goog.debug.DebugWindow,Base)", "");

    
    
    testSame("goog.debug.DebugWindowFoo=function(){}");
    testSame("goog.inherits(goog.debug.DebugWindowFoo,Base)");
    testSame("goog.debug.DebugWindowFoo");
    testSame("goog.debug.DebugWindowFoo=1");

    
    test("goog.debug.DebugWindow.Foo=function(){}", "");
    test("goog.inherits(goog.debug.DebugWindow.Foo,Base)", "");
    test("goog.debug.DebugWindow.Foo", "");
    test("goog.debug.DebugWindow.Foo=1", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testPropertyWithEmptyStringKey
  public void testPropertyWithEmptyStringKey() {
    test("goog.format.NUMERIC_SCALES_BINARY_ = {'': 1};",
         "goog.format.NUMERIC_SCALES_BINARY_={\"\":1}");
  }

// com.google.javascript.jscomp.StripCodeTest::testVarinIf
  public void testVarinIf() {
    test("if(x)var logger=null;else foo()", "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testGetElemInIf
  public void testGetElemInIf() {
    test("var logger=null;if(x)logger[f];else foo()", "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testAssignInIf
  public void testAssignInIf() {
    test("var logger=null;if(x)logger=1;else foo()",
         "if(x);else foo()");
  }

// com.google.javascript.jscomp.StripCodeTest::testNamePrefix
  public void testNamePrefix() {
    test("a = function(traceZZZ) {}; a.prototype.traceXXX = {x: 1};" +
         "a.prototype.z = function() { this.traceXXX.f(); };" +
         "var traceYYY = 0;",
         "a=function(traceZZZ){};a.prototype.z=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testTypePrefix
  public void testTypePrefix() {
    test("e.f.TraceXXX = function() {}; " +
         "e.f.TraceXXX.prototype.yyy = 2;", "");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripCallsToStrippedNames
  public void testStripCallsToStrippedNames() {
    test("a = function() { this.logger_ = function(msg){}; };" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
    test("a = function() {};" +
         "a.prototype.logger_ = function(msg) {};" +
         "a.prototype.b = function() { this.logger_('hi'); }",
         "a=function(){};a.prototype.b=function(){}");
  }

// com.google.javascript.jscomp.StripCodeTest::testStripVarsInitializedFromStrippedNames
  public void testStripVarsInitializedFromStrippedNames() {
    test("a = function() { this.logger_ = function() { return 1; }; };" +
         "a.prototype.b = function() { " +
         "  var one = this.logger_(); if (one) foo() }",
          "a=function(){};a.prototype.b=function(){if(null)foo()}");
  }

// com.google.javascript.jscomp.StripCodeTest::testReportErrorOnStripInNestedAssignment
  public void testReportErrorOnStripInNestedAssignment() {
    
    test("(foo.logger_ = 7) + 8",
         "(foo.logger_ = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);

    
    test("(goog.debug.Logger.foo = 7) + 8",
         "(goog.debug.Logger.foo = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);

    
    test("(GA_GoogleDebugger.foo = 7) + 8",
         "(GA_GoogleDebugger.foo = 7) + 8",
         StripCode.STRIP_ASSIGNMENT_ERROR);
  }

// com.google.javascript.jscomp.StripCodeTest::testNewOperatior1
  public void testNewOperatior1() {
    test("function foo() {} foo.bar = new goog.debug.Logger();",
         "function foo() {} foo.bar = null;");
  }
