package calebzhou.rdi.core.client.screen;

import calebzhou.rdi.core.client.RdiSharedConstants;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.*;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by calebzhou on 2022-09-19,19:08.
 */
public class RdiDebugOverlay extends GuiComponent {
	private static final int COLOR_GREY = 14737632;
	private static final int MARGIN_RIGHT = 2;
	private static final int MARGIN_LEFT = 2;
	private static final int MARGIN_TOP = 2;
	private static final Map<Heightmap.Types, String> HEIGHTMAP_NAMES = Util.make(new EnumMap(Heightmap.Types.class), types -> {
		types.put(Heightmap.Types.WORLD_SURFACE_WG, "SW");
		types.put(Heightmap.Types.WORLD_SURFACE, "S");
		types.put(Heightmap.Types.OCEAN_FLOOR_WG, "OW");
		types.put(Heightmap.Types.OCEAN_FLOOR, "O");
		types.put(Heightmap.Types.MOTION_BLOCKING, "M");
		types.put(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "ML");
	});
	private final Minecraft minecraft;
	private final AllocationRateCalculator allocationRateCalculator;
	private final Font font;
	private HitResult block;
	private HitResult liquid;
	@Nullable
	private ChunkPos lastPos;
	@Nullable
	private LevelChunk clientChunk;
	@Nullable
	private CompletableFuture<LevelChunk> serverChunk;
	private static final int RED = -65536;
	private static final int YELLOW = -256;
	private static final int GREEN = -16711936;

	public RdiDebugOverlay(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.allocationRateCalculator = new AllocationRateCalculator();
		this.font = minecraft.font;
	}

	public void clearChunkCache() {
		this.serverChunk = null;
		this.clientChunk = null;
	}

	public void render(PoseStack poseStack) {
		this.minecraft.getProfiler().push("debug");
		Entity entity = this.minecraft.getCameraEntity();
		this.block = entity.pick(20.0, 0.0F, false);
		this.liquid = entity.pick(20.0, 0.0F, true);
		this.drawGameInformation(poseStack);
		this.drawSystemInformation(poseStack);
		if (this.minecraft.options.renderFpsChart) {
			int i = this.minecraft.getWindow().getGuiScaledWidth();
			this.drawChart(poseStack, this.minecraft.getFrameTimer(), 0, i / 2, true);
			IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
			if (integratedServer != null) {
				this.drawChart(poseStack, integratedServer.getFrameTimer(), i - Math.min(i / 2, 240), i / 2, false);
			}
		}

		this.minecraft.getProfiler().pop();
	}
	protected void drawGameInformation(PoseStack poseStack) {
		List<String> list = this.getGameInformation();

		for(int i = 0; i < list.size(); ++i) {
			String string = (String)list.get(i);
			if (!Strings.isNullOrEmpty(string)) {
				int j = 9;
				int k = this.font.width(string);
				int l = 2;
				int m = 2 + j * i;
				fill(poseStack, 1, m - 1, 2 + k + 1, m + j - 1, -1873784752);
				this.font.draw(poseStack, string, 2.0F, (float)m, 14737632);
			}
		}

	}
	protected void drawSystemInformation(PoseStack poseStack) {
		List<String> list = this.getSystemInformation();

		for(int i = 0; i < list.size(); ++i) {
			String string = (String)list.get(i);
			if (!Strings.isNullOrEmpty(string)) {
				int j = 9;
				int k = this.font.width(string);
				int l = this.minecraft.getWindow().getGuiScaledWidth() - 2 - k;
				int m = 2 + j * i;
				fill(poseStack, l - 1, m - 1, l + k + 1, m + j - 1, -1873784752);
				this.font.draw(poseStack, string, (float)l, (float)m, 14737632);
			}
		}

	}

	protected List<String> getGameInformation() {
		IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
		Connection connection = this.minecraft.getConnection().getConnection();
		float f = connection.getAverageSentPackets();
		float g = connection.getAverageReceivedPackets();
		String string;
		if (integratedServer != null) {
			string = String.format(Locale.ROOT, "Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", integratedServer.getAverageTickTime(), f, g);
		} else {
			string = String.format(Locale.ROOT, "%.0f发%.0f收", f, g);
		}

		BlockPos blockPos = this.minecraft.getCameraEntity().blockPosition();
		if (this.minecraft.showOnlyReducedInfo()) {
			return Lists.newArrayList(
					"Minecraft "
							+ SharedConstants.getCurrentVersion().getName()
							+ " ("
							+ this.minecraft.getLaunchedVersion()
							+ "/"
							+ ClientBrandRetriever.getClientModName()
							+ ")",
					this.minecraft.fpsString,
					string,
					this.minecraft.levelRenderer.getChunkStatistics(),
					this.minecraft.levelRenderer.getEntityStatistics(),
					"P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(),
					this.minecraft.level.gatherChunkSourceStats(),
					"",
					String.format(Locale.ROOT, "Chunk-relative: %d %d %d", blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15)
			);
		} else {
			Entity entity = this.minecraft.getCameraEntity();
			Direction direction = entity.getDirection();

			String towardsAxis = switch(direction) {
				case NORTH -> "-Z";
				case SOUTH -> "+Z";
				case WEST -> "-X";
				case EAST -> "+X";
				default -> "无";
			};
			ChunkPos chunkPos = new ChunkPos(blockPos);
			if (!Objects.equals(this.lastPos, chunkPos)) {
				this.lastPos = chunkPos;
				this.clearChunkCache();
			}

			Level level = this.getLevel();
			LongSet longSet = (LongSet)(level instanceof ServerLevel ? ((ServerLevel)level).getForcedChunks() : LongSets.EMPTY_SET);
			List<String> list = Lists.newArrayList(
					"Minecraft %s(%s)".formatted(SharedConstants.getCurrentVersion().getName(), RdiSharedConstants.MODID_DISPLAY+RdiSharedConstants.CORE_VERSION),
					this.minecraft.fpsString,
					string,
					this.minecraft.levelRenderer.getChunkStatistics(),
					this.minecraft.levelRenderer.getEntityStatistics(),
					"P: " + this.minecraft.particleEngine.countParticles() + ". T: " + this.minecraft.level.getEntityCount(),
					this.minecraft.level.gatherChunkSourceStats()
			);
			String string3 = this.getServerChunkStats();
			if (string3 != null) {
				list.add(string3);
			}

			list.add(this.minecraft.level.dimension().location() + " FC: " + longSet.size());
			list.add("");
			list.add(
					String.format(
							Locale.ROOT,
							"坐%.3f/%.5f/%.3f",
							this.minecraft.getCameraEntity().getX(),
							this.minecraft.getCameraEntity().getY(),
							this.minecraft.getCameraEntity().getZ()
					)
			);
			list.add(
					String.format(
							Locale.ROOT,
							"块%d %d %d [%d %d %d]",
							blockPos.getX(),
							blockPos.getY(),
							blockPos.getZ(),
							blockPos.getX() & 15,
							blockPos.getY() & 15,
							blockPos.getZ() & 15
					)
			);
			list.add(
					String.format(
							Locale.ROOT,
							"区%d %d %d [%d %d in r.%d.%d.mca]",
							chunkPos.x,
							SectionPos.blockToSectionCoord(blockPos.getY()),
							chunkPos.z,
							chunkPos.getRegionLocalX(),
							chunkPos.getRegionLocalZ(),
							chunkPos.getRegionX(),
							chunkPos.getRegionZ()
					)
			);
			list.add(
					String.format(Locale.ROOT, "朝%s(%s) (%.1f / %.1f)", direction, towardsAxis, Mth.wrapDegrees(entity.getYRot()), Mth.wrapDegrees(entity.getXRot()))
			);
			LevelChunk levelChunk = this.getClientChunk();
			if (levelChunk.isEmpty()) {
				list.add("等块..");
			} else {
				int i = this.minecraft.level.getChunkSource().getLightEngine().getRawBrightness(blockPos, 0);
				int j = this.minecraft.level.getBrightness(LightLayer.SKY, blockPos);
				int k = this.minecraft.level.getBrightness(LightLayer.BLOCK, blockPos);
				list.add("客亮" + i + "(" + j + "空" + k + "块)");
				LevelChunk levelChunk2 = this.getServerChunk();
				StringBuilder stringBuilder = new StringBuilder("CH");

				for(Heightmap.Types types : Heightmap.Types.values()) {
					if (types.sendToClient()) {
						stringBuilder.append(" ").append((String)HEIGHTMAP_NAMES.get(types)).append(": ").append(levelChunk.getHeight(types, blockPos.getX(), blockPos.getZ()));
					}
				}

				list.add(stringBuilder.toString());
				stringBuilder.setLength(0);
				stringBuilder.append("SH");

				for(Heightmap.Types types : Heightmap.Types.values()) {
					if (types.keepAfterWorldgen()) {
						stringBuilder.append(" ").append((String)HEIGHTMAP_NAMES.get(types)).append(": ");
						if (levelChunk2 != null) {
							stringBuilder.append(levelChunk2.getHeight(types, blockPos.getX(), blockPos.getZ()));
						} else {
							stringBuilder.append("??");
						}
					}
				}

				list.add(stringBuilder.toString());
				if (blockPos.getY() >= this.minecraft.level.getMinBuildHeight() && blockPos.getY() < this.minecraft.level.getMaxBuildHeight()) {
					list.add("群" + printBiome(this.minecraft.level.getBiome(blockPos)));
					long l = 0L;
					float h = 0.0F;
					if (levelChunk2 != null) {
						h = level.getMoonBrightness();
						l = levelChunk2.getInhabitedTime();
					}

					DifficultyInstance difficultyInstance = new DifficultyInstance(level.getDifficulty(), level.getDayTime(), l, h);
					list.add(
							String.format(
									Locale.ROOT,
									"难%.2f//%.2f(%d天)",
									difficultyInstance.getEffectiveDifficulty(),
									difficultyInstance.getSpecialMultiplier(),
									this.minecraft.level.getDayTime() / 24000L
							)
					);
				}

				if (levelChunk2 != null && levelChunk2.isOldNoiseGeneration()) {
					list.add("Blending: Old");
				}
			}

			ServerLevel serverLevel = this.getServerLevel();
			if (serverLevel != null) {
				ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
				ChunkGenerator chunkGenerator = serverChunkCache.getGenerator();
				RandomState randomState = serverChunkCache.randomState();
				chunkGenerator.addDebugScreenInfo(list, randomState, blockPos);
				Climate.Sampler sampler = randomState.sampler();
				BiomeSource biomeSource = chunkGenerator.getBiomeSource();
				biomeSource.addDebugInfo(list, blockPos, sampler);
				NaturalSpawner.SpawnState spawnState = serverChunkCache.getLastSpawnState();
				if (spawnState != null) {
					Object2IntMap<MobCategory> object2IntMap = spawnState.getMobCategoryCounts();
					int m = spawnState.getSpawnableChunkCount();
					list.add(
							"SC: "
									+ m
									+ ", "
									+ (String) Stream.of(MobCategory.values())
									.map(mobCategory -> Character.toUpperCase(mobCategory.getName().charAt(0)) + ": " + object2IntMap.getInt(mobCategory))
									.collect(Collectors.joining(", "))
					);
				} else {
					list.add("SC: N/A");
				}
			}

			PostChain postChain = this.minecraft.gameRenderer.currentEffect();
			if (postChain != null) {
				list.add("Shader: " + postChain.getName());
			}

			list.add(
					this.minecraft.getSoundManager().getDebugString()
							+ String.format(Locale.ROOT, " (Mood %d%%)", Math.round(this.minecraft.player.getCurrentMood() * 100.0F))
			);
			return list;
		}
	}

	private static String printBiome(Holder<Biome> biomeHolder) {
		return biomeHolder.unwrap().map(registryKey -> registryKey.location().toString(), biome -> "[unregistered " + biome + "]");
	}

	@Nullable
	private ServerLevel getServerLevel() {
		IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
		return integratedServer != null ? integratedServer.getLevel(this.minecraft.level.dimension()) : null;
	}

	@Nullable
	private String getServerChunkStats() {
		ServerLevel serverLevel = this.getServerLevel();
		return serverLevel != null ? serverLevel.gatherChunkSourceStats() : null;
	}

	private Level getLevel() {
		return DataFixUtils.orElse(
				Optional.ofNullable(this.minecraft.getSingleplayerServer()).flatMap(server -> Optional.ofNullable(server.getLevel(this.minecraft.level.dimension()))),
				this.minecraft.level
		);
	}

	@Nullable
	private LevelChunk getServerChunk() {
		if (this.serverChunk == null) {
			ServerLevel serverLevel = this.getServerLevel();
			if (serverLevel != null) {
				this.serverChunk = serverLevel.getChunkSource()
						.getChunkFuture(this.lastPos.x, this.lastPos.z, ChunkStatus.FULL, false)
						.thenApply(either -> either.map(chunk -> (LevelChunk)chunk, chunkLoadingFailure -> null));
			}

			if (this.serverChunk == null) {
				this.serverChunk = CompletableFuture.completedFuture(this.getClientChunk());
			}
		}

		return (LevelChunk)this.serverChunk.getNow(null);
	}

	private LevelChunk getClientChunk() {
		if (this.clientChunk == null) {
			this.clientChunk = this.minecraft.level.getChunk(this.lastPos.x, this.lastPos.z);
		}

		return this.clientChunk;
	}

	protected List<String> getSystemInformation() {
		long l = Runtime.getRuntime().maxMemory();
		long m = Runtime.getRuntime().totalMemory();
		long n = Runtime.getRuntime().freeMemory();
		long o = m - n;
		List<String> list = Lists.newArrayList(
				String.format(Locale.ROOT, "Java %s %dbit", System.getProperty("java.version"), this.minecraft.is64Bit() ? 64 : 32),
				String.format(Locale.ROOT, "RAM% 2d%% %03d/%03dMB", o * 100L / l, bytesToMegabytes(o), bytesToMegabytes(l)),
				String.format(Locale.ROOT, "配率%03dMB /s", bytesToMegabytes(this.allocationRateCalculator.bytesAllocatedPerSecond(o))),
				String.format(Locale.ROOT, "已配% 2d%% %03dMB", m * 100L / l, bytesToMegabytes(m)),
				"",
				String.format(Locale.ROOT, "CPU %s", GlUtil.getCpuInfo()),
				"",
				String.format(
						Locale.ROOT, "显 %dx%d (%s)", Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight(), GlUtil.getVendor()
				),
				GlUtil.getRenderer(),
				GlUtil.getOpenGLVersion()
		);
		if (this.minecraft.showOnlyReducedInfo()) {
			return list;
		} else {
			if (this.block.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos = ((BlockHitResult)this.block).getBlockPos();
				BlockState blockState = this.minecraft.level.getBlockState(blockPos);
				list.add("");
				list.add(ChatFormatting.UNDERLINE + "块" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
				list.add(String.valueOf(Registry.BLOCK.getKey(blockState.getBlock())));

				for(Map.Entry<Property<?>, Comparable<?>> entry : blockState.getValues().entrySet()) {
					list.add(this.getPropertyValueString(entry));
				}

				blockState.getTags().map(tag -> "#" + tag.location()).forEach(list::add);
			}

			if (this.liquid.getType() == HitResult.Type.BLOCK) {
				BlockPos blockPos = ((BlockHitResult)this.liquid).getBlockPos();
				FluidState fluidState = this.minecraft.level.getFluidState(blockPos);
				list.add("");
				list.add(ChatFormatting.UNDERLINE + "流" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ());
				list.add(String.valueOf(Registry.FLUID.getKey(fluidState.getType())));

				for(Map.Entry<Property<?>, Comparable<?>> entry : fluidState.getValues().entrySet()) {
					list.add(this.getPropertyValueString(entry));
				}

				fluidState.getTags().map(tag -> "#" + tag.location()).forEach(list::add);
			}

			Entity entity = this.minecraft.crosshairPickEntity;
			if (entity != null) {
				list.add("");
				list.add(ChatFormatting.UNDERLINE + "实");
				list.add(String.valueOf(Registry.ENTITY_TYPE.getKey(entity.getType())));
			}

			return list;
		}
	}

	private String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> entry) {
		Property<?> property = (Property)entry.getKey();
		Comparable<?> comparable = (Comparable)entry.getValue();
		String string = Util.getPropertyName(property, comparable);
		if (Boolean.TRUE.equals(comparable)) {
			string = ChatFormatting.GREEN + string;
		} else if (Boolean.FALSE.equals(comparable)) {
			string = ChatFormatting.RED + string;
		}

		return property.getName() + ": " + string;
	}

	/**
	 * @param drawForFps If set to true, will draw debugChart for FPS. If set to false, will draw for TPS.
	 */
	private void drawChart(PoseStack poseStack, FrameTimer frameTimer, int x, int width, boolean drawForFps) {
		RenderSystem.disableDepthTest();
		int i = frameTimer.getLogStart();
		int j = frameTimer.getLogEnd();
		long[] ls = frameTimer.getLog();
		int l = x;
		int m = Math.max(0, ls.length - width);
		int n = ls.length - m;
		int k = frameTimer.wrapIndex(i + m);
		long o = 0L;
		int p = Integer.MAX_VALUE;
		int q = Integer.MIN_VALUE;

		for(int r = 0; r < n; ++r) {
			int s = (int)(ls[frameTimer.wrapIndex(k + r)] / 1000000L);
			p = Math.min(p, s);
			q = Math.max(q, s);
			o += (long)s;
		}

		int r = this.minecraft.getWindow().getGuiScaledHeight();
		fill(poseStack, x, r - 60, x + n, r, -1873784752);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		for(Matrix4f matrix4f = Transformation.identity().getMatrix(); k != j; k = frameTimer.wrapIndex(k + 1)) {
			int t = frameTimer.scaleSampleTo(ls[k], drawForFps ? 30 : 60, drawForFps ? 60 : 20);
			int u = drawForFps ? 100 : 60;
			int v = this.getSampleColor(Mth.clamp(t, 0, u), 0, u / 2, u);
			int w = v >> 24 & 0xFF;
			int y = v >> 16 & 0xFF;
			int z = v >> 8 & 0xFF;
			int aa = v & 0xFF;
			bufferBuilder.vertex(matrix4f, (float)(l + 1), (float)r, 0.0F).color(y, z, aa, w).endVertex();
			bufferBuilder.vertex(matrix4f, (float)(l + 1), (float)(r - t + 1), 0.0F).color(y, z, aa, w).endVertex();
			bufferBuilder.vertex(matrix4f, (float)l, (float)(r - t + 1), 0.0F).color(y, z, aa, w).endVertex();
			bufferBuilder.vertex(matrix4f, (float)l, (float)r, 0.0F).color(y, z, aa, w).endVertex();
			++l;
		}

		BufferUploader.drawWithShader(bufferBuilder.end());
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
		if (drawForFps) {
			fill(poseStack, x + 1, r - 30 + 1, x + 14, r - 30 + 10, -1873784752);
			this.font.draw(poseStack, "60 FPS", (float)(x + 2), (float)(r - 30 + 2), 14737632);
			this.hLine(poseStack, x, x + n - 1, r - 30, -1);
			fill(poseStack, x + 1, r - 60 + 1, x + 14, r - 60 + 10, -1873784752);
			this.font.draw(poseStack, "30 FPS", (float)(x + 2), (float)(r - 60 + 2), 14737632);
			this.hLine(poseStack, x, x + n - 1, r - 60, -1);
		} else {
			fill(poseStack, x + 1, r - 60 + 1, x + 14, r - 60 + 10, -1873784752);
			this.font.draw(poseStack, "20 TPS", (float)(x + 2), (float)(r - 60 + 2), 14737632);
			this.hLine(poseStack, x, x + n - 1, r - 60, -1);
		}

		this.hLine(poseStack, x, x + n - 1, r - 1, -1);
		this.vLine(poseStack, x, r - 60, r, -1);
		this.vLine(poseStack, x + n - 1, r - 60, r, -1);
		int t = this.minecraft.options.framerateLimit().get();
		if (drawForFps && t > 0 && t <= 250) {
			this.hLine(poseStack, x, x + n - 1, r - 1 - (int)(1800.0 / (double)t), -16711681);
		}

		String string = p + "ms低";
		String string2 = o / (long)n + "ms均";
		String string3 = q + " ms高";
		this.font.drawShadow(poseStack, string, (float)(x + 2), (float)(r - 60 - 9), 14737632);
		this.font.drawShadow(poseStack, string2, (float)(x + n / 2 - this.font.width(string2) / 2), (float)(r - 60 - 9), 14737632);
		this.font.drawShadow(poseStack, string3, (float)(x + n - this.font.width(string3)), (float)(r - 60 - 9), 14737632);
		RenderSystem.enableDepthTest();
	}

	private int getSampleColor(int height, int heightMin, int heightMid, int heightMax) {
		return height < heightMid
				? this.colorLerp(-16711936, -256, (float)height / (float)heightMid)
				: this.colorLerp(-256, -65536, (float)(height - heightMid) / (float)(heightMax - heightMid));
	}

	private int colorLerp(int col1, int col2, float factor) {
		int i = col1 >> 24 & 0xFF;
		int j = col1 >> 16 & 0xFF;
		int k = col1 >> 8 & 0xFF;
		int l = col1 & 0xFF;
		int m = col2 >> 24 & 0xFF;
		int n = col2 >> 16 & 0xFF;
		int o = col2 >> 8 & 0xFF;
		int p = col2 & 0xFF;
		int q = Mth.clamp((int)Mth.lerp(factor, (float)i, (float)m), 0, 255);
		int r = Mth.clamp((int)Mth.lerp(factor, (float)j, (float)n), 0, 255);
		int s = Mth.clamp((int)Mth.lerp(factor, (float)k, (float)o), 0, 255);
		int t = Mth.clamp((int)Mth.lerp(factor, (float)l, (float)p), 0, 255);
		return q << 24 | r << 16 | s << 8 | t;
	}

	private static long bytesToMegabytes(long bytes) {
		return bytes / 1024L / 1024L;
	}

	@Environment(EnvType.CLIENT)
	static class AllocationRateCalculator {
		private static final int UPDATE_INTERVAL_MS = 500;
		private static final List<GarbageCollectorMXBean> GC_MBEANS = ManagementFactory.getGarbageCollectorMXBeans();
		private long lastTime = 0L;
		private long lastHeapUsage = -1L;
		private long lastGcCounts = -1L;
		private long lastRate = 0L;

		long bytesAllocatedPerSecond(long l) {
			long m = System.currentTimeMillis();
			if (m - this.lastTime < 500L) {
				return this.lastRate;
			} else {
				long n = gcCounts();
				if (this.lastTime != 0L && n == this.lastGcCounts) {
					double d = (double) TimeUnit.SECONDS.toMillis(1L) / (double)(m - this.lastTime);
					long o = l - this.lastHeapUsage;
					this.lastRate = Math.round((double)o * d);
				}

				this.lastTime = m;
				this.lastHeapUsage = l;
				this.lastGcCounts = n;
				return this.lastRate;
			}
		}

		private static long gcCounts() {
			long l = 0L;

			for(GarbageCollectorMXBean garbageCollectorMXBean : GC_MBEANS) {
				l += garbageCollectorMXBean.getCollectionCount();
			}

			return l;
		}
	}
}
