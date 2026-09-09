package com.gupta.nullsafety;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class SolutionsJSpecify {

    record Coffee(@Nullable String name, String origin, String brewingInstructions) {}
    record Order(String customerName, Coffee coffee) {}

    static void main() {
        Coffee coffee1 = new Coffee(null, "abv", "hg");

        Order order = new Order("Mike", null); // @Nullable Coffee — valid
        Coffee coffee = order.coffee();

        if (coffee == null) {
            System.out.println("@Nullable  : caught — coffee absent, handled cleanly");
        } else {
            String instr = coffee.brewingInstructions();
            String result = (instr != null) ? instr.toUpperCase() : "No instructions";
            System.out.println("@Nullable  : " + result);
        }
    }
}
