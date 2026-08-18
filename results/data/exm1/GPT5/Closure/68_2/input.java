// buggy code
  boolean parse() {
    int lineno;
    int charno;

    // JSTypes are represented as Rhino AST nodes, and then resolved later.
    JSTypeExpression type;

    state = State.SEARCHING_ANNOTATION;
    skipEOLs();

    JsDocToken token = next();

    List<ExtendedTypeInfo> extendedTypes = Lists.newArrayList();

    // Always record that we have a comment.
    if (jsdocBuilder.shouldParseDocumentation()) {
      ExtractionInfo blockInfo = extractBlockComment(token);
      token = blockInfo.token;
      if (!blockInfo.string.isEmpty()) {
        jsdocBuilder.recordBlockDescription(blockInfo.string);
      }
    } else {
      if (token != JsDocToken.ANNOTATION &&
          token != JsDocToken.EOC) {
        // Mark that there was a description, but don't bother marking
        // what it was.
        jsdocBuilder.recordBlockDescription("");
      }
    }

    // Parse the actual JsDoc.
    retry: for (;;) {
      switch (token) {
        case ANNOTATION:
          if (state == State.SEARCHING_ANNOTATION) {
            state = State.SEARCHING_NEWLINE;
            lineno = stream.getLineno();
            charno = stream.getCharno();

            String annotationName = stream.getString();
            Annotation annotation = annotationNames.get(annotationName);
            if (annotation == null) {
              parser.addParserWarning("msg.bad.jsdoc.tag", annotationName,
                  stream.getLineno(), stream.getCharno());
            } else {
              // Mark the beginning of the annotation.
              jsdocBuilder.markAnnotation(annotationName, lineno, charno);

              switch (annotation) {
                case AUTHOR:
                  if (jsdocBuilder.shouldParseDocumentation()) {
                    ExtractionInfo authorInfo = extractSingleLineBlock();
                    String author = authorInfo.string;

                    if (author.length() == 0) {
                      parser.addParserWarning("msg.jsdoc.authormissing",
                          stream.getLineno(), stream.getCharno());
                    } else {
                      jsdocBuilder.addAuthor(author);
                    }
                    token = authorInfo.token;
                  } else {
                    token = eatTokensUntilEOL(token);
                  }
                  continue retry;

                case CONSTANT:
                  if (!jsdocBuilder.recordConstancy()) {
                    parser.addParserWarning("msg.jsdoc.const",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case CONSTRUCTOR:
                  if (!jsdocBuilder.recordConstructor()) {
                    if (jsdocBuilder.isInterfaceRecorded()) {
                      parser.addTypeWarning("msg.jsdoc.interface.constructor",
                          stream.getLineno(), stream.getCharno());
                    } else {
                      parser.addTypeWarning("msg.jsdoc.incompat.type",
                          stream.getLineno(), stream.getCharno());
                    }
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case DEPRECATED:
                  if (!jsdocBuilder.recordDeprecated()) {
                    parser.addParserWarning("msg.jsdoc.deprecated",
                        stream.getLineno(), stream.getCharno());
                  }

                  // Find the reason/description, if any.
                  ExtractionInfo reasonInfo =
                      extractMultilineTextualBlock(token);

                  String reason = reasonInfo.string;

                  if (reason.length() > 0) {
                    jsdocBuilder.recordDeprecationReason(reason);
                  }

                  token = reasonInfo.token;
                  continue retry;

                case INTERFACE:
                  if (!jsdocBuilder.recordInterface()) {
                    if (jsdocBuilder.isConstructorRecorded()) {
                      parser.addTypeWarning("msg.jsdoc.interface.constructor",
                          stream.getLineno(), stream.getCharno());
                    } else {
                      parser.addTypeWarning("msg.jsdoc.incompat.type",
                          stream.getLineno(), stream.getCharno());
                    }
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case DESC:
                  if (jsdocBuilder.isDescriptionRecorded()) {
                    parser.addParserWarning("msg.jsdoc.desc.extra",
                        stream.getLineno(), stream.getCharno());
                    token = eatTokensUntilEOL();
                    continue retry;
                  } else {
                    ExtractionInfo descriptionInfo =
                        extractMultilineTextualBlock(token);

                    String description = descriptionInfo.string;

                    jsdocBuilder.recordDescription(description);
                    token = descriptionInfo.token;
                    continue retry;
                  }

                case FILE_OVERVIEW:
                  String fileOverview = "";
                  if (jsdocBuilder.shouldParseDocumentation()) {
                    ExtractionInfo fileOverviewInfo =
                        extractMultilineTextualBlock(token,
                            WhitespaceOption.TRIM);

                    fileOverview = fileOverviewInfo.string;

                    token = fileOverviewInfo.token;
                  } else {
                    token = eatTokensUntilEOL(token);
                  }

                  if (!jsdocBuilder.recordFileOverview(fileOverview) ||
                      fileOverviewJSDocInfo != null) {
                    parser.addParserWarning("msg.jsdoc.fileoverview.extra",
                        stream.getLineno(), stream.getCharno());
                  }
                  continue retry;

                case LICENSE:
                case PRESERVE:
                  ExtractionInfo preserveInfo =
                      extractMultilineTextualBlock(token,
                                                   WhitespaceOption.PRESERVE);

                  String preserve = preserveInfo.string;

                  if (preserve.length() > 0) {
                    if (fileLevelJsDocBuilder != null) {
                      fileLevelJsDocBuilder.append(preserve);
                    }
                  }

                  token = preserveInfo.token;
                  continue retry;

                case ENUM:
                  token = next();
                  lineno = stream.getLineno();
                  charno = stream.getCharno();

                  type = null;
                  if (token != JsDocToken.EOL && token != JsDocToken.EOC) {
                    type = createJSTypeExpression(
                        parseAndRecordTypeNode(token));
                  }

                  if (type == null) {
                    type = createJSTypeExpression(newStringNode("number"));
                  }
                  if (!jsdocBuilder.recordEnumParameterType(type)) {
                    parser.addTypeWarning(
                        "msg.jsdoc.incompat.type", lineno, charno);
                  }
                  token = eatTokensUntilEOL(token);
                  continue retry;

                case EXPORT:
                  if (!jsdocBuilder.recordExport()) {
                    parser.addParserWarning("msg.jsdoc.export",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case EXTERNS:
                  if (!jsdocBuilder.recordExterns()) {
                    parser.addParserWarning("msg.jsdoc.externs",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case JAVA_DISPATCH:
                  if (!jsdocBuilder.recordJavaDispatch()) {
                    parser.addParserWarning("msg.jsdoc.javadispatch",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case EXTENDS:
                case IMPLEMENTS:
                  skipEOLs();
                  token = next();
                  lineno = stream.getLineno();
                  charno = stream.getCharno();
                  boolean matchingRc = false;

                  if (token == JsDocToken.LC) {
                    token = next();
                    matchingRc = true;
                  }

                  if (token == JsDocToken.STRING) {
                    Node typeNode = parseAndRecordTypeNameNode(
                        token, lineno, charno, matchingRc);

                    lineno = stream.getLineno();
                    charno = stream.getCharno();

                    typeNode = wrapNode(Token.BANG, typeNode);
                    if (typeNode != null && !matchingRc) {
                      typeNode.putBooleanProp(Node.BRACELESS_TYPE, true);
                    }
                    type = createJSTypeExpression(typeNode);

                    if (annotation == Annotation.EXTENDS) {
                      // record the extended type, check later
                      extendedTypes.add(new ExtendedTypeInfo(
                          type, stream.getLineno(), stream.getCharno()));
                    } else {
                      Preconditions.checkState(
                          annotation == Annotation.IMPLEMENTS);
                      if (!jsdocBuilder.recordImplementedInterface(type)) {
                        parser.addTypeWarning("msg.jsdoc.implements.duplicate",
                            lineno, charno);
                      }
                    }
                    token = next();
                    if (matchingRc) {
                      if (token != JsDocToken.RC) {
                        parser.addTypeWarning("msg.jsdoc.missing.rc",
                            stream.getLineno(), stream.getCharno());
                      }
                    } else if (token != JsDocToken.EOL &&
                        token != JsDocToken.EOF && token != JsDocToken.EOC) {
                      parser.addTypeWarning("msg.end.annotation.expected",
                          stream.getLineno(), stream.getCharno());
                    }
                  } else {
                    parser.addTypeWarning("msg.no.type.name", lineno, charno);
                  }
                  token = eatTokensUntilEOL(token);
                  continue retry;

                case HIDDEN:
                  if (!jsdocBuilder.recordHiddenness()) {
                    parser.addParserWarning("msg.jsdoc.hidden",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case LENDS:
                  skipEOLs();

                  matchingRc = false;
                  if (match(JsDocToken.LC)) {
                    token = next();
                    matchingRc = true;
                  }

                  if (match(JsDocToken.STRING)) {
                    token = next();
                    if (!jsdocBuilder.recordLends(stream.getString())) {
                      parser.addTypeWarning("msg.jsdoc.lends.incompatible",
                          stream.getLineno(), stream.getCharno());
                    }
                  } else {
                    parser.addTypeWarning("msg.jsdoc.lends.missing",
                        stream.getLineno(), stream.getCharno());
                  }

                  if (matchingRc && !match(JsDocToken.RC)) {
                    parser.addTypeWarning("msg.jsdoc.missing.rc",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case MEANING:
                  ExtractionInfo meaningInfo =
                      extractMultilineTextualBlock(token);
                  String meaning = meaningInfo.string;
                  token = meaningInfo.token;
                  if (!jsdocBuilder.recordMeaning(meaning)) {
                    parser.addParserWarning("msg.jsdoc.meaning.extra",
                        stream.getLineno(), stream.getCharno());
                  }
                  continue retry;

                case NO_ALIAS:
                  if (!jsdocBuilder.recordNoAlias()) {
                    parser.addParserWarning("msg.jsdoc.noalias",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case NO_COMPILE:
                  if (!jsdocBuilder.recordNoCompile()) {
                    parser.addParserWarning("msg.jsdoc.nocompile",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case NO_TYPE_CHECK:
                  if (!jsdocBuilder.recordNoTypeCheck()) {
                    parser.addParserWarning("msg.jsdoc.nocheck",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case NOT_IMPLEMENTED:
                  token = eatTokensUntilEOL();
                  continue retry;

                case INHERIT_DOC:
                case OVERRIDE:
                  if (!jsdocBuilder.recordOverride()) {
                    parser.addTypeWarning("msg.jsdoc.override",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case THROWS:
                  skipEOLs();
                  token = next();
                  lineno = stream.getLineno();
                  charno = stream.getCharno();
                  type = null;

                  if (token == JsDocToken.LC) {
                    type = createJSTypeExpression(
                        parseAndRecordTypeNode(token));

                    if (type == null) {
                      // parsing error reported during recursive descent
                      // recovering parsing
                      token = eatTokensUntilEOL();
                      continue retry;
                    }
                  }

                  // *Update* the token to that after the type annotation.
                  token = current();

                  // Save the throw type.
                  jsdocBuilder.recordThrowType(type);

                  // Find the throw's description (if applicable).
                  if (jsdocBuilder.shouldParseDocumentation()) {
                    ExtractionInfo descriptionInfo =
                        extractMultilineTextualBlock(token);

                    String description = descriptionInfo.string;

                    if (description.length() > 0) {
                      jsdocBuilder.recordThrowDescription(type, description);
                    }

                    token = descriptionInfo.token;
                  } else {
                    token = eatTokensUntilEOL(token);
                  }
                  continue retry;

                case PARAM:
                  skipEOLs();
                  token = next();
                  lineno = stream.getLineno();
                  charno = stream.getCharno();
                  type = null;

                  if (token == JsDocToken.LC) {
                    type = createJSTypeExpression(
                        parseAndRecordParamTypeNode(token));

                    if (type == null) {
                      // parsing error reported during recursive descent
                      // recovering parsing
                      token = eatTokensUntilEOL();
                      continue retry;
                    }
                    skipEOLs();
                    token = next();
                    lineno = stream.getLineno();
                    charno = stream.getCharno();
                  }

                  String name = null;
                  boolean isBracketedParam = JsDocToken.LB == token;
                  if (isBracketedParam) {
                    token = next();
                  }

                  if (JsDocToken.STRING != token) {
                    parser.addTypeWarning("msg.missing.variable.name",
                        lineno, charno);
                  } else {
                    name = stream.getString();

                    if (isBracketedParam) {
                      token = next();

                      // Throw out JsDocToolkit's "default" parameter
                      // annotation.  It makes no sense under our type
                      // system.
                      if (JsDocToken.EQUALS == token) {
                        token = next();
                        if (JsDocToken.STRING == token) {
                          token = next();
                        }
                      }

                      if (JsDocToken.RB != token) {
                        reportTypeSyntaxWarning("msg.jsdoc.missing.rb");
                      } else if (type != null) {
                        // Make the type expression optional, if it isn't
                        // already.
                        type = JSTypeExpression.makeOptionalArg(type);
                      }
                    }

                    // If the param name has a DOT in it, just throw it out
                    // quietly. We do not handle the JsDocToolkit method
                    // for handling properties of params.
                    if (name.indexOf('.') > -1) {
                      name = null;
                    } else if (!jsdocBuilder.recordParameter(name, type)) {
                      if (jsdocBuilder.hasParameter(name)) {
                        parser.addTypeWarning("msg.dup.variable.name", name,
                            lineno, charno);
                      } else {
                        parser.addTypeWarning("msg.jsdoc.incompat.type", name,
                            lineno, charno);
                      }
                    }
                  }

                  if (name == null) {
                    token = eatTokensUntilEOL(token);
                    continue retry;
                  }

                  jsdocBuilder.markName(name, lineno, charno);

                  // Find the parameter's description (if applicable).
                  if (jsdocBuilder.shouldParseDocumentation()) {
                    ExtractionInfo paramDescriptionInfo =
                        extractMultilineTextualBlock(token);

                    String paramDescription = paramDescriptionInfo.string;

                    if (paramDescription.length() > 0) {
                      jsdocBuilder.recordParameterDescription(name,
                          paramDescription);
                    }

                    token = paramDescriptionInfo.token;
                  } else {
                    token = eatTokensUntilEOL(token);
                  }
                  continue retry;

                case PRESERVE_TRY:
                  if (!jsdocBuilder.recordPreserveTry()) {
                    parser.addParserWarning("msg.jsdoc.preservertry",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case PRIVATE:
                  if (!jsdocBuilder.recordVisibility(Visibility.PRIVATE)) {
                    parser.addParserWarning("msg.jsdoc.visibility.private",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case PROTECTED:
                  if (!jsdocBuilder.recordVisibility(Visibility.PROTECTED)) {
                    parser.addParserWarning("msg.jsdoc.visibility.protected",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case PUBLIC:
                  if (!jsdocBuilder.recordVisibility(Visibility.PUBLIC)) {
                    parser.addParserWarning("msg.jsdoc.visibility.public",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case NO_SHADOW:
                  if (!jsdocBuilder.recordNoShadow()) {
                    parser.addParserWarning("msg.jsdoc.noshadow",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case NO_SIDE_EFFECTS:
                  if (!jsdocBuilder.recordNoSideEffects()) {
                    parser.addParserWarning("msg.jsdoc.nosideeffects",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case MODIFIES:
                  token = parseModifiesTag(next());
                  continue retry;

                case IMPLICIT_CAST:
                  if (!jsdocBuilder.recordImplicitCast()) {
                    parser.addTypeWarning("msg.jsdoc.implicitcast",
                        stream.getLineno(), stream.getCharno());
                  }
                  token = eatTokensUntilEOL();
                  continue retry;

                case SEE:
                  if (jsdocBuilder.shouldParseDocumentation()) {
                    ExtractionInfo referenceInfo = extractSingleLineBlock();
                    String reference = referenceInfo.string;

                    if (reference.length() == 0) {
                      parser.addParserWarning("msg.jsdoc.seemissing",
                          stream.getLineno(), stream.getCharno());
                    } else {
                      jsdocBuilder.addReference(reference);
                    }

                    token = referenceInfo.token;
                  } else {
                    token = eatTokensUntilEOL(token);
                  }
                  continue retry;

                case SUPPRESS:
                  token = parseSuppressTag(next());
                  continue retry;

                case TEMPLATE:
                  ExtractionInfo templateInfo = extractSingleLineBlock();
                  String templateTypeName = templateInfo.string;

                  if (templateTypeName.length() == 0) {
                    parser.addTypeWarning("msg.jsdoc.templatemissing",
                          stream.getLineno(), stream.getCharno());
                  } else if (!jsdocBuilder.recordTemplateTypeName(
                      templateTypeName)) {
                    parser.addTypeWarning("msg.jsdoc.template.at.most.once",
                        stream.getLineno(), stream.getCharno());
                  }

                  token = templateInfo.token;
                  continue retry;

                case VERSION:
                  ExtractionInfo versionInfo = extractSingleLineBlock();
                  String version = versionInfo.string;

                  if (version.length() == 0) {
                    parser.addParserWarning("msg.jsdoc.versionmissing",
                          stream.getLineno(), stream.getCharno());
                  } else {
                    if (!jsdocBuilder.recordVersion(version)) {
                       parser.addParserWarning("msg.jsdoc.extraversion",
                          stream.getLineno(), stream.getCharno());
                    }
                  }

                  token = versionInfo.token;
                  continue retry;

                case DEFINE:
                case RETURN:
                case THIS:
                case TYPE:
                case TYPEDEF:
                  lineno = stream.getLineno();
                  charno = stream.getCharno();

                  Node typeNode = null;
                  if (!lookAheadForTypeAnnotation() &&
                      annotation == Annotation.RETURN) {
                    // If RETURN doesn't have a type annotation, record
                    // it as the unknown type.
                    typeNode = newNode(Token.QMARK);
                  } else {
                    skipEOLs();
                    token = next();
                    typeNode = parseAndRecordTypeNode(token, lineno, charno);
                  }

                  if (annotation == Annotation.THIS) {
                    typeNode = wrapNode(Token.BANG, typeNode);
                    if (typeNode != null && token != JsDocToken.LC) {
                      typeNode.putBooleanProp(Node.BRACELESS_TYPE, true);
                    }
                  }
                  type = createJSTypeExpression(typeNode);

                  if (type == null) {
                    // error reported during recursive descent
                    // recovering parsing
                  } else {
                    switch (annotation) {
                      case DEFINE:
                        if (!jsdocBuilder.recordDefineType(type)) {
                          parser.addParserWarning("msg.jsdoc.define",
                              lineno, charno);
                        }
                        break;

                      case RETURN:
                        if (!jsdocBuilder.recordReturnType(type)) {
                          parser.addTypeWarning(
                              "msg.jsdoc.incompat.type", lineno, charno);
                          break;
                        }

                        // Find the return's description (if applicable).
                        if (jsdocBuilder.shouldParseDocumentation()) {
                          ExtractionInfo returnDescriptionInfo =
                              extractMultilineTextualBlock(token);

                          String returnDescription =
                              returnDescriptionInfo.string;

                          if (returnDescription.length() > 0) {
                            jsdocBuilder.recordReturnDescription(
                                returnDescription);
                          }

                          token = returnDescriptionInfo.token;
                        } else {
                          token = eatTokensUntilEOL(token);
                        }
                        continue retry;

                      case THIS:
                        if (!jsdocBuilder.recordThisType(type)) {
                          parser.addTypeWarning(
                              "msg.jsdoc.incompat.type", lineno, charno);
                        }
                        break;

                      case TYPE:
                        if (!jsdocBuilder.recordType(type)) {
                          parser.addTypeWarning(
                              "msg.jsdoc.incompat.type", lineno, charno);
                        }
                        break;

                      case TYPEDEF:
                        if (!jsdocBuilder.recordTypedef(type)) {
                          parser.addTypeWarning(
                              "msg.jsdoc.incompat.type", lineno, charno);
                        }
                        break;
                    }

                  token = eatTokensUntilEOL();
                  }
                  continue retry;
              }
            }
          }
          break;

        case EOC:
          if (hasParsedFileOverviewDocInfo()) {
            fileOverviewJSDocInfo = retrieveAndResetParsedJSDocInfo();
          }
          checkExtendedTypes(extendedTypes);
          return true;

        case EOF:
          // discard any accumulated information
          jsdocBuilder.build(null);
          parser.addParserWarning("msg.unexpected.eof",
              stream.getLineno(), stream.getCharno());
          checkExtendedTypes(extendedTypes);
          return false;

        case EOL:
          if (state == State.SEARCHING_NEWLINE) {
            state = State.SEARCHING_ANNOTATION;
          }
          token = next();
          continue retry;

        default:
          if (token == JsDocToken.STAR && state == State.SEARCHING_ANNOTATION) {
            token = next();
            continue retry;
          } else {
            state = State.SEARCHING_NEWLINE;
            token = eatTokensUntilEOL();
            continue retry;
          }
      }

      // next token
      token = next();
    }
  }

  private Node parseBasicTypeExpression(JsDocToken token) {
    if (token == JsDocToken.STAR) {
      return newNode(Token.STAR);
    } else if (token == JsDocToken.LB) {
      skipEOLs();
      return parseArrayType(next());
    } else if (token == JsDocToken.LC) {
      skipEOLs();
      return parseRecordType(next());
    } else if (token == JsDocToken.LP) {
      skipEOLs();
      return parseUnionType(next());
    } else if (token == JsDocToken.STRING) {
      String string = stream.getString();
      if ("function".equals(string)) {
        skipEOLs();
        return parseFunctionType(next());
      } else if ("null".equals(string) || "undefined".equals(string)) {
        return newStringNode(string);
      } else {
        return parseTypeName(token);
      }
    }

    return reportGenericTypeSyntaxWarning();
  }

  private Node parseFunctionType(JsDocToken token) {
    // NOTE(nicksantos): We're not implementing generics at the moment, so
    // just throw out TypeParameters.
    if (token != JsDocToken.LP) {
      return reportTypeSyntaxWarning("msg.jsdoc.missing.lp");
    }

    Node functionType = newNode(Token.FUNCTION);
    Node parameters = null;
    skipEOLs();
    if (!match(JsDocToken.RP)) {
      token = next();

      boolean hasParams = true;
      if (token == JsDocToken.STRING) {
        String tokenStr = stream.getString();
        boolean isThis = "this".equals(tokenStr);
        boolean isNew = "new".equals(tokenStr);
        if (isThis || isNew) {
          if (match(JsDocToken.COLON)) {
            next();
            skipEOLs();
            Node contextType = wrapNode(
                isThis ? Token.THIS : Token.NEW,
                parseTypeName(next()));
            if (contextType == null) {
              return null;
            }

            functionType.addChildToFront(contextType);
          } else {
            return reportTypeSyntaxWarning("msg.jsdoc.missing.colon");
          }

          if (match(JsDocToken.COMMA)) {
            next();
            skipEOLs();
            token = next();
          } else {
            hasParams = false;
          }
        }
      }

      if (hasParams) {
        parameters = parseParametersType(token);
        if (parameters == null) {
          return null;
        }
      }
    }

    if (parameters != null) {
      functionType.addChildToBack(parameters);
    }

    skipEOLs();
    if (!match(JsDocToken.RP)) {
      return reportTypeSyntaxWarning("msg.jsdoc.missing.rp");
    }

    skipEOLs();
    Node resultType = parseResultType(next());
    if (resultType == null) {
      return null;
    } else {
      functionType.addChildToBack(resultType);
    }
    return functionType;
  }

// relevant test
// com.google.javascript.jscomp.CollapsePropertiesTest::testNoCollapseForEachInExterns
  public void testNoCollapseForEachInExterns() {
    collapsePropertiesOnExternTypes = true;
    test(" function Array() {}" +
         "Array.forEach = function() {}",
         "if (!Array.forEach) {\n" +
         "  Array.forEach = function() {};\n" +
         "}",
         "if (!Array.forEach) {\n" +
         "  Array.forEach = function() {};\n" +
         "}", null, null);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testDoNotCollapsePropertyOnExternType
  public void testDoNotCollapsePropertyOnExternType() {
    collapsePropertiesOnExternTypes = false;
    test("String.myFunc = function() {}; String.myFunc()",
         "String.myFunc = function() {}; String.myFunc()");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testBug1704733
  public void testBug1704733() {
    String prelude =
        "function protect(x) { return x; }" +
        "function O() {}" +
        "protect(O).m1 = function() {};" +
        "protect(O).m2 = function() {};" +
        "protect(O).m3 = function() {};";

    testSame(prelude +
        "alert(O.m1); alert(O.m2()); alert(!O.m3);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testBug1956277
  public void testBug1956277() {
    test("var CONST = {}; CONST.URL = 3;",
         "var CONST$URL = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testBug1974371
  public void testBug1974371() {
    test(
        " var Foo = {A: {c: 2}, B: {c: 3}};" +
        "for (var key in Foo) {}",
        "var Foo$A = {c: 2}; var Foo$B = {c: 3};" +
        "var Foo = {A: Foo$A, B: Foo$B};" +
         "for (var key in Foo) {}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects1
  public void testEnumOfObjects1() {
    test(
        COMMON_ENUM +
        "for (var key in Foo.A) {}",
         "var Foo$A = {c: 2}; var Foo$B$c = 3; for (var key in Foo$A) {}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects2
  public void testEnumOfObjects2() {
    test(
        COMMON_ENUM +
        "foo(Foo.A.c);",
         "var Foo$A$c = 2; var Foo$B$c = 3; foo(Foo$A$c);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects3
  public void testEnumOfObjects3() {
    test(
        "var x = {c: 2}; var y = {c: 3};" +
        " var Foo = {A: x, B: y};" +
        "for (var key in Foo) {}",
        "var x = {c: 2}; var y = {c: 3};" +
        "var Foo$A = x; var Foo$B = y; var Foo = {A: Foo$A, B: Foo$B};" +
        "for (var key in Foo) {}");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testEnumOfObjects4
  public void testEnumOfObjects4() {
    
    
    
    test(
        COMMON_ENUM +
        "for (var key in Foo) {} Foo.A = 3; alert(Foo.A);",
        "var Foo$A = {c: 2}; var Foo$B = {c: 3};" +
        "var Foo = {A: Foo$A, B: Foo$B};" +
        "for (var key in Foo) {} Foo$A = 3; alert(Foo$A);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjectOfObjects1
  public void testObjectOfObjects1() {
    
    
    testSame(
        "var Foo = {a: {c: 2}, b: {c: 3}};" +
        "for (var key in Foo) {} Foo.a = 3; alert(Foo.a);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject0
  public void testReferenceInAnonymousObject0() {
    test("var a = {};" +
         "a.b = function(){};" +
         "a.b.prototype.c = function(){};" +
         "var d = a.b.prototype.c;",
         "var a$b = function(){};" +
         "a$b.prototype.c = function(){};" +
         "var d = a$b.prototype.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject1
  public void testReferenceInAnonymousObject1() {
    test("var a = {};" +
         "a.b = function(){};" +
         "var d = a.b.prototype.c;",
         "var a$b = function(){};" +
         "var d = a$b.prototype.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject2
  public void testReferenceInAnonymousObject2() {
    test("var a = {};" +
         "a.b = function(){};" +
         "a.b.prototype.c = function(){};" +
         "var d = {c: a.b.prototype.c};",
         "var a$b = function(){};" +
         "a$b.prototype.c = function(){};" +
         "var d$c = a$b.prototype.c;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject3
  public void testReferenceInAnonymousObject3() {
    test("function CreateClass(a$$1) {}" +
         "var a = {};" +
         "a.b = function(){};" +
         "a.b.prototype.c = function(){};" +
         "a.d = CreateClass({c: a.b.prototype.c});",
         "function CreateClass(a$$1) {}" +
         "var a$b = function(){};" +
         "a$b.prototype.c = function(){};" +
         "var a$d = CreateClass({c: a$b.prototype.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject4
  public void testReferenceInAnonymousObject4() {
    test("function CreateClass(a) {}" +
         "var a = {};" +
         "a.b = CreateClass({c: function() {}});" +
         "a.d = CreateClass({c: a.b.c});",
         "function CreateClass(a$$1) {}" +
         "var a$b = CreateClass({c: function() {}});" +
         "var a$d = CreateClass({c: a$b.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testReferenceInAnonymousObject5
  public void testReferenceInAnonymousObject5() {
    test("function CreateClass(a) {}" +
         "var a = {};" +
         "a.b = CreateClass({c: function() {}});" +
         "a.d = CreateClass({c: a.b.prototype.c});",
         "function CreateClass(a$$1) {}" +
         "var a$b = CreateClass({c: function() {}});" +
         "var a$d = CreateClass({c: a$b.prototype.c});");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCrashInCommaOperator
  public void testCrashInCommaOperator() {
    test("var a = {}; a.b = function() {},a.b();",
         "var a$b; a$b=function() {},a$b();");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testCrashInNestedAssign
  public void testCrashInNestedAssign() {
    test("var a = {}; if (a.b = function() {}) a.b();",
         "var a$b; if (a$b=function() {}) { a$b(); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTwinReferenceCancelsChildCollapsing
  public void testTwinReferenceCancelsChildCollapsing() {
    test("var a = {}; if (a.b = function() {}) { a.b.c = 3; a.b(a.b.c); }",
         "var a$b; if (a$b = function() {}) { a$b.c = 3; a$b(a$b.c); }");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign
  public void testPropWithDollarSign() {
    test("var a = {$: 3}", "var a$$0 = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign2
  public void testPropWithDollarSign2() {
    test("var a = {$: function(){}}", "var a$$0 = function(){};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign3
  public void testPropWithDollarSign3() {
    test("var a = {b: {c: 3}, b$c: function(){}}",
         "var a$b$c = 3; var a$b$0c = function(){};");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign4
  public void testPropWithDollarSign4() {
    test("var a = {$$: {$$$: 3}};", "var a$$0$0$$0$0$0 = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropWithDollarSign5
  public void testPropWithDollarSign5() {
    test("var a = {b: {$0c: true}, b$0c: false};",
         "var a$b$$00c = true; var a$b$00c = false;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testConstKey
  public void testConstKey() {
    test("var foo = {A: 3};", "var foo$A = 3;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOnGlobalCtor
  public void testPropertyOnGlobalCtor() {
    test(" function Map() {} Map.foo = 3; Map;",
         "function Map() {} var Map$foo = 3; Map;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testPropertyOnGlobalFunction
  public void testPropertyOnGlobalFunction() {
    testSame("function Map() {} Map.foo = 3; Map;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testIssue389
  public void testIssue389() {
    test(
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "dojo.gfx.Shape = function() {};" +
        "dojo.gfx.Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);",
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "var dojo$gfx$Shape = function() {};" +
        "dojo$gfx$Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);",
        null,
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasedTopLevelName
  public void testAliasedTopLevelName() {
    testSame(
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "dojo.gfx.Shape = {SQUARE: 2};" +
        "dojo.gfx.Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);" +
        "alias(dojo$gfx$Shape$SQUARE);");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAliasedTopLevelEnum
  public void testAliasedTopLevelEnum() {
    test(
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "dojo.gfx.Shape = {SQUARE: 2};" +
        "dojo.gfx.Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);" +
        "alias(dojo.gfx.Shape.SQUARE);",
        "function alias() {}" +
        "var dojo = {};" +
        "dojo.gfx = {};" +
        "dojo.declare = function() {};" +
        "" +
        "var dojo$gfx$Shape = {SQUARE: 2};" +
        "dojo$gfx$Shape = dojo.declare('dojo.gfx.Shape');" +
        "alias(dojo);" +
        "alias(dojo$gfx$Shape.SQUARE);",
        null,
        CollapseProperties.UNSAFE_NAMESPACE_WARNING);
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testAssignFunctionBeforeDefinition
  public void testAssignFunctionBeforeDefinition() {
    testSame(
        "f = function() {};" +
        "var f = null;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testObjectLitBeforeDefinition
  public void testObjectLitBeforeDefinition() {
    testSame(
        "a = {b: 3};" +
        "var a = null;" +
        "this.c = a.b;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTypedef1
  public void testTypedef1() {
    test("var foo = {};" +
         " foo.Baz;",
         "var foo = {}; var foo$Baz;");
  }

// com.google.javascript.jscomp.CollapsePropertiesTest::testTypedef2
  public void testTypedef2() {
    test("var foo = {};" +
         " foo.Bar.Baz;" +
         "foo.Bar = function() {};",
         "var foo$Bar$Baz; var foo$Bar = function(){};");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering1
  public void testWarningGuardOrdering1() {
    args.add("--jscomp_error=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering2
  public void testWarningGuardOrdering2() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering3
  public void testWarningGuardOrdering3() {
    args.add("--jscomp_warning=globalThis");
    args.add("--jscomp_off=globalThis");
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testWarningGuardOrdering4
  public void testWarningGuardOrdering4() {
    args.add("--jscomp_off=globalThis");
    args.add("--jscomp_warning=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOffByDefault
  public void testCheckGlobalThisOffByDefault() {
    testSame("function f() { this.a = 3; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithAdvancedMode
  public void testCheckGlobalThisOnWithAdvancedMode() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckGlobalThisOnWithErrorFlag
  public void testCheckGlobalThisOnWithErrorFlag() {
    args.add("--jscomp_error=globalThis");
    test("function f() { this.a = 3; }", CheckGlobalThis.GLOBAL_THIS);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOffByDefault
  public void testTypeCheckingOffByDefault() {
    test("function f(x) { return x; } f();",
         "function f(a) { return a; } f();");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testReflectedMethods
  public void testReflectedMethods() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test(
        "" +
        "function Foo() {}" +
        "Foo.prototype.handle = function(x, y) { alert(y); };" +
        "var x = goog.reflect.object(Foo, {handle: 1});" +
        "for (var i in x) { x[i].call(x); }" +
        "window['Foo'] = Foo;",
        "function a() {}" +
        "a.prototype.a = function(e, d) { alert(d); };" +
        "var b = goog.c.b(a, {a: 1}),c;" +
        "for (c in b) { b[c].call(b); }" +
        "window.Foo = a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckingOnWithVerbose
  public void testTypeCheckingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test("function f(x) { return x; } f();", TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOffByDefault
  public void testTypeParsingOffByDefault() {
    testSame(" function f(a) { return a; }");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeParsingOnWithVerbose
  public void testTypeParsingOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
    test(" function f(a) { return a; }",
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride1
  public void testTypeCheckOverride1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=checkTypes");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTypeCheckOverride2
  public void testTypeCheckOverride2() {
    args.add("--warning_level=DEFAULT");
    testSame("var x = x || {}; x.f = function() {}; x.f(3);");

    args.add("--jscomp_warning=checkTypes");
    test("var x = x || {}; x.f = function() {}; x.f(3);",
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOffForDefault
  public void testCheckSymbolsOffForDefault() {
    args.add("--warning_level=DEFAULT");
    test("x = 3; var y; var y;", "x=3; var y;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOnForVerbose
  public void testCheckSymbolsOnForVerbose() {
    args.add("--warning_level=VERBOSE");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
    test("var y; var y;", SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForVerbose
  public void testCheckSymbolsOverrideForVerbose() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=undefinedVars");
    testSame("x = 3;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckSymbolsOverrideForQuiet
  public void testCheckSymbolsOverrideForQuiet() {
    args.add("--warning_level=QUIET");
    args.add("--jscomp_error=undefinedVars");
    test("x = 3;", VarCheck.UNDEFINED_VAR_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties1
  public void testCheckUndefinedProperties1() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_error=missingProperties");
    test("var x = {}; var y = x.bar;", TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties2
  public void testCheckUndefinedProperties2() {
    args.add("--warning_level=VERBOSE");
    args.add("--jscomp_off=missingProperties");
    test("var x = {}; var y = x.bar;", CheckGlobalNames.UNDEFINED_NAME_WARNING);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCheckUndefinedProperties3
  public void testCheckUndefinedProperties3() {
    args.add("--warning_level=VERBOSE");
    test("function f() {var x = {}; var y = x.bar;}",
        TypeCheck.INEXISTENT_PROPERTY);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDuplicateParams
  public void testDuplicateParams() {
    test("function f(a, a) {}", RhinoErrorReporter.DUPLICATE_PARAM);
    assertTrue(lastCompiler.hasHaltingErrors());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag
  public void testDefineFlag() {
    args.add("--define=FOO");
    args.add("--define=\"BAR=5\"");
    args.add("--D"); args.add("CCC");
    args.add("-D"); args.add("DDD");
    test(" var FOO = false;" +
         " var BAR = 3;" +
         " var CCC = false;" +
         " var DDD = false;",
         "var FOO = !0, BAR = 5, CCC = !0, DDD = !0;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag2
  public void testDefineFlag2() {
    args.add("--define=FOO='x\"'");
    test(" var FOO = \"a\";",
         "var FOO = \"x\\\"\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDefineFlag3
  public void testDefineFlag3() {
    args.add("--define=FOO=\"x'\"");
    test(" var FOO = \"a\";",
         "var FOO = \"x'\";");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testScriptStrictModeNoWarning
  public void testScriptStrictModeNoWarning() {
    test("'use strict';", "");
    test("'no use strict';", CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testFunctionStrictModeNoWarning
  public void testFunctionStrictModeNoWarning() {
    test("function f() {'use strict';}", "function f() {}");
    test("function f() {'no use strict';}",
         CheckSideEffects.USELESS_CODE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testQuietMode
  public void testQuietMode() {
    args.add("--warning_level=DEFAULT");
    test(" var x;",
         RhinoErrorReporter.PARSE_ERROR);
    args.add("--warning_level=QUIET");
    testSame(" var x;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testProcessClosurePrimitives
  public void testProcessClosurePrimitives() {
    test("var goog = {}; goog.provide('goog.dom');",
         "var goog = {dom:{}};");
    args.add("--process_closure_primitives=false");
    testSame("var goog = {}; goog.provide('goog.dom');");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCssNameWiring
  public void testCssNameWiring() throws Exception {
    test("var goog = {}; goog.getCssName = function() {};" +
         "goog.setCssNameMapping = function() {};" +
         "goog.setCssNameMapping({'goog': 'a', 'button': 'b'});" +
         "var a = goog.getCssName('goog-button');" +
         "var b = goog.getCssName('css-button');" +
         "var c = goog.getCssName('goog-menu');" +
         "var d = goog.getCssName('css-menu');",
         "var goog = { getCssName: function() {}," +
         "             setCssNameMapping: function() {} }," +
         "    a = 'a-b'," +
         "    b = 'css-b'," +
         "    c = 'a-menu'," +
         "    d = 'css-menu';");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue70
  public void testIssue70() {
    test("function foo({}) {}", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue81
  public void testIssue81() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    useStringComparison = true;
    test("eval('1'); var x = eval; x('2');",
         "eval(\"1\");(0,eval)(\"2\");");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue115
  public void testIssue115() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--warning_level=VERBOSE");
    test("function f() { " +
         "  var arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}",
         "function f() { " +
         "  arguments = Array.prototype.slice.call(arguments, 0);" +
         "  return arguments[0]; " +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testIssue297
  public void testIssue297() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function f(p) {" +
         " var x;" +
         " return ((x=p.id) && (x=parseInt(x.substr(1))) && x>0);" +
         "}",
         "function f(b) {" +
         " var a;" +
         " return ((a=b.id) && (a=parseInt(a.substr(1))) && a>0);" +
         "}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag1
  public void testDebugFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=false");
    test("function foo(a) {}",
         "function foo() {}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag2
  public void testDebugFlag2() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug=true");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag3
  public void testDebugFlag3() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=false");
    test("function Foo() {}" +
         "Foo.x = 1;" +
         "function f() {throw new Foo().x;} f();",
         "throw (new function() {}).a;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDebugFlag4
  public void testDebugFlag4() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    args.add("--warning_level=QUIET");
    args.add("--debug=true");
    test("function Foo() {}" +
        "Foo.x = 1;" +
        "function f() {throw new Foo().x;} f();",
        "throw (new function Foo() {}).$x$;");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag1
  public void testBooleanFlag1() {
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    args.add("--debug");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testBooleanFlag2
  public void testBooleanFlag2() {
    args.add("--debug");
    args.add("--compilation_level=SIMPLE_OPTIMIZATIONS");
    test("function foo(a) {alert(a)}",
         "function foo($a$$) {alert($a$$)}");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testHelpFlag
  public void testHelpFlag() {
    args.add("--help");
    assertFalse(
        createCommandLineRunner(
            new String[] {"function f() {}"}).shouldRunCompiler());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting1
  public void testExternsLifting1() throws Exception{
    String code = " function f() {}";
    test(new String[] {code},
         new String[] {});

    assertEquals(2, lastCompiler.getExternsForTesting().size());

    CompilerInput extern = lastCompiler.getExternsForTesting().get(1);
    assertNull(extern.getModule());
    assertTrue(extern.isExtern());
    assertEquals(code, extern.getCode());

    assertEquals(1, lastCompiler.getInputsForTesting().size());

    CompilerInput input = lastCompiler.getInputsForTesting().get(0);
    assertNotNull(input.getModule());
    assertFalse(input.isExtern());
    assertEquals("", input.getCode());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testExternsLifting2
  public void testExternsLifting2() {
    args.add("--warning_level=VERBOSE");
    test(new String[] {" function f() {}", "f(3);"},
         new String[] {"f(3);"},
         TypeCheck.WRONG_ARGUMENT_COUNT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOff
  public void testSourceSortingOff() {
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         }, ProcessClosurePrimitives.LATE_PROVIDE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingOn
  public void testSourceSortingOn() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps1
  public void testSourceSortingCircularDeps1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceSortingCircularDeps2
  public void testSourceSortingCircularDeps2() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.provide('roses.lime.juice');",
          "goog.provide('gin'); goog.require('tonic'); var gin = {};",
          "goog.provide('tonic'); goog.require('gin'); var tonic = {};",
          "goog.require('gin'); goog.require('tonic');",
          "goog.provide('gimlet');" +
          "     goog.require('gin'); goog.require('roses.lime.juice');"
         },
         JSModule.CIRCULAR_DEPENDENCY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn1
  public void testSourcePruningOn1() {
    args.add("--manage_closure_dependencies=true");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           ""
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn2
  public void testSourcePruningOn2() {
    args.add("--closure_entry_point=guinness");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var guinness = {};"
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn3
  public void testSourcePruningOn3() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn4
  public void testSourcePruningOn4() {
    args.add("--closure_entry_point=scotch");
    args.add("--closure_entry_point=beer");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn5
  public void testSourcePruningOn5() {
    args.add("--closure_entry_point=shiraz");
    test(new String[] {
          "goog.provide('guinness');\ngoog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         Compiler.MISSING_ENTRY_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourcePruningOn6
  public void testSourcePruningOn6() {
    args.add("--closure_entry_point=scotch");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');",
          "goog.provide('scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {};",
           "",
           "var scotch = {}, x = 3;",
         });
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testForwardDeclareDroppedTypes
  public void testForwardDeclareDroppedTypes() {
    args.add("--manage_closure_dependencies=true");

    args.add("--warning_level=VERBOSE");
    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}",
          "goog.provide('Scotch'); var x = 3;"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         });

    test(new String[] {
          "goog.require('beer');",
          "goog.provide('beer');  function f(x) {}"
         },
         new String[] {
           "var beer = {}; function f() {}",
           ""
         },
         RhinoErrorReporter.TYPE_PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion1
  public void testSourceMapExpansion1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    args.add("--create_source_map=%outname%.map");
    testSame("var x = 3;");
    assertEquals("/path/to/out.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion2
  public void testSourceMapExpansion2() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(), null));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapExpansion3
  public void testSourceMapExpansion3() {
    useModules = ModulePattern.CHAIN;
    args.add("--create_source_map=%outname%.map");
    args.add("--module_output_path_prefix=foo_");
    testSame(new String[] {"var x = 3;", "var y = 5;"});
    assertEquals("foo_m0.js.map",
        lastCommandLineRunner.expandSourceMapPath(
            lastCompiler.getOptions(),
            lastCompiler.getModuleGraph().getRootModule()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSourceMapFormat1
  public void testSourceMapFormat1() {
    args.add("--js_output_file");
    args.add("/path/to/out.js");
    testSame("var x = 3;");
    assertEquals(SourceMap.Format.DEFAULT,
        lastCompiler.getOptions().sourceMapFormat);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testCharSetExpansion
  public void testCharSetExpansion() {
    testSame("");
    assertEquals("US-ASCII", lastCompiler.getOptions().outputCharset);
    args.add("--charset=UTF-8");
    testSame("");
    assertEquals("UTF-8", lastCompiler.getOptions().outputCharset);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testChainModuleManifest
  public void testChainModuleManifest() throws Exception {
    useModules = ModulePattern.CHAIN;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestTo(
        lastCompiler.getModuleGraph(), builder);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m1}\n" +
        "i2\n" +
        "\n" +
        "{m3:m2}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testStarModuleManifest
  public void testStarModuleManifest() throws Exception {
    useModules = ModulePattern.STAR;
    testSame(new String[] {
          "var x = 3;", "var y = 5;", "var z = 7;", "var a = 9;"});

    StringBuilder builder = new StringBuilder();
    lastCommandLineRunner.printModuleGraphManifestTo(
        lastCompiler.getModuleGraph(), builder);
    assertEquals(
        "{m0}\n" +
        "i0\n" +
        "\n" +
        "{m1:m0}\n" +
        "i1\n" +
        "\n" +
        "{m2:m0}\n" +
        "i2\n" +
        "\n" +
        "{m3:m0}\n" +
        "i3\n",
        builder.toString());
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag
  public void testVersionFlag() {
    args.add("--version");
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testVersionFlag2
  public void testVersionFlag2() {
    lastArg = "--version";
    testSame("");
    assertEquals(
        0,
        new String(errReader.toByteArray()).indexOf(
            "Closure Compiler (http://code.google.com/closure/compiler)\n" +
            "Version: "));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testPrintAstFlag
  public void testPrintAstFlag() {
    args.add("--print_ast=true");
    testSame("");
    assertEquals(
        "digraph AST {\n" +
        "  node [color=lightblue2, style=filled];\n" +
        "  node0 [label=\"BLOCK\"];\n" +
        "  node1 [label=\"SCRIPT\"];\n" +
        "  node0 -> node1 [weight=1];\n" +
        "  node1 -> RETURN [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> RETURN [label=\"SYN_BLOCK\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "  node0 -> node1 [label=\"UNCOND\", " +
            "fontcolor=\"red\", weight=0.01, color=\"red\"];\n" +
        "}\n\n",
        new String(outReader.toByteArray()));
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testSyntheticExterns
  public void testSyntheticExterns() {
    externs = ImmutableList.of(
        JSSourceFile.fromCode("externs", "myVar.property;"));
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         VarCheck.UNDEFINED_EXTERN_VAR_ERROR);

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var yourVar = {};",
         "var theirVar={},myVar={},yourVar={};");

    args.add("--jscomp_off=externsValidation");
    args.add("--warning_level=VERBOSE");
    test("var theirVar = {}; var myVar = {}; var myVar = {};",
         SyntacticScopeCreator.VAR_MULTIPLY_DECLARED_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGoogAssertStripping
  public void testGoogAssertStripping() {
    args.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
    test("goog.asserts.assert(false)",
         "");
    args.add("--debug");
    test("goog.asserts.assert(false)", "goog.$asserts$.$assert$(!1)");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testMissingReturnCheckOnWithVerbose
  public void testMissingReturnCheckOnWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {f()} f();",
        CheckMissingReturn.MISSING_RETURN_STATEMENT);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testGenerateExports
  public void testGenerateExports() {
    args.add("--generate_exports=true");
    test(" foo.prototype.x = function() {};",
        "foo.prototype.x=function(){};"+
        "goog.exportSymbol(\"foo.prototype.x\",foo.prototype.x);");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testDepreciationWithVerbose
  public void testDepreciationWithVerbose() {
    args.add("--warning_level=VERBOSE");
    test(" function f() {}; f()",
       CheckAccessControls.DEPRECATED_NAME);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testTwoParseErrors
  public void testTwoParseErrors() {
    
    
    Compiler compiler = compile(new String[] {
      "var a b;",
      "var b c;"
    });
    assertEquals(2, compiler.getErrors().length);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES3ByDefault
  public void testES3ByDefault() {
    test("var x = f.function", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5
  public void testES5() {
    args.add("--language_in=ECMASCRIPT5");
    test("var x = f.function", "var x = f.function");
    test("var let", "var let");
  }

// com.google.javascript.jscomp.CommandLineRunnerTest::testES5Strict
  public void testES5Strict() {
    args.add("--language_in=ECMASCRIPT5_STRICT");
    test("var x = f.function", "'use strict';var x = f.function");
    test("var let", RhinoErrorReporter.PARSE_ERROR);
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantDefinition1
  public void testConstantDefinition1() {
    testSame("var XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantDefinition2
  public void testConstantDefinition2() {
    testSame("var a$b$XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantInitializedInAnonymousNamespace1
  public void testConstantInitializedInAnonymousNamespace1() {
    testSame("var XYZ; (function(){ XYZ = 1; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantInitializedInAnonymousNamespace2
  public void testConstantInitializedInAnonymousNamespace2() {
    testSame("var a$b$XYZ; (function(){ a$b$XYZ = 1; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectModified
  public void testObjectModified() {
    testSame("var IE = true, XYZ = {a:1,b:1}; if (IE) XYZ['c'] = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectPropertyInitializedLate
  public void testObjectPropertyInitializedLate() {
    testSame("var XYZ = {}; for (var i = 0; i < 10; i++) { XYZ[i] = i; }");
  }

// com.google.javascript.jscomp.ConstCheckTest::testObjectRedefined1
  public void testObjectRedefined1() {
    testError("var XYZ = {}; XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefined1
  public void testConstantRedefined1() {
    testError("var XYZ = 1; XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefined2
  public void testConstantRedefined2() {
    testError("var a$b$XYZ = 1; a$b$XYZ = 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScope1
  public void testConstantRedefinedInLocalScope1() {
    testError("var XYZ = 1; (function(){ XYZ = 2; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScope2
  public void testConstantRedefinedInLocalScope2() {
    testError("var a$b$XYZ = 1; (function(){ a$b$XYZ = 2; })();");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantRedefinedInLocalScopeOutOfOrder
  public void testConstantRedefinedInLocalScopeOutOfOrder() {
    testError("function f() { XYZ = 2; } var XYZ = 1;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostIncremented1
  public void testConstantPostIncremented1() {
    testError("var XYZ = 1; XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostIncremented2
  public void testConstantPostIncremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreIncremented1
  public void testConstantPreIncremented1() {
    testError("var XYZ = 1; XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreIncremented2
  public void testConstantPreIncremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ++;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostDecremented1
  public void testConstantPostDecremented1() {
    testError("var XYZ = 1; XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPostDecremented2
  public void testConstantPostDecremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreDecremented1
  public void testConstantPreDecremented1() {
    testError("var XYZ = 1; XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstantPreDecremented2
  public void testConstantPreDecremented2() {
    testError("var a$b$XYZ = 1; a$b$XYZ--;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedArithmeticAssignment1
  public void testAbbreviatedArithmeticAssignment1() {
    testError("var XYZ = 1; XYZ += 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedArithmeticAssignment2
  public void testAbbreviatedArithmeticAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ %= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedBitAssignment1
  public void testAbbreviatedBitAssignment1() {
    testError("var XYZ = 1; XYZ |= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedBitAssignment2
  public void testAbbreviatedBitAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ &= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedShiftAssignment1
  public void testAbbreviatedShiftAssignment1() {
    testError("var XYZ = 1; XYZ >>= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testAbbreviatedShiftAssignment2
  public void testAbbreviatedShiftAssignment2() {
    testError("var a$b$XYZ = 1; a$b$XYZ <<= 2;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstAnnotation
  public void testConstAnnotation() {
    testError(" var xyz = 1; xyz = 3;");
  }

// com.google.javascript.jscomp.ConstCheckTest::testConstSuppression
  public void testConstSuppression() {
    testSame("\n" +
             " var xyz = 1; xyz = 3;");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testWhile
  public void testWhile() {
    assertNoError("while(1) { break; }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testNextedWhile
  public void testNextedWhile() {
    assertNoError("while(1) { while(1) { break; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreak
  public void testBreak() {
    assertInvalidBreak("break;");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinue
  public void testContinue() {
    assertInvalidContinue("continue;");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreakCrossFunction
  public void testBreakCrossFunction() {
    assertInvalidBreak("while(1) { function f() { break; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testBreakCrossFunctionInFor
  public void testBreakCrossFunctionInFor() {
    assertInvalidBreak("while(1) {for(var f = function () { break; };;) {}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitch
  public void testContinueToSwitch() {
    assertInvalidContinue("switch(1) {case(1): continue; }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithNoCases
  public void testContinueToSwitchWithNoCases() {
    assertNoError("switch(1){}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithTwoCases
  public void testContinueToSwitchWithTwoCases() {
    assertInvalidContinue("switch(1){case(1):break;case(2):continue;}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToSwitchWithDefault
  public void testContinueToSwitchWithDefault() {
    assertInvalidContinue("switch(1){case(1):break;case(2):default:continue;}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueToLabelSwitch
  public void testContinueToLabelSwitch() {
    assertInvalidLabeledContinue(
        "while(1) {a: switch(1) {case(1): continue a; }}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueOutsideSwitch
  public void testContinueOutsideSwitch() {
    assertNoError("b: while(1) { a: switch(1) { case(1): continue b; } }");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueNotCrossFunction1
  public void testContinueNotCrossFunction1() {
    assertNoError("a:switch(1){case(1):function f(){a:while(1){continue a;}}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testContinueNotCrossFunction2
  public void testContinueNotCrossFunction2() {
    assertUndefinedLabel(
        "a:switch(1){case(1):function f(){while(1){continue a;}}}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith1
  public void testUseOfWith1() {
    testSame("with(a){}", ControlStructureCheck.USE_OF_WITH);
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith2
  public void testUseOfWith2() {
    testSame("" +
             "with(a){}");
  }

// com.google.javascript.jscomp.ControlStructureCheckTest::testUseOfWith3
  public void testUseOfWith3() {
    testSame(
        "function f(expr, context) {\n" +
        "  try {\n" +
        "     with (context) {\n" +
        "      return eval('[' + expr + '][0]');\n" +
        "    }\n" +
        "  } catch (e) {\n" +
        "    return null;\n" +
        "  }\n" +
        "};\n");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeMethods1
  public void testRewritePrototypeMethods1() throws Exception {
    
    disableTypeCheck();
    checkTypes(RewritePrototypeMethodTestInput.INPUT,
               RewritePrototypeMethodTestInput.EXPECTED,
               RewritePrototypeMethodTestInput.EXPECTED_TYPE_CHECKING_OFF);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeMethods2
  public void testRewritePrototypeMethods2() throws Exception {
    
    enableTypeCheck(CheckLevel.ERROR);
    checkTypes(RewritePrototypeMethodTestInput.INPUT,
               RewritePrototypeMethodTestInput.EXPECTED,
               RewritePrototypeMethodTestInput.EXPECTED_TYPE_CHECKING_ON);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteChained
  public void testRewriteChained() throws Exception {
    String source = newlineJoin(
        "A.prototype.foo = function(){return this.b};",
        "B.prototype.bar = function(){};",
        "o.foo().bar()");

    String expected = newlineJoin(
        "var JSCompiler_StaticMethods_foo = ",
        "function(JSCompiler_StaticMethods_foo$self) {",
        "  return JSCompiler_StaticMethods_foo$self.b",
        "};",
        "var JSCompiler_StaticMethods_bar = ",
        "function(JSCompiler_StaticMethods_bar$self) {",
        "};",
        "JSCompiler_StaticMethods_bar(JSCompiler_StaticMethods_foo(o))");
    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteDeclIsExpressionStatement
  public void testRewriteDeclIsExpressionStatement() throws Exception {
    test(semicolonJoin(NoRewriteDeclarationUsedAsRValue.DECL,
                       NoRewriteDeclarationUsedAsRValue.CALL),
         "var JSCompiler_StaticMethods_foo =" +
         "function(JSCompiler_StaticMethods_foo$self) {};" +
         "JSCompiler_StaticMethods_foo(o)");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteDeclUsedAsAssignmentRhs
  public void testNoRewriteDeclUsedAsAssignmentRhs() throws Exception {
    testSame(semicolonJoin("var c = " + NoRewriteDeclarationUsedAsRValue.DECL,
                           NoRewriteDeclarationUsedAsRValue.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteDeclUsedAsCallArgument
  public void testNoRewriteDeclUsedAsCallArgument() throws Exception {
    testSame(semicolonJoin("f(" + NoRewriteDeclarationUsedAsRValue.DECL + ")",
                           NoRewriteDeclarationUsedAsRValue.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteInGlobalScope
  public void testRewriteInGlobalScope() throws Exception {
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "function(JSCompiler_StaticMethods_foo$self) {",
        "  return JSCompiler_StaticMethods_foo$self.x",
        "};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o);");

    test(NoRewriteIfNotInGlobalScopeTestInput.INPUT, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteIfNotInGlobalScope1
  public void testNoRewriteIfNotInGlobalScope1() throws Exception {
    testSame("if(true){" + NoRewriteIfNotInGlobalScopeTestInput.INPUT + "}");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteIfNotInGlobalScope2
  public void testNoRewriteIfNotInGlobalScope2() throws Exception {
    testSame("function enclosingFunction() {" +
             NoRewriteIfNotInGlobalScopeTestInput.INPUT +
             "}");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNamespaceFunctions
  public void testNoRewriteNamespaceFunctions() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.foo = function() {return this.x};",
        "a.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSingleDefinition1
  public void testRewriteSingleDefinition1() throws Exception {
    test(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                       NoRewriteMultipleDefinitionTestInput.CALL),
         NoRewriteMultipleDefinitionTestInput.SINGLE_DEFINITION_EXPECTED);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSingleDefinition2
  public void testRewriteSingleDefinition2() throws Exception {
    test(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                       NoRewriteMultipleDefinitionTestInput.CALL),
         NoRewriteMultipleDefinitionTestInput.SINGLE_DEFINITION_EXPECTED);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition1
  public void testNoRewriteMultipleDefinition1() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition2
  public void testNoRewriteMultipleDefinition2() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteMultipleDefinition3
  public void testNoRewriteMultipleDefinition3() throws Exception {
    testSame(semicolonJoin(NoRewriteMultipleDefinitionTestInput.SOURCE_A,
                           NoRewriteMultipleDefinitionTestInput.SOURCE_B,
                           NoRewriteMultipleDefinitionTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewritePrototypeNoObjectLiterals
  public void testRewritePrototypeNoObjectLiterals() throws Exception {
    test(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.REGULAR,
                       NoRewritePrototypeObjectLiteralsTestInput.CALL),
         "var JSCompiler_StaticMethods_foo = " +
         "function(JSCompiler_StaticMethods_foo$self) {};" +
         "JSCompiler_StaticMethods_foo(o)");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewritePrototypeObjectLiterals1
  public void testNoRewritePrototypeObjectLiterals1() throws Exception {
    testSame(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.OBJ_LIT,
                           NoRewritePrototypeObjectLiteralsTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewritePrototypeObjectLiterals2
  public void testNoRewritePrototypeObjectLiterals2() throws Exception {
    testSame(semicolonJoin(NoRewritePrototypeObjectLiteralsTestInput.OBJ_LIT,
                           NoRewritePrototypeObjectLiteralsTestInput.REGULAR,
                           NoRewritePrototypeObjectLiteralsTestInput.CALL));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteExternalMethods1
  public void testNoRewriteExternalMethods1() throws Exception {
    testSame("a.externalMethod()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteExternalMethods2
  public void testNoRewriteExternalMethods2() throws Exception {
    testSame("A.prototype.externalMethod = function(){}; o.externalMethod()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteCodingConvention
  public void testNoRewriteCodingConvention() throws Exception {
    
    testSame("a.prototype._foo = function() {};");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteNoVarArgs
  public void testRewriteNoVarArgs() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");

    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "  function(JSCompiler_StaticMethods_foo$self, args) {return args};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o)");

    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteVarArgs
  public void testNoRewriteVarArgs() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(var_args) {return arguments};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteCallReference
  public void testRewriteCallReference() throws Exception {
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "function(JSCompiler_StaticMethods_foo$self) {",
        "  return JSCompiler_StaticMethods_foo$self.x",
        "};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o);");

    test(NoRewriteNonCallReferenceTestInput.BASE + "o.foo()", expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNoReferences
  public void testNoRewriteNoReferences() throws Exception {
    testSame(NoRewriteNonCallReferenceTestInput.BASE);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNonCallReference
  public void testNoRewriteNonCallReference() throws Exception {
    testSame(NoRewriteNonCallReferenceTestInput.BASE + "o.foo && o.foo()");
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteNoNestedFunction
  public void testRewriteNoNestedFunction() throws Exception {
    test(semicolonJoin(
             NoRewriteNestedFunctionTestInput.PREFIX + "}",
             NoRewriteNestedFunctionTestInput.SUFFIX,
             NoRewriteNestedFunctionTestInput.INNER),
         semicolonJoin(
             NoRewriteNestedFunctionTestInput.EXPECTED_PREFIX + "}",
             NoRewriteNestedFunctionTestInput.EXPECTED_SUFFIX,
             "var JSCompiler_StaticMethods_bar=" +
             "function(JSCompiler_StaticMethods_bar$self){}",
             "JSCompiler_StaticMethods_bar(o)"));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNestedFunction
  public void testNoRewriteNestedFunction() throws Exception {
    test(NoRewriteNestedFunctionTestInput.PREFIX +
         NoRewriteNestedFunctionTestInput.INNER + "};" +
         NoRewriteNestedFunctionTestInput.SUFFIX,
         NoRewriteNestedFunctionTestInput.EXPECTED_PREFIX +
         NoRewriteNestedFunctionTestInput.INNER + "};" +
         NoRewriteNestedFunctionTestInput.EXPECTED_SUFFIX);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod
  public void testRewriteImplementedMethod() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    String expected = newlineJoin(
        "function a(){}",
        "var JSCompiler_StaticMethods_foo = ",
        "  function(JSCompiler_StaticMethods_foo$self, args) {return args};",
        "var o = new a;",
        "JSCompiler_StaticMethods_foo(o)");
    test(source, expected);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod2
  public void testRewriteImplementedMethod2() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype['foo'] = function(args) {return args};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod3
  public void testRewriteImplementedMethod3() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype.foo = function(args) {return args};",
        "var o = new a;",
        "o['foo']");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethod4
  public void testRewriteImplementedMethod4() throws Exception {
    String source = newlineJoin(
        "function a(){}",
        "a.prototype['foo'] = function(args) {return args};",
        "var o = new a;",
        "o['foo']");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteImplementedMethodInObj
  public void testRewriteImplementedMethodInObj() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {foo: function(args) {return args}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteGet1
  public void testNoRewriteGet1() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {get foo(){return f}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteGet2
  public void testNoRewriteGet2() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {get foo(){return 1}};",
        "var o = new a;",
        "o.foo");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteSet1
  public void testNoRewriteSet1() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {set foo(a){}};",
        "var o = new a;",
        "o.foo()");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteSet2
  public void testNoRewriteSet2() throws Exception {
    
    String source = newlineJoin(
        "function a(){}",
        "a.prototype = {set foo(a){}};",
        "var o = new a;",
        "o.foo = 1");
    testSame(source);
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteNotImplementedMethod
  public void testNoRewriteNotImplementedMethod() throws Exception {
    testSame(newlineJoin("function a(){}",
                         "var o = new a;",
                         "o.foo()"));
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule1
  public void testRewriteSameModule1() throws Exception {
    JSModule[] modules = createModuleStar(
        
        semicolonJoin(ModuleTestInput.DEFINITION,
                      ModuleTestInput.USE),
        
        "");

    test(modules, new String[] {
        
        semicolonJoin(ModuleTestInput.REWRITTEN_DEFINITION,
                      ModuleTestInput.REWRITTEN_USE),
        
        "",
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule2
  public void testRewriteSameModule2() throws Exception {
    JSModule[] modules = createModuleStar(
        
        "",
        
        semicolonJoin(ModuleTestInput.DEFINITION,
                      ModuleTestInput.USE));

    test(modules, new String[] {
        
        "",
        
        semicolonJoin(ModuleTestInput.REWRITTEN_DEFINITION,
                      ModuleTestInput.REWRITTEN_USE)
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteSameModule3
  public void testRewriteSameModule3() throws Exception {
    JSModule[] modules = createModuleStar(
        
        semicolonJoin(ModuleTestInput.USE,
                      ModuleTestInput.DEFINITION),
        
        "");

    test(modules, new String[] {
        
        semicolonJoin(ModuleTestInput.REWRITTEN_USE,
                      ModuleTestInput.REWRITTEN_DEFINITION),
        
        ""
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testRewriteDefinitionBeforeUse
  public void testRewriteDefinitionBeforeUse() throws Exception {
    JSModule[] modules = createModuleStar(
        
        ModuleTestInput.DEFINITION,
        
        ModuleTestInput.USE);

    test(modules, new String[] {
        
        ModuleTestInput.REWRITTEN_DEFINITION,
        
        ModuleTestInput.REWRITTEN_USE
      });
  }

// com.google.javascript.jscomp.DevirtualizePrototypeMethodsTest::testNoRewriteUseBeforeDefinition
  public void testNoRewriteUseBeforeDefinition() throws Exception {
    JSModule[] modules = createModuleStar(
        
        ModuleTestInput.USE,
        
        ModuleTestInput.DEFINITION);

    testSame(modules);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType1
  public void testOneType1() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType2
  public void testOneType2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = {a: 0};\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    
    
    String desired = "{a=[[Foo.prototype]]}";
    String expected = "{}";
    testSets(false, js, js, expected);

    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testOneType3
  public void testOneType3() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    
    
    String desired = "{a=[[Foo.prototype]]}";
    String expected = "{}";
    testSets(false, js, js, expected);

    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testPrototypeAndInstance
  public void testPrototypeAndInstance() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;";
    testSets(false, js, js, "{a=[[Foo.prototype]]}");
    testSets(true, js, js, "{a=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes1
  public void testTwoTypes1() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes2
  public void testTwoTypes2() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = {a: 0};"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype = {a: 0};"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";

    String output = ""
        + "function Foo(){}"
        + "Foo.prototype = {Foo_prototype$a: 0};"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype = {Bar_prototype$a: 0};"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";

    
    
    testSets(false, js, js, "{}");
    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoTypes3
  public void testTwoTypes3() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype = { get a() {return  0},"
        + "                  set a(b) {} };\n"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;";

    String output = ""
        + "function Foo(){}"
        + "Foo.prototype = { get Foo_prototype$a() {return  0},"
        + "                  set Foo_prototype$a(b) {} };\n"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "function Bar(){}"
        + "Bar.prototype = { get Bar_prototype$a() {return  0},"
        + "                  set Bar_prototype$a(b) {} };\n"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0";

    
    
    testSets(false, js, js, "{}");
    
    
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoFields
  public void testTwoFields() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "Foo.prototype.b = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + "F.b = 0;";
    String output = "function Foo(){}Foo.prototype.a=0;Foo.prototype.b=0;"
        + "var F=new Foo;F.a=0;F.b=0";
    testSets(false, js, output, "{a=[[Foo.prototype]], b=[[Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Foo.prototype]], b=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTwoSeparateFieldsTwoTypes
  public void testTwoSeparateFieldsTwoTypes() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + "Foo.prototype.b = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 0;"
        + "F.b = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "Bar.prototype.b = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;"
        + "B.b = 0;";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "Foo.prototype.Foo_prototype$b=0;"
        + "var F=new Foo;"
        + "F.Foo_prototype$a=0;"
        + "F.Foo_prototype$b=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.Bar_prototype$b=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0;"
        + "B.Bar_prototype$b=0";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
                                + " b=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
                               + " b=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionType
  public void testUnionType() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;\n"
        + "B = new Foo;\n"
        + "B.a = 0;\n"
        + " function Baz() {}\n"
        + "Baz.prototype.a = 0;\n";
    testSets(false, js,
             "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testIgnoreUnknownType
  public void testIgnoreUnknownType() {
    String js = ""
        + "\n"
        + "function Foo() {}\n"
        + "Foo.prototype.blah = 3;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.blah = 0;\n"
        + "var U = function() { return {} };\n"
        + "U().blah();";
    String expected = ""
        + "function Foo(){}Foo.prototype.blah=3;var F = new Foo;F.blah=0;"
        + "var U=function(){return{}};U().blah()";
    testSets(false, js, expected, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, expected, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionTypeTwoFields
  public void testUnionTypeTwoFields() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "Foo.prototype.b = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "Bar.prototype.b = 0;\n"
        + "\n"
        + "var B = new Bar;\n"
        + "B.a = 0;\n"
        + "B.b = 0;\n"
        + "B = new Foo;\n"
        + " function Baz() {}\n"
        + "Baz.prototype.a = 0;\n"
        + "Baz.prototype.b = 0;\n";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Bar_prototype$a=0;"
        + "Foo.prototype.Bar_prototype$b=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.Bar_prototype$b=0;"
        + "var B=new Bar;"
        + "B.Bar_prototype$a=0;"
        + "B.Bar_prototype$b=0;"
        + "function Baz(){}"
        + "Baz.prototype.a$Baz_prototype=0;"
        + "Baz.prototype.b$Baz_prototype=0;";
    testSets(false, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]],"
                 + " b=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype], [Baz.prototype]],"
                 + " b=[[Bar.prototype, Foo.prototype], [Baz.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testCast
  public void testCast() {
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;"
        + "\n"
        + "var F = new Foo;\n"
        + "((F)).a = 0;";
    String output = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var F=new Foo;F.Bar_prototype$a=0;";
    String ttOutput = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var F=new Foo;F.Unique$1$a=0;";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, ttOutput,
        "{a=[[Bar.prototype], [Foo.prototype], [Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testConstructorFields
  public void testConstructorFields() {
    String js = ""
      + "\n"
      + "var Foo = function() { this.a = 0; };\n"
      + " function Bar() {}\n"
      + "Bar.prototype.a = 0;"
      + "new Foo";
    String output = ""
        + "var Foo=function(){this.Foo$a=0};"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "new Foo";
    String ttOutput = ""
        + "var Foo=function(){this.Foo_prototype$a=0};"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "new Foo";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo]]}");
    testSets(true, js, ttOutput, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testStaticProperty
  public void testStaticProperty() {
    String js = ""
      + " function Foo() {} \n"
      + " function Bar() {}\n"
      + "Foo.a = 0;"
      + "Bar.a = 0;";
    String output = ""
        + "function Foo(){}"
        + "function Bar(){}"
        + "Foo.function__new_Foo___undefined$a = 0;"
        + "Bar.function__new_Bar___undefined$a = 0;";

    testSets(false, js, output,
        "{a=[[function (new:Bar): undefined]," +
        " [function (new:Foo): undefined]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSupertypeWithSameField
  public void testSupertypeWithSameField() {
    String js = ""
      + " function Foo() {}\n"
      + "Foo.prototype.a = 0;\n"
      + " function Bar() {}\n"
      + "\n"
      + "Bar.prototype.a = 0;\n"
      + " var B = new Bar;\n"
      + "B.a = 0;"
      + " function Baz() {}\n"
      + "Baz.prototype.a = function(){};\n";

    String output = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Foo_prototype$a=0;"
        + "var B = new Bar;B.Foo_prototype$a=0;"
        + "function Baz(){}Baz.prototype.Baz_prototype$a=function(){};";
    String ttOutput = ""
        + "function Foo(){}Foo.prototype.Foo_prototype$a=0;"
        + "function Bar(){}Bar.prototype.Bar_prototype$a=0;"
        + "var B = new Bar;B.Bar_prototype$a=0;"
        + "function Baz(){}Baz.prototype.Baz_prototype$a=function(){};";
    testSets(false, js, output, "{a=[[Baz.prototype], [Foo.prototype]]}");
    testSets(true, js, ttOutput,
        "{a=[[Bar.prototype], [Baz.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testScopedType
  public void testScopedType() {
    String js = ""
        + "var g = {};\n"
        + " g.Foo = function() {}\n"
        + "g.Foo.prototype.a = 0;"
        + " g.Bar = function() {}\n"
        + "g.Bar.prototype.a = 0;";
    String output = ""
        + "var g={};"
        + "g.Foo=function(){};"
        + "g.Foo.prototype.g_Foo_prototype$a=0;"
        + "g.Bar=function(){};"
        + "g.Bar.prototype.g_Bar_prototype$a=0;";
    testSets(false, js, output, "{a=[[g.Bar.prototype], [g.Foo.prototype]]}");
    testSets(true, js, output, "{a=[[g.Bar.prototype], [g.Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnresolvedType
  public void testUnresolvedType() {
    
    String js = ""
        + "var g = {};"
        + " "
        + "var Foo = function() {};\n"
        + "Foo.prototype.a = 0;"
        + " var Bar = function() {};\n"
        + "Bar.prototype.a = 0;";
    String output = ""
        + "var g={};"
        + "var Foo=function(){};"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var Bar=function(){};"
        + "Bar.prototype.Bar_prototype$a=0;";
    testSets(false, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES,
        js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testNamedType
  public void testNamedType() {
    String js = ""
        + "var g = {};"
        + " var Foo = function() {}\n"
        + "Foo.prototype.a = 0;"
        + " var Bar = function() {}\n"
        + "Bar.prototype.a = 0;"
        + " g.Late = function() {}";
    String output = ""
        + "var g={};"
        + "var Foo=function(){};"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "var Bar=function(){};"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "g.Late = function(){}";
    testSets(false, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, js, output, "{a=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnknownType
  public void testUnknownType() {
    String js = ""
        + " var Foo = function() {};\n"
        + " var Bar = function() {};\n"
        + " function fun() {}\n"
        + "Foo.prototype.a = fun();\n"
        + "fun().a;\n"
        + "Bar.prototype.a = 0;";
    String ttOutput = ""
        + "var Foo=function(){};\n"
        + "var Bar=function(){};\n"
        + "function fun(){}\n"
        + "Foo.prototype.Foo_prototype$a=fun();\n"
        + "fun().Unique$1$a;\n"
        + "Bar.prototype.Bar_prototype$a=0;";
    testSets(false, js, js, "{}");
    testSets(true, BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES, js, ttOutput,
             "{a=[[Bar.prototype], [Foo.prototype], [Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testEnum
  public void testEnum() {
    String js = ""
      + " var En = {\n"
      + "  A: 'first',\n"
      + "  B: 'second'\n"
      + "};\n"
      + "var EA = En.A;\n"
      + "var EB = En.B;\n"
      + " function Foo(){};\n"
      + "Foo.prototype.A = 0;\n"
      + "Foo.prototype.B = 0;\n";
    String output = ""
        + "var En={A:'first',B:'second'};"
        + "var EA=En.A;"
        + "var EB=En.B;"
        + "function Foo(){};"
        + "Foo.prototype.Foo_prototype$A=0;"
        + "Foo.prototype.Foo_prototype$B=0";
    String ttOutput = ""
        + "var En={A:'first',B:'second'};"
        + "var EA=En.A;"
        + "var EB=En.B;"
        + "function Foo(){};"
        + "Foo.prototype.Foo_prototype$A=0;"
        + "Foo.prototype.Foo_prototype$B=0";
    testSets(false, js, output, "{A=[[Foo.prototype]], B=[[Foo.prototype]]}");
    testSets(true, js, ttOutput, "{A=[[Foo.prototype]], B=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUntypedExterns
  public void testUntypedExterns() {
    String externs =
        BaseJSTypeTestCase.ALL_NATIVE_EXTERN_TYPES
        + "var window;"
        + "window.alert = function() {x};";
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + "Foo.prototype.alert = 0;\n"
        + "Foo.prototype.window = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "Bar.prototype.alert = 0;\n"
        + "Bar.prototype.window = 0;\n"
        + "window.alert();";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$a=0;"
        + "Foo.prototype.alert=0;"
        + "Foo.prototype.Foo_prototype$window=0;"
        + "function Bar(){}"
        + "Bar.prototype.Bar_prototype$a=0;"
        + "Bar.prototype.alert=0;"
        + "Bar.prototype.Bar_prototype$window=0;"
        + "window.alert();";

    testSets(false, externs, js, output, "{a=[[Bar.prototype], [Foo.prototype]]"
             + ", window=[[Bar.prototype], [Foo.prototype]]}");
    testSets(true, externs, js, output, "{a=[[Bar.prototype], [Foo.prototype]],"
             + " window=[[Bar.prototype], [Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionTypeInvalidation
  public void testUnionTypeInvalidation() {
    String externs = ""
        + " function Baz() {}"
        + "Baz.prototype.a";
    String js = ""
        + " function Ind() {this.a=0}\n"
        + " function Foo() {}\n"
        + "Foo.prototype.a = 0;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a = 0;\n"
        + "\n"
        + "var F = new Foo;\n"
        + "F.a = 1\n;"
        + "F = new Bar;\n"
        + "\n"
        + "var Z = new Baz;\n"
        + "Z.a = 1\n;"
        + "\n"
        + "var B = new Baz;\n"
        + "B.a = 1;\n"
        + "B = new Bar;\n";
    
    
    String output = ""
        + "function Ind() { this.Ind$a = 0; }"
        + "function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var F = new Foo;"
        + "F.a = 1;"
        + "F = new Bar;"
        + "var Z = new Baz;"
        + "Z.a = 1;"
        + "var B = new Baz;"
        + "B.a = 1;"
        + "B = new Bar;";
    String ttOutput = ""
        + "function Ind() { this.Unique$1$a = 0; }"
        + "function Foo() {}"
        + "Foo.prototype.a = 0;"
        + "function Bar() {}"
        + "Bar.prototype.a = 0;"
        + "var F = new Foo;"
        + "F.a = 1;"
        + "F = new Bar;"
        + "var Z = new Baz;"
        + "Z.a = 1;"
        + "var B = new Baz;"
        + "B.a = 1;"
        + "B = new Bar;";
    testSets(false, externs, js, output, "{a=[[Ind]]}");
    testSets(true, externs, js, ttOutput, "{a=[[Unique$1]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnionAndExternTypes
  public void testUnionAndExternTypes() {
    String externs = ""
      + " function Foo() { }"
      + "Foo.prototype.a = 4;\n";
    String js = ""
      + " function Bar() { this.a = 2; }\n"
      + " function Baz() { this.a = 3; }\n"
      + " function Buz() { this.a = 4; }\n"
      + " function T1() { this.a = 3; }\n"
      + " function T2() { this.a = 3; }\n"
      + " var b;\n"
      + " var c;\n"
      + " var d;\n"
      + "b.a = 5; c.a = 6; d.a = 7;";
    String output = ""
      + " function Bar() { this.a = 2; }\n"
      + " function Baz() { this.a = 3; }\n"
      + " function Buz() { this.a = 4; }\n"
      + " function T1() { this.T1$a = 3; }\n"
      + " function T2() { this.T2$a = 3; }\n"
      + " var b;\n"
      + " var c;\n"
      + " var d;\n"
      + "b.a = 5; c.a = 6; d.a = 7;";

    
    
    testSets(false, externs, js, output, "{a=[[T1], [T2]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testTypedExterns
  public void testTypedExterns() {
    String externs = ""
        + " function Window() {};\n"
        + "Window.prototype.alert;"
        + ""
        + "var window;";
    String js = ""
        + " function Foo() {}\n"
        + "Foo.prototype.alert = 0;\n"
        + "window.alert('blarg');";
    String output = ""
        + "function Foo(){}"
        + "Foo.prototype.Foo_prototype$alert=0;"
        + "window.alert('blarg');";
    testSets(false, externs, js, output, "{alert=[[Foo.prototype]]}");
    testSets(true, externs, js, output, "{alert=[[Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSubtypesWithSameField
  public void testSubtypesWithSameField() {
    String js = ""
        + " function Top() {}\n"
        + " function Foo() {}\n"
        + "Foo.prototype.a;\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + ""
        + "function foo(top) {\n"
        + "  var x = top.a;\n"
        + "}\n"
        + "foo(new Foo);\n"
        + "foo(new Bar);\n";
    testSets(false, js, "{}");
    testSets(true, js, "{a=[[Bar.prototype, Foo.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSupertypeReferenceOfSubtypeProperty
  public void testSupertypeReferenceOfSubtypeProperty() {
    String externs = ""
        + " function Ext() {}"
        + "Ext.prototype.a;";
    String js = ""
        + " function Foo() {}\n"
        + " function Bar() {}\n"
        + "Bar.prototype.a;\n"
        + ""
        + "function foo(foo) {\n"
        + "  var x = foo.a;\n"
        + "}\n";
    String result = ""
        + "function Foo() {}\n"
        + "function Bar() {}\n"
        + "Bar.prototype.Bar_prototype$a;\n"
        + "function foo(foo) {\n"
        + "  var x = foo.Bar_prototype$a;\n"
        + "}\n";
    testSets(false, externs, js, result, "{a=[[Bar.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralNotRenamed
  public void testObjectLiteralNotRenamed() {
    String js = ""
        + "var F = {a:'a', b:'b'};"
        + "F.a = 'z';";
    testSets(false, js, js, "{}");
    testSets(true, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralReflected
  public void testObjectLiteralReflected() {
    String js = ""
        + "var goog = {};"
        + "goog.reflect = {};"
        + "goog.reflect.object = function(x, y) { return y; };"
        + " function F() {}"
        + " F.prototype.foo = 3;"
        + " function G() {}"
        + " G.prototype.foo = 3;"
        + "goog.reflect.object(F, {foo: 5});";
    String result = ""
        + "var goog = {};"
        + "goog.reflect = {};"
        + "goog.reflect.object = function(x, y) { return y; };"
        + "function F() {}"
        + "F.prototype.F_prototype$foo = 3;"
        + "function G() {}"
        + "G.prototype.G_prototype$foo = 3;"
        + "goog.reflect.object(F, {F_prototype$foo: 5});";
    testSets(false, js, result, "{foo=[[F.prototype], [G.prototype]]}");
    testSets(true, js, result, "{foo=[[F.prototype], [G.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testObjectLiteralLends
  public void testObjectLiteralLends() {
    String js = ""
        + "var mixin = function(x) { return x; };"
        + " function F() {}"
        + " F.prototype.foo = 3;"
        + " function G() {}"
        + " G.prototype.foo = 3;"
        + "mixin( ({foo: 5}));";
    String result = ""
        + "var mixin = function(x) { return x; };"
        + "function F() {}"
        + "F.prototype.F_prototype$foo = 3;"
        + "function G() {}"
        + "G.prototype.G_prototype$foo = 3;"
        + "mixin( ({F_prototype$foo: 5}));";
    testSets(false, js, result, "{foo=[[F.prototype], [G.prototype]]}");
    testSets(true, js, result, "{foo=[[F.prototype], [G.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testClosureInherits
  public void testClosureInherits() {
    String js = ""
        + "var goog = {};"
        + "\n"
        + "goog.inherits = function(childCtor, parentCtor) {\n"
        + "  \n"
        + "  function tempCtor() {};\n"
        + "  tempCtor.prototype = parentCtor.prototype;\n"
        + "  childCtor.superClass_ = parentCtor.prototype;\n"
        + "  childCtor.prototype = new tempCtor();\n"
        + "  childCtor.prototype.constructor = childCtor;\n"
        + "};"
        + " function Top() {}\n"
        + "Top.prototype.f = function() {};"
        + " function Foo() {}\n"
        + "goog.inherits(Foo, Top);\n"
        + "Foo.prototype.f = function() {"
        + "  Foo.superClass_.f();"
        + "};\n"
        + " function Bar() {}\n"
        + "goog.inherits(Bar, Foo);\n"
        + "Bar.prototype.f = function() {"
        + "  Bar.superClass_.f();"
        + "};\n"
        + "(new Bar).f();\n";
    testSets(false, js, "{f=[[Top.prototype]]}");
    testSets(true, js, "{constructor=[[Bar.prototype, Foo.prototype]], "
                 + "f=[[Bar.prototype], [Foo.prototype], [Top.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeFunctionMethod
  public void testSkipNativeFunctionMethod() {
    String externs = ""
        + ""
        + "function Function(var_args) {}"
        + "Function.prototype.call = function() {};";
    String js = ""
        + " function Foo(){};"
        + ""
        + "function Bar() { Foo.call(this); };"; 
    testSame(externs, js, null);
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeObjectMethod
  public void testSkipNativeObjectMethod() {
    String externs = ""
        + " function Object(opt_v) {}"
        + "Object.prototype.hasOwnProperty;";
    String js = ""
        + " function Foo(){};"
        + "(new Foo).hasOwnProperty('x');";
    testSets(false, externs, js, js, "{}");
    testSets(true, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testExtendNativeType
  public void testExtendNativeType() {
    String externs = ""
        + ""
        + "function Date(opt_1, opt_2, opt_3, opt_4, opt_5, opt_6, opt_7) {}"
        + " Date.prototype.toString = function() {}";
    String js = ""
        + " function SuperDate() {};\n"
        + "(new SuperDate).toString();";
    testSets(true, externs, js, js, "{}");
    testSets(false, externs, js, js, "{}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testStringFunction
  public void testStringFunction() {
    
    
    String externs = ""
         + "function String(opt_str) {};\n"
         + "\n"
         + "String.prototype.toString = function() { };\n";
    String js = ""
         + " function Foo() {};\n"
         + "Foo.prototype.foo = function() {};\n"
         + "String.prototype.foo = function() {};\n"
         + "var a = 'str'.toString().foo();\n";
    String output = ""
         + "function Foo() {};\n"
         + "Foo.prototype.Foo_prototype$foo = function() {};\n"
         + "String.prototype.String_prototype$foo = function() {};\n"
         + "var a = 'str'.toString().String_prototype$foo();\n";

    testSets(false, externs, js, output,
             "{foo=[[Foo.prototype], [String.prototype]]}");
    testSets(true, externs, js, output,
             "{foo=[[Foo.prototype], [String.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testUnusedTypeInExterns
  public void testUnusedTypeInExterns() {
    String externs = ""
        + " function Foo() {};\n"
        + "Foo.prototype.a";
    String js = ""
        + " function Bar() {};\n"
        + "Bar.prototype.a;"
        + " function Baz() {};\n"
        + "Baz.prototype.a;";
    String output = ""
        + " function Bar() {};\n"
        + "Bar.prototype.Bar_prototype$a;"
        + " function Baz() {};\n"
        + "Baz.prototype.Baz_prototype$a";
    testSets(false, externs, js, output,
             "{a=[[Bar.prototype], [Baz.prototype]]}");
    testSets(true, externs, js, output,
             "{a=[[Bar.prototype], [Baz.prototype]]}");
  }

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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testInterfaceUnionWithCtor
  public void testInterfaceUnionWithCtor() {
    String js = ""
        + " function I() {};\n"
        + " I.prototype.addEventListener;\n"
        + " function Impl() {};\n"
        + " Impl.prototype.addEventListener;"
        + " function C() {};\n"
        + " C.prototype.addEventListener;"
        + ""
        + "function f(x) { x.addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(false, js, js,
        "{addEventListener=[[C.prototype, I.prototype, Impl.prototype]]}");

    
    
    String tightenedOutput = ""
        + "function I() {};\n"
        + "I.prototype.I_prototype$addEventListener;\n"
        + "function Impl() {};\n"
        + "Impl.prototype.C_prototype$addEventListener;"
        + "function C() {};\n"
        + "C.prototype.C_prototype$addEventListener;"
        + ""
        + "function f(x) { x.C_prototype$addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(true, js, tightenedOutput,
        "{addEventListener=[[C.prototype, Impl.prototype], [I.prototype]]}");
  }

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testExternInterfaceUnionWithCtor
  public void testExternInterfaceUnionWithCtor() {
    String externs = ""
        + " function I() {};\n"
        + " I.prototype.addEventListener;\n"
        + " function Impl() {};\n"
        + " Impl.prototype.addEventListener;";

    String js = ""
        + " function C() {};\n"
        + " C.prototype.addEventListener;"
        + ""
        + "function f(x) { x.addEventListener(); };\n"
        + "f(new C()); f(new Impl());";

    testSets(false, externs, js, js, "{}");
    testSets(true, externs, js, js, "{}");
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

// com.google.javascript.jscomp.DisambiguatePropertiesTest::testSkipNativeFunctionStaticProperty
  public void testSkipNativeFunctionStaticProperty() {
    String js = ""
      + "\n"
      + "function addSingletonGetter(ctor) { ctor.a; }\n"
      + " function Foo() {}\n"
      + "Foo.a = 0;"
      + " function Bar() {}\n"
      + "Bar.a = 0;";

    String output = ""
        + "function addSingletonGetter(ctor){ctor.a}"
        + "function Foo(){}"
        + "Foo.a=0;"
        + "function Bar(){}"
        + "Bar.a=0";

    testSets(false, js, output, "{}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression1
  public void testCanExposeExpression1() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "while(foo());", "foo");
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "while(x = goo()&&foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "while(x += goo()&&foo()){}", "foo");

    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "do{}while(foo());", "foo");
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "for(;foo(););", "foo");
    
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "for(;;foo());", "foo");
    
    

    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "switch(1){case foo():;}", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression2
  public void testCanExposeExpression2() {
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "var x = foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "if(foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "switch(foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "switch(foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "function f(){ return foo();}", "foo");

    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() && 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() || 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "x = foo() ? 0 : 1", "foo");
    helperCanExposeExpression(
        DecompositionType.MOVABLE, "(function(a){b = a})(foo())", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression3
  public void testCanExposeExpression3() {
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x = 0 && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x = 1 || foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "var x = 1 ? foo() : 0", "foo");

    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x = goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "x += goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "var x = goo() && foo()", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "if(goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "switch(goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "switch(goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE, "switch(x = goo() && foo()){}", "foo");
    helperCanExposeExpression(
        DecompositionType.DECOMPOSABLE,
        "function f(){ return goo() && foo();}", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression4
  public void testCanExposeExpression4() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "if (goo.a(1, foo()));", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression5
  public void testCanExposeExpression5() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "if (goo['a'](foo()));", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression6
  public void testCanExposeExpression6() {
    
    helperCanExposeExpression(
        DecompositionType.UNDECOMPOSABLE, "z:if (goo.a(1, foo()));", "foo");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testCanExposeExpression7
  public void testCanExposeExpression7() {
    
    helperCanExposeFunctionExpression(
        DecompositionType.MOVABLE,
        "(function(map){descriptions_=map})(\n" +
            "function(){\n" +
                "var ret={};\n" +
                "ret[INIT]='a';\n" +
                "ret[MIGRATION_BANNER_DISMISS]='b';\n" +
                "return ret\n" +
            "}()\n" +
        ");", 2);
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression1
  public void testMoveExpression1() {
    
    helperMoveExpression("foo()", "foo", "var temp$$0 = foo(); temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression2
  public void testMoveExpression2() {
    helperMoveExpression(
        "x = foo()",
        "foo",
        "var temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression3
  public void testMoveExpression3() {
    helperMoveExpression(
        "var x = foo()",
        "foo",
        "var temp$$0 = foo(); var x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression4
  public void testMoveExpression4() {
    helperMoveExpression(
        "if(foo()){}",
        "foo",
        "var temp$$0 = foo(); if (temp$$0);");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression5
  public void testMoveExpression5() {
    helperMoveExpression(
        "switch(foo()){}",
        "foo",
        "var temp$$0 = foo(); switch(temp$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression6
  public void testMoveExpression6() {
    helperMoveExpression(
        "switch(1 + foo()){}",
        "foo",
        "var temp$$0 = foo(); switch(1 + temp$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression7
  public void testMoveExpression7() {
    helperMoveExpression(
        "function f(){ return foo();}",
        "foo",
        "function f(){ var temp$$0 = foo(); return temp$$0;}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression8
  public void testMoveExpression8() {
    helperMoveExpression(
        "x = foo() && 1",
        "foo",
        "var temp$$0 = foo(); x = temp$$0 && 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression9
  public void testMoveExpression9() {
    helperMoveExpression(
        "x = foo() || 1",
        "foo",
        "var temp$$0 = foo(); x = temp$$0 || 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testMoveExpression10
  public void testMoveExpression10() {
    helperMoveExpression(
        "x = foo() ? 0 : 1",
        "foo",
        "var temp$$0 = foo(); x = temp$$0 ? 0 : 1");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression1
  public void testExposeExpression1() {
    helperExposeExpression(
        "x = 0 && foo()",
        "foo",
        "var temp$$0; if (temp$$0 = 0) temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression2
  public void testExposeExpression2() {
    helperExposeExpression(
        "x = 1 || foo()",
        "foo",
        "var temp$$0; if (temp$$0 = 1); else temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression3
  public void testExposeExpression3() {
    helperExposeExpression(
        "var x = 1 ? foo() : 0",
        "foo",
        "var temp$$0;" +
        " if (1) temp$$0 = foo(); else temp$$0 = 0;var x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression4
  public void testExposeExpression4() {
    helperExposeExpression(
        "goo() && foo()",
        "foo",
        "if (goo()) foo();");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression5
  public void testExposeExpression5() {
    helperExposeExpression(
        "x = goo() && foo()",
        "foo",
        "var temp$$0; if (temp$$0 = goo()) temp$$0 = foo(); x = temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression6
  public void testExposeExpression6() {
    helperExposeExpression(
        "var x = 1 + (goo() && foo())",
        "foo",
        "var temp$$0; if (temp$$0 = goo()) temp$$0 = foo();" +
        "var x = 1 + temp$$0;");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression7
  public void testExposeExpression7() {
    helperExposeExpression(
        "if(goo() && foo());",
        "foo",
        "var temp$$0;" +
        "if (temp$$0 = goo()) temp$$0 = foo();" +
        "if(temp$$0);");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression8
  public void testExposeExpression8() {
    helperExposeExpression(
        "switch(goo() && foo()){}",
        "foo",
        "var temp$$0;" +
        "if (temp$$0 = goo()) temp$$0 = foo();" +
        "switch(temp$$0){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression9
  public void testExposeExpression9() {
    helperExposeExpression(
        "switch(1 + goo() + foo()){}",
        "foo",
        "var temp_const$$0 = 1 + goo();" +
        "switch(temp_const$$0 + foo()){}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression10
  public void testExposeExpression10() {
    helperExposeExpression(
        "function f(){ return goo() && foo();}",
        "foo",
        "function f(){" +
          "var temp$$0; if (temp$$0 = goo()) temp$$0 = foo();" +
          "return temp$$0;" +
         "}");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeExpression11
  public void testExposeExpression11() {
    
    
    helperExposeExpression(
        "if (goo(1, goo(2), (1 ? foo() : 0)));",
        "foo",
        "var temp_const$$1 = goo;" +
        "var temp_const$$0 = goo(2);" +
        "var temp$$2;" +
        "if (1) temp$$2 = foo(); else temp$$2 = 0;" +
        "if (temp_const$$1(1, temp_const$$0, temp$$2));");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals1
  public void testExposePlusEquals1() {
    helperExposeExpression(
        "var x = 0; x += foo() + 1",
        "foo",
        "var x = 0; var temp_const$$0 = x;" +
        "x = temp_const$$0 + (foo() + 1);");

    helperExposeExpression(
        "var x = 0; y = (x += foo()) + x",
        "foo",
        "var x = 0; var temp_const$$0 = x;" +
        "y = (x = temp_const$$0 + foo()) + x");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals2
  public void testExposePlusEquals2() {
    helperExposeExpression(
        "var x = {}; x.a += foo() + 1",
        "foo",
        "var x = {}; var temp_const$$0 = x;" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "temp_const$$0.a = temp_const$$1 + (foo() + 1);");

    helperExposeExpression(
        "var x = {}; y = (x.a += foo()) + x.a",
        "foo",
        "var x = {}; var temp_const$$0 = x;" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "y = (temp_const$$0.a = temp_const$$1 + foo()) + x.a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals3
  public void testExposePlusEquals3() {
    helperExposeExpression(
        " var XX = {};\n" +
        "XX.a += foo() + 1",
        "foo",
        "var XX = {}; var temp_const$$0 = XX.a;" +
        "XX.a = temp_const$$0 + (foo() + 1);");

    helperExposeExpression(
        "var XX = {}; y = (XX.a += foo()) + XX.a",
        "foo",
        "var XX = {}; var temp_const$$0 = XX.a;" +
        "y = (XX.a = temp_const$$0 + foo()) + XX.a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals4
  public void testExposePlusEquals4() {
    helperExposeExpression(
        "var x = {}; goo().a += foo() + 1",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo();" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "temp_const$$0.a = temp_const$$1 + (foo() + 1);");

    helperExposeExpression(
        "var x = {}; y = (goo().a += foo()) + goo().a",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo();" +
        "var temp_const$$1 = temp_const$$0.a;" +
        "y = (temp_const$$0.a = temp_const$$1 + foo()) + goo().a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposePlusEquals5
  public void testExposePlusEquals5() {
    helperExposeExpression(
        "var x = {}; goo().a.b += foo() + 1",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo().a;" +
        "var temp_const$$1 = temp_const$$0.b;" +
        "temp_const$$0.b = temp_const$$1 + (foo() + 1);");

    helperExposeExpression(
        "var x = {}; y = (goo().a.b += foo()) + goo().a",
        "foo",
        "var x = {};" +
        "var temp_const$$0 = goo().a;" +
        "var temp_const$$1 = temp_const$$0.b;" +
        "y = (temp_const$$0.b = temp_const$$1 + foo()) + goo().a");
  }

// com.google.javascript.jscomp.ExpressionDecomposerTest::testExposeObjectLit1
  public void testExposeObjectLit1() {
    
    
    
    
    helperMoveExpression(
        "var x = {get a() {}, b: foo()};",
        "foo",
        "var temp$$0=foo();var x = {get a() {}, b: temp$$0};");

    helperMoveExpression(
        "var x = {set a(p) {}, b: foo()};",
        "foo",
        "var temp$$0=foo();var x = {set a(p) {}, b: temp$$0};");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbol
  public void testExportSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolDefinedInVar
  public void testExportSymbolDefinedInVar() throws Exception {
    compileAndCheck("var a = function(d, e, f) {};" +
                    "goog.exportSymbol('foobar', a)",
                    "\n" +
                    "var foobar = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportProperty
  public void testExportProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'cprop', a.b.c)",
                    "var a = {};\n" +
                    "a.b = {};\n" +
                    "\n" +
                    "a.b.cprop = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple
  public void testExportMultiple() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('a.b', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var a = {};\n" +
                    "\n" +
                    "a.b = function(p1) {\n};\n" +
                    "\n" +
                    "a.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "a.b.prototype.c = function(g, h, i) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple2
  public void testExportMultiple2() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('hello', a);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);" +
                    "goog.exportProperty(a.b.prototype, 'c', a.b.prototype.c);",

                    "var hello = {};\n" +
                    "hello.b = {};\n" +
                    "\n" +
                    "hello.b.c = function(d, e, f) {\n};\n" +
                    "\n" +
                    "hello.b.prototype.c = function(g, h, i) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportMultiple3
  public void testExportMultiple3() throws Exception {
    compileAndCheck("var a = {}; a.b = function(p1) {}; " +
                    "a.b.c = function(d, e, f) {};" +
                    "a.b.prototype.c = function(g, h, i) {};" +
                    "goog.exportSymbol('prefix', a.b);" +
                    "goog.exportProperty(a.b, 'c', a.b.c);",

                    "\n" +
                    "var prefix = function(p1) {\n};\n" +
                    "\n" +
                    "prefix.c = function(d, e, f) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonStaticSymbol
  public void testExportNonStaticSymbol() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; var d = {}; a.b.c = d;" +
                    "goog.exportSymbol('foobar', a.b.c)",
                    "var foobar = {};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonStaticSymbol2
  public void testExportNonStaticSymbol2() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; var d = null; a.b.c = d;" +
                    "goog.exportSymbol('foobar', a.b.c())",
                    "var foobar = {};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportNonexistentProperty
  public void testExportNonexistentProperty() throws Exception {
    compileAndCheck("var a = {}; a.b = {}; a.b.c = function(d, e, f) {};" +
                    "goog.exportProperty(a.b, 'none', a.b.none)",
                    "var a = {};\n" +
                    "a.b = {};\n" +
                    "a.b.none = {};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithTypeAnnotation
  public void testExportSymbolWithTypeAnnotation() {

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1, param2) {" +
                      "return param1 + param2;" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function(param1, param2) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithoutTypeCheck
  public void testExportSymbolWithoutTypeCheck() {
    
    
    setRunCheckTypes(false);

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function(param1, param2) {" +
                      "return param1 + param2;" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "var externalName = function(param1, param2) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithConstructor
  public void testExportSymbolWithConstructor() {
    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function() {" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "\n" +
                    "var externalName = function() {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportSymbolWithConstructorWithoutTypeCheck
  public void testExportSymbolWithConstructorWithoutTypeCheck() {
    
    
    
    
    

    setRunCheckTypes(false);

    compileAndCheck("var internalName;\n" +
                    "\n" +
                    "internalName = function() {" +
                    "};" +
                    "goog.exportSymbol('externalName', internalName)",
                    "var externalName = function() {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithOptionalArguments
  public void testExportFunctionWithOptionalArguments() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "  return 6;\n" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportFunctionWithVariableArguments
  public void testExportFunctionWithVariableArguments() {
    compileAndCheck("var internalName;\n" +
        "\n" +
        "internalName = function(a) {" +
        "  return 6;\n" +
        "};" +
        "goog.exportSymbol('externalName', internalName)",
        "\n" +
        "var externalName = function(a) {\n};\n");
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportEnum
   public void testExportEnum() {
     compileAndCheck(
         " var E = {A:1, B:2};" +
         "goog.exportSymbol('E', E);",
         
         
         "var E = {};\n"
     );
   }

// com.google.javascript.jscomp.ExternExportsPassTest::testExportDontEmitPrototypePathPrefix
  public void testExportDontEmitPrototypePathPrefix() {
    compileAndCheck(
        "\n" +
        "var Foo = function() {};" +
        "\n" +
        "Foo.prototype.m = function() {return 6;};\n" +
        "goog.exportSymbol('Foo', Foo);\n" +
        "goog.exportProperty(Foo.prototype, 'm', Foo.prototype.m);",
        "\n" +
        "var Foo = function() {\n};\n" +
        "\n" +
        "Foo.prototype.m = function() {\n};\n"
    );
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testUseExportsAsExterns
  public void testUseExportsAsExterns() {
    String librarySource =
    "\n" +
    "var InternalName = function(a) {" +
    "};" +
    "goog.exportSymbol('ExternalName', InternalName)";

    String clientSource =
      "var a = new ExternalName(6);\n" +
      "\n" +
      "var b = function(x) {};";

    Result libraryCompileResult = compileAndExportExterns(librarySource);

    assertEquals(0, libraryCompileResult.warnings.length);
    assertEquals(0, libraryCompileResult.errors.length);

    String generatedExterns = libraryCompileResult.externExport;

    Result clientCompileResult = compileAndExportExterns(clientSource,
        generatedExterns);

    assertEquals(0, clientCompileResult.warnings.length);
    assertEquals(0, clientCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testWarnOnExportFunctionWithUnknownReturnType
  public void testWarnOnExportFunctionWithUnknownReturnType() {
    String librarySource =
      "var InternalName = function() {" +
      "  return 6;" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(1, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }

// com.google.javascript.jscomp.ExternExportsPassTest::testDontWarnOnExportConstructorWithUnknownReturnType
  public void testDontWarnOnExportConstructorWithUnknownReturnType() {
    String librarySource =
      "\n " +
      "var InternalName = function() {" +
      "};" +
      "goog.exportSymbol('ExternalName', InternalName)";

      Result libraryCompileResult = compileAndExportExterns(librarySource);

      assertEquals(0, libraryCompileResult.warnings.length);
      assertEquals(0, libraryCompileResult.errors.length);
  }
