package mtr.data;

import mtr.block.IBlock;
import mtr.mappings.BlockEntityClientSerializableMapper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public interface IPIDS extends IBlock {

    abstract class TileEntityPIDS extends BlockEntityClientSerializableMapper {
        private long cachedRefreshTime;
        private long cachedPlatformId;
        public final String[] messages = new String[getMaxArrivals() * getLinesPerArrival()];
        private final boolean[] hideArrival = new boolean[getMaxArrivals()];
        private final Set<Long> platformIds = new HashSet<>();
        private static final String KEY_MESSAGE = "message";
        private static final String KEY_HIDE_ARRIVAL = "hide_arrival";
        private static final String KEY_PLATFORM_IDS = "platform_ids";

        public TileEntityPIDS(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            for (int i = 0; i < getMaxArrivals(); i++) {
                messages[i] = compoundTag.getString(KEY_MESSAGE + i);
                hideArrival[i] = compoundTag.getBoolean(KEY_HIDE_ARRIVAL + i);
            }
            platformIds.clear();
            final long[] platformIdsArray = compoundTag.getLongArray(KEY_PLATFORM_IDS);
            for (final long platformId : platformIdsArray) {
                platformIds.add(platformId);
            }
        }

        @Override
        public void writeCompoundTag(CompoundTag compoundTag) {
            for (int i = 0; i < getMaxArrivals(); i++) {
                compoundTag.putString(KEY_MESSAGE + i, messages[i] == null ? "" : messages[i]);
                compoundTag.putBoolean(KEY_HIDE_ARRIVAL + i, hideArrival[i]);
            }
            compoundTag.putLongArray(KEY_PLATFORM_IDS, new ArrayList<>(platformIds));
        }

        public AABB getRenderBoundingBox() {
            return new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        public void setData(String[] messages, boolean[] hideArrival, Set<Long> platformIds) {
            System.arraycopy(messages, 0, this.messages, 0, Math.min(messages.length, this.messages.length));
            System.arraycopy(hideArrival, 0, this.hideArrival, 0, Math.min(hideArrival.length, this.hideArrival.length));
            this.platformIds.clear();
            this.platformIds.addAll(platformIds);
            setChanged();
            syncData();
        }

        public long getPlatformId(Set<Platform> platforms, DataCache dataCache) {
            if (dataCache.needsRefresh(cachedRefreshTime)) {
                cachedPlatformId = RailwayData.getClosePlatformId(platforms, dataCache, getBlockPos());
                cachedRefreshTime = System.currentTimeMillis();
            }
            return cachedPlatformId;
        }

        public Set<Long> getPlatformIds() {
            return platformIds;
        }

        public String getMessage(int index) {
            if (index >= 0 && index < getMaxArrivals()) {
                if (messages[index] == null) {
                    messages[index] = "";
                }
                return messages[index];
            } else {
                return "";
            }
        }

        public boolean getHideArrival(int index) {
            if (index >= 0 && index < getMaxArrivals()) {
                return hideArrival[index];
            } else {
                return false;
            }
        }

        public abstract int getMaxArrivals();

        public abstract int getLinesPerArrival();
    }

}
