package com.yourname.compassmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class ForceSprintHandler {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private KeyBinding toggleKeyBinding;
    private boolean lastToggleState = false;
    private boolean isSprintForced = false;
    private boolean needsStateUpdate = false; // 新增：状态更新标志

    public ForceSprintHandler() {
        toggleKeyBinding = new KeyBinding("切换强制疾跑", CompassMod.config.forceSprintKey, "Compass Mod");
        ClientRegistry.registerKeyBinding(toggleKeyBinding);
        isSprintForced = CompassMod.config.forceSprintEnabled;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!CompassMod.config.enableForceSprint) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // 检测按键切换
        boolean currentToggleState = toggleKeyBinding.isPressed();
        if (currentToggleState && !lastToggleState) {
            // 切换强制疾跑状态
            isSprintForced = !isSprintForced;
            CompassMod.config.forceSprintEnabled = isSprintForced;

            // 立即保存配置
            CompassMod.config.config.get(Configuration.CATEGORY_CLIENT, "forceSprintEnabled", true)
                    .set(isSprintForced);
            CompassMod.config.saveConfig();

            // 设置状态更新标志
            needsStateUpdate = true;

            // 立即应用状态变化
            applySprintStateImmediately();

            // 调试信息
            if (CompassMod.config.showDebugInfo) {
                System.out.println("[ForceSprint] 状态切换: " + (isSprintForced ? "开启" : "关闭"));
            }
        }
        lastToggleState = currentToggleState;

        // 应用强制疾跑
        if (isSprintForced) {
            int sprintKeyCode = mc.gameSettings.keyBindSprint.getKeyCode();
            KeyBinding.setKeyBindState(sprintKeyCode, true);

            // 确保玩家处于疾跑状态
            if (mc.thePlayer != null && !mc.thePlayer.isSprinting()) {
                mc.thePlayer.setSprinting(true);
            }
        }

        // 处理状态更新
        if (needsStateUpdate) {
            applySprintStateImmediately();
            needsStateUpdate = false;
        }
    }

    /**
     * 立即应用疾跑状态变化
     */
    private void applySprintStateImmediately() {
        if (mc.thePlayer == null) return;

        if (isSprintForced) {
            // 强制开启疾跑
            int sprintKeyCode = mc.gameSettings.keyBindSprint.getKeyCode();
            KeyBinding.setKeyBindState(sprintKeyCode, true);
            mc.thePlayer.setSprinting(true);

            // 确保疾跑状态持续
            if (CompassMod.config.showDebugInfo) {
                System.out.println("[ForceSprint] 立即应用疾跑状态: 开启");
            }
        } else {
            // 关闭强制疾跑，但不影响手动疾跑
            int sprintKeyCode = mc.gameSettings.keyBindSprint.getKeyCode();
            boolean isManuallySprinting = Keyboard.isKeyDown(sprintKeyCode);

            if (!isManuallySprinting) {
                KeyBinding.setKeyBindState(sprintKeyCode, false);
            }

            if (CompassMod.config.showDebugInfo) {
                System.out.println("[ForceSprint] 立即应用疾跑状态: 关闭");
            }
        }
    }

    /**
     * 从配置重新加载状态（修复版本）
     */
    public void reloadFromConfig() {
        boolean newState = CompassMod.config.forceSprintEnabled;
        if (newState != isSprintForced) {
            isSprintForced = newState;
            needsStateUpdate = true;

            if (CompassMod.config.showDebugInfo) {
                System.out.println("[ForceSprint] 从配置重新加载状态: " + (isSprintForced ? "开启" : "关闭"));
            }
        }
    }

    /**
     * 获取当前强制疾跑状态
     */
    public boolean isSprintForced() {
        return isSprintForced;
    }

    /**
     * 外部强制状态更新
     */
    public void forceStateUpdate() {
        needsStateUpdate = true;
    }
}