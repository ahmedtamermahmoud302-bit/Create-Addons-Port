package dev.createport.trains;

import dev.createport.trains.TrackCurve.V;

/** Flat-track part of Create 6.0.8 TrackPlacement: endpoint correction, standard radii and Sprint. */
public final class TrackPlacement {
    public record Plan(V a,V b,V axisA,V axisB,int extendA,int extendB,boolean straight){
        public TrackCurve curve(){return straight?null:new TrackCurve(a.add(axisA.scale(extendA)),b.add(axisB.scale(extendB)),axisA,axisB);}
    }
    private static double[] intersect(V a,V b,V d,V e){
        double det=d.x()*e.z()-d.z()*e.x();if(Math.abs(det)<1e-8)return null;V v=b.sub(a);
        return new double[]{(v.x()*e.z()-v.z()*e.x())/det,(v.x()*d.z()-v.z()*d.x())/det};
    }
    private static V raw(V axis){return axis.scale(1/Math.max(Math.abs(axis.x()),Math.abs(axis.z())));}
    public static Plan connect(V centerA,V centerB,V selectedAxis,V targetAxis,boolean maximise){
        V axisA=raw(selectedAxis),axisB=raw(targetAxis),a=centerA.add(axisA.scale(.5)),b=centerB.add(axisB.scale(.5));
        if(axisA.dot(b.sub(a))<0){axisA=axisA.scale(-1);a=centerA.add(axisA.scale(.5));}
        V d=axisA.unit(),e=axisB.unit();double[] intersection=intersect(a,b,d,e);boolean parallel=intersection==null;
        if(parallel && d.dot(e)>0 || !parallel && (intersection[0]<0 || intersection[1]<0)){axisB=axisB.scale(-1);e=e.scale(-1);b=centerB.add(axisB.scale(.5));}
        double ascend=Math.abs(b.y()-a.y());int first=0,second=0;
        if(parallel){
            double[] s=intersect(a,b,d,e.cross(new V(0,1,0)));
            if(s==null)throw new IllegalArgumentException("Track axes do not define a connection");
            double t=Math.abs(s[0]),u=Math.abs(s[1]);
            if(u<1e-6 && ascend<1e-6)return new Plan(a,b,axisA,axisB,(int)Math.round(centerA.sub(centerB).length()/axisA.length()),0,true);
            if(u>=1e-6){
                if(s[0]<0)throw new IllegalArgumentException("The tracks are perpendicular to this connection");
                if(ascend>1e-6 || Math.abs(d.y())>1e-6)throw new IllegalArgumentException("An S bend must be level");
                double required=u<=1?3:u*2;if(t<required)throw new IllegalArgumentException("The S bend is too sharp");
                int correction=(int)((t-required)/axisA.length());if(!maximise){first=correction/2+correction%2;second=correction/2;}
            }else{
                double h=centerA.sub(centerB).length()/axisA.length()+1,required=Math.max(ascend<4?ascend*4:ascend*3,6)/axisA.length();
                if(h<required)throw new IllegalArgumentException("The track is too steep");
                int correction=(int)(h-required);if(!maximise){first=correction/2+correction%2;second=correction/2;}
            }
        }else{
            double degrees=Math.abs(Math.toDegrees(Math.atan2(e.z(),e.x())-Math.atan2(d.z(),d.x())));
            if(degrees<60 || degrees>300)throw new IllegalArgumentException("A track cannot make this turn");
            intersection=intersect(a,b,d,e);if(intersection[0]<0 || intersection[1]<0)throw new IllegalArgumentException("The turn is too sharp");
            double t=intersection[0],u=intersection[1],size=Math.min(t,u)-.1;
            boolean ninety=(degrees+.25)%90<1;double minimum=ninety?7:3.25;
            double required=minimum+(ninety?Math.max(0,ascend-3)*2:Math.max(0,ascend-1.5)*1.5);
            if(size<minimum)throw new IllegalArgumentException("The turn is too sharp");if(size<required)throw new IllegalArgumentException("The turn is too steep");
            first=(int)Math.floor((Math.max(0,t-u)+(maximise?0:size-required))/axisA.length());
            second=(int)Math.floor((Math.max(0,u-t)+(maximise?0:size-required))/axisB.length());
        }
        return new Plan(a,b,axisA,axisB,first,second,false);
    }
    private TrackPlacement(){}
}
