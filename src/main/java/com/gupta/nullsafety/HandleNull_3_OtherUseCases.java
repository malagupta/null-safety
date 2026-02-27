package com.gupta.nullsafety;

public class HandleNull_3_OtherUseCases {

    record Coffee (String name, String brewingInstructions, String origin) {}
    record Order(String customerName, Coffee coffee) {}


    static void main() {
        // 3.1 Synchronizing on a null reference
        synchronizedBlockOfCode();
    }

    // 3.1 Synchronizing on a null reference
    private static void synchronizedBlockOfCode() {
        Object lock = null;
        synchronized (lock) {
            //
        } // NPE when lock is null
    }

    // 3.2 Throwing a null
    // JOKE: The last time I presented this
    // on the participants commented.. you must celebrate if ex is null..
    // not try to handle the situation.. because that's the reason you have an ex.
    private static void throwException(RuntimeException ex) {
        throw ex; // NPE if ex is null — you can't throw null

    }

    private static void moreUnboxingExamples() {
        // 1
        Integer shots = null;
        boolean isDouble = (shots == 2); // NPE — unboxing during comparison

        // 2
        // In a ternary — especially sneaky
        Integer temp = null;
        int t = (temp != null) ? temp : null; // NPE — result forces unboxing to int

        // 3 - addition
        Integer a = null, b = 5;
        int result = a + b; // NPE — a is unboxed before addition
    }

    private static String getStaticCoffeeName() {
        return "Espresso";
    }

    private static void callStaticViaInstance() {
        HandleNull_3_OtherUseCases anInstance = null;
        String name = anInstance.getStaticCoffeeName(); // compiles, runs, but... NPE because coffee is null
    }

    private static void getMyClass() {
        Coffee coffee = null;
        assert coffee.getClass() != null; // NPE even inside an assertion
    }

    private static void switchPre21() {
        String size = null;
        switch (size) { // NPE — switch on null String throws before any case is evaluated
            case "small": IO.println(1);
            case "large": IO.println(2);
        }

        // Chandra/ Mala:
        // very common use case.
        // Configuration values,
        // HTTP parameters,
        // enum-like string routing
        // — all classic places where null sneaks
        // into a switch.
        // Every web application that routes on a
        // request parameter has hit this at least once
    }
}
