package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.UserSearchDto;

public interface AssociateSearchService {
    public ResponseResult associateSearch(UserSearchDto userSearchDto);
}
