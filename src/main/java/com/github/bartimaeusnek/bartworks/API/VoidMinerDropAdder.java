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

import com.github.bartimaeusnek.bartworks.system.material.Werkstoff;
import com.github.bartimaeusnek.bartworks.util.Pair;
import com.google.common.collect.ArrayListMultimap;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.ISubTagContainer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class VoidMinerDropAdder {

   static Method getExtraDropsDimMap;

   static{
       try {
           getExtraDropsDimMap = Class.forName("com.github.bartimaeusnek.crossmod.galacticgreg.GT_TileEntity_VoidMiner").getMethod("getExtraDropsDimMap");
       } catch (NoSuchMethodException | ClassNotFoundException e) {
           e.printStackTrace();
       }
   }

    public static void addDropsToDim(int dimID, ISubTagContainer material, float chance) throws InvocationTargetException, IllegalAccessException {
        Pair<Integer,Boolean> stuffpair;
        if (material instanceof Werkstoff){
            stuffpair = new Pair<>((int) ((Werkstoff)material).getmID(),true);
        }
        else if (material instanceof Materials){
            stuffpair = new Pair<>(((Materials)material).mMetaItemSubID,true);
        }
        else
            throw new IllegalArgumentException("material neither an instance of Materials nor Werkstoff!");
        Pair<Pair<Integer,Boolean>,Float> chancepair = new Pair<>(stuffpair,chance);
        ((ArrayListMultimap) getExtraDropsDimMap.invoke(null)).put(dimID,chancepair);
    }

}
