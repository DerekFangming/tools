package com.tools.service;

import com.tools.dao.PostRepo;
import com.tools.domain.Post;
import com.tools.type.HtmlReaderType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class PostService {

    private String matcher;
    private String rootUrl;
    public static final String imgDir = System.getenv("imgDir");

    private static CloseableHttpClient httpClient;
    private static final String httpAgent = "Mozilla/5.0 (Platform; Security; OS-or-CPU; Localization; rv:1.4) Gecko/20030624 Netscape/7.1 (ax)";

    private static final List<Integer> CATEGORIES = Arrays.asList(798);
    private static final int PAGE_READ_PER_CATEGORY = 2;
    private static final int DAYS_TO_KEEP_POST = 14;

    private AtomicBoolean loadingPosts = new AtomicBoolean(false);

    private final PostRepo postRepo;
    private final QueryService queryService;

    @PostConstruct
    public void init() {
        matcher = System.getenv("matcher");

        String urlToken = System.getenv("urlToken");
        rootUrl = new String(Base64.decodeBase64(urlToken.getBytes()));

        System.setProperty("http.agent", httpAgent);
        try {
            SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(
                    SSLContexts.custom().loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true).build(),
                    NoopHostnameVerifier.INSTANCE);
            httpClient = HttpClients.custom().setSSLSocketFactory(sslFactory).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPostUrl(int id) {
        return rootUrl + "thread-" + id + "-1-1.html";
    }

    public void cleanupViewedPosts() {

        List<Post> postList = postRepo.findByViewedLessThan(Instant.now().minus(DAYS_TO_KEEP_POST, ChronoUnit.DAYS));
        for (Post p : postList) {
            File folder = new File(imgDir + p.getId());
            folder.delete();
            postRepo.delete(p);
        }
    }

    public void loadPosts() {
        if (loadingPosts.get()) return;
        loadingPosts.set(true);

        try {
            for (Integer category : CATEGORIES) {
                for (int page = 1; page <= PAGE_READ_PER_CATEGORY; page++) {
                    System.out.println("PAGE: " + page);
                    Post pagePost = Post.builder()
                            .url(rootUrl + "forum-" + category + "-" + page + ".html")
                            .build();

                    downloadHtml(pagePost);

                    if (page == 1) {
                        AtomicInteger rank = new AtomicInteger(1);
                        Matcher postMatcher = Pattern.compile("(<li><em>[\\s\\S]*?)<\\/ul>").matcher(pagePost.getHtml());
                        while (postMatcher.find()) {
                            Post highlightedPost = Post.builder().html(postMatcher.group(0)).build();
                            getElementsByTag(highlightedPost, "a").forEach(p -> {
                                Post post = Post.builder().title(p.html()).category(category).rank(rank.get()).created(Instant.now()).build();
                                processPost(post, p.attr("href"));
                            });
                            rank.incrementAndGet();
                        }
                    }

                    Elements anchorElements = getElementsByTag(pagePost, "a");
                    for (Element e : anchorElements) {
                        if (e.hasClass("s xst")) {
                            Post post = Post.builder().title(e.html()).category(category).created(Instant.now()).firstPage(page == 1).build();
                            boolean createdNewPost = processPost(post, e.attr("href"));
                            if (!createdNewPost) return;
                        }
                    }
                }
            }
        } finally {
            loadingPosts.set(false);
        }
    }

    private boolean processPost(Post post, String href) {
        String[] parts = href.split("-");
        if (parts.length == 4) {
            int id = Integer.parseInt(parts[1]);
            Optional<Post> existingPostOpt = postRepo.findById(id);
            if (existingPostOpt.isPresent()) {
                Post existingPost = existingPostOpt.get();
                if (existingPost.getRank() == 0 && post.getRank() == 0 && !post.isFirstPage()) {
                    return false;
                } else if (existingPost.getRank() != post.getRank()) {
                    existingPost.setRank(post.getRank());
                    postRepo.save(existingPost);
                }
                return true;
            }
            post.setId(id);
            downloadImageFromPost(post);
            setExtraAttributes(post);
        } else {
            post.setId(queryService.getNextQuerySequence());
            post.logException("Post url format err: " + href);
        }

        postRepo.save(post);
        return true;
    }

    private void downloadImageFromPost(Post post){
        String folderPath = imgDir + post.getId() + "/";
        new File(folderPath).mkdirs();
        post.setUrl(rootUrl + "thread-" + post.getId() + "-1-1.html");
        downloadHtml(post);

        try {
            getElementsByTag(post, "img").forEach(e -> {
                if ((e.hasAttr("id") && e.attr("id").startsWith("aimg_")) || e.hasAttr("lazyloadthumb") ||
                        (e.hasAttr("src") && e.attr("alt").startsWith("title"))) {
                    String imgUrl;
                    if (e.hasAttr("file")) {
                        imgUrl = e.attr("file");
                    } else if (e.hasAttr("src")) {
                        imgUrl = e.attr("src");
                        if (imgUrl.startsWith("forum.php")) {
                            imgUrl = rootUrl + imgUrl;
                        }
                    } else {
                        return;
                    }
                    post.logImageUrl(imgUrl);

                    String extension = FilenameUtils.getExtension(imgUrl);
                    if (StringUtils.isEmpty(extension)) extension = "jpg";
                    String fileName = folderPath + UUID.randomUUID().toString() + "." + extension;

                    try {
                        BufferedImage img = ImageIO.read(new URL(imgUrl));
                        if (img == null) throw new NullPointerException();
                        File file = new File(fileName);
                        ImageIO.write(img, extension, file);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        HttpGet httpGet = new HttpGet(imgUrl);
                        httpGet.setHeader("User-Agent", httpAgent);
                        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                            HttpEntity entity = response.getEntity();
                            FileOutputStream outstream = new FileOutputStream(new File(fileName));
                            entity.writeTo(outstream);
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                }
            });

            if (post.getImgUrls() == null && post.getHtmlType() == HtmlReaderType.JSOUP) {
                post.setHtmlType(HtmlReaderType.IOUTIL);
                Thread.sleep(500);
                downloadImageFromPost(post);
            }
        } catch (Exception e) {
            post.logException(e);
        }
    }

    private void setExtraAttributes(Post post) {
        // Find attachment
        Matcher attachmentMatcher = Pattern.compile("\"forum\\.php\\?mod=attachment.*?\"").matcher(post.getHtml());
        while (attachmentMatcher.find()) {
            String attachment = attachmentMatcher.group().replaceAll("\"", "");
            if (!attachment.endsWith("nothumb=yes")) {
                post.setAttachment(rootUrl + attachment.replaceAll("amp;", ""));
                break;
            }
        }

        // Check flag
        Matcher flagMatcher = Pattern.compile(matcher).matcher(post.getTitle());
        post.setFlagged(flagMatcher.find());
    }

    private void downloadHtml(Post post) {
        if (post.getHtmlType() == null) post.setHtmlType(HtmlReaderType.JSOUP);

        try {
            if (post.getHtmlType() == HtmlReaderType.JSOUP) {
                Document doc = Jsoup.connect(post.getUrl()).get();
                post.setHtml(doc.toString());
            } else {
                post.setHtml(IOUtils.toString(new URL(post.getUrl()), StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            post.logException(e);
            if (post.getHtmlType() == HtmlReaderType.JSOUP) {
                post.setHtmlType(HtmlReaderType.IOUTIL);
                downloadHtml(post);
            }
        }
    }

    private Elements getElementsByTag(Post post, String tag) {
        if (post.getHtmlType() == HtmlReaderType.JSOUP) {
            return Jsoup.parse(post.getHtml()).getElementsByTag(tag);
        } else {
            StringBuilder matchedTags = new StringBuilder();
            Matcher matcher = Pattern.compile(tag.equals("img") ? "<" + tag + ".*?>" : "<" + tag + ".*?" + tag + ">").matcher(post.getHtml());
            while (matcher.find()) {
                matchedTags.append(matcher.group());
            }
            return Jsoup.parse(matchedTags.toString()).getElementsByTag(tag);
        }
    }

}
