package ds.mods.CCLights2.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ds.mods.CCLights2.CCLights2;
import ds.mods.CCLights2.block.tileentity.TileEntityColorLight;

public class BlockColorLight extends BlockContainer {

	public BlockColorLight(int par1, Material par2Material) {
		super(par1, par2Material);
		this.setUnlocalizedName("Light");
		this.setLightValue(1.0F);
		this.setHardness(0.6F).setStepSound(soundStoneFootstep);
		this.setCreativeTab(CCLights2.ccltab);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		return this.blockIcon;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
       par1IconRegister.registerIcon("CCLights:light");
	}
	@Override
	public int quantityDropped(Random random)
    {
        return 1;
    }

    @Override
	public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityColorLight();
    }
}
