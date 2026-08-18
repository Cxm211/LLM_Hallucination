// ===== FIXED org.jfree.data.time.TimeSeries :: clone() [lines 856-860] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-17-fixed/source/org/jfree/data/time/TimeSeries.java =====
    public Object clone() throws CloneNotSupportedException {
        TimeSeries clone = (TimeSeries) super.clone();
        clone.data = (List) ObjectUtilities.deepClone(this.data);
        return clone;
    }
