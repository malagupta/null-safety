package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

// ═══════════════════════════════════════════════════════════════════════════
// LIVE CODE — The Past, Present, and Future of Null Safety in Java
//
// Each step builds on the previous. Nothing is deleted — only added.
// Run main() at any step to see where things stand.
// ═══════════════════════════════════════════════════════════════════════════

public class Demo00_LiveCode {

    // ─────────────────────────────────────────────────────────────────────
    // STEP 1 — The world before null safety
    //          Plain classes. No validation. This is where most codebases start.
    // ─────────────────────────────────────────────────────────────────────

    static class Coffee_v1 {
        String name;
        String origin;
        String brewingInstructions;

        Coffee_v1(String name, String origin, String brewingInstructions) {
            this.name                = name;
            this.origin              = origin;
            this.brewingInstructions = brewingInstructions;
        }
    }

    static class Order_v1 {
        String    customerName;
        Coffee_v1 coffee;

        Order_v1(String customerName, Coffee_v1 coffee) {
            this.customerName = customerName;
            this.coffee       = coffee;
        }
    }

    static void step1() {
        System.out.println("── Step 1: no validation ──");

        Order_v1 order = new Order_v1("Jonathan", null); // accepted silently

        // Three lines later, somewhere completely different in the codebase:
        System.out.println(order.coffee.name);           // 💥 NPE
                                                         // coffee is null
                                                         // .name doesn't exist on null
    }


    // ─────────────────────────────────────────────────────────────────────
    // STEP 2 — requireNonNull: fail at the source, not three frames later
    //          The bad object never exists.
    // ─────────────────────────────────────────────────────────────────────

    static class Coffee_v2 {
        String name;
        String origin;
        String brewingInstructions;     // null is valid — new items may not have instructions

        Coffee_v2(String name, String origin, String brewingInstructions) {
            this.name   = Objects.requireNonNull(name,   "Coffee needs a name");
            this.origin = Objects.requireNonNull(origin, "Coffee needs an origin");
            this.brewingInstructions = brewingInstructions; // no check — null is ok here
        }
    }

    static class Order_v2 {
        String    customerName;
        Coffee_v2 coffee;               // null is valid — customer may not have chosen yet

        Order_v2(String customerName, Coffee_v2 coffee) {
            this.customerName = Objects.requireNonNull(customerName, "Order needs a customer");
            this.coffee       = coffee; // no check — null is ok here
        }
    }

    static void step2() {
        System.out.println("── Step 2: requireNonNull ──");

        try {
            Coffee_v2 bad = new Coffee_v2(null, "Ethiopian", null);
        } catch (NullPointerException e) {
            System.out.println("Caught at construction: " + e.getMessage());
            // "Coffee needs a name"
            // Stack trace points HERE — not three frames away at the dereference.
        }

        Order_v2 pending = new Order_v2("Jonathan", null); // valid — no coffee yet
        System.out.println("Pending order: " + pending.customerName + ", coffee=" + pending.coffee);
    }


    // ─────────────────────────────────────────────────────────────────────
    // STEP 3 — Helpful NPE (Java 14, JEP 358)
    //          Same classes. New JVM. Completely different error message.
    // ─────────────────────────────────────────────────────────────────────

    static String getInstructions(Order_v2 order) {
        return order.coffee.brewingInstructions.toUpperCase();
        //           ↑                ↑
        //     null here?        or null here?
        // Before Java 14: "NullPointerException at LiveCode.java:89" — that's all.
        // Java 14+: tells you WHICH variable was null.
    }

    static void step3() {
        System.out.println("── Step 3: JEP 358 helpful NPE ──");

        Order_v2 noCoffee = new Order_v2("Steve", null);
        try {
            getInstructions(noCoffee);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
            // "Cannot invoke Coffee_v2.brewingInstructions because
            //  LiveCode.Order_v2.coffee is null"
            // Exact field. Exact reason. Zero guessing.
        }

        Order_v2 noInstructions = new Order_v2("Harry",
                new Coffee_v2("New Blend", "Rwandan", null));
        try {
            getInstructions(noInstructions);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
            // "Cannot invoke String.toUpperCase() because
            //  LiveCode.Coffee_v2.brewingInstructions is null"
            // Same line. Different null. Different message.
        }
    }


    // ─────────────────────────────────────────────────────────────────────
    // STEP 4 — Signatures lie by omission
    //          What does findByName return when the coffee isn't on the menu?
    //          The signature says Coffee. The reality says null. Nobody told you.
    // ─────────────────────────────────────────────────────────────────────

    static final Map<String, Coffee_v2> MENU = new HashMap<>();
    static {
        MENU.put("espresso", new Coffee_v2("Espresso", "Ethiopian", "Grind fine. 90°C."));
        MENU.put("latte",    new Coffee_v2("Latte",    "Colombian", "Shot + steamed milk."));
        MENU.put("newblend", new Coffee_v2("New Blend","Rwandan",   null));
    }

    // Return type says Coffee_v2. Doesn't say "or null".
    // Every caller has to either know this or discover it the hard way.
    static Coffee_v2 findByName_unsafe(String name) {
        return MENU.get(name);  // null if not found — Map.get() contract
    }

    static void step4() {
        System.out.println("── Step 4: signatures lie ──");

        Coffee_v2 found   = findByName_unsafe("espresso");
        Coffee_v2 missing = findByName_unsafe("coldpresso"); // null — caller has no idea

        System.out.println("Found   : " + found.name);
        System.out.println("Missing : " + missing.name); // 💥 NPE — nothing warned us
    }


    // ─────────────────────────────────────────────────────────────────────
    // STEP 5 — Optional: absence becomes visible in the return type
    //          The signature now tells the truth.
    // ─────────────────────────────────────────────────────────────────────

    static Optional<Coffee_v2> findByName(String name) {
        return Optional.ofNullable(MENU.get(name));
        // Optional.empty() if not found — never null
        // The return type IS the contract now. Caller cannot ignore absence.
    }

    // Optional.or()             — chain fallback sources, first non-empty wins (Java 9)
    // Optional.ifPresentOrElse() — handle both cases in one call (Java 9)
    // Optional.stream()          — bridge to Stream without null entering pipeline (Java 9)

    static void step5() {
        System.out.println("── Step 5: Optional return type ──");

        // ✅ happy path
        findByName("espresso")
                .ifPresentOrElse(
                    c -> System.out.println("Serving  : " + c.name),
                    () -> System.out.println("Not found: espresso")
                );

        // ✅ missing — Optional.empty() — no NPE, no surprise
        findByName("coldpresso")
                .ifPresentOrElse(
                    c -> System.out.println("Serving  : " + c.name),
                    () -> System.out.println("Not found: coldpresso")
                );

        // ✅ chain: primary → backup, first non-empty wins (lazy)
        Optional<Coffee_v2> result = findByName("coldpresso")       // empty
                .or(() -> findByName("newblend"));                   // present — stops here
        System.out.println("Fallback : " + result.map(c -> c.name).orElse("nothing"));

        // ✅ stream pipeline — null never enters
        List<String> names = List.of("espresso", "coldpresso", "latte", "mystery");
        List<Coffee_v2> found = names.stream()
                                     .map(Demo00_LiveCode::findByName)    // Stream<Optional<Coffee_v2>>
                                     .flatMap(Optional::stream)    // empties removed, null never enters
                                     .collect(Collectors.toList());
        System.out.println("Pipeline : " + found.size() + " found");
    }


    // ─────────────────────────────────────────────────────────────────────
    // STEP 6 — Null-hostile collections (Java 9)
    //          List.of() / Map.of() reject null at the boundary, not at use.
    // ─────────────────────────────────────────────────────────────────────

    static void step6() {
        System.out.println("── Step 6: null-hostile collections ──");

        // ❌ ArrayList accepts null silently — NPE deferred to wherever it's used
        List<Coffee_v2> unsafe = new ArrayList<>();
        unsafe.add(new Coffee_v2("Espresso", "Ethiopian", null));
        unsafe.add(null);                                // accepted — null travels in
        System.out.println("ArrayList: accepted at add() — NPE deferred");

        // ✅ List.of() rejects null immediately — gap between cause and crash = zero
        try {
            List<Coffee_v2> safe = List.of(
                    new Coffee_v2("Espresso", "Ethiopian", null),
                    null                                 // 💥 NPE fires HERE
            );
        } catch (NullPointerException e) {
            System.out.println("List.of(): rejected immediately at factory call");
        }
    }


    // ─────────────────────────────────────────────────────────────────────
    // STEP 7 — Records (Java 16)
    //          Compact constructor. The bad object cannot exist.
    //          Fields final. No setters. Immutable by definition.
    // ─────────────────────────────────────────────────────────────────────

    record Coffee(String name, String origin, String brewingInstructions) {
        Coffee {
            Objects.requireNonNull(name,   "Coffee needs a name");
            Objects.requireNonNull(origin, "Coffee needs an origin");
            // brewingInstructions — not checked. null is valid.
        }
    }

    record Order(String customerName, Coffee coffee) {
        Order {
            Objects.requireNonNull(customerName, "Order needs a customer");
            // coffee — not checked. Customer may not have chosen yet.
        }

        Optional<String> getCoffeeName() {
            return Optional.ofNullable(coffee).map(Coffee::name);
        }
    }

    static void step7() {
        System.out.println("── Step 7: records ──");

        // ✅ valid
        Coffee espresso = new Coffee("Espresso", "Ethiopian", "Grind fine.");
        System.out.println("Coffee  : " + espresso.name() + " / " + espresso.origin());

        // ✅ null instructions — accepted
        Coffee newBlend = new Coffee("New Blend", "Rwandan", null);
        System.out.println("Blend   : " + newBlend.name() + " / instructions=" + newBlend.brewingInstructions());

        // ❌ null name — rejected at construction, NPE fires here not later
        try {
            Coffee bad = new Coffee(null, "Ethiopian", null);
        } catch (NullPointerException e) {
            System.out.println("Caught  : " + e.getMessage());
        }

        // ✅ order without coffee — valid state
        Order pending = new Order("Jonathan", null);
        System.out.println("Pending : " + pending.customerName() + " / " + pending.getCoffeeName().orElse("still deciding"));
    }


    // ─────────────────────────────────────────────────────────────────────
    // STEP 8 — Sealed classes (Java 17)
    //          null return replaced by an explicit named type.
    //          Compiler verifies every case is handled.
    // ─────────────────────────────────────────────────────────────────────

    sealed interface CoffeeResult
            permits Found, NotFound {}

    record Found(Coffee coffee)    implements CoffeeResult {}
    record NotFound(String name)   implements CoffeeResult {}

    static CoffeeResult lookup(String name) {
        Map<String, Coffee> menu = Map.of(
                "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine."),
                "latte",    new Coffee("Latte",    "Colombian", "Shot + milk.")
        );
        Coffee c = menu.get(name);
        return c != null ? new Found(c) : new NotFound(name);
        // No null returned. Ever.
        // Found, NotFound — two explicit states. Caller cannot ignore either.
    }

    static void step8() {
        System.out.println("── Step 8: sealed classes ──");

        List.of("espresso", "coldpresso").forEach(name -> {
            String msg = switch (lookup(name)) {
                case Found    f -> "Serving : " + f.coffee().name();
                case NotFound n -> "Missing : " + n.name() + " not on menu";
                // No default needed — compiler verifies all subtypes are covered.
                // Add a new subtype tomorrow, forget to handle it here — compile error.
            };
            System.out.println(msg);
        });
    }


    // ─────────────────────────────────────────────────────────────────────
    // STEP 9 — Pattern matching + null case (Java 16 / Java 21)
    //          instanceof null = false (always, by spec — no guard needed)
    //          switch null case = null as a first-class case label
    // ─────────────────────────────────────────────────────────────────────

    static void step9() {
        System.out.println("── Step 9: pattern matching + null case ──");

        // instanceof null is always false — no null check needed before it
        Coffee maybe = null;
        if (maybe instanceof Coffee c) {
            System.out.println("Should not print");
        } else {
            System.out.println("instanceof null = false. Always. No NPE.");
        }

        // switch + null case (Java 21)
        // Before Java 21: switching on null threw NPE before any case ran.
        // Java 21: null is a first-class case.
        List<String> sizes = new ArrayList<>();
        sizes.add(null);
        sizes.add("small");
        sizes.add("large");

        sizes.forEach(size -> {
            String label = switch (size) {
                case null    -> "not specified — defaulting to regular";
                case "small" -> "8oz";
                case "large" -> "16oz";
                default      -> "regular";
            };
            System.out.println("Size: " + label);
        });

        // Sealed type + switch: exhaustive, no default, null handled explicitly
        List.of("espresso", "coldpresso").forEach(name -> {
            String msg = switch (lookup(name)) {
                case Found    f -> f.coffee().name();
                case NotFound n -> n.name() + " not found";
            };
            System.out.println("Lookup: " + msg);
        });
    }


    // ─────────────────────────────────────────────────────────────────────
    // STEP 10 — @NullMarked (JSpecify 1.0 + NullAway)
    //           Non-null becomes the default. @Nullable marks the exceptions.
    //           Violations fail the BUILD — not a squiggle, the build.
    // ─────────────────────────────────────────────────────────────────────

    // package-info.java (separate file — shown here for reference):
    //
    //   @NullMarked
    //   package com.gupta.session;
    //
    //   import org.jspecify.annotations.NullMarked;
    //
    // That one annotation on the package declaration makes every field,
    // parameter, and return type in the package non-null by default.
    // @Nullable then marks the deliberate exceptions — silence means non-null.

    @NullMarked
    record Coffee_annotated(String name, String origin, @Nullable String brewingInstructions) {
        Coffee_annotated {
            Objects.requireNonNull(name,   "Coffee needs a name");
            Objects.requireNonNull(origin, "Coffee needs an origin");
            // @Nullable on brewingInstructions — annotation now says what the comment said
        }
    }

    @NullMarked
    record Order_annotated(String customerName, @Nullable Coffee_annotated coffee) {
        Order_annotated {
            Objects.requireNonNull(customerName, "Order needs a customer");
        }

        Optional<String> getCoffeeName() {
            return Optional.ofNullable(coffee).map(Coffee_annotated::name);
        }
    }

    // With NullAway in the build (pom.xml / build.gradle):
    //
    //   <arg>-XepOpt:NullAway:AnnotatedPackages=com.gupta.session</arg>
    //   <arg>-XepOpt:NullAway:JSpecifyMode=true</arg>
    //   <arg>-XepOpt:NullAway:OnlyNullMarked=true</arg>  ← enforce only annotated packages
    //
    // Now this is a COMPILE ERROR:
    //   new Coffee_annotated(null, "Ethiopian", null)
    //                         ↑
    //   [NullAway] passing @Nullable parameter where @NonNull is required
    //
    // The test still exists. But it catches callers that bypass annotations —
    // reflection, legacy code, unannotated modules. Runtime stays the last line.

    static void step10() {
        System.out.println("── Step 10: @NullMarked ──");

        Coffee_annotated espresso = new Coffee_annotated("Espresso", "Ethiopian", "Grind fine.");
        Coffee_annotated newBlend = new Coffee_annotated("New Blend", "Rwandan",  null);

        Order_annotated withCoffee = new Order_annotated("Alice",   espresso);
        Order_annotated pending    = new Order_annotated("Jonathan", null); // @Nullable — valid

        System.out.println("With coffee : " + withCoffee.getCoffeeName().orElse("none"));
        System.out.println("Pending     : " + pending.getCoffeeName().orElse("still deciding"));
    }


    // ─────────────────────────────────────────────────────────────────────
    // STEP 11 — Valhalla (illustrative — not compilable today)
    //           String! = null-restricted type. JVM enforced.
    //           No annotation. No requireNonNull. No NullAway config.
    // ─────────────────────────────────────────────────────────────────────

    // What Coffee looks like under Valhalla:
    //
    //   record Coffee(String! name, String! origin, String brewingInstructions) {}
    //                        ↑              ↑               ↑
    //                  null-restricted  null-restricted   nullable (no !)
    //                  JVM enforced     JVM enforced
    //
    // String! name — passing null is a JVM error before the constructor runs.
    // No requireNonNull needed. No annotation needed. The type says no.
    //
    // Today (Step 10): three mechanisms — @NullMarked + requireNonNull + NullAway
    // Valhalla:        one mechanism    — String!


    // ─────────────────────────────────────────────────────────────────────
    // DECISION FRAMEWORK — three questions, answered in order
    // ─────────────────────────────────────────────────────────────────────

    // Q1: Valid state or bug?
    //     Valid  → @Nullable + Optional return type + null check where used
    //     Bug    → requireNonNull at construction — fail fast, fail loud
    //
    // Q2: Control the boundary?
    //     Yes    → @NullMarked + @Nullable + NullAway in CI
    //     No     → adapter wraps at boundary with Optional.ofNullable — once, never again
    //
    // Q3: Java version?
    //     8–11   → Optional, requireNonNull, JSpecify + NullAway
    //     16+    → add records (compact constructor validation)
    //     17+    → add sealed classes (null return replaced by type)
    //     21+    → switch null case, pattern matching stable
    //     Valhalla → String! — the type system says no. Final answer.
    //
    // One rule across all three:
    //     Pick ONE layer and enforce it.
    //     Annotations without enforcement = documentation.

    static Coffee_v2 wrapLegacyApi(String name) {
        return MENU_V2.getOrDefault(name, DEFAULT_COFFEE); // never null, no Optional needed
    }

    static final Map<String, Coffee_v2> MENU_V2 = new HashMap<>();
    static final Coffee_v2 DEFAULT_COFFEE = new Coffee_v2("House Blend", "Unknown", null);
    static {
        MENU_V2.put("espresso", new Coffee_v2("Espresso", "Ethiopian", "Grind fine."));
        MENU_V2.put("latte",    new Coffee_v2("Latte",    "Colombian", "Shot + milk."));
    }


    // ─────────────────────────────────────────────────────────────────────
    // MAIN — run each step to see the progression
    // ─────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {

        // step1(); // ← uncomment to see the raw NPE with no message

        step2();
        System.out.println();
        step3();
        System.out.println();

        // step4(); // ← uncomment to see the "signatures lie" NPE

        step5();
        System.out.println();
        step6();
        System.out.println();
        step7();
        System.out.println();
        step8();
        System.out.println();
        step9();
        System.out.println();
        step10();
    }
}