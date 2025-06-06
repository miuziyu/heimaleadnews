package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.mysql.fabric.xmlrpc.base.Array;
import com.zaxxer.hikari.util.FastList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {
    /**
     * @param id
     */
    @Autowired
    WmNewsMapper wmNewsMapper;

    @Override
    @Async
    public void autoScanWmNews(Integer id) {
        //1.查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }
        if (wmNews.getStatus().equals((WmNews.Status.SUBMIT.getCode()))) {
            //提取出image和content
            Map<String, Object> textandimages = handletextandimage(wmNews);
            Object raw = textandimages.get("content");
            System.out.println("content: " + raw);
            System.out.println("type: " + (raw == null ? "null" : raw.getClass().getName()));
            boolean isSensitiveScan = handlesensitiveScan((String)textandimages.get("content"),wmNews);
            //审核文字
            if(!isSensitiveScan)
            {
                return;
            }
            boolean isTextScan = handletextScan((String)textandimages.get("content"),wmNews);
            //审核文字
            if(!isTextScan)
            {
                return;
            }
            //审核图片
            boolean isImagesScan = handleimagesScan((List<String>)textandimages.get("images"),wmNews);
            if(!isImagesScan)
            {
                return;
            }
            //审核通过保存wmnews
            ResponseResult result = SaveWmnews(wmNews);
            if(!result.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
            }
            //把articleid回填到wmnews里面
            wmNews.setArticleId((long)result.getData());
            updateWmnews(wmNews,(short)9,"申请成功");
        }


    }
    @Autowired
    WmSensitiveMapper wmSensitiveMapper;
    private boolean handlesensitiveScan(String content, WmNews wmNews) {
       boolean flag = true;
       //查询敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery().select(WmSensitive::getSensitives));
        List<String> sensitiveList
                = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        //初始化敏感词库
        SensitiveWordUtil.initMap(sensitiveList);
        //查看文章中是否存在敏感词
        Map<String,Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size() > 0 )
        {
            updateWmnews(wmNews,(short) 2,"当前文章中存在违规内容"+map);
            flag = false;
        }

        return flag;



    }


    private void updateWmnews(WmNews wmNews, short statue, String reason ) {
        wmNews.setStatus(statue);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }



    //保存app端的文章数据
    @Autowired
    private  IArticleClient articleClient;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmUserMapper wmUserMapper;

    private ResponseResult SaveWmnews(WmNews wmNews) {
        ArticleDto dto = new ArticleDto();
        //拷贝大部分数据
        BeanUtils.copyProperties(wmNews,dto);
        dto.setLayout(wmNews.getType());
        //拷贝channlename和作者id,作者name,文章id
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(wmChannel != null)
        {
            dto.setChannelName(wmChannel.getName());
        }

        dto.setAuthorId((long)wmNews.getUserId());
        WmUser user = wmUserMapper.selectById(wmNews.getUserId());

        if(user!=null)
        {
            dto.setAuthorName(user.getName());
        }
        if(wmNews.getArticleId() != null)
        {
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());
        ResponseResult responseResult = articleClient.saveArticle(dto);
        return responseResult;





        //保存


    }

    @Autowired
    private Tess4jClient tess4jClient;
    @Autowired
    private FileStorageService fileStorageService;
    private boolean handleimagesScan(List<String> images, WmNews wmNews) {
        boolean flag = true;
        if (images == null || images.size() == 0) {
            return true;
        }

        List<byte[]> imagesList = new ArrayList<>();
        images = images.stream().distinct().collect(Collectors.toList());

        try {
            for (String image : images)
                {
                    byte[] bytes = fileStorageService.downLoadFile(image);//下载图片
                    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                    BufferedImage imageFile = ImageIO.read(in);
                    String result = tess4jClient.doOCR(imageFile);//提取出文字
                    if(!handlesensitiveScan(result,wmNews))
                    {
                        flag = false;
                        return flag;
                    }
                    imagesList.add(bytes);
                }

            }
        catch (Exception e)
            {
                e.printStackTrace();
            }




       
        return true;
    }

    private boolean handletextScan(String content, WmNews wmNews) {
        return true;
    }

    //从content中提取图片和文章
    //从cover中提取图片
    private Map<String, Object> handletextandimage(WmNews wmNews) {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> images = new ArrayList<>();
        if (StringUtils.isNotBlank(wmNews.getContent())) {

            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);
            for (Map map : maps) {
                if (map.get("type").equals("text")) {
                    stringBuilder.append(map.get("value"));
                }
                if (map.get("type").equals("image")) {
                    images.add((String) map.get("value"));
                }
            }
        }
        if (StringUtils.isNotBlank(wmNews.getImages())) {
                String[] split = wmNews.getImages().split(",");
                images.addAll(Arrays.asList(split));
            }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("content", stringBuilder.toString());
        resultMap.put("images", images);
        return resultMap;
    }
}
