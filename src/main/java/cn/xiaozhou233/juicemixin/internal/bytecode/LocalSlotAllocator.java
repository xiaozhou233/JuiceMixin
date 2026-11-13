package cn.xiaozhou233.juicemixin.internal.bytecode;

/**
 * Hands out non-overlapping local variable slot ranges for one target method.
 */
public final class LocalSlotAllocator {

    private int nextSlot;

    public LocalSlotAllocator(int firstFreeSlot) {
        this.nextSlot = Math.max(0, firstFreeSlot);
    }

    public int allocate(int slotCount) {
        int start = nextSlot;
        nextSlot += Math.max(0, slotCount);
        return start;
    }
}
