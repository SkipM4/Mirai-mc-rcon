package cn.ltcraft;

import cn.ltcraft.rcon.Rcon;
import cn.ltcraft.rcon.ex.AuthenticationException;
import net.mamoe.mirai.console.command.*;
import net.mamoe.mirai.console.plugins.Config;
import net.mamoe.mirai.console.plugins.PluginBase;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;


class Main extends PluginBase {
    private Map<Long, Config> configs = new LinkedHashMap<Long, Config>();
    private Map<Long, Rcon> rcons = new LinkedHashMap<Long, Rcon>();
    private List<Long> defaultList;
    private List<String> defaultPrefixes;

    public void onLoad(){
        defaultList = new LinkedList<Long>();
        defaultList.add(123456);
        defaultPrefixes = new ArrayList<>();
        defaultPrefixes.add("sudo ");
        checkSample();
        loadConfigFiles();
        super.onLoad();
    }

    @Override
    public void onDisable() {
        disconnects();
        configs.clear();
        super.onDisable();
    }

    public void onEnable(){
        load();
        Listener listener = new Listener(this);
        JCommandManager.getInstance().register(this, new BlockingCommand(
                "rcon", new ArrayList<>(),"rcon插件指令","/rcon <add/remove>"
        ) {
            @Override
            public boolean onCommandBlocking(@NotNull CommandSender commandSender, @NotNull List<String> list) {
                if(list.size() < 1){
                    return false;
                }
                Long groupID;
                switch (list.get(0)){
                    case "add":
                        if(list.size() < 2){
                            commandSender.sendMessageBlocking("/rcon add 群号");
                            return true;
                        }
                        System.out.println(list.get(1));
                        groupID = Long.valueOf(list.get(1));
                        Config config = loadConfig(groupID+".yml");
                        config.setIfAbsent("groupID", groupID);
                        config.setIfAbsent("serverAddr", "");
                        config.setIfAbsent("serverPort", 0);
                        config.setIfAbsent("password", "");
                        config.setIfAbsent("canPerform", defaultList);
                        config.setIfAbsent("prefixes", defaultPrefixes);
                        config.save();
                        commandSender.sendMessageBlocking("添加成功,请修改plugins/Rcon/"+groupID+".yml,并重载本插件或重启Mirai");
                    break;
                    case "reload":
                        loadConfigFiles();
                        load();
                        commandSender.sendMessageBlocking("重载完成！");
                    break;
                    case "remove":
                        if(list.size() < 2){
                            commandSender.sendMessageBlocking("/rcon remove 群号");
                            return true;
                        }
                        groupID = Long.valueOf(list.get(1));
                    break;
                    default:
                        commandSender.sendMessageBlocking("添加群/rcon add 群号");
                        commandSender.sendMessageBlocking("删除群/rcon remove 群号");
                        commandSender.sendMessageBlocking("重载/rcon reload");
                    return false;
                }
                return true;
            }
        });
        super.onEnable();
    }
    public String command(String command, Rcon rcon) throws IOException {
        String results = rcon.command(command);
        if (results.length()>0) {
            results = utils.clean(results);
            return results;
        }else {
            return "执行成功！服务器返回空！";
        }
    }
    public void disconnects(){
        for (Rcon rcon : rcons.values()){
            try {
                rcon.disconnect();
            } catch (IOException e) {

            }
        }
        rcons.clear();
    }
    public void checkSample(){
        File file = new File(getDataFolder().getPath()+"/example.yml");
        if (!file.exists()){
            InputStream inputStream = getResources("example.yml");
            OutputStream outputStream = null;
            try {
                outputStream = new DataOutputStream(new FileOutputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            byte[] bytes;
            try {
                bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                outputStream.write(bytes);
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public void load(){
        disconnects();
        Rcon rcon;
        Config config;
        for (Long groupID : configs.keySet()){
            config = configs.get(groupID);

            if (utils.configNormal(config)) {
                config.set("groupID", groupID);
                rcon = connected(config);
                if (rcon == null) {
                    continue;
                }
                rcons.put(groupID, rcon);
            }else{
                getLogger().info(groupID+"配置文件似乎格式不对呢!");
            }
        }
    }
    public Rcon connected(Config config){
        try {
            getLogger().info("尝试连接"+config.getString("serverAddr")+":"+config.getInt("serverPort")+"中...");
            Rcon rcon = new Rcon(config);
            return rcon;
        } catch (IOException| AuthenticationException e) {
            getLogger().info("没能连接上"+config.getString("serverAddr")+":"+config.getInt("serverPort")+"呢!再检查一下配置文件吧!");
            e.printStackTrace();
            return null;
        }
    }
    public void loadConfigFiles(){
        configs.clear();
        File file = getDataFolder();
        File[] fs = file.listFiles();
        Config config;
        //不懂 为什么不能Config.load(new File())
        for(File f:fs){
            if(!f.isDirectory() && f.getName().endsWith(".yml") && !f.getName().equals("example.yml")){
                config = loadConfig(f.getName());
                if (config!=null) {
                    String fileName = f.getName();
                    Long groupID = Long.valueOf(fileName.substring(0, fileName.lastIndexOf(".")));
                    try {
                        config.setIfAbsent("groupID", 0);
                        config.setIfAbsent("serverAddr", "");
                        config.setIfAbsent("serverPort", 0);
                        config.setIfAbsent("passworld", "");
                        config.setIfAbsent("canPerform", defaultList);
                        config.setIfAbsent("prefixes", defaultPrefixes);
                        if (config.getLong("groupID")!=0){
                            groupID = config.getLong("groupID");
                        }
                    } catch (NoSuchElementException e){
                        //使用文件名
                    }
                    configs.put(groupID, config);
                }
            }
        }
    }

    public Map<Long, Rcon> getRcon() {
        return rcons;
    }
}
