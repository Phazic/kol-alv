package com.googlecode.logVisualizer.creator;

import java.util.Collections;

import com.googlecode.logVisualizer.logData.LogDataHolder;

public class BBCodeLogCreator extends TextLogCreator {

    public BBCodeLogCreator(LogDataHolder logData) {
        super(logData);
        // Nothing else to do for this class
    }

    @Override
    protected void setAugmentationsMap()
    {
        logAdditionsMap = Collections.unmodifiableMap(readAugmentationsList("bbcodeAugmentations.txt"));
    }
    
    public static TextLogCreator newTextLogCreator(LogDataHolder logData)
    {
        return new BBCodeLogCreator(logData);
    }
    
}
