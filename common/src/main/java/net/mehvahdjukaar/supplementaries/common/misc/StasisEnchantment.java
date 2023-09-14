package net.mehvahdjukaar.supplementaries.common.misc;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.supplementaries.common.items.BubbleBlower;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class StasisEnchantment extends Enchantment {

    public StasisEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.CROSSBOW,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 10 + level * 5;
    }

    @Override
    public int getMaxCost(int level) {
        return 40;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public boolean isTradeable() {
        return CommonConfigs.stasisEnabled();
    }

    @Override
    public boolean isDiscoverable() {
        return CommonConfigs.stasisEnabled();
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public boolean isAllowedOnBooks() {
        return CommonConfigs.stasisEnabled();
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public boolean checkCompatibility(Enchantment enchantment) {
        return super.checkCompatibility(enchantment) && enchantment != Enchantments.MULTISHOT;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        Item i = stack.getItem();
        return i instanceof BubbleBlower || super.canEnchant(stack);
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        Item i = stack.getItem();
        return i instanceof BubbleBlower;
    }
}
