package com.gupta.nullsafety;

record Name       (String fName, String lName) { }
record PhoneNumber(String areaCode, String number) { }
record Country    (String countryCode, String countryName) { }
record Passenger  (Name name,
                   PhoneNumber phoneNumber,
                   Country from,
                   Country destination) { }

/*
 * Chandra/ Mala: Without record patterns, nultiple null checks required
 */
public class HandleNull_1_ReferenceVariable_Solution6_LanguageFeatures {

    boolean checkFirstNameAndCountryCode (Object obj) {
        if (obj != null) {
            if (obj instanceof Passenger passenger) {
                Name name = null;
                Country destination = null;

                if (passenger.name() != null) {
                    name = passenger.name();

                    if (passenger.destination() != null) {
                        destination = passenger.destination();

                        String fName = name.fName();
                        String countryCode = destination.countryCode();

                        if (fName != null && countryCode != null) {
                            return fName.startsWith("Simo") &&
                                   countryCode.equals("PRG");
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * Cleaner code - same functionality as checkFirstNameAndCountryCode
     * Record Patterns : The instanceof check will fail if the record
     * components name and destination are null
     */

    boolean checkFirstNameAndCountryCodeAgain (Object obj) {
        if (obj instanceof Passenger(Name (String fName, String lName),
                                     PhoneNumber phoneNumber,
                                     Country from,
                                     Country (String countryCode, String countryName) )) {

            if (fName != null && countryCode != null) {
                return fName.startsWith("Simo") && countryCode.equals("PRG");
            }
        }
        return false;
    }
}
