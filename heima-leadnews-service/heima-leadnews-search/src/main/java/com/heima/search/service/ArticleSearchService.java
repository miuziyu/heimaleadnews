package com.heima.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dto.UserSearchDto;

import java.io.IOException;

public interface ArticleSearchService {
    public ResponseResult search(UserSearchDto userSearchDto) throws IOException;

}
