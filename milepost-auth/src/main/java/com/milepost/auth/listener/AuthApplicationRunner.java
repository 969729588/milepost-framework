package com.milepost.auth.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.milepost.auth.clientDetail.entity.ClientDetails;
import com.milepost.auth.clientDetail.service.ClientDetailsService;
import com.milepost.core.multipleTenant.MultipleTenantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AuthApplicationRunner implements ApplicationRunner {

    private Logger logger = LoggerFactory.getLogger(AuthApplicationRunner.class);

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private ClientDetails clientDetailsProperties;

    @Autowired
    private MultipleTenantProperties multipleTenantProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //插入client数据
        insertOrUpdateClient();
    }

    /**
     * 插入/更新client数据
     * clientId = "client_id_" + tenant;
     * clientSecret = "client_secret_" + tenant;
     * 当client数据不存在时候插入，否则更新
     */
    private void insertOrUpdateClient() throws Exception {
        String tenant = multipleTenantProperties.getTenant();
        String clientId = "client_id_" + tenant;
        String clientSecret = "client_secret_" + tenant;

        clientDetailsProperties.setClientId(clientId);
        clientDetailsProperties.setClientSecret(clientSecret);

        ClientDetails clientDetails = clientDetailsService.getOne(new QueryWrapper<ClientDetails>()
                .eq("client_id", clientId)
                .eq("client_secret", clientSecret));
        if(clientDetails == null){
            boolean saveSuccess = clientDetailsService.save(clientDetailsProperties);
            //effectRow = clientDetailService.insert(clientDetailsProperties);
            if(saveSuccess){
                logger.info("认证数据库中不存在租户["+ tenant +"]的客户端数据，初始化客户端数据成功。");
            }else{
                logger.error("认证数据库中不存在租户["+ tenant +"]的客户端数据，初始化客户端数据失败。");
            }
        }else{
            boolean updateSuccess = clientDetailsService.updateById(clientDetailsProperties);
            //effectRow = clientDetailService.updateByPrimaryKey(clientDetailsProperties);
            if(updateSuccess){
                logger.info("认证数据库存中在租户["+ tenant +"]的客户端数据，更新客户端数据成功。");
            }else{
                logger.error("认证数据库存中在租户["+ tenant +"]的客户端数据，更新客户端数据失败。");
            }
        }
    }
}
