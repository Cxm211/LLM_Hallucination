  public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, externs, new ProcessExterns());
    NodeTraversal.traverse(compiler, root, new ProcessProperties());

    Set<String> reservedNames =
        new HashSet<String>(externedNames.size() + quotedNames.size());
    reservedNames.addAll(externedNames);
    reservedNames.addAll(quotedNames);

    int numRenamedPropertyNames = 0;
    int numSkippedPropertyNames = 0;
    Set<Property> propsByFreq = new TreeSet<Property>(FREQUENCY_COMPARATOR);
    for (Property p : propertyMap.values()) {
      if (!p.skipAmbiguating) {
        ++numRenamedPropertyNames;
        computeRelatedTypes(p.type);
        propsByFreq.add(p);
      } else {
        ++numSkippedPropertyNames;
        reservedNames.add(p.oldName);
      }
    }

    PropertyGraph graph = new PropertyGraph(Lists.newLinkedList(propsByFreq));
    GraphColoring<Property, Void> coloring =
        new GreedyGraphColoring<Property, Void>(graph, FREQUENCY_COMPARATOR);
    int numNewPropertyNames = coloring.color();

    NameGenerator nameGen = new NameGenerator(
        reservedNames, "", reservedCharacters);
    for (int i = 0; i < numNewPropertyNames; ++i) {
      colorMap.put(i, nameGen.generateNextName());
    }
    for (GraphNode<Property, Void> node : graph.getNodes()) {
      node.getValue().newName = colorMap.get(node.getAnnotation().hashCode());
      renamingMap.put(node.getValue().oldName, node.getValue().newName);
    }

    // Update the string nodes.
    for (Node n : stringNodesToRename) {
      String oldName = n.getString();
      Property p = propertyMap.get(oldName);
      if (p != null && p.newName != null) {
        Preconditions.checkState(oldName.equals(p.oldName));
        if (!p.newName.equals(oldName)) {
          n.setString(p.newName);
          compiler.reportCodeChange();
        }
      }
    }

    logger.info("Collapsed " + numRenamedPropertyNames + " properties into "
                + numNewPropertyNames + " and skipped renaming "
                + numSkippedPropertyNames + " properties.");
  }

    public boolean isIndependentOf(Property prop) {
      if (typesRelatedToSet.intersects(prop.typesSet)) {
        return false;
      }
      return !getRelated(prop.type).intersects(typesInSet);
    }

    public void addNode(Property prop) {
      typesInSet.or(prop.typesSet);
      typesRelatedToSet.or(getRelated(prop.type));
    }

  private JSType getJSType(Node n) {
    JSType jsType = n.getJSType();
    if (jsType == null) {
      // TODO(user): This branch indicates a compiler bug, not worthy of
      // halting the compilation but we should log this and analyze to track
      // down why it happens. This is not critical and will be resolved over
      // time as the type checker is extended.
      return compiler.getTypeRegistry().getNativeType(
          JSTypeNative.UNKNOWN_TYPE);
    } else {
      return jsType;
    }
  }

    private void addNonUnionType(JSType newType) {
      if (skipAmbiguating || isInvalidatingType(newType)) {
        skipAmbiguating = true;
        return;
      }

      if (type == null) {
        type = newType;
      } else {
        type = type.getLeastSupertype(newType);
      }
      typesSet.set(getIntForType(newType));
    }

    private FunctionType findOverriddenFunction(
        ObjectType ownerType, String propName) {
      // First, check to see if the property is implemented
      // on a superclass.
      JSType propType = ownerType.getPropertyType(propName);
      if (propType instanceof FunctionType) {
        return (FunctionType) propType;
      }
        // If it's not, then check to see if it's implemented
        // on an implemented interface.

      return null;
    }

// trigger testcase
public void testImplementsAndExtends() {
    String js = ""
        + "/** @interface */ function Foo() {}\n"
        + "/**\n"
        + " * @constructor\n"
        + " */\n"
        + "function Bar(){}\n"
        + "Bar.prototype.y = function() { return 3; };\n"
        + "/**\n"
        + " * @constructor\n"
        + " * @extends {Bar}\n"
        + " * @implements {Foo}\n"
        + " */\n"
        + "function SubBar(){ }\n"
        + "/** @param {Foo} x */ function f(x) { x.z = 3; }\n"
        + "/** @param {SubBar} x */ function g(x) { x.z = 3; }";
    String output = ""
        + "function Foo(){}\n"
        + "function Bar(){}\n"
        + "Bar.prototype.b = function() { return 3; };\n"
        + "function SubBar(){}\n"
        + "function f(x) { x.a = 3; }\n"
        + "function g(x) { x.a = 3; }";
    test(js, output);
  }

public void testIssue86() throws Exception {
    testTypes(
        "/** @interface */ function I() {}" +
        "/** @return {number} */ I.prototype.get = function(){};" +
        "/** @constructor \n * @implements {I} */ function F() {}" +
        "/** @override */ F.prototype.get = function() { return true; };",
        "inconsistent return type\n" +
        "found   : boolean\n" +
        "required: number");
  }
