package com.misfit.ble.sample.utils;

/**
 * Created by willhou on 11/25/15.
 */
public class StopWatch {
    long lastTimeForPeroid;
    long lastTime;

    boolean isFirst;

    public StopWatch() {
        lastTime = 0;
        isFirst = true;
    }

    public long click(){
        if(isFirst){
            lastTime = System.currentTimeMillis();
            isFirst = false;
            return -1;
        }else{
            long now = System.currentTimeMillis();
            long dt = now - lastTime;
            lastTime = now;
            return dt;
        }
    }

    public void start(){
        lastTimeForPeroid = System.currentTimeMillis();
    }

    public long stop(){
        return System.currentTimeMillis()-lastTimeForPeroid;
    }

}
