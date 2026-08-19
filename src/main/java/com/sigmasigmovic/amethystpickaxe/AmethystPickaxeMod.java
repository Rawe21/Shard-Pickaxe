package com.sigmasigmovic.amethystpickaxe;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class AmethystPickaxeMod implements ModInitializer {
    public static final String MOD_ID = "amethystpickaxe";

    public static final Item AMETHYST_PICKAXE = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "amethyst_pickaxe"),
            new AmethystPickaxeItem(
                    ToolMaterials.DIAMOND,
                    new Item.Settings()
                            .maxCount(1)
                            .maxDamage(1000)
                            .rarity(Rarity.EPIC)
            )
    );

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(AMETHYST_PICKAXE);
        });
    }

    public static ItemStack createPickaxeStack() {
        return new ItemStack(AMETHYST_PICKAXE);
    }
}
