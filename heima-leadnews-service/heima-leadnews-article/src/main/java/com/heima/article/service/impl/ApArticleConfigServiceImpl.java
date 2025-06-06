package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.model.article.pojos.ApArticleConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@Transactional
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService{
    /**
     * 根据map上传
     */
    @Override
    public void updatebyMap(Map map) {
        boolean IsDown = false;
        if(map.get("enable").equals(0))
        {
            IsDown = true;
        }
        update(Wrappers.<ApArticleConfig>lambdaUpdate()
                .set(ApArticleConfig::getIsDown,IsDown)
                .eq(ApArticleConfig::getArticleId,map.get("articleId")));
    }

}
