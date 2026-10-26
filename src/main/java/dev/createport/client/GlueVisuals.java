package dev.createport.client;
import dev.createport.Create;
import dev.createport.glue.*;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
@Mod.EventBusSubscriber(modid=Create.ID,value=Side.CLIENT)
public final class GlueVisuals {
    private static NBTTagCompound saved=new NBTTagCompound();
    private static BlockPos cachedFirst,cachedSecond;private static boolean reachable;
    public static void accept(NBTTagCompound data){Minecraft.getMinecraft().addScheduledTask(()->saved=data);}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){var mc=Minecraft.getMinecraft();if(event.phase==TickEvent.Phase.END&&mc.player!=null&&mc.world!=null&&mc.player.getHeldItemMainhand().getItem()==Create.SUPER_GLUE&&mc.player.ticksExisted%10==0)Create.NETWORK.sendToServer(new GlueDataPacket());}
    private static NBTTagCompound selected(){
        var mc=Minecraft.getMinecraft();if(mc.player==null||mc.world==null||saved.getInteger("Dimension")!=mc.player.dimension)return null;
        Vec3d eye=mc.player.getPositionEyes(1),end=eye.add(mc.player.getLookVec().scale(mc.playerController.getBlockReachDistance()+1));double nearest=100;NBTTagCompound selected=null;
        var list=saved.getTagList("Areas",10);for(int i=0;i<list.tagCount();i++){var n=list.getCompoundTagAt(i);var b=bounds(n);var hit=b.calculateIntercept(eye,end);double d=b.contains(eye)?0:hit==null?100:eye.squareDistanceTo(hit.hitVec);if(d<nearest){nearest=d;selected=n;}}return selected;
    }
    private static AxisAlignedBB bounds(NBTTagCompound n){return new AxisAlignedBB(BlockPos.fromLong(n.getLong("A")),BlockPos.fromLong(n.getLong("B"))).expand(1,1,1);}
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void mouse(MouseEvent event){
        var mc=Minecraft.getMinecraft();if(event.getButton()!=0||!event.isButtonstate()||mc.player==null||mc.currentScreen!=null||mc.player.getHeldItemMainhand().getItem()!=Create.SUPER_GLUE)return;
        var stack=mc.player.getHeldItemMainhand();if(stack.hasTagCompound()&&stack.getTagCompound().hasKey("FirstBlock"))return;
        var n=selected();if(n==null)return;var request=new NBTTagCompound();if(n.hasUniqueId("Id"))request.setUniqueId("Remove",n.getUniqueId("Id"));else{request.setLong("RemoveA",n.getLong("A"));request.setLong("RemoveB",n.getLong("B"));}Create.NETWORK.sendToServer(new GlueDataPacket(request));event.setCanceled(true);
    }
    @SubscribeEvent public static void render(RenderWorldLastEvent event){
        var mc=Minecraft.getMinecraft();if(mc.player==null||mc.world==null||mc.currentScreen!=null||mc.player.getHeldItemMainhand().getItem()!=Create.SUPER_GLUE)return;
        var stack=mc.player.getHeldItemMainhand();var hit=mc.objectMouseOver;
        WorldVisuals.begin(event.getPartialTicks());
        try{
            if(stack.hasTagCompound()&&stack.getTagCompound().hasKey("FirstBlock")&&stack.getTagCompound().getInteger("GlueDimension")==mc.player.dimension){
                BlockPos first=BlockPos.fromLong(stack.getTagCompound().getLong("FirstBlock"));WorldVisuals.outline(new AxisAlignedBB(first).grow(.002),1/64d,0x4D9162);
                if(hit==null||hit.typeOfHit!=RayTraceResult.Type.BLOCK)return;BlockPos second=hit.getBlockPos();
                if(!first.equals(cachedFirst)||!second.equals(cachedSecond)||mc.world.getTotalWorldTime()%10==0){
                    cachedFirst=first;cachedSecond=second;try{var group=GlueSelection.collect(GlueWorldData.pos(first),GlueWorldData.pos(second),p->!mc.world.isAirBlock(GlueWorldData.block(p))&&mc.world.getBlockState(GlueWorldData.block(p)).getMaterial().isSolid());reachable=group.size()>1&&group.contains(GlueWorldData.pos(second));}catch(IllegalArgumentException invalid){reachable=false;}
                }
                int color=reachable&&!mc.player.isSneaking()?0x68c586:0xc5b548;var box=new AxisAlignedBB(first,second).expand(1,1,1).grow(.002);WorldVisuals.textured(box,"glue",color,.4f,false);WorldVisuals.outline(box,1/16d,color);
            }else if(saved.getInteger("Dimension")==mc.player.dimension){
                var selected=selected();var list=saved.getTagList("Areas",10);
                for(int i=0;i<list.tagCount();i++){var n=list.getCompoundTagAt(i);boolean active=n==selected;var box=bounds(n).grow(.002);if(active)WorldVisuals.textured(box,"glue",0x68c586,.35f,false);WorldVisuals.outline(box,active?1/16d:1/64d,active?0x68c586:0x4D9162);}
            }
        }finally{WorldVisuals.end();}
    }
}
