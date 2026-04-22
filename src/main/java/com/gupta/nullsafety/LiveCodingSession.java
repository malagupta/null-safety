package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Live-coding session
 *
 * Flow:
 *   Act 0 — Same prompt to AI, no CLAUDE.md rules → NPE
 *   Act 1 — Why: null is invisible in the type system
 *   Act 2 — Fix: null check at the boundary
 *   Act 3 — Fix: Optional puts absence in the type
 *   Act 4 — Fix: Records + JSpecify encode the contract
 *   Act 5 — Close the loop: update CLAUDE.md, same AI prompt → no NPE
 *
 * Each TODO marks what to type live.
 * Commented blocks are the answers — reveal only after asking the audience.
 */
public class LiveCodingSession {

    // ── Domain model — pre-built so we can focus on null safety ──────────
    record Coffee(String name, String origin, String brewingInstructions) {}
    record Order(String customerName, Coffee coffee) {}

    static final Map<String, Coffee> MENU = Map.of(
            "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine. 90°C."),
            "latte",    new Coffee("Latte",    "Colombian", "Shot + steamed milk."),
            "newblend", new Coffee("New Blend", "Rwandan",   null)   // null instructions!
    );


    // ═════════════════════════════════════════════════════════════════════
    // ACT 0 — What the AI generated for us (no CLAUDE.md null-safety rules)
    //
    // Prompt: "Write a method that finds a coffee by name and returns its
    //          brewing instructions in uppercase."
    //
    // The AI wrote reasonable-looking code. It compiles. Tests pass on the
    // happy path. Two NPEs are hiding inside.
    // ═════════════════════════════════════════════════════════════════════

    static String aiGenerated_getBrewingInstructions(String name) {
        Coffee coffee = MENU.get(name);                       // null if name not in menu
        return coffee.brewingInstructions().toUpperCase();    // NPE #1 or NPE #2
    }

    static void act0_AIWithoutGuidance() {
        System.out.println("── Act 0: AI output with no CLAUDE.md guidance ──");

        // Works on the happy path — lulls you into confidence
        System.out.println(aiGenerated_getBrewingInstructions("espresso"));

        // TODO: uncomment one at a time to reveal the two hidden NPEs:
        // System.out.println(aiGenerated_getBrewingInstructions("newblend"));   // NPE: instructions null
        // System.out.println(aiGenerated_getBrewingInstructions("coldpresso")); // NPE: coffee null
    }


    // ═════════════════════════════════════════════════════════════════════
    // ACT 1 — Why: null is invisible in the type system
    // Slide: 01_WhyNullExists → 02_TypicalNPEs
    // ═════════════════════════════════════════════════════════════════════

    static void act1_TheProblem() {
        System.out.println("\n── Act 1: The problem ──");

        // Map.get() returns V — no indication that null is possible
        // Coffee.brewingInstructions() returns String — no indication either
        // The AI had no signal to add null checks. Neither does the next developer.

        Order order = new Order("Alice", null);

        // TODO: uncomment — NPE, but nothing in Order(String, Coffee) warns you:
        // System.out.println(order.coffee().name());

        System.out.println("Order: " + order.customerName() + " — coffee=" + order.coffee());
    }


    // ═════════════════════════════════════════════════════════════════════
    // ACT 2 — Fix 1: null check at the boundary
    // Slide: 03_Solutions (Null Checks)
    // ═════════════════════════════════════════════════════════════════════

    // TODO: write serve(Order order) — one guard at the boundary
    // static String serve(Order order) {
    //     Coffee coffee = order.coffee();
    //     if (coffee == null) return "No coffee on this order.";
    //     return coffee.name();
    // }

    // Contrast (show briefly, don't leave in):
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

    static void act2_NullChecks() {
        System.out.println("\n── Act 2: Null checks ──");
        // TODO: call serve() with null coffee and with a real coffee
    }


    // ═════════════════════════════════════════════════════════════════════
    // ACT 3 — Fix 2: Optional — put absence in the type
    // Slide: 03_Solutions (Optional)
    // ═════════════════════════════════════════════════════════════════════

    // TODO: write findByName(String name) returning Optional<Coffee>
    // static Optional<Coffee> findByName(String name) {
    //     return Optional.ofNullable(MENU.get(name));
    // }

    static void act3_Optional() {
        System.out.println("\n── Act 3: Optional ──");

        // TODO: chain .map(Coffee::name).orElse("Not found") on findByName("espresso")
        // TODO: show "coldpresso" → .orElse("Not on menu — try our cold brew")
        // TODO: show .orElseThrow() — when absence is a bug, not a state
        // TODO: show why .get() is an anti-pattern (same as null deref, just wrapped)
    }


    // ═════════════════════════════════════════════════════════════════════
    // ACT 4 — Fix 3 + 4: Records (fail fast) + JSpecify (encoded contract)
    // Slide: 04_ModernJavaSolutions1, 03_Solutions (JSpecify)
    // ═════════════════════════════════════════════════════════════════════

    // TODO 4a: define CoffeeOrder — compact constructor rejects null at construction
    // record CoffeeOrder(String customerName, Coffee coffee, int shots) {
    //     CoffeeOrder {
    //         Objects.requireNonNull(customerName);
    //         Objects.requireNonNull(coffee);
    //         if (shots < 1 || shots > 4) throw new IllegalArgumentException();
    //     }
    // }

    // TODO 4b: add @NullMarked above the class declaration, then define:
    // record CoffeeV2(String name, String origin, @Nullable String brewingInstructions) {}
    // record OrderV2(String customerName, @Nullable CoffeeV2 coffee) {}

    static void act4_RecordsAndJSpecify() {
        System.out.println("\n── Act 4: Records + JSpecify ──");

        // Records: null is caught at construction, not 20 frames later
        // TODO: new CoffeeOrder("Bob", null, 2) — NPE fires here, right now

        // JSpecify: @Nullable is the contract. The type says it. No comment needed.
        // TODO: new OrderV2("Jonathan", null) — allowed, @Nullable says so
        // TODO: show the line that NullAway would reject as a compile error:
        //   String name = pending.coffee().name();  // dereference @Nullable without check

        // Safe dereference:
        // TODO: check pending.coffee() != null before calling .name()
    }


    // ═════════════════════════════════════════════════════════════════════
    // ACT 5 — Close the loop: update CLAUDE.md live, re-prompt AI
    //
    // LIVE EDIT: open CLAUDE.md and add the null-safety rules block.
    // Then show what the AI generates for the exact same prompt as Act 0.
    // ═════════════════════════════════════════════════════════════════════

    // Same prompt, same AI — but now CLAUDE.md has null-safety rules:
    //   "Write a method that finds a coffee by name and returns its
    //    brewing instructions in uppercase."
    //
    // AI now generates this (no NPEs, Optional contract, @NullMarked context):
    static Optional<String> aiGenerated_v2_getBrewingInstructions(String name) {
        return Optional.ofNullable(MENU.get(name))
                .map(Coffee::brewingInstructions)
                .map(String::toUpperCase);
    }

    static void act5_CloseTheLoop() {
        System.out.println("\n── Act 5: Same prompt — after updating CLAUDE.md ──");

        // All three cases handled. No explicit null checks. No NPE possible.
        aiGenerated_v2_getBrewingInstructions("espresso")
                .ifPresentOrElse(
                    i -> System.out.println("espresso    : " + i),
                    () -> System.out.println("espresso    : No instructions"));

        aiGenerated_v2_getBrewingInstructions("newblend")
                .ifPresentOrElse(
                    i -> System.out.println("newblend    : " + i),
                    () -> System.out.println("newblend    : No instructions"));

        aiGenerated_v2_getBrewingInstructions("coldpresso")
                .ifPresentOrElse(
                    i -> System.out.println("coldpresso  : " + i),
                    () -> System.out.println("coldpresso  : Coffee not found"));
    }


    // ═════════════════════════════════════════════════════════════════════
    // MAIN — uncomment each act as you reach it in the session
    // ═════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        act0_AIWithoutGuidance();
        act1_TheProblem();
        act2_NullChecks();
        act3_Optional();
        act4_RecordsAndJSpecify();
        act5_CloseTheLoop();
    }
}
