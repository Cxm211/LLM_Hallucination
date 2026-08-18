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
// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction21
  public void testCanInlineReferenceToFunction21() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(new Date);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction22
  public void testCanInlineReferenceToFunction22() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a} foo(true && new Date);", "foo",
        INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction23
  public void testCanInlineReferenceToFunction23() {
    
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction24
  public void testCanInlineReferenceToFunction24() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction25
  public void testCanInlineReferenceToFunction25() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction26
  public void testCanInlineReferenceToFunction26() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return a+a;}; foo(x++);", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction27
  public void testCanInlineReferenceToFunction27() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return a+a;}; " +
        "function x() { foo(x++); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction28
  public void testCanInlineReferenceToFunction28() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; foo(goo());", "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction29
  public void testCanInlineReferenceToFunction29() {
    helperCanInlineReferenceToFunction(NEW_VARS_IN_GLOBAL_SCOPE,
        "function foo(a){return true;}; foo(goo());", "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction30
  public void testCanInlineReferenceToFunction30() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo(goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction31
  public void testCanInlineReferenceToFunction31() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a) {return true;}; " +
        "function x() {foo.call(this, 1);}",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction32
  public void testCanInlineReferenceToFunction32() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.apply(this, [1]); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction33
  public void testCanInlineReferenceToFunction33() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.bar(this, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction34
  public void testCanInlineReferenceToFunction34() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.call(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction35
  public void testCanInlineReferenceToFunction35() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.apply(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction36
  public void testCanInlineReferenceToFunction36() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { foo.bar(this, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction37
  public void testCanInlineReferenceToFunction37() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction38
  public void testCanInlineReferenceToFunction38() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(null, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction39
  public void testCanInlineReferenceToFunction39() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction40
  public void testCanInlineReferenceToFunction40() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(bar, goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction41
  public void testCanInlineReferenceToFunction41() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), 1); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction42
  public void testCanInlineReferenceToFunction42() {
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() { foo.call(new bar(), goo()); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction43
  public void testCanInlineReferenceToFunction43() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction44
  public void testCanInlineReferenceToFunction44() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return true;}; " +
        "function x() { foo.call(); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction45
  public void testCanInlineReferenceToFunction45() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction46
  public void testCanInlineReferenceToFunction46() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction47
  public void testCanInlineReferenceToFunction47() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){var a; return function() {return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction48
  public void testCanInlineReferenceToFunction48() {
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){var a; return function() {return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction49
  public void testCanInlineReferenceToFunction49() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {var a; return true;}}; foo();",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction50
  public void testCanInlineReferenceToFunction50() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return function() {var a; return true;}}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunction51
  public void testCanInlineReferenceToFunction51() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){function x() {var a; return true;} return x}; foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression1
  public void testCanInlineReferenceToFunctionInExpression1() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression2
  public void testCanInlineReferenceToFunctionInExpression2() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression3
  public void testCanInlineReferenceToFunctionInExpression3() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression4
  public void testCanInlineReferenceToFunctionInExpression4() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression5
  public void testCanInlineReferenceToFunctionInExpression5() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {true?foo(1):1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression5a
 public void testCanInlineReferenceToFunctionInExpression5a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {true?foo(1):1 }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression6
  public void testCanInlineReferenceToFunctionInExpression6() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression7
  public void testCanInlineReferenceToFunctionInExpression7() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {1 && foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression7a
  public void testCanInlineReferenceToFunctionInExpression7a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {1 && foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression8
  public void testCanInlineReferenceToFunctionInExpression8() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression9
  public void testCanInlineReferenceToFunctionInExpression9() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression10
  public void testCanInlineReferenceToFunctionInExpression10() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(a){return true;}; " +
        "function x() {var b; b += 1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression10a
  public void testCanInlineReferenceToFunctionInExpression10a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(a){return true;}; " +
        "function x() {var b; b += 1 + foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression12
  public void testCanInlineReferenceToFunctionInExpression12() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var a,b,c; a = b = c = foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression13
  public void testCanInlineReferenceToFunctionInExpression13() {
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(a){return true;}; " +
        "function x() {var a,b,c; a = b = c = 1 + foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression14
  public void testCanInlineReferenceToFunctionInExpression14() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "var a = {}, b = {}, c;" +
        "a.test = 'a';" +
        "b.test = 'b';" +
        "c = a;" +
        "function foo(){c = b; return 'foo'};" +
        "c.test=foo();",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression14a
  public void testCanInlineReferenceToFunctionInExpression14a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "var a = {}, b = {}, c;" +
        "a.test = 'a';" +
        "b.test = 'b';" +
        "c = a;" +
        "function foo(){c = b; return 'foo'};" +
        "c.test=foo();",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression18
  public void testCanInlineReferenceToFunctionInExpression18() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.YES,
        "function foo(){return _g();}; " +
        "function x() {1 + foo()() }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression19
  public void testCanInlineReferenceToFunctionInExpression19() {
    
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(foo()) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression19a
  public void testCanInlineReferenceToFunctionInExpression19a() {
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(foo()) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression21
  public void testCanInlineReferenceToFunctionInExpression21() {
    
    
    
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression21a
  public void testCanInlineReferenceToFunctionInExpression21a() {
    
    
    
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression22
  public void testCanInlineReferenceToFunctionInExpression22() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo()) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression22a
  public void testCanInlineReferenceToFunctionInExpression22a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo()) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression23
  public void testCanInlineReferenceToFunctionInExpression23() {
    
    helperCanInlineReferenceToFunction(CanInlineResult.NO,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo.call(this)) }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testCanInlineReferenceToFunctionInExpression23a
  public void testCanInlineReferenceToFunctionInExpression23a() {
    
    helperCanInlineReferenceToFunction(
        CanInlineResult.AFTER_DECOMPOSITION,
        "function foo(){return a;}; " +
        "function x() {1 + _g(_a(), foo.call(this)) }",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline1
  public void testInline1() {
    helperInlineReferenceToFunction(
        "function foo(){}; foo();",
        "function foo(){}; void 0",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline2
  public void testInline2() {
    helperInlineReferenceToFunction(
        "function foo(){}; foo();",
        "function foo(){}; {}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline3
  public void testInline3() {
    helperInlineReferenceToFunction(
        "function foo(){return;}; foo();",
        "function foo(){return;}; {}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline4
  public void testInline4() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; foo();",
        "function foo(){return true;}; true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline5
  public void testInline5() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; foo();",
        "function foo(){return true;}; {true;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline6
  public void testInline6() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x=foo();",
        "function foo(){return true;}; var x=true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline7
  public void testInline7() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x=foo();",
        "function foo(){return true;}; var x;" +
            "{x=true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline8
  public void testInline8() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=foo();",
        "function foo(){return true;}; var x; x=true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline9
  public void testInline9() {
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=foo();",
        "function foo(){return true;}; var x;{x=true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline10
  public void testInline10() {
    
    helperInlineReferenceToFunction(
        "function foo(){return true;}; var x; x=x+foo();",
        "function foo(){return true;}; var x; x=x+true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline11
  public void testInline11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(x);",
        "function foo(a){return true;}; true;",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline12
  public void testInline12() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(x);",
        "function foo(a){return true;}; {true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline13
  public void testInline13() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "function foo(a){return a;}; " +
        "function x() {{var a$$inline_1=x++;" +
            "a$$inline_1}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline14
  public void testInline14() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(x++);",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_1=x++;" +
            " a$$inline_1+" +
            "a$$inline_1;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline15
  public void testInline15() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(new Date());",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_1=new Date();" +
            " a$$inline_1+" +
            "a$$inline_1;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline16
  public void testInline16() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return a+a;}; foo(function(){});",
        "function foo(a){return a+a;}; " +
            "{var a$$inline_1=function(){};" +
            " a$$inline_1+" +
            "a$$inline_1;}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline17
  public void testInline17() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; foo(goo());",
        "function foo(a){return true;};" +
            "{var a$$inline_1=goo();true}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline18
  public void testInline18() {
    
    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
            "function x() { foo(goo()); }",
            "function foo(a){var b;return a;}; " +
            "function x() {{var a$$inline_2=goo();" +
                "var b$$inline_3;a$$inline_2}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline19
  public void testInline19() {
    
    helperInlineReferenceToFunction(
        "var x = 1; var y = 2;" +
        "function foo(a,b){x = b; y = a;}; " +
        "function bar() { foo(x,y); }",
        "var x = 1; var y = 2;" +
        "function foo(a,b){x = b; y = a;}; " +
        "function bar() {" +
           "{var a$$inline_2=x;" +
            "x = y;" +
            "y = a$$inline_2;}" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInline19b
  public void testInline19b() {
    helperInlineReferenceToFunction(
        "var x = 1; var y = 2;" +
        "function foo(a,b){y = a; x = b;}; " +
        "function bar() { foo(x,y); }",
        "var x = 1; var y = 2;" +
        "function foo(a,b){y = a; x = b;}; " +
        "function bar() {" +
           "{var b$$inline_3=y;" +
            "y = x;" +
            "x = b$$inline_3;}" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineIntoLoop
  public void testInlineIntoLoop() {
    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
        "for(;1;){ foo(1); }",
        "function foo(a){var b;return a;}; " +
        "for(;1;){ {" +
            "var b$$inline_3=void 0;1}}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){var b;return a;}; " +
        "do{ foo(1); } while(1)",
        "function foo(a){var b;return a;}; " +
        "do{ {" +
            "var b$$inline_3=void 0;1}}while(1)",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){for(var b in c)return a;}; " +
        "for(;1;){ foo(1); }",
        "function foo(a){var b;for(b in c)return a;}; " +
        "for(;1;){ {JSCompiler_inline_label_foo_4:{" +
            "var b$$inline_3=void 0;for(b$$inline_3 in c){" +
              "1;break JSCompiler_inline_label_foo_4" +
            "}}}}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction1
  public void testInlineFunctionWithInnerFunction1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {return true;}}; foo();",
        "function foo(){return function() {return true;}};" +
            "(function() {return true;})",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction2
  public void testInlineFunctionWithInnerFunction2() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {return true;}}; foo();",
        "function foo(){return function() {return true;}};" +
            "{(function() {return true;})}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction3
  public void testInlineFunctionWithInnerFunction3() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {var a; return true;}}; foo();",
        "function foo(){return function() {var a; return true;}};" +
            "(function() {var a; return true;});",
        "foo", INLINE_DIRECT);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction4
  public void testInlineFunctionWithInnerFunction4() {
    
    helperInlineReferenceToFunction(
        "function foo(){return function() {var a; return true;}}; foo();",
        "function foo(){return function() {var a; return true;}};" +
            "{(function() {var a$$inline_0; return true;});}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineFunctionWithInnerFunction5
  public void testInlineFunctionWithInnerFunction5() {
    
    helperInlineReferenceToFunction(
        "function foo(){function x() {var a; return true;} return x}; foo();",
        "function foo(){function x(){var a;return true}return x};" +
            "{function x$$inline_1(){var a$$inline_2;return true}x$$inline_1}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression1
  public void testInlineReferenceInExpression1() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { if (foo(1)) throw 'test'; }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "if (JSCompiler_inline_result$$0) throw 'test'; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression2
  public void testInlineReferenceInExpression2() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { return foo(1); }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "return JSCompiler_inline_result$$0; }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression3
  public void testInlineReferenceInExpression3() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { switch(foo(1)) { default:break; } }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "switch(JSCompiler_inline_result$$0) { default:break; } }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression4
  public void testInlineReferenceInExpression4() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0?0:1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression5
  public void testInlineReferenceInExpression5() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1)&&1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0&&1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression6
  public void testInlineReferenceInExpression6() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression7
  public void testInlineReferenceInExpression7() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {foo(1) && 1 }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0&&1 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression8
  public void testInlineReferenceInExpression8() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1 + foo(1) }",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression9
  public void testInlineReferenceInExpression9() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {var b = 1 + foo(1)}",
        "function foo(a){return true;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "var b = 1 + JSCompiler_inline_result$$0 }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression11
  public void testInlineReferenceInExpression11() {
    
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {a:foo(1)?0:1 }",
        "function foo(a){return true;}; " +
        "function x() { a:{{var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=true;}" +
        "JSCompiler_inline_result$$0?0:1 }}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression12
  public void testInlineReferenceInExpression12() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() {1?foo(1):1 }",
        "function foo(a){return true;}; " +
        "function x() { if(1) { {true;} } else { 1 }}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression13
  public void testInlineReferenceInExpression13() {
    helperInlineReferenceToFunction(
        "function foo(a){return true;}; " +
        "function x() { goo() + (1?foo(1):1) }",
        "function foo(a){return true;}; " +
        "function x() { var JSCompiler_temp_const$$0=goo();" +
            "var JSCompiler_temp$$1;" +
            "if(1) {" +
            "  {JSCompiler_temp$$1=true;} " +
            "} else {" +
            "  JSCompiler_temp$$1=1;" +
            "}" +
            "JSCompiler_temp_const$$0 + JSCompiler_temp$$1" +
            "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression14
  public void testInlineReferenceInExpression14() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression15
  public void testInlineReferenceInExpression15() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.gack = foo.call(this,1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression16
  public void testInlineReferenceInExpression16() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z[bar()] = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$1=z;" +
            "var JSCompiler_temp_const$$0=bar();" +
            "{" +
             "var JSCompiler_inline_result$$2;" +
             "z= {};" +
             "JSCompiler_inline_result$$2 = true;" +
            "}" +
            "JSCompiler_temp_const$$1[JSCompiler_temp_const$$0] = " +
                "JSCompiler_inline_result$$2;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineReferenceInExpression17
  public void testInlineReferenceInExpression17() {
    helperInlineReferenceToFunction(
        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() { z.y.x.gack = foo(1) }",

        "var z = {};" +
        "function foo(a){z = {};return true;}; " +
        "function x() {" +
            "var JSCompiler_temp_const$$0=z.y.x;" +
            "{" +
             "var JSCompiler_inline_result$$1;" +
             "z= {};" +
             "JSCompiler_inline_result$$1 = true;" +
            "}" +
            "JSCompiler_temp_const$$0.gack = JSCompiler_inline_result$$1;" +
        "}",
        "foo", INLINE_BLOCK, true);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineWithinCalls1
  public void testInlineWithinCalls1() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x() {1 + foo()() }",
        "function foo(){return _g;}; " +
        "function x() { {var JSCompiler_inline_result$$0; " +
        "JSCompiler_inline_result$$0=_g;}" +
        "1 + JSCompiler_inline_result$$0() }",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testInlineAssignmentToConstant
  public void testInlineAssignmentToConstant() {
    
    helperInlineReferenceToFunction(
        "function foo(){return _g;}; " +
        "function x(){var CONSTANT_RESULT = foo(); }",

        "function foo(){return _g;}; " +
        "function x() {" +
        "  {var JSCompiler_inline_result$$0; JSCompiler_inline_result$$0=_g;}" +
        "  var CONSTANT_RESULT = JSCompiler_inline_result$$0;" +
        "}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionInjectorTest::testBug1897706
  public void testBug1897706() {
    helperInlineReferenceToFunction(
        "function foo(a){}; foo(x())",
        "function foo(a){}; {var a$$inline_1=x()}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a){bar()}; foo(x())",
        "function foo(a){bar()}; {var a$$inline_1=x();bar()}",
        "foo", INLINE_BLOCK);

    helperInlineReferenceToFunction(
        "function foo(a,b){bar()}; foo(x(),y())",
        "function foo(a,b){bar()};" +
        "{var a$$inline_2=x();var b$$inline_3=y();bar()}",
        "foo", INLINE_BLOCK);
  }

// com.google.javascript.jscomp.FunctionNamesTest::testFunctionsNamesAndIds
  public void testFunctionsNamesAndIds() {
    final String jsSource =
        "goog.widget = function(str) {\n" +
        "  this.member_fn = function() {};\n" +
        "  local_fn = function() {};\n" +
        "  (function(a){})(1);\n" +
        "}\n" +
        "function foo() {\n" +
        "  function bar() {}\n" +
        "}\n" +
        "literal = {f1 : function(){}, f2 : function(){}};\n" +
        "goog.array.map(arr, function named(){});\n" +
        "goog.array.map(arr, function(){});\n" +
        "named_twice = function quax(){};\n" +
        "recliteral = {l1 : {l2 : function(){}}};\n" +
        "namedliteral = {n1 : function litnamed(){}};\n" +
        "namedrecliteral = {n1 : {n2 : function reclitnamed(){}}};\n" +
        "numliteral = {1 : function(){}};\n" +
        "recnumliteral = {1 : {a : function(){}}};\n";

    testSame(jsSource);

    final Map<Integer, String> idNameMap = Maps.newLinkedHashMap();
    int count = 0;
    for (Node f : functionNames.getFunctionNodeList()) {
      int id = functionNames.getFunctionId(f);
      String name = functionNames.getFunctionName(f);
      idNameMap.put(id, name);
      count++;
    }

    assertEquals("Unexpected number of functions", 16, count);

    final Map<Integer, String> expectedMap = Maps.newLinkedHashMap();

    expectedMap.put(0, "goog.widget.member_fn");
    expectedMap.put(1, "goog.widget::local_fn");
    expectedMap.put(2, "goog.widget::<anonymous>");
    expectedMap.put(3, "goog.widget");
    expectedMap.put(4, "foo::bar");
    expectedMap.put(5, "foo");
    expectedMap.put(6, "literal.f1");
    expectedMap.put(7, "literal.f2");
    expectedMap.put(8, "named");
    expectedMap.put(9, "<anonymous>");
    expectedMap.put(10, "quax");
    expectedMap.put(11, "recliteral.l1.l2");
    expectedMap.put(12, "litnamed");
    expectedMap.put(13, "reclitnamed");
    expectedMap.put(14, "<anonymous>");
    expectedMap.put(15, "<anonymous>");
    assertEquals("Function id/name mismatch",
                 expectedMap, idNameMap);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst1
  public void testReplaceReturnConst1() {
    String source = "a.prototype.foo = function() {return \"foobar\"}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(\"foobar\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst2
  public void testReplaceReturnConst2() {
    checkCompilesToSame("a.prototype.foo = function() {return foobar}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceReturnConst3
  public void testReplaceReturnConst3() {
    String source = "a.prototype.foo = function() {return void 0;}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    RETURNARG_HELPER,
                    "a.prototype.foo = JSCompiler_returnArg(void 0)",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter1
  public void testReplaceGetter1() {
    String source = "a.prototype.foo = function() {return this.foo_}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    GET_HELPER,
                    "a.prototype.foo = JSCompiler_get(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceGetter2
  public void testReplaceGetter2() {
    checkCompilesToSame("a.prototype.foo = function() {return}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter1
  public void testReplaceSetter1() {
    String source = "a.prototype.foo = function(v) {this.foo_ = v}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter2
  public void testReplaceSetter2() {
    String source = "a.prototype.foo = function(v, v2) {this.foo_ = v}";
    checkCompilesToSame(source, 3);
    checkCompilesTo(source,
                    SET_HELPER,
                    "a.prototype.foo = JSCompiler_set(\"foo_\")",
                    4);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter3
  public void testReplaceSetter3() {
    checkCompilesToSame("a.prototype.foo = function() {this.foo_ = v}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceSetter4
  public void testReplaceSetter4() {
    checkCompilesToSame(
        "a.prototype.foo = function(v, v2) {this.foo_ = v2}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction1
  public void testReplaceEmptyFunction1() {
    String source = "a.prototype.foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "a.prototype.foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction2
  public void testReplaceEmptyFunction2() {
    checkCompilesToSame("function foo() {}", 10);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceEmptyFunction3
  public void testReplaceEmptyFunction3() {
    String source = "var foo = function() {}";
    checkCompilesToSame(source, 4);
    checkCompilesTo(source,
                    EMPTY_HELPER,
                    "var foo = JSCompiler_emptyFn()",
                    5);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction1
  public void testReplaceIdentityFunction1() {
    String source = "a.prototype.foo = function(a) {return a}";
    checkCompilesToSame(source, 2);
    checkCompilesTo(source,
                    IDENTITY_HELPER,
                    "a.prototype.foo = JSCompiler_identityFn()",
                    3);
  }

// com.google.javascript.jscomp.FunctionRewriterTest::testReplaceIdentityFunction2
  public void testReplaceIdentityFunction2() {
    checkCompilesToSame("a.prototype.foo = function(a) {return a + 1}", 10);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoReturnWithoutResultAssignment
  public void testMutateNoReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){}; foo();",
        "{}",
        "foo");
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoReturnWithResultAssignment
  public void testMutateNoReturnWithResultAssignment() {
    helperMutate(
        "function foo(){}; var result = foo();",
        "{result = void 0}",
        "foo", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoValueReturnWithoutResultAssignment
  public void testMutateNoValueReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){return;}; foo();",
        "{}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateNoValueReturnWithResultAssignment
  public void testMutateNoValueReturnWithResultAssignment() {
    helperMutate(
        "function foo(){return;}; var result = foo();",
        "{result = void 0}",
        "foo");
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateValueReturnWithoutResultAssignment
  public void testMutateValueReturnWithoutResultAssignment() {
    helperMutate(
        "function foo(){return true;}; foo();",
        "{true;}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateValueReturnWithResultAssignment
  public void testMutateValueReturnWithResultAssignment() {
    helperMutate(
        "function foo(){return true;}; var x=foo();",
        "{x=true}",
        "foo", "x", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithMultipleReturns
  public void testMutateWithMultipleReturns() {
    helperMutate(
        "function foo(){ if (0) {return 0} else {return 1} };" +
          "var result=foo();",
        "{" +
          "JSCompiler_inline_label_foo_0:{" +
            "if(0) {" +
              "result=0; break JSCompiler_inline_label_foo_0" +
            "} else {" +
              "result=1; break JSCompiler_inline_label_foo_0" +
            "} result=void 0" +
          "}" +
        "}",
        "foo", true, false);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters1
  public void testMutateWithParameters1() {
    
    helperMutate(
        "function foo(a){return true;}; foo(x);",
        "{true}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters2
  public void testMutateWithParameters2() {
    
    helperMutate(
        "function foo(a){return x;}; foo(x);",
        "{x}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateWithParameters3
  public void testMutateWithParameters3() {
    
    helperMutate(
        "function foo(a){return a;}; " +
        "function x() { foo(x++); }",
        "{var a$$inline_1 = x++; a$$inline_1}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutate8
  public void testMutate8() {
    
    helperMutate(
        "function foo(a){return a+a;}; foo(x++);",
        "{var a$$inline_1 = x++;" +
            "a$$inline_1 + a$$inline_1;}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateInitializeUninitializedVars1
  public void testMutateInitializeUninitializedVars1() {
    helperMutate(
        "function foo(a){var b;return a;}; foo(1);",
        "{var b$$inline_3=void 0;1}",
        "foo", null, false, true);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateInitializeUninitializedVars2
  public void testMutateInitializeUninitializedVars2() {
    helperMutate(
        "function foo(a){for(var b in c)return a;}; foo(1);",
        "{JSCompiler_inline_label_foo_4:" +
          "{" +
            "for(var b$$inline_3 in c){" +
                "1;break JSCompiler_inline_label_foo_4" +
             "}" +
          "}" +
        "}",
        "foo", null);
  }

// com.google.javascript.jscomp.FunctionToBlockMutatorTest::testMutateCallInLoopVars1
  public void testMutateCallInLoopVars1() {
    
    boolean callInLoop = false;
    helperMutate(
        "function foo(a){var B = bar(); a;}; foo(1);",
        "{var B$$inline_3=bar(); 1;}",
        "foo", null, false, callInLoop);
    
    
    callInLoop = true;
    helperMutate(
        "function foo(a){var B = bar(); a;}; foo(1);",
        "{var B$$inline_3 = bar(); 1;}",
        "foo", null, false, callInLoop);
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
        + "found   : function (new:String, *): number\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams
  public void testBuiltInTypeDifferentNumParams() throws Exception {
    testSame(
        "\n"
        + "function String() {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String): string\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentNumParams2
  public void testBuiltInTypeDifferentNumParams2() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str, opt_nothing) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, ?, ?): string\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBuiltInTypeDifferentParamType
  public void testBuiltInTypeDifferentParamType() throws Exception {
    testSame(
        "\n"
        + "function String(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type String\n"
        + "found   : function (new:String, ?): string\n"
        + "expected: function (new:String, *): string");
  }

// com.google.javascript.jscomp.FunctionTypeBuilderTest::testBadFunctionTypeDefinition
  public void testBadFunctionTypeDefinition() throws Exception {
    testSame(
        "function Function(opt_str) {}\n",
        "", FunctionTypeBuilder.TYPE_REDEFINITION,
        "attempted re-definition of type Function\n"
        + "found   : function (new:Function, ?): ?\n"
        + "expected: function (new:Function, ...[*]): ?");
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

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound1
  public void testExportsFound1() {
    assertExported("var a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound2
  public void testExportsFound2() {
    assertExported("window['a']", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound3
  public void testExportsFound3() {
    assertExported("window.a", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound4
  public void testExportsFound4() {
    assertExported("this['a']", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound5
  public void testExportsFound5() {
    assertExported("this.a", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound6
  public void testExportsFound6() {
    assertExported("function f() { this['a'] }");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound7
  public void testExportsFound7() {
    assertExported("function f() { this.a }");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound8
  public void testExportsFound8() {
    assertExported("window['foo']", "foo");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound9
  public void testExportsFound9() {
    assertExported("window['a'] = 1;", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound10
  public void testExportsFound10() {
    assertExported("window['a']['b']['c'] = 1;", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound11
  public void testExportsFound11() {
    assertExported("if (window['a'] = 1) alert(x);", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound12
  public void testExportsFound12() {
    assertExported("function foo() { window['a'] = 1; }", "a");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound13
  public void testExportsFound13() {
    assertExported("function foo() {var window; window['a'] = 1; }");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound14
  public void testExportsFound14() {
    assertExported("var a={window:{}}; a.window['b']");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound15
  public void testExportsFound15() {
    assertExported("window.window['b']", "window");
  }

// com.google.javascript.jscomp.GatherRawExportsTest::testExportsFound16
  public void testExportsFound16() {
    
    
    assertExported("var a = window; a['b']");
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testAndOr
  public void testAndOr() throws Exception {
    Node andNode = getSideEffectsAndNode();
    checkKeepSimplifiedShortCircuitExpr(andNode,
                                        ImmutableList.of("foo&&(bar=0)"));
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testIllegalArgumentIfNotAndOr
  public void testIllegalArgumentIfNotAndOr() throws Exception {
    Node nameNode = Node.newString(Token.NAME, "foo");
    try {
      checkKeepSimplifiedShortCircuitExpr(nameNode,
                                          ImmutableList.<String>of());
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      
    }
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testIllegalArgumentIfNoSideEffectAndOr
  public void testIllegalArgumentIfNoSideEffectAndOr() throws Exception {
    Node andNode = getNoSideEffectsAndNode();
    try {
      checkKeepSimplifiedShortCircuitExpr(andNode,
                                          ImmutableList.<String>of());
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      
    }
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testHook
  public void testHook() throws Exception {
    Node hook = getSideEffectsHookNode();

    checkKeepSimplifiedHookExpr(hook,
                                true,
                                true,
                                ImmutableList.of("foo?bar=0:baz=0"));
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testIllegalArgumentIfNotHook
  public void testIllegalArgumentIfNotHook() throws Exception {
    Node nameNode = Node.newString(Token.NAME, "foo");
    try {
      checkKeepSimplifiedHookExpr(nameNode,
                                  true,
                                  true,
                                  ImmutableList.<String>of());
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      
    }
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testIllegalArgumentIfNoSideEffectHook
  public void testIllegalArgumentIfNoSideEffectHook() throws Exception {
    Node hookNode = getNoSideEffectsHookNode();
    try {
      checkKeepSimplifiedHookExpr(hookNode,
                                  true,
                                  true,
                                  ImmutableList.<String>of());
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      
    }
  }

// com.google.javascript.jscomp.GatherSideEffectSubexpressionsCallbackTest::testIllegalArgumentIfHookKeepNeitherBranch
  public void testIllegalArgumentIfHookKeepNeitherBranch() throws Exception {
    Node hookNode = getSideEffectsHookNode();
    try {
      checkKeepSimplifiedHookExpr(hookNode,
                                  false,
                                  false,
                                  ImmutableList.<String>of());
      fail("Expected exception");
    } catch (IllegalArgumentException e) {
      
    }
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSymbol
  public void testExportSymbol() {
    test("function foo() {}",
        "function foo(){}google_exportSymbol(\"foo\",foo)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSymbolAndProperties
  public void testExportSymbolAndProperties() {
    test("function foo() {}" +
         "foo.prototype.bar = function() {}",
         "function foo(){}" +
         "google_exportSymbol(\"foo\",foo);" +
         "foo.prototype.bar=function(){};" +
         "goog.exportProperty(foo.prototype,\"bar\",foo.prototype.bar)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSymbolAndConstantProperties
  public void testExportSymbolAndConstantProperties() {
    test("function foo() {}" +
         "foo.BAR = 5;",
         "function foo(){}" +
         "google_exportSymbol(\"foo\",foo);" +
         "foo.BAR=5;" +
         "goog.exportProperty(foo,\"BAR\",foo.BAR)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportVars
  public void testExportVars() {
    test("var FOO = 5",
         "var FOO=5;" +
         "google_exportSymbol(\"FOO\",FOO)");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNoExport
  public void testNoExport() {
    test("var FOO = 5", "var FOO=5");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNestedVarAssign
  public void testNestedVarAssign() {
    test("var BAR;\nvar FOO = BAR = 5",
         null, FindExportableNodes.NON_GLOBAL_ERROR);
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNestedAssign
  public void testNestedAssign() {
    test("var BAR;var FOO = {};\nFOO.test = BAR = 5",
         null, FindExportableNodes.NON_GLOBAL_ERROR);
  }

// com.google.javascript.jscomp.GenerateExportsTest::testNonGlobalScopeExport
  public void testNonGlobalScopeExport() {
    test("(function() { var FOO = 5 })()",
         null, FindExportableNodes.NON_GLOBAL_ERROR);
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportClass
  public void testExportClass() {
    test(" function G() {} foo();",
         "function G() {} google_exportSymbol('G', G); foo();");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportSubclass
  public void testExportSubclass() {
    test("var goog = {}; function F() {}" +
         " function G() {} goog.inherits(G, F);",
         "var goog = {}; function F() {}" +
         "function G() {} goog.inherits(G, F); google_exportSymbol('G', G);");
  }

// com.google.javascript.jscomp.GenerateExportsTest::testExportEnum
  public void testExportEnum() {
    
    test(" var E = {A:1, B:2};",
         " var E = {A:1, B:2};" +
         "google_exportSymbol('E', E);");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingUninitializedVarsInScope
  public void testGroupingUninitializedVarsInScope() {
    
    test("var a = 1; f1(); var b;", "var a = 1, b; f1();");
    
    test("var a = \"mangoes\"; f1(); alert(a); var b;",
         "var a = \"mangoes\", b; f1(); alert(a);");
    
    
    test("var a = 1; {var c; alert(c);} var b;",
         "var a = 1, c, b; {alert(c);}");
    
    test("var a = 1; var b = 1; f1(); f2(); var c; var d;",
         "var a = 1, b, c, d; b = 1; f1(); f2();");
    test("var a = 1; var b = 2; var c; f1(); f2(); var d, e;",
         "var a = 1, b, c, d, e; b = 2; f1(); f2();");
    test("var a = 1, b = 2, c; f1(); f2(); var d; var e; " +
         "f3(); f4(); var f = 10; var g; var h = a + b;",
         "var a = 1, b = 2, c, d, e, f, g, h; f1(); f2(); f3(); f4(); " +
         "f = 10; h = a + b;");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingInitializedVarsInScope
  public void testGroupingInitializedVarsInScope() {
    
    test("var a = 1; f1(); var b = 2;", "var a = 1, b; f1(); b = 2;");
    
    test("var a = \"mangoes\"; f1(); alert(a); var b = 2;",
         "var a = \"mangoes\", b; f1(); alert(a); b = 2;");
    
    
    test("var a = 1; {var c = 34; alert(c);} var b = 2;",
         "var a = 1, c, b; {c = 34; alert(c);} b = 2;");
    
    test("var a = 1; var b = 1; f1(); f2(); var c = 3; var d = 4;",
         "var a = 1, b, c, d; b = 1; f1(); f2(); c = 3; d = 4;");
    test("var a = 1; var b = 2; var c; f1(); f2(); var d = 4, e;",
         "var a = 1, b, c, d, e; b = 2; f1(); f2(); d = 4;");
    test("var a = 1, b = 2, c; f1(); f2(); var d; var e = 6; " +
         "f3(); f4(); var f; var g; var h = a + b;",
         "var a = 1, b = 2, c, d, e, f, g, h; f1(); f2(); e = 6; " +
         "f3(); f4(); h = a + b;");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsInForAndForInLoops
  public void testGroupingVarsInForAndForInLoops() {
    
    test("var a = 1; for (var x = 0; x < 10; ++x) {a++;} var y;",
         "var a = 1, x, y; for (x = 0; x < 10; ++x) {a++;}");
    test("var a = 1, x; for (x = 0; x < 10; ++x) {a++;} var y;",
         "var a = 1, x, y; for (x = 0; x < 10; ++x) {a++;}");
    test("var a = 1, x; for (x; x < 10; ++x) {a++;} var y;",
         "var a = 1, x, y; for (x; x < 10; ++x) {a++;}");
    test("var a = 1; for (; a < 10; ++a) {alert(a);} var y;",
         "var a = 1, y; for (; a < 10; ++a) {alert(a);}");
    test("var a = 1; for (var x; x < 10; ++x) {a += 2;} var y = 5;",
         "var a = 1, x, y; for (; x < 10; ++x) {a += 2;} y = 5;");
    
    test("var a = 1; " +
         "for (var a1 = 0, a2 = 10; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "var x = 5;",
         "var a = 1, x;" +
         "for (var a1 = 0, a2 = 10; a1 < 10 && a2 > 0; ++a1, --a2) {} " +
         "x = 5;");
    test("var a = 1; " +
         "for (var a1 = 0, a2; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "var x = 5;",
         "var a = 1, a1, a2, x;" +
         "for (a1 = 0; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "x = 5;");
    test("var a = 1; " +
         "for (var a1, a2; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "var x = 5;",
         "var a = 1, a1, a2, x;" +
         "for (; a1 < 10 && a2 > 0; ++a1, --a2) {}" +
         "x = 5;");

    
    test("var a = [1, 2, 3, 4]; for (var z in a) {alert(z);} var y;",
         "var a = [1, 2, 3, 4], z, y; for (z in a) {alert(z);}");
    test("var a = [1, 2, 3, 4]; for (var z in a) {alert(z);} var y = 5;",
         "var a = [1, 2, 3, 4], z, y; for (z in a) {alert(z);} y = 5;");
    test("var a; for (var z in a = [1, 2, 3, 4]) {alert(z);} var y, x = 5;",
         "var a, z, y, x; for (z in a = [1, 2, 3, 4]) {alert(z);} x = 5;");
    test("var a; for (var z = 1 in a = [1, 2, 3, 4]) {alert(z);} var y, x = 5;",
         "var a, z, y, x; for (z = 1 in a = [1, 2, 3, 4]) {alert(z);} x = 5;");
    test("var a, z; for (z in a = [1, 2, 3, 4]) {alert(z);} var y, x = 5;",
         "var a, z, y, x; for (z in a = [1, 2, 3, 4]) {alert(z);} x = 5;");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsNestedFunction
  public void testGroupingVarsNestedFunction() {
    test("function f(b) {var x; function g() {var x; a = x; var y;} var a;}",
         "function f(b) {var x, a; function g() {var x, y; a = x;}}");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsInnerFunction
  public void testGroupingVarsInnerFunction() {
    test("function f(b) {var x; h = x * x; var myfn = function() " +
         "{var x; a = x; var y;}; var a;}",
         "function f(b) {var x, myfn, a; h = x * x; myfn = function() " +
         "{var x, y; a = x;};}");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsFirstStatementNotVar
  public void testGroupingVarsFirstStatementNotVar() {
    test("f(); var a; g(); var b;", "f(); var a, b; g();");
  }

// com.google.javascript.jscomp.GroupVariableDeclarationsTest::testGroupingVarsInScopeRegtest
  public void testGroupingVarsInScopeRegtest() {
    
    test("var x = 0, y = 1, z;" +
         "function f1(aa, bb) {" +
         "  if (y) {" +
         "    if (x === 0) {" +
         "      var h, r = 999;" +
         "    }" +
         "  } else {" +
         "    r = 1000;" +
         "  }" +
         "  var mylist = [1, 2, 3, 4];" +
         "  var k1 = 200, k2 = 400;" +
         "  for (var i1 = 0; i1 < 10; ++i1) {" +
         "    for (var i2 in mylist) {" +
         "      alert(i1);" +
         "    }" +
         "  }" +
         "  var jam, q = 100;" +
         "  var myfn = function() {" +
         "    var x = 1;" +
         "    f5();" +
         "    var z = 5;" +
         "  };" +
         "  function f5() {" +
         "    var aa = 5;" +
         "    if (y === 1) {" +
         "      var x = 100;" +
         "    }" +
         "  }" +
         "}" +
         "var h = x + y;" +
         "function g() {" +
         "  y = 0;" +
         "  { var x = 200;}" +
         "  var h = y + x;" +
         "}" +
         "var ggg = 0;",  
         "var x = 0, y = 1, z, h, ggg;" +
         "function f1(aa, bb) {" +
         "  if (y) {" +
         "    if (x === 0) {" +
         "      var h, r = 999, mylist, i1, i2, jam, q, myfn;" +
         "    }" +
         "  } else {" +
         "    r = 1000;" +
         "  }" +
         "  mylist = [1, 2, 3, 4];" +
         "  var k1 = 200, k2 = 400;" +
         "  for (i1 = 0; i1 < 10; ++i1) {" +
         "    for (i2 in mylist) {" +
         "      alert(i1);" +
         "    }" +
         "  }" +
         "  q = 100; " +
         "  myfn = function() {" +
         "    var x = 1, z;" +
         "    f5();" +
         "    z = 5;" +
         "  };" +
         "  function f5() {" +
         "    var aa = 5, x;" +
         "    if (y === 1) {" +
         "      x = 100;" +
         "    }" +
         "  }" +
         "}" +
         "h = x + y;" +
         "function g() {" +
         "  y = 0;" +
         "  { var x = 200, h;}" +
         "  h = y + x;" +
         "}" +
         "ggg = 0;");
  }

// com.google.javascript.jscomp.IgnoreCajaPropertiesTest::testSimpleKey
  public void testSimpleKey() {
    
    test("for (i in x) f(i);",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x)" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    i = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(i); }" +
         "  }");
    
    test("for (i in x) { f(i); f(i); }",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x)" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    i = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(i); f(i); }" +
         "  }");
    
    
    test("for (i in x) for (j in y) f(i,j);",
         "for (var JSCompiler_IgnoreCajaProperties_1 in x)" +
         "  if (!JSCompiler_IgnoreCajaProperties_1.match(/___$/)) {" +
         "    i = JSCompiler_IgnoreCajaProperties_1;" +
         "    {" +
         "      for (var JSCompiler_IgnoreCajaProperties_0 in y)" +
         "        if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "          j = JSCompiler_IgnoreCajaProperties_0;" +
         "          { f(i,j); }" +
         "        }" +
         "    }" +
         "  }");
  }

// com.google.javascript.jscomp.IgnoreCajaPropertiesTest::testPropertyKey
  public void testPropertyKey() {
    test("for (z.i in x) { f(z.i); f(z.i); }",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x) {" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    z.i = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(z.i); f(z.i); }" +
         "  }" +
         "}");
  }

// com.google.javascript.jscomp.IgnoreCajaPropertiesTest::testFunctionPropertyKey
  public void testFunctionPropertyKey() {
    
    
    
    test("for (z.j().i in x) { f(z.j().i); f(z.j().i); }",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x) {" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    z.j().i = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(z.j().i); f(z.j().i); }" +
         "  }" +
         "}");
  }

// com.google.javascript.jscomp.IgnoreCajaPropertiesTest::testVarKey
  public void testVarKey() {
    
    test("for (var j in x) { f(j); }",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x) {" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    var j;" +
         "    j = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(j); }" +
         "  }" +
         "}");
    
    test("for (var j in x) { f(j); f(j); }",
         "for (var JSCompiler_IgnoreCajaProperties_0 in x) {" +
         "  if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "    var j;" +
         "    j = JSCompiler_IgnoreCajaProperties_0;" +
         "    { f(j); f(j); }" +
         "  }" +
         "}");
    
    test("for (var i in x) for (var j in y) f(i,j);",
         "for (var JSCompiler_IgnoreCajaProperties_1 in x)" +
         "  if (!JSCompiler_IgnoreCajaProperties_1.match(/___$/)) {" +
         "    var i;" +
         "    i = JSCompiler_IgnoreCajaProperties_1;" +
         "    {" +
         "      for (var JSCompiler_IgnoreCajaProperties_0 in y)" +
         "        if (!JSCompiler_IgnoreCajaProperties_0.match(/___$/)) {" +
         "          var j;" +
         "          j = JSCompiler_IgnoreCajaProperties_0;" +
         "          { f(i,j); }" +
         "        }" +
         "    }" +
         "  }");
  }

// com.google.javascript.jscomp.IgnoreCajaPropertiesTest::testFourChildFor
  public void testFourChildFor() {
    test("for (i = 0; i < 10; ++i) { f(i); }",
         "for (i = 0; i < 10; ++i) { f(i); }");
  }

// com.google.javascript.jscomp.InferJSDocInfoTest::testNativeCtor
  public void testNativeCtor() {
    testSame(
        " " +
        "function Object(x) {};",
        "var x = new Object();" +
        " var y = new Object();", null);
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
        "function Object(x) {};",
        " " +
        "function fn(x) {};" +
        "var goog = {};" +
        " goog.x = new Object();" +
        " goog.y = fn;", null);
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

// com.google.javascript.jscomp.InlineCostEstimatorTest::testCost
  public void testCost() {
    checkCost("1", "1");
    checkCost("a", "xx");
    checkCost("a + b", "xx+xx");
    checkCost("foo()", "xx()");
    checkCost("foo(a,b)", "xx(xx,xx)");
    checkCost("10 + foo(a,b)", "10+xx(xx,xx)");
    checkCost("1 + foo(a,b)", "1+xx(xx,xx)");
    checkCost("a ? 1 : 0", "xx?1:0");
    checkCost("a.b", "xx.xx");
    checkCost("new Obj()", "new xx");
    checkCost("function a() {return \"monkey\"}",
              "function xx(){return\"monkey\"}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction1
  public void testInlineEmptyFunction1() {
    
    test("function foo(){}" +
        "foo();",
        "void 0;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction2
  public void testInlineEmptyFunction2() {
    
    test("function foo(){}" +
        "foo(1, new Date, function(){});",
        "void 0;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction3
  public void testInlineEmptyFunction3() {
    
    test("function foo(){}" +
        "foo();foo();foo();",
        "void 0;void 0;void 0");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction4
  public void testInlineEmptyFunction4() {
    
    test("function foo(){}" +
        "foo(x());",
        "{var JSCompiler_inline_anon_param_0=x();}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineEmptyFunction5
  public void testInlineEmptyFunction5() {
    
    
    allowBlockInlining = false;
    testSame("function foo(){}" +
        "foo(x());");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions1
  public void testInlineFunctions1() {
    
    test("function foo(){ return 4 }" +
        "foo();",
        "4");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions2
  public void testInlineFunctions2() {
    
    
    test("var t;var AB=function(){return 4};" +
         "function BC(){return 6;}" +
         "CD=function(x){return x + 5};x=CD(3);y=AB();z=BC();",
         "var t;CD=function(x){return x+5};x=CD(3);y=4;z=6"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions3
  public void testInlineFunctions3() {
    
    test("var t;var AB=function(){return 4};" +
        "function BC(){return 6;}" +
        "var CD=function(x){return x + 5};x=CD(3);y=AB();z=BC();",
        "var t;x=3+5;y=4;z=6");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions4
  public void testInlineFunctions4() {
    
    test("var t; var AB = function() { return 4 }; " +
        "function BC() { return 6; }" +
        "CD = 0;" +
        "CD = function(x) { return x + 5 }; x = CD(3); y = AB(); z = BC();",

        "var t;CD=0;CD=function(x){return x+5};x=CD(3);y=4;z=6");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions5
  public void testInlineFunctions5() {
    
    test("var FOO_FN=function(x,y) { return \"de\" + x + \"nu\" + y };" +
         "var a = FOO_FN(\"ez\", \"ts\")",

         "var a=\"de\"+\"ez\"+\"nu\"+\"ts\"");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions6
  public void testInlineFunctions6() {
    
    test("function BAR_FN(x, y, z) { return z(foo(x + y)) }" +
         "alert(BAR_FN(1, 2, baz))",

         "alert(baz(foo(1+2)))");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions7
  public void testInlineFunctions7() {
    
    test("function FN(x,y,z){return x+x+y}" +
         "var b=FN(1,2,3)",

         "var b=1+1+2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions8
  public void testInlineFunctions8() {
    
    test("function MUL(x,y){return x*y}function ADD(x,y){return x+y}" +
         "var a=1+MUL(2,3);var b=2*ADD(3,4)",

         "var a=1+2*3;var b=2*(3+4)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions9
  public void testInlineFunctions9() {
    
    test("function INC(x){return x++}" +
         "var y=INC(i)",
         "var y;{var x$$inline_1=i;" +
         "y=x$$inline_1++}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions10
  public void testInlineFunctions10() {
    test("function INC(x){return x++}" +
         "var y=INC(i);y=INC(i)",
         "var y;" +
         "{var x$$inline_1=i;" +
         "y=x$$inline_1++}" +
         "{var x$$inline_4=i;" +
         "y=x$$inline_4++}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions11
  public void testInlineFunctions11() {
    test("function f(x){return x}" +
          "var y=f(i)",
          "var y=i");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions12
  public void testInlineFunctions12() {
    
    allowBlockInlining = false;
    test("function f(x){return x}" +
          "var y=f(i)",
          "var y=i");
    testSame("function f(x){return x}" +
         "var y=f(i++)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions13
  public void testInlineFunctions13() {
    
    test("function f(x){return x}" +
         "var y=f(i++)",
         "var y;{var x$$inline_1=i++;y=x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions14
  public void testInlineFunctions14() {
    
    test("function FOO(x){return x}var BAR=function(y){return y}" +
             ";b=FOO;a(BAR);x=FOO(1);y=BAR(2)",

         "function FOO(x){return x}var BAR=function(y){return y}" +
             ";b=FOO;a(BAR);x=1;y=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15a
  public void testInlineFunctions15a() {
    
    test("function foo(){return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "var d=b()+foo()",

         "var d=c+function(a){return a+1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15b
  public void testInlineFunctions15b() {
    
    test("function foo(){var x;return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "var d=b()+foo()",

         "function foo(){var x;return function(a){return a+1}}" +
         "var d=c+foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions15c
  public void testInlineFunctions15c() {
    
    test("function foo(){return function(a){return a+1}}" +
         "var b=function(){return c};" +
         "function _x(){ var d=b()+foo() }",

         "function foo(){return function(a){return a+1}}" +
         "function _x(){ var d=c+foo() }");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions16
  public void testInlineFunctions16() {
    
    testSame("function foo(b){return window.bar(function(){c(b)})}" +
             "var d=foo(e)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions17
  public void testInlineFunctions17() {
    
    testSame("function foo(x){return x*x+foo(3)}var bar=foo(4)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions18
  public void testInlineFunctions18() {
    
    allowBlockInlining = false;
    test("function foo(a, b){return a+b}" +
         "function bar(d){return c}" +
         "var d=foo(bar(1),e)",
         "var d=c+e");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions19
  public void testInlineFunctions19() {
    
    
    test("function foo(a, b){return a+b}" +
        "function bar(d){return c}" +
        "var d=foo(bar(1),e)",
        "var d;{d=c+e}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions20
  public void testInlineFunctions20() {
    
    allowBlockInlining = false;
    test("function foo(a, b){return a+b}" +
         "function bar(d){return c}" +
         "var d=bar(foo(1,e));",
         "var d=c");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions21
  public void testInlineFunctions21() {
    
    test("function foo(a, b){return a+b}" +
        "function bar(d){return c}" +
        "var d=bar(foo(1,e))",
        "var d;{d=c}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions22
  public void testInlineFunctions22() {
    
    test("function plex(a){if(a) return 0;else return 1;}" +
         "function foo(a, b){return bar(a+b)}" +
         "function bar(d){return plex(d)}" +
         "var d=foo(1,2)",

         "var d;{JSCompiler_inline_label_plex_2:{" +
         "if(1+2){" +
         "d=0;break JSCompiler_inline_label_plex_2}" +
         "else{" +
         "d=1;break JSCompiler_inline_label_plex_2}d=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions23
  public void testInlineFunctions23() {
    
    test("function complex(a){if(a) return 0;else return 1;}" +
         "function bar(d){return complex(d)}" +
         "function foo(a, b){return bar(a+b)}" +
         "var d=foo(1,2)",

         "var d;{JSCompiler_inline_label_complex_2:{" +
         "if(1+2){" +
         "d=0;break JSCompiler_inline_label_complex_2" +
         "}else{" +
         "d=1;break JSCompiler_inline_label_complex_2" +
         "}d=void 0}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions24
  public void testInlineFunctions24() {
    
    testSame("function foo(x){return this}foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions25
  public void testInlineFunctions25() {
    testSame("function foo(){return arguments[0]}foo()");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions26
  public void testInlineFunctions26() {
    
    testSame("function _foo(x){return x}_foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions27
  public void testInlineFunctions27() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {y: 1, z: foo(2)};",
        "var window={};" +
        "{" +
        "  var JSCompiler_inline_result$$0;" +
        "  window.bar++;" +
        "  JSCompiler_inline_result$$0 = 3;" +
        "}" +
        "var x = {y: 1, z: JSCompiler_inline_result$$0};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions28
  public void testInlineFunctions28() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {y: alert(), z: foo(2)};",
        "var window = {};" +
        "var JSCompiler_temp_const$$0 = alert();" +
        "{" +
        " var JSCompiler_inline_result$$1;" +
        " window.bar++;" +
        " JSCompiler_inline_result$$1 = 3;}" +
        "var x = {" +
        "  y: JSCompiler_temp_const$$0," +
        "  z: JSCompiler_inline_result$$1" +
        "};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions29
  public void testInlineFunctions29() {
    test("var window = {}; function foo(){window.bar++; return 3;}" +
        "var x = {a: alert(), b: alert2(), c: foo(2)};",
        "var window = {};" +
        "var JSCompiler_temp_const$$1 = alert();" +
        "var JSCompiler_temp_const$$0 = alert2();" +
        "{" +
        " var JSCompiler_inline_result$$2;" +
        " window.bar++;" +
        " JSCompiler_inline_result$$2 = 3;}" +
        "var x = {" +
        "  a: JSCompiler_temp_const$$1," +
        "  b: JSCompiler_temp_const$$0," +
        "  c: JSCompiler_inline_result$$2" +
        "};");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineFunctions30
  public void testInlineFunctions30() {
    
    testSame("function foo(){ return eval() }" +
        "foo();");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining1
  public void testMixedModeInlining1() {
    
    test("function foo(){return 1}" +
        "foo();",
        "1;");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining2
  public void testMixedModeInlining2() {
    
    
    test("function foo(){return 1}" +
        "foo(x());",
        "{var JSCompiler_inline_anon_param_0=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining3
  public void testMixedModeInlining3() {
    
    test("function foo(){return 1}" +
        "foo();foo(x());",
        "1;{var JSCompiler_inline_anon_param_0=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInlining4
  public void testMixedModeInlining4() {
    
    
    test("function foo(){return 1}" +
        "foo();foo(x());" +
        "foo(1);foo(1,x());",
        "1;{var JSCompiler_inline_anon_param_0=x();1}" +
        "1;{var JSCompiler_inline_anon_param_4=x();1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting1
  public void testMixedModeInliningCosting1() {
    

    
    test(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "foo(1,2);" +
        "foo(2,3)",

        "1+2+1+2+4+5+6+7+8+9+1+2+3+4+5;" +
        "2+3+2+3+4+5+6+7+8+9+1+2+3+4+5");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting2
  public void testMixedModeInliningCosting2() {
    
    
    testSame(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "foo(1,2);" +
        "foo(2,3,x())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting3
  public void testMixedModeInliningCosting3() {
    
    test(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+10}" +
        "foo(1,2);" +
        "foo(2,3,x())",

        "1+2+1+2+4+5+6+7+8+9+1+2+3+10;" +
        "{var JSCompiler_inline_anon_param_4=x();" +
        "2+3+2+3+4+5+6+7+8+9+1+2+3+10}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testMixedModeInliningCosting4
  public void testMixedModeInliningCosting4() {
    
    testSame(
        "function foo(a,b){return a+b+a+b+4+5+6+7+8+9+1+2+3+4+101}" +
        "foo(1,2);" +
        "foo(2,3,x())");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified1
  public void testNoInlineIfParametersModified1() {
    
    test("function f(x){return x=1}f(undefined)",
         "{var x$$inline_1=undefined;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified2
  public void testNoInlineIfParametersModified2() {
    test("function f(x){return (x)=1;}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified3
  public void testNoInlineIfParametersModified3() {
    
    test("function f(x){return x*=2}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1*=2}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified4
  public void testNoInlineIfParametersModified4() {
    
    test("function f(x){return x?(x=2):0}f(2)",
         "{var x$$inline_1=2;" +
         "x$$inline_1?(" +
         "x$$inline_1=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified5
  public void testNoInlineIfParametersModified5() {
    
    test("function f(x,y){return x?(y=2):0}f(2,undefined)",
         "{var y$$inline_3=undefined;2?(" +
         "y$$inline_3=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified6
  public void testNoInlineIfParametersModified6() {
    test("function f(x,y){return x?(y=2):0}f(2)",
         "{var y$$inline_3=void 0;2?(" +
         "y$$inline_3=2):0}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified7
  public void testNoInlineIfParametersModified7() {
    
    test("function f(a){return++a<++a}f(1)",
         "{var a$$inline_1=1;" +
         "++a$$inline_1<" +
         "++a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified8
  public void testNoInlineIfParametersModified8() {
    
    test("function f(a){return a.x=2}f(o)", "o.x=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineIfParametersModified9
  public void testNoInlineIfParametersModified9() {
    
    test("function f(a){return a[2]=2}f(o)", "o[2]=2");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverPartialSubtitution1
  public void testInlineNeverPartialSubtitution1() {
    test("function f(z){return x.y.z;}f(1)",
         "x.y.z");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverPartialSubtitution2
  public void testInlineNeverPartialSubtitution2() {
    test("function f(z){return x.y[z];}f(a)",
         "x.y[a]");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverMutateConstants
  public void testInlineNeverMutateConstants() {
    test("function f(x){return x=1}f(undefined)",
         "{var x$$inline_1=undefined;" +
         "x$$inline_1=1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineNeverOverrideNewValues
  public void testInlineNeverOverrideNewValues() {
    test("function f(a){return++a<++a}f(1)",
        "{var a$$inline_1=1;" +
        "++a$$inline_1<++a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineMutableArgsReferencedOnce
  public void testInlineMutableArgsReferencedOnce() {
    test("function foo(x){return x;}foo([])", "[]");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs1
  public void testNoInlineMutableArgs1() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo([])");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs2
  public void testNoInlineMutableArgs2() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo(new Date)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs3
  public void testNoInlineMutableArgs3() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo(true&&new Date)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineMutableArgs4
  public void testNoInlineMutableArgs4() {
    allowBlockInlining = false;
    testSame("function foo(x){return x+x} foo({})");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs1
  public void testInlineBlockMutableArgs1() {
    test("function foo(x){x+x}foo([])",
         "{var x$$inline_1=[];" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs2
  public void testInlineBlockMutableArgs2() {
    test("function foo(x){x+x}foo(new Date)",
         "{var x$$inline_1=new Date;" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs3
  public void testInlineBlockMutableArgs3() {
    test("function foo(x){x+x}foo(true&&new Date)",
         "{var x$$inline_1=true&&new Date;" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testInlineBlockMutableArgs4
  public void testInlineBlockMutableArgs4() {
    test("function foo(x){x+x}foo({})",
         "{var x$$inline_1={};" +
         "x$$inline_1+x$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables1
  public void testShadowVariables1() {
    
    

    
    
    test("var a=0;" +
         "function foo(a){return 3+a}" +
         "function bar(){var a=foo(4)}" +
         "bar();",

         "var a=0;" +
         "{var a$$inline_1=3+4}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables2
  public void testShadowVariables2() {
    
    
    
    test("var a=0;" +
        "function foo(a){return 3+a}" +
        "function bar(){a=foo(4)}" +
        "bar()",

        "var a=0;" +
        "{a=3+4}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables3
  public void testShadowVariables3() {
    
    test("var a=0;" +
        "function foo(){var a=2;return 3+a}" +
        "function _bar(){a=foo()}",

        "var a=0;" +
        "function _bar(){{var a$$inline_1=2;" +
        "a=3+a$$inline_1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables4
  public void testShadowVariables4() {
    
    
    test("var a=0;" +
         "function foo(){return 3+a}" +
         "function _bar(a){a=foo(4)+a}",

         "var a=0;function _bar(a$$1){" +
         "a$$1=" +
         "3+a+a$$1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables5
  public void testShadowVariables5() {
    
    
    allowBlockInlining = false;
    testSame("var a=0;" +
        "function foo(){var a=4;return 3+a}" +
        "function _bar(a){a=foo(4)+a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables6
  public void testShadowVariables6() {
    test("var a=0;" +
        "function foo(){var a=4;return 3+a}" +
        "function _bar(a){a=foo(4)}",

        "var a=0;function _bar(a$$2){{" +
        "var a$$inline_1=4;" +
        "a$$2=3+a$$inline_1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables7
  public void testShadowVariables7() {
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_1=5;{a}}"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables8
  public void testShadowVariables8() {
    
    test("var a=0;" +
         "function foo(){return 3}" +
         "function _bar(){var a=foo()}",

         "var a=0;" +
         "function _bar(){var a=3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables9
  public void testShadowVariables9() {
    
    test("function foo(){return 3}" +
         "function _bar(){var a=foo()}",

         "function _bar(){var a=3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables10
  public void testShadowVariables10() {
    
    test("var a;function foo(){return a}" +
         "function _bar(){var a=foo()}",
         "var a;function _bar(){var a$$1=a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables11
  public void testShadowVariables11() {
    
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var a=foo();alert(a)}",
         "var a=0;var b=1;" +
         "function _bar(){var a$$1=a+a;" +
         "alert(a$$1)}"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables12
  public void testShadowVariables12() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+b}" +
         "function _bar(){var a=foo(),b;alert(a)}",
         "var a=0;var b=1;" +
         "function _bar(){var a$$1=a+b," +
         "b$$1;" +
         "alert(a$$1)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables13
  public void testShadowVariables13() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var c=foo();alert(c)}",

         "var a=0;var b=1;" +
         "function _bar(){var c=a+a;alert(c)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables14
  public void testShadowVariables14() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+b}" +
         "function _bar(){var c=foo(),b;alert(c)}",
         "var a=0;var b=1;" +
         "function _bar(){var c=a+b," +
         "b$$1;alert(c)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables15
  public void testShadowVariables15() {
    
    test("var a=0;var b=1;" +
         "function foo(){return a+a}" +
         "function _bar(){var c=foo();alert(c+a)}",

         "var a=0;var b=1;" +
         "function _bar(){var c=a+a;alert(c+a)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables16
  public void testShadowVariables16() {
    
    test("var a=3;" +
         "function foo(){return a}" +
         "(function(){var a=5;(function(){foo()})()})()",
         "var a=3;" +
         "{var a$$inline_1=5;{a}}"
         );
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables17
  public void testShadowVariables17() {
    test("var a=0;" +
         "function bar(){return a+a}" +
         "function foo(){return bar()}" +
         "function _goo(){var a=2;var x=foo();}",

         "var a=0;" +
         "function _goo(){var a$$1=2;var x=a+a}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testShadowVariables18
  public void testShadowVariables18() {
    test("var a=0;" +
        "function bar(){return a+a}" +
        "function foo(){var a=3;return bar()}" +
        "function _goo(){var a=2;var x=foo();}",

        "var a=0;" +
        "function _goo(){var a$$2=2;var x;" +
        "{var a$$inline_1=3;x=a+a}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining1
  public void testCostBasedInlining1() {
    testSame(
        "function foo(a){return a}" +
        "foo=new Function(\"return 1\");" +
        "foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining2
  public void testCostBasedInlining2() {
    
    
    test(
        "function foo(a){return a}" +
        "var b=foo;" +
        "function _t1(){return foo(1)}",

        "function foo(a){return a}" +
        "var b=foo;" +
        "function _t1(){return 1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining3
  public void testCostBasedInlining3() {
    
    test(
        "function foo(a,b){return a+b}" +
        "var b=foo;" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function foo(a,b){return a+b}" +
        "var b=foo;" +
        "function _t1(){return 1+2}" +
        "function _t2(){return 2+3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining4
  public void testCostBasedInlining4() {
    
    
    testSame(
        "function foo(a,b){return a+b+a+b}" +
        "var b=foo;" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining5
  public void testCostBasedInlining5() {
    
    test(
        "function foo(a,b){return a+b+a+b}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function _t1(){return 1+2+1+2}" +
        "function _t2(){return 2+3+2+3}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining6
  public void testCostBasedInlining6() {
    
    
    test(
        "function foo(a,b){return a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2+3+4+5}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}",

        "function _t1(){return 1+2+1+2+1+2+1+2+4+5+6+7+8+9+1+2+3+4+5}" +
        "function _t2(){return 2+3+2+3+2+3+2+3+4+5+6+7+8+9+1+2+3+4+5}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining7
  public void testCostBasedInlining7() {
    
    testSame(
        "function foo(a,b){" +
        "    return a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2+3+4+5+6}" +
        "function _t1(){return foo(1,2)}" +
        "function _t2(){return foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining8
  public void testCostBasedInlining8() {
    
    
    
    
    
    
    
    allowBlockInlining = false;
    testSame("function f(a){return 1 + a + a;}" +
        "var a = f(f(1));");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining9
  public void testCostBasedInlining9() {
    
    
    
    test("function f(a){return 1 + a + a;}" +
         "var a = f(f(1));",
         "var a;" +
         "{var a$$inline_1=1+1+1;" +
         "a=1+a$$inline_1+a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining10
  public void testCostBasedInlining10() {
    
    
    
    allowBlockInlining = false;
    test("function f(a){return a + a;}" +
        "var a = f(f(1));",
        "var a= 1+1+(1+1);");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining11
  public void testCostBasedInlining11() {
    
    test("function f(a){return a + a;}" +
         "var a = f(f(1))",
         "var a;" +
         "{var a$$inline_1=1+1;" +
         "a=a$$inline_1+a$$inline_1}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInlining12
  public void testCostBasedInlining12() {
    test("function f(a){return 1 + a + a;}" +
         "var a = f(1) + f(2);",

         "var a=1+1+1+(1+2+2)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex1
  public void testCostBasedInliningComplex1() {
    testSame(
        "function foo(a){a()}" +
        "foo=new Function(\"return 1\");" +
        "foo(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex2
  public void testCostBasedInliningComplex2() {
    
    
    test(
        "function foo(a){a()}" +
        "var b=foo;" +
        "function _t1(){foo(x)}",

        "function foo(a){a()}" +
        "var b=foo;" +
        "function _t1(){{x()}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex3
  public void testCostBasedInliningComplex3() {
    
    test(
        "function foo(a,b){a+b}" +
        "var b=foo;" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function foo(a,b){a+b}" +
        "var b=foo;" +
        "function _t1(){{1+2}}" +
        "function _t2(){{2+3}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex4
  public void testCostBasedInliningComplex4() {
    
    
    testSame(
        "function foo(a,b){a+b+a+b}" +
        "var b=foo;" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex5
  public void testCostBasedInliningComplex5() {
    
    test(
        "function foo(a,b){a+b+a+b}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function _t1(){{1+2+1+2}}" +
        "function _t2(){{2+3+2+3}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex6
  public void testCostBasedInliningComplex6() {
    
    
    test(
        "function foo(a,b){a+b+a+b+a+b+a+b+4+5+6+7+8+9+1}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}",

        "function _t1(){{1+2+1+2+1+2+1+2+4+5+6+7+8+9+1}}" +
        "function _t2(){{2+3+2+3+2+3+2+3+4+5+6+7+8+9+1}}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex7
  public void testCostBasedInliningComplex7() {
    
    testSame(
        "function foo(a,b){a+b+a+b+a+b+a+b+4+5+6+7+8+9+1+2}" +
        "function _t1(){foo(1,2)}" +
        "function _t2(){foo(2,3)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex8
  public void testCostBasedInliningComplex8() {
    
    testSame("function _f(a){1+a+a}" +
             "a=_f(1)+_f(1)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testCostBasedInliningComplex9
  public void testCostBasedInliningComplex9() {
    test("function f(a){1 + a + a;}" +
         "f(1);f(2);",
         "{1+1+1}{1+2+2}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDoubleInlining1
  public void testDoubleInlining1() {
    allowBlockInlining = false;
    test("var foo = function(a) { return getWindow(a); };" +
         "var bar = function(b) { return b; };" +
         "foo(bar(x));",
         "getWindow(x)");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testDoubleInlining2
  public void testDoubleInlining2() {
    test("var foo = function(a) { return getWindow(a); };" +
         "var bar = function(b) { return b; };" +
         "foo(bar(x));",
         "{getWindow(x)}");
  }

// com.google.javascript.jscomp.InlineFunctionsTest::testNoInlineOfNonGlobalFunction1
  public void testNoInlineOfNonGlobalFunction1() {
    test("var g;function _f(){function g(){return 0}}" +
         "function _h(){return g()}",
         "var g;function _f(){}" +
         "function _h(){return g()}");
  }
