final void newAttribute() {
            if (attributes == null)
                attributes = new Attributes();

            if (pendingAttributeName != null) {
                // the tokeniser has skipped whitespace control chars, but trimming could collapse to empty for other control codes, so verify here
                pendingAttributeName = pendingAttributeName.trim();
                // remove any remaining control characters from the attribute name
                if (pendingAttributeName.length() != 0) {
                    StringBuilder cleaned = new StringBuilder(pendingAttributeName.length());
                    for (int i = 0; i < pendingAttributeName.length(); i++) {
                        char c = pendingAttributeName.charAt(i);
                        if (c > 0x1F && c != 0x7F) // strip non-whitespace control chars
                            cleaned.append(c);
                    }
                    pendingAttributeName = cleaned.toString();
                }
                if (pendingAttributeName.length() > 0) {
                    Attribute attribute;
                    if (hasPendingAttributeValue)
                        attribute = new Attribute(pendingAttributeName,
                            pendingAttributeValue.length() > 0 ? pendingAttributeValue.toString() : pendingAttributeValueS);
                    else if (hasEmptyAttributeValue)
                        attribute = new Attribute(pendingAttributeName, "");
                    else
                        attribute = new BooleanAttribute(pendingAttributeName);
                    attributes.put(attribute);
                }
            }
            pendingAttributeName = null;
            hasEmptyAttributeValue = false;
            hasPendingAttributeValue = false;
            reset(pendingAttributeValue);
            pendingAttributeValueS = null;
        }