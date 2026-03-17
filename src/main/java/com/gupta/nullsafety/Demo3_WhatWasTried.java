package com.gupta.nullsafety;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DEMO 3 — WHAT WAS TRIED, WHAT WORKED, WHAT DIDN'T            [15:00 – 27:00]
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Chandra/ Mala:
 * Four eras. Each one a genuine attempt to fix null.
 *  I'm going to be direct about what worked and what didn't —
 *  because understanding why each solution fell short
 *  is the only way to understand why the next one was needed."
 *
 *      // ERA 1 — Defensive null checks (always existed)
 *      // ERA 2 — JSR-305 / FindBugs annotations (2006)   (Cyclomatic complexity)
 *      // ERA 3 — Optional<T> (Java 8, 2014)   (TODO: Add links to Optional antipatterns)
 *      // ERA 4 — JSpecify (Inception - 2014 - ) — the standard the ecosystem needed (TODO: Check for all the dates)
 *
 *
 *   // TODO: AI Agents ()
 *   //
 */
@NullMarked
public class Demo3_WhatWasTried {

    record Coffee(String name, String origin, @Nullable String brewingInstructions) {}
    record Order(String customerName, @Nullable Coffee coffee) {}

    private static final Map<String, Coffee> menu = new HashMap<>();
    static {
        menu.put("espresso", new Coffee("Espresso", "Ethiopian", "Grind fine. 90°C."));
        menu.put("latte",    new Coffee("Latte",     "Colombian", "Shot + steamed milk."));
        menu.put("newblend", new Coffee("New Blend", "Rwandan",   null));
    }

    // ─────────────────────────────────────────────────────────────────────
    // ERA 1 — Defensive null checks (always existed)
    // ─────────────────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Era one. Null checks. The oldest tool. Still relevant.
    //  Let me show you the full spectrum — from correct to catastrophic."

    // 😊 Check at the boundary, one guard, clear message
    static String serveCorrect(Order order) {
        Coffee coffee = order.coffee();
        if (coffee == null) return "No coffee on this order.";
        return coffee.name();
    }

    // 😈 Null check hell
    static String serveNullCheckHell(Order order) {
        if (order != null) {
            if (order.coffee() != null) {
                if (order.coffee().name() != null) {
                    if (order.coffee().brewingInstructions() != null) {
                        return order.coffee().brewingInstructions().toUpperCase();
                    } else {
                        return "No instructions.";
                    }
                } else {
                    return "No name.";
                }
            } else {
                return "No coffee.";
            }
        }
        return "No order.";
    }

    static void demo_Era1() {
        System.out.println("══ ERA 1: Null Checks ══");

        Order goodOrder  = new Order("Alice", new Coffee("Espresso", "Ethiopian", "Grind fine."));
        Order emptyOrder = new Order("Bob",   null);

        System.out.println("Correct    : " + serveCorrect(goodOrder));
        System.out.println("Correct    : " + serveCorrect(emptyOrder));
        System.out.println("Hell       : " + serveNullCheckHell(goodOrder));

        // Chandra/ Mala:
        // "serve_correct — clean. One guard. One message. This is null checks done right.
        //  serve_nullCheckHell — this is null checks at scale. Four fields, five branches,
        //  and we haven't even handled the case where the ORDER itself is null.
        //
        //  What worked: null checks are universal — no dependencies, no Java version,
        //  no framework. They always work.
        //
        //  What didn't: they're easy to forget, impossible to enforce, and the code
        //  gets unreadable fast. They communicate nothing about INTENT.
        //  Is this null a valid state or a bug? The check doesn't say."
    }

    // ─────────────────────────────────────────────────────────────────────
    // ERA 2 — JSR-305 / FindBugs annotations (2006)
    // ─────────────────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Era two. 2006. The community tried to fix null with annotations.
    //  The idea was sound: mark the CONTRACT in the code itself.
    //  @Nullable means 'I might return null, you must check.'
    //  @NonNull means 'I promise this is never null.'
    //
    //  The execution was fractured.
    //
    //  By 2010 we had:
    //  javax.annotation.Nullable,
    //  org.jetbrains.annotations.Nullable,
    //  android.annotation.Nullable,
    //  edu.umd.cs.findbugs.annotations.Nullable...
    //
    //  Every tool understood different annotations.
    //  Every team picked one and hoped the others agreed.
    //  They did not agree.
    //
    //  What worked: IDE support was genuinely valuable. IntelliJ would
    //  underline unsafe dereferences. That caught real bugs.
    //
    //  What didn't: adoption was voluntary. A developer under deadline
    //  pressure ignores a yellow squiggle. No enforcement = no guarantee."

    // The intent was right — the annotation declares the contract
    // @Nullable  Coffee findByName(@NotNull String name)  // ← JSR-305 idea
    // Problem: which @Nullable? Which tool enforces it? No standard.

    // ─────────────────────────────────────────────────────────────────────
    // ERA 3 — Optional<T> (Java 8, 2014)
    // ─────────────────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Era three. Java 8. Optional<T>. The most visible attempt.
    //  The idea: make absence a TYPE, not a value.
    //  Instead of returning null and hoping the caller checks —
    //  return a box. The box is either full or empty.
    //  The TYPE SYSTEM forces the caller to acknowledge both cases.
    //
    //  Let me show you what changed."

    // Before Optional — caller has no idea
    static Coffee findByName_unsafe(String name) {
        return menu.get(name.toLowerCase()); // null if not found — caller doesn't know
    }

    // After Optional — contract is in the type
    static Optional<Coffee> findByName(String name) {
        return Optional.ofNullable(menu.get(name.toLowerCase()));
    }

    static void demo_Era3() {
        System.out.println("\n══ ERA 3: Optional<T> ══");

        // ✅ What Optional does well — chained operations, no null checks
        String result = findByName("espresso")
                .map(Coffee::name)
                .map(String::toUpperCase)
                .orElse("Not found");
        System.out.println("Found    : " + result);

        // ✅ orElse — default value
        String missing = findByName("coldpresso")
                .map(Coffee::name)
                .orElse("Not on menu — try our cold brew");
        System.out.println("Missing  : " + missing);

        // ✅ orElseThrow — when absence is an error
        try {
            Coffee required = findByName("coldpresso")
                    .orElseThrow(() -> new IllegalStateException(
                            "'coldpresso' must be configured — check setup"));
        } catch (IllegalStateException e) {
            System.out.println("Error    : " + e.getMessage());
        }

        // Optional.get() without checking
        // Chandra/ Mala:
        // "And here's the one that makes me genuinely sad.
        //  Optional.get() without isPresent() first.
        //  You've traded a NullPointerException for a NoSuchElementException.
        //  Different exception. Same mindset. Same problem.
        //  If you're calling .get() — you're using Optional wrong."
        try {
            Coffee bad = findByName("coldpresso").get(); // 💥 NoSuchElementException
        } catch (java.util.NoSuchElementException e) {
            System.out.println("Anti-pat : " + e.getClass().getSimpleName()
                    + " — Optional.get() is Optional used wrong");
        }

        // Chandra/ Mala:
        // "What Optional did well: it made absence visible in the return type.
        //  The contract moved from documentation into the type system.
        //
        //  What Optional didn't fix: the existing APIs. Map.get() still returns null.
        //  Every pre-Java-8 library still returns null. Optional only helps
        //  when YOU control the return type. The world you can't change
        //  is still full of null.
        //
        //  And Optional was immediately misused — as field types, as parameters.
        //  The language architect said explicitly: Optional is for return types only.
        //  Optional as a field type is a code smell. It serialises badly,
        //  it's heap-allocated unnecessarily, and it makes the field non-null
        //  while the value inside is still absent. Two layers of maybe. Worse."
    }

    static class ModernJavaRecordPatterns {
        record Name       (String fName, String lName) { }
        record PhoneNumber(String areaCode, String number) { }
        record Country    (String countryCode, String countryName) { }
        record Passenger  (Name name,
                           PhoneNumber phoneNumber,
                           Country from,
                           Country destination) { }

        boolean checkFirstNameAndCountryCode (Object obj) {
            if (obj != null) {
                if (obj instanceof Passenger passenger) {
                    Name name = null;
                    Country destination = null;

                    if (passenger.name() != null) {
                        name = passenger.name();

                        if (passenger.destination() != null) {
                            destination = passenger.destination();

                            String fName = name.fName();
                            String countryCode = destination.countryCode();

                            if (fName != null && countryCode != null) {
                                return fName.startsWith("Simo") &&
                                       countryCode.equals("PRG");
                            }
                        }
                    }
                }
            }
            return false;
        }

        boolean checkFirstNameAndCountryCodeAgain (Object obj) {
            if (obj instanceof Passenger(Name (String fName, String lName),
                                         PhoneNumber phoneNumber,
                                         Country from,
                                         Country (String countryCode, String countryName) )) {

                if (fName != null && countryCode != null) {
                    return fName.startsWith("Simo") && countryCode.equals("PRG");
                }
            }
            return false;
        }
    }





    // ─────────────────────────────────────────────────────────────────────
    // ERA 4 — JSpecify (2024) — the standard the ecosystem needed
    // ─────────────────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Era four. 2024. JSpecify 1.0.
    //  Google, JetBrains, Uber, Oracle, and others sat in a room
    //  and agreed on ONE annotation standard. Seventeen years after JSR-305.
    //
    //  The key insight JSpecify got right that JSR-305 missed: MAKE NON-NULL THE DEFAULT
    //  don't annotate every non-null field. Make non-null the DEFAULT.
    //  Only annotate the EXCEPTIONS.
    //
     // Annotations on PACKAGE - @NullMarked
    //  @NullMarked on the package says: everything here is non-null unless stated.
    //  @Nullable marks the deliberate exceptions.
    //  That's it. Two annotations cover the entire null contract."
    // TWO ANNOTATIONS COVER THE ENTIRE NULL CONTRACT

    // With @NullMarked on this class (see top of file):
    // String, Coffee, Order — all non-null by default. No annotation needed.
    // @Nullable marks the exceptions — brewingInstructions, coffee in Order.

    // What the JetBrains version looked like:
    //   record Coffee(@NotNull String name, @NotNull String origin,
    //                 @Nullable String brewingInstructions)
    //
    // What JSpecify @NullMarked looks like:
    //   record Coffee(String name, String origin,
    //                 @Nullable String brewingInstructions)
    //
    // Chandra/ Mala:
    // "Same contract. @NotNull disappears entirely.
    //  The annotation is the silence. @Nullable breaks the silence.
    //  You read the ABSENCE of @Nullable as a guarantee — not an oversight."

    // Combined with NullAway — compile-time enforcement, not just IDE warnings
    //
    // Chandra/ Mala:
    // "And paired with NullAway — Uber's compiler plugin —
    //  violating the @Nullable contract fails the BUILD.
    //  Not a yellow squiggle. Not a code review comment.
    //  The code does not compile.
    //  That's the gap JSR-305 never closed."

    // TODO: Remove conversation hints, rename methods.
    static void demo_Era4() {
        System.out.println("\n══ ERA 4: JSpecify @NullMarked ══");

        // @NullMarked is declared at the class level above.
        // All parameters and return types are non-null by default.
        // Only @Nullable fields need explicit marking.

        Order order = new Order("Mike", null); // @Nullable Coffee — valid

        // @Nullable on coffee — NullAway warns if we dereference without checking
        Coffee coffee = order.coffee();
        if (coffee == null) {
            System.out.println("@Nullable  : caught — coffee absent, handled cleanly");
        } else {
            // @Nullable on brewingInstructions — must check before calling toUpperCase()
            String instr = coffee.brewingInstructions();
            String result = (instr != null) ? instr.toUpperCase() : "No instructions";
            System.out.println("@Nullable  : " + result);
        }

        // Chandra/ Mala:
        // "What JSpecify got right: one standard, industry backing, generics support,
        //  @NullMarked eliminates @NonNull noise, NullAway gives build enforcement.
        //
        //  What JSpecify still doesn't give us: language-level guarantees.
        //  It's still annotations. Still conventions. Still requires NullAway config.
        //  A developer who doesn't add NullAway to the build gets documentation,
        //  not enforcement. The annotations are only as strong as the tooling around them.
        //
        //  The gap that remains: null restriction in the TYPE SYSTEM itself.
        //  Not in annotations. Not in tools. In the compiler and JVM.
        //  That's what Valhalla is for. But that's Demo 5."
    }

    public static void main(String[] args) {
        demo_Era1();
        demo_Era3();
        demo_Era4();

        // Chandra/ Mala:
        // "The four eras in one sentence each:
        //
        //  Era 1 — Null checks:  always worked, never scaled, communicated nothing.
        //  Era 2 — JSR-305:      right idea, wrong execution, died from fragmentation.
        //  Era 3 — Optional:     made absence a type, misused immediately, helps at boundaries.
        //  Era 4 — JSpecify:     industry consensus finally, non-null by default,
        //                        enforcement through NullAway, still annotation-based.
        //
        //  Each era fixed what the previous one missed.
        //  Each era left one gap that the next era tried to close.
        //  That's not failure. That's how standards evolve under real constraints.
        //
        //  Now — given all four eras — which one do YOU use on Monday morning?
        //  That's Demo 4."
    }
}