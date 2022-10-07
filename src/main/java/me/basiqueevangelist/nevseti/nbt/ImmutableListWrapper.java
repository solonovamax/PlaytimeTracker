package me.basiqueevangelist.nevseti.nbt;


import me.basiqueevangelist.nevseti.nbt.NbtCompoundView;
import me.basiqueevangelist.nevseti.nbt.NbtListView;
import net.minecraft.nbt.NbtList;


public class ImmutableListWrapper implements me.basiqueevangelist.nevseti.nbt.NbtListView {
    private final NbtList tag;
    
    public ImmutableListWrapper(NbtList tag) {
        this.tag = tag;
    }
    
    @Override
    public String toString() {
        return "ImmutableListWrapper{" +
               "tag=" + tag +
               '}';
    }
    
    @Override
    public byte getElementType() {
        return tag.getType();
    }
    
    @Override
    public boolean isEmpty() {
        return tag.isEmpty();
    }
    
    @Override
    public me.basiqueevangelist.nevseti.nbt.NbtCompoundView getCompound(int index) {
        return NbtCompoundView.take(tag.getCompound(index));
    }
    
    @Override
    public me.basiqueevangelist.nevseti.nbt.NbtListView getList(int index) {
        return NbtListView.take(tag.getList(index));
    }
    
    @Override
    public short getShort(int index) {
        return tag.getShort(index);
    }
    
    @Override
    public int getInt(int i) {
        return tag.getInt(i);
    }
    
    @Override
    public int[] getIntArray(int index) {
        return tag.getIntArray(index);
    }
    
    @Override
    public double getDouble(int index) {
        return tag.getDouble(index);
    }
    
    @Override
    public float getFloat(int index) {
        return tag.getFloat(index);
    }
    
    @Override
    public String getString(int index) {
        return tag.getString(index);
    }
    
    @Override
    public int size() {
        return tag.size();
    }
    
    @Override
    public NbtList copy() {
        return tag.copy();
    }
}
