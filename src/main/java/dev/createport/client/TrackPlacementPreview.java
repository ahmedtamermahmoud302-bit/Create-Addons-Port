package dev.createport.client;
import dev.createport.Create;
import dev.createport.trains.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.relauncher.Side;
@Mod.EventBusSubscriber(modid=Create.ID,value=Side.CLIENT)
public final class TrackPlacementPreview {
    private static final int VALID=0x95CD41,INVALID=0xEA5C2B;
    private record Hint(BlockPos p,boolean valid){}
    private static List<Hint> hints=List.of();private static BlockPos cached;private static int angle;private static long tick;private static boolean sprint;
    @SubscribeEvent(priority=EventPriority.HIGH) public static void mouse(MouseEvent event){
        var mc=Minecraft.getMinecraft();if(event.getButton()!=1||!event.isButtonstate()||mc.player==null||mc.currentScreen!=null||mc.player.isSneaking())return;
        var stack=mc.player.getHeldItemMainhand();var hit=mc.objectMouseOver;
        if(stack.getItem() instanceof TrackItem item&&item.hasEffect(stack)&&hit!=null&&hit.typeOfHit==RayTraceResult.Type.BLOCK){Create.NETWORK.sendToServer(new TrackToolPacket(hit.getBlockPos(),hit.sideHit,mc.gameSettings.keyBindSprint.isKeyDown()));event.setCanceled(true);}
    }
    @SubscribeEvent public static void render(RenderWorldLastEvent event){
        var mc=Minecraft.getMinecraft();if(mc.player==null||mc.world==null||mc.currentScreen!=null)return;
        var stack=mc.player.getHeldItemMainhand();if(!(stack.getItem() instanceof TrackItem item)||!item.hasEffect(stack))return;
        var hit=mc.objectMouseOver;if(hit==null||hit.typeOfHit!=RayTraceResult.Type.BLOCK)return;
        BlockPos target=mc.world.getBlockState(hit.getBlockPos()).getBlock() instanceof TrainTrackBlock?hit.getBlockPos():mc.world.getBlockState(hit.getBlockPos()).getBlock().isReplaceable(mc.world,hit.getBlockPos())?hit.getBlockPos():hit.getBlockPos().offset(hit.sideHit);
        var preview=stack.copy();preview.getTagCompound().setBoolean("ExtendCurve",mc.gameSettings.keyBindSprint.isKeyDown());
        boolean valid=true;String message="Valid connection";TrackItem.Placement placement=null;
        try{placement=TrackItem.check(mc.world,preview,target,mc.player).placement();}catch(IllegalArgumentException error){valid=false;message=error.getMessage();try{placement=TrackItem.plan(mc.world,preview,target,mc.player);}catch(IllegalArgumentException ignored){}}
        int color=valid?VALID:INVALID;
        int currentAngle=Math.round(mc.player.rotationYaw/5);long now=mc.world.getTotalWorldTime();boolean currentSprint=mc.gameSettings.keyBindSprint.isKeyDown();
        if(!target.equals(cached)||currentAngle!=angle||currentSprint!=sprint||now-tick>=10){
            cached=target;angle=currentAngle;sprint=currentSprint;tick=now;var updated=new ArrayList<Hint>();
            if(hit.sideHit==net.minecraft.util.EnumFacing.UP)for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){BlockPos p=target.add(x,0,z);boolean okay=true;try{TrackItem.check(mc.world,preview,p,mc.player);}catch(IllegalArgumentException bad){okay=false;}updated.add(new Hint(p.down(),okay));}hints=List.copyOf(updated);
            mc.player.sendStatusMessage(new net.minecraft.util.text.TextComponentString(message).setStyle(new net.minecraft.util.text.Style().setColor(valid?net.minecraft.util.text.TextFormatting.GREEN:net.minecraft.util.text.TextFormatting.RED)),true);
        }
        WorldVisuals.begin(event.getPartialTicks());
        try{
            for(var hint:hints)WorldVisuals.top(hint.p(),hint.valid()?VALID:INVALID);
            if(placement!=null){var p=placement.plan();var c=p.curve();if(c==null)rails(p.a(),p.b(),p.axisA().unit(),color);else{
                rails(p.a(),c.a,p.axisA().unit(),color);rails(p.b(),c.b,p.axisB().unit(),color);
                TrackCurve.V prev=c.a,n=c.normal(0);for(int i=1;i<=c.segments();i++){double t=i/(double)c.segments();var q=c.point(t);var normal=c.normal(t);for(int sign:new int[]{-1,1})WorldVisuals.line(vector(prev.add(n.scale(sign*15/16d))).add(0,.25,0),vector(q.add(normal.scale(sign*15/16d))).add(0,.25,0),1/8d,color);prev=q;n=normal;}
            }}else{
                var from=stack.getTagCompound().getCompoundTag("ConnectingFrom");var a=CurveTrackEntity.vector(from,"End");WorldVisuals.line(vector(a).add(0,.25,0),new Vec3d(target).add(.5,.25,.5),1/8d,color);
            }
        }catch(IllegalArgumentException invalid){}finally{WorldVisuals.end();}
    }
    private static Vec3d vector(TrackCurve.V v){return new Vec3d(v.x(),v.y(),v.z());}
    private static void rails(TrackCurve.V a,TrackCurve.V b,TrackCurve.V axis,int color){var n=new TrackCurve.V(0,1,0).cross(axis).unit().scale(15/16d);for(int sign:new int[]{-1,1})WorldVisuals.line(vector(a.add(n.scale(sign))).add(0,.25,0),vector(b.add(n.scale(sign))).add(0,.25,0),1/8d,color);}
}
