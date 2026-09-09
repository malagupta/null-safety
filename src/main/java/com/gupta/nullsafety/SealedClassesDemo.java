package com.gupta.nullsafety;

import java.util.Map;

public class SealedClassesDemo {

    record Coffee(String name, String origin, String brewingInstructions) {}

    sealed interface CoffeeResult permits Found, NotFound, Unavailable, BitterCoffee {}
    record Found(Coffee coffee) implements CoffeeResult {}
    record NotFound(String name) implements CoffeeResult {}
    record Unavailable(String reason) implements CoffeeResult {}
    record BitterCoffee(String name) implements CoffeeResult {}

    static final Map<String, Coffee> MENU = Map.of(
            "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine. 90°C."),
            "latte",    new Coffee("Latte",    "Colombian", "Shot + steamed milk."),
            "matcha",   new Coffee("Matcha",   "Japanese",  null)
    );

    static CoffeeResult lookup(String name) {
        Coffee coffee = MENU.get(name);
        if (coffee == null) {
            return new NotFound(name);
        }
        if (coffee.brewingInstructions() == null) {
            return new Unavailable("No instructions for " + coffee.name());
        }
        return new Found(coffee);
    }

    static void demo() {

        // Pattern matching is EXHAUSTIVE — all three cases MUST be handled.
        // Add a fourth permitted type? This code stops compiling.
        // Null cannot hide. The compiler enforces it.

        for (String name : new String[]{"espresso", "cappuccino", "matcha"}) {
            String message = switch (lookup(name)) {
                case Found(Coffee coffee) -> "✓ " + coffee.name() + ": " + coffee.brewingInstructions().toUpperCase();
                case NotFound(String n) ->   "✗ \"" + n + "\" not found";
                case Unavailable(String reason) ->  "⚠ " + reason;

            };
            System.out.println(message);
        }
    }

    public static void main(String[] args) {
        demo();
    }
}

