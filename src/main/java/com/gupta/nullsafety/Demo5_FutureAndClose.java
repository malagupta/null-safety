package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DEMO 5 — WHERE IT IS GOING + CLOSE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ⚠️  VALHALLA SECTIONS: ILLUSTRATIVE. NOT COMPILABLE TODAY.
 *    The ! suffix is the proposed Valhalla syntax — final syntax may change.
 *
 * Chandra/ Mala:
 * "Last section. Five minutes. Where is Java taking this,
 *  and what should you do on Monday morning.
 *
 *  Everything we've covered — annotations, Optional, records, sealed types —
 *  they are all workarounds. Good workarounds. Production-grade workarounds.
 *  But they all share one limitation: they sit OUTSIDE the type system.
 *  They're conventions enforced by tools. Not guarantees enforced by the JVM.
 *
 *  Project Valhalla changes that."
 */
@NullMarked
public class Demo5_FutureAndClose {

    // ─────────────────────────────────────────────────────────────────────
    // THE VALHALLA SHIFT — same code, different enforcement layer
    // ─────────────────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Here is the same Coffee record across three eras.
    //  Watch what changes — and more importantly, what disappears."

    // TODAY with JSpecify @NullMarked
    @NullMarked
    record Coffee_Today(String name, String origin, @Nullable String brewingInstructions) {
        Coffee_Today {
            Objects.requireNonNull(name,   "Coffee must have a name");    // runtime safety net
            Objects.requireNonNull(origin, "Coffee must have an origin"); // runtime safety net
        }
        // name and origin are non-null because:
        //   @NullMarked says so (annotation convention)
        //   NullAway enforces it (build tool)
        //   requireNonNull catches what slips through (runtime)
        // Three mechanisms. One guarantee.
    }

    // [VALHALLA - FUTURE] — same record, one mechanism
    //
    // Chandra/ Mala:
    // "Under Valhalla, the same record looks like this:
    //
    //   record Coffee(String! name, String! origin, String brewingInstructions) {}
    //
    //  String! = null-restricted type. JVM enforced. No annotation needed.
    //  No requireNonNull needed. The compact constructor is empty.
    //
    //  Three mechanisms became one.
    //  The enforcement moved from 'tools around the language'
    //  to 'the language itself.'"
    //
    //   record Coffee_Valhalla(String! name, String! origin, String brewingInstructions) {
    //       // Empty. String! cannot be null. JVM says so. That's the entire contract.
    //   }

    // ─────────────────────────────────────────────────────────────────────
    // THE FULL EVOLUTION — show this as a live comparison
    // ─────────────────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // "Let me put the full arc on screen.
    //  Same method. Same intent. Different eras."

    // ── Pre-2006: no contract ─────────────────────────────────────────────

    // static Coffee findByName(String name) {
    //     return menu.get(name); // null if not found — caller has no idea
    // }
    //
    // Chandra/ Mala:
    // "No contract. The caller reads the implementation or finds out at runtime.
    //  The most common pattern in Java codebases until 2010."

    // ── 2006-2014: annotation contract, no enforcement ────────────────────

    // @Nullable
    // static Coffee findByName(@NonNull String name) {
    //     return menu.get(name); // @Nullable says it might be null
    // }
    //
    // Chandra/ Mala:
    // "The contract is visible. The IDE warns. The developer can ignore it.
    //  Adoption was voluntary. Coverage was inconsistent.
    //  Better than nothing. Not enough."

    // ── 2014-2024: Optional, contract in the type ─────────────────────────

    static Optional<Coffee_Today> findByName_Optional(String name) {
        return Optional.empty(); // absence is the TYPE now
    }
    //
    // Chandra/ Mala:
    // "The contract moved into the return type. You cannot call .name()
    //  on an Optional<Coffee> without going through the Optional API.
    //  The type system forces acknowledgement of absence.
    //  For new code you control — genuinely better.
    //  For the 20 years of existing Java code — still returns null."

    // ── 2024: JSpecify, non-null by default ───────────────────────────────
    //
    // @NullMarked on this class (see top) + @Nullable on exceptions.
    // NullAway in CI fails the build on contract violations.
    //
    // Chandra/ Mala:
    // "@NullMarked makes non-null the package default. @Nullable marks exceptions.
    //  NullAway enforces at build time — cannot push past a failing build.
    //  The contract is documented AND enforced. The gap JSR-305 never closed."

    // ── Valhalla (future): type system is the enforcement ─────────────────
    //
    // Chandra/ Mala:
    // "Valhalla closes the last gap. String! is not an annotation.
    //  It's a type. The JVM rejects null before the constructor body runs.
    //  No tool needed. No annotation needed. No discipline needed.
    //  The contract IS the type."
    //
    //   static Coffee! findByName_Valhalla(String! name) {
    //       Coffee result = menu.get(name);
    //       if (result == null) throw new CoffeeNotFoundException(name);
    //       return result;  // Coffee! — JVM-verified non-null
    //   }

    // ─────────────────────────────────────────────────────────────────────
    // WHAT TO DO MONDAY MORNING — the actionable close
    // ─────────────────────────────────────────────────────────────────────

    static void whatToDoMondayMorning() {
        System.out.println("══ Monday Morning Actions ══");
        System.out.println();

        // Chandra/ Mala:
        // "Three actions. In priority order."

        // ACTION 1 — Add @NullMarked to your packages today
        System.out.println("Action 1: Add @NullMarked to package-info.java");
        System.out.println("          One annotation per package.");
        System.out.println("          Every field, parameter, return type becomes non-null by default.");
        System.out.println("          Only @Nullable marks deliberate exceptions.");
        System.out.println("          Cost: 3 minutes. Benefit: no @NonNull noise anywhere.");
        System.out.println();

        // Chandra/ Mala:
        // "package-info.java in your package:
        //
        //   @NullMarked
        //   package com.yourcompany.coffee;
        //   import org.jspecify.annotations.NullMarked;
        //
        //  That's it. Everything in the package is now non-null by default."

        // ACTION 2 — Add NullAway to your CI build
        System.out.println("Action 2: Add NullAway to your Maven/Gradle build");
        System.out.println("          Converts @NullMarked contracts from documentation to enforcement.");
        System.out.println("          Null contract violations fail the build.");
        System.out.println("          Cannot push past a failing build. That's the point.");
        System.out.println();

        // Chandra/ Mala:
        // "The NullAway Maven config in pom.xml — two additions:
        //
        //  1. annotationProcessorPaths: NullAway 0.10.25
        //  2. compilerArgs:
        //     -XepOpt:NullAway:AnnotatedPackages=com.yourcompany
        //     -XepOpt:NullAway:JSpecifyMode=true
        //
        //  JSpecifyMode=true tells NullAway to honour @NullMarked semantics.
        //  Without it — the annotations are ignored at build time."

        // ACTION 3 — Migrate to records for domain objects
        System.out.println("Action 3: Replace data classes with records");
        System.out.println("          Compact constructors enforce invariants at construction.");
        System.out.println("          The bad object never exists.");
        System.out.println("          Null cannot hide inside a partially-constructed record.");
        System.out.println();

        System.out.println("When Valhalla ships:");
        System.out.println("  Replace String (non-null) with String!");
        System.out.println("  Remove @NullMarked — String! is the declaration.");
        System.out.println("  Remove requireNonNull — JVM rejects null before constructor runs.");
        System.out.println("  Migration is mechanical — you already documented the contracts.");
    }

    public static void main(String[] args) {
        whatToDoMondayMorning();

        System.out.println();
        System.out.println("══ The Arc ══");
        System.out.println();
        System.out.println("1965  null invented.    Tony Hoare: 'absence needs a value.'");
        System.out.println("1995  null lands in Java. Assignable to any reference type.");
        System.out.println("2006  JSR-305.           @Nullable. Right idea. No standard.");
        System.out.println("2014  Optional<T>.       Absence becomes a type. Misused immediately.");
        System.out.println("2020  Helpful NPE.       Java 14 tells you WHICH null. Finally.");
        System.out.println("2021  Records + sealed.  Null stopped at construction. Exhaustive handling.");
        System.out.println("2023  Switch null case.  Language handles null explicitly.");
        System.out.println("2024  JSpecify 1.0.      Industry agrees on one standard. Build enforcement.");
        System.out.println("Next  Valhalla.          String! — the type system says no.");
        System.out.println();

        // Chandra/ Mala:
        // "Thirty years. One problem. Incrementally better answers.
        //
        //  The direction is clear. Java is moving toward a world where
        //  null has to earn its place. Every feature since Java 8 has made
        //  that harder to ignore.
        //
        //  @NullMarked and NullAway are the state of the art today.
        //  Use them. They cost one pom.xml change and one annotation per package.
        //  They catch null contract violations when the code is written —
        //  not when it ships.
        //
        //  Valhalla makes it the language tomorrow.
        //  When it ships — and it will ship —
        //  the migration will be mechanical because you already documented the contracts.
        //
        //  Start now. The work isn't wasted. It's preparation.
        //
        //  I'll be outside if you have questions —
        //  getting a coffee that is, hopefully, not null.
        //
        //  Thank you."
    }
}