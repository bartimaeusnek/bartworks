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

package com.github.bartimaeusnek.crossmod.tectech.tileentites.tiered;

import com.github.bartimaeusnek.bartworks.util.ChatColorHelper;
import gregtech.api.enums.Dyes;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaPipeEntity_Cable;
import gregtech.api.objects.GT_CopiedBlockTexture;
import gregtech.api.util.GT_Utility;
import gregtech.common.GT_Client;
import ic2.core.Ic2Items;
import net.minecraft.block.Block;
import net.minecraft.util.StatCollector;

public class TT_MetaTileEntity_Pipe_Energy_LowPower extends GT_MetaPipeEntity_Cable implements LowPowerLaser {

    public TT_MetaTileEntity_Pipe_Energy_LowPower(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 0.25f, Materials.BorosilicateGlass, 0, 0, 0, false, false);
    }

    public TT_MetaTileEntity_Pipe_Energy_LowPower(String aName, float aThickNess, Materials aMaterial, long aCableLossPerMeter, long aAmperage, long aVoltage, boolean aInsulated, boolean aCanShock) {
        super(aName, aThickNess, aMaterial, aCableLossPerMeter, aAmperage, aVoltage, aInsulated, aCanShock);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new TT_MetaTileEntity_Pipe_Energy_LowPower(this.mName, this.mThickNess, this.mMaterial, this.mCableLossPerMeter, this.mAmperage, this.mVoltage, this.mInsulated, this.mCanShock);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aTick % 20 == 13 && aBaseMetaTileEntity.isClientSide() && GT_Client.changeDetected == 4) {
            aBaseMetaTileEntity.issueTextureUpdate();
        }
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aConnections, byte aColorIndex, boolean aConnected, boolean aRedstone) {
        return new ITexture[]{
                new GT_CopiedBlockTexture(
                        Block.getBlockFromItem(Ic2Items.glassFiberCableBlock.getItem()),
                        aSide,
                        Ic2Items.glassFiberCableBlock.getItemDamage(),
                        Dyes.getModulation(aColorIndex, Dyes._NULL.mRGBa)
                )
        };
    }

    @Override
    public String[] getDescription() {
        return new String[]{
                "Primitive Laser Cable intended for Low Power Applications",
                "Does not auto-connect",
                "Does not turn or bend",
                ChatColorHelper.WHITE + "Must be " + ChatColorHelper.YELLOW + "c" + ChatColorHelper.RED + "o" + ChatColorHelper.BLUE + "l" + ChatColorHelper.DARKPURPLE + "o" + ChatColorHelper.GOLD + "r" + ChatColorHelper.DARKRED + "e" + ChatColorHelper.DARKGREEN + "d" + ChatColorHelper.WHITE + " in order to work",
                StatCollector.translateToLocal("tooltip.bw.1.name") + ChatColorHelper.DARKGREEN + " BartWorks"
        };
    }

    @Override
    public boolean isSender() {
        return false;
    }

    @Override
    public boolean isReceiver() {
        return false;
    }

    @Override
    public boolean isTunnel() {
        return true;
    }

    @Override
    public boolean canConnect(byte b) {
        return true;
    }

    @Override
    public boolean isGivingInformation() {
        return false;
    }

    @Override
    public boolean isConnectedCorrectly(byte aSide) {
        return this.isConnectedAtSide(aSide) && this.isConnectedAtSide(GT_Utility.getOppositeSide(aSide));
    }
}
