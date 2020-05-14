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

package com.github.bartimaeusnek.bartworks.system.material;

import com.github.bartimaeusnek.bartworks.API.SideReference;
import com.github.bartimaeusnek.bartworks.client.textures.PrefixTextureLinker;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TextureSet;
import gregtech.api.interfaces.ITexture;
import gregtech.api.objects.GT_CopiedBlockTexture;
import gregtech.api.objects.GT_RenderedTexture;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public class BW_MetaGenerated_WerkstoffBlock_TE extends BW_MetaGenerated_Block_TE {

    @Override
    public ITexture[] getTexture(Block aBlock, byte aSide) {
        if (SideReference.Side.Client) {
            Werkstoff aMaterial = Werkstoff.werkstoffHashMap.get(this.mMetaData);
            if ((aMaterial != null)) {
                TextureSet set = aMaterial.getTexSet();
                GT_RenderedTexture aIconSet = new GT_RenderedTexture(
                        set.mTextures[PrefixTextureLinker.blockTexMap.getOrDefault(set, OrePrefixes.block.mTextureIndex)], aMaterial.getRGBA()
                );
                return new ITexture[]{new GT_CopiedBlockTexture(Blocks.iron_block, 0, 0), aIconSet};
            }
        }
        return new ITexture[]{new GT_CopiedBlockTexture(Blocks.iron_block, 0, 0), new GT_RenderedTexture(gregtech.api.enums.TextureSet.SET_NONE.mTextures[OrePrefixes.block.mTextureIndex])};
    }

    @Override
    protected Block GetProperBlock() {
        return WerkstoffLoader.BWBlocks;
    }
}
