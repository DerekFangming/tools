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
import java.util.stream.Collectors;

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
        add("woc牛逼！");
        add("芜湖 起飞");
        add("这么恐怖的吗 兄弟");
        add("这就是大腿吗");
        add("打得好啊");
    }};

    private Random random = new Random();

    public void help(MessageChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("妖风电竞 bot指令")
                .setDescription(
                        "**----------------------------------------------组队----------------------------------------------**\n" +
                        "**组队命令详细说明：**`yf help invite` or `yf h i`\n" +
                        "**绑定或更新Origin ID：**`yf apex link 你的ID` or `yf a l 你的ID`\n" +
                        "**查看别人绑定的Origin ID：**`yf apex id @某人` or `yf a i @某人`\n" +
                        "**发送Apex组队邀请：**`yf apex 你想说的` or `yf a 你想说的`\n" +
                        "**发送组队邀请：**`yf invite 你想说的` or `yf i 你想说的`\n" +
                        "**----------------------------------------------Tag----------------------------------------------**\n" +
                        "**Tag命令详细说明：**`yf help tag` or `yf h t`\n" +
                        "**创建或更新等级Tag：**`yf tag 颜色 名字` or `yf t 颜色 名字`\n" +
                        "**创建或更新Booster专属Tag：**`yf tag boost 颜色 名字` or `yf t b 颜色 名字`\n" +
                        "**分享自己的等级Tag给别人：**`yf tag share @某人` or `yf t s @某人`\n" +
                        "**向别人请求他的等级Tag：**`yf tag request @某人` or `yf t r @某人`\n" +
                        "**同意Tag请求：**`yf tag confirm 代码` or `yf t c 代码`\n" +
                        "**查看自己的Tag：**`yf tag` or `yf t`\n" +
                        "**查看可删除的Tag：**`yf tag delete` or `yf t d`\n" +
                        "**删除指定Tag：**`yf tag delete 代码` or `yf t d 代码`\n" +
                        "**----------------------------------------------频道----------------------------------------------**\n" +
                        "**频道命令详细说明：**`yf help channel` or `yf h c`\n" +
                        "**创建或更新临时频道：**`yf channel 名字` or `yf c 名字`\n" +
                        "**创建或更新Booster私人频道：**`yf channel boost 名字` or `yf c b 名字`\n" +
                        "**查看你的频道：**`yf channel` or `yf c`\n" +
                        "**删除你的临时频道：**`yf channel delete` or `yf c d`\n" +
                        "**删除你的私人频道：**`yf channel boost delete` or `yf c b d`\n"+
                        "**----------------------------------------------生日----------------------------------------------**\n" +
                        "**注册生日并得到专属Tag及生日祝福：**`yf birthday MM-DD` or `yf b MM-DD`\n" +
                        "**查看全部注册的生日：**`yf birthday` or `yf b`\n" +
                        "**取消生日提醒：**`yf birthday delete` or `yf b d`\n" +
                        "**查看本月过生日的成员：**`yf birthday month` or `yf b m`\n" +
                        "**查看某人注册的生日：**`yf birthday @某人` or `yf b @某人`\n" +
                        "**----------------------------------------------唱歌----------------------------------------------**\n" +
                        "**唱歌：**`yf play 关键字或者youtube歌曲链接` or `yf p 关键字`\n" +
                        "**循环当前歌曲：**`yf loop` or `yf l`\n" +
                        "**显示当前播放队列：**`yf queue` or `yf q`\n" +
                        "**跳过当前正在播放的歌曲：**`yf skip` or `yf s`\n" +
                        "**停止播放并清空播放队列：**`yf stop`\n" +
                        "**----------------------------------------------其他----------------------------------------------**\n" +
                        "**太牛了：**`yf nb @某人` or `yf n @某人`")
                .build()).queue();
    }

    public void helpInvite(MessageChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("妖风电竞 bot组队指令")
                .setDescription(
                        "**绑定或更新Origin ID：**`yf apex link 你的ID` or `yf a l 你的ID`\n绑定或更新Origin ID。" +
                        "绑定你的Apex账号。绑定之后，每次使用yf组队，bot都会在Apex tacker上查询你当前的战绩并将他们放在组队邀请里，便于" +
                        "找到水平相当的玩家。重复使用该命令就会更新的绑定的Origin ID。\n\n" +
                        "**查看别人绑定的Origin ID：**`yf apex id @某人` or `yf a i @某人`\n" +
                        "查看被@的人所绑定的Apex Id。 Credit to KingJz1997。\n\n" +
                        "**发送Apex组队邀请：**`yf apex 你想说的` or `yf a 你想说的`\n" +
                        "发送组队邀请。 如果在发送之前你已经通过bot绑定了你的Origin ID， 组队邀请上就会带有你当前的战绩。 如果你在妖风电竞的某个语音频道中" +
                        "使用这条指令，组队邀请会带有上车链接，方便其他玩家点击加入你当前所在的语音频道。\n\n" +
                        "**发送组队邀请：**`yf invite 你想说的` or `yf i 你想说的`\n这个指令可用于非Apex游戏，或者你只是想生成你当前所在的" +
                        "语音频道的上车链接，方便其他玩家加入。")
                .build()).queue();
    }

    public void helpTag(MessageChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("妖风电竞 bot tag指令")
                .setDescription(
                        "**创建或更新等级Tag：**`yf tag 颜色 名字` or `yf t 颜色 名字`\n在我们频道达到指定等级之后，你就可以创建你自己的等级Tag。" +
                        "等级Tag可以通过bot分享给别人别人。颜色代码是六位hex，你可以去[这个网站上](https://htmlcolorcodes.com/color-picker/)随意选择hex颜色。" +
                        "重复使用这条指令就可以更新等级Tag的名字和颜色。\n\n" +
                        "**创建或更新Booster专属Tag：**`yf tag boost 颜色 名字` or `yf t b 颜色 名字`\n专属tag只有Server Booster才可以创建。专属tag的拥有者" +
                        "会在右侧用户列表里有自己独立的分组。Booster tag和等级tag一样可以随时更新。因为Booster tag的排名比较靠前，你可以通过拥有Booster tag来" +
                        "随时改变你名字的颜色。\n\n" +
                        "**分享自己的等级Tag给别人：**`yf tag share @某人` or `yf t s @某人`\n当你创建了等级Tag之后，可以通过这条命令把你的等级Tag分享给被@的人。" +
                        "被@的人会受到一条验证码。他必须使用验证命令，分享才会成功。\n\n" +
                        "**向别人请求他的等级Tag：**`yf tag request @某人` or `yf t r @某人`\n你可以随时向拥有等级Tag的人索要他的等级tag。同理，被@的人需要输入" +
                        "验证命令，你才会得到他的等级tag。\n\n" +
                        "**同意Tag请求：**`yf tag confirm 代码` or `yf t c 代码`\n使用这个命令来同意tag分享或者索要请求。只有被@的人才能使用验证码。如果你不想" +
                        "同意@你的请求，可以无视，不发送这个同意指令。\n\n" +
                        "**查看自己的Tag：**`yf tag` or `yf t`\n查看当前的tag情况。这会列出你当前的等级tag，booster tag，别人分享给你的全部等级tag，以及你的" +
                        "等级tag分享给别人的列表。\n\n" +
                        "**查看可删除的Tag：**`yf tag delete` or `yf t d`\n查看你可以删除的tag。这个指令还会告诉你删除每个tag所需要的验证码。 \n\n" +
                        "**删除指定Tag：**`yf tag delete 代码` or `yf t d 代码`\n删除验证码所对应的tag。")
                .build()).queue();
    }

    public void helpChannel(MessageChannel channel) {
        channel.sendMessage(new EmbedBuilder()
                .setTitle("妖风电竞 bot频道指令")
                .setDescription(
                        "**创建或更新临时频道：**`yf channel 名字` or `yf c 名字`\n在我们频道达到5级之后，你就可以创建临时频道。" +
                                "比如在现有频道已满或者你想和朋友玩一些我们没有提前设定频道的游戏，比如Dota，CSGO和坦克世界，或者你只是需要一个临时频道，这个指令将会" +
                                "为你创建频道并生成频道链接，方便其他人加入。重复使用这条指令就可以更新临时频道的名字。如果一段时间之后临时频道里面没有人的话，" +
                                "频道将会被自动删除。删除之后你可以随时重新创建临时频道。\n\n" +
                                "**创建或更新Booster私人频道：**`yf channel boost 名字` or `yf c b 名字`\n私人频道只有Server Booster才可以创建。私人频道的主人" +
                                "拥有你自己的私人频道的全部管理权。创建之后，你可以自行设置权限，允许进入频道的成员等等。\n\n" +
                                "**查看你的频道：**`yf channel` or `yf c`\n查看你当前的频道情况。\n\n" +
                                "**删除你的临时频道：**`yf channel delete` or `yf c d`\n如果你只是想改变名字，重复创建命令即可，不需要删除再重新创建。\n\n" +
                                "**删除你的私人频道：**`yf channel boost delete` or `yf c b d`\n同理，如果你只是想改变名字，重复创建命令即可，不需要删除再重新创建。")
                .build()).queue();
    }

    public void nb(MessageChannel channel, Member member, List<Member> mentions) {
        if (mentions.size() == 0) {
            channel.sendMessage("<@" + member.getId() + "> " + nbList.get(random.nextInt(nbList.size()))).queue();
        } else {
            String allMentions = mentions.stream().map(m -> "<@" + m.getId() + ">")
                    .collect(Collectors.joining(" "));
            channel.sendMessage(allMentions + " " + nbList.get(random.nextInt(nbList.size()))).queue();
        }
    }
}
