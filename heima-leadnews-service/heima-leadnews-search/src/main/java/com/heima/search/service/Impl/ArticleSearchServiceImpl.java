package com.heima.search.service.Impl;

import com.alibaba.fastjson.JSON;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dto.UserSearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.service.ApUserSearchService;
import com.heima.search.service.ArticleSearchService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.xml.transform.Source;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class ArticleSearchServiceImpl implements ArticleSearchService {
    /**
     * @param userSearchDto
     * @return
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private ApUserSearchService apUserSearchService;
    @Override
    public ResponseResult search(UserSearchDto userSearchDto) throws IOException {

        //1.参数校验
        if (userSearchDto == null || StringUtils.isBlank(userSearchDto.getSearchWords())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser user = AppThreadLocalUtil.getUser();

        if(user != null && userSearchDto.getFromIndex() == 0)
        {
            apUserSearchService.insert(userSearchDto.getSearchWords(),user.getId());
        }

        //从es库中查询article
        SearchRequest searchRequest = new SearchRequest("app_info_article");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创造出bool查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(userSearchDto.getSearchWords())
                        .field("title")
                        .field("content")
                                .defaultOperator(Operator.OR);
        //将queryStringQueryBuilder添加到boolQueryBuilder中进行组合
        boolQueryBuilder.must(queryStringQueryBuilder);
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("publishTime")
                        .lt(userSearchDto.getMinBehotTime().getTime());

        //将rangeQueryBuilder添加到boolQueryBuilder中进行组合
        boolQueryBuilder.filter(rangeQueryBuilder);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<font style='color: red;font-size: inherit;'>");
        highlightBuilder.postTags("</font>");
        //将高亮查询放入查询器中
        searchSourceBuilder.highlighter(highlightBuilder);

        //将查询器进行分页
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(userSearchDto.getPageSize());
        //按时间倒叙查询
        searchSourceBuilder.sort("publishTime", SortOrder.DESC);
        //将布尔查询放入查询器中
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        //返回结果封装
        List<Map> list = new ArrayList<>();


        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            Map map = JSON.parseObject(json, Map.class);
            //处理高亮
            if(hit.getHighlightFields() != null && hit.getHighlightFields().size() > 0)
            {
                Text[] titles = hit.getHighlightFields().get("title").getFragments();
                String title = StringUtils.join(titles);
                map.put("h_title",title);

            }
            else {
                map.put("h_titlr",map.get("title"));
            }
            list.add(map);

        }
        return ResponseResult.okResult(list);
    }
}
