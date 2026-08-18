        public long add(long instant, long value) {
            long originalInstant = instant;
            if (instant >= iCutover) {
                instant = iGregorianField.add(instant, value);
                if (instant < iCutover) {
                    if (originalInstant - iGapDuration < iCutover) {
                        instant = gregorianToJulian(instant);
                    }
                }
            } else {
                instant = iJulianField.add(instant, value);
                if (instant >= iCutover) {
                    if (originalInstant + iGapDuration >= iCutover) {
                        instant = julianToGregorian(instant);
                    }
                }
            }
            return instant;
        }