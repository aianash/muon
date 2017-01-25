package com.shoplane.muon.common.utils;

import java.util.Random;

/**
 * Created by ravmon on 1/10/15.
 */
public class UniqueIdGenerator {


    private static UniqueIdGenerator uniqueIdGeneratorInstance;

    Random mRandomLong;

    private UniqueIdGenerator() {
        this.mRandomLong = new Random();
    }

    public String getUniqueId() {
        return (mRandomLong.nextLong() + "");
    }

    public static synchronized UniqueIdGenerator getInstance() {
        if (null == uniqueIdGeneratorInstance) {
            uniqueIdGeneratorInstance = new UniqueIdGenerator();
        }
        return uniqueIdGeneratorInstance;
    }

}
