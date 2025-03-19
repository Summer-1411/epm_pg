package com.fis.epm.controller;

import com.fis.epm.entity.LogApiResult;
import com.fis.epm.service.MBMoneyService;
import com.fis.epm.utils.EPMBaseCommon;
import com.fis.epm.utils.EPMQueueManager;
import com.fis.epm.utils.Utils;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.gw.server.models.ResponseModel;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("MBFmoney")
public class EPMMobifoneMoneyController extends EPMBasicController {
    private static final Logger log = LoggerFactory.getLogger(EPMMobifoneMoneyController.class);
    @Autowired
    private MBMoneyService mbMoneyService;

    @SneakyThrows
    @PostMapping("/callback")
    public ResponseModel callback(@RequestBody Map map, HttpServletRequest request, HttpServletResponse response) {
        String url = "/MBFmoney/callback";
        String ipRequest = EPMBaseCommon.getIpAddress(request);
        ResponseModel res = mbMoneyService.callback(url, ipRequest, map);
        return res;
    }
}
