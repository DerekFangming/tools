package com.fmning.tools.service.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class DiscordMiscService {

    private List<String> nbList = new ArrayList<String>() {{
        add("可太牛逼了");
        add("真是帅炸了");
        add("tql tql tql");
        add("带带我带带我");
        add("真会玩");
        add("沃日这波无敌啊");
        add("大腿带带我");
        add("还有这种操作，学到了学到了");
        add("6666666666 很骚");
        add("哇 好帅啊");
        add("大佬 tql");
        add("超神啦超神啦");
        add("有内味儿了");
        add("有点东西");
        add("对面就这？就这？");
        add("美汁汁儿啊！");
    }};

    private Random random = new Random();

    public void health(MessageChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("妖风电竞 bot指令")
                .setDescription(
                        "**----------------------------------------------组队----------------------------------------------**\n" +
                        "**绑定Origin ID：**`yf apex link ID`\n将指令里的ID替换成你的Origin ID即可。" +
                        "绑定之后，每次使用yf指令Apex组队，都将查询你的战绩。重复运行将更新绑定的Origin ID。\n\n" +
                        "**发送Apex组队邀请：**`yf apex 你想说的`\n指令里`你想说的`可随意输入。" +
                        "如果使用这条指令的时候你在妖风电竞的某个语音频道中，组队邀请会自带上车链接，方便其他玩家点击进入。\n\n" +
                        "**发送组队邀请：**`yf invite 你想说的`\n这个指令可用于非Apex游戏，同样可以自动生成邀请链接。\n\n" +
                        "**----------------------------------------------生日----------------------------------------------**\n" +
                        "**注册生日：**`yf birthday MM-DD`\n注册你的生日。注册后生日当天会在生日频道得到祝福以及专属Tag。\n\n" +
                        "**取消生日提醒：**`yf birthday disable`\n" +
                        "**查看全部注册的生日：**`yf birthday`\n" +
                        "**查看本月过生日的成员：**`yf birthday month`\n\n" +
                        "**----------------------------------------------唱歌----------------------------------------------**\n" +
                        "**唱歌：**`yf play 关键字或者youtube歌曲链接`\n把歌曲加入播放队列。 如果当前队列中无歌曲，直接开始播放。\n\n" +
                        "**循环当前歌曲：**`yf loop`\n循环播放正在播放的歌曲。再次运行这个指令或者使用`yf skip`取消循环。\n\n" +
                        "**显示当前播放队列：**`yf queue`\n" +
                        "**跳过当前正在播放的歌曲：**`yf skip`\n" +
                        "**停止播放并清空播放队列：**`yf stop`\n\n" +
                        "**----------------------------------------------其他----------------------------------------------**\n" +
                        "**太牛了：**`yf nb @某人`\n被@的人太强了！如果要夸自己，可以省略@，直接使用`yf nb`\n\n")
                .build()).queue();
    }

    public void nb(MessageChannel channel, Member member, List<Member> mentions) {
        String userId = mentions.size() > 0 ? mentions.get(0).getId() : member.getId();
        channel.sendMessage("<@" + userId + "> " + nbList.get(random.nextInt(nbList.size()))).queue();
    }
}
