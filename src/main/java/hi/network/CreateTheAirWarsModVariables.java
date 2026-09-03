package hi.network;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.saveddata.SavedData.Factory;
import net.minecraft.util.datafix.DataFixTypes;

import hi.CreateTheAirWarsMod;

public class CreateTheAirWarsModVariables {

    public static class WorldVariables extends SavedData {
        public static final String DATA_NAME = "create_the_air_wars_worldvars";

        public static WorldVariables load(CompoundTag tag, HolderLookup.Provider provider) {
            WorldVariables data = new WorldVariables();
            data.read(tag, provider);
            return data;
        }

        public void read(CompoundTag nbt, HolderLookup.Provider provider) {
        }

        @Override
        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
            return nbt;
        }

        public void syncData(LevelAccessor world) {
            this.setDirty();
        }

        static WorldVariables clientSide = new WorldVariables();

        public static WorldVariables get(LevelAccessor world) {
            if (world instanceof ServerLevel level) {
                return level.getDataStorage().computeIfAbsent(new Factory<>(WorldVariables::new, WorldVariables::load, DataFixTypes.LEVEL), DATA_NAME);
            } else {
                return clientSide;
            }
        }
    }

    public static class MapVariables extends SavedData {
        public static final String DATA_NAME = "create_the_air_wars_mapvars";
        public double ZM54E = 0;
        public String WT = "";

        public static MapVariables load(CompoundTag tag, HolderLookup.Provider provider) {
            MapVariables data = new MapVariables();
            data.read(tag, provider);
            return data;
        }

        public void read(CompoundTag nbt, HolderLookup.Provider provider) {
            ZM54E = nbt.getDouble("ZM54E");
            WT = nbt.getString("WT");
        }

        @Override
        public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
            nbt.putDouble("ZM54E", ZM54E);
            nbt.putString("WT", WT);
            return nbt;
        }

        public void syncData(LevelAccessor world) {
            this.setDirty();
        }

        static MapVariables clientSide = new MapVariables();

        public static MapVariables get(LevelAccessor world) {
            if (world instanceof ServerLevelAccessor serverLevelAcc) {
                return serverLevelAcc.getLevel().getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(new Factory<>(MapVariables::new, MapVariables::load, DataFixTypes.LEVEL), DATA_NAME);
            } else {
                return clientSide;
            }
        }
    }
}
