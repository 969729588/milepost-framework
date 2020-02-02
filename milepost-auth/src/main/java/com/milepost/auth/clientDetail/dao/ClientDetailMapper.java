package com.milepost.auth.clientDetail.dao;

import com.milepost.auth.clientDetail.entity.ClientDetail;
import com.milepost.auth.clientDetail.entity.ClientDetailExample;
import com.milepost.service.mybatis.dao.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by Ruifu Hua on 2020/2/1.
 */
@Mapper
public interface ClientDetailMapper extends BaseMapper<ClientDetail, ClientDetailExample>{

}
