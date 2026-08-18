// buggy code
    private void annotateCalls(Node n) {
      Preconditions.checkState(n.isCall());

      // Keep track of of the "this" context of a call.  A call without an
      // explicit "this" is a free call.
      Node first = n.getFirstChild();

      // ignore cast nodes.

      if (!NodeUtil.isGet(first)) {
        n.putBooleanProp(Node.FREE_CALL, true);
      }

      // Keep track of the context in which eval is called. It is important
      // to distinguish between "(0, eval)()" and "eval()".
      if (first.isName() &&
          "eval".equals(first.getString())) {
        first.putBooleanProp(Node.DIRECT_EVAL, true);
      }
    }

// relevant test
// com.google.javascript.jscomp.ReplaceMessagesTest::testStrictModeAndMessageReplacementAbsentInNonEmptyBundle
  public void testStrictModeAndMessageReplacementAbsentInNonEmptyBundle()  {
    registerMessage(new JsMessage.Builder("MSG_J")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());

    strictReplacement = true;
    test("var MSG_E = 'Hello';", "var MSG_E = 'Hello';",
        ReplaceMessages.BUNDLE_DOES_NOT_HAVE_THE_MESSAGE);

  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testFunctionReplacementMissing
  public void testFunctionReplacementMissing()  {
    style = Style.LEGACY;
    test("var MSG_F = function() {return 'asdf'};",
         "var MSG_F = function() {return\"asdf\"}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testFunctionWithParamReplacementMissing
  public void testFunctionWithParamReplacementMissing()  {
    style = Style.LEGACY;
    test(
        "var MSG_G = function(measly) {return 'asdf' + measly};",
        "var MSG_G=function(measly){return\"asdf\"+measly}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testPlaceholderNameInLowerUnderscoreCase
  public void testPlaceholderNameInLowerUnderscoreCase()  {
    test(
        "var MSG_J = goog.getMsg('${$amt_earned}', {amt_earned: x});",
        "var MSG_J = goog.getMsg('${$amt_earned}', {amt_earned: x});",
        MESSAGE_TREE_MALFORMED);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testBadPlaceholderReferenceInReplacement
  public void testBadPlaceholderReferenceInReplacement()  {
    registerMessage(new JsMessage.Builder("MSG_K")
        .appendPlaceholderReference("amount")
        .build());

    test(
        "var MSG_K = goog.getMsg('Hi {$jane}', {jane: x});",
        "var MSG_K = goog.getMsg('Hi {$jane}', {jane: x});",
         MESSAGE_TREE_MALFORMED);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleNoPlaceholdersVarSyntax
  public void testLegacyStyleNoPlaceholdersVarSyntax()  {
    registerMessage(new JsMessage.Builder("MSG_A")
        .appendStringPart("Hi\nthere")
        .build());
    style = Style.LEGACY;
    test("var MSG_A = 'd*6a0@z>t';",
         "var MSG_A=\"Hi\\nthere\"");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleNoPlaceholdersFunctionSyntax
  public void testLegacyStyleNoPlaceholdersFunctionSyntax()  {
    registerMessage(new JsMessage.Builder("MSG_B")
        .appendStringPart("Hi\nthere")
        .build());
    style = Style.LEGACY;
    test("var MSG_B = function() {return 'asdf'};",
         "var MSG_B=function(){return\"Hi\\nthere\"}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleOnePlaceholder
  public void testLegacyStyleOnePlaceholder()  {
    registerMessage(new JsMessage.Builder("MSG_C")
        .appendStringPart("One ")
        .appendPlaceholderReference("measly")
        .appendStringPart(" ph")
        .build());
    style = Style.LEGACY;
    test(
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
    style = Style.LEGACY;
    test(
        "var MSG_D = function(jane, dick) {return jane + dick};",
        "var MSG_D=function(jane,dick){return dick+(\" and \"+jane)}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStylePlaceholderNameInLowerCamelCase
  public void testLegacyStylePlaceholderNameInLowerCamelCase() {
    registerMessage(new JsMessage.Builder("MSG_E")
        .appendStringPart("Sum: $")
        .appendPlaceholderReference("amtEarned")
        .build());
    style = Style.LEGACY;
    test(
        "var MSG_E = function(amtEarned) {return amtEarned + 'x'};",
        "var MSG_E=function(amtEarned){return\"Sum: $\"+amtEarned}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStylePlaceholderNameInLowerUnderscoreCase
  public void testLegacyStylePlaceholderNameInLowerUnderscoreCase() {
    registerMessage(new JsMessage.Builder("MSG_F")
        .appendStringPart("Sum: $")
        .appendPlaceholderReference("amt_earned")
        .build());

    
    style = Style.LEGACY;
    test(
        "var MSG_F = function(amt_earned) {return amt_earned + 'x'};",
        "var MSG_F=function(amt_earned){return\"Sum: $\"+amt_earned}");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testLegacyStyleBadPlaceholderReferenceInReplacemen
  public void testLegacyStyleBadPlaceholderReferenceInReplacemen() {
    registerMessage(new JsMessage.Builder("MSG_B")
        .appendStringPart("Ola, ")
        .appendPlaceholderReference("chimp")
        .build());

    test("var MSG_B = function(chump) {return chump + 'x'};",
         "var MSG_B = function(chump) {return chump + 'x'};",
         JsMessageVisitor.MESSAGE_TREE_MALFORMED);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testTranslatedPlaceHolderMissMatch
  public void testTranslatedPlaceHolderMissMatch() {
    registerMessage(new JsMessage.Builder("MSG_A")
        .appendPlaceholderReference("a")
        .appendStringPart("!")
        .build());

    test("var MSG_A = goog.getMsg('{$a}');",
         "var MSG_A = goog.getMsg('{$a}');",
         MESSAGE_TREE_MALFORMED);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testBadFallbackSyntax1
  public void testBadFallbackSyntax1() {
    test("\n" +
         "var MSG_A = goog.getMsg('asdf');" +
         "var x = goog.getMsgWithFallback(MSG_A);", null,
         JsMessageVisitor.BAD_FALLBACK_SYNTAX);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testBadFallbackSyntax2
  public void testBadFallbackSyntax2() {
    test("var x = goog.getMsgWithFallback('abc', 'bcd');", null,
         JsMessageVisitor.BAD_FALLBACK_SYNTAX);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testBadFallbackSyntax3
  public void testBadFallbackSyntax3() {
    test("\n" +
         "var MSG_A = goog.getMsg('asdf');" +
         "var x = goog.getMsgWithFallback(MSG_A, y);", null,
         JsMessageVisitor.FALLBACK_ARG_ERROR);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testBadFallbackSyntax4
  public void testBadFallbackSyntax4() {
    test("\n" +
         "var MSG_A = goog.getMsg('asdf');" +
         "var x = goog.getMsgWithFallback(y, MSG_A);", null,
         JsMessageVisitor.FALLBACK_ARG_ERROR);
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testUseFallback
  public void testUseFallback() {
    registerMessage(new JsMessage.Builder("MSG_B")
        .appendStringPart("translated")
        .build());
    test("\n" +
         "var MSG_A = goog.getMsg('msg A');" +
         "\n" +
         "var MSG_B = goog.getMsg('msg B');" +
         "var x = goog.getMsgWithFallback(MSG_A, MSG_B);",
         "var MSG_A = 'msg A';" +
         "var MSG_B = 'translated';" +
         "var x = MSG_B;");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testFallbackEmptyBundle
  public void testFallbackEmptyBundle() {
    test("\n" +
         "var MSG_A = goog.getMsg('msg A');" +
         "\n" +
         "var MSG_B = goog.getMsg('msg B');" +
         "var x = goog.getMsgWithFallback(MSG_A, MSG_B);",
         "var MSG_A = 'msg A';" +
         "var MSG_B = 'msg B';" +
         "var x = MSG_A;");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testNoUseFallback
  public void testNoUseFallback() {
    registerMessage(new JsMessage.Builder("MSG_A")
        .appendStringPart("translated")
        .build());
    test("\n" +
         "var MSG_A = goog.getMsg('msg A');" +
         "\n" +
         "var MSG_B = goog.getMsg('msg B');" +
         "var x = goog.getMsgWithFallback(MSG_A, MSG_B);",
         "var MSG_A = 'translated';" +
         "var MSG_B = 'msg B';" +
         "var x = MSG_A;");
  }

// com.google.javascript.jscomp.ReplaceMessagesTest::testNoUseFallback2
  public void testNoUseFallback2() {
    registerMessage(new JsMessage.Builder("MSG_C")
        .appendStringPart("translated")
        .build());
    test("\n" +
         "var MSG_A = goog.getMsg('msg A');" +
         "\n" +
         "var MSG_B = goog.getMsg('msg B');" +
         "var x = goog.getMsgWithFallback(MSG_A, MSG_B);",
         "var MSG_A = 'msg A';" +
         "var MSG_B = 'msg B';" +
         "var x = MSG_A;");
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStable1
  public void testStable1() {
    previous = VariableMap.fromMap(ImmutableMap.of("previous","xyz"));
    testDebugStrings(
        "Error('xyz');",
        "Error('previous');",
        (new String[] { "previous", "xyz" }));
    reserved = ImmutableSet.of("a", "b", "previous");
    testDebugStrings(
        "Error('xyz');",
        "Error('c');",
        (new String[] { "c", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testStable2
  public void testStable2() {
    
    
    
    
    
    previous = VariableMap.fromMap(ImmutableMap.of("a","unused"));
    testDebugStrings(
        "Error('xyz');",
        "Error('b');",
        (new String[] { "b", "xyz" }));
  }

// com.google.javascript.jscomp.ReplaceStringsTest::testThrowError1
  public void testThrowError1() {
    testDebugStrings(
        "throw Error('xyz');",
        "throw Error('a');",
        (new String[] { "a", "xyz" }));
    previous = VariableMap.fromMap(ImmutableMap.of("previous","xyz"));
    testDebugStrings(
        "throw Error('xyz');",
        "throw Error('previous');",
        (new String[] { "previous", "xyz" }));
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

// com.google.javascript.jscomp.RescopeGlobalSymbolsTest::testVarDeclarations
  public void testVarDeclarations() {
    test("var a = 1;", "_.a = 1;");
    test("var a = 1, b = 2, c = 3;", "_.a = 1; _.b = 2; _.c = 3;");
    test("var a = 'str', b = 1, c = { foo: 'bar' }, d = function() {};",
        "_.a = 'str'; _.b = 1; _.c = { foo: 'bar' }; _.d = function() {};");
    test("if(1){var x = 1;}", "if(1){_.x = 1;}");
    test("var x;", "");
    test("var a, b = 1;", "_.b = 1");
  }

// com.google.javascript.jscomp.RescopeGlobalSymbolsTest::testForLoops
  public void testForLoops() {
    test("for (var i = 0; i < 1000; i++);",
        "for (_.i = 0; _.i < 1000; _.i++);");
    test("for (var i = 0, c = 2; i < 1000; i++);",
        "for (_.i = 0, _.c = 2; _.i < 1000; _.i++);");
    test("for (var i = 0, c = 2, d = 3; i < 1000; i++);",
        "for (_.i = 0, _.c = 2, _.d = 3; _.i < 1000; _.i++);");
    test("for (var i = 0, c = 2, d = 3, e = 4; i < 1000; i++);",
        "for (_.i = 0, _.c = 2, _.d = 3, _.e = 4; _.i < 1000; _.i++);");
    test("for (var i = 0; i < 1000;)i++;",
        "for (_.i = 0; _.i < 1000;)_.i++;");
    test("for (var i = 0,b; i < 1000;)i++;b++",
        "for (_.i = 0,_.b; _.i < 1000;)_.i++;_.b++");
    test("var o={};for (var i in o)i++;",
        "_.o={};for (_.i in _.o)_.i++;");
  }

// com.google.javascript.jscomp.RescopeGlobalSymbolsTest::testFunctionStatements
  public void testFunctionStatements() {
    test("function test(){}",
        "_.test=function (){}");
    test("if(1)function test(){}",
        "if(1)_.test=function (){}");
    new StringCompare().testFreeCallSemantics();
  }

// com.google.javascript.jscomp.RescopeGlobalSymbolsTest::testDeeperScopes
  public void testDeeperScopes() {
    test("var a = function(b){return b}",
        "_.a = function(b){return b}");
    test("var a = function(b){var a; return a+b}",
        "_.a = function(b){var a; return a+b}");
    test("var a = function(a,b){return a+b}",
        "_.a = function(a,b){return a+b}");
    test("var x=1,a = function(b){var a; return a+b+x}",
        "_.x=1;_.a = function(b){var a; return a+b+_.x}");
    test("var x=1,a = function(b){return function(){var a;return a+b+x}}",
        "_.x=1;_.a = function(b){return function(){var a; return a+b+_.x}}");
  }

// com.google.javascript.jscomp.RescopeGlobalSymbolsTest::testTryCatch
  public void testTryCatch() {
    test("try{var a = 1}catch(e){throw e}",
        "try{_.a = 1}catch(e){throw e}");
  }

// com.google.javascript.jscomp.RescopeGlobalSymbolsTest::testShadow
  public void testShadow() {
    test("var _ = 1; (function () { _ = 2 })()",
        "_._ = 1; (function () { _._ = 2 })()");
    test("function foo() { var _ = {}; _.foo = foo; _.bar = 1; }",
        "_.foo = function () { var _$ = {}; _$.foo = _.foo; _$.bar = 1}");
    test("function foo() { var _ = {}; _.foo = foo; _.bar = 1; " +
        "(function() { var _ = 0;})() }",
        "_.foo = function () { var _$ = {}; _$.foo = _.foo; _$.bar = 1; " +
        "(function() { var _$ = 0;})() }");
    test("function foo() { var _ = {}; _.foo = foo; _.bar = 1; " +
        "var _$ = 1; }",
        "_.foo = function () { var _$ = {}; _$.foo = _.foo; _$.bar = 1; " +
        "var _$$ = 1; }");
    test("function foo() { var _ = {}; _.foo = foo; _.bar = 1; " +
        "var _$ = 1; (function() { _ = _$ })() }",
        "_.foo = function () { var _$ = {}; _$.foo = _.foo; _$.bar = 1; " +
        "var _$$ = 1; (function() { _$ = _$$ })() }");
    test("function foo() { var _ = {}; _.foo = foo; _.bar = 1; " +
        "var _$ = 1, _$$ = 2 (function() { _ = _$ = _$$; " +
        "var _$, _$$$ })() }",
        "_.foo = function () { var _$ = {}; _$.foo = _.foo; _$.bar = 1; " +
        "var _$$ = 1, _$$$ = 2 (function() { _$ = _$$ = _$$$; " +
        "var _$$, _$$$$ })() }");
    test("function foo() { var _a = 1;}",
        "_.foo = function () { var _a = 1;}");
    
    
    test("function foo() { var _$a = 1;}",
        "_.foo = function () { var _$a$ = 1;}");
  }

// com.google.javascript.jscomp.RescopeGlobalSymbolsTest::testExterns
  public void testExterns() {
    test("var document;",
        "document",
        "window.document", null, null);
    test("var document;",
        "document.getElementsByTagName('test')",
        "window.document.getElementsByTagName('test')", null, null);
    test("var document;",
        "window.document.getElementsByTagName('test')",
        "window.document.getElementsByTagName('test')", null, null);
    test("var document;document.getElementsByTagName",
        "document.getElementsByTagName('test')",
        "window.document.getElementsByTagName('test')", null, null);
    test("var document,navigator",
        "document.navigator;navigator",
        "window.document.navigator;window.navigator", null, null);
    test("var iframes",
        "function test() { iframes.resize(); }",
        "_.test = function() { window.iframes.resize(); }", null, null);
    test("var iframes",
        "var foo = iframes;",
        "_.foo = window.iframes;", null, null);
    
    test("var arguments, window, eval;",
        "arguments;window;eval;",
        "arguments;window;eval;", null, null);
    
    test("",
        "document",
        "window.document", null, null);
  }

// com.google.javascript.jscomp.RescopeGlobalSymbolsTest::testFreeCallSemantics
    public void testFreeCallSemantics() {
      test("function x(){};var y=function(){var val=x()||{}}",
          "_.x=function(){};_.y=function(){var val=(0,_.x)()||{}}");
      test("function x(){x()}",
          "_.x=function(){(0,_.x)()}");
    }

// com.google.javascript.jscomp.RhinoErrorReporterTest::testTrailingComma
  public void testTrailingComma() throws Exception {
    String message =
        "Parse error. IE8 (and below) will parse trailing commas in " +
        "array and object literals incorrectly. " +
        "If you are targeting newer versions of JS, " +
        "set the appropriate language_in option.";
    assertError(
        "var x = [1,];",
        RhinoErrorReporter.TRAILING_COMMA,
        message);
    JSError error = assertError(
        "var x = {\n" +
        "    1: 2,\n" +
        "};",
        RhinoErrorReporter.TRAILING_COMMA,
        message);

    assertEquals(2, error.getLineNumber());

    
    
    assertEquals(4, error.getCharno());
  }

// com.google.javascript.jscomp.RhinoErrorReporterTest::testMisplacedTypeAnnotation
  public void testMisplacedTypeAnnotation() throws Exception {
    reportMisplacedTypeAnnotations = false;

    assertNoWarningOrError("var x =  y;");

    reportMisplacedTypeAnnotations = true;

    String message =
        "Type annotations are not allowed here. " +
        "Are you missing parentheses?";
    assertWarning(
        "var x =  y;",
        RhinoErrorReporter.MISPLACED_TYPE_ANNOTATION,
        message);
    JSError error = assertWarning(
        "var x =  y;",
        RhinoErrorReporter.MISPLACED_TYPE_ANNOTATION,
        message);

    assertEquals(1, error.getLineNumber());

    
    
    assertEquals(0, error.getCharno());
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValue
  public void testValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testConstValue
  public void testConstValue() {
    
    
    testChecks(" function f(CONST) {}",
        "function f(CONST) {" +
        "  $jscomp.typecheck.checkType(CONST, " +
        "      [$jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValueWithInnerFn
  public void testValueWithInnerFn() {
    testChecks(" function f(i) { function g() {} }",
        "function f(i) {" +
        "  function g() {}" +
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.valueChecker('number')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNullValue
  public void testNullValue() {
    testChecks(" function f(i) {}",
        "function f(i) {" +
        "  $jscomp.typecheck.checkType(i, [$jscomp.typecheck.nullChecker]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testValues
  public void testValues() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.valueChecker('number')]);" +
        "  $jscomp.typecheck.checkType(j, " +
        "      [$jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testSkipParamOK
  public void testSkipParamOK() {
    testChecks(" function f(i, j) {}",
        "function f(i, j) {" +
        "  $jscomp.typecheck.checkType(j, " +
        "      [$jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testUnion
  public void testUnion() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  $jscomp.typecheck.checkType(x, [" +
        "      $jscomp.typecheck.valueChecker('number'), " +
        "      $jscomp.typecheck.valueChecker('string')" +
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
        "  return $jscomp.typecheck.checkType('x', " +
        "      [$jscomp.typecheck.valueChecker('string')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testNativeClass
  public void testNativeClass() {
    testChecks(" function f(x) {}",
        "function f(x) {" +
        "  $jscomp.typecheck.checkType(x, " +
        "      [$jscomp.typecheck.externClassChecker('String')]);" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testQualifiedClass
  public void testQualifiedClass() {
    testChecks("var goog = {}; goog.Foo = function() {};" +
        " function f(x) {}",
        "var goog = {}; goog.Foo = function() {};" +
        "goog.Foo.prototype['instance_of__goog.Foo'] = true;" +
        "function f(x) {" +
        "  $jscomp.typecheck.checkType(x, " +
        "    [$jscomp.typecheck.classChecker('goog.Foo')]);" +
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
        "  $jscomp.typecheck.checkType(i, " +
        "    [$jscomp.typecheck.interfaceChecker('I')])" +
        "}");
  }

// com.google.javascript.jscomp.RuntimeTypeCheckTest::testImplementedInterface
  public void testImplementedInterface() {
    testChecks("function I() {}" +
        "function f(i) {}" +
        "function C() {}",
        "function I() {}" +
        "function f(i) {" +
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.interfaceChecker('I')])" +
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
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.interfaceChecker('I')])" +
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
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.interfaceChecker('I')])" +
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
        "  $jscomp.typecheck.checkType(i, " +
        "      [$jscomp.typecheck.interfaceChecker('I')])" +
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

// com.google.javascript.jscomp.SanityCheckTest::testUnnormalizeNodeTypes
  public void testUnnormalizeNodeTypes() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().reportCodeChange();
        root.getFirstChild().addChildToBack(
              new Node(Token.IF, new Node(Token.TRUE), new Node(Token.EMPTY)));
      }
    };

    boolean exceptionCaught = false;
    try {
      test("var x = 3;", "var x=3;0;0");
    } catch (IllegalStateException e) {
      assertEquals("Expected BLOCK but was EMPTY Reference node EMPTY",
          e.getMessage());
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

// com.google.javascript.jscomp.SanityCheckTest::testUnnormalized
  public void testUnnormalized() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().setLifeCycleStage(LifeCycleStage.NORMALIZED);
      }
    };

    boolean exceptionCaught = false;
    try {
      test("while(1){}", "while(1){}");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(
          "Normalize constraints violated:\nWHILE node"));
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
  }

// com.google.javascript.jscomp.SanityCheckTest::testConstantAnnotationMismatch
  public void testConstantAnnotationMismatch() throws Exception {
    otherPass = new CompilerPass() {
      @Override public void process(Node externs, Node root) {
        getLastCompiler().reportCodeChange();
        Node name = Node.newString(Token.NAME, "x");
        name.putBooleanProp(Node.IS_CONSTANT_NAME, true);
        root.getFirstChild().addChildToBack(new Node(Token.EXPR_RESULT, name));
        getLastCompiler().setLifeCycleStage(LifeCycleStage.NORMALIZED);
      }
    };

    boolean exceptionCaught = false;
    try {
      test("var x;", "var x; x;");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains(
          "The name x is not consistently annotated as constant."));
      exceptionCaught = true;
    }
    assertTrue(exceptionCaught);
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

// com.google.javascript.jscomp.ScopedAliasesTest::testShadowedScopedVar
  public void testShadowedScopedVar() {
    test("var goog = {};" +
         "goog.bar = {};" +
         "goog.scope(function() {" +
         "  var bar = goog.bar;" +
         
         
         "  bar.newMethod = function(goog) { return goog + bar; };" +
         "});",
         "var goog={};" +
         "goog.bar={};" +
         "goog.bar.newMethod=function(goog$$1){return goog$$1 + goog.bar}");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testShadowedScopedVarTwoScopes
  public void testShadowedScopedVarTwoScopes() {
    test("var goog = {};" +
         "goog.bar = {};" +
         "goog.scope(function() {" +
         "  var bar = goog.bar;" +
         "  bar.newMethod = function(goog, a) { return bar + a; };" +
         "});" +
         "goog.scope(function() {" +
         "  var bar = goog.bar;" +
         "  bar.newMethod2 = function(goog, b) { return bar + b; };" +
         "});",
         "var goog={};" +
         "goog.bar={};" +
         "goog.bar.newMethod=function(goog$$1, a){return goog.bar + a};" +
         "goog.bar.newMethod2=function(goog$$1, b){return goog.bar + b};");
  }

// com.google.javascript.jscomp.ScopedAliasesTest::testUsingObjectLiteralToEscapeScoping
  public void testUsingObjectLiteralToEscapeScoping() {
    
    
    
    
    
    test(
        "var goog = {};" +
        "goog.bar = {};" +
        "goog.scope(function() {" +
        "  var bar = goog.bar;" +
        "  var baz = goog.bar.baz;" +
        "  goog.foo = function() {" +
        "    goog.bar = {baz: 3};" +
        "    return baz;" +
        "  };" +
        "});",
        "var goog = {};" +
        "goog.bar = {};" +
        "goog.foo = function(){" +
        "  goog.bar = {baz:3};" +
        "  return goog.bar.baz;" +
        "};");
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

// com.google.javascript.jscomp.ScopedAliasesTest::testForwardJsDoc
  public void testForwardJsDoc() {
    testScoped(
        "\n" +
        "foo.Foo = function() {};" +
        " foo.Foo.actual = function(x) {3};" +
        "var Foo = foo.Foo;" +
        " Foo.Bar = function() {};" +
        " foo.Foo.expected = function(x) {};",

        "\n" +
        "foo.Foo = function() {};" +
        " foo.Foo.actual = function(x) {3};" +
        " foo.Foo.Bar = function() {};" +
        " foo.Foo.expected = function(x) {};");
    verifyTypes();
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

// com.google.javascript.jscomp.ScopedAliasesTest::testIssue772
  public void testIssue772() {
    testTypes(
        "var b = a.b;" +
        "var c = b.c;",
        " types.actual;" +
        " types.expected;");
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
    testScopedFailure("function f() {}",
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
    verifyAliasTransformationPosition(1, 0, 2, 1, positions.get(0));

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
    verifyAliasTransformationPosition(1, 0, 3, 1, positions.get(0));

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

    verifyAliasTransformationPosition(1, 0, 6, 0, positions.get(0));

    verifyAliasTransformationPosition(8, 0, 11, 4, positions.get(1));

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

// com.google.javascript.jscomp.ShadowVariablesTest::testRenameMapHasNoDuplicates
  public void testRenameMapHasNoDuplicates() {
    test("function foo(x) { return function (y) {} }",
         "function   b(a) { return function (a) {} }");

    VariableMap vm = pass.getVariableMap();
    try {
      vm.getNewNameToOriginalNameMap();
    } catch (java.lang.IllegalArgumentException unexpected) {
      fail("Invalid VariableMap generated: " +
           vm.getOriginalNameToNewNameMap().toString());
    }
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
        ImmutableSet.of("DEF STRING_KEY null -> NUMBER",
                        "USE GETPROP o.a -> [NUMBER]"));

    
    checkDefinitionsInJs(
      "({'a' : 1}); o['a']",
      ImmutableSet.<String>of("DEF STRING_KEY null -> NUMBER"));

    checkDefinitionsInJs(
      "({1 : 1}); o[1]",
      ImmutableSet.<String>of("DEF STRING_KEY null -> NUMBER"));

    checkDefinitionsInJs(
        "var a = {b : 1}; a.b",
        ImmutableSet.of("DEF NAME a -> <null>",
                        "DEF STRING_KEY null -> NUMBER",
                        "USE NAME a -> [<null>]",
                        "USE GETPROP a.b -> [NUMBER]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineGet
  public void testDefineGet() throws Exception {
    
    checkDefinitionsInJs(
      "({get a() {}}); o.a",
      ImmutableSet.of("DEF GETTER_DEF null -> FUNCTION",
                      "USE GETPROP o.a -> [FUNCTION]"));
  }

// com.google.javascript.jscomp.SimpleDefinitionFinderTest::testDefineSet
  public void testDefineSet() throws Exception {
    
    checkDefinitionsInJs(
      "({set a(b) {}}); o.a",
      ImmutableSet.of("DEF NAME b -> <null>",
                      "DEF SETTER_DEF null -> FUNCTION",
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
            "DEF STRING_KEY null -> EXTERN NUMBER",
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

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement1
  public void testPrefixReplacement1() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("pre/","") );
    checkSourceMap2("", "pre/file1", "", "pre/file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"file1\",\"file2\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement2
    public void testPrefixReplacement2() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("pre/file","src") );
    checkSourceMap2("", "pre/file1", "", "pre/file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"src1\",\"src2\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement3
  public void testPrefixReplacement3() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("file1","x") );
    mappings.add( new SourceMap.LocationMapping("file2","y") );
    checkSourceMap2("", "file1", "", "file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"x\",\"y\"],\n" +
        "\"names\":[]\n" +
        "}\n");
  }

// com.google.javascript.jscomp.SourceMapTest::testPrefixReplacement4
  public void testPrefixReplacement4() throws IOException {
    mappings = new ArrayList<SourceMap.LocationMapping>();
    
    mappings.add( new SourceMap.LocationMapping("file1","x") );
    mappings.add( new SourceMap.LocationMapping("file","y") );
    checkSourceMap2("", "file1", "", "file2" , "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"\"],\n" +
        "\"mappings\":[],\n" +
        "\"sources\":[\"x\",\"y2\"],\n" +
        "\"names\":[]\n" +
        "}\n");
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

// com.google.javascript.jscomp.StrictModeCheckTest::testEval
  public void testEval() {
    test("function foo() { eval('a'); }", null,
         StrictModeCheck.EVAL_USE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval2
  public void testEval2() {
    testSame("function foo(eval) {}",
         StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval3
  public void testEval3() {
    testSame("function foo() {} foo.eval = 3;");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval4
  public void testEval4() {
    testSame("function foo() { var eval = 3; }",
         StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval5
  public void testEval5() {
    testSame("function eval() {}", StrictModeCheck.EVAL_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEval6
  public void testEval6() {
    testSame("try {} catch (eval) {}", StrictModeCheck.EVAL_DECLARATION);
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
    testSame("function foo(a) { a = b; }", StrictModeCheck.UNKNOWN_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable2
  public void testUnknownVariable2() {
    testSame("a: while (true) { a = 3; }", StrictModeCheck.UNKNOWN_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testUnknownVariable3
  public void testUnknownVariable3() {
    testSame("try {} catch (ex) { ex = 3; }");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments
  public void testArguments() {
    testSame("function foo(arguments) {}",
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments2
  public void testArguments2() {
    testSame("function foo() { var arguments = 3; }",
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments3
  public void testArguments3() {
    testSame("function arguments() {}",
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments4
  public void testArguments4() {
    testSame("try {} catch (arguments) {}",
         StrictModeCheck.ARGUMENTS_DECLARATION);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testArguments5
  public void testArguments5() {
    testSame("var o = {arguments: 3};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEvalAssignment
  public void testEvalAssignment() {
    noCajaChecks = true;
    testSame("function foo() { eval = []; }",
         StrictModeCheck.EVAL_ASSIGNMENT);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testEvalAssignment2
  public void testEvalAssignment2() {
    test("function foo() { eval = []; }", null, StrictModeCheck.EVAL_USE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testAssignToArguments
  public void testAssignToArguments() {
    testSame("function foo() { arguments = []; }",
         StrictModeCheck.ARGUMENTS_ASSIGNMENT);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteVar
  public void testDeleteVar() {
    testSame("var a; delete a", StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteFunction
  public void testDeleteFunction() {
    testSame("function a() {} delete a", StrictModeCheck.DELETE_VARIABLE);
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testDeleteArgument
  public void testDeleteArgument() {
    testSame("function b(a) { delete a; }",
        StrictModeCheck.DELETE_VARIABLE);
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

// com.google.javascript.jscomp.StrictModeCheckTest::testDuplicateObjectLiteralKey
  public void testDuplicateObjectLiteralKey() {
    testSame("var o = {a: 1, b: 2, c: 3};");
    testSame("var x = { get a() {}, set a(p) {} };");

    testSame("var o = {a: 1, b: 2, a: 3};",
        StrictModeCheck.DUPLICATE_OBJECT_KEY);
    testSame("var x = { get a() {}, get a() {} };",
         StrictModeCheck.DUPLICATE_OBJECT_KEY);
    testSame("var x = { get a() {}, a: 1 };",
         StrictModeCheck.DUPLICATE_OBJECT_KEY);
    testSame("var x = { set a(p) {}, a: 1 };",
         StrictModeCheck.DUPLICATE_OBJECT_KEY);

    testSame(
        "'use strict';\n" +
        "function App() {}\n" +
        "App.prototype = {\n" +
        "  get appData() { return this.appData_; },\n" +
        "  set appData(data) { this.appData_ = data; }\n" +
        "};");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testFunctionDecl
  public void testFunctionDecl() {
    testSame("function g() {}");
    testSame("var g = function() {};");
    testSame("(function() {})();");
    testSame("(function() {});");
    testSame(inFn("function g() {}"));
    testSame(inFn("var g = function() {};"));
    testSame(inFn("(function() {})();"));
    testSame(inFn("(function() {});"));

    test("{function g() {}}", null, StrictModeCheck.BAD_FUNCTION_DECLARATION);
    testSame("{var g = function () {}}");
    testSame("{(function g() {})()}");

    test("var x;if (x) { function g(){} }", null,
        StrictModeCheck.BAD_FUNCTION_DECLARATION);
    testSame("var x;if (x) {var g = function () {}}");
    testSame("var x;if (x) {(function g() {})()}");
  }

// com.google.javascript.jscomp.StrictModeCheckTest::testFunctionDecl2
  public void testFunctionDecl2() {
    test("{function g() {}}", null, StrictModeCheck.BAD_FUNCTION_DECLARATION);
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

// com.google.javascript.jscomp.StripCodeTest::testNewOperatior2
  public void testNewOperatior2() {
    test("function foo() {} foo.bar = (new goog.debug.Logger()).foo();",
         "function foo() {} foo.bar = null;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting1
  public void testCrazyNesting1() {
    test("var x = {}; x[new goog.debug.Logger()] = 3;",
         "var x = {}; x[null] = 3;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting2
  public void testCrazyNesting2() {
    test("var x = {}; x[goog.debug.Logger.getLogger()] = 3;",
         "var x = {}; x[null] = 3;");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting3
  public void testCrazyNesting3() {
    test("var x = function() {}; x(new goog.debug.Logger());",
         "var x = function() {}; x(null);");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting4
  public void testCrazyNesting4() {
    test("var x = function() {}; x(goog.debug.Logger.getLogger());",
         "var x = function() {}; x(null);");
  }

// com.google.javascript.jscomp.StripCodeTest::testCrazyNesting5
  public void testCrazyNesting5() {
    test("var x = function() {}; var y = {}; " +
         "var z = goog.debug.Logger.getLogger(); x(y[z['foo']]);",
         "var x = function() {}; var y = {}; x(y[null]);");
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVar
  public void testGlobalVar() throws Exception {
    SymbolTable table = createSymbolTable(
        " var x = 5;");
    assertNull(getGlobalVar(table, "y"));
    assertNotNull(getGlobalVar(table, "x"));
    assertEquals("number", getGlobalVar(table, "x").getType().toString());

    
    assertEquals(2, getVars(table).size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences
  public void testGlobalThisReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "var x = this; function f() { return this + this + this; }");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(1, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences2
  public void testGlobalThisReferences2() throws Exception {
    
    SymbolTable table = createSymbolTable("");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(0, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisReferences3
  public void testGlobalThisReferences3() throws Exception {
    SymbolTable table = createSymbolTable("this.foo = {}; this.foo.bar = {};");

    Symbol global = getGlobalVar(table, "*global*");
    assertNotNull(global);

    List<Reference> refs = table.getReferenceList(global);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalThisPropertyReferences
  public void testGlobalThisPropertyReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function Foo() {} this.Foo;");

    Symbol foo = getGlobalVar(table, "Foo");
    assertNotNull(foo);

    List<Reference> refs = table.getReferenceList(foo);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testGlobalVarReferences
  public void testGlobalVarReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " var x = 5; x = 6;");
    Symbol x = getGlobalVar(table, "x");
    List<Reference> refs = table.getReferenceList(x);

    assertEquals(2, refs.size());
    assertEquals(x.getDeclaration(), refs.get(0));
    assertEquals(Token.VAR, refs.get(0).getNode().getParent().getType());
    assertEquals(Token.ASSIGN, refs.get(1).getNode().getParent().getType());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalVarReferences
  public void testLocalVarReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        "function f(x) { return x; }");
    Symbol x = getLocalVar(table, "x");
    List<Reference> refs = table.getReferenceList(x);

    assertEquals(2, refs.size());
    assertEquals(x.getDeclaration(), refs.get(0));
    assertEquals(Token.PARAM_LIST, refs.get(0).getNode().getParent().getType());
    assertEquals(Token.RETURN, refs.get(1).getNode().getParent().getType());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences
  public void testLocalThisReferences() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() { this.foo = 3; this.bar = 5; }");

    Symbol f = getGlobalVar(table, "F");
    assertNotNull(f);

    Symbol t = table.getParameterInFunction(f, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences2
  public void testLocalThisReferences2() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() {}" +
        "F.prototype.baz = " +
        "    function() { this.foo = 3; this.bar = 5; };");

    Symbol baz = getGlobalVar(table, "F.prototype.baz");
    assertNotNull(baz);

    Symbol t = table.getParameterInFunction(baz, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(2, refs.size());
  }

// com.google.javascript.jscomp.SymbolTableTest::testLocalThisReferences3
  public void testLocalThisReferences3() throws Exception {
    SymbolTable table = createSymbolTable(
        " function F() {}");

    Symbol baz = getGlobalVar(table, "F");
    assertNotNull(baz);

    Symbol t = table.getParameterInFunction(baz, "this");
    assertNotNull(t);

    List<Reference> refs = table.getReferenceList(t);
    assertEquals(0, refs.size());
  }
