    public void smartNullPointerException(Location location) {
        throw new SmartNullPointerException(join(
                "You have a NullPointerException here:",
                location,
                "Because this method was *not* stubbed correctly:",
                location,
                ""
                ));
    }