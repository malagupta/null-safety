package com.gupta.nullsafety;

// ─────────────────────────────────────────────────────────────────────────────
// Valhalla - Use Valhalla's early access version to compile this code
//
// The ! suffix is the proposed syntax in Valhalla.
//
// What Valhalla replaces completely:
//   @NullMarked            → String! is non-null by the TYPE, not by annotation
//   @Nullable / @NotNull   → presence/absence of ! in the type says it all
//   Objects.requireNonNull → JVM rejects null before the constructor body runs
//
// What stays — because Valhalla doesn't change them:
//   Optional<T>            → still the right return type for genuinely absent values
//   Objects.requireNonNullElse  → still needed for DEFAULT VALUE logic (not null-guarding)
//
// The key distinction Valhalla draws:
//   String!  — null-restricted: this CANNOT be null. JVM enforced. No tool needed.
//   String   — nullable reference: this CAN be null. Caller handles it.
//

import java.util.Objects;
import java.util.Optional;

/*
public class HandleNull_1_ReferenceVariable_Solution4_Valhalla {

    // ── Records ───────────────────────────────────────────────────────────────
    //
    // String! on name and origin — null-restricted types.
    // The JVM rejects null assignment before the constructor body runs.
    // Objects.requireNonNull is gone — the type system made it redundant.
    // The compact constructor body is empty. There is nothing left to check.

    record Coffee(String! name, String! origin, int shots) {
        // No compact constructor needed.
        // String! guarantees non-null at the JVM level.
        // If null arrives — compile error if detectable at compile time,
        // JVM NullPointerException with a clear message if it arrives at runtime
        // (e.g. via reflection or an unannotated legacy library).
    }

    // Order — customerName must always exist: String!
    //         coffee may be absent: Coffee (no !) — nullable reference
    //
    // The presence or absence of ! is the entire null contract for this record.
    // No annotation. No comment. No requireNonNull. The types say everything.

    record Order(String! customerName, Coffee coffee) {
        // No compact constructor needed.
        // String! enforces customerName at the JVM level.
        // Coffee (nullable) is intentionally allowed to be absent —
        // the absence of ! is the declaration that null is valid here.
    }

    // ── Methods ───────────────────────────────────────────────────────────────
    //
    // Order! parameter — the caller cannot pass null. JVM enforced.
    // Optional<Coffee> return — unchanged. Valhalla doesn't replace Optional.
    // A nullable Coffee reference and an Optional<Coffee> solve different problems:
    //   Coffee  coffee  — field/param that can be null (internal state)
    //   Optional<Coffee> — return type that makes absence explicit to the caller

    static Optional<Coffee> getCoffee(Order! order) {
        return Optional.ofNullable(order.coffee());
    }

    static Optional<String> getCoffeeName(Order! order) {
        return getCoffee(order).map(Coffee::name);
    }

    static Optional<String> getCoffeeOrigin(Order! order) {
        return getCoffee(order).map(Coffee::origin);
    }

    // ── Methods with nullable parameters ─────────────────────────────────────
    //
    // String  parameter  — nullable, caller may pass null (no !)
    // String! return type — JVM guarantees this never returns null
    //
    // Objects.requireNonNullElse stays — not for null-guarding (Valhalla handles that)
    // but for DEFAULT VALUE logic. Valhalla has no syntax for "use this if absent".
    // That's a different problem. requireNonNullElse solves it cleanly.

    static String! resolveCustomerName(String name) {
        return Objects.requireNonNullElse(name, "Guest");
    }

    static String! resolveBrewingTemp(String configuredTemp) {
        return Objects.requireNonNullElseGet(configuredTemp,
                () -> Objects.requireNonNullElse(System.getenv("DEFAULT_BREW_TEMP"), "90°C"));
    }

    static void main(String[] args) {

        // =====================================================================
        // 1.1 — Absent coffee
        // =====================================================================
        //
        // Order constructor takes String! — passing null for customerName
        // is a JVM error. No annotation needed to catch it.
        // Coffee is nullable (no !) — null is valid and expected.

        Order order = new Order("Jonathan", null); // coffee = null, valid

        System.out.println(order.coffee()); // prints null — no crash

        String coffeeName = getCoffee(order)
                .map(Coffee::name)
                .orElse("No coffee selected yet — Jonathan, what would you like?");

        System.out.println(coffeeName);

        // =====================================================================
        // 1.2 — Chained call
        // =====================================================================
        //
        // getCoffeeName takes Order! — passing a null Order is a JVM error.
        // Optional handles the absent coffee inside the order.

        String announcement = getCoffeeName(order)
                .map(String::toUpperCase)
                .orElse("(no coffee on this order)");

        System.out.println("I ordered: " + announcement);

        // =====================================================================
        // 1.3 — Null field inside an object
        // =====================================================================
        //
        // String! on Coffee's components means:
        //
        //   new Coffee("Espresso", null, 1)
        //   → COMPILE ERROR if the compiler sees the null literal
        //   → JVM rejects it before the constructor body if null arrives at runtime
        //
        // No requireNonNull. No annotation. No NullAway config.
        // The type system is the only enforcement layer needed.

        // new Coffee("Espresso", null, 1); // ← COMPILE ERROR under Valhalla

        // ── Default value fallback — requireNonNullElse ───────────────────────
        //
        // When origin is genuinely optional with a sensible default —
        // requireNonNullElse handles the default value logic.
        // Valhalla's ! ensures the RESULT is non-null. requireNonNullElse
        // provides the value when the input is absent. They do different jobs.

        record CoffeeWithFallback(String! name, String! origin, int shots) {
            CoffeeWithFallback {
                // String! on name — JVM handles null rejection. requireNonNull gone.
                // origin — still needs requireNonNullElse for default value logic,
                // because we want to ACCEPT null origin and substitute a default,
                // rather than reject it outright.
                origin = Objects.requireNonNullElse(origin, "Unknown origin");
            }
        }

        CoffeeWithFallback mysteryBlend = new CoffeeWithFallback("Mystery Blend", null, 1);
        System.out.println("Origin  : " + mysteryBlend.origin().toUpperCase()); // safe — String!

        System.out.println("Brewing : " + resolveBrewingTemp(null));
        System.out.println("Customer: " + resolveCustomerName(null));

        // =====================================================================
        // The happy path
        // =====================================================================

        Order mikeOrder = new Order("Mike", new Coffee("Espresso", "Ethiopian", 1));

        System.out.println("I ordered : " + getCoffeeName(mikeOrder)
                .map(String::toUpperCase).orElse("(no coffee)"));
        System.out.println("It's from : " + getCoffeeOrigin(mikeOrder)
                .map(String::toUpperCase).orElse("(origin unknown)"));

        // =====================================================================
        // What Valhalla removed vs what it kept — the final accounting
        // =====================================================================
        //
        //  REMOVED — replaced by ! in the type:
        //    import org.jspecify.annotations.*   → no import block needed
        //    @NullMarked                          → String! is the declaration
        //    @Nullable / @NonNull                 → presence/absence of ! says it
        //    Objects.requireNonNull(x, msg)       → JVM rejects null before ctor runs
        //    NullAway plugin in pom.xml           → compiler sees ! directly
        //
        //  KEPT — because Valhalla doesn't solve these:
        //    Optional<T> return types             → absence is still a valid state
        //    Objects.requireNonNullElse           → default value logic, not null-guarding
        //    Objects.requireNonNullElseGet        → lazy default value logic
        //
        //  THE RULE:
        //    String!  — I promise this is never null. The JVM enforces the promise.
        //    String   — This might be null. Handle it. Optional helps at return boundaries.
        //    No annotation. No tool. No discipline. Just the type.
    }
}*/
