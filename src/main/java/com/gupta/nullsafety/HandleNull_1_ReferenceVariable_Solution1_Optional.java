package com.gupta.nullsafety;

import java.util.Objects;
import java.util.Optional;

public class HandleNull_1_ReferenceVariable_Solution1_Optional {

    // Records with built-in validation

    record Coffee(String name, String origin, int shots) {
        Coffee {
            Objects.requireNonNull(name,   "Coffee must have a name");
            Objects.requireNonNull(origin, "Coffee must have an origin");
        }
    }

    // Order takes a plain Coffee parameter — null is allowed to represent absence.
    record Order(String customerName, Coffee coffee) {
        Order {
            Objects.requireNonNull(customerName, "Order must have a customer name");
            // coffee is intentionally allowed to be null — absence is valid business state
        }
    }

    // Caller is forced to acknowledge the absence of Coffee via Optiobal<Coffee>
    static Optional<Coffee> getCoffee(Order order) {
        return Optional.ofNullable(order.coffee());
    }

    static Optional<String> getCoffeeName(Order order) {
        return getCoffee(order).map(Coffee::name);
    }

    static Optional<String> getCoffeeOrigin(Order order) {
        return getCoffee(order).map(Coffee::origin);
    }

    static void main(String[] args) {

        // =========================================================
        // 1.1 — Uninitialised reference: coffee is absent on the order
        // =========================================================
        //
        // Original: new Order("Jonathan", null)
        //           order.coffee().name()  — NPE
        //
        // Solution: null is passed as a plain Coffee parameter.
        // getCoffee() wraps it in Optional — only at the return boundary.
        // map() only executes when coffee is present.

        Order order = new Order("Jonathan", null);

        // Safe — prints null, no crash (record accessor, not dereferenced)
        IO.println(order.coffee());

        // Safe — getCoffee() returns Optional, map() handles absence
        String coffeeName = getCoffee(order)
                .map(Coffee::name)
                .orElse("No coffee selected yet — Jonathan, what would you like?");

        IO.println(coffeeName);

        // =========================================================
        // 1.2 — Chained call on null
        // =========================================================
        //
        // Original: order.coffee().name().toUpperCase() — NPE
        //
        // Solution: each .map() in the chain propagates empty safely.
        // toUpperCase() is never called on a null String.

        String announcement = getCoffeeName(order)
                .map(String::toUpperCase)
                .orElse("(no coffee on this order)");

        IO.println("I ordered: " + announcement);

        // =========================================================
        // 1.3 — Object exists, but a field inside is null
        // =========================================================
        //
        // Original: new Coffee("Espresso", null, 1)
        //           order.coffee().origin().toUpperCase() — NPE
        //
        // Solution A (preferred): compact constructor rejects null origin
        // at construction. The bad Coffee object never exists.
        // NPE fires immediately at the source, with a clear message.

        try {
            Coffee badCoffee = new Coffee("Espresso", null, 1); // NPE here — not later
        } catch (NullPointerException e) {
            System.out.println("Caught at construction: " + e.getMessage());
        }

        // Solution B: when origin is genuinely optional by design,
        // keep the record field as a plain String (nullable),
        // and expose it through an Optional return type only.

        record CoffeeWithOptionalOrigin(String name, String origin, int shots) {
            CoffeeWithOptionalOrigin {
                Objects.requireNonNull(name, "Coffee must have a name");
                // origin is intentionally allowed to be null
            }

            // Optional as return type only — not a field, not a parameter
            Optional<String> findOrigin() {
                return Optional.ofNullable(origin);
            }
        }

        CoffeeWithOptionalOrigin mysteryBlend =
                new CoffeeWithOptionalOrigin("Mystery Blend", null, 1);

        String origin = mysteryBlend.findOrigin()
                                    .map(String::toUpperCase)
                                    .orElse("Origin unknown — the beans arrived on a Friday. Nobody asked.");

        System.out.println("My coffee's origin is: " + origin);

        // =========================================================
        // The happy path — everything valid, no checks needed
        // =========================================================

        Order mikeOrder = new Order("Mike", new Coffee("Espresso", "Ethiopian", 1));

        String mikeAnnouncement = getCoffeeName(mikeOrder)
                .map(String::toUpperCase)
                .orElse("(no coffee)");

        String mikeOrigin = getCoffeeOrigin(mikeOrder)
                .map(String::toUpperCase)
                .orElse("(origin unknown)");

        IO.println("I ordered  : " + mikeAnnouncement);
        IO.println("It's from  : " + mikeOrigin);

        // Summary
        //
        // The pattern:
        //   record field   → plain type (Coffee, String) — null allowed where valid
        //   return type    → Optional<T>                 — wraps null at the boundary
        //   compact constructor → Objects.requireNonNull      — rejects null where invalid
        //
        // One rule: null is permitted to exist inside the record.
        //           null is never permitted to ESCAPE unchecked.
        //           Optional is the gate at the exit.
    }
}