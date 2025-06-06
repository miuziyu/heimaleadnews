package com.heima.search.service.Impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.pojos.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
@Transactional
public class ApUserSearchServiceImpl implements ApUserSearchService {
    /**
     * @param keyword
     * @param userId
     */
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * @return
     */
    @Override
    public ResponseResult findUserSearch() {
        ApUser user = AppThreadLocalUtil.getUser();
        if(user == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Query query1 = Query.query(Criteria.where("userId").is(user.getId()));
        query1.with(Sort.by(Sort.Direction.DESC,"createdTime"));
        List<ApUserSearch> apUserSearcheList = mongoTemplate.find(query1, ApUserSearch.class);
        return ResponseResult.okResult(apUserSearcheList);
    }

    /**
     * @param historySearchDto
     * @return
     */
    @Override
    public ResponseResult delUserSearch(HistorySearchDto historySearchDto) {
       //参数检验
        if(historySearchDto.getId()  == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //看用户是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if(user == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        Query query = Query.query(Criteria.where("Id").is(historySearchDto.getId()).and("userId").is(user.getId()));
        mongoTemplate.remove(query,HistorySearchDto.class);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    @Async
    public void insert(String keyword, Integer userId) {
        //查询当前用户的搜索关键词
        Query query = Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query,ApUserSearch.class);

        //如果已经有，则更新创造时间
        if(apUserSearch != null)
        {
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
            return;
        }
        //如果没有判断是否超过10个
        apUserSearch = new ApUserSearch();
        apUserSearch.setUserId(userId);
        apUserSearch.setKeyword(keyword);
        apUserSearch.setCreatedTime(new Date());

        Query query1 = Query.query(Criteria.where("userId").is(userId));
        query1.with(Sort.by(Sort.Direction.DESC,"createdTime"));
        List<ApUserSearch> apUserSearcheList = mongoTemplate.find(query1, ApUserSearch.class);
        if(apUserSearcheList != null && apUserSearcheList.size()<10)
        {
            mongoTemplate.save(apUserSearch);
        }
        else {
            //超过十个则替换
            ApUserSearch lastUserSearch = apUserSearcheList.get(apUserSearcheList.size() - 1);
            mongoTemplate.findAndReplace(Query.query(Criteria.where("Id").is(lastUserSearch.getId())), apUserSearch);
        }



    }
}
