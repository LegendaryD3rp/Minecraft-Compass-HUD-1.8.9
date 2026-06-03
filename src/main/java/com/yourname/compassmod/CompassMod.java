package com.yourname.compassmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import java.io.File;

@Mod(modid = CompassMod.MODID, version = CompassMod.VERSION, guiFactory = "com.yourname.compassmod.CompassGuiFactory")
public class CompassMod {
    public static final String MODID = "compassmod";
    public static final String VERSION = "1.0";
    // 将config改为public static以便访问
    public static CompassConfig config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // 初始化配置
        File configFile = new File(event.getModConfigurationDirectory(), "compassmod.cfg");
        config = new CompassConfig(configFile);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // 注册事件处理器
        MinecraftForge.EVENT_BUS.register(new PlayerHUDHandler());
        MinecraftForge.EVENT_BUS.register(new CompassHUDHandler());
        MinecraftForge.EVENT_BUS.register(new RealTimeHUDHandler()); // 新增时间显示处理器
        MinecraftForge.EVENT_BUS.register(new SpeedHUDHandler());
        MinecraftForge.EVENT_BUS.register(new GameTimeHUDHandler());
        MinecraftForge.EVENT_BUS.register(new CPSHUDHandler());
        MinecraftForge.EVENT_BUS.register(new DistanceHUDHandler());
        MinecraftForge.EVENT_BUS.register(new CoordinatesHUDHandler()); // 新增坐标显示处理器
        MinecraftForge.EVENT_BUS.register(new EnhancedPingHUDHandler());
        MinecraftForge.EVENT_BUS.register(new RealPingCalculator());
        MinecraftForge.EVENT_BUS.register(new TrafficBasedPingCalculator());
        KeysDisplayHandler.register();

        ForceSprintHandler forceSprintHandler = new ForceSprintHandler();
        SprintStatusHandler sprintStatusHandler = new SprintStatusHandler();

        // 设置处理器间的依赖
        sprintStatusHandler.setForceSprintHandler(forceSprintHandler);

        // 注册事件处理器
        MinecraftForge.EVENT_BUS.register(forceSprintHandler);
        MinecraftForge.EVENT_BUS.register(sprintStatusHandler);

        // 注册配置变更监听器
        MinecraftForge.EVENT_BUS.register(new ConfigChangeHandler(forceSprintHandler));
        // 注册配置变更监听器 - 使用正确的两个参数
        MinecraftForge.EVENT_BUS.register(new ConfigChangeHandler(forceSprintHandler));

        MinecraftForge.EVENT_BUS.register(new PotionStatusHUDHandler());




        // 注册简化强制潜行功能
        SimpleForceSneakHandler sneakHandler = new SimpleForceSneakHandler();
        SimpleSneakStatusHUD sneakStatusHUD = new SimpleSneakStatusHUD();

        // 设置处理器间的依赖
        sneakStatusHUD.setSneakHandler(sneakHandler);

        // 注册事件处理器
        MinecraftForge.EVENT_BUS.register(sneakHandler);
        MinecraftForge.EVENT_BUS.register(sneakStatusHUD);
        // 注册方块描边处理器
        MinecraftForge.EVENT_BUS.register(new BlockOutlineHandler());
        MinecraftForge.EVENT_BUS.register(new ItemInfoHUDHandler());
        MinecraftForge.EVENT_BUS.register(new LowHealthWarningHandler());
        MinecraftForge.EVENT_BUS.register(new TargetHealthHandler());
    }
}
