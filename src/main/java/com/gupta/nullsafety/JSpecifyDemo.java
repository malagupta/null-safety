package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * JSpecify demo using Coffee and Order.
 *
 * Three things to understand before reading the code:
 *
 * @NullMarked  — every parameter, field, and return type in this scope
 *                is non-null by default. Silence means non-null.
 *
 * @Nullable    — this specific field, parameter, or return type
 *                may be null. It breaks the @NullMarked silence.
 *
 * @NullUnmarked — opt a class or method OUT of @NullMarked scope.
 *                 Used for legacy code or unannotated third-party bridges.
 *
 * NullAway (build plugin) turns these into compile errors, not squiggles.
 * Without NullAway the annotations are documentation. With it — enforcement.
 *
 * Maven dependency:
 *   <dependency>
 *       <groupId>org.jspecify</groupId>
 *       <artifactId>jspecify</artifactId>
 *       <version>1.0.0</version>
 *   </dependency>
 *
 * NullAway build config (pom.xml):
 *   <arg>-XepOpt:NullAway:AnnotatedPackages=com.gupta.session</arg>
 *   <arg>-XepOpt:NullAway:JSpecifyMode=true</arg>
 *   <arg>-XepOpt:NullAway:OnlyNullMarked=true</arg>
 */
@NullMarked   // ← everything in this class is non-null by default
public class JSpecifyDemo {

    // ─────────────────────────────────────────────────────────────────────
    // THE RECORDS
    //
    // Without @NullMarked these are plain records — no null contract expressed.
    // Inside @NullMarked scope, every component is non-null unless marked @Nullable.
    // ─────────────────────────────────────────────────────────────────────

    // ── Version 1: no annotations ─────────────────────────────────────────
    //
    // Valid Java. But the null contract is invisible.
    // Is brewingInstructions required? Can coffee be absent? Nobody knows.
    // The compiler doesn't know. NullAway doesn't know. The next developer doesn't know.

    record Coffee(String name, String origin, String brewingInstructions) {}

    record Order(String customerName, Coffee coffee) {}


    // ── Version 2: with @Nullable where absence is valid ──────────────────
    //
    // @NullMarked makes non-null the default — no annotation needed on name/origin.
    // @Nullable on brewingInstructions and coffee breaks the silence deliberately.
    // Now the contract is in the type. No comment needed.

    record CoffeeV2(
            String name,                              // non-null — @NullMarked default
            String origin,                            // non-null — @NullMarked default
            @Nullable String brewingInstructions      // nullable — new items may lack instructions
    ) {}

    record OrderV2(
            String customerName,                      // non-null — @NullMarked default
            @Nullable CoffeeV2 coffee                 // nullable — customer may not have chosen yet
    ) {}


    // ─────────────────────────────────────────────────────────────────────
    // DEMO 1 — @Nullable on a field vs non-null default
    //
    // What NullAway enforces:
    //   passing null for a non-null parameter  → compile error
    //   passing null for a @Nullable parameter → allowed
    //   dereferencing a @Nullable without check → compile error
    // ─────────────────────────────────────────────────────────────────────

    static void demo1_FieldAnnotations() {
        System.out.println("── Demo 1: @Nullable field vs non-null default ──");

        // ✅ All fields provided
        CoffeeV2 espresso = new CoffeeV2("Espresso", "Ethiopian", "Grind fine. 90°C.");
        System.out.println("Full    : " + espresso.name() + " / " + espresso.brewingInstructions());

        // ✅ brewingInstructions is @Nullable — null accepted
        Coffee newBlend = new Coffee("New Blend", "Rwandan", null);
        System.out.println("No instr: " + newBlend.name() + " / instructions=" + newBlend.brewingInstructions());

        // ✅ coffee is @Nullable on Order — customer exists, choice pending
        OrderV2 pending = new OrderV2("Jonathan", null);
        System.out.println("Pending : " + pending.customerName() + " / coffee=" + pending.coffee());

        // With NullAway, this would be a COMPILE ERROR:
        //   CoffeeV2 bad = new CoffeeV2(null, "Ethiopian", null);
        //                               ↑
        //   [NullAway] passing @Nullable to @NonNull parameter 'name'

        // With NullAway, this would also be a COMPILE ERROR:
        //   String instructions = newBlend.brewingInstructions().toUpperCase();
        //                                                        ↑
        //   [NullAway] dereferencing @Nullable value without null check
    }


    // ─────────────────────────────────────────────────────────────────────
    // DEMO 2 — @Nullable on return types
    //
    // The return type annotation is the most important one.
    // It is the contract every caller sees at the call site.
    // ─────────────────────────────────────────────────────────────────────

    private static final Map<String, CoffeeV2> MENU = Map.of(
            "espresso", new CoffeeV2("Espresso", "Ethiopian", "Grind fine. 90°C."),
            "latte",    new CoffeeV2("Latte",     "Colombian", "Shot + steamed milk."),
            "newblend", new CoffeeV2("New Blend", "Rwandan",    null)
    );

    // @Nullable return type — null when not found.
    // Every caller MUST null-check before dereferencing. NullAway enforces this.
    static @Nullable CoffeeV2 findByName(@Nullable String name) {
        if (name == null) return null;   // @Nullable param — must handle it
        return MENU.get(name);           // Map.get() returns null — that is our contract
    }

    // Non-null return type (no @Nullable) — never returns null.
    // Caller can dereference directly. NullAway trusts the contract.
    static CoffeeV2 findByNameOrDefault(String name, CoffeeV2 fallback) {
        return Objects.requireNonNullElse(MENU.get(name), fallback);
    }

    // Optional return — when absence is meaningful enough to model explicitly
    static Optional<CoffeeV2> findByNameOptional(String name) {
        return Optional.ofNullable(MENU.get(name));
    }

    static void demo2_ReturnTypes() {
        System.out.println("\n── Demo 2: @Nullable on return types ──");

        // @Nullable return — caller must check
        CoffeeV2 result = findByName("espresso");
        if (result != null) {                            // NullAway requires this check
            System.out.println("Found   : " + result.name());
        }

        CoffeeV2 missing = findByName("coldpresso");
        if (missing == null) {
            System.out.println("Missing : coldpresso not on menu");
        }

        // Non-null return — no check needed, NullAway trusts it
        CoffeeV2 fallback = new CoffeeV2("House Blend", "Unknown", null);
        CoffeeV2 safe     = findByNameOrDefault("coldpresso", fallback);
        System.out.println("Default : " + safe.name());  // safe to dereference directly

        // Optional return — absence modelled in the type
        findByNameOptional("latte")
                .ifPresentOrElse(
                    c  -> System.out.println("Optional: " + c.name()),
                    () -> System.out.println("Optional: not found")
                );
    }


    // ─────────────────────────────────────────────────────────────────────
    // DEMO 3 — @Nullable on parameters
    //
    // @NullMarked makes parameters non-null by default.
    // @Nullable on a parameter means: the caller is allowed to pass null.
    // The METHOD is then responsible for handling it.
    // ─────────────────────────────────────────────────────────────────────

    // Non-null parameter (default under @NullMarked) — caller MUST NOT pass null
    static String describeOrder(OrderV2 order) {
        // NullAway guarantees order is non-null here — no null check needed
        String coffeePart = order.coffee() != null     // coffee is @Nullable — must check
                ? order.coffee().name()
                : "not chosen yet";
        return order.customerName() + " → " + coffeePart;
    }

    // @Nullable parameter — caller MAY pass null, method owns the handling
    static String describeOrderSafely(@Nullable OrderV2 order) {
        if (order == null) return "no order";          // null check is our responsibility
        return describeOrder(order);                   // delegated — order is non-null now
    }

    static void demo3_Parameters() {
        System.out.println("\n── Demo 3: @Nullable on parameters ──");

        CoffeeV2 espresso = new CoffeeV2("Espresso", "Ethiopian", "Grind fine.");
        OrderV2  complete = new OrderV2("Alice",    espresso);
        OrderV2  pending  = new OrderV2("Jonathan", null);

        System.out.println(describeOrder(complete));    // "Alice → Espresso"
        System.out.println(describeOrder(pending));     // "Jonathan → not chosen yet"
        System.out.println(describeOrderSafely(null));  // "no order" — @Nullable param

        // With NullAway, passing null to describeOrder() would be a COMPILE ERROR:
        //   describeOrder(null);
        //   ↑ [NullAway] passing @Nullable to @NonNull parameter 'order'
    }


    // ─────────────────────────────────────────────────────────────────────
    // DEMO 4 — Chained calls and @Nullable field access
    //
    // This is where most NPEs come from in practice.
    // @Nullable annotations make the chain's weak links visible.
    // ─────────────────────────────────────────────────────────────────────

    static void demo4_ChainedAccess() {
        System.out.println("\n── Demo 4: chained access with @Nullable ──");

        CoffeeV2 newBlend = new CoffeeV2("New Blend", "Rwandan", null);
        OrderV2  order    = new OrderV2("Jonathan", newBlend);

        // ── Unsafe chain — two @Nullable fields in one expression ──
        //
        // With NullAway, this line is a COMPILE ERROR:
        //   String upper = order.coffee().brewingInstructions().toUpperCase();
        //                               ↑                      ↑
        //   coffee() is @Nullable           brewingInstructions() is @Nullable
        //   NullAway rejects dereference without check at BOTH points

        // ── Safe: check each @Nullable before dereferencing ──
        if (order.coffee() != null && order.coffee().brewingInstructions() != null) {
            String upper = order.coffee().brewingInstructions().toUpperCase();
            System.out.println("Instructions: " + upper);
        } else {
            System.out.println("Instructions: not available");
        }

        // ── Safe: Optional chain — more readable for deeper chains ──
        String instructions = Optional.ofNullable(order.coffee())
                .map(CoffeeV2::brewingInstructions)
                .map(String::toUpperCase)
                .orElse("not available");
        System.out.println("Optional chain: " + instructions);
    }


    // ─────────────────────────────────────────────────────────────────────
    // DEMO 5 — @NullUnmarked: opting legacy code out of @NullMarked scope
    //
    // Real codebases have unannotated modules, generated code, and third-party
    // bridges that cannot be annotated. @NullUnmarked opts them out cleanly
    // without disabling @NullMarked for the rest of the codebase.
    // ─────────────────────────────────────────────────────────────────────

    @NullUnmarked   // ← this class is outside @NullMarked scope
    static class LegacyCoffeeApi {

        // This method predates JSpecify. It returns null when not found.
        // @NullUnmarked means NullAway does not enforce here —
        // treat everything as potentially null, like unannotated Java always was.
        static Coffee findLegacy(String name) {
            Map<String, Coffee> store = Map.of(
                    "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine.")
            );
            return store.get(name); // null if not found — NullAway ignores this method
        }
    }

    // The adapter brings the legacy null back into @NullMarked territory
    // by wrapping it at the boundary. One place. Never again elsewhere.
    static Optional<Coffee> findFromLegacy(String name) {
        return Optional.ofNullable(LegacyCoffeeApi.findLegacy(name));
        // LegacyCoffeeApi.findLegacy returns potentially-null (unannotated)
        // Optional.ofNullable wraps it — null never crosses this boundary inward
    }

    static void demo5_NullUnmarked() {
        System.out.println("\n── Demo 5: @NullUnmarked legacy bridge ──");

        findFromLegacy("espresso")
                .ifPresentOrElse(
                    c  -> System.out.println("Legacy found   : " + c.name()),
                    () -> System.out.println("Legacy missing : espresso")
                );

        findFromLegacy("coldpresso")
                .ifPresentOrElse(
                    c  -> System.out.println("Legacy found   : " + c.name()),
                    () -> System.out.println("Legacy missing : coldpresso")
                );
    }


    // ─────────────────────────────────────────────────────────────────────
    // SUMMARY — what each annotation says, in one place
    // ─────────────────────────────────────────────────────────────────────

    static void printSummary() {
        System.out.println("\n── JSpecify annotations — what each one means ──\n");
        System.out.println("@NullMarked   on a class or package");
        System.out.println("              → every parameter, field, return type is non-null by default");
        System.out.println("              → silence means non-null");
        System.out.println("              → @Nullable breaks the silence for deliberate exceptions");
        System.out.println();
        System.out.println("@Nullable     on a field, parameter, or return type");
        System.out.println("              → this specific element may be null");
        System.out.println("              → NullAway requires a null check before dereference");
        System.out.println("              → caller is responsible for handling the absent case");
        System.out.println();
        System.out.println("@NullUnmarked on a class or method");
        System.out.println("              → this scope is outside @NullMarked");
        System.out.println("              → NullAway does not enforce here");
        System.out.println("              → used for legacy code, generated code, third-party bridges");
        System.out.println();
        System.out.println("NullAway      build plugin (not an annotation)");
        System.out.println("              → turns @NullMarked violations into compile errors");
        System.out.println("              → annotations without NullAway = documentation");
        System.out.println("              → annotations with NullAway    = enforcement");
    }


    public static void main(String[] args) {
        demo1_FieldAnnotations();
        demo2_ReturnTypes();
        demo3_Parameters();
        demo4_ChainedAccess();
        demo5_NullUnmarked();
        printSummary();
    }
}