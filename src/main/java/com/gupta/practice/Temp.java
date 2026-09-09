package com.gupta.practice;

import com.gupta.nullsafety.HandleNull_1_ReferenceVariables;

record Coffee(String name, String origin, int shots) { }
record Order(String customerName, Coffee coffee) {}

public class Temp {
    static void main() {

        // 1.1: Initializing null implicitly/ explicitly-
        // All instance and static reference variables are implicitly set to null.
        // (( DEFER: Local reference variables are not implicitly set to null. ))
        // null isn't a mistake.
        // Sometimes it's a conscious design decision,
        //
        // <<<< Lazy initialization >>>>
        // The coffee shop has cups to serve coffee.
        // The cups exist but
        // not filled until a customer actually places
        // an order. That's lazy initialisation.
        //
        /*
        // All of us are more used to something like the following:
        class CoffeeGrinder {
            private Connection dbConnection = null; // expensive to create — defer until needed

            Connection getConnection() {
                if (dbConnection == null) {
                    dbConnection = createConnection(); // created only on first use
                }
                return dbConnection;
            }
        }

        (Hibernate does this internally — which is why lazy-loaded
        entity fields appear as null outside a transaction.)
         */

        // =====================================
        // 1.1 : Uninitialised instance/ static reference variable
        // You explictly set a reference variable to null

        // 1.2 : null passed as a method argument
        // You pass null to a contsructor

        // 1.3 : The sneaky null — object exists, field inside is null
        // No values passed - implicit assignment of null

        Order order = new Order("Jonathan", null);
        IO.println(order.coffee());
        IO.println(order.coffee().name());

        order = new Order("Mike", new Coffee("Espresso", null, 1));
        IO.println("My coffee's origin is: " + order.coffee()
                                                            .origin()
                                                            .toUpperCase()); // NPE since coffee's origin is null


        // 2.1 : The chained call — the classic one-liner NPE ──────────────
        // 2.2 : Core Java API calls returning null - handling something you can't change?
        // Use case 2.3 : Deeper chain — null hiding two calls deep ─────────────────
        // Use case 2.4 : The Integer unboxing surprise ──────────────────────────────


    }

}
