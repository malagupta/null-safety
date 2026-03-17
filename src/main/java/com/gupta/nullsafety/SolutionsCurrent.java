package com.gupta.nullsafety;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SolutionsCurrent {


	record Coffee(String name, String origin, String brewingInstructions) {}
	record Inventory(Coffee coffee, String brewingSuggestion){}
	record Order(String customerName, Coffee coffee) {}
	record Customer(String name, String email, String brewingPreference){}

	static class DetectingNulls {

        //Detect nulls in a method call chain
		// 😊 Check at the boundary, one guard, clear message
        static String serveCorrect(Order order) {
            Coffee coffee = order.coffee();
            if (coffee == null) return "No coffee on this order.";
            return coffee.name();
        }

        // 😈 Null check hell
        static String serveNullCheckHell(Order order) {
            if (order != null) {
                if (order.coffee() != null) {
                    if (order.coffee().name() != null) {
                        if (order.coffee().brewingInstructions() != null) {
                            return order.coffee().brewingInstructions().toUpperCase();
                        } else {
                            return "No instructions.";
                        }
                    } else {
                        return "No name.";
                    }
                } else {
                    return "No coffee.";
                }
            }
            return "No order.";
        }
    }

    static class JavaRecordPatterns {

		record CoffeeOrder(String customerName, Coffee coffee, int shots) {
			CoffeeOrder {
				Objects.requireNonNull(customerName);        //<1>
				Objects.requireNonNull(coffee);              //<1>
				if (shots < 1 || shots > 4)
					throw new IllegalArgumentException();
			}
		}

		boolean hasBrewingInstructions(Object obj) {
			if (obj != null) {
				if (obj instanceof CoffeeOrder coffeeOrder) {
					if (coffeeOrder.coffee() != null) {
						return coffeeOrder.coffee().brewingInstructions() != null;
					}
				}
			}
			return false;
		}

		boolean hasBrewingInstructionsAgain(Object obj) {
			if (obj instanceof CoffeeOrder(
					String _,
					Coffee(
							String _,
							String _,
							String brewingInstructions       //<1>
					),
					int _
			)) {
				return brewingInstructions != null;
			}
			return false;
		}
	}


    static class UsingOptional {

        private static final Map<String, Coffee> menu = new HashMap<>();
        static {
            menu.put("espresso", new Coffee("Espresso", "Ethiopian", "Grind fine. 90°C."));
            menu.put("latte",    new Coffee("Latte",     "Colombian", "Shot + steamed milk."));
            menu.put("newblend", new Coffee("New Blend", "Rwandan",   null));
        }

        Coffee findByNameNotUsingOptional(@NotNull String name) {
            return menu.get(name);
        }

        //Detect Optionals in a method call chain
		// Before Optional — caller has no idea
        static Coffee findByName_unsafe(String name) {
            return menu.get(name.toLowerCase()); // null if not found — caller doesn't know
        }

        // After Optional — contract is in the type
        static Optional<Coffee> findByName(String name) {
            return Optional.ofNullable(menu.get(name.toLowerCase()));
        }

        static void usingMethodsReturningOptionalValues() {
            // Missing values checked via methods such as orElse
            String result = findByName("espresso")
                    .map(Coffee::name)
                    .map(String::toUpperCase)
                    .orElse("Not found");
            System.out.println("Found    : " + result);

            String missing = findByName("coldpresso")
                    .map(Coffee::name)
                    .orElse("Not on menu — try our cold brew");
            System.out.println("Missing  : " + missing);

            // orElseThrow — when absence is an error
            try {
                Coffee required = findByName("coldpresso")
                        .orElseThrow(() -> new IllegalStateException(
                                "'coldpresso' must be configured — check setup"));
            } catch (IllegalStateException e) {
                System.out.println("Error    : " + e.getMessage());
            }

            // Calling .get() without checking for nullness is an anti-pattern.
            try {
                Coffee bad = findByName("coldpresso").get(); // NoSuchElementException if null
            } catch (java.util.NoSuchElementException e) {
                System.out.println(e);
            }

        }
    }
}

