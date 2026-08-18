// buggy code
  private ExtractionInfo extractMultilineTextualBlock(JsDocToken token,
                                                      WhitespaceOption option) {

    if (token == JsDocToken.EOC || token == JsDocToken.EOL ||
        token == JsDocToken.EOF) {
      return new ExtractionInfo("", token);
    }

    stream.update();
    int startLineno = stream.getLineno();
    int startCharno = stream.getCharno() + 1;

    // Read the content from the first line.
    String line = stream.getRemainingJSDocLine();
    if (option != WhitespaceOption.PRESERVE) {
      line = line.trim();
    }

    StringBuilder builder = new StringBuilder();
    builder.append(line);

    state = State.SEARCHING_ANNOTATION;
    token = next();

    boolean ignoreStar = false;

    // Track the start of the line to count whitespace that
    // the tokenizer skipped. Because this case is rare, it's easier
    // to do this here than in the tokenizer.

    do {
      switch (token) {
        case STAR:
          if (ignoreStar) {
            // Mark the position after the star as the new start of the line.
          } else {
            // The star is part of the comment.
            if (builder.length() > 0) {
              builder.append(' ');
            }

            builder.append('*');
          }

          token = next();
          continue;

        case EOL:
          if (option != WhitespaceOption.SINGLE_LINE) {
            builder.append("\n");
          }

          ignoreStar = true;
          token = next();
          continue;

        default:
          ignoreStar = false;
          state = State.SEARCHING_ANNOTATION;

              // All tokens must be separated by a space.

          if (token == JsDocToken.EOC ||
              token == JsDocToken.EOF ||
              // When we're capturing a license block, annotations
              // in the block are ok.
              (token == JsDocToken.ANNOTATION &&
               option != WhitespaceOption.PRESERVE)) {
            String multilineText = builder.toString();

            if (option != WhitespaceOption.PRESERVE) {
              multilineText = multilineText.trim();
            }

            int endLineno = stream.getLineno();
            int endCharno = stream.getCharno();

            if (multilineText.length() > 0) {
              jsdocBuilder.markText(multilineText, startLineno, startCharno,
                  endLineno, endCharno);
            }

            return new ExtractionInfo(multilineText, token);
          }

          if (builder.length() > 0) {
            builder.append(' ');
          }
          builder.append(toString(token));

          line = stream.getRemainingJSDocLine();

          if (option != WhitespaceOption.PRESERVE) {
            line = trimEnd(line);
          }

          builder.append(line);
          token = next();
      }
    } while (true);
  }

// relevant test
// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testBasicMapping
  public void testBasicMapping() throws Exception {
    compileAndCheck("function __BASIC__() { }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testLiteralMappings
  public void testLiteralMappings() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                    "var __VAR__ = '__STR__'; }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testMultilineMapping
  public void testMultilineMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testMultiFunctionMapping
  public void testMultiFunctionMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}\n\n" +

                    "function __BASIC2__(__PARAM3__, __PARAM4__) {\n" +
                    "var __VAR2__ = '__STR2__';\n" +
                    "var __ANO2__ = \"__STR3__\";\n" +
                    "}\n\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput0
  public void testGoldenOutput0() throws Exception {
    
    checkSourceMap("",

                   "{ \"file\" : \"testcode\"," +
                   " \"count\": 1 }\n" +

                   "[]\n" +

                   "\n" +
                   "[]\n" +

                   "\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testFunctionNameOutput1
  public void testFunctionNameOutput1() throws Exception {
    checkSourceMap("function f() {}",
                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,2,2,3,3]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,0,\"f\"]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,10]\n" +
                   "[\"testcode\",1,13]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testFunctionNameOutput2
  public void testFunctionNameOutput2() throws Exception {
    checkSourceMap("a.b.c = function () {};",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[3,2,2,1,1,0,4,4,4,4,4,4,4,4,5,5,6,6]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,0]\n" +
                   "[\"testcode\",1,0,\"c\"]\n" +
                   "[\"testcode\",1,0,\"b\"]\n" +
                   "[\"testcode\",1,0,\"a\"]\n" +
                   "[\"testcode\",1,8,\"a.b.c\"]\n" +
                   "[\"testcode\",1,17]\n" +
                   "[\"testcode\",1,20]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testFunctionNameOutput3
  public void testFunctionNameOutput3() throws Exception {
    checkSourceMap("var q = function () {};",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,1,1,2,2,2,2,2,2,2,2,3,3,4,4]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,0]\n" +
                   "[\"testcode\",1,4,\"q\"]\n" +
                   "[\"testcode\",1,8,\"q\"]\n" +
                   "[\"testcode\",1,17]\n" +
                   "[\"testcode\",1,20]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testFunctionNameOutput4
  public void testFunctionNameOutput4() throws Exception {
    checkSourceMap("({ 'q' : function () {} })",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,1,1,1,0,2,2,2,2,2,2,2,2,3,3,4,4,0,0]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,1]\n" +
                   "[\"testcode\",1,3]\n" +
                   "[\"testcode\",1,9,\"q\"]\n" +
                   "[\"testcode\",1,18]\n" +
                   "[\"testcode\",1,21]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput1
  public void testGoldenOutput1() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,2,3,3,3,2,4,4,4,2,5,7,7,7,6,8,8,8,6," +
                   "9,9,9,6,10,11,11,11,11,11,11,11,12,12,12,12,5]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,0,\"f\"]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,10]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,21]\n" +
                   "[\"testcode\",1,23]\n" +
                   "[\"testcode\",1,23,\"foo\"]\n" +
                   "[\"testcode\",1,29,\"foo\"]\n" +
                   "[\"testcode\",1,35,\"bar\"]\n" +
                   "[\"testcode\",1,41]\n" +
                   "[\"testcode\",1,44]\n" +
                   "[\"testcode\",1,51,\"foo\"]\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;
    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,0,2,2,2,0,3,3,3,0,0,4,4,4,0,5,5,5,0," +
                   "6,6,6,0,0,0,0,0,0,0,0,0,7,7,7,7,0]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"testcode\",1,0,\"f\"]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,23,\"foo\"]\n" +
                   "[\"testcode\",1,29,\"foo\"]\n" +
                   "[\"testcode\",1,35,\"bar\"]\n" +
                   "[\"testcode\",1,51,\"foo\"]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput2
  public void testGoldenOutput2() throws Exception {
    checkSourceMap("function f(foo, bar) {\r\n\n\n\nfoo = foo + bar + foo;" +
                   "\nreturn foo;\n}",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0,0,0,0,0,0,1,1,2,3,3,3,2,4,4,4,2,5,7,7,7,6,8,8,8," +
                   "6,9,9,9,6,10,10,10,11,11,11,11,11,11,11,12,12,12," +
                   "12,5]\n" +

                   "\n" +
                   "[]\n" +
                   "\n" +
                   "[\"testcode\",1,0,\"f\"]\n" +
                   "[\"testcode\",1,9,\"f\"]\n" +
                   "[\"testcode\",1,10]\n" +
                   "[\"testcode\",1,11,\"foo\"]\n" +
                   "[\"testcode\",1,16,\"bar\"]\n" +
                   "[\"testcode\",1,21]\n" +
                   "[\"testcode\",5,0]\n" +
                   "[\"testcode\",5,0,\"foo\"]\n" +
                   "[\"testcode\",5,6,\"foo\"]\n" +
                   "[\"testcode\",5,12,\"bar\"]\n" +
                   "[\"testcode\",5,18,\"foo\"]\n" +
                   "[\"testcode\",6,0]\n" +
                   "[\"testcode\",6,7,\"foo\"]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput3
  public void testGoldenOutput3() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;",

                   "{ \"file\" : \"testcode\", " +
                   "\"count\": 1 }\n" +

                   "[0,0,0]\n" +

                   "\n" +
                   "[]\n" +
                   "\n" +
                   "[\"c:\\\\myfile.js\",1,0,\"foo\"]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput4
  public void testGoldenOutput4() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;   boo;   goo;",

                   "" +
                   "{ \"file\" : \"testcode\", \"count\": 1 }\n" +
                   "[0,0,0,1,1,1,1,2,2,2,2]\n" +

                   "\n" +
                   "[]\n" +

                   "\n" +
                   "[\"c:\\\\myfile.js\",1,0,\"foo\"]\n" +
                   "[\"c:\\\\myfile.js\",1,7,\"boo\"]\n" +
                   "[\"c:\\\\myfile.js\",1,14,\"goo\"]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testGoldenOutput5
  public void testGoldenOutput5() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("c:\\myfile.js",
                   "\n" +
                   "var foo=a + 'this is a really long line that will force the"
                   + " mapping to span multiple lines 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + "' + c + d + e;",

                   "" +
                   "{ \"file\" : \"testcode\", \"count\": 6 }\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[0,0,0,0,1,1,1,1,2,1,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3," +
                   "3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3]\n" +
                   "[4,1,5,1,6]\n" +
                   "\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "[]\n" +
                   "\n" +
                   "[\"c:\\\\myfile.js\",4,0]\n" +
                   "[\"c:\\\\myfile.js\",4,4,\"foo\"]\n" +
                   "[\"c:\\\\myfile.js\",4,8,\"a\"]\n" +
                   "[\"c:\\\\myfile.js\",4,12]\n" +
                   "[\"c:\\\\myfile.js\",4,1314,\"c\"]\n" +
                   "[\"c:\\\\myfile.js\",4,1318,\"d\"]\n" +
                   "[\"c:\\\\myfile.js\",4,1322,\"e\"]\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("c:\\myfile.js",
        "\n" +
        "var foo=a + 'this is a really long line that will force the"
        + " mapping to span multiple lines 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + "' + c + d + e;",

        "" +
        "{ \"file\" : \"testcode\", \"count\": 6 }\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[-1,-1,-1,-1,0,0,0,0,1]\n" +
        "[2,0,3,0,4]\n" +
        "\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "[]\n" +
        "\n" +
        "[\"c:\\\\myfile.js\",4,4,\"foo\"]\n" +
        "[\"c:\\\\myfile.js\",4,8,\"a\"]\n" +
        "[\"c:\\\\myfile.js\",4,1314,\"c\"]\n" +
        "[\"c:\\\\myfile.js\",4,1318,\"d\"]\n" +
        "[\"c:\\\\myfile.js\",4,1322,\"e\"]\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV1Test::testBasicDeterminism
  public void testBasicDeterminism() throws Exception {
    RunResult result1 = compile("file1", "foo;", "file2", "bar;");
    RunResult result2 = compile("file2", "foo;", "file1", "bar;");

    String map1 = getSourceMap(result1);
    String map2 = getSourceMap(result2);

    
    

    
    String files1 = map1.split("\n")[4];
    String files2 = map2.split("\n")[4];

    assertEquals(files1, files2);
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testBasicMapping
  public void testBasicMapping() throws Exception {
    compileAndCheck("function __BASIC__() { }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testBasicMappingGoldenOutput
  public void testBasicMappingGoldenOutput() throws Exception {
    
    checkSourceMap("function __BASIC__() { }",

                   
                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"cAkBEBEB\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "[0,1,9,0],\n" +
                   "[0,1,18],\n" +
                   "[0,1,21],\n" +
                   "],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"__BASIC__\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testLiteralMappings
  public void testLiteralMappings() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                    "var __VAR__ = '__STR__'; }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testLiteralMappingsGoldenOutput
  public void testLiteralMappingsGoldenOutput() throws Exception {
    
    checkSourceMap("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                   "var __VAR__ = '__STR__'; }",

                   
                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"cAkBABkBA/kCA+ADMBcBgBA9\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "[0,1,9,0],\n" +
                   "[0,1,18],\n" +
                   "[0,1,19,1],\n" +
                   "[0,1,31,2],\n" +
                   "[0,1,43],\n" +
                   "[0,1,45],\n" +
                   "[0,1,49,3],\n" +
                   "[0,1,59],\n" +
                   "],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[" +
                       "\"__BASIC__\",\"__PARAM1__\",\"__PARAM2__\"," +
                       "\"__VAR__\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testMultilineMapping
  public void testMultilineMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testMultiFunctionMapping
  public void testMultiFunctionMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}\n\n" +

                    "function __BASIC2__(__PARAM3__, __PARAM4__) {\n" +
                    "var __VAR2__ = '__STR2__';\n" +
                    "var __ANO2__ = \"__STR3__\";\n" +
                    "}\n\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput0
  public void testGoldenOutput0() throws Exception {
    
    checkSourceMap("",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"\"],\n" +
                   "\"mappings\":[],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput1
  public void testGoldenOutput1() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap(
        "function f(foo, bar) { foo = foo + bar + 2; return foo; }",

        "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":1,\n" +
        "\"lineMaps\":[\"cAEBABIBA/ICA+ADICA/ICA+IDA9AEYBMBA5\"],\n" +
        "\"mappings\":[[0,1,0,0],\n" +
        "[0,1,9,0],\n" +
        "[0,1,10],\n" +
        "[0,1,11,1],\n" +
        "[0,1,16,2],\n" +
        "[0,1,21],\n" +
        "[0,1,23],\n" +
        "[0,1,23,1],\n" +
        "[0,1,29,1],\n" +
        "[0,1,35,2],\n" +
        "[0,1,41],\n" +
        "[0,1,44],\n" +
        "[0,1,51,1],\n" +
        "],\n" +
        "\"sources\":[\"testcode\"],\n" +
        "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
        "}\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"cAEBA/ICA+IDE9IEA8IFA7IGg6MHA5\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "[0,1,9,0],\n" +
                   "[0,1,11,1],\n" +
                   "[0,1,16,2],\n" +
                   "[0,1,23,1],\n" +
                   "[0,1,29,1],\n" +
                   "[0,1,35,2],\n" +
                   "[0,1,51,1],\n" +
                   "],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput2
  public void testGoldenOutput2() throws Exception {
    checkSourceMap("function f(foo, bar) {\r\n\n\n\nfoo = foo + bar + foo;" +
                   "\nreturn foo;\n}",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[" +
                       "\"cAEBABIBA/ICA+ADICA/ICA+IDA9IEYBMBA5\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "[0,1,9,0],\n" +
                   "[0,1,10],\n" +
                   "[0,1,11,1],\n" +
                   "[0,1,16,2],\n" +
                   "[0,1,21],\n" +
                   "[0,5,0],\n" +
                   "[0,5,0,1],\n" +
                   "[0,5,6,1],\n" +
                   "[0,5,12,2],\n" +
                   "[0,5,18,1],\n" +
                   "[0,6,0],\n" +
                   "[0,6,7,1],\n" +
                   "],\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput3
  public void testGoldenOutput3() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"IA\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "],\n" +
                   "\"sources\":[\"c:\\\\myfile.js\"],\n" +
                   "\"names\":[\"foo\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput4
  public void testGoldenOutput4() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;   boo;   goo;",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"lineMaps\":[\"IAMBMB\"],\n" +
                   "\"mappings\":[[0,1,0,0],\n" +
                   "[0,1,7,1],\n" +
                   "[0,1,14,2],\n" +
                   "],\n" +
                   "\"sources\":[\"c:\\\\myfile.js\"],\n" +
                   "\"names\":[\"foo\",\"boo\",\"goo\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testGoldenOutput5
  public void testGoldenOutput5() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("c:\\myfile.js",
                   "\n" +
                   "var foo=a + 'this is a really long line that will force the"
                   + " mapping to span multiple lines 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + " 123456789 123456789 123456789 123456789 123456789"
                   + "' + c + d + e;",

                   "{\n" +
                   "\"version\":2,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":6,\n" +
                   "\"lineMaps\":[\"\",\n" +
                   "\"\",\n" +
                   "\"\",\n" +
                   "\"\",\n" +
                   "\"MAMBABA/!!AUSC\",\n" +
                   "\"AEA9AEA8AF\"],\n" +
                   "\"mappings\":[[0,4,0],\n" +
                   "[0,4,4,0],\n" +
                   "[0,4,8,1],\n" +
                   "[0,4,12],\n" +
                   "[0,4,1314,2],\n" +
                   "[0,4,1318,3],\n" +
                   "[0,4,1322,4],\n" +
                   "],\n" +
                   "\"sources\":[\"c:\\\\myfile.js\"],\n" +
                   "\"names\":[\"foo\",\"a\",\"c\",\"d\",\"e\"]\n" +
                   "}\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("c:\\myfile.js",
        "\n" +
        "var foo=a + 'this is a really long line that will force the"
        + " mapping to span multiple lines 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + "' + c + d + e;",

        "{\n" +
        "\"version\":2,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":6,\n" +
        "\"lineMaps\":[\"\",\n" +
        "\"\",\n" +
        "\"\",\n" +
        "\"\",\n" +
        "\"M/MBAB\",\n" +
        "\"ACA+ADA9AE\"],\n" +
        "\"mappings\":[[0,4,4,0],\n" +
        "[0,4,8,1],\n" +
        "[0,4,1314,2],\n" +
        "[0,4,1318,3],\n" +
        "[0,4,1322,4],\n" +
        "],\n" +
        "\"sources\":[\"c:\\\\myfile.js\"],\n" +
        "\"names\":[\"foo\",\"a\",\"c\",\"d\",\"e\"]\n" +
        "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testBasicDeterminism
  public void testBasicDeterminism() throws Exception {
    RunResult result1 = compile("file1", "foo;", "file2", "bar;");
    RunResult result2 = compile("file2", "foo;", "file1", "bar;");

    String map1 = getSourceMap(result1);
    String map2 = getSourceMap(result2);

    
    

    
    String files1 = map1.split("\n")[4];
    String files2 = map2.split("\n")[4];

    assertEquals(files1, files2);
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testEncodingRelativeId
  public void testEncodingRelativeId() {
    assertEquals(0, getRelativeId(0, 0));
    assertEquals(64 + (-1), getRelativeId(-1, 0));
    assertEquals(64 + (-32), getRelativeId(0, 32));
    assertEquals(31, getRelativeId(31, 0));
    assertEquals(4096 + (-33), getRelativeId(0, 33));
    assertEquals(32, getRelativeId(32, 0));
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testEncodingIdLength
  public void testEncodingIdLength() {
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(0, 0));
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(-1, 0));
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(0, 32));
    assertEquals(1, LineMapEncoder.getRelativeMappingIdLength(31, 0));
    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(0, 33));
    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(32, 0));

    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(2047, 0));
    assertEquals(3, LineMapEncoder.getRelativeMappingIdLength(2048, 0));
    assertEquals(2, LineMapEncoder.getRelativeMappingIdLength(0, 2048));
    assertEquals(3, LineMapEncoder.getRelativeMappingIdLength(0, 2049));
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV2Test::testEncoding
  public void testEncoding() throws IOException {
    assertEquals("AA", getEntry(0, 0, 1));
    assertEquals("EA", getEntry(0, 0, 2));
    assertEquals("8A", getEntry(0, 0, 16));
    assertEquals("!AQA", getEntry(0, 0, 17));
    assertEquals("!ARA", getEntry(0, 0, 18));
    assertEquals("!A+A", getEntry(0, 0, 63));
    assertEquals("!A/A", getEntry(0, 0, 64));
    assertEquals("!!ABAA", getEntry(0, 0, 65));
    assertEquals("!!A//A", getEntry(0, 0, 4096));
    assertEquals("!!!ABAAA", getEntry(0, 0, 4097));

    assertEquals("Af", getEntry(31, 0, 1));
    assertEquals("BAg", getEntry(32, 0, 1));
    assertEquals("AB", getEntry(32, 31, 1));

    assertEquals("!AQf", getEntry(31, 0, 17));
    assertEquals("!BQAg", getEntry(32, 0, 17));
    assertEquals("!AQB", getEntry(32, 31, 17));

    assertEquals("!A/B", getEntry(32, 31, 64));
    assertEquals("!!ABAB", getEntry(32, 31, 65));
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testBasicMapping1
  public void testBasicMapping1() throws Exception {
    compileAndCheck("function __BASIC__() { }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testBasicMappingGoldenOutput
  public void testBasicMappingGoldenOutput() throws Exception {
    
    checkSourceMap("function __BASIC__() { }",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,QAASA,UAAS,EAAG;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"__BASIC__\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testBasicMapping2
  public void testBasicMapping2() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__) {}");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testLiteralMappings
  public void testLiteralMappings() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                    "var __VAR__ = '__STR__'; }");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testLiteralMappingsGoldenOutput
  public void testLiteralMappingsGoldenOutput() throws Exception {
    
    checkSourceMap("function __BASIC__(__PARAM1__, __PARAM2__) { " +
                   "var __VAR__ = '__STR__'; }",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,QAASA,UAAS,CAACC,UAAD,CAAaC,UAAb," +
                       "CAAyB,CAAE,IAAIC,QAAU,SAAhB;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"__BASIC__\",\"__PARAM1__\",\"__PARAM2__\"," +
                       "\"__VAR__\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testMultilineMapping
  public void testMultilineMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testMultilineMapping2
  public void testMultilineMapping2() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = 1;\n" +
                    "var __ANO__ = 2;\n" +
                    "}");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testMultiFunctionMapping
  public void testMultiFunctionMapping() throws Exception {
    compileAndCheck("function __BASIC__(__PARAM1__, __PARAM2__) {\n" +
                    "var __VAR__ = '__STR__';\n" +
                    "var __ANO__ = \"__STR2__\";\n" +
                    "}\n" +

                    "function __BASIC2__(__PARAM3__, __PARAM4__) {\n" +
                    "var __VAR2__ = '__STR2__';\n" +
                    "var __ANO2__ = \"__STR3__\";\n" +
                    "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput0
  public void testGoldenOutput0() throws Exception {
    
    checkSourceMap("",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\";\",\n" +
                   "\"sources\":[],\n" +
                   "\"names\":[]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput0a
  public void testGoldenOutput0a() throws Exception {
    
    checkSourceMap("a;",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"a\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput1
  public void testGoldenOutput1() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,QAASA,EAAC,CAACC,GAAD,CAAMC,GAAN," +
                       "CAAW,CAAED,GAAA,CAAMA,GAAN,CAAYC,GAAZ,CAAkB,CAAG," +
                       "OAAOD,IAA9B;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
                   "}\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("function f(foo, bar) { foo = foo + bar + 2; return foo; }",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,QAASA,EAATA,CAAWC,GAAXD,CAAgBE," +
                       "GAAhBF,EAAuBC,GAAvBD,CAA6BC,GAA7BD,CAAmCE,GAAnCF," +
                       "SAAmDC,IAAnDD;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput2
  public void testGoldenOutput2() throws Exception {
    checkSourceMap("function f(foo, bar) {\r\n\n\n\nfoo = foo + bar + foo;" +
                   "\nreturn foo;\n}",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,QAASA,EAAC,CAACC,GAAD,CAAMC,GAAN," +
                       "CAAW,CAIrBD,GAAA,CAAMA,GAAN,CAAYC,GAAZ,CAAkBD," +
                       "GAClB,OAAOA,IALc;\",\n" +
                   "\"sources\":[\"testcode\"],\n" +
                   "\"names\":[\"f\",\"foo\",\"bar\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput3
  public void testGoldenOutput3() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA;\",\n" +
                   "\"sources\":[\"c:\\\\myfile.js\"],\n" +
                   "\"names\":[\"foo\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput4
  public void testGoldenOutput4() throws Exception {
    checkSourceMap("c:\\myfile.js",
                   "foo;   boo;   goo;",

                   "{\n" +
                   "\"version\":3,\n" +
                   "\"file\":\"testcode\",\n" +
                   "\"lineCount\":1,\n" +
                   "\"mappings\":\"AAAAA,GAAOC,IAAOC;\",\n" +
                   "\"sources\":[\"c:\\\\myfile.js\"],\n" +
                   "\"names\":[\"foo\",\"boo\",\"goo\"]\n" +
                   "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testGoldenOutput5
  public void testGoldenOutput5() throws Exception {
    detailLevel = SourceMap.DetailLevel.ALL;

    checkSourceMap(
        "c:\\myfile.js",
        "\n" +
        "var foo=a + 'this is a really long line that will force the"
        + " mapping to span multiple lines 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + "' + c + d + e;",

        "{\n" +
        "\"version\":3,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":6,\n" +
        "\"mappings\":\"A;;;;AAGA,IAAIA,IAAIC,CAAJD,CAAQ,mxCAARA;AAA8xCE," +
            "CAA9xCF,CAAkyCG,CAAlyCH,CAAsyCI;\",\n" +
        "\"sources\":[\"c:\\\\myfile.js\"],\n" +
        "\"names\":[\"foo\",\"a\",\"c\",\"d\",\"e\"]\n" +
        "}\n");

    detailLevel = SourceMap.DetailLevel.SYMBOLS;

    checkSourceMap("c:\\myfile.js",
        "\n" +
        "var foo=a + 'this is a really long line that will force the"
        + " mapping to span multiple lines 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + " 123456789 123456789 123456789 123456789 123456789"
        + "' + c + d + e;",

        "{\n" +
        "\"version\":3,\n" +
        "\"file\":\"testcode\",\n" +
        "\"lineCount\":6,\n" +
        "\"mappings\":\"A;;;;IAGIA,IAAIC,CAAJD;AAA8xCE,CAA9xCF,CAAkyCG," +
            "CAAlyCH,CAAsyCI;\",\n" +
        "\"sources\":[\"c:\\\\myfile.js\"],\n" +
        "\"names\":[\"foo\",\"a\",\"c\",\"d\",\"e\"]\n" +
        "}\n");
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testBasicDeterminism
  public void testBasicDeterminism() throws Exception {
    RunResult result1 = compile("file1", "foo;", "file2", "bar;");
    RunResult result2 = compile("file2", "foo;", "file1", "bar;");

    String map1 = getSourceMap(result1);
    String map2 = getSourceMap(result2);

    
    

    
    String files1 = map1.split("\n")[4];
    String files2 = map2.split("\n")[4];

    assertEquals(files1, files2);
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testWriteMetaMap
  public void testWriteMetaMap() throws IOException {
    StringWriter out = new StringWriter();
    String name = "./app.js";
    List<SourceMapSection> appSections = Lists.newArrayList(
        SourceMapSection.forURL("src1", 0, 0),
        SourceMapSection.forURL("src2", 100, 10),
        SourceMapSection.forURL("src3", 150, 5));

    SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
    generator.appendIndexMapTo(out, name, appSections);

    assertEquals(
            "{\n" +
            "\"version\":3,\n" +
            "\"file\":\"./app.js\",\n" +
            "\"sections\":[\n" +
            "{\n" +
            "\"offset\":{\n" +
            "\"line\":0,\n" +
            "\"column\":0\n" +
            "},\n" +
            "\"url\":\"src1\"\n" +
            "},\n" +
            "{\n" +
            "\"offset\":{\n" +
            "\"line\":100,\n" +
            "\"column\":10\n" +
            "},\n" +
            "\"url\":\"src2\"\n" +
            "},\n" +
            "{\n" +
            "\"offset\":{\n" +
            "\"line\":150,\n" +
            "\"column\":5\n" +
            "},\n" +
            "\"url\":\"src3\"\n" +
            "}\n" +
            "]\n" +
            "}\n",
            out.toString());
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testWriteMetaMap2
  public void testWriteMetaMap2() throws IOException {
    StringWriter out = new StringWriter();
    String name = "./app.js";
    List<SourceMapSection> appSections = Lists.newArrayList(
        
        SourceMapSection.forMap(getEmptyMapFor("./part.js"), 0, 0),
        SourceMapSection.forURL("src2", 100, 10));

    SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
    generator.appendIndexMapTo(out, name, appSections);

    assertEquals(
            "{\n" +
            "\"version\":3,\n" +
            "\"file\":\"./app.js\",\n" +
            "\"sections\":[\n" +
            "{\n" +
            "\"offset\":{\n" +
            "\"line\":0,\n" +
            "\"column\":0\n" +
            "},\n" +
            "\"map\":{\n" +
              "\"version\":3,\n" +
              "\"file\":\"./part.js\",\n" +
              "\"lineCount\":1,\n" +
              "\"mappings\":\";\",\n" +
              "\"sources\":[],\n" +
              "\"names\":[]\n" +
            "}\n" +
            "\n" +
            "},\n" +
            "{\n" +
            "\"offset\":{\n" +
            "\"line\":100,\n" +
            "\"column\":10\n" +
            "},\n" +
            "\"url\":\"src2\"\n" +
            "}\n" +
            "]\n" +
            "}\n",
            out.toString());
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testParseSourceMetaMap
  public void testParseSourceMetaMap() throws Exception {
    final String INPUT1 = "file1";
    final String INPUT2 = "file2";
    LinkedHashMap<String, String> inputs = Maps.newLinkedHashMap();
    inputs.put(INPUT1, "var __FOO__ = 1;");
    inputs.put(INPUT2, "var __BAR__ = 2;");
    RunResult result1 = compile(inputs.get(INPUT1), INPUT1);
    RunResult result2 = compile(inputs.get(INPUT2), INPUT2);

    final String MAP1 = "map1";
    final String MAP2 = "map2";
    final LinkedHashMap<String, String> maps = Maps.newLinkedHashMap();
    maps.put(MAP1, result1.sourceMapFileContent);
    maps.put(MAP2, result2.sourceMapFileContent);

    List<SourceMapSection> sections = Lists.newArrayList();

    StringBuilder output = new StringBuilder();
    FilePosition offset = appendAndCount(output, result1.generatedSource);
    sections.add(SourceMapSection.forURL(MAP1, 0, 0));
    output.append(result2.generatedSource);
    sections.add(
        SourceMapSection.forURL(MAP2, offset.getLine(), offset.getColumn()));

    SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();
    StringBuilder mapContents = new StringBuilder();
    generator.appendIndexMapTo(mapContents, "out.js", sections);

    check(inputs, output.toString(), mapContents.toString(),
      new SourceMapSupplier() {
        @Override
        public String getSourceMap(String url){
          return maps.get(url);
      }});
  }

// com.google.debugging.sourcemap.SourceMapGeneratorV3Test::testSourceMapMerging
  public void testSourceMapMerging() throws Exception {
    final String INPUT1 = "file1";
    final String INPUT2 = "file2";
    LinkedHashMap<String, String> inputs = Maps.newLinkedHashMap();
    inputs.put(INPUT1, "var __FOO__ = 1;");
    inputs.put(INPUT2, "var __BAR__ = 2;");
    RunResult result1 = compile(inputs.get(INPUT1), INPUT1);
    RunResult result2 = compile(inputs.get(INPUT2), INPUT2);

    StringBuilder output = new StringBuilder();
    FilePosition offset = appendAndCount(output, result1.generatedSource);
    output.append(result2.generatedSource);

    SourceMapGeneratorV3 generator = new SourceMapGeneratorV3();

    generator.mergeMapSection(0, 0, result1.sourceMapFileContent);
    generator.mergeMapSection(offset.getLine(), offset.getColumn(),
        result2.sourceMapFileContent);

    StringBuilder mapContents = new StringBuilder();
    generator.appendTo(mapContents, "out.js");

    check(inputs, output.toString(), mapContents.toString());
  }

// com.google.javascript.jscomp.AliasExternalsTest::testGlobalAlias
  public void testGlobalAlias() {
    test("window.setTimeout(function() {}, 0);" +
         "var doc=window.document;" +
         "window.alert(\"foo\");" +
         "window.eval(\"1\");" +
         "window.location.href=\"http://www.example.com\";" +
         "function foo() {var window = \"bar\"; return window}foo();",

         "var GLOBAL_window=window;" +
         formatPropNameDecl("setTimeout") +
         "GLOBAL_window[$$PROP_setTimeout](function() {}, 0);" +
         "var doc=GLOBAL_window.document;" +
         "GLOBAL_window.alert(\"foo\");" +
         "GLOBAL_window.eval(\"1\");" +
         "GLOBAL_window.location.href=\"http://www.example.com\";" +
         "function foo() {var window = \"bar\"; return window}foo();");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testUnaliasable
  public void testUnaliasable() {
    test("function foo() {" +
          "var x=arguments.length;" +
          "var y=arguments.length;" +
          "var z=arguments.length;" +
          "var w=arguments.length;" +
          "return x + y + z + w" +
         "};foo();",

         formatPropNameDecl("length") +
         "function foo() {" +
          "var x=arguments[$$PROP_length];" +
          "var y=arguments[$$PROP_length];" +
          "var z=arguments[$$PROP_length];" +
          "var w=arguments[$$PROP_length];" +
          "return x + y + z + w" +
         "};foo();");

    test("var x=new ActiveXObject();" +
         "x.foo=\"bar\";" +
         "var y=new ActiveXObject();" +
         "y.foo=\"bar\";" +
         "var z=new ActiveXObject();" +
         "z.foo=\"bar\";",

         "var x=new ActiveXObject();" +
         "x.foo=\"bar\";" +
         "var y=new ActiveXObject();" +
         "y.foo=\"bar\";" +
         "var z=new ActiveXObject();" +
         "z.foo=\"bar\";");

    test("var _a=eval('foo'),_b=eval('foo'),_c=eval('foo'),_d=eval('foo')," +
             "_e=eval('foo'),_f=eval('foo'),_g=eval('foo');",
         "var _a=eval('foo'),_b=eval('foo'),_c=eval('foo'),_d=eval('foo')," +
             "_e=eval('foo'),_f=eval('foo'),_g=eval('foo');");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testAliasableGlobals
  public void testAliasableGlobals() {
    aliasableGlobals = "notused,length";
    test("function foo() {" +
          "var x=arguments.length;" +
          "var y=arguments.length;" +
          "var z=arguments.length;" +
          "var w=arguments.length;" +
          "return x + y + z + w" +
         "};foo();",

         formatPropNameDecl("length") +
         "function foo() {" +
          "var x=arguments[$$PROP_length];" +
          "var y=arguments[$$PROP_length];" +
          "var z=arguments[$$PROP_length];" +
          "var w=arguments[$$PROP_length];" +
          "return x + y + z + w" +
         "};foo();");

    aliasableGlobals = "notused,notlength";
    test("function foo() {" +
          "var x=arguments.length;" +
          "var y=arguments.length;" +
          "var z=arguments.length;" +
          "var w=arguments.length;" +
          "return x + y + z + w" +
         "};foo();",

         "function foo() {" +
          "var x=arguments.length;" +
          "var y=arguments.length;" +
          "var z=arguments.length;" +
          "var w=arguments.length;" +
          "return x + y + z + w" +
         "};foo();");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testAliasableAndUnaliasableGlobals
  public void testAliasableAndUnaliasableGlobals() {
    
    aliasableGlobals = "foo,bar";
    unaliasableGlobals = "";
    test("var x;", "var x;");

    
    aliasableGlobals = "";
    unaliasableGlobals = "baz,qux";
    test("var x;", "var x;");

    
    aliasableGlobals = "foo,bar";
    unaliasableGlobals = "baz,qux";
    try {
      test("var x;", "var x;");
      fail("Expected an IllegalArgumentException");
    } catch (IllegalArgumentException ex) {
      
    }
  }

// com.google.javascript.jscomp.AliasExternalsTest::testGlobalAssigment
  public void testGlobalAssigment() {
    test("var x=_USER_ID+window;" +
         "var y=_USER_ID+window;" +
         "var z=_USER_ID+window;" +
         "var w=x+y+z;" +
         "_USER_ID = \"foo\";" +
         "window++;",

         "var x=_USER_ID+window;" +
         "var y=_USER_ID+window;" +
         "var z=_USER_ID+window;" +
         "var w=x+y+z;" +
         "_USER_ID = \"foo\";" +
         "window++");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testNewOperator
  public void testNewOperator() {
    test("var x;new x(window);window;window;window;window;window",

         "var GLOBAL_window=window; var x;" +
         "  new x(GLOBAL_window);GLOBAL_window;GLOBAL_window;" +
         "  GLOBAL_window;GLOBAL_window;GLOBAL_window");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testGetProp
  public void testGetProp() {
    test("function foo(a,b){return a.length > b.length;}",
         formatPropNameDecl("length") +
         "function foo(a, b){return a[$$PROP_length] > b[$$PROP_length];}");
    test("Foo.prototype.bar = function() { return 'foo'; }",
         formatPropNameDecl("prototype") +
         "Foo[$$PROP_prototype].bar = function() { return 'foo'; }");
    test("Foo.notreplaced = 5", "Foo.notreplaced=5");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testIgnoredOps
  public void testIgnoredOps() {
    testSame("function foo() { this.length-- }");
    testSame("function foo() { this.length++ }");
    testSame("function foo() { this.length+=5 }");
    testSame("function foo() { this.length-=5 }");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testSetProp
  public void testSetProp() {
    test("function foo() { this.innerHTML = 'hello!'; }",
      formatSetPropFn("innerHTML")
        + "function foo() { SETPROP_innerHTML(this, 'hello!'); }");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testParentChild
  public void testParentChild() {
    test("a.length = b.length = c.length;", formatSetPropFn("length")
      + formatPropNameDecl("length")
      + "SETPROP_length(a, SETPROP_length(b, c[$$PROP_length]))");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testModulesWithoutDependencies
  public void testModulesWithoutDependencies() {
    test(createModules(MODULE_SRC_ONE, MODULE_SRC_TWO),
         new String[] {
           "var $$PROP_length=\"length\";a=b[$$PROP_length];" +
           "a=b[$$PROP_length];a=b[$$PROP_length];",
           "c=d[$$PROP_length];"});
  }

// com.google.javascript.jscomp.AliasExternalsTest::testModulesWithDependencies
  public void testModulesWithDependencies() {
    test(createModuleChain(MODULE_SRC_ONE, MODULE_SRC_TWO),
         new String[] {
           "var $$PROP_length=\"length\";a=b[$$PROP_length];" +
           "a=b[$$PROP_length];a=b[$$PROP_length];",
           "c=d[$$PROP_length];"});
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropAccessorPushedDeeper1
  public void testPropAccessorPushedDeeper1() {
    test(createModuleChain("var a = \"foo\";", "var b = a.length;"),
         new String[] {
           "var a = \"foo\";",
           formatPropNameDecl("length") + "var b = a[$$PROP_length]" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropAccessorPushedDeeper2
  public void testPropAccessorPushedDeeper2() {
    test(createModuleChain(
             "var a = \"foo\";", "var b = a.length;", "var c = a.length;"),
         new String[] {
           "var a = \"foo\";",
           formatPropNameDecl("length") + "var b = a[$$PROP_length]",
           "var c = a[$$PROP_length]" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropAccessorPushedDeeper3
  public void testPropAccessorPushedDeeper3() {
    test(createModuleStar(
             "var a = \"foo\";", "var b = a.length;", "var c = a.length;"),
         new String[] {
           formatPropNameDecl("length") + "var a = \"foo\";",
           "var b = a[$$PROP_length]",
           "var c = a[$$PROP_length]" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropAccessorNotPushedDeeper
  public void testPropAccessorNotPushedDeeper() {
    test(createModuleChain("var a = \"foo\"; var b = a.length;",
                                    "var c = a.length;"),
         new String[] {
           formatPropNameDecl("length") +
           "var a = \"foo\"; var b = a[$$PROP_length]",
           "var c = a[$$PROP_length]" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropMutatorPushedDeeper
  public void testPropMutatorPushedDeeper() {
    test(createModuleChain("var a = [1];", "a.length = 0;"),
         new String[] {
           "var a = [1];",
           formatSetPropFn("length") + "SETPROP_length(a, 0);" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testPropMutatorNotPushedDeeper
  public void testPropMutatorNotPushedDeeper() {
    test(createModuleChain(
             "var a = [1]; a.length = 1;", "a.length = 0;"),
         new String[] {
           formatSetPropFn("length") +  "var a = [1]; SETPROP_length(a, 1);",
           "SETPROP_length(a, 0);" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testGlobalAliasPushedDeeper
  public void testGlobalAliasPushedDeeper() {
    test(createModuleChain(
             "var a = 1;",
             "var b = window, c = window, d = window, e = window;"),
         new String[] { "var a = 1;",
                        "var GLOBAL_window = window;" +
                        "var b = GLOBAL_window, c = GLOBAL_window, " +
                        "    d = GLOBAL_window, e = GLOBAL_window;" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testGlobalAliasNotPushedDeeper
  public void testGlobalAliasNotPushedDeeper() {
    test(createModuleChain(
             "var a = 1, b = window;",
             "var c = window, d = window, e = window;"),
         new String[] { "var GLOBAL_window = window;" +
                        "var a = 1, b = GLOBAL_window;",
                        "var c = GLOBAL_window, " +
                        "    d = GLOBAL_window, e = GLOBAL_window;" });
  }

// com.google.javascript.jscomp.AliasExternalsTest::testNoAliasAnnotationForSingleVar
  public void testNoAliasAnnotationForSingleVar() {
    testSame("[RangeObject, RangeObject, RangeObject]");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testNoAliasAnnotationForMultiVarDeclaration
  public void testNoAliasAnnotationForMultiVarDeclaration() {
    test("[RuntimeObject, RuntimeObject, RuntimeObject," +
         " SelectionObject, SelectionObject, SelectionObject]",
         "var GLOBAL_SelectionObject = SelectionObject;" +
         "[RuntimeObject, RuntimeObject, RuntimeObject," +
         " GLOBAL_SelectionObject, GLOBAL_SelectionObject," +
         " GLOBAL_SelectionObject]");
  }

// com.google.javascript.jscomp.AliasExternalsTest::testNoAliasAnnotationForFunction
  public void testNoAliasAnnotationForFunction() {
    testSame("[NoAliasFunction(), NoAliasFunction(), NoAliasFunction()]");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar1
  public void testOneVar1() {
    test(" var Foo = function(){};Foo.prototype.b = 0;",
         "var Foo = function(){};Foo.prototype.a = 0;");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar2
  public void testOneVar2() {
    testSame(" var Foo = function(){};" +
             "Foo.prototype = {b: 0};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar3
  public void testOneVar3() {
    testSame(" var Foo = function(){};" +
             "Foo.prototype = {get b() {return 0}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOneVar4
  public void testOneVar4() {
    testSame(" var Foo = function(){};" +
             "Foo.prototype = {set b(a) {}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoVar1
  public void testTwoVar1() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.x=0;";
    String output = ""
        + "var Foo = function(){};\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.b=0;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoVar2
  public void testTwoVar2() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype={z:0, z:1, x:0};\n";
    
    testSame(js);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoIndependentVar
  public void testTwoIndependentVar() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.b = 0;\n"
        + " var Bar = function(){};\n"
        + "Bar.prototype.c = 0;";
    String output = ""
        + "var Foo = function(){};"
        + "Foo.prototype.a=0;"
        + "var Bar = function(){};"
        + "Bar.prototype.a=0;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTwoTypesTwoVar
  public void testTwoTypesTwoVar() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.r = 0;\n"
        + "Foo.prototype.g = 0;\n"
        + " var Bar = function(){};\n"
        + "Bar.prototype.c = 0;"
        + "Bar.prototype.r = 0;";
    String output = ""
        + "var Foo = function(){};"
        + "Foo.prototype.a=0;"
        + "Foo.prototype.b=0;"
        + "var Bar = function(){};"
        + "Bar.prototype.b=0;"
        + "Bar.prototype.a=0;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testUnion
  public void testUnion() {
    String js = ""
        + " var Foo = function(){};\n"
        + " var Bar = function(){};\n"
        + "Foo.prototype.foodoo=0;\n"
        + "Bar.prototype.bardoo=0;\n"
        + "\n"
        + "var U;\n"
        + "U.joint;"
        + "U.joint";
    String output = ""
        + "var Foo = function(){};\n"
        + "var Bar = function(){};\n"
        + "Foo.prototype.b=0;\n"
        + "Bar.prototype.b=0;\n"
        + "var U;\n"
        + "U.a;"
        + "U.a";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testUnions
  public void testUnions() {
    String js = ""
        + " var Foo = function(){};\n"
        + " var Bar = function(){};\n"
        + " var Baz = function(){};\n"
        + " var Bat = function(){};\n"
        + "Foo.prototype.lone1=0;\n"
        + "Bar.prototype.lone2=0;\n"
        + "Baz.prototype.lone3=0;\n"
        + "Bat.prototype.lone4=0;\n"
        + "\n"
        + "var U1;\n"
        + "U1.j1;"
        + "U1.j2;"
        + "\n"
        + "var U2;\n"
        + "U2.j3;"
        + "U2.j4;"
        + "\n"
        + "var U3;"
        + "U3.j5;"
        + "U3.j6";
    String output = ""
        + "var Foo = function(){};\n"
        + "var Bar = function(){};\n"
        + "var Baz = function(){};\n"
        + "var Bat = function(){};\n"
        + "Foo.prototype.c=0;\n"
        + "Bar.prototype.e=0;\n"
        + "Baz.prototype.e=0;\n"
        + "Bat.prototype.c=0;\n"
        + "var U1;\n"
        + "U1.a;"
        + "U1.b;"
        + "var U2;\n"
        + "U2.c;"
        + "U2.d;"
        + "var U3;"
        + "U3.a;"
        + "U3.b";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExtends
  public void testExtends() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.x=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.prototype.y=0;\n"
        + "Bar.prototype.z=0;\n"
        + " var Baz = function(){};\n"
        + "Baz.prototype.l=0;\n"
        + "Baz.prototype.m=0;\n"
        + "Baz.prototype.n=0;\n"
        + "(new Baz).m\n";
    String output = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.a=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.prototype.b=0;\n"
        + "Bar.prototype.c=0;\n"
        + " var Baz = function(){};\n"
        + "Baz.prototype.b=0;\n"
        + "Baz.prototype.a=0;\n"
        + "Baz.prototype.c=0;\n"
        + "(new Baz).a\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testLotsOfVars
  public void testLotsOfVars() {
    StringBuilder js = new StringBuilder();
    StringBuilder output = new StringBuilder();
    js.append(" var Foo = function(){};\n");
    js.append(" var Bar = function(){};\n");
    output.append(js.toString());

    int vars = 10;
    for (int i = 0; i < vars; i++) {
      js.append("Foo.prototype.var" + i + " = 0;");
      js.append("Bar.prototype.var" + (i + 10000) + " = 0;");
      output.append("Foo.prototype." + (char) ('a' + i) + "=0;");
      output.append("Bar.prototype." + (char) ('a' + i) + "=0;");
    }
    test(js.toString(), output.toString());
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testLotsOfClasses
  public void testLotsOfClasses() {
    StringBuilder b = new StringBuilder();
    int classes = 10;
    for (int i = 0; i < classes; i++) {
      String c = "Foo" + i;
      b.append(" var " + c + " = function(){};\n");
      b.append(c + ".prototype.varness" + i + " = 0;");
    }
    String js = b.toString();
    test(js, js.replaceAll("varness\\d+", "a"));
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testFunctionType
  public void testFunctionType() {
    String js = ""
        + " function Foo(){};\n"
        + "\n"
        + "Foo.prototype.fun = function() { return new Bar(); };\n"
        + " function Bar(){};\n"
        + "Bar.prototype.bazz;\n"
        + "(new Foo).fun().bazz();";
    String output = ""
        + "function Foo(){};\n"
        + "Foo.prototype.a = function() { return new Bar(); };\n"
        + "function Bar(){};\n"
        + "Bar.prototype.a;\n"
        + "(new Foo).a().a();";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPrototypePropertiesAsObjLitKeys1
  public void testPrototypePropertiesAsObjLitKeys1() {
    test(" function Bar() {};" +
             "Bar.prototype = {2: function(){}, getA: function(){}};",
             " function Bar() {};" +
             "Bar.prototype = {2: function(){}, a: function(){}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPrototypePropertiesAsObjLitKeys2
  public void testPrototypePropertiesAsObjLitKeys2() {
    testSame(" function Bar() {};" +
             "Bar.prototype = {2: function(){}, 'getA': function(){}};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testQuotedPrototypeProperty
  public void testQuotedPrototypeProperty() {
    testSame(" function Bar() {};" +
             "Bar.prototype['getA'] = function(){};" +
             "var bar = new Bar();" +
             "bar['getA']();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testOverlappingOriginalAndGeneratedNames
  public void testOverlappingOriginalAndGeneratedNames() {
    test(" function Bar(){};"
         + "Bar.prototype.b = function(){};"
         + "Bar.prototype.a = function(){};"
         + "var bar = new Bar();"
         + "bar.b();",
         "function Bar(){};"
         + "Bar.prototype.a = function(){};"
         + "Bar.prototype.b = function(){};"
         + "var bar = new Bar();"
         + "bar.a();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyAddedToObject
  public void testPropertyAddedToObject() {
    testSame("var foo = {}; foo.prop = '';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyAddedToFunction
  public void testPropertyAddedToFunction() {
    test("var foo = function(){}; foo.prop = '';",
         "var foo = function(){}; foo.a = '';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyOfObjectOfUnknownType
  public void testPropertyOfObjectOfUnknownType() {
    testSame("var foo = x(); foo.prop = '';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testPropertyOnParamOfUnknownType
  public void testPropertyOnParamOfUnknownType() {
    testSame(" function Foo(){};\n"
             + "Foo.prototype.prop = 0;"
             + "function go(aFoo){\n"
             + "  aFoo.prop = 1;"
             + "}");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testSetPropertyOfGlobalThis
  public void testSetPropertyOfGlobalThis() {
    testSame("this.prop = 'bar'");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testReadPropertyOfGlobalThis
  public void testReadPropertyOfGlobalThis() {
    testSame("f(this.prop);");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testSetQuotedPropertyOfThis
  public void testSetQuotedPropertyOfThis() {
    testSame("this['prop'] = 'bar';");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExternedPropertyName
  public void testExternedPropertyName() {
    test(" var Bar = function(){};"
         + " Bar.prototype.toString = function(){};"
         + "Bar.prototype.func = function(){};"
         + "var bar = new Bar();"
         + "bar.toString();",
         "var Bar = function(){};"
         + "Bar.prototype.toString = function(){};"
         + "Bar.prototype.a = function(){};"
         + "var bar = new Bar();"
         + "bar.toString();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExternedPropertyNameDefinedByObjectLiteral
  public void testExternedPropertyNameDefinedByObjectLiteral() {
    testSame("function Bar(){};Bar.prototype.factory");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticAndInstanceMethodWithSameName
  public void testStaticAndInstanceMethodWithSameName() {
    test("function Bar(){}; Bar.getA = function(){}; " +
         "Bar.prototype.getA = function(){}; Bar.getA();" +
         "var bar = new Bar(); bar.getA();",
         "function Bar(){}; Bar.a = function(){};" +
         "Bar.prototype.a = function(){}; Bar.a();" +
         "var bar = new Bar(); bar.a();");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticAndInstanceProperties
  public void testStaticAndInstanceProperties() {
    test("function Bar(){};" +
         "Bar.getA = function(){}; " +
         "Bar.prototype.getB = function(){};",
         "function Bar(){}; Bar.a = function(){};" +
         "Bar.prototype.a = function(){};");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticAndSubInstanceProperties
  public void testStaticAndSubInstanceProperties() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.x=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.y=0;\n"
        + "Bar.prototype.z=0;\n";
    String output = ""
        + " var Foo = function(){};\n"
        + "Foo.a=0;\n"
        + " var Bar = function(){};\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.a=0;\n"
        + "Bar.prototype.a=0;\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testStaticWithFunctions
  public void testStaticWithFunctions() {
    String js = ""
      + " var Foo = function() {};\n"
      + "Foo.x = 0;"
      + " function f(x) { x.y = 1 }"
      + "f(Foo)";
    String output = ""
      + " var Foo = function() {};\n"
      + "Foo.a = 0;"
      + " function f(x) { x.y = 1 }"
      + "f(Foo)";
    test(js, output);

    js = ""
      + " var Foo = function() {};\n"
      + "Foo.x = 0;"
      + " function f(x) { x.y = 1; x.x = 2;}"
      + "f(Foo)";
    test(js, js);

    js = ""
      + " var Foo = function() {};\n"
      + "Foo.x = 0;"
      + " var Bar = function() {};\n"
      + "Bar.y = 0;";

    output = ""
      + " var Foo = function() {};\n"
      + "Foo.a = 0;"
      + " var Bar = function() {};\n"
      + "Bar.a = 0;";
    test(js, output);

  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testTypeMismatch
  public void testTypeMismatch() {
    testSame(EXTERNS, "var Foo = function(){};\n"
             + "var Bar = function(){};\n"
             + "Foo.prototype.b = 0;\n"
             + "\n"
             + "var F = new Bar();",
             TypeValidator.TYPE_MISMATCH_WARNING,
             "initializing variable\n"
             + "found   : Bar\n"
             + "required: (Foo|null)");
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testRenamingMap
  public void testRenamingMap() {
    String js = ""
        + " var Foo = function(){};\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.z=0;\n"
        + "Foo.prototype.x=0;\n"
        + "Foo.prototype.y=0;";
    String output = ""
        + "var Foo = function(){};\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.a=0;\n"
        + "Foo.prototype.b=0;\n"
        + "Foo.prototype.c=0;";
    test(js, output);

    Map<String, String> answerMap = Maps.newHashMap();
    answerMap.put("x", "b");
    answerMap.put("y", "c");
    answerMap.put("z", "a");
    assertEquals(answerMap, lastPass.getRenamingMap());
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testInline
  public void testInline() {
    String js = ""
        + " function Foo(){}\n"
        + "Foo.prototype.x = function(){};\n"
        + "\n"
        + "function Bar(){}\n"
        + "\n"
        + "Bar.prototype.x = function() { return this.y; };\n"
        + "Bar.prototype.z = function() {};\n"
        
        + " (new Bar).y;";
    String output = ""
        + "function Foo(){}\n"
        + "Foo.prototype.a = function(){};\n"
        + "function Bar(){}\n"
        + "Bar.prototype.a = function() { return this.b; };\n"
        + "Bar.prototype.c = function() {};\n"
        
        + "(new Bar).b;";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testImplementsAndExtends
  public void testImplementsAndExtends() {
    String js = ""
        + " function Foo() {}\n"
        + "\n"
        + "function Bar(){}\n"
        + "Bar.prototype.y = function() { return 3; };\n"
        + "\n"
        + "function SubBar(){ }\n"
        + " function f(x) { x.z = 3; }\n"
        + " function g(x) { x.z = 3; }";
    String output = ""
        + "function Foo(){}\n"
        + "function Bar(){}\n"
        + "Bar.prototype.b = function() { return 3; };\n"
        + "function SubBar(){}\n"
        + "function f(x) { x.a = 3; }\n"
        + "function g(x) { x.a = 3; }";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testImplementsAndExtends2
  public void testImplementsAndExtends2() {
    String js = ""
        + " function A() {}\n"
        + "\n"
        + "function C1(){}\n"
        + "\n"
        + "function C2(){}\n"
        + " function f(x) { x.y = 3; }\n"
        + " function g(x) { x.z = 3; }\n";
    String output = ""
        + "function A(){}\n"
        + "function C1(){}\n"
        + "function C2(){}\n"
        + "function f(x) { x.a = 3; }\n"
        + "function g(x) { x.b = 3; }\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testExtendsInterface
  public void testExtendsInterface() {
    String js = ""
        + " function A() {}\n"
        + " function B() {}\n"
        + " function f(x) { x.y = 3; }\n"
        + " function g(x) { x.z = 3; }\n";
    String output = ""
        + "function A(){}\n"
        + "function B(){}\n"
        + "function f(x) { x.a = 3; }\n"
        + "function g(x) { x.b = 3; }\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testFunctionSubType
  public void testFunctionSubType() {
    String js = ""
        + "Function.prototype.a = 1;\n"
        + "function f() {}\n"
        + "f.y = 2;\n";
    String output = ""
        + "Function.prototype.a = 1;\n"
        + "function f() {}\n"
        + "f.b = 2;\n";
    test(js, output);
  }

// com.google.javascript.jscomp.AmbiguatePropertiesTest::testFunctionSubType2
  public void testFunctionSubType2() {
    String js = ""
        + "Function.prototype.a = 1;\n"
        + " function F() {}\n"
        + "F.y = 2;\n";
    String output = ""
        + "Function.prototype.a = 1;\n"
        + "function F() {}\n"
        + "F.b = 2;\n";
    test(js, output);
  }

// com.google.javascript.jscomp.ChainCallsTest::testUnchainedCalls
  public void testUnchainedCalls() {
    test(
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + "var f = new Foo();\n"
        + "f.bar();\n"
        + "f.bar();\n",
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + "var f = new Foo();\n"
        + "f.bar().bar();\n");

  }

// com.google.javascript.jscomp.ChainCallsTest::testSecondCallReturnNotThis
  public void testSecondCallReturnNotThis() {
    test(
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + "Foo.prototype.baz = function() {};\n"
        + "var f = new Foo();\n"
        + "f.bar();\n"
        + "f.baz();\n",
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + "Foo.prototype.baz = function() {};\n"
        + "var f = new Foo();\n"
        + "f.bar().baz();\n");
  }

// com.google.javascript.jscomp.ChainCallsTest::testDifferentInstance
  public void testDifferentInstance() {
    testSame(
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + "new Foo().bar();\n"
        + "new Foo().bar();\n");
  }

// com.google.javascript.jscomp.ChainCallsTest::testSubclass
  public void testSubclass() {
    testSame(
        ""
        + " function Foo() {}\n"
        + "Foo.prototype.bar = function() { return this; };\n"
        + " function Baz() {}\n"
        + "Baz.prototype.bar = function() {};\n"
        + "( new Baz()).bar();\n"
        + "( new Baz()).bar();\n");
  }

// com.google.javascript.jscomp.ChainCallsTest::testSimpleDefinitionFinder
  public void testSimpleDefinitionFinder() {
    String defs =
        " function Foo() {}\n" +
        "Foo.prototype.a = function() { return this; };" +
        " function Bar() {}\n" +
        "Bar.prototype.a = function() {};";
    testSame(
        defs +
        "var o = new Foo; o.a(); o.a();");
    testSame(
        defs +
        "var o = new Bar; o.a(); o.a();");
  }

// com.google.javascript.jscomp.ChainCallsTest::testSimpleDefinitionFinder2
  public void testSimpleDefinitionFinder2() {
    String defs =
        " function Foo() {}\n" +
        "Foo.prototype.a = function() { return this; };" +
        " function Bar() {}\n" +
        "Bar.prototype.a = function() { return this; };";
    testSame(
        defs +
        "var o = new Foo; o.a().a();");
    testSame(
        defs +
        "var o = new Bar; o.a().a();");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testDeprecatedFunction
  public void testDeprecatedFunction() {
    testDep(" function f() {} function g() { f(); }",
            "Some Reason",
            DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningOnDeprecatedConstVariable
  public void testWarningOnDeprecatedConstVariable() {
    testDep(" var f = 4; function g() { alert(f); }",
            "Another reason",
            DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testThatNumbersArentDeprecated
  public void testThatNumbersArentDeprecated() {
    testSame(" var f = 4; var h = 3; " +
             "function g() { alert(h); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testDeprecatedFunctionVariable
  public void testDeprecatedFunctionVariable() {
    testDep(" var f = function() {}; " +
            "function g() { f(); }", "I like g...",
            DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInGlobalScope
  public void testNoWarningInGlobalScope() {
    testSame("var goog = {}; goog.makeSingleton = function(x) {};" +
        " function f() {} goog.makeSingleton(f);");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInGlobalScopeForCall
  public void testNoWarningInGlobalScopeForCall() {
    testDep(" function f() {} f();",
            "Some global scope", DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedFunction
  public void testNoWarningInDeprecatedFunction() {
    testSame(" function f() {} " +
             " function g() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningInNormalClass
  public void testWarningInNormalClass() {
    testDep(" function f() {}" +
            "  var Foo = function() {}; " +
            "Foo.prototype.bar = function() { f(); }",
            "FooBar", DEPRECATED_NAME, DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForProperty1
  public void testWarningForProperty1() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = 3;" +
            "Foo.prototype.baz = function() { alert((new Foo()).bar); };",
            "A property is bad",
            DEPRECATED_PROP, DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForProperty2
  public void testWarningForProperty2() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = 3;" +
            "Foo.prototype.baz = function() { alert(this.bar); };",
            "Zee prop, it is deprecated!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedClass
  public void testWarningForDeprecatedClass() {
    testDep(" function Foo() {} " +
            "function f() { new Foo(); }",
            "Use the class 'Bar'",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningForDeprecatedClassInstance
  public void testNoWarningForDeprecatedClassInstance() {
    testSame(" function Foo() {} " +
             " function f(x) { return x; }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedSuperClass
  public void testWarningForDeprecatedSuperClass() {
    testDep(" function Foo() {} " +
            " function SubFoo() {}" +
            "function f() { new SubFoo(); }",
            "Superclass to the rescue!",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedSuperClass2
  public void testWarningForDeprecatedSuperClass2() {
    testDep(" function Foo() {} " +
            "var namespace = {}; " +
            " " +
            "namespace.SubFoo = function() {}; " +
            "function f() { new namespace.SubFoo(); }",
            "Its only weakness is Kryptoclass",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForPrototypeProperty
  public void testWarningForPrototypeProperty() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = 3;" +
            "Foo.prototype.baz = function() { alert(Foo.prototype.bar); };",
            "It is now in production, use that model...",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningForNumbers
  public void testNoWarningForNumbers() {
    testSame(" function Foo() {}" +
             " Foo.prototype.bar = 3;" +
             "Foo.prototype.baz = function() { alert(3); };");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForMethod1
  public void testWarningForMethod1() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = function() {};" +
            "Foo.prototype.baz = function() { this.bar(); };",
            "There is a madness to this method",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForMethod2
  public void testWarningForMethod2() {
    testDep(" function Foo() {} " +
            " Foo.prototype.bar; " +
            "Foo.prototype.baz = function() { this.bar(); };",
            "Stop the ringing!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedClass
  public void testNoWarningInDeprecatedClass() {
    testSame(" function f() {} " +
             " " +
             "var Foo = function() {}; " +
             "Foo.prototype.bar = function() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedClass2
  public void testNoWarningInDeprecatedClass2() {
    testSame(" function f() {} " +
             " " +
             "var Foo = function() {}; " +
             "Foo.bar = function() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningInDeprecatedStaticMethod
  public void testNoWarningInDeprecatedStaticMethod() {
    testSame(" function f() {} " +
             " " +
             "var Foo = function() {}; " +
             " Foo.bar = function() { f(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningInStaticMethod
  public void testWarningInStaticMethod() {
    testDep(" function f() {} " +
            " " +
            "var Foo = function() {}; " +
            "Foo.bar = function() { f(); }",
            "crazy!",
            DEPRECATED_NAME,
            DEPRECATED_NAME_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testDeprecatedObjLitKey
  public void testDeprecatedObjLitKey() {
    testDep("var f = {};  f.foo = 3; " +
            "function g() { return f.foo; }",
            "It is literally not used anymore",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSubclassMethod
  public void testWarningForSubclassMethod() {
    testDep(" function Foo() {}" +
            "Foo.prototype.bar = function() {};" +
            " function SubFoo() {}" +
            " SubFoo.prototype.bar = function() {};" +
            "function f() { (new SubFoo()).bar(); };",
            "I have a parent class!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSuperClassWithDeprecatedSubclassMethod
  public void testWarningForSuperClassWithDeprecatedSubclassMethod() {
    testSame(" function Foo() {}" +
             "Foo.prototype.bar = function() {};" +
             " function SubFoo() {}" +
             " SubFoo.prototype.bar = " +
             "function() {};" +
             "function f() { (new Foo()).bar(); };");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSuperclassMethod
  public void testWarningForSuperclassMethod() {
    testDep(" function Foo() {}" +
            " Foo.prototype.bar = function() {};" +
            " function SubFoo() {}" +
            "SubFoo.prototype.bar = function() {};" +
            "function f() { (new SubFoo()).bar(); };",
            "I have a child class!",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForSuperclassMethod2
  public void testWarningForSuperclassMethod2() {
    testDep(" function Foo() {}" +
            "" +
            "Foo.prototype.bar = function() {};" +
            " function SubFoo() {}" +
            "SubFoo.prototype.bar = function() {};" +
            "function f() { (new SubFoo()).bar(); };",
            "I have another child class...",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForBind
  public void testWarningForBind() {
    testDep(" Function.prototype.bind = function() {};" +
            "(function() {}).bind();",
            "I'm bound to this method...",
            DEPRECATED_PROP,
            DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testWarningForDeprecatedClassInGlobalScope
  public void testWarningForDeprecatedClassInGlobalScope() {
    testDep(" var Foo = function() {};" +
            "new Foo();",
            "I'm a very worldly object!",
            DEPRECATED_CLASS,
            DEPRECATED_CLASS_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningForPrototypeCopying
  public void testNoWarningForPrototypeCopying() {
    testSame(" var Foo = function() {};" +
             "Foo.prototype.bar = function() {};" +
             " Foo.prototype.baz = Foo.prototype.bar;" +
             "(new Foo()).bar();");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoWarningOnDeprecatedPrototype
  public void testNoWarningOnDeprecatedPrototype() {
    
    testSame(" var Foo = function() {};" +
        " Foo.prototype = {};" +
        "Foo.prototype.bar = function() {};");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForNames
  public void testPrivateAccessForNames() {
    testSame(" function foo_() {}; foo_();");
    test(new String[] {
      " function foo_() {};",
      "foo_();"
    }, null, BAD_PRIVATE_GLOBAL_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties1
  public void testPrivateAccessForProperties1() {
    testSame(" function Foo() {}" +
        " Foo.prototype.bar_ = function() {};" +
        "Foo.prototype.baz = function() { this.bar_(); }; (new Foo).bar_();");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties2
  public void testPrivateAccessForProperties2() {
    testSame(new String[] {
      " function Foo() {}",
      " Foo.prototype.bar_ = function() {};" +
      "Foo.prototype.baz = function() { this.bar_(); }; (new Foo).bar_();"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties3
  public void testPrivateAccessForProperties3() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar_ = function() {}; (new Foo).bar_();",
      "Foo.prototype.baz = function() { this.bar_(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testPrivateAccessForProperties4
  public void testPrivateAccessForProperties4() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar_ = function() {};",
      "Foo.prototype['baz'] = function() { (new Foo()).bar_(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties1
  public void testNoPrivateAccessForProperties1() {
    test(new String[] {
      " function Foo() {} (new Foo).bar_();",
      " Foo.prototype.bar_ = function() {};" +
      "Foo.prototype.baz = function() { this.bar_(); };"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties2
  public void testNoPrivateAccessForProperties2() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};" +
      "Foo.prototype.baz = function() { this.bar_(); };",
      "(new Foo).bar_();"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties3
  public void testNoPrivateAccessForProperties3() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " function OtherFoo() { (new Foo).bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties4
  public void testNoPrivateAccessForProperties4() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " " +
      "function SubFoo() { this.bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties5
  public void testNoPrivateAccessForProperties5() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " " +
      "function SubFoo() {};" +
      "SubFoo.prototype.baz = function() { this.bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties6
  public void testNoPrivateAccessForProperties6() {
    
    
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};",
      " " +
      "function SubFoo() {};" +
      "SubFoo.prototype.bar_ = function() {};"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties7
  public void testNoPrivateAccessForProperties7() {
    
    
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar_ = function() {};" +
      " " +
      "function SubFoo() {};" +
      "SubFoo.prototype.bar_ = function() {};",
      "SubFoo.prototype.baz = function() { this.bar_(); }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoPrivateAccessForProperties8
  public void testNoPrivateAccessForProperties8() {
    test(new String[] {
      " function Foo() {  this.bar_ = 3; }",
      " " +
      "function SubFoo() {  this.bar_ = 3; };"
    }, null, PRIVATE_OVERRIDE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties1
  public void testProtectedAccessForProperties1() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "Foo.prototype.baz = function() { this.bar(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties2
  public void testProtectedAccessForProperties2() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "" +
      "function SubFoo() { this.bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties3
  public void testProtectedAccessForProperties3() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "" +
      "function SubFoo() { }" +
      "SubFoo.baz = function() { (new Foo).bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties4
  public void testProtectedAccessForProperties4() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.bar = function() {};",
      "" +
      "function SubFoo() { Foo.bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties5
  public void testProtectedAccessForProperties5() {
    testSame(new String[] {
      " function Foo() {}" +
      " Foo.prototype.bar = function() {};" +
      "(new Foo).bar();",
      "" +
      "var SubFoo = function() { this.bar(); }"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testProtectedAccessForProperties6
  public void testProtectedAccessForProperties6() {
    testSame(new String[] {
      "var goog = {};" +
      " goog.Foo = function() {};" +
      " goog.Foo.prototype.bar = function() {};",
      "" +
      "goog.SubFoo = function() { this.bar(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties1
  public void testNoProtectedAccessForProperties1() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar = function() {};",
      "(new Foo).bar();"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties2
  public void testNoProtectedAccessForProperties2() {
    test(new String[] {
      " function Foo() {} " +
      " Foo.prototype.bar = function() {};",
      " function OtherFoo() { (new Foo).bar(); }"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties3
  public void testNoProtectedAccessForProperties3() {
    test(new String[] {
      " function Foo() {} " +
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
      " " +
      "function SubberFoo() { (new SubFoo).bar(); }"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties4
  public void testNoProtectedAccessForProperties4() {
    test(new String[] {
      " function Foo() { (new SubFoo).bar(); } ",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoProtectedAccessForProperties5
  public void testNoProtectedAccessForProperties5() {
    test(new String[] {
      "var goog = {};" +
      " goog.Foo = function() {};" +
      " goog.Foo.prototype.bar = function() {};",
      "" +
      "goog.NotASubFoo = function() { (new goog.Foo).bar(); };"
    }, null, BAD_PROTECTED_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoExceptionsWithBadConstructors1
  public void testNoExceptionsWithBadConstructors1() {
    testSame(new String[] {
      "function Foo() { (new SubFoo).bar(); } " +
      " function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNoExceptionsWithBadConstructors2
  public void testNoExceptionsWithBadConstructors2() {
    testSame(new String[] {
      " function Foo() {} " +
      "Foo.prototype.bar = function() {};" +
      "" +
      "function SubFoo() {}" +
      " " +
      "SubFoo.prototype.bar = function() { (new Foo).bar(); };"
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testGoodOverrideOfProtectedProperty
  public void testGoodOverrideOfProtectedProperty() {
    testSame(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testBadOverrideOfProtectedProperty
  public void testBadOverrideOfProtectedProperty() {
    test(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    }, null, VISIBILITY_MISMATCH);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testBadOverrideOfPrivateProperty
  public void testBadOverrideOfPrivateProperty() {
    test(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      " SubFoo.prototype.bar = function() {};",
    }, null, PRIVATE_OVERRIDE);

    testSame(new String[] {
      " function Foo() { } " +
      " Foo.prototype.bar = function() {};",
      " " +
      "function SubFoo() {}" +
      "\n" +
      " SubFoo.prototype.bar = function() {};",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAccessOfStaticMethodOnPrivateConstructor
  public void testAccessOfStaticMethodOnPrivateConstructor() {
    testSame(new String[] {
      " function Foo() { } " +
      "Foo.create = function() { return new Foo(); };",
      "Foo.create()",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAccessOfStaticMethodOnPrivateQualifiedConstructor
  public void testAccessOfStaticMethodOnPrivateQualifiedConstructor() {
    testSame(new String[] {
      "var goog = {};" +
      " goog.Foo = function() { }; " +
      "goog.Foo.create = function() { return new goog.Foo(); };",
      "goog.Foo.create()",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testInstanceofOfPrivateConstructor
  public void testInstanceofOfPrivateConstructor() {
    testSame(new String[] {
      "var goog = {};" +
      " goog.Foo = function() { }; " +
      "goog.Foo.create = function() { return new goog.Foo(); };",
      "goog instanceof goog.Foo",
    });
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testOkAssignmentOfDeprecatedProperty
  public void testOkAssignmentOfDeprecatedProperty() {
    testSame(
        " function Foo() {" +
        "  this.bar = 3;" +
        "}");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testBadReadOfDeprecatedProperty
  public void testBadReadOfDeprecatedProperty() {
    testDep(
        " function Foo() {" +
        "  this.bar = 3;" +
        "  this.baz = this.bar;" +
        "}",
        "GRR",
        DEPRECATED_PROP,
        DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAutoboxedDeprecatedProperty
  public void testAutoboxedDeprecatedProperty() {
    testDep(
        " function String() {}" +
        " String.prototype.length;" +
        "function f() { return 'x'.length; }",
        "GRR",
        DEPRECATED_PROP,
        DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testAutoboxedPrivateProperty
  public void testAutoboxedPrivateProperty() {
    test(new String[] {
        " function String() {}" +
        " String.prototype.length;",
        "function f() { return 'x'.length; }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNullableDeprecatedProperty
  public void testNullableDeprecatedProperty() {
    testDep(
        " function Foo() {}" +
        " Foo.prototype.length;" +
        " function f(x) { return x.length; }",
        "GRR",
        DEPRECATED_PROP,
        DEPRECATED_PROP_REASON);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testNullablePrivateProperty
  public void testNullablePrivateProperty() {
    test(new String[] {
        " function Foo() {}" +
        " Foo.prototype.length;",
        " function f(x) { return x.length; }"
    }, null, BAD_PRIVATE_PROPERTY_ACCESS);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty1
  public void testConstantProperty1() {
    test(" function A() {" +
        " this.bar = 3;}" +
        " function B() {" +
        " this.bar = 3;this.bar += 4;}",
        null, CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty2
  public void testConstantProperty2() {
    test(" function Foo() {}" +
        " Foo.prototype.prop = 2;" +
        "var foo = new Foo();" +
        "foo.prop = 3;",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty3
  public void testConstantProperty3() {
    testSame("var o = {  x: 1 };" +
        "o.x = 2;");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty4
  public void testConstantProperty4() {
    test(" function cat(name) {}" +
        " cat.test = 1;" +
        "cat.test *= 2;",
        null, CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty5
  public void testConstantProperty5() {
    test(" function Foo() { this.prop = 1;}" +
        " Foo.prototype.prop;" +
        "Foo.prototype.prop = 2",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty6
  public void testConstantProperty6() {
    test(" function Foo() { this.prop = 1;}" +
        " Foo.prototype.prop = 2;",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty7
  public void testConstantProperty7() {
    testSame(" function Foo() {} " +
      "Foo.prototype.bar_ = function() {};" +
      " " +
      "function SubFoo() {};" +
      "  SubFoo.prototype.bar_ = function() {};" +
      "SubFoo.prototype.baz = function() { this.bar_(); }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty8
  public void testConstantProperty8() {
    testSame("var o = {  x: 1 };" +
        "var y = o.x;");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty9
  public void testConstantProperty9() {
    testSame(" function A() {" +
        " this.bar = 3;}" +
        " function B() {" +
        "this.bar = 4;}");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty10
  public void testConstantProperty10() {
    testSame(" function Foo() { this.prop = 1;}" +
        " Foo.prototype.prop;");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty11
  public void testConstantProperty11() {
    test(" function Foo() {}" +
        " Foo.prototype.bar;" +
        " function SubFoo() { this.bar = 5; this.bar = 6; }",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty12
  public void testConstantProperty12() {
    testSame(" function Foo() {}" +
        " Foo.prototype.bar;" +
        " function SubFoo() { this.bar = 5; }" +
        " function SubFoo2() { this.bar = 5; }");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty13
  public void testConstantProperty13() {
    test(" function Foo() {}" +
        " Foo.prototype.bar;" +
        " function SubFoo() { this.bar = 5; }" +
        " function SubSubFoo() { this.bar = 5; }",
        null , CONST_PROPERTY_REASSIGNED_VALUE);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testConstantProperty14
  public void testConstantProperty14() {
    test(" function Foo() {" +
        " this.bar = 3; delete this.bar; }",
        null, CONST_PROPERTY_DELETED);
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testSuppressConstantProperty
  public void testSuppressConstantProperty() {
    testSame(" function A() {" +
        " this.bar = 3;}" +
        " function B() {" +
        " this.bar = 3;this.bar += 4;}");
  }

// com.google.javascript.jscomp.CheckAccessControlsTest::testSuppressConstantProperty2
  public void testSuppressConstantProperty2() {
    testSame(" function A() {" +
        " this.bar = 3;}" +
        " function B() {" +
        " this.bar = 3;this.bar += 4;}");
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
