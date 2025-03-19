package com.fis.epm.service;

import com.fis.epm.entity.Partners;
import com.fis.pg.epm.models.PartnersRedisModel;

public interface PartnersService {
    public Partners findByPartnerId(Long partnerId);
    public Partners findByCode(String code);
    public PartnersRedisModel findRedisByPartnerId(Long partnerId);
}
