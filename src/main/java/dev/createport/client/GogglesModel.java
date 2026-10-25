package dev.createport.client;
import net.minecraft.client.renderer.block.model.*;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.apache.commons.lang3.tuple.Pair;
import javax.vecmath.Matrix4f;
/** Source-style perspective switch; vanilla's head layer supplies the worn transform. */
public final class GogglesModel extends BakedModelWrapper<IBakedModel> {
    private final IBakedModel worn;
    public GogglesModel(IBakedModel item,IBakedModel worn){super(item);this.worn=worn;}
    @Override public Pair<? extends IBakedModel,Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type){
        return (type==ItemCameraTransforms.TransformType.HEAD?worn:originalModel).handlePerspective(type);
    }
}
