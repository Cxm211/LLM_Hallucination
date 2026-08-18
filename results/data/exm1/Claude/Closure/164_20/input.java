// buggy code
  public boolean isSubtype(JSType other) {
    if (!(other instanceof ArrowType)) {
      return false;
    }

    ArrowType that = (ArrowType) other;

    // This is described in Draft 2 of the ES4 spec,
    // Section 3.4.7: Subtyping Function Types.

    // this.returnType <: that.returnType (covariant)
    if (!this.returnType.isSubtype(that.returnType)) {
      return false;
    }

    // that.paramType[i] <: this.paramType[i] (contravariant)
    //
    // If this.paramType[i] is required,
    // then that.paramType[i] is required.
    //
    // In theory, the "required-ness" should work in the other direction as
    // well. In other words, if we have
    //
    // function f(number, number) {}
    // function g(number) {}
    //
    // Then f *should* not be a subtype of g, and g *should* not be
    // a subtype of f. But in practice, we do not implement it this way.
    // We want to support the use case where you can pass g where f is
    // expected, and pretend that g ignores the second argument.
    // That way, you can have a single "no-op" function, and you don't have
    // to create a new no-op function for every possible type signature.
    //
    // So, in this case, g < f, but f !< g
    Node thisParam = parameters.getFirstChild();
    Node thatParam = that.parameters.getFirstChild();
    while (thisParam != null && thatParam != null) {
      JSType thisParamType = thisParam.getJSType();
      JSType thatParamType = thatParam.getJSType();
      if (thisParamType != null) {
        if (thatParamType == null ||
            !thatParamType.isSubtype(thisParamType)) {
          return false;
        }
      }

      boolean thisIsVarArgs = thisParam.isVarArgs();
      boolean thatIsVarArgs = thatParam.isVarArgs();

      // "that" can't be a supertype, because it's missing a required argument.
        // NOTE(nicksantos): In our type system, we use {function(...?)} and
        // {function(...NoType)} to to indicate that arity should not be
        // checked. Strictly speaking, this is not a correct formulation,
        // because now a sub-function can required arguments that are var_args
        // in the super-function. So we special-case this.

      // don't advance if we have variable arguments
      if (!thisIsVarArgs) {
        thisParam = thisParam.getNext();
      }
      if (!thatIsVarArgs) {
        thatParam = thatParam.getNext();
      }

      // both var_args indicates the end
      if (thisIsVarArgs && thatIsVarArgs) {
        thisParam = null;
        thatParam = null;
      }
    }

    // "that" can't be a supertype, because it's missing a required arguement.

    return true;
  }

// relevant test
// com.google.javascript.jscomp.parsing.IRFactoryTest::testAssignmentValidation
  public void testAssignmentValidation() {
    testNoParseError("x=1");
    testNoParseError("x.y=1");
    testNoParseError("f().y=1");
    testParseError("(x||y)=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("(x?y:z)=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("f()=1", INVALID_ASSIGNMENT_TARGET);

    testNoParseError("x+=1");
    testNoParseError("x.y+=1");
    testNoParseError("f().y+=1");
    testParseError("(x||y)+=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("(x?y:z)+=1", INVALID_ASSIGNMENT_TARGET);
    testParseError("f()+=1", INVALID_ASSIGNMENT_TARGET);

    testParseError("f()++", INVALID_INCREMENT_TARGET);
    testParseError("f()--", INVALID_DECREMENT_TARGET);
    testParseError("++f()", INVALID_INCREMENT_TARGET);
    testParseError("--f()", INVALID_DECREMENT_TARGET);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic1
  public void testParseTypeViaStatic1() throws Exception {
    Node typeNode = parseType("null");
    assertTypeEquals(NULL_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic2
  public void testParseTypeViaStatic2() throws Exception {
    Node typeNode = parseType("string");
    assertTypeEquals(STRING_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic3
  public void testParseTypeViaStatic3() throws Exception {
    Node typeNode = parseType("!Date");
    assertTypeEquals(DATE_TYPE, typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseTypeViaStatic4
  public void testParseTypeViaStatic4() throws Exception {
    Node typeNode = parseType("boolean|string");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, STRING_TYPE), typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInvalidTypeViaStatic
  public void testParseInvalidTypeViaStatic() throws Exception {
    Node typeNode = parseType("sometype.<anothertype");
    assertNull(typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInvalidTypeViaStatic2
  public void testParseInvalidTypeViaStatic2() throws Exception {
    Node typeNode = parseType("");
    assertNull(typeNode);
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType1
  public void testParseNamedType1() throws Exception {
    assertNull(parse("@type null", "Unexpected end of file"));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType2
  public void testParseNamedType2() throws Exception {
    JSDocInfo info = parse("@type null*/");
    assertTypeEquals(NULL_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType3
  public void testParseNamedType3() throws Exception {
    JSDocInfo info = parse("@type {string}*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType4
  public void testParseNamedType4() throws Exception {
    
    JSDocInfo info = parse("@type \n {string}*/");
    assertTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType5
  public void testParseNamedType5() throws Exception {
    JSDocInfo info = parse("@type {!goog.\nBar}*/");
    assertTypeEquals(
        registry.createNamedType("goog.Bar", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedType6
  public void testParseNamedType6() throws Exception {
    JSDocInfo info = parse("@type {!goog.\n * Bar.\n * Baz}*/");
    assertTypeEquals(
        registry.createNamedType("goog.Bar.Baz", null, -1, -1),
        info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedTypeError1
  public void testParseNamedTypeError1() throws Exception {
    
    
    parse("@type {!goog\n * .Bar} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNamedTypeError2
  public void testParseNamedTypeError2() throws Exception {
    parse("@type {!goog.\n * Bar\n * .Baz} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType1
  public void testTypedefType1() throws Exception {
    JSDocInfo info = parse("@typedef string */");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(STRING_TYPE, info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType2
  public void testTypedefType2() throws Exception {
    JSDocInfo info = parse("@typedef \n {string}*/");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(STRING_TYPE, info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testTypedefType3
  public void testTypedefType3() throws Exception {
    JSDocInfo info = parse("@typedef \n {(string|number)}*/");
    assertTrue(info.hasTypedefType());
    assertTypeEquals(
        createUnionType(NUMBER_TYPE, STRING_TYPE),
        info.getTypedefType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseStringType1
  public void testParseStringType1() throws Exception {
    assertTypeEquals(STRING_TYPE, parse("@type {string}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseStringType2
  public void testParseStringType2() throws Exception {
    assertTypeEquals(STRING_OBJECT_TYPE, parse("@type {!String}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBooleanType1
  public void testParseBooleanType1() throws Exception {
    assertTypeEquals(BOOLEAN_TYPE, parse("@type {boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseBooleanType2
  public void testParseBooleanType2() throws Exception {
    assertTypeEquals(
        BOOLEAN_OBJECT_TYPE, parse("@type {!Boolean}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNumberType1
  public void testParseNumberType1() throws Exception {
    assertTypeEquals(NUMBER_TYPE, parse("@type {number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNumberType2
  public void testParseNumberType2() throws Exception {
    assertTypeEquals(NUMBER_OBJECT_TYPE, parse("@type {!Number}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullType1
  public void testParseNullType1() throws Exception {
    assertTypeEquals(NULL_TYPE, parse("@type {null}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseNullType2
  public void testParseNullType2() throws Exception {
    assertTypeEquals(NULL_TYPE, parse("@type {Null}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseAllType1
  public void testParseAllType1() throws Exception {
    testParseType("*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseAllType2
  public void testParseAllType2() throws Exception {
    testParseType("*?", "*");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseObjectType
  public void testParseObjectType() throws Exception {
    assertTypeEquals(OBJECT_TYPE, parse("@type {!Object}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseDateType
  public void testParseDateType() throws Exception {
    assertTypeEquals(DATE_TYPE, parse("@type {!Date}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionType
  public void testParseFunctionType() throws Exception {
    assertTypeEquals(
        createNullableType(U2U_CONSTRUCTOR_TYPE),
        parse("@type {Function}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRegExpType
  public void testParseRegExpType() throws Exception {
    assertTypeEquals(REGEXP_TYPE, parse("@type {!RegExp}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseErrorTypes
  public void testParseErrorTypes() throws Exception {
    assertTypeEquals(ERROR_TYPE, parse("@type {!Error}*/").getType());
    assertTypeEquals(URI_ERROR_TYPE, parse("@type {!URIError}*/").getType());
    assertTypeEquals(EVAL_ERROR_TYPE, parse("@type {!EvalError}*/").getType());
    assertTypeEquals(REFERENCE_ERROR_TYPE,
        parse("@type {!ReferenceError}*/").getType());
    assertTypeEquals(TYPE_ERROR_TYPE, parse("@type {!TypeError}*/").getType());
    assertTypeEquals(
        RANGE_ERROR_TYPE, parse("@type {!RangeError}*/").getType());
    assertTypeEquals(
        SYNTAX_ERROR_TYPE, parse("@type {!SyntaxError}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType1
  public void testParseUndefinedType1() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {undefined}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType2
  public void testParseUndefinedType2() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {Undefined}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUndefinedType3
  public void testParseUndefinedType3() throws Exception {
    assertTypeEquals(VOID_TYPE, parse("@type {void}*/").getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType1
  public void testParseParametrizedType1() throws Exception {
    JSDocInfo info = parse("@type !Array.<number> */");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType2
  public void testParseParametrizedType2() throws Exception {
    JSDocInfo info = parse("@type {!Array.<number>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType3
  public void testParseParametrizedType3() throws Exception {
    JSDocInfo info = parse("@type !Array.<(number,null)>*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType4
  public void testParseParametrizedType4() throws Exception {
    JSDocInfo info = parse("@type {!Array.<(number|null)>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType5
  public void testParseParametrizedType5() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Array.<(number|null)>>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType6
  public void testParseParametrizedType6() throws Exception {
    JSDocInfo info = parse("@type {!Array.<!Array.<(number|null)>>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType7
  public void testParseParametrizedType7() throws Exception {
    JSDocInfo info = parse("@type {!Array.<function():Date>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType8
  public void testParseParametrizedType8() throws Exception {
    JSDocInfo info = parse("@type {!Array.<function():!Date>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType9
  public void testParseParametrizedType9() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Date|number>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParametrizedType10
  public void testParseParametrizedType10() throws Exception {
    JSDocInfo info = parse("@type {!Array.<Date|number|boolean>}*/");
    assertTypeEquals(ARRAY_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamterizedType11
  public void testParseParamterizedType11() throws Exception {
    JSDocInfo info = parse("@type {!Object.<number>}*/");
    assertTypeEquals(OBJECT_TYPE, info.getType());
    assertParameterTypeEquals(NUMBER_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamterizedType12
  public void testParseParamterizedType12() throws Exception {
    JSDocInfo info = parse("@type {!Object.<string,number>}*/");
    assertTypeEquals(OBJECT_TYPE, info.getType());
    assertParameterTypeEquals(NUMBER_TYPE, info.getType());
    assertIndexTypeEquals(STRING_TYPE, info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType1
  public void testParseUnionType1() throws Exception {
    JSDocInfo info = parse("@type {(boolean,null)}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType2
  public void testParseUnionType2() throws Exception {
    JSDocInfo info = parse("@type {boolean|null}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType3
  public void testParseUnionType3() throws Exception {
    JSDocInfo info = parse("@type {boolean||null}*/");
    assertTypeEquals(createUnionType(BOOLEAN_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType4
  public void testParseUnionType4() throws Exception {
    JSDocInfo info = parse("@type {(Array.<boolean>,null)}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType5
  public void testParseUnionType5() throws Exception {
    JSDocInfo info = parse("@type {(null, Array.<boolean>)}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType6
  public void testParseUnionType6() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>|null}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType7
  public void testParseUnionType7() throws Exception {
    JSDocInfo info = parse("@type {null|Array.<boolean>}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType8
  public void testParseUnionType8() throws Exception {
    JSDocInfo info = parse("@type {null||Array.<boolean>}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType9
  public void testParseUnionType9() throws Exception {
    JSDocInfo info = parse("@type {Array.<boolean>||null}*/");
    assertTypeEquals(createUnionType(ARRAY_TYPE, NULL_TYPE), info.getType());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType10
  public void testParseUnionType10() throws Exception {
    parse("@type {string|}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType11
  public void testParseUnionType11() throws Exception {
    parse("@type {(string,)}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType12
  public void testParseUnionType12() throws Exception {
    parse("@type {()}*/",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType13
  public void testParseUnionType13() throws Exception {
    testParseType(
        "(function(this:Date),function(this:String):number)",
        "Function");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseUnionType14
  public void testParseUnionType14() throws Exception {
    testParseType(
        "(function(...[function(number):boolean]):number)|" +
        "function(this:String, string):number",
        "Function");
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
        "Bad type annotation. type not recognized due to syntax error");
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

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testStructuralConstructor
  public void testStructuralConstructor() throws Exception {
    JSType type = testParseType(
        "function (new:Object)", "function (new:Object): ?");
    assertTrue(type.isConstructor());
    assertFalse(type.isNominalConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testNominalConstructor
  public void testNominalConstructor() throws Exception {
    ObjectType type = testParseType("Array", "(Array|null)").dereference();
    assertTrue(type.getConstructor().isNominalConstructor());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug1419535
  public void testBug1419535() throws Exception {
    parse("@type {function(Object, string, *)?} */");
    parse("@type {function(Object, string, *)|null} */");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testIssue477
  public void testIssue477() throws Exception {
    parse("@type function */",
        "Bad type annotation. missing opening (");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testMalformedThisAnnotation
  public void testMalformedThisAnnotation() throws Exception {
    parse("@this */",
        "Bad type annotation. type not recognized due to syntax error");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError1
  public void testParseFunctionalTypeError1() throws Exception {
    parse("@type {function number):string}*/",
        "Bad type annotation. missing opening (");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError2
  public void testParseFunctionalTypeError2() throws Exception {
    parse("@type {function( number}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError3
  public void testParseFunctionalTypeError3() throws Exception {
    parse("@type {function(...[number], string)}*/",
        "Bad type annotation. variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError4
  public void testParseFunctionalTypeError4() throws Exception {
    parse("@type {function(string, ...[number], boolean):string}*/",
        "Bad type annotation. variable length argument must be last");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError5
  public void testParseFunctionalTypeError5() throws Exception {
    parse("@type {function (thi:Array)}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError6
  public void testParseFunctionalTypeError6() throws Exception {
    resolve(parse("@type {function (this:number)}*/").getType(),
        "this type must be an object type");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError7
  public void testParseFunctionalTypeError7() throws Exception {
    parse("@type {function(...[number)}*/",
        "Bad type annotation. missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError8
  public void testParseFunctionalTypeError8() throws Exception {
    parse("@type {function(...number])}*/",
        "Bad type annotation. missing opening [");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError9
  public void testParseFunctionalTypeError9() throws Exception {
    parse("@type {function (new:Array, this:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError10
  public void testParseFunctionalTypeError10() throws Exception {
    parse("@type {function (this:Array, new:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError11
  public void testParseFunctionalTypeError11() throws Exception {
    parse("@type {function (Array, new:Object)} */",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseFunctionalTypeError12
  public void testParseFunctionalTypeError12() throws Exception {
    resolve(parse("@type {function (new:number)}*/").getType(),
        "constructed type must be an object type");
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
    parse("@type {[number}*/",
        "Bad type annotation. missing closing ]");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError2
  public void testParseArrayTypeError2() throws Exception {
    parse("@type {number]}*/",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError3
  public void testParseArrayTypeError3() throws Exception {
    parse("@type {[(number,boolean,Object?])]}*/",
        "Bad type annotation. missing closing )");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseArrayTypeError4
  public void testParseArrayTypeError4() throws Exception {
    parse("@type {(number,boolean,[Object?)]}*/",
        "Bad type annotation. missing closing ]");
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
        "Bad type annotation. type annotation incompatible " +
        "with other annotations");
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
        "Bad type annotation. expected closing }",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam10
  public void testParseParam10() throws Exception {
    parse("@param {...number index */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParam11
  public void testParseParam11() throws Exception {
    parse("@param {number= index */",
        "Bad type annotation. expected closing }");
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
    JSDocInfo info = parse("@param {string} [index */",
        "Bad type annotation. missing closing ]");
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
              "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseRecordType12
  public void testParseRecordType12() throws Exception {
    parseFull("",
              "Bad type annotation. type not recognized due to syntax error");
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
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError2
  public void testParseParamError2() throws Exception {
    parseFull("",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError3
  public void testParseParamError3() throws Exception {
    parseFull("",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError4
  public void testParseParamError4() throws Exception {
    parseFull("",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseParamError5
  public void testParseParamError5() throws Exception {
    parse("@param {number} x \n * @param {string} x */",
        "Bad type annotation. duplicate variable name \"x\"");
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
        parse("@extends {String*/",
              "Bad type annotation. expected closing }").getBaseType());
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
    parse("@extends {Base?} */", "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseExtendsNullable2
  public void testParseExtendsNullable2() throws Exception {
    parse("@extends Base? */",
        "Bad type annotation. expected end of line or comment");
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
    parse("@lends {name */", "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends4
  public void testParseLends4() throws Exception {
    parse("@lends {} */",
        "Bad type annotation. missing object name in @lends tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends5
  public void testParseLends5() throws Exception {
    parse("@lends } */",
        "Bad type annotation. missing object name in @lends tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends6
  public void testParseLends6() throws Exception {
    parse("@lends {string} \n * @lends {string} */",
        "Bad type annotation. @lends tag incompatible with other annotations");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseLends7
  public void testParseLends7() throws Exception {
    parse("@type {string} \n * @lends {string} */",
        "Bad type annotation. @lends tag incompatible with other annotations");
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
        parse("@define {string*/",
              "Bad type annotation. expected closing }").getType());
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
        "Bad type annotation. " +
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
        "Bad type annotation. " +
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
    parse("@override\n@override*/",
        "Bad type annotation. extra @override/@inheritDoc tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInheritDoc1
  public void testParseInheritDoc1() throws Exception {
    assertTrue(parse("@inheritDoc*/").isOverride());
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInheritDoc2
  public void testParseInheritDoc2() throws Exception {
    parse("@override\n@inheritDoc*/",
        "Bad type annotation. extra @override/@inheritDoc tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInheritDoc3
  public void testParseInheritDoc3() throws Exception {
    parse("@inheritDoc\n@inheritDoc*/",
        "Bad type annotation. extra @override/@inheritDoc tag");
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
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression2
  public void testFullRegression2() throws Exception {
    parseFull("function bar(foo){}",
        "Bad type annotation. expected closing }",
        "Bad type annotation. expecting a variable name in a @param tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testFullRegression3
  public void testFullRegression3() throws Exception {
    parseFull("");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug907488
  public void testBug907488() throws Exception {
    parse("@type {number,null} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug907494
  public void testBug907494() throws Exception {
    parse("@return {Object,undefined} */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBug909468
  public void testBug909468() throws Exception {
    parse("@extends {(x)}*/",
        "Bad type annotation. expecting a type name");
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
          "Bad type annotation. extra @implicitCast tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseInterfaceDoubled
  public void testParseInterfaceDoubled() throws Exception {
    parse(
        "* @interface\n" +
        "* @interface\n" +
        "*/",
        "Bad type annotation. " +
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
        "Bad type annotation. duplicate @implements tag");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNoName
  public void testParseImplementsNoName() throws Exception {
    parse("* @implements {} */",
        "Bad type annotation. expecting a type name");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsMissingRC
  public void testParseImplementsMissingRC() throws Exception {
    parse("* @implements {Smth */",
        "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNullable1
  public void testParseImplementsNullable1() throws Exception {
    parse("@implements {Base?} */", "Bad type annotation. expected closing }");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testParseImplementsNullable2
  public void testParseImplementsNullable2() throws Exception {
    parse("@implements Base? */",
        "Bad type annotation. expected end of line or comment");
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testInterfaceExtends
  public void testInterfaceExtends() throws Exception {
     JSDocInfo jsdoc = parse(
         " * @interface \n" +
         " * @extends {Extended} */");
    assertTrue(jsdoc.isInterface());
    assertEquals(1, jsdoc.getExtendedInterfacesCount());
    List<JSTypeExpression> types = jsdoc.getExtendedInterfaces();
    assertTypeEquals(registry.createNamedType("Extended", null, -1, -1),
        types.get(0));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testInterfaceMultiExtends1
  public void testInterfaceMultiExtends1() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @interface \n" +
        " * @extends {Extended1} \n" +
        " * @extends {Extended2} */");
    assertTrue(jsdoc.isInterface());
    assertNull(jsdoc.getBaseType());
    assertEquals(2, jsdoc.getExtendedInterfacesCount());
    List<JSTypeExpression> types = jsdoc.getExtendedInterfaces();
    assertTypeEquals(registry.createNamedType("Extended1", null, -1, -1),
       types.get(0));
    assertTypeEquals(registry.createNamedType("Extended2", null, -1, -1),
        types.get(1));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testInterfaceMultiExtends2
  public void testInterfaceMultiExtends2() throws Exception {
    JSDocInfo jsdoc = parse(
        " * @extends {Extended1} \n" +
        " * @interface \n" +
        " * @extends {Extended2} \n" +
        " * @extends {Extended3} */");
    assertTrue(jsdoc.isInterface());
    assertNull(jsdoc.getBaseType());
    assertEquals(3, jsdoc.getExtendedInterfacesCount());
    List<JSTypeExpression> types = jsdoc.getExtendedInterfaces();
    assertTypeEquals(registry.createNamedType("Extended1", null, -1, -1),
       types.get(0));
    assertTypeEquals(registry.createNamedType("Extended2", null, -1, -1),
        types.get(1));
    assertTypeEquals(registry.createNamedType("Extended3", null, -1, -1),
        types.get(2));
  }

// com.google.javascript.jscomp.parsing.JsDocInfoParserTest::testBadClassMultiExtends
  public void testBadClassMultiExtends() throws Exception {
    parse(" * @extends {Extended1} \n" +
        " * @constructor \n" +
        " * @extends {Extended2} */",
        "Bad type annotation. type annotation incompatible with other " +
        "annotations");
  }
