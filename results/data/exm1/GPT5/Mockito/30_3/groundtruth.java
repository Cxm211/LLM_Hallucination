// ===== FIXED org.mockito.exceptions.Reporter :: smartNullPointerException(Location) [lines 438-447] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-30-fixed/src/org/mockito/exceptions/Reporter.java =====
    public void smartNullPointerException(Object obj, Location location) {
        throw new SmartNullPointerException(join(
                "You have a NullPointerException here:",
                new Location(),
                obj,
                "Because this method was *not* stubbed correctly:",
                location,
                ""
                ));
    }
