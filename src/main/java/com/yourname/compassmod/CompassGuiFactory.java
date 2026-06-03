package com.yourname.compassmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import java.util.Set;

public class CompassGuiFactory implements IModGuiFactory {

    // 移除有问题的@Override注解 - 这些方法在1.8.9 API中可能不需要@Override
    public void initialize(Minecraft minecraftInstance) {
        // 初始化方法 - 在1.8.9中通常为空实现
    }

    @Override  // 这个通常可以安全保留
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        // 返回配置GUI主类
        return CompassConfigGUI.class;
    }

    @Override  // 这个通常可以安全保留
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        // 返回null表示不使用运行时分类
        return null;
    }

    // 移除@Override注解 - 这个方法在1.8.9中可能不是接口的必需方法
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        // 返回null表示使用默认处理器
        return null;
    }

    // 移除@Override注解 - 这个方法在1.8.9中可能不是接口的必需方法
    public boolean hasConfigGui() {
        // 返回true表示此mod有配置GUI
        return true;
    }

    // 移除@Override注解 - 这个方法在1.8.9中可能不是接口的必需方法
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        // 创建配置GUI实例
        return new CompassConfigGUI(parentScreen);
    }
}