/* Copyright (c) 2020-2020, developers of the Ascension Log Visualizer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.googlecode.alv.creator;

import java.util.Collections;

import com.googlecode.alv.logData.LogDataHolder;

/**
 * This class extends TextLogCreator so as to create BBCode log output.  It's
 * really very similar to TextLogCreator, except that BBCode augmentations are
 * used instead of text augmentations.
 */
public class BBCodeLogCreator extends TextLogCreator {

    /**
     * Create and return an instance of BBCodeLogCreator.
     * 
     * @param logData LogDataHolder containg the data to write.
     * @return New instance of BBCodeLogCreator.
     */
    public static TextLogCreator newTextLogCreator(LogDataHolder logData)
    {
        return new BBCodeLogCreator(logData);
    }

    /**
     * Creates a BBCodeLogCreator instance for further use.
     *
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     */
    public BBCodeLogCreator(LogDataHolder logData) {
        super(logData);
        // Nothing else to do for this class
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setAugmentationsMap()
    {
        logAdditionsMap = Collections.unmodifiableMap(readAugmentationsList("bbcodeAugmentations.txt"));
    }
    
    
}
