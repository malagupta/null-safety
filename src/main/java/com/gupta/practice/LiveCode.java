package com.gupta.practice;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

// ═══════════════════════════════════════════════════════════════════════════
//
//  THE PAST, PRESENT, AND FUTURE OF NULL SAFETY IN JAVA
//  Live code — structured on Donald Miller's SB7 StoryBrand framework
//
//  SB7 IN ONE LINE:
//  A CHARACTER with a goal meets a PROBLEM,
//  finds a GUIDE who gives them a PLAN,
//  is called to ACTION, avoids FAILURE, and reaches SUCCESS.
//
//  APPLIED HERE:
//  CHARACTER  — the senior developer in this room
//  PROBLEM    — NPE: the symptom everyone knows, the cause nobody talks about
//  GUIDE      — us: empathy (we've been there) + authority (here's what works)
//  PLAN       — three questions, answered in order
//  ACTION     — three things you do on Monday morning
//  FAILURE    — what stays the same if you walk out and change nothing
//  SUCCESS    — what your codebase looks like when the plan is followed
//
// ═══════════════════════════════════════════════════════════════════════════

public class LiveCode {

    // ═════════════════════════════════════════════════════════════════════
    // SB7 BEAT 1 — CHARACTER
    // Who is the hero? What do they want?
    // ═════════════════════════════════════════════════════════════════════
    //
    // The hero is NOT Tony Hoare. Not Java. Not Valhalla.
    // The hero is the developer in this room.
    //
    // What they want: to ship Java that doesn't break in production.
    // What they have: a codebase that uses null in three different ways
    //                 with no way to tell them apart.
    //
    // This is their world — Priya's coffee shop. Completely normal Java.
    // No annotations. No Optional. This is how most production code looks.

    static class Coffee {
        String name;
        String origin;
        String brewingInstructions;   // sometimes null — new items lack instructions

        Coffee(String name, String origin, String brewingInstructions) {
            this.name                = name;
            this.origin              = origin;
            this.brewingInstructions = brewingInstructions;
        }
    }

    static class Order {
        String customerName;
        Coffee coffee;                // sometimes null — customer hasn't chosen yet

        Order(String customerName, Coffee coffee) {
            this.customerName = customerName;
            this.coffee       = coffee;
        }
    }

    // The hero's current state: three nulls, three different meanings,
    // no way to tell them apart.
    static void character() {
        System.out.println("── CHARACTER: The hero's world ──");

        Order jonathan = new Order("Jonathan", null);  // hasn't chosen yet
        Order alice    = new Order("Alice",    new Coffee("Espresso", "Ethiopian", null)); // new item, no instructions
        Order session  = new Order("Session",  null);  // session cleared at shutdown — GC hint

        System.out.println("Jonathan's coffee        : " + jonathan.coffee);       // null — deciding
        System.out.println("Alice's instructions     : " + alice.coffee.brewingInstructions); // null — new item
        System.out.println("Session coffee (closed)  : " + session.coffee);        // null — released

        // Three nulls. Three intents. One value.
        // The language cannot tell them apart.
        // The developer who joins in six months cannot tell them apart.
        // This is the hero's world. Completely normal. About to get complicated.
    }


    // ═════════════════════════════════════════════════════════════════════
    // SB7 BEAT 2 — PROBLEM
    // Three levels: external, internal, philosophical.
    // ═════════════════════════════════════════════════════════════════════
    //
    // EXTERNAL  — the observable crash
    //             NPE is the #1 production exception in Java. Not second. First.
    //             Stack trace says line number. Nothing else.
    //
    // INTERNAL  — the feeling it creates
    //             "I know how to fix NPE. So why does it keep happening?
    //              Am I doing something wrong? Is the team doing something wrong?"
    //             The frustration is not incompetence. It is a language limitation.
    //
    // PHILOSOPHICAL — the belief that should be challenged
    //             "I shouldn't have to null-check everything defensively forever.
    //              Java is 30 years old. This should be solved by now."
    //             It is. The tools exist. Most developers haven't found them yet.

    static String getInstructions(Order order) {
        return order.coffee.brewingInstructions.toUpperCase();
        //           ↑                ↑
        //     null here?        or null here?
        // One line. Two candidates. The stack trace picks one at random (from the
        // developer's perspective). Twenty minutes of debugging to find which.
    }

    static void problem() {
        System.out.println("\n── PROBLEM: External, internal, philosophical ──");

        // EXTERNAL — the crash itself
        Order noCoffee = new Order("Steve", null);
        try {
            getInstructions(noCoffee);
        } catch (NullPointerException e) {
            System.out.println("External : NPE — " + e.getMessage());
            // Pre-Java 14: message is null. You get a line number. That's it.
            // Java 14+: "cannot invoke X because Y is null" — exact field, exact reason
            // The hero has been living with the pre-Java-14 experience by default.
        }

        // INTERNAL — the null check that hides a bug
        // Sprint 1: order without coffee is valid. The null check is correct.
        // Sprint 4: UI enforces coffee selection before checkout. Null should never arrive.
        //           The null check is now wrong — but it still compiles. Still runs.
        //           Silently swallows a data integrity violation for three months.
        Order phantom = new Order("Jonathan", null); // should be impossible after sprint 4
        if (phantom.coffee == null) {
            System.out.println("Internal : null check absorbs a bug — silent. No alert.");
            // This is not defensive programming. It is bug preservation.
        }

        // PHILOSOPHICAL — the signature that says nothing
        // findByName returns Coffee. Or null. The signature doesn't say which.
        // Every caller must either know the convention or discover it the hard way.
        Map<String, Coffee> menu = Map.of(
                "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine."));
        Coffee result = menu.get("coldpresso"); // null — Map.get() contract
        System.out.println("Philosophical: findByName returned — " + result);
        // The hero has written this code. And checked for null. And wondered
        // whether they even needed to. And done it again next sprint. And again.
    }


    // ═════════════════════════════════════════════════════════════════════
    // SB7 BEAT 3 — GUIDE
    // The presenter is not the hero. The presenter is the guide.
    // Two things a guide must show: empathy, then authority.
    // ═════════════════════════════════════════════════════════════════════
    //
    // EMPATHY  — "We've been paged at 2am for this.
    //             We've written the defensive null check that hid a bug for six months.
    //             We've spent 40 minutes tracing a stack trace that said: null."
    //
    // AUTHORITY — "Here is what thirty years of Java evolution actually gave you.
    //              Most of it has been there since Java 8 or Java 9.
    //              You've been paying for it. Let's use it."
    //
    // The guide does NOT say: "null is your fault."
    // The guide DOES say: "null communicates nothing about WHY it's there.
    //                      That's the root. Everything else follows from it."
    //
    // The guide shows the hero the tools they already own.

    // AUTHORITY point 1: JEP 358 — Java 14 — you already have this
    static void guide_JEP358() {
        System.out.println("\n── GUIDE authority 1: JEP 358 helpful NPE (Java 14) ──");

        Order noInstructions = new Order("Harry",
                new Coffee("New Blend", "Rwandan", null));
        try {
            getInstructions(noInstructions);
        } catch (NullPointerException e) {
            System.out.println("Java 14+ : " + e.getMessage());
            // "Cannot invoke String.toUpperCase() because
            //  LiveCode_SB7.Coffee.brewingInstructions is null"
            //
            // The hero has had this since 2020. If they are still spending 40 minutes
            // tracing nulls — they are not reading the exception message.
            // That is the guide's job to say, clearly and without blame.
        }
    }


    // ═════════════════════════════════════════════════════════════════════
    // SB7 BEAT 4 — PLAN
    // A clear, simple plan that reduces anxiety.
    // Not a list of tools. Three questions. Answered in order.
    // The hero knows exactly what to do next.
    // ═════════════════════════════════════════════════════════════════════
    //
    // Q1: Is null a valid domain state — or a bug?
    // Q2: Do you control the boundary that produces null?
    // Q3: What Java version are you running?
    //
    // The plan is demonstrated through code. Each answer leads to a specific tool.

    // ── PLAN Q1: Valid state → model it. Bug → reject it at construction. ──

    static class Coffee_Plan {
        final String name;
        final String origin;
        final String brewingInstructions; // valid null — no requireNonNull

        Coffee_Plan(String name, String origin, String brewingInstructions) {
            this.name   = Objects.requireNonNull(name,   "Coffee needs a name");   // bug if null
            this.origin = Objects.requireNonNull(origin, "Coffee needs an origin"); // bug if null
            this.brewingInstructions = brewingInstructions;                         // valid null
        }
    }

    static class Order_Plan {
        final String       customerName;
        final Coffee_Plan  coffee;       // valid null — customer deciding

        Order_Plan(String customerName, Coffee_Plan coffee) {
            this.customerName = Objects.requireNonNull(customerName, "Order needs a customer");
            this.coffee       = coffee;
        }
    }

    static void plan_Q1() {
        System.out.println("\n── PLAN Q1: valid state or bug? ──");

        // Bug — name cannot be null. requireNonNull fires at construction.
        // Stack trace points HERE. Not three frames away. Not at 2am. HERE.
        try {
            Coffee_Plan bad = new Coffee_Plan(null, "Ethiopian", null);
        } catch (NullPointerException e) {
            System.out.println("Bug caught at construction : " + e.getMessage());
        }

        // Valid state — coffee absent, order still exists
        Order_Plan pending = new Order_Plan("Jonathan", null);
        System.out.println("Valid null accepted       : coffee=" + pending.coffee);
    }

    // ── PLAN Q2: control the boundary → annotate. Don't → wrap once. ──

    // You control this code. The null contract belongs to you.
    // @NullMarked makes non-null the default. @Nullable marks exceptions.
    // NullAway turns violations into compile errors, not production crashes.
    @NullMarked
    record Coffee_Annotated(
            String name,                           // non-null — @NullMarked default
            String origin,                         // non-null — @NullMarked default
            @Nullable String brewingInstructions   // nullable — @Nullable breaks the silence
    ) {
        Coffee_Annotated {
            Objects.requireNonNull(name,   "Coffee needs a name");
            Objects.requireNonNull(origin, "Coffee needs an origin");
        }
    }

    @NullMarked
    record Order_Annotated(
            String customerName,                   // non-null — @NullMarked default
            @Nullable Coffee_Annotated coffee      // nullable — customer deciding
    ) {
        Order_Annotated {
            Objects.requireNonNull(customerName, "Order needs a customer");
        }
    }

    // You don't control this. External API. Returns null. Cannot change it.
    static class LegacyMenu {
        private static final Map<String, Coffee_Plan> store = new HashMap<>();
        static { store.put("espresso", new Coffee_Plan("Espresso", "Ethiopian", "Grind fine.")); }

        static Coffee_Plan find(String name) { return store.get(name); } // null if absent
    }

    // Wrap once at the boundary. Null stays outside. Optional carries it inward.
    static Optional<Coffee_Plan> findSafely(String name) {
        return Optional.ofNullable(LegacyMenu.find(name));
    }

    static void plan_Q2() {
        System.out.println("\n── PLAN Q2: control the boundary? ──");

        // Control: annotations + NullAway enforce at compile time
        Coffee_Annotated espresso = new Coffee_Annotated("Espresso", "Ethiopian", "Grind fine.");
        Order_Annotated  withCoffee = new Order_Annotated("Alice", espresso);
        Order_Annotated  pending    = new Order_Annotated("Jonathan", null); // @Nullable — valid
        System.out.println("Annotated  : " + withCoffee.customerName() + " / " + withCoffee.coffee().name());
        System.out.println("Pending    : " + pending.customerName() + " / coffee=" + pending.coffee());

        // No control: wrap at boundary once, never again
        findSafely("espresso")
                .ifPresentOrElse(
                    c  -> System.out.println("Wrapped    : " + c.name),
                    () -> System.out.println("Wrapped    : not found")
                );
        findSafely("coldpresso")
                .ifPresentOrElse(
                    c  -> System.out.println("Wrapped    : " + c.name),
                    () -> System.out.println("Wrapped    : not found")
                );
    }

    // ── PLAN Q3: Java version → which tool is available ──

    // Java 16+: records — compact constructor, the bad object cannot exist
    record Coffee_Record(String name, String origin, String brewingInstructions) {
        Coffee_Record {
            Objects.requireNonNull(name,   "Coffee needs a name");
            Objects.requireNonNull(origin, "Coffee needs an origin");
        }
    }

    record Order_Record(String customerName, Coffee_Record coffee) {
        Order_Record {
            Objects.requireNonNull(customerName, "Order needs a customer");
        }
        Optional<String> getCoffeeName() {
            return Optional.ofNullable(coffee).map(Coffee_Record::name);
        }
    }

    // Java 17+: sealed classes — null return replaced by an explicit named type
    sealed interface CoffeeResult permits Found, NotFound {}
    record Found(Coffee_Record coffee) implements CoffeeResult {}
    record NotFound(String name)       implements CoffeeResult {}

    static CoffeeResult lookup(String name) {
        Map<String, Coffee_Record> menu = Map.of(
                "espresso", new Coffee_Record("Espresso", "Ethiopian", "Grind fine."),
                "latte",    new Coffee_Record("Latte",    "Colombian", "Shot + milk.")
        );
        Coffee_Record c = menu.get(name);
        return c != null ? new Found(c) : new NotFound(name);
        // No null returned. Ever.
    }

    // Java 21+: switch null case — null as a first-class case, no NPE before switch
    static String describeSize(@Nullable String size) {
        return switch (size) {
            case null    -> "not specified";
            case "small" -> "8oz";
            case "large" -> "16oz";
            default      -> "regular";
        };
    }

    static void plan_Q3() {
        System.out.println("\n── PLAN Q3: Java version? ──");

        // Java 16 — records
        Order_Record pending = new Order_Record("Jonathan", null);
        System.out.println("Record   : " + pending.getCoffeeName().orElse("still deciding"));

        // Java 17 — sealed
        List.of("espresso", "coldpresso").forEach(name -> {
            String msg = switch (lookup(name)) {
                case Found    f -> "Found : " + f.coffee().name();
                case NotFound n -> "Gone  : " + n.name() + " not on menu";
                // No default — compiler verifies every subtype is handled
            };
            System.out.println("Sealed   : " + msg);
        });

        // Java 21 — switch null case
        List.of("small", "large", "null_placeholder").forEach(s -> {
            String size = s.equals("null_placeholder") ? null : s;
            System.out.println("SwitchNull: " + describeSize(size));
        });
    }


    // ═════════════════════════════════════════════════════════════════════
    // SB7 BEAT 5 — CALL TO ACTION
    // Direct and transitional. Both must be present.
    // The hero must know exactly what to do next.
    // ═════════════════════════════════════════════════════════════════════
    //
    // DIRECT CTA — three things, Monday morning, in this order:
    //
    //   1. Add @NullMarked to one package-info.java
    //      One annotation. One file. Every type in the package is now non-null
    //      by default. @Nullable marks the exceptions. Three minutes.
    //
    //   2. Add NullAway to the build with OnlyNullMarked=true
    //      Enforce only annotated packages. Leave legacy code untouched.
    //      Violations become compile errors. Cannot push past a failing build.
    //
    //   3. Migrate one data class to a record
    //      One compact constructor. The bad object cannot exist.
    //      Null fires at construction, not three frames away at runtime.
    //
    // TRANSITIONAL CTA — the blog posts:
    //   → Optional<T> deep dive
    //   → NullAway setup: Maven/Gradle + CI config
    //   → JSpecify migration: package by package
    //   → Project Valhalla: null-restricted types, timeline
    //
    // The direct CTA is low-risk and reversible.
    // @NullMarked on one package hurts nothing. If NullAway finds violations —
    // that is exactly what you wanted. Fix them. Ship safer code.

    static void callToAction() {
        System.out.println("\n── CALL TO ACTION: three things, Monday morning ──");

        // Step 1 is a file — package-info.java:
        //
        //   @NullMarked
        //   package com.gupta.session;
        //   import org.jspecify.annotations.NullMarked;
        //
        // Step 2 is two lines in pom.xml:
        //
        //   <arg>-XepOpt:NullAway:AnnotatedPackages=com.gupta.session</arg>
        //   <arg>-XepOpt:NullAway:OnlyNullMarked=true</arg>

        // Step 3 — one class becomes one record:
        // BEFORE:
        //   class Coffee { String name; String origin; ... constructor + getters }
        // AFTER:
        record Coffee_CTA(String name, String origin, String brewingInstructions) {
            Coffee_CTA {
                Objects.requireNonNull(name,   "Coffee needs a name");
                Objects.requireNonNull(origin, "Coffee needs an origin");
            }
        }

        Coffee_CTA c = new Coffee_CTA("Espresso", "Ethiopian", "Grind fine.");
        System.out.println("CTA Step 3 : migrated to record — " + c.name() + " from " + c.origin());
        System.out.println("CTA Step 3 : null name fires at construction, not at the service layer");
    }


    // ═════════════════════════════════════════════════════════════════════
    // SB7 BEAT 6 — FAILURE
    // What stays the same if the hero walks out and changes nothing.
    // Stakes must be real. Not catastrophic — specific.
    // ═════════════════════════════════════════════════════════════════════
    //
    // FAILURE 1 — the null that travels
    //   No requireNonNull. No annotation. A null name on Coffee enters the system.
    //   It gets stored. It reaches a service. It reaches a controller.
    //   NPE fires three layers away. The stack trace points at the dereference.
    //   You spend 40 minutes tracing back to where null entered.
    //   The fix is one line. The cost was 40 minutes.

    static void failure_NullTravels() {
        System.out.println("\n── FAILURE 1: null travels ──");

        Coffee bad = new Coffee(null, "Ethiopian", null); // no validation — accepted
        Order  order = new Order("Alice", bad);

        // Three method calls later, in a completely different class:
        try {
            String upper = order.coffee.name.toUpperCase(); // 💥 NPE here
        } catch (NullPointerException e) {
            System.out.println("NPE at dereference : " + e.getMessage());
            System.out.println("Null entered       : at new Coffee(null, ...)");
            System.out.println("Gap                : 3 method calls, could be 30");
        }
    }

    // FAILURE 2 — the null check that hid a bug
    //   Sprint 1: null coffee is valid. Business said so.
    //   Sprint 4: UI enforces selection. Null should be impossible now.
    //             Null check is now wrong. Still compiles. Absorbs a data violation.
    //             Discovered in a database audit three months later.

    static void failure_HiddenBug() {
        System.out.println("\n── FAILURE 2: null check hides a bug ──");

        // Sprint 4: coffee should never be null at this point in the flow
        Order badOrder = new Order("Jonathan", null); // should not exist post-sprint-4

        if (badOrder.coffee == null) {
            System.out.println("Silently skipped — data integrity violation absorbed");
            // A NullPointerException here would have been BETTER.
            // It would have fired immediately and told the team the upstream contract broke.
            // Instead — silence. The bug ships. The data corrupts. Audited in three months.
        }
    }

    // FAILURE 3 — annotations without enforcement
    //   @NullMarked without NullAway is a comment in annotation syntax.
    //   It documents the intent. It enforces nothing at compile time.
    //   The developer reads the annotation, feels safe, passes null anyway.

    static void failure_AnnotationsWithoutEnforcement() {
        System.out.println("\n── FAILURE 3: annotations without enforcement ──");

        // @NullMarked is on the class — but NullAway is not configured
        // This compiles and runs with zero warnings:
        Coffee_Annotated annotatedButUnprotected = new Coffee_Annotated(
                "name",
                "origin",
                null  // @Nullable — fine
        );
        System.out.println("@NullMarked alone  : annotations present, NPE still possible at runtime");
        System.out.println("@NullMarked + NullAway: passing null to non-null param = compile error");
        System.out.println("Rule: annotations without enforcement = documentation");
    }


    // ═════════════════════════════════════════════════════════════════════
    // SB7 BEAT 7 — SUCCESS
    // What the hero's world looks like when the plan is followed.
    // Specific. Tangible. Earned.
    // ═════════════════════════════════════════════════════════════════════
    //
    // SUCCESS 1 — the bad object cannot exist
    //   requireNonNull in the compact constructor.
    //   Null fires at construction. Stack trace points at the caller. Not downstream.
    //
    // SUCCESS 2 — the signature tells the truth
    //   @Nullable on the return type. Optional<T> as the return type.
    //   The caller knows without reading the implementation.
    //   NullAway enforces the claim at compile time.
    //
    // SUCCESS 3 — violations fail the build, not production
    //   NullAway in CI. Null contract broken = build fails.
    //   Cannot push past a failing build. The bug never reaches a customer.
    //
    // SUCCESS 4 — sealed types replace null returns
    //   NotFound is not null. It is a first-class state with a name.
    //   Every case is handled. Compiler verifies it. No forgotten null check.

    @NullMarked
    record Coffee_Success(String name, String origin, @Nullable String brewingInstructions) {
        Coffee_Success {
            Objects.requireNonNull(name,   "Coffee needs a name");
            Objects.requireNonNull(origin, "Coffee needs an origin");
        }
    }

    @NullMarked
    record Order_Success(String customerName, @Nullable Coffee_Success coffee) {
        Order_Success {
            Objects.requireNonNull(customerName, "Order needs a customer");
        }
        Optional<String> getCoffeeName() {
            return Optional.ofNullable(coffee).map(Coffee_Success::name);
        }
    }

    static CoffeeResult lookupSuccess(String name) {
        Map<String, Coffee_Record> menu = Map.of(
                "espresso", new Coffee_Record("Espresso", "Ethiopian", "Grind fine."),
                "latte",    new Coffee_Record("Latte",    "Colombian", "Shot + milk.")
        );
        Coffee_Record c = menu.get(name);
        return c != null ? new Found(c) : new NotFound(name);
    }

    static void success() {
        System.out.println("\n── SUCCESS: what the codebase looks like after ──");

        // SUCCESS 1 — bad object cannot exist
        try {
            Coffee_Success bad = new Coffee_Success(null, "Ethiopian", null);
        } catch (NullPointerException e) {
            System.out.println("Bad object : rejected at construction — " + e.getMessage());
        }

        // SUCCESS 2 — signature tells the truth
        Order_Success pending = new Order_Success("Jonathan", null);
        String coffeeName = pending.getCoffeeName().orElse("still deciding");
        System.out.println("Signature  : Optional return — caller handles both paths");
        System.out.println("Jonathan   : " + coffeeName);

        // SUCCESS 3 — violations fail the build (shown as a comment — NullAway is a build tool)
        // With NullAway configured, this line is a COMPILE ERROR:
        //   new Coffee_Success(null, "Ethiopian", null);
        //                      ↑ [NullAway] passing @Nullable where @NonNull required
        System.out.println("Build      : NullAway rejects null for non-null params at compile time");
        System.out.println("           : violations never reach production");

        // SUCCESS 4 — sealed types, no null returns
        List.of("espresso", "coldpresso").forEach(name -> {
            String msg = switch (lookupSuccess(name)) {
                case Found    f -> "Serving : " + f.coffee().name();
                case NotFound n -> "Missing : " + n.name() + " — suggest alternatives";
            };
            System.out.println("Sealed     : " + msg);
        });

        // The Valhalla horizon — when it ships:
        // record Coffee(String! name, String! origin, String brewingInstructions) {}
        //                      ↑              ↑
        //               JVM-enforced non-null. No annotation. No requireNonNull.
        //               The type system says no. That's the final success state.
        System.out.println("\nValhalla   : String! — JVM enforced. No annotation. No plugin. Final answer.");
    }


    // ═════════════════════════════════════════════════════════════════════
    // SB7 — ONE SENTENCE
    // The complete story arc in a single statement.
    // Read this before presenting. It keeps you oriented.
    // ═════════════════════════════════════════════════════════════════════
    //
    // "A senior Java developer [CHARACTER] wants to ship code that doesn't
    //  break in production, but NPE — the most common production exception
    //  in Java — keeps finding its way through [PROBLEM]. We [GUIDE] have
    //  been there, and Java has given you three questions [PLAN] that tell
    //  you exactly which tool to use. Add @NullMarked to one package today
    //  [ACTION]. Without it: silent bugs, 2am pages, broken customer trust
    //  [FAILURE]. With it: violations fail the build, the bad object cannot
    //  exist, and the type system tells the truth [SUCCESS]."


    public static void main(String[] args) {
        character();
        problem();
        guide_JEP358();
        plan_Q1();
        plan_Q2();
        plan_Q3();
        callToAction();
        failure_NullTravels();
        failure_HiddenBug();
        failure_AnnotationsWithoutEnforcement();
        success();

        System.out.println("\n" + "─".repeat(68));
        System.out.println("SB7 in one sentence:");
        System.out.println("  Character — the senior developer who wants to stop debugging NPE");
        System.out.println("  Problem   — null communicates nothing about why it's there");
        System.out.println("  Guide     — we've been there + Java has the tools");
        System.out.println("  Plan      — three questions: valid state? boundary? Java version?");
        System.out.println("  Action    — @NullMarked + NullAway + one record, Monday morning");
        System.out.println("  Failure   — null travels, checks hide bugs, annotations do nothing");
        System.out.println("  Success   — bad object can't exist, build catches it, type tells truth");
        System.out.println("─".repeat(68));
    }
}
