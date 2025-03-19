package com.fis.epm.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.fis.pg.epm.models.PaymentChannelConfigRedisModel;

@Repository
public interface PaymentChanelConfigRedisRepo extends CrudRepository<PaymentChannelConfigRedisModel, Long>{
	List<PaymentChannelConfigRedisModel> findByUserName(String userName);
}
