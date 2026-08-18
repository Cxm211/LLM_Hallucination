public static LocalDate fromDateFields(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int yearOfEra = cal.get(Calendar.YEAR);
        int era = cal.get(Calendar.ERA);
        int year = (era == GregorianCalendar.BC) ? 1 - yearOfEra : yearOfEra;
        return new LocalDate(
            year,
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        );
    }