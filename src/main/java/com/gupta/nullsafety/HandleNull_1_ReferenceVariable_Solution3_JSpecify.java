package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

// ─────────────────────────────────────────────────────────────────────────────
// @NullMarked — the single most important annotation in this file.
//
// It declares: "everything in this class is non-null BY DEFAULT".
// Every parameter, every return type, every field — non-null unless stated otherwise.
//
// The result: @NonNull disappears from the entire codebase.
// You only write @Nullable — for the deliberate, meaningful exceptions.
//
// With JetBrains annotations you wrote:
//     record Coffee(@NotNull String name, @NotNull String origin, int shots)
//
// With JSpecify @NullMarked you write:
//     record Coffee(String name, String origin, int shots)
//
// Same contract. Half the noise. All the enforcement.
// ─────────────────────────────────────────────────────────────────────────────
@NullMarked
public class HandleNull_1_ReferenceVariable_Solution3_JSpecify {

    // ── Records ───────────────────────────────────────────────────────────────
    // No @NonNull anywhere — @NullMarked covers the whole class.
    // name and origin are non-null by default. No annotation needed.
    // Objects.requireNonNull stays — it's the runtime enforcement layer.
    // Annotations catch bugs at compile time. requireNonNull catches what slips through.

    record Coffee(String name, String origin, int shots) {
        Coffee {
            Objects.requireNonNull(name,   "Coffee must have a name");
            Objects.requireNonNull(origin, "Coffee must have an origin");
        }
    }

    record Order(String customerName, @Nullable Coffee coffee) {
        // customerName — non-null by @NullMarked default. No annotation needed.
        // coffee       — @Nullable is the ONLY annotation in this record.
        //                One annotation. One deliberate exception. Reads clearly:
        //                "everything here is non-null, except coffee — absence is valid."
        Order {
            Objects.requireNonNull(customerName, "Order must have a customer name");
        }
    }

    // ── Optional return types — no annotation needed ──────────────────────────
    //
    // Under @NullMarked, Optional return types need no annotation at all:
    //   - The Optional itself is non-null by default (no null Optional)
    //   - The presence/absence of the value inside is Optional's own contract
    //
    // Compare with JetBrains version which needed @NotNull on the parameter:
    //   static Optional<Coffee> getCoffee(@NotNull Order order)  ← JetBrains
    //   static Optional<Coffee> getCoffee(Order order)           ← JSpecify @NullMarked

    static Optional<Coffee> getCoffee(Order order) {
        return Optional.ofNullable(order.coffee());
    }

    static Optional<String> getCoffeeName(Order order) {
        return getCoffee(order).map(Coffee::name);
    }

    static Optional<String> getCoffeeOrigin(Order order) {
        return getCoffee(order).map(Coffee::origin);
    }

    // ── Methods with nullable parameters — only @Nullable needed ─────────────
    //
    // The return types (String) are non-null by @NullMarked default — no annotation.
    // Only the parameters that accept null are annotated — and only with @Nullable.
    // This is the entire annotation story for these methods. One annotation each.

    static String resolveCustomerName(@Nullable String name) {
        return Objects.requireNonNullElse(name, "Guest");
    }

    static String resolveBrewingTemp(@Nullable String configuredTemp) {
        return Objects.requireNonNullElseGet(configuredTemp,
                () -> Objects.requireNonNullElse(System.getenv("DEFAULT_BREW_TEMP"), "90°C"));
    }

    static void main(String[] args) {

        // =====================================================================
        // 1.1 — Uninitialised reference: coffee is absent
        // =====================================================================
        //
        // @Nullable on coffee in Order tells IntelliJ and NullAway:
        // order.coffee() might be null — dereference directly and the build fails.

        Order order = new Order("Jonathan", null);

        System.out.println(order.coffee()); // prints null — no crash

        String coffeeName = getCoffee(order)
                .map(Coffee::name)
                .orElse("No coffee selected yet — Jonathan, what would you like?");

        System.out.println(coffeeName);

        // =====================================================================
        // 1.2 — Chained call on null
        // =====================================================================

        String announcement = getCoffeeName(order)
                .map(String::toUpperCase)
                .orElse("(no coffee on this order)");

        System.out.println("I ordered: " + announcement);

        // =====================================================================
        // 1.3 — Object exists, but a field inside is null
        // =====================================================================
        //
        // Under @NullMarked, passing null to Coffee's non-null parameter:
        //   IntelliJ: "Passing null argument to non-null parameter"
        //   NullAway: build failure
        //   Runtime:  "Coffee must have an origin" (requireNonNull fires)

        try {
            Coffee badCoffee = new Coffee("Espresso", null, 1);
        } catch (NullPointerException e) {
            System.out.println("Caught at construction: " + e.getMessage());
        }

        // ── Default value fallback — requireNonNullElse ───────────────────────

        record CoffeeWithFallback(String name, String origin, int shots) {
            CoffeeWithFallback {
                Objects.requireNonNull(name, "Coffee must have a name");
                origin = Objects.requireNonNullElse(origin, "Unknown origin");
            }
        }

        CoffeeWithFallback mysteryBlend =
                new CoffeeWithFallback("Mystery Blend", null, 1);

        System.out.println("My coffee's origin is: " + mysteryBlend.origin().toUpperCase());

        System.out.println("Brewing at: " + resolveBrewingTemp(null));
        System.out.println("Customer  : " + resolveCustomerName(null));

        // =====================================================================
        // The happy path
        // =====================================================================

        Order mikeOrder = new Order("Mike", new Coffee("Espresso", "Ethiopian", 1));

        String mikeAnnouncement = getCoffeeName(mikeOrder)
                .map(String::toUpperCase)
                .orElse("(no coffee)");

        String mikeOrigin = getCoffeeOrigin(mikeOrder)
                .map(String::toUpperCase)
                .orElse("(origin unknown)");

        System.out.println("I ordered  : " + mikeAnnouncement);
        System.out.println("It's from  : " + mikeOrigin);

        // =====================================================================
        // JSpecify vs JetBrains — the annotation count tells the story
        // =====================================================================
        //
        //  JetBrains version had:
        //    @NotNull String name         ← on every non-null field
        //    @NotNull String origin       ← on every non-null parameter
        //    @NotNull Order order         ← on every non-null method param
        //    @NotNull String resolveX(..) ← on every non-null return type
        //    @Nullable Coffee coffee      ← on the one nullable field
        //
        //  JSpecify version has:
        //    @NullMarked                  ← once, on the class
        //    @Nullable Coffee coffee      ← once, on the one nullable field
        //    @Nullable String name        ← on parameters that accept null
        //
        //  Same contract. Same enforcement. Fraction of the annotation noise.
        //
        //  The rule: with @NullMarked, you only ever write @Nullable.
        //            @NonNull is the silence. @Nullable breaks the silence.
        //            Read the absence of annotation as a guarantee, not an oversight.


        
    }

    /*

    To get the full benefit — static analysis that actually enforces the contracts at build time — pair it with NullAway:
        xml<plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.uber.nullaway</groupId>
                        <artifactId>nullaway</artifactId>
                        <version>0.10.25</version>
                    </path>
                </annotationProcessorPaths>
                <compilerArgs>
                    <arg>-XepOpt:NullAway:AnnotatedPackages=com.gupta.sessioncode</arg>
                    <arg>-XepOpt:NullAway:JSpecifyMode=true</arg>
                </compilerArgs>
            </configuration>
        </plugin>
        The JSpecifyMode=true flag tells NullAway to treat @NullMarked
        as the default — without it, NullAway won't fully honour the
        JSpecify semantics. That one flag is the difference between
        annotations as documentation and annotations as enforced contracts.

     */
}