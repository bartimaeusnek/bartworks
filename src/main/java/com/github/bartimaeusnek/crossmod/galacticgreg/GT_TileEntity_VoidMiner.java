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

package com.github.bartimaeusnek.crossmod.galacticgreg;

import bloodasp.galacticgreg.GT_Worldgen_GT_Ore_Layer_Space;
import bloodasp.galacticgreg.GT_Worldgen_GT_Ore_SmallPieces_Space;
import bloodasp.galacticgreg.GalacticGreg;
import bloodasp.galacticgreg.api.ModDimensionDef;
import gregtech.api.GregTech_API;
import gregtech.api.enums.GT_Values;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.objects.XSTR;
import gregtech.common.GT_Worldgen_GT_Ore_Layer;
import gregtech.common.GT_Worldgen_GT_Ore_SmallPieces;
import gregtech.common.tileentities.machines.multi.GT_MetaTileEntity_DrillerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static bloodasp.galacticgreg.registry.GalacticGregRegistry.getModContainers;

@SuppressWarnings("unused")
public class GT_TileEntity_VoidMiner extends GT_MetaTileEntity_DrillerBase {

    private HashMap<Short,Float> dropmap = null;
    private float dividor;

    public GT_TileEntity_VoidMiner(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_TileEntity_VoidMiner(String aName) {
        super(aName);
    }

    @Override
    protected ItemList getCasingBlockItem() {
        return ItemList.Casing_Coil_Superconductor;
    }

    @Override
    protected Materials getFrameMaterial() {
        return Materials.Neutronium;
    }

    @Override
    protected int getCasingTextureIndex() {
        return 0;
    }

    @Override
    protected int getMinTier() {
        return 8;
    }

    @Override
    protected boolean checkHatches() {
        return false;
    }

    @Override
    protected void setElectricityStats() {
        try {
            this.mEUt = this.isPickingPipes ? 60 : Math.toIntExact(GT_Values.V[this.getMinTier()]);
        } catch (ArithmeticException e) {
            e.printStackTrace();
            this.mEUt = Integer.MAX_VALUE - 7;
        }
        this.mOutputItems = new ItemStack[0];
        this.mProgresstime = 0;
        this.mMaxProgresstime = 20;
        this.mEfficiency = this.getCurrentEfficiency(null);
        this.mEfficiencyIncrease = 10000;
    }

    private HashMap<Short,Float> getDropMapVanilla(){
        HashMap<Short, Float> dropmap = new HashMap<>();
        Predicate<GT_Worldgen_GT_Ore_Layer> world;
        Predicate<GT_Worldgen_GT_Ore_SmallPieces> world2;
        switch (this.getBaseMetaTileEntity().getWorld().provider.dimensionId) {
            case -1:
                world = gt_worldgen -> gt_worldgen.mNether; break;
            case 0:
                world = gt_worldgen -> gt_worldgen.mOverworld; break;
            case 1:
                world = gt_worldgen -> gt_worldgen.mEnd || gt_worldgen.mEndAsteroid; break;
            default: throw new IllegalStateException();
        }
        switch (this.getBaseMetaTileEntity().getWorld().provider.dimensionId) {
            case -1:
                world2 = gt_worldgen -> gt_worldgen.mNether; break;
            case 0:
                world2 = gt_worldgen -> gt_worldgen.mOverworld; break;
            case 1:
                world2 = gt_worldgen -> gt_worldgen.mEnd; break;
            default: throw new IllegalStateException();
        }
        GT_Worldgen_GT_Ore_Layer.sList.parallelStream().filter(gt_worldgen -> gt_worldgen.mEnabled && world.test(gt_worldgen)).forEach(element -> {
                    dropmap.put(element.mPrimaryMeta, (float) element.mWeight);
                    dropmap.put(element.mSecondaryMeta, (float) element.mWeight);
                    dropmap.put(element.mSporadicMeta, (element.mWeight / 8f));
                    dropmap.put(element.mBetweenMeta, (element.mWeight / 8f));
                }
        );
        GT_Worldgen_GT_Ore_SmallPieces.sList.parallelStream().filter(gt_worldgen -> gt_worldgen.mEnabled && world2.test(gt_worldgen)).forEach(element ->
                    dropmap.put(element.mMeta, (float) element.mAmount)
        );
        return dropmap;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private HashMap<Short,Float> getDropMapSpace(){
        IChunkProvider provider = this.getBaseMetaTileEntity().getWorld().getChunkProvider();

        final ModDimensionDef finalDef = getModContainers().parallelStream()
                .flatMap(modContainer -> modContainer.getDimensionList().parallelStream())
                .filter(modDimensionDef -> modDimensionDef.getChunkProviderName().equals(provider.getClass().getName()))
                .findFirst().orElse(null);

        HashMap<Short, Float> dropmap = new HashMap<>();

        Set space = GalacticGreg.oreVeinWorldgenList.parallelStream()
                .filter(gt_worldgen -> gt_worldgen.mEnabled && gt_worldgen instanceof GT_Worldgen_GT_Ore_Layer_Space && ((GT_Worldgen_GT_Ore_Layer_Space) gt_worldgen).isEnabledForDim(finalDef))
                .collect(Collectors.toSet());

        space.parallelStream().forEach(
                element -> {
                    dropmap.put(((GT_Worldgen_GT_Ore_Layer_Space) element).mPrimaryMeta, (float) ((GT_Worldgen_GT_Ore_Layer_Space) element).mWeight);
                    dropmap.put(((GT_Worldgen_GT_Ore_Layer_Space) element).mSecondaryMeta, (float) ((GT_Worldgen_GT_Ore_Layer_Space) element).mWeight);
                    dropmap.put(((GT_Worldgen_GT_Ore_Layer_Space) element).mSporadicMeta, (((GT_Worldgen_GT_Ore_Layer_Space) element).mWeight/8f));
                    dropmap.put(((GT_Worldgen_GT_Ore_Layer_Space) element).mBetweenMeta, (((GT_Worldgen_GT_Ore_Layer_Space) element).mWeight/8f));
                }
        );

        space = GalacticGreg.smallOreWorldgenList.parallelStream()
                .filter(gt_worldgen -> gt_worldgen.mEnabled && gt_worldgen instanceof GT_Worldgen_GT_Ore_SmallPieces_Space && ((GT_Worldgen_GT_Ore_SmallPieces_Space) gt_worldgen).isEnabledForDim(finalDef))
                .collect(Collectors.toSet());

        space.parallelStream().forEach(
                element ->
                    dropmap.put(((GT_Worldgen_GT_Ore_SmallPieces_Space) element).mMeta, (float) ((GT_Worldgen_GT_Ore_SmallPieces_Space) element).mAmount)
        );

        return dropmap;
    }



    private int mapper(Map.Entry<Short,Float> entry) {
        int randomint = XSTR.XSTR_INSTANCE.nextInt(100);
        if ((int) ((entry.getValue() / dividor) * 100.0f) - randomint == 0)
            return entry.getKey();
        else
            return mapper(entry);
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        if (dropmap == null) {
            if (aBaseMetaTileEntity.getWorld().provider.dimensionId > 1 || aBaseMetaTileEntity.getWorld().provider.dimensionId < -1)
                dropmap = getDropMapSpace();
            else
                dropmap = getDropMapVanilla();

            dividor = 0.0f;

            for (Float e : dropmap.values()) {
                dividor = Math.max(e,dividor);
            }
        }
    }

    @Override
    protected boolean workingAtBottom(ItemStack aStack, int xDrill, int yDrill, int zDrill, int xPipe, int zPipe, int yHead, int oldYHead) {
        if (this.mProgresstime == 10){
            dropmap.entrySet().parallelStream().mapToInt(this::mapper).findAny().ifPresent( k ->
                    this.addOutput(new ItemStack(GregTech_API.sBlockOres1,1,k))
            );
        }
        return super.workingAtBottom(aStack, xDrill, yDrill, zDrill, xPipe, zPipe, yHead, oldYHead);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_TileEntity_VoidMiner(this.mName);
    }

    @Override
    public String[] getDescription() {
        return new String[0];
    }
}
