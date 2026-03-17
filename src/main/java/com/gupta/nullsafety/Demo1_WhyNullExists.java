package com.gupta.nullsafety;

import java.util.HashMap;
import java.util.Map;

/**
 *  (MALA/ Mala doesn't mean Mala will say this. It means
 *  Mala has summarised stuff or is unsure about it.)
 *  * --------------------------------------------
 *  *  MALA: Three points
 *  *  a) null is everywhere, not because it was an accident or a mistake. It was a decision.
 *  *  b) how the scale of using null causes damage
 *  *  c) a decision framework.
 *  * --------------------------------------------
 *
 * Session title: The Past, Present, and Future of Null Safety in Java
 *
 * Introduction:
 *
 * CHANDRA/ MALA:
 *
 * Show of hands - how many of you had a production issue with NPE?
 *
 * Congratulations - You are a seasoned Java developer if you can relate.
 * If not, you are lucky seasoned seasoned developer.
 *
 * You have coded the fix for NullPointerException more times than you can count.
 *
 * You know what causes it & you know how to prevent it.
 * You've reviewed pull requests for it.
 * You've probably suggested others how to avoid it.
 *
 * But still.. it is the most common exception in production.
 * Not second. First. By a significant margin. (CAN WE HAVE ANY NUMBERS HERE?)
 *
 * The problem is.. we are not doing enough (MAYBE CHANGE THE WORDING).
 *
 * In the next forty minutes we want
 * to show you three things that changed how we thought about null entirely.
 *
 * ------------------
 * == First == null is everywhere, not because it was an accident
 * or a mistake. It was a decision. Made under constraints that have changed
 *
 * -----------
 * == Second ==  how the scale of using null causes damage - the three specific mechanisms
 * Not 'null is bad' — three precise failure modes that appear
 * only when the codebase gets large enough that no single developer
 * can hold the full picture in their head.
 * Which is every production system you've ever worked on.
 *
 * -------------
 * == Third == a decision framework.
 * Not a list of tools. Three questions that tell you exactly which
 * solution belongs in your codebase, on your Java version, under
 * your constraints. Something you can use on Monday morning.
 *
 * -------------
 * Before we deep live into the session..
 *
 * [pick up an emplty cup of Coffee]
 *
 * This is lazy initialisation.
 *
 * [pause, let that land]
 *
 * The cup existed before I needed it. It was empty. It was null.
 * If this is at a coffee shop, It will be filled when someone asks for it.
 * That is the most common (and legit) use of null in any Java codebase — and it's been in
 * production systems since 1995.
 *
 * If you understand why this cup was empty before I picked it up,
 * you understand null better than most of the articles written about it.
 *
 * Let's start there."
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * DEMO 1 — WHY NULL EXISTS                                      [0:00 – 7:00]
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CHANDRA/ MALA:
 *  Before we talk about Null safety or any solutions -
 *  let's quicky talk about:
 *
 *  a) why null exists, and
 *  b) why it is everywhere in Java
 *
 *  If we don't understand the reasons why it exists, it will be
 *  difficult to understand everything else related to it.
 *
 *  Creation of null (1965), Tony Hoare, is refferred to as the billion-dollar mistake.
 *  But what about the semantics?
 *  he needed a way to represent 'absence of a value'.
 *
 *  Java used null in 1995 for the same reason. The language needed a
 *  single value that could be assigned to ANY reference type.
 *  Without null,
 *  you couldn't say 'this field hasn't been set yet.' You couldn't return
 *  'not found' from a Map.
 *
 *  Using null was a decision decision. Made under constraints
 *  we no longer have (we'll talk about them soon)
 *
 *  Let me show you the three places senior developers
 *  put null deliberately — because they had good reasons."
 */

// Chandra: Change name to Restaurant
public class Demo1_WhyNullExists {

    record Coffee(String name, String origin, int shots) {}
    record Order(String customerName, Coffee coffee) {}

    // ─────────────────────────────────────────────────────────────────────
    // USE 1 — Missing values are okay (some variables are allowed to be null)
    // ─────────────────────────────────────────────────────────────────────
    //
    // CHANDRA/ MALA:
    // Use case - a customer walks in. They haven't ordered yet.
    // A customer without an order can exist.
    //
    // Here's a class to model this:

    static class CustomerWithOptionalFields {
        String customerName;
        Coffee selectedCoffee  = null;  // not chosen yet — valid state
        String  promoCode      = null;  // customer may not have one
        Integer tableNumber    = null;  // takeaway orders have no table

        // SCRIPT:
        // Three null assignments. All deliberate. All representing
        // this value might not exist.' Not bugs — design decisions.
        //
        // The problem isn't these assignments. The problem is that
        // null silently means THREE DIFFERENT THINGS here:
        // i) 'not yet set',
        // ii) 'not applicable', and
        // iii) 'customer didn't provide one.'
        // The same value. Three different intents. No way to tell them apart."

        // You could assign null implicitly or expliictly,
        // instance/ static - implicitly null.
        // local reference variables - no value assigned.
    }

    // ─────────────────────────────────────────────────────────────────────
    // LEGITIMATE USE 2 — Deferred fetching of content (Lazy initialisation)
    // ─────────────────────────────────────────────────────────────────────
    //
    // Chandra/ Mala:
    // Second — lazy initialisation.
    //
    // Analogy: Think about the coffee cups at the start of the day.
    // The cups are on the counter. Empty. Available. But not filled.
    // Why fill them before a customer orders? That's wasted coffee.
    // That's wasted memory. That's lazy initialisation.
    //
    // Streams - Lazy initialization. 

    static class CoffeeStation {
        private Map<String, Coffee> menuCache = null; // expensive to load — defer it

        // Chandra/ Mala:
        // The menu cache is null at startup. Not because we forgot.
        // Because loading the menu hits the database, takes 200ms,
        // and nobody has asked for it yet. Why pay that cost at startup
        // if the first request might be five minutes away?
        //
        // This pattern is in Hibernate.
        // It's in Spring.
        // It's in the JDK.
        // It was a legitimate performance optimisation."

        Map<String, Coffee> getMenu() {
            if (menuCache == null) {                        // first request?
                menuCache = loadMenuFromDatabase();         // fill the cup
            }
            return menuCache;                              // serve from cache
        }

        private Map<String, Coffee> loadMenuFromDatabase() {
            Map<String, Coffee> menu = new HashMap<>();
            menu.put("espresso", new Coffee("Espresso", "Ethiopian", 2));
            menu.put("latte",    new Coffee("Latte",    "Colombian", 1));
            return menu;
        }

        // CHANDRA/ MALA:
        // The cup analogy holds perfectly:
        // Empty cup on the counter = null field at startup.
        // First customer orders    = first method call.
        // Barista fills the cup    = field gets initialised.
        // Every customer after     = served from the same cup — from cache.
        //
        // The problem? What if two customers ask at exactly the same time?"

        // Thread safety problem — the hidden cost of null as lazy init signal
        //
        // SCRIPT:
        // Thread 1 checks — menuCache is null.
        // Thread 2 checks — menuCache is null.  (both see null simultaneously)
        // Thread 1 loads  — sets menuCache.
        // Thread 2 loads  — sets menuCache AGAIN.
        // Two database hits. One cache. Race condition.
        // The null that saved startup time just created a concurrency bug."
    }

    // ─────────────────────────────────────────────────────────────────────
    // LEGITIMATE USE 3 — Releasing references for GC
    // ─────────────────────────────────────────────────────────────────────
    //
    // SCRIPT:
    // "Third — the one that dates this talk most clearly, but I include it
    //  because it explains something senior developers did deliberately
    //  for years and got criticised for later.
    //
    //  In early Java — pre-generational GC improvements — holding a reference
    //  in a long-lived object prevented the GC from collecting it.
    //  The solution was explicit: set it to null. Tell the GC: I'm done with this."

    static class SessionManager {
        private static SessionManager instance     = new SessionManager();
        private        Coffee          currentOrder = null;

        static void shutdown() {
            instance = null;      // SCRIPT: "Release the singleton. Let it be collected."
        }

        void clearOrder() {
            currentOrder = null;  // SCRIPT: "Release the order. Customer has left."
        }

        // SCRIPT:
        // "This was real, necessary, and correct — on Java 1.4 with large heaps.
        //  Modern JVMs with G1, ZGC, and Shenandoah detect reachability without
        //  this hint. The pattern survives today mainly in Android development
        //  and large-heap applications where memory pressure is explicit.
        //
        //  Why show it? Because senior developers in this room wrote this.
        //  It was the right call at the time. Understanding that null had
        //  legitimate uses is the foundation for understanding why removing it
        //  took 30 years."
    }

    public static void main(String[] args) {

        // SCRIPT:
        // "Let me close this section with the key observation.
        //  Look at these three patterns:"

        // Pattern 1 — optional value
        Order order = new Order("Jonathan", null);
        System.out.println("Coffee on order : " + order.coffee());        // null — valid

        // Pattern 2 — lazy init
        CoffeeStation station = new CoffeeStation();
        System.out.println("Cache at startup: " + station.menuCache);     // null — intentional

        // Pattern 3 — GC hint
        SessionManager.shutdown();
        System.out.println("Instance after shutdown: " + SessionManager.instance); // null — deliberate

        // SCRIPT:
        // "Three nulls. Three completely different intents.
        //  The language cannot tell them apart. Neither can your tools.
        //  Neither can the developer who joins your team six months from now.
        //
        //  That is the root of the problem. Not that null exists.
        //  That null communicates nothing about WHY it's there.
        //
        //  Keep that in mind. Everything in the rest of this session
        //  is an attempt to give null's silence a voice."
    }
}