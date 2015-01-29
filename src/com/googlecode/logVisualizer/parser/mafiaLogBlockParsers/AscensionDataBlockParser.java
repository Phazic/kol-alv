/* Copyright (c) 2008-2011, developers of the Ascension Log Visualizer
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

package com.googlecode.logVisualizer.parser.mafiaLogBlockParsers;

import java.util.List;

import com.googlecode.logVisualizer.logData.LogDataHolder;
import com.googlecode.logVisualizer.logData.LogDataHolder.AscensionPath;
import com.googlecode.logVisualizer.logData.LogDataHolder.CharacterClass;
import com.googlecode.logVisualizer.logData.LogDataHolder.GameMode;

/**
 * A parser for the ascension data found at the start of mafia logs.
 * <p>
 * The format of the first line looks like this:
 * <p>
 * {@code Ascension #_ascensionNumber_:}
 */
public final class AscensionDataBlockParser implements LogBlockParser {

    public void parseBlock(
                           final List<String> block, final LogDataHolder logData) {
        if (logData.getCharacterClass() == CharacterClass.NOT_DEFINED)
            classCheck: {
                for (final String line : block)
                    for (final CharacterClass clazz : CharacterClass.values())
                        if (line.endsWith(clazz.toString())) {
                            logData.setCharacterClass(clazz);
                            break classCheck;
                        }
            }

        if (logData.getGameMode() == GameMode.NOT_DEFINED)
            modeCheck: {
                for (final String line : block)
                    for (final GameMode mode : GameMode.values())
                        if (line.startsWith(mode.toString())) {
                            logData.setGameMode(mode);
                            break modeCheck;
                        }
            }

        if (logData.getAscensionPath() == AscensionPath.NOT_DEFINED)
            pathCheck: {
                for (final String line : block)
                    for (final AscensionPath path : AscensionPath.values())
                        if (line.contains(path.toString())) {
                            logData.setAscensionPath(path);
                            break pathCheck;
                        }
            }
    }
}
