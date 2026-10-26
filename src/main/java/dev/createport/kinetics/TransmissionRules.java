package dev.createport.kinetics;
/** RotationPropagator's cog contacts, gearbox face signs and analog chain modifiers. Axes: X=0,Y=1,Z=2. */
public final class TransmissionRules {
    public static double cogs(boolean largeFrom,boolean largeTo,int fromAxis,int toAxis,int dx,int dy,int dz){
        int[] d={dx,dy,dz};if(Math.max(Math.abs(dx),Math.max(Math.abs(dy),Math.abs(dz)))>1)return 0;
        if(largeFrom&&largeTo&&fromAxis!=toAxis){for(int axis=0;axis<3;axis++)if(axis==fromAxis||axis==toAxis){if(d[axis]==0)return 0;}else if(d[axis]!=0)return 0;return (d[fromAxis]>0)^(d[toAxis]>0)?-1:1;}
        if(fromAxis!=toAxis||d[fromAxis]!=0)return 0;
        if(largeFrom!=largeTo){for(int axis=0;axis<3;axis++)if(axis!=fromAxis&&Math.abs(d[axis])!=1)return 0;return largeFrom?-2:-.5;}
        return !largeFrom&&Math.abs(dx)+Math.abs(dy)+Math.abs(dz)==1?-1:0;
    }
    public static double gearbox(int sourceFace,int targetFace){if(sourceFace==targetFace)return 1;int sourceAxis=axis(sourceFace),targetAxis=axis(targetFace);return sourceAxis==targetAxis?-1:(sourceFace%2)==(targetFace%2)?-1:1;}
    private static int axis(int face){return switch(face/2){case 0->1;case 1->2;default->0;};}
    public static double chain(int power){if(power<0||power>15)throw new IllegalArgumentException("Invalid analog signal");return power==0?1:1+(power+1)/16d;}
    private TransmissionRules(){}
}
