package com.fis.epm.task;

import com.fis.epm.service.LoadCacheRamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class LoadCache {
    @Autowired
    private LoadCacheRamService loadCacheRamService;

    @PostConstruct
    public void LoadCache() {
        loadCacheRamService.loadCacheRam();
    }
}
