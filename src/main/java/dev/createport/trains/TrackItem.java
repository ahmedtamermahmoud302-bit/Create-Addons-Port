package dev.createport.trains;

import dev.createport.Create;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/** Select an existing track end, then connect to a second track or a new endpoint. */
public final class TrackItem extends ItemBlock {
    public TrackItem(){super(Create.TRACK);setRegistryName(Create.TRACK.getRegistryName());}
    @Override public boolean hasEffect(ItemStack stack){return stack.hasTagCompound() && stack.getTagCompound().hasKey("ConnectingFrom");}
    public static TrackCurve.V axis(IBlockState state){
        RailPath.Point p=RailPath.of(TrainTrackBlock.pathShape(state)).tangent(.5);return new TrackCurve.V(p.x(),p.y(),p.z());
    }
    public static TrackCurve.V start(BlockPos pos,TrackCurve.V outward){double m=Math.max(Math.abs(outward.x()),Math.abs(outward.z()));TrackCurve.V raw=outward.scale(1/m);return new TrackCurve.V(pos.getX()+.5,pos.getY()+(Math.abs(raw.y())>1e-6?.5:0),pos.getZ()+.5).add(raw.scale(.5));}
    public record Placement(BlockPos first,BlockPos second,IBlockState firstState,IBlockState secondState,TrackPlacement.Plan plan){
        public java.util.Map<BlockPos,IBlockState> blocks(){
            var result=new java.util.LinkedHashMap<BlockPos,IBlockState>();
            for(int i=0;i<=plan.extendA();i++)result.put(offset(first,plan.axisA(),i),firstState);
            for(int i=0;i<=plan.extendB();i++)result.put(offset(second,plan.axisB(),i),secondState);
            return result;
        }
        public BlockPos anchor(boolean firstEnd){return firstEnd?offset(first,plan.axisA(),plan.extendA()):offset(second,plan.axisB(),plan.extendB());}
    }
    private static BlockPos offset(BlockPos p,TrackCurve.V axis,int length){return p.add(Math.round(axis.x()*length),Math.round(axis.y()*length),Math.round(axis.z()*length));}
    public static Placement plan(World world,ItemStack stack,BlockPos target,EntityPlayer player){
        NBTTagCompound n=stack.getTagCompound().getCompoundTag("ConnectingFrom");BlockPos first=BlockPos.fromLong(n.getLong("Pos"));
        if(!world.isBlockLoaded(first) || !(world.getBlockState(first).getBlock() instanceof TrainTrackBlock))throw new IllegalArgumentException("The selected track is unavailable");
        if(first.equals(target))throw new IllegalArgumentException("Choose a different track endpoint");
        IBlockState state=world.getBlockState(target),firstState=world.getBlockState(first);
        if(!(state.getBlock() instanceof TrainTrackBlock))state=Create.TRACK.getStateForPlacement(world,target,EnumFacing.UP,0,0,0,0,player);
        TrackCurve.V d=CurveTrackEntity.vector(n,"Axis"),e=axis(state);
        if(Math.abs(d.y())>1e-6 || Math.abs(e.y())>1e-6)throw new IllegalArgumentException("Slope transition placement is not available yet");
        Vec3d look=player.getLookVec();if(e.dot(new TrackCurve.V(look.x,look.y,look.z))>0)e=e.scale(-1);
        var plan=TrackPlacement.connect(new TrackCurve.V(first.getX()+.5,first.getY(),first.getZ()+.5),new TrackCurve.V(target.getX()+.5,target.getY(),target.getZ()+.5),d,e,player.isSprinting());
        if(first.distanceSq(target)>128*128)throw new IllegalArgumentException("Track connection out of range");
        return new Placement(first,target,firstState,state,plan);
    }
    public static TrackCurve preview(World world,ItemStack stack,BlockPos target,EntityPlayer player){return plan(world,stack,target,player).plan.curve();}
    @Override public ActionResult<ItemStack> onItemRightClick(World world,EntityPlayer player,EnumHand hand){
        ItemStack stack=player.getHeldItem(hand);if(player.isSneaking() && hasEffect(stack)){if(!world.isRemote)stack.getTagCompound().removeTag("ConnectingFrom");return new ActionResult<>(EnumActionResult.SUCCESS,stack);}return super.onItemRightClick(world,player,hand);
    }
    @Override public EnumActionResult onItemUse(EntityPlayer player,World world,BlockPos clicked,EnumHand hand,EnumFacing face,float x,float y,float z){
        ItemStack stack=player.getHeldItem(hand);if(hand==EnumHand.OFF_HAND)return super.onItemUse(player,world,clicked,hand,face,x,y,z);
        if(player.isSpectator() || !player.capabilities.allowEdit)return EnumActionResult.FAIL;
        if(hasEffect(stack) && player.isSneaking()){if(!world.isRemote)stack.getTagCompound().removeTag("ConnectingFrom");return EnumActionResult.SUCCESS;}
        if(!hasEffect(stack)){
            if(!(world.getBlockState(clicked).getBlock() instanceof TrainTrackBlock))return super.onItemUse(player,world,clicked,hand,face,x,y,z);
            if(!world.isRemote){
                TrackCurve.V axis=axis(world.getBlockState(clicked));Vec3d look=player.getLookVec();if(axis.dot(new TrackCurve.V(look.x,look.y,look.z))>0)axis=axis.scale(-1);
                NBTTagCompound tag=stack.getTagCompound();if(tag==null){tag=new NBTTagCompound();stack.setTagCompound(tag);}
                NBTTagCompound from=new NBTTagCompound();from.setLong("Pos",clicked.toLong());from.setInteger("Dimension",world.provider.getDimension());CurveTrackEntity.vector(from,"End",start(clicked,axis));CurveTrackEntity.vector(from,"Axis",axis);tag.setTag("ConnectingFrom",from);
            }
            return EnumActionResult.SUCCESS;
        }
        BlockPos target=world.getBlockState(clicked).getBlock() instanceof TrainTrackBlock ? clicked:world.getBlockState(clicked).getBlock().isReplaceable(world,clicked)?clicked:clicked.offset(face);
        if(world.isRemote)return EnumActionResult.SUCCESS;
        try{
            if(stack.getTagCompound().getCompoundTag("ConnectingFrom").getInteger("Dimension")!=world.provider.getDimension())throw new IllegalArgumentException("Choose endpoints in the same dimension");
            Placement placement=plan(world,stack,target,player);TrackCurve curve=placement.plan.curve();
            var blocks=placement.blocks();int blockCost=0;
            for(var entry:blocks.entrySet()){
                BlockPos p=entry.getKey();if(!world.isBlockLoaded(p) || !world.isBlockModifiable(player,p) || !player.canPlayerEdit(p,face,stack))throw new IllegalArgumentException("Track would enter protected or unloaded space");
                var existing=world.getBlockState(p);if(existing.equals(entry.getValue()))continue;
                if(!existing.getBlock().isReplaceable(world,p))throw new IllegalArgumentException("Track path is obstructed");blockCost++;
            }
            if(curve!=null){
                for(int i=0;i<=curve.segments();i++){
                    TrackCurve.V p=curve.point(curve.renderT(i));BlockPos cell=new BlockPos(p.x(),p.y()+1e-5,p.z());
                    if(!world.isBlockLoaded(cell) || !world.isBlockModifiable(player,cell) || !player.canPlayerEdit(cell,face,stack))throw new IllegalArgumentException("Track would enter protected or unloaded space");
                    if(!blocks.containsKey(cell) && !world.getBlockState(cell).getBlock().isReplaceable(world,cell))throw new IllegalArgumentException("Track path is obstructed");
                }
                if(CurveTrackEntity.at(world,curve.a)!=null || CurveTrackEntity.at(world,curve.b)!=null)throw new IllegalArgumentException("This endpoint already has a connection");
            }
            int cost=blockCost+(curve==null?0:(curve.segments()+1)/2),available=0;
            for(ItemStack item:player.inventory.mainInventory)if(item.getItem()==this)available+=item.getCount();
            if(!player.capabilities.isCreativeMode && available<cost)throw new IllegalArgumentException("Requires "+cost+" tracks");
            var previous=new java.util.LinkedHashMap<BlockPos,IBlockState>();
            for(var entry:blocks.entrySet()){
                if(world.getBlockState(entry.getKey()).equals(entry.getValue()))continue;
                previous.put(entry.getKey(),world.getBlockState(entry.getKey()));
                if(!world.setBlockState(entry.getKey(),entry.getValue(),3)){for(var old:previous.entrySet())world.setBlockState(old.getKey(),old.getValue(),3);return EnumActionResult.FAIL;}
            }
            if(curve!=null){CurveTrackEntity connection=new CurveTrackEntity(world);connection.configure(placement.anchor(true),placement.anchor(false),curve,!player.capabilities.isCreativeMode);CurveGraphData.get(world).add(world,connection);}
            if(!player.capabilities.isCreativeMode){int remaining=cost;for(ItemStack item:player.inventory.mainInventory)if(item.getItem()==this){int take=Math.min(remaining,item.getCount());item.shrink(take);remaining-=take;if(remaining==0)break;}}
            if(stack.hasTagCompound())stack.getTagCompound().removeTag("ConnectingFrom");
            world.playSound(null,target,net.minecraft.init.SoundEvents.BLOCK_METAL_PLACE,SoundCategory.BLOCKS,.8f,1);return EnumActionResult.SUCCESS;
        }catch(IllegalArgumentException invalid){player.sendStatusMessage(new TextComponentString(invalid.getMessage()),true);return EnumActionResult.FAIL;}
    }
}
