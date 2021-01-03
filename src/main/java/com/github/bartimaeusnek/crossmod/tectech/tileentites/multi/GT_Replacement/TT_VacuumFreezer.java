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

package com.github.bartimaeusnek.crossmod.tectech.tileentites.multi.GT_Replacement;

import com.github.bartimaeusnek.crossmod.tectech.helper.StructureDefinitions;
import com.github.technus.tectech.mechanics.structure.IStructureDefinition;
import com.github.technus.tectech.mechanics.structure.StructureDefinition;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_DynamoMulti;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_EnergyMulti;
import com.github.technus.tectech.thing.metaTileEntity.multi.base.render.TT_RenderedExtendedFacingTexture;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.*;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

import static com.github.bartimaeusnek.bartworks.util.BW_Tooltip_Reference.ADV_STR_CHECK;
import static com.github.bartimaeusnek.bartworks.util.BW_Tooltip_Reference.TT_BLUEPRINT;
import static com.github.technus.tectech.mechanics.structure.StructureUtility.*;

public class TT_VacuumFreezer extends TT_Abstract_GT_Replacement {
    public TT_VacuumFreezer(Object unused, Object unused2) {
        super(1002, "multimachine.vacuumfreezer", "Vacuum Freezer");
    }

    private TT_VacuumFreezer(String aName) {
        super(aName);
    }

    private static final byte TEXTURE_INDEX = 17;

    private byte blocks = 0;

    private static final IStructureDefinition<TT_VacuumFreezer> STRUCTURE_DEFINITION =
        StructureDefinition.<TT_VacuumFreezer>builder().addShape("main",
                StructureDefinitions.CUBE_NO_MUFFLER.getDefinition()
        ).addElement(
                'V',
                    ofChain(
                            onElementPass(
                                    x -> ++x.blocks,
                                    ofBlock(
                                            GregTech_API.sBlockCasings2,
                                            1
                                    )
                            ),
                            ofHatchAdder(
                                TT_VacuumFreezer::addVacuumFreezerHatches,
                                TEXTURE_INDEX,
                            1
                            )
                    )
        ).build();

    @Override
    public IStructureDefinition<TT_VacuumFreezer> getStructure_EM() {
        return STRUCTURE_DEFINITION;
    }

    private final static String[] desc = new String[]{
            "Vacuum Freezer",
            "Controller Block for the Vacuum Freezer",
            "Cools hot ingots and cells",
            ADV_STR_CHECK,
            TT_BLUEPRINT
    };

    private static final String[] sfStructureDescription = new String[] {
            "0 - Air",
            "Required: Output Bus, Input Bus, Energy Hatch, Maintenance Hatch"
    };

    @Override
    public String[] getStructureDescription(ItemStack itemStack) {
        return sfStructureDescription;
    }

    public String[] getDescription() {
        return desc;
    }

    @Override
    protected boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        this.blocks = 0;
        return this.structureCheck_EM("main", 1,1,0)
                && this.blocks >= 16;
    }

    @SideOnly(Side.CLIENT)
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            return new ITexture[]{Textures.BlockIcons.casingTexturePages[0][TEXTURE_INDEX], new TT_RenderedExtendedFacingTexture(aActive ? Textures.BlockIcons.OVERLAY_FRONT_VACUUM_FREEZER_ACTIVE : Textures.BlockIcons.OVERLAY_FRONT_VACUUM_FREEZER)};
        }
        return new ITexture[]{Textures.BlockIcons.casingTexturePages[0][TEXTURE_INDEX]};
    }

    @Override
    public boolean checkRecipe_EM(ItemStack itemStack) {
        ArrayList<ItemStack> tInputList = getStoredInputs();
        for (ItemStack tInput : tInputList) {
            long tVoltage = getMaxInputVoltage();
            byte tTier = (byte) Math.max(1, GT_Utility.getTier(tVoltage));

            GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sVacuumRecipes.findRecipe(getBaseMetaTileEntity(), false, gregtech.api.enums.GT_Values.V[tTier], null, tInput);
            if (tRecipe != null) {
                if (tRecipe.isRecipeInputEqual(true, null, tInput)) {
                    setEfficiencyAndOc(tRecipe);
                    //In case recipe is too OP for that machine
                    if (mMaxProgresstime == Integer.MAX_VALUE - 1 && mEUt == Integer.MAX_VALUE - 1)
                        return false;
                    if (this.mEUt > 0) {
                        this.mEUt = (-this.mEUt);
                    }
                    this.mMaxProgresstime = Math.max(1, this.mMaxProgresstime);
                    this.mOutputItems = new ItemStack[]{tRecipe.getOutput(0)};
                    updateSlots();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new TT_VacuumFreezer(this.mName);
    }

    @Override
    public void construct(ItemStack itemStack, boolean b) {
        this.structureBuild_EM("main", 1,1,0, b, itemStack);
    }

    public final boolean addVacuumFreezerHatches(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity != null) {
            IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
            if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch)
                ((GT_MetaTileEntity_Hatch) aMetaTileEntity).updateTexture(aBaseCasingIndex);
            else
                return false;

            if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_InputBus)
                return this.mInputBusses.add((GT_MetaTileEntity_Hatch_InputBus) aMetaTileEntity);
            else if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_OutputBus)
                return this.mOutputBusses.add((GT_MetaTileEntity_Hatch_OutputBus) aMetaTileEntity);
            else if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_Energy)
                return this.mEnergyHatches.add((GT_MetaTileEntity_Hatch_Energy) aMetaTileEntity);
            else if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_Maintenance)
                return this.mMaintenanceHatches.add((GT_MetaTileEntity_Hatch_Maintenance) aMetaTileEntity);
            else if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_EnergyMulti)
                return this.eEnergyMulti.add((GT_MetaTileEntity_Hatch_EnergyMulti) aMetaTileEntity);
            else if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_DynamoMulti)
                return this.eDynamoMulti.add((GT_MetaTileEntity_Hatch_DynamoMulti) aMetaTileEntity);
        }
        return false;
    }
}
