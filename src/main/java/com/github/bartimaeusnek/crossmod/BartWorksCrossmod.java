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

package com.github.bartimaeusnek.crossmod;

import com.github.bartimaeusnek.bartworks.API.LoaderReference;
import com.github.bartimaeusnek.bartworks.MainMod;
import com.github.bartimaeusnek.crossmod.GTpp.loader.RadioHatchCompat;
import com.github.bartimaeusnek.crossmod.galacticraft.GalacticraftProxy;
import com.github.bartimaeusnek.crossmod.tectech.TecTechResearchLoader;
import com.github.bartimaeusnek.crossmod.tectech.tileentites.multi.GT_Replacement.*;
import com.github.bartimaeusnek.crossmod.tgregworks.MaterialsInjector;
import com.github.bartimaeusnek.crossmod.thaumcraft.CustomAspects;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.util.StringTranslate;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.StringReader;

@Mod(
        modid = BartWorksCrossmod.MOD_ID, name = BartWorksCrossmod.NAME, version = BartWorksCrossmod.VERSION,
        dependencies = "required-after:IC2; "
                + "required-after:gregtech; "
                + "required-after:bartworks;"
                + "before:TGregworks; "
                + "after:GalacticraftMars; "
                + "after:GalacticraftCore; "
                + "after:Micdoodlecore; "
                + "after:miscutils;"
                + "after:EMT;"
                + "after:tectech;"
)
public class BartWorksCrossmod {
    public static final String NAME = "BartWorks Mod Additions";
    public static final String VERSION = MainMod.VERSION;
    public static final String MOD_ID = "bartworkscrossmod";
    public static final Logger LOGGER = LogManager.getLogger(BartWorksCrossmod.NAME);

    @Mod.Instance(BartWorksCrossmod.MOD_ID)
    public static BartWorksCrossmod instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent preinit) {
//        if (LoaderReference.appliedenergistics2)
//            new ItemSingleItemStorageCell("singleItemStorageCell");
        if (LoaderReference.GalacticraftCore)
            GalacticraftProxy.preInit(preinit);
        if (LoaderReference.Thaumcraft)
            new CustomAspects();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent init) {
        if (LoaderReference.GalacticraftCore)
            GalacticraftProxy.init(init);
        //Base GT -> TT Replacement
        if (LoaderReference.tectech) {
            new TT_VacuumFreezer(null,null);
            new TT_OilCrackingUnit(null,null);
            new TT_ImplosionCompressor(null,null);
            new TT_ElectronicBlastFurnace(null,null);
            new TT_MultiSmelter(null,null);
            new TT_PyrolyseOven(null, null);

            new BW_TT_HeatExchanger(null, null);
        }
        if (LoaderReference.TGregworks)
            MaterialsInjector.run();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent init) {
        if (LoaderReference.GalacticraftCore)
            GalacticraftProxy.postInit(init);
        if (LoaderReference.miscutils)
            RadioHatchCompat.run();
        if (LoaderReference.tectech)
            TecTechResearchLoader.runResearches();
    }

//    @Mod.EventHandler
//    public void onFMLMissingMappingsEvent(FMLMissingMappingsEvent event){
//        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()){
//            if (mapping.name.equalsIgnoreCase())
//        }
//    }


//    @Mod.EventHandler
//    public void onServerStarted(FMLServerStartedEvent event) {
//        if (LoaderReference.EMT){
//            try {
//                TCRecipeHandler.init();
//            } catch (IllegalAccessException | InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Mod.EventHandler
    public void onFMLServerStart(FMLServerStartingEvent event) {
        if (LoaderReference.miscutils)
            for (Object s : RadioHatchCompat.TranslateSet){
                StringTranslate.inject(new ReaderInputStream(new StringReader((String) s)));
            }
    }
}
