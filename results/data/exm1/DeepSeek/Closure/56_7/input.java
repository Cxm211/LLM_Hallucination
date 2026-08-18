// buggy code
  public String getLine(int lineNumber) {
    String js = "";
    try {
      // NOTE(nicksantos): Right now, this is optimized for few warnings.
      // This is probably the right trade-off, but will be slow if there
      // are lots of warnings in one file.
      js = getCode();
    } catch (IOException e) {
      return null;
    }

    int pos = 0;
    int startLine = 1;

    // If we've saved a previous offset and it's for a line less than the
    // one we're searching for, then start at that point.
    if (lineNumber >= lastLine) {
      pos = lastOffset;
      startLine = lastLine;
    }

    for (int n = startLine; n < lineNumber; n++) {
      int nextpos = js.indexOf('\n', pos);
      if (nextpos == -1) {
        return null;
      }
      pos = nextpos + 1;
    }

    // Remember this offset for the next search we do.
    lastOffset = pos;
    lastLine = lineNumber;

    if (js.indexOf('\n', pos) == -1) {
      // If next new line cannot be found, there are two cases
      // 1. pos already reaches the end of file, then null should be returned
      // 2. otherwise, return the contents between pos and the end of file.
        return null;
    } else {
      return js.substring(pos, js.indexOf('\n', pos));
    }
  }

// relevant test
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

// com.google.javascript.jscomp.InstrumentFunctionsTest::testNestedExit
  public void testNestedExit() {
    this.instrumentationPb = "report_exit: \"$$testExit\"\n" +
        "report_defined: \"$$testDefine\"";
    test("function a(){ return function(){ return c;}}",
         "$$testDefine(1);function a(){$$testDefine(0);" +
         "return $$testExit(1, function(){return $$testExit(0, c);});}");
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

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptOneLine
  public void testExcerptOneLine() throws Exception {
    assertEquals("foo:first line", provider.getSourceLine("foo", 1));
    assertEquals("foo:second line", provider.getSourceLine("foo", 2));
    assertEquals("foo:third line", provider.getSourceLine("foo", 3));
    assertEquals("bar:first line", provider.getSourceLine("bar", 1));
    assertEquals("bar:second line", provider.getSourceLine("bar", 2));
    assertEquals("bar:third line", provider.getSourceLine("bar", 3));
    assertEquals("bar:fourth line", provider.getSourceLine("bar", 4));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptLineFromInexistantSource
  public void testExcerptLineFromInexistantSource() throws Exception {
    assertEquals(null, provider.getSourceLine("inexistant", 1));
    assertEquals(null, provider.getSourceLine("inexistant", 7));
    assertEquals(null, provider.getSourceLine("inexistant", 90));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptInexistantLine
  public void testExcerptInexistantLine() throws Exception {
    assertEquals(null, provider.getSourceLine("foo", 0));
    assertEquals(null, provider.getSourceLine("foo", 4));
    assertEquals(null, provider.getSourceLine("bar", 0));
    assertEquals(null, provider.getSourceLine("bar", 5));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExceptNoNewLine
  public void testExceptNoNewLine() throws Exception {
    assertEquals("foo2:first line", provider.getSourceLine("foo2", 1));
    assertEquals("foo2:second line", provider.getSourceLine("foo2", 2));
    assertEquals("foo2:third line", provider.getSourceLine("foo2", 3));
    assertEquals(null, provider.getSourceLine("foo2", 4));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptRegion
  public void testExcerptRegion() throws Exception {
    assertRegionWellFormed("foo", 1);
    assertRegionWellFormed("foo", 2);
    assertRegionWellFormed("foo", 3);
    assertRegionWellFormed("bar", 1);
    assertRegionWellFormed("bar", 2);
    assertRegionWellFormed("bar", 3);
    assertRegionWellFormed("bar", 4);
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptRegionFromInexistantSource
  public void testExcerptRegionFromInexistantSource() throws Exception {
    assertEquals(null, provider.getSourceRegion("inexistant", 0));
    assertEquals(null, provider.getSourceRegion("inexistant", 6));
    assertEquals(null, provider.getSourceRegion("inexistant", 90));
  }

// com.google.javascript.jscomp.JSCompilerSourceExcerptProviderTest::testExcerptInexistantRegion
  public void testExcerptInexistantRegion() throws Exception {
    assertEquals(null, provider.getSourceRegion("foo", 0));
    assertEquals(null, provider.getSourceRegion("foo", 4));
    assertEquals(null, provider.getSourceRegion("bar", 0));
    assertEquals(null, provider.getSourceRegion("bar", 5));
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testModuleDepth
  public void testModuleDepth() {
    assertEquals("A should have depth 0", 0, A.getDepth());
    assertEquals("B should have depth 1", 1, B.getDepth());
    assertEquals("C should have depth 1", 1, C.getDepth());
    assertEquals("D should have depth 2", 2, D.getDepth());
    assertEquals("E should have depth 2", 2, E.getDepth());
    assertEquals("F should have depth 3", 3, F.getDepth());
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testDeepestCommonDep
  public void testDeepestCommonDep() {
    assertDeepestCommonDep(null, A, A);
    assertDeepestCommonDep(null, A, B);
    assertDeepestCommonDep(null, A, C);
    assertDeepestCommonDep(null, A, D);
    assertDeepestCommonDep(null, A, E);
    assertDeepestCommonDep(null, A, F);
    assertDeepestCommonDep(A, B, B);
    assertDeepestCommonDep(A, B, C);
    assertDeepestCommonDep(A, B, D);
    assertDeepestCommonDep(A, B, E);
    assertDeepestCommonDep(A, B, F);
    assertDeepestCommonDep(A, C, C);
    assertDeepestCommonDep(A, C, D);
    assertDeepestCommonDep(A, C, E);
    assertDeepestCommonDep(A, C, F);
    assertDeepestCommonDep(B, D, D);
    assertDeepestCommonDep(B, D, E);
    assertDeepestCommonDep(B, D, F);
    assertDeepestCommonDep(C, E, E);
    assertDeepestCommonDep(C, E, F);
    assertDeepestCommonDep(E, F, F);
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testDeepestCommonDepInclusive
  public void testDeepestCommonDepInclusive() {
    assertDeepestCommonDepInclusive(A, A, A);
    assertDeepestCommonDepInclusive(A, A, B);
    assertDeepestCommonDepInclusive(A, A, C);
    assertDeepestCommonDepInclusive(A, A, D);
    assertDeepestCommonDepInclusive(A, A, E);
    assertDeepestCommonDepInclusive(A, A, F);
    assertDeepestCommonDepInclusive(B, B, B);
    assertDeepestCommonDepInclusive(A, B, C);
    assertDeepestCommonDepInclusive(B, B, D);
    assertDeepestCommonDepInclusive(B, B, E);
    assertDeepestCommonDepInclusive(B, B, F);
    assertDeepestCommonDepInclusive(C, C, C);
    assertDeepestCommonDepInclusive(A, C, D);
    assertDeepestCommonDepInclusive(C, C, E);
    assertDeepestCommonDepInclusive(C, C, F);
    assertDeepestCommonDepInclusive(D, D, D);
    assertDeepestCommonDepInclusive(B, D, E);
    assertDeepestCommonDepInclusive(B, D, F);
    assertDeepestCommonDepInclusive(E, E, E);
    assertDeepestCommonDepInclusive(E, E, F);
    assertDeepestCommonDepInclusive(F, F, F);
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testGetTransitiveDepsDeepestFirst
  public void testGetTransitiveDepsDeepestFirst() {
    assertTransitiveDepsDeepestFirst(A);
    assertTransitiveDepsDeepestFirst(B, A);
    assertTransitiveDepsDeepestFirst(C, A);
    assertTransitiveDepsDeepestFirst(D, B, A);
    assertTransitiveDepsDeepestFirst(E, C, B, A);
    assertTransitiveDepsDeepestFirst(F, E, C, B, A);
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testCoalesceDuplicateFiles
  public void testCoalesceDuplicateFiles() {
    A.add(JSSourceFile.fromCode("a.js", ""));

    B.add(JSSourceFile.fromCode("a.js", ""));
    B.add(JSSourceFile.fromCode("b.js", ""));

    C.add(JSSourceFile.fromCode("b.js", ""));
    C.add(JSSourceFile.fromCode("c.js", ""));

    E.add(JSSourceFile.fromCode("c.js", ""));
    E.add(JSSourceFile.fromCode("d.js", ""));

    graph.coalesceDuplicateFiles();

    assertEquals(2, A.getInputs().size());
    assertEquals("a.js", A.getInputs().get(0).getName());
    assertEquals("b.js", A.getInputs().get(1).getName());
    assertEquals(0, B.getInputs().size());
    assertEquals(1, C.getInputs().size());
    assertEquals("c.js", C.getInputs().get(0).getName());
    assertEquals(1, E.getInputs().size());
    assertEquals("d.js", E.getInputs().get(0).getName());
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testManageDependencies1
  public void testManageDependencies1() throws Exception {
    List<CompilerInput> inputs = setUpManageDependenciesTest();
    List<CompilerInput> results = graph.manageDependencies(
        ImmutableList.<String>of(), inputs);

    assertInputs(A, "a1", "a3");
    assertInputs(B, "a2", "b2");
    assertInputs(C); 
    assertInputs(E, "c1", "e1", "e2");

    assertEquals(
        Lists.newArrayList("a1", "a3", "a2", "b2", "c1", "e1", "e2"),
        sourceNames(results));
  }

// com.google.javascript.jscomp.JSModuleGraphTest::testManageDependencies2
  public void testManageDependencies2() throws Exception {
    List<CompilerInput> inputs = setUpManageDependenciesTest();
    List<CompilerInput> results = graph.manageDependencies(
        ImmutableList.<String>of("c2"), inputs);

    assertInputs(A, "a1", "a3");
    assertInputs(B, "a2", "b2");
    assertInputs(C, "c1", "c2");
    assertInputs(E, "e1", "e2");

    assertEquals(
        Lists.newArrayList("a1", "a3", "a2", "b2", "c1", "c2", "e1", "e2"),
        sourceNames(results));
  }

// com.google.javascript.jscomp.JSModuleTest::testDependencies
  public void testDependencies() {
    assertEquals(ImmutableSet.of(), mod1.getAllDependencies());
    assertEquals(ImmutableSet.of(mod1), mod2.getAllDependencies());
    assertEquals(ImmutableSet.of(mod1), mod3.getAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod2, mod3), mod4.getAllDependencies());

    assertEquals(ImmutableSet.of(mod1), mod1.getThisAndAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod2), mod2.getThisAndAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod3), mod3.getThisAndAllDependencies());
    assertEquals(ImmutableSet.of(mod1, mod2, mod3, mod4),
                 mod4.getThisAndAllDependencies());
  }

// com.google.javascript.jscomp.JSModuleTest::testSortInputs
  public void testSortInputs() throws Exception {
    CompilerInput a = new CompilerInput(
        JSSourceFile.fromCode("a.js",
            "goog.require('b');goog.require('c')"));
    CompilerInput b = new CompilerInput(
        JSSourceFile.fromCode("b.js",
            "goog.provide('b');goog.require('d')"));
    CompilerInput c = new CompilerInput(
        JSSourceFile.fromCode("c.js",
            "goog.provide('c');goog.require('d')"));
    CompilerInput d = new CompilerInput(
        JSSourceFile.fromCode("d.js",
            "goog.provide('d')"));

    
    CompilerInput e = new CompilerInput(
        JSSourceFile.fromCode("e.js",
            "goog.provide('e')"));
    CompilerInput f = new CompilerInput(
        JSSourceFile.fromCode("f.js",
            "goog.provide('f')"));

    assertSortedInputs(
        ImmutableList.of(d, b, c, a),
        ImmutableList.of(a, b, c, d));
    assertSortedInputs(
        ImmutableList.of(d, b, c, a),
        ImmutableList.of(d, b, c, a));
    assertSortedInputs(
        ImmutableList.of(d, c, b, a),
        ImmutableList.of(d, c, b, a));
    assertSortedInputs(
        ImmutableList.of(d, b, c, a),
        ImmutableList.of(d, a, b, c));
  }

// com.google.javascript.jscomp.JSModuleTest::testSortJsModules
  public void testSortJsModules() throws Exception {
    
    assertEquals(ImmutableList.of(mod1, mod2, mod3, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod1, mod2, mod3, mod4))));
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod1, mod3, mod2, mod4))));

    
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod4, mod3, mod2, mod1))));
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod3, mod1, mod2, mod4))));

    
    assertEquals(ImmutableList.of(mod1, mod3, mod2, mod4),
        Arrays.asList(JSModule.sortJsModules(
            ImmutableList.of(mod4, mod3, mod1, mod2))));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testSyntaxError1
  public void testSyntaxError1() {
    try {
      extractMessage("if (true) {}}");
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("JSCompiler errors\n"));
      assertTrue(e.getMessage().contains(
          "testcode:1: ERROR - Parse error. syntax error\n"));
      assertTrue(e.getMessage().contains("if (true) {}}\n"));
    }
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testSyntaxError2
  public void testSyntaxError2() {
    try {
      extractMessage("", "if (true) {}}");
      fail("Expected exception");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("JSCompiler errors\n"));
      assertTrue(e.getMessage().contains(
          "testcode:2: ERROR - Parse error. syntax error\n"));
      assertTrue(e.getMessage().contains("if (true) {}}\n"));
    }
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractNewStyleMessage1
  public void testExtractNewStyleMessage1() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .build(),
        extractMessage("var MSG_SILLY = goog.getMsg('silly test message');"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractNewStyleMessage2
  public void testExtractNewStyleMessage2() {
    
    assertEquals(
        new JsMessage.Builder("MSG_WELCOME")
            .appendStringPart("Hi ")
            .appendPlaceholderReference("userName")
            .appendStringPart("! Welcome to ")
            .appendPlaceholderReference("product")
            .appendStringPart(".")
            .setDesc("The welcome message.")
            .setIsHidden(true)
            .build(),
        extractMessage(
            "",
            "var MSG_WELCOME = goog.getMsg(",
            "    'Hi {$userName}! Welcome to {$product}.',",
            "    {userName: someUserName, product: getProductName()});"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage1
  public void testExtractOldStyleMessage1() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .setDesc("Description.")
            .build(),
        extractMessage(
            "var MSG_SILLY_HELP = 'Description.';",
            "var MSG_SILLY = 'silly test message';"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage2
  public void testExtractOldStyleMessage2() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendStringPart("silly test message")
            .setDesc("Description.")
            .build(),
        extractMessage(
            "var MSG_SILLY = 'silly test message';",
            "var MSG_SILLY_HELP = 'Descrip' + 'tion.';"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractOldStyleMessage3
  public void testExtractOldStyleMessage3() {
    
    assertEquals(
        new JsMessage.Builder("MSG_SILLY")
            .appendPlaceholderReference("one")
            .appendStringPart(", ")
            .appendPlaceholderReference("two")
            .appendStringPart(", buckle my shoe")
            .build(),
        extractMessage(
            "var MSG_SILLY = function(one, two) {",
            "  return one + ', ' + two + ', buckle my shoe';",
            "};"));
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testExtractMixedMessages
  public void testExtractMixedMessages() {
    
    Iterator<JsMessage> msgs = extractMessages(
        "var MSG_MONEY = function(amount) {",
        "  return 'You owe $' + amount +",
        "         ' to the credit card company.';",
        "};",
        "var MSG_TIME = goog.getMsg('You need to finish your work in ' +",
        "                           '{$duration} hours.', {'duration': d});",
        "var MSG_NAG = 'Clean your room.\\n\\nWash your clothes.';",
        "var MSG_NAG_HELP = 'Just some ' +",
        "                   'nags.';").iterator();

    assertEquals(
        new JsMessage.Builder("MSG_MONEY")
            .appendStringPart("You owe $")
            .appendPlaceholderReference("amount")
            .appendStringPart(" to the credit card company.")
            .build(),
        msgs.next());
    assertEquals(
        new JsMessage.Builder("MSG_TIME")
            .appendStringPart("You need to finish your work in ")
            .appendPlaceholderReference("duration")
            .appendStringPart(" hours.")
            .build(),
        msgs.next());
    assertEquals(
        new JsMessage.Builder("MSG_NAG")
            .appendStringPart("Clean your room.\n\nWash your clothes.")
            .setDesc("Just some nags.")
            .build(),
        msgs.next());
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testDuplicateUnnamedVariables
  public void testDuplicateUnnamedVariables() {
    
    
    Collection<JsMessage> msgs = extractMessages(
        "function a() {",
        "  var MSG_UNNAMED_2 = goog.getMsg('foo');",
        "}",
        "function b() {",
        "  var MSG_UNNAMED_2 = goog.getMsg('bar');",
        "}");

    assertEquals(2, msgs.size());
    final Iterator<JsMessage> iter = msgs.iterator();
    assertEquals("foo", iter.next().toString());
    assertEquals("bar", iter.next().toString());
  }

// com.google.javascript.jscomp.JsMessageExtractorTest::testMeaningAnnotation
  public void testMeaningAnnotation() {
    List<JsMessage> msgs = Lists.newArrayList(
        extractMessages(
            "var MSG_UNNAMED_1 = goog.getMsg('foo');",
            "var MSG_UNNAMED_2 = goog.getMsg('foo');"));
    assertEquals(2, msgs.size());
    assertTrue(msgs.get(0).getId().equals(msgs.get(1).getId()));
    assertEquals(msgs.get(0), msgs.get(1));

    msgs = Lists.newArrayList(
        extractMessages(
            "var MSG_UNNAMED_1 = goog.getMsg('foo');",
            " var MSG_UNNAMED_2 = goog.getMsg('foo');"));
    assertEquals(2, msgs.size());
    assertFalse(msgs.get(0).getId().equals(msgs.get(1).getId()));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testJsMessageOnVar
  public void testJsMessageOnVar() {
    extractMessagesSafely(
        " var MSG_HELLO = goog.getMsg('a')");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_HELLO", msg.getKey());
    assertEquals("Hello", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testJsMessageOnProperty
  public void testJsMessageOnProperty() {
    extractMessagesSafely(" " +
        "pint.sub.MSG_MENU_MARK_AS_UNREAD = goog.getMsg('a')");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_MENU_MARK_AS_UNREAD", msg.getKey());
    assertEquals("a", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testOrphanedJsMessage
  public void testOrphanedJsMessage() {
    extractMessagesSafely("goog.getMsg('a')");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(0, messages.size());

    JSError warn = compiler.getWarnings()[0];
    assertEquals(JsMessageVisitor.MESSAGE_NODE_IS_ORPHANED, warn.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMessageWithoutDescription
  public void testMessageWithoutDescription() {
    extractMessagesSafely("var MSG_HELLO = goog.getMsg('a')");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(1, messages.size());

    JsMessage msg = messages.get(0);
    assertEquals("MSG_HELLO", msg.getKey());

    assertEquals(JsMessageVisitor.MESSAGE_HAS_NO_DESCRIPTION,
        compiler.getWarnings()[0].getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIncorrectMessageReporting
  public void testIncorrectMessageReporting() {
    extractMessages("var MSG_HELLO = goog.getMsg('a' + + 'b')");
    assertEquals(1, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, messages.size());

    JSError mailformedTreeError = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED,
        mailformedTreeError.getType());
    assertEquals("Message parse tree malformed. "
        + "STRING or ADD node expected; found: POS",
        mailformedTreeError.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyMessage
  public void testEmptyMessage() {
    
    extractMessagesSafely("var MSG_EMPTY = '';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_EMPTY", msg.getKey());
    assertEquals("", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testConcatOfStrings
  public void testConcatOfStrings() {
    extractMessagesSafely("var MSG_NOTEMPTY = 'aa' + 'bbb' \n + ' ccc';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_NOTEMPTY", msg.getKey());
    assertEquals("aabbb ccc", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyFormatDescription
  public void testLegacyFormatDescription() {
    extractMessagesSafely("var MSG_SILLY = 'silly test message';\n"
        + "var MSG_SILLY_HELP = 'help text';");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("silly test message", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyFormatParametizedFunction
  public void testLegacyFormatParametizedFunction() {
    extractMessagesSafely("var MSG_SILLY = function(one, two) {"
        + "  return one + ', ' + two + ', buckle my shoe';"
        + "};");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals(null, msg.getDesc());
    assertEquals("{$one}, {$two}, buckle my shoe", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyMessageWithDescAnnotation
  public void testLegacyMessageWithDescAnnotation() {
    
    
    extractMessagesSafely(
        " var MSG_A = 'The Message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_A", msg.getKey());
    assertEquals("The Message", msg.toString());
    assertEquals("The description", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testLegacyMessageWithDescAnnotationAndHelpVar
  public void testLegacyMessageWithDescAnnotationAndHelpVar() {
    
    
    extractMessagesSafely(
        "var MSG_A_HELP = 'This is a help var';\n" +
        " var MSG_A = 'The Message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_A", msg.getKey());
    assertEquals("The Message", msg.toString());
    assertEquals("The description in @desc", msg.getDesc());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureMessageWithHelpPostfix
  public void testClosureMessageWithHelpPostfix() {
    extractMessagesSafely("\n"
        + "var MSG_FOO_HELP = goog.getMsg('Help!');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_FOO_HELP", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("Help!", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureMessageWithoutGoogGetmsg
  public void testClosureMessageWithoutGoogGetmsg() {
    allowLegacyMessages = false;

    extractMessages("var MSG_FOO_HELP = 'I am a bad message';");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_NOT_INITIALIZED_USING_NEW_SYNTAX,
        error.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testClosureFormatParametizedFunction
  public void testClosureFormatParametizedFunction() {
    extractMessagesSafely(""
        + "var MSG_SILLY = goog.getMsg('{$adjective} ' + 'message', "
        + "{'adjective': 'silly'});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("help text", msg.getDesc());
    assertEquals("{$adjective} message", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testHugeMessage
  public void testHugeMessage() {
    extractMessagesSafely("" +
        "var MSG_HUGE = goog.getMsg(" +
        "    '{$startLink_1}Google{$endLink}' +" +
        "    '{$startLink_2}blah{$endLink}{$boo}{$foo_001}{$boo}' +" +
        "    '{$foo_002}{$xxx_001}{$image}{$image_001}{$xxx_002}'," +
        "    {'startLink_1': '<a href=http://www.google.com/>'," +
        "     'endLink': '</a>'," +
        "     'startLink_2': '<a href=\"' + opt_data.url + '\">'," +
        "     'boo': opt_data.boo," +
        "     'foo_001': opt_data.foo," +
        "     'foo_002': opt_data.boo.foo," +
        "     'xxx_001': opt_data.boo + opt_data.foo," +
        "     'image': htmlTag7," +
        "     'image_001': opt_data.image," +
        "     'xxx_002': foo.callWithOnlyTopLevelKeys(" +
        "         bogusFn, opt_data, null, 'bogusKey1'," +
        "         opt_data.moo, 'bogusKey2', param10)});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_HUGE", msg.getKey());
    assertEquals("A message with lots of stuff.", msg.getDesc());
    assertTrue(msg.isHidden());
    assertEquals("{$startLink_1}Google{$endLink}{$startLink_2}blah{$endLink}" +
        "{$boo}{$foo_001}{$boo}{$foo_002}{$xxx_001}{$image}" +
        "{$image_001}{$xxx_002}", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnnamedGoogleMessage
  public void testUnnamedGoogleMessage() {
    extractMessagesSafely("var MSG_UNNAMED_2 = goog.getMsg('Hullo');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals(null, msg.getDesc());
    assertEquals("MSG_16LJMYKCXT84X", msg.getKey());
    assertEquals("MSG_16LJMYKCXT84X", msg.getId());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyTextMessage
  public void testEmptyTextMessage() {
    extractMessagesSafely(" var MSG_FOO = goog.getMsg('');");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    assertEquals("Message value of MSG_FOO is just an empty string. "
        + "Empty messages are forbidden.",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testEmptyTextComplexMessage
  public void testEmptyTextComplexMessage() {
    extractMessagesSafely(" var MSG_BAR = goog.getMsg("
        + "'' + '' + ''     + ''\n+'');");

    assertEquals(1, messages.size());
    assertEquals(1, compiler.getWarningCount());
    assertEquals("Message value of MSG_BAR is just an empty string. "
        + "Empty messages are forbidden.",
        compiler.getWarnings()[0].description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMessageIsNoUnnamed
  public void testMessageIsNoUnnamed() {
    extractMessagesSafely("var MSG_UNNAMED_ITEM = goog.getMsg('Hullo');");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_UNNAMED_ITEM", msg.getKey());
    assertFalse(msg.isHidden());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMsgVarWithoutAssignment
  public void testMsgVarWithoutAssignment() {
    extractMessages("var MSG_SILLY;");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals(JsMessageVisitor.MESSAGE_HAS_NO_VALUE, error.getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testRegularVarWithoutAssignment
  public void testRegularVarWithoutAssignment() {
    extractMessagesSafely("var SILLY;");

    assertTrue(messages.isEmpty());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testMsgVarWithIncorrectRightSide
  public void testMsgVarWithIncorrectRightSide() {
    extractMessages("var MSG_SILLY = 0;");

    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. Cannot parse value of "
        + "message MSG_SILLY", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIncorrectMessage
  public void testIncorrectMessage() {
    extractMessages("DP_DatePicker.MSG_DATE_SELECTION = {};");

    assertEquals(0, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. "+
                 "Message must be initialized using goog.getMsg function.",
                 error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnrecognizedFunction
  public void testUnrecognizedFunction() {
    allowLegacyMessages = false;
    extractMessages("DP_DatePicker.MSG_DATE_SELECTION = somefunc('a')");

    assertEquals(0, messages.size());
    assertEquals(1, compiler.getErrors().length);
    JSError error = compiler.getErrors()[0];
    assertEquals("Message parse tree malformed. "+
                 "Message initialized using unrecognized function. " +
                 "Please use goog.getMsg() instead.",
                 error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testExtractPropertyMessage
  public void testExtractPropertyMessage() {
    extractMessagesSafely(""
        + "a.b.MSG_SILLY = goog.getMsg(\n"
        + "    '{$adjective} ' + '{$someNoun}',\n"
        + "    {'adjective': adj, 'someNoun': noun});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_SILLY", msg.getKey());
    assertEquals("{$adjective} {$someNoun}", msg.toString());
    assertEquals("A message that demonstrates placeholders", msg.getDesc());
    assertTrue(msg.isHidden());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testAlmostButNotExternalMessage
  public void testAlmostButNotExternalMessage() {
    extractMessagesSafely(
        " var MSG_EXTERNAL = goog.getMsg('External');");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());
    assertFalse(messages.get(0).isExternal());
    assertEquals("MSG_EXTERNAL", messages.get(0).getKey());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testExternalMessage
  public void testExternalMessage() {
    extractMessagesSafely("var MSG_EXTERNAL_111 = goog.getMsg('Hello World');");
    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, messages.size());
    assertTrue(messages.get(0).isExternal());
    assertEquals("111", messages.get(0).getId());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameStrict
  public void testIsValidMessageNameStrict() {
    JsMessageVisitor visitor = new DummyJsVisitor(CLOSURE);

    assertTrue(visitor.isMessageName("MSG_HELLO", true));
    assertTrue(visitor.isMessageName("MSG_", true));
    assertTrue(visitor.isMessageName("MSG_HELP", true));
    assertTrue(visitor.isMessageName("MSG_FOO_HELP", true));

    assertFalse(visitor.isMessageName("_FOO_HELP", true));
    assertFalse(visitor.isMessageName("MSGFOOP", true));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameRelax
  public void testIsValidMessageNameRelax() {
    JsMessageVisitor visitor = new DummyJsVisitor(RELAX);

    assertFalse(visitor.isMessageName("MSG_HELP", false));
    assertFalse(visitor.isMessageName("MSG_FOO_HELP", false));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsValidMessageNameLegacy
  public void testIsValidMessageNameLegacy() {
    theseAreLegacyMessageNames(new DummyJsVisitor(RELAX));
    theseAreLegacyMessageNames(new DummyJsVisitor(LEGACY));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnexistedPlaceholders
  public void testUnexistedPlaceholders() {
    extractMessages("var MSG_FOO = goog.getMsg('{$foo}:', {});");

    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Unrecognized message "
        + "placeholder referenced: foo", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnusedReferenesAreNotOK
  public void testUnusedReferenesAreNotOK() {
    extractMessages(" "
        + "var MSG_FOO = goog.getMsg('lalala:', {foo:1});");
    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Unused message placeholder: "
        + "foo", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testDuplicatePlaceHoldersAreBad
  public void testDuplicatePlaceHoldersAreBad() {
    extractMessages("var MSG_FOO = goog.getMsg("
        + "'{$foo}:', {'foo': 1, 'foo' : 2});");

    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Duplicate placeholder "
        + "name: foo", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testDuplicatePlaceholderReferencesAreOk
  public void testDuplicatePlaceholderReferencesAreOk() {
    extractMessagesSafely("var MSG_FOO = goog.getMsg("
        + "'{$foo}:, {$foo}', {'foo': 1});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("{$foo}:, {$foo}", msg.toString());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testCamelcasePlaceholderNamesAreOk
  public void testCamelcasePlaceholderNamesAreOk() {
    extractMessagesSafely("var MSG_WITH_CAMELCASE = goog.getMsg("
        + "'Slide {$slideNumber}:', {'slideNumber': opt_index + 1});");

    assertEquals(1, messages.size());
    JsMessage msg = messages.get(0);
    assertEquals("MSG_WITH_CAMELCASE", msg.getKey());
    assertEquals("Slide {$slideNumber}:", msg.toString());
    List<CharSequence> parts = msg.parts();
    assertEquals(3, parts.size());
    assertEquals("slideNumber",
        ((JsMessage.PlaceholderReference)parts.get(1)).getName());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testWithNonCamelcasePlaceholderNamesAreNotOk
  public void testWithNonCamelcasePlaceholderNamesAreNotOk() {
    extractMessages("var MSG_WITH_CAMELCASE = goog.getMsg("
        + "'Slide {$slide_number}:', {'slide_number': opt_index + 1});");

    assertEquals(0, messages.size());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    JSError error = errors[0];
    assertEquals(JsMessageVisitor.MESSAGE_TREE_MALFORMED, error.getType());
    assertEquals("Message parse tree malformed. Placeholder name not in "
        + "lowerCamelCase: slide_number", error.description);
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testUnquotedPlaceholdersAreOk
  public void testUnquotedPlaceholdersAreOk() {
    extractMessagesSafely(" "
        + "var MSG_FOO = goog.getMsg('foo {$unquoted}:', {unquoted: 12});");

    assertEquals(1, messages.size());
    assertEquals(0, compiler.getWarningCount());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testIsLowerCamelCaseWithNumericSuffixes
  public void testIsLowerCamelCaseWithNumericSuffixes() {
    assertTrue(isLowerCamelCaseWithNumericSuffixes("name"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("NAME"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("Name"));

    assertTrue(isLowerCamelCaseWithNumericSuffixes("a4Letter"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("A4_LETTER"));

    assertTrue(isLowerCamelCaseWithNumericSuffixes("startSpan_1_23"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("startSpan_1_23b"));
    assertFalse(isLowerCamelCaseWithNumericSuffixes("START_SPAN_1_23"));

    assertFalse(isLowerCamelCaseWithNumericSuffixes(""));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testToLowerCamelCaseWithNumericSuffixes
  public void testToLowerCamelCaseWithNumericSuffixes() {
    assertEquals("name", toLowerCamelCaseWithNumericSuffixes("NAME"));
    assertEquals("a4Letter", toLowerCamelCaseWithNumericSuffixes("A4_LETTER"));
    assertEquals("startSpan_1_23",
        toLowerCamelCaseWithNumericSuffixes("START_SPAN_1_23"));
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testDuplicateMessageError
  public void testDuplicateMessageError() {
    extractMessages(
        "(function () { var MSG_HELLO = goog.getMsg('a')})" +
        "(function () { var MSG_HELLO = goog.getMsg('a')})");

    assertEquals(0, compiler.getWarningCount());

    String errors = Joiner.on("\n").join(compiler.getErrors());
    assertEquals("There should be one error. " + errors,
        1, compiler.getErrorCount());
    assertEquals(errors, JsMessageVisitor.MESSAGE_DUPLICATE_KEY,
        compiler.getErrors()[0].getType());
  }

// com.google.javascript.jscomp.JsMessageVisitorTest::testNoDuplicateErrorOnExternMessage
  public void testNoDuplicateErrorOnExternMessage() {
    extractMessagesSafely(
        "(function () { " +
        "var MSG_EXTERNAL_2 = goog.getMsg('a')})" +
        "(function () { " +
        "var MSG_EXTERNAL_2 = goog.getMsg('a')})");
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testOptimize
  public void testOptimize() {
    assertEquals(localEntry, localEntry.optimize());

    FlowScope child = localEntry.createChildFlowScope();
    assertEquals(localEntry, child.optimize());

    child.inferSlotType("localB", NUMBER_TYPE);
    assertEquals(child, child.optimize());
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testJoin1
  public void testJoin1() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localB", NUMBER_TYPE);

    FlowScope childAB = childA.createChildFlowScope();
    childAB.inferSlotType("localB", STRING_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childB.inferSlotType("localB", BOOLEAN_TYPE);

    assertEquals(STRING_TYPE, childAB.getSlot("localB").getType());
    assertEquals(BOOLEAN_TYPE, childB.getSlot("localB").getType());
    assertNull(childB.getSlot("localA").getType());

    FlowScope joined = join(childB, childAB);
    assertEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localB").getType());
    assertNull(joined.getSlot("localA").getType());

    joined = join(childAB, childB);
    assertEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localB").getType());
    assertNull(joined.getSlot("localA").getType());

    assertEquals("Join should be symmetric",
        join(childB, childAB), join(childAB, childB));
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testJoin2
  public void testJoin2() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localA", STRING_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childB.inferSlotType("globalB", BOOLEAN_TYPE);

    assertEquals(STRING_TYPE, childA.getSlot("localA").getType());
    assertEquals(BOOLEAN_TYPE, childB.getSlot("globalB").getType());
    assertNull(childB.getSlot("localB").getType());

    FlowScope joined = join(childB, childA);
    assertEquals(STRING_TYPE, joined.getSlot("localA").getType());
    assertEquals(BOOLEAN_TYPE, joined.getSlot("globalB").getType());

    joined = join(childA, childB);
    assertEquals(STRING_TYPE, joined.getSlot("localA").getType());
    assertEquals(BOOLEAN_TYPE, joined.getSlot("globalB").getType());

    assertEquals("Join should be symmetric",
        join(childB, childA), join(childA, childB));
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testJoin3
  public void testJoin3() {
    localScope.declare("localC", null, STRING_TYPE, null);
    localScope.declare("localD", null, STRING_TYPE, null);

    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localC", NUMBER_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childA.inferSlotType("localD", BOOLEAN_TYPE);

    FlowScope joined = join(childB, childA);
    assertEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        joined.getSlot("localC").getType());
    assertEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localD").getType());

    joined = join(childA, childB);
    assertEquals(createUnionType(STRING_TYPE, NUMBER_TYPE),
        joined.getSlot("localC").getType());
    assertEquals(createUnionType(STRING_TYPE, BOOLEAN_TYPE),
        joined.getSlot("localD").getType());

    assertEquals("Join should be symmetric",
        join(childB, childA), join(childA, childB));
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testLongChain1
  public void testLongChain1() {
    FlowScope chainA = localEntry.createChildFlowScope();
    FlowScope chainB = localEntry.createChildFlowScope();
    for (int i = 0; i < LONG_CHAIN_LENGTH; i++) {
      localScope.declare("local" + i, null, null, null);
      chainA.inferSlotType("local" + i,
          i % 2 == 0 ? NUMBER_TYPE : BOOLEAN_TYPE);
      chainB.inferSlotType("local" + i,
          i % 3 == 0 ? STRING_TYPE : BOOLEAN_TYPE);

      chainA = chainA.createChildFlowScope();
      chainB = chainB.createChildFlowScope();
    }

    verifyLongChains(chainA, chainB);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testLongChain2
  public void testLongChain2() {
    FlowScope chainA = localEntry.createChildFlowScope();
    FlowScope chainB = localEntry.createChildFlowScope();
    for (int i = 0; i < LONG_CHAIN_LENGTH * 7; i++) {
      localScope.declare("local" + i, null, null, null);
      chainA.inferSlotType("local" + i,
          i % 2 == 0 ? NUMBER_TYPE : BOOLEAN_TYPE);
      chainB.inferSlotType("local" + i,
          i % 3 == 0 ? STRING_TYPE : BOOLEAN_TYPE);

      if (i % 7 == 0) {
        chainA = chainA.createChildFlowScope();
        chainB = chainB.createChildFlowScope();
      }
    }

    verifyLongChains(chainA, chainB);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testLongChain3
  public void testLongChain3() {
    FlowScope chainA = localEntry.createChildFlowScope();
    FlowScope chainB = localEntry.createChildFlowScope();
    for (int i = 0; i < LONG_CHAIN_LENGTH * 7; i++) {
      if (i % 7 == 0) {
        int j = i / 7;
        localScope.declare("local" + j, null, null, null);
        chainA.inferSlotType("local" + j,
            j % 2 == 0 ? NUMBER_TYPE : BOOLEAN_TYPE);
        chainB.inferSlotType("local" + j,
            j % 3 == 0 ? STRING_TYPE : BOOLEAN_TYPE);
      }

      chainA = chainA.createChildFlowScope();
      chainB = chainB.createChildFlowScope();
    }

    verifyLongChains(chainA, chainB);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testFindUniqueSlot
  public void testFindUniqueSlot() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localB", NUMBER_TYPE);

    FlowScope childAB = childA.createChildFlowScope();
    childAB.inferSlotType("localB", STRING_TYPE);

    FlowScope childABC = childAB.createChildFlowScope();
    childABC.inferSlotType("localA", BOOLEAN_TYPE);

    assertNull(childABC.findUniqueRefinedSlot(childABC));
    assertEquals(BOOLEAN_TYPE,
        childABC.findUniqueRefinedSlot(childAB).getType());
    assertNull(childABC.findUniqueRefinedSlot(childA));
    assertNull(childABC.findUniqueRefinedSlot(localEntry));

    assertEquals(STRING_TYPE,
        childAB.findUniqueRefinedSlot(childA).getType());
    assertEquals(STRING_TYPE,
        childAB.findUniqueRefinedSlot(localEntry).getType());

    assertEquals(NUMBER_TYPE,
        childA.findUniqueRefinedSlot(localEntry).getType());
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testDiffer1
  public void testDiffer1() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localB", NUMBER_TYPE);

    FlowScope childAB = childA.createChildFlowScope();
    childAB.inferSlotType("localB", STRING_TYPE);

    FlowScope childABC = childAB.createChildFlowScope();
    childABC.inferSlotType("localA", BOOLEAN_TYPE);

    FlowScope childB = childAB.createChildFlowScope();
    childB.inferSlotType("localB", STRING_TYPE);

    FlowScope childBC = childB.createChildFlowScope();
    childBC.inferSlotType("localA", NO_TYPE);

    assertScopesSame(childAB, childB);
    assertScopesDiffer(childABC, childBC);

    assertScopesDiffer(childABC, childB);
    assertScopesDiffer(childAB, childBC);

    assertScopesDiffer(childA, childAB);
    assertScopesDiffer(childA, childABC);
    assertScopesDiffer(childA, childB);
    assertScopesDiffer(childA, childBC);
  }

// com.google.javascript.jscomp.LinkedFlowScopeTest::testDiffer2
  public void testDiffer2() {
    FlowScope childA = localEntry.createChildFlowScope();
    childA.inferSlotType("localA", NUMBER_TYPE);

    FlowScope childB = localEntry.createChildFlowScope();
    childB.inferSlotType("localA", NO_TYPE);

    assertScopesDiffer(childA, childB);
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

// com.google.javascript.jscomp.LiveVariableAnalysisTest::testExpressionInForIn
  public void testExpressionInForIn() {
    assertLiveBeforeX("var a = [0]; X:for (a[1] in foo) { }", "a");
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

// com.google.javascript.jscomp.LooseTypeCheckTest::testInitialTypingScope
  public void testInitialTypingScope() {
    Scope s = new TypedScopeCreator(compiler,
        new DefaultCodingConvention()).createInitialScope(
            new Node(Token.BLOCK));

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

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck1
  public void testTypeCheck1() throws Exception {
    testTypes("function foo(){ if (foo()) return; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck2
  public void testTypeCheck2() throws Exception {
    testTypes("function foo(){ var x=foo(); x--; }",
        "increment/decrement\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck4
  public void testTypeCheck4() throws Exception {
    testTypes("function foo(){ !foo(); }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck5
  public void testTypeCheck5() throws Exception {
    testTypes("function foo(){ var a = +foo(); }",
        "sign operator\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck6
  public void testTypeCheck6() throws Exception {
    testTypes(
        "function foo(){" +
        "var a;if (a == foo())return;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck8
  public void testTypeCheck8() throws Exception {
    testTypes("function foo(){do {} while (foo());}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck9
  public void testTypeCheck9() throws Exception {
    testTypes("function foo(){while (foo());}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck10
  public void testTypeCheck10() throws Exception {
    testTypes("function foo(){for (;foo(););}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck11
  public void testTypeCheck11() throws Exception {
    testTypes("var a;" +
        "var b;" +
        "a = b;",
        "assignment\n" +
        "found   : String\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck12
  public void testTypeCheck12() throws Exception {
    testTypes("function foo(){var a = 3^foo();}",
        "bad right operand to bitwise operator\n" +
        "found   : Object\n" +
        "required: (boolean|null|number|string|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck13
  public void testTypeCheck13() throws Exception {
    testTypes("var i; i=/xx/;",
        "assignment\n" +
        "found   : RegExp\n" +
        "required: (Number|String)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck14
  public void testTypeCheck14() throws Exception {
    testTypes("function foo(opt_a){}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck15
  public void testTypeCheck15() throws Exception {
    testTypes("var x;x=null;x=10;",
        "assignment\n" +
        "found   : number\n" +
        "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck16a
  public void testTypeCheck16a() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck16b
  public void testTypeCheck16b() throws Exception {
    testTypes("var x='';",
              "initializing variable\n" +
              "found   : string\n" +
              "required: (Number|null)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck17
  public void testTypeCheck17() throws Exception {
    testTypes("\n" +
        "function a(opt_foo){\nreturn (opt_foo);\n}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck18
  public void testTypeCheck18() throws Exception {
    testTypes("\n function a(){return new RegExp();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck19
  public void testTypeCheck19() throws Exception {
    testTypes("\n function a(){return new Array();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck20
  public void testTypeCheck20() throws Exception {
    testTypes("\n function a(){return new Date();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckBasicDowncast
  public void testTypeCheckBasicDowncast() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckNoDowncastToNumber
  public void testTypeCheckNoDowncastToNumber() throws Exception {
    testTypes("function foo() {}\n" +
                  " var bar = new foo();\n",
        "initializing variable\n" +
        "found   : foo\n" +
        "required: Number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck21
  public void testTypeCheck21() throws Exception {
    testTypes("var foo;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck22
  public void testTypeCheck22() throws Exception {
    testTypes("\nfunction foo(p){}\n" +
                  "function Element(){}\n" +
                  "var v;\n" +
                  "foo(v);\n");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck23
  public void testTypeCheck23() throws Exception {
    testTypes("var foo; foo = null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheck24
  public void testTypeCheck24() throws Exception {
    testTypes("function MyType(){}\n" +
        "var foo; foo = null;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckDefaultExterns
  public void testTypeCheckDefaultExterns() throws Exception {
    testTypes(" function f(x) {}" +
        "f([].length);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeCheckCustomExterns
  public void testTypeCheckCustomExterns() throws Exception {
    testTypes(
        DEFAULT_EXTERNS + " Array.prototype.oogabooga;",
        " function f(x) {}" +
        "f([].oogabooga);" ,
        "actual parameter 1 of f does not match formal parameter\n" +
        "found   : boolean\n" +
        "required: string", false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray1
  public void testParameterizedArray1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray2
  public void testParameterizedArray2() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : Array\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray3
  public void testParameterizedArray3() throws Exception {
    testTypes(" var f = function(a) { a[1] = 0; return a[0]; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray4
  public void testParameterizedArray4() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };",
        "assignment\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray5
  public void testParameterizedArray5() throws Exception {
    testTypes(" var f = function(a) { a[0] = 'a'; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray6
  public void testParameterizedArray6() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : *\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedArray7
  public void testParameterizedArray7() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject1
  public void testParameterizedObject1() throws Exception {
    testTypes(" var f = function(a) { return a[0]; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject2
  public void testParameterizedObject2() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject3
  public void testParameterizedObject3() throws Exception {
    testTypes(" var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testParameterizedObject4
  public void testParameterizedObject4() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " var f = function(a) { return a['x']; };",
        "restricted index type\n" +
        "found   : string\n" +
        "required: E.<string>");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testUnionOfFunctionAndType
  public void testUnionOfFunctionAndType() throws Exception {
    testTypes(" var a;" +
        " var b = null; a = b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalParameterComparedToUndefined
  public void testOptionalParameterComparedToUndefined() throws Exception {
    testTypes("function foo(opt_a)" +
        "{if (opt_a==undefined) var b = 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalAllType
  public void testOptionalAllType() throws Exception {
    testTypes("function f(opt_x) { return opt_x }\n" +
        "var y;\n" +
        "f(y);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalUnknownNamedType
  public void testOptionalUnknownNamedType() throws Exception {
    testTypes("\n" +
        "function f(opt_x) { return opt_x; }\n" +
        "var T = function() {};",
        "inconsistent return type\n" +
        "found   : (T|undefined)\n" +
        "required: undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam
  public void testOptionalArgFunctionParam() throws Exception {
    testTypes("" +
        "function f(a) {a()};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam2
  public void testOptionalArgFunctionParam2() throws Exception {
    testTypes("" +
        "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam3
  public void testOptionalArgFunctionParam3() throws Exception {
    testTypes("" +
        "function f(a) {a(undefined)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParam4
  public void testOptionalArgFunctionParam4() throws Exception {
    String expectedWarning = "Function a: called with 2 argument(s). " +
        "Function requires at least 0 argument(s) and no more than 1 " +
        "argument(s).";

    testTypes("function f(a) {a(3,4)};",
              expectedWarning, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionParamError
  public void testOptionalArgFunctionParamError() throws Exception {
    String expectedWarning =
        "Bad type annotation. variable length argument must be last";
    testTypes("" +
              "function f(a) {};", expectedWarning, false);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam
  public void testOptionalNullableArgFunctionParam() throws Exception {
    testTypes("" +
              "function f(a) {a()};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam2
  public void testOptionalNullableArgFunctionParam2() throws Exception {
    testTypes("" +
              "function f(a) {a(null)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalNullableArgFunctionParam3
  public void testOptionalNullableArgFunctionParam3() throws Exception {
    testTypes("" +
              "function f(a) {a(3)};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionReturn
  public void testOptionalArgFunctionReturn() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()()");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testOptionalArgFunctionReturn2
  public void testOptionalArgFunctionReturn2() throws Exception {
    testTypes("" +
              "function f() { return function(opt_x) { }; };" +
              "f()({})");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanType
  public void testBooleanType() throws Exception {
    testTypes("var x = 1 < 2;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction1
  public void testBooleanReduction1() throws Exception {
    testTypes("var x; x = null || \"a\";");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction2
  public void testBooleanReduction2() throws Exception {
    
    
    testTypes("" +
        "(function(s) { return ((s == 'a') && s) || 'b'; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction3
  public void testBooleanReduction3() throws Exception {
    testTypes("" +
        "(function(s) { return s && null && 3; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction4
  public void testBooleanReduction4() throws Exception {
    testTypes("" +
        "(function(x) { return null || x || null ; })");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction5
  public void testBooleanReduction5() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!x || typeof x == 'string') {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction6
  public void testBooleanReduction6() throws Exception {
    testTypes("\n" +
        "var f = function(x) {\n" +
        "if (!(x && typeof x != 'string')) {\n" +
        "return x;\n" +
        "}\n" +
        "return null;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanReduction7
   public void testBooleanReduction7() throws Exception {
     testTypes("var T = function() {};\n" +
         "\n" +
         "var f = function(x) {\n" +
         "if (!x) {\n" +
         "return x;\n" +
         "}\n" +
         "return null;\n" +
         "};");
   }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNullAnd
  public void testNullAnd() throws Exception {
    testTypes("var x;\n" +
        "var r = x && x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNullOr
  public void testNullOr() throws Exception {
    testTypes("var x;\n" +
        "var r = x || x;",
        "initializing variable\n" +
        "found   : null\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation1
  public void testBooleanPreservation1() throws Exception {
    testTypes("var x = \"a\";" +
        "x = ((x == \"a\") && x) || x == \"b\";",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation2
  public void testBooleanPreservation2() throws Exception {
    testTypes("var x = \"a\"; x = (x == \"a\") || x;",
        "assignment\n" +
        "found   : (boolean|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation3
  public void testBooleanPreservation3() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "condition always evaluates to false\n" +
        "left : Function\n" +
        "right: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testBooleanPreservation4
  public void testBooleanPreservation4() throws Exception {
    testTypes("" +
        "function f(x) { return x && x == \"a\"; }",
        "inconsistent return type\n" +
        "found   : (boolean|null|undefined)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction1
  public void testTypeOfReduction1() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'number' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction2
  public void testTypeOfReduction2() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x != 'string' ? String(x) : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction3
  public void testTypeOfReduction3() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'object' ? 1 : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction4
  public void testTypeOfReduction4() throws Exception {
    testTypes(" " +
        "function f(x) { return typeof x == 'undefined' ? {} : x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction5
  public void testTypeOfReduction5() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x != 'number' ? x : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction6
  public void testTypeOfReduction6() throws Exception {
    testTypes("\n" +
        "function f(x) {\n" +
        "return typeof x == 'string' && x.length == 3 ? x : 'a';\n" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction7
  public void testTypeOfReduction7() throws Exception {
    testTypes("var f = function(x) { " +
        "return typeof x == 'number' ? x : 'a'; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction8
  public void testTypeOfReduction8() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isString(x) && x.length == 3 ? x : 'a';\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction9
  public void testTypeOfReduction9() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isArray(x) ? 'a' : x;\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction10
  public void testTypeOfReduction10() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isArray(x) ? x : [];\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction11
  public void testTypeOfReduction11() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        "\n" +
        "function f(x) {\n" +
        "return goog.isObject(x) ? x : [];\n" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction12
  public void testTypeOfReduction12() throws Exception {
    testTypes(" var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return typeof x == 'object' ? x : []; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction13
  public void testTypeOfReduction13() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var E = {A: 'a', B: 'b'};\n" +
        " " +
        "function f(x) { return goog.isObject(x) ? x : []; }", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction14
  public void testTypeOfReduction14() throws Exception {
    
    testClosureTypes(
        CLOSURE_DEFS +
        "function f(arguments) { " +
        "  return goog.isString(arguments[0]) ? arguments[0] : 0;" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testTypeOfReduction15
  public void testTypeOfReduction15() throws Exception {
    
    testClosureTypes(
        CLOSURE_DEFS +
        "function f(arguments) { " +
        "  return typeof arguments[0] == 'string' ? arguments[0] : 0;" +
        "}", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction1
  public void testQualifiedNameReduction1() throws Exception {
    testTypes("var x = {};  x.a = 'a';\n" +
        " var f = function() {\n" +
        "return x.a ? x.a : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction2
  public void testQualifiedNameReduction2() throws Exception {
    testTypes(" var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return this.a ? this.a : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction3
  public void testQualifiedNameReduction3() throws Exception {
    testTypes(" var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return typeof this.a == 'string' ? this.a : 'a'; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testQualifiedNameReduction4
  public void testQualifiedNameReduction4() throws Exception {
    testClosureTypes(
        CLOSURE_DEFS +
        " var T = " +
        "function(a) {this.a = a};\n" +
        " T.prototype.f = function() {\n" +
        "return goog.isString(this.a) ? this.a : 'a'; }", null);
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceOfReduction1
  public void testInstanceOfReduction1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return x; } else { return new T(); }\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceOfReduction2
  public void testInstanceOfReduction2() throws Exception {
    testTypes(" var T = function() {};\n" +
        "\n" +
        "var f = function(x) {\n" +
        "if (x instanceof T) { return ''; } else { return x; }\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInferredPropagation
  public void testPropertyInferredPropagation() throws Exception {
    testTypes("function f() { return {}; }\n" +
         "function g() { var x = f(); if (x.p) x.a = 'a'; else x.a = 'b'; }\n" +
         "function h() { var x = f(); x.a = false; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference1
  public void testPropertyInference1() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference2
  public void testPropertyInference2() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = null; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference3
  public void testPropertyInference3() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : (boolean|number)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference4
  public void testPropertyInference4() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.x_ = 3;" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference5
  public void testPropertyInference5() throws Exception {
    testTypes(
        " function F() { }" +
        "F.prototype.baz = function() { this.x_ = 3; };" +
        "" +
        "F.prototype.bar = function() { if (this.x_) return this.x_; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference6
  public void testPropertyInference6() throws Exception {
    testTypes(
        " function F() { }" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference7
  public void testPropertyInference7() throws Exception {
    testTypes(
        " function F() { this.x_ = true; }" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyInference8
  public void testPropertyInference8() throws Exception {
    testTypes(
        " function F() { " +
        "   this.x_ = 'x';" +
        "}" +
        "(new F).x_ = 3;" +
        "" +
        "F.prototype.bar = function() { return this.x_; };",
        "assignment to property x_ of F\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoPersistentTypeInferenceForObjectProperties
  public void testNoPersistentTypeInferenceForObjectProperties()
      throws Exception {
    testTypes("\n" +
        "function s1(o,x) { o.x = x; }\n" +
        "\n" +
        "function g1(o) { return typeof o.x == 'undefined' ? '' : o.x; }\n" +
        "\n" +
        "function s2(o,x) { o.x = x; }\n" +
        "\n" +
        "function g2(o) { return typeof o.x == 'undefined' ? 0 : o.x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNoPersistentTypeInferenceForFunctionProperties
  public void testNoPersistentTypeInferenceForFunctionProperties()
      throws Exception {
    testTypes("\n" +
        "function s1(o,x) { o.x = x; }\n" +
        "\n" +
        "function g1(o) { return typeof o.x == 'undefined' ? '' : o.x; }\n" +
        "\n" +
        "function s2(o,x) { o.x = x; }\n" +
        "\n" +
        "function g2(o) { return typeof o.x == 'undefined' ? 0 : o.x; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testObjectPropertyTypeInferredInLocalScope1
  public void testObjectPropertyTypeInferredInLocalScope1() throws Exception {
    testTypes("\n" +
        "function f(o) { o.x = 1; return o.x; }",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testObjectPropertyTypeInferredInLocalScope2
  public void testObjectPropertyTypeInferredInLocalScope2() throws Exception {
    testTypes("" +
        "function f(o, x) { o.x = 'a';\nif (x) {o.x = x;}\nreturn o.x; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testObjectPropertyTypeInferredInLocalScope3
  public void testObjectPropertyTypeInferredInLocalScope3() throws Exception {
    testTypes("" +
        "function f(o, x) { if (x) {o.x = x;} else {o.x = 'a';}\nreturn o.x; }",
        "inconsistent return type\n" +
        "found   : (number|string)\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty1
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty1()
      throws Exception {
    testTypes("var T = function() { this.x = ''; };\n" +
        " T.prototype.x = 0;",
        "assignment to property x of T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty2
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty2()
      throws Exception {
    testTypes("var T = function() { this.x = ''; };\n" +
        " T.prototype.x;",
        "assignment to property x of T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty3
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty3()
      throws Exception {
    testTypes(" var n = {};\n" +
        " n.T = function() { this.x = ''; };\n" +
        " n.T.prototype.x = 0;",
        "assignment to property x of n.T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testMismatchingOverridingInferredPropertyBeforeDeclaredProperty4
  public void testMismatchingOverridingInferredPropertyBeforeDeclaredProperty4()
      throws Exception {
    testTypes("var n = {};\n" +
        " n.T = function() { this.x = ''; };\n" +
        " n.T.prototype.x = 0;",
        "assignment to property x of n.T\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyUsedBeforeDefinition1
  public void testPropertyUsedBeforeDefinition1() throws Exception {
    testTypes(" var T = function() {};\n" +
        "" +
        "T.prototype.f = function() { return this.g(); };\n" +
        " T.prototype.g = function() { return 1; };\n",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPropertyUsedBeforeDefinition2
  public void testPropertyUsedBeforeDefinition2() throws Exception {
    testTypes("var n = {};\n" +
        " n.T = function() {};\n" +
        "" +
        "n.T.prototype.f = function() { return this.g(); };\n" +
        " n.T.prototype.g = function() { return 1; };\n",
        "inconsistent return type\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd1
  public void testAdd1() throws Exception {
    testTypes("function foo(){var a = 'abc'+foo();}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd2
  public void testAdd2() throws Exception {
    testTypes("function foo(){var a = foo()+4;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd3
  public void testAdd3() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 'b';" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd4
  public void testAdd4() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 'b';" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd5
  public void testAdd5() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd6
  public void testAdd6() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 5;" +
        " var c = a + b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd7
  public void testAdd7() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 'b';" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd8
  public void testAdd8() throws Exception {
    testTypes(" var a = 'a';" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd9
  public void testAdd9() throws Exception {
    testTypes(" var a = 5;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : number\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd10
  public void testAdd10() throws Exception {
    
    testTypes(
        suppressMissingProperty("e", "f") +
        " var a = 5;" +
        " var c = a + d.e.f;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd11
  public void testAdd11() throws Exception {
    
    testTypes(
        suppressMissingProperty("e", "f") +
        " var a = 5;" +
        " var c = a + d.e.f;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd12
  public void testAdd12() throws Exception {
    testTypes(" function a() { return 5; }" +
        " var b = 5;" +
        " var c = a() + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd13
  public void testAdd13() throws Exception {
    testTypes(" var a = 5;" +
        " function b() { return 5; }" +
        " var c = a + b();",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd14
  public void testAdd14() throws Exception {
    testTypes(" var a = null;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd15
  public void testAdd15() throws Exception {
    testTypes(" var a = 5;" +
        " function b() { return 5; }" +
        " var c = a + b();",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd16
  public void testAdd16() throws Exception {
    testTypes(" var a = undefined;" +
        " var b = 5;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd17
  public void testAdd17() throws Exception {
    testTypes(" var a = 5;" +
        " var b = undefined;" +
        " var c = a + b;",
        "initializing variable\n" +
        "found   : (number|string)\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd18
  public void testAdd18() throws Exception {
    testTypes("function f() {};" +
        " var a = 'a';" +
        " var c = a + f();",
        "initializing variable\n" +
        "found   : string\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd19
  public void testAdd19() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd20
  public void testAdd20() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testAdd21
  public void testAdd21() throws Exception {
    testTypes(" function f(opt_x, opt_y) {" +
        "return opt_x + opt_y;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison1
  public void testNumericComparison1() throws Exception {
    testTypes(" function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison2
  public void testNumericComparison2() throws Exception {
    testTypes(" function f(a) {return a < 3;}",
        "left side of numeric comparison\n" +
        "found   : Object\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison3
  public void testNumericComparison3() throws Exception {
    testTypes(" function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison4
  public void testNumericComparison4() throws Exception {
    testTypes(" " +
              "function f(a) {return a < 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison5
  public void testNumericComparison5() throws Exception {
    testTypes(" function f(a) {return a < 3;}",
        "left side of numeric comparison\n" +
        "found   : *\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testNumericComparison6
  public void testNumericComparison6() throws Exception {
    testTypes(" function foo() { if (3 >= foo()) return; }",
        "right side of numeric comparison\n" +
        "found   : undefined\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison1
  public void testStringComparison1() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison2
  public void testStringComparison2() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison3
  public void testStringComparison3() throws Exception {
    testTypes(" function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison4
  public void testStringComparison4() throws Exception {
    testTypes(" " +
                  "function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison5
  public void testStringComparison5() throws Exception {
    testTypes(" " +
                  "function f(a) {return a < 'x';}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testStringComparison6
  public void testStringComparison6() throws Exception {
    testTypes(" " +
        "function foo() { if ('a' >= foo()) return; }",
        "right side of comparison\n" +
        "found   : undefined\n" +
        "required: string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testValueOfComparison1
  public void testValueOfComparison1() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.valueOf = function() { return 1; };" +
        " function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testValueOfComparison2
  public void testValueOfComparison2() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.valueOf = function() { return 1; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testValueOfComparison3
  public void testValueOfComparison3() throws Exception {
    testTypes("function O() {};" +
        "O.prototype.toString = function() { return 'o'; };" +
        "" +
        "function f(a,b) { return a < b; }");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testGenericRelationalExpression
  public void testGenericRelationalExpression() throws Exception {
    testTypes(" " +
                  "function f(a,b) {return a < b;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof1
  public void testInstanceof1() throws Exception {
    testTypes("function foo(){" +
        "if (bar instanceof 3)return;}",
        "instanceof requires an object\n" +
        "found   : number\n" +
        "required: Object");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof2
  public void testInstanceof2() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return;}",
        "deterministic instanceof yields false\n" +
        "found   : undefined\n" +
        "required: NoObject");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof3
  public void testInstanceof3() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof4
  public void testInstanceof4() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof5
  public void testInstanceof5() throws Exception {
    
    testTypes(" function foo(){" +
        "if (foo() instanceof Object)return;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceof6
  public void testInstanceof6() throws Exception {
    testTypes("function foo(){" +
        "if (foo() instanceof Object)return 3;}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testInstanceOfReduction3
  public void testInstanceOfReduction3() throws Exception {
    testTypes(
        "\n" +
        "var f = function(x, y) {\n" +
        "  return x instanceof y;\n" +
        "};");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping1
  public void testScoping1() throws Exception {
    testTypes(
        "function foo(a){" +
        "  function bar(a){" +
        "    if (a instanceof Array)return;" +
        "  }" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping2
  public void testScoping2() throws Exception {
    testTypes(
        " var a;" +
        "function Foo() {" +
        "   var a;" +
        "}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping3
  public void testScoping3() throws Exception {
    testTypes("\n\nvar b;\nvar b;",
        "variable b redefined with type String, original " +
        "definition at [testcode]:3 with type (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping4
  public void testScoping4() throws Exception {
    testTypes("var b; if (true) var b;",
        "variable b redefined with type String, original " +
        "definition at [testcode]:1 with type (Number|null|undefined)");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping5
  public void testScoping5() throws Exception {
    
    
    testTypes("if (true) var b; var b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping6
  public void testScoping6() throws Exception {
    
    
    testTypes("if (true) var b; if (true) var b;");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping7
  public void testScoping7() throws Exception {
    testTypes("function A() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of A\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping8
  public void testScoping8() throws Exception {
    testTypes("function A() {}" +
        "function B() {" +
        "  this.a = null;" +
        "}",
        "assignment to property a of B\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping9
  public void testScoping9() throws Exception {
    testTypes("function B() {" +
        "  this.a = null;" +
        "}" +
        "function A() {}",
        "assignment to property a of B\n" +
        "found   : null\n" +
        "required: A");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping10
  public void testScoping10() throws Exception {
    TypeCheckResult p = parseAndTypeCheckWithScope("var a = function b(){};");

    
    assertTrue(p.scope.isDeclared("a", false));
    assertFalse(p.scope.isDeclared("b", false));

    
    assertEquals("function (): undefined",
        p.scope.getVar("a").getType().toString());
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testScoping11
  public void testScoping11() throws Exception {
    
    
    testTypes(
        "var a = function b(){ return b };",
        "inconsistent return type\n" +
        "found   : function (): number\n" +
        "required: number");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments1
  public void testFunctionArguments1() throws Exception {
    testFunctionType(
        "" +
        "function f(a) {}",
        "function (number): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments2
  public void testFunctionArguments2() throws Exception {
    testFunctionType(
        "" +
        "function f(opt_a) {}",
        "function ((number|undefined)): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments3
  public void testFunctionArguments3() throws Exception {
    testFunctionType(
        "" +
        "function f(a,b) {}",
        "function (?, number): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments4
  public void testFunctionArguments4() throws Exception {
    testFunctionType(
        "" +
        "function f(a,opt_a) {}",
        "function (?, (number|undefined)): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments5
  public void testFunctionArguments5() throws Exception {
    testTypes(
        "function a(opt_a,a) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments6
  public void testFunctionArguments6() throws Exception {
    testTypes(
        "function a(var_args,a) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments7
  public void testFunctionArguments7() throws Exception {
    testTypes(
        "" +
        "function a(a,opt_a,var_args) {}");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments8
  public void testFunctionArguments8() throws Exception {
    testTypes(
        "function a(a,opt_a,var_args,b) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments9
  public void testFunctionArguments9() throws Exception {
    
    testTypes(
        "function a(a,opt_a,var_args,b,c) {}",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments10
  public void testFunctionArguments10() throws Exception {
    
    testTypes(
        "function a(a,opt_a,b,c) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments11
  public void testFunctionArguments11() throws Exception {
    testTypes(
        "function a(a,opt_a,b,c,var_args,d) {}",
        "optional arguments must be at the end");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments12
  public void testFunctionArguments12() throws Exception {
    testTypes("function bar(baz){}",
        "parameter foo does not appear in bar's parameter list");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments13
  public void testFunctionArguments13() throws Exception {
    
    testTypes(
        " function u() { return true; }" +
        "" +
        "function f(b) { if (u()) { b = null; } return b; }",
        "assignment\n" +
        "found   : null\n" +
        "required: boolean");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments14
  public void testFunctionArguments14() throws Exception {
    testTypes(
        " function f(x, opt_y, var_args) {}" +
        "f('3'); f('3', 2); f('3', 2, true); f('3', 2, true, false);");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionArguments15
  public void testFunctionArguments15() throws Exception {
    testTypes(
        "" +
        "function g(f) { f(1, 2); }",
        "Function f: called with 2 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrintFunctionName1
  public void testPrintFunctionName1() throws Exception {
    
    testTypes(
        "var goog = {}; goog.run = function(f) {};" +
        "goog.run();",
        "Function goog.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testPrintFunctionName2
  public void testPrintFunctionName2() throws Exception {
    testTypes(
        " var Foo = function() {}; " +
        "Foo.prototype.run = function(f) {};" +
        "(new Foo).run();",
        "Function Foo.prototype.run: called with 0 argument(s). " +
        "Function requires at least 1 argument(s) " +
        "and no more than 1 argument(s).");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference1
  public void testFunctionInference1() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (?): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference2
  public void testFunctionInference2() throws Exception {
    testFunctionType(
        "function f(a,b) {}",
        "function (?, ?): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference3
  public void testFunctionInference3() throws Exception {
    testFunctionType(
        "function f(var_args) {}",
        "function (...[?]): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference4
  public void testFunctionInference4() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference5
  public void testFunctionInference5() throws Exception {
    testFunctionType(
        "function f(a) {}",
        "function (this:Date, ?): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference6
  public void testFunctionInference6() throws Exception {
    testFunctionType(
        "function f(opt_a) {}",
        "function (this:Date, ?): string");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference7
  public void testFunctionInference7() throws Exception {
    testFunctionType(
        "function f(a,b,c,var_args) {}",
        "function (this:Date, ?, ?, ?, ...[?]): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference8
  public void testFunctionInference8() throws Exception {
    testFunctionType(
        "function f() {}",
        "function (): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference9
  public void testFunctionInference9() throws Exception {
    testFunctionType(
        "var f = function() {};",
        "function (): undefined");
  }

// com.google.javascript.jscomp.LooseTypeCheckTest::testFunctionInference10
  public void testFunctionInference10() throws Exception {
    testFunctionType(
        "" +
        "var f = function(a,b) {};",
        "function (this:Date, ?, boolean): string");
  }
