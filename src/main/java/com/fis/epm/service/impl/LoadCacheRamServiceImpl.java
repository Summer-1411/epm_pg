package com.fis.epm.service.impl;

import com.fis.epm.entity.EpmUserPG;
import com.fis.epm.repo.EpmUserPGRepo;
import com.fis.epm.service.LoadCacheRamService;
import com.fis.epm.utils.EPMBaseCommon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class LoadCacheRamServiceImpl implements LoadCacheRamService {

    @Autowired
    private EpmUserPGRepo epmUserPGRepo;

    @Override
    public void loadCacheRam() {
        List<EpmUserPG> lstUser = epmUserPGRepo.findAllByActive();
        if (lstUser != null || lstUser.isEmpty()) {
            for (EpmUserPG user : lstUser)
                EPMBaseCommon.mapUserByService.put(user.getServiceCode(), user);
        }
    }
}
