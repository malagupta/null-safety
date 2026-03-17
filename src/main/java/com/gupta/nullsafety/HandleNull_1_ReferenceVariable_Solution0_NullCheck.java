package com.gupta.nullsafety;

public class HandleNull_1_ReferenceVariable_Solution0_NullCheck {
    /*
        Chandra/Mala:

        a) Handling null values become unmanageable as the code base increases
        b) The null flow is unpredictable
        c) Multiple channels - malformed data, network connectivity issues, service failures, thread deadlocks
        d) Uncertainity increases exponentially with the increasing codebase
        e) Before we talk about everything - we need to understand what causes NPE, and where:
        f) Who - Reference variables - Yes, primitives - No
        g) How - Dereferencing nulls, NPE is not thrown by a null.. it is thrown when we dereference a null
                      (derefernce - calling a method or a variable on null)
        h) Where - References as
            local,
            instance,
            static,
            method and constructor parameters/ variables,
            array elements,
            lambda parameters
     */

    record Coffee(String name, String origin, int shots) { }
    record Order(String customerName, Coffee coffee) {}

    static void main(String[] args) {

        // ===========================================================
        // 1 - reference variables
        // ===========================================================

        // -------------------------------------------------------------------------
        // 1.1 : Uninitialised instance/ static reference variable
        // -------------------------------------------------------------------------
        //
        // Chandra/ Mala:
        // Here's one of the most common scenarios - an instance field reference variable set to null.
        // Our coffee story - You place an Order, but the Coffee details are not set.
        // Java initialises it null by default.
        // No warning.
        // No error. Everything looks fine.
        // Until you try to drink it :)

        Order order = new Order("Jonathan", null);

        // null but still no NPE — printing the reference itself is safe
        IO.println(order.coffee());

        // null check before dereferencing — guards the NPE
        // NPE is not thrown by a null.. it is thrown when we dereference a null
        // (dereference - calling a method or a variable on null)
        if (order.coffee() == null) {
            IO.println("No coffee on Jonathan's order — the cup is empty.");
        } else {
            IO.println(order.coffee().name());
        }

        // null itself is not the issue. dereferencing it is.

        // -------------------------------------------------------------------------
        // 1.2 : null passed as a method argument
        // -------------------------------------------------------------------------
        //
        // Chandra/ Mala:
        // Second use case. Even more insidious. The reference is null, not because
        // we forgot to initialise it — but because someone passed null to it.
        // From another class. That we didn't write. In a codebase we inherited.
        // At 11pm. On a Friday.
        // Joke: The mistake hurts when someone else made it.

        // Which coffee are you ordering?
        // Guard the entire chain — one check at the entry point covers all chained calls
        if (order.coffee() == null) {
            System.out.println("I ordered: nothing. The order has no coffee.");
        } else {
            System.out.println("I ordered: " + order.coffee()
                                                    .name()
                                                    .toUpperCase());
        }

        // -------------------------------------------------------------------------
        // 1.3 : The sneaky null — object exists, field inside is null
        // -------------------------------------------------------------------------

        order = new Order("Mike", new Coffee("Espresso", null, 1));

        // Question — where is your coffee from?
        // Two levels to check: the Coffee object, and then the origin field inside it
        if (order.coffee() == null) {
            System.out.println("My coffee's origin is: unknown — no coffee on the order.");
        } else if (order.coffee().origin() == null) {
            System.out.println("My coffee's origin is: unknown — origin was never set.");
        } else {
            System.out.println("My coffee's origin is: " + order.coffee()
                                                                .origin()
                                                                .toUpperCase());
        }

        // Chandra/ Mala
        // For the senior developers in the room — this is exactly what happens with
        // JPA entities that have lazily-loaded relationships outside a transaction.
        // The entity is there. The collection field is there. But it's null
        // because Hibernate never populated it. Same shape of bug, much harder to spot.

        // Chandra/ Mala — look at this null check code.
        // It works. It is correct. And it is already getting hard to read.
        // Two fields. Three branches. Imagine ten fields.
        // This is why we need better tools — and that is exactly what we cover next.

        // Chandra/ Mala  - Summary of three flavours of null reference variables:
        //  1. Uninitialised — you didn't set it. Null assigned implicitly.
        //  2. Null method argument — null passed explicitly / a null reference variable
        //  3. Null field    — Object exists, but a field inside is null.
        //
        //  In all three cases — null itself didn't crash anything.
        //  Null dereference — calling a member on null throws NPE.
        //  Keep that distinction in your head. It matters in the next section.
    }
}
