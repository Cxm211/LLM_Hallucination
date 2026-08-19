// ===== FIXED org.jfree.data.time.Week :: Week [lines 117-119] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Chart/Chart-8-fixed/source/org/jfree/data/time/Week.java =====
public Week(Date time, TimeZone zone) {
    // defer argument checking...
    this(time, zone, Locale.getDefault());
}