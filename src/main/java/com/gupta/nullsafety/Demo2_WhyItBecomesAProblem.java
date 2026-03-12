package com.gupta.nullsafety;

import java.util.HashMap;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DEMO 2 — WHY NULL BECOMES A PROBLEM AT SCALE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Chandra/ Mala:
 * "So null had legitimate reasons to exist. Why did it become the single
 *  most common source of production bugs in Java?
 *
 *  I) Scale of code that can throw a NPE
 *  II) null can assigned to any reference variable (local, instance, static, .. you name it)
 *  III) Null dereference:  Calling methods on/ accessing attributes on a null reference,
 *                          either implicitly or explicitly.
 *
 * --------------------------------------------
 *  MALA: Three USE cases in this class -
 *  a) calling (methods/ attributes) on a null reference variable   (TODO: Mala: Fix the lang)
 *  b) Method return type is null
 *  c) Evolved business logic - issue
 * --------------------------------------------
 *
 *                                                                                  
 *  Three specific failure modes. Not 'null is bad' — three precise
 *  mechanisms by which null causes damage at team scale.
 *  I want you to recognise all three from your own experience."
 */
public class Demo2_WhyItBecomesAProblem {

    record Coffee(String name, String origin, String brewingInstructions) {}
    record Order(String customerName, Coffee coffee) {}
    record Payment(String cardNumber, String cardHolder) {}
    record CustomerOrder(String id, Coffee coffee, Payment payment) {}

    // ─────────────────────────────────────────────────────────────────────
    // FAILURE MODE 1 — null has no type and no origin
    //                  (MALA: Calling a member on a reference variable??)
    //                  The stack trace tells you WHERE. Not WHY. Not FROM WHERE.
    // ─────────────────────────────────────────────────────────────────────
    //
    // SCRIPT:
    // "Failure mode one. This is the one that costs the most debugging time.
    //
    //  When null travels through your codebase and eventually crashes,
    //  the stack trace is a crime scene photo of the explosion.
    //  It does not tell you who lit the fuse, or where, or why.
    //
    //  Let me show you what I mean."

    static String getBrewingInstructions(Order order) {
        return order.coffee().brewingInstructions();
        //           ↑                ↑
        //     null here?        or null here?
        // Stack trace says line 67. That's all you get pre-Java-14.
    }

    // SCRIPT:
    // "One line. Two potential nulls.
    //  coffee() could be null — order placed, no coffee selected.
    //  brewingInstructions() could be null — coffee exists, instructions missing.
    //
    //  The stack trace points at this line. Unhelpfully.
    //  You open the debugger. You add logging. You reproduce it locally.
    //  45 minutes later you find the null came from a Map.get() call
    //  in a service three classes upstream that silently returned null
    //  for an unknown key at 2:47am when the menu was being updated.
    //
    //  Java 14 helped — it now tells you WHICH variable was null.
    //  But before Java 14 — this one pattern alone was responsible for
    //  more debugging sessions than I care to count."

    static void demo_NoTypeNoOrigin() {
        System.out.println("── Failure Mode 1: null has no type, no origin ──");

        // Scenario A — coffee is null
        Order noCoffee = new Order("Steve", null);
        try {
            System.out.println(getBrewingInstructions(noCoffee));
        } catch (NullPointerException e) {
            System.out.println("NPE — but which null? coffee() or brewingInstructions()?");
            System.out.println("Java 14+ message: " + e.getMessage()); // now tells you
            System.out.println("Pre-Java 14: just a line number. Good luck.");
        }

        // Scenario B — coffee exists, instructions null — same exception, different origin
        Order noInstructions = new Order("Alice",
                new Coffee("New Blend", "Rwandan", null));
        try {
            System.out.println(getBrewingInstructions(noInstructions));
        } catch (NullPointerException e) {
            System.out.println("NPE — same line, different null, different root cause.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // FAILURE MODE 2 — (MALA: Methods that return null??)
    //                  method signatures lie by omission
    //                  Can I trust this return value? The compiler won't say.
    // ─────────────────────────────────────────────────────────────────────
    //
    // SCRIPT:
    // "Failure mode two. The one that erodes team trust over time.
    //
    //  Here are four method signatures. One question: which ones can return null?"

    static Coffee findCoffeeById(String id)            { return null; }     // can return null
    static Coffee getDefaultCoffee()                   { return new Coffee("House Blend", "Unknown", null); } // never null
    static Payment processPayment(String card)         { return null; }     // can return null
    static String  formatOrderId(String customerId)    { return "ORD-" + customerId; } // never null

    // SCRIPT:
    // "You cannot tell. Not from the signature. Not without reading the implementation.
    //  findCoffeeById — null if not found? Throws? Returns a default? The signature says nothing.
    //  getDefaultCoffee — 'default' implies always present. But is that a contract or a hope?
    //  processPayment — null if payment failed? Or does it throw? Nobody knows.
    //
    //  This is what I mean by 'method signatures lie by omission.'
    //  At team scale — with 50 developers and 500 service methods — every call
    //  becomes a trust exercise. Do I need to null-check this? Should I?
    //  The safe answer is: check everything. And that's exactly what happens."

    static void demo_SignaturesLie() {
        System.out.println("\n── Failure Mode 2: method signatures lie by omission ──");

        // The cautious developer checks everything — including things that never return null
        Coffee defaultCoffee = getDefaultCoffee();
        if (defaultCoffee == null) {                    // (((((defensive but unnecessary)))))
            System.out.println("Defensive: checking even when it can't be null.");
        }

        // The trusting developer skips checks — including things that can return null
        Coffee found = findCoffeeById("unknown-id");    // returns null
        System.out.println("Trusting : " + found.name()); // 💥 NPE — misplaced trust

        // SCRIPT:
        // "Neither developer is wrong given the information available.
        //  The defensive one writes noise. The trusting one ships bugs.
        //  Both outcomes come from the same root cause: the signature said nothing."
    }

    // ─────────────────────────────────────────────────────────────────────
    // FAILURE MODE 3 — Relevance - changing business requirements
    //                  null checks protect today, hide bugs tomorrow
    //                  The fix that becomes the problem six months later
    // ─────────────────────────────────────────────────────────────────────
    //
    // SCRIPT:
    // "Failure mode three. The subtle one. The one senior developers feel
    //  but struggle to articulate when reviewing code.
    //
    //  You add a null check to fix a bug. It ships. It works.
    //  Six months later, the business rule changes.
    //  The null check is still there — silently absorbing what is now
    //  a data integrity violation.
    //
    //  Let me show you exactly how this happens."

    static void processOrder_v1(CustomerOrder order) {

        // SCRIPT:
        // "Sprint 1. Coffee is optional — customer can browse without selecting.
        //  The null check is correct. It handles a valid state."

        if (order.coffee() == null) {
            System.out.println("v1: No coffee selected yet. Prompting customer.");
            return; // valid — customer hasn't chosen
        }
        System.out.println("v1: Processing — " + order.coffee().name());
    }


    //TODO: Mala: Create script to explain what this means in 10 seconds (Should be able to make sense to a layman)
    static void processOrder_v2(CustomerOrder order) {

        // SCRIPT:
        // "Sprint 4. Business rule changes.
        //  Payment is now mandatory — you can't process an order without it.
        //  The developer adds the new code. The old null check is still there."

        if (order.coffee() == null) {
            System.out.println("v2: No coffee — skipping.");
            return;                   // ← this is now wrong. But it compiles. It runs.
        }

        if (order.payment() == null) {
            // SCRIPT:
            // "The new check is correct. But the OLD check now silently swallows
            //  a data integrity violation. An order with no coffee should never
            //  reach this method anymore — the UI enforces it. When it does,
            //  it means something upstream is broken.
            //  But the null check doesn't tell us that. It just returns quietly."
            System.out.println("v2: No payment — cannot process.");
            return;
        }

        System.out.println("v2: Processing — " + order.coffee().name()
                + " paid with " + order.payment().cardNumber());
    }

    static void demo_ChecksHideBugs() {
        System.out.println("\n── Failure Mode 3: null checks hide bugs ──");

        // Sprint 1 — null coffee is a valid state
        CustomerOrder browsingOrder = new CustomerOrder("ORD-001", null, null);
        processOrder_v1(browsingOrder); // "No coffee selected yet" — correct

        // Sprint 4 — null coffee is now a data integrity violation
        // But the null check absorbs it silently. No error. No alert. Nothing.
        processOrder_v2(browsingOrder); // "No coffee — skipping" — WRONG. Hides a bug.

        // SCRIPT:
        // "A NullPointerException here would have been better.
        //  It would have told us immediately that the upstream system
        //  sent us an order without a coffee after that was supposed to
        //  be impossible. Instead — silence. The bug ships to production.
        //  The data silently corrupts. The on-call engineer finds it three weeks
        //  later in a database audit.
        //
        //  This is why I said earlier: null checks don't just defend against bugs.
        //  They bury them. An exception tells you the rule changed.
        //  A null check just carries on quietly."
    }

    public static void main(String[] args) {
        demo_NoTypeNoOrigin();
        demo_SignaturesLie();
        demo_ChecksHideBugs();

        // SCRIPT:
        // "Three failure modes. Each one a different dimension of the same problem:
        //
        //  1. No type, no origin   → debugging cost compounds with codebase size
        //  2. Signatures lie       → defensive checks everywhere, trust nowhere
        //  3. Checks hide bugs     → correctness today, corruption tomorrow
        //
        //  These are not beginner mistakes. They're systemic problems that
        //  appear precisely when the codebase gets large enough that no single
        //  developer can hold the full null contract in their head.
        //
        //  And that's exactly the scale at which every production system operates.
        //
        //  So — what did we try to fix it?"
    }
}