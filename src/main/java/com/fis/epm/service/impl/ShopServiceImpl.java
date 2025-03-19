package com.fis.epm.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fis.epm.models.PosModel;
import com.fis.epm.repo.ShopRedisRepo;
import com.fis.epm.service.ShopService;
import com.fis.pg.epm.models.ShopRedisModel;
import com.google.common.base.Optional;

@Service
public class ShopServiceImpl implements ShopService{

	@Autowired
	private ShopRedisRepo shopRedisRepo;
	
	@Override
	public Iterable<ShopRedisModel> findAll() {
		// TODO Auto-generated method stub
		return shopRedisRepo.findAll();
	}

	@Override
	public List<PosModel> findAllPosRedisModel(String provinceCode) {
		List<ShopRedisModel> shopRedisModels = shopRedisRepo.findByProvinceCode(provinceCode);
		List<PosModel> posModels = new ArrayList<PosModel>();
		for(ShopRedisModel item:shopRedisModels){
			PosModel posModel = new PosModel();
			posModel.setShopAddress(item.getShopAddress());
			posModel.setShopCode(item.getShopCode());
			posModel.setShopId(item.getShopId());
			posModel.setShopName(item.getShopName());
			posModels.add(posModel);
		}
		return posModels;
	}

//	@Override
//	public ShopRedisModel findById(Long id) {
//	ShopRedisModel shopRedisModel = new ShopRedisModel();
//	java.util.Optional<ShopRedisModel> posModels = shopRedisRepo.findById(id);
//	shopRedisModel = (ShopRedisModel)posModels.get();
//	return shopRedisModel;
//	}
	@Override
	public ShopRedisModel findById(Long id) {
		java.util.Optional<ShopRedisModel> posModels = shopRedisRepo.findById(id);

		if (posModels.isPresent()) {
			return posModels.get(); // Nếu có giá trị, trả về
		} else {
			// Nếu không tìm thấy giá trị, có thể xử lý theo cách khác hoặc trả về giá trị mặc định
			return null; // Hoặc tạo đối tượng mặc định
		}
	}

}
