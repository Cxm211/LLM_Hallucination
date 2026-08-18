// buggy code
  JSType resolveInternal(ErrorReporter t, StaticScope<JSType> scope) {
    setResolvedTypeInternal(this);

    call = (ArrowType) safeResolve(call, t, scope);
    prototype = (FunctionPrototypeType) safeResolve(prototype, t, scope);

    // Warning about typeOfThis if it doesn't resolve to an ObjectType
    // is handled further upstream.
    // TODO(nicksantos): Handle this correctly if we have a UnionType.
    typeOfThis = (ObjectType) safeResolve(typeOfThis, t, scope);

    boolean changed = false;
    ImmutableList.Builder<ObjectType> resolvedInterfaces =
        ImmutableList.builder();
    for (ObjectType iface : implementedInterfaces) {
      ObjectType resolvedIface = (ObjectType) iface.resolve(t, scope);
      resolvedInterfaces.add(resolvedIface);
      changed |= (resolvedIface != iface);
    }
    if (changed) {
      implementedInterfaces = resolvedInterfaces.build();
    }

    if (subTypes != null) {
      for (int i = 0; i < subTypes.size(); i++) {
        subTypes.set(i, (FunctionType) subTypes.get(i).resolve(t, scope));
      }
    }

    return super.resolveInternal(t, scope);
  }

// relevant test
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
        "type not recognized due to syntax error");
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

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug1419535
  public void testBug1419535() throws Exception {
    parse("@type {function(Object, string, *)?} */");
    parse("@type {function(Object, string, *)|null} */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError1
  public void testParseFunctionalTypeError1() throws Exception {
    parse("@type {function number):string}*/", "missing opening (");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError2
  public void testParseFunctionalTypeError2() throws Exception {
    parse("@type {function( number}*/", "missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError3
  public void testParseFunctionalTypeError3() throws Exception {
    parse("@type {function(...[number], string)}*/",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError4
  public void testParseFunctionalTypeError4() throws Exception {
    parse("@type {function(string, ...[number], boolean):string}*/",
        "variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError5
  public void testParseFunctionalTypeError5() throws Exception {
    parse("@type {function (thi:Array)}*/", "missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError6
  public void testParseFunctionalTypeError6() throws Exception {
    resolve(parse("@type {function (this:number)}*/").getType(),
        "this type must be an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError7
  public void testParseFunctionalTypeError7() throws Exception {
    parse("@type {function(...[number)}*/", "missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError8
  public void testParseFunctionalTypeError8() throws Exception {
    parse("@type {function(...number])}*/", "missing opening [");
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

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError1
  public void testParseArrayTypeError1() throws Exception {
    parse("@type {[number}*/", "missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError2
  public void testParseArrayTypeError2() throws Exception {
    parse("@type {number]}*/", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError3
  public void testParseArrayTypeError3() throws Exception {
    parse("@type {[(number,boolean,Object?])]}*/", "missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError4
  public void testParseArrayTypeError4() throws Exception {
    parse("@type {(number,boolean,[Object?)]}*/",
        "missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers1
  public void testParseNullableModifiers1() throws Exception {
    JSDocInfo info = parse("@type {string?}*/");
    assertTypeEquals(createNullableType(STRING_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers2
  public void testParseNullableModifiers2() throws Exception {
    JSDocInfo info = parse("@type {!Array.<string?>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers3
  public void testParseNullableModifiers3() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>?}*/");
    assertTypeEquals(createNullableType(ARRAY_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers4
  public void testParseNullableModifiers4() throws Exception {
    JSDocInfo info = parse("@type {(string,boolean)?}*/");
    assertTypeEquals(
        createNullableType(createUnionType(STRING_TYPE, BOOLEAN_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers5
  public void testParseNullableModifiers5() throws Exception {
    JSDocInfo info = parse("@type {(string?,boolean)}*/");
    assertTypeEquals(
        createUnionType(createNullableType(STRING_TYPE), BOOLEAN_TYPE),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers6
  public void testParseNullableModifiers6() throws Exception {
    JSDocInfo info = parse("@type {(string,boolean?)}*/");
    assertTypeEquals(
        createUnionType(STRING_TYPE, createNullableType(BOOLEAN_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers7
  public void testParseNullableModifiers7() throws Exception {
    JSDocInfo info = parse("@type {string?|boolean}*/");
    assertTypeEquals(
        createUnionType(createNullableType(STRING_TYPE), BOOLEAN_TYPE),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers8
  public void testParseNullableModifiers8() throws Exception {
    JSDocInfo info = parse("@type {string|boolean?}*/");
    assertTypeEquals(
        createUnionType(STRING_TYPE, createNullableType(BOOLEAN_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullableModifiers9
  public void testParseNullableModifiers9() throws Exception {
    JSDocInfo info = parse("@type {foo.Hello.World?}*/");
    assertTypeEquals(
        createNullableType(
            registry.createNamedType(
                "foo.Hello.World", null, -1, -1)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseOptionalModifier
  public void testParseOptionalModifier() throws Exception {
    JSDocInfo info = parse("@type {function(number=)}*/");
    assertTypeEquals(
        registry.createFunctionType(
            UNKNOWN_TYPE, createUnionType(VOID_TYPE, NUMBER_TYPE)),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline1
  public void testParseNewline1() throws Exception {
    JSDocInfo info = parse("@type {string\n* }\n*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline2
  public void testParseNewline2() throws Exception {
    JSDocInfo info = parse("@type !Array.<\n* number\n* > */");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline3
  public void testParseNewline3() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number,\n* null)>*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline4
  public void testParseNewline4() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number|\n* null)>*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNewline5
  public void testParseNewline5() throws Exception {
    JSDocInfo info = parse("@type !Array.<function(\n* )\n* :\n* Date>*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseReturnType1
  public void testParseReturnType1() throws Exception {
    JSDocInfo info =
        parse("@return {null|string|Array.<boolean>}*/");
    assertTypeEquals(
        createUnionType(ARRAY_TYPE, NULL_TYPE, STRING_TYPE),
        info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseReturnType2
  public void testParseReturnType2() throws Exception {
    JSDocInfo info =
        parse("@returns {null|(string,Array.<boolean>)}*/");
    assertTypeEquals(
        createUnionType(ARRAY_TYPE, NULL_TYPE, STRING_TYPE),
        info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseReturnType3
  public void testParseReturnType3() throws Exception {
    JSDocInfo info =
        parse("@return {((null||Array.<boolean>,string),boolean)}*/");
    assertTypeEquals(
        createUnionType(ARRAY_TYPE, NULL_TYPE, STRING_TYPE, BOOLEAN_TYPE),
        info.getReturnType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType1
  public void testParseThisType1() throws Exception {
    JSDocInfo info =
        parse("@this {goog.foo.Bar}*/");
    assertTypeEquals(
        registry.createNamedType("goog.foo.Bar", null, -1, -1),
        info.getThisType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType2
  public void testParseThisType2() throws Exception {
    JSDocInfo info =
        parse("@this goog.foo.Bar*/");
    assertTypeEquals(
        registry.createNamedType("goog.foo.Bar", null, -1, -1),
        info.getThisType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType3
  public void testParseThisType3() throws Exception {
    parse("@type {number}\n@this goog.foo.Bar*/",
        "type annotation incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType4
  public void testParseThisType4() throws Exception {
    resolve(parse("@this number*/").getThisType(),
        "@this must specify an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType5
  public void testParseThisType5() throws Exception {
    parse("@this {Date|Error}*/");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThisType6
  public void testParseThisType6() throws Exception {
    resolve(parse("@this {Date|number}*/").getThisType(),
        "@this must specify an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam1
  public void testParseParam1() throws Exception {
    JSDocInfo info = parse("@param {number} index*/");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam2
  public void testParseParam2() throws Exception {
    JSDocInfo info = parse("@param index*/");
    assertEquals(1, info.getParameterCount());
    assertEquals(null, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam3
  public void testParseParam3() throws Exception {
    JSDocInfo info = parse("@param {number} index useful comments*/");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam4
  public void testParseParam4() throws Exception {
    JSDocInfo info = parse("@param index useful comments*/");
    assertEquals(1, info.getParameterCount());
    assertEquals(null, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam5
  public void testParseParam5() throws Exception {
    
    JSDocInfo info = parse("@param {number} \n index */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam6
  public void testParseParam6() throws Exception {
    
    JSDocInfo info = parse("@param {number} \n * index */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(NUMBER_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam7
  public void testParseParam7() throws Exception {
    
    JSDocInfo info = parse("@param {number=} index */");
    assertTypeEquals(
        registry.createOptionalType(NUMBER_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam8
  public void testParseParam8() throws Exception {
    
    JSDocInfo info = parse("@param {...number} index */");
    assertTypeEquals(
        registry.createOptionalType(NUMBER_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam9
  public void testParseParam9() throws Exception {
    parse("@param {...number=} index */",
        "expected closing }", "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam10
  public void testParseParam10() throws Exception {
    parse("@param {...number index */", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam11
  public void testParseParam11() throws Exception {
    parse("@param {number= index */", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam12
  public void testParseParam12() throws Exception {
    JSDocInfo info = parse("@param {...number|string} index */");
    assertTypeEquals(
        registry.createOptionalType(
            registry.createUnionType(STRING_TYPE, NUMBER_TYPE)),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam13
  public void testParseParam13() throws Exception {
    JSDocInfo info = parse("@param {...(number|string)} index */");
    assertTypeEquals(
        registry.createOptionalType(
            registry.createUnionType(STRING_TYPE, NUMBER_TYPE)),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam14
  public void testParseParam14() throws Exception {
    JSDocInfo info = parse("@param {string} [index] */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(STRING_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam15
  public void testParseParam15() throws Exception {
    JSDocInfo info = parse("@param {string} [index */", "missing closing ]");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(STRING_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam16
  public void testParseParam16() throws Exception {
    JSDocInfo info = parse("@param {string} index] */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(STRING_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam17
  public void testParseParam17() throws Exception {
    JSDocInfo info = parse("@param {string=} [index] */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(STRING_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam18
  public void testParseParam18() throws Exception {
    JSDocInfo info = parse("@param {...string} [index] */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(STRING_TYPE),
        info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam19
  public void testParseParam19() throws Exception {
    JSDocInfo info = parse("@param {...} [index] */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        registry.createOptionalType(UNKNOWN_TYPE),
        info.getParameterType("index"));
    assertTrue(info.getParameterType("index").isVarArgs());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam20
  public void testParseParam20() throws Exception {
    JSDocInfo info = parse("@param {?=} index */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        UNKNOWN_TYPE, info.getParameterType("index"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam21
  public void testParseParam21() throws Exception {
    JSDocInfo info = parse("@param {...?} index */");
    assertEquals(1, info.getParameterCount());
    assertTypeEquals(
        UNKNOWN_TYPE, info.getParameterType("index"));
    assertTrue(info.getParameterType("index").isVarArgs());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThrows1
  public void testParseThrows1() throws Exception {
    JSDocInfo info = parse("@throws {number} Some number */");
    assertEquals(1, info.getThrownTypes().size());
    assertTypeEquals(NUMBER_TYPE, info.getThrownTypes().get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseThrows2
  public void testParseThrows2() throws Exception {
    JSDocInfo info = parse("@throws {number} Some number\n "
                           + "*@throws {String} A string */");
    assertEquals(2, info.getThrownTypes().size());
    assertTypeEquals(NUMBER_TYPE, info.getThrownTypes().get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType1
  public void testParseRecordType1() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType2
  public void testParseRecordType2() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType3
  public void testParseRecordType3() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType4
  public void testParseRecordType4() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType5
  public void testParseRecordType5() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType6
  public void testParseRecordType6() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType7
  public void testParseRecordType7() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType8
  public void testParseRecordType8() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType9
  public void testParseRecordType9() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType10
  public void testParseRecordType10() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType11
  public void testParseRecordType11() throws Exception {
    parseFull("",
              "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType12
  public void testParseRecordType12() throws Exception {
    parseFull("",
              "type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType13
  public void testParseRecordType13() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType14
  public void testParseRecordType14() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType15
  public void testParseRecordType15() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType16
  public void testParseRecordType16() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType17
  public void testParseRecordType17() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType18
  public void testParseRecordType18() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType19
  public void testParseRecordType19() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType20
  public void testParseRecordType20() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType21
  public void testParseRecordType21() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType22
  public void testParseRecordType22() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType23
  public void testParseRecordType23() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError1
  public void testParseParamError1() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError2
  public void testParseParamError2() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError3
  public void testParseParamError3() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError4
  public void testParseParamError4() throws Exception {
    parseFull("",
        "expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError5
  public void testParseParamError5() throws Exception {
    parse("@param {number} x \n * @param {string} x */",
        "duplicate variable name \"x\"");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends1
  public void testParseExtends1() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE,
                     parse("@extends String*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends2
  public void testParseExtends2() throws Exception {
    JSDocInfo info = parse("@extends com.google.Foo.Bar.Hello.World*/");
    assertTypeEquals(
        registry.createNamedType(
            "com.google.Foo.Bar.Hello.World", null, -1, -1),
        info.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsGenerics
  public void testParseExtendsGenerics() throws Exception {
    JSDocInfo info =
        parse("@extends com.google.Foo.Bar.Hello.World.<Boolean,number>*/");
    assertTypeEquals(
        registry.createNamedType(
            "com.google.Foo.Bar.Hello.World", null, -1, -1),
        info.getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsGenerics
  public void testParseImplementsGenerics() throws Exception {
    
    List<JSTypeExpression> interfaces =
        parse("@implements {SomeInterface.<*>} */")
        .getImplementedInterfaces();
    assertEquals(1, interfaces.size());
    assertTypeEquals(registry.createNamedType("SomeInterface", null, -1, -1),
        interfaces.get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends4
  public void testParseExtends4() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends {String}*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends5
  public void testParseExtends5() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends {String*/", "expected closing }").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtends6
  public void testParseExtends6() throws Exception {
    
    assertTypeEquals(STRING_OBJECT_TYPE,
        parse("@extends \n * {String}*/").getBaseType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsInvalidName
  public void testParseExtendsInvalidName() throws Exception {
    
    
    
    
    assertTypeEquals(
        registry.createNamedType("some_++#%$%_UglyString", null, -1, -1),
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

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseMeaning1
  public void testParseMeaning1() throws Exception {
    assertEquals("tigers",
        parse("@meaning tigers   */").getMeaning());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseMeaning2
  public void testParseMeaning2() throws Exception {
    assertEquals("tigers and lions and bears",
        parse("@meaning tigers\n * and lions\n * and bears */").getMeaning());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseMeaning3
  public void testParseMeaning3() throws Exception {
    JSDocInfo info =
        parse("@meaning  tigers\n * and lions\n * @desc  and bears */");
    assertEquals("tigers and lions", info.getMeaning());
    assertEquals("and bears", info.getDescription());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseMeaning4
  public void testParseMeaning4() throws Exception {
    parse("@meaning  tigers\n * @meaning and lions  */",
        "extra @meaning tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends1
  public void testParseLends1() throws Exception {
    JSDocInfo info = parse("@lends {name} */");
    assertEquals("name", info.getLendsName());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends2
  public void testParseLends2() throws Exception {
    JSDocInfo info = parse("@lends   foo.bar  */");
    assertEquals("foo.bar", info.getLendsName());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends3
  public void testParseLends3() throws Exception {
    parse("@lends {name */", "expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends4
  public void testParseLends4() throws Exception {
    parse("@lends {} */", "missing object name in @lends tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends5
  public void testParseLends5() throws Exception {
    parse("@lends } */", "missing object name in @lends tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends6
  public void testParseLends6() throws Exception {
    parse("@lends {string} \n * @lends {string} */",
        "@lends tag incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends7
  public void testParseLends7() throws Exception {
    parse("@type {string} \n * @lends {string} */",
        "@lends tag incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParsePreserve
  public void testParsePreserve() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@preserve Foo\nBar\n\nBaz*/";
    parse(comment);
    assertEquals(" Foo\n Bar\n\n Baz", node.getJSDocInfo().getLicense());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLicense
  public void testParseLicense() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@license Foo\nBar\n\nBaz*/";
    parse(comment);
    assertEquals(" Foo\n Bar\n\n Baz", node.getJSDocInfo().getLicense());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLicenseWithAnnotation
  public void testParseLicenseWithAnnotation() throws Exception {
    Node node = new Node(1);
    this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
    String comment = "@license Foo \n * @author Charlie Brown */";
    parse(comment);
    assertEquals(" Foo \n @author Charlie Brown ",
        node.getJSDocInfo().getLicense());
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

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoCompile1
  public void testParseNoCompile1() throws Exception {
    assertTrue(parseFileOverview("@nocompile*/").isNoCompile());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNoCompile2
  public void testParseNoCompile2() throws Exception {
    parseFileOverview("@nocompile\n@nocompile*/", "extra @nocompile tag");
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

    assertTypeEquals(registry.createNamedType("FooBar", null, 0, 0),
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
    assertTypeEquals(registry.createNamedType("SomeInterface", null, -1, -1),
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
    assertTypeEquals(registry.createNamedType("SomeInterface1", null, -1, -1),
        interfaces.get(0));
    assertTypeEquals(registry.createNamedType("SomeInterface2", null, -1, -1),
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
    assertTypeEquals(registry.createNamedType("Extended", null, -1, -1),
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
    assertTypeEquals(registry.createNamedType("Disposable", null, -1, -1),
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

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadSuppress7
  public void testBadSuppress7() throws Exception {
    parse("@suppress {impossible} */",
          "unknown @suppress parameter: impossible");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies1
  public void testModifies1() throws Exception {
    JSDocInfo info = parse("@modifies {this} */");
    assertEquals(Sets.newHashSet("this"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies2
  public void testModifies2() throws Exception {
    JSDocInfo info = parse("@modifies {arguments} */");
    assertEquals(Sets.newHashSet("arguments"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies3
  public void testModifies3() throws Exception {
    JSDocInfo info = parse("@modifies {this|arguments} */");
    assertEquals(Sets.newHashSet("this", "arguments"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies4
  public void testModifies4() throws Exception {
    JSDocInfo info = parse("@param {*} x\n * @modifies {x} */");
    assertEquals(Sets.newHashSet("x"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies5
  public void testModifies5() throws Exception {
    JSDocInfo info = parse(
        "@param {*} x\n"
        + " * @param {*} y\n"
        + " * @modifies {x} */");
    assertEquals(Sets.newHashSet("x"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testModifies6
  public void testModifies6() throws Exception {
    JSDocInfo info = parse(
        "@param {*} x\n"
        + " * @param {*} y\n"
        + " * @modifies {x|y} */");
    assertEquals(Sets.newHashSet("x", "y"), info.getModifies());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies1
  public void testBadModifies1() throws Exception {
    parse("@modifies {} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies2
  public void testBadModifies2() throws Exception {
    parse("@modifies {this|} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies3
  public void testBadModifies3() throws Exception {
    parse("@modifies {|this} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies4
  public void testBadModifies4() throws Exception {
    parse("@modifies {this|arguments */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies5
  public void testBadModifies5() throws Exception {
    parse("@modifies {this,arguments} */", "malformed @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies6
  public void testBadModifies6() throws Exception {
    parse("@modifies {this} \n * @modifies {this} */", 
        "conflicting @modifies tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies7
  public void testBadModifies7() throws Exception {
    parse("@modifies {impossible} */",
          "unknown @modifies parameter: impossible");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies8
  public void testBadModifies8() throws Exception {
    parse("@modifies {this}\n"
        + "@nosideeffects */", "conflicting @nosideeffects tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadModifies9
  public void testBadModifies9() throws Exception {
    parse("@nosideeffects\n"
        + "@modifies {this} */", "conflicting @modifies tag");
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
