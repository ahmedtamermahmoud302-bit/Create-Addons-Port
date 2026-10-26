package dev.createport.trains;
/** Create 6.0.8 AllCTTypes.OMNIDIRECTIONAL's 8 x 8 sheet selection. */
public final class CasingCT {
    public static int index(int mask){
        boolean up=(mask&1)!=0,down=(mask&2)!=0,left=(mask&4)!=0,right=(mask&8)!=0;
        boolean tl=up&&left&&(mask&16)!=0,tr=up&&right&&(mask&32)!=0,bl=down&&left&&(mask&64)!=0,br=down&&right&&(mask&128)!=0;
        int x=(up?1:0)+(down?2:0),y=(left?1:0)+(right?2:0),borders=(up?0:1)+(down?0:1)+(left?0:1)+(right?0:1);
        if(borders==0){x+=(tr?1:0)+(tl?2:0);y+=(br?2:0)+(bl?1:0);}
        if(borders==1){
            if(!right&&(tl||bl)){y=4;x=-1+(bl?1:0)+(tl?2:0);}
            if(!left&&(tr||br)){y=5;x=-1+(br?1:0)+(tr?2:0);}
            if(!down&&(tl||tr)){y=6;x=-1+(tl?1:0)+(tr?2:0);}
            if(!up&&(bl||br)){y=7;x=-1+(bl?1:0)+(br?2:0);}
        }
        if(borders==2 && (up&&left&&tl || down&&left&&bl || up&&right&&tr || down&&right&&br))x+=3;
        return x+8*y;
    }
    private CasingCT(){}
}
