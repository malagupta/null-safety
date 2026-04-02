package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Live-coding starter — Null Safety in Modern Java
 *
 * Run main() after each step to see the output change.
 * Each TODO marks exactly what to type during the session.
 */
public class LiveCodingSession {

    // ── Domain model — already done so we can focus on null safety ───────
    record Coffee(String name, String origin, String brewingInstructions) {}
    record Order(String customerName, Coffee coffee) {}

    static final Map<String, Coffee> MENU = Map.of(
            "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine. 90°C."),
            "latte",    new Coffee("Latte",    "Colombian", "Shot + steamed milk."),
            "newblend", new Coffee("New Blend", "Rwandan",   null)   // null instructions!
    );

    // ═════════════════════════════════════════════════════════════════════
    // STEP 1 — The problem: null is invisible in the type system
    // Slide: 02_TypicalNPEs
    // ═════════════════════════════════════════════════════════════════════

    static void step1_TheProblem() {
        System.out.println("── Step 1: The NPE problem ──");

        Order order = new Order("Alice", null);

        // TODO: call order.coffee().name() — watch it blow up
        // System.out.println(order.coffee().name());

        // Nothing in the Order signature says coffee can be null.
        // The NPE lands far from where null was introduced.
    }


    // ═════════════════════════════════════════════════════════════════════
    // STEP 2 — Solution 1: null checks
    // Slide: 03_Solutions (Null Checks)
    // ═════════════════════════════════════════════════════════════════════

    // TODO: write serve(Order order)
    //   Step 2a — check at the boundary (one guard, clear message)
    //   Step 2b — show null check hell (nested ifs) for contrast

    // Step 2a answer:
    // static String serve(Order order) {
    //     Coffee coffee = order.coffee();
    //     if (coffee == null) return "No coffee on this order.";
    //     return coffee.name();
    // }

    // Step 2b answer (show the horror, don't leave it in):
    // static String serveNullCheckHell(Order order) {
    //     if (order != null) {
    //         if (order.coffee() != null) {
    //             if (order.coffee().brewingInstructions() != null) {
    //                 return order.coffee().brewingInstructions().toUpperCase();
    //             } else { return "No instructions."; }
    //         } else { return "No coffee."; }
    //     }
    //     return "No order.";
    // }

    static void step2_NullChecks() {
        System.out.println("\n── Step 2: Null checks ──");
        // TODO: call serve() after writing it above
    }


    // ═════════════════════════════════════════════════════════════════════
    // STEP 3 — Solution 2: Optional — contract in the type
    // Slide: 03_Solutions (Optional)
    // ═════════════════════════════════════════════════════════════════════

    // TODO: write findByName(String name) returning Optional<Coffee>
    //   return Optional.ofNullable(MENU.get(name));

    // Step 3 answer:
    // static Optional<Coffee> findByName(String name) {
    //     return Optional.ofNullable(MENU.get(name));
    // }

    static void step3_Optional() {
        System.out.println("\n── Step 3: Optional ──");

        // TODO: chain .map(Coffee::name).orElse("Not found") on findByName("espresso")
        // TODO: show findByName("coldpresso").orElse("Not on menu")
        // TODO: show findByName("coldpresso").orElseThrow() — and why .get() is bad
    }


    // ═════════════════════════════════════════════════════════════════════
    // STEP 4 — Solution 3: Records — fail fast at construction
    // Slide: 04_ModernJavaSolutions1
    // ═════════════════════════════════════════════════════════════════════

    // TODO: define CoffeeOrder record with compact constructor
    //   compact constructor calls Objects.requireNonNull on customerName and coffee

    // Step 4 answer:
    // record CoffeeOrder(String customerName, Coffee coffee, int shots) {
    //     CoffeeOrder {
    //         Objects.requireNonNull(customerName);
    //         Objects.requireNonNull(coffee);
    //         if (shots < 1 || shots > 4) throw new IllegalArgumentException();
    //     }
    // }

    static void step4_Records() {
        System.out.println("\n── Step 4: Records — fail fast ──");

        // TODO: try new CoffeeOrder("Alice", null, 2) — NPE fires at construction
        // This is GOOD: null is rejected before the object exists,
        // not when some method deep in the stack tries to use it.
    }


    // ═════════════════════════════════════════════════════════════════════
    // STEP 5 — Solution 4: JSpecify — @NullMarked + @Nullable
    // Slide: 03_Solutions (JSpecify), 08_SolutionDecisions
    // ═════════════════════════════════════════════════════════════════════
    //
    // Key annotations:
    //   @NullMarked  — every param/field/return in scope is non-null by default
    //   @Nullable    — this specific element may be null
    //   @NullUnmarked — opt out (for legacy code)
    //
    // NullAway (build plugin) turns violations into compile errors.

    // TODO: annotate the class with @NullMarked (put it above the class declaration)

    // TODO: define CoffeeV2 record with @Nullable brewingInstructions
    // record CoffeeV2(String name, String origin, @Nullable String brewingInstructions) {}

    // TODO: define OrderV2 record with @Nullable coffee
    // record OrderV2(String customerName, @Nullable CoffeeV2 coffee) {}

    // TODO: write findCoffee(@Nullable String name) returning @Nullable CoffeeV2
    // static @Nullable CoffeeV2 findCoffee(@Nullable String name) {
    //     if (name == null) return null;
    //     return MENU_V2.get(name);   // Map.get() can return null — that is our contract
    // }

    static void step5_JSpecify() {
        System.out.println("\n── Step 5: JSpecify @NullMarked / @Nullable ──");

        // TODO: create an OrderV2 with null coffee — show it's allowed (@Nullable)
        // TODO: show that accessing coffee().name() without a null check
        //       would be a compile error with NullAway (show the commented line)
        // TODO: show proper null check before dereference
    }


    // ═════════════════════════════════════════════════════════════════════
    // STEP 6 — @NullUnmarked: bridging legacy code
    // Slide: 08_SolutionDecisions (Do you control the boundary?)
    // ═════════════════════════════════════════════════════════════════════

    @NullUnmarked  // opts this class out of @NullMarked enforcement
    static class LegacyCoffeeApi {
        // Pretend this is third-party / pre-JSpecify code.
        // Returns null when not found. NullAway won't enforce here.
        static Coffee findLegacy(String name) {
            return Map.of("espresso",
                    new Coffee("Espresso", "Ethiopian", "Grind fine.")).get(name);
        }
    }

    // TODO: write findFromLegacy(String name) returning Optional<Coffee>
    //   wrap LegacyCoffeeApi.findLegacy with Optional.ofNullable
    //   null never crosses this boundary inward

    // Step 6 answer:
    // static Optional<Coffee> findFromLegacy(String name) {
    //     return Optional.ofNullable(LegacyCoffeeApi.findLegacy(name));
    // }

    static void step6_LegacyBridge() {
        System.out.println("\n── Step 6: @NullUnmarked legacy bridge ──");
        // TODO: call findFromLegacy("espresso") and findFromLegacy("coldpresso")
        // The Optional wrapper is the single containment point for the legacy null.
    }


    // ═════════════════════════════════════════════════════════════════════
    // MAIN — uncomment each step as you code it
    // ═════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        step1_TheProblem();
        step2_NullChecks();
        step3_Optional();
        step4_Records();
        step5_JSpecify();
        step6_LegacyBridge();
    }
}
