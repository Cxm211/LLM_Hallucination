        final void newAttribute() {
            if (attributes == null)
                attributes = new Attributes();
            if (pendingAttributeName != null) {
                pendingAttributeName = pendingAttributeName.trim();
                if (pendingAttributeName.length() > 0) {
                    String value;
                    if (hasPendingAttributeValue)
                        value = pendingAttributeValue.length() > 0 ? pendingAttributeValue.toString() : pendingAttributeValueS;
                    else if (hasEmptyAttributeValue)
                        value = "";
                    else
                        value = null;
                    attributes.add(pendingAttributeName, value);
                }
            }
            pendingAttributeName = null;
            hasEmptyAttributeValue = false;
            hasPendingAttributeValue = false;
            reset(pendingAttributeValue);
            pendingAttributeValueS = null;
        }