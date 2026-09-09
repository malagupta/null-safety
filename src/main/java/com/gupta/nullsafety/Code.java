package com.gupta.nullsafety;

import java.util.Map;
import java.util.Optional;

public class Code {

    record Coffee(String name, String origin, String brewingInstructions) {}
    record Order(String customerName, Coffee coffee) {}

    static final Map<String, Coffee> MENU = Map.of(
            "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine. 90°C."),
            "latte", new Coffee("Latte", "Colombian", "Shot + steamed milk."),
            "newblend", new Coffee("New Blend", "Rwandan", null)
    );

    /*
    Given the following code, Write a method that finds a coffee by name and returns its brewing instructions in uppercase:
    record Coffee(String name, String origin, String brewingInstructions) {}
    record Order(String customerName, Coffee coffee) {}

    static final Map<String, Coffee> MENU = Map.of(
            "espresso", new Coffee("Espresso", "Ethiopian", "Grind fine. 90°C."),
            "latte", new Coffee("Latte", "Colombian", "Shot + steamed milk."),
            "newblend", new.Coffee("New Blend", "Rwandan", null)
     */



    // Model 0
    // Long long time ago! (Model name?)
    // Method that finds a coffee by name and returns its brewing instructions in uppercase
    static String getCoffeeBrewingInstructions(String name) {
        Coffee coffee = MENU.get(name);                       // null if name not in menu
        return coffee.brewingInstructions().toUpperCase();    // NPE #1 or NPE #2
    }


    // Model 1
    // Claude 4.7 Opus
    // Method that finds a coffee by name and returns its brewing instructions in uppercase
    static Optional<String> findBrewingInstructionsUpper(String name) {
        return Optional.ofNullable(name)
                       .map(String::toLowerCase)
                       .map(MENU::get)
                       .map(Coffee::brewingInstructions)
                       .map(String::toUpperCase);
    }

    // Model 2
    // Claude 4 Sonnet
    // Method that finds a coffee by name and returns its brewing instructions in uppercase
    static String getBrewingInstructionsUppercase(String name) {
        if (name == null) {
            return "COFFEE NAME CANNOT BE NULL";
        }

        Coffee coffee = MENU.get(name);
        if (coffee == null) {
            return "COFFEE NOT FOUND: " + name.toUpperCase();
        }

        String instructions = coffee.brewingInstructions();
        if (instructions == null) {
            return "NO BREWING INSTRUCTIONS AVAILABLE FOR " + coffee.name().toUpperCase();
        }

        return instructions.toUpperCase();
    }

    // Model 3
    // Gemini 2.5 Flash
    // Method that finds a coffee by name and returns its brewing instructions in uppercase
    static Optional<String> getBrewingInstructions(String name) {
        return Optional.ofNullable(MENU.get(name))
                       .map(Coffee::brewingInstructions)
                       .map(String::toUpperCase);
    }

    // Model 4
    // ChatGPT 4o
    // Method that finds a coffee by name and returns its brewing instructions in uppercase
    public static String getBrewingInstructionsInUppercase(String name) {
        return Optional.ofNullable(MENU.get(name))              // Find the coffee, may be null.
                       .map(Coffee::brewingInstructions)        // Extract brewing instructions, may be null.
                       .map(String::toUpperCase)                // Convert to uppercase when present.
                       .orElse("NO BREWING INSTRUCTIONS");      // Default value if absent.
    }


    /*
     AGENTS.md - vendor neutral
     CLAUDE.md
     github/copilot-instructions.md
    */

}
