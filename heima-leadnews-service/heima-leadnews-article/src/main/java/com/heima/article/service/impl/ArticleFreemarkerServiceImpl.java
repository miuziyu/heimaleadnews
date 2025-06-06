package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.search.vos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {
    /**
     * @param apArticle
     * @param content
     */

    @Autowired
    private Configuration configuration;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private ApArticleService apArticleService;
    //生成静态文件上传到minIO中
    @Async
    @Override
    public void buildArticleToMinIO(ApArticle apArticle, String content) {
        if(StringUtils.isNotBlank(content))
        {
            // 1.获取模板
            Template template = null;
            StringWriter out = new StringWriter();
            try {

                template = configuration.getTemplate("article.ftl");
                Map<String,Object> contentDateaModel = new HashMap<>();
                contentDateaModel.put("content", JSONArray.parseArray(content));
                template.process(contentDateaModel,out);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //将文件上传到minio
            InputStream in = new ByteArrayInputStream(out.toString().getBytes());
            String path = fileStorageService.uploadHtmlFile("",apArticle.getId() + ".html", in);
            // 2.将文件路径保存到数据库
            apArticleService.update(Wrappers.<ApArticle>lambdaUpdate()
                    .eq(ApArticle::getId,apArticle.getId())
                    .set(ApArticle::getStaticUrl, path));

            SendmessagetoSearch(apArticle,content,path);

        }

    }

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    private void SendmessagetoSearch(ApArticle apArticle, String content, String path) {
        SearchArticleVo searchArticleVo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle,searchArticleVo);
        searchArticleVo.setContent(content);
        searchArticleVo.setStaticUrl(path);
        kafkaTemplate.send(ArticleConstants.ARTICLE_ES_SYNC_TOPIC, JSON.toJSONString(searchArticleVo));

    }
}
