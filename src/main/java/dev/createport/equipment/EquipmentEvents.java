package dev.createport.equipment;
import dev.createport.Create;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
@Mod.EventBusSubscriber(modid=Create.ID)
public final class EquipmentEvents {
    @SubscribeEvent public static void attack(AttackEntityEvent event){
        var player=event.getEntityPlayer();if(!player.world.isRemote && !player.capabilities.isCreativeMode && player.getHeldItemMainhand().getItem()==Create.WRENCH && event.getTarget() instanceof EntityMinecart cart)cart.attackEntityFrom(DamageSource.causePlayerDamage(player),100);
    }
}
