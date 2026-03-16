package com.gupta.nullsafety;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SolutionsCurrent {

    static class NullChecks {
        record Coffee(String name, String origin, String brewingInstructions) {}
        record Order(String customerName, Coffee coffee) {}

        // 😊 Check at the boundary, one guard, clear message
        static String serveCorrect(Order order) {
            Coffee coffee = order.coffee();
            if (coffee == null) return "No coffee on this order.";
            return coffee.name();
        }

        // 😈 Null check hell
        static String serveNullCheckHell(Order order) {
            if (order != null) {
                if (order.coffee() != null) {
                    if (order.coffee().name() != null) {
                        if (order.coffee().brewingInstructions() != null) {
                            return order.coffee().brewingInstructions().toUpperCase();
                        } else {
                            return "No instructions.";
                        }
                    } else {
                        return "No name.";
                    }
                } else {
                    return "No coffee.";
                }
            }
            return "No order.";
        }
    }

    static class ModernJavaRecordPatterns {
        record Name       (String fName, String lName) { }
        record PhoneNumber(String areaCode, String number) { }
        record Country    (String countryCode, String countryName) { }
        record Passenger  (Name name,
                           PhoneNumber phoneNumber,
                           Country from,
                           Country destination) { }

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

    static class AnnotationsCheck {
        record Coffee(String name, String origin, String brewingInstructions) {}
        record Order(String customerName, Coffee coffee) {}

        private static final Map<String, Coffee> menu = new HashMap<>();
        static {
            menu.put("espresso", new Coffee("Espresso", "Ethiopian", "Grind fine. 90°C."));
            menu.put("latte",    new Coffee("Latte",     "Colombian", "Shot + steamed milk."));
            menu.put("newblend", new Coffee("New Blend", "Rwandan",   null));
        }

        @Nullable Coffee findByNameNotUsingOptional(@NotNull String name) {
            return menu.get(name);
        }

        // Before Optional — caller has no idea
        static Coffee findByName_unsafe(String name) {
            return menu.get(name.toLowerCase()); // null if not found — caller doesn't know
        }

        // After Optional — contract is in the type
        static Optional<Coffee> findByName(String name) {
            return Optional.ofNullable(menu.get(name.toLowerCase()));
        }

        static void usingMethodsReturningOptionalValues() {
            // Missing values checked via methods such as orElse
            String result = findByName("espresso")
                    .map(Coffee::name)
                    .map(String::toUpperCase)
                    .orElse("Not found");
            System.out.println("Found    : " + result);

            String missing = findByName("coldpresso")
                    .map(Coffee::name)
                    .orElse("Not on menu — try our cold brew");
            System.out.println("Missing  : " + missing);

            // orElseThrow — when absence is an error
            try {
                Coffee required = findByName("coldpresso")
                        .orElseThrow(() -> new IllegalStateException(
                                "'coldpresso' must be configured — check setup"));
            } catch (IllegalStateException e) {
                System.out.println("Error    : " + e.getMessage());
            }

            // Calling .get() without checking for nullness is an anti-pattern.
            try {
                Coffee bad = findByName("coldpresso").get(); // NoSuchElementException if null
            } catch (java.util.NoSuchElementException e) {
                System.out.println(e);
            }

        }
    }
    

}

