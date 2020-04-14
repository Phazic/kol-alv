/**
 * Copyright (c) 2003, Spellcast development team
 * http://spellcast.dev.java.net/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  [1] Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  [2] Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *  [3] Neither the name "Spellcast development team" nor the names of
 *      its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.java.dev.spellcast.utilities;

import java.io.File;

/**
 * Formed after the same idea as {@code WindowConstants}, this contains common
 * constants needed by many of the utility-related classes. Any methods which
 * are used by multiple instances of a JComponent and have a non-class-specific
 * purpose should be placed into this class in order to simplify the overall
 * design of the system and to facilitate documentation.
 */
public final class UtilityConstants {
    public static final ClassLoader SYSTEM_CLASSLOADER = ClassLoader.getSystemClassLoader();

    public static final ClassLoader MAINCLASS_CLASSLOADER = net.java.dev.spellcast.utilities.UtilityConstants.class.getClassLoader();

    public static final File BASE_LOCATION = new File(System.getProperty("user.dir")).getAbsoluteFile();

    public static final File ROOT_LOCATION = new File(System.getProperty("user.home"),
                                                      ".ascensionLogVisualizer").getAbsoluteFile();

    public static final String ROOT_DIRECTORY = ROOT_LOCATION.getAbsolutePath();

    public static final String TEMP_DIRECTORY = "temp/";

    public static final String CACHE_DIRECTORY = "cache/";

    public static final String DATA_DIRECTORY = "alvdata/";

    public static final String LICENSE_DIRECTORY = "alvdata/license/";

    public static final String KOL_DATA_DIRECTORY = "alvdata/koldata/";

    public static final File TEMP_LOCATION = new File(ROOT_LOCATION, TEMP_DIRECTORY);

    public static final File CACHE_LOCATION = new File(ROOT_LOCATION, CACHE_DIRECTORY);

    public static final File DATA_LOCATION = new File(ROOT_LOCATION, DATA_DIRECTORY);

    public static final File LICENSE_LOCATION = new File(ROOT_LOCATION, LICENSE_DIRECTORY);

    public static final File KOL_DATA_LOCATION = new File(ROOT_LOCATION, KOL_DATA_DIRECTORY);

    // This class is not to be instanced.
    private UtilityConstants() {}
}
