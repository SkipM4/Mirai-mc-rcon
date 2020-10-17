package cn.ltcraft;

import cn.ltcraft.rcon.Rcon;
import java.io.IOException;
import net.mamoe.mirai.message.GroupMessage;

public class Listener {
    private Main plugin;

    public Listener(Main plugin) {
        this.plugin = plugin;
        this.registerEvents();
    }
    private void registerEvents() {
        this.plugin.getEventListener().subscribeAlways(GroupMessage.class, (event) -> {
            Rcon rcon = this.plugin.getRcon().get(event.getGroup().getId());
            if (rcon == null) {
                return;
            }
            String message = event.getMessage().contentToString();
            for (String prefix : rcon.getConfig().getStringList("prefixes")) {
                if (message.startsWith(prefix)) {
                    if (rcon.getConfig().getLongList("canPerform").contains(event.getSender().getId())) {
                        message = message.substring(prefix.length());

                        try {
                            event.getSubject().sendMessage(this.plugin.command(message, rcon));
                        } catch (IOException e) {
                            this.plugin.getLogger().info("正在尝试重连" + rcon.getConfig().getString("serverAddr") + ":" + rcon.getConfig().getInt("serverPort") + "中...");
                            try {
                                rcon.disconnect();
                            } catch (IOException ex) {

                            }
                            Rcon rcon1 = this.plugin.connected(rcon.getConfig());
                            if (rcon1 == null) {
                                event.getSubject().sendMessage("没能连接上服务器RCON呢!再检查一下配置文件吧!");
                            } else {
                                try {
                                    this.plugin.getRcon().put(event.getGroup().getId(), rcon1);
                                    event.getSubject().sendMessage(this.plugin.command(message, rcon1));
                                } catch (IOException e2) {
                                    e2.printStackTrace();
                                    event.getSubject().sendMessage("指令执行失败了呢!");
                                }
                            }
                        }
                    }
                    break;
                }
            }
        });
    }
}
