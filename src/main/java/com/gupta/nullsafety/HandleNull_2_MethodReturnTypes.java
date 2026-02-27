package com.gupta.nullsafety;

import java.util.HashMap;
import java.util.Map;

public class HandleNull_2_MethodReturnTypes {

    // Let's up our game and add brewing instructions to Coffee
    record Coffee (String name, String brewingInstructions, String origin) {}
    record Order(String customerName, Coffee coffee) {}

    // ====================================================
    // 2. Method return types - null
    // ====================================================

    // Chandra/ Mala:
    // Section 2 is sneakier — the NPE comes from what a method returns to you.
    // You call a method. It returns null. You trust it. You chain another call.
    // Boom.
    // And the worst part? The method was technically correct to return null.
    // The contract just wasn't communicated anywhere."
    //
    // Back to the coffee shop. The waiter takes the order. Goes to the kitchen.
    // Comes back. You ask: 'What coffee did I order?' They hand you... a note
    // that says null. You try to read the note. NullPointerException.
    // NOTHING will happen UNLESS you try to read the note
    //

    private static final Map<String, Coffee> menu = new HashMap<>();
    static {
        menu.put("espresso",   new Coffee("Espresso",   "Ethiopian",  "Grind fine. 90°C. 25ml in 25 seconds."));
        menu.put("latte",      new Coffee("Latte",      "Colombian",  "Espresso + steamed oat milk."));
        menu.put("newblend",   new Coffee("New Blend",  "Rwandan",    null)); // menu item exists, instructions missing
        // "coldpresso" is not in the menu at all
    }

    // Map.get() returns null when key is not found — standard Java behaviour
    // this is one of THE most common sources of NPE in real codebases
    static Coffee findByName(String name) {
        return menu.get(name.toLowerCase()); // returns null if not found — no exception
    }



    // ------------------------------------------------------------------
    // 2.1 : The chained call — the classic one-liner NPE ──────────────
    // ------------------------------------------------------------------
    //
    // Chandra/ Mala:
    // Here's our star. The one-liner. Looks elegant. Reads naturally.
    // Hides a loaded gun.

    // DO WE NEED THIS BECAUSE WE ALREADY COVERED IT IN SECTION 1
    static String whatCoffeeAreYouDrinking(Order order) {
        return order.coffee().name(); // NPE when coffee() is null
    }

    // using whatCoffeeAreYouDrinking
    Order order = new Order("Peter", null);
    // call whatCoffeeAreYouDrinking(order);

    // Chandra/ Mala:
    // For senior devs — this is what your REST controller looks like when
    // a JSON field is missing and Jackson deserialises it as null.
    // order.getPayment().getCardNumber() -- and payment was optional in the schema.
    // Same bug. Different Tuesday.


    // 2.2 : Core Java API calls returning null - handling something you can't change?
    // Map.get() — the most common NPE in production ─────────────
    //
    // Chandra/ Mala:
    // This one is responsible for more production incidents than we care to admit.
    // Map.get() does not throw an exception when the key isn't found.
    // It returns null. Silently. Politely. Lethally.

    static void demo_MapGet() {
        Coffee coldpresso = findByName("coldpresso");     // returns null
        IO.println(coldpresso.name()); // NPE because coldpresso is null

        // Chandra/ Mala:
        // How many of you have seen this exact bug with a configuration map?
        // config.get('database.url').trim() — key wasn't in the config file.
        // Hands up. Yes. All of you.
        // Joke: For those who haven't raised their hands.. are we looking at managers here?
    }

    // Use case 2.3 : Deeper chain — null hiding two calls deep ─────────────────
    //
    // Chandra/ Mala:
    // Now let's talk about the NPE that takes 45 minutes to find.
    // Because the stack trace points at a line with THREE method calls,
    // and you have to figure out WHICH one returned null.
    // In Java 14+ the JVM tells you (NamedNullPointer exception?). Before that — good luck.

    static String getBrewingInstructions(Order order) {
        return order.coffee().brewingInstructions(); // TWO potential nulls
        //           ^^^^^^^^^^                            // null if no coffee on order
        //                      ^^^^^^^^^^^^^^^^^^^^^^    // null if instructions not written
    }

    static void demo_DeeperChain() {
        // Failure point 1 — getCoffee() is null
        Order noCoffee = new Order("Steve", null); // If a regular class, I could set just one value and not explicitly pass null
        IO.println(getBrewingInstructions(noCoffee));     // NPE at getCoffee() — no coffee on the order.

        // Failure point 2 — coffee exists, but getBrewingInstructions() is null
        // "New Blend" is on the menu — but the barista forgot to write instructions
        Order newBlendOrder = new Order("Harry", findByName("newblend"));
        String instructions = getBrewingInstructions(newBlendOrder);
        IO.println("Instructions: " + instructions.toUpperCase()); // NPE at getBrewingInstructions() — coffee exists, instructions don't;

        // Chandra/ Mala:
        // Java 14 introduced helpful NPE messages — the JVM now tells you EXACTLY
        // which variable was null. Before Java 14, the stack trace just said
        // 'NullPointerException at line 87' and you had to guess.
        // If you're still on Java 11 — I'm sorry. And also, upgrade."
    }

    // Use case 2.4 : The Integer unboxing surprise ──────────────────────────────
    //
    // Chandra/ Mala
    // This one is my favourite to show senior developers because it looks
    // completely safe. No method chaining. No suspicious objects.
    // Just an assignment. And it still throws NPE.

    static class CoffeeOrder {
        Integer temperatureC; // Integer — nullable. Not int — not nullable.
    }

    static void demo_Unboxing() {
        // order.temperatureC is null — nobody set it
        CoffeeOrder order = new CoffeeOrder();

        IO.println("Temperature field: " + order.temperatureC); // prints null — fine

        int temp = order.temperatureC; // NPE — unboxing null Integer to primitive int

        // Same trap in a comparison — also common
        if (order.temperatureC == 65) { // NPE - also unboxes — also NPE
            //
        }
    }
}
