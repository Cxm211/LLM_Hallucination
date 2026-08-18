// ===== FIXED org.jfree.data.DefaultKeyedValues :: removeValue(Comparable) [lines 330-337] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-18-fixed/source/org/jfree/data/DefaultKeyedValues.java =====
    public void removeValue(Comparable key) {
        int index = getIndex(key);
        if (index < 0) {
            throw new UnknownKeyException("The key (" + key 
                    + ") is not recognised.");
        }
        removeValue(index);
    }

// ===== FIXED org.jfree.data.DefaultKeyedValues :: removeValue(int) [lines 315-319] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-18-fixed/source/org/jfree/data/DefaultKeyedValues.java =====
    public void removeValue(int index) {
        this.keys.remove(index);
        this.values.remove(index);
        rebuildIndex();
    }

// ===== FIXED org.jfree.data.DefaultKeyedValues2D :: removeColumn(Comparable) [lines 454-470] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-18-fixed/source/org/jfree/data/DefaultKeyedValues2D.java =====
    public void removeColumn(Comparable columnKey) {
    	if (columnKey == null) {
    		throw new IllegalArgumentException("Null 'columnKey' argument.");
    	}
    	if (!this.columnKeys.contains(columnKey)) {
    		throw new UnknownKeyException("Unknown key: " + columnKey);
    	}
        Iterator iterator = this.rows.iterator();
        while (iterator.hasNext()) {
            DefaultKeyedValues rowData = (DefaultKeyedValues) iterator.next();
            int index = rowData.getIndex(columnKey);
            if (index >= 0) {
                rowData.removeValue(columnKey);
            }
        }
        this.columnKeys.remove(columnKey);
    }
