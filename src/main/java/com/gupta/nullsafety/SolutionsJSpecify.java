package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class SolutionsJSpecify {

    record Coffee(String name, String origin, String brewingInstructions) {}
    record Order(String customerName, Coffee coffee) {}

    static void main() {
        Order order = new Order("Mike", null); // @Nullable Coffee — valid
        Coffee coffee = order.coffee();

        if (coffee == null) {
            System.out.println("@Nullable  : caught — coffee absent, handled cleanly");
        } else {
            // @Nullable on brewingInstructions — must check before calling toUpperCase()
            String instr = coffee.brewingInstructions();
            String result = (instr != null) ? instr.toUpperCase() : "No instructions";
            System.out.println("@Nullable  : " + result);
        }
    }
}
