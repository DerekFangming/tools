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
                        "**绑定Origin ID：**`yf apex link 你的ID` or `yf a l 你的ID`\n绑定或更新Origin ID。" +
                        "绑定后，每次使用yf组队，都将查询你的战绩。\n\n" +
                        "**发送Apex组队邀请：**`yf apex 你想说的` or `yf a 你想说的`\n" +
                        "妖风电竞的某个语音频道中使用这条指令，组队邀请会自带上车链接。\n\n" +
                        "**发送组队邀请：**`yf invite 你想说的` or `yf i 你想说的`\n这个指令可用于非Apex游戏，同样可以自动生成邀请链接。\n\n" +
                        "**----------------------------------------------Tag----------------------------------------------**\n" +
                        "**创建或更新等级Tag：**`yf tag 颜色 名字` or `yf t 颜色 名字`\n达到指定等级才能创建等级Tag。颜色代码是六位hex。\n\n" +
                        "**创建或更新Booster专属Tag：**`yf tag boost 颜色 名字` or `yf t b 颜色 名字`\n专属tag有自己独立的名字分组。\n\n" +
                        "**分享自己的等级Tag给别人：**`yf tag share @某人` or `yf t s @某人`\n" +
                        "**向别人请求他的等级Tag：**`yf tag request @某人` or `yf t r @某人`\n" +
                        "**同意Tag请求：**`yf tag confirm 代码` or `yf t c 代码`\n" +
                        "**查看自己的Tag：**`yf tag` or `yf t`\n" +
                        "**查看可删除的Tag：**`yf tag delete` or `yf t d`\n" +
                        "**删除指定Tag：**`yf tag delete 代码` or `yf t d 代码`\n\n" +
                        "**----------------------------------------------生日----------------------------------------------**\n" +
                        "**注册生日：**`yf birthday MM-DD` or `yf b MM-DD`\n注册你的生日。注册后生日当天会在生日频道得到祝福以及专属Tag。\n\n" +
                        "**查看自己注册的生日：**`yf birthday` or `yf b`\n" +
                        "**取消生日提醒：**`yf birthday disable` or `yf b d`\n" +
                        "**查看本月过生日的成员：**`yf birthday month` or `yf b m`\n" +
                        "**查看某人注册的生日：**`yf birthday @某人` or `yf b @某人`\n\n" +
                        "**----------------------------------------------唱歌----------------------------------------------**\n" +
                        "**唱歌：**`yf play 关键字或者youtube歌曲链接` or `yf p 关键字`\n把歌曲加入播放队列。 如果当前队列中无歌曲，直接开始播放。\n\n" +
                        "**循环当前歌曲：**`yf loop` or `yf l`\n循环播放正在播放的歌曲。再次运行这个指令或者使用`yf skip`取消循环。\n\n" +
                        "**显示当前播放队列：**`yf queue` or `yf q`\n" +
                        "**跳过当前正在播放的歌曲：**`yf skip` or `yf s`\n" +
                        "**停止播放并清空播放队列：**`yf stop`\n\n" +
                        "**----------------------------------------------其他----------------------------------------------**\n" +
                        "**太牛了：**`yf nb @某人` or `yf n @某人`\n被@的人太强了！如果要夸自己，可以省略@，直接使用`yf nb`\n\n")
                .build()).queue();
    }

    public void nb(MessageChannel channel, Member member, List<Member> mentions) {
        String userId = mentions.size() > 0 ? mentions.get(0).getId() : member.getId();
        channel.sendMessage("<@" + userId + "> " + nbList.get(random.nextInt(nbList.size()))).queue();
    }
}
