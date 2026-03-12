package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DEMO 4 — What to consider when picking a solution
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Chandra/ Mala:
 * "This is what senior developers actually came for.
 *  Not a list of tools — a decision framework.
 *  Three questions. Answer them in order. The right tool follows."
 */
@NullMarked
public class Demo4_HowToPickASolution {

    record Coffee(String name, String origin, @Nullable String brewingInstructions) {
        Coffee {
            Objects.requireNonNull(name,   "Coffee must have a name");
            Objects.requireNonNull(origin, "Coffee must have an origin");
        }
    }

    record Order(String customerName, @Nullable Coffee coffee) {
        Order {
            Objects.requireNonNull(customerName, "Order must have a customer name");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // DECISION QUESTION 1
    // Is null a valid domain state, or is it a bug?
    // ─────────────────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Question one. The most important question. Everything else follows from it.
    //
    //  If null means 'this is a valid, expected, business state' —
    //  you model it. You communicate it. You handle it gracefully.
    //
    //  If null means 'this should never happen given correct inputs' —
    //  you reject it immediately. Loudly. At the entry point.
    //  You do NOT silently swallow it with a null check that carries on.
    //
    //  The mistake most codebases make: treating all nulls the same.
    //  Null-checking everything defensively, regardless of which kind it is."

    // CASE A — null is a valid state: model it explicitly
    // "An order can exist before the customer has chosen their coffee.
    //  That's not a bug. That's the order being in an 'incomplete' state.
    //  The @Nullable annotation — or Valhalla's Coffee without ! —
    //  communicates: 'callers, check before you use this.'"
    static Optional<String> getCoffeeName(Order order) {
        // @Nullable on order.coffee() — Optional makes the contract explicit to callers
        return Optional.ofNullable(order.coffee()).map(Coffee::name);
    }

    // CASE B — null is a bug: reject it immediately with requireNonNull
    // "A CoffeeStation without a grinder is not in an 'incomplete' state.
    //  It's broken. If Spring injection failed, we want to know NOW —
    //  at startup — not when the first customer orders."
    static class CoffeeStation {
        private final Coffee signature;
        private final String location;

        CoffeeStation(Coffee signature, String location) {
            // Objects.requireNonNull — fail at construction, clear message
            // The broken object never exists. The NPE fires at the source.
            this.signature = Objects.requireNonNull(signature, "Station needs a signature coffee");
            this.location  = Objects.requireNonNull(location,  "Station needs a location");
        }

        void serve() {
            // No null checks needed here — constructor already guaranteed these
            System.out.println("Serving " + signature.name() + " at " + location);
        }
    }

    static void demo_Question1() {
        System.out.println("══ Q1: Valid state or bug? ══");

        // Valid state — coffee absent, handled via Optional
        Order pending = new Order("Jonathan", null);
        String name = getCoffeeName(pending).orElse("Not chosen yet");
        System.out.println("Valid null : " + name);

        // Bug — null grinder, rejected at construction
        try {
            CoffeeStation broken = new CoffeeStation(null, "Counter 3");
        } catch (NullPointerException e) {
            System.out.println("Bug null   : " + e.getMessage()); // clear, immediate
        }

        // Chandra/ Mala:
        // "Same technique — Objects.requireNonNull — but different intent.
        //  In Order, @Nullable coffee is a documented, valid absence.
        //  In CoffeeStation, null coffee is a broken invariant.
        //  The distinction drives every other decision."
    }

    // ─────────────────────────────────────────────────────────────────────
    // DECISION QUESTION 2
    // Do you control the boundary where null enters?
    // ─────────────────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Question two. Do you control the code that returns null — or not?
    //
    //  If you control it: JSpecify @NullMarked + @Nullable + NullAway.
    //  Make the contract visible in the type, enforce it at build time.
    //
    //  If you DON'T control it — an external API, a legacy library,
    //  the JDK's own Map.get() — you have one job:
    //  wrap it at the boundary. Once. In one place.
    //  After that, your code never sees the raw null."

    // External API you cannot change — returns null when not found
    // Simulates a legacy library, third-party SDK, or core Java API
    static class LegacyCoffeeApi {
        private static final Map<String, Coffee> store = new HashMap<>();
        static {
            store.put("espresso", new Coffee("Espresso", "Ethiopian", "Grind fine."));
            store.put("latte",    new Coffee("Latte",     "Colombian", "Shot + milk."));
        }
        // Returns null — you cannot change this
        static Coffee findByName(String name) { return store.get(name); }
        static String getOrigin(String name)  { return null; } // always null — broken API
    }

    // Your adapter — the ONE place that knows about the external null contract
    // Wrap at the boundary. Null never escapes inward.
    static class CoffeeAdapter {

        // Wrap nullable return in Optional — once, here, never again
        static Optional<Coffee> findByName(String name) {
            return Optional.ofNullable(LegacyCoffeeApi.findByName(name));
        }

        // When a default makes sense — getOrDefault eliminates null entirely
        static Coffee findByNameOrDefault(String name, Coffee fallback) {
            return Objects.requireNonNullElse(LegacyCoffeeApi.findByName(name), fallback);
        }
    }

    static void demo_Question2() {
        System.out.println("\n══ Q2: Control the boundary? ══");

        // ✅ You control it — JSpecify + @Nullable + NullAway (see Demo 3)
        // ✅ You don't control it — wrap once at the adapter

        Coffee fallback = new Coffee("House Blend", "Unknown", null);

        // Key not found — adapter returns Optional.empty(), not null
        String missing = CoffeeAdapter.findByName("coldpresso")
                .map(Coffee::name)
                .orElse("Not on menu");
        System.out.println("Adapter    : " + missing);

        // Key not found — adapter returns fallback, never null
        Coffee found = CoffeeAdapter.findByNameOrDefault("coldpresso", fallback);
        System.out.println("Default    : " + found.name()); // House Blend — never null

        // Chandra/ Mala:
        // "The adapter is your anti-corruption layer.
        //  It owns all knowledge of the external API's null behaviour.
        //  The rest of your codebase imports the adapter — never the raw API.
        //  Null stays at the door."
    }

    // ─────────────────────────────────────────────────────────────────────
    // DECISION QUESTION 3
    // What is your Java version?
    // ─────────────────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Question three. What Java version are you on?
    //  Not which version you want — which version you're running in production today.
    //  The tools available to you are different at each level."

    // ── Java 8-11 ─────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Java 8-11: Optional for return types, requireNonNull in constructors,
    //  JSpecify annotations with NullAway if you can add a build plugin.
    //  That combination gives you contract documentation plus build enforcement.
    //  It's not perfect — but it catches the majority of null bugs before they ship."

    // ── Java 16+ Records ──────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Java 16: Records. This is underused for null safety.
    //  A record's compact constructor lets you validate ALL fields at construction.
    //  The bad object never exists. The NPE fires immediately at the source.
    //  Not three method calls later. Not in production. At the constructor call site."

    record CoffeeOrder(String customerName, Coffee coffee, int shots) {
        CoffeeOrder {
            Objects.requireNonNull(customerName, "Customer name required");
            Objects.requireNonNull(coffee,       "Coffee required — use pending state for incomplete orders");
            if (shots < 1 || shots > 4) throw new IllegalArgumentException("Shots must be 1-4");
        }
        // Chandra/ Mala:
        // "Three validations. One compact constructor. The object is either
        //  fully valid or it doesn't exist. No partially-constructed state.
        //  No null fields hiding inside a real object."
    }

    // ── Java 17+ Sealed classes ───────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Java 17: Sealed classes. The compiler now knows EVERY possible type.
    //  Switch expressions on sealed types must be exhaustive.
    //  Add a new subtype and forget to handle it — compile error.
    //  The forgotten null-returning case becomes impossible."

    sealed interface CoffeeResult permits Found, NotFound, Unavailable {}
    record Found(Coffee coffee)  implements CoffeeResult {}
    record NotFound(String name) implements CoffeeResult {}
    record Unavailable(String reason) implements CoffeeResult {}

    static CoffeeResult lookupCoffee(String name) {
        if (name.equals("coldpresso"))  return new NotFound(name);
        if (name.equals("outofstock"))  return new Unavailable("Beans not delivered today");
        Coffee c = new Coffee("Espresso", "Ethiopian", "Grind fine.");
        return new Found(c);
        // Chandra/ Mala:
        // "No null returned anywhere. The return type IS the contract.
        //  Found, NotFound, Unavailable — three explicit states.
        //  The caller cannot ignore any of them."
    }

    static String describeLookup(CoffeeResult result) {
        return switch (result) {
            case Found      f -> "Found: "       + f.coffee().name();
            case NotFound   n -> "Not found: "   + n.name();
            case Unavailable u -> "Unavailable: " + u.reason();
            // No default needed — compiler verifies all cases covered
            // Add a new sealed subtype without handling it here = compile error
        };
    }

    // ── Java 21+ Switch null case ─────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Java 21: null as an explicit switch case.
    //  Before Java 21, a null value in a switch threw NPE before any case ran.
    //  Java 21 lets you handle it explicitly — null is no longer silent."

    static String describeOrder(@Nullable Order order) {
        return switch (order) {
            case null  -> "No order — walk-in with no details";
            case Order o when o.coffee() == null -> "Order for " + o.customerName() + " — coffee not chosen";
            case Order o -> "Order for " + o.customerName() + ": " + o.coffee().name();
        };
    }

    static void demo_Question3() {
        System.out.println("\n══ Q3: Java version? ══");

        // Records — validation at construction
        try {
            CoffeeOrder bad = new CoffeeOrder(null, new Coffee("Espresso","Ethiopian",null), 1);
        } catch (NullPointerException e) {
            System.out.println("Record     : " + e.getMessage());
        }

        // Sealed — exhaustive handling, no null return
        System.out.println("Sealed     : " + describeLookup(lookupCoffee("espresso")));
        System.out.println("Sealed     : " + describeLookup(lookupCoffee("coldpresso")));
        System.out.println("Sealed     : " + describeLookup(lookupCoffee("outofstock")));

        // Switch null case — Java 21
        System.out.println("Switch null: " + describeOrder(null));
        System.out.println("Switch null: " + describeOrder(new Order("Bob", null)));
        System.out.println("Switch null: " + describeOrder(
                new Order("Alice", new Coffee("Latte", "Colombian", null))));
    }

    public static void main(String[] args) {
        demo_Question1();
        demo_Question2();
        demo_Question3();

        // Chandra/ Mala:
        // "The decision framework in one table:
        //
        //  Q1: Valid state or bug?
        //      Valid  → @Nullable + Optional as return type + null check
        //      Bug    → Objects.requireNonNull at construction — fail fast
        //
        //  Q2: Control the boundary?
        //      Yes    → @NullMarked + @Nullable + NullAway in CI
        //      No     → Adapter wraps at boundary, Optional.ofNullable once
        //
        //  Q3: Java version?
        //      8-11   → Optional, requireNonNull, JSpecify annotations
        //      16+    → Add records with compact constructor validation
        //      17+    → Add sealed classes — no null return type needed
        //      21+    → Add switch null case — language handles it explicitly
        //      Valhalla → Coffee! makes the type system the enforcement layer
        //
        //  One rule that cuts across all three questions:
        //  Pick ONE layer and enforce it.
        //  Annotations without enforcement = documentation.
        //  Enforcement without contract = false confidence.
        //  Contract + enforcement together = the bugs stop at build time."
    }
}