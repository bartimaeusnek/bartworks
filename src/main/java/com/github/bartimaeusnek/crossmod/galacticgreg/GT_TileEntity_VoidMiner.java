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
import bloodasp.galacticgreg.bartworks.BW_Worldgen_Ore_Layer_Space;
import bloodasp.galacticgreg.bartworks.BW_Worldgen_Ore_SmallOre_Space;
import com.github.bartimaeusnek.bartworks.common.configs.ConfigHandler;
import com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader;
import com.github.bartimaeusnek.bartworks.system.oregen.BW_OreLayer;
import com.github.bartimaeusnek.bartworks.system.oregen.BW_WorldGenRoss128b;
import com.github.bartimaeusnek.bartworks.system.oregen.BW_WorldGenRoss128ba;
import com.github.bartimaeusnek.bartworks.util.Pair;
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
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static bloodasp.galacticgreg.registry.GalacticGregRegistry.getModContainers;

@SuppressWarnings("unused")
public class GT_TileEntity_VoidMiner extends GT_MetaTileEntity_DrillerBase {

    private HashMap<Pair<Integer,Boolean>, Float> dropmap = null;
    private float totalWeight;

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
        return 8;
    }

    @Override
    protected int getMinTier() {
        return 8;
    }

    @Override
    protected boolean checkHatches() {
        return true;
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

    private void getDropMapVanilla() {
        Predicate<GT_Worldgen_GT_Ore_Layer> world;
        Predicate<GT_Worldgen_GT_Ore_SmallPieces> world2;
        switch (this.getBaseMetaTileEntity().getWorld().provider.dimensionId) {
            case -1:
                world = gt_worldgen -> gt_worldgen.mNether;
                break;
            case 0:
                world = gt_worldgen -> gt_worldgen.mOverworld;
                break;
            case 1:
                world = gt_worldgen -> gt_worldgen.mEnd || gt_worldgen.mEndAsteroid;
                break;
            default:
                throw new IllegalStateException();
        }
        switch (this.getBaseMetaTileEntity().getWorld().provider.dimensionId) {
            case -1:
                world2 = gt_worldgen -> gt_worldgen.mNether;
                break;
            case 0:
                world2 = gt_worldgen -> gt_worldgen.mOverworld;
                break;
            case 1:
                world2 = gt_worldgen -> gt_worldgen.mEnd;
                break;
            default:
                throw new IllegalStateException();
        }
        GT_Worldgen_GT_Ore_Layer.sList.stream().filter(gt_worldgen -> gt_worldgen.mEnabled && world.test(gt_worldgen)).forEach(element -> {
                    dropmap.put(new Pair<>((int) element.mPrimaryMeta,false), (float) element.mWeight);
                    dropmap.put(new Pair<>((int) element.mSecondaryMeta,false), (float) element.mWeight);
                    dropmap.put(new Pair<>((int) element.mSporadicMeta,false), (element.mWeight / 8f));
                    dropmap.put(new Pair<>((int) element.mBetweenMeta,false), (element.mWeight / 8f));
                }
        );
        GT_Worldgen_GT_Ore_SmallPieces.sList.stream().filter(gt_worldgen -> gt_worldgen.mEnabled && world2.test(gt_worldgen)).forEach(element ->
                dropmap.put(new Pair<>((int) element.mMeta,false), (float) element.mAmount)
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void getDropMapSpace() {
        ChunkProviderServer provider = (ChunkProviderServer) this.getBaseMetaTileEntity().getWorld().getChunkProvider();

        final ModDimensionDef finalDef = getModContainers().stream()
                .flatMap(modContainer -> modContainer.getDimensionList().stream())
                .filter(modDimensionDef -> modDimensionDef.getChunkProviderName().equals(provider.currentChunkProvider.getClass().getName()))
                .findFirst().orElse(null);

        Set space = GalacticGreg.oreVeinWorldgenList.stream()
                .filter(gt_worldgen -> gt_worldgen.mEnabled && gt_worldgen instanceof GT_Worldgen_GT_Ore_Layer_Space && ((GT_Worldgen_GT_Ore_Layer_Space) gt_worldgen).isEnabledForDim(finalDef))
                .collect(Collectors.toSet());

        space.forEach(
                element -> {
                    dropmap.put(new Pair<>((int) ((GT_Worldgen_GT_Ore_Layer_Space) element).mPrimaryMeta,false), (float) ((GT_Worldgen_GT_Ore_Layer_Space) element).mWeight);
                    dropmap.put(new Pair<>((int) ((GT_Worldgen_GT_Ore_Layer_Space) element).mSecondaryMeta,false), (float) ((GT_Worldgen_GT_Ore_Layer_Space) element).mWeight);
                    dropmap.put(new Pair<>((int) ((GT_Worldgen_GT_Ore_Layer_Space) element).mSporadicMeta,false), (((GT_Worldgen_GT_Ore_Layer_Space) element).mWeight / 8f));
                    dropmap.put(new Pair<>((int) ((GT_Worldgen_GT_Ore_Layer_Space) element).mBetweenMeta,false), (((GT_Worldgen_GT_Ore_Layer_Space) element).mWeight / 8f));
                }
        );

        space = GalacticGreg.smallOreWorldgenList.stream()
                .filter(gt_worldgen -> gt_worldgen.mEnabled && gt_worldgen instanceof GT_Worldgen_GT_Ore_SmallPieces_Space && ((GT_Worldgen_GT_Ore_SmallPieces_Space) gt_worldgen).isEnabledForDim(finalDef))
                .collect(Collectors.toSet());

        space.forEach(
                element ->
                        dropmap.put(new Pair<>((int) ((GT_Worldgen_GT_Ore_SmallPieces_Space) element).mMeta,false), (float) ((GT_Worldgen_GT_Ore_SmallPieces_Space) element).mAmount)
        );
    }

    private Pair<Integer,Boolean> getOreDamage() {
        int curentWeight = 0;
        while (true) {
            int randomeint = (Math.abs(XSTR.XSTR_INSTANCE.nextInt((int) Math.ceil(totalWeight))));
            for (Map.Entry<Pair<Integer,Boolean>, Float> entry : dropmap.entrySet()) {
                curentWeight += entry.getValue();
                if (randomeint < curentWeight)
                    return entry.getKey();
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void getDropMapBartworks(int aID) {
        Consumer<BW_OreLayer> addToList = element -> {
            List<Pair<Integer,Boolean>> data = element.getStacksRawData();
            for (int i = 0; i < data.size(); i++) {
                if (i < data.size()-1)
                    dropmap.put(data.get(i), (float) element.mWeight);
                else
                    dropmap.put(data.get(i), (element.mWeight/8f));
            }
        };

        if (aID == ConfigHandler.ross128BID)
            BW_WorldGenRoss128b.sList.forEach(addToList);
        else if (aID == ConfigHandler.ross128BAID)
            BW_WorldGenRoss128ba.sList.forEach(addToList);
        else {
            ChunkProviderServer provider = (ChunkProviderServer) this.getBaseMetaTileEntity().getWorld().getChunkProvider();

            final ModDimensionDef finalDef = getModContainers().stream()
                    .flatMap(modContainer -> modContainer.getDimensionList().stream())
                    .filter(modDimensionDef -> modDimensionDef.getChunkProviderName().equals(provider.currentChunkProvider.getClass().getName()))
                    .findFirst().orElse(null);

            Set space = GalacticGreg.oreVeinWorldgenList.stream()
                    .filter(gt_worldgen -> gt_worldgen.mEnabled && gt_worldgen instanceof BW_Worldgen_Ore_Layer_Space && ((BW_Worldgen_Ore_Layer_Space) gt_worldgen).isEnabledForDim(finalDef))
                    .collect(Collectors.toSet());

            space.forEach(addToList);

            space = GalacticGreg.smallOreWorldgenList.stream()
                    .filter(gt_worldgen -> gt_worldgen.mEnabled && gt_worldgen instanceof BW_Worldgen_Ore_SmallOre_Space && ((BW_Worldgen_Ore_SmallOre_Space) gt_worldgen).isEnabledForDim(finalDef))
                    .collect(Collectors.toSet());

            space.forEach(
                    element ->
                            dropmap.put(new Pair<>(((BW_Worldgen_Ore_SmallOre_Space) element).mPrimaryMeta,((BW_Worldgen_Ore_SmallOre_Space) element).bwOres != 0), (float) ((BW_Worldgen_Ore_SmallOre_Space) element).mDensity)
            );
        }
    }

    private void makeDropMap(){
        if (dropmap == null) {
            dropmap = new HashMap<>();
            int id = this.getBaseMetaTileEntity().getWorld().provider.dimensionId;
            if (id != ConfigHandler.ross128BID && id != ConfigHandler.ross128BAID) {
                if (id > 1 || id < -1)
                    getDropMapSpace();
                else
                    getDropMapVanilla();
            }

            getDropMapBartworks(id);
            totalWeight = 0.0f;

            for (Float f : dropmap.values()) {
                totalWeight += f;
            }
        }
    }

    @Override
    protected boolean workingAtBottom(ItemStack aStack, int xDrill, int yDrill, int zDrill, int xPipe, int zPipe, int yHead, int oldYHead) {
        makeDropMap();
        Pair<Integer,Boolean> stats = getOreDamage();
        this.addOutput(new ItemStack(stats.getValue() ? WerkstoffLoader.BWOres : GregTech_API.sBlockOres1, 1, stats.getKey()));
        this.updateSlots();
        return true;
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
