package dev.createport.trains;

/** Create 6.0.8's handle rule, 16-step length estimate and two-segments-per-block render LUT.
 * The separate dense distance table is used by this port's server-owned carriage adapter. */
public final class TrackCurve {
    public record V(double x,double y,double z) {
        public V { if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IllegalArgumentException("Invalid track vector"); }
        public V add(V v){return new V(x+v.x,y+v.y,z+v.z);} public V sub(V v){return new V(x-v.x,y-v.y,z-v.z);}
        public V scale(double s){return new V(x*s,y*s,z*s);} public double dot(V v){return x*v.x+y*v.y+z*v.z;}
        public double length(){return Math.sqrt(dot(this));} public V unit(){double l=length();if(l<1e-9)throw new IllegalArgumentException("Zero tangent");return scale(1/l);}
        public V cross(V v){return new V(y*v.z-z*v.y,z*v.x-x*v.z,x*v.y-y*v.x);}
    }
    public final V a,b,c1,c2,axisA,axisB;
    public final double handle,length;
    private final double[] distances=new double[513];
    private final float[] lut;
    public TrackCurve(V a,V b,V outwardA,V outwardB) {
        this.a=a;this.b=b;axisA=outwardA.unit();axisB=outwardB.unit();
        if(a.sub(b).length()<.25 || a.sub(b).length()>128)throw new IllegalArgumentException("Track connection out of range");
        handle=handles(a,b,axisA,axisB);c1=a.add(axisA.scale(handle));c2=b.add(axisB.scale(handle));
        V previous=a;double measured=0;
        for(int i=1;i<=16;i++){V p=point(i/16d);measured+=p.sub(previous).length();previous=p;}
        length=measured;int count=Math.max(1,(int)(length*2));lut=new float[count+1];lut[0]=1;
        previous=a;float sum=0;
        for(int i=1;i<=count;i++){float t=i/(float)count;V p=point(t);sum+=p.sub(previous).length()/length;lut[i]=t/sum;previous=p;}
        previous=a;for(int i=1;i<distances.length;i++){V p=point(i/512d);distances[i]=distances[i-1]+p.sub(previous).length();previous=p;}
    }
    public int segments(){return lut.length-1;}
    public double renderT(int i){return i==segments()?1:i*lut[i]/segments();}
    public V point(double t){double u=1-t;return a.scale(u*u*u).add(c1.scale(3*u*u*t)).add(c2.scale(3*u*t*t)).add(b.scale(t*t*t));}
    public V tangent(double t){double u=1-t;return c1.sub(a).scale(3*u*u).add(c2.sub(c1).scale(6*u*t)).add(b.sub(c2).scale(3*t*t)).unit();}
    public V normal(double t){return new V(0,1,0).cross(tangent(t)).unit();}
    public double distanceLength(){return distances[512];}
    public double parameterAtDistance(double distance){
        double s=Math.max(0,Math.min(distanceLength(),distance));int lo=0,hi=512;
        while(lo+1<hi){int mid=(lo+hi)/2;if(distances[mid]<s)lo=mid;else hi=mid;}
        double span=distances[hi]-distances[lo];return (lo+(span==0?0:(s-distances[lo])/span))/512;
    }
    private static double[] intersect(V p,V q,V d,V e){
        double det=d.x*e.z-d.z*e.x;if(Math.abs(det)<1e-8)return null;V v=q.sub(p);
        return new double[]{(v.x*e.z-v.z*e.x)/det,(v.x*d.z-v.z*d.x)/det};
    }
    private static double handles(V a,V b,V d,V e){
        V up=new V(0,1,0),cross1=d.cross(up),cross2=e.cross(up);
        double angle=(Math.atan2(-e.z,-e.x)-Math.atan2(d.z,d.x)+Math.PI*2)%(Math.PI*2);if(angle>Math.PI)angle=Math.PI*2-angle;
        if(Math.abs(angle)<1e-6){
            double[] intersection=intersect(a,b,d,cross2);
            if(intersection!=null){double min=Math.min(Math.abs(intersection[0]),Math.abs(intersection[1])),max=Math.max(Math.abs(intersection[0]),Math.abs(intersection[1]));if(min>1.2 && max/min>1 && max/min<3)return max-min;}
            return b.sub(a).length()/3;
        }
        double[] intersection=intersect(a,b,cross1,cross2);if(intersection==null)return b.sub(a).length()/3;
        double h=Math.abs(intersection[1])*4/3*Math.tan(angle/4);return Math.abs(h)<1e-6 ? 1:h;
    }
}
