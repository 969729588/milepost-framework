package com.milepost.auth.clientDetail.service;

import com.milepost.auth.clientDetail.dao.ClientDetailMapper;
import com.milepost.auth.clientDetail.entity.ClientDetail;
import com.milepost.auth.clientDetail.entity.ClientDetailExample;
import com.milepost.service.mybatis.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Ruifu Hua on 2020/2/1.
 */
@Service
public class ClientDetailService extends BaseService<ClientDetail, ClientDetailExample>{
    @Autowired
    private ClientDetailMapper clientDetailMapper;


}
