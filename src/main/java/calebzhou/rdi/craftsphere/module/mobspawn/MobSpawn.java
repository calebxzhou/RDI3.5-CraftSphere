package calebzhou.rdi.craftsphere.module.mobspawn;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.NetherFortressFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import static calebzhou.rdi.craftsphere.ExampleMod.LOGGER;

public class MobSpawn {
    private static boolean isAcceptableSpawnPosition(ServerWorld world, Chunk chunk, BlockPos.Mutable pos, double squaredDistance) {
        if (squaredDistance <= 24*24d) {
            return false;
        } else if (world.getSpawnPos().isWithinDistance(new Vec3d((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D), 24.0D)) {
            return false;
        } else {
            return Objects.equals(new ChunkPos(pos), chunk.getPos()) || world.shouldTickEntity(pos);
        }
    }

    private static boolean canSpawn(ServerWorld world, SpawnGroup group, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnSettings.SpawnEntry spawnEntry, BlockPos.Mutable pos, double squaredDistance) {
        EntityType<?> entityType = spawnEntry.type;
        if (entityType.getSpawnGroup() == SpawnGroup.MISC) {
            return false;
        } else if (!entityType.isSpawnableFarFromPlayer() && squaredDistance > (double)(entityType.getSpawnGroup().getImmediateDespawnRange() * entityType.getSpawnGroup().getImmediateDespawnRange())) {
            return false;
        } else if (entityType.isSummonable() && containsSpawnEntry(world, structureAccessor, chunkGenerator, group, spawnEntry, pos)) {
            SpawnRestriction.Location location = SpawnRestriction.getLocation(entityType);
            if (!canSpawn(location, world, pos, entityType)) {
                return false;
            } else if (!SpawnRestriction.canSpawn(entityType, world, SpawnReason.NATURAL, pos, world.random)) {
                return false;
            } else {
                return world.isSpaceEmpty(entityType.createSimpleBoundingBox((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D));
            }
        } else {
            return false;
        }
    }
    private static Pool<SpawnSettings.SpawnEntry> getSpawnEntries(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, BlockPos pos, @Nullable Biome biome) {
        return shouldUseNetherFortressSpawns(pos, world, spawnGroup, structureAccessor) ? NetherFortressFeature.MONSTER_SPAWNS : chunkGenerator.getEntitySpawnList(biome != null ? biome : world.getBiome(pos), structureAccessor, spawnGroup, pos);
    }

    public static boolean shouldUseNetherFortressSpawns(BlockPos pos, ServerWorld world, SpawnGroup spawnGroup, StructureAccessor structureAccessor) {
        return spawnGroup == SpawnGroup.MONSTER && world.getBlockState(pos.down()).isOf(Blocks.NETHER_BRICKS) && structureAccessor.getStructureAt(pos, StructureFeature.FORTRESS).hasChildren();
    }

    private static BlockPos getRandomPosInChunkSection(World world, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.getStartX() + world.random.nextInt(16);
        int j = chunkPos.getStartZ() + world.random.nextInt(16);
        int k = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, i, j) + 1;
        int l = MathHelper.nextBetween(world.random, world.getBottomY(), k);
        return new BlockPos(i, l, j);
    }

    public static boolean isClearForSpawn(BlockView blockView, BlockPos pos, BlockState state, FluidState fluidState, EntityType<?> entityType) {
        if (state.isFullCube(blockView, pos)) {
            return false;
        } else if (state.emitsRedstonePower()) {
            return false;
        } else if (!fluidState.isEmpty()) {
            return false;
        } else if (state.isIn(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
            return false;
        } else {
            return !entityType.isInvalidSpawn(state);
        }
    }

    public static boolean canSpawn(SpawnRestriction.Location location, WorldView world, BlockPos pos, @Nullable EntityType<?> entityType) {
        if (location == SpawnRestriction.Location.NO_RESTRICTIONS) {
            return true;
        } else if (entityType != null && world.getWorldBorder().contains(pos)) {
            BlockState blockState = world.getBlockState(pos);
            FluidState fluidState = world.getFluidState(pos);
            BlockPos upPos = pos.up();
            BlockPos downPos = pos.down();
            switch(location) {
                case IN_WATER:
                    return fluidState.isIn(FluidTags.WATER) && !world.getBlockState(upPos).isSolidBlock(world, upPos);
                case IN_LAVA:
                    return fluidState.isIn(FluidTags.LAVA);
                case ON_GROUND:
                default:
                    BlockState blockState2 = world.getBlockState(downPos);
                    if (!blockState2.allowsSpawning(world, downPos, entityType)) {
                        return false;
                    } else {
                        return isClearForSpawn(world, pos, blockState, fluidState, entityType) && isClearForSpawn(world, upPos, world.getBlockState(upPos), world.getFluidState(upPos), entityType);
                    }
            }
        } else {
            return false;
        }
    }

    public static void populateEntities(ServerWorldAccess world, Biome biome, ChunkPos chunkPos, Random random) {
        SpawnSettings spawnSettings = biome.getSpawnSettings();
        Pool<SpawnSettings.SpawnEntry> pool = spawnSettings.getSpawnEntries(SpawnGroup.CREATURE);
        if (!pool.isEmpty()) {
            int i = chunkPos.getStartX();
            int j = chunkPos.getStartZ();

            while(true) {
                Optional optional;
                do {
                    if (!(random.nextFloat() < spawnSettings.getCreatureSpawnProbability())) {
                        return;
                    }

                    optional = pool.getOrEmpty(random);
                } while(!optional.isPresent());

                SpawnSettings.SpawnEntry spawnEntry = (SpawnSettings.SpawnEntry)optional.get();
                int k = spawnEntry.minGroupSize + random.nextInt(1 + spawnEntry.maxGroupSize - spawnEntry.minGroupSize);
                EntityData entityData = null;
                int l = i + random.nextInt(16);
                int m = j + random.nextInt(16);
                int n = l;
                int o = m;

                for(int p = 0; p < k; ++p) {
                    boolean bl = false;

                    for(int q = 0; !bl && q < 4; ++q) {
                        BlockPos blockPos = getEntitySpawnPos(world, spawnEntry.type, l, m);
                        if (spawnEntry.type.isSummonable() && canSpawn(SpawnRestriction.getLocation(spawnEntry.type), world, blockPos, spawnEntry.type)) {
                            float f = spawnEntry.type.getWidth();
                            double d = MathHelper.clamp((double)l, (double)i + (double)f, (double)i + 16.0D - (double)f);
                            double e = MathHelper.clamp((double)m, (double)j + (double)f, (double)j + 16.0D - (double)f);
                            if (!world.isSpaceEmpty(spawnEntry.type.createSimpleBoundingBox(d, (double)blockPos.getY(), e)) || !SpawnRestriction.canSpawn(spawnEntry.type, world, SpawnReason.CHUNK_GENERATION, new BlockPos(d, (double)blockPos.getY(), e), world.getRandom())) {
                                continue;
                            }

                            Entity entity;
                            try {
                                entity = spawnEntry.type.create(world.toServerWorld());
                            } catch (Exception var27) {
                                LOGGER.warn("Failed to create mob", var27);
                                continue;
                            }

                            entity.refreshPositionAndAngles(d, (double)blockPos.getY(), e, random.nextFloat() * 360.0F, 0.0F);
                            if (entity instanceof MobEntity) {
                                MobEntity exception = (MobEntity)entity;
                                if (exception.canSpawn(world, SpawnReason.CHUNK_GENERATION) && exception.canSpawn(world)) {
                                    entityData = exception.initialize(world, world.getLocalDifficulty(exception.getBlockPos()), SpawnReason.CHUNK_GENERATION, entityData, (NbtCompound)null);
                                    world.spawnEntityAndPassengers(exception);
                                    bl = true;
                                }
                            }
                        }

                        l += random.nextInt(5) - random.nextInt(5);

                        for(m += random.nextInt(5) - random.nextInt(5); l < i || l >= i + 16 || m < j || m >= j + 16; m = o + random.nextInt(5) - random.nextInt(5)) {
                            l = n + random.nextInt(5) - random.nextInt(5);
                        }
                    }
                }
            }
        }
    }

    private static BlockPos getEntitySpawnPos(WorldView world, EntityType<?> entityType, int x, int z) {
        int worldTopY = world.getTopY(SpawnRestriction.getHeightmapType(entityType), x, z);
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, worldTopY, z);
        if (world.getDimension().hasCeiling()) {
            do {
                mutable.move(Direction.DOWN);
            } while(!world.getBlockState(mutable).isAir());

            do {
                mutable.move(Direction.DOWN);
            } while(world.getBlockState(mutable).isAir() && mutable.getY() > world.getBottomY());
        }

        if (SpawnRestriction.getLocation(entityType) == SpawnRestriction.Location.ON_GROUND) {
            BlockPos blockPos = mutable.down();
            if (world.getBlockState(blockPos).canPathfindThrough(world, blockPos, NavigationType.LAND)) {
                return blockPos;
            }
        }

        return mutable.toImmutable();
    }

    private static boolean containsSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, SpawnSettings.SpawnEntry spawnEntry, BlockPos pos) {
        return getSpawnEntries(world, structureAccessor, chunkGenerator, spawnGroup, pos, (Biome)null).getEntries().contains(spawnEntry);
    }

    static final int CHUNK_AREA = (int)Math.pow(17.0D, 2.0D);
    @FunctionalInterface
    public interface ChunkSource {
        void query(long pos, Consumer<WorldChunk> chunkConsumer);
    }
    static Biome getBiomeDirectly(BlockPos pos, Chunk chunk) {
        return chunk.getBiomeForNoiseGen(BiomeCoords.fromBlock(pos.getX()), BiomeCoords.fromBlock(pos.getY()), BiomeCoords.fromBlock(pos.getZ()));
    }
    public static class Info {
        private final int spawningChunkCount;
        private final Object2IntOpenHashMap<SpawnGroup> groupToCount;
        private final GravityField densityField;
        private final Object2IntMap<SpawnGroup> groupToCountView;
        private final SpawnDensityCapper densityCapper;
        @Nullable
        private BlockPos cachedPos;
        @Nullable
        private EntityType<?> cachedEntityType;
        private double cachedDensityMass;

        Info(int spawningChunkCount, Object2IntOpenHashMap<SpawnGroup> groupToCount, GravityField densityField, SpawnDensityCapper densityCapper) {
            this.spawningChunkCount = spawningChunkCount;
            this.groupToCount = groupToCount;
            this.densityField = densityField;
            this.densityCapper = densityCapper;
            this.groupToCountView = Object2IntMaps.unmodifiable(groupToCount);
        }

        private boolean test(EntityType<?> type, BlockPos pos, Chunk chunk) {
            this.cachedPos = pos;
            this.cachedEntityType = type;
            SpawnSettings.SpawnDensity spawnDensity = getBiomeDirectly(pos, chunk).getSpawnSettings().getSpawnDensity(type);
            if (spawnDensity == null) {
                this.cachedDensityMass = 0.0D;
                return true;
            } else {
                double d = spawnDensity.getMass();
                this.cachedDensityMass = d;
                double e = this.densityField.calculate(pos, d);
                return e <= spawnDensity.getGravityLimit();
            }
        }

        private void run(MobEntity entity, Chunk chunk) {
            EntityType<?> entityType = entity.getType();
            BlockPos blockPos = entity.getBlockPos();
            double d;
            if (blockPos.equals(this.cachedPos) && entityType == this.cachedEntityType) {
                d = this.cachedDensityMass;
            } else {
                SpawnSettings.SpawnDensity spawnDensity = getBiomeDirectly(blockPos, chunk).getSpawnSettings().getSpawnDensity(entityType);
                if (spawnDensity != null) {
                    d = spawnDensity.getMass();
                } else {
                    d = 0.0D;
                }
            }

            this.densityField.addPoint(blockPos, d);
            SpawnGroup spawnDensity = entityType.getSpawnGroup();
            this.groupToCount.addTo(spawnDensity, 1);
            this.densityCapper.increaseDensity(new ChunkPos(blockPos), spawnDensity);
        }

        public int getSpawningChunkCount() {
            return this.spawningChunkCount;
        }

        public Object2IntMap<SpawnGroup> getGroupToCount() {
            return this.groupToCountView;
        }

        boolean isBelowCap(SpawnGroup group, ChunkPos chunkPos) {
            int i = group.getCapacity() * this.spawningChunkCount / CHUNK_AREA;
            if (this.groupToCount.getInt(group) >= i) {
                return false;
            } else {
                return this.densityCapper.canSpawn(group, chunkPos);
            }
        }
    }

    @FunctionalInterface
    public interface Checker {
        boolean test(EntityType<?> type, BlockPos pos, Chunk chunk);
    }

    @FunctionalInterface
    public interface Runner {
        void run(MobEntity entity, Chunk chunk);
    }
    private static Optional<SpawnSettings.SpawnEntry> pickRandomSpawnEntry(ServerWorld world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, Random random, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        return spawnGroup == SpawnGroup.WATER_AMBIENT && biome.getCategory() == Biome.Category.RIVER && random.nextFloat() < 0.98F ? Optional.empty() : getSpawnEntries(world, structureAccessor, chunkGenerator, spawnGroup, pos, biome).getOrEmpty(random);
    }
    @Nullable
    private static MobEntity createMob(ServerWorld world, EntityType<?> type) {
        try {
            Entity entity = type.create(world);
            if (!(entity instanceof MobEntity)) {
                throw new IllegalStateException("Trying to spawn a non-mob: " + Registry.ENTITY_TYPE.getId(type));
            } else {
                MobEntity mobEntity = (MobEntity)entity;
                return mobEntity;
            }
        } catch (Exception var4) {
            LOGGER.warn("Failed to create mob", var4);
            return null;
        }
    }
    public static void spawnEntitiesInChunk(SpawnGroup group, ServerWorld world, Chunk chunk, BlockPos pos, SpawnHelper.Checker checker, SpawnHelper.Runner runner) {
        StructureAccessor structureAccessor = world.getStructureAccessor();
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        int posY = pos.getY();
        BlockState blockState = chunk.getBlockState(pos);
        if (!blockState.isSolidBlock(chunk, pos)) {
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            int j = 0;

            for(int k = 0; k < 3; ++k) {
                int posX = pos.getX();
                int posZ = pos.getZ();
                SpawnSettings.SpawnEntry spawnEntry = null;
                EntityData entityData = null;
                int randFloat = MathHelper.ceil(world.random.nextFloat() * 4.0F);
                int p = 0;

                for(int q = 0; q < randFloat; ++q) {
                    posX += world.random.nextInt(6) - world.random.nextInt(6);
                    posZ += world.random.nextInt(6) - world.random.nextInt(6);
                    mutable.set(posX, posY, posZ);
                    double offsetPosX = (double)posX + 0.5D;
                    double offsetPosZ = (double)posZ + 0.5D;
                    PlayerEntity closestPlayer = world.getClosestPlayer(offsetPosX, posY, offsetPosZ, -1.0D, false);
                    if (closestPlayer != null) {
                        double playerToDistance = closestPlayer.squaredDistanceTo(offsetPosX, posY, offsetPosZ);
                        if (isAcceptableSpawnPosition(world, chunk, mutable, playerToDistance)) {
                            if (spawnEntry == null) {
                                Optional<SpawnSettings.SpawnEntry> entry = pickRandomSpawnEntry(world, structureAccessor, chunkGenerator, group, world.random, mutable);
                                if (entry.isEmpty()) {
                                    break;
                                }

                                spawnEntry = entry.get();
                                randFloat = spawnEntry.minGroupSize + world.random.nextInt(1 + spawnEntry.maxGroupSize - spawnEntry.minGroupSize);
                            }

                            if (canSpawn(world, group, structureAccessor, chunkGenerator, spawnEntry, mutable, playerToDistance) && checker.test(spawnEntry.type, mutable, chunk)) {
                                MobEntity optional = createMob(world, spawnEntry.type);
                                if (optional == null) {
                                    return;
                                }

                                optional.refreshPositionAndAngles(offsetPosX, (double)posY, offsetPosZ, world.random.nextFloat() * 360.0F, 0.0F);
                                if (isValidSpawn(world, optional, playerToDistance)) {
                                    entityData = optional.initialize(world, world.getLocalDifficulty(optional.getBlockPos()), SpawnReason.NATURAL, entityData, (NbtCompound)null);
                                    ++j;
                                    ++p;
                                    world.spawnEntityAndPassengers(optional);
                                    runner.run(optional, chunk);
                                    if (j >= optional.getLimitPerChunk()) {
                                        return;
                                    }

                                    if (optional.spawnsTooManyForEachTry(p)) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
    private static boolean isValidSpawn(ServerWorld world, MobEntity entity, double squaredDistance) {
        if (squaredDistance > (double)(entity.getType().getSpawnGroup().getImmediateDespawnRange() * entity.getType().getSpawnGroup().getImmediateDespawnRange()) && entity.canImmediatelyDespawn(squaredDistance)) {
            return false;
        } else {
            return entity.canSpawn(world, SpawnReason.NATURAL) && entity.canSpawn(world);
        }
    }

}
