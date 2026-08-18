// buggy code
  private void replaceReferencesToThis(Node node, String name) {
    if (NodeUtil.isFunction(node)) {
      return;
    }

    for (Node child : node.children()) {
      if (NodeUtil.isThis(child)) {
        Node newName = Node.newString(Token.NAME, name);
        node.replaceChild(child, newName);
      } else {
        replaceReferencesToThis(child, name);
      }
    }
  }

  public boolean hasProperty(String name) {
    return super.hasProperty(name) || "prototype".equals(name);
  }

  boolean defineProperty(String name, JSType type,
      boolean inferred, boolean inExterns) {
    if ("prototype".equals(name)) {
      ObjectType objType = type.toObjectType();
      if (objType != null) {
        return setPrototype(
            new FunctionPrototypeType(
                registry, this, objType, isNativeObjectType()));
      } else {
        return false;
      }
    }
    return super.defineProperty(name, type, inferred, inExterns);
  }

// relevant test
// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends6
  public void testParseExtends6() throws Exception {
    
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends \n * {String}*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsInvalidName
  public void testParseExtendsInvalidName() throws Exception {
    
    
    
    
    assertTypeEquals(
        new NamedType(registry, "some_++#%$%_UglyString", null, -1, -1),
        parse("@extends {some_++#%$%_UglyString} */").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsNullable1
  public void testParseExtendsNullable1() throws Exception {
    parse("@extends {Base?} */", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsNullable2
  public void testParseExtendsNullable2() throws Exception {
    parse("@extends Base? */", "expected end of line or comment");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseEnum1
  public void testParseEnum1() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@enum*/").getEnumParameterType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseEnum2
  public void testParseEnum2() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@enum {string}*/").getEnumParameterType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseEnum3
  public void testParseEnum3() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@enum string*/").getEnumParameterType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc1
  public void testParseDesc1() throws Exception {
    assertEquals("hello world!",
        parse("@desc hello world!*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc2
  public void testParseDesc2() throws Exception {
    assertEquals("hello world!",
        parse("@desc hello world!\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc3
  public void testParseDesc3() throws Exception {
    assertEquals("", parse("@desc*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc4
  public void testParseDesc4() throws Exception {
    assertEquals("", parse("@desc\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc5
  public void testParseDesc5() throws Exception {
    assertEquals("hello world!",
                 parse("@desc hello\nworld!\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc6
  public void testParseDesc6() throws Exception {
    assertEquals("hello world!",
        parse("@desc hello\n* world!\n*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc7
  public void testParseDesc7() throws Exception {
    assertEquals("a b c", parse("@desc a\n\nb\nc*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc8
  public void testParseDesc8() throws Exception {
    assertEquals("a b c d",
        parse("@desc a\n      *b\n\n  *c\n\nd*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc9
  public void testParseDesc9() throws Exception {
    String comment = "@desc\n.\n,\n{\n)\n}\n|\n.<\n>\n<\n?\n~\n+\n-\n;\n:\n*/";

    assertEquals(". , { ) } | .< > < ? ~ + - ; :",
        parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc10
  public void testParseDesc10() throws Exception {
    String comment = "@desc\n?\n?\n?\n?*/";

    assertEquals("? ? ? ?", parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc11
  public void testParseDesc11() throws Exception {
    String comment = "@desc :[]*/";

    assertEquals(":[]", parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDesc12
  public void testParseDesc12() throws Exception {
    String comment = "@desc\n:\n[\n]\n...*/";

    assertEquals(": [ ] ...", parse(comment).getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine1
  public void testParseDefine1() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@define {string}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine2
  public void testParseDefine2() throws Exception {
    assertTypeEquals(STRING_TYPE,
        parse("@define {string*/", "expected closing }").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine3
  public void testParseDefine3() throws Exception {
    JSDocInfo info = parse("@define {boolean}*/");
    assertTrue(info.isConstant());
    assertTrue(info.isDefine());
    assertTypeEquals(BOOLEAN_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine4
  public void testParseDefine4() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@define {number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefine5
  public void testParseDefine5() throws Exception {
    assertTypeEquals(createUnionType(NUMBER_TYPE, BOOLEAN_TYPE),
        parse("@define {number|boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors1
  public void testParseDefineErrors1() throws Exception {
    parse("@enum {string}\n @define {string} */", "conflicting @define tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors2
  public void testParseDefineErrors2() throws Exception {
    parse("@define {string}\n @enum {string} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors3
  public void testParseDefineErrors3() throws Exception {
    parse("@const\n @define {string} */", "conflicting @define tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors4
  public void testParseDefineErrors4() throws Exception {
    parse("@type string \n @define {string} */", "conflicting @define tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors5
  public void testParseDefineErrors5() throws Exception {
    parse("@return {string}\n @define {string} */", "conflicting @define tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors6
  public void testParseDefineErrors6() throws Exception {
    parse("@define {String}*/", "@define tag only permits literal types");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors7
  public void testParseDefineErrors7() throws Exception {
    parse("@define {string}\n @const */", "conflicting @const tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDefineErrors8
  public void testParseDefineErrors8() throws Exception {
    parse("@define {string}\n @type string */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoCheck1
  public void testParseNoCheck1() throws Exception {
    assertTrue(parse("@notypecheck*/").isNoTypeCheck());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoCheck2
  public void testParseNoCheck2() throws Exception {
    parse("@notypecheck\n@notypecheck*/", "extra @notypecheck tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseOverride1
  public void testParseOverride1() throws Exception {
    assertTrue(parse("@override*/").isOverride());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseOverride2
  public void testParseOverride2() throws Exception {
    parse("@override\n@override*/", "extra @override/@inheritDoc tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInheritDoc1
  public void testParseInheritDoc1() throws Exception {
    assertTrue(parse("@inheritDoc*/").isOverride());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInheritDoc2
  public void testParseInheritDoc2() throws Exception {
    parse("@override\n@inheritDoc*/", "extra @override/@inheritDoc tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInheritDoc3
  public void testParseInheritDoc3() throws Exception {
    parse("@inheritDoc\n@inheritDoc*/", "extra @override/@inheritDoc tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoAlias1
  public void testParseNoAlias1() throws Exception {
    assertTrue(parse("@noalias*/").isNoAlias());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoAlias2
  public void testParseNoAlias2() throws Exception {
    parse("@noalias\n@noalias*/", "extra @noalias tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDeprecated1
  public void testParseDeprecated1() throws Exception {
    assertTrue(parse("@deprecated*/").isDeprecated());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDeprecated2
  public void testParseDeprecated2() throws Exception {
    parse("@deprecated\n@deprecated*/", "extra @deprecated tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExport1
  public void testParseExport1() throws Exception {
    assertTrue(parse("@export*/").isExport());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExport2
  public void testParseExport2() throws Exception {
    parse("@export\n@export*/", "extra @export tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExterns1
  public void testParseExterns1() throws Exception {
    assertTrue(parseFileOverview("@externs*/").isExterns());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExterns2
  public void testParseExterns2() throws Exception {
    parseFileOverview("@externs\n@externs*/", "extra @externs tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExterns3
  public void testParseExterns3() throws Exception {
    assertNull(parse("@externs*/"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJavaDispatch1
  public void testParseJavaDispatch1() throws Exception {
    assertTrue(parse("@javadispatch*/").isJavaDispatch());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJavaDispatch2
  public void testParseJavaDispatch2() throws Exception {
    parse("@javadispatch\n@javadispatch*/",
        "extra @javadispatch tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseJavaDispatch3
  public void testParseJavaDispatch3() throws Exception {
    assertNull(parseFileOverview("@javadispatch*/"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBugAnnotation
  public void testBugAnnotation() throws Exception {
    parse("@bug */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDescriptionAnnotation
  public void testDescriptionAnnotation() throws Exception {
    parse("@description */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression1
  public void testRegression1() throws Exception {
    String comment =
        " * @param {number} index the index of blah\n" +
        " * @return {boolean} whatever\n" +
        " * @private\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
    assertTypeEquals(BOOLEAN_TYPE, info.getReturnType());
    assertEquals(Visibility.PRIVATE, info.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression2
  public void testRegression2() throws Exception {
    String comment =
        " * @return {boolean} whatever\n" +
        " * but important\n" +
        " *\n" +
        " * @param {number} index the index of blah\n" +
        " * some more comments here\n" +
        " * @param name the name of the guy\n" +
        " *\n" +
        " * @protected\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertEquals(2, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
    assertEquals(null, info.getParameterType("name"));
    assertTypeEquals(BOOLEAN_TYPE, info.getReturnType());
    assertEquals(Visibility.PROTECTED, info.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression3
  public void testRegression3() throws Exception {
    String comment =
        " * @param mediaTag this specified whether the @media tag is ....\n" +
        " *\n" +
        "\n" +
        "@public\n" +
        " *\n" +
        "\n" +
        " **********\n" +
        " * @final\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertEquals(1, info.getParameterCount());
    assertEquals(null, info.getParameterType("mediaTag"));
    assertEquals(Visibility.PUBLIC, info.getVisibility());
    assertTrue(info.isConstant());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression4
  public void testRegression4() throws Exception {
    String comment =
        " * @const\n" +
        " * @hidden\n" +
        " * @preserveTry\n" +
        " * @constructor\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertTrue(info.isConstant());
    assertFalse(info.isDefine());
    assertTrue(info.isConstructor());
    assertTrue(info.isHidden());
    assertTrue(info.shouldPreserveTry());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression5
  public void testRegression5() throws Exception {
    String comment = "@const\n@enum {string}\n@public*/";

    JSDocInfo info = parse(comment);
    assertTrue(info.isConstant());
    assertFalse(info.isDefine());
    assertTypeEquals(STRING_TYPE, info.getEnumParameterType());
    assertEquals(Visibility.PUBLIC, info.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression6
  public void testRegression6() throws Exception {
    String comment = "@hidden\n@enum\n@public*/";

    JSDocInfo info = parse(comment);
    assertTrue(info.isHidden());
    assertTypeEquals(NUMBER_TYPE, info.getEnumParameterType());
    assertEquals(Visibility.PUBLIC, info.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression7
  public void testRegression7() throws Exception {
    String comment =
        " * @desc description here\n" +
        " * @param {boolean} flag and some more description\n" +
        " *     nicely formatted\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(BOOLEAN_TYPE, info.getParameterType("flag"));
    assertEquals("description here", info.getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression8
  public void testRegression8() throws Exception {
    String comment =
        " * @name random tag here\n" +
        " * @desc description here\n" +
        " *\n" +
        " * @param {boolean} flag and some more description\n" +
        " *     nicely formatted\n" +
        " */";

    JSDocInfo info = parse(comment);
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(BOOLEAN_TYPE, info.getParameterType("flag"));
    assertEquals("description here", info.getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression9
  public void testRegression9() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @param {string} p0 blah blah blah\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(1, jsdoc.getParameterCount());
    assertTypeEquals(STRING_TYPE, jsdoc.getParameterType("p0"));
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression10
  public void testRegression10() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @param {!String} p0 blah blah blah\n" +
        " * @param {boolean} p1 fobar\n" +
        " * @return {!Date} jksjkash dshad\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(2, jsdoc.getParameterCount());
    assertTypeEquals(STRING_OBJECT_TYPE, jsdoc.getParameterType("p0"));
    assertTypeEquals(BOOLEAN_TYPE, jsdoc.getParameterType("p1"));
    assertTypeEquals(DATE_TYPE, jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression11
  public void testRegression11() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @constructor\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression12
  public void testRegression12() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @extends FooBar\n" +
        " */");

    assertTypeEquals(new NamedType(registry, "FooBar", null, 0, 0),
        jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression13
  public void testRegression13() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @type {!RegExp}\n" +
        " * @protected\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertTypeEquals(REGEXP_TYPE, jsdoc.getType());
    assertEquals(Visibility.PROTECTED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression14
  public void testRegression14() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @const\n" +
        " * @private\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertTrue(jsdoc.isConstant());
    assertNull(jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.PRIVATE, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression15
  public void testRegression15() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @desc Hello,\n" +
        " * World!\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertEquals("Hello, World!", jsdoc.getDescription());
    assertNull(jsdoc.getEnumParameterType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertNull(jsdoc.getType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
    assertFalse(jsdoc.isExport());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression16
  public void testRegression16() throws Exception {
    JSDocInfo jsdoc = parse(
        " Email is plp@foo.bar\n" +
        " @type {string}\n" +
        " */");

    assertNull(jsdoc.getBaseType());
    assertFalse(jsdoc.isConstant());
    assertTypeEquals(STRING_TYPE, jsdoc.getType());
    assertFalse(jsdoc.isHidden());
    assertEquals(0, jsdoc.getParameterCount());
    assertNull(jsdoc.getReturnType());
    assertEquals(Visibility.INHERITED, jsdoc.getVisibility());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testRegression17
  public void testRegression17() throws Exception {
    
    assertNull(parse("@private*/").getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression1
  public void testFullRegression1() throws Exception {
    parseFull("function bar(foo){}",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression2
  public void testFullRegression2() throws Exception {
    parseFull("function bar(foo){}",
        "expected closing }",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression3
  public void testFullRegression3() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug907488
  public void testBug907488() throws Exception {
    parse("@type {number,null} */",
        "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug907494
  public void testBug907494() throws Exception {
    parse("@return {Object,undefined} */",
        "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug909468
  public void testBug909468() throws Exception {
    parse("@extends {(x)}*/",
        "expecting a type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInterface
  public void testParseInterface() throws Exception {
    assertTrue(parse("@interface*/").isInterface());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplicitCast1
  public void testParseImplicitCast1() throws Exception {
    assertTrue(parse("@type {string} \n * @implicitCast*/").isImplicitCast());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplicitCast2
  public void testParseImplicitCast2() throws Exception {
    assertFalse(parse("@type {string}*/").isImplicitCast());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDuplicateImplicitCast
  public void testParseDuplicateImplicitCast() throws Exception {
    parse("@type {string} \n * @implicitCast \n * @implicitCast*/",
          "extra @implicitCast tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInterfaceDoubled
  public void testParseInterfaceDoubled() throws Exception {
    parse(
        "* @interface\n" +
        "* @interface\n" +
        "*/",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplements
  public void testParseImplements() throws Exception {
    List<JSTypeExpression> interfaces = parse("@implements {SomeInterface}*/")
        .getImplementedInterfaces();
    assertEquals(1, interfaces.size());
    assertTypeEquals(new NamedType(registry, "SomeInterface", null, -1, -1),
        interfaces.get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsTwo
  public void testParseImplementsTwo() throws Exception {
    List<JSTypeExpression> interfaces =
        parse(
            "* @implements {SomeInterface1}\n" +
            "* @implements {SomeInterface2}\n" +
            "*/")
        .getImplementedInterfaces();
    assertEquals(2, interfaces.size());
    assertTypeEquals(new NamedType(registry, "SomeInterface1", null, -1, -1),
        interfaces.get(0));
    assertTypeEquals(new NamedType(registry, "SomeInterface2", null, -1, -1),
        interfaces.get(1));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsSameTwice
  public void testParseImplementsSameTwice() throws Exception {
    parse(
        "* @implements {Smth}\n" +
        "* @implements {Smth}\n" +
        "*/",
        "duplicate @implements tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNoName
  public void testParseImplementsNoName() throws Exception {
    parse("* @implements {} */",
        "expecting a type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsMissingRC
  public void testParseImplementsMissingRC() throws Exception {
    parse("* @implements {Smth */",
        "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNullable1
  public void testParseImplementsNullable1() throws Exception {
    parse("@implements {Base?} */", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNullable2
  public void testParseImplementsNullable2() throws Exception {
    parse("@implements Base? */", "expected end of line or comment");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testInterfaceExtends
  public void testInterfaceExtends() throws Exception {
     JSDocInfo jsdoc = parse(
         " * @interface \n" +
         " * @extends {Extended} */");
    assertTrue(jsdoc.isInterface());
    assertTypeEquals(new NamedType(registry, "Extended", null, -1, -1),
        jsdoc.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadExtendsWithNullable
  public void testBadExtendsWithNullable() throws Exception {
    JSDocInfo jsdoc = parse("@constructor\n * @extends {Object?} */",
        "expected closing }");
    assertTrue(jsdoc.isConstructor());
    assertTypeEquals(OBJECT_TYPE, jsdoc.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadImplementsWithNullable
  public void testBadImplementsWithNullable() throws Exception {
  JSDocInfo jsdoc = parse("@implements {Disposable?}\n * @constructor */",
      "expected closing }");
    assertTrue(jsdoc.isConstructor());
    assertTypeEquals(new NamedType(registry, "Disposable", null, -1, -1),
        jsdoc.getImplementedInterfaces().get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadTypeDefInterfaceAndConstructor1
  public void testBadTypeDefInterfaceAndConstructor1() throws Exception {
    JSDocInfo jsdoc = parse("@interface\n@constructor*/",
        "cannot be both an interface and a constructor");
    assertTrue(jsdoc.isInterface());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadTypeDefInterfaceAndConstructor2
  public void testBadTypeDefInterfaceAndConstructor2() throws Exception {
    JSDocInfo jsdoc = parse("@constructor\n@interface*/",
        "cannot be both an interface and a constructor");
    assertTrue(jsdoc.isConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationThrows
  public void testDocumentationThrows() throws Exception {
    JSDocInfo jsdoc
        = parse("@throws {number} This is a description.*/", true);

    assertEquals("This is a description.",
                 jsdoc.getDescriptionForThrownType(NUMBER_TYPE, null));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationParameter
  public void testDocumentationParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description.*/", true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description.",
                 jsdoc.getDescriptionForParameter("number42"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testMultilineDocumentationParameter
  public void testMultilineDocumentationParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description"
                + "\n* on multiple \n* lines.*/", true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description on multiple lines.",
                 jsdoc.getDescriptionForParameter("number42"));

  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationMultipleParameter
  public void testDocumentationMultipleParameter() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description."
                + "\n* @param {Integer} number87 This is another description.*/"
                , true);

    assertTrue(jsdoc.hasDescriptionForParameter("number42"));
    assertEquals("This is a description.",
                 jsdoc.getDescriptionForParameter("number42"));

    assertTrue(jsdoc.hasDescriptionForParameter("number87"));
    assertEquals("This is another description.",
                 jsdoc.getDescriptionForParameter("number87"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testDocumentationMultipleParameter2
  public void testDocumentationMultipleParameter2() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {number} delta = 0 results in a redraw\n" +
                "  != 0 ..... */", true);
    assertTrue(jsdoc.hasDescriptionForParameter("delta"));
    assertEquals("= 0 results in a redraw != 0 .....",
                 jsdoc.getDescriptionForParameter("delta"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testAuthors
  public void testAuthors() throws Exception {
    JSDocInfo jsdoc
        = parse("@param {Number} number42 This is a description."
                + "\n* @param {Integer} number87 This is another description."
                + "\n* @author a@google.com (A Person)"
                + "\n* @author b@google.com (B Person)"
                + "\n* @author c@google.com (C Person)*/"
                , true);

    Collection<String> authors = jsdoc.getAuthors();

    assertTrue(authors != null);
    assertTrue(authors.size() == 3);

    assertContains(authors, "a@google.com (A Person)");
    assertContains(authors, "b@google.com (B Person)");
    assertContains(authors, "c@google.com (C Person)");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSuppress1
  public void testSuppress1() throws Exception {
    JSDocInfo info = parse("@suppress {x} */");
    assertEquals(Sets.newHashSet("x"), info.getSuppressions());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSuppress2
  public void testSuppress2() throws Exception {
    JSDocInfo info = parse("@suppress {x|y|x|z} */");
    assertEquals(Sets.newHashSet("x", "y", "z"), info.getSuppressions());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress1
  public void testBadSuppress1() throws Exception {
    parse("@suppress {} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress2
  public void testBadSuppress2() throws Exception {
    parse("@suppress {x|} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress3
  public void testBadSuppress3() throws Exception {
    parse("@suppress {|x} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress4
  public void testBadSuppress4() throws Exception {
    parse("@suppress {x|y */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress5
  public void testBadSuppress5() throws Exception {
    parse("@suppress {x,y} */", "malformed @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress6
  public void testBadSuppress6() throws Exception {
    parse("@suppress {x} \n * @suppress {y} */", "duplicate @suppress tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewSingleLine
  public void testFileOverviewSingleLine() throws Exception {
    JSDocInfo jsdoc = parseFileOverview("@fileoverview Hi mom! */");
    assertEquals("Hi mom!", jsdoc.getFileOverview());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewMultiLine
  public void testFileOverviewMultiLine() throws Exception {
    JSDocInfo jsdoc = parseFileOverview("@fileoverview Pie is \n * good! */");
    assertEquals("Pie is\n good!", jsdoc.getFileOverview());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFileOverviewDuplicate
  public void testFileOverviewDuplicate() throws Exception {
    JSDocInfo jsdoc = parseFileOverview(
        "@fileoverview Pie \n * @fileoverview Cake */",
        "extra @fileoverview tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testReferences
  public void testReferences() throws Exception {
    JSDocInfo jsdoc
        = parse("@see A cool place!"
                + "\n* @see The world."
                + "\n* @see SomeClass#SomeMember"
                + "\n* @see A boring test case*/"
                , true);

    Collection<String> references = jsdoc.getReferences();

    assertTrue(references != null);
    assertTrue(references.size() == 4);

    assertContains(references, "A cool place!");
    assertContains(references, "The world.");
    assertContains(references, "SomeClass#SomeMember");
    assertContains(references, "A boring test case");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSingleTags
  public void testSingleTags() throws Exception {
    JSDocInfo jsdoc
        = parse("@version Some old version"
                + "\n* @deprecated In favor of the new one!"
                + "\n* @return {SomeType} The most important object :-)*/"
                , true);

    assertTrue(jsdoc.isDeprecated());
    assertEquals("In favor of the new one!", jsdoc.getDeprecationReason());
    assertEquals("Some old version", jsdoc.getVersion());
    assertEquals("The most important object :-)", jsdoc.getReturnDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSingleTagsReordered
  public void testSingleTagsReordered() throws Exception {
    JSDocInfo jsdoc
        = parse("@deprecated In favor of the new one!"
                + "\n * @return {SomeType} The most important object :-)"
                + "\n * @version Some old version*/"
                , true);

    assertTrue(jsdoc.isDeprecated());
    assertEquals("In favor of the new one!", jsdoc.getDeprecationReason());
    assertEquals("Some old version", jsdoc.getVersion());
    assertEquals("The most important object :-)", jsdoc.getReturnDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testVersionDuplication
  public void testVersionDuplication() throws Exception {
    parse("* @version Some old version"
          + "\n* @version Another version*/", true,
          "conflicting @version tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testVersionMissing
  public void testVersionMissing() throws Exception {
    parse("* @version */", true,
          "@version tag missing version information");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testAuthorMissing
  public void testAuthorMissing() throws Exception {
    parse("* @author */", true,
          "@author tag missing author");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSeeMissing
  public void testSeeMissing() throws Exception {
    parse("* @see */", true,
          "@see tag missing description");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testSourceName
  public void testSourceName() throws Exception {
    JSDocInfo jsdoc = parse("@deprecated */", true);
    assertEquals("testcode", jsdoc.getSourceName());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment
  public void testParseBlockComment() throws Exception {
    JSDocInfo jsdoc = parse("this is a nice comment\n "
                            + "* that is multiline \n"
                            + "* @author abc@google.com */", true);

    assertEquals("this is a nice comment\nthat is multiline",
                 jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment2
  public void testParseBlockComment2() throws Exception {
    JSDocInfo jsdoc = parse("this is a nice comment\n "
                            + "* that is *** multiline \n"
                            + "* @author abc@google.com */", true);

    assertEquals("this is a nice comment\nthat is *** multiline",
                 jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBlockComment3
  public void testParseBlockComment3() throws Exception {
    JSDocInfo jsdoc = parse("\n "
                            + "* hello world \n"
                            + "* @author abc@google.com */", true);

    assertEquals("hello world", jsdoc.getBlockDescription());

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 2, 2),
        "abc@google.com", 9, 2, 23);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers1
  public void testParseWithMarkers1() throws Exception {
    JSDocInfo jsdoc = parse("@author abc@google.com */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "author", 0, 0),
        "abc@google.com", 7, 0, 21);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers2
  public void testParseWithMarkers2() throws Exception {
    JSDocInfo jsdoc = parse("@param {Foo} somename abc@google.com */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "param", 0, 0),
        "abc@google.com", 21, 0, 37);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers3
  public void testParseWithMarkers3() throws Exception {
    JSDocInfo jsdoc =
        parse("@return {Foo} some long \n * multiline" +
              " \n * description */", true);

    assertDocumentationInMarker(
        assertAnnotationMarker(jsdoc, "return", 0, 0),
        "some long multiline description", 13, 2, 15);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkers4
  public void testParseWithMarkers4() throws Exception {
    JSDocInfo jsdoc =
        parse("@author foobar \n * @param {Foo} somename abc@google.com */",
              true);

    assertAnnotationMarker(jsdoc, "author", 0, 0);
    assertAnnotationMarker(jsdoc, "param", 1, 3);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerNames1
  public void testParseWithMarkerNames1() throws Exception {
    JSDocInfo jsdoc = parse("@param {SomeType} name somedescription */", true);

    assertNameInMarker(
        assertAnnotationMarker(jsdoc, "param", 0, 0),
        "name", 18);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerNames2
  public void testParseWithMarkerNames2() throws Exception {
    JSDocInfo jsdoc = parse("@param {SomeType} name somedescription \n" +
                            "* @param {AnotherType} anothername des */", true);

    assertTypeInMarker(
        assertNameInMarker(
            assertAnnotationMarker(jsdoc, "param", 0, 0, 0),
            "name", 18),
        "SomeType", 7, true);

    assertTypeInMarker(
        assertNameInMarker(
            assertAnnotationMarker(jsdoc, "param", 1, 2, 1),
            "anothername", 23),
        "AnotherType", 9, true);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithoutMarkerName
  public void testParseWithoutMarkerName() throws Exception {
    JSDocInfo jsdoc = parse("@author helloworld*/", true);
    assertNull(assertAnnotationMarker(jsdoc, "author", 0, 0).name);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerType
  public void testParseWithMarkerType() throws Exception {
    JSDocInfo jsdoc = parse("@extends {FooBar}*/", true);

    assertTypeInMarker(
        assertAnnotationMarker(jsdoc, "extends", 0, 0),
        "FooBar", 9, true);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseWithMarkerType2
  public void testParseWithMarkerType2() throws Exception {
    JSDocInfo jsdoc = parse("@extends FooBar*/", true);

    assertTypeInMarker(
        assertAnnotationMarker(jsdoc, "extends", 0, 0),
        "FooBar", 9, false);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict1
  public void testTypeTagConflict1() throws Exception {
    parse("@constructor \n * @constructor */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict2
  public void testTypeTagConflict2() throws Exception {
    parse("@interface \n * @interface */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict3
  public void testTypeTagConflict3() throws Exception {
    parse("@constructor \n * @interface */",
        "cannot be both an interface and a constructor");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict4
  public void testTypeTagConflict4() throws Exception {
    parse("@interface \n * @constructor */",
        "cannot be both an interface and a constructor");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict5
  public void testTypeTagConflict5() throws Exception {
    parse("@interface \n * @type {string} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict6
  public void testTypeTagConflict6() throws Exception {
    parse("@typedef {string} \n * @type {string} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict7
  public void testTypeTagConflict7() throws Exception {
    parse("@typedef {string} \n * @constructor */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict8
  public void testTypeTagConflict8() throws Exception {
    parse("@typedef {string} \n * @return {boolean} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict9
  public void testTypeTagConflict9() throws Exception {
    parse("@enum {string} \n * @return {boolean} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict10
  public void testTypeTagConflict10() throws Exception {
    parse("@this {Object} \n * @enum {boolean} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict11
  public void testTypeTagConflict11() throws Exception {
    parse("@param {Object} x \n * @type {boolean} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict12
  public void testTypeTagConflict12() throws Exception {
    parse("@typedef {boolean} \n * @param {Object} x */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypeTagConflict13
  public void testTypeTagConflict13() throws Exception {
    parse("@typedef {boolean} \n * @extends {Object} */",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParserWithTemplateTypeNameMisisng
  public void testParserWithTemplateTypeNameMisisng() {
    parse("@template */", "@template tag missing type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParserWithTemplateTypeNameMissing
  public void testParserWithTemplateTypeNameMissing() {
    parse("@template T\n@template V */", "@template tag at most once");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedNewAnnotations
  public void testWhitelistedNewAnnotations() {
    parse("@foobar */",
        "illegal use of unknown JSDoc tag \"foobar\"; ignoring it");
    extraAnnotations.add("foobar");
    parse("@foobar */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedConflictingAnnotation
  public void testWhitelistedConflictingAnnotation() {
    extraAnnotations.add("param");
    JSDocInfo info = parse("@param {number} index */");
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testNonIdentifierAnnotation
  public void testNonIdentifierAnnotation() {
    
    
    extraAnnotations.add("123");
    parse("@123 */", "illegal use of unknown JSDoc tag \"\"; ignoring it");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testUnsupportedJsDocSyntax1
  public void testUnsupportedJsDocSyntax1() {
    JSDocInfo info =
        parse("@param {string} [accessLevel=\"author\"] The user level */",
            true);
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(STRING_TYPE),
        info.getParameterType("accessLevel"));
    assertEquals("The user level",
        info.getDescriptionForParameter("accessLevel"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testUnsupportedJsDocSyntax2
  public void testUnsupportedJsDocSyntax2() {
    JSDocInfo info =
        parse("@param userInfo The user info. \n" +
              " * @param userInfo.name The name of the user */", true);
    assertEquals(1, info.getParameterCount());
    assertEquals("The user info.",
        info.getDescriptionForParameter("userInfo"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testWhitelistedAnnotations
  public void testWhitelistedAnnotations() {
    parse(
      "* @addon \n" +
      "* @augments \n" +
      "* @base \n" +
      "* @borrows \n" +
      "* @bug \n" +
      "* @class \n" +
      "* @config \n" +
      "* @constructs \n" +
      "* @default \n" +
      "* @description \n" +
      "* @event \n" +
      "* @example \n" +
      "* @exception \n" +
      "* @exec \n" +
      "* @externs \n" +
      "* @field \n" +
      "* @function \n" +
      "* @id \n" +
      "* @ignore \n" +
      "* @inner \n" +
      "* @lends \n" +
      "* @link \n" +
      "* @member \n" +
      "* @memberOf \n" +
      "* @modName \n" +
      "* @mods \n" +
      "* @name \n" +
      "* @namespace \n" +
      "* @property \n" +
      "* @requires \n" +
      "* @since \n" +
      "* @static \n" +
      "* @supported */");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAssign1
  public void testLinenoCharnoAssign1() throws Exception {
    Node assign = parse("a = b").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(1, assign.getLineno());
    assertEquals(2, assign.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAssign2
  public void testLinenoCharnoAssign2() throws Exception {
    Node assign = parse("\n a.g.h.k    =  45").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(2, assign.getLineno());
    assertEquals(12, assign.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoCall
  public void testLinenoCharnoCall() throws Exception {
    Node call = parse("\n foo(123);").getFirstChild().getFirstChild();

    assertEquals(Token.CALL, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(4, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetProp1
  public void testLinenoCharnoGetProp1() throws Exception {
    Node getprop = parse("\n foo.bar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(2, getprop.getLineno());
    assertEquals(1, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(2, name.getLineno());
    assertEquals(5, name.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetProp2
  public void testLinenoCharnoGetProp2() throws Exception {
    Node getprop = parse("\n foo.\nbar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(2, getprop.getLineno());
    assertEquals(1, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(3, name.getLineno());
    assertEquals(0, name.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem1
  public void testLinenoCharnoGetelem1() throws Exception {
    Node call = parse("\n foo[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem2
  public void testLinenoCharnoGetelem2() throws Exception {
    Node call = parse("\n   \n foo()[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(3, call.getLineno());
    assertEquals(1, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGetelem3
  public void testLinenoCharnoGetelem3() throws Exception {
    Node call = parse("\n   \n (8 + kl)[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(3, call.getLineno());
    assertEquals(2, call.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoForComparison
  public void testLinenoCharnoForComparison() throws Exception {
    Node lt =
      parse("for (; i < j;){}").getFirstChild().getFirstChild().getNext();

    assertEquals(Token.LT, lt.getType());
    assertEquals(1, lt.getLineno());
    assertEquals(9, lt.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoHook
  public void testLinenoCharnoHook() throws Exception {
    Node n = parse("\n a ? 9 : 0").getFirstChild().getFirstChild();

    assertEquals(Token.HOOK, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(1, n.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoArrayLiteral
  public void testLinenoCharnoArrayLiteral() throws Exception {
    Node n = parse("\n  [8, 9]").getFirstChild().getFirstChild();

    assertEquals(Token.ARRAYLIT, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(2, n.getCharno());

    n = n.getFirstChild();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(3, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(6, n.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoObjectLiteral
  public void testLinenoCharnoObjectLiteral() throws Exception {
    Node n = parse("\n\n var a = {a:0\n,b :1};")
        .getFirstChild().getFirstChild().getFirstChild();

    assertEquals(Token.OBJECTLIT, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(9, n.getCharno());

    n = n.getFirstChild();

    assertEquals(Token.STRING, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(10, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(12, n.getCharno());

    n = n.getNext();

    assertEquals(Token.STRING, n.getType());
    assertEquals(4, n.getLineno());
    assertEquals(1, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(4, n.getLineno());
    assertEquals(4, n.getCharno());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAdd
  public void testLinenoCharnoAdd() throws Exception {
    testLinenoCharnoBinop("+");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoSub
  public void testLinenoCharnoSub() throws Exception {
    testLinenoCharnoBinop("-");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoMul
  public void testLinenoCharnoMul() throws Exception {
    testLinenoCharnoBinop("*");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoDiv
  public void testLinenoCharnoDiv() throws Exception {
    testLinenoCharnoBinop("/");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoMod
  public void testLinenoCharnoMod() throws Exception {
    testLinenoCharnoBinop("%");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoShift
  public void testLinenoCharnoShift() throws Exception {
    testLinenoCharnoBinop("<<");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoBinaryAnd
  public void testLinenoCharnoBinaryAnd() throws Exception {
    testLinenoCharnoBinop("&");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoAnd
  public void testLinenoCharnoAnd() throws Exception {
    testLinenoCharnoBinop("&&");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoBinaryOr
  public void testLinenoCharnoBinaryOr() throws Exception {
    testLinenoCharnoBinop("|");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoOr
  public void testLinenoCharnoOr() throws Exception {
    testLinenoCharnoBinop("||");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoLt
  public void testLinenoCharnoLt() throws Exception {
    testLinenoCharnoBinop("<");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoLe
  public void testLinenoCharnoLe() throws Exception {
    testLinenoCharnoBinop("<=");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGt
  public void testLinenoCharnoGt() throws Exception {
    testLinenoCharnoBinop(">");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLinenoCharnoGe
  public void testLinenoCharnoGe() throws Exception {
    testLinenoCharnoBinop(">=");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment1
  public void testJSDocAttachment1() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment2
  public void testJSDocAttachment2() {
    Node varNode = parse("var a,b;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node nameNode1 = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode1.getType());
    assertNull(nameNode1.getJSDocInfo());

    
    Node nameNode2 = nameNode1.getNext();
    assertEquals(Token.NAME, nameNode2.getType());
    assertNull(nameNode2.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment3
  public void testJSDocAttachment3() {
    Node assignNode = parse(
        "goog.FOO = 5;").getFirstChild().getFirstChild();

    
    assertEquals(Token.ASSIGN, assignNode.getType());
    JSDocInfo info = assignNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment4
  public void testJSDocAttachment4() {
    Node varNode = parse(
        "var a, b = 5;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    assertNull(varNode.getJSDocInfo());

    
    Node a = varNode.getFirstChild();
    assertNull(a.getJSDocInfo());

    
    Node b = a.getNext();
    JSDocInfo info = b.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment5
  public void testJSDocAttachment5() {
    Node varNode = parse(
        "var a, b = 5;")
        .getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    assertNull(varNode.getJSDocInfo());

    
    Node a = varNode.getFirstChild();
    assertNotNull(a.getJSDocInfo());
    JSDocInfo info = a.getJSDocInfo();
    assertNotNull(info);
    assertFalse(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());

    
    Node b = a.getNext();
    info = b.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment6
  public void testJSDocAttachment6() throws Exception {
    Node functionNode = parse(
        "var a = 5;" +
        "function f(index){}")
        .getFirstChild().getNext();

    assertEquals(Token.FUNCTION, functionNode.getType());
    JSDocInfo info = functionNode.getJSDocInfo();
    assertNotNull(info);
    assertFalse(info.hasParameter("index"));
    assertTrue(info.hasReturnType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment7
  public void testJSDocAttachment7() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment8
  public void testJSDocAttachment8() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment9
  public void testJSDocAttachment9() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment10
  public void testJSDocAttachment10() {
    Node varNode = parse("var a;").getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment11
  public void testJSDocAttachment11() {
    Node varNode =
       parse("var a;")
        .getFirstChild();

    
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);

    assertTypeEquals(createRecordTypeBuilder().
                     addProperty("x", NUMBER_TYPE).
                     addProperty("y", STRING_TYPE).
                     addProperty("z", UNKNOWN_TYPE).
                     build(),
                     info.getType());

    
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testJSDocAttachment12
  public void testJSDocAttachment12() {
    Node varNode =
       parse("var a = { b: c};")
        .getFirstChild();
    Node objectLitNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLitNode.getType());
    assertNotNull(objectLitNode.getFirstChild().getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing1
  public void testIncorrectJSDocDoesNotAlterJSParsing1() throws Exception {
    assertNodeEquality(
        parse("var a = [1,2]"),
        parse("var a = [1,2]",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing2
  public void testIncorrectJSDocDoesNotAlterJSParsing2() throws Exception {
    assertNodeEquality(
        parse("var a = [1,2]"),
        parse("var a = [1,2]",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing3
  public void testIncorrectJSDocDoesNotAlterJSParsing3() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
            MISSING_GT_MESSAGE));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing4
  public void testIncorrectJSDocDoesNotAlterJSParsing4() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing5
  public void testIncorrectJSDocDoesNotAlterJSParsing5() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing6
  public void testIncorrectJSDocDoesNotAlterJSParsing6() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "expected closing }",
              "expecting a variable name in a @param tag"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing7
  public void testIncorrectJSDocDoesNotAlterJSParsing7() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "@see tag missing description"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing8
  public void testIncorrectJSDocDoesNotAlterJSParsing8() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "@author tag missing author"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testIncorrectJSDocDoesNotAlterJSParsing9
  public void testIncorrectJSDocDoesNotAlterJSParsing9() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("" +
              "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "illegal use of unknown JSDoc tag \"someillegaltag\";"
              + " ignoring it"));
  }

// com.google.javascript.jscomp.parsing.ParserTest::testUnescapedSlashInRegexpCharClass
  public void testUnescapedSlashInRegexpCharClass() throws Exception {
    
    parse("var foo = /[/]/;");
    parse("var foo = /[hi there/]/;");
    parse("var foo = /[/yo dude]/;");
    parse("var foo = /\\/[@#$/watashi/wa/suteevu/desu]/;");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testParse
  public void testParse() {
    Node a = Node.newString(Token.NAME, "a");
    a.addChildToFront(Node.newString(Token.NAME, "b"));
    List<ParserResult> testCases = ImmutableList.of(
        new ParserResult(
            "3;",
            createScript(new Node(Token.EXPR_RESULT, Node.newNumber(3.0)))),
        new ParserResult(
            "var a = b;",
             createScript(new Node(Token.VAR, a))),
        new ParserResult(
            "\"hell\\\no\\ world\\\n\\\n!\"",
             createScript(new Node(Token.EXPR_RESULT,
             Node.newString(Token.STRING, "hello world!")))));

    for (ParserResult testCase : testCases) {
      assertNodeEquality(testCase.node, parse(testCase.code));
    }
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning1
  public void testTrailingCommaWarning1() {
    parse("var a = ['foo', 'bar'];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning2
  public void testTrailingCommaWarning2() {
    parse("var a = ['foo',,'bar'];");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning3
  public void testTrailingCommaWarning3() {
    parse("var a = ['foo', 'bar',];", TRAILING_COMMA_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning4
  public void testTrailingCommaWarning4() {
    parse("var a = [,];", TRAILING_COMMA_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning5
  public void testTrailingCommaWarning5() {
    parse("var a = {'foo': 'bar'};");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning6
  public void testTrailingCommaWarning6() {
    parse("var a = {'foo': 'bar',};", TRAILING_COMMA_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testTrailingCommaWarning7
  public void testTrailingCommaWarning7() {
    parseError("var a = {,};", BAD_PROPERTY_MESSAGE);
  }

// com.google.javascript.jscomp.parsing.ParserTest::testConstForbidden
  public void testConstForbidden() {
    parseError("const x = 3;", "Unsupported syntax: CONST");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden
  public void testDestructuringAssignForbidden() {
    parseError("var [x, y] = foo();", "destructuring assignment forbidden");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden2
  public void testDestructuringAssignForbidden2() {
    parseError("var {x, y} = foo();", "missing : after property id");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden3
  public void testDestructuringAssignForbidden3() {
    parseError("var {x: x, y: y} = foo();",
        "destructuring assignment forbidden");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDestructuringAssignForbidden4
  public void testDestructuringAssignForbidden4() {
    parseError("[x, y] = foo();", "destructuring assignment forbidden");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLetForbidden
  public void testLetForbidden() {
    parseError("function f() { let (x = 3) { alert(x); }; }",
        "missing ; before statement", "syntax error");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testYieldForbidden
  public void testYieldForbidden() {
    parseError("function f() { yield 3; }", "missing ; before statement");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testBracelessFunctionForbidden
  public void testBracelessFunctionForbidden() {
    parseError("var sq = function(x) x * x;",
        "missing { before function body");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testGeneratorsForbidden
  public void testGeneratorsForbidden() {
    parseError("var i = (x for (x in obj));",
        "missing ) in parenthetical");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDoc1
  public void testFileOverviewJSDoc1() {
    Node n = parse(" function Foo() {}");
    assertEquals(Token.FUNCTION, n.getFirstChild().getType());
    assertTrue(n.getJSDocInfo() != null);
    assertNull(n.getFirstChild().getJSDocInfo());
    assertEquals("Hi mom!",
        n.getJSDocInfo().getFileOverview());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDocDoesNotHoseParsing
  public void testFileOverviewJSDocDoesNotHoseParsing() {
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse(" function Foo() {}")
            .getFirstChild().getType());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testFileOverviewJSDoc2
  public void testFileOverviewJSDoc2() {
    Node n = parse(" " +
        " function Foo() {}");
    assertTrue(n.getJSDocInfo() != null);
    assertEquals("Hi mom!", n.getJSDocInfo().getFileOverview());
    assertTrue(n.getFirstChild().getJSDocInfo() != null);
    assertFalse(n.getFirstChild().getJSDocInfo().hasFileOverview());
    assertTrue(n.getFirstChild().getJSDocInfo().isConstructor());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testObjectLiteralDoc1
  public void testObjectLiteralDoc1() {
    Node n = parse("var x = { 1: 2};");

    Node objectLit = n.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLit.getType());

    Node number = objectLit.getFirstChild();
    assertEquals(Token.NUMBER, number.getType());
    assertNotNull(number.getJSDocInfo());
  }

// com.google.javascript.jscomp.parsing.ParserTest::testDuplicatedParam
  public void testDuplicatedParam() {
    parse("function foo(x, x) {}", "Duplicate parameter name \"x\".");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testLamestWarningEver
  public void testLamestWarningEver() {
    
    parse("var x =  (y);");
    parse("var x =  (y);");
  }

// com.google.javascript.jscomp.parsing.ParserTest::testParseBlockDescription
  public void testParseBlockDescription() {
    Node n = parse(" var x;");
    Node var = n.getFirstChild();
    assertNotNull(var.getJSDocInfo());
    assertEquals("This is a variable.",
        var.getJSDocInfo().getBlockDescription());
  }

// com.google.javascript.rhino.JSDocInfoTest::testVisibilityOrdinal
  public void testVisibilityOrdinal() {
    assertEquals(0, PRIVATE.ordinal());
    assertEquals(1, PROTECTED.ordinal());
    assertEquals(2, PUBLIC.ordinal());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetType
  public void testSetType() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertEquals(getNativeType(STRING_TYPE), resolve(info.getType()));
    assertNull(info.getVisibility());
    assertTrue(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetTypeAndVisibility
  public void testSetTypeAndVisibility() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("string"));
    info.setVisibility(PROTECTED);

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertEquals(getNativeType(STRING_TYPE), resolve(info.getType()));
    assertEquals(PROTECTED, info.getVisibility());
    assertTrue(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetReturnType
  public void testSetReturnType() {
    JSDocInfo info = new JSDocInfo();
    info.setReturnType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertEquals(getNativeType(STRING_TYPE), resolve(info.getReturnType()));
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetReturnTypeAndBaseType
  public void testSetReturnTypeAndBaseType() {
    JSDocInfo info = new JSDocInfo();
    info.setBaseType(
        new JSTypeExpression(
            new Node(Token.BANG, Node.newString("Number")), "", registry));
    info.setReturnType(fromString("string"));

    assertEquals(getNativeType(NUMBER_OBJECT_TYPE),
        resolve(info.getBaseType()));
    assertNull(info.getDescription());
    assertNull(info.getEnumParameterType());
    assertEquals(0, info.getParameterCount());
    assertEquals(getNativeType(STRING_TYPE), resolve(info.getReturnType()));
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetEnumParameterType
  public void testSetEnumParameterType() {
    JSDocInfo info = new JSDocInfo();
    info.setEnumParameterType(fromString("string"));

    assertNull(info.getBaseType());
    assertNull(info.getDescription());
    assertEquals(getNativeType(STRING_TYPE),
        resolve(info.getEnumParameterType()));
    assertEquals(0, info.getParameterCount());
    assertNull(info.getReturnType());
    assertNull(info.getType());
    assertNull(info.getVisibility());
    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType
  public void testMultipleSetType() {
    JSDocInfo info = new JSDocInfo();
    info.setType(fromString("number"));

    try {
      info.setReturnType(fromString("boolean"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setEnumParameterType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertEquals(getNativeType(NUMBER_TYPE), resolve(info.getType()));
    assertNull(info.getReturnType());
    assertNull(info.getEnumParameterType());
    assertNull(info.getTypedefType());
    assertTrue(info.hasType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType2
  public void testMultipleSetType2() {
    JSDocInfo info = new JSDocInfo();

    info.setReturnType(fromString("boolean"));

    try {
      info.setType(fromString("number"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setEnumParameterType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertEquals(getNativeType(BOOLEAN_TYPE),
        resolve(info.getReturnType()));
    assertNull(info.getEnumParameterType());
    assertNull(info.getType());
    assertNull(info.getTypedefType());
    assertFalse(info.hasType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetType3
  public void testMultipleSetType3() {
    JSDocInfo info = new JSDocInfo();
    info.setEnumParameterType(fromString("boolean"));

    try {
      info.setType(fromString("number"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setReturnType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    try {
      info.setTypedefType(fromString("string"));
      fail("Expected exception");
    } catch (IllegalStateException e) {}

    assertNull(info.getType());
    assertNull(info.getTypedefType());
    assertNull(info.getReturnType());
    assertEquals(getNativeType(BOOLEAN_TYPE),
        resolve(info.getEnumParameterType()));
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetTypedefType
  public void testSetTypedefType() {
    JSDocInfo info = new JSDocInfo();
    info.setTypedefType(fromString("boolean"));

    assertEquals(getNativeType(BOOLEAN_TYPE),
        resolve(info.getTypedefType()));
    assertTrue(info.hasTypedefType());
    assertFalse(info.hasType());
    assertFalse(info.hasEnumParameterType());
    assertFalse(info.hasReturnType());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetConstant
  public void testSetConstant() {
    JSDocInfo info = new JSDocInfo();
    info.setConstant(true);

    assertFalse(info.hasType());
    assertTrue(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetConstructor
  public void testSetConstructor() {
    JSDocInfo info = new JSDocInfo();
    info.setConstructor(true);

    assertFalse(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetDefine
  public void testSetDefine() {
    JSDocInfo info = new JSDocInfo();
    info.setDefine(true);

    assertTrue(info.isConstant());
    assertFalse(info.isConstructor());
    assertTrue(info.isDefine());
    assertFalse(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetHidden
  public void testSetHidden() {
    JSDocInfo info = new JSDocInfo();
    info.setHidden(true);

    assertFalse(info.hasType());
    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertTrue(info.isHidden());
    assertFalse(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetShouldPreserveTry
  public void testSetShouldPreserveTry() {
    JSDocInfo info = new JSDocInfo();
    info.setShouldPreserveTry(true);

    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetNoTypeCheck
  public void testSetNoTypeCheck() {
    JSDocInfo info = new JSDocInfo();
    info.setNoCheck(true);

    assertFalse(info.isDeprecated());
    assertFalse(info.isNoAlias());
    assertFalse(info.isOverride());
    assertTrue(info.isNoTypeCheck());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetOverride
  public void testSetOverride() {
    JSDocInfo info = new JSDocInfo();
    info.setOverride(true);

    assertFalse(info.isDeprecated());
    assertFalse(info.isNoAlias());
    assertTrue(info.isOverride());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetExport
  public void testSetExport() {
    JSDocInfo info = new JSDocInfo();
    info.setExport(true);

    assertTrue(info.isExport());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetNoAlias
  public void testSetNoAlias() {
    JSDocInfo info = new JSDocInfo();
    info.setNoAlias(true);

    assertFalse(info.isDeprecated());
    assertFalse(info.isOverride());
    assertTrue(info.isNoAlias());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetDeprecated
  public void testSetDeprecated() {
    JSDocInfo info = new JSDocInfo();
    info.setDeprecated(true);

    assertFalse(info.isNoAlias());
    assertFalse(info.isOverride());
    assertTrue(info.isDeprecated());
  }

// com.google.javascript.rhino.JSDocInfoTest::testMultipleSetFlags1
  public void testMultipleSetFlags1() {
    JSDocInfo info = new JSDocInfo();
    info.setConstant(true);
    info.setConstructor(true);
    info.setHidden(true);
    info.setShouldPreserveTry(true);

    assertFalse(info.hasType());
    assertTrue(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertTrue(info.isHidden());
    assertTrue(info.shouldPreserveTry());

    info.setHidden(false);

    assertTrue(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());

    info.setConstant(false);
    info.setConstructor(false);

    assertFalse(info.isConstant());
    assertFalse(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());

    info.setConstructor(true);

    assertFalse(info.isConstant());
    assertTrue(info.isConstructor());
    assertFalse(info.isDefine());
    assertFalse(info.isHidden());
    assertTrue(info.shouldPreserveTry());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetFileOverviewWithDocumentationOff
  public void testSetFileOverviewWithDocumentationOff() {
    JSDocInfo info = new JSDocInfo();
    info.documentFileOverview("hi bob");
    assertNull(info.getFileOverview());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetFileOverviewWithDocumentationOn
  public void testSetFileOverviewWithDocumentationOn() {
    JSDocInfo info = new JSDocInfo(true);
    info.documentFileOverview("hi bob");
    assertEquals("hi bob", info.getFileOverview());
  }

// com.google.javascript.rhino.JSDocInfoTest::testSetSuppressions
  public void testSetSuppressions() {
    JSDocInfo info = new JSDocInfo(true);
    info.setSuppressions(Sets.newHashSet("sam", "bob"));
    assertEquals(Sets.newHashSet("bob", "sam"), info.getSuppressions());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoAssign1
  public void testLinenoCharnoAssign1() throws Exception {
    Node assign = parse("a = b").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(0, assign.getLineno());
    assertEquals(2, assign.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoAssign2
  public void testLinenoCharnoAssign2() throws Exception {
    Node assign = parse("\n a.g.h.k    =  45").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(1, assign.getLineno());
    assertEquals(12, assign.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoCall
  public void testLinenoCharnoCall() throws Exception {
    Node call = parse("\n foo(123);").getFirstChild().getFirstChild();

    assertEquals(Token.CALL, call.getType());
    assertEquals(1, call.getLineno());
    assertEquals(4, call.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGetProp1
  public void testLinenoCharnoGetProp1() throws Exception {
    Node getprop = parse("\n foo.bar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(1, getprop.getLineno());
    assertEquals(4, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(1, name.getLineno());
    assertEquals(5, name.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGetProp2
  public void testLinenoCharnoGetProp2() throws Exception {
    Node getprop = parse("\n foo.\nbar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(1, getprop.getLineno());
    assertEquals(4, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(2, name.getLineno());
    assertEquals(0, name.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGetelem1
  public void testLinenoCharnoGetelem1() throws Exception {
    Node call = parse("\n foo[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(1, call.getLineno());
    assertEquals(4, call.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGetelem2
  public void testLinenoCharnoGetelem2() throws Exception {
    Node call = parse("\n   \n foo()[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(6, call.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGetelem3
  public void testLinenoCharnoGetelem3() throws Exception {
    Node call = parse("\n   \n (8 + kl)[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(9, call.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoForComparison
  public void testLinenoCharnoForComparison() throws Exception {
    Node lt =
      parse("for (; i < j;){}").getFirstChild().getFirstChild().getNext();

    assertEquals(Token.LT, lt.getType());
    assertEquals(0, lt.getLineno());
    assertEquals(9, lt.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoHook
  public void testLinenoCharnoHook() throws Exception {
    Node n = parse("\n a ? 9 : 0").getFirstChild().getFirstChild();

    assertEquals(Token.HOOK, n.getType());
    assertEquals(1, n.getLineno());
    assertEquals(3, n.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoArrayLiteral
  public void testLinenoCharnoArrayLiteral() throws Exception {
    Node n = parse("\n  [8, 9]").getFirstChild().getFirstChild();

    assertEquals(Token.ARRAYLIT, n.getType());
    assertEquals(1, n.getLineno());
    assertEquals(2, n.getCharno());

    n = n.getFirstChild();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(1, n.getLineno());
    assertEquals(3, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(1, n.getLineno());
    assertEquals(6, n.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoObjectLiteral
  public void testLinenoCharnoObjectLiteral() throws Exception {
    Node n = parse("\n\n var a = {a:0\n,b :1};")
        .getFirstChild().getFirstChild().getFirstChild();

    assertEquals(Token.OBJECTLIT, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(9, n.getCharno());

    n = n.getFirstChild();

    assertEquals(Token.STRING, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(10, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(12, n.getCharno());

    n = n.getNext();

    assertEquals(Token.STRING, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(1, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(4, n.getCharno());
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoAdd
  public void testLinenoCharnoAdd() throws Exception {
    testLinenoCharnoBinop("+");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoSub
  public void testLinenoCharnoSub() throws Exception {
    testLinenoCharnoBinop("-");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoMul
  public void testLinenoCharnoMul() throws Exception {
    testLinenoCharnoBinop("*");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoDiv
  public void testLinenoCharnoDiv() throws Exception {
    testLinenoCharnoBinop("/");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoMod
  public void testLinenoCharnoMod() throws Exception {
    testLinenoCharnoBinop("%");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoShift
  public void testLinenoCharnoShift() throws Exception {
    testLinenoCharnoBinop("<<");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoBinaryAnd
  public void testLinenoCharnoBinaryAnd() throws Exception {
    testLinenoCharnoBinop("&");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoAnd
  public void testLinenoCharnoAnd() throws Exception {
    testLinenoCharnoBinop("&&");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoBinaryOr
  public void testLinenoCharnoBinaryOr() throws Exception {
    testLinenoCharnoBinop("|");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoOr
  public void testLinenoCharnoOr() throws Exception {
    testLinenoCharnoBinop("||");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoLt
  public void testLinenoCharnoLt() throws Exception {
    testLinenoCharnoBinop("<");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoLe
  public void testLinenoCharnoLe() throws Exception {
    testLinenoCharnoBinop("<=");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGt
  public void testLinenoCharnoGt() throws Exception {
    testLinenoCharnoBinop(">");
  }

// com.google.javascript.rhino.ParserTest::testLinenoCharnoGe
  public void testLinenoCharnoGe() throws Exception {
    testLinenoCharnoBinop(">=");
  }

// com.google.javascript.rhino.ParserTest::testUnescapedSlashInRegexpCharClass
  public void testUnescapedSlashInRegexpCharClass() throws Exception {
    
    parse("var foo = /[/]/;");
    parse("var foo = /[hi there/]/;");
    parse("var foo = /[/yo dude]/;");
    parse("var foo = /\\/[@#$/watashi/wa/suteevu/desu]/;");
  }

// com.google.javascript.rhino.ParserTest::testParse
  public void testParse() {
    Node a = Node.newString(Token.NAME, "a");
    a.addChildToFront(Node.newString(Token.NAME, "b"));
    List<ParserResult> testCases = ImmutableList.of(
        new ParserResult(
            "3;",
            createScript(new Node(Token.EXPR_RESULT, Node.newNumber(3.0)))),
        new ParserResult(
            "var a = b;",
             createScript(new Node(Token.VAR, a))),
        new ParserResult(
            "\"hell\\\no\\ world\\\n\\\n!\"",
             createScript(new Node(Token.EXPR_RESULT,
             Node.newString(Token.STRING, "hello world!")))));

    for (ParserResult testCase : testCases) {
      assertNodeEquality(testCase.node, parse(testCase.code));
    }
  }

// com.google.javascript.rhino.ParserTest::testTrailingCommaWarning1
  public void testTrailingCommaWarning1() {
    parse("var a = ['foo', 'bar'];");
  }

// com.google.javascript.rhino.ParserTest::testTrailingCommaWarning2
  public void testTrailingCommaWarning2() {
    parse("var a = ['foo',,'bar'];");
  }

// com.google.javascript.rhino.ParserTest::testTrailingCommaWarning3
  public void testTrailingCommaWarning3() {
    parse("var a = ['foo', 'bar',];", TRAILING_COMMA_MESSAGE);
  }

// com.google.javascript.rhino.ParserTest::testTrailingCommaWarning4
  public void testTrailingCommaWarning4() {
    parse("var a = [,];", TRAILING_COMMA_MESSAGE);
  }

// com.google.javascript.rhino.ParserTest::testTrailingCommaWarning5
  public void testTrailingCommaWarning5() {
    parse("var a = {'foo': 'bar'};");
  }

// com.google.javascript.rhino.ParserTest::testTrailingCommaWarning6
  public void testTrailingCommaWarning6() {
    parse("var a = {'foo': 'bar',};", TRAILING_COMMA_MESSAGE);
  }

// com.google.javascript.rhino.ParserTest::testTrailingCommaWarning7
  public void testTrailingCommaWarning7() {
    parse("var a = {,};", TRAILING_COMMA_MESSAGE);
  }

// com.google.javascript.rhino.jstype.FunctionParamBuilderTest::testBuild
  public void testBuild() throws Exception {
    FunctionParamBuilder builder = new FunctionParamBuilder(registry);
    assertTrue(builder.addRequiredParams(NUMBER_TYPE));
    assertTrue(builder.addOptionalParams(BOOLEAN_TYPE));
    assertTrue(builder.addVarArgs(STRING_TYPE));

    Node params = builder.build();
    assertEquals(NUMBER_TYPE, params.getFirstChild().getJSType());
    assertEquals(registry.createOptionalType(BOOLEAN_TYPE),
        params.getFirstChild().getNext().getJSType());
    assertEquals(registry.createOptionalType(STRING_TYPE),
        params.getLastChild().getJSType());

    assertTrue(params.getFirstChild().getNext().isOptionalArg());
    assertTrue(params.getLastChild().isVarArgs());
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGetBuiltInType
  public void testGetBuiltInType() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    assertEquals(typeRegistry.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        typeRegistry.getType("boolean"));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGetDeclaredType
  public void testGetDeclaredType() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "Foo";
    typeRegistry.declareType(name, type);
    assertEquals(type, typeRegistry.getType(name));

    
    JSTypeRegistry typeRegistry2 = new JSTypeRegistry(null);
    assertEquals(null, typeRegistry2.getType(name));
    assertEquals(type, typeRegistry.getType(name));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGetDeclaredTypeInNamespace
  public void testGetDeclaredTypeInNamespace() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);
    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "a.b.Foo";
    typeRegistry.declareType(name, type);
    assertEquals(type, typeRegistry.getType(name));
    assertTrue(typeRegistry.hasNamespace("a"));
    assertTrue(typeRegistry.hasNamespace("a.b"));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testTypeAsNamespace
  public void testTypeAsNamespace() {
    JSTypeRegistry typeRegistry = new JSTypeRegistry(null);

    JSType type = typeRegistry.createAnonymousObjectType();
    String name = "a.b.Foo";
    typeRegistry.declareType(name, type);
    assertEquals(type, typeRegistry.getType(name));

    type = typeRegistry.createAnonymousObjectType();
    name = "a.b.Foo.Bar";
    typeRegistry.declareType(name, type);
    assertEquals(type, typeRegistry.getType(name));

    assertTrue(typeRegistry.hasNamespace("a"));
    assertTrue(typeRegistry.hasNamespace("a.b"));
    assertTrue(typeRegistry.hasNamespace("a.b.Foo"));
  }

// com.google.javascript.rhino.jstype.JSTypeRegistryTest::testGenerationIncrementing
  public void testGenerationIncrementing() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    final JSTypeRegistry typeRegistry = new JSTypeRegistry(reporter);

    StaticScope<JSType> scope = new StaticScope<JSType>() {
          public StaticSlot<JSType> getSlot(final String name) {
            return new SimpleSlot(
                name,
                typeRegistry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
                false);
          }
          public StaticSlot<JSType> getOwnSlot(String name) {
            return getSlot(name);
          }
          public StaticScope<JSType> getParentScope() { return null; }
          public JSType getTypeOfThis() { return null; }
        };

    ObjectType namedType =
        (ObjectType) typeRegistry.getType(scope, "Foo", null, 0, 0);
    ObjectType subNamed =
        typeRegistry.createObjectType(typeRegistry.createObjectType(namedType));

    
    typeRegistry.setLastGeneration(false);
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(subNamed.isUnknownType());

    
    
    typeRegistry.declareType("Foo", typeRegistry.createAnonymousObjectType());
    typeRegistry.resolveTypesInScope(scope);
    assertTrue(subNamed.isUnknownType());

    assertNull("Unexpected errors: " + reporter.errors(),
        reporter.errors());
    assertNull("Unexpected warnings: " + reporter.warnings(),
        reporter.warnings());

    
    typeRegistry.incrementGeneration();
    typeRegistry.setLastGeneration(true);
    typeRegistry.resolveTypesInScope(scope);
    assertFalse(subNamed.isUnknownType());
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testUniversalConstructorType
  public void testUniversalConstructorType() throws Exception {
    
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNoObjectType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNoType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isArrayType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isBooleanValueType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isDateType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isEnumElementType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNullType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNamedType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNullType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNumber());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNumberObjectType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNumberValueType());
    assertTrue(U2U_CONSTRUCTOR_TYPE.isObject());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isFunctionPrototypeType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isRegexpType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isString());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isStringObjectType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isStringValueType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isEnumType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isUnionType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isAllType());
    assertFalse(U2U_CONSTRUCTOR_TYPE.isVoidType());
    assertTrue(U2U_CONSTRUCTOR_TYPE.isConstructor());
    assertTrue(U2U_CONSTRUCTOR_TYPE.isInstanceType());

    
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NO_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.canAssignTo(functionType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(recordType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(STRING_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.canAssignTo(VOID_TYPE));

    
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NO_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NO_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(ALL_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(ARRAY_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(BOOLEAN_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(DATE_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(functionType));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(recordType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NULL_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(URI_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(STRING_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(TYPE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForEqualityWith(VOID_TYPE));

    
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(functionType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(recordType));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue( U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(U2U_CONSTRUCTOR_TYPE.
        canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(U2U_CONSTRUCTOR_TYPE.isNullable());

    
    assertTrue(U2U_CONSTRUCTOR_TYPE.isObject());

    
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesInt32Context());
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesNumberContext());
    assertTrue(U2U_CONSTRUCTOR_TYPE.matchesObjectContext());
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesStringContext());
    assertFalse(U2U_CONSTRUCTOR_TYPE.matchesUint32Context());

    
    assertEquals("Function",
        U2U_CONSTRUCTOR_TYPE.toString());

    
    assertEquals(UNKNOWN_TYPE,
        U2U_CONSTRUCTOR_TYPE.getPropertyType("anyProperty"));

    assertTrue(U2U_CONSTRUCTOR_TYPE.isNative());
    assertTrue(U2U_CONSTRUCTOR_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(U2U_CONSTRUCTOR_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNoObjectType
  public void testNoObjectType() throws Exception {
    
    assertTrue(NO_OBJECT_TYPE.isNoObjectType());
    assertFalse(NO_OBJECT_TYPE.isNoType());
    assertFalse(NO_OBJECT_TYPE.isArrayType());
    assertFalse(NO_OBJECT_TYPE.isBooleanValueType());
    assertFalse(NO_OBJECT_TYPE.isDateType());
    assertFalse(NO_OBJECT_TYPE.isEnumElementType());
    assertFalse(NO_OBJECT_TYPE.isNullType());
    assertFalse(NO_OBJECT_TYPE.isNamedType());
    assertFalse(NO_OBJECT_TYPE.isNullType());
    assertTrue(NO_OBJECT_TYPE.isNumber());
    assertFalse(NO_OBJECT_TYPE.isNumberObjectType());
    assertFalse(NO_OBJECT_TYPE.isNumberValueType());
    assertTrue(NO_OBJECT_TYPE.isObject());
    assertFalse(NO_OBJECT_TYPE.isFunctionPrototypeType());
    assertFalse(NO_OBJECT_TYPE.isRegexpType());
    assertTrue(NO_OBJECT_TYPE.isString());
    assertFalse(NO_OBJECT_TYPE.isStringObjectType());
    assertFalse(NO_OBJECT_TYPE.isStringValueType());
    assertFalse(NO_OBJECT_TYPE.isEnumType());
    assertFalse(NO_OBJECT_TYPE.isUnionType());
    assertFalse(NO_OBJECT_TYPE.isAllType());
    assertFalse(NO_OBJECT_TYPE.isVoidType());
    assertTrue(NO_OBJECT_TYPE.isConstructor());
    assertFalse(NO_OBJECT_TYPE.isInstanceType());

    
    assertFalse(NO_OBJECT_TYPE.canAssignTo(NO_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(functionType));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(recordType));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canAssignTo(VOID_TYPE));

    
    assertFalse(NO_OBJECT_TYPE.canTestForEqualityWith(NO_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(ARRAY_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(BOOLEAN_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(functionType));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(recordType));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(NULL_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(URI_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(STRING_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForEqualityWith(VOID_TYPE));

    
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(recordType));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(NO_OBJECT_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NO_OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NO_OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(NO_OBJECT_TYPE.isNullable());

    
    assertTrue(NO_OBJECT_TYPE.isObject());

    
    assertTrue(NO_OBJECT_TYPE.matchesInt32Context());
    assertTrue(NO_OBJECT_TYPE.matchesNumberContext());
    assertTrue(NO_OBJECT_TYPE.matchesObjectContext());
    assertTrue(NO_OBJECT_TYPE.matchesStringContext());
    assertTrue(NO_OBJECT_TYPE.matchesUint32Context());

    
    assertEquals("NoObject", NO_OBJECT_TYPE.toString());

    
    assertEquals(NO_TYPE,
        NO_OBJECT_TYPE.getPropertyType("anyProperty"));

    Asserts.assertResolvesToSame(NO_OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNoType
  public void testNoType() throws Exception {
    
    assertFalse(NO_TYPE.isNoObjectType());
    assertTrue(NO_TYPE.isNoType());
    assertFalse(NO_TYPE.isArrayType());
    assertFalse(NO_TYPE.isBooleanValueType());
    assertFalse(NO_TYPE.isDateType());
    assertFalse(NO_TYPE.isEnumElementType());
    assertFalse(NO_TYPE.isNullType());
    assertFalse(NO_TYPE.isNamedType());
    assertFalse(NO_TYPE.isNullType());
    assertTrue(NO_TYPE.isNumber());
    assertFalse(NO_TYPE.isNumberObjectType());
    assertFalse(NO_TYPE.isNumberValueType());
    assertTrue(NO_TYPE.isObject());
    assertFalse(NO_TYPE.isFunctionPrototypeType());
    assertFalse(NO_TYPE.isRegexpType());
    assertTrue(NO_TYPE.isString());
    assertFalse(NO_TYPE.isStringObjectType());
    assertFalse(NO_TYPE.isStringValueType());
    assertFalse(NO_TYPE.isEnumType());
    assertFalse(NO_TYPE.isUnionType());
    assertFalse(NO_TYPE.isAllType());
    assertFalse(NO_TYPE.isVoidType());
    assertTrue(NO_TYPE.isConstructor());
    assertFalse(NO_TYPE.isInstanceType());

    
    assertTrue(NO_TYPE.canAssignTo(NO_TYPE));
    assertTrue(NO_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(NO_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertTrue(NO_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NO_TYPE.canAssignTo(ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(functionType));
    assertTrue(NO_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NO_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(NO_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(REGEXP_TYPE));
    assertTrue(NO_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(NO_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NO_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(NO_TYPE.canAssignTo(ALL_TYPE));
    assertTrue(NO_TYPE.canAssignTo(VOID_TYPE));

    
    assertFalse(NO_TYPE.canTestForEqualityWith(NO_TYPE));
    assertFalse(NO_TYPE.canTestForEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(ARRAY_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(BOOLEAN_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(functionType));
    assertTrue(NO_TYPE.canTestForEqualityWith(NULL_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(URI_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(STRING_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(NO_TYPE.canTestForEqualityWith(VOID_TYPE));

    
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertTrue(NO_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertTrue(NO_TYPE.isNullable());

    
    assertTrue(NO_TYPE.isObject());

    
    assertTrue(NO_TYPE.matchesInt32Context());
    assertTrue(NO_TYPE.matchesNumberContext());
    assertTrue(NO_TYPE.matchesObjectContext());
    assertTrue(NO_TYPE.matchesStringContext());
    assertTrue(NO_TYPE.matchesUint32Context());

    
    assertEquals("None", NO_TYPE.toString());

    
    assertEquals(NO_TYPE,
        NO_TYPE.getPropertyType("anyProperty"));

    Asserts.assertResolvesToSame(NO_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testArrayType
  public void testArrayType() throws Exception {
    
    assertTrue(ARRAY_TYPE.isArrayType());
    assertFalse(ARRAY_TYPE.isBooleanValueType());
    assertFalse(ARRAY_TYPE.isDateType());
    assertFalse(ARRAY_TYPE.isEnumElementType());
    assertFalse(ARRAY_TYPE.isNamedType());
    assertFalse(ARRAY_TYPE.isNullType());
    assertFalse(ARRAY_TYPE.isNumber());
    assertFalse(ARRAY_TYPE.isNumberObjectType());
    assertFalse(ARRAY_TYPE.isNumberValueType());
    assertTrue(ARRAY_TYPE.isObject());
    assertFalse(ARRAY_TYPE.isFunctionPrototypeType());
    assertTrue(ARRAY_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(ARRAY_TYPE.isRegexpType());
    assertFalse(ARRAY_TYPE.isString());
    assertFalse(ARRAY_TYPE.isStringObjectType());
    assertFalse(ARRAY_TYPE.isStringValueType());
    assertFalse(ARRAY_TYPE.isEnumType());
    assertFalse(ARRAY_TYPE.isUnionType());
    assertFalse(ARRAY_TYPE.isAllType());
    assertFalse(ARRAY_TYPE.isVoidType());
    assertFalse(ARRAY_TYPE.isConstructor());
    assertTrue(ARRAY_TYPE.isInstanceType());

    
    assertFalse(ARRAY_TYPE.canAssignTo(NO_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(functionType));
    assertFalse(ARRAY_TYPE.canAssignTo(recordType));
    assertFalse(ARRAY_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(ARRAY_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(ARRAY_TYPE.canAssignTo(namedGoogBar));
    assertFalse(ARRAY_TYPE.canAssignTo(REGEXP_TYPE));

    
    assertFalse(ARRAY_TYPE.canBeCalled());

    
    assertTrue(ARRAY_TYPE.canTestForEqualityWith(NO_TYPE));
    assertTrue(ARRAY_TYPE.canTestForEqualityWith(NO_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(ARRAY_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(ARRAY_TYPE.canTestForEqualityWith(functionType));
    assertTrue(ARRAY_TYPE.canTestForEqualityWith(recordType));
    assertFalse(ARRAY_TYPE.canTestForEqualityWith(VOID_TYPE));
    assertTrue(ARRAY_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(ARRAY_TYPE.canTestForEqualityWith(REGEXP_TYPE));

    
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(recordType));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(ARRAY_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(ARRAY_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(ARRAY_TYPE.isNullable());
    assertTrue(createUnionType(ARRAY_TYPE, NULL_TYPE).isNullable());

    
    assertTrue(ARRAY_TYPE.isObject());

    
    assertEquals(ALL_TYPE,
        ARRAY_TYPE.getLeastSupertype(ALL_TYPE));
    assertEquals(createUnionType(STRING_OBJECT_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertEquals(createUnionType(NUMBER_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertEquals(createUnionType(ARRAY_TYPE, functionType),
        ARRAY_TYPE.getLeastSupertype(functionType));
    assertEquals(OBJECT_TYPE, ARRAY_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertEquals(createUnionType(DATE_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(DATE_TYPE));
    assertEquals(createUnionType(REGEXP_TYPE, ARRAY_TYPE),
        ARRAY_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(17, ARRAY_TYPE.getImplicitPrototype().getPropertiesCount());
    assertEquals(18, ARRAY_TYPE.getPropertiesCount());
    assertReturnTypeEquals(ARRAY_TYPE,
        ARRAY_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(STRING_TYPE,
        ARRAY_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        ARRAY_TYPE.getPropertyType("toLocaleString"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("concat"));
    assertReturnTypeEquals(STRING_TYPE,
        ARRAY_TYPE.getPropertyType("join"));
    assertReturnTypeEquals(UNKNOWN_TYPE, ARRAY_TYPE.getPropertyType("pop"));
    assertReturnTypeEquals(NUMBER_TYPE, ARRAY_TYPE.getPropertyType("push"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("reverse"));
    assertReturnTypeEquals(UNKNOWN_TYPE, ARRAY_TYPE.getPropertyType("shift"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("slice"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("sort"));
    assertReturnTypeEquals(ARRAY_TYPE, ARRAY_TYPE.getPropertyType("splice"));
    assertReturnTypeEquals(NUMBER_TYPE, ARRAY_TYPE.getPropertyType("unshift"));
    assertEquals(NUMBER_TYPE, ARRAY_TYPE.getPropertyType("length"));

    
    assertPropertyTypeDeclared(ARRAY_TYPE, "pop");

    
    assertFalse(ARRAY_TYPE.matchesInt32Context());
    assertFalse(ARRAY_TYPE.matchesNumberContext());
    assertTrue(ARRAY_TYPE.matchesObjectContext());
    assertTrue(ARRAY_TYPE.matchesStringContext());
    assertFalse(ARRAY_TYPE.matchesUint32Context());

    
    assertEquals("Array", ARRAY_TYPE.toString());

    assertTrue(ARRAY_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(ARRAY_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testUnknownType
  public void testUnknownType() throws Exception {
    
    assertFalse(UNKNOWN_TYPE.isArrayType());
    assertFalse(UNKNOWN_TYPE.isBooleanObjectType());
    assertFalse(UNKNOWN_TYPE.isBooleanValueType());
    assertFalse(UNKNOWN_TYPE.isDateType());
    assertFalse(UNKNOWN_TYPE.isEnumElementType());
    assertFalse(UNKNOWN_TYPE.isNamedType());
    assertFalse(UNKNOWN_TYPE.isNullType());
    assertFalse(UNKNOWN_TYPE.isNumberObjectType());
    assertFalse(UNKNOWN_TYPE.isNumberValueType());
    assertTrue(UNKNOWN_TYPE.isObject());
    assertFalse(UNKNOWN_TYPE.isFunctionPrototypeType());
    assertFalse(UNKNOWN_TYPE.isRegexpType());
    assertFalse(UNKNOWN_TYPE.isStringObjectType());
    assertFalse(UNKNOWN_TYPE.isStringValueType());
    assertFalse(UNKNOWN_TYPE.isEnumType());
    assertFalse(UNKNOWN_TYPE.isUnionType());
    assertTrue(UNKNOWN_TYPE.isUnknownType());
    assertFalse(UNKNOWN_TYPE.isVoidType());
    assertFalse(UNKNOWN_TYPE.isConstructor());
    assertFalse(UNKNOWN_TYPE.isInstanceType());

    
    assertNull(UNKNOWN_TYPE.autoboxesTo());

    
    assertTrue(UNKNOWN_TYPE.canAssignTo(UNKNOWN_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(STRING_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(NUMBER_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(functionType));
    assertTrue(UNKNOWN_TYPE.canAssignTo(recordType));
    assertTrue(UNKNOWN_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(OBJECT_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(namedGoogBar));
    assertTrue(UNKNOWN_TYPE.canAssignTo(unresolvedNamedType));
    assertTrue(UNKNOWN_TYPE.canAssignTo(REGEXP_TYPE));
    assertTrue(UNKNOWN_TYPE.canAssignTo(VOID_TYPE));

    
    assertTrue(UNKNOWN_TYPE.canBeCalled());

    
    assertTrue(UNKNOWN_TYPE.canTestForEqualityWith(UNKNOWN_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForEqualityWith(STRING_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForEqualityWith(functionType));
    assertTrue(UNKNOWN_TYPE.canTestForEqualityWith(recordType));
    assertTrue(UNKNOWN_TYPE.canTestForEqualityWith(VOID_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForEqualityWith(BOOLEAN_TYPE));

    
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(recordType));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(UNKNOWN_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));

    
    assertTrue(UNKNOWN_TYPE.isNullable());

    
    assertEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(UNKNOWN_TYPE));
    assertEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(STRING_TYPE));
    assertEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(functionType));
    assertEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(DATE_TYPE));
    assertEquals(UNKNOWN_TYPE,
        UNKNOWN_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(UNKNOWN_TYPE.matchesInt32Context());
    assertTrue(UNKNOWN_TYPE.matchesNumberContext());
    assertTrue(UNKNOWN_TYPE.matchesObjectContext());
    assertTrue(UNKNOWN_TYPE.matchesStringContext());
    assertTrue(UNKNOWN_TYPE.matchesUint32Context());

    
    assertPropertyTypeUnknown(UNKNOWN_TYPE, "XXX");

    
    assertEquals("?", UNKNOWN_TYPE.toString());

    Asserts.assertResolvesToSame(UNKNOWN_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testAllType
  public void testAllType() throws Exception {
    
    assertFalse(ALL_TYPE.isArrayType());
    assertFalse(ALL_TYPE.isBooleanValueType());
    assertFalse(ALL_TYPE.isDateType());
    assertFalse(ALL_TYPE.isEnumElementType());
    assertFalse(ALL_TYPE.isNamedType());
    assertFalse(ALL_TYPE.isNullType());
    assertFalse(ALL_TYPE.isNumber());
    assertFalse(ALL_TYPE.isNumberObjectType());
    assertFalse(ALL_TYPE.isNumberValueType());
    assertFalse(ALL_TYPE.isObject());
    assertFalse(ALL_TYPE.isFunctionPrototypeType());
    assertFalse(ALL_TYPE.isRegexpType());
    assertFalse(ALL_TYPE.isString());
    assertFalse(ALL_TYPE.isStringObjectType());
    assertFalse(ALL_TYPE.isStringValueType());
    assertFalse(ALL_TYPE.isEnumType());
    assertFalse(ALL_TYPE.isUnionType());
    assertTrue(ALL_TYPE.isAllType());
    assertFalse(ALL_TYPE.isVoidType());
    assertFalse(ALL_TYPE.isConstructor());
    assertFalse(ALL_TYPE.isInstanceType());

    
    assertFalse(ALL_TYPE.canAssignTo(NO_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(functionType));
    assertFalse(ALL_TYPE.canAssignTo(recordType));
    assertFalse(ALL_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(ALL_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(ALL_TYPE.canAssignTo(namedGoogBar));
    assertFalse(ALL_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(ALL_TYPE.canAssignTo(VOID_TYPE));
    assertTrue(ALL_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(ALL_TYPE.canBeCalled());

    
    assertTrue(ALL_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(ALL_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(ALL_TYPE.canTestForEqualityWith(functionType));
    assertTrue(ALL_TYPE.canTestForEqualityWith(recordType));
    assertTrue(ALL_TYPE.canTestForEqualityWith(VOID_TYPE));
    assertTrue(ALL_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(ALL_TYPE.canTestForEqualityWith(REGEXP_TYPE));

    
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(recordType));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertTrue(ALL_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(ALL_TYPE.isNullable());

    
    assertEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(ALL_TYPE));
    assertEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(functionType));
    assertEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(DATE_TYPE));
    assertEquals(ALL_TYPE,
        ALL_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertFalse(ALL_TYPE.matchesInt32Context());
    assertFalse(ALL_TYPE.matchesNumberContext());
    assertTrue(ALL_TYPE.matchesObjectContext());
    assertTrue(ALL_TYPE.matchesStringContext());
    assertFalse(ALL_TYPE.matchesUint32Context());

    
    assertEquals("*", ALL_TYPE.toString());

    Asserts.assertResolvesToSame(ALL_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testTheObjectType
  public void testTheObjectType() throws Exception {
    
    assertEquals(OBJECT_PROTOTYPE, OBJECT_TYPE.getImplicitPrototype());

    
    assertFalse(OBJECT_TYPE.isNoObjectType());
    assertFalse(OBJECT_TYPE.isNoType());
    assertFalse(OBJECT_TYPE.isArrayType());
    assertFalse(OBJECT_TYPE.isBooleanValueType());
    assertFalse(OBJECT_TYPE.isDateType());
    assertFalse(OBJECT_TYPE.isEnumElementType());
    assertFalse(OBJECT_TYPE.isNullType());
    assertFalse(OBJECT_TYPE.isNamedType());
    assertFalse(OBJECT_TYPE.isNullType());
    assertFalse(OBJECT_TYPE.isNumber());
    assertFalse(OBJECT_TYPE.isNumberObjectType());
    assertFalse(OBJECT_TYPE.isNumberValueType());
    assertTrue(OBJECT_TYPE.isObject());
    assertFalse(OBJECT_TYPE.isFunctionPrototypeType());
    assertTrue(OBJECT_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(OBJECT_TYPE.isRegexpType());
    assertFalse(OBJECT_TYPE.isString());
    assertFalse(OBJECT_TYPE.isStringObjectType());
    assertFalse(OBJECT_TYPE.isStringValueType());
    assertFalse(OBJECT_TYPE.isEnumType());
    assertFalse(OBJECT_TYPE.isUnionType());
    assertFalse(OBJECT_TYPE.isAllType());
    assertFalse(OBJECT_TYPE.isVoidType());
    assertFalse(OBJECT_TYPE.isConstructor());
    assertTrue(OBJECT_TYPE.isInstanceType());

    
    assertFalse(OBJECT_TYPE.canAssignTo(NO_TYPE));
    assertTrue(OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(functionType));
    assertFalse(OBJECT_TYPE.canAssignTo(recordType));
    assertFalse(OBJECT_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(namedGoogBar));
    assertTrue(OBJECT_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(OBJECT_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(OBJECT_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(OBJECT_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(OBJECT_TYPE.canBeCalled());

    
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(STRING_TYPE));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(BOOLEAN_TYPE));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(functionType));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(recordType));
    assertFalse(OBJECT_TYPE.canTestForEqualityWith(VOID_TYPE));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(ARRAY_TYPE));
    assertTrue(OBJECT_TYPE.canTestForEqualityWith(UNKNOWN_TYPE));

    
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(recordType));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(OBJECT_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(OBJECT_TYPE.isNullable());

    
    assertEquals(ALL_TYPE,
        OBJECT_TYPE.getLeastSupertype(ALL_TYPE));
    assertEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertEquals(createUnionType(OBJECT_TYPE, NUMBER_TYPE),
        OBJECT_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(functionType));
    assertEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(DATE_TYPE));
    assertEquals(OBJECT_TYPE,
        OBJECT_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(7, OBJECT_TYPE.getPropertiesCount());
    assertReturnTypeEquals(OBJECT_TYPE,
        OBJECT_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(STRING_TYPE,
        OBJECT_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        OBJECT_TYPE.getPropertyType("toLocaleString"));
    assertReturnTypeEquals(UNKNOWN_TYPE,
        OBJECT_TYPE.getPropertyType("valueOf"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        OBJECT_TYPE.getPropertyType("hasOwnProperty"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        OBJECT_TYPE.getPropertyType("isPrototypeOf"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        OBJECT_TYPE.getPropertyType("propertyIsEnumerable"));

    
    assertFalse(OBJECT_TYPE.matchesInt32Context());
    assertFalse(OBJECT_TYPE.matchesNumberContext());
    assertTrue(OBJECT_TYPE.matchesObjectContext());
    assertTrue(OBJECT_TYPE.matchesStringContext());
    assertFalse(OBJECT_TYPE.matchesUint32Context());

    
    assertEquals(OBJECT_PROTOTYPE, OBJECT_TYPE.getImplicitPrototype());

    
    assertEquals("Object", OBJECT_TYPE.toString());

    assertTrue(OBJECT_TYPE.isNativeObjectType());
    assertTrue(OBJECT_TYPE.getImplicitPrototype().isNativeObjectType());

    Asserts.assertResolvesToSame(OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNumberObjectType
  public void testNumberObjectType() throws Exception {
    
    assertFalse(NUMBER_OBJECT_TYPE.isArrayType());
    assertFalse(NUMBER_OBJECT_TYPE.isBooleanObjectType());
    assertFalse(NUMBER_OBJECT_TYPE.isBooleanValueType());
    assertFalse(NUMBER_OBJECT_TYPE.isDateType());
    assertFalse(NUMBER_OBJECT_TYPE.isEnumElementType());
    assertFalse(NUMBER_OBJECT_TYPE.isNamedType());
    assertFalse(NUMBER_OBJECT_TYPE.isNullType());
    assertTrue(NUMBER_OBJECT_TYPE.isNumber());
    assertTrue(NUMBER_OBJECT_TYPE.isNumberObjectType());
    assertFalse(NUMBER_OBJECT_TYPE.isNumberValueType());
    assertTrue(NUMBER_OBJECT_TYPE.isObject());
    assertFalse(NUMBER_OBJECT_TYPE.isFunctionPrototypeType());
    assertTrue(NUMBER_OBJECT_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(NUMBER_OBJECT_TYPE.isRegexpType());
    assertFalse(NUMBER_OBJECT_TYPE.isString());
    assertFalse(NUMBER_OBJECT_TYPE.isStringObjectType());
    assertFalse(NUMBER_OBJECT_TYPE.isStringValueType());
    assertFalse(NUMBER_OBJECT_TYPE.isEnumType());
    assertFalse(NUMBER_OBJECT_TYPE.isUnionType());
    assertFalse(NUMBER_OBJECT_TYPE.isAllType());
    assertFalse(NUMBER_OBJECT_TYPE.isVoidType());
    assertFalse(NUMBER_OBJECT_TYPE.isConstructor());
    assertTrue(NUMBER_OBJECT_TYPE.isInstanceType());

    
    assertEquals(NUMBER_OBJECT_TYPE, NUMBER_TYPE.autoboxesTo());

    
    assertEquals(NUMBER_TYPE, NUMBER_OBJECT_TYPE.unboxesTo());

    
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(functionType));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(namedGoogBar));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(
            createUnionType(NUMBER_OBJECT_TYPE, NULL_TYPE)));
    assertFalse(NUMBER_OBJECT_TYPE.canAssignTo(
            createUnionType(NUMBER_TYPE, NULL_TYPE)));
    assertTrue(NUMBER_OBJECT_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(NUMBER_OBJECT_TYPE.canBeCalled());

    
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(NO_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(functionType));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(elementsType));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForEqualityWith(VOID_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForEqualityWith(ARRAY_TYPE));

    
    assertTrue(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.
        canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NUMBER_OBJECT_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(NUMBER_OBJECT_TYPE.isNullable());

    
    assertEquals(ALL_TYPE,
        NUMBER_OBJECT_TYPE.getLeastSupertype(ALL_TYPE));
    assertEquals(createUnionType(NUMBER_OBJECT_TYPE, STRING_OBJECT_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertEquals(createUnionType(NUMBER_OBJECT_TYPE, NUMBER_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertEquals(createUnionType(NUMBER_OBJECT_TYPE, functionType),
        NUMBER_OBJECT_TYPE.getLeastSupertype(functionType));
    assertEquals(OBJECT_TYPE,
        NUMBER_OBJECT_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertEquals(createUnionType(NUMBER_OBJECT_TYPE, DATE_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(DATE_TYPE));
    assertEquals(createUnionType(NUMBER_OBJECT_TYPE, REGEXP_TYPE),
        NUMBER_OBJECT_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(NUMBER_OBJECT_TYPE.matchesInt32Context());
    assertTrue(NUMBER_OBJECT_TYPE.matchesNumberContext());
    assertTrue(NUMBER_OBJECT_TYPE.matchesObjectContext());
    assertTrue(NUMBER_OBJECT_TYPE.matchesStringContext());
    assertTrue(NUMBER_OBJECT_TYPE.matchesUint32Context());

    
    assertEquals("Number", NUMBER_OBJECT_TYPE.toString());

    assertTrue(NUMBER_OBJECT_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(NUMBER_OBJECT_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNumberValueType
  public void testNumberValueType() throws Exception {
    
    assertFalse(NUMBER_TYPE.isArrayType());
    assertFalse(NUMBER_TYPE.isBooleanObjectType());
    assertFalse(NUMBER_TYPE.isBooleanValueType());
    assertFalse(NUMBER_TYPE.isDateType());
    assertFalse(NUMBER_TYPE.isEnumElementType());
    assertFalse(NUMBER_TYPE.isNamedType());
    assertFalse(NUMBER_TYPE.isNullType());
    assertTrue(NUMBER_TYPE.isNumber());
    assertFalse(NUMBER_TYPE.isNumberObjectType());
    assertTrue(NUMBER_TYPE.isNumberValueType());
    assertFalse(NUMBER_TYPE.isFunctionPrototypeType());
    assertFalse(NUMBER_TYPE.isRegexpType());
    assertFalse(NUMBER_TYPE.isString());
    assertFalse(NUMBER_TYPE.isStringObjectType());
    assertFalse(NUMBER_TYPE.isStringValueType());
    assertFalse(NUMBER_TYPE.isEnumType());
    assertFalse(NUMBER_TYPE.isUnionType());
    assertFalse(NUMBER_TYPE.isAllType());
    assertFalse(NUMBER_TYPE.isVoidType());
    assertFalse(NUMBER_TYPE.isConstructor());
    assertFalse(NUMBER_TYPE.isInstanceType());

    
    assertEquals(NUMBER_OBJECT_TYPE, NUMBER_TYPE.autoboxesTo());

    
    assertTrue(NUMBER_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertTrue(NUMBER_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(functionType));
    assertFalse(NUMBER_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canAssignTo(DATE_TYPE));
    assertTrue(NUMBER_TYPE.canAssignTo(unresolvedNamedType));
    assertFalse(NUMBER_TYPE.canAssignTo(namedGoogBar));
    assertTrue(NUMBER_TYPE.canAssignTo(
            createUnionType(NUMBER_TYPE, NULL_TYPE)));
    assertTrue(NUMBER_TYPE.canAssignTo(UNKNOWN_TYPE));

    
    assertFalse(NUMBER_TYPE.canBeCalled());

    
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(NO_TYPE));
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(functionType));
    assertFalse(NUMBER_TYPE.canTestForEqualityWith(VOID_TYPE));
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(ARRAY_TYPE));
    assertTrue(NUMBER_TYPE.canTestForEqualityWith(UNKNOWN_TYPE));

    
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NUMBER_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(NUMBER_TYPE.canTestForShallowEqualityWith(UNKNOWN_TYPE));

    
    assertFalse(NUMBER_TYPE.isNullable());

    
    assertEquals(ALL_TYPE,
        NUMBER_TYPE.getLeastSupertype(ALL_TYPE));
    assertEquals(createUnionType(NUMBER_TYPE, STRING_OBJECT_TYPE),
        NUMBER_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertEquals(NUMBER_TYPE,
        NUMBER_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertEquals(createUnionType(NUMBER_TYPE, functionType),
        NUMBER_TYPE.getLeastSupertype(functionType));
    assertEquals(createUnionType(NUMBER_TYPE, OBJECT_TYPE),
        NUMBER_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertEquals(createUnionType(NUMBER_TYPE, DATE_TYPE),
        NUMBER_TYPE.getLeastSupertype(DATE_TYPE));
    assertEquals(createUnionType(NUMBER_TYPE, REGEXP_TYPE),
        NUMBER_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(NUMBER_TYPE.matchesInt32Context());
    assertTrue(NUMBER_TYPE.matchesNumberContext());
    assertTrue(NUMBER_TYPE.matchesObjectContext());
    assertTrue(NUMBER_TYPE.matchesStringContext());
    assertTrue(NUMBER_TYPE.matchesUint32Context());

    
    assertEquals("number", NUMBER_TYPE.toString());

    Asserts.assertResolvesToSame(NUMBER_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testNullType
  public void testNullType() throws Exception {
    
    assertFalse(NULL_TYPE.isArrayType());
    assertFalse(NULL_TYPE.isBooleanValueType());
    assertFalse(NULL_TYPE.isDateType());
    assertFalse(NULL_TYPE.isEnumElementType());
    assertFalse(NULL_TYPE.isNamedType());
    assertTrue(NULL_TYPE.isNullType());
    assertFalse(NULL_TYPE.isNumber());
    assertFalse(NULL_TYPE.isNumberObjectType());
    assertFalse(NULL_TYPE.isNumberValueType());
    assertFalse(NULL_TYPE.isFunctionPrototypeType());
    assertFalse(NULL_TYPE.isRegexpType());
    assertFalse(NULL_TYPE.isString());
    assertFalse(NULL_TYPE.isStringObjectType());
    assertFalse(NULL_TYPE.isStringValueType());
    assertFalse(NULL_TYPE.isEnumType());
    assertFalse(NULL_TYPE.isUnionType());
    assertFalse(NULL_TYPE.isAllType());
    assertFalse(NULL_TYPE.isVoidType());
    assertFalse(NULL_TYPE.isConstructor());
    assertFalse(NULL_TYPE.isInstanceType());

    
    assertNull(NULL_TYPE.autoboxesTo());

    
    assertFalse(NULL_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(NO_TYPE));
    assertTrue(NULL_TYPE.canAssignTo(NULL_TYPE));
    assertTrue(NULL_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(functionType));
    assertFalse(NULL_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(NULL_TYPE.canAssignTo(ARRAY_TYPE));
    assertTrue(NULL_TYPE.canAssignTo(UNKNOWN_TYPE));

    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NO_OBJECT_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NO_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NULL_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(ALL_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(STRING_OBJECT_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(NUMBER_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(functionType)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(OBJECT_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(DATE_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(REGEXP_TYPE)));
    assertTrue(NULL_TYPE.canAssignTo(createNullableType(ARRAY_TYPE)));

    
    assertFalse(NULL_TYPE.canBeCalled());

    
    assertTrue(NULL_TYPE.canTestForEqualityWith(NO_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(NO_OBJECT_TYPE));
    assertTrue(NULL_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(ARRAY_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(BOOLEAN_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(functionType));
    assertFalse(NULL_TYPE.canTestForEqualityWith(NULL_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(URI_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(STRING_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(TYPE_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForEqualityWith(VOID_TYPE));

    
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(NULL_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(functionType));
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(NULL_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(NULL_TYPE.canTestForShallowEqualityWith(VOID_TYPE));
    assertTrue(NULL_TYPE.canTestForShallowEqualityWith(
            createNullableType(STRING_OBJECT_TYPE)));

    
    assertEquals(NULL_TYPE, NULL_TYPE.getLeastSupertype(NULL_TYPE));
    assertEquals(ALL_TYPE, NULL_TYPE.getLeastSupertype(ALL_TYPE));
    assertEquals(createNullableType(STRING_OBJECT_TYPE),
        NULL_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertEquals(createNullableType(NUMBER_TYPE),
        NULL_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertEquals(createNullableType(functionType),
        NULL_TYPE.getLeastSupertype(functionType));
    assertEquals(createNullableType(OBJECT_TYPE),
        NULL_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertEquals(createNullableType(DATE_TYPE),
        NULL_TYPE.getLeastSupertype(DATE_TYPE));
    assertEquals(createNullableType(REGEXP_TYPE),
        NULL_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertTrue(NULL_TYPE.matchesInt32Context());
    assertTrue(NULL_TYPE.matchesNumberContext());
    assertFalse(NULL_TYPE.matchesObjectContext());
    assertTrue(NULL_TYPE.matchesStringContext());
    assertTrue(NULL_TYPE.matchesUint32Context());

    
    assertFalse(NULL_TYPE.matchesObjectContext());

    
    assertEquals("null", NULL_TYPE.toString());

    Asserts.assertResolvesToSame(NULL_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testDateType
  public void testDateType() throws Exception {
    
    assertFalse(DATE_TYPE.isArrayType());
    assertFalse(DATE_TYPE.isBooleanValueType());
    assertTrue(DATE_TYPE.isDateType());
    assertFalse(DATE_TYPE.isEnumElementType());
    assertFalse(DATE_TYPE.isNamedType());
    assertFalse(DATE_TYPE.isNullType());
    assertFalse(DATE_TYPE.isNumberValueType());
    assertFalse(DATE_TYPE.isFunctionPrototypeType());
    assertTrue(DATE_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertFalse(DATE_TYPE.isRegexpType());
    assertFalse(DATE_TYPE.isStringValueType());
    assertFalse(DATE_TYPE.isEnumType());
    assertFalse(DATE_TYPE.isUnionType());
    assertFalse(DATE_TYPE.isAllType());
    assertFalse(DATE_TYPE.isVoidType());
    assertFalse(DATE_TYPE.isConstructor());
    assertTrue(DATE_TYPE.isInstanceType());

    
    assertNull(DATE_TYPE.autoboxesTo());

    
    assertFalse(DATE_TYPE.canAssignTo(NO_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(DATE_TYPE.isSubtype(ARRAY_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(functionType));
    assertFalse(DATE_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(STRING_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(DATE_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(DATE_TYPE.canAssignTo(VOID_TYPE));

    
    assertFalse(DATE_TYPE.canBeCalled());

    
    assertTrue(DATE_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(DATE_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(DATE_TYPE.canTestForEqualityWith(functionType));
    assertFalse(DATE_TYPE.canTestForEqualityWith(VOID_TYPE));
    assertTrue(DATE_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(DATE_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(DATE_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(DATE_TYPE.canTestForEqualityWith(ARRAY_TYPE));

    
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(DATE_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(DATE_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(DATE_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(DATE_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(DATE_TYPE.isNullable());
    assertTrue(createNullableType(DATE_TYPE).isNullable());

    
    assertEquals(ALL_TYPE,
        DATE_TYPE.getLeastSupertype(ALL_TYPE));
    assertEquals(createUnionType(DATE_TYPE, STRING_OBJECT_TYPE),
        DATE_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertEquals(createUnionType(DATE_TYPE, NUMBER_TYPE),
        DATE_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertEquals(createUnionType(DATE_TYPE, functionType),
        DATE_TYPE.getLeastSupertype(functionType));
    assertEquals(OBJECT_TYPE, DATE_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertEquals(DATE_TYPE, DATE_TYPE.getLeastSupertype(DATE_TYPE));
    assertEquals(createUnionType(DATE_TYPE, REGEXP_TYPE),
        DATE_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(46, DATE_TYPE.getImplicitPrototype().getPropertiesCount());
    assertEquals(46, DATE_TYPE.getPropertiesCount());
    assertReturnTypeEquals(DATE_TYPE, DATE_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toDateString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toTimeString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toLocaleString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toLocaleDateString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toLocaleTimeString"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("valueOf"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getTime"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getFullYear"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCFullYear"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getMonth"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCMonth"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getDate"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCDate"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getDay"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getUTCDay"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("getHours"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCHours"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getUTCMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("getTimezoneOffset"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setTime"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCMilliseconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCSeconds"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCMinutes"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setHours"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCHours"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setDate"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCDate"));
    assertReturnTypeEquals(NUMBER_TYPE, DATE_TYPE.getPropertyType("setMonth"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCMonth"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setFullYear"));
    assertReturnTypeEquals(NUMBER_TYPE,
        DATE_TYPE.getPropertyType("setUTCFullYear"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toUTCString"));
    assertReturnTypeEquals(STRING_TYPE,
        DATE_TYPE.getPropertyType("toGMTString"));

    
    assertTrue(DATE_TYPE.matchesInt32Context());
    assertTrue(DATE_TYPE.matchesNumberContext());
    assertTrue(DATE_TYPE.matchesObjectContext());
    assertTrue(DATE_TYPE.matchesStringContext());
    assertTrue(DATE_TYPE.matchesUint32Context());

    
    assertEquals("Date", DATE_TYPE.toString());

    assertTrue(DATE_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(DATE_TYPE);
  }

// com.google.javascript.rhino.jstype.JSTypeTest::testRegExpType
  public void testRegExpType() throws Exception {
    
    assertFalse(REGEXP_TYPE.isNoType());
    assertFalse(REGEXP_TYPE.isNoObjectType());
    assertFalse(REGEXP_TYPE.isArrayType());
    assertFalse(REGEXP_TYPE.isBooleanValueType());
    assertFalse(REGEXP_TYPE.isDateType());
    assertFalse(REGEXP_TYPE.isEnumElementType());
    assertFalse(REGEXP_TYPE.isNamedType());
    assertFalse(REGEXP_TYPE.isNullType());
    assertFalse(REGEXP_TYPE.isNumberValueType());
    assertFalse(REGEXP_TYPE.isFunctionPrototypeType());
    assertTrue(REGEXP_TYPE.getImplicitPrototype().isFunctionPrototypeType());
    assertTrue(REGEXP_TYPE.isRegexpType());
    assertFalse(REGEXP_TYPE.isStringValueType());
    assertFalse(REGEXP_TYPE.isEnumType());
    assertFalse(REGEXP_TYPE.isUnionType());
    assertFalse(REGEXP_TYPE.isAllType());
    assertFalse(REGEXP_TYPE.isVoidType());

    
    assertNull(REGEXP_TYPE.autoboxesTo());

    
    assertFalse(REGEXP_TYPE.canAssignTo(NO_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(NO_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(ARRAY_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(BOOLEAN_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(BOOLEAN_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(DATE_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(EVAL_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(functionType));
    assertFalse(REGEXP_TYPE.canAssignTo(NULL_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(NUMBER_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(NUMBER_OBJECT_TYPE));
    assertTrue(REGEXP_TYPE.canAssignTo(OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(URI_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(RANGE_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(REFERENCE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canAssignTo(REGEXP_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(STRING_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(STRING_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(SYNTAX_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(TYPE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canAssignTo(ALL_TYPE));
    assertFalse(REGEXP_TYPE.canAssignTo(VOID_TYPE));

    
    assertTrue(REGEXP_TYPE.canBeCalled());

    
    assertTrue(REGEXP_TYPE.canTestForEqualityWith(ALL_TYPE));
    assertTrue(REGEXP_TYPE.canTestForEqualityWith(STRING_OBJECT_TYPE));
    assertTrue(REGEXP_TYPE.canTestForEqualityWith(NUMBER_TYPE));
    assertTrue(REGEXP_TYPE.canTestForEqualityWith(functionType));
    assertFalse(REGEXP_TYPE.canTestForEqualityWith(VOID_TYPE));
    assertTrue(REGEXP_TYPE.canTestForEqualityWith(OBJECT_TYPE));
    assertTrue(REGEXP_TYPE.canTestForEqualityWith(DATE_TYPE));
    assertTrue(REGEXP_TYPE.canTestForEqualityWith(REGEXP_TYPE));
    assertTrue(REGEXP_TYPE.canTestForEqualityWith(ARRAY_TYPE));

    
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(NO_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(NO_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(ARRAY_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(BOOLEAN_TYPE));
    assertFalse(REGEXP_TYPE.
        canTestForShallowEqualityWith(BOOLEAN_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(DATE_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(EVAL_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(functionType));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(NULL_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(NUMBER_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(NUMBER_OBJECT_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(URI_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(RANGE_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.
        canTestForShallowEqualityWith(REFERENCE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(REGEXP_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(STRING_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(STRING_OBJECT_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(SYNTAX_ERROR_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(TYPE_ERROR_TYPE));
    assertTrue(REGEXP_TYPE.canTestForShallowEqualityWith(ALL_TYPE));
    assertFalse(REGEXP_TYPE.canTestForShallowEqualityWith(VOID_TYPE));

    
    assertFalse(REGEXP_TYPE.isNullable());
    assertTrue(createNullableType(REGEXP_TYPE).isNullable());

    
    assertEquals(ALL_TYPE,
        REGEXP_TYPE.getLeastSupertype(ALL_TYPE));
    assertEquals(createUnionType(REGEXP_TYPE, STRING_OBJECT_TYPE),
        REGEXP_TYPE.getLeastSupertype(STRING_OBJECT_TYPE));
    assertEquals(createUnionType(REGEXP_TYPE, NUMBER_TYPE),
        REGEXP_TYPE.getLeastSupertype(NUMBER_TYPE));
    assertEquals(createUnionType(REGEXP_TYPE, functionType),
        REGEXP_TYPE.getLeastSupertype(functionType));
    assertEquals(OBJECT_TYPE, REGEXP_TYPE.getLeastSupertype(OBJECT_TYPE));
    assertEquals(createUnionType(DATE_TYPE, REGEXP_TYPE),
        REGEXP_TYPE.getLeastSupertype(DATE_TYPE));
    assertEquals(REGEXP_TYPE,
        REGEXP_TYPE.getLeastSupertype(REGEXP_TYPE));

    
    assertEquals(9, REGEXP_TYPE.getImplicitPrototype().getPropertiesCount());
    assertEquals(14, REGEXP_TYPE.getPropertiesCount());
    assertReturnTypeEquals(REGEXP_TYPE,
        REGEXP_TYPE.getPropertyType("constructor"));
    assertReturnTypeEquals(createNullableType(ARRAY_TYPE),
        REGEXP_TYPE.getPropertyType("exec"));
    assertReturnTypeEquals(BOOLEAN_TYPE,
        REGEXP_TYPE.getPropertyType("test"));
    assertReturnTypeEquals(STRING_TYPE,
        REGEXP_TYPE.getPropertyType("toString"));
    assertEquals(STRING_TYPE, REGEXP_TYPE.getPropertyType("source"));
    assertEquals(BOOLEAN_TYPE, REGEXP_TYPE.getPropertyType("global"));
    assertEquals(BOOLEAN_TYPE, REGEXP_TYPE.getPropertyType("ignoreCase"));
    assertEquals(BOOLEAN_TYPE, REGEXP_TYPE.getPropertyType("multiline"));
    assertEquals(NUMBER_TYPE, REGEXP_TYPE.getPropertyType("lastIndex"));

    
    assertFalse(REGEXP_TYPE.matchesInt32Context());
    assertFalse(REGEXP_TYPE.matchesNumberContext());
    assertTrue(REGEXP_TYPE.matchesObjectContext());
    assertTrue(REGEXP_TYPE.matchesStringContext());
    assertFalse(REGEXP_TYPE.matchesUint32Context());

    
    assertEquals("RegExp", REGEXP_TYPE.toString());

    assertTrue(REGEXP_TYPE.isNativeObjectType());

    Asserts.assertResolvesToSame(REGEXP_TYPE);
  }
