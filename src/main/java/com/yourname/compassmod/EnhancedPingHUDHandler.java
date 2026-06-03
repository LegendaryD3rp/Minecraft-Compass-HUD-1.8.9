package com.yourname.compassmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.network.NetworkPlayerInfo;

public class EnhancedPingHUDHandler {
    private static final long PING_CACHE_DURATION = 1000L;
    private static final int MAX_PING_VALUE = 10000;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final RealPingCalculator realPingCalc;
    private final TrafficBasedPingCalculator trafficPingCalc;

    private long lastPingUpdate = 0;
    private PingData cachedPingData;

    // 调试标志
    private boolean debugMode = true;

    public EnhancedPingHUDHandler() {
        this.realPingCalc = new RealPingCalculator();
        this.trafficPingCalc = new TrafficBasedPingCalculator();

        // 注册事件监听
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(realPingCalc);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(trafficPingCalc);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (shouldSkipRendering(event)) return;

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        renderEnhancedPingHUD(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
    }

    private boolean shouldSkipRendering(RenderGameOverlayEvent event) {
        return event.type != RenderGameOverlayEvent.ElementType.TEXT ||
                mc.thePlayer == null ||
                mc.gameSettings.hideGUI ||
                !CompassMod.config.showPingHUD;
    }

    private void renderEnhancedPingHUD(int screenWidth, int screenHeight) {
        FontRenderer fr = mc.fontRendererObj;
        PingData pingData = getCurrentPingData();

        // 调试信息：显示所有三种ping值
        if (debugMode) {
            String debugText = String.format("服务器: %dms | 真实: %dms | 流量: %dms",
                    pingData.serverPing, pingData.realPing, pingData.trafficPing);
            fr.drawStringWithShadow(debugText, 5, 5, 0xFFFFFF);
        }

        String pingText = formatPingText(pingData);
        int textWidth = fr.getStringWidth(pingText);

        int xPos = calculatePingXPosition(screenWidth, textWidth);
        int yPos = calculatePingYPosition(screenHeight, 10);
        int color = getPingColor(pingData.displayPing);

        fr.drawStringWithShadow(pingText, xPos, yPos, color);
        renderNetworkDetails(fr, xPos, yPos, pingData);
    }

    private PingData getCurrentPingData() {
        long currentTime = System.currentTimeMillis();
        if (cachedPingData != null && currentTime - lastPingUpdate < PING_CACHE_DURATION) {
            return cachedPingData;
        }

        // 确保所有三种方法都被调用
        long serverPing = getServerReportedPing();
        long realPing = realPingCalc.getRealPing();
        long trafficPing = trafficPingCalc.getTrafficBasedPing();

        // 调试输出
      //  if (debugMode) {
    //        System.out.printf("[PingDebug] 服务器: %d, 真实: %d, 流量: %d%n",
        //            serverPing, realPing, trafficPing);
     //   }

        long displayPing = selectBestPingEstimate(serverPing, realPing, trafficPing);

        cachedPingData = new PingData(serverPing, realPing, trafficPing, displayPing);
        lastPingUpdate = currentTime;
        return cachedPingData;
    }

    private long selectBestPingEstimate(long serverPing, long realPing, long trafficPing) {
        int trafficConfidence = trafficPingCalc.getDataConfidence();

        // 调试信息
        if (debugMode) {
         //   System.out.printf("[PingSelect] 流量可信度: %d%%, 流量Ping: %d, 真实Ping: %d%n",
         //           trafficConfidence, trafficPing, realPing);
        }

        // 修复：确保所有方法都有机会被选择
        boolean hasValidTrafficPing = trafficPing > 0 && trafficPing < MAX_PING_VALUE;
        boolean hasValidRealPing = realPing > 0 && realPing < MAX_PING_VALUE;
        boolean hasValidServerPing = serverPing > 0 && serverPing < MAX_PING_VALUE;

        // 优先级逻辑修复
        if (hasValidTrafficPing && trafficConfidence > 30) { // 降低阈值到30%
       //     if (debugMode) System.out.println("[PingSelect] 选择流量分析Ping");
            return trafficPing;
        }

        if (hasValidRealPing) {
         //   if (debugMode) System.out.println("[PingSelect] 选择真实Ping");
            return realPing;
        }

        if (hasValidServerPing) {
       //     if (debugMode) System.out.println("[PingSelect] 选择服务器Ping");
            return serverPing;
        }

     //   if (debugMode) System.out.println("[PingSelect] 使用默认值0");
        return 0;
    }

    private String formatPingText(PingData pingData) {
        StringBuilder sb = new StringBuilder();
        sb.append("延迟: ").append(pingData.displayPing).append("ms");

        String quality = trafficPingCalc.getPingQualityIndicator();
        String confidenceIndicator = getConfidenceIndicator(trafficPingCalc.getDataConfidence());

        sb.append(" [").append(quality).append(confidenceIndicator).append("]");

        // 检测可能的ping伪造
        if (pingData.serverPing <= 1 && (pingData.realPing > 50 || pingData.trafficPing > 50)) {
            sb.append(" *");
        }

        return sb.toString();
    }

    private String getConfidenceIndicator(int confidence) {
        if (confidence >= 80) return "✓";
        if (confidence >= 60) return "~";
        if (confidence >= 40) return "?";
        return "!";
    }

    private void renderNetworkDetails(FontRenderer fr, int xPos, int yPos, PingData pingData) {
        if (!CompassMod.config.showNetworkDetails) return;

        String[] details = {
                "质量: " + trafficPingCalc.getPingQualityIndicator(),
                "方法: " + trafficPingCalc.getEstimationMethod(),
                "可信度: " + trafficPingCalc.getDataConfidence() + "%",
                "服务器Ping: " + pingData.serverPing + "ms",
                "真实Ping: " + pingData.realPing + "ms"
        };

        for (int i = 0; i < details.length; i++) {
            fr.drawStringWithShadow(details[i], xPos, yPos + 10 + (i * 10), 0xAAAAAA);
        }
    }

    private long getServerReportedPing() {
        if (mc.getNetHandler() == null || mc.thePlayer == null) {
        //    if (debugMode) System.out.println("[ServerPing] 无法获取服务器Ping: 网络处理器或玩家为null");
            return 0;
        }

        try {
            NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            long ping = playerInfo != null ? playerInfo.getResponseTime() : 0;
            //       if (debugMode) System.out.println("[ServerPing] 获取到服务器Ping: " + ping + "ms");
            return ping;
        } catch (Exception e) {
      //      if (debugMode) System.out.println("[ServerPing] 获取服务器Ping异常: " + e.getMessage());
            return 0;
        }
    }

    private int getPingColor(long ping) {
        if (ping <= 0) return CompassMod.config.pingColor;

        if (ping <= CompassMod.config.goodPingThreshold) {
            return CompassMod.config.pingGoodColor;
        } else if (ping <= CompassMod.config.mediumPingThreshold) {
            return CompassMod.config.pingMediumColor;
        } else {
            return CompassMod.config.pingBadColor;
        }
    }

    private int calculatePingXPosition(int screenWidth, int textWidth) {
        int offset = CompassMod.config.pingHudX;
        return offset >= 0 ? offset : screenWidth + offset - textWidth;
    }

    private int calculatePingYPosition(int screenHeight, int textHeight) {
        int offset = CompassMod.config.pingHudY;
        return offset >= 0 ? offset : screenHeight + offset - textHeight;
    }

    // 添加方法用于数据包处理
    public void onPacketSent(net.minecraft.network.Packet<?> packet) {
        realPingCalc.onPacketSent(packet);
        trafficPingCalc.onPacketSent(packet); // 新增
    }

    public void onPacketReceived(net.minecraft.network.Packet<?> packet) {
        realPingCalc.onPacketReceived(packet);
        trafficPingCalc.onPacketReceived(packet); // 新增
    }
    private static class PingData {
        final long serverPing;
        final long realPing;
        final long trafficPing;
        final long displayPing;

        PingData(long serverPing, long realPing, long trafficPing, long displayPing) {
            this.serverPing = serverPing;
            this.realPing = realPing;
            this.trafficPing = trafficPing;
            this.displayPing = displayPing;
        }
    }
}