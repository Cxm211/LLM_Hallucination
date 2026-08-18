  String toStringHelper(boolean forAnnotations) {
    if (hasReferenceName()) {
      return getReferenceName();
    } else if (prettyPrint) {
      // Don't pretty print recursively.
      prettyPrint = false;
      try {
        // Use a tree set so that the properties are sorted.
        Set<String> propertyNames = Sets.newTreeSet();
        for (ObjectType current = this;
             current != null && !current.isNativeObjectType() &&
                 propertyNames.size() <= MAX_PRETTY_PRINTED_PROPERTIES;
             current = current.getImplicitPrototype()) {
          Set<String> ownProps = current.getOwnPropertyNames();
          if (ownProps != null) {
            propertyNames.addAll(ownProps);
          }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        int i = 0;
        for (String property : propertyNames) {
          if (i > 0) {
            sb.append(", ");
          }

          sb.append(property);
          sb.append(": ");
          JSType propType = getPropertyType(property);
          sb.append(propType == null ? "?" : propType.toString());

          ++i;
          if (i == MAX_PRETTY_PRINTED_PROPERTIES) {
            sb.append(", ...");
            break;
          }
        }

        sb.append("}");
        return sb.toString();
      } finally {
        prettyPrint = true;
      }
    } else {
      return "{...}";
    }
  }