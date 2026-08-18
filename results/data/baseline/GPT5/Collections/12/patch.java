public void combine(ExtendedProperties props) {
        for (Iterator it = props.getKeys(); it.hasNext();) {
            String key = (String) it.next();
            Object newVal = props.get(key);
            if (this.containsKey(key)) {
                Object curVal = this.get(key);
                if (curVal instanceof java.util.List) {
                    java.util.List list = (java.util.List) curVal;
                    if (newVal instanceof java.util.List) {
                        list.addAll((java.util.List) newVal);
                    } else {
                        list.add(newVal);
                    }
                } else {
                    java.util.List list = new java.util.Vector();
                    list.add(curVal);
                    if (newVal instanceof java.util.List) {
                        list.addAll((java.util.List) newVal);
                    } else {
                        list.add(newVal);
                    }
                    super.put(key, list);
                }
            } else {
                super.put(key, newVal);
            }
        }
    }