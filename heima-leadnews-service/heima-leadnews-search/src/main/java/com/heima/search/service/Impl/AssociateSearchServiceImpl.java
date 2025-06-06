package com.heima.search.service.Impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dto.UserSearchDto;
import com.heima.search.pojos.ApAssociateWords;
import com.heima.search.service.AssociateSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AssociateSearchServiceImpl implements AssociateSearchService {
    /**
     * @param userSearchDto
     * @return
     */
    @Autowired
    MongoTemplate mongoTemplate;
    @Override
    public ResponseResult associateSearch(UserSearchDto userSearchDto) {
        //参数检验
        if(userSearchDto.getSearchWords() == null)
        {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //限制分页数量
        if(userSearchDto.getPageSize() > 20)
        {
            userSearchDto.setPageSize(20);
        }
        //指定查询query
        int pageSize = userSearchDto.getPageSize();
        Query query = Query.query(Criteria.where("associateWords").regex(".*?\\" + userSearchDto.getSearchWords() + ".*"));
        query.limit(userSearchDto.getPageSize());
        List<ApAssociateWords> apAssociateWords = mongoTemplate.find(query, ApAssociateWords.class);


        return ResponseResult.okResult(apAssociateWords);
    }
}
