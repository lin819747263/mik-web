package com.mik.oauth;

import com.mik.oauth.enetity.Client;
import com.mik.oauth.enetity.ClientMapper;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ClientService extends ServiceImpl<ClientMapper, Client> {

    public Client findByClientId(String clientId) {
        QueryCondition condition =  QueryCondition.create(new QueryColumn("client_id"), "=", clientId);
        return getMapper().selectOneByCondition(condition);
    }

}
