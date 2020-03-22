/*
 * Copyright (c) 2018-2020 bartimaeusnek
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.bartimaeusnek.bartworks.API;

import cpw.mods.fml.common.Loader;

public class LoaderReference {
    private LoaderReference(){}

    public static final boolean Natura = Loader.isModLoaded("Natura");
    public static final boolean RandomThings = Loader.isModLoaded("RandomThings");
    public static final boolean TConstruct = Loader.isModLoaded("TConstruct");
    public static final boolean chisel = Loader.isModLoaded("chisel");
    public static final boolean Railcraft = Loader.isModLoaded("Railcraft");
    public static final boolean Ztones = Loader.isModLoaded("Ztones");
    public static final boolean witchery = Loader.isModLoaded("witchery");
    public static final boolean GalaxySpace = Loader.isModLoaded("GalaxySpace");
    public static final boolean GalacticraftCore = Loader.isModLoaded("GalacticraftCore;");
    public static final boolean GalacticraftMars = Loader.isModLoaded("GalacticraftMars;");
    public static final boolean Thaumcraft = Loader.isModLoaded("Thaumcraft;");
    public static final boolean miscutils = Loader.isModLoaded("miscutils;");
    public static final boolean tectech = Loader.isModLoaded("tectech;");
    public static final boolean ExtraUtilities = Loader.isModLoaded("ExtraUtilities;");
    public static final boolean RWG = Loader.isModLoaded("RWG;");
    public static final boolean galacticgreg = Loader.isModLoaded("galacticgreg;");
    public static final boolean gendustry = Loader.isModLoaded("gendustry;");
    public static final boolean croploadcore = Loader.isModLoaded("croploadcore;");
    public static final boolean Forestry = Loader.isModLoaded("Forestry");
    public static final boolean berriespp = Loader.isModLoaded("berriespp");
    public static final boolean dreamcraft = Loader.isModLoaded("dreamcraft");
    public static final boolean BloodArsenal = Loader.isModLoaded("BloodArsenal");
    public static final boolean Botany = Loader.isModLoaded("Botany");
    public static final boolean EnderIO = Loader.isModLoaded("EnderIO");
    public static final boolean HardcoreEnderExpension = Loader.isModLoaded("HardcoreEnderExpension");
}
