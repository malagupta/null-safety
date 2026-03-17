package com.gupta.nullsafety;

public class HandleNull_1_ReferenceVariables {

    /*
     *
        Chandra/Mala:

        When can you say you have become a senior Java developer - when you code
        throws NPE in production. (SHOULD WE INCLUDE OR DROP THIS?)

        ((Could we get a hooter/ whistle - to signal we have a NPE))

        Chandra: Hey Rodrigo, could you please pass me my coffee. Thanks. - Hi everyone, welcome to this session (takes a sip, no coffee!)
        Mala: (Chuckles) Did you just get a NPE!
                 Did you order black coffee or cappachino? I guess they ran out of black coffee?
        Chandra: Opens lid.. no coffee -
        Mala: I guess you got another NPE ;)

        Mala: Having a null value doesn't result in a NPE.
              Dereferencing a Null does.
              What it means is - When we access any member on a null - either its property/ behavior.

        2 Ways that call NPE
        a) Calling a variable on a null value
        b) Calling a method on a null value

        So, why is that a problem -
        a) NPE is one the most common production issues
         ((While, NPE is easy to detect - Handling null values
          become unmanageable as the code base increases  (CAN WE GET SOME NUMBERS HERE) ))
          (When it goes to production.. damn.. it is an issue)

        Chandra: The other main issues is:
        b) The null flow is unpredictable - also because we have multiple layers here
            a) Caused by derefenced null reference variables - which are everywhere -
                local, instance, static, method/ constructor/ lambda parameters,
                elements of array/ other collections.
            b) These can be set null due to
                i)  malformed data,
                ii) network connectivity issues,
                iii) service failures,
                iv) thread deadlocks
                v) Coding practices.
                    1) Because it is allowed - billion dollar mistake.
                    2) Implicit assignment  (static variables/ fields - at the time of creation)
                    3) Explicitly passing null to method parameters
                    4) Enters via external boundaries - reading a file or JSON etc.
                    5) Partially created objects (the ones that have instances as reference variables)

        Mala:
        c) Uncertainity increases exponentially with the increasing codebase (WHY?)

        Chandra:
        * Agenda:
        a) We'll talk about various ways to address these situations
            i) Early solutions - that still work
            ii) Current solutions
            iii) Solutions in works - such as Valhalla
            iv) Comparing which one works better/ do you need a combination/ where/ why?
            v) What else do we have to offer you beyond this session.
     */


    record Coffee(String name, String origin, int shots) { }
    record Order(String customerName, Coffee coffee) {}

    static void main(String[] args) {

        // ===========================================================
        // 1 - reference variables
        // ===========================================================

        // -------------------------------------------------------------------------
        // 1.1 : Uninitialised instance/ static reference variable
        // -------------------------------------------------------------------------
        //
        // Chandra/ Mala:
        // Here's one of the most common scenarious - an instance of field reference variable set to null.
        // Our coffee story - You place an Order, nut the Coffee details are not set.
        // Java initialises it null by default.
        // No warning.
        // No error. Everything looks fine.
        // Until you try to drink it :)

        // 1. Create an order Reference variables - nulls
        Order order = new Order("Jonathan", null); // Or I could read that from a file (coffee value could be null)
        // null but still no NPE
        IO.println(order.coffee());
        // null dereference - throws - NPE
        // NPE is not thrown by a null.. it is thrown when we dereference a null
        // (derefernce - calling a method or a variable on null)
        IO.println(order.coffee()
                        .name());

        // null itself is not the issue. dereferencing it is.

        // -------------------------------------------------------------------------
        // 1.2 : null passed as a method argument
        // -------------------------------------------------------------------------
        //
        // Chandra/ Mala:
        // Second user case. Even more insidious. The reference is null, not because
        // we forgot to initialise it — but because someone passed null to it.
        // From another class. That we didn't write. In a codebase we inherited.
        // At 11pm. On a Friday.
        // Joke: The mistake hurts when someone else made it.

        // Which coffee are your ordering?
        System.out.println("I ordered: " + order.coffee()
                                                .name()
                                                .toUpperCase()); // NPE since coffee is null


        // -------------------------------------------------------------------------
        // 1.3 : The sneaky null — object exists, field inside is null
        // -------------------------------------------------------------------------
        order = new Order("Mike", new Coffee("Espresso", null, 1)); // Or I could read that from a file (coffee value could be null)
        // Question - where is your coffee from?
        System.out.println("My coffee's origin is: " + order.coffee()
                                                .origin()
                                                .toUpperCase()); // NPE since coffee's origin is null

        // Chandra/ Mala
        // For the senior developers in the room — this is exactly what happens with
        // JPA entities that have lazily-loaded relationships outside a transaction.
        // The entity is there. The collection field is there. But it's null
        // because Hibernate never populated it. Same shape of bug, much harder to spot."


        // Chandra/ Mala  - Summary of three flavours of null reference variables:
        //  1. Uninitialised — you didn't set it. Null assigned implicitly.
        //  2. Null method argument - null passed explicitly/ a null reference variable
        //  3. Null field    — Object exists, but a field is null.
        //
        //  In all three cases — null itself didn't crash anything.
        //  Null dereference - calling a member on null throws NPE
        //  Keep that distinction in your head. It matters in next section 2"



    }

}

/**
 *
 * 1. Lazy initialisation ✅ your first choice — most popular by far
 * The empty cup analogy. Defer expensive object creation until first use.
 * Every developer has written this. Hibernate, Spring, and the JDK itself do it internally.
 * java private Coffee freshBrew = null; // cup is empty — filled only when first ordered
 * Why it was compelling: Memory and startup time. Don't pay the cost until you need to.
 * Why it's problematic today: Thread safety, null travelling further than expected, and Optional / Supplier<T> / framework-managed singletons do it better.
 *
 * 2. Representing optional or absent domain values, or resetting values
 * This is the single most common reason developers put null in a field today. Not lazy init, not GC — just "this value might not exist."
 * javaclass Order {
 *     String  promoCode    = null; // customer may not have a promo code
 *     Coffee  selectedItem = null; // order placed but coffee not chosen yet
 *     Integer tableNumber  = null; // takeaway orders have no table
 * }
 * Why it was compelling: It's the simplest representation of "not there yet." One field, two states. No extra type needed.
 * Why it's the biggest source of NPE in production: null silently means too many things — never set, deliberately absent, expired, not applicable. Code that checks for one meaning misses the others. This is the case Optional<T> was invented to solve, and what Valhalla's nullable types address at the language level.
 * Why it's better than lazy init for the session: Every developer in the room has a promoCode or tableNumber field somewhere. It's universal. It's relatable. And the transition from String promoCode = null to Optional<String> promoCode() as a return type tells the whole null-safety story in one slide.
 *
 * 3. Garbage collection / releasing references — your second choice
 * Explicitly nulling a reference so the GC can collect the object.
 * javaprivate static EventProcessor instance = null; // signal to GC after shutdown()
 *
 * static void shutdown() {
 *     instance = null; // release — let it be collected
 * }
 * Why it was compelling: In early Java (pre-generational GC improvements), holding a reference prevented collection. Nulling fields in long-lived objects (static singletons, thread locals, servlet contexts) was a real and necessary technique.
 * Why it matters less today: Modern JVMs with generational and region-based GC (G1, ZGC, Shenandoah) are far better at detecting when objects are unreachable. The JVM usually knows before you do. The pattern survives mainly in Android development, embedded Java, and very large heap applications where memory pressure is explicit.
 * Why it's still worth showing: It demonstrates that null was sometimes a tool, not just a mistake. It earns credibility with senior developers who remember writing this deliberately.
 *
 *
 */
