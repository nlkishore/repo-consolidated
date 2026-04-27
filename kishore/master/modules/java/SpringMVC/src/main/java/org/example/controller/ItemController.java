package org.example.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/items")
@Tag(name = "Item Management", description = "Operations related to items")
public class ItemController {

    private final List<Item> items = new ArrayList<>();

    @GetMapping
    @Operation(summary = "Get all items", description = "Returns a list of all items")
    public List<Item> getAllItems() {
        return items;
    }

    @PostMapping
    @Operation(summary = "Add a new item", description = "Creates a new item")
    public Item addItem(@Parameter(description = "Item to be added") @RequestBody Item item) {
        items.add(item);
        return item;
    }

    public static class Item {
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
