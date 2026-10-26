package dev.createport.client;
import dev.createport.trains.TrainCasingBlock;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.common.property.IExtendedBlockState;
/** Remaps original casing quads to the source connected sprite, retaining native lighting. */
public final class CasingModel extends BakedModelWrapper<IBakedModel> {
    private final TextureAtlasSprite top,side;
    private final Map<String,List<BakedQuad>> cache=new HashMap<>();
    public CasingModel(IBakedModel original,TextureAtlasSprite top,TextureAtlasSprite side){super(original);this.top=top;this.side=side;}
    public IBakedModel forState(IBlockState state){return new BakedModelWrapper<IBakedModel>(this){@Override public List<BakedQuad> getQuads(IBlockState ignored,EnumFacing face,long rand){return originalModel.getQuads(state,face,rand);}};}
    @Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing face,long rand){
        if(!(state instanceof IExtendedBlockState extended))return originalModel.getQuads(state,face,rand);
        var connections=extended.getValue(TrainCasingBlock.CONNECTIONS);if(connections==null)return originalModel.getQuads(extended.getClean(),face,rand);
        String key=(face==null?"all":face.name())+connections.tiles();
        return cache.computeIfAbsent(key,k->{
            List<BakedQuad> result=new ArrayList<>();
            for(var quad:originalModel.getQuads(extended.getClean(),face,rand)){
                int tile=connections.tiles().get(quad.getFace().getIndex());TextureAtlasSprite target=quad.getFace().getAxis()==EnumFacing.Axis.Y?top:side;
                int[] vertices=quad.getVertexData().clone();int stride=vertices.length/4;var source=quad.getSprite();
                for(int i=0;i<4;i++){
                    float u=Float.intBitsToFloat(vertices[i*stride+4]),v=Float.intBitsToFloat(vertices[i*stride+5]);
                    double un=(u-source.getMinU())/(source.getMaxU()-source.getMinU()),vn=(v-source.getMinV())/(source.getMaxV()-source.getMinV());
                    vertices[i*stride+4]=Float.floatToRawIntBits(target.getInterpolatedU(((tile%8)+un)*2));
                    vertices[i*stride+5]=Float.floatToRawIntBits(target.getInterpolatedV(((tile/8)+vn)*2));
                }
                result.add(new BakedQuad(vertices,quad.getTintIndex(),quad.getFace(),target,quad.shouldApplyDiffuseLighting(),quad.getFormat()));
            }return List.copyOf(result);
        });
    }
}
