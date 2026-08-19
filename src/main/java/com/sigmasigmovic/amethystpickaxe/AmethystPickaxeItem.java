package com.sigmasigmovic.amethystpickaxe;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AmethystPickaxeItem extends PickaxeItem {
    private static final String CHARGE_KEY = "AmethystPickaxeCharge";

    // charge is counted in mining actions, not blocks broken
    private static final int MAX_CHARGE = 300;

    private static final float MIN_SPEED = 2.0f;
    private static final float MAX_SPEED = 32.0f;

    // speed = MIN_SPEED + (MAX_SPEED - MIN_SPEED) * progress^CURVE_EXPONENT
    private static final double CURVE_EXPONENT = 2.5;

    public AmethystPickaxeItem(ToolMaterial material, Item.Settings settings) {
        // tooltip that doesnt work (fix later)
        super(material, settings);
    }

    @Override
    public float getMiningSpeed(ItemStack stack, BlockState state) {
        if (state.isAir()) {
            return MIN_SPEED;
        }

        return speedForCharge(getCharge(stack));
    }

    private static float speedForCharge(int charge) {
        double progress = Math.max(0.0, Math.min(1.0, (double) charge / (double) MAX_CHARGE));
        double curved = Math.pow(progress, CURVE_EXPONENT);
        return (float) (MIN_SPEED + (MAX_SPEED - MIN_SPEED) * curved);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!selected && getCharge(stack) > 0) {
            setCharge(stack, 0);
        }
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.postMine(stack, world, state, pos, miner);

        if (world.isClient || !(miner instanceof PlayerEntity player)) {
            return result;
        }

        setCharge(stack, getCharge(stack) + 1);
        mineArea(world, pos, player, stack);

        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.BLOCKS, 0.65f, 1.15f);

        return result;
    }

    @Override
    public SoundEvent getBreakSound() {
        return SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK;
    }

    private void mineArea(World world, BlockPos center, PlayerEntity player, ItemStack stack) {
        Vec3d look = player.getRotationVec(1.0f);
        double ax = Math.abs(look.x);
        double ay = Math.abs(look.y);
        double az = Math.abs(look.z);

        if (ay > ax && ay > az) {
            // facing up/down -> break the X/Z plane
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    tryBreak(world, center.add(dx, 0, dz), player, stack);
                }
            }
        } else if (ax > az) {
            // facing east/west -> break the Y/Z plane
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dy == 0 && dz == 0) continue;
                    tryBreak(world, center.add(0, dy, dz), player, stack);
                }
            }
        } else {
            // facing north/south -> break the X/Y plane
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    tryBreak(world, center.add(dx, dy, 0), player, stack);
                }
            }
        }
    }

    private void tryBreak(World world, BlockPos pos, PlayerEntity player, ItemStack stack) {
        if (stack.isEmpty() || stack.getDamage() >= stack.getMaxDamage() - 1) {
            return;
        }

        BlockState target = world.getBlockState(pos);
        if (target.isAir() || target.getHardness(world, pos) < 0.0f) {
            return;
        }

        if (!world.breakBlock(pos, true, player)) {
            return;
        }

        stack.damage(1, player, EquipmentSlot.MAINHAND);
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.BLOCKS,
                0.45f, 1.0f + world.random.nextFloat() * 0.25f);
    }

    private static int getCharge(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }
        return Math.min(customData.getNbt().getInt(CHARGE_KEY), MAX_CHARGE);
    }

    private static void setCharge(ItemStack stack, int charge) {
        int clamped = Math.max(0, Math.min(MAX_CHARGE, charge));
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> nbt.putInt(CHARGE_KEY, clamped));
    }
}
