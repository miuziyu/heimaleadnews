package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.UserSearchDto;
import com.heima.search.service.AssociateSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/associate")
public class AssociateSearchController {
    @Autowired
    private AssociateSearchService associateSearchService;

    @PostMapping("/search")
    public ResponseResult AssociateSearch(@RequestBody UserSearchDto userSearchDto)
    {
        ResponseResult responseResult = associateSearchService.associateSearch(userSearchDto);
        return responseResult;
    }
}
