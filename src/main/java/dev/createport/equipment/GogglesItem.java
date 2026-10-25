package dev.createport.equipment;

import dev.createport.Create;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;

/** A head-slot accessory, as in Create: no added armor or damage durability. */
public final class GogglesItem extends Item {
    private static final List<Predicate<EntityPlayer>> WEARING=new CopyOnWriteArrayList<>();
    static { WEARING.add(player -> player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()==Create.GOGGLES); }
    public GogglesItem() {
        setRegistryName(Create.ID,"goggles");setTranslationKey("create.goggles");setMaxStackSize(1);setCreativeTab(CreativeTabs.TOOLS);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this,ItemArmor.DISPENSER_BEHAVIOR);
    }
    public static void addIsWearingPredicate(Predicate<EntityPlayer> predicate) { WEARING.add(java.util.Objects.requireNonNull(predicate)); }
    public static boolean isWearing(EntityPlayer player) { return player!=null && WEARING.stream().anyMatch(test->test.test(player)); }
    @Override public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) { return EntityEquipmentSlot.HEAD; }
    @Override public boolean isValidArmor(ItemStack stack,EntityEquipmentSlot slot,Entity entity) { return slot==EntityEquipmentSlot.HEAD; }
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand) {
        ItemStack held=player.getHeldItem(hand),previous=player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if(previous.getItem()==this)return new ActionResult<>(EnumActionResult.PASS,held);
        if(!world.isRemote) {
            player.setItemStackToSlot(EntityEquipmentSlot.HEAD,held.copy());player.setHeldItem(hand,previous.copy());
            player.inventory.markDirty();world.playSound(null,player.getPosition(),SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,SoundCategory.PLAYERS,.6f,1.2f);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS,player.getHeldItem(hand));
    }
}
