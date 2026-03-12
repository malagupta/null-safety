package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DEMO 7 — MODERN JAVA NULL SAFETY: JAVA 9 TO JAVA 26
 * The features most developers on Java 21 are still not using
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * OPENING SCRIPT:
 *
 * "I want to start with a number. 358.
 *
 *  That's a JEP number. JEP 358. It shipped in Java 14.
 *  Raise your hand if you know what it does.
 *
 *  [wait]
 *
 *  Most hands are down. That's what I expected.
 *  And it's the most underrated null safety improvement Java has ever shipped.
 *  I'm going to show you JEP 358 first — because once you see it,
 *  you'll understand why everything else in this demo matters.
 *
 *  Then I'm going to show you six more features across Java 9 to 22
 *  that directly address null — that most developers on Java 21
 *  are still not using.
 *
 *  You already paid for these. Let's use them."
 */
@NullMarked
public class Demo7_ModernJava9to26 {

    record Coffee(String name, String origin, @Nullable String brewingInstructions) {
        Coffee {
            Objects.requireNonNull(name,   "Coffee needs a name");
            Objects.requireNonNull(origin, "Coffee needs an origin");
        }
    }

    record Order(String customerName, @Nullable Coffee coffee) {
        Order { Objects.requireNonNull(customerName, "Order needs a customer"); }
    }

    // ═════════════════════════════════════════════════════════════════════
    // FEATURE 1 — JEP 358: Helpful NullPointerExceptions (Java 14)
    // "The debugger you always wanted, built into the JVM"
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "Here is the scenario every Java developer in this room has lived.
    //  It is 11pm. The build just failed in staging.
    //  The stack trace says: NullPointerException at OrderService.java:47.
    //
    //  You open line 47. You see this:"

    static class OrderService {
        static String getBrewingInstructions(Order order) {
            return order.coffee().brewingInstructions().toUpperCase();
            //           ↑              ↑                    ↑
            //      null here?     null here?           null here?
            // Three candidates. One line. Zero information from the stack trace.
            // Pre-Java-14: you're guessing.
        }
    }

    static void demo_HelpfulNPE() {
        System.out.println("══ Feature 1: JEP 358 — Helpful NPE Messages (Java 14) ══\n");

        // SCRIPT:
        // "Pre-Java-14, this stack trace said:
        //  NullPointerException at OrderService.getBrewingInstructions(OrderService.java:47)
        //
        //  That's it. Three possible nulls. One line number.
        //  You add logging. You add a debugger. You reproduce locally.
        //  You find out 20 minutes later it was the coffee. Not the instructions.
        //
        //  Java 14 changed this. The JVM now analyses the bytecode
        //  and tells you PRECISELY which variable was null.
        //  Let me show you the difference."

        // Null 1: coffee is null
        Order noCoffee = new Order("Steve", null);
        try {
            OrderService.getBrewingInstructions(noCoffee);
        } catch (NullPointerException e) {
            System.out.println("Null 1 — coffee is null:");
            System.out.println("  " + e.getMessage());
            // Java 14+: "Cannot invoke \"com.gupta.session.Coffee.brewingInstructions()\""
            //           "because the return value of \"Order.coffee()\" is null"
            //
            // SCRIPT:
            // "Read that. 'because the return value of Order.coffee() is null.'
            //  Exact method. Exact reason. Zero ambiguity.
            //  The 20-minute debugging session just became a 20-second read."
        }

        System.out.println();

        // Null 2: brewingInstructions is null — same line, different null, different message
        Order noInstructions = new Order("Alice",
                new Coffee("New Blend", "Rwandan", null));
        try {
            OrderService.getBrewingInstructions(noInstructions);
        } catch (NullPointerException e) {
            System.out.println("Null 2 — brewingInstructions is null:");
            System.out.println("  " + e.getMessage());
            // Java 14+: "Cannot invoke \"String.toUpperCase()\""
            //           "because the return value of \"Coffee.brewingInstructions()\" is null"
            //
            // SCRIPT:
            // "Same line. Different null. Different message.
            //  The JVM distinguished them without any extra code from you.
            //  No logging. No debugger. No reproducing locally.
            //
            //  JEP 358 doesn't prevent NPE. It makes the one you get
            //  actually useful. That's a different kind of null safety —
            //  and it's been shipping since Java 14 on every JVM by default.
            //
            //  If you're on Java 14+ and still spending 20 minutes tracing nulls —
            //  you're not reading the exception message closely enough."
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // FEATURE 2 — Null-hostile collections: List.of(), Map.of() (Java 9)
    // "Collections that fail fast. At the boundary. Loudly."
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "Java 9 shipped List.of(), Map.of(), Set.of().
    //  Most developers know these as 'immutable collections.'
    //  What they don't realise is that these collections are
    //  DELIBERATELY null-hostile.
    //
    //  Not null-tolerant. Not null-ignoring. Null-HOSTILE.
    //  Pass a null element — NPE fires immediately at the call site.
    //  Not when you iterate. Not when you get. Right there.
    //
    //  That's the fail-fast principle built into the collection factory."

    static void demo_NullHostileCollections() {
        System.out.println("\n══ Feature 2: Null-Hostile Collections (Java 9) ══\n");

        // ── Old style: ArrayList — accepts null silently ──────────────────
        List<Coffee> mutableMenu = new ArrayList<>();
        mutableMenu.add(new Coffee("Espresso", "Ethiopian", "Grind fine."));
        mutableMenu.add(null);   // ← accepted silently — null travels into the list
        mutableMenu.add(new Coffee("Latte", "Colombian", "Shot + milk."));

        System.out.println("ArrayList null: accepted silently — null is element index 1");

        // The NPE is deferred — fires wherever you first dereference element 1
        try {
            mutableMenu.forEach(c -> System.out.println(c.name())); // 💥 NPE at index 1
        } catch (NullPointerException e) {
            System.out.println("ArrayList NPE: deferred — fires at forEach, not at add()");
        }

        System.out.println();

        // ── Modern: List.of() — null-hostile, fails at boundary ──────────
        try {
            List<Coffee> menu = List.of(
                    new Coffee("Espresso", "Ethiopian", "Grind fine."),
                    null,  // ← NPE fires HERE — at construction, not at use
                    new Coffee("Latte", "Colombian", "Shot + milk.")
            );
        } catch (NullPointerException e) {
            System.out.println("List.of() NPE : immediate — fires at List.of(), not at forEach");
            System.out.println("              " + e.getMessage());
        }

        // SCRIPT:
        // "Two lists. Same null. Completely different behaviour.
        //
        //  ArrayList: null enters silently at add(). NPE fires later — at forEach,
        //  at sort, at stream, at serialisation — wherever you first dereference it.
        //  The gap between cause and crash can be enormous.
        //
        //  List.of(): null is rejected at the factory call. NPE fires immediately.
        //  The stack trace points directly at whoever passed null.
        //  The gap between cause and crash is zero.
        //
        //  This is the fail-fast principle. Not a new concept.
        //  But Java 9 finally gave us collections that enforce it by default.
        //
        //  One migration: everywhere you have 'new ArrayList<>()' for a list
        //  that shouldn't change — replace it with List.of() or List.copyOf().
        //  Every null that slips in will be caught immediately."

        // ✅ The valid case — no null, no NPE, immutable menu
        List<Coffee> safeMenu = List.of(
                new Coffee("Espresso", "Ethiopian", "Grind fine."),
                new Coffee("Latte",    "Colombian", "Shot + milk."),
                new Coffee("Cold Brew","Rwandan",   "Steep 18h cold.")
        );
        System.out.println("\nList.of() valid: " + safeMenu.size() + " items, null impossible");

        // Map.of() — same contract
        try {
            Map<String, Coffee> menuMap = Map.of(
                    "espresso", new Coffee("Espresso", "Ethiopian", null),
                    "latte",    null  // ← NPE at Map.of() — not at get()
            );
        } catch (NullPointerException e) {
            System.out.println("Map.of() NPE : immediate — null value rejected at boundary");
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    // FEATURE 3 — Optional upgrades: or(), ifPresentOrElse(), stream() (Java 9)
    // "The three Optional methods most developers don't know exist"
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "Most developers who use Optional know three methods:
    //  map(), orElse(), and isPresent(). That's it.
    //  Java 9 shipped three more that are genuinely useful
    //  for null safety — and almost nobody uses them.
    //
    //  I've reviewed hundreds of pull requests. I have never once
    //  seen Optional.or() used correctly in production code.
    //  Let me show you what you're missing."

    static Optional<Coffee> findInPrimaryMenu(String name) {
        Map<String, Coffee> primary = Map.of(
                "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine.")
        );
        return Optional.ofNullable(primary.get(name));
    }

    static Optional<Coffee> findInBackupMenu(String name) {
        Map<String, Coffee> backup = Map.of(
                "latte", new Coffee("Latte", "Colombian", "Shot + milk.")
        );
        return Optional.ofNullable(backup.get(name));
    }

    static Optional<Coffee> findInSeasonalMenu(String name) {
        return Optional.empty(); // seasonal menu is empty today
    }

    static void demo_OptionalUpgrades() {
        System.out.println("\n══ Feature 3: Optional.or(), ifPresentOrElse(), stream() (Java 9) ══\n");

        // ── Optional.or() — chain optionals, first non-empty wins ────────
        //
        // SCRIPT:
        // "or() — if this Optional is empty, try another one.
        //  Lazy — the supplier is only called if the first is empty.
        //  Chains cleanly. No nested flatMap. No if-else ladder."

        // Old style — nested, verbose
        Optional<Coffee> found_old;
        Optional<Coffee> primary = findInPrimaryMenu("latte");
        if (primary.isPresent()) {
            found_old = primary;
        } else {
            found_old = findInBackupMenu("latte");
        }
        System.out.println("or() old  : " + found_old.map(Coffee::name).orElse("not found"));

        // Modern: Optional.or() — chain three sources, first non-empty wins
        Optional<Coffee> found_new = findInPrimaryMenu("latte")      // empty
                .or(() -> findInBackupMenu("latte"))                  // present — stops here
                .or(() -> findInSeasonalMenu("latte"));               // not reached

        System.out.println("or() new  : " + found_new.map(Coffee::name).orElse("not found"));

        // SCRIPT:
        // "Three sources. One expression. Lazy evaluation — backup is only consulted
        //  if primary is empty. Seasonal only if backup is also empty.
        //  This replaces an if-else ladder that most codebases have
        //  somewhere in their lookup logic."

        System.out.println();

        // ── Optional.ifPresentOrElse() — handle BOTH cases in one call ───
        //
        // SCRIPT:
        // "ifPresent() is useful but incomplete. It handles the present case.
        //  What about the empty case? You write another else block.
        //  Java 9 gives you ifPresentOrElse — both cases, one call."

        // Old style — two separate statements
        Optional<Coffee> espresso = findInPrimaryMenu("espresso");
        if (espresso.isPresent()) {
            System.out.println("old present : serving " + espresso.get().name());
        } else {
            System.out.println("old empty   : espresso not found");
        }

        // Modern: ifPresentOrElse — both cases together
        findInPrimaryMenu("espresso")
                .ifPresentOrElse(
                        c  -> System.out.println("new present : serving " + c.name()),
                        () -> System.out.println("new empty   : espresso not found")
                );

        findInPrimaryMenu("coldpresso")
                .ifPresentOrElse(
                        c  -> System.out.println("new present : serving " + c.name()),
                        () -> System.out.println("new empty   : coldpresso not on menu")
                );

        System.out.println();

        // ── Optional.stream() — bridge Optional to Stream API ────────────
        //
        // SCRIPT:
        // "The third one. Optional.stream(). One method. Eliminates an entire
        //  class of null bug in stream pipelines.
        //
        //  Classic scenario: you have a list of order IDs.
        //  You look up each one. Some are found, some aren't.
        //  How do you collect only the ones that exist, without null checks?"

        List<String> names = List.of("espresso", "coldpresso", "latte", "mysteryblend");

        // Old style — filter(Objects::nonNull) — null travels into the stream
        List<Coffee> found_stream_old = names.stream()
                .map(name -> findInPrimaryMenu(name).orElse(null)) // null enters stream
                .filter(Objects::nonNull)                          // filtered out later
                .collect(Collectors.toList());
        System.out.println("stream old : " + found_stream_old.size() + " found (null entered stream)");

        // Modern: Optional.stream() — null never enters the stream
        List<Coffee> found_stream_new = names.stream()
                .map(Demo7_ModernJava9to26::findInPrimaryMenu) // Stream<Optional<Coffee>>
                .flatMap(Optional::stream)                      // Stream<Coffee> — empties removed
                .toList();                                       // Java 16: Stream.toList()
        System.out.println("stream new : " + found_stream_new.size() + " found (null never entered)");

        // SCRIPT:
        // "Optional.stream() returns a stream of zero or one elements.
        //  flatMap flattens them. Empty optionals contribute nothing.
        //  Present optionals contribute their value.
        //  Null never enters the stream. No filter needed. No Objects::nonNull.
        //  The pipeline is clean by construction."
    }

    // ═════════════════════════════════════════════════════════════════════
    // FEATURE 4 — Null-safe Map operations (Java 8, universally missed)
    // "The four Map methods that make Map.get() + null check obsolete"
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "These are technically Java 8. I include them because in six years
    //  of code reviews I have seen them used correctly approximately never.
    //  Every codebase I've worked in has Map.get() followed by a null check
    //  in fifty different places — when these four methods would have
    //  handled it in one line each."

    static void demo_NullSafeMapOperations() {
        System.out.println("\n══ Feature 4: Null-Safe Map Operations ══\n");

        Map<String, Integer> orderCount = new HashMap<>();
        orderCount.put("espresso", 47);
        orderCount.put("latte",    23);

        // ── getOrDefault — eliminate the get() + null check ──────────────
        // Old
        Integer count_old = orderCount.get("coldpresso");
        int result_old = (count_old != null) ? count_old : 0;
        System.out.println("getOrDefault old : " + result_old);

        // Modern
        int result_new = orderCount.getOrDefault("coldpresso", 0);
        System.out.println("getOrDefault new : " + result_new);

        System.out.println();

        // ── computeIfAbsent — eliminate the get() + null check + put() ───
        //
        // SCRIPT:
        // "computeIfAbsent. The one that eliminates the most boilerplate.
        //  Classic pattern: check if key exists, if not, create and put.
        //  Five lines become one."

        Map<String, List<String>> ordersByStation = new HashMap<>();

        // Old — five lines, two null checks, verbose
        List<String> station1_old = ordersByStation.get("counter-1");
        if (station1_old == null) {
            station1_old = new ArrayList<>();
            ordersByStation.put("counter-1", station1_old);
        }
        station1_old.add("Jonathan's Espresso");
        System.out.println("computeIfAbsent old : " + ordersByStation.get("counter-1"));

        // Modern — one line, zero null checks
        ordersByStation.computeIfAbsent("counter-2", k -> new ArrayList<>())
                       .add("Alice's Latte");
        System.out.println("computeIfAbsent new : " + ordersByStation.get("counter-2"));

        System.out.println();

        // ── merge — update or initialise in one null-safe call ────────────
        //
        // SCRIPT:
        // "merge is the one that handles both cases at once:
        //  if the key doesn't exist — initialise it.
        //  if it does exist — apply a combining function.
        //  One method. Zero null checks. Every counter pattern you've ever written."

        Map<String, Integer> coffeeCounters = new HashMap<>();

        List<String> orders = List.of("espresso", "latte", "espresso",
                                      "cold brew", "espresso", "latte");
        // Old — get + null check + put — written in every codebase
        for (String name : orders) {
            Integer current = coffeeCounters.get(name);
            coffeeCounters.put(name, current == null ? 1 : current + 1);
        }
        System.out.println("merge old : " + coffeeCounters);

        // Modern — merge handles absent and present in one call
        Map<String, Integer> coffeeCounters2 = new HashMap<>();
        orders.forEach(name ->
                coffeeCounters2.merge(name, 1, Integer::sum));
        System.out.println("merge new : " + coffeeCounters2);

        // SCRIPT:
        // "Same result. The old version has a null check embedded in a ternary
        //  that every developer reads slightly differently.
        //  The new version has an intent: merge this value using addition.
        //  No null in sight."
    }

    // ═════════════════════════════════════════════════════════════════════
    // FEATURE 5 — Unnamed variables _ (Java 22)
    // "The null you caught but don't need to name"
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "Java 22. Unnamed variables. The underscore.
    //  Small feature. Direct null safety benefit.
    //  Two places where this matters for null handling."

    static void demo_UnnamedVariables() {
        System.out.println("\n══ Feature 5: Unnamed Variables _ (Java 22) ══\n");

        // ── Catch block — catching NPE without naming the variable ────────
        //
        // SCRIPT:
        // "First use — catch blocks. You're catching an exception
        //  not to inspect it, but to handle the null case.
        //  The variable name 'e' or 'npe' is noise you never use.
        //  Java 22 lets you discard it explicitly."

        Order order = new Order("Bob", null);

        // Old — named variable you never use
        try {
            String name = order.coffee().name(); // 💥 NPE
        } catch (NullPointerException e) {       // e is declared, never used
            System.out.println("old catch : no coffee on Bob's order (e unused)");
        }

        // Modern — _ makes the intent explicit: I caught it, I don't need it
        try {
            String name = order.coffee().name();
        } catch (NullPointerException _) {       // _ = I know, I'm handling it, not inspecting it
            System.out.println("new catch : no coffee on Bob's order (_ = intentionally ignored)");
        }

        System.out.println();

        // ── Pattern matching — discard components you don't need ──────────
        //
        // SCRIPT:
        // "Second use — pattern matching. When you match a record pattern
        //  but only need some of its components, _ discards the ones you don't.
        //  This becomes significant when records have nullable fields —
        //  you can match on presence without binding a variable you'd null-check."

        sealed interface DrinkOrder permits DrinkOrder.Hot, DrinkOrder.Cold {}
        record Hot(Coffee coffee, int tempC, @Nullable String milkType) implements DrinkOrder {}
        record Cold(Coffee coffee, int iceLevel) implements DrinkOrder {}

        List<DrinkOrder> drinks = List.of(
                new Hot(new Coffee("Espresso", "Ethiopian", null), 90, null),
                new Hot(new Coffee("Latte",    "Colombian", null), 65, "oat"),
                new Cold(new Coffee("Cold Brew","Rwandan",  null), 3)
        );

        drinks.forEach(drink -> {
            // _ discards tempC — we only care about the coffee name and milk
            // No null check needed on _ — it's not bound
            String desc = switch (drink) {
                case Hot(var coffee, _, var milk) when milk != null
                        -> coffee.name() + " with " + milk + " milk";
                case Hot(var coffee, _, _)
                        -> coffee.name() + " black";
                case Cold(var coffee, var ice)
                        -> coffee.name() + " iced (level " + ice + ")";
            };
            System.out.println("_ pattern : " + desc);
        });

        // SCRIPT:
        // "The _ in the Hot pattern discards tempC entirely.
        //  The pattern still matches. The field is still there.
        //  We just told the compiler — and more importantly, the reader —
        //  'I know this field exists and I don't need it here.'
        //  That's not laziness. That's intent. And intent in code is null safety."
    }

    // ═════════════════════════════════════════════════════════════════════
    // FEATURE 6 — Primitive patterns in switch (Java 23 preview)
    // "The Integer unboxing NPE finally has a language-level fix"
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "Last one. And I show it because it fixes the NPE from Demo 2
    //  that most developers don't even recognise as a null bug.
    //
    //  The unboxing NPE. int temp = order.getTemperature() when temperatureC is null.
    //  No method call. Just an assignment. And it throws NPE.
    //
    //  Java 23 introduced primitive type patterns in switch — preview.
    //  For the first time, you can switch on int AND Integer in the same expression,
    //  handling null explicitly as a case."

    static void demo_PrimitivePatterns() {
        System.out.println("\n══ Feature 6: Primitive Patterns in Switch (Java 23 preview) ══\n");

        // The old unboxing trap
        Integer temp = null; // comes from config, DB, external API

        // ❌ Old — NPE on unboxing
        try {
            int t = temp; // NPE — temp.intValue() called on null
            System.out.println("temp: " + t);
        } catch (NullPointerException e) {
            System.out.println("unbox NPE : int t = null Integer — " + e.getMessage());
        }

        // ❌ Old comparison trap
        try {
            if (temp == 90) { // NPE — unboxes Integer to int before comparing
                System.out.println("perfect temp");
            }
        } catch (NullPointerException e) {
            System.out.println("compare NPE: temp == 90 unboxes null — " + e.getMessage());
        }

        System.out.println();

        // ✅ Current best practice — requireNonNullElse
        int safeTemp = Objects.requireNonNullElse(temp, 90);
        System.out.println("requireNonNullElse : " + safeTemp + "°C (default applied)");

        // ✅ Current — explicit null check before comparison
        boolean isPerfect = temp != null && temp == 90;
        System.out.println("null check first   : isPerfect = " + isPerfect);

        // [JAVA 23 PREVIEW — illustrative, not compilable without --enable-preview]
        //
        // SCRIPT:
        // "With primitive patterns in switch — Java 23 preview — you write:
        //
        //   String describe = switch (temp) {
        //       case null    -> 'Temperature not set — using default';
        //       case int t when t < 60  -> 'Too cold for espresso';
        //       case int t when t <= 70 -> 'Latte temperature — ' + t + '°C';
        //       case int t when t <= 95 -> 'Espresso range — ' + t + '°C';
        //       case int t              -> 'Too hot — ' + t + '°C';
        //   };
        //
        //  null is a first-class case. The int pattern handles the non-null value.
        //  The unboxing happens safely inside the matched case — not before.
        //  The NPE from 'int t = nullInteger' is impossible here.
        //  The language handles the unboxing only when null has been ruled out."

        System.out.println("\n[Java 23 preview — primitive switch + null case]");
        System.out.println("case null    -> 'Temperature not set — using default'");
        System.out.println("case int t   -> matched safely — null already handled above");
        System.out.println("Unboxing NPE : impossible — null handled before int pattern fires");
    }

    // ═════════════════════════════════════════════════════════════════════
    // THE FULL TIMELINE — one slide, every feature, Java 9 to 26
    // ═════════════════════════════════════════════════════════════════════

    static void printTimeline() {
        System.out.println("\n══ Java 9→26 Null Safety Timeline ══\n");
        System.out.println("Java  9  (2017) Objects.requireNonNullElse/ElseGet");
        System.out.println("             Optional.or() / ifPresentOrElse() / stream()");
        System.out.println("             List.of() / Map.of() / Set.of() — null-hostile");
        System.out.println();
        System.out.println("Java 10  (2018) List.copyOf() / Map.copyOf() — null-hostile");
        System.out.println("             var — makes null more visible in type inference");
        System.out.println();
        System.out.println("Java 14  (2020) JEP 358: Helpful NPE messages ⭐");
        System.out.println("             'Cannot invoke X because Y is null' — finally");
        System.out.println();
        System.out.println("Java 16  (2021) Records — null stopped at construction");
        System.out.println("             Pattern matching instanceof — null-safe by spec");
        System.out.println("             Stream.toList() — immutable, null-hostile");
        System.out.println();
        System.out.println("Java 17  (2021) Sealed classes — null return replaced by type");
        System.out.println();
        System.out.println("Java 21  (2023) Pattern matching switch — stable");
        System.out.println("             null case in switch — explicit, no silent NPE");
        System.out.println("             Unnamed patterns _ — discard without binding");
        System.out.println();
        System.out.println("Java 22  (2024) Unnamed variables _ stable — catch (NPE _)");
        System.out.println("             Flexible constructors — validate before super()");
        System.out.println();
        System.out.println("Java 23  (2024) Primitive patterns in switch (preview)");
        System.out.println("             Integer unboxing NPE handled in switch case");
        System.out.println();
        System.out.println("Java 24  (2025) JEP 488: Primitive types in patterns stable");
        System.out.println();
        System.out.println("Java 26+ (next) Project Valhalla — String! null-restricted types");
        System.out.println("             JVM-enforced non-null. No annotation. No tool.");
        System.out.println("             The type system says no. Final answer.");
    }

    public static void main(String[] args) {
        demo_HelpfulNPE();
        demo_NullHostileCollections();
        demo_OptionalUpgrades();
        demo_NullSafeMapOperations();
        demo_UnnamedVariables();
        demo_PrimitivePatterns();
        printTimeline();

        System.out.println();
        System.out.println("══ Closing Script ══");
        System.out.println();

        // SCRIPT — deliver without notes:
        //
        // "I started this demo with JEP 358 — the most underrated null
        //  safety improvement Java has ever shipped — and a show of hands
        //  that told me most of you had never heard of it.
        //
        //  I want to end with the same theme.
        //
        //  Java has been shipping null safety improvements every release
        //  since Java 9. Quietly. Without a marketing campaign.
        //  Without a major version bump.
        //
        //  List.of() has been null-hostile since 2017.
        //  Optional.or() has been available since 2017.
        //  Map.computeIfAbsent() has been there since Java 8.
        //  Stream.flatMap(Optional::stream) has worked since Java 9.
        //  And JEP 358 has been telling you exactly which variable was null
        //  since 2020.
        //
        //  The language kept its side of the deal.
        //
        //  The question I'd leave you with — and I mean this genuinely,
        //  not rhetorically — is this:
        //
        //  When was the last time you read the release notes?
        //
        //  Not the headline features. The small ones.
        //  The ones that don't get conference talks.
        //  The ones that are just sitting there in the API,
        //  waiting for you to replace the Map.get() + null check
        //  that you've been copy-pasting since 2012.
        //
        //  The tools are there. Have been there for years.
        //  The only thing left is the habit of reaching for them."
    }
}