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

package com.github.bartimaeusnek.bartworks.common.tileentities.multis;

import com.github.bartimaeusnek.bartworks.common.items.SimpleSubItemClass;
import com.github.bartimaeusnek.bartworks.system.material.WerkstoffLoader;
import com.github.bartimaeusnek.bartworks.util.BW_Util;
import com.github.bartimaeusnek.bartworks.util.ChatColorHelper;
import com.github.bartimaeusnek.bartworks.util.MathUtils;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.GregTech_API;
import gregtech.api.enums.GT_Values;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.objects.GT_RenderedTexture;
import gregtech.api.objects.XSTR;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

public class GT_TileEntity_THTR extends GT_MetaTileEntity_MultiBlockBase {

    private static final int BASECASINGINDEX = 44;
    private static final int HELIUM_NEEDED = 730000;
    private int HeliumSupply;
    private int BISOPeletSupply, savedBISO;
    private int TRISOPeletSupply, savedTRISO;
    private boolean empty;

    public GT_TileEntity_THTR(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    private GT_TileEntity_THTR(String aName) {
        super(aName);
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack itemStack) {
        return true;
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        this.HeliumSupply = aNBT.getInteger("HeliumSupply");
        this.BISOPeletSupply = aNBT.getInteger("BISOPeletSupply");
        this.TRISOPeletSupply = aNBT.getInteger("TRISOPeletSupply");
        this.savedBISO = aNBT.getInteger("savedBISO");
        this.savedTRISO = aNBT.getInteger("savedTRISO");
        this.empty = aNBT.getBoolean("EmptyMode");
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setInteger("HeliumSupply", this.HeliumSupply);
        aNBT.setInteger("BISOPeletSupply", this.BISOPeletSupply);
        aNBT.setInteger("TRISOPeletSupply", this.TRISOPeletSupply);
        aNBT.setInteger("savedBISO", this.savedBISO);
        aNBT.setInteger("savedTRISO", this.savedTRISO);
        aNBT.setBoolean("EmptyMode", this.empty);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (aBaseMetaTileEntity.isServerSide()){
            if (this.HeliumSupply < GT_TileEntity_THTR.HELIUM_NEEDED){
                for (FluidStack fluidStack : this.getStoredFluids()){
                    if (fluidStack.isFluidEqual(Materials.Helium.getGas(1000))) {
                        while (this.HeliumSupply < GT_TileEntity_THTR.HELIUM_NEEDED && fluidStack.amount > 0) {
                            this.HeliumSupply++;
                            fluidStack.amount--;
                        }
                    }
                }
            }
            for (ItemStack itemStack : this.getStoredInputs()) {
                if (GT_Utility.areStacksEqual(itemStack, new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials, 1, 3))) {
                    if (this.BISOPeletSupply + this.TRISOPeletSupply < 675000) {
                        while (this.BISOPeletSupply + this.TRISOPeletSupply < 675000 && itemStack.stackSize > 0) {
                            itemStack.stackSize--;
                            this.TRISOPeletSupply++;
                        }
                        this.updateSlots();
                    }
                } else if (GT_Utility.areStacksEqual(itemStack, new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials, 1, 1))) {
                    if (this.BISOPeletSupply + this.TRISOPeletSupply < 675000) {
                        while (this.BISOPeletSupply + this.TRISOPeletSupply < 675000 && itemStack.stackSize > 0) {
                            itemStack.stackSize--;
                            this.BISOPeletSupply++;

                        }
                        this.updateSlots();
                    }
                }
            }
        }
    }

    @Override
    public boolean checkRecipe(ItemStack controllerStack) {

        if (!(this.HeliumSupply >= GT_TileEntity_THTR.HELIUM_NEEDED && this.BISOPeletSupply + this.TRISOPeletSupply >= 100000))
            return false;

        reduceSupply();
        addBurnedOutBalls();
        this.updateSlots();

        this.mOutputFluids = new FluidStack[]{FluidRegistry.getFluidStack("ic2hotcoolant",0)};

        this.mEUt=0;
        this.mMaxProgresstime=648000;
        return true;
    }

    private void reduceSupply() {
        int toReduce = new XSTR().nextInt(MathUtils.floorInt(((double)this.BISOPeletSupply + (double)this.TRISOPeletSupply) / 200D));

        this.savedBISO = BISOPeletSupply;
        this.savedTRISO = TRISOPeletSupply;

        if (new XSTR().nextBoolean()) {
            if (this.BISOPeletSupply > 0)
                this.BISOPeletSupply -= toReduce;
            else
                this.TRISOPeletSupply -= toReduce;
        } else {
            if (this.TRISOPeletSupply > 0)
                this.TRISOPeletSupply -= toReduce;
            else
                this.BISOPeletSupply -= toReduce;
        }
    }

    private void addBurnedOutBalls() {
        int burnedTRISO = this.savedTRISO - this.TRISOPeletSupply;
        int burnedBISO = this.savedBISO - this.BISOPeletSupply;
        int burnedTRISOBall = burnedTRISO / 64;
        int burnedBISOBall = burnedBISO / 64;

        burnedBISO -= (burnedBISOBall*64);
        burnedTRISO -= (burnedTRISOBall*64);

        this.mOutputItems = new ItemStack[] {
                new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials, burnedTRISOBall, 6),
                new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials, burnedTRISO, 8),
                new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials, burnedBISOBall, 7),
                new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials, burnedBISO, 9)
        };
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {

        if (this.empty){
            this.addOutput(Materials.Helium.getGas(this.HeliumSupply));
            this.addOutput(new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials, this.TRISOPeletSupply, 3));
            this.addOutput(new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials, this.BISOPeletSupply, 1));
            this.HeliumSupply = 0;
            this.TRISOPeletSupply = 0;
            this.BISOPeletSupply = 0;
            this.savedTRISO = 0;
            this.savedBISO = 0;
            return true;
        }

        long accessibleCoolant = 0;
        for (FluidStack fluidStack : this.getStoredFluids()) {
            if (fluidStack.isFluidEqual(FluidRegistry.getFluidStack("ic2coolant",1))) {
                accessibleCoolant += fluidStack.amount;
            }
        }

        accessibleCoolant = Math.min((long) ((0.00711111111111111111111111111111D*(double)this.savedTRISO + 0.00474074074074074074074074074074D*(double)this.savedBISO)), accessibleCoolant);

        for (FluidStack fluidStack : this.getStoredFluids()) {
            if (fluidStack.isFluidEqual(FluidRegistry.getFluidStack("ic2coolant",1))) {
                if (accessibleCoolant >= fluidStack.amount) {
                    accessibleCoolant -= fluidStack.amount;
                    fluidStack.amount = 0;
                } else if (accessibleCoolant > 0) {
                    fluidStack.amount -= accessibleCoolant;
                    accessibleCoolant = 0;
                }
            }
        }
        this.mOutputFluids[0].amount += accessibleCoolant;
        this.updateSlots();
        return true;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack itemStack) {
        byte xz = 5;
        int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX * xz;
        int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ * xz;
        for (int x = -xz; x <= xz; x++) {
                for (int z = -xz; z <= xz; z++) {
                    for (int y = 0; y < 12; y++) {
                        if (y == 0 || y == 11) {
                            if (
                                    !((Math.abs(z) == xz-1 && Math.abs(x) == xz)) &&
                                    !((Math.abs(z) == xz && Math.abs(x) == xz-1)) &&
                                    !((Math.abs(x) == Math.abs(z) && Math.abs(x) == xz))
                            ) {
                                if (x + xDir == 0 && y == 0 && z + zDir == 0)
                                    continue;
                                if (!(aBaseMetaTileEntity.getBlockOffset(xDir + x, y, zDir + z) == GregTech_API.sBlockCasings3 && aBaseMetaTileEntity.getMetaIDOffset(xDir + x, y, zDir + z) == 12)) {
                                    if (
                                            (
                                                    !(this.addInputToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + x, y, zDir + z), GT_TileEntity_THTR.BASECASINGINDEX) && y == 11) &&
                                                    !(this.addOutputToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + x, y, zDir + z), GT_TileEntity_THTR.BASECASINGINDEX) && y == 0)) &&
                                                    !this.addMaintenanceToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + x, y, zDir + z), GT_TileEntity_THTR.BASECASINGINDEX)
                                    ) {
                                        return false;
                                    }
                                }
                            }
                        }


//                        else if (!((Math.abs(x) == 4 && Math.abs(z) == 4) || (Math.abs(x) == 3 && Math.abs(z) == 3)) && !(Math.abs(x) < 3 || Math.abs(z) < 3) && !((Math.abs(x) == Math.abs(z) && Math.abs(x) == 3) || Math.abs(x) == 4 || Math.abs(z) == 4)) {
                        else if (!((Math.abs(z) == xz-1 && Math.abs(x) == xz)))
                                        if (!((Math.abs(z) == xz && Math.abs(x) == xz-1)))
                                            if (!((Math.abs(x) == Math.abs(z) && Math.abs(x) == xz)))
                                                if (!(Math.abs(x) < xz && Math.abs(z) != xz))

                        {
                            if (!(aBaseMetaTileEntity.getBlockOffset(xDir + x, y, zDir + z) == GregTech_API.sBlockCasings3 && aBaseMetaTileEntity.getMetaIDOffset(xDir + x, y, zDir + z) == 12)) {
                            if (
                                    !this.addMaintenanceToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + x, y, zDir + z), GT_TileEntity_THTR.BASECASINGINDEX))
                            {
                                return false;
                            }
                        }
                    }
                }
            }

        }

        return this.mMaintenanceHatches.size() == 1;
    }

    @Override
    public int getMaxEfficiency(ItemStack itemStack) {
        return 10000;
    }

    @Override
    public int getPollutionPerTick(ItemStack itemStack) {
        return 0;
    }

    @Override
    public int getDamageToComponent(ItemStack itemStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack itemStack) {
        return false;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new GT_TileEntity_THTR(this.mName);
    }


    @Override
    public String[] getInfoData() {
        return new String[]{
                "Progress:", this.mProgresstime / 20 + "secs", this.mMaxProgresstime / 20 + "secs",
                "BISO-Pebbles:", this.BISOPeletSupply + "pcs.",
                "TRISO-Pebbles:", this.TRISOPeletSupply + "pcs.",
                "Helium-Level:", this.HeliumSupply+"L / "+ GT_TileEntity_THTR.HELIUM_NEEDED +"L",
                "Coolant/t:", this.savedBISO+this.savedTRISO >= 100000 ? (long) ((0.00711111111111111111111111111111D * (double) this.savedTRISO + 0.00474074074074074074074074074074D * (double) this.savedBISO))+"L/t" : "0L/t",
                "Problems:", String.valueOf(this.getIdealStatus() - this.getRepairStatus())
        };
    }

    @Override
    public String[] getDescription() {
        String[] dsc = StatCollector.translateToLocal("tooltip.tile.htr.0.name").split(";");
        String[] mDescription = new String[dsc.length + 1];
        for (int i = 0; i < dsc.length; i++) {
            mDescription[i] = dsc[i];
            mDescription[dsc.length] = StatCollector.translateToLocal("tooltip.bw.1.name") + ChatColorHelper.DARKGREEN + " BartWorks";
        }
        return mDescription;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        return aSide == aFacing ? new ITexture[]{Textures.BlockIcons.CASING_BLOCKS[GT_TileEntity_THTR.BASECASINGINDEX], new GT_RenderedTexture(aActive ? Textures.BlockIcons.OVERLAY_FRONT_HEAT_EXCHANGER_ACTIVE : Textures.BlockIcons.OVERLAY_FRONT_HEAT_EXCHANGER)} : new ITexture[]{Textures.BlockIcons.CASING_BLOCKS[GT_TileEntity_THTR.BASECASINGINDEX]};
    }

    @Override
    public void onScrewdriverRightClick(byte aSide, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        this.empty = !this.empty;
        GT_Utility.sendChatToPlayer(aPlayer, this.empty ? "THTR will now empty itself." : "THTR is back in normal Operation");
    }

    public static class THTRMaterials{
        static final SimpleSubItemClass aTHTR_Materials = new SimpleSubItemClass(
                "BISOPelletCompound",
                "BISOPellet",
                "TRISOPelletCompound",
                "TRISOPellet",
                "BISOPelletBall",
                "TRISOPelletBall",
                "BurnedOutBISOPelletBall",
                "BurnedOutTRISOPelletBall",
                "BurnedOutBISOPellet",
                "BurnedOutTRISOPellet"
        );
        public static void registeraTHR_Materials(){
            GameRegistry.registerItem(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,"bw.THTRMaterials");
        }

        public static void registerTHR_Recipes(){
            GT_Values.RA.addCentrifugeRecipe(
                    Materials.Thorium.getDust(1),GT_Values.NI,GT_Values.NF,GT_Values.NF,
                    Materials.Thorium.getDustSmall(2),Materials.Thorium.getDustSmall(1),
                    WerkstoffLoader.Thorium232.get(OrePrefixes.dustTiny,1),WerkstoffLoader.Thorium232.get(OrePrefixes.dustTiny,1),
                    WerkstoffLoader.Thorium232.get(OrePrefixes.dustTiny,1),Materials.Lutetium.getDustTiny(1),
                    new int[]{1600,1500,200,200,50,50},
                    10000, BW_Util.getMachineVoltageFromTier(4));
            GT_Values.RA.addAssemblerRecipe(new ItemStack[]{
                    GT_OreDictUnificator.get(OrePrefixes.plateDense,Materials.Lead,6),
                    GT_OreDictUnificator.get(OrePrefixes.frameGt,Materials.TungstenSteel,1)
                    },
                    Materials.Concrete.getMolten(1296),
                    new ItemStack(GregTech_API.sBlockCasings3,1,12),
                    40,
                    BW_Util.getMachineVoltageFromTier(5)
            );
            GT_Values.RA.addMixerRecipe(WerkstoffLoader.Thorium232.get(OrePrefixes.dust,10),Materials.Uranium235.getDust(1),GT_Utility.getIntegratedCircuit(1),null,null,null,new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials),400,30);
            GT_Values.RA.addFormingPressRecipe(new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials),Materials.Graphite.getDust(64),new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,4),40,30);
            ItemStack[] pellets = new ItemStack[6];
            Arrays.fill(pellets,new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,64,1));
            GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes.addRecipe(false,new ItemStack[]{new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,4),GT_Utility.getIntegratedCircuit(17)}, pellets,null,null,null,null,24000,30,0);
            GT_Values.RA.addFormingPressRecipe(new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,4),Materials.Silicon.getDust(64),new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,2),40,30);
            GT_Values.RA.addFormingPressRecipe(new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,2),Materials.Graphite.getDust(64),new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,5),40,30);
            pellets = new ItemStack[6];
            Arrays.fill(pellets,new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,64,3));
            GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes.addRecipe(false,new ItemStack[]{new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,5),GT_Utility.getIntegratedCircuit(17)}, pellets,null,null,null,null,48000,30,0);
            GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes.addRecipe(false,new ItemStack[]{new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,6),GT_Utility.getIntegratedCircuit(17)}, new ItemStack[]{new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,64,8)},null,null,null,null,48000,30,0);
            GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes.addRecipe(false,new ItemStack[]{new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,7),GT_Utility.getIntegratedCircuit(17)}, new ItemStack[]{new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,64,9)},null,null,null,null,48000,30,0);
            GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes.addRecipe(false,new ItemStack[]{new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,8)},new ItemStack[]{Materials.Lutetium.getDustSmall(2)},null,null,null,null,1200,30,0);
            GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes.addRecipe(false,new ItemStack[]{new ItemStack(GT_TileEntity_THTR.THTRMaterials.aTHTR_Materials,1,9)},new ItemStack[]{Materials.Lutetium.getDustSmall(4)},null,null,null,null,1200,30,0);
        }

    }
}