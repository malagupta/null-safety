package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DEMO 6 — HOW MODERN JAVA ADDRESSES NULL                      [INSERT SLOT]
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * OPENING SCRIPT — deliver this before touching the keyboard:
 *
 * "I want to ask you something uncomfortable.
 *
 *  How many of you are on Java 17 or above in production?
 *
 *  [wait for hands]
 *
 *  Keep your hand up if you're using records.
 *
 *  [hands drop]
 *
 *  Keep your hand up if you're using sealed classes.
 *
 *  [more hands drop]
 *
 *  Keep your hand up if you're using pattern matching in switch.
 *
 *  [most hands drop]
 *
 *  Here's the thing. Every feature I just named was designed — at least partly —
 *  to reduce null surface area in Java. Not as a side effect. As a design goal.
 *
 *  You upgraded the runtime. You didn't upgrade the code.
 *  You're paying for a sports car and driving it in first gear.
 *
 *  Let me show you what second gear looks like."
 */
@NullMarked
public class Demo6_ModernJavaAndNull {

    // ─────────────────────────────────────────────────────────────────────
    // THE SETUP — what the old code looked like
    // ─────────────────────────────────────────────────────────────────────
    //
    // SCRIPT:
    // "Let's start with something every one of you has written.
    //  A data class. A coffee order. Pre-Java-16 style.
    //  I want you to look at this and count the null opportunities."

    // ── Pre-Java-16: classic data class ──────────────────────────────────
    static class CoffeeOrder_Old {
        private String customerName;  // null?
        private String coffeeName;    // null?
        private String origin;        // null?
        private Integer temperature;  // null?  Integer, not int — unboxing trap
        private String  milkType;     // null?  no milk = null or "none"?
        private String  promoCode;    // null?  always null for most customers

        // Six fields. Six potential nulls. All invisible from the outside.
        // The caller has no idea which ones are safe to dereference.
        // Getters return the raw values — null travels freely.

        String getCustomerName() { return customerName; }
        String getCoffeeName()   { return coffeeName;   }
        String getOrigin()       { return origin;        }
        Integer getTemperature() { return temperature;   }
        String getMilkType()     { return milkType;      }
        String getPromoCode()    { return promoCode;     }
    }

    static void demo_OldStyle() {
        System.out.println("══ Old Style: six fields, six silent nulls ══");

        CoffeeOrder_Old order = new CoffeeOrder_Old();
        // Everything is null. Compiler is fine with this. Runtime is not.

        System.out.println("Name  : " + order.getCustomerName()); // null — no crash yet
        System.out.println("Coffee: " + order.getCoffeeName());   // null — no crash yet

        try {
            // This is the moment of reckoning
            System.out.println(order.getCoffeeName().toUpperCase()); // 💥 NPE
        } catch (NullPointerException e) {
            System.out.println("💥 NPE — and nobody warned us. Not the compiler.");
            System.out.println("   Not the IDE. Not the type system.");
            System.out.println("   Just the runtime. At 2am. In production.");
        }

        // SCRIPT:
        // "Six fields. All null by default. The compiler saw nothing wrong.
        //  This compiled. This was pushed. This ran in production.
        //
        //  Now let me show you what the same model looks like
        //  when you use what Java has given you since 2021."
    }

    // ═════════════════════════════════════════════════════════════════════
    // MODERN FEATURE 1 — Records (Java 16)
    // "Immutability by default. Null stopped at the door."
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "Records. Java 16. I know what you're thinking —
    //  'records are just DTOs with less boilerplate.'
    //
    //  You're right. And that undersells them completely.
    //
    //  A record has two properties that directly address null.
    //  One: every field is final. You cannot set it to null after construction.
    //  Two: the compact constructor lets you validate ALL fields at the point of creation.
    //  Not later. Not in a service method three layers deep. RIGHT THERE.
    //
    //  The bad object cannot exist. Let me show you what that means."

    record Coffee(String name, String origin, int shots) {
        // Compact constructor — validation happens before the record is created
        Coffee {
            // SCRIPT:
            // "This runs before the fields are assigned.
            //  If name is null — NPE fires HERE. At the call site.
            //  Not in a service. Not in a controller. HERE.
            //  The stack trace points directly at whoever passed null.
            //  No detective work. No reproducing in local. Just: you passed null. Fix it."
            Objects.requireNonNull(name,   "Coffee needs a name — 'null blend' is not a thing");
            Objects.requireNonNull(origin, "Coffee needs an origin — we're not hiding where this came from");
            if (shots < 1 || shots > 4) throw new IllegalArgumentException(
                    "Shots must be 1-4. This is a coffee shop, not a chemistry lab.");
        }
    }

    // SCRIPT:
    // "And notice — name and origin are String. Not String with @NonNull.
    //  Not String with a comment saying 'do not pass null.'
    //  Under @NullMarked — which is on this class — String IS non-null.
    //  The annotation isn't noise. It's the default. Silence means safety."

    static void demo_Records() {
        System.out.println("\n══ Modern Feature 1: Records ══");

        // ✅ Valid construction — all fields present and correct
        Coffee espresso = new Coffee("Espresso", "Ethiopian", 2);
        System.out.println("Valid   : " + espresso.name()
                + " from " + espresso.origin()
                + " — " + espresso.shots() + " shots");

        // 💥 Invalid — caught IMMEDIATELY at construction
        try {
            Coffee mystery = new Coffee(null, "Unknown", 1);
        } catch (NullPointerException e) {
            System.out.println("Null    : " + e.getMessage());
            // "'Coffee needs a name — null blend is not a thing'"
            // SCRIPT:
            // "Read that message. Whoever wrote the test that hit this
            //  knows exactly what to fix. No ambiguity. No stack trace archaeology."
        }

        // 💥 Invalid business rule — caught at construction too
        try {
            Coffee overdose = new Coffee("Espresso", "Ethiopian", 7);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid : " + e.getMessage());
        }

        // Records are immutable — once valid, always valid
        // SCRIPT:
        // "After construction, espresso.name() will NEVER return null.
        //  The field is final. Nobody can set it to null after the fact.
        //  No setter. No reflection trick that slips through in a test.
        //  Final means final.
        //
        //  Compare that to CoffeeOrder_Old — where any code with a reference
        //  could call setName(null) at any time and break everyone downstream.
        //
        //  Records make the valid state permanent. That's not a small thing."
    }

    // ═════════════════════════════════════════════════════════════════════
    // MODERN FEATURE 2 — Sealed Classes (Java 17)
    // "Eliminate null returns. Replace with explicit states."
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "Second feature. Sealed classes. Java 17.
    //  This one addresses failure mode two from earlier —
    //  the method signature that lies about whether it returns null.
    //
    //  Watch what happens when a lookup method's return type
    //  tells the whole truth."

    // The old way — null as a return value signal
    //   static Coffee find(String name) { return null; } // found? not found? who knows.

    // The modern way — sealed type carries the full contract
    sealed interface OrderResult
            permits OrderResult.Ready, OrderResult.Pending, OrderResult.Unavailable {

        record Ready(Coffee coffee, String station)     implements OrderResult {}
        record Pending(String customerName, int queuePosition) implements OrderResult {}
        record Unavailable(String coffeeName, String reason)   implements OrderResult {}
    }

    // SCRIPT:
    // "Three outcomes. All explicit. No null.
    //  The METHOD SIGNATURE now tells you every possible state.
    //  You cannot receive a null from this method.
    //  You cannot forget to handle 'not found' — the compiler won't let you."

    static OrderResult processOrder(String customerName, String coffeeName) {
        if (coffeeName.equals("coldpresso"))
            return new OrderResult.Unavailable(coffeeName, "Not on our menu — yet");
        if (customerName.equals("VIP"))
            return new OrderResult.Ready(
                    new Coffee("Espresso", "Ethiopian", 2), "Counter 1 — priority lane");
        return new OrderResult.Pending(customerName, 3);
    }

    static void demo_SealedClasses() {
        System.out.println("\n══ Modern Feature 2: Sealed Classes ══");

        List<OrderResult> results = List.of(
                processOrder("Alice",  "espresso"),
                processOrder("VIP",    "latte"),
                processOrder("Bob",    "coldpresso")
        );

        results.forEach(result -> {
            // SCRIPT:
            // "This switch must cover ALL cases. The compiler enforces it.
            //  Add a new sealed subtype and forget to handle it here?
            //  Compile error. Not a runtime NPE. Not a missing branch silently returning null.
            //  A compile error. Before the code ships."
            String message = switch (result) {
                case OrderResult.Ready      r -> "✅ Ready: "    + r.coffee().name()
                                                + " at " + r.station();
                case OrderResult.Pending    p -> "⏳ Pending: "  + p.customerName()
                                                + " — position " + p.queuePosition();
                case OrderResult.Unavailable u -> "❌ Sorry: "   + u.coffeeName()
                                                + " — " + u.reason();
                // No default needed — compiler verifies exhaustiveness
                // This is the compile-time guarantee null never gave you
            };
            System.out.println(message);
        });

        // SCRIPT:
        // "Three results. Three branches. Zero null. Zero NPE risk.
        //
        //  And here's the thing nobody mentions about sealed classes —
        //  they make your domain model HONEST.
        //  Unavailable isn't null. It's a first-class state with a reason.
        //  Pending isn't null. It's a queue position and a customer name.
        //
        //  null never had a reason. null never had a queue position.
        //  null was just... nothing. And nothing is hard to debug."
    }

    // ═════════════════════════════════════════════════════════════════════
    // MODERN FEATURE 3 — Pattern Matching instanceof (Java 16)
    // "Null-safe by definition. One expression. No cast."
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "Third feature. Pattern matching instanceof. Java 16.
    //  This one I want to show you because it has a property
    //  that I think most developers don't realise is there:
    //
    //  instanceof on null ALWAYS returns false.
    //  Always. Without exception. Without NPE. Without a null check first.
    //
    //  That sounds obvious. Watch what it means in practice."

    sealed interface DrinkItem permits HotDrink, ColdDrink {}
    record HotDrink(Coffee coffee, int tempC) implements DrinkItem {}
    record ColdDrink(Coffee coffee, int iceLevel) implements DrinkItem {}

    static void describe(DrinkItem item) {

        // Old style — three lines, one cast, one redundant variable
        if (item instanceof HotDrink) {
            HotDrink hd = (HotDrink) item;  // redundant cast
            System.out.println("Old  : Hot — " + hd.coffee().name() + " at " + hd.tempC() + "°C");
        }

        // SCRIPT:
        // "Three lines. A redundant cast. A new variable.
        //  The kind of boilerplate Java developers have written since 2004.
        //  Now watch the same logic with pattern matching:"

        // Pattern matching — one expression, null-safe, no cast
        if (item instanceof HotDrink hd) {
            System.out.println("New  : Hot — " + hd.coffee().name() + " at " + hd.tempC() + "°C");
        } else if (item instanceof ColdDrink cd) {
            System.out.println("New  : Cold — " + cd.coffee().name() + " ice level " + cd.iceLevel());
        }

        // Guard patterns — Java 21
        if (item instanceof HotDrink hd && hd.tempC() > 85) {
            System.out.println("Guard: " + hd.coffee().name() + " is served VERY hot — warn the customer");
        }
    }

    static void demo_PatternMatching() {
        System.out.println("\n══ Modern Feature 3: Pattern Matching ══");

        Coffee latte = new Coffee("Latte", "Colombian", 1);
        DrinkItem hot  = new HotDrink(latte, 90);
        DrinkItem cold = new ColdDrink(new Coffee("Cold Brew", "Rwandan", 1), 3);

        describe(hot);
        describe(cold);

        // SCRIPT:
        // "Now — the null safety property I mentioned."
        System.out.println();

        DrinkItem maybeNull = null;

        // This does NOT throw NPE — instanceof on null = false, always
        if (maybeNull instanceof HotDrink hd) {
            System.out.println("Should not print — null instanceof = false");
        } else {
            System.out.println("Null : instanceof null = false. Always. No NPE. No null check needed.");
        }

        // SCRIPT:
        // "Think about what you just saw.
        //  In the old world — you'd write:
        //    if (item != null && item instanceof HotDrink)
        //  Two conditions. The first one is always null-defensive noise.
        //
        //  Pattern matching instanceof removes the noise.
        //  Not because it's clever. Because null instanceof ANYTHING is false.
        //  It's in the JLS. It's been there since Java 1.
        //  Pattern matching just makes it impossible to forget."
    }

    // ═════════════════════════════════════════════════════════════════════
    // MODERN FEATURE 4 — Switch Expressions + Null Case (Java 14 / 21)
    // "null is no longer silent. It has a case."
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "Fourth feature. Switch expressions and the null case.
    //  This one has a history I want to share because it illustrates
    //  exactly how long Java took to take null seriously.
    //
    //  Before Java 14 — switch on a null reference threw NPE
    //  before any case was evaluated. Silently. Before your code ran.
    //  Your switch statement never got a chance to handle it.
    //
    //  Java 14 gave us switch expressions — arrow syntax, returns a value.
    //  Java 21 went further — null is now an explicit case.
    //  The language stopped pretending null doesn't exist."

    static String describeSize(@Nullable String size) {

        // Pre-Java-14: switch on null = NPE before any case runs
        // switch (size) { case "small": ... }  // 💥 if size is null

        // Java 21 — null as an explicit case
        return switch (size) {
            case null          -> "Customer didn't specify — defaulting to regular";
            case "small"       -> "Small — 8oz. Enough caffeine to start. Barely.";
            case "regular"     -> "Regular — 12oz. The sensible choice.";
            case "large"       -> "Large — 16oz. We've all been there.";
            case "extra-large" -> "Extra large — 20oz. Everything okay at home?";
            default            -> "Unknown size '" + size + "' — we'll make it regular";
        };
    }

    // SCRIPT:
    // "And with sealed classes — no default needed. Compiler verifies all cases."
    static String describeResult(OrderResult result) {
        return switch (result) {
            case OrderResult.Ready      r -> "Order ready at " + r.station();
            case OrderResult.Pending    p -> "Position " + p.queuePosition() + " in queue";
            case OrderResult.Unavailable u -> "Unavailable: " + u.reason();
            // No default — exhaustive by sealed type contract
            // Every possible OrderResult is handled. Compiler verified.
        };
    }

    static void demo_SwitchExpressions() {
        System.out.println("\n══ Modern Feature 4: Switch + Null Case ══");

        // null case — language handles it, no NPE
        System.out.println(describeSize(null));           // null case fires
        System.out.println(describeSize("large"));        // matches case
        System.out.println(describeSize("venti"));        // default fires

        System.out.println();

        // Exhaustive switch on sealed type
        System.out.println(describeResult(processOrder("Alice", "espresso")));
        System.out.println(describeResult(processOrder("Bob",   "coldpresso")));

        // SCRIPT:
        // "Two things happened here that deserve attention.
        //
        //  First — null had its own case. The language acknowledged its existence.
        //  Not a workaround. Not a null check before the switch.
        //  A case. Like any other value. With a label and a result.
        //
        //  Second — the sealed switch had no default clause.
        //  The compiler knows every subtype of OrderResult.
        //  It verified every case is handled. If I add a new subtype tomorrow —
        //  this switch is a compile error until I handle it.
        //
        //  Think about what that means for null bugs specifically.
        //  The 'I forgot to handle the empty case' bug — which is null's
        //  most common disguise — is now a compile error.
        //  Not a code review catch. Not a test failure. A compile error."
    }

    // ═════════════════════════════════════════════════════════════════════
    // MODERN FEATURE 5 — The combination that removes null entirely
    // "When you compose all four features, null has nowhere to hide."
    // ═════════════════════════════════════════════════════════════════════
    //
    // SCRIPT:
    // "I've shown you four features individually. Now I want to show you
    //  what happens when you combine them. Because the individual features
    //  are useful. The combination is transformative.
    //
    //  Here is a complete order processing flow.
    //  Old style first. Then modern. Side by side.
    //  Count the null opportunities in each version."

    // ── Old style — null everywhere ───────────────────────────────────────
    static String processOrder_Old(CoffeeOrder_Old order) {
        if (order == null) return "No order";                       // null check 1
        if (order.getCoffeeName() == null) return "No coffee";      // null check 2
        if (order.getCustomerName() == null) return "No customer";  // null check 3
        if (order.getTemperature() == null) return "No temperature"; // null check 4

        String coffee = order.getCoffeeName().toUpperCase();        // safe? maybe.
        String name   = order.getCustomerName().toUpperCase();      // safe? maybe.
        int    temp   = order.getTemperature();                     // unboxing — safe? maybe.

        // SCRIPT:
        // "Four null checks before a single line of business logic.
        //  And we're still not sure about the unboxing on line three.
        //  This isn't defensive programming. This is fear-driven programming."

        return name + " ordered " + coffee + " at " + temp + "°C";
    }

    // ── Modern style — records + sealed + pattern matching ────────────────
    static String processOrder_Modern(OrderResult result) {
        // Pattern matching in switch — exhaustive, null-safe, no defensive checks
        return switch (result) {
            case OrderResult.Ready r ->
                    r.coffee().name().toUpperCase()          // Coffee is non-null — record guarantees it
                    + " ready at " + r.station();           // station is non-null — record guarantees it
            case OrderResult.Pending p ->
                    p.customerName().toUpperCase()           // non-null — record guarantees it
                    + " — position " + p.queuePosition();
            case OrderResult.Unavailable u ->
                    "Sorry — " + u.coffeeName()
                    + ": " + u.reason();
        };
        // SCRIPT:
        // "Zero null checks. Zero defensive guards. Zero NPE risk.
        //  Not because we're being reckless — because the TYPE SYSTEM
        //  has already guaranteed that every field that reaches this switch
        //  is non-null. The record compact constructor rejected the bad object.
        //  The sealed type made every case explicit. Pattern matching
        //  dispatched to the right branch. The combination did the work."
    }

    static void demo_Combination() {
        System.out.println("\n══ Modern Feature 5: The Combination ══");

        // Old style — null fields, null checks, fragile
        CoffeeOrder_Old oldOrder = new CoffeeOrder_Old();
        // deliberately leave most fields null
        System.out.println("Old: " + processOrder_Old(oldOrder));

        // Modern style — sealed result, record fields, zero null checks
        OrderResult ready   = processOrder("Alice",  "espresso");
        OrderResult pending = processOrder("Bob",    "latte");
        OrderResult missing = processOrder("Carol",  "coldpresso");

        System.out.println("New: " + processOrder_Modern(ready));
        System.out.println("New: " + processOrder_Modern(pending));
        System.out.println("New: " + processOrder_Modern(missing));

        // SCRIPT:
        // "Same business problem. Two implementations.
        //
        //  Old: four null checks, implicit assumptions, unboxing trap.
        //  New: zero null checks, explicit states, compiler-verified exhaustiveness.
        //
        //  The null didn't go away. It just got handled at the right layer —
        //  at construction, at the type boundary, at the switch expression —
        //  instead of scattered across every method that touches the data.
        //
        //  That is the shift modern Java is offering you.
        //  Not a new library. Not a new framework. The language itself."
    }

    // ═════════════════════════════════════════════════════════════════════
    // THE CLOSING MOMENT
    // ═════════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        demo_OldStyle();
        demo_Records();
        demo_SealedClasses();
        demo_PatternMatching();
        demo_SwitchExpressions();
        demo_Combination();

        System.out.println();
        System.out.println("══ What each feature took away from null ══");
        System.out.println();
        System.out.println("Records (Java 16)");
        System.out.println("  Took away : null fields in valid objects");
        System.out.println("  How       : compact constructor rejects null at construction");
        System.out.println("  Null moves: from silent field → immediate NPE at the call site");
        System.out.println();
        System.out.println("Sealed classes (Java 17)");
        System.out.println("  Took away : null as a return value signal");
        System.out.println("  How       : every state is an explicit named type");
        System.out.println("  Null moves: from implicit absence → Unavailable, Pending, NotFound");
        System.out.println();
        System.out.println("Pattern matching instanceof (Java 16)");
        System.out.println("  Took away : null check before instanceof");
        System.out.println("  How       : instanceof null is always false — language spec");
        System.out.println("  Null moves: from defensive check → implicit safety");
        System.out.println();
        System.out.println("Switch + null case (Java 21)");
        System.out.println("  Took away : NPE before switch evaluates any case");
        System.out.println("  How       : null is now a first-class case label");
        System.out.println("  Null moves: from silent crash → explicit handled case");
        System.out.println();

        // SCRIPT — deliver this without looking at the screen:
        //
        // "You upgraded to Java 21. That was the easy part.
        //
        //  The hard part is looking at a class like CoffeeOrder_Old —
        //  which is in your codebase right now, with a different class name —
        //  and replacing it with a record.
        //
        //  And looking at a service method that returns null for 'not found'
        //  and replacing it with a sealed type that has an explicit NotFound case.
        //
        //  And looking at an instanceof check followed by a cast
        //  and collapsing it into one pattern matching expression.
        //
        //  None of these changes are difficult. Any of them takes an afternoon.
        //  All of them together — across a module — takes a sprint.
        //
        //  And every one of them moves the null conversation from
        //  'did you remember to check?' to 'the compiler already checked.'
        //
        //  The question isn't whether Java gives you the tools.
        //  Java 21 absolutely gives you the tools.
        //
        //  The question is whether you're going to pick them up."
    }
}