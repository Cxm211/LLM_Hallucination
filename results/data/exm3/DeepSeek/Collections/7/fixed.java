// ===== FIXED org.apache.commons.collections.ExtendedProperties :: addPropertyDirect(String, Object) [lines 704-710] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    private void addPropertyDirect(String key, Object value) {
        // safety check
        if (!containsKey(key)) {
            keysAsListed.add(key);
        }
        super.put(key, value);
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: addPropertyInternal(String, Object) [lines 723-744] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    private void addPropertyInternal(String key, Object value) {
        Object current = this.get(key);

        if (current instanceof String) {
            // one object already in map - convert it to a vector
            List values = new Vector(2);
            values.add(current);
            values.add(value);
            super.put(key, values);
            
        } else if (current instanceof List) {
            // already a list - just add the new token
            ((List) current).add(value);
            
        } else {
            // brand new key - store in keysAsListed to retain order
            if (!containsKey(key)) {
                keysAsListed.add(key);
            }
            super.put(key, value);
        }
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: clearProperty(String) [lines 825-837] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public void clearProperty(String key) {
        if (containsKey(key)) {
            // we also need to rebuild the keysAsListed or else
            // things get *very* confusing
            for (int i = 0; i < keysAsListed.size(); i++) {
                if (( keysAsListed.get(i)).equals(key)) {
                    keysAsListed.remove(i);
                    break;
                }
            }
            super.remove(key);
        }
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: getBoolean(String, Boolean) [lines 1202-1224] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public Boolean getBoolean(String key, Boolean defaultValue) {

        Object value = get(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
            
        } else if (value instanceof String) {
            String s = testBoolean((String) value);
            Boolean b = new Boolean(s);
            super.put(key, b);
            return b;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getBoolean(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Boolean object");
        }
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: getByte(String, Byte) [lines 1298-1318] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public Byte getByte(String key, Byte defaultValue) {
        Object value = get(key);

        if (value instanceof Byte) {
            return (Byte) value;
            
        } else if (value instanceof String) {
            Byte b = new Byte((String) value);
            super.put(key, b);
            return b;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getByte(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Byte object");
        }
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: getDouble(String, Double) [lines 1676-1696] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public Double getDouble(String key, Double defaultValue) {
        Object value = get(key);

        if (value instanceof Double) {
            return (Double) value;
            
        } else if (value instanceof String) {
            Double d = new Double((String) value);
            super.put(key, d);
            return d;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getDouble(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Double object");
        }
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: getFloat(String, Float) [lines 1606-1626] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public Float getFloat(String key, Float defaultValue) {
        Object value = get(key);

        if (value instanceof Float) {
            return (Float) value;
            
        } else if (value instanceof String) {
            Float f = new Float((String) value);
            super.put(key, f);
            return f;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getFloat(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Float object");
        }
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: getInteger(String, Integer) [lines 1466-1486] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public Integer getInteger(String key, Integer defaultValue) {
        Object value = get(key);

        if (value instanceof Integer) {
            return (Integer) value;
            
        } else if (value instanceof String) {
            Integer i = new Integer((String) value);
            super.put(key, i);
            return i;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getInteger(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Integer object");
        }
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: getList(String, List) [lines 1137-1158] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public List getList(String key, List defaultValue) {
        Object value = get(key);

        if (value instanceof List) {
            return new ArrayList((List) value);
            
        } else if (value instanceof String) {
            List values = new ArrayList(1);
            values.add(value);
            super.put(key, values);
            return values;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getList(key, defaultValue);
            } else {
                return ((defaultValue == null) ? new ArrayList() : defaultValue);
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a List object");
        }
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: getLong(String, Long) [lines 1536-1556] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public Long getLong(String key, Long defaultValue) {
        Object value = get(key);

        if (value instanceof Long) {
            return (Long) value;
            
        } else if (value instanceof String) {
            Long l = new Long((String) value);
            super.put(key, l);
            return l;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getLong(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Long object");
        }
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: getProperty(String) [lines 641-654] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public Object getProperty(String key) {
        // first, try to get from the 'user value' store
        Object obj = super.get(key);

        if (obj == null) {
            // if there isn't a value there, get it from the
            // defaults if we have them
            if (defaults != null) {
                obj = defaults.get(key);
            }
        }

        return obj;
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: getShort(String, Short) [lines 1368-1388] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public Short getShort(String key, Short defaultValue) {
        Object value = get(key);

        if (value instanceof Short) {
            return (Short) value;
            
        } else if (value instanceof String) {
            Short s = new Short((String) value);
            super.put(key, s);
            return s;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getShort(key, defaultValue);
            } else {
                return defaultValue;
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Short object");
        }
    }

// ===== FIXED org.apache.commons.collections.ExtendedProperties :: getVector(String, Vector) [lines 1085-1106] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-7-fixed/src/java/org/apache/commons/collections/ExtendedProperties.java =====
    public Vector getVector(String key, Vector defaultValue) {
        Object value = get(key);

        if (value instanceof List) {
            return new Vector((List) value);
            
        } else if (value instanceof String) {
            Vector values = new Vector(1);
            values.add(value);
            super.put(key, values);
            return values;
            
        } else if (value == null) {
            if (defaults != null) {
                return defaults.getVector(key, defaultValue);
            } else {
                return ((defaultValue == null) ? new Vector() : defaultValue);
            }
        } else {
            throw new ClassCastException('\'' + key + "' doesn't map to a Vector object");
        }
    }
