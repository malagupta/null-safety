package com.gupta.nullsafety;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class HandleNull_1_ReferenceVariable_Solution2_Annotations {

    // Why both annotations AND Objects.requireNonNull?
    //
    // They operate at DIFFERENT layers — they are not alternatives:
    //
    //   @NotNull / @Nullable   → compile time  — IDE warns, NullAway fails the build
    //   Objects.requireNonNull → runtime       — throws NPE with a clear message
    //
    // Annotations catch the bug when the code is WRITTEN.
    // requireNonNull catches it when the code RUNS (last line of defence).
    // Using both is belt AND suspenders — not redundancy, defence in depth.

    record Coffee(@NotNull String name, @NotNull String origin, int shots) {
        // @NotNull on record components — tells IntelliJ / NullAway:
        // passing null for name or origin is a contract violation at the call site
        // IDE underlines the caller. NullAway fails the build.
        Coffee {
            // Objects.requireNonNull — runtime enforcement.
            // If a null somehow slips past the static analysis (reflection,
            // legacy code, unannotated library), this is the safety net.
            Objects.requireNonNull(name,   "Coffee must have a name");
            Objects.requireNonNull(origin, "Coffee must have an origin");
        }
    }

    record Order(@NotNull String customerName, @Nullable Coffee coffee) {
        // @NotNull on customerName — must always be present
        // @Nullable on coffee     — explicitly documents that absence is valid.
        //                           No comment needed. The annotation IS the comment.
        Order {
            Objects.requireNonNull(customerName, "Order must have a customer name");
            // coffee — no requireNonNull. @Nullable declares absence is intentional.
        }
    }

    static Optional<Coffee> getCoffee(@NotNull Order order) {
        return Optional.ofNullable(order.coffee());
    }

    static Optional<String> getCoffeeName(@NotNull Order order) {
        return getCoffee(order).map(Coffee::name);
    }

    static Optional<String> getCoffeeOrigin(@NotNull Order order) {
        return getCoffee(order).map(Coffee::origin);
    }

    // ── @NotNull on return type — method contracts ────────────────────────────
    //
    // These two methods always return a non-null String.
    // @NotNull on the return type tells the caller — no null check needed here.
    // Objects.requireNonNull cannot express this. Only an annotation can.

    @NotNull
    static String resolveCustomerName(@Nullable String name) {
        // requireNonNullElse — core Java fallback, no annotation equivalent
        return Objects.requireNonNullElse(name, "Guest");
    }

    @NotNull
    static String resolveBrewingTemp(@Nullable String configuredTemp) {
        // requireNonNullElseGet — lazy variant, supplier only called when null
        return Objects.requireNonNullElseGet(configuredTemp,
                                             () -> Objects.requireNonNullElse(System.getenv("DEFAULT_BREW_TEMP"), "90°C"));
    }

    static void main(String[] args) {

        // =====================================================================
        // 1.1 — Uninitialised reference: coffee is absent
        // =====================================================================
        //
        // @Nullable on the coffee component tells IntelliJ:
        // "order.coffee() might be null — warn if dereferenced directly"
        // Any attempt to call order.coffee().name() gets a yellow squiggle.
        // NullAway fails the build. The bug is caught before it ships.

        Order order = new Order("Jonathan", null);

        System.out.println(order.coffee()); // prints null — no crash

        String coffeeName = getCoffee(order)
                .map(Coffee::name)
                .orElse("No coffee selected yet — Jonathan, what would you like?");

        System.out.println(coffeeName);

        // =====================================================================
        // 1.2 — Chained call on null
        // =====================================================================
        //
        // getCoffeeName() is @NotNull — its return value (the Optional) is
        // guaranteed non-null. Only the value INSIDE Optional might be absent.
        // The annotation removes one layer of uncertainty for the caller.

        String announcement = getCoffeeName(order)
                .map(String::toUpperCase)
                .orElse("(no coffee on this order)");

        System.out.println("I ordered: " + announcement);

        // =====================================================================
        // 1.3 — Object exists, but a field inside is null
        // =====================================================================
        //
        // @NotNull on Coffee's name and origin components means:
        // IntelliJ warns at the call site before construction.
        // Objects.requireNonNull in the compact constructor catches it at runtime.
        // Two layers. One clear message wherever it fires.

        try {
            Coffee badCoffee = new Coffee("Espresso", null, 1);
            //                                        ^^^^
            //  IntelliJ: "Passing null argument to @NotNull parameter"
            //  NullAway: build failure
            //  Runtime:  "Coffee must have an origin"
        } catch (NullPointerException e) {
            System.out.println("Caught at construction: " + e.getMessage());
        }

        // ── requireNonNullElse — default value, no annotation equivalent ─────
        //
        // When absence means "use a sensible default" — not a failure, not an Optional.
        // @NotNull on resolveCustomerName() tells the caller: this always returns a value.
        // The annotation documents the guarantee. requireNonNullElse delivers it.

        record CoffeeWithFallback(String name, String origin, int shots) {
            CoffeeWithFallback {
                Objects.requireNonNull(name, "Coffee must have a name");
                origin = Objects.requireNonNullElse(origin, "Unknown origin");
            }
        }

        CoffeeWithFallback mysteryBlend =
                new CoffeeWithFallback("Mystery Blend", null, 1);

        System.out.println("My coffee's origin is: " + mysteryBlend.origin().toUpperCase());

        // ── resolveBrewingTemp — @NotNull return, lazy fallback ──────────────

        System.out.println("Brewing at: " + resolveBrewingTemp(null));
        System.out.println("Customer  : " + resolveCustomerName(null));

        // =====================================================================
        // The happy path
        // =====================================================================

        Order mikeOrder = new Order("Mike", new Coffee("Espresso", "Ethiopian", 1));

        String mikeAnnouncement = getCoffeeName(mikeOrder)
                .map(String::toUpperCase)
                .orElse("(no coffee)");

        String mikeOrigin = getCoffeeOrigin(mikeOrder)
                .map(String::toUpperCase)
                .orElse("(origin unknown)");

        System.out.println("I ordered  : " + mikeAnnouncement);
        System.out.println("It's from  : " + mikeOrigin);

        // =====================================================================
        // Summary — annotations vs Objects.requireNonNull — not OR, but AND
        // =====================================================================
        //
        //  @NotNull on parameter    → IDE warns caller at the point of the call
        //                             NullAway fails the build before code ships
        //                             Objects.requireNonNull catches what slips through
        //
        //  @Nullable on parameter   → no Objects equivalent — only annotations
        //                             can document "null is intentionally valid here"
        //
        //  @NotNull on return type  → no Objects equivalent — only annotations
        //                             can tell callers "this never returns null"
        //
        //  Optional<T> return type  → no @NotNull needed — Optional IS the contract
        //                             the type already says "present or absent, never null"
        //                             adding @NotNull Optional<T> is redundant noise
        //
        //  requireNonNullElse       → no annotation equivalent for default values
        //  requireNonNullElseGet    → no annotation equivalent for lazy defaults
        //
        //  Rule of thumb:
        //    Annotations  = the CONTRACT  (visible to tools, IDEs, developers)
        //    requireNonNull = the ENFORCER (fires at runtime if contract is broken)
    }
}