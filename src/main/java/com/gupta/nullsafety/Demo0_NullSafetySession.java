package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *   THE PAST, PRESENT, AND FUTURE OF NULL SAFETY IN JAVA
 *   A 40-minute session for senior Java developers
 *
 *   Presenters: Chandra / Mala
 *
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *  SESSION MAP                                              CUMULATIVE TIME
 *  ─────────────────────────────────────────────────────────────────────────
 *  Opening          — Priya's coffee shop. The show of hands.      0:00– 3:00
 *  Part 1           — Why null exists. Three legitimate uses.       3:00– 8:00
 *  Part 2           — Why it breaks at scale. Three failure modes.  8:00–16:00
 *  Part 3           — What was tried. Four eras. Honest verdict.   16:00–23:00
 *  Part 4           — What modern Java gives you. Use it now.      23:00–34:00
 *  Part 5           — How to pick your solution. Three questions.  34:00–39:00
 *  Close            — Valhalla. Monday morning. One line.          39:00–40:00
 *  ─────────────────────────────────────────────────────────────────────────
 *
 *  BLOG POSTS — topics that deserve depth beyond 40 minutes:
 *    → Optional<T> deep dive: map, flatMap, or, stream, anti-patterns
 *    → NullAway setup guide: Maven/Gradle config, OnlyNullMarked, CI integration
 *    → JSpecify migration guide: package-info.java, @Nullable scope, generics
 *    → Project Valhalla: null-restricted types, String!, Integer!, timeline
 *    → Null-safe Map operations: computeIfAbsent, merge, getOrDefault at scale
 *    → JPA lazy loading and null: the Hibernate trap senior devs know
 *
 * ═══════════════════════════════════════════════════════════════════════════
 */
@NullMarked
public class Demo0_NullSafetySession {

    // ── Domain — Priya's coffee shop ─────────────────────────────────────
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
    // OPENING                                                   [0:00–3:00]
    // ═════════════════════════════════════════════════════════════════════

    /*
     * [Walk to the front. Pick up an empty coffee cup. Say nothing for three
     *  seconds. Let the room notice the cup. Then:]
     *
     * CHANDRA:
     * "This is Priya's coffee shop.
     *
     *  Priya opened six months ago. Thirty seats. One espresso machine.
     *  Two baristas and a Java system her nephew Arjun built in a weekend.
     *
     *  It's 7am. She's been here since 5. Every surface wiped.
     *  Cups stacked. Sign flipped to OPEN.
     *  And she has not made a single coffee yet.
     *
     *  Not because she forgot. Because nobody has ordered.
     *
     *  [hold the cup up]
     *
     *  This cup is waiting. It is empty. In Java — it is null.
     *  And that is the most natural thing in the world.
     *
     *  Hold that thought."
     *
     * ── SHOW OF HANDS ────────────────────────────────────────────────────
     *
     * MALA:
     * "How many of you have had a production issue caused by a NullPointerException?
     *
     *  [pause — look around]
     *
     *  Good. Congratulations. You are seasoned Java developers.
     *  If your hand is not up — you are either very new, or very lucky.
     *  Either way — stay with us. Your turn is coming.
     *
     *  You know what causes NPE. You know how to prevent it.
     *  You've reviewed pull requests for it.
     *  You've probably walked someone through it at 11pm.
     *
     *  And yet — it is still the number one exception in Java production systems.
     *  Not second. First. Across Sentry's data. Across Elastic's reports. First.
     *
     *  So either every Java developer on the planet is incompetent —
     *
     *  [pause]
     *
     *  — or the problem is not where we've been looking.
     *
     *  We think it's the second one.
     *
     *  Three things in the next forty minutes:"
     *
     * CHANDRA:
     * "First  — null is everywhere not by accident. It was a decision.
     *            Made under constraints that have changed.
     *
     *  Second — three precise mechanisms by which null causes damage at scale.
     *            Not 'null is bad.' Three mechanisms. Each one a different
     *            dimension of the same problem.
     *
     *  Third  — a decision framework. Not a list of tools.
     *            Three questions that tell you exactly which solution belongs
     *            in your codebase, your Java version, your constraints.
     *            Something you can use on Monday morning.
     *
     *  [pick up the cup again]
     *
     *  If you understand why this cup was empty when Priya unlocked the door —
     *  you understand null better than most articles written about it.
     *
     *  Let's start there."
     */

    // ═════════════════════════════════════════════════════════════════════
    // PART 1 — WHY NULL EXISTS. THREE LEGITIMATE USES.          [3:00–8:00]
    // ═════════════════════════════════════════════════════════════════════

    /*
     * CHANDRA:
     * "Tony Hoare invented null in 1965. He later called it his billion-dollar mistake.
     *  But here is what he actually needed: a way to represent absence of a value
     *  in a type system that had no other mechanism for it.
     *
     *  Java used null in 1995 for the same reason.
     *  The language needed one value assignable to ANY reference type.
     *  Without null, you couldn't say 'this field hasn't been set yet.'
     *  You couldn't return 'not found' from a Map.
     *
     *  It was a decision. Made with good intentions. Under real constraints.
     *
     *  Let me show you the three places senior developers put null deliberately —
     *  because they had good reasons."
     */

    // ── 1.1  Optional domain value ────────────────────────────────────────
    //
    // STORY:
    // "7:15am. First customer. Jonathan. Regular.
    //  Priya opens a new order. Writes his name.
    //  'What are you having, Jonathan?'
    //  Jonathan stares at the menu board. Every morning. Three minutes minimum.
    //
    //  The order exists. Jonathan is real. His name is there.
    //  The coffee? Blank. He hasn't decided.
    //  null. And that is correct."

    static class CustomerAtPriyasShop {
        String  customerName;
        Coffee  selectedCoffee = null;  // Jonathan is still staring at the board
        String  promoCode      = null;  // most customers don't have one
        Integer tableNumber    = null;  // takeaway orders have no table number
    }

    /*
     * MALA:
     * "Three nulls. All deliberate. All representing something real.
     *
     *  selectedCoffee = null  — Jonathan hasn't decided. Come back in three minutes.
     *  promoCode      = null  — not applicable. He pays full price.
     *  tableNumber    = null  — takeaway. There is no table.
     *
     *  Three business states. The same value. null, null, null.
     *
     *  The compiler cannot tell them apart.
     *  Your tools cannot tell them apart.
     *  Arjun cannot tell them apart six months after he wrote this.
     *
     *  Keep that. We'll come back to it."
     */

    // ── 1.2  Lazy initialisation ──────────────────────────────────────────
    //
    // STORY:
    // "7:30am. Arjun is looking at the startup logs on his laptop.
    //  Every morning the system queries the database for 47 menu items.
    //  Half won't be ordered today. Why load them at 5am?
    //
    //  Arjun makes the menuCache null at startup.
    //  It loads the first time someone asks. Cached after that.
    //  This pattern is in Hibernate. In Spring. In the JDK.
    //  It was a legitimate performance optimisation."

    static class CoffeeStation {
        Map<String, Coffee> menuCache = null; // load on first request — not at startup

        Map<String, Coffee> getMenu() {
            if (menuCache == null)
                menuCache = loadFromDatabase();
            return menuCache;
        }

        private Map<String, Coffee> loadFromDatabase() {
            Map<String, Coffee> m = new HashMap<>();
            m.put("espresso", new Coffee("Espresso", "Ethiopian", "Grind fine. 90°C."));
            m.put("latte",    new Coffee("Latte",    "Colombian", "Shot + steamed milk."));
            m.put("newblend", new Coffee("New Blend","Rwandan",   null)); // instructions missing
            return m;
        }
    }

    /*
     * CHANDRA:
     * "The cup analogy holds — until the morning rush.
     *
     *  Thread 1 checks: menuCache is null.
     *  Thread 2 checks: menuCache is null.   [simultaneously]
     *  Thread 1 loads — sets menuCache.
     *  Thread 2 loads — overwrites menuCache. Again.
     *
     *  Two database hits. One cache. One race condition.
     *  No error. No warning. Arjun finds it three weeks later in a performance audit.
     *
     *  [pause]
     *
     *  The null that saved startup time just created a concurrency bug."
     */

    // ── 1.3  Releasing references for GC ─────────────────────────────────
    //
    // STORY:
    // "4:30pm. Jonathan left hours ago.
    //  Priya wipes his table. Marks the seat available.
    //  In early Java — on Java 1.4 with large heaps —
    //  your code had to do the same thing explicitly."

    static class SessionManager {
        static SessionManager instance     = new SessionManager();
        Coffee                currentOrder = null;

        static void shutdown()  { instance     = null; } // tell GC: done with this
        void       clearOrder() { currentOrder = null; } // table is clear
    }

    /*
     * MALA:
     * "In 2003 — correct. Intentional. Senior developers wrote this deliberately.
     *  Modern JVMs — G1, ZGC, Shenandoah — detect reachability automatically.
     *  Priya no longer needs to wipe the table herself. The GC sees when he left.
     *
     *  Why show it? Because understanding that null was used correctly —
     *  with good reason, by experienced people, for years —
     *  is the only honest foundation for understanding why removing it took thirty years.
     *
     *  [three outputs on screen — deliver the closing beat:]
     */

    static void part1_Demo() {
        Order jonathanOrder = new Order("Jonathan", null);
        System.out.println("Jonathan's coffee : " + jonathanOrder.coffee());   // null — he's deciding

        CoffeeStation station = new CoffeeStation();
        System.out.println("Menu at startup   : " + station.menuCache);        // null — waiting

        SessionManager.shutdown();
        System.out.println("Session at close  : " + SessionManager.instance);  // null — released
    }

    /*
     * CHANDRA:
     * "Three nulls. Three completely different intents.
     *
     *  The language cannot distinguish them.
     *  That is the root of the problem.
     *  Not that null exists. That null communicates nothing about WHY it's there.
     *
     *  [hold up the empty cup]
     *
     *  This cup told you a story. Jonathan. Arjun. Priya.
     *  The null in your stack trace does not.
     *
     *  Let's talk about where that silence causes the most damage."
     */


    // ═════════════════════════════════════════════════════════════════════
    // PART 2 — WHY IT BREAKS AT SCALE. THREE FAILURE MODES.    [8:00–16:00]
    // ═════════════════════════════════════════════════════════════════════

    /*
     * MALA:
     * "Back to Priya's shop. 7:45am. The morning rush.
     *  This is where null stops being a design decision
     *  and starts being a production incident.
     *
     *  Three failure modes. Not 'null is bad.'
     *  Three precise mechanisms. I want you to recognise
     *  all three from your own experience."
     */

    // ── Failure Mode 1 — null has no type and no origin ──────────────────
    //
    // STORY:
    // "Arjun gets a call. The ordering screen crashed.
    //  He opens the stack trace:
    //  NullPointerException at OrderService.java:47.
    //  He opens line 47."

    static String getBrewingInstructions(Order order) {
        return order.coffee().brewingInstructions().toUpperCase();
        //           ↑               ↑
        //     null here?       or null here?
        // One line. Two candidates. Zero information.
    }

    /*
     * CHANDRA:
     * "One line. Three method calls. Two potential nulls.
     *  coffee() could be null — Jonathan ordered but never chose.
     *  brewingInstructions() could be null — New Blend has no instructions yet.
     *
     *  The stack trace says line 47. That is all it says.
     *  Pre-Java 14 — Arjun adds logging, sets up a debugger,
     *  reproduces locally. Twenty minutes. Maybe forty.
     *  To find out which null it was.
     *
     *  Java 14 changed this. Let me show you:"
     */

    static void failureMode1_Demo() {
        System.out.println("── Failure Mode 1: null has no type, no origin ──");

        // Null 1 — coffee is absent
        Order noCoffee = new Order("Steve", null);
        try {
            getBrewingInstructions(noCoffee);
        } catch (NullPointerException e) {
            System.out.println("Java 14+ tells you exactly:");
            System.out.println("  → " + e.getMessage());
            // "Cannot invoke Coffee.brewingInstructions() because
            //  the return value of Order.coffee() is null"
            // Twenty minutes of debugging. Now it's a twenty-second read.
        }

        // Null 2 — coffee exists, instructions missing — same line, different null
        Order newBlend = new Order("Harry",
                new Coffee("New Blend", "Rwandan", null));
        try {
            getBrewingInstructions(newBlend);
        } catch (NullPointerException e) {
            System.out.println("Same line, different null, different message:");
            System.out.println("  → " + e.getMessage());
            // "Cannot invoke String.toUpperCase() because
            //  the return value of Coffee.brewingInstructions() is null"
        }
    }

    /*
     * MALA:
     * "JEP 358. Java 14. The most underrated null safety improvement
     *  Java has ever shipped.
     *  It doesn't prevent NPE. It makes the one you get actually useful.
     *  If you're on Java 14+ and still spending 40 minutes tracing nulls —
     *  you're not reading the exception message closely enough.
     *
     *  [to the room]
     *  How many of you knew about JEP 358?
     *
     *  [wait]
     *
     *  That's what I thought. It has been there since 2020."
     */

    // ── Failure Mode 2 — method signatures lie by omission ───────────────
    //
    // STORY:
    // "Priya asks Arjun: 'Does findCoffee return null if it's not found?'
    //  Arjun looks at the code. 'I... think so. Let me check.'
    //  He wrote it. Six months ago. He can't remember.
    //  This is failure mode two."

    static Coffee findCoffee_unsafe(String name) {
        CoffeeStation s = new CoffeeStation();
        return s.getMenu().get(name); // null if not found — caller has no idea
    }

    /*
     * CHANDRA:
     * "One question a developer should never have to ask about their own code:
     *  'Does this return null?'
     *
     *  At team scale — with fifty developers and five hundred service methods —
     *  every call becomes a trust exercise.
     *  Do I need to null-check this? The safe answer is: check everything.
     *  And that's exactly what happens. Defensive null checks everywhere.
     *  Some necessary. Most not. Nobody can tell which is which.
     *
     *  [a real story from the internet — exact quote]
     *  One developer described an API that sent null for half the payload.
     *  Their DTO assumed every field was non-null.
     *  Logs: none. Stack trace: minimal.
     *  Just one angry customer and a Jira ticket titled:
     *  'Your app broke my dashboard.'
     *
     *  The contract was not in the signature.
     *  The caller had no idea null was possible.
     *  Both sides were 'correct' and nobody communicated."
     */

    // ── Failure Mode 3 — null checks protect today, hide bugs tomorrow ───
    //
    // STORY:
    // "Sprint 1. Priya says: a customer can sit down before choosing their coffee.
    //  Arjun writes the null check. It's correct."

    static void processOrder_sprint1(Order order) {
        if (order.coffee() == null) {
            System.out.println("Sprint 1: No coffee yet — prompting customer."); // valid
            return;
        }
        System.out.println("Sprint 1: Processing — " + order.coffee().name());
    }

    /*
     * MALA:
     * "Sprint 4. Business rule changes.
     *  The new tablet UI enforces coffee selection before checkout.
     *  An order without a coffee should never reach the backend anymore.
     *  Arjun adds the new feature. The old null check is still there.
     *  He doesn't touch it. It's not broken. It still compiles."
     */

    static void processOrder_sprint4(Order order) {

        if (order.coffee() == null) {
            System.out.println("Sprint 4: skipping."); // ← now WRONG. Hides a bug.
            return;
            // This is no longer a valid state. It's a data integrity violation.
            // Something upstream is broken if coffee is null here.
            // But the null check absorbs it. Silently. No alert. No error. Nothing.
        }
        System.out.println("Sprint 4: Processing — " + order.coffee().name());
    }

    static void failureMode3_Demo() {
        System.out.println("\n── Failure Mode 3: checks hide bugs ──");

        Order missingCoffee = new Order("Jonathan", null);

        processOrder_sprint1(missingCoffee); // correct — valid state in sprint 1
        processOrder_sprint4(missingCoffee); // wrong — hides a violation in sprint 4
    }

    /*
     * CHANDRA:
     * "A NullPointerException here would have been better.
     *  It would have told Arjun immediately that something upstream
     *  sent an incomplete order after that was supposed to be impossible.
     *  Instead — silence. The bug ships. The data corrupts.
     *  Someone finds it three weeks later in a database audit.
     *
     *  [pause]
     *
     *  Null checks don't just defend against bugs. They bury them.
     *  An exception tells you the rule changed.
     *  A null check just carries on quietly.
     *
     *  Three failure modes. Each one a different dimension of the same problem:
     *
     *  1 — No type, no origin     → the stack trace tells you WHERE. Not WHY.
     *  2 — Signatures lie         → defensive checks everywhere, trust nowhere
     *  3 — Checks hide bugs       → correctness today, corruption tomorrow
     *
     *  So. What did we try to fix it?"
     */


    // ═════════════════════════════════════════════════════════════════════
    // PART 3 — WHAT WAS TRIED. FOUR ERAS. HONEST VERDICT.      [16:00–23:00]
    // ═════════════════════════════════════════════════════════════════════

    /*
     * MALA:
     * "Four eras. Each one a genuine attempt to fix null.
     *  I'm going to be direct about what worked and what didn't —
     *  because understanding why each fell short is the only way to understand
     *  why the next one was needed."
     */

    // ── Era 1 — Null checks (always existed) ─────────────────────────────

    // ✅ Done right — one guard, one message, at the boundary
    static String serve_nullCheckDoneRight(Order order) {
        Coffee coffee = order.coffee();
        if (coffee == null) return "Nothing ordered yet — Jonathan is still deciding.";
        return coffee.name();
    }

    // ❌ Done wrong — what happens at scale
    static String serve_nullCheckHell(Order order) {
        if (order != null) {
            if (order.coffee() != null) {
                if (order.coffee().name() != null) {
                    if (order.coffee().brewingInstructions() != null) {
                        return order.coffee().brewingInstructions().toUpperCase();
                    } else { return "No instructions."; }
                } else { return "No name."; }
            } else { return "No coffee."; }
        }
        return "No order.";
    }

    /*
     * CHANDRA:
     * "serve_nullCheckDoneRight — clean. One guard. This is null checks done right.
     *  serve_nullCheckHell — this is null checks at scale. Four fields, five branches.
     *  This is on GitHub in your organisation right now. Different class name. Same shape.
     *
     *  What worked: universal. No dependencies. No Java version. Always works.
     *  What didn't: easy to forget, impossible to enforce, communicates nothing about intent.
     *  Is this null a valid state or a bug? The check doesn't say."
     */

    // ── Era 2 — Annotations: JSR-305 / JetBrains (2006–2024) ────────────

    /*
     * MALA:
     * "2006. The community tried to fix null with annotations.
     *  The idea was sound: mark the contract in the code itself.
     *  @Nullable means 'I might return null — check before you use this.'
     *  @NonNull means 'I promise this is never null.'
     *
     *  The execution was fractured. By 2010 we had:
     *  javax.annotation.Nullable, org.jetbrains.annotations.Nullable,
     *  android.annotation.Nullable, edu.umd.cs.findbugs.annotations.Nullable.
     *  Every tool understood a different one.
     *  Every team picked one and hoped the others agreed. They did not.
     *
     *  What worked: IDE support was genuinely valuable — IntelliJ would underline
     *  unsafe dereferences. That caught real bugs.
     *
     *  What didn't: adoption was voluntary. A developer under deadline
     *  ignores a yellow squiggle. No enforcement = no guarantee.
     *
     *  2024: JSpecify 1.0. Google, JetBrains, Uber, Oracle. One standard. Finally.
     *  @NullMarked makes non-null the default. @Nullable marks the exceptions.
     *  Paired with NullAway — violations fail the BUILD. Not a squiggle. The build.
     *
     *  Still annotation-based. Still requires tool config.
     *  But the best answer available today without changing your Java version."
     */

    // ── Era 3 — Optional<T> (Java 8, 2014) ───────────────────────────────

    static Optional<Coffee> findCoffee_safe(String name) {
        CoffeeStation s = new CoffeeStation();
        return Optional.ofNullable(s.getMenu().get(name));
        // The return type IS the contract now.
        // Caller cannot call .name() without going through Optional API.
    }

    static void era3_Demo() {
        System.out.println("\n── Era 3: Optional<T> ──");

        // ✅ What Optional does well
        String result = findCoffee_safe("espresso")
                .map(Coffee::name)
                .map(String::toUpperCase)
                .orElse("Not on the menu");
        System.out.println("Found   : " + result);

        // ✅ ifPresentOrElse (Java 9) — handle both cases in one call
        findCoffee_safe("coldpresso")
                .ifPresentOrElse(
                    c  -> System.out.println("Serving : " + c.name()),
                    () -> System.out.println("Missing : Not on menu — try our cold brew")
                );

        // ❌ The anti-pattern — Optional.get() without checking
        try {
            Coffee bad = findCoffee_safe("coldpresso").get(); // NoSuchElementException
        } catch (NoSuchElementException e) {
            System.out.println("Anti-pat: Optional.get() is Optional used wrong");
            // Traded NullPointerException for NoSuchElementException.
            // Different exception. Same mindset. Same problem.
        }
    }

    /*
     * CHANDRA:
     * "What Optional did well: absence is now visible in the return type.
     *  The contract moved from documentation into the type system.
     *  The compiler forces you to acknowledge both cases.
     *
     *  What Optional didn't fix: the existing world.
     *  Map.get() still returns null. Every pre-Java-8 library still returns null.
     *  Optional only helps when YOU control the return type.
     *
     *  And it was immediately misused — as field types, as parameters.
     *  [look at the audience]
     *  The Java language architect said explicitly:
     *  Optional is for return types only.
     *  Optional as a field type is a code smell.
     *
     *  Three eras. Each one fixed what the previous missed.
     *  Each one left one gap the next tried to close.
     *  That's not failure. That's how standards evolve under real constraints."
     */


    // ═════════════════════════════════════════════════════════════════════
    // PART 4 — WHAT MODERN JAVA GIVES YOU. USE IT NOW.         [23:00–34:00]
    // ═════════════════════════════════════════════════════════════════════

    /*
     * MALA:
     * "I want to ask you something uncomfortable.
     *  How many of you are on Java 17 or above in production?
     *  [wait for hands]
     *
     *  Keep your hand up if you're using records.
     *  [hands drop]
     *
     *  Keep your hand up if you're using sealed classes.
     *  [more hands drop]
     *
     *  Every feature I just named was designed — at least partly —
     *  to reduce null surface area in Java. Not as a side effect. As a design goal.
     *
     *  You upgraded the runtime. You didn't upgrade the code.
     *  You're paying for a sports car and driving it in first gear.
     *
     *  Five features. Let me show you what second gear looks like."
     */

    // ── Modern Feature 1 — Records (Java 16) ─────────────────────────────
    //
    // CHANDRA:
    // "Arjun's original CoffeeOrder class had six fields. All null by default.
    //  No validation. No contract. The bad object could exist indefinitely.
    //  Here is the same model as a record."

    record CoffeeOrder(String customerName, Coffee coffee, int shots) {
        CoffeeOrder {
            Objects.requireNonNull(customerName, "Customer name required");
            Objects.requireNonNull(coffee,       "Coffee required — use a pending state for incomplete orders");
            if (shots < 1 || shots > 4)
                throw new IllegalArgumentException("Shots 1–4. This is a coffee shop, not a chemistry lab.");
        }
        // CHANDRA:
        // "The bad object cannot exist. Null stopped at the door.
        //  If construction fails — NPE fires at the call site.
        //  Not in a service. Not in a controller. Not at 2am. RIGHT THERE.
        //  The stack trace points directly at whoever passed null.
        //  No detective work. The record made the crime scene the same as the crime."
    }

    static void feature1_Records() {
        System.out.println("\n── Feature 1: Records — null stopped at construction ──");

        // ✅ Valid
        CoffeeOrder good = new CoffeeOrder("Alice",
                new Coffee("Espresso", "Ethiopian", "Grind fine."), 2);
        System.out.println("Valid : " + good.customerName() + " — " + good.coffee().name());

        // ❌ Null stopped immediately — at the call site
        try {
            CoffeeOrder bad = new CoffeeOrder(null,
                    new Coffee("Espresso", "Ethiopian", null), 1);
        } catch (NullPointerException e) {
            System.out.println("Null  : " + e.getMessage());
        }
    }

    // ── Modern Feature 2 — Sealed classes (Java 17) ──────────────────────
    //
    // MALA:
    // "Arjun's findCoffee used to return null for 'not found.'
    //  The caller had no idea. The signature said nothing.
    //  Here is the same lookup with a sealed return type."

    sealed interface CoffeeResult
            permits Found, NotFound, Unavailable {}
    record Found(Coffee coffee)        implements CoffeeResult {}
    record NotFound(String name)       implements CoffeeResult {}
    record Unavailable(String reason)  implements CoffeeResult {}

    static CoffeeResult lookupCoffee(String name) {
        CoffeeStation s = new CoffeeStation();
        Coffee c = s.getMenu().get(name);
        if (c == null) return new NotFound(name);
        return new Found(c);
        // No null returned. Ever. The return type IS the full contract.
        // Found, NotFound, Unavailable — three explicit states.
        // The caller cannot ignore any of them.
    }

    static void feature2_SealedClasses() {
        System.out.println("\n── Feature 2: Sealed classes — null replaced by type ──");

        List.of("espresso", "coldpresso").forEach(name -> {
            String msg = switch (lookupCoffee(name)) {
                case Found      f -> "Found : " + f.coffee().name();
                case NotFound   n -> "Gone  : " + n.name() + " — not on the menu";
                case Unavailable u -> "Sorry : " + u.reason();
                // No default — compiler verifies all cases.
                // Add a new sealed subtype, forget to handle it — compile error.
                // The forgotten null-returning case is now impossible.
            };
            System.out.println(msg);
        });
    }

    /*
     * CHANDRA:
     * "NotFound isn't null. It's a first-class state with a name.
     *  null never had a name. null was just... nothing.
     *  And nothing is hard to debug.
     *
     *  One more thing about the sealed switch — no default clause.
     *  The compiler knows every subtype of CoffeeResult.
     *  Add a new one tomorrow, forget to handle it here — compile error.
     *  Before the code ships. That's the guarantee null never gave you."
     */

    // ── Modern Feature 3 — Null-hostile collections (Java 9) ─────────────
    //
    // MALA:
    // "This one surprises most developers. Java 9's List.of(), Map.of(), Set.of()
    //  are not just immutable. They are deliberately null-HOSTILE.
    //  Pass a null element — NPE fires immediately at the factory call.
    //  Not when you iterate. Not when you serialise. RIGHT THERE."

    static void feature3_NullHostileCollections() {
        System.out.println("\n── Feature 3: Null-hostile collections (Java 9) ──");

        // ❌ ArrayList — accepts null silently, NPE deferred to wherever you use it
        List<Coffee> mutableMenu = new ArrayList<>();
        mutableMenu.add(new Coffee("Espresso", "Ethiopian", "Grind fine."));
        mutableMenu.add(null);   // accepted. null travels into the list.
        System.out.println("ArrayList: null accepted at add() — NPE deferred to forEach");

        // ✅ List.of() — null rejected at the boundary
        try {
            List<Coffee> menu = List.of(
                    new Coffee("Espresso", "Ethiopian", "Grind fine."),
                    null  // ← NPE fires HERE. Not at forEach. Here.
            );
        } catch (NullPointerException e) {
            System.out.println("List.of(): null rejected immediately — gap = zero");
        }

        // CHANDRA:
        // "Two lists. Same null. Completely different behaviour.
        //  ArrayList: null enters at add(), NPE fires at forEach — or sort, or stream,
        //  or serialisation. The gap between cause and crash can be enormous.
        //  List.of(): gap is zero. The stack trace points at whoever passed null.
        //
        //  One migration: everywhere you use 'new ArrayList<>()' for a list
        //  that shouldn't change — replace it with List.of() or List.copyOf().
        //  Every null that slips in gets caught immediately."
    }

    // ── Modern Feature 4 — Pattern matching + null case (Java 16/21) ─────
    //
    // MALA:
    // "Pattern matching instanceof. Java 16.
    //  One property most developers don't realise:
    //  instanceof on null ALWAYS returns false. Always.
    //  No NPE. No null check before the instanceof. It's in the spec."

    static void feature4_PatternMatching() {
        System.out.println("\n── Feature 4: Pattern matching + null case (Java 21) ──");

        // instanceof null = false, always — no null check needed first
        Coffee maybeNull = null;
        if (maybeNull instanceof Coffee c) {
            System.out.println("Should not print");
        } else {
            System.out.println("instanceof null = false. Always. No NPE. No guard needed.");
        }

        // Switch + null case (Java 21)
        // Before Java 21: switch on null threw NPE before any case ran.
        // Java 21: null is a first-class case.
        List<@Nullable String> sizes = new ArrayList<>();
        sizes.add(null);
        sizes.add("small");
        sizes.add("large");

        sizes.forEach(size -> {
            String desc = switch (size) {
                case null    -> "Not specified — defaulting to regular";
                case "small" -> "8oz — enough to start";
                case "large" -> "16oz — we've all been there";
                default      -> "Regular — the sensible choice";
            };
            System.out.println("Size: " + desc);
        });

        // CHANDRA:
        // "null had its own case. The language acknowledged its existence.
        //  Not a workaround. Not a guard before the switch.
        //  A case. Like any other value.
        //  The 'I forgot to handle the empty case' bug — null's most common disguise —
        //  is now a compile error with sealed types. Cannot be forgotten."
    }

    // ── Modern Feature 5 — Optional upgrades (Java 9) ────────────────────
    //
    // MALA:
    // "Most developers know three Optional methods: map(), orElse(), isPresent().
    //  Java 9 shipped three more. I have never once seen Optional.or() used
    //  correctly in a production pull request. Here is what you are missing."

    static Optional<Coffee> findInPrimary(String name) {
        Map<String, Coffee> m = Map.of(
                "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine."));
        return Optional.ofNullable(m.get(name));
    }

    static Optional<Coffee> findInBackup(String name) {
        Map<String, Coffee> m = Map.of(
                "latte", new Coffee("Latte", "Colombian", "Shot + milk."));
        return Optional.ofNullable(m.get(name));
    }

    static void feature5_OptionalUpgrades() {
        System.out.println("\n── Feature 5: Optional.or() and stream() (Java 9) ──");

        // Optional.or() — chain sources, first non-empty wins, lazy
        Optional<Coffee> found = findInPrimary("latte")          // empty
                .or(() -> findInBackup("latte"))                  // present — stops here
                .or(() -> Optional.empty());                      // not reached
        System.out.println("or()     : " + found.map(Coffee::name).orElse("not found"));

        // Optional.stream() — null never enters the stream
        List<String> names = List.of("espresso", "coldpresso", "latte");

        // ❌ Old — null enters stream, filtered later
        List<Coffee> old = names.stream()
                .map(n -> findInPrimary(n).orElse(null)) // null travels in
                .filter(Objects::nonNull)                 // filtered out later
                .collect(Collectors.toList());
        System.out.println("stream old: " + old.size() + " found (null entered stream)");

        // ✅ Modern — null never enters
        List<Coffee> modern = names.stream()
                                   .map(Demo0_NullSafetySession::findInPrimary)    // Stream<Optional<Coffee>>
                                   .flatMap(Optional::stream)                 // empties removed, null never enters
                                   .toList();                                 // Java 16
        System.out.println("stream new: " + modern.size() + " found (null never entered)");

        // CHANDRA:
        // "flatMap(Optional::stream) — empty optionals contribute nothing.
        //  Present optionals contribute their value. The pipeline is clean by construction.
        //  No filter needed. No Objects::nonNull. null never gets in."
    }


    // ═════════════════════════════════════════════════════════════════════
    // PART 5 — HOW TO PICK YOUR SOLUTION. THREE QUESTIONS.     [34:00–39:00]
    // ═════════════════════════════════════════════════════════════════════

    /*
     * MALA:
     * "This is what senior developers actually came for.
     *  Not a list of tools. A decision framework.
     *  Three questions. Answer them in order. The right tool follows."
     */

    // ── Question 1 — Is null a valid domain state, or a bug? ─────────────
    //
    // CHANDRA:
    // "Most important question. Everything else follows from it.
    //
    //  If null means a valid, expected, business state — model it. Communicate it.
    //  If null means something should never happen — reject it immediately. Loudly.
    //
    //  The mistake most codebases make: treating all nulls the same."

    // Valid state → @Nullable + null check + Optional as return type
    static Optional<String> getCoffeeName(Order order) {
        return Optional.ofNullable(order.coffee()).map(Coffee::name);
        // @Nullable on order.coffee() — Optional makes absence explicit to callers
    }

    // Bug → Objects.requireNonNull at construction — the bad object never exists
    static class BrewStation {
        private final Coffee signature;
        private final String location;

        BrewStation(Coffee signature, String location) {
            this.signature = Objects.requireNonNull(signature, "Station needs a signature coffee");
            this.location  = Objects.requireNonNull(location,  "Station needs a location");
            // If Spring injection failed — this throws HERE. At startup.
            // Not when the first customer orders. Not at 2am. HERE.
        }
    }

    static void question1_Demo() {
        System.out.println("\n── Q1: Valid state or bug? ──");

        // Valid — coffee absent, handled gracefully
        Order pending = new Order("Jonathan", null);
        System.out.println("Valid null : " + getCoffeeName(pending).orElse("Not chosen yet"));

        // Bug — null rejected at construction, immediately
        try {
            BrewStation broken = new BrewStation(null, "Counter 3");
        } catch (NullPointerException e) {
            System.out.println("Bug null   : " + e.getMessage());
        }
    }

    // ── Question 2 — Do you control the boundary? ─────────────────────────
    //
    // MALA:
    // "Do you control the code that returns null — or not?
    //
    //  If you control it: @NullMarked on the package, @Nullable on exceptions,
    //  NullAway in the build. Non-null is the default. @Nullable breaks the silence.
    //  The contract is documented and enforced at build time.
    //
    //  If you don't control it — external API, legacy library, Map.get() —
    //  one job: wrap it at the boundary. Once. In one adapter.
    //  After that — your code never sees the raw null."

    // Simulates an external API you cannot change
    static class LegacyCoffeeApi {
        private static final Map<String, Coffee> store = Map.of(
                "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine."));
        static Coffee findByName(String name) { return store.get(name); } // returns null — can't change this
    }

    // Your adapter — the ONE place that knows about the external null contract
    static class CoffeeAdapter {
        // Wrap once here. Null never crosses inward.
        static Optional<Coffee> findByName(String name) {
            return Optional.ofNullable(LegacyCoffeeApi.findByName(name));
        }

        static Coffee findByNameOrDefault(String name, Coffee fallback) {
            return Objects.requireNonNullElse(LegacyCoffeeApi.findByName(name), fallback);
        }
    }

    static void question2_Demo() {
        System.out.println("\n── Q2: Control the boundary? ──");

        Coffee fallback = new Coffee("House Blend", "Unknown", null);

        System.out.println("Found   : " + CoffeeAdapter.findByName("espresso")
                .map(Coffee::name).orElse("not found"));
        System.out.println("Missing : " + CoffeeAdapter.findByName("coldpresso")
                .map(Coffee::name).orElse("Not on menu"));
        System.out.println("Default : " + CoffeeAdapter.findByNameOrDefault("coldpresso", fallback).name());

        // CHANDRA:
        // "The adapter is your anti-corruption layer.
        //  It owns all knowledge of the external API's null behaviour.
        //  The rest of your codebase imports the adapter — never the raw API.
        //  Null stays at the door."
    }

    // ── Question 3 — What is your Java version? ───────────────────────────
    /*
     * MALA:
     * "Third question. Not which version you want. Which you're running today.
     *
     *  Java 8–11:  Optional for return types. requireNonNull in constructors.
     *              JSpecify @NullMarked + NullAway if you can add a build plugin.
     *              That combination catches the majority of null bugs before they ship.
     *
     *  Java 16+:   Add records. Compact constructors enforce invariants at construction.
     *              The bad object never exists.
     *
     *  Java 17+:   Add sealed classes. Null return type replaced by explicit named states.
     *              Exhaustive switch — compiler verifies every case is handled.
     *
     *  Java 21+:   Switch null case. instanceof pattern matching stable.
     *              Language handles null explicitly — no guard before the switch.
     *
     *  Valhalla:   String! — null-restricted types. JVM enforced.
     *              No annotation, no tool, no discipline required. Just the type."
     *
     * CHANDRA — THE DECISION TABLE — say this without looking at notes:
     *
     * "One table. Use it on Monday.
     *
     *  Q1: Valid state or bug?
     *      Valid  → @Nullable + Optional return type + null check
     *      Bug    → requireNonNull at construction — fail fast, fail loud
     *
     *  Q2: Control the boundary?
     *      Yes    → @NullMarked + @Nullable + NullAway in CI
     *      No     → Adapter wraps at boundary — Optional.ofNullable once
     *
     *  Q3: Java version?
     *      8–11   → Optional, requireNonNull, JSpecify + NullAway
     *      16+    → Add records with compact constructor validation
     *      17+    → Add sealed classes — explicit states, no null return
     *      21+    → Switch null case, pattern matching — language handles it
     *      Valhalla → Coffee! — the type system says no. Final answer.
     *
     *  One rule that cuts across all three:
     *  Pick ONE layer and enforce it.
     *  Annotations without enforcement = documentation.
     *  Enforcement without contract = false confidence.
     *  Contract + enforcement together = bugs stop at build time."
     */


    // ═════════════════════════════════════════════════════════════════════
    // CLOSE — VALHALLA. MONDAY MORNING. ONE LINE.               [39:00–40:00]
    // ═════════════════════════════════════════════════════════════════════

    /*
     * ⚠️  VALHALLA — ILLUSTRATIVE. NOT COMPILABLE TODAY.
     *      String! is the proposed null-restricted type syntax.
     *      Final syntax may differ. Not yet released.
     */

    // What the session's Coffee record looks like under Valhalla:
    //
    //   record Coffee(String! name, String! origin, String brewingInstructions) {}
    //                        ↑              ↑
    //                 null-restricted — JVM enforced
    //                 No annotation. No requireNonNull. No NullAway config.
    //                 The type says no. The JVM enforces it before the constructor runs.
    //
    //   record Order(String! customerName, Coffee coffee) {}
    //                       ↑                     ↑
    //                 must exist         may be null — absence of ! IS the declaration

    /*
     * MALA:
     * "The arc — one sentence each:
     *
     *  1965  null invented.    'Absence needs a value.'
     *  1995  null lands in Java. Assignable to any reference type.
     *  2006  JSR-305.           @Nullable. Right idea. No standard.
     *  2014  Optional<T>.       Absence becomes a type. Misused immediately.
     *  2020  JEP 358.           Java tells you WHICH null. Finally.
     *  2021  Records + sealed.  Null stopped at construction. States made explicit.
     *  2023  Switch null case.  Language handles null. Not your guard clause.
     *  2024  JSpecify 1.0.      Industry agrees. One standard. Build enforcement.
     *  Next  Valhalla.          String! — the type system says no. Final answer."
     *
     * CHANDRA — three actions. Deliver without notes:
     *
     * "Monday morning. Three things.
     *
     *  One: Add @NullMarked to your package-info.java.
     *       One annotation. One file. Every field and return type in the package
     *       becomes non-null by default. Only @Nullable marks exceptions.
     *       Three minutes. No noise. No @NonNull everywhere.
     *
     *  Two: Add NullAway to your build with OnlyNullMarked=true.
     *       Restrict enforcement to packages you've annotated.
     *       Leave everything else untouched.
     *       Null contract violations fail the build.
     *       Cannot push past a failing build. That's the point.
     *
     *  Three: Replace one data class with a record.
     *         One class. One compact constructor. One sprint.
     *         The null that was hiding in that class now fires at construction.
     *         The stack trace points at the source. Not three layers deep.
     *
     *  When Valhalla ships — the migration is mechanical.
     *  Because you already documented the contracts.
     *  String! replaces @NullMarked. The JVM replaces requireNonNull.
     *  The annotations become redundant. The work isn't wasted. It's preparation.
     *
     *  [pick up the coffee cup one last time]
     *
     *  Priya's cup was empty this morning.
     *  Jonathan finally ordered — espresso, two shots.
     *  Arjun fixed the race condition.
     *  The session cache was released at close.
     *
     *  Three nulls. Three stories. Three solutions.
     *  All of them in your codebase. All of them fixable.
     *
     *  [set the cup down]
     *
     *  Tony Hoare's billion-dollar mistake finally has a bill of sale.
     *
     *  Thank you."
     *
     * ─────────────────────────────────────────────────────────────────────
     *  [No summary slide. Senior developers don't need their own talk
     *   summarised back to them. The cup on the table IS the closing image.]
     * ─────────────────────────────────────────────────────────────────────
     */

    // ═════════════════════════════════════════════════════════════════════
    // MAIN — run all demos in order
    // ═════════════════════════════════════════════════════════════════════

    public static void main(String[] args) {

        System.out.println("═".repeat(70));
        System.out.println("THE PAST, PRESENT, AND FUTURE OF NULL SAFETY IN JAVA");
        System.out.println("═".repeat(70));

        // Part 1 — Why null exists
        System.out.println("\n[ PART 1 — WHY NULL EXISTS ]");
        part1_Demo();

        // Part 2 — Why it breaks at scale
        System.out.println("\n[ PART 2 — WHY IT BREAKS AT SCALE ]");
        failureMode1_Demo();
        failureMode3_Demo();

        // Part 3 — What was tried
        System.out.println("\n[ PART 3 — WHAT WAS TRIED ]");
        era3_Demo();

        // Part 4 — What modern Java gives you
        System.out.println("\n[ PART 4 — MODERN JAVA ]");
        feature1_Records();
        feature2_SealedClasses();
        feature3_NullHostileCollections();
        feature4_PatternMatching();
        feature5_OptionalUpgrades();

        // Part 5 — Decision framework
        System.out.println("\n[ PART 5 — DECISION FRAMEWORK ]");
        question1_Demo();
        question2_Demo();

        System.out.println("\n" + "═".repeat(70));
        System.out.println("BLOG POSTS — topics that go deeper:");
        System.out.println("  → Optional<T> deep dive");
        System.out.println("  → NullAway setup guide: Maven/Gradle + CI");
        System.out.println("  → JSpecify migration: package by package");
        System.out.println("  → Project Valhalla: null-restricted types");
        System.out.println("  → Null-safe Map operations at scale");
        System.out.println("  → JPA lazy loading and null: the Hibernate trap");
        System.out.println("═".repeat(70));
    }
}