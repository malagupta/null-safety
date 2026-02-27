package com.gupta.nullsafety;

import java.util.Objects;
import java.util.Optional;

public class HandleNull_1_ReferenceVariable_Solution5_AdapterWrapperAPICalls
{

}

// The adapter — owns all knowledge of the external API's null behaviour
class CoffeeInventoryAdapter {

    private final CoffeeInventoryApi api;

    CoffeeInventoryAdapter(CoffeeInventoryApi api) {
        this.api = Objects.requireNonNull(api);
    }

    Optional<Coffee>  findByName(String name) { return Optional.ofNullable(api.findByName(name)); }
    Optional<String>  getOrigin(String id)    { return Optional.ofNullable(api.getOrigin(id));    }
    Optional<Integer> getTemperature(String id){ return Optional.ofNullable(api.getTemperature(id)); }
}

// The rest of your codebase only ever sees CoffeeInventoryAdapter.
// Optional everywhere. No raw null from the external API ever crosses the boundary.