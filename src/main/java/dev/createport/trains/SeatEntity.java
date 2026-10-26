package dev.createport.trains;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
/** Invisible attachment for a seat that is still a world block. Carried seats use the carriage palette. */
public final class SeatEntity extends Entity {
    private BlockPos seatPos=BlockPos.ORIGIN;
    public SeatEntity(World world){super(world);setSize(.25f,.25f);noClip=true;}
    public void place(BlockPos pos){seatPos=pos;setPosition(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);}
    @Override protected void entityInit(){}
    @Override public void onUpdate(){super.onUpdate();if(!world.isRemote&&(getPassengers().isEmpty()||!(world.getBlockState(seatPos).getBlock() instanceof SeatBlock))){removePassengers();setDead();}}
    @Override public double getMountedYOffset(){return 0;}
    @Override public boolean canPassengerSteer(){return false;}
    @Override protected void writeEntityToNBT(NBTTagCompound n){n.setLong("Seat",seatPos.toLong());}
    @Override protected void readEntityFromNBT(NBTTagCompound n){place(BlockPos.fromLong(n.getLong("Seat")));}
}
