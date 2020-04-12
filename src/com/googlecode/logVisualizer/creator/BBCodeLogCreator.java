package com.googlecode.logVisualizer.creator;

import java.util.Collections;
import java.util.Map;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.util.LogOutputFormat;

public class BBCodeLogCreator extends TextLogCreator {

    public BBCodeLogCreator(LogDataHolder logData) {
        super(logData);
        // TODO Auto-generated constructor stub
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
