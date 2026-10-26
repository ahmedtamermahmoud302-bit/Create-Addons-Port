package dev.createport.trains;

import java.util.ArrayList;
import java.util.List;

/** Yaw-oriented block footprints, clipped before exposing them to 1.12's AABB solver. */
public final class PlatformGeometry {
    public record Point(double x,double z){}
    public record Box(double minX,double minY,double minZ,double maxX,double maxY,double maxZ){}
    public static Point world(double x,double z,double ox,double oz,double yaw){double a=Math.toRadians(yaw),c=Math.cos(a),s=Math.sin(a);return new Point(ox+c*x-s*z,oz+s*x+c*z);}
    public static Point local(double x,double z,double ox,double oz,double yaw){return world(x-ox,z-oz,0,0,-yaw);}
    public static Box bounds(Box box,double x,double y,double z,double yaw){
        List<Point> p=polygon(box,x,z,yaw);return envelope(p,box.minY+y,box.maxY+y);
    }
    public static Box clip(Box box,double x,double y,double z,double yaw,Box query){
        if(box.maxY+y<query.minY||box.minY+y>query.maxY)return null;
        List<Point> p=polygon(box,x,z,yaw);
        p=clip(p,0,query.minX,true);p=clip(p,0,query.maxX,false);p=clip(p,1,query.minZ,true);p=clip(p,1,query.maxZ,false);
        if(p.size()<3)return null;Box result=envelope(p,box.minY+y,box.maxY+y);
        return result.maxX-result.minX<1e-8||result.maxZ-result.minZ<1e-8?null:result;
    }
    private static List<Point> polygon(Box b,double x,double z,double yaw){return List.of(world(b.minX,b.minZ,x,z,yaw),world(b.maxX,b.minZ,x,z,yaw),world(b.maxX,b.maxZ,x,z,yaw),world(b.minX,b.maxZ,x,z,yaw));}
    private static Box envelope(List<Point> p,double minY,double maxY){double x=Double.POSITIVE_INFINITY,z=x,X=-x,Z=-x;for(Point v:p){x=Math.min(x,v.x);X=Math.max(X,v.x);z=Math.min(z,v.z);Z=Math.max(Z,v.z);}return new Box(x,minY,z,X,maxY,Z);}
    private static List<Point> clip(List<Point> points,int axis,double boundary,boolean greater){
        List<Point> result=new ArrayList<>();if(points.isEmpty())return result;Point prev=points.get(points.size()-1);boolean prevInside=inside(prev,axis,boundary,greater);
        for(Point p:points){boolean nextInside=inside(p,axis,boundary,greater);if(prevInside!=nextInside){double a=axis==0?prev.x:prev.z,b=axis==0?p.x:p.z,t=(boundary-a)/(b-a);result.add(new Point(prev.x+(p.x-prev.x)*t,prev.z+(p.z-prev.z)*t));}if(nextInside)result.add(p);prev=p;prevInside=nextInside;}return result;
    }
    private static boolean inside(Point p,int axis,double boundary,boolean greater){double v=axis==0?p.x:p.z;return greater?v>=boundary:v<=boundary;}
    private PlatformGeometry(){}
}
